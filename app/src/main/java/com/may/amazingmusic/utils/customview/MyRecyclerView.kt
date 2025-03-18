package com.may.amazingmusic.utils.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerView(context: Context, attrs: AttributeSet?=null) : RecyclerView(context, attrs) {
    private var mLastY = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (ev.y > mLastY && !canScrollVertically(-1)) {
                mLastY = ev.y
                super.dispatchTouchEvent(ev)
                return false
            }
        }
        mLastY = ev.y
        return super.dispatchTouchEvent(ev)
    }

    private lateinit var mParentView: ViewGroup
    private var maxH: Int = 0

    fun init(parentView: ViewGroup, maxH: Float) {
        this.mParentView = parentView
        this.maxH = maxH.toInt()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val hSpec = MeasureSpec.makeMeasureSpec(maxH, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, hSpec)
    }
}