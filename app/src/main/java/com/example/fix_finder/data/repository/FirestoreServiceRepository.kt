package com.example.fix_finder.data.repository


import Service
//import com.example.fix_finder.data.model.Services
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreServiceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getProviderServices(): List<Service> {
        val providerId = auth.currentUser?.uid
        println("Current logged-in providerId: $providerId")
        if (providerId == null) return emptyList()

        val snapshot = db.collection("services")
            .whereEqualTo("providerId", providerId)
            .get()
            .await()

        println("Firestore services count: ${snapshot.size()}")
        snapshot.documents.forEach {
            println("Service doc data: ${it.data}")
        }

        return snapshot.toObjects(Service::class.java)
    }



    suspend fun addService(service: Service) {
        db.collection("services")
            .document(service.id)
            .set(service)
            .await()
    }

    suspend fun updateService(service: Service) {
        db.collection("services")
            .document(service.id)
            .set(service)
            .await()
    }

    suspend fun deleteService(serviceId: String) {
        db.collection("services")
            .document(serviceId)
            .delete()
            .await()
    }



}
