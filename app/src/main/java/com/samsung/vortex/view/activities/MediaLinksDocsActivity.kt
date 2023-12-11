package com.samsung.vortex.view.activities

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.samsung.vortex.R
import com.samsung.vortex.adapters.MediaLinksDocsViewPagerAdapter
import com.samsung.vortex.databinding.ActivityMediaLinksDocsBinding
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.TAG
import com.samsung.vortex.utils.WhatsappLikeProfilePicPreview
import java.io.File


class MediaLinksDocsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaLinksDocsBinding
    private var tabsName = arrayOf("Media", "Links", "Docs")

    private var startTime = 0
    private var finalTime = 0

    private val myHandler: Handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            startTime = mediaPlayer.currentPosition
            binding.expandedAudio.time.text = Utils.formatMilliSecond(startTime.toLong())
            val seekBarValue = (startTime / (finalTime * 1.0) * 100.0).toInt()
            binding.expandedAudio.seekBar.progress = seekBarValue
            myHandler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaLinksDocsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val receiverId = intent.getStringExtra(getString(R.string.RECEIVER_ID))!!

        val adapter = MediaLinksDocsViewPagerAdapter(this, receiverId)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabsName[position]
        }.attach()

        backButtonPressed()
    }

    fun showImagePreview(thumbView: View, url: File) {
        WhatsappLikeProfilePicPreview.zoomImageFromThumb(
            thumbView,
            binding.expandedImage.cardView,
            binding.expandedImage.image,
            binding.root.rootView,
            url
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun showVideoPreview(thumbView: View?, url: String?) {
        WhatsappLikeProfilePicPreview.zoomVideoFromThumb(thumbView!!, binding.expandedVideo.cardView, binding.root.rootView)
        val player = ExoPlayer.Builder(this).build()
        binding.expandedVideo.video.player = player
        binding.expandedVideo.video.setShowNextButton(false)
        binding.expandedVideo.video.setShowPreviousButton(false)
        val mediaItem = MediaItem.fromUri(url!!)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun showAudioPreview(thumbView: View, url: File) {
        WhatsappLikeProfilePicPreview.zoomAudioFromThumb(thumbView, binding.expandedAudio.mainLayout, binding.root.rootView)
        mediaPlayer = MediaPlayer.create(this, url.toUri())
        binding.expandedAudio.seekBar.isClickable = false
        myHandler.postDelayed(updateSongTime, 100)
        binding.expandedAudio.pause.setImageResource(R.drawable.baseline_pause_24)

        mediaPlayer.start()

        finalTime = mediaPlayer.duration
        startTime = mediaPlayer.currentPosition

        Log.i(TAG, "showAudioPreview: $startTime : $finalTime")
        binding.expandedAudio.seekBar.progress = startTime

        binding.expandedAudio.pause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                binding.expandedAudio.pause.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                mediaPlayer.start()
                binding.expandedAudio.pause.setImageResource(R.drawable.baseline_pause_24)
            }
        }
    }

    private fun backButtonPressed() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.expandedImage.cardView.visibility == View.VISIBLE) {
                    WhatsappLikeProfilePicPreview.dismissPhotoPreview()
                } else if (binding.expandedVideo.cardView.visibility == View.VISIBLE) {
                    binding.expandedVideo.video.player!!.release()
                    WhatsappLikeProfilePicPreview.dismissVideoPreview()
                }  else if (binding.expandedAudio.mainLayout.visibility == View.VISIBLE) {
                    WhatsappLikeProfilePicPreview.dismissAudioPreview()
                    mediaPlayer.stop()
                } else {
                    finish()
                }
            }
        })
    }
}