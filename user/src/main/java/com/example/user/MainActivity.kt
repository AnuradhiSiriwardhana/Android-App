package com.example.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.user.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val NOTIFICATION_PERMISSION_CODE = 102
    private val EXPIRY_CHECK_WORK_NAME = "expiryCheckWork"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        scheduleExpiryCheckWorker()

        binding.registerCard.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.historyCard.setOnClickListener { 
            startActivity(Intent(this, RegistrationHistoryActivity::class.java))
        }

        binding.messagesCard.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        binding.profileCard.setOnClickListener { 
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            WorkManager.getInstance(this).cancelUniqueWork(EXPIRY_CHECK_WORK_NAME)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
            }
        }
    }

    private fun scheduleExpiryCheckWorker() {
        val expiryCheckWorkRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            EXPIRY_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            expiryCheckWorkRequest
        )
    }
}
