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
import com.may.amazingmusic.utils.GridSpaceItemDecoration
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
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
        override fun itemClickListener(songListId: Long) {
            kuwoViewModel.songListId.postValue(songListId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("my_songs", ArrayList(songs))
        outState.putInt("my_page", PlayerManager.page)
        outState.putParcelableArrayList("my_banners", ArrayList(banners))
        outState.putParcelableArrayList("my_kuwo_song_lists", ArrayList(banners))

        lifecycleScope.launch {
            outState.putLongArray("my_favorite_ids", songViewModel.favoriteSids.first()?.toLongArray())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initData(savedInstanceState)
        initClick()
        collectAndObserver()
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

        kuwoViewModel.songListId.postValue(-1)

        songListAdapter = KuwoSongListsAdapter(songLists, songListItemClickListener)
        binding.songListsRv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.songListsRv.addItemDecoration(GridSpaceItemDecoration(4f.spToPx(requireContext()).toInt()))
        binding.songListsRv.adapter = songListAdapter

        @Suppress("DEPRECATION")
        if (savedInstanceState != null) {
            val myFavoriteIds = savedInstanceState.getLongArray("my_favorite_ids")?.toList()
            val myPage = savedInstanceState.getInt("my_favorite_ids", 1)
            var mySongs: ArrayList<Song>? = arrayListOf()
            var myBanners: ArrayList<Banner>? = arrayListOf()
            var mySongLists: ArrayList<SongList>? = arrayListOf()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mySongs = savedInstanceState.getParcelableArrayList("my_songs", Song::class.java)
                myBanners = savedInstanceState.getParcelableArrayList("my_banners", Banner::class.java)
                mySongLists = savedInstanceState.getParcelableArrayList("my_kuwo_song_lists", SongList::class.java)
            } else {
                mySongs = savedInstanceState.getParcelableArrayList("my_songs")
                myBanners = savedInstanceState.getParcelableArrayList("my_banners")
                mySongLists = savedInstanceState.getParcelableArrayList("my_kuwo_song_lists")
            }

            PlayerManager.page = myPage
            if (mySongs != null) {
                songs.clear()
                songs.addAll(mySongs)
                setSongsHashMap(songs)
            }
            adapter.setFavoriteSongIds(myFavoriteIds)
            adapter.updateSongs(songs)

            if (myBanners != null) {
                banners.clear()
                banners.addAll(myBanners)
            }
            bannerAdapter = MyBannerAdapter(banners, songListItemClickListener)
            binding.banner.setAdapter(bannerAdapter, true)
                .setViewTreeLifecycleOwner(viewLifecycleOwner)

            if (mySongLists != null) {
                songLists.clear()
                songLists.addAll(mySongLists)
            }
            songListAdapter.updateSongLists(songLists)

        } else {
            refreshView()
            binding.kuwoScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val totalHeight = binding.kuwoScrollView.getChildAt(0).height
                val myHeight = binding.kuwoScrollView.height

                if (scrollY + myHeight >= totalHeight && kuwoViewModel.songListPage == 3) {
                    binding.songListsRv.isNestedScrollingEnabled = true
                }
            }
            lifecycleScope.launch {
                DataStoreManager.updateTimerOpened(false)
            }
        }
    }

    private fun initClick() {
        binding.refreshLayout.setOnRefreshListener {
            refreshView()
        }
        binding.songsRv.removeOnScrollListener(scrollListener)
        binding.songsRv.addOnScrollListener(scrollListener)
    }

    fun refreshView() {
        if (PlayerManager.isKuwoSource) {
            Log.e(TAG, "refreshView: isKuwoSource")
            binding.loading.visibility = View.INVISIBLE
            binding.songsRv.visibility = View.INVISIBLE
            binding.kuwoScrollView.visibility = View.VISIBLE
            binding.refreshLayout.isRefreshing = false

            kuwoViewModel.getBanners()
            kuwoViewModel.getKuwoSongLists()
        } else {
            Log.e(TAG, "refreshView: not KuwoSource")
            binding.kuwoScrollView.visibility = View.GONE
            binding.songsRv.visibility = View.VISIBLE
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
                Log.e(TAG, "collectAndObserver: banners=$it")
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
                Log.e(TAG, "collectAndObserver: kuwoSongLists=$it")
                binding.songListLoading.visibility = View.GONE
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
                    binding.songListsRv.addOnScrollListener(scrollListener)
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