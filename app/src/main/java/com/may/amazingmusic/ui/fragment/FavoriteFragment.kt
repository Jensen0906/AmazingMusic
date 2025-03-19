package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentFavoriteBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.KuwoSongAdapter
import com.may.amazingmusic.ui.adapter.KuwoSongClickListener
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.convertToSong
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/9/15 20:45
 * @description FavoriteFragment
 */
@OptIn(UnstableApi::class)
class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel
    private lateinit var adapter: SongsAdapter
    private lateinit var kuwoSongAdapter: KuwoSongAdapter

    private val songs: MutableList<Song> = mutableListOf()
    private val kuwoSongs: MutableList<KuwoSong> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initCollectAndObserve()
    }

    private fun initAdapter() {
        adapter = SongsAdapter(songs, object : SongsItemClickListener {
            override fun itemClickListener(song: Song) {
                if (songViewModel.allHasAdded.value.isTrue()) {
                    songViewModel.addSongToPlaylist(song, true)
                }
            }

            override fun addSongToList(song: Song) {
                songViewModel.addSongToPlaylist(song)
            }

            override fun showSongInfo(song: Song) {
                (activity as? MainActivity)?.displayInfo(song)
            }

            override fun favorite(song: Song, position: Int) {
                songViewModel.operateFavorite(song, position)
            }

        })

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

        }, false )

        if (PlayerManager.isKuwoSource) {
            kuwoViewModel.getMyKuwoSongs()
            binding.favoriteSongsRv.adapter = kuwoSongAdapter
            binding.favoriteSongsRv.layoutManager = LinearLayoutManager(requireContext())
        } else {
            songViewModel.getFavoriteSongs()
            binding.favoriteSongsRv.adapter = adapter
            binding.favoriteSongsRv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initCollectAndObserve() {
        lifecycleScope.launch {
            kuwoViewModel.myKuwoSongs.collect {
                songViewModel.favoriteSongs.postValue(
                    it?.map { kuwoSong ->
                        kuwoSong.convertToSong()
                    }
                )
                kuwoSongAdapter.setFavoriteKuwoSongs(it)
                (activity as? MainActivity)?.makePlayAllEnable(!it.isNullOrEmpty(), true)
            }
        }
        songViewModel.favoriteSongs.observe(viewLifecycleOwner) {
            adapter.setFavoriteSongs(it)
            (activity as? MainActivity)?.makePlayAllEnable(!it.isNullOrEmpty(), false)
        }
        lifecycleScope.launch {
            kuwoViewModel.operateFavoriteSong.collect {
                kuwoSongAdapter.updateFavoriteSong(it.first, position = it.second, isFavorite = it.third)
            }
        }
    }
    override fun setDataBinding(): FragmentFavoriteBinding {
        return FragmentFavoriteBinding.inflate(layoutInflater)
    }
}