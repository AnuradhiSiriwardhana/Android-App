package com.example.rmvadmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rmvadmin.databinding.ActivityRejectedHistoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RejectedHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRejectedHistoryBinding
    private lateinit var rejectedAdapter: VehicleAdapter
    private val rejectedList = mutableListOf<VehicleData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRejectedHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rejectedAdapter = VehicleAdapter(rejectedList as ArrayList<VehicleData>, object : VehicleAdapter.OnItemClickListener {
            override fun onItemClick(vehicle: VehicleData) {
                // You can optionally handle clicks on rejected items, e.g., to view details
            }
            override fun onDeleteClick(vehicleKey: String) {
                // You can optionally handle delete clicks on rejected items
            }
        })
        binding.rejectedRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.rejectedRecyclerView.adapter = rejectedAdapter

        fetchRejectedData()
    }

    private fun fetchRejectedData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Rejected Vehicles")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rejectedList.clear()
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                    vehicle?.let { rejectedList.add(it) }
                }
                rejectedAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
