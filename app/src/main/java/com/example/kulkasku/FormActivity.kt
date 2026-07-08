package com.example.kulkasku

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FormActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var itemId: String? = null
    private var jumlahLama: String = "0"

    // Inisialisasi ViewModel untuk memanggil master kategori
    private val viewModel: MainViewModel by viewModels()

    // Menyimpan daftar nama kategori secara dinamis dari database
    private val daftarKategoriDinamis = ArrayList<String>()
    private lateinit var adapterKategori: ArrayAdapter<String>

    // SINKRONISASI DIAGRAM: Mekanisme penangkap Aliran Data Scan dari ScannerActivity
    private val barcodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcodeHasilScan = result.data?.getStringExtra("BARCODE_RESULT")
            if (barcodeHasilScan != null) {
                val etBarcode = findViewById<EditText>(R.id.etBarcode)
                etBarcode.setText(barcodeHasilScan)
                Toast.makeText(this, "Barcode Berhasil Dimuat!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        db = FirebaseFirestore.getInstance()

        val etBarcode = findViewById<EditText>(R.id.etBarcode)
        val btnScan = findViewById<Button>(R.id.btnScan)
        val etNama = findViewById<EditText>(R.id.etNama)
        val spKategori = findViewById<Spinner>(R.id.spKategori)
        val etJumlah = findViewById<EditText>(R.id.etJumlah)
        val etTanggal = findViewById<EditText>(R.id.etTanggal)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnHapus = findViewById<Button>(R.id.btnHapus)

        // Pemicu aksi tombol SCAN untuk membuka kamera
        btnScan.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            barcodeLauncher.launch(intent)
        }

        // Setup Awal Spinner dengan list kosong (akan diisi via Observer LiveData)
        adapterKategori = ArrayAdapter(this, android.R.layout.simple_spinner_item, daftarKategoriDinamis)
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKategori.adapter = adapterKategori

        // [SINKRONISASI NO SQL] Mengamati perubahan data koleksi "CATEGORIES" dari Firestore via ViewModel
        viewModel.categoryList.observe(this) { listKategoriMaster ->
            daftarKategoriDinamis.clear()
            for (kategori in listKategoriMaster) {
                daftarKategoriDinamis.add(kategori.nama_kategori)
            }
            adapterKategori.notifyDataSetChanged()

            // Jika dalam mode EDIT, posisikan spinner ke kategori item ini setelah data selesai dimuat
            val kategoriLama = intent.getStringExtra("KATEGORI")
            if (kategoriLama != null) {
                val posisiKategori = adapterKategori.getPosition(kategoriLama)
                if (posisiKategori >= 0) spKategori.setSelection(posisiKategori)
            }
        }

        // Tangkap pesan error dari ViewModel jika ada gangguan koneksi database
        viewModel.errorMessage.observe(this) { pesanError ->
            Toast.makeText(this, pesanError, Toast.LENGTH_SHORT).show()
        }

        // Memerintahkan ViewModel mengambil data dari koleksi root "categories"
        viewModel.fetchKategoriMaster()

        // Setup DatePicker
        etTanggal.isFocusable = false
        etTanggal.isClickable = true
        etTanggal.setOnClickListener {
            val kalender = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                val bulanFormat = String.format("%02d", monthOfYear + 1)
                val hariFormat = String.format("%02d", dayOfMonth)
                etTanggal.setText("$year-$bulanFormat-$hariFormat")
            }, kalender.get(Calendar.YEAR), kalender.get(Calendar.MONTH), kalender.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // Cek Mode Tambah Baru atau Mode Edit
        itemId = intent.getStringExtra("ITEM_ID")
        if (itemId != null) {
            etNama.setText(intent.getStringExtra("NAMA"))
            jumlahLama = intent.getStringExtra("JUMLAH") ?: "0"
            etJumlah.setText(jumlahLama)
            etTanggal.setText(intent.getStringExtra("TANGGAL"))

            val barcodeLama = intent.getStringExtra("BARCODE") ?: ""
            etBarcode.setText(barcodeLama)

            btnSimpan.text = "PERBARUI DATA"
            btnHapus.visibility = View.VISIBLE
        } else {
            btnSimpan.text = "SIMPAN BARANG"
            btnHapus.visibility = View.GONE
        }

        // AKSI SIMPAN (CREATE ATAU UPDATE)
        btnSimpan.setOnClickListener {
            val barcode = etBarcode.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val kategori = if (spKategori.selectedItem != null) spKategori.selectedItem.toString() else "Lainnya"
            val jumlah = etJumlah.text.toString().trim()
            val tanggal = etTanggal.text.toString().trim()

            if (nama.isEmpty() || jumlah.isEmpty() || tanggal.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val waktuSekarang = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val itemData = hashMapOf(
                "id_pengguna" to userId,
                "nama_item" to nama,
                "kategori" to kategori,
                "jumlah" to jumlah,
                "satuan" to "Pcs",
                "tgl_kedaluwarsa" to tanggal,
                "tgl_masuk" to waktuSekarang,
                "barcode" to barcode,
                "foto_url" to ""
            )

            val userRef = db.collection("users").document(userId)

            if (itemId == null) {
                userRef.collection("makanan").add(itemData)
                    .addOnSuccessListener { docRef ->
                        simpanLogAktivitas(userId, docRef.id, "TAMBAH", "Menambahkan $nama", "0", jumlah, nama)

                        // FITUR PENGINGAT: Jadwalkan otomatis notifikasi H-3 dan H-1
                        jadwalkanNotifikasi(nama, tanggal)

                        Toast.makeText(this, "Barang berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            } else {
                userRef.collection("makanan").document(itemId!!).set(itemData)
                    .addOnSuccessListener {
                        simpanLogAktivitas(userId, itemId!!, "UBAH", "Mengubah data $nama", jumlahLama, jumlah, nama)

                        // FITUR PENGINGAT: Perbarui atau daftarkan ulang jadwal notifikasi item ini
                        jadwalkanNotifikasi(nama, tanggal)

                        Toast.makeText(this, "Barang berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }

        // AKSI HAPUS (DELETE)
        btnHapus.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val namaBarang = etNama.text.toString()

            if (itemId != null) {
                db.collection("users").document(userId).collection("makanan").document(itemId!!)
                    .delete()
                    .addOnSuccessListener {
                        simpanLogAktivitas(userId, itemId!!, "HAPUS", "Menghapus $namaBarang", jumlahLama, "0", namaBarang)
                        Toast.makeText(this, "Barang berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }

    private fun simpanLogAktivitas(
        userId: String,
        itemIdRef: String,
        tipeAksi: String,
        detail: String,
        jumlahSebelum: String,
        jumlahSesudah: String,
        namaItem: String
    ) {
        val waktuAksi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val logData = hashMapOf(
            "id_item_ref" to itemIdRef,
            "tipe_aksi" to tipeAksi,
            "detail" to detail,
            "waktu_aksi" to waktuAksi,
            "nama_item" to namaItem,
            "jumlah_sebelum" to jumlahSebelum
        )

        db.collection("users").document(userId).collection("activity_log").add(logData)
    }

    // FITUR PENGINGAT OTOMATIS: Logika menghitung mundur hari dan mendaftarkan pengingat ke OS Android
    @SuppressLint("ScheduleExactAlarm")
    private fun jadwalkanNotifikasi(namaBarang: String, tglKedaluwarsaStr: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val tglExpired = sdf.parse(tglKedaluwarsaStr) ?: return
            val kalenderExpired = Calendar.getInstance().apply { time = tglExpired }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intentReceiver = Intent(this, NotificationReceiver::class.java)

            // Mengatur alarm pemicu otomatis pada H-3 dan H-1 sebelum kedaluwarsa
            val listHariPengingat = listOf(3, 1)

            for (hariSebelum in listHariPengingat) {
                val kalenderNotif = (kalenderExpired.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -hariSebelum)

                    // PEMBARUAN LOGIKA: Set waktu pemicu mengikuti jam saat ini + 2 menit ke depan (Fleksibel untuk Demo Sidang)
                    val waktuSekarang = Calendar.getInstance()
                    set(Calendar.HOUR_OF_DAY, waktuSekarang.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, waktuSekarang.get(Calendar.MINUTE) + 2)
                    set(Calendar.SECOND, 0)
                }

                // Sistem mendaftarkan pengingat jika tanggal kalkulasi berada di masa depan terhadap waktu internal device
                if (kalenderNotif.timeInMillis > System.currentTimeMillis()) {
                    intentReceiver.putExtra("NAMA_BARANG", namaBarang)
                    intentReceiver.putExtra("PESAN", "Awas! Bahan makanan ini akan kedaluwarsa dalam $hariSebelum hari lagi.")

                    val uniqueId = (namaBarang.hashCode() + hariSebelum)
                    intentReceiver.putExtra("NOTIFICATION_ID", uniqueId)

                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        uniqueId,
                        intentReceiver,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Daftarkan ke OS Core Android
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        kalenderNotif.timeInMillis,
                        pendingIntent
                    )

                    // INDIKATOR PELACAK 1: Menandakan registrasi alarm ke OS Android Berhasil
                    Toast.makeText(this, "Alarm H-$hariSebelum Aktif (Memicu dalam 2 Menit)!", Toast.LENGTH_SHORT).show()
                } else {
                    // INDIKATOR PELACAK 2: Menandakan kegagalan karena perhitungan jatuh pada tanggal/waktu lampau
                    Toast.makeText(this, "Gagal H-$hariSebelum: Waktu Berada di Masa Lalu!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}