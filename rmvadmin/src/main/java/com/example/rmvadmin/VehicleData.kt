package com.example.rmvadmin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleData(
    var key: String? = null, // Added to store the unique Firebase key
    val ownerName: String? = null,
    val vehicleNumber: String? = null,
    val vehicleModel: String? = null,
    val chassisNumber: String? = null,
    val registrationDate: String? = null,
    var isApproved: Boolean = false,
    val licenseNumber: String? = null,
    val licenseExpiryDate: String? = null,
    val licensePhotoUrl: String? = null,
    val insuranceNumber: String? = null,
    val insuranceExpiryDate: String? = null,
    val insurancePhotoUrl: String? = null
) : Parcelable
