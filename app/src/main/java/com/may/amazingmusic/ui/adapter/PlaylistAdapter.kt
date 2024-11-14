package com.may.amazingmusic.ui.adapter

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.ItemPlaylistBinding
import com.may.amazingmusic.utils.player.PlayerManager

/**
 *
 * @author May
 * @date 2024/9/17 1:30
 * @description PlaylistAdapter
 */
class PlaylistAdapter(private var playlist: List<Song>, private val clickListener: PlaylistItemClickListener) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {
    private val TAG = this.javaClass.simpleName

    private var currentSongIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaylistBinding.inflate(layoutInflater)
        return PlaylistViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (playlist.isEmpty()) 0 else playlist.size
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        if (playlist.isEmpty() || position >= playlist.size) return
        val song = playlist[position]
        if (position == 0) holder.itemPlaylistBinding.view1.visibility = View.VISIBLE
        holder.itemPlaylistBinding.songTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        holder.itemPlaylistBinding.singer.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        if (position == currentSongIndex) {
            holder.itemPlaylistBinding.songTitle.paint.isFakeBoldText = true
            holder.itemPlaylistBinding.singer.paint.isFakeBoldText = true
            holder.itemPlaylistBinding.songTitle.setTextColor(
                ContextCompat.getColor(appContext, R.color.playlist_playing_song_text_color)
            )
            holder.itemPlaylistBinding.singer.setTextColor(
                ContextCompat.getColor(appContext, R.color.playlist_playing_song_text_color)
            )
        } else {
            holder.itemPlaylistBinding.songTitle.paint.isFakeBoldText = false
            holder.itemPlaylistBinding.singer.paint.isFakeBoldText = false
            holder.itemPlaylistBinding.songTitle.setTextColor(ContextCompat.getColor(appContext, R.color.black))
            holder.itemPlaylistBinding.singer.setTextColor(ContextCompat.getColor(appContext, R.color.black))
        }
        holder.itemPlaylistBinding.song = song
        holder.itemPlaylistBinding.root.setOnClickListener { clickListener.itemClickListener(position) }
        holder.itemPlaylistBinding.removeLayout.setOnClickListener {
            clickListener.itemRemoveListener(position)
        }
    }

    fun setSongToPlaylist() {
        this.playlist = PlayerManager.playlist.toList()
        notifyDataSetChanged()
    }

    fun resetPlaylist(song: Song) {
        playlist = listOf(song)
        notifyItemChanged(0)
    }

    fun removeSongFromList(position: Int) {
        this.playlist = PlayerManager.playlist.toList()
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, playlist.size)
    }

    fun setCurrentSongIndex(currentSongIndex: Int) {
        if (currentSongIndex < 0) return
        val lastIndex = this.currentSongIndex
        this.currentSongIndex = currentSongIndex
        Log.i(TAG, "setCurrentSongIndex: index=${currentSongIndex}, playlist.size=${playlist.size}")
        notifyItemChanged(lastIndex)
        notifyItemChanged(currentSongIndex)
    }

    class PlaylistViewHolder(val itemPlaylistBinding: ItemPlaylistBinding) : RecyclerView.ViewHolder
        (itemPlaylistBinding.root)
}