package com.example.rmvadmin

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.rmvadmin.databinding.ActivityUpdateBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null
    private var vehicleKey: String? = null
    private var oldLicenseImageUrl: String? = null
    private var oldInsuranceImageUrl: String? = null
    private lateinit var progressDialog: ProgressDialog

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

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating...")
        progressDialog.setCancelable(false)

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

    private fun updateVehicleData() {
        progressDialog.show()
        val ownerName = binding.ownerNameEditText.text.toString()
        val vehicleNumber = binding.vehicleNumberEditText.text.toString()
        val vehicleModel = binding.vehicleModelEditText.text.toString()
        val chassisNumber = binding.chassisNumberEditText.text.toString()
        val registrationDate = binding.registrationDateEditText.text.toString()
        val licenseNumber = binding.licenseNumberEditText.text.toString()
        val licenseExpiryDate = binding.licenseExpiryDateEditText.text.toString()
        val insuranceNumber = binding.insuranceNumberEditText.text.toString()
        val insuranceExpiryDate = binding.insuranceExpiryDateEditText.text.toString()
        
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(vehicleKey!!)

        uploadImage(licenseImageUri, "license_photos/$vehicleKey") { newLicensePhotoUrl ->
            uploadImage(insuranceImageUri, "insurance_photos/$vehicleKey") { newInsurancePhotoUrl ->
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
                    licensePhotoUrl = if (newLicensePhotoUrl.isNotEmpty()) newLicensePhotoUrl else oldLicenseImageUrl,
                    insuranceNumber = insuranceNumber,
                    insuranceExpiryDate = insuranceExpiryDate,
                    insurancePhotoUrl = if (newInsurancePhotoUrl.isNotEmpty()) newInsurancePhotoUrl else oldInsuranceImageUrl
                )

                databaseReference.setValue(updatedVehicleData)
                    .addOnSuccessListener { 
                        progressDialog.dismiss()
                        Toast.makeText(this, "Vehicle data updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                     }
                    .addOnFailureListener { 
                        progressDialog.dismiss()
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
        progressDialog.setMessage("Deleting...")
        progressDialog.show()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(vehicleKey!!)
        val licenseStorageReference = FirebaseStorage.getInstance().getReference("license_photos/$vehicleKey")
        val insuranceStorageReference = FirebaseStorage.getInstance().getReference("insurance_photos/$vehicleKey")
        
        // Delete images first
        licenseStorageReference.delete().addOnSuccessListener { 
            insuranceStorageReference.delete().addOnSuccessListener { 
                // Then delete the database record
                databaseReference.removeValue()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { 
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to delete insurance photo", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to delete license photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(imageUri: Uri?, path: String, onComplete: (String) -> Unit) {
        if (imageUri == null) {
            onComplete("")
            return
        }
        val storageReference = FirebaseStorage.getInstance().getReference(path)
        storageReference.putFile(imageUri)
            .addOnSuccessListener { 
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_LONG).show()
                    onComplete("")
                }
             }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
                onComplete("")
             }
    }
}
