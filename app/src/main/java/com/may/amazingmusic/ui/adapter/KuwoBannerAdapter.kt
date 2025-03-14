package com.may.amazingmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Banner
import com.may.amazingmusic.databinding.ItemBannerBinding
import com.youth.banner.adapter.BannerAdapter

/**
 * @Author Jensen
 * @Date 2025/3/15 3:38
 */
class KuwoBannerAdapter(var bannerList: List<Banner?>): BannerAdapter<Banner, KuwoBannerAdapter.BannerViewHolder>(bannerList) {

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val binding = ItemBannerBinding.inflate(layoutInflater)
        return BannerViewHolder(binding)
    }

    override fun onBindView(holder: BannerViewHolder, banner: Banner, position: Int, size: Int) {
        Glide.with(appContext).load(banner.pic).diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.amazingmusic).into(holder.itemBannerBinding.itemBannerIv)
    }

    class BannerViewHolder(val itemBannerBinding: ItemBannerBinding) : RecyclerView.ViewHolder(itemBannerBinding.root)
}