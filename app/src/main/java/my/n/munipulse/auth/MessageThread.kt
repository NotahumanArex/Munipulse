package my.n.munipulse.models // Ensure this matches your package structure

data class MessageThread(
    val threadId: String = "",
    val senderName: String = "",
    val senderRole: String = "citizen", // citizen, official, admin
    val lastMessage: String = "",
    val timestamp: String = "",
    val unreadCount: Int = 0
)