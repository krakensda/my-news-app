package com.fajarsn.mynewsapp.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.fajarsn.mynewsapp.BuildConfig
import java.net.ConnectException
import java.net.UnknownHostException

sealed class Result private constructor() {
    data class Success<out T>(val data: T) : Result()
    data class Error(val error: String) : Result()
    object Loading : Result()
}

class Repository private constructor(private val service: ApiService) {
    suspend fun getSources(category: String, liveData: MutableLiveData<Result>) = try {
        val response = service.getSources(category)
        liveData.value = Result.Success(response)
    } catch (exception: Exception) {
        catchError(exception, liveData)
    }

    private fun catchError(exception: Exception, liveData: MutableLiveData<Result>) =
        when (exception) {
            is UnknownHostException -> liveData.value =
                Result.Error("Please check your internet connection and try again.")
            is ConnectException -> liveData.value =
                Result.Error("Please check your internet connection and try again.")
            else -> liveData.value = exception.message?.let {
                if (BuildConfig.DEBUG) {
                    Log.e("Repository", "${exception.message}\n")
                    exception.printStackTrace()
                }
                Result.Error(it)
            } as Result.Error
        }

    companion object {
        @Volatile
        private var instance: Repository? = null

        fun getInstance(service: ApiService) =
            instance ?: synchronized(this) { instance ?: Repository(service) }.also {
                instance = it
            }
    }
}