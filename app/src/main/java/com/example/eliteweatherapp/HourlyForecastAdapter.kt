package com.example.eliteweatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eliteweatherapp.databinding.HourlyForecastItemBinding

class HourlyForecastAdapter(private val hourlyList: List<HourlyForecastItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = HourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        holder.bind(hourlyList[position])
    }

    override fun getItemCount(): Int = hourlyList.size

    class HourlyForecastViewHolder(private val binding: HourlyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourlyForecastItem) {
            binding.hourText.text = item.time
            binding.hourTemp.text = item.temp.toString() + "°C"
            Glide.with(binding.hourIcon.context)
                .load(item.iconUrl)
                .into(binding.hourIcon)
        }
    }
}

