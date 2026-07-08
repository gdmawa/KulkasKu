package com.example.kulkasku

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val namaBarang = intent.getStringExtra("NAMA_BARANG") ?: "Barang"
        val pesan = intent.getStringExtra("PESAN") ?: "Akan segera kedaluwarsa!"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        val channelId = "kulkasku_notif_channel"

        // Menggunakan NotificationManager standar OS Android (Lebih kompatibel)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Buat Notification Channel (Wajib untuk Android 8.0 ke atas)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Kedaluwarsa KulkasKu",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran notifikasi untuk pengingat makanan kedaluwarsa"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // 2. Aksi ketika Notifikasi di-klik (Membuka MainActivity)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Bangun Struktur Notifikasi
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Ikon alarm bawaan sistem android
            .setContentTitle("Peringatan Kedaluwarsa: $namaBarang")
            .setContentText(pesan)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Aktifkan suara dan getar bawaan HP
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. Tembakkan Notifikasi langsung ke layar lewat manager inti
        manager.notify(notificationId, builder.build())
    }
}