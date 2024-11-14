package com.may.amazingmusic.utils.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding

/**
 * @Author Jensen
 * @Date 2023/10/07
 */

abstract class BaseActivity<VDB : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: VDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setDataBinding()
        setContentView(binding.root)
    }

    protected abstract fun setDataBinding(): VDB
}