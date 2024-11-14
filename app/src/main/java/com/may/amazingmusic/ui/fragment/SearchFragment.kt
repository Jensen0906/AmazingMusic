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
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentSearchBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/10/20 21:28
 * @description SearchFragment
 */
class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var adapter: SongsAdapter
    private val songs: MutableList<Song> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initDataAndView()
        collectAndObserver()
        return binding.root
    }

    override fun setDataBinding(): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(layoutInflater)
    }

    fun searchSong(keyword: String?) {
        binding.loading.visibility = View.VISIBLE
        songViewModel.findSongsByAny(keyword)
        clearAdapterView()
    }

    fun clearAdapterView() {
        Log.d(TAG, "clearAdapterView: ", )
        songs.clear()
        adapter.updateSongs(songs)
    }

    private fun initDataAndView() {
        adapter = SongsAdapter(songs, object : SongsItemClickListener {
            override fun itemClickListener(song: Song) {
                songViewModel.addSongToPlaylist(song, true)
            }

            override fun addSongToList(song: Song) {
                songViewModel.addSongToPlaylist(song)
            }

            @OptIn(UnstableApi::class)
            override fun showSongInfo(song: Song) {
                (activity as? MainActivity)?.displayInfo(song)
            }

            override fun favorite(song: Song, position: Int) {
                songViewModel.operateFavorite(song, position)
            }
        })
        binding.searchSongsRv.adapter = adapter
        binding.searchSongsRv.layoutManager = LinearLayoutManager(requireContext())

        songViewModel.getFavoriteIds()
    }

    private val favoriteChangedSids = mutableListOf<Long>()
    private fun collectAndObserver() {
        lifecycleScope.launch {
            songViewModel.searchSongs.collect {
                binding.loading.visibility = View.GONE
                if (it.isNullOrEmpty()) {
                    ToastyUtils.error("哦豁，没有找到相关歌曲")
                    return@collect
                }
                songs.clear()
                songs.addAll(it)
                if (adapter.hasSetFavorite) adapter.updateSongs(songs)
            }
        }

        lifecycleScope.launch {
            songViewModel.favoriteSids.collect {
                adapter.setFavoriteSongIds(it)
                adapter.updateSongs(songs)
            }
        }

        lifecycleScope.launch {
            songViewModel.operateFavoriteSong.collect {
                val sid = it.first
                adapter.updateFavoriteSong(sid, position = it.second, isFavorite = it.third)
                if (sid in favoriteChangedSids) {
                    favoriteChangedSids.remove(sid)
                } else {
                    favoriteChangedSids.add(sid)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        songViewModel.notifyFavoriteChanged(favoriteChangedSids.toList())
        favoriteChangedSids.clear()
    }
}