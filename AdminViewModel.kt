package com.nibm.tmapp.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.nibm.tmapp.model.Role
import com.nibm.tmapp.model.User
import com.nibm.tmapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    // Use Firebase.auth directly when needed

    private val _users = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val users: StateFlow<Resource<List<User>>> = _users

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

    private val _deleteState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteState: StateFlow<Resource<Unit>?> = _deleteState

    private val _addUserState = MutableStateFlow<Resource<Unit>?>(null)
    val addUserState: StateFlow<Resource<Unit>?> = _addUserState

    init {
        fetchUsers()
    }

    fun refreshUsers() {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _users.value = Resource.Loading()
            try {
                val snapshot = firestore.collection("users").get().await()
                val userList = snapshot.toObjects(User::class.java)
                _users.value = Resource.Success(userList)
            } catch (e: Exception) {
                _users.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun updateUserRole(user: User, newRole: Role) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            try {
                val updatedRoles = user.roles.toMutableList()
                if (!updatedRoles.contains(newRole)) {
                    updatedRoles.add(newRole)
                }
                firestore.collection("users").document(user.uid).update("roles", updatedRoles).await()
                _updateState.value = Resource.Success(Unit)
                fetchUsers() // Refresh the list after updating
            } catch (e: Exception) {
                _updateState.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            try {
                firestore.collection("users").document(user.uid).delete().await()
                _deleteState.value = Resource.Success(Unit)
                fetchUsers() // Refresh the list after deleting
            } catch (e: Exception) {
                _deleteState.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun addUser(email: String, password: String, role: Role) {
        viewModelScope.launch {
            _addUserState.value = Resource.Loading()
            try {
                val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", roles = mutableListOf(role))
                    firestore.collection("users").document(firebaseUser.uid).set(user).await()
                    _addUserState.value = Resource.Success(Unit)
                    fetchUsers()
                } else {
                    _addUserState.value = Resource.Error("Failed to create user.")
                }
            } catch (e: Exception) {
                _addUserState.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun resetAddUserState() {
        _addUserState.value = null
    }

    fun logoutUser(user: User) {
        viewModelScope.launch {
            // TODO: Implement actual logout logic (revoke tokens or disable user via Firebase Admin SDK)
            // For now, just show a placeholder success
            _updateState.value = Resource.Success(Unit)
        }
    }
}

