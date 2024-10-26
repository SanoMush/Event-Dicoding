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
import com.example.eventdicoding.data.response.ListEventsItem
import com.example.eventdicoding.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var fabFavorite: FloatingActionButton
    private var isFavorite = false

    companion object {
        const val EVENT_KEY = "event"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menginisialisasi FloatingActionButton
        fabFavorite = binding.fabFav

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

            // Cek status awal favorite
            isFavorite = getFavoriteStatus()
            updateFavoriteIcon()

            // Handle klik pada FloatingActionButton
            fabFavorite.setOnClickListener {
                isFavorite = !isFavorite // Toggle status favorite
                saveFavoriteStatus(isFavorite) // Simpan status terbaru
                updateFavoriteIcon() // Update icon
            }
        } ?: run {
            // Handle null event
            Snackbar.make(binding.root, "Event tidak ditemukan", Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_black_24dp) // Icon favorite
        } else {
            fabFavorite.setImageResource(R.drawable.ic_unfavorite_black_24dp) // Icon belum favorite
        }
    }

    private fun getFavoriteStatus(): Boolean {
        // Implementasi menggunakan SharedPreferences
        val sharedPreferences = getSharedPreferences("favorite_prefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFavorite", false)
    }

    private fun saveFavoriteStatus(isFavorite: Boolean) {
        // Simpan status ke SharedPreferences
        val sharedPreferences = getSharedPreferences("favorite_prefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFavorite", isFavorite).apply()
    }
}

// Extension function for ImageView to load image with Glide
fun ImageView.loadImage(url: String?) {
    Glide.with(this.context)
        .load(url)
        .centerCrop()
        .into(this)
}
