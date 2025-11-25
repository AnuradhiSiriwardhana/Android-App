package com.example.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.user.databinding.ActivityRegistrationBinding

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
            // You will need to get the data from the EditTexts and upload the images to Firebase Storage
        }
    }
}
