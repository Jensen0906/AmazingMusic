package com.may.amazingmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.databinding.ItemKuwoSongBinding

/**
 * @Author Jensen
 * @Date 2025/3/9 11:57
 */
class KuwoSongAdapter(private var songList: List<KuwoSong>, private val clickListener: KuwoSongClickListener) :
    RecyclerView.Adapter<KuwoSongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemKuwoSongBinding.inflate(layoutInflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (songList.isEmpty()) 0 else songList.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (songList.isEmpty() || position >= songList.size) return
        val song = songList[position]

        holder.itemKuwoSongBinding.song = song
        Glide.with(appContext)
            .load(song.pic)
            .placeholder(R.drawable.amazingmusic).error(R.drawable.amazingmusic)
            .transform(CenterCrop(), RoundedCorners(30))
            .into(holder.itemKuwoSongBinding.songImg)
        holder.itemKuwoSongBinding.root.setOnClickListener { clickListener.itemClickListener(song) }
    }

    fun updateSongs(newSongList: List<KuwoSong>, isLastOne: Boolean = false) {
        this.songList = newSongList
        if (isLastOne) notifyItemChanged(songList.size) else notifyDataSetChanged()
    }

    class SongViewHolder(val itemKuwoSongBinding: ItemKuwoSongBinding) : RecyclerView.ViewHolder(itemKuwoSongBinding.root)
}