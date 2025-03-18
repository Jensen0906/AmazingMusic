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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Banner
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.bean.SongList
import com.may.amazingmusic.databinding.FragmentHomeBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.KuwoSongListsAdapter
import com.may.amazingmusic.ui.adapter.MyBannerAdapter
import com.may.amazingmusic.ui.adapter.SongListClickListener
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.customview.GridSpaceItemDecoration
import com.may.amazingmusic.utils.isFalse
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.utils.spToPx
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
    private var bannerAdapter: MyBannerAdapter? = null
    private lateinit var songListAdapter: KuwoSongListsAdapter

    private val songs = mutableListOf<Song>()
    private val songsMap = mutableMapOf<Long, Int>()
    private val banners = mutableListOf<Banner>()
    private val songLists = mutableListOf<SongList>()

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val lastChildView = layoutManager?.getChildAt(layoutManager.childCount - 1)
            val lastChildBottom = lastChildView?.bottom
            val recyclerBottom = recyclerView.bottom - recyclerView.paddingBottom
            val lastPosition = lastChildView?.let { layoutManager.getPosition(it) }

            if (PlayerManager.isKuwoSource.isFalse() && lastChildBottom == recyclerBottom && lastPosition == layoutManager.itemCount - 1) {
                getSongsContinue()
            }

            if (PlayerManager.isKuwoSource.isTrue() && lastPosition == layoutManager?.itemCount.orZero() - 1) {
                getSongListsContinue()
            }
        }
    }

    private val songListItemClickListener = object : SongListClickListener {
        override fun itemClickListener(songListId: Long, songListPic: String?, songListName: String?) {
            kuwoViewModel.songList.postValue(SongList().apply {
                rid = songListId
                pic = songListPic
                name = songListName
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initData()
        initClick()
        collectAndObserver()
        return binding.root
    }

    fun invalidateFavorite() {
        adapter.setFavoriteSongIds(emptyList())
    }

    private fun initData() {
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

        kuwoViewModel.songList.postValue(null)

        songListAdapter = KuwoSongListsAdapter(songLists, songListItemClickListener, false)
        binding.songListsRv.init(binding.kuwoScrollView, resources.displayMetrics.heightPixels - 180f.spToPx(requireContext()))
        binding.songListsRv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.songListsRv.addItemDecoration(GridSpaceItemDecoration(10f.spToPx(requireContext()).toInt()))
        binding.songListsRv.adapter = songListAdapter

        refreshView()
        lifecycleScope.launch {
            DataStoreManager.updateTimerOpened(false)
        }
    }

    private fun initClick() {
        binding.refreshLayout.setOnRefreshListener {
            refreshView()
        }
        binding.kuwoRefresh.setOnRefreshListener {
            refreshView()
        }
        binding.songsRv.removeOnScrollListener(scrollListener)
        binding.songsRv.addOnScrollListener(scrollListener)
    }

    fun refreshView() {
        if (PlayerManager.isKuwoSource) {
            binding.loading.visibility = View.INVISIBLE
            binding.songsRv.visibility = View.INVISIBLE
            binding.kuwoRefresh.visibility = View.VISIBLE
            binding.kuwoScrollView.visibility = View.VISIBLE
            binding.bannerLoading.visibility = View.VISIBLE
            binding.songListLoading.visibility = View.VISIBLE
            binding.refreshLayout.isRefreshing = false
            binding.songListsRv.removeOnScrollListener(scrollListener)
            binding.songListsRv.addOnScrollListener(scrollListener)

            songLists.clear()
            kuwoViewModel.getBanners()
            kuwoViewModel.songListPage = 1
            Log.e(TAG, "refreshView: getKuwoSongLists")
            kuwoViewModel.getKuwoSongLists()
        } else {
            binding.kuwoRefresh.visibility = View.GONE
            binding.kuwoScrollView.visibility = View.GONE
            binding.songsRv.visibility = View.VISIBLE
            binding.kuwoRefresh.isRefreshing = false
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
            if (DataStoreManager.isKuwoSelected.first().isTrue()) {
                binding.loading.visibility = View.INVISIBLE
                binding.songsRv.visibility = View.INVISIBLE
                binding.kuwoScrollView.visibility = View.VISIBLE
            } else {
                binding.kuwoScrollView.visibility = View.GONE
                binding.songsRv.visibility = View.VISIBLE
            }
        }
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

        lifecycleScope.launch {
            kuwoViewModel.banners.collect {
                binding.bannerLoading.visibility = View.GONE
                banners.clear()
                banners.addAll(it ?: emptyList())
                bannerAdapter = MyBannerAdapter(banners, songListItemClickListener)
                binding.banner.setAdapter(bannerAdapter, true)
                    .setViewTreeLifecycleOwner(viewLifecycleOwner)
            }
        }

        lifecycleScope.launch {
            kuwoViewModel.kuwoSongLists.collect {
                binding.songListLoading.visibility = View.GONE
                binding.kuwoRefresh.isRefreshing = false
                Log.e(TAG, "collectAndObserver: song list=$it")
                if (it.isNullOrEmpty()) {
                    ToastyUtils.error("获取歌单失败")
                } else {
                    if (kuwoViewModel.songListPage <= 20) {
                        songLists.addAll(it)
                    } else {
                        binding.songListsRv.removeOnScrollListener(scrollListener)
                        lockGetSongLists = true
                        songListAdapter.setLoading(false)
                        return@collect
                    }
                    kuwoViewModel.songListPage++
                    lockGetSongLists = false
                    songListAdapter.updateSongLists(songLists, true)
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

    private var lockGetSongLists = true
    private fun getSongListsContinue() {
        if (!lockGetSongLists) {
            kuwoViewModel.getKuwoSongLists()
            lockGetSongLists = true
        }
    }

    override fun setDataBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }
}