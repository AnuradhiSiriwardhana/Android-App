package com.example.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.user.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null

    private val cloudName = "du8rqkbtb"
    private val uploadPreset = "Android_App"

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

        initCloudinary()

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

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = cloudName
        MediaManager.init(this, config)
    }

    private fun submitRegistration() {
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

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            hideProgressBar()
            Toast.makeText(this, "You must be logged in to submit.", Toast.LENGTH_SHORT).show()
            return
        }

        if (licenseImageUri == null || insuranceImageUri == null) {
            hideProgressBar()
            Toast.makeText(this, "Please upload both license and insurance photos.", Toast.LENGTH_SHORT).show()
            return
        }
        
        uploadImage(licenseImageUri) { licensePhotoUrl ->
            uploadImage(insuranceImageUri) { insurancePhotoUrl ->
                if (licensePhotoUrl == null || insurancePhotoUrl == null) {
                    hideProgressBar()
                    Toast.makeText(this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@uploadImage
                }

                val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details")
                val vehicleId = databaseReference.push().key!!

                val vehicleData = VehicleData(
                    key = vehicleId,
                    userId = currentUserId,
                    ownerName = ownerName,
                    vehicleNumber = vehicleNumber,
                    vehicleModel = vehicleModel,
                    chassisNumber = chassisNumber,
                    registrationDate = registrationDate,
                    isApproved = false,
                    licenseNumber = licenseNumber,
                    licenseExpiryDate = licenseExpiryDate,
                    licensePhotoUrl = licensePhotoUrl,
                    insuranceNumber = insuranceNumber,
                    insuranceExpiryDate = insuranceExpiryDate,
                    insurancePhotoUrl = insurancePhotoUrl
                )

                databaseReference.child(vehicleId).setValue(vehicleData)
                    .addOnSuccessListener { 
                        hideProgressBar()
                        showSuccessState()
                     }
                    .addOnFailureListener { 
                        hideProgressBar()
                        Toast.makeText(this, "Failed to submit registration.", Toast.LENGTH_SHORT).show()
                     }
            }
        }
    }

    private fun uploadImage(imageUri: Uri?, onComplete: (String?) -> Unit) {
        if (imageUri == null) {
            onComplete(null)
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
                Toast.makeText(this@RegistrationActivity, "Image upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                onComplete(null) // Signal that the upload failed
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun showProgressBar() {
        // You can implement a ProgressBar in your layout and show it here
    }

    private fun hideProgressBar() {
        // You can hide the ProgressBar here
    }

    private fun showSuccessState() {
        binding.statusTextView.visibility = View.VISIBLE
        // Disable all form fields
        for (i in 0 until binding.formLayout.childCount) {
            val view = binding.formLayout.getChildAt(i)
            view.isEnabled = false
        }
        binding.submitButton.isEnabled = false
        Toast.makeText(this, "Registration submitted for approval!", Toast.LENGTH_LONG).show()
    }
}
