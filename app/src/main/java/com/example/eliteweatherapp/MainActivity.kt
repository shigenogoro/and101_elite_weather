package com.example.eliteweatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.eliteweatherapp.BodyAdapter
import com.example.eliteweatherapp.databinding.ActivityMainBinding
import com.example.eliteweatherapp.ForecastItem
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bodyAdapter: BodyAdapter

    private val executor = Executors.newSingleThreadExecutor()
    private val client = OkHttpClient()
    private val API_KEY = BuildConfig.WEATHER_API_KEY
    private var currentLocation = "Boston"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        bodyAdapter = BodyAdapter(
            onSearch = { location ->
                currentLocation = location
                fetchWeatherData(location, isRefresh = false)
            },
            onRefresh = {
                fetchWeatherData(currentLocation, isRefresh = true)
            }
        )

        binding.bodyContent.layoutManager = LinearLayoutManager(this)
        binding.bodyContent.adapter = bodyAdapter

        fetchWeatherData(currentLocation, isRefresh = false)
    }

    private fun fetchWeatherData(location: String, isRefresh: Boolean) {
        executor.execute {
            try {
                val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$location&days=3&aqi=yes&alerts=no"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: "")

                val city = json.getJSONObject("location").getString("name")
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temp_c").toInt().toString()
                val feelsLike = current.getDouble("feelslike_c").toInt().toString()
                val conditionText = current.getJSONObject("condition").getString("text")
                val iconUrl = "https:" + current.getJSONObject("condition").getString("icon")
                val forecast = json.getJSONObject("forecast").getJSONArray("forecastday")
                val aqi = json.getJSONObject("current").getJSONObject("air_quality")
                val epaIndex = aqi.getDouble("us-epa-index").toInt().toString()

                val forecastItems = mutableListOf<ForecastItem>()
                for (i in 0 until 3) {
                    val day = forecast.getJSONObject(i)
                    val date = day.getString("date")
                    val avgTemp = day.getJSONObject("day").getDouble("avgtemp_c").toInt()
                    val icon = "https:" + day.getJSONObject("day")
                        .getJSONObject("condition").getString("icon")
                    val label = when (i) {
                        0 -> "Today"
                        1 -> "Tomorrow"
                        else -> date
                    }
                    forecastItems.add(ForecastItem(label, avgTemp.toString(), icon))
                }

                runOnUiThread {
                    bodyAdapter.updateData(
                        city = city,
                        temp = temp,
                        feelsLike = feelsLike,
                        conditionText = conditionText,
                        iconUrl = iconUrl,
                        forecastList = forecastItems,
                        airQualityIndex = epaIndex
                    )

                    if (isRefresh) {
                        Toast.makeText(this, "Weather refreshed for $city", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to fetch weather for $location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
