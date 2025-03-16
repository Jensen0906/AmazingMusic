package com.may.amazingmusic.service

import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.os.Bundle
import android.service.media.MediaBrowserService
import com.may.amazingmusic.utils.player.PlayerManager

/**
 *
 * @author May
 * @date 2024/10/31 23:56
 * @description TargetService
 */
class TargetService : MediaBrowserService() {

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession(this, "TargetService")
        sessionToken = mediaSession.sessionToken
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                PlayerManager.playPreviousSong()
            }
        })
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        return BrowserRoot("rootId", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowser.MediaItem>>) {
        // nothing to do
    }
}