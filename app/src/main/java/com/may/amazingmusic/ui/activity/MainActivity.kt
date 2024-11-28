package com.may.amazingmusic.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.may.amazingmusic.ui.fragment.FeedbackFragment
import com.may.amazingmusic.ui.fragment.HomeFragment
import com.may.amazingmusic.ui.fragment.MineFragment
import com.may.amazingmusic.ui.fragment.PlayFragment
import com.may.amazingmusic.ui.fragment.SearchFragment
import com.may.amazingmusic.ui.fragment.SettingsFragment
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseActivity
import com.may.amazingmusic.utils.isFalse
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orInvalid
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerListener
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private var currentFragment: Fragment = homeFragment
    private var lastFragment: Fragment = homeFragment

    private lateinit var songViewModel: SongViewModel

    private var hasOpenPlayer = false

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: action=${intent.action}")

        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]

        initViewAndAdapter()

        setSupportActionBar(binding.toolbar)

        onClick()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fg_view, homeFragment)
            .commit()

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

    fun makePlayAllEnable(isEnable: Boolean) {
        binding.playAllBtn.isEnabled = isEnable
    }

    private var playlistAdapter: PlaylistAdapter? = null
    private lateinit var playlistBinding: DialogShowPlaylistBinding
    private fun initViewAndAdapter() {
        playlistBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_show_playlist, null, false)
        playlistAdapter = PlaylistAdapter(PlayerManager.playlist, object : PlaylistItemClickListener {
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
        })

        playlistBinding.playlistRv.adapter = playlistAdapter
        playlistBinding.playlistRv.layoutManager = LinearLayoutManager(this)

        binding.playIv.setImageResource(if (PlayerManager.player?.isPlaying.isTrue()) R.drawable.icon_pause_20 else R.drawable.icon_play_20)
    }

    private fun initDataAndObserver() {
        lifecycleScope.launch {
            songViewModel.addSongToPlay.collect {
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
            PlayerManager.repeatModeLiveData.postValue(DataStoreManager.repeatModeFlow.first().orZero())
        }

        PlayerManager.curSongIndexLiveData.observe(this) {
            if (it < 0) {
                playlistAdapter?.setSongToPlaylist()
                playlistDialog?.dismiss()
            } else {
                playlistAdapter?.setCurrentSongIndex(it)
                playlistBinding.playlistRv.scrollToPosition(it)
            }
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
            if (binding.searchInputLayout.isGone) {
                switchFragment(searchFragment)
            } else {
                searchFragment.searchSong(binding.searchKeyword.text?.toString())
            }
        }
        binding.playAllBtn.setOnClickListener {
            songViewModel.addAllSongsToPlaylist()
        }
        binding.displayPlayerIv.setOnClickListener {
            switchFragment(if (currentFragment == playFragment) lastFragment else playFragment)
            if (hasOpenPlayer.isTrue()) {
                playFragment.setPlayer()
            } else {
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
        PlayerManager.playerListener = object : PlayerListener {
            override fun onIsPlayingChanged(isPlaying: Boolean, title: String?) {
                if (isPlaying.isTrue()) ToastyUtils.success("正在播放 - $title")
                binding.playIv.setImageResource(
                    if (isPlaying) R.drawable.icon_pause_20 else R.drawable.icon_play_20
                )
            }
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
        Log.d(TAG, "displayPlaylist: currentSongIndex=${PlayerManager.curSongIndexLiveData.value}")
        playlistBinding.clearListLayout.visibility = if (PlayerManager.playlist.isEmpty()) View.GONE else View.VISIBLE
        playlistBinding.playModeLayout.visibility = playlistBinding.clearListLayout.visibility
        playlistAdapter?.setCurrentSongIndex(PlayerManager.curSongIndexLiveData.value.orInvalid())
        playlistBinding.playlistRv.scrollToPosition(PlayerManager.curSongIndexLiveData.value.orZero())
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
    private fun justPlayFirstSong(song: Song) {
        Log.d(TAG, "justPlayFirstSong: song=${song.title}")
        PlayerManager.clearPlaylist()
        val intent = Intent(this, PlayService::class.java)

        if (isPlayServiceBinding.isTrue() && PlayerManager.player == null) unbindService(serviceConnection)

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
            song.url?.let { MediaItem.fromUri(it) }?.let {
                player.setMediaItem(it)
                PlayerManager.playlist.add(song)
            }
            playlistAdapter?.resetPlaylist(song)
            player.prepare()
            player.repeatMode =
                if (PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SINGLE) ExoPlayer.REPEAT_MODE_ONE
                else ExoPlayer.REPEAT_MODE_ALL
            player.shuffleModeEnabled = PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SHUFFLE
            player.playWhenReady = true
        }
    }

    private fun addSongToPlaylist(song: Song, playNow: Boolean = false, addToLast: Boolean = false) {
        Log.d(TAG, "addSongToPlaylist: song=${song.title}, is first?=${PlayerManager.playlist.isEmpty()}")
        if (PlayerManager.playlist.isEmpty()) {
            justPlayFirstSong(song)
        } else {
            PlayerManager.player?.let { player ->
                val positionAdded = if (addToLast) {
                    player.mediaItemCount
                } else player.currentMediaItemIndex + 1
                song.url?.let { MediaItem.fromUri(it) }?.let {
                    player.addMediaItem(positionAdded, it)
                    PlayerManager.playlist.add(positionAdded, song)
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
        when (fragment) {
            playFragment -> {
                transaction.setCustomAnimations(R.anim.fragment_play_enter, R.anim.fragment_hold_on)
                lastFragment = currentFragment
            }

            homeFragment -> {
                transaction.setCustomAnimations(R.anim.fragment_hold_on, R.anim.fragment_right_exit)
            }

            else -> {
                transaction.setCustomAnimations(R.anim.fragment_right_enter, R.anim.fragment_hold_on)
            }
        }
        when (currentFragment) {
            playFragment -> {
                if (fragment == homeFragment) {
                    transaction.setCustomAnimations(R.anim.fragment_hold_on, R.anim.fragment_play_exit)
                        .hide(currentFragment).show(fragment).commit()
                    supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                } else {
                    transaction.setCustomAnimations(R.anim.fragment_hold_on, R.anim.fragment_play_exit)
                        .hide(currentFragment).add(R.id.fg_view, fragment).show(fragment).commit()
                    supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                }
            }

            else -> {
                if (fragment == homeFragment) {
                    transaction.hide(currentFragment).show(fragment).commit()
                    supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                } else if (currentFragment != homeFragment) {
                    transaction.remove(currentFragment).add(R.id.fg_view, fragment).show(fragment).commit()
                } else {
                    transaction.hide(currentFragment).add(R.id.fg_view, fragment).show(fragment).commit()
                }
            }
        }

        if (currentFragment == searchFragment) {
            binding.searchKeyword.setText("")
            searchFragment.clearAdapterView()
        }

        currentFragment = fragment

        binding.displayPlayerIv.setImageResource(
            if (currentFragment == playFragment) R.drawable.icon_arrow_down_20 else R.drawable.icon_arrow_up_20
        )
        binding.searchIv.visibility =
            if (currentFragment == homeFragment || currentFragment == searchFragment) View.VISIBLE else View.GONE

        if (currentFragment == searchFragment) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            binding.searchInputLayout.visibility = View.VISIBLE
        } else {
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.searchInputLayout.visibility = View.GONE
        }
        binding.notifyIv.visibility = if (currentFragment == mineFragment) View.VISIBLE else View.GONE
        binding.playAllBtn.visibility = if (currentFragment == favoriteFragment) View.VISIBLE else View.GONE
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (currentFragment != homeFragment) {
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