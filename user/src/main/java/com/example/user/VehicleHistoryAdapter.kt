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

            when {
                vehicle.isApproved -> {
                    binding.tvApprovalStatus.text = "Approved"
                    val approvedColor = ContextCompat.getColor(itemView.context, R.color.status_approved)
                    binding.tvApprovalStatus.background.setTint(approvedColor)
                }
                // This assumes a rejected vehicle is removed from the "Vehicle Details" node.
                // If you have a specific "isRejected" flag, you would check it here.
                else -> {
                    binding.tvApprovalStatus.text = "Pending Approval"
                    val pendingColor = ContextCompat.getColor(itemView.context, R.color.status_pending)
                    binding.tvApprovalStatus.background.setTint(pendingColor)
                }
            }
        }
    }
}
