package com.example.rmvadmin

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.rmvadmin.databinding.ActivityDetailBinding
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var vehicleKey: String? = null
    private var vehicleData: VehicleData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            vehicleKey = bundle.getString("VEHICLE_KEY")
            vehicleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("VEHICLE_DATA", VehicleData::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("VEHICLE_DATA")
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

                Glide.with(this).load(it.licensePhotoUrl).into(binding.detailLicensePhoto)
                Glide.with(this).load(it.insurancePhotoUrl).into(binding.detailInsurancePhoto)
            }
        }

        binding.updateButton.setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java).apply {
                putExtra("VEHICLE_KEY", vehicleKey)
                putExtra("VEHICLE_DATA", vehicleData)
            }
            startActivity(intent)
        }

        binding.approveButton.setOnClickListener {
            approveVehicle()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun approveVehicle() {
        vehicleKey?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(it)
            databaseReference.child("approved").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vehicle Approved", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the main list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to approve vehicle", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Vehicle")
            .setMessage("Are you sure you want to delete this vehicle record?")
            .setPositiveButton("Delete") { _, _ -> deleteVehicle() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteVehicle() {
        vehicleKey?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(it)
            databaseReference.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the main list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
