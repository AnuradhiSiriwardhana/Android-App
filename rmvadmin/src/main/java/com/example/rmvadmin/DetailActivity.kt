package com.example.rmvadmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.rmvadmin.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vehicle = intent.getParcelableExtra<VehicleData>("VEHICLE_DATA")

        if (vehicle != null) {
            binding.tvDetailVehicleNumber.text = vehicle.vehicleNumber
            binding.tvDetailOwnerName.text = vehicle.ownerName
            binding.tvDetailModel.text = "Model: ${vehicle.vehicleModel}"
            binding.tvDetailChassis.text = "Chassis: ${vehicle.chassisNumber}"
            binding.tvDetailRegDate.text = "Registered: ${vehicle.registrationDate}"

            // Load images
            loadImage(vehicle.licensePhotoUrl, binding.ivDetailLicensePhoto)
            loadImage(vehicle.insurancePhotoUrl, binding.ivDetailInsurancePhoto)
        }
    }

    private fun loadImage(url: String?, imageView: android.widget.ImageView) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this).load(url).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder) // Optional: a placeholder image
        }
    }
}
