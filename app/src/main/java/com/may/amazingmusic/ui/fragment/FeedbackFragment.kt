package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.may.amazingmusic.databinding.FragmentFeedbackBinding
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.viewmodel.FeedbackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/9/15 20:45
 * @description SettingsFragment
 */
class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>() {
    private val TAG = this.javaClass.simpleName

    private val feedbackViewModel by viewModels<FeedbackViewModel>()
    private var btnClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            feedbackViewModel.addFeedbackResult.collect {
                val timeDiff = System.currentTimeMillis() - btnClickTime
                if (timeDiff < 800) {
                    delay(800 - timeDiff)
                }
                binding.feedbackProgressBar.visibility = View.GONE
                binding.feedbackBtn.isEnabled = true
                if (it.orZero() == 0) ToastyUtils.error("反馈失败！！")
                else {
                    ToastyUtils.success("~反馈成功~")
                    binding.feedbackInfo.setText("")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.feedbackBtn.setOnClickListener {
            binding.feedbackBtn.isEnabled = false
            binding.feedbackProgressBar.visibility = View.VISIBLE
            btnClickTime = System.currentTimeMillis()
            feedbackViewModel.addFeedback(binding.feedbackInfo.text?.toString())
        }
    }

    override fun setDataBinding(): FragmentFeedbackBinding {
        return FragmentFeedbackBinding.inflate(layoutInflater)
    }
}