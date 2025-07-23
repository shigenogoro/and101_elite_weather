package com.example.eliteweatherapp

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.eliteweatherapp.databinding.ActivityMainBinding
import okhttp3.Headers
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bodyAdapter: BodyAdapter

    private val API_KEY = BuildConfig.WEATHER_API_KEY
    private val BASE_URL = "https://api.weatherapi.com/v1"
    private var currentLocation = "Amherst"

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


        // Set up RecyclerView
        bodyAdapter = BodyAdapter(
            onRefresh = {
                fetchWeatherData(currentLocation, isRefresh = true)
            }
        )

        binding.bodyContent.layoutManager = LinearLayoutManager(this)
        binding.bodyContent.adapter = bodyAdapter

        // Initial fetch
        fetchWeatherData(currentLocation, isRefresh = false)
    }

    private fun fetchWeatherData(location: String, isRefresh: Boolean) {
        Log.d("API_REQUEST", "Requesting weather for location: $location")

        val client = AsyncHttpClient()
        val url = "$BASE_URL/forecast.json?key=$API_KEY&q=$location&days=3&aqi=yes"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                try {
                    val jsonObj = json?.jsonObject ?: return

                    val city = jsonObj.getJSONObject("location").getString("name")
                    val current = jsonObj.getJSONObject("current")
                    val condition = current.getJSONObject("condition")
                    val temp = current.getDouble("temp_c").toInt()
                    val feelsLike = current.getDouble("feelslike_c").toInt()
                    val conditionText = condition.getString("text")
                    val iconUrl = "https:" + condition.getString("icon")

                    val forecastArray = jsonObj.getJSONObject("forecast").getJSONArray("forecastday")
                    val aqi = current.getJSONObject("air_quality")
                    val epaIndex = aqi.getDouble("us-epa-index").toInt()

                    val forecastItems = mutableListOf<ForecastItem>()
                    for (i in 0 until 3) {
                        val day = forecastArray.getJSONObject(i)
                        val date = day.getString("date")
                        val avgTemp = day.getJSONObject("day").getDouble("avgtemp_c").toInt()
                        val icon = "https:" + day.getJSONObject("day")
                            .getJSONObject("condition").getString("icon")
                        val label = when (i) {
                            0 -> "Today"
                            1 -> "Tomorrow"
                            else -> date
                        }
                        forecastItems.add(ForecastItem(label, avgTemp, icon))
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
                            Toast.makeText(
                                this@MainActivity,
                                "Weather refreshed for $city",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    Log.d("WEATHER", "Fetched weather for: $city")

                } catch (e: Exception) {
                    Log.e("WEATHER", "Parsing error", e)
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to parse weather data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e("WEATHER", "API call failed", throwable)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch weather for $location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
