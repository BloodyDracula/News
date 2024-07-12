package com.example.news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.api.NewsApiService
import com.example.news.model.Article
import com.example.news.model.NewsResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsViewModel : ViewModel() {

    private val _news = MutableLiveData<List<Article>>()
    val news: LiveData<List<Article>> get() = _news

    fun fetchTopHeadlines(country: String, language: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val call = NewsApiService.getApi().getTopHeadlines(country, language, apiKey)
                call.enqueue(object : Callback<NewsResponse> {
                    override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                        if (response.isSuccessful) {
                            _news.value = response.body()?.articles
                        } else {
                            println("Failed to fetch top headlines: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                        println("Error fetching top headlines: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                println("Error fetching top headlines: ${e.message}")
            }
        }
    }
}
