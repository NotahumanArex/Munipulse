package my.n.munipulse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import my.n.munipulse.R

class EditProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Text Views & Inputs
    private lateinit var tvAvatarInitials: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etBio: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etProfession: TextInputEditText
    private lateinit var etWebsite: TextInputEditText

    // Switches
    private lateinit var switchPublic: SwitchMaterial
    private lateinit var switchReports: SwitchMaterial

    // Buttons
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        tvAvatarInitials = view.findViewById(R.id.tv_edit_avatar)
        etName = view.findViewById(R.id.et_edit_name)
        etBio = view.findViewById(R.id.et_edit_bio)
        etLocation = view.findViewById(R.id.et_edit_location)
        etProfession = view.findViewById(R.id.et_edit_profession)
        etWebsite = view.findViewById(R.id.et_edit_website)

        switchPublic = view.findViewById(R.id.switch_public_profile)
        switchReports = view.findViewById(R.id.switch_show_reports)

        btnSave = view.findViewById(R.id.btn_save_profile)
        btnCancel = view.findViewById(R.id.btn_cancel_edit)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_edit_profile)
        toolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
        btnCancel.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // Disable the Change Photo button
        val btnChangeAvatar = view.findViewById<Button>(R.id.btn_change_avatar)
        btnChangeAvatar?.setOnClickListener {
            Toast.makeText(context, "Using text initials for profile.", Toast.LENGTH_SHORT).show()
        }

        // Load and Save Logic
        loadUserData()

        btnSave.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    etName.setText(name)
                    etBio.setText(document.getString("bio") ?: "")
                    etLocation.setText(document.getString("location") ?: "")
                    etProfession.setText(document.getString("profession") ?: "")
                    etWebsite.setText(document.getString("website") ?: "")

                    switchPublic.isChecked = document.getBoolean("isPublicProfile") ?: true
                    switchReports.isChecked = document.getBoolean("showMyReports") ?: true

                    // Generate Initials dynamically based on their current name!
                    val nameParts = name.split(" ")
                    val initials = "${nameParts.getOrNull(0)?.firstOrNull() ?: ""}${nameParts.getOrNull(1)?.firstOrNull() ?: ""}"
                    tvAvatarInitials.text = initials.uppercase()
                }
            }
    }

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid ?: return
        val name = etName.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "bio" to etBio.text.toString().trim(),
            "location" to etLocation.text.toString().trim(),
            "profession" to etProfession.text.toString().trim(),
            "website" to etWebsite.text.toString().trim(),
            "isPublicProfile" to switchPublic.isChecked,
            "showMyReports" to switchReports.isChecked
        )
        // 1. You must import SetOptions at the very top of your file:
        // import com.google.firebase.firestore.SetOptions

        // 2. Change .update() to .set(..., SetOptions.merge())
        db.collection("users").document(userId).set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                btnSave.text = "Save Changes"
                // Let's also print the exact error message so we know exactly why it failed!
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }


    }
}