package com.example.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.user.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null

    private val selectLicenseImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            licenseImageUri = data?.data
            binding.licenseImageView.setImageURI(licenseImageUri)
            binding.licenseImageView.visibility = View.VISIBLE
        }
    }

    private val selectInsuranceImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            insuranceImageUri = data?.data
            binding.insuranceImageView.setImageURI(insuranceImageUri)
            binding.insuranceImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uploadLicenseButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectLicenseImageLauncher.launch(intent)
        }

        binding.uploadInsuranceButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectInsuranceImageLauncher.launch(intent)
        }

        binding.submitButton.setOnClickListener {
            submitRegistration()
        }
    }

    private fun submitRegistration() {
        val ownerName = binding.ownerNameEditText.text.toString()
        val vehicleNumber = binding.vehicleNumberEditText.text.toString()
        // ... get all other details ...
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in to submit.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // NOTE: This is a simplified version. In a real app, you would upload images first.
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details")
        val vehicleId = databaseReference.push().key!!

        val vehicleData = VehicleData(
            key = vehicleId,
            userId = currentUserId,
            ownerName = ownerName,
            vehicleNumber = vehicleNumber,
            // ... add all other fields ...
            isApproved = false
        )

        databaseReference.child(vehicleId).setValue(vehicleData)
            .addOnSuccessListener { 
                Toast.makeText(this, "Registration submitted for approval.", Toast.LENGTH_SHORT).show()
                finish()
             }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to submit registration.", Toast.LENGTH_SHORT).show()
             }
    }
}
