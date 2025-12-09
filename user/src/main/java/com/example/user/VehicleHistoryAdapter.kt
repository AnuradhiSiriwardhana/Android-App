package com.example.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.user.databinding.ItemVehicleHistoryBinding

class VehicleHistoryAdapter(private var vehicleList: List<VehicleData>, private val listener: OnItemClickListener) : RecyclerView.Adapter<VehicleHistoryAdapter.VehicleHistoryViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(vehicle: VehicleData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleHistoryViewHolder {
        val binding = ItemVehicleHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleHistoryViewHolder, position: Int) {
        holder.bind(vehicleList[position])
    }

    override fun getItemCount() = vehicleList.size

    fun updateList(newList: List<VehicleData>) {
        vehicleList = newList
        notifyDataSetChanged()
    }

    inner class VehicleHistoryViewHolder(private val binding: ItemVehicleHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(vehicleList[position])
                }
            }
        }

        fun bind(vehicle: VehicleData) {
            binding.tvVehicleNumber.text = vehicle.vehicleNumber
            binding.tvOwnerName.text = vehicle.ownerName

            if (vehicle.isApproved) {
                binding.tvApprovalStatus.text = "Approved"
                val greenColor = ContextCompat.getColor(itemView.context, R.color.green)
                binding.tvApprovalStatus.background.setTint(greenColor)
            } else {
                binding.tvApprovalStatus.text = "Pending Approval"
                val blueColor = ContextCompat.getColor(itemView.context, R.color.blue)
                binding.tvApprovalStatus.background.setTint(blueColor)
            }
        }
    }
}
