package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.databinding.FragmentSongListBinding
import com.may.amazingmusic.ui.adapter.KuwoSongAdapter
import com.may.amazingmusic.ui.adapter.KuwoSongClickListener
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.convertToSong
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2025/3/16 13:53
 */
class SongListFragment : BaseFragment<FragmentSongListBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel
    private lateinit var kuwoSongAdapter: KuwoSongAdapter

    private val kuwoSongs: MutableList<KuwoSong> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectsAndObserves()
    }

    override fun onResume() {
        super.onResume()
        kuwoViewModel.getSongListInfo()
        binding.loadingBar.visibility = View.VISIBLE
    }

    private fun initData() {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]

        kuwoSongAdapter = KuwoSongAdapter(kuwoSongs, object : KuwoSongClickListener {
            override fun itemClickListener(song: KuwoSong) {
                songViewModel.addSongToPlaylist(song.convertToSong(), true)
            }

            override fun addSongToList(song: KuwoSong) {
                songViewModel.addSongToPlaylist(song.convertToSong())
            }

            override fun favoriteClickListener(song: KuwoSong, position: Int) {
                kuwoViewModel.operateFavorite(song, position)
            }
        })
        kuwoSongAdapter.setLoading(false)
        binding.songListRv.adapter = kuwoSongAdapter
        binding.songListRv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun collectsAndObserves() {
        lifecycleScope.launch {
            kuwoViewModel.songListInfo.collect {
                binding.loadingBar.visibility = View.GONE
                if (it == null || it.musicList.isNullOrEmpty()) {
                    ToastyUtils.error("获取歌曲失败")
                } else {
                    kuwoSongAdapter.updateSongs(it.musicList!!)
                }
            }
        }
    }

    override fun setDataBinding(): FragmentSongListBinding {
        return FragmentSongListBinding.inflate(layoutInflater)
    }
}