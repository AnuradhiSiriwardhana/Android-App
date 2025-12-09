package com.example.user

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.user.databinding.ActivityVehicleDetailBinding

class VehicleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vehicleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("VEHICLE_DATA", VehicleData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("VEHICLE_DATA")
        }

        vehicleData?.let {
            binding.detailOwnerName.text = it.ownerName
            binding.detailVehicleNumber.text = it.vehicleNumber
            binding.detailVehicleModel.text = "Model: ${it.vehicleModel}"
            binding.detailChassisNumber.text = "Chassis: ${it.chassisNumber}"
            binding.detailRegistrationDate.text = "Registered: ${it.registrationDate}"
            binding.detailLicenseNumber.text = "License: ${it.licenseNumber}"
            binding.detailLicenseExpiryDate.text = "Expires: ${it.licenseExpiryDate}"
            binding.detailInsuranceNumber.text = "Insurance: ${it.insuranceNumber}"
            binding.detailInsuranceExpiryDate.text = "Expires: ${it.insuranceExpiryDate}"

            if (it.isApproved) {
                binding.approvalStatus.text = "Approved"
                binding.approvalStatus.setTextColor(ContextCompat.getColor(this, R.color.status_approved))
            } else {
                binding.approvalStatus.text = "Pending Approval"
                binding.approvalStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending))
            }

            Glide.with(this).load(it.licensePhotoUrl).into(binding.detailLicensePhoto)
            Glide.with(this).load(it.insurancePhotoUrl).into(binding.detailInsurancePhoto)
        }
    }
}
