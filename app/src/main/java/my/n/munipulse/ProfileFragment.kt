package my.n.munipulse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.n.munipulse.R


class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // UI Elements
    private lateinit var tvName: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvCivicScore: TextView
    private lateinit var tvAvatar: TextView
    private lateinit var tvRoleBadge: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize UI Elements
        tvName = view.findViewById(R.id.tv_profile_name)
        tvLocation = view.findViewById(R.id.tv_profile_sub)
        tvCivicScore = view.findViewById(R.id.tv_impact_score)
        tvAvatar = view.findViewById(R.id.tv_profile_avatar)
        tvRoleBadge = view.findViewById(R.id.chip_verified)

        // 2. Start listening for real-time user data
        startRealtimeProfileListener()

        // 3. Navigation to Edit Profile Screen
        val btnEditProfile = view.findViewById<Button>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun startRealtimeProfileListener() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // UPGRADE: Changed .get() to .addSnapshotListener()
        db.collection("users").document(currentUserId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Toast.makeText(context, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)

                    if (user != null) {
                        // Dynamically generate initials
                        tvName.text = user.displayName()
                        tvAvatar.text = user.initials()

                        // Location AND Email Subtitle
                        val locationText = if (user.location.isNotEmpty()) "📍 ${user.location}" else "📍 Location not set"
                        val emailText = user.email.ifEmpty { auth.currentUser?.email ?: "" }
                        tvLocation.text = "$locationText • $emailText"

                        // Civic Score and Role Badge
                        tvCivicScore.text = "${user.civicScore} pts"
                        tvRoleBadge.text = "✅ Verified ${user.role.replaceFirstChar { it.uppercase() }}"
                    }
                }
            }
    }
}