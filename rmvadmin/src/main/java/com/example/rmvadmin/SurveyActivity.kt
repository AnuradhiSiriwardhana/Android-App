package com.example.rmvadmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import com.example.rmvadmin.databinding.ActivitySurveyBinding
import com.google.firebase.database.FirebaseDatabase

class SurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        binding.submitSurveyButton.setOnClickListener {
            submitSurvey()
        }
    }

    private fun submitSurvey() {
        val satisfaction = binding.satisfactionRatingBar.rating
        val easeOfUse = binding.easeOfUseRatingBar.rating
        val selectedClarityId = binding.clarityRadioGroup.checkedRadioButtonId
        val isClarityYes = findViewById<RadioButton>(selectedClarityId)?.text.toString() == "Yes"
        val usefulFeature = binding.usefulFeatureEditText.text.toString()
        val suggestions = binding.suggestionsEditText.text.toString()

        val surveyResponse = mapOf(
            "satisfaction" to satisfaction,
            "easeOfUse" to easeOfUse,
            "isClarityYes" to isClarityYes,
            "mostUsefulFeature" to usefulFeature,
            "suggestions" to suggestions,
            "timestamp" to System.currentTimeMillis()
        )

        database.getReference("SurveyResponses").push().setValue(surveyResponse)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
