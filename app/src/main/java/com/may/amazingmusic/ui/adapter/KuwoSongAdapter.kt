package com.may.amazingmusic.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.databinding.ItemKuwoSongBinding
import com.may.amazingmusic.databinding.ItemSearchFooterBinding

/**
 * @Author Jensen
 * @Date 2025/3/9 11:57
 */
class KuwoSongAdapter(
    private var songList: List<KuwoSong>,
    private val clickListener: KuwoSongClickListener,
    private var isLoading: Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            val song = songList[position]
            holder.itemKuwoSongBinding.song = song
            Glide.with(appContext)
                .load(song.pic)
                .placeholder(R.drawable.amazingmusic).error(R.drawable.amazingmusic)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(holder.itemKuwoSongBinding.songImg)
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

    fun setLoading(loading: Boolean) {
        this.isLoading = loading
        notifyItemChanged(songList.size)
    }

    class SongViewHolder(val itemKuwoSongBinding: ItemKuwoSongBinding) : RecyclerView.ViewHolder(itemKuwoSongBinding.root)
    class SongViewHolderFooter(val itemSearchFooterBinding: ItemSearchFooterBinding) : RecyclerView.ViewHolder(itemSearchFooterBinding.root)
}