package com.example.rmvadmin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                vehicleList.clear() 
                for (vehicleSnapshot in snapshot.children) {
                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                    vehicle?.key = vehicleSnapshot.key
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
            putExtra("VEHICLE_KEY", vehicle.key)
            putExtra("VEHICLE_DATA", vehicle)
        }
        startActivity(intent)
    }

    override fun onDeleteClick(vehicleKey: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Vehicle")
            .setMessage("Are you sure you want to delete this vehicle record?")
            .setPositiveButton("Delete") { _, _ -> deleteVehicle(vehicleKey) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteVehicle(vehicleKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Vehicle Details").child(vehicleKey)
        databaseReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Vehicle Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to delete vehicle", Toast.LENGTH_SHORT).show()
            }
    }
}
