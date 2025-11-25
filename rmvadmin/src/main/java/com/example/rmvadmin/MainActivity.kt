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

        database = FirebaseDatabase.getInstance().getReference("Vehicles")

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
                val list = ArrayList<VehicleData>()
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                    vehicle?.let { list.add(it) }
                }
                adapter.updateList(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onItemClick(vehicle: VehicleData) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }

    override fun onApproveClick(vehicle: VehicleData) {
        vehicle.vehicleNumber?.let {
            database.child(it).child("isApproved").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "${vehicle.vehicleNumber} Approved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to approve", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onEditClick(vehicle: VehicleData) {
        val intent = Intent(this, UploadActivity::class.java).apply {
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }

    override fun onDeleteClick(vehicleNumber: String) {
        database.child(vehicleNumber).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "$vehicleNumber Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }
}
