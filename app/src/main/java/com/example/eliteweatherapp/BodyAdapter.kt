package com.example.eliteweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eliteweatherapp.databinding.HourlyForecastContainerBinding

class BodyAdapter(
    private var city: String = "",
    private var temp: String = "",
    private var feelsLike: String = "",
    private var conditionText: String = "",
    private var iconUrl: String = "",
    private var forecastList: List<ForecastItem> = listOf(),
    private var hourlyForecastList: List<HourlyForecastItem> = listOf(),
    private var airQualityIndex: String = "",
    private val onRefresh: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CURRENT = 0
        private const val TYPE_HOURLY_FORECAST = 1
        private const val TYPE_REFRESH_BUTTON = 2
        private const val TYPE_FORECAST_LIST = 3
        private const val TYPE_AQI = 4
    }

    override fun getItemCount(): Int = 5

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_CURRENT
        1 -> TYPE_HOURLY_FORECAST
        2 -> TYPE_REFRESH_BUTTON
        3 -> TYPE_FORECAST_LIST
        4 -> TYPE_AQI
        else -> throw IllegalArgumentException("Invalid view type")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_CURRENT -> {
                val view = inflater.inflate(R.layout.current_weather_container, parent, false)
                CurrentWeatherViewHolder(view)
            }
            TYPE_HOURLY_FORECAST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.hourly_forecast_container, parent, false)
                HourlyForecastViewHolder(HourlyForecastContainerBinding.bind(view))
            }
            TYPE_REFRESH_BUTTON -> {
                val view = inflater.inflate(R.layout.refresh_button, parent, false)
                RefreshButtonViewHolder(view)
            }
            TYPE_FORECAST_LIST -> {
                val view = inflater.inflate(R.layout.forecast_container, parent, false)
                ForecastListViewHolder(view)
            }
            TYPE_AQI -> {
                val view = inflater.inflate(R.layout.air_quality_container, parent, false)
                AirQualityViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CurrentWeatherViewHolder -> {
                holder.location.text = city
                holder.temp.text = "$temp°C"
                holder.feelsLike.text = "Feels like: $feelsLike°C"
                holder.condition.text = conditionText
                Glide.with(holder.icon.context).load(iconUrl).into(holder.icon)
            }

            is ForecastListViewHolder -> {
                holder.bind(forecastList)
            }

            is AirQualityViewHolder -> {
                holder.index.text = "Index $airQualityIndex"
            }

            is RefreshButtonViewHolder -> {
                holder.button.setOnClickListener {
                    onRefresh()
                }
            }

            is HourlyForecastViewHolder -> {
                holder.binding.hourlyForecastHeader.text = "Hourly Forecast"
                holder.bind(hourlyForecastList)
            }
        }
    }

    fun updateData(
        city: String,
        temp: Int,
        feelsLike: Int,
        conditionText: String,
        iconUrl: String,
        forecastList: List<ForecastItem>,
        hourlyForecastList: List<HourlyForecastItem>,
        airQualityIndex: Int
    ) {
        this.city = city
        this.temp = temp.toString()
        this.feelsLike = feelsLike.toString()
        this.conditionText = conditionText
        this.iconUrl = iconUrl
        this.forecastList = forecastList
        this.hourlyForecastList = hourlyForecastList
        this.airQualityIndex = airQualityIndex.toString()
        notifyDataSetChanged()
    }

    class CurrentWeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val location: TextView = view.findViewById(R.id.current_location)
        val temp: TextView = view.findViewById(R.id.current_temp)
        val feelsLike: TextView = view.findViewById(R.id.feellike_temp)
        val condition: TextView = view.findViewById(R.id.current_condition)
        val icon: ImageView = view.findViewById(R.id.current_weather_icon)
    }

    class HourlyForecastViewHolder(val binding: HourlyForecastContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hourlyList: List<HourlyForecastItem>) {
            val context = binding.root.context

            // Set up horizontal layout
            binding.hourlyForecastRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            // Set adapter
            binding.hourlyForecastRecycler.adapter = HourlyForecastAdapter(hourlyList)

            // Add divider if not already added
            if (binding.hourlyForecastRecycler.itemDecorationCount == 0) {
                val divider = DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
                ContextCompat.getDrawable(context, R.drawable.divider_horizontal)?.let {
                    divider.setDrawable(it)
                    binding.hourlyForecastRecycler.addItemDecoration(divider)
                }
            }
        }
    }

    class RefreshButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.refresh_button)
    }

    class ForecastListViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val dayLabels = arrayOf<TextView>(
            view.findViewById(R.id.day1_label),
            view.findViewById(R.id.day2_label),
            view.findViewById(R.id.day3_label)
        )

        private val dayTemps = arrayOf<TextView>(
            view.findViewById(R.id.day1_temp),
            view.findViewById(R.id.day2_temp),
            view.findViewById(R.id.day3_temp)
        )

        private val dayIcons = arrayOf<ImageView>(
            view.findViewById(R.id.day1_icon),
            view.findViewById(R.id.day2_icon),
            view.findViewById(R.id.day3_icon)
        )

        fun bind(forecasts: List<ForecastItem>) {
            for (i in forecasts.indices) {
                val forecast = forecasts[i]
                dayLabels[i].text = forecast.date
                dayTemps[i].text = "${forecast.avgTemp}°C"
                Glide.with(view.context).load(forecast.iconUrl).into(dayIcons[i])
            }
        }
    }

    class AirQualityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val index: TextView = view.findViewById(R.id.air_quality_index)
    }
}
