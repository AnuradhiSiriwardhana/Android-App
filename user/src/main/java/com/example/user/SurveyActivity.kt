package com.example.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.user.databinding.ActivitySurveyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.submitSurveyButton.setOnClickListener {
            submitSurvey()
        }
    }

    private fun submitSurvey() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to submit feedback.", Toast.LENGTH_SHORT).show()
            return
        }

        val overallExperience = binding.overallExperienceRatingBar.rating
        val easeOfRegistration = binding.easeOfRegistrationRatingBar.rating
        val featureRequest = binding.featureRequestEditText.text.toString()
        val generalFeedback = binding.generalFeedbackEditText.text.toString()

        if (overallExperience == 0f || easeOfRegistration == 0f) {
            Toast.makeText(this, "Please provide a rating for the first two questions.", Toast.LENGTH_SHORT).show()
            return
        }

        val surveyResponse = mapOf(
            "userId" to userId,
            "overallExperience" to overallExperience,
            "easeOfRegistration" to easeOfRegistration,
            "featureRequest" to featureRequest,
            "generalFeedback" to generalFeedback,
            "timestamp" to System.currentTimeMillis()
        )

        database.getReference("UserFeedback").push().setValue(surveyResponse)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
