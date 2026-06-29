package my.n.munipulse.ui.posts

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.createSupabaseClient

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.n.munipulse.R
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etDesc: EditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null
    private var cameraBitmap: Bitmap? = null

    // Initialize Supabase Client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://woqocnupxzzdtmopivbw.supabase.co",
        supabaseKey = "sb_publishable_hShUoe1_Jtqb1vW9j78LgQ_p9D_d1rP"
    ) {
        install(Storage)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivPreview.setImageURI(selectedImageUri)
            ivPreview.visibility = View.VISIBLE
            cameraBitmap = null
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraBitmap = result.data?.extras?.get("data") as Bitmap
            ivPreview.setImageBitmap(cameraBitmap)
            ivPreview.visibility = View.VISIBLE
            selectedImageUri = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        ivPreview = findViewById(R.id.iv_preview_photo)
        etTitle = findViewById(R.id.et_issue_title)
        etDesc = findViewById(R.id.et_issue_desc)
        chipGroup = findViewById(R.id.chip_group_category)
        btnSubmit = findViewById(R.id.btn_submit_report)
        val btnCamera = findViewById<Button>(R.id.btn_camera)
        val btnGallery = findViewById<Button>(R.id.btn_gallery)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_create_post)

        btnCamera.setOnClickListener {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        btnSubmit.setOnClickListener {
            validateAndUpload()
        }

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun validateAndUpload() {
        val title = etTitle.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val selectedChipId = chipGroup.checkedChipId

        if (title.isEmpty() || desc.isEmpty() || selectedChipId == -1 || (selectedImageUri == null && cameraBitmap == null)) {
            Toast.makeText(this, "Please provide all details and a photo", Toast.LENGTH_SHORT).show()
            return
        }

        val category = findViewById<Chip>(selectedChipId).text.toString()
        btnSubmit.isEnabled = false
        btnSubmit.text = "Uploading to Supabase..."

        // Launch Coroutine for Supabase
        lifecycleScope.launch {
            uploadToSupabase(title, desc, category)
        }
    }

    private suspend fun uploadToSupabase(title: String, desc: String, category: String) {
        val bucket = supabase.storage.from("post-images")
        val fileName = "${UUID.randomUUID()}.jpg"

        try {
            // Convert to Byte Array on IO thread
            val bytes = withContext(Dispatchers.IO) {
                if (selectedImageUri != null) {
                    contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                } else {
                    val baos = ByteArrayOutputStream()
                    cameraBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    baos.toByteArray()
                }
            }

            if (bytes != null) {
                // Upload to Supabase Bucket
                bucket.upload(fileName, bytes)
                val publicUrl = bucket.publicUrl(fileName)

                // Save meta-data to Firestore
                saveToFirestore(title, desc, category, publicUrl)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Report"
                Toast.makeText(this@CreatePostActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveToFirestore(title: String, desc: String, category: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        val postData = hashMapOf(
            "title" to title,
            "description" to desc,
            "category" to category,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "status" to "Pending", //
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("posts")
            .add(postData)
            .addOnSuccessListener {
                // 1. Show a quick confirmation to the user
                Toast.makeText(this, "Report Published!", Toast.LENGTH_SHORT).show()

                // 2. THIS IS THE KEY: Close this activity to go back to the home/feed screen
                finish()
            }
            .addOnFailureListener { e ->
                // Re-enable the button so they can try again if it fails
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Report"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}