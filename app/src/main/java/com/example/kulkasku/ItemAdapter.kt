package com.example.kulkasku

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ItemAdapter(
    private val itemList: List<ItemMakanan>,
    private val onClick: (ItemMakanan) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvKategoriQty: TextView = view.findViewById(R.id.tvKategoriQty)
        val tvExpired: TextView = view.findViewById(R.id.tvExpired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_makanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // Sinkronisasi data ke komponen visual (Menggunakan nama_item dan satuan baru)
        holder.tvNama.text = item.nama_item
        holder.tvKategoriQty.text = "Kategori: ${item.kategori} | Jumlah: ${item.jumlah} ${item.satuan}"
        holder.tvExpired.text = "Kedaluwarsa: ${item.tgl_kedaluwarsa}"

        // LOGIKA PERINGATAN KEDALUWARSA VISUAL (Sesuai DFD Proses 2.0 Pantau Kedaluwarsa)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val expDate = sdf.parse(item.tgl_kedaluwarsa)
            if (expDate != null) {
                // Menghitung selisih hari antara tanggal sekarang dan tanggal kedaluwarsa
                val diff = expDate.time - Date().time
                val days = diff / (1000 * 60 * 60 * 24)

                if (days <= 3) {
                    holder.tvExpired.setTextColor(Color.RED) // Berubah merah jika kritis (<= 3 hari)
                } else {
                    holder.tvExpired.setTextColor(Color.parseColor("#4CAF50")) // Hijau jika aman
                }
            }
        } catch (e: Exception) {
            holder.tvExpired.setTextColor(Color.BLACK)
        }

        // Aksi lempar data ketika salah satu baris list makanan diklik oleh user
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = itemList.size
}