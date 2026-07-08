package com.example.kulkasku

data class ItemMakanan(
    var id: String = "",
    val id_pengguna: String = "",
    val nama_item: String = "",        // Disesuaikan dengan nama_item di skema NoSQL kamu
    val kategori: String = "",
    val jumlah: String = "0",
    val satuan: String = "Pcs",         // Atribut baru sesuai rancangan dokumen
    val tgl_kedaluwarsa: String = "",
    val tgl_masuk: String = "",         // Atribut baru sesuai rancangan dokumen
    val barcode: String = "",           // Atribut baru persiapan fitur kamera
    val foto_url: String = ""           // Atribut baru sesuai rancangan dokumen
)