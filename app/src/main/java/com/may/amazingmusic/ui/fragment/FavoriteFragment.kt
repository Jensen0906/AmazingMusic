package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentFavoriteBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.viewmodel.SongViewModel

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
    private lateinit var adapter: SongsAdapter

    private val songs: MutableList<Song> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        super.onCreate(savedInstanceState)
        songViewModel.getFavoriteSongs()
        adapter = SongsAdapter(songs, object : SongsItemClickListener {
            override fun itemClickListener(song: Song) {
                songViewModel.addSongToPlaylist(song, true)
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
        binding.favoriteSongsRv.adapter = adapter
        binding.favoriteSongsRv.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        songViewModel.favoriteSongs.observe(viewLifecycleOwner) {
            Log.d(TAG, "adapter.setFavoriteSongs: $it")
            adapter.setFavoriteSongs(it)
            (activity as? MainActivity)?.makePlayAllEnable(!it.isNullOrEmpty())
        }
        return binding.root
    }

    override fun setDataBinding(): FragmentFavoriteBinding {
        return FragmentFavoriteBinding.inflate(layoutInflater)
    }
}