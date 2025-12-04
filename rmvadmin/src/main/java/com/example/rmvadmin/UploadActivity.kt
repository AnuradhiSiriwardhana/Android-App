package com.example.rmvadmin

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rmvadmin.databinding.ActivityUploadBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var licenseImageUri: Uri? = null
    private var insuranceImageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

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
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(false)

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

        binding.saveButton.setOnClickListener {
            saveVehicleData()
        }
    }

    private fun saveVehicleData() {
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

        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details")
        val vehicleId = databaseReference.push().key!!

        uploadImage(licenseImageUri, "license_photos/$vehicleId") { licensePhotoUrl ->
            uploadImage(insuranceImageUri, "insurance_photos/$vehicleId") { insurancePhotoUrl ->
                val vehicleData = VehicleData(
                    key = vehicleId,
                    ownerName = ownerName,
                    vehicleNumber = vehicleNumber,
                    vehicleModel = vehicleModel,
                    chassisNumber = chassisNumber,
                    registrationDate = registrationDate,
                    isApproved = true,
                    licenseNumber = licenseNumber,
                    licenseExpiryDate = licenseExpiryDate,
                    licensePhotoUrl = licensePhotoUrl,
                    insuranceNumber = insuranceNumber,
                    insuranceExpiryDate = insuranceExpiryDate,
                    insurancePhotoUrl = insurancePhotoUrl
                )

                databaseReference.child(vehicleId).setValue(vehicleData)
                    .addOnSuccessListener { 
                        progressDialog.dismiss()
                        Toast.makeText(this, "Vehicle data saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                     }
                    .addOnFailureListener { 
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to save vehicle data", Toast.LENGTH_SHORT).show()
                     }
            }
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
