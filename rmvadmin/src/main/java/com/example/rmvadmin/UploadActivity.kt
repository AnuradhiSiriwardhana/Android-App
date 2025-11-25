package com.example.rmvadmin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.rmvadmin.databinding.ActivityUploadBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null
    private var existingVehicle: VehicleData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Vehicles")
        storage = FirebaseStorage.getInstance().reference

        // Check if in edit mode
        existingVehicle = intent.getParcelableExtra("VEHICLE_DATA")
        if (existingVehicle != null) {
            title = "Edit Vehicle"
            populateFields(existingVehicle!!)
        }

        setupImagePickers()

        binding.btnSaveData.setOnClickListener { 
            uploadImagesAndSaveData()
        }
    }

    private fun populateFields(vehicle: VehicleData) {
        binding.etOwnerName.setText(vehicle.ownerName)
        binding.etVehicleNumber.setText(vehicle.vehicleNumber)
        binding.etVehicleModel.setText(vehicle.vehicleModel)
        binding.etChassisNumber.setText(vehicle.chassisNumber)
        binding.etRegistrationDate.setText(vehicle.registrationDate)
        binding.etLicenseNumber.setText(vehicle.licenseNumber)
        binding.etLicenseExpiry.setText(vehicle.licenseExpiryDate)
        binding.etInsuranceNumber.setText(vehicle.insuranceNumber)
        binding.etInsuranceExpiry.setText(vehicle.insuranceExpiryDate)

        // Load existing images
        loadImage(vehicle.licensePhotoUrl, binding.ivLicensePreview)
        loadImage(vehicle.insurancePhotoUrl, binding.ivInsurancePreview)
    }

    private fun loadImage(url: String?, imageView: android.widget.ImageView) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this).load(url).into(imageView)
            imageView.visibility = android.view.View.VISIBLE
        }
    }

    private val licenseImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            licenseImageUri = it.data?.data
            binding.ivLicensePreview.setImageURI(licenseImageUri)
            binding.ivLicensePreview.visibility = android.view.View.VISIBLE
        }
    }

    private val insuranceImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            insuranceImageUri = it.data?.data
            binding.ivInsurancePreview.setImageURI(insuranceImageUri)
            binding.ivInsurancePreview.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupImagePickers() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        binding.btnUploadLicense.setOnClickListener { licenseImagePicker.launch(intent) }
        binding.btnUploadInsurance.setOnClickListener { insuranceImagePicker.launch(intent) }
    }

    private fun uploadImagesAndSaveData() {
        val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
        if (vehicleNumber.isEmpty()) {
            Toast.makeText(this, "Vehicle Number is required", Toast.LENGTH_SHORT).show()
            return
        }

        val uploadTasks = mutableListOf<Task<Uri>>()

        licenseImageUri?.let { uri ->
            val imageRef = storage.child("images/$vehicleNumber/license.jpg")
            val uploadTask = imageRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            uploadTasks.add(uploadTask)
        }

        insuranceImageUri?.let { uri ->
            val imageRef = storage.child("images/$vehicleNumber/insurance.jpg")
            val uploadTask = imageRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            uploadTasks.add(uploadTask)
        }

        Tasks.whenAllSuccess<Uri>(uploadTasks).addOnSuccessListener { uris ->
            var licenseUrl = existingVehicle?.licensePhotoUrl
            var insuranceUrl = existingVehicle?.insurancePhotoUrl

            // This logic assumes we know which URL belongs to which upload based on the order.
            // A more robust solution might use a map or check the path.
            if (licenseImageUri != null) {
                 licenseUrl = uris.find { it.toString().contains("license") }?.toString() ?: licenseUrl
            }
            if (insuranceImageUri != null) {
                insuranceUrl = uris.find { it.toString().contains("insurance") }?.toString() ?: insuranceUrl
            }

            saveVehicleData(vehicleNumber, licenseUrl, insuranceUrl)

        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveVehicleData(vehicleNumber: String, licenseUrl: String?, insuranceUrl: String?) {
        val vehicleData = VehicleData(
            ownerName = binding.etOwnerName.text.toString(),
            vehicleNumber = vehicleNumber,
            vehicleModel = binding.etVehicleModel.text.toString(),
            chassisNumber = binding.etChassisNumber.text.toString(),
            registrationDate = binding.etRegistrationDate.text.toString(),
            isApproved = existingVehicle?.isApproved ?: false,
            licenseNumber = binding.etLicenseNumber.text.toString(),
            licenseExpiryDate = binding.etLicenseExpiry.text.toString(),
            licensePhotoUrl = licenseUrl,
            insuranceNumber = binding.etInsuranceNumber.text.toString(),
            insuranceExpiryDate = binding.etInsuranceExpiry.text.toString(),
            insurancePhotoUrl = insuranceUrl
        )

        // If the key (vehicle number) was changed in edit mode, delete the old record
        if (existingVehicle != null && existingVehicle?.vehicleNumber != vehicleNumber) {
            database.child(existingVehicle!!.vehicleNumber!!).removeValue()
        }

        database.child(vehicleNumber).setValue(vehicleData).addOnSuccessListener {
            Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }
}
