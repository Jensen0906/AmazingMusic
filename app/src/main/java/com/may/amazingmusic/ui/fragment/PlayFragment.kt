package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.may.amazingmusic.R
import com.may.amazingmusic.databinding.FragmentPlayBinding
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.player.PlayerManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        setPlayer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        songViewModel.currentSongPic.observe(viewLifecycleOwner) {
            setCoverPic(it)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setPlayer() {
        if (PlayerManager.isKuwoSource) {
            binding.playerView.visibility = View.GONE
            binding.songCover.visibility = View.VISIBLE
            setCoverPic(songViewModel.currentSongPic.value)
        } else {
            binding.playerView.player = PlayerManager.player
            binding.songCover.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
        }
    }

    private fun setCoverPic(coverUrl: String?) {
        Glide.with(this)
            .load(coverUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.amazingmusic)
            .into(binding.songCover)
    }

    override fun setDataBinding(): FragmentPlayBinding {
        return FragmentPlayBinding.inflate(layoutInflater)
    }
}