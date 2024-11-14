package com.may.amazingmusic.utils.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 *
 * @author May
 * @date 2024/9/15 21:05
 * @description BaseFragment
 */
abstract class BaseFragment<VDB : ViewDataBinding> : Fragment() {
    protected lateinit var binding: VDB

    override fun onAttach(context: Context) {
        binding = setDataBinding()
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    protected abstract fun setDataBinding(): VDB
}