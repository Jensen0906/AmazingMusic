package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.isVisible
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
import com.may.amazingmusic.utils.player.PlayerListener
import com.may.amazingmusic.utils.player.PlayerManager
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
                getSongContinue()
            }
        }
    }

    private val playerListener = object : PlayerListener {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        kuwoSongAdapter.updateSongs(emptyList())
        collectsAndObserves()
        (requireActivity() as MainActivity).songListFragmentAlive = true
    }

    private fun initData() {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        kuwoViewModel.songInListPage = 1

        PlayerManager.playerListeners.add(playerListener)
        kuwoSongAdapter = KuwoSongAdapter(kuwoSongs, object : KuwoSongClickListener {
            override fun itemClickListener(song: KuwoSong) {
                if (binding.loadingBar.isVisible) return
                binding.loadingBar.visibility = View.VISIBLE
                songViewModel.clickThisKuwoSong = song
                songViewModel.hasAdd = 0
                songViewModel.addAllSongsToPlaylist()

//                viewLifecycleOwner.lifecycleScope.launch {
//                    delay(5000)
//                    if (binding.loadingBar.isVisible) binding.loadingBar.visibility = View.GONE
//                }
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

        kuwoViewModel.getMyKuwoSongRids()
        kuwoViewModel.getSongListInfo()
        binding.loadingBar.visibility = View.VISIBLE
        binding.songListRv.addOnScrollListener(scrollListener)
    }

    private fun collectsAndObserves() {
        lifecycleScope.launch {
            kuwoViewModel.songListInfo.collect {
                binding.loadingBar.visibility = View.GONE
                if (it == null || it.musicList.isNullOrEmpty()) {
                    ToastyUtils.info("就这么多了")
                    binding.songListRv.removeOnScrollListener(scrollListener)
                    lockGetSongs = true
                    kuwoSongAdapter.setLoading(false)
                } else {
                    if (kuwoViewModel.songInListPage <= 1) {
                        kuwoSongs.clear()
                        songViewModel.songInList.clear()
                        kuwoSongs.addAll(it.musicList!!)
                        if (kuwoSongAdapter.hasSetFavorite) {
                            kuwoSongAdapter.updateSongs(kuwoSongs)
                        }
                    } else {
                        kuwoSongs.addAll(it.musicList!!)
                        if (kuwoSongAdapter.hasSetFavorite) {
                            kuwoSongAdapter.updateSongs(kuwoSongs, true)
                        }
                    }
                    it.musicList?.forEach { kuwoSong ->
                        songViewModel.songInList.add(kuwoSong.convertToSong())
                    }
                    kuwoViewModel.songInListPage++
                    if (kuwoViewModel.songInListPage > 40) {
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
        lifecycleScope.launch {
            kuwoViewModel.myKuwoSongRids.collect {
                kuwoSongAdapter.setFavoriteKuwoSongRids(it)
                kuwoSongAdapter.updateSongs(kuwoSongs)
            }
        }
        lifecycleScope.launch {
            kuwoViewModel.operateFavoriteSong.collect {
                kuwoSongAdapter.updateFavoriteSong(
                    it.first,
                    position = it.second,
                    isFavorite = it.third
                )
            }
        }
        songViewModel.allHasAdded.observe(viewLifecycleOwner) {
            if (it) {
                binding.loadingBar.visibility = View.GONE
                PlayerManager.playSongBySongId(songViewModel.clickThisKuwoSong?.rid)
                songViewModel.clickThisKuwoSong = null
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
        PlayerManager.playerListeners.remove(playerListener)
        (requireActivity() as MainActivity).songListFragmentAlive = false
    }

    override fun setDataBinding(): FragmentSongListBinding {
        return FragmentSongListBinding.inflate(layoutInflater)
    }
}