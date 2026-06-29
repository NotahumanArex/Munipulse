package my.n.munipulse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import my.n.munipulse.fragments.FeedFragment

// Ensure this matches your package

class SMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the FeedFragment into the container if it's the first time

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FeedFragment())
                .commit()
        }
    }
}