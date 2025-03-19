package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dirror.lyricviewx.OnPlayClickListener
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.databinding.FragmentPlayBinding
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.msToTimeString
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerListener
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.utils.spToPx
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel


/**
 *
 * @author May
 * @date 2024/9/15 22:00
 * @description PlayFragment
 */
@UnstableApi
class PlayFragment : BaseFragment<FragmentPlayBinding>() {
    private val TAG = PlayFragment::class.java.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel

    private val playerListener = object : PlayerListener {
        override fun onIsPlayingChanged(isPlaying: Boolean, title: String?) {
            if (isPlaying) handler.post(updateRunnable) else handler.removeCallbacks(updateRunnable)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, position: Int) {
            updateCoverPicture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        setPlayer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        observe()
        PlayerManager.playerListeners.add(playerListener)
        handler.post(updateRunnable)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViews() {
        binding.switchDisplay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.songCover.visibility = View.INVISIBLE
                binding.lrcView.visibility = View.VISIBLE
            } else {
                binding.lrcView.visibility = View.INVISIBLE
                binding.songCover.visibility = View.VISIBLE
            }
        }
        binding.lrcView.smoothScrollInterpolator = LinearInterpolator()
        binding.lrcView.run {
            setNormalColor(requireActivity().getColor(R.color.track_color))
            setCurrentColor(requireActivity().getColor(R.color.thumb_color))
            setNormalTextSize(16f.spToPx(appContext))
            setCurrentTextSize(22f.spToPx(appContext))
            setLabel("暂无歌词")
        }

        binding.lrcView.setDraggable(true, object : OnPlayClickListener {
            override fun onPlayClick(time: Long): Boolean {
                PlayerManager.player?.seekTo(time)
                return true
            }
        })
    }

    private fun observe() {
        kuwoViewModel.currentLrc.observe(viewLifecycleOwner) {
            binding.lrcView.loadLyric(it)
        }
    }

    private var duration = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (PlayerManager.player?.isPlaying.isTrue()) {
                if (duration <= 0 || duration != PlayerManager.player?.duration) {
                    duration = PlayerManager.player?.duration ?: 0
                    binding.timeDur.text = duration.msToTimeString()
                }
                val current = PlayerManager.player?.currentPosition ?: 0
                binding.timeCur.text = current.msToTimeString()
                binding.lrcView.updateTime(current)
                if (duration > 0) {
                    val progress = (current * 500 / duration).toInt()
                    binding.playSeekBar.progress = progress
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private fun setPlayer() {
        if (PlayerManager.isKuwoSource) {
            binding.playerView.visibility = View.INVISIBLE
            binding.songCover.visibility = View.VISIBLE
            binding.switchGroup.visibility = View.VISIBLE
            updateCoverPicture()
        } else {
            binding.playerView.player = PlayerManager.player
            binding.songCover.visibility = View.INVISIBLE
            binding.switchGroup.visibility = View.INVISIBLE
            binding.playerView.visibility = View.VISIBLE
        }
        binding.playSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.timeCur.text = (progress * duration / 500).msToTimeString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                PlayerManager.player?.seekTo(duration * seekBar?.progress.orZero().toLong() / 500)
                handler.post(updateRunnable)
            }
        })
    }

    private fun updateCoverPicture() {
        Glide.with(this)
            .load(PlayerManager.player?.mediaMetadata?.artworkData)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .error(R.drawable.amazingmusic)
            .into(binding.songCover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerManager.playerListeners.remove(playerListener)
    }

    override fun setDataBinding(): FragmentPlayBinding {
        return FragmentPlayBinding.inflate(layoutInflater)
    }
}