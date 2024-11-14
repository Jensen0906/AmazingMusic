package com.may.amazingmusic.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.IntentConst.REGISTER_SUCCESS_ACTION
import com.may.amazingmusic.constant.IntentConst.REGISTER_SUCCESS_INTENT_EXTRA
import com.may.amazingmusic.databinding.ActivityRegisterBinding
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.base.BaseActivity
import com.may.amazingmusic.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/10/29 22:02
 * @description RegisterActivity
 */
class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    private val TAG = this.javaClass.simpleName

    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var mUser: User
    private var clickBtnTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initClick()
        liveDataObserve()
    }

    private fun initData() {
        mUser = User()
        binding.user = mUser
    }

    private fun initClick() {
        binding.registerBtn.setOnClickListener {
            userViewModel.register(mUser, binding.password1.text.toString())
            clickBtnTime = System.currentTimeMillis()
            binding.registerBtn.isEnabled = false
            binding.loginProgressBar.visibility = View.VISIBLE
        }
    }

    private fun liveDataObserve() {
        userViewModel.registerUser.observe(this) {
            val timeDiff = System.currentTimeMillis() - clickBtnTime
            lifecycleScope.launch {
                if (timeDiff < 800) {
                    delay(500 - timeDiff)
                }
                binding.loginProgressBar.visibility = View.GONE
                binding.registerBtn.isEnabled = true
            }
            if (it != null) {
                ToastyUtils.success("注册成功，跳转至登录界面")
                Intent(this, LoginActivity::class.java)
                    .apply {
                        this.action = REGISTER_SUCCESS_ACTION
                        this.putExtra(REGISTER_SUCCESS_INTENT_EXTRA, it.username)
                    }
                    .run { startActivity(this) }
                this@RegisterActivity.finish()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }

            else -> {}
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun setDataBinding(): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(layoutInflater)
    }
}