package my.n.munipulse.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    @DocumentId
    var postId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val status: String = "Pending",
    @ServerTimestamp val timestamp: Date? = null,
    val adminReply: String? = null
)