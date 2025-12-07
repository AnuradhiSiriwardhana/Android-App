package com.example.rmvadmin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.rmvadmin.databinding.ActivityUpdateBinding
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null
    private var vehicleKey: String? = null
    private var oldLicenseImageUrl: String? = null
    private var oldInsuranceImageUrl: String? = null

    // Corrected Cloud Name from your screenshot
    private val cloudName = "du8rqkbtb"
    private val uploadPreset = "Android_App"

    private val selectLicenseImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            licenseImageUri = data?.data
            binding.licenseImageView.setImageURI(licenseImageUri)
        }
    }

    private val selectInsuranceImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            insuranceImageUri = data?.data
            binding.insuranceImageView.setImageURI(insuranceImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCloudinary()

        val bundle = intent.extras
        if (bundle != null) {
            val vehicleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("VEHICLE_DATA", VehicleData::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("VEHICLE_DATA")
            }
            vehicleKey = bundle.getString("VEHICLE_KEY")
            
            vehicleData?.let {
                binding.ownerNameEditText.setText(it.ownerName)
                binding.vehicleNumberEditText.setText(it.vehicleNumber)
                binding.vehicleModelEditText.setText(it.vehicleModel)
                binding.chassisNumberEditText.setText(it.chassisNumber)
                binding.registrationDateEditText.setText(it.registrationDate)
                binding.licenseNumberEditText.setText(it.licenseNumber)
                binding.licenseExpiryDateEditText.setText(it.licenseExpiryDate)
                binding.insuranceNumberEditText.setText(it.insuranceNumber)
                binding.insuranceExpiryDateEditText.setText(it.insuranceExpiryDate)
                oldLicenseImageUrl = it.licensePhotoUrl
                oldInsuranceImageUrl = it.insurancePhotoUrl

                Glide.with(this).load(oldLicenseImageUrl).into(binding.licenseImageView)
                Glide.with(this).load(oldInsuranceImageUrl).into(binding.insuranceImageView)
            }
        }

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

        binding.updateButton.setOnClickListener {
            updateVehicleData()
        }
        
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = cloudName
        MediaManager.init(this, config)
    }

    private fun updateVehicleData() {
        showProgressBar()
        val ownerName = binding.ownerNameEditText.text.toString()
        val vehicleNumber = binding.vehicleNumberEditText.text.toString()
        val vehicleModel = binding.vehicleModelEditText.text.toString()
        val chassisNumber = binding.chassisNumberEditText.text.toString()
        val registrationDate = binding.registrationDateEditText.text.toString()
        val licenseNumber = binding.licenseNumberEditText.text.toString()
        val licenseExpiryDate = binding.licenseExpiryDateEditText.text.toString()
        val insuranceNumber = binding.insuranceNumberEditText.text.toString()
        val insuranceExpiryDate = binding.insuranceExpiryDateEditText.text.toString()

        uploadImage(licenseImageUri) { newLicensePhotoUrl ->
            uploadImage(insuranceImageUri) { newInsurancePhotoUrl ->

                val licenseUrl = newLicensePhotoUrl ?: oldLicenseImageUrl
                val insuranceUrl = newInsurancePhotoUrl ?: oldInsuranceImageUrl

                if (licenseUrl == null || insuranceUrl == null) {
                    hideProgressBar()
                    Toast.makeText(this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@uploadImage
                }

                val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(vehicleKey!!)

                val updatedVehicleData = VehicleData(
                    key = vehicleKey,
                    ownerName = ownerName,
                    vehicleNumber = vehicleNumber,
                    vehicleModel = vehicleModel,
                    chassisNumber = chassisNumber,
                    registrationDate = registrationDate,
                    isApproved = true,
                    licenseNumber = licenseNumber,
                    licenseExpiryDate = licenseExpiryDate,
                    licensePhotoUrl = licenseUrl,
                    insuranceNumber = insuranceNumber,
                    insuranceExpiryDate = insuranceExpiryDate,
                    insurancePhotoUrl = insuranceUrl
                )

                databaseReference.setValue(updatedVehicleData)
                    .addOnSuccessListener { 
                        hideProgressBar()
                        Toast.makeText(this, "Vehicle data updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                     }
                    .addOnFailureListener { 
                        hideProgressBar()
                        Toast.makeText(this, "Failed to update vehicle data", Toast.LENGTH_SHORT).show()
                     }
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
        showProgressBar()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(vehicleKey!!)
        databaseReference.removeValue()
            .addOnSuccessListener {
                hideProgressBar()
                Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { 
                hideProgressBar()
                Toast.makeText(this, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(imageUri: Uri?, onComplete: (String?) -> Unit) {
        if (imageUri == null) {
            onComplete("") // If no new image is selected, return an empty string
            return
        }

        MediaManager.get().upload(imageUri).unsigned(uploadPreset).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                Log.d("Cloudinary", "Upload started")
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val url = resultData["secure_url"] as String
                onComplete(url)
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                Log.e("Cloudinary", "Upload error: ${error.description}")
                Toast.makeText(this@UpdateActivity, "Image upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                onComplete(null) // Signal that the upload failed
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}
