package com.example.eliteweatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.eliteweatherapp.BuildConfig
import com.example.eliteweatherapp.databinding.ActivityMainBinding
import okhttp3.Headers
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val baseURL = "https://api.weatherapi.com/v1/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Trigger Search on Enter Key
        binding.searchLocation.setOnEditorActionListener { v, _, _ ->
            val location = v.text.toString()
            if(location.isNotEmpty()) {
                fetchWeatherData(location)
            }
            true
        }

        // Default Fetch
        fetchWeatherData("Amherst")
    }

    private fun fetchWeatherData(location: String) {
        val client = AsyncHttpClient()

        // Fetch Real-time Data
        val real_time_url = baseURL + "forecast.json" + "?key=${BuildConfig.WEATHER_API_KEY}" + "&q=${location}&days=3&api=yes"

        client.get(real_time_url, object: JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {

            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String, throwable: Throwable) {

            }
        })
    }

}