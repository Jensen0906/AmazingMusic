package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.databinding.FragmentSongListBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.KuwoSongAdapter
import com.may.amazingmusic.ui.adapter.KuwoSongClickListener
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.convertToSong
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2025/3/16 13:53
 */
@OptIn(UnstableApi::class)
class SongListFragment : BaseFragment<FragmentSongListBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel
    private lateinit var kuwoSongAdapter: KuwoSongAdapter

    private val kuwoSongs: MutableList<KuwoSong> = mutableListOf()

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val lastChildView = layoutManager?.getChildAt(layoutManager.childCount - 1)
            val lastPosition = lastChildView?.let { layoutManager.getPosition(it) }

            if (lastPosition == layoutManager?.itemCount.orZero() - 1) {
                Log.e(TAG, "onScrolled: ")
                getSongContinue()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initData()
        return binding.root
    }

    override fun onResume() {
        kuwoSongAdapter.updateSongs(emptyList())
        super.onResume()
        collectsAndObserves()
        kuwoViewModel.getSongListInfo()
        binding.loadingBar.visibility = View.VISIBLE
        binding.songListRv.addOnScrollListener(scrollListener)
        (requireActivity() as MainActivity).songListFragmentAlive = true
    }

    private fun initData() {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        kuwoViewModel.songListPage = 1

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
                    if (kuwoViewModel.songListPage <= 1) {
                        kuwoSongs.clear()
                        kuwoSongs.addAll(it.musicList!!)
                        kuwoSongAdapter.updateSongs(kuwoSongs)
                    } else {
                        kuwoSongs.addAll(it.musicList!!)
                        kuwoSongAdapter.updateSongs(kuwoSongs, true)
                    }
                    kuwoViewModel.songListPage++
                    if (kuwoViewModel.songListPage > 10) {
                        binding.songListRv.removeOnScrollListener(scrollListener)
                        lockGetSongs = true
                        kuwoSongAdapter.setLoading(false)
                    } else {
                        binding.songListRv.addOnScrollListener(scrollListener)
                        lockGetSongs = false
                    }
                }
            }
        }
    }


    private var lockGetSongs = false
    private fun getSongContinue() {
        binding.songListRv.removeOnScrollListener(scrollListener)
        if (!lockGetSongs) {
            lockGetSongs = true
            kuwoViewModel.getSongListInfo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).songListFragmentAlive = false
    }
    override fun setDataBinding(): FragmentSongListBinding {
        return FragmentSongListBinding.inflate(layoutInflater)
    }
}