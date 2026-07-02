package com.example.ui.components

import android.content.Context
import android.content.Intent
import java.net.URLEncoder

object ShareUtils {
    fun shareAudioTrack(context: Context, id: String, title: String, artist: String) {
        val appLink = "lokvaani://track?id=$id"
        val webLink = "https://lokvaani.com/track?id=$id"
        
        val message = "🎵 Listen to \"$title\" by $artist on LokVaani! Region's Devout & Lore.\n\nOpen in App: $appLink\nWeb Link: $webLink"
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Track")
        context.startActivity(shareIntent)
    }

    fun shareAIStory(context: Context, title: String, content: String, dialect: String) {
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        val encodedContent = URLEncoder.encode(content, "UTF-8")
        val encodedDialect = URLEncoder.encode(dialect, "UTF-8")
        
        val appLink = "lokvaani://story?title=$encodedTitle&content=$encodedContent&dialect=$encodedDialect"
        val webLink = "https://lokvaani.com/story?title=$encodedTitle&content=$encodedContent&dialect=$encodedDialect"
        
        val message = "📖 Read this amazing LokVaani AI Story: \"$title\" in $dialect dialect!\n\nOpen in App: $appLink\nWeb Link: $webLink"
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share AI Story")
        context.startActivity(shareIntent)
    }

    fun shareProverb(context: Context, text: String, translation: String, meaning: String, dialect: String) {
        val message = "📖 LokVaani Proverb:\n\n\"$text\"\n\nTranslation: $translation\nMeaning: $meaning\nDialect: $dialect\n\nShared via LokVaani App."
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Proverb")
        context.startActivity(shareIntent)
    }
}
