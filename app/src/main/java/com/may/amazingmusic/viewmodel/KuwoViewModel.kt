package com.may.amazingmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.repository.KuwoRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.isTrue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2025/3/9 11:34
 */
class KuwoViewModel : ViewModel() {
    private val TAG = this.javaClass.simpleName

    private val repository = KuwoRepository()
    private var keyword = ""


    val searchSongs = MutableSharedFlow<List<KuwoSong>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun searchSongs(keyword: String?) {
        if (keyword.isNullOrEmpty()) {
            searchSongs.tryEmit(emptyList())
            return
        }
        this.keyword = keyword
        viewModelScope.launch {
            repository.searchSongs(searchSongs, keyword)
        }
    }

    val selectSong = MutableSharedFlow<KuwoSong?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun selectSong(index: Int, rid: String) {
        if (index <= 0) {
            selectSong.tryEmit(null)
        }
        viewModelScope.launch {
            repository.selectSong(selectSong, keyword, index, rid)
        }
    }

    val isKuwoSource = MutableLiveData(false)
    fun isKuwoSource() {
        viewModelScope.launch {
            DataStoreManager.isKuwoSelected.collect {
                isKuwoSource.postValue(it.isTrue())
            }
        }
    }

}