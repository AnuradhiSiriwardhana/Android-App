package com.example.rmvadmin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rmvadmin.databinding.VehicleItemBinding

class VehicleAdapter(
    private var vehicleList: ArrayList<VehicleData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(vehicle: VehicleData)
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
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(vehicleList[position])
                }
            }
        }

        fun bind(vehicle: VehicleData) {
            binding.tvOwnerName.text = vehicle.ownerName
            binding.tvVehicleNumber.text = vehicle.vehicleNumber

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
