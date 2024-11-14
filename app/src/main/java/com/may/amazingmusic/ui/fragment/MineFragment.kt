package com.may.amazingmusic.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.databinding.FragmentMineBinding
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.base.BaseFragment
import com.may.amazingmusic.viewmodel.UserViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 *
 * @author May
 * @date 2024/9/15 20:44
 * @description MineFragment
 */
class MineFragment() : BaseFragment<FragmentMineBinding>() {
    private val TAG = this.javaClass.simpleName

    private val userViewModel: UserViewModel by viewModels()

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().lifecycleScope.launch {
            val user = User()
            user.uid = DataStoreManager.userIDFlow.first() ?: 0
            user.userStatus = DataStoreManager.userStatusFlow.first() ?: 0
            user.username = DataStoreManager.userUsernameFlow.first()
            user.password = DataStoreManager.userPasswordFlow.first()
            Log.d(TAG, "onCreate: user=$user")
            binding.user = user
        }

        binding.logOutBtn.setOnClickListener {
            requireActivity().lifecycleScope.launch {
                DataStoreManager.deleteUserID()
                (activity as? MainActivity)?.closeMineAndToLogin()
            }
        }
    }

    override fun setDataBinding(): FragmentMineBinding {
        return FragmentMineBinding.inflate(layoutInflater)
    }
}