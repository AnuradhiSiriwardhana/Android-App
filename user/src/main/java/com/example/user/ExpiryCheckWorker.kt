package com.example.user

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpiryCheckWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()

        val dbRef = FirebaseDatabase.getInstance().getReference("Vehicle Details")
        dbRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                    vehicle?.let {
                        checkDateAndNotify(it.licenseExpiryDate, "License")
                        checkDateAndNotify(it.insuranceExpiryDate, "Insurance")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) { /* ... */ }
        })

        return Result.success()
    }

    private fun checkDateAndNotify(dateStr: String?, documentType: String) {
        if (dateStr.isNullOrEmpty()) return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiryDate = sdf.parse(dateStr)

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30) // 30-day reminder window
            val reminderDate = calendar.time

            if (expiryDate != null && expiryDate.before(reminderDate)) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.createNotificationChannel()
                notificationHelper.showNotification(
                    "$documentType Expiry Reminder",
                    "Your $documentType is expiring on $dateStr."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
