package com.example.data.model

data class NoteSection(
    val heading: String,
    val bullets: List<String>,
    val timestampSec: Int,
    val timestampFormatted: String
)

enum class DiagramType {
    FLOWCHART,
    MINDMAP,
    TIMELINE
}

enum class NodeType {
    START,
    STEP,
    DECISION,
    END,
    CONCEPT
}

data class DiagramNode(
    val id: String,
    val label: String,
    val detail: String = "",
    val nodeType: NodeType = NodeType.STEP,
    val pastelTint: String = "mint" // mint, peach, butter, sky, lavender
)

data class DiagramEdge(
    val fromId: String,
    val toId: String,
    val label: String = ""
)

data class FlowchartData(
    val diagramType: DiagramType = DiagramType.FLOWCHART,
    val nodes: List<DiagramNode>,
    val edges: List<DiagramEdge>
)

data class RecapTable(
    val title: String,
    val columns: List<String>,
    val rows: List<List<String>>
)

data class QuizItem(
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val why: String
)

data class FlashcardItem(
    val id: String,
    val front: String,
    val back: String,
    val topic: String,
    val isMastered: Boolean = false
)

data class SaarRecap(
    val id: String,
    val videoUrl: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val durationText: String,
    val thumbnailUrl: String,
    val category: String,
    val tldr: String,
    val keyTakeaways: List<String>,
    val notes: List<NoteSection>,
    val flowchart: FlowchartData,
    val tables: List<RecapTable>,
    val quiz: List<QuizItem>,
    val flashcards: List<FlashcardItem>,
    val audioScript: String,
    val language: String = "English",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class DailyStudy(
    val day: String,
    val hours: Float,
    val colorHex: String
)

data class TopicShare(
    val name: String,
    val percentage: Int,
    val colorHex: String
)

data class StudyStats(
    val videosSummarized: Int = 18,
    val notesSaved: Int = 42,
    val hoursSaved: Double = 26.4,
    val streakDays: Int = 7,
    val weeklyOverview: List<DailyStudy> = listOf(
        DailyStudy("Mon", 2.5f, "#CFE5D9"),
        DailyStudy("Tue", 4.0f, "#FAD0BF"),
        DailyStudy("Wed", 3.2f, "#FCE8B0"),
        DailyStudy("Thu", 5.5f, "#CFDDEE"),
        DailyStudy("Fri", 3.0f, "#DCCDF2"),
        DailyStudy("Sat", 4.8f, "#CFE5D9"),
        DailyStudy("Sun", 3.6f, "#FAD0BF")
    ),
    val topTopics: List<TopicShare> = listOf(
        TopicShare("STEM & AI", 45, "#DCCDF2"),
        TopicShare("History & Civics", 25, "#FAD0BF"),
        TopicShare("Biology & Nature", 15, "#CFE5D9"),
        TopicShare("Tech & Code", 10, "#CFDDEE"),
        TopicShare("Other Studies", 5, "#FCE8B0")
    )
)

data class SupportedLanguage(
    val code: String,
    val name: String,
    val nativeName: String
)

val AllSupportedLanguages = listOf(
    SupportedLanguage("en", "English", "English"),
    SupportedLanguage("hinglish", "Hinglish", "Hinglish"),
    SupportedLanguage("hi", "Hindi", "हिन्दी"),
    SupportedLanguage("bn", "Bengali", "বাংলা"),
    SupportedLanguage("ta", "Tamil", "தமிழ்"),
    SupportedLanguage("te", "Telugu", "తెలుగు"),
    SupportedLanguage("mr", "Marathi", "मराठी"),
    SupportedLanguage("gu", "Gujarati", "ગુજરાતી"),
    SupportedLanguage("kn", "Kannada", "ಕನ್ನಡ"),
    SupportedLanguage("ml", "Malayalam", "മലയാളം"),
    SupportedLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    SupportedLanguage("or", "Odia", "ଓଡ଼ିଆ"),
    SupportedLanguage("ur", "Urdu", "اردو")
)
