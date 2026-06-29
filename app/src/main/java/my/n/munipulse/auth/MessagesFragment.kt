package my.n.munipulse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import my.n.munipulse.R

class MessagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Wire up the Compose Message Button
        val btnCompose = view.findViewById<ImageButton>(R.id.btn_compose)
        btnCompose.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Compose Message...", Toast.LENGTH_SHORT).show()
            // Future: Open a new ComposeActivity here
        }

        // 2. Wire up the Tab Layout Filters
        val tabMessages = view.findViewById<TabLayout>(R.id.tab_messages)
        tabMessages.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedFilter = tab?.text.toString()
                Toast.makeText(requireContext(), "Filtering by: $selectedFilter", Toast.LENGTH_SHORT).show()

                // Future: Update your RecyclerView query based on 'selectedFilter'
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }
        })

        // Note: The Empty State (R.id.empty_state) is already visible in XML by default
        // When you add actual messages to the RecyclerView later, you will hide the empty state.
    }
}