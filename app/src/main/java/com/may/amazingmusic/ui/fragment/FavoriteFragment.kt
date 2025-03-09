package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentFavoriteBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.flow.first
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
    private var adapter: SongsAdapter? = null

    private val songs: MutableList<Song> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (DataStoreManager.isKuwoSelected.first().isTrue()) {
                // todo adaptive Kuwo Source favorite operation

                binding.favoriteSongsRv.visibility = View.GONE
                (activity as? MainActivity)?.makePlayAllEnable(false)
            } else {
                binding.favoriteSongsRv.visibility = View.VISIBLE
            }
        }
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
            adapter?.setFavoriteSongs(it)
            (activity as? MainActivity)?.makePlayAllEnable(!it.isNullOrEmpty() && binding.favoriteSongsRv.isVisible)
        }
        return binding.root
    }

    override fun setDataBinding(): FragmentFavoriteBinding {
        return FragmentFavoriteBinding.inflate(layoutInflater)
    }
}