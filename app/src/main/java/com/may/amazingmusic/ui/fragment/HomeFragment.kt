package com.may.amazingmusic.ui.fragment

import android.os.Build
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
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentHomeBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/9/16 13:00
 * @description HomeFragment
 */
@OptIn(UnstableApi::class)
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel
    private lateinit var adapter: SongsAdapter
    private val songs = mutableListOf<Song>()
    private val songsMap = mutableMapOf<Long, Int>()

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val lastChildView = layoutManager?.getChildAt(layoutManager.childCount - 1)
            val lastChildBottom = lastChildView?.bottom
            val recyclerBottom = recyclerView.bottom - recyclerView.paddingBottom
            val lastPosition = lastChildView?.let { layoutManager.getPosition(it) }

            if (lastChildBottom == recyclerBottom && lastPosition == layoutManager.itemCount - 1) {
                getSongsContinue()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (DataStoreManager.isKuwoSelected.first().isTrue()) {
                binding.loading.visibility = View.INVISIBLE
                binding.songsRv.visibility = View.INVISIBLE
                binding.kuwoTips.visibility = View.VISIBLE
                binding.banner.visibility = View.VISIBLE
            } else {
                binding.banner.visibility = View.INVISIBLE
                binding.kuwoTips.visibility = View.INVISIBLE
                binding.songsRv.visibility = View.VISIBLE
            }
        }
        initData(savedInstanceState)
        initClick()
        collectAndObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("my_songs", ArrayList(songs))
        outState.putInt("my_page", PlayerManager.page)
        lifecycleScope.launch {
            outState.putLongArray("my_favorite_ids", songViewModel.favoriteSids.first()?.toLongArray())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.songsRv.removeOnScrollListener(scrollListener)
        binding.songsRv.addOnScrollListener(scrollListener)
        return binding.root
    }

    fun invalidateFavorite() {
        adapter.setFavoriteSongIds(emptyList())
    }

    private fun initData(savedInstanceState: Bundle?) {
        adapter = SongsAdapter(songs, object : SongsItemClickListener {
            override fun itemClickListener(song: Song) {
                Log.d(TAG, "itemClickListener: ")
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
        binding.songsRv.adapter = adapter
        binding.songsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.songsRv.addOnScrollListener(scrollListener)

        if (savedInstanceState != null) {
            val mySongs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelableArrayList("my_songs", Song::class.java)
            } else @Suppress("DEPRECATION") savedInstanceState.getParcelableArrayList("my_songs")
            val myFavoriteIds = savedInstanceState.getLongArray("my_favorite_ids")?.toList()
            val myPage = savedInstanceState.getInt("my_favorite_ids", 1)
            PlayerManager.page = myPage
            if (mySongs != null) {
                songs.clear()
                songs.addAll(mySongs)
                setSongsHashMap(songs)
            }
            adapter.setFavoriteSongIds(myFavoriteIds)
            adapter.updateSongs(songs)
        } else {
            PlayerManager.page = 1
            songViewModel.getFavoriteIds()
            songViewModel.getSongs(PlayerManager.page)
            binding.loading.visibility = View.VISIBLE
            lifecycleScope.launch {
                DataStoreManager.updateTimerOpened(false)
            }
        }
    }

    private fun initClick() {
        binding.refreshLayout.setOnRefreshListener {
            refreshView()
        }
    }

    fun refreshView() {
        if (PlayerManager.isKuwoSource) {
            binding.loading.visibility = View.INVISIBLE
            binding.kuwoTips.visibility = View.VISIBLE
            binding.songsRv.visibility = View.INVISIBLE
            binding.refreshLayout.isRefreshing = false
        } else {
            binding.songsRv.visibility = View.VISIBLE
            binding.kuwoTips.visibility = View.INVISIBLE
            songs.clear()
            setSongsHashMap(songs)
            adapter.updateSongs(emptyList())
            PlayerManager.page = 1

            binding.songsRv.removeOnScrollListener(scrollListener)
            binding.songsRv.addOnScrollListener(scrollListener)

            songViewModel.getFavoriteIds()
            songViewModel.getSongs(1)
        }
    }

    private fun collectAndObserver() {
        lifecycleScope.launch {
            songViewModel.songs.collect {
                binding.loading.visibility = View.INVISIBLE
                binding.refreshLayout.isRefreshing = false
                if (it == null) {
                    ToastyUtils.error(getString(R.string.get_songs_error))
                } else if (it.isEmpty()) {
                    ToastyUtils.warning(getString(R.string.get_songs_empty))
                    binding.songsRv.removeOnScrollListener(scrollListener)
                } else if (songViewModel.checkGetSongs && (songs.isEmpty() || songs[0].sid != it[0].sid)) {
                    songs.addAll(it)
                    setSongsHashMap(songs)
                    PlayerManager.page++
                    if (adapter.hasSetFavorite) adapter.updateSongs(songs, PlayerManager.page)
                } else if (songs[0].sid == it[0].sid) {
                    PlayerManager.page++
                }
                songViewModel.checkGetSongs = false
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
                adapter.updateFavoriteSong(it.first, it.second, it.third)
            }
        }

        lifecycleScope.launch {
            songViewModel.songsChanged.collect {
                it.forEach { sid ->
                    adapter.notifyItemChanged(songsMap[sid].orZero())
                }
            }
        }
    }

    private fun getSongsContinue() {
        if (binding.loading.isVisible) return
        songViewModel.getSongs(PlayerManager.page)
        binding.loading.visibility = View.VISIBLE
    }

    private fun setSongsHashMap(songs: List<Song>) {
        if (songs.isEmpty()) {
            songsMap.clear()
            return
        }
        songs.forEachIndexed { index, song ->
            songsMap.putIfAbsent(song.sid, index)
        }
    }

    override fun setDataBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }
}