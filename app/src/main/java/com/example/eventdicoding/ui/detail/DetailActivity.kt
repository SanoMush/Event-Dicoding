package com.example.eventdicoding.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.eventdicoding.R
import com.example.eventdicoding.data.ListEventsItem
import com.example.eventdicoding.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val EVENT_KEY = "event"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EVENT_KEY, ListEventsItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EVENT_KEY)
        }

        event?.let {
            // Displaying event details
            with(binding) {
                tvDetailName.text = event.name
                tvDetailOwnername.text = event.ownerName
                tvDetailBegintime.text = event.beginTime
                tvDetailQuota.text = getString(
                    R.string.quota_left,
                    event.quota?.minus(event.registrants ?: 0)
                )
                tvDetailDescription.text = event.description?.let {
                    HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                } ?: ""

                // Using the extension function for image loading
                ivImageUpcoming.loadImage(event.imageLogo ?: event.mediaCover)
            }

            binding.btnDetailSign.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(event.link)
                startActivity(intent)
            }
        } ?: run {
            // Handle null event
            Snackbar.make(binding.root, "Event tidak ditemukan", Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }
}

// Extension function for ImageView to load image with Glide
fun ImageView.loadImage(url: String?) {
    Glide.with(this.context)
        .load(url)
        .centerCrop()
        .into(this)
}
