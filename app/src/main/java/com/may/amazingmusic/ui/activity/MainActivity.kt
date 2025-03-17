package com.may.amazingmusic.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_AND_PLAY
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_LAST
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_NEXT
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_LOOP
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SHUFFLE
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SINGLE
import com.may.amazingmusic.databinding.ActivityMainBinding
import com.may.amazingmusic.databinding.DialogShowPlaylistBinding
import com.may.amazingmusic.databinding.DialogShowSongInfoBinding
import com.may.amazingmusic.service.PlayService
import com.may.amazingmusic.ui.adapter.PlaylistAdapter
import com.may.amazingmusic.ui.adapter.PlaylistItemClickListener
import com.may.amazingmusic.ui.fragment.FavoriteFragment
import com.may.amazingmusic.ui.fragment.HomeFragment
import com.may.amazingmusic.ui.fragment.MineFragment
import com.may.amazingmusic.ui.fragment.PlayFragment
import com.may.amazingmusic.ui.fragment.SearchFragment
import com.may.amazingmusic.ui.fragment.SettingsFragment
import com.may.amazingmusic.ui.fragment.SongListFragment
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseActivity
import com.may.amazingmusic.utils.globalGlideOptions
import com.may.amazingmusic.utils.isFalse
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerListener
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.KuwoViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description MainActivity
 */
