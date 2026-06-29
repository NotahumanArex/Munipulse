package my.n.munipulse.adapters // Make sure this matches your package

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import my.n.munipulse.R
import my.n.munipulse.models.MessageThread

class MessagesAdapter(private var threads: List<MessageThread>) :
    RecyclerView.Adapter<MessagesAdapter.ThreadViewHolder>() {

    class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Updated to match YOUR item_thread.xml IDs exactly
        val tvAvatarInitials: TextView = itemView.findViewById(R.id.tv_avatar_initials)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvPreview: TextView = itemView.findViewById(R.id.tv_preview)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val viewUnreadDot: View = itemView.findViewById(R.id.view_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_thread, parent, false)
        return ThreadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val thread = threads[position]

        // Set the text data
        holder.tvName.text = thread.senderName
        holder.tvAvatarInitials.text = thread.senderName.firstOrNull()?.toString()?.uppercase() ?: "U"
        holder.tvPreview.text = thread.lastMessage
        holder.tvTime.text = thread.timestamp

        // Handle Unread Dot Visibility (Since yours is a dot, not a number counter)
        if (thread.unreadCount > 0) {
            holder.viewUnreadDot.visibility = View.VISIBLE
            // Optional: Make the sender's name darker if unread
            holder.tvName.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
        } else {
            holder.viewUnreadDot.visibility = View.GONE
            // Optional: Make the sender's name lighter if read
            holder.tvName.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        }

        // Click Listener to open the chat
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Opening chat with ${thread.senderName}", Toast.LENGTH_SHORT).show()
            // Future: Navigate to ChatFragment/ChatActivity here passing the threadId!
        }
    }

    override fun getItemCount(): Int = threads.size

    fun updateData(newThreads: List<MessageThread>) {
        threads = newThreads
        notifyDataSetChanged()
    }
}