package com.example.kulkasku

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _itemList = MutableLiveData<List<ItemMakanan>>()
    val itemList: LiveData<List<ItemMakanan>> get() = _itemList

    // LiveData Baru untuk menampung daftar kategori dinamis dari Firestore
    private val _categoryList = MutableLiveData<List<Kategori>>()
    val categoryList: LiveData<List<Kategori>> get() = _categoryList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun listenToFoodData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("makanan")
                .orderBy("tgl_kedaluwarsa", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        _errorMessage.value = "Gagal memuat data: ${error.message}"
                        return@addSnapshotListener
                    }

                    val tempList = ArrayList<ItemMakanan>()
                    if (value != null) {
                        for (doc in value.documents) {
                            val item = doc.toObject(ItemMakanan::class.java)
                            if (item != null) {
                                item.id = doc.id
                                tempList.add(item)
                            }
                        }
                    }
                    _itemList.value = tempList
                }
        }
    }

    // SINKRONISASI DIAGRAM: Fungsi mengambil daftar kategori master dari koleksi root "categories" dengan pengurutan
    fun fetchKategoriMaster() {
        db.collection("categories")
            .orderBy("urutan", Query.Direction.ASCENDING) // ➔ Pembaruan: Mengurutkan kategori secara Ascending (1, 2, 3...) sesuai Firebase
            .get()
            .addOnSuccessListener { snapshot ->
                val tempList = ArrayList<Kategori>()
                for (doc in snapshot.documents) {
                    val kat = doc.toObject(Kategori::class.java)
                    if (kat != null) {
                        kat.id_kategori = doc.id
                        tempList.add(kat)
                    }
                }
                _categoryList.value = tempList
            }
            .addOnFailureListener { error ->
                _errorMessage.value = "Gagal mengambil master kategori: ${error.message}"
            }
    }
}