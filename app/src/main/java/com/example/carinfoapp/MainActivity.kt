package com.example.carinfoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.carinfoapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener {
            val vehicleNumber = binding.vehicleNumberSearchEditText.text.toString().trim()
            if (vehicleNumber.isNotEmpty()) {
                searchVehicle(vehicleNumber)
            } else {
                Toast.makeText(this, "Please enter a vehicle number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchVehicle(vehicleNumber: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details")
        val query = databaseReference.orderByChild("vehicleNumber").equalTo(vehicleNumber)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (vehicleSnapshot in snapshot.children) {
                        val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                        displayVehicleDetails(vehicle)
                        return 
                    }
                } else {
                    binding.resultsScrollView.visibility = View.GONE
                    binding.notFoundTextView.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayVehicleDetails(vehicle: VehicleData?) {
        vehicle?.let {
            binding.resultsScrollView.visibility = View.VISIBLE
            binding.notFoundTextView.visibility = View.GONE

            binding.detailOwnerName.text = "Owner Name: ${it.ownerName}"
            binding.detailVehicleNumber.text = "Vehicle Number: ${it.vehicleNumber}"
            
            if (it.isApproved) {
                binding.approvalStatus.text = "Status: Approved"
                binding.approvalStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            } else {
                binding.approvalStatus.text = "Status: Pending Approval"
                binding.approvalStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            }

            Glide.with(this).load(it.licensePhotoUrl).into(binding.detailLicensePhoto)
            Glide.with(this).load(it.insurancePhotoUrl).into(binding.detailInsurancePhoto)
        }
    }
}
