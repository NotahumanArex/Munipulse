import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
    val bio: String = "",           // Added for UI
    val location: String = "",
    val profession: String = "",    // Added for UI
    val website: String = "",       // Added for UI
    val role: String = "citizen",
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val civicScore: Int = 0,
    val isPublicProfile: Boolean = true, // Added for UI
    val showMyReports: Boolean = true,   // Added for UI
    val createdAt: Timestamp = Timestamp.now()
) {
    // Helpful extensions for the UI
    fun displayName() = "$firstName $lastName".trim()
    fun initials() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}"
}