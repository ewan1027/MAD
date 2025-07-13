package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nibm.tmapp.model.CourseMaterial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CourseMaterialListState {
    object Loading : CourseMaterialListState()
    data class Success(val materials: List<CourseMaterial>) : CourseMaterialListState()
    data class Error(val message: String) : CourseMaterialListState()
}

class ViewCourseMaterialsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _courseMaterialListState = MutableStateFlow<CourseMaterialListState>(CourseMaterialListState.Loading)
    val courseMaterialListState: StateFlow<CourseMaterialListState> = _courseMaterialListState

    init {
        android.util.Log.d("CourseMaterialsVM", "ViewModel initialized")
        fetchCourseMaterials()
    }

    private fun fetchCourseMaterials() {
        viewModelScope.launch {
            try {
                db.collection("courseMaterials")
                    .orderBy("uploadedAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, e ->
                        android.util.Log.d("CourseMaterialsVM", "Snapshot listener triggered")
                        if (e != null) {
                            val errorMessage = e.message ?: "Unknown error"
                            android.util.Log.e("CourseMaterialsVM", "Error fetching materials: $errorMessage")
                            _courseMaterialListState.value = CourseMaterialListState.Error(errorMessage)
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {

                            val materials = snapshots.toObjects(CourseMaterial::class.java)
                            android.util.Log.d("CourseMaterialsVM", "Fetched ${materials.size} materials")
                            _courseMaterialListState.value = CourseMaterialListState.Success(materials)
                        } else {
                            android.util.Log.d("CourseMaterialsVM", "No materials found in snapshot")
                            _courseMaterialListState.value = CourseMaterialListState.Success(emptyList())
                        }
                    }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred"
                android.util.Log.e("CourseMaterialsVM", "Exception in fetchCourseMaterials: $errorMessage")
                _courseMaterialListState.value = CourseMaterialListState.Error(errorMessage)
            }
        }
    }
}
