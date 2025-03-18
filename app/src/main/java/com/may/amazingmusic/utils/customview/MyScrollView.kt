package com.may.amazingmusic.utils.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import java.security.AccessControlContext

class MyScrollView(context: Context, attrs: AttributeSet?=null) : ScrollView(context, attrs) {
    private var mLastY = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (ev.y < mLastY && canScrollVertically(1)) {
                super.onInterceptTouchEvent(ev)
                mLastY = ev.y
                return true
            }
        }
        mLastY = ev.y
        return super.onInterceptTouchEvent(ev)
    }
}