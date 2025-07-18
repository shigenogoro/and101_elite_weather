package com.example.eliteweatherapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.eliteweatherapp.BuildConfig
import com.example.eliteweatherapp.databinding.ActivityMainBinding
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val baseURL = "https://api.weatherapi.com/v1/"
    private var currentLocation: String = "Amherst" // Store current location for refresh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Trigger Search on Enter Key
        binding.searchLocation.setOnEditorActionListener { v, _, _ ->
            val location = v.text.toString()
            if(location.isNotEmpty()) {
                // Update current location and fetch weather
                currentLocation = location
                fetchWeatherData(location, isRefresh = false)
                hideKeyboard()
            }
            true
        }

        // Trigger Refresh Button
        binding.refreshButton.setOnClickListener {
            // Use stored current location for refresh
            fetchWeatherData(currentLocation, isRefresh = true)
        }

        // Default Fetch
        fetchWeatherData("Amherst", isRefresh = false)
    }

    private fun fetchWeatherData(location: String, isRefresh: Boolean = false) {
        val client = AsyncHttpClient()
        val url = "$baseURL/forecast.json?key=${BuildConfig.WEATHER_API_KEY}&q=$location&days=3&aqi=yes"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                try {
                    val body = json?.jsonObject ?: return
                    val locationObj = body.getJSONObject("location")
                    val current = body.getJSONObject("current")
                    val condition = current.getJSONObject("condition")
                    val forecast = body.getJSONObject("forecast").getJSONArray("forecastday")
                    val airQuality = current.getJSONObject("air_quality")

                    // Current weather
                    val city = locationObj.getString("name")
                    val temp = current.getDouble("temp_c").toInt()
                    val feelsLike = current.getDouble("feelslike_c").toInt()
                    val conditionText = condition.getString("text")
                    val iconUrl = "https:" + condition.getString("icon")

                    // Store the current location for refresh usage
                    currentLocation = location

                    Log.d("WEATHER", "City: $city | Temp: $temp | Feels: $feelsLike | $conditionText")

                    runOnUiThread {
                        binding.currentLocation.text = city
                        binding.currentTemp.text = "$temp°C"
                        binding.feellikeTemp.text = "Feels like: $feelsLike°C"
                        binding.currentCondition.text = conditionText
                        Glide.with(this@MainActivity).load(iconUrl).into(binding.currentWeatherIcon)

                        // Show toast if this is a refresh action
                        if (isRefresh) {
                            Toast.makeText(this@MainActivity, "Weather refreshed for $city", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Forecast loop (Today, Tomorrow, +1)
                    val dayLabels = arrayOf(binding.day1Label, binding.day2Label, binding.day3Label)
                    val dayTemps = arrayOf(binding.day1Temp, binding.day2Temp, binding.day3Temp)
                    val dayIcons = arrayOf(binding.day1Icon, binding.day2Icon, binding.day3Icon)

                    for (i in 0 until 3) {
                        val forecastDay = forecast.getJSONObject(i)
                        val date = forecastDay.getString("date")
                        val avgTemp = forecastDay.getJSONObject("day").getDouble("avgtemp_c").toInt()
                        val icon = "https:" + forecastDay.getJSONObject("day")
                            .getJSONObject("condition").getString("icon")

                        Log.d("FORECAST", "Day $i: $date | $avgTemp°C")

                        runOnUiThread {
                            dayLabels[i].text = when (i) {
                                0 -> "Today"
                                1 -> "Tomorrow"
                                else -> date
                            }
                            dayTemps[i].text = "$avgTemp°C"
                            Glide.with(this@MainActivity).load(icon).into(dayIcons[i])
                        }
                    }

                    // Air Quality
                    val epaIndex = airQuality.getInt("us-epa-index")
                    runOnUiThread {
                        binding.airQualityIndex.text = "Index $epaIndex"
                    }

                } catch (e: Exception) {
                    Log.e("WEATHER", "Parsing error", e)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.e("WEATHER", "API call failed", throwable)
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        currentFocus?.let {
            view -> imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}