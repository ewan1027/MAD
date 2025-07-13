package com.nibm.tmapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nibm.tmapp.model.Role
import com.nibm.tmapp.model.User
import com.nibm.tmapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState

    private val _signupState = MutableStateFlow<Resource<AuthResult>?>(null)
    val signupState: StateFlow<Resource<AuthResult>?> = _signupState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        _loginState.value = Resource.Success(user)
                    } else {
                        _loginState.value = Resource.Error("User data not found.")
                    }
                } else {
                    _loginState.value = Resource.Error("Login failed.")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message.toString())
            }
        }
    }

    fun signup(email: String, pass: String) {
        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            try {
                val usersCollection = firestore.collection("users")
                val userCount = usersCollection.get().await().size()
                android.util.Log.d("SignupDebug", "Current user count: $userCount")
                if (userCount > 0) {
                    _signupState.value = Resource.Error("Only the admin can sign up.")
                    return@launch
                }
                val role = Role.ADMIN

                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val user = User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", roles = mutableListOf(role))
                    usersCollection.document(firebaseUser.uid).set(user).await()
                    _signupState.value = Resource.Success(authResult)
                } else {
                    _signupState.value = Resource.Error("Failed to create user.")
                }
            } catch (e: Exception) {
                _signupState.value = Resource.Error(e.message.toString())
            }
        }
    }
}