@UnstableApi
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val TAG = this.javaClass.simpleName

    private val mineFragment = MineFragment()
    private val homeFragment = HomeFragment()
    private val favoriteFragment = FavoriteFragment()
    private val settingsFragment = SettingsFragment()
    private val playFragment = PlayFragment()
    private val searchFragment = SearchFragment()
    private val songListFragment = SongListFragment()
    private var currentFragment: Fragment = homeFragment
    private var lastFragment: Fragment = homeFragment

    private lateinit var songViewModel: SongViewModel
    private lateinit var kuwoViewModel: KuwoViewModel

    private var hasOpenPlayer = false
    var songListFragmentAlive = false

    private var curSongPos = 0
    private var showToast = false
    private val playerListener = object : PlayerListener {
        override fun onIsPlayingChanged(isPlaying: Boolean, title: String?) {
            if (isPlaying && showToast) {
                ToastyUtils.success("正在播放 - $title")
                Glide.with(this@MainActivity).load(PlayerManager.player?.mediaMetadata?.artworkData)
                    .apply(globalGlideOptions(50)).into(binding.displayPlayerIv)
                showToast = false
            }
            binding.playIv.setImageResource(
                if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, position: Int) {
            if (position < 0) {
                playlistAdapter?.setSongToPlaylist()
                playlistDialog?.dismiss()
            } else {
                curSongPos = position
                playlistAdapter?.setCurrentSongIndex(position)
                playlistBinding.playlistRv.scrollToPosition(position)
                showToast = true
            }

            if (PlayerManager.isKuwoSource) {
                if (position >= 0 && position < PlayerManager.playlist.size) {
                    kuwoViewModel.getKuwoLrc(PlayerManager.playlist[position].sid)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: action=${intent.action}")

        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        kuwoViewModel = ViewModelProvider(this)[KuwoViewModel::class.java]

        supportFragmentManager.beginTransaction()
            .replace(R.id.fg_view, homeFragment)
            .commit()
        setSupportActionBar(binding.toolbar)

        kuwoViewModel.isKuwoSource()

        initViewAndAdapter()
        onClick()
        initDataAndObserver()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun closeMineAndToLogin() {
        switchFragment(homeFragment)
        homeFragment.invalidateFavorite()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private var infoDialog: Dialog? = null
    private lateinit var infoBinding: DialogShowSongInfoBinding
    fun displayInfo(song: Song?) {
        if (infoDialog == null) {
            infoDialog = Dialog(this, R.style.CustomDialogTheme)
            infoBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.dialog_show_song_info, null, false)
            infoDialog?.let { dialog ->
                dialog.setContentView(infoBinding.root)
                dialog.window?.let {
                    it.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        (resources.displayMetrics.density * 160).toInt()
                    )

                    val params = it.attributes
                    params.gravity = Gravity.BOTTOM
                    params.y = resources.getDimensionPixelSize(R.dimen.bottom_margin_76dp)
                    it.attributes = params
                    it.setWindowAnimations(R.style.CustomAnimBottom)
                    it.setDimAmount(0f)
                }
                dialog.setCancelable(true)
                infoBinding.root.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        infoBinding.song = song
        infoDialog?.show()

        binding.root.setOnClickListener {
            if (infoDialog?.isShowing.isTrue()) {
                infoDialog?.dismiss()
            }
        }
    }

    fun makePlayAllEnable(isEnable: Boolean, isKuwoSong: Boolean) {
        if (isKuwoSong == PlayerManager.isKuwoSource) {
            binding.playAllBtn.isEnabled = isEnable
        }
    }

    private var playlistAdapter: PlaylistAdapter? = null
    private lateinit var playlistBinding: DialogShowPlaylistBinding
    private fun initViewAndAdapter() {
        playlistBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.dialog_show_playlist,
            null, false
        )
        playlistAdapter = PlaylistAdapter(
            PlayerManager.playlist, object : PlaylistItemClickListener {
                override fun itemClickListener(position: Int) {
                    PlayerManager.playSongByPosition(position)
                }

                override fun itemRemoveListener(position: Int) {
                    if (isAnimating) return
                    PlayerManager.removeMediaItem(position)
                    if (PlayerManager.playlist.isEmpty()) {
                        playlistBinding.clearListLayout.visibility = View.GONE
                        playlistBinding.playModeLayout.visibility = View.GONE
                        playlistDialog?.dismiss()
                        PlayerManager.disableTimer.postValue(false)
                    }
                    playlistAdapter?.removeSongFromList(position)
                }
            }
        )

        playlistBinding.playlistRv.adapter = playlistAdapter
        playlistBinding.playlistRv.layoutManager = LinearLayoutManager(this)

        binding.playIv.setImageResource(if (PlayerManager.player?.isPlaying.isTrue()) R.drawable.icon_pause else R.drawable.icon_play)

        PlayerManager.playerListeners.add(playerListener)
    }

    private fun initDataAndObserver() {
        lifecycleScope.launch {
            songViewModel.addSongToPlay.collect {
                Log.e(TAG, "initDataAndObserver: ")
                it.firstNotNullOf { entry ->
                    Log.d(TAG, "addSongToPlay: song=${entry.key}, position=${entry.value}")
                    when (entry.value) {
                        ADD_LIST_AND_PLAY -> addSongToPlaylist(entry.key, playNow = true)
                        ADD_LIST_NEXT -> addSongToPlaylist(entry.key)
                        ADD_LIST_LAST -> addSongToPlaylist(entry.key, addToLast = true)
                        else -> PlayerManager.playSongByPosition(entry.value)
                    }
                }
            }
        }

        lifecycleScope.launch {
            PlayerManager.repeatModeLiveData.postValue(
                DataStoreManager.repeatModeFlow.first().orZero()
            )
        }

        PlayerManager.repeatModeLiveData.observe(this) {
            lifecycleScope.launch { DataStoreManager.saveRepeatMode(it) }
            if (it < ExoPlayer.REPEAT_MODE_OFF) {
                playlistBinding.ivPlayMode.setImageResource(
                    when (it) {
                        REPEAT_MODE_SINGLE -> R.drawable.ic_singles
                        REPEAT_MODE_LOOP -> R.drawable.ic_list_loop
                        REPEAT_MODE_SHUFFLE -> R.drawable.ic_shuffle
                        else -> R.drawable.ic_list_loop
                    }
                )
            }
        }

        kuwoViewModel.isKuwoSource.observe(this) {
            if (PlayerManager.isKuwoSource == it) return@observe
            PlayerManager.isKuwoSource = it
            homeFragment.refreshView()
            PlayerManager.clearPlaylist()
        }
        kuwoViewModel.songListId.observe(this) {
            if (currentFragment is HomeFragment && it >= 0)
                switchFragment(songListFragment)
        }
    }

    private fun onClick() {
        val drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.left_drawer_open,
            R.string.left_drawer_closed
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.nvView.getHeaderView(0).rootView.setOnClickListener {
            lifecycleScope.launch {
                val uid = DataStoreManager.userIDFlow.first()
                if (uid != null) {
                    switchFragment(mineFragment)
                    binding.drawerLayout.closeDrawers()
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            }
        }
        binding.nvView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    switchFragment(homeFragment)
                }

                R.id.menu_favorite -> {
                    switchFragment(favoriteFragment)
                }

                R.id.menu_settings -> {
                    switchFragment(settingsFragment)
                }

                else -> {}
            }
            binding.drawerLayout.closeDrawers()
            true
        }
        binding.searchIv.setOnClickListener {
            switchFragment(searchFragment)
        }
        binding.playAllBtn.setOnClickListener {
            songViewModel.addAllSongsToPlaylist()
        }
        binding.displayPlayerIv.setOnClickListener {
            switchFragment(if (currentFragment is PlayFragment) lastFragment else playFragment)
            if (hasOpenPlayer.isFalse()) {
                hasOpenPlayer = true
            }
        }
        binding.playPreviousIv.setOnClickListener {
            if (PlayerManager.player == null || PlayerManager.playlist.isEmpty()) {
                ToastyUtils.warning("当前播放列表无歌曲！")
                return@setOnClickListener
            }
            PlayerManager.playPreviousSong()
        }
        binding.playIv.setOnClickListener {
            if (PlayerManager.player == null || PlayerManager.playlist.isEmpty()) {
                ToastyUtils.warning("当前播放列表无歌曲！")
                return@setOnClickListener
            }
            PlayerManager.player?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else if (player.mediaItemCount > 0) {
                    player.play()
                }
            }
        }
        binding.playNextIv.setOnClickListener {
            if (PlayerManager.player == null || PlayerManager.playlist.isEmpty()) {
                ToastyUtils.warning("当前播放列表无歌曲！")
                return@setOnClickListener
            }
            PlayerManager.playNextSong()
        }
        binding.playlistIv.setOnClickListener {
            displayPlaylist()
        }
        binding.notifyIv.setOnLongClickListener {
            PlayerManager.playFunVideo()
            return@setOnLongClickListener true
        }
    }

    private var playlistDialog: Dialog? = null
    private var isAnimating = false
    private fun displayPlaylist() {
        if (playlistDialog == null) {
            playlistDialog = Dialog(this, R.style.CustomDialogTheme)
            playlistDialog?.let { dialog ->
                dialog.setContentView(playlistBinding.root)
                dialog.window?.let {
                    it.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        (resources.displayMetrics.density * 340).toInt()
                    )
                    val params = it.attributes
                    params.gravity = Gravity.BOTTOM
                    params.y = resources.getDimensionPixelSize(R.dimen.bottom_margin_76dp)
                    it.attributes = params
                    it.setWindowAnimations(R.style.CustomAnimBottom)
                    it.setDimAmount(0f)

                    it.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
                    it.decorView.setOnTouchListener { _, event ->
                        if (event.y <= 0 || event.x >= 850) {
                            if (!isAnimating) dialog.dismiss()
                        } else if (event.action == MotionEvent.ACTION_UP && event.y > 930) {
                            when {
                                event.x > 60 && event.x < 190 -> {
                                    dialog.dismiss()
                                    binding.displayPlayerIv.performClick()
                                }

                                event.x > 260 && event.x < 400 -> binding.playPreviousIv.performClick()
                                event.x > 460 && event.x < 600 -> binding.playIv.performClick()
                                event.x > 680 && event.x < 820 -> binding.playNextIv.performClick()
                            }

                        }
                        return@setOnTouchListener true
                    }
                }

                dialog.setOnShowListener {
                    // The view should be clicked just when dialog already display.
                    isAnimating = true
                    dialog.window?.decorView?.postDelayed({ isAnimating = false }, 500)
                    songViewModel.dialogShowing = true
                }
                dialog.setOnDismissListener {
                    isAnimating = true
                    dialog.window?.decorView?.postDelayed({ isAnimating = false }, 500)
                    songViewModel.dialogShowing = false
                }
            }
            playlistBinding.clearListLayout.setOnClickListener {
                if (isAnimating) return@setOnClickListener
                PlayerManager.clearPlaylist()
            }
            playlistBinding.playModeLayout.setOnClickListener {
                if (isAnimating) return@setOnClickListener
                PlayerManager.changePlayMode()
            }
            playlistBinding.root.setOnClickListener {
                if (isAnimating) return@setOnClickListener
                playlistDialog?.dismiss()
            }
        }

        playlistBinding.clearListLayout.visibility =
            if (PlayerManager.playlist.isEmpty()) View.GONE else View.VISIBLE
        playlistBinding.playModeLayout.visibility = playlistBinding.clearListLayout.visibility
        playlistAdapter?.setCurrentSongIndex(curSongPos.orZero())
        playlistBinding.playlistRv.scrollToPosition(curSongPos.orZero())
        playlistDialog?.show()
    }

    private var isPlayServiceBinding = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isPlayServiceBinding = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayServiceBinding = false
        }
    }

    private suspend fun justPlayFirstSong(song: Song) {
        Log.d(TAG, "justPlayFirstSong: song=${song.title}")
//        PlayerManager.clearPlaylist()
        val intent = Intent(this, PlayService::class.java)

        if (isPlayServiceBinding.isTrue() && PlayerManager.player == null) unbindService(
            serviceConnection
        )

        if (PlayerManager.player == null) {
            val dataSourceFactory = PlayerManager.buildCacheDataSourceFactory(this)
            PlayerManager.player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .build()

            PlayerManager.setPlayerListener()
            PlayerManager.addAnalyticsListenerForTest()
        }

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        PlayerManager.player?.let { player ->
            if (player.isPlaying) {
                player.clearMediaItems()
                PlayerManager.playlist.clear()
            }
            song.url?.let {
                if (PlayerManager.isKuwoSource) {
                    MediaItem.Builder()
                        .setUri(it)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title).setArtist(song.singer)
                                .setArtworkData(
                                    loadImageToByteArray(song.coverUrl),
                                    MediaMetadata.PICTURE_TYPE_FRONT_COVER
                                )
                                .build()
                        ).build()
                } else MediaItem.fromUri(it)
            }?.let {
                PlayerManager.playlist.add(song)
                player.setMediaItem(it)
            }
            playlistAdapter?.resetPlaylist(song)
            player.prepare()
            player.repeatMode =
                if (PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SINGLE) ExoPlayer.REPEAT_MODE_ONE
                else ExoPlayer.REPEAT_MODE_ALL
            player.shuffleModeEnabled =
                PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SHUFFLE
            player.playWhenReady = true
        }
    }

    private suspend fun addSongToPlaylist(
        song: Song,
        playNow: Boolean = false,
        addToLast: Boolean = false
    ) {
        Log.d(
            TAG,
            "addSongToPlaylist: song=${song.title}, is first?=${PlayerManager.playlist.isEmpty()}"
        )
        if (PlayerManager.playlist.isEmpty()) {
            justPlayFirstSong(song)
        } else {
            PlayerManager.player?.let { player ->
                val positionAdded = if (addToLast) {
                    player.mediaItemCount
                } else player.currentMediaItemIndex + 1
                song.url?.let {
                    if (PlayerManager.isKuwoSource) {
                        MediaItem.Builder()
                            .setUri(it)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(song.title).setArtist(song.singer)
                                    .setArtworkData(
                                        loadImageToByteArray(song.coverUrl),
                                        MediaMetadata.PICTURE_TYPE_FRONT_COVER
                                    )
                                    .build()
                            )
                            .build()
                    } else MediaItem.fromUri(it)
                }?.let {
                    PlayerManager.playlist.add(positionAdded, song)
                    player.addMediaItem(positionAdded, it)
                    playlistAdapter?.setSongToPlaylist()
                }
                if (playNow) {
                    PlayerManager.playSongByPosition(positionAdded)
                } else if (!addToLast) {
                    ToastyUtils.success("下一首播放 - ${song.title}")
                }
            }

        }
    }

    private fun switchFragment(fragment: Fragment) {
        if (currentFragment == fragment) return

        val transaction = supportFragmentManager.beginTransaction()
        // Set anim
        when (fragment) {
            playFragment -> {
                transaction.setCustomAnimations(R.anim.fragment_play_enter, R.anim.fragment_hold_on)
                lastFragment = currentFragment
            }

            homeFragment -> {
                transaction.setCustomAnimations(R.anim.fragment_hold_on, R.anim.fragment_right_exit)
            }

            else -> {
                transaction.setCustomAnimations(
                    R.anim.fragment_right_enter,
                    R.anim.fragment_hold_on
                )
            }
        }

        when (currentFragment) {
            playFragment -> {
                if (fragment is HomeFragment || fragment is SongListFragment) {
                    transaction.setCustomAnimations(
                        R.anim.fragment_hold_on,
                        R.anim.fragment_play_exit
                    )
                        .hide(currentFragment).show(fragment).commit()
                } else {
                    transaction.setCustomAnimations(
                        R.anim.fragment_hold_on,
                        R.anim.fragment_play_exit
                    )
                        .hide(currentFragment).add(R.id.fg_view, fragment).show(fragment).commit()
                }
                supportFragmentManager.beginTransaction().remove(currentFragment).commit()
            }

            else -> {
                if (fragment is SongListFragment && currentFragment is HomeFragment && songListFragmentAlive) {
                    transaction.hide(currentFragment).show(fragment).commit()
                    fragment.onResume()
                } else if (fragment is HomeFragment) {
                    transaction.hide(currentFragment).show(fragment).commit()
                    supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                    lastFragment = homeFragment
                } else if (currentFragment !is HomeFragment && currentFragment !is SongListFragment) {
                    transaction.remove(currentFragment).add(R.id.fg_view, fragment).show(fragment)
                        .commit()
                } else {
                    transaction.hide(currentFragment).add(R.id.fg_view, fragment).show(fragment)
                        .commit()
                }
            }
        }

        if (currentFragment is SearchFragment) {
            searchFragment.clearAdapterView()
        }

        currentFragment = fragment

        binding.searchIv.visibility =
            if (currentFragment is HomeFragment) View.VISIBLE else View.GONE

        binding.notifyIv.visibility =
            if (currentFragment is MineFragment) View.VISIBLE else View.GONE
        binding.playAllBtn.visibility =
            if (currentFragment is FavoriteFragment) View.VISIBLE else View.GONE
    }

    private suspend fun loadImageToByteArray(coverUrl: String?): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(this@MainActivity).asBitmap()
                    .load(coverUrl).submit().get()
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.toByteArray()
            } catch (it: Exception) {
                it.printStackTrace()
                null
            }
        }

    }

    override fun onDestroy() {
        PlayerManager.playerListeners.remove(playerListener)
        super.onDestroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (currentFragment is PlayFragment) {
                    switchFragment(lastFragment)
                } else if (currentFragment != homeFragment) {
                    switchFragment(homeFragment)
                } else {
                    Log.d(TAG, "onKeyUp: go home")
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                    }.let {
                        startActivity(it)
                    }
                }
                return true
            }

            else -> {}
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun setDataBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
}