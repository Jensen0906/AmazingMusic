package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
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