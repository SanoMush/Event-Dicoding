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
import com.example.eventdicoding.data.local.FavoriteEventEntity
import com.example.eventdicoding.data.local.FavoriteEventRepository
import com.example.eventdicoding.data.response.ListEventsItem
import com.example.eventdicoding.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var fabFavorite: FloatingActionButton
    private var isFavorite = false
    private lateinit var favoriteEventRepository: FavoriteEventRepository

    companion object {
        const val EVENT_KEY = "event"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FavoriteEventRepository
        favoriteEventRepository = FavoriteEventRepository.getInstance(this)

        fabFavorite = binding.fabFav

        val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EVENT_KEY, ListEventsItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EVENT_KEY)
        }

        event?.let {
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
            checkFavoriteStatus(event.id.toString()) // Konversi ID ke String

            // Handle klik pada FloatingActionButton
            fabFavorite.setOnClickListener {
                isFavorite = !isFavorite // Toggle status favorite
                saveFavoriteStatus(event) // Simpan status terbaru ke database
                updateFavoriteIcon() // Update icon
            }
        } ?: run {
            Snackbar.make(binding.root, "Event tidak ditemukan", Snackbar.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_black_24dp) // Icon favorite
        } else {
            fabFavorite.setImageResource(R.drawable.ic_unfavorite_black_24dp) // Icon unfavorite
        }
    }

    private fun checkFavoriteStatus(eventId: String) {
        favoriteEventRepository.getFavoriteEventById(eventId).observe(this) { favoriteEvent ->
            isFavorite = favoriteEvent != null // Jika ada, berarti itu adalah favorit
            updateFavoriteIcon() // Update ikon berdasarkan status favorit
        }
    }

    private fun saveFavoriteStatus(event: ListEventsItem) {
        if (isFavorite) {
            val favoriteEventEntity = FavoriteEventEntity(
                id = event.id.toString(), // Pastikan ID dikonversi ke String
                name = event.name,
                imageLogo = event.imageLogo
            )
            favoriteEventRepository.insertEvent(favoriteEventEntity) // Simpan sebagai favorit
        } else {
            favoriteEventRepository.getFavoriteEventById(event.id.toString()).observe(this) { favoriteEvent -> // Konversi ID ke String
                favoriteEvent?.let {
                    favoriteEventRepository.delete(it) // Hapus dari database jika sebelumnya ada
                }
            }
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
