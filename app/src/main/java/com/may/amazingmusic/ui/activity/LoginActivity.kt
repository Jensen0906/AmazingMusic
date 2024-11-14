package com.may.amazingmusic.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.IntentConst.REGISTER_SUCCESS_ACTION
import com.may.amazingmusic.constant.IntentConst.REGISTER_SUCCESS_INTENT_EXTRA
import com.may.amazingmusic.databinding.ActivityLoginBinding
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.base.BaseActivity
import com.may.amazingmusic.utils.changePassShow
import com.may.amazingmusic.utils.changeShowType
import com.may.amazingmusic.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2023/10/07
 */

@OptIn(UnstableApi::class)
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private val TAG = this.javaClass.simpleName

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var mUser: User
    private var clickLoginBtnTime = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewAndData()
        initClick()
        liveDataObserve()
    }

    private fun initViewAndData() {
        mUser = User()
        binding.user = mUser

        if (intent.action == REGISTER_SUCCESS_ACTION) {
            mUser.username = intent.getStringExtra(REGISTER_SUCCESS_INTENT_EXTRA)
        }

        binding.password.changeShowType(binding.showPassword, binding.notShowPassword)
    }

    private fun liveDataObserve() {
        userViewModel.userLiveData.observe(this) {
            val timeDiff = System.currentTimeMillis() - clickLoginBtnTime
            lifecycleScope.launch {
                if (timeDiff < 800) {
                    delay(500 - timeDiff)
                }
                binding.loginProgressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                if (it == null) return@launch
                if (mUser.username == it.username) {
                    DataStoreManager.saveUserInfo(user = it)
                    finish()
                }
            }
        }
    }

    /**
     * init all click listener
     * */
    private fun initClick() {
        binding.btnLogin.setOnClickListener {
            Log.d(TAG, mUser.toString())
            binding.btnLogin.isEnabled = false
            binding.loginProgressBar.visibility = View.VISIBLE
            clickLoginBtnTime = System.currentTimeMillis()
            userViewModel.login(mUser)
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.showPassword.changePassShow(binding.password, needShow = false)
        binding.notShowPassword.changePassShow(password = binding.password, needShow = true)
    }

    override fun setDataBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }
}