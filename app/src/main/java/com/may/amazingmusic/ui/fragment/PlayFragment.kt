package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.databinding.FragmentPlayBinding
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            if (DataStoreManager.isKuwoSelected.first().isTrue()) {
                binding.playerView.isClickable = false
                binding.playerView.visibility = View.INVISIBLE
            }
            songViewModel.songCoverUrl.observe(requireActivity()) {
                Glide.with(appContext)
                    .load(it)
                    .placeholder(R.drawable.amazingmusic).error(R.drawable.amazingmusic)
                    .into(binding.songCover)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        setPlayer()
    }

    fun setPlayer() {
        binding.playerView.player = PlayerManager.player
    }

    override fun setDataBinding(): FragmentPlayBinding {
        return FragmentPlayBinding.inflate(layoutInflater)
    }
}