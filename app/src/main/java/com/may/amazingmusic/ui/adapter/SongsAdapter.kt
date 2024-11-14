package com.may.amazingmusic.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.databinding.ItemSongBinding

/**
 *
 * @author May
 * @date 2024/9/16 12:36
 * @description SongsAdapter
 */
class SongsAdapter(private var songList: List<Song>, private val clickListener: SongsItemClickListener) :
    RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {
    private val TAG = this.javaClass.simpleName
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSongBinding.inflate(layoutInflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (songList.isEmpty()) 0 else songList.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (songList.isEmpty() || position >= songList.size) return
        val song = songList[position]

        if (isFavoriteAdapter) holder.itemSongBinding.favoriteIv.visibility = View.GONE
        song.isFavorite = (song.sid in favIds)

        holder.itemSongBinding.favoriteIv.setImageResource(
            if (song.isFavorite) R.drawable.ic_favorite else R.drawable.ic_no_favorite
        )
        holder.itemSongBinding.song = song
        holder.itemSongBinding.addToPlaylist.setOnClickListener { clickListener.addSongToList(song) }
        holder.itemSongBinding.displaySongInfo.setOnClickListener { clickListener.showSongInfo(song) }
        holder.itemSongBinding.favoriteIv.setOnClickListener { clickListener.favorite(song, position) }
        holder.itemSongBinding.root.setOnClickListener { clickListener.itemClickListener(song) }
    }

    fun updateSongs(newSongList: List<Song>, page: Int) {
        this.songList = newSongList
        notifyItemInserted(page * 15 - 1)
    }

    fun updateSongs(newSongList: List<Song>) {
        this.songList = newSongList
        notifyDataSetChanged()
    }

    /**
     * use for song list
     */
    private var favIds: List<Long> = listOf()
    var hasSetFavorite = false
    fun setFavoriteSongIds(favoriteIds: List<Long>?) {
        Log.d(TAG, "setFavoriteSongIds: favoriteIds=$favoriteIds")
        if (favoriteIds.isNullOrEmpty()) this.favIds = emptyList()
        else this.favIds = favoriteIds
        hasSetFavorite = true
    }

    /**
     * use for favorite song list
     */
    private var isFavoriteAdapter = false
    fun setFavoriteSongs(favoriteSongs: List<Song>?) {
        if (favoriteSongs.isNullOrEmpty()) {
            favIds = emptyList()
            this.songList = emptyList()
        } else {
            favIds = favoriteSongs.map { it.sid }
            this.songList = favoriteSongs
        }
        isFavoriteAdapter = true
        notifyDataSetChanged()
    }

    fun updateFavoriteSong(sid: Long, position: Int, isFavorite: Boolean) {
        Log.d(TAG, "updateFavoriteSong: sid=$sid, isFavorite=$isFavorite, itemCount=$itemCount")
        if (position >= itemCount) return
        val mutableListFavIds = favIds.toMutableList()
        if (isFavorite) mutableListFavIds.add(sid)
        else mutableListFavIds.remove(sid)
        favIds = mutableListFavIds.toList()
        notifyItemChanged(position)
    }

    class SongViewHolder(val itemSongBinding: ItemSongBinding) : RecyclerView.ViewHolder(itemSongBinding.root)
}