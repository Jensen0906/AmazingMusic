package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
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

    private fun setPlayer() {
        if (PlayerManager.isKuwoSource) {
            binding.playerView.visibility = View.GONE
            binding.songCover.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(PlayerManager.coverUrl)
                .placeholder(R.drawable.amazingmusic).error(R.drawable.amazingmusic)
                .into(binding.songCover)
        } else {
            binding.playerView.player = PlayerManager.player
            binding.songCover.visibility = View.GONE
            binding.playerView.visibility = View.VISIBLE
        }
    }

    override fun setDataBinding(): FragmentPlayBinding {
        return FragmentPlayBinding.inflate(layoutInflater)
    }
}