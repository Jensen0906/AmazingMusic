package com.may.amazingmusic.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.bean.SongList
import com.may.amazingmusic.databinding.ItemSearchFooterBinding
import com.may.amazingmusic.databinding.ItemSongListsBinding
import com.may.amazingmusic.utils.globalGlideOptions
import com.may.amazingmusic.utils.spToPx

/**
 * @Author Jensen
 * @Date 2025/3/17 15:08
 */
@SuppressLint("NotifyDataSetChanged")
class KuwoSongListsAdapter(
    private var songLists: List<SongList>,
    private val clickListener: SongListClickListener,
    private var isLoading: Boolean = true
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = this.javaClass.simpleName

    private val ITEM_DATA = 0
    private val ITEM_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == ITEM_DATA) {
            SongListViewHolder(ItemSongListsBinding.inflate(layoutInflater, parent, false))
        } else {
            SongListViewHolderFooter(ItemSearchFooterBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == songLists.size) ITEM_LOADING else ITEM_DATA
    }

    override fun getItemCount(): Int {
        return if (songLists.isEmpty()) 0 else songLists.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SongListViewHolder) {
            if (songLists.isEmpty() || position >= songLists.size) return

            val songList = songLists[position]

            holder.itemSongListsBinding.songList = songList
            Glide.with(appContext)
                .load(songList.pic)
                .apply(globalGlideOptions(20f.spToPx(appContext).toInt()))
                .into(holder.itemSongListsBinding.songListIv)
            holder.itemSongListsBinding.root.setOnClickListener { clickListener.itemClickListener(songList.rid, songList.pic, songList.name) }
        } else if (holder is SongListViewHolderFooter) {
            holder.itemSearchFooterBinding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    fun setLoading(loading: Boolean) {
        this.isLoading = loading
        notifyItemChanged(if (songLists.isEmpty()) 0 else songLists.size)
    }


    fun updateSongLists(songLists: List<SongList>, addLast: Boolean = false) {
        val last = this.songLists.size - 1
        this.songLists = songLists
        if (addLast) notifyItemInserted(last) else notifyDataSetChanged()
    }

    class SongListViewHolder(val itemSongListsBinding: ItemSongListsBinding) :
        RecyclerView.ViewHolder(itemSongListsBinding.root)

    class SongListViewHolderFooter(val itemSearchFooterBinding: ItemSearchFooterBinding) :
        RecyclerView.ViewHolder(itemSearchFooterBinding.root)
}