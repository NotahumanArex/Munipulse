package my.n.munipulse.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import my.n.munipulse.R
import my.n.munipulse.models.Post

class FeedAdapter : ListAdapter<Post, FeedAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val db = FirebaseFirestore.getInstance()
        private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Header
        private val tvAvatar: TextView = itemView.findViewById(R.id.tv_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_post_meta)
        private val btnMenu: ImageButton = itemView.findViewById(R.id.btn_post_menu)

        // Content
        private val ivPostImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_post_desc)

        // Status & Admin
        private val chipStatus: com.google.android.material.chip.Chip = itemView.findViewById(R.id.chip_status)
        private val adminReplyContainer: View = itemView.findViewById(R.id.cv_admin_reply)
        private val tvAdminReplyText: TextView = itemView.findViewById(R.id.tv_admin_reply_text)

        // Action Buttons at the bottom
        private val btnLike: View = itemView.findViewById(R.id.btn_like)
        private val btnComment: View = itemView.findViewById(R.id.btn_comment)
        private val btnShare: View = itemView.findViewById(R.id.btn_share)
        private val btnFlag: View = itemView.findViewById(R.id.btn_flag)

        fun bind(post: Post) {
            // Set Text Data
            tvTitle.text = post.title
            tvDesc.text = post.description
            tvUserName.text = "Citizen" // Later you can use post.userName
            tvAvatar.text = "C"
            tvMeta.text = "Just now"

            // Handle Image Visibility
            if (post.imageUrl.isNotEmpty()) {
                ivPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(ivPostImage)
            } else {
                ivPostImage.visibility = View.GONE
            }

            // Handle Status Colors
            chipStatus.text = post.status
            val statusColor = when (post.status) {
                "Resolved" -> R.color.mp_green_light
                "In Progress" -> R.color.mp_blue_light
                else -> R.color.mp_yellow_light
            }
            chipStatus.setChipBackgroundColorResource(statusColor)

            // Handle Admin Reply
            if (post.adminReply != null) {
                adminReplyContainer.visibility = View.VISIBLE
                tvAdminReplyText.text = post.adminReply
            } else {
                adminReplyContainer.visibility = View.GONE
            }

            // --- BOTTOM ACTION BUTTON CLICKS ---
            btnLike.setOnClickListener {
                Toast.makeText(itemView.context, "Liked post!", Toast.LENGTH_SHORT).show()
                // Future: Update Firestore like count here
            }

            btnComment.setOnClickListener {
                Toast.makeText(itemView.context, "Opening comments...", Toast.LENGTH_SHORT).show()
            }

            // Route Bottom Buttons to actual logic
            btnShare.setOnClickListener { sharePost(post, itemView.context) }
            btnFlag.setOnClickListener { reportPost(post, itemView.context) }

            // --- 3-DOT OVERFLOW MENU ---
            btnMenu.setOnClickListener { view ->
                showPopupMenu(view, post, view.context)
            }
        }

        private fun showPopupMenu(view: View, post: Post, context: Context) {
            val popupMenu = PopupMenu(context, view)

            // 1. Actions everyone can see
            popupMenu.menu.add(0, 1, 0, "🔖 Save Post")
            popupMenu.menu.add(0, 2, 1, "🔔 Follow Issue")
            popupMenu.menu.add(0, 3, 2, "📤 Share")
            popupMenu.menu.add(0, 4, 3, "🚩 Report Post")

            // 2. Actions ONLY the post creator can see
            if (currentUserId.isNotEmpty() && post.userId == currentUserId) {
                popupMenu.menu.add(0, 5, 4, "✏️ Edit Post")
                popupMenu.menu.add(0, 6, 5, "🗑️ Delete Post")
            }

            // Force show icons in the popup menu (Optional)
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)
            } catch (e: Exception) {}

            // 3. Handle Menu Clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> savePost(post, context)
                    2 -> followIssue(post, context)
                    3 -> sharePost(post, context)
                    4 -> reportPost(post, context)
                    5 -> editPost(post, context)
                    6 -> deletePostFromFirestore(post, context)
                }
                true
            }
            popupMenu.show()
        }

        // --- THE ACTION FUNCTIONS ---

        private fun sharePost(post: Post, context: Context) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareMessage = "Check out this issue reported on MuniPulse: ${post.title}\nLocation: "
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
        }

        private fun deletePostFromFirestore(post: Post, context: Context) {
            if (post.postId.isNotEmpty()) {
                db.collection("posts").document(post.postId).delete()
                    .addOnSuccessListener { Toast.makeText(context, "Report Deleted", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { e -> Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            } else {
                Toast.makeText(context, "Cannot delete: Missing Document ID", Toast.LENGTH_SHORT).show()
            }
        }

        private fun savePost(post: Post, context: Context) {
            if (currentUserId.isEmpty()) return
            db.collection("users").document(currentUserId)
                .collection("saved_posts").document(post.postId)
                .set(mapOf("savedAt" to System.currentTimeMillis(), "postId" to post.postId))
                .addOnSuccessListener { Toast.makeText(context, "Post Saved!", Toast.LENGTH_SHORT).show() }
        }

        private fun followIssue(post: Post, context: Context) {
            if (currentUserId.isEmpty()) return
            db.collection("posts").document(post.postId)
                .update("followers", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener { Toast.makeText(context, "Following issue updates", Toast.LENGTH_SHORT).show() }
        }

        private fun reportPost(post: Post, context: Context) {
            val reportData = hashMapOf(
                "postId" to post.postId,
                "reportedBy" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("reports").add(reportData)
                .addOnSuccessListener { Toast.makeText(context, "Post flagged for review", Toast.LENGTH_SHORT).show() }
        }

        private fun editPost(post: Post, context: Context) {
            Toast.makeText(context, "Opening Edit Screen...", Toast.LENGTH_SHORT).show()
            // Future: Navigate to your Edit Post Activity here
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.postId == newItem.postId
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}