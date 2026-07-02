package com.example.ui.components

object Localization {
    val languages = listOf(
        "English" to "English",
        "Hindi" to "हिन्दी (Hindi)",
        "Bhojpuri" to "भोजपुरी (Bhojpuri)",
        "Maithili" to "मैथिली (Maithili)",
        "Punjabi" to "ਪੰਜਾਬੀ (Punjabi)",
        "Tamil" to "தமிழ் (Tamil)",
        "Bengali" to "বাংলা (Bengali)",
        "Sanskrit" to "संस्कृतम् (Sanskrit)",
        "Marathi" to "मराठी (Marathi)"
    )

    private val translations = mapOf(
        "tab_home" to mapOf(
            "English" to "Home",
            "Hindi" to "मुख्य पृष्ठ",
            "Bhojpuri" to "घर",
            "Maithili" to "मुख्य",
            "Punjabi" to "ਘਰ",
            "Tamil" to "முகப்பு",
            "Bengali" to "বাড়ি",
            "Sanskrit" to "मुख्यम्",
            "Marathi" to "मुख्य"
        ),
        "tab_discover" to mapOf(
            "English" to "Discover",
            "Hindi" to "खोजें",
            "Bhojpuri" to "खोजल",
            "Maithili" to "खोज",
            "Punjabi" to "ਖੋਜੋ",
            "Tamil" to "கண்டறி",
            "Bengali" to "আবিষ্কার",
            "Sanskrit" to "अन्वेषणम्",
            "Marathi" to "शोध"
        ),
        "tab_ai_stories" to mapOf(
            "English" to "AI Stories",
            "Hindi" to "एआई कहानियां",
            "Bhojpuri" to "एआई कहानी",
            "Maithili" to "एआई कथा",
            "Punjabi" to "ਏਆਈ ਕਹਾਣੀਆਂ",
            "Tamil" to "AI கதைகள்",
            "Bengali" to "এআই গল্প",
            "Sanskrit" to "एआई कथाः",
            "Marathi" to "एआय कथा"
        ),
        "tab_community" to mapOf(
            "English" to "Community",
            "Hindi" to "समुदाय",
            "Bhojpuri" to "समाज",
            "Maithili" to "गोठ",
            "Punjabi" to "ਭਾਈਚਾਰਾ",
            "Tamil" to "சமூகம்",
            "Bengali" to "সম্প্রদায়",
            "Sanskrit" to "समुदायः",
            "Marathi" to "समुदाय"
        ),
        "tab_offline" to mapOf(
            "English" to "Library",
            "Hindi" to "पुस्तकालय",
            "Bhojpuri" to "लाइब्रेरी",
            "Maithili" to "पुस्तकालय",
            "Punjabi" to "ਲਾਇਬ੍ਰੇਰੀ",
            "Tamil" to "நூலகம்",
            "Bengali" to "গ্রন্থাগার",
            "Sanskrit" to "ग्रंथालयः",
            "Marathi" to "लायब्ररी"
        ),
        "tab_profile" to mapOf(
            "English" to "Profile",
            "Hindi" to "प्रोफ़ाइल",
            "Bhojpuri" to "खाता",
            "Maithili" to "प्रोफाइल",
            "Punjabi" to "ਪ੍ਰੋਫਾਈਲ",
            "Tamil" to "சுயவிவரம்",
            "Bengali" to "প্রোফাইল",
            "Sanskrit" to "विवरणम्",
            "Marathi" to "प्रोफाईल"
        ),
        "welcome_user" to mapOf(
            "English" to "Namaste Karan! 🙏",
            "Hindi" to "नमस्ते करण! 🙏",
            "Bhojpuri" to "प्रणाम करण! 🙏",
            "Maithili" to "प्रणाम करण! 🙏",
            "Punjabi" to "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ ਕਰਨ! 🙏",
            "Tamil" to "வணக்கம் கரண்! 🙏",
            "Bengali" to "নমস্কার করণ! 🙏",
            "Sanskrit" to "नमो नमः करण! 🙏",
            "Marathi" to "नमस्कार करण! 🙏"
        ),
        "daily_word" to mapOf(
            "English" to "Daily Word Accent",
            "Hindi" to "आज का शब्द",
            "Bhojpuri" to "आजु के शब्द",
            "Maithili" to "आइ क शब्द",
            "Punjabi" to "ਅੱਜ ਦਾ ਸ਼ਬਦ",
            "Tamil" to "இன்றைய வார்த்தை",
            "Bengali" to "আজকের শব্দ",
            "Sanskrit" to "अद्यतनशब्दः",
            "Marathi" to "आजचा शब्द"
        ),
        "daily_proverb" to mapOf(
            "English" to "Proverb of the Day",
            "Hindi" to "आज की कहावत",
            "Bhojpuri" to "आजु के कहावत",
            "Maithili" to "आइ क लोकोक्ति",
            "Punjabi" to "ਅੱਜ ਦਾ ਅਖਾਣ",
            "Tamil" to "இன்றைய பழமொழி",
            "Bengali" to "আজকের প্রবাদ",
            "Sanskrit" to "अद्यतनी सुभाषितम्",
            "Marathi" to "आजची म्हण"
        ),
        "featured_stories" to mapOf(
            "English" to "Featured Folklore",
            "Hindi" to "विशेष लोककथाएं",
            "Bhojpuri" to "खास लोककथा",
            "Maithili" to "विशेष लोककथा",
            "Punjabi" to "ਖਾਸ ਲੋਕ-ਕਹਾਣੀਆਂ",
            "Tamil" to "சிறப்பு நாட்டுப்புறக் கதைகள்",
            "Bengali" to "विशेष लोकকাহিনী",
            "Sanskrit" to "विशिष्टलोककथाः",
            "Marathi" to "वैशिष्ट्यपूर्ण लोककथा"
        ),
        "popular_dialects" to mapOf(
            "English" to "Popular Indian Dialects",
            "Hindi" to "लोकप्रिय भारतीय बोलियां",
            "Bhojpuri" to "प्रसिद्ध बोलियाँ",
            "Maithili" to "लोकप्रिय बोली",
            "Punjabi" to "ਪ੍ਰਸਿੱਧ ਬੋਲੀਆਂ",
            "Tamil" to "பிரபலமான வட்டார வழக்குகள்",
            "Bengali" to "জনপ্রিয় উপভাষা",
            "Sanskrit" to "प्रसिद्धभाषाः",
            "Marathi" to "लोकप्रिय बोली"
        ),
        "language_settings" to mapOf(
            "English" to "Interface Language",
            "Hindi" to "इंटरफ़ेस भाषा",
            "Bhojpuri" to "एप के भाषा",
            "Maithili" to "इंटरफेसक भाषा",
            "Punjabi" to "ਇੰਟਰਫੇਸ ਭਾਸ਼ਾ",
            "Tamil" to "இடைமுக மொழி",
            "Bengali" to "ইন্টারফেস ভাষা",
            "Sanskrit" to "माध्यमभाषा",
            "Marathi" to "इंटरफेस भाषा"
        ),
        "choose_lang_desc" to mapOf(
            "English" to "Toggle interface language to your native regional dialect.",
            "Hindi" to "इंटरफ़ेस भाषा को अपनी मातृभाषा में बदलें।",
            "Bhojpuri" to "एप के भाषा आपन मातृभाषा में बदलीं।",
            "Maithili" to "इंटरफेसक भाषा अपन मातृभाषा में बदलू।",
            "Punjabi" to "ਇੰਟਰਫੇਸ ਭਾਸ਼ਾ ਨੂੰ ਆਪਣੀ ਮਾਂ-ਬੋਲੀ ਵਿੱਚ ਬਦਲੋ।",
            "Tamil" to "இடைமுக மொழியை உங்கள் தாய்மொழிக்கு மாற்றவும்.",
            "Bengali" to "ইন্টারফেস ভাষা আপনার মাতৃভাষায় পরিবর্তন করুন।",
            "Sanskrit" to "माध्यमभाषां स्वमातृभाषायां परिवर्तयन्तु।",
            "Marathi" to "इंटरफेस भाषा आपल्या मातृभाषेत बदला."
        ),
        "saved_offline" to mapOf(
            "English" to "Stored Offline",
            "Hindi" to "ऑफ़लाइन सुरक्षित",
            "Bhojpuri" to "ऑफ़लाइन सहेजाइल",
            "Maithili" to "ऑफ़लाइन सुरक्षित",
            "Punjabi" to "ਆਫ਼ਲਾਈਨ ਸੁਰੱਖਿਅਤ",
            "Tamil" to "ஆஃப்லைனில் சேமிக்கப்பட்டது",
            "Bengali" to "অফলাইনে সংরক্ষিত",
            "Sanskrit" to "ऑफ़लाइन सुरक्षितः",
            "Marathi" to "ऑफलाईन सुरक्षित"
        ),
        "weekly_activity" to mapOf(
            "English" to "Weekly Activity",
            "Hindi" to "साक्रीय साप्ताहिक",
            "Bhojpuri" to "हफ्ता के काम",
            "Maithili" to "साप्ताहिक क्रियाकलाप",
            "Punjabi" to "ਹਫਤਾਵਾਰੀ ਗਤੀਵਿਧੀ",
            "Tamil" to "வாராந்திர செயல்பாடு",
            "Bengali" to "সাপ্তাহিক ক্রিয়াকলাপ",
            "Sanskrit" to "साप्ताहिक क्रियाकलापः",
            "Marathi" to "साप्ताहिक क्रियाकलाप"
        ),
        "badges_achievements" to mapOf(
            "English" to "Badges & Accomplishments",
            "Hindi" to "पुरस्कार और उपलब्धियां",
            "Bhojpuri" to "पदक और पुरस्कार",
            "Maithili" to "पुरस्कार आ सम्मान",
            "Punjabi" to "ਬੈਜ ਅਤੇ ਪ੍ਰਾਪਤੀਆਂ",
            "Tamil" to "விருதுகள் மற்றும் சாதனைகள்",
            "Bengali" to "ব্যাজ এবং অর্জন",
            "Sanskrit" to "पुरस्काराः उपलब्धयश्च",
            "Marathi" to "पुरस्कार व उपलब्धी"
        ),
        "premium_membership" to mapOf(
            "English" to "Premium Membership",
            "Hindi" to "प्रीमियम सदस्यता",
            "Bhojpuri" to "प्रीमियम मेंबरशिप",
            "Maithili" to "प्रीमियम सदस्यता",
            "Punjabi" to "ਪ੍ਰੀਮੀਅਮ ਮੈਂਬਰਸ਼ਿਪ",
            "Tamil" to "பிரசீமியம் உறுப்பினர்",
            "Bengali" to "প্রিমিয়াম সদস্যপদ",
            "Sanskrit" to "प्रीमियम सदस्यता",
            "Marathi" to "प्रीमियम सदस्यता"
        ),
        "scholar_rank" to mapOf(
            "English" to "Sanskriti Scholar • Premium Patron",
            "Hindi" to "संस्कृति विद्वान • प्रीमियम संरक्षक",
            "Bhojpuri" to "संस्कृति विद्वान • प्रीमियम संरक्षक",
            "Maithili" to "संस्कृति विद्वान • प्रीमियम संरक्षक",
            "Punjabi" to "ਸੰਸਕ੍ਰਿਤੀ ਵਿਦਵਾਨ • ਪ੍ਰੀਮੀਅਮ ਸਰਪ੍ਰਸਤ",
            "Tamil" to "சமஸ்கிருத அறிஞர் • பிரீமியம் ஆதரவாளர்",
            "Bengali" to "সংস্কৃতি পণ্ডিত • প্রিমিয়াম পৃষ্ঠপোষক",
            "Sanskrit" to "संस्कृति विद्वान • विशिष्ट संरक्षकः",
            "Marathi" to "संस्कृती विद्वान • प्रीमियम संरक्षक"
        ),
        "standard_tier" to mapOf(
            "English" to "Standard Tier",
            "Hindi" to "सामान्य श्रेणी",
            "Bhojpuri" to "सामान्य श्रेणी",
            "Maithili" to "सामान्य श्रेणी",
            "Punjabi" to "ਆਮ ਸ਼੍ਰੇਣੀ",
            "Tamil" to "சாதாரண அடுக்கு",
            "Bengali" to "সাধারণ স্তর",
            "Sanskrit" to "सामान्यश्रेणी",
            "Marathi" to "सामान्य स्तर"
        ),
        "total_xp" to mapOf(
            "English" to "Total XP",
            "Hindi" to "कुल अनुभव अंक",
            "Bhojpuri" to "कुल अनुभव अंक",
            "Maithili" to "कुल अनुभव अंक",
            "Punjabi" to "ਕੁੱਲ ਤਜਰਬਾ",
            "Tamil" to "மொத்த எக்ஸ்பி",
            "Bengali" to "মোট এক্সপি",
            "Sanskrit" to "सम्पूर्णम् अनुभवम्",
            "Marathi" to "एकूण एक्सपी"
        ),
        "level" to mapOf(
            "English" to "Level",
            "Hindi" to "स्तर",
            "Bhojpuri" to "स्तर",
            "Maithili" to "स्तर",
            "Punjabi" to "ਪੱਧਰ",
            "Tamil" to "நிலை",
            "Bengali" to "স্তর",
            "Sanskrit" to "स्तरः",
            "Marathi" to "स्तर"
        ),
        "streak" to mapOf(
            "English" to "Streak",
            "Hindi" to "सक्रियता क्रम",
            "Bhojpuri" to "सक्रियता क्रम",
            "Maithili" to "सक्रियता क्रम",
            "Punjabi" to "ਲਗਾਤਾਰ ਦਿਨ",
            "Tamil" to "தொடர்ச்சி",
            "Bengali" to "ধারাবাহিকতা",
            "Sanskrit" to "क्रमशः",
            "Marathi" to "सातत्य"
        ),
        "bhasha_mitra" to mapOf(
            "English" to "Bhasha Mitra (Language Friend)",
            "Hindi" to "भाषा मित्र",
            "Bhojpuri" to "भाषा मित्र",
            "Maithili" to "भाषा मित्र",
            "Punjabi" to "ਭਾਸ਼ਾ ਮਿੱਤਰ",
            "Tamil" to "மொழி நண்பன்",
            "Bengali" to "ভাষা মিত্র",
            "Sanskrit" to "भाषा मित्रम्",
            "Marathi" to "भाषा मित्र"
        ),
        "bhasha_mitra_desc" to mapOf(
            "English" to "Completed learning 3 daily words in regional dialects.",
            "Hindi" to "क्षेत्रीय बोलियों में रोज़ाना ३ शब्द सीखने का कार्य पूरा किया।",
            "Bhojpuri" to "क्षेत्रीय बोली में रोज ३ शब्द सीखे के काम पूरा भईल।",
            "Maithili" to "क्षेत्रीय बोली में दैनिक ३ शब्द सीखब पूरा भेल।",
            "Punjabi" to "ਖੇਤਰੀ ਬੋਲੀਆਂ ਵਿੱਚ ਰੋਜ਼ਾਨਾ 3 ਸ਼ਬਦ ਸਿੱਖਣ ਦਾ ਕੰਮ ਪੂਰਾ ਕੀਤਾ।",
            "Tamil" to "வட்டார வழக்குகளில் தினமும் 3 வார்த்தைகளைக் கற்றுக்கொண்டார்.",
            "Bengali" to "আঞ্চলিক উপভাষায় প্রতিদিন ৩টি শব্দ শেখা সম্পন্ন হয়েছে।",
            "Sanskrit" to "क्षेत्रीयभाषासु अद्यतनशब्दत्रयस्य पाठनं सम्पादितम्।",
            "Marathi" to "प्रादेशिक भाषांमधील दररोजचे ३ शब्द शिकणे पूर्ण केले."
        ),
        "sufi_shishya" to mapOf(
            "English" to "Sufi Shishya (Lore disciple)",
            "Hindi" to "सूफी शिष्य",
            "Bhojpuri" to "सूफी शिष्य",
            "Maithili" to "सूफी शिष्य",
            "Punjabi" to "ਸੂਫੀ ਸ਼ਿਸ਼",
            "Tamil" to "சூஃபி சீடர்",
            "Bengali" to "সুফি শিষ্য",
            "Sanskrit" to "सूफी शिष्यः",
            "Marathi" to "सुफी शिष्य"
        ),
        "sufi_shishya_desc" to mapOf(
            "English" to "Listened to 2 devotional katha audio files completely offline.",
            "Hindi" to "ऑफ़लाइन रूप से २ भक्ति कथा ऑडियो पूरी तरह से सुने।",
            "Bhojpuri" to "ऑफ़लाइन २ गो भक्ति कथा ऑडियो पूरा सुनल गईल।",
            "Maithili" to "ऑफ़लाइन २ टा भक्ति कथा ऑडियो पूरा सुनल गेल।",
            "Punjabi" to "ਔਫਲਾਈਨ 2 ਭਗਤੀ ਕਥਾ ਆਡੀਓ ਫਾਈਲਾਂ ਪੂਰੀ ਤਰ੍ਹਾਂ ਸੁਣੀਆਂ।",
            "Tamil" to "ஆஃப்லைனில் 2 பக்தி ஆடியோ கோப்புகளை முழுமையாகக் கேட்டார்.",
            "Bengali" to "অফলাইনে ২টি ভক্তিগীতি বা কথা সম্পূর্ণ শুনেছেন।",
            "Sanskrit" to "ऑफ़लाइन माध्यमेन भक्ति कथाद्वयं सम्पूर्णं श्रुतवान्।",
            "Marathi" to "२ भक्तीकथा ऑडिओ पूर्णपणे ऑफलाईन ऐकल्या."
        )
    )

    fun getString(key: String, lang: String): String {
        val langMap = translations[key] ?: return key
        return langMap[lang] ?: langMap["English"] ?: key
    }
}
