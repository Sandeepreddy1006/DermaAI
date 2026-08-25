package com.simats.dermacareai.doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.dermacareai.R
import com.simats.dermacareai.models.Doctor

class DoctorAdapter(private val doctors: List<Doctor>, private val onItemClick: (Doctor) -> Unit) :
    RecyclerView.Adapter<DoctorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvSpecialty: TextView = view.findViewById(R.id.tvSpecialty)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.tvName.text = doctor.name
        holder.tvSpecialty.text = doctor.specialty
        holder.tvAddress.text = doctor.address
        holder.tvRating.text = "⭐ ${doctor.rating}"
        holder.tvDistance.text = "📍 ${doctor.distance}"
        
        holder.itemView.setOnClickListener { onItemClick(doctor) }
    }

    override fun getItemCount() = doctors.size
}
