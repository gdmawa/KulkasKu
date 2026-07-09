# KulkasKu 🧊📱

KulkasKu adalah aplikasi *mobile* berbasis Android native yang dirancang untuk mengelola stok bahan makanan dan melacak tanggal kedaluwarsa di dalam kulkas secara efisien. Aplikasi ini mengintegrasikan pemindaian kode batang (*barcode scanning*) untuk input data instan, pencatatan log aktivitas otomatis, serta sistem peringatan dini berbasis kecerdasan waktu lokal demi mencegah pemborosan makanan (*food waste*).

---

## ✨ Fitur Utama

* **Sistem Autentikasi Pengguna:** Login dan registrasi aman yang terintegrasi penuh menggunakan Firebase Authentication.
* **Manajemen Stok Makanan Dinamis:** Fitur penambahan, pembaruan, dan penghapusan item makanan (CRUD) secara *real-time*.
* **Sinkronisasi NoSQL Cloud Firestore:** Penyimpanan data terstruktur menggunakan model hierarki sub-koleksi (*sub-collections*) berkinerja tinggi, menjamin data tetap sinkron di berbagai perangkat.
* **Pemindai Kode Batang (Barcode Scanner):** Input cepat menggunakan modul kamera bawaan perangkat untuk membaca kode batang kemasan produk makanan.
* **Log Aktivitas Riwayat Otomatis:** Setiap aksi tambah, ubah, dan hapus barang tercatat otomatis ke dalam sub-koleksi `activity_log` untuk pelacakan inventaris.
* **Pengingat Kedaluwarsa Otomatis (H-3 & H-1):** Fitur pencegahan yang memanfaatkan `AlarmManager` inti Android untuk memicu *Push Notification* lokal tepat pada pukul 08:00 pagi waktu setempat ketika makanan mendekati masa kedaluwarsa.

---

## 🛠️ Arsitektur & Teknologi

* **Bahasa Pemrograman:** Kotlin 🚀
* **Arsitektur Kode:** MVVM (Model-View-ViewModel) dengan LiveData Observer.
* **Database & Backend:** Cloud Firestore (NoSQL Document-Oriented) & Firebase Auth.
* **Komponen Native Android:**
  * `AlarmManager` & `BroadcastReceiver` (Untuk penjadwalan alarm dan notifikasi handal latar belakang).
  * `ActivityResultContracts` (Untuk pertukaran aliran data antar-activity kamera pemindai).
  * `NotificationChannel` (Kompatibilitas penuh untuk Android 8.0 Oreo hingga Android 13+).

---

## 📐 Desain Struktur Data NoSQL (Firestore)

Aplikasi ini menerapkan teknik *denormalisasi* dan struktur hierarkis untuk meminimalkan beban query jaringan:

```text
root
└── categories (collection)
│   └── {categoryId} (document) ➔ [nama_kategori, urutan]
│
└── users (collection)
    └── {userId} (document) ➔ [nama, email, fcm_token]
        ├── makanan (sub-collection)
        │   └── {makananId} (document) ➔ [nama_item, kategori, jumlah, satuan, tgl_kedaluwarsa, barcode]
        │
        └── activity_log (sub-collection)
            └── {logId} (document) ➔ [tipe_aksi, detail, nama_item, jumlah_sebelum, waktu_aksi]
