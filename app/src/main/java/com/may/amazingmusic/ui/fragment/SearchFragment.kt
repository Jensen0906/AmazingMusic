package com.may.amazingmusic.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.FragmentSearchBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.ui.adapter.KuwoSongAdapter
import com.may.amazingmusic.ui.adapter.KuwoSongClickListener
import com.may.amazingmusic.ui.adapter.SongsAdapter
import com.may.amazingmusic.ui.adapter.SongsItemClickListener
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.convertToSong
import com.may.amazingmusic.utils.isFalse
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/10/20 21:28
 * @description SearchFragment
 */
@OptIn(UnstableApi::class)
class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private val TAG = this.javaClass.simpleName

    private lateinit var kuwoViewModel: KuwoViewModel
    private lateinit var songViewModel: SongViewModel
    private lateinit var adapter: SongsAdapter
    private lateinit var kuwoSongAdapter: KuwoSongAdapter
    private val songs: MutableList<Song> = mutableListOf()
    private val kuwoSongs: MutableList<KuwoSong> = mutableListOf()
    private var keyword: String? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val lastChildView = layoutManager?.getChildAt(layoutManager.childCount - 1)
            val lastPosition = lastChildView?.let { layoutManager.getPosition(it) }

            if (lastPosition == layoutManager?.itemCount.orZero() - 1) {
                searchSong(keyword)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(requireActivity())[KuwoViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initDataAndView()
        binding.searchKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchKeyword.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

                PlayerManager.kuwoPage = 1
                kuwoSongs.clear()
                kuwoSongAdapter.updateSongs(kuwoSongs)

                searchSong(binding.searchKeyword.text?.toString())
                clearAdapterView()
                true
            } else false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectAndObserver()
    }

    fun clearAdapterView() {
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
        kuwoSongAdapter = KuwoSongAdapter(kuwoSongs, object : KuwoSongClickListener {
            override fun itemClickListener(song: KuwoSong) {
                binding.searchSongsRv.isClickable = false
                binding.loading.visibility = View.VISIBLE
                // about play use songViewModel
                songViewModel.addSongToPlaylist(song.convertToSong(), true)
            }

            override fun addSongToList(song: KuwoSong) {
                // about play use songViewModel
                songViewModel.addSongToPlaylist(song.convertToSong())
            }

            override fun favoriteClickListener(song: KuwoSong, position: Int) {
                kuwoViewModel.operateFavorite(song, position)
            }
        })

        if (PlayerManager.isKuwoSource) {
            kuwoViewModel.getMyKuwoSongRids()
            binding.searchSongsRv.adapter = kuwoSongAdapter
            binding.searchSongsRv.layoutManager = LinearLayoutManager(requireContext())
            binding.searchSongsRv.addOnScrollListener(scrollListener)
        } else {
            songViewModel.getFavoriteIds()
            binding.searchSongsRv.adapter = adapter
            binding.searchSongsRv.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private var lockSearch = false
    private fun searchSong(keyword: String?) {
        binding.searchSongsRv.removeOnScrollListener(scrollListener)
        this.keyword = keyword
        kuwoSongAdapter.setLoading(true)
        binding.loading.visibility = if (PlayerManager.isKuwoSource && PlayerManager.kuwoPage > 1) View.GONE else View.VISIBLE
        if (PlayerManager.isKuwoSource) {
            if (!lockSearch) {
                lockSearch = true
                kuwoViewModel.searchSongs(keyword)
            }
        } else songViewModel.findSongsByAny(keyword)
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

        lifecycleScope.launch {
            kuwoViewModel.searchSongs.collect {
                binding.loading.visibility = View.GONE
                binding.searchSongsRv.addOnScrollListener(scrollListener)
                lockSearch = false
                if (it.isNullOrEmpty()) {
                    if (PlayerManager.kuwoPage >= 10) {
                        ToastyUtils.warning("再玩要被玩坏了")
                    } else {
                        ToastyUtils.error("哦豁，没有找到相关歌曲")
                    }
                    kuwoSongAdapter.setLoading(false)
                    binding.searchSongsRv.removeOnScrollListener(scrollListener)
                    return@collect
                }
                PlayerManager.kuwoPage++

                kuwoSongs.addAll(it)
                if (kuwoSongAdapter.hasSetFavorite) {
                    kuwoSongAdapter.updateSongs(kuwoSongs, true)
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
                val rid = it.first
                kuwoSongAdapter.updateFavoriteSong(rid, position = it.second, isFavorite = it.third)
            }
        }

        PlayerManager.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it.isFalse()) {
                binding.searchSongsRv.isClickable = true
                binding.loading.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        songViewModel.notifyFavoriteChanged(favoriteChangedSids.toList())
        favoriteChangedSids.clear()
//        kuwoViewModel.notifyFavoriteChanged(fChangedKuwoRids.toList())
//        fChangedKuwoRids.clear()
    }

    override fun setDataBinding(): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(layoutInflater)
    }
}