package com.example.eliteweatherapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eliteweatherapp.BuildConfig
import com.codepath.asynchttpclient.AsyncHttpClient
import com.example.eliteweatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        // Read the API key from the local.properties file


    }

}