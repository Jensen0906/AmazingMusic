package com.may.amazingmusic.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.constant.IntentConst.ALARM_TIME_MINUTE
import com.may.amazingmusic.service.AlarmService
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.isFalse
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import com.may.amazingmusic.viewmodel.FeedbackViewModel
import com.may.amazingmusic.viewmodel.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2024/11/24 12:07
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = this.javaClass.simpleName

    private lateinit var songViewModel: SongViewModel
    private val feedbackViewModel by viewModels<FeedbackViewModel>()
    private var feedbackEdit: EditTextPreference? = null
    private var feedback: Preference? = null
    private var timerSwitch: SwitchPreference? = null
    private var timeList: ListPreference? = null
    private var untilPlayCompleted: SwitchPreference? = null
    private var sourceSelect: ListPreference? = null

    private var btnClickTime = 0L

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        initPreferences()

        setFeedbackCategory()
        setTimerCategory()
        setSourceCategory()

        lifecycleScope.launch {
            feedbackViewModel.addFeedbackResult.collect {
                val timeDiff = System.currentTimeMillis() - btnClickTime
                if (timeDiff < 800) {
                    delay(800 - timeDiff)
                }
                feedback?.isEnabled = true
                Log.d(TAG, "onViewCreated: feedbackResult = $it")
                if (it.orZero() == 0) ToastyUtils.error("反馈失败！！")
                else {
                    ToastyUtils.success("~反馈成功~")
                    feedbackEdit?.setText("")
                }
            }
        }
    }

    private fun initPreferences() {
        feedbackEdit = findPreference("feedback_ed")
        feedback = findPreference("feedback_pf")

        timerSwitch = findPreference("timer_switch")
        timeList = findPreference("time_list")
        untilPlayCompleted = findPreference("until_play_completed")

        sourceSelect = findPreference("source_list")

        lifecycleScope.launch {
            DataStoreManager.timerOpenedFlow.collect {
                timerSwitch?.isChecked = it.isTrue()
            }
        }
        timerSwitch?.summaryOn = getString(R.string.timer_summary_on, timeList?.value)
        timerSwitch?.isChecked = PlayerManager.playlist.isNotEmpty()
        timerSwitch?.isEnabled = PlayerManager.playlist.isNotEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PlayerManager.disableTimer.observe(viewLifecycleOwner) {
            if (PlayerManager.playlist.isEmpty() && it.isFalse()) {
                lifecycleScope.launch {
                    DataStoreManager.updateTimerOpened(false)
                }
                timerSwitch?.isChecked = false
                timerSwitch?.isEnabled = false
            }
        }
    }

    private fun setFeedbackCategory() {
        feedbackEdit?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            if (it.text.isNullOrBlank()) "" else it.text
        }
        feedback?.isEnabled = feedbackEdit?.text.isNullOrBlank().isFalse()
        feedbackEdit?.setOnPreferenceChangeListener { _, newValue ->
            feedback?.isEnabled = (newValue as String).isBlank().isFalse()
            true
        }
        feedback?.setOnPreferenceClickListener {
            it.isEnabled = false
            btnClickTime = System.currentTimeMillis()
            feedbackViewModel.addFeedback(feedbackEdit?.text)
            true
        }
    }

    private fun setTimerCategory() {
        timerSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (PlayerManager.playlist.isEmpty()) return@setOnPreferenceChangeListener true
            val isOpen = newValue as Boolean
            lifecycleScope.launch {
                DataStoreManager.updateTimerOpened(isOpen)
            }
            if (isOpen) startTimer() else cancelTimer()
            true
        }

        untilPlayCompleted?.setOnPreferenceChangeListener { _, newValue ->
            lifecycleScope.launch {
                DataStoreManager.updateStopUntilPlayCompleted(newValue as Boolean)
            }
            true
        }

        timeList?.setOnPreferenceChangeListener { _, newValue ->
            if (timerSwitch?.isChecked.isTrue()) {
                timerSwitch?.summaryOn = getString(R.string.timer_summary_on, newValue)
            }
            cancelTimer()
            startTimer((newValue as String).toInt())
            true
        }
    }

    private fun setSourceCategory() {
        sourceSelect?.setOnPreferenceChangeListener { _, newValue ->
            lifecycleScope.launch {
                DataStoreManager.updateKuwoSourceSelected((newValue as String) == "kuwo")
            }
            true
        }
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun startTimer(timeInMinutes: Int = 0) {
        val intent = Intent(requireActivity(), AlarmService::class.java)
        intent.putExtra(ALARM_TIME_MINUTE, if (timeInMinutes == 0) timeList?.value?.toInt() else timeInMinutes)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun cancelTimer() {
        requireActivity().unbindService(connection)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(appContext, android.R.color.white))
        return view
    }
}