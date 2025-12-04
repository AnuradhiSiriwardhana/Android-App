package com.example.rmvadmin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rmvadmin.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), VehicleAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var vehicleList: ArrayList<VehicleData>
    private lateinit var adapter: VehicleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Vehicle Details")

        setupRecyclerView()

        binding.fab.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

        fetchVehicleData()
    }

    private fun setupRecyclerView() {
        vehicleList = ArrayList()
        adapter = VehicleAdapter(vehicleList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun fetchVehicleData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vehicleList.clear() // Clear the list before adding new data
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                    vehicle?.key = vehicleSnapshot.key // Store the unique key
                    if (vehicle != null) {
                        vehicleList.add(vehicle)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onItemClick(vehicle: VehicleData) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("VEHICLE_KEY", vehicle.key) // Pass the correct key
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }

    override fun onApproveClick(vehicle: VehicleData) {
        vehicle.key?.let {
            database.child(it).child("approved").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "${vehicle.vehicleNumber} Approved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to approve", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onEditClick(vehicle: VehicleData) {
        val intent = Intent(this, UpdateActivity::class.java).apply {
            putExtra("VEHICLE_KEY", vehicle.key) // Pass the correct key
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }

    override fun onDeleteClick(vehicleKey: String) {
        database.child(vehicleKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }
}
