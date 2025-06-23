package com.example.fix_finder.data.repository


import User
//import com.example.fix_finder.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String,
        userType: String,
        location: String
    ): Result<User> {
        return try {
            // Create Firebase user
            println("Creating Firebase user...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val firebaseUser = authResult.user ?: throw Exception("User is null after registration")

            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone,
                userType = userType,
                location = location
            )

            println("Saving user to Firestore...")
            // Save to Firestore
            try {
                firestore.collection("users")
                    .document(user.uid)
                    .set(user)
                    .await()
                println("User saved to Firestore.")
            } catch (firestoreException: Exception) {
                println("Firestore save failed: ${firestoreException.message}")
                throw firestoreException
            }

            println("Successfully registered and saved to Firestore: $user")
            Result.success(user)
        } catch (e: Exception) {
            println("Error in registerUser: ${e.message}")
            Result.failure(e)
        }
    }


    // Login existing user
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch user info from Firestore
    suspend fun getUser(uid: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
