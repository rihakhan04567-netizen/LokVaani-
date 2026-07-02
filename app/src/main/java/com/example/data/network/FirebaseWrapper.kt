package com.example.data.network

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * A production-ready, thread-safe service wrapper for Firebase Authentication,
 * Cloud Firestore, and Cloud Storage with offline configuration and optional emulator support.
 */
object FirebaseWrapper {
    private const val TAG = "FirebaseWrapper"

    // Flags for local emulator development / testing
    private var useEmulators = false
    private const val EMULATOR_HOST = "10.0.2.2" // Standard Android Emulator Loopback
    private const val AUTH_PORT = 9099
    private const val FIRESTORE_PORT = 8080
    private const val STORAGE_PORT = 9199

    @Volatile
    private var isInitialized = false

    // Cached service instances
    private var authInstance: FirebaseAuth? = null
    private var firestoreInstance: FirebaseFirestore? = null
    private var storageInstance: FirebaseStorage? = null

    /**
     * Safe initializer for the Firebase wrapper.
     * Can be invoked from the Application class or during lazy initialization of repositories.
     */
    @Synchronized
    fun initialize(context: Context, useEmulators: Boolean = false) {
        if (isInitialized) return
        this.useEmulators = useEmulators

        try {
            // Verify FirebaseApp is initialized
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }

            if (app != null) {
                Log.i(TAG, "Firebase successfully initialized.")
                setupAuth()
                setupFirestore()
                setupStorage()
                isInitialized = true
            } else {
                Log.e(TAG, "FirebaseApp initialization returned null.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Services failed to initialize. Ensure google-services.json is configured.", e)
        }
    }

    private fun setupAuth() {
        try {
            authInstance = FirebaseAuth.getInstance()
            if (useEmulators) {
                authInstance?.useEmulator(EMULATOR_HOST, AUTH_PORT)
                Log.i(TAG, "Auth configured to use local Emulator on port $AUTH_PORT")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Firebase Auth", e)
        }
    }

    private fun setupFirestore() {
        try {
            firestoreInstance = FirebaseFirestore.getInstance()

            // Configure modern production-ready Firestore local offline cache (Material 3 compliant)
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(100 * 1024 * 1024) // 100 MB cache limit
                        .build()
                )
                .build()
            firestoreInstance?.firestoreSettings = settings

            if (useEmulators) {
                firestoreInstance?.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
                Log.i(TAG, "Firestore configured to use local Emulator on port $FIRESTORE_PORT")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Cloud Firestore", e)
        }
    }

    private fun setupStorage() {
        try {
            storageInstance = FirebaseStorage.getInstance()
            if (useEmulators) {
                storageInstance?.useEmulator(EMULATOR_HOST, STORAGE_PORT)
                Log.i(TAG, "Storage configured to use local Emulator on port $STORAGE_PORT")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Cloud Storage", e)
        }
    }

    // ==========================================
    // --- Firebase Authentication APIs ---
    // ==========================================

    val auth: FirebaseAuth
        get() {
            if (authInstance == null) {
                Log.w(TAG, "Auth was not pre-initialized, accessing directly")
                authInstance = FirebaseAuth.getInstance()
            }
            return authInstance ?: throw IllegalStateException("Firebase Auth is not available")
        }

    val currentUser: FirebaseUser?
        get() = authInstance?.currentUser

    fun isUserLoggedIn(): Boolean = currentUser != null

    /**
     * Returns a Flow that emits the current state of the logged-in Firebase user
     * whenever they log in, log out, or change their credentials.
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        authInstance?.addAuthStateListener(listener)
        awaitClose {
            authInstance?.removeAuthStateListener(listener)
        }
    }

    /**
     * Anonymously authenticate. Excellent for seamless onboarding or demo content.
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("Signed in but user is null")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in using standard email and password.
     */
    suspend fun signInWithEmail(email: String, authCode: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, authCode).await()
            val user = result.user ?: throw Exception("Signed in but user is null")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed for $email", e)
            Result.failure(e)
        }
    }

    /**
     * Create user profile with email and password.
     */
    suspend fun signUpWithEmail(email: String, authCode: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, authCode).await()
            val user = result.user ?: throw Exception("Sign-up succeeded but user is null")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-up failed for $email", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out current session
     */
    fun signOut() {
        try {
            auth.signOut()
            Log.i(TAG, "Successfully logged out user.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out encountered an error", e)
        }
    }

    // ==========================================
    // --- Cloud Firestore APIs ---
    // ==========================================

    val db: FirebaseFirestore
        get() {
            if (firestoreInstance == null) {
                Log.w(TAG, "Firestore was not pre-initialized, accessing directly")
                firestoreInstance = FirebaseFirestore.getInstance()
            }
            return firestoreInstance ?: throw IllegalStateException("Cloud Firestore is not available")
        }

    /**
     * Set a document in a given collection.
     * Uses coroutine await to suspend cleanly.
     */
    suspend fun saveDocument(collection: String, docId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            db.collection(collection).document(docId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing document to Firestore: collection=$collection, id=$docId", e)
            Result.failure(e)
        }
    }

    /**
     * Get a specific document from a collection.
     */
    suspend fun getDocument(collection: String, docId: String): Result<Map<String, Any>?> {
        return try {
            val snapshot = db.collection(collection).document(docId).get().await()
            Result.success(snapshot.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading document from Firestore: collection=$collection, id=$docId", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time updates for a whole Firestore collection.
     * Converts Firestore listener callbacks into a modern Kotlin flow.
     */
    fun observeCollection(collection: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val registration = db.collection(collection).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Firestore subscription error on collection: $collection", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc -> doc.data }
                trySend(list)
            }
        }
        awaitClose {
            registration.remove()
        }
    }

    /**
     * Delete a specific document.
     */
    suspend fun deleteDocument(collection: String, docId: String): Result<Unit> {
        return try {
            db.collection(collection).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document: collection=$collection, id=$docId", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // --- Cloud Storage APIs ---
    // ==========================================

    val storage: FirebaseStorage
        get() {
            if (storageInstance == null) {
                Log.w(TAG, "Storage was not pre-initialized, accessing directly")
                storageInstance = FirebaseStorage.getInstance()
            }
            return storageInstance ?: throw IllegalStateException("Cloud Storage is not available")
        }

    /**
     * Upload raw byte array data directly to Cloud Storage.
     */
    suspend fun uploadFile(path: String, bytes: ByteArray, mimeType: String? = null): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            val metadataBuilder = com.google.firebase.storage.StorageMetadata.Builder()
            if (mimeType != null) {
                metadataBuilder.setContentType(mimeType)
            }
            ref.putBytes(bytes, metadataBuilder.build()).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Cloud Storage upload failed for path: $path", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve public file download URL from path.
     */
    suspend fun getDownloadUrl(path: String): Result<String> {
        return try {
            val url = storage.reference.child(path).downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Storage URL for path: $path", e)
            Result.failure(e)
        }
    }

    /**
     * Delete file from Cloud Storage.
     */
    suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            storage.reference.child(path).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting Storage file at path: $path", e)
            Result.failure(e)
        }
    }
}
