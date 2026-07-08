package com.example.kulkasku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ItemAdapter
    private val itemList = ArrayList<ItemMakanan>()
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvMakan = findViewById<RecyclerView>(R.id.rvMakanan)
        val btnTambah = findViewById<Button>(R.id.btnTambah)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        rvMakan.layoutManager = LinearLayoutManager(this)

        // [SINKRONISASI BARCODE] Mengirim seluruh data item termasuk barcode ke FormActivity saat diklik
        adapter = ItemAdapter(itemList) { itemMakanan ->
            val intent = Intent(this, FormActivity::class.java).apply {
                putExtra("ITEM_ID", itemMakanan.id)
                putExtra("NAMA", itemMakanan.nama_item)
                putExtra("KATEGORI", itemMakanan.kategori)
                putExtra("JUMLAH", itemMakanan.jumlah)
                putExtra("TANGGAL", itemMakanan.tgl_kedaluwarsa)
                putExtra("BARCODE", itemMakanan.barcode) // Data barcode ikut dibawa ke mode edit
            }
            startActivity(intent)
        }
        rvMakan.adapter = adapter

        // Mendengarkan data live dari ViewModel secara real-time
        viewModel.itemList.observe(this) { dataTerbaru ->
            itemList.clear()
            itemList.addAll(dataTerbaru)
            adapter.notifyDataSetChanged()
        }

        viewModel.errorMessage.observe(this) { pesanError ->
            Toast.makeText(this, pesanError, Toast.LENGTH_SHORT).show()
        }

        btnTambah.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        periksaSesiDanMuatData()
    }

    private fun periksaSesiDanMuatData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModel.listenToFoodData()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}