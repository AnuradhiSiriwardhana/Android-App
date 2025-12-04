package com.example.rmvadmin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rmvadmin.databinding.VehicleItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehicleAdapter(
    private var vehicleList: ArrayList<VehicleData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(vehicle: VehicleData) // For opening details
        fun onApproveClick(vehicle: VehicleData)
        fun onEditClick(vehicle: VehicleData)
        fun onDeleteClick(vehicleKey: String) // Changed to pass the key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = VehicleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val currentItem = vehicleList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = vehicleList.size

    fun updateList(newList: List<VehicleData>) {
        vehicleList.clear()
        vehicleList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class VehicleViewHolder(private val binding: VehicleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Click listener for the whole item
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(vehicleList[position])
                }
            }

            binding.btnApprove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onApproveClick(vehicleList[position])
                }
            }
            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(vehicleList[position])
                }
            }
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    vehicleList[position].key?.let { key ->
                        listener.onDeleteClick(key) // Pass the correct key
                    }
                }
            }
        }

        fun bind(vehicle: VehicleData) {
            binding.tvVehicleNumber.text = vehicle.vehicleNumber
            binding.tvOwnerName.text = vehicle.ownerName

            // Approval Status
            updateStatus(binding.tvApprovalStatus, vehicle.isApproved, "Approved", "Not Approved")

            // License Status
            updateValidityStatus(binding.tvLicenseStatus, "License", vehicle.licenseExpiryDate)

            // Insurance Status
            updateValidityStatus(binding.tvInsuranceStatus, "Insurance", vehicle.insuranceExpiryDate)

            // Update Approve button state
            binding.btnApprove.isEnabled = !vehicle.isApproved
            binding.btnApprove.text = if (vehicle.isApproved) "Approved" else "Approve"
        }

        private fun isDateValid(dateStr: String?): Boolean {
            if (dateStr.isNullOrEmpty()) return false
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val expiryDate = sdf.parse(dateStr)
                expiryDate?.after(Date()) ?: false
            } catch (e: Exception) {
                false
            }
        }

        private fun updateStatus(textView: android.widget.TextView, isValid: Boolean, validText: String, invalidText: String) {
            if (isValid) {
                textView.text = validText
                textView.setTextColor(Color.parseColor("#008000")) // Green
            } else {
                textView.text = invalidText
                textView.setTextColor(Color.RED)
            }
        }

        private fun updateValidityStatus(textView: android.widget.TextView, prefix: String, date: String?) {
            if (date.isNullOrEmpty()) {
                textView.text = "$prefix: N/A"
                textView.setTextColor(Color.GRAY)
                return
            }

            if (isDateValid(date)) {
                textView.text = "$prefix: Valid"
                textView.setTextColor(Color.parseColor("#008000")) // Green
            } else {
                textView.text = "$prefix: Expired"
                textView.setTextColor(Color.RED)
            }
        }
    }
}
