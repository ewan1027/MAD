package com.nibm.tmapp.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nibm.tmapp.model.Submission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _allSubmissions = MutableStateFlow<List<Submission>>(emptyList())
    val allSubmissions: StateFlow<List<Submission>> = _allSubmissions

    init {
        fetchAllSubmissions()
    }

    private fun fetchAllSubmissions() {
        viewModelScope.launch {
            db.collection("submissions")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        // Handle error
                        _allSubmissions.value = emptyList()
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        _allSubmissions.value = snapshots.documents.mapNotNull { document ->
                            document.toObject(Submission::class.java)?.copy(documentId = document.id)
                        }
                    } else {
                        _allSubmissions.value = emptyList()
                    }
                }
        }
    }
}
