package com.example.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.user.databinding.ActivityRegistrationHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegistrationHistoryActivity : AppCompatActivity(), VehicleHistoryAdapter.OnItemClickListener {

    private lateinit var binding: ActivityRegistrationHistoryBinding
    private lateinit var historyAdapter: VehicleHistoryAdapter
    private val vehicleList = mutableListOf<VehicleData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyAdapter = VehicleHistoryAdapter(vehicleList, this)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        fetchRegistrationHistory()
    }

    private fun fetchRegistrationHistory() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details")
            databaseReference.orderByChild("userId").equalTo(currentUserId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vehicleList.clear()
                    for (vehicleSnapshot in snapshot.children) {
                        val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                        vehicle?.let { vehicleList.add(it) }
                    }
                    historyAdapter.updateList(vehicleList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    override fun onItemClick(vehicle: VehicleData) {
        val intent = Intent(this, VehicleDetailActivity::class.java).apply {
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }
}
