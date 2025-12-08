package com.example.rmvadmin

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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

                updateApprovalStatus(it.isApproved)

                Glide.with(this).load(it.licensePhotoUrl).into(binding.detailLicensePhoto)
                Glide.with(this).load(it.insurancePhotoUrl).into(binding.detailInsurancePhoto)
            }
        }

        binding.approveButton.setOnClickListener {
            approveVehicle()
        }

        binding.rejectButton.setOnClickListener {
            showRejectConfirmationDialog()
        }
        
        binding.sendMessageButton.setOnClickListener {
            sendManualMessage()
        }
    }

    private fun updateApprovalStatus(isApproved: Boolean) {
        if (isApproved) {
            binding.approvalStatus.text = "Approved"
            binding.approvalStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
            binding.actionButtonsLayout.visibility = View.GONE // Hide buttons if already approved
        } else {
            binding.approvalStatus.text = "Pending Approval"
            binding.approvalStatus.setTextColor(ContextCompat.getColor(this, R.color.blue))
            binding.actionButtonsLayout.visibility = View.VISIBLE
        }
    }

    private fun approveVehicle() {
        vehicleKey?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(it)
            databaseReference.child("approved").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vehicle Approved", Toast.LENGTH_SHORT).show()
                    updateApprovalStatus(true)
                    sendAutomatedMessage("Your vehicle registration has been approved. Reference Number: $vehicleKey")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to approve vehicle", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showRejectConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reject Submission")
            .setMessage("Are you sure you want to reject and delete this submission?")
            .setPositiveButton("Reject") { _, _ -> rejectVehicle() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun rejectVehicle() {
        vehicleKey?.let {
            sendAutomatedMessage("Your vehicle registration has been rejected. Please check your details and resubmit. Reference Number: $vehicleKey")
            val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(it)
            databaseReference.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Submission Rejected", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to reject submission", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendManualMessage() {
        val message = binding.messageEditText.text.toString()
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        sendAutomatedMessage(message) // Re-use the same message sending logic
        binding.messageEditText.text?.clear()
    }
    
    private fun sendAutomatedMessage(message: String) {
        val userId = vehicleData?.userId
        if (userId == null) {
            Toast.makeText(this, "Cannot send message: User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("messages").child(userId)
        val messageId = databaseReference.push().key!!
        
        val messageData = mapOf("message" to message, "timestamp" to System.currentTimeMillis())

        databaseReference.child(messageId).setValue(messageData)
            .addOnSuccessListener { 
                Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
             }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
             }
    }
}
