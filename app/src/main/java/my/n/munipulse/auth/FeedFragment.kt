package my.n.munipulse.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import my.n.munipulse.R
import my.n.munipulse.models.Post
import my.n.munipulse.ui.posts.CreatePostActivity
import my.n.munipulse.adapters.FeedAdapter

class FeedFragment : Fragment() {

    private lateinit var feedAdapter: FeedAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Top Toolbar Action Buttons
        val btnNearby = view.findViewById<ImageButton>(R.id.btn_profile) // Currently uses ic_nearby
        btnNearby.setOnClickListener {
            Toast.makeText(context, "Nearby coming soon", Toast.LENGTH_SHORT).show()
        }

        val btnTrending = view.findViewById<ImageButton>(R.id.btn_trending)
        btnTrending.setOnClickListener {
            Toast.makeText(context, "Trending coming soon", Toast.LENGTH_SHORT).show()
        }

        val btnSettings = view.findViewById<ImageButton>(R.id.btn_settings)
        btnSettings.setOnClickListener {
            Toast.makeText(context, "Settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // 2. Setup Custom Bottom Navigation Bar
        val navHome = view.findViewById<LinearLayout>(R.id.nav_home)
        navHome.setOnClickListener {
            // Already on the feed screen
        }

        val navMap = view.findViewById<LinearLayout>(R.id.nav_map)
        navMap.setOnClickListener {
            Toast.makeText(context, "Map selected", Toast.LENGTH_SHORT).show()
        }

        // Messages / Alerts (Bottom Nav Bell Icon)
        val btnMessaging = view.findViewById<LinearLayout>(R.id.btn_messaging)
        btnMessaging.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, my.n.munipulse.fragments.MessagesFragment())
                .addToBackStack(null)
                .commit()
        }

        val navProfile = view.findViewById<LinearLayout>(R.id.nav_profile)
        navProfile.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // 3. Initialize RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_feed)
        feedAdapter = FeedAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)
        }

        // 4. FAB Navigation to Create Post
        val fabPost = view.findViewById<FloatingActionButton>(R.id.fab_post)
        fabPost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 5. Start the Real-time Listener
        startRealtimeListener()
    }

    private fun startRealtimeListener() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val postList = snapshots.toObjects(Post::class.java)
                    feedAdapter.submitList(postList)
                }
            }
    }
}