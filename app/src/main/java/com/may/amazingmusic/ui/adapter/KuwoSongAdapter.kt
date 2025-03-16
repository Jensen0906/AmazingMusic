package com.may.amazingmusic.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.databinding.ItemKuwoSongBinding
import com.may.amazingmusic.databinding.ItemSearchFooterBinding
import com.may.amazingmusic.utils.globalGlideOptions

/**
 * @Author Jensen
 * @Date 2025/3/9 11:57
 */
@SuppressLint("NotifyDataSetChanged")
class KuwoSongAdapter(
    private var songList: List<KuwoSong>,
    private val clickListener: KuwoSongClickListener,
    private var isLoading: Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = this.javaClass.simpleName

    private val ITEM_DATA = 0
    private val ITEM_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == ITEM_DATA) {
            SongViewHolder(ItemKuwoSongBinding.inflate(layoutInflater, parent, false))
        } else {
            SongViewHolderFooter(ItemSearchFooterBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SongViewHolder) {
            if (songList.isEmpty() || position >= songList.size) return
//            if (isFavoriteAdapter) holder.itemKuwoSongBinding.favoriteIv.visibility = View.GONE

            val song = songList[position]

            song.isFavorite = (song.rid in favIds)
            holder.itemKuwoSongBinding.favoriteIv.setImageResource(
                if (song.isFavorite) R.drawable.ic_favorite else R.drawable.ic_no_favorite
            )


            holder.itemKuwoSongBinding.song = song
            Glide.with(appContext)
                .load(song.pic)
                .apply(globalGlideOptions(30))
                .into(holder.itemKuwoSongBinding.songImg)
            holder.itemKuwoSongBinding.addToPlaylist.setOnClickListener { clickListener.addSongToList(song) }
            holder.itemKuwoSongBinding.favoriteIv.setOnClickListener { clickListener.favoriteClickListener(song, position) }
            holder.itemKuwoSongBinding.root.setOnClickListener { clickListener.itemClickListener(song) }
        } else if (holder is SongViewHolderFooter) {
            holder.itemSearchFooterBinding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == songList.size) ITEM_LOADING else ITEM_DATA
    }

    override fun getItemCount(): Int {
        return if (songList.isEmpty()) 0 else songList.size + 1
    }


    fun updateSongs(newSongList: List<KuwoSong>, page: Int) {
        this.songList = newSongList
        notifyItemInserted(page * 10 - 1)
    }

    fun updateSongs(newSongList: List<KuwoSong>) {
        this.songList = newSongList
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        this.isLoading = loading
        notifyItemChanged(songList.size)
    }

    fun updateFavoriteSong(rid: Long, position: Int, isFavorite: Boolean) {
        if (position >= itemCount) return
        val mutableListFavIds = favIds.toMutableList()
        if (isFavorite) mutableListFavIds.add(rid)
        else mutableListFavIds.remove(rid)
        favIds = mutableListFavIds.toList()
        notifyItemChanged(position)
    }

    private var isFavoriteAdapter = false
    fun setFavoriteKuwoSongs(kuwoSongs: List<KuwoSong>?) {
        if (kuwoSongs.isNullOrEmpty()) {
            this.songList = emptyList()
        } else {
            this.songList = kuwoSongs
            val rids = mutableListOf<Long>()
            kuwoSongs.forEach {
                rids.add(it.rid)
            }
            favIds = rids
        }
        isFavoriteAdapter = true
        notifyDataSetChanged()
    }

    private var favIds: List<Long> = listOf()
    var hasSetFavorite = false
    fun setFavoriteKuwoSongRids(favoriteIds: List<Long>?) {
        if (favoriteIds.isNullOrEmpty()) this.favIds = emptyList()
        else this.favIds = favoriteIds
        hasSetFavorite = true
    }

    class SongViewHolder(val itemKuwoSongBinding: ItemKuwoSongBinding) : RecyclerView.ViewHolder(itemKuwoSongBinding.root)
    class SongViewHolderFooter(val itemSearchFooterBinding: ItemSearchFooterBinding) : RecyclerView.ViewHolder(itemSearchFooterBinding.root)
}