package com.may.amazingmusic.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Banner
import com.youth.banner.adapter.BannerAdapter


/**
 * @Author Jensen
 * @Date 2025/3/15 3:38
 */
@SuppressLint("NotifyDataSetChanged")
class MyBannerAdapter(bannerList: List<Banner?>, private val bannerListener: MyBannerClickListener): BannerAdapter<Banner, MyBannerAdapter.BannerViewHolder>(bannerList) {
    private val TAG = this.javaClass.simpleName

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent?.context)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return BannerViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindView(holder: BannerViewHolder, banner: Banner, position: Int, size: Int) {
        Glide.with(appContext).load(banner.pic).diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.amazingmusic).transform(RoundedCorners(50))
            .into(holder.imageView)
        holder.imageView.setOnClickListener { bannerListener.itemClickListener(banner.id) }
    }

    class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

}