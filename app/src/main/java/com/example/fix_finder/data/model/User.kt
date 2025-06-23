//package com.example.fix_finder.data.model
//
//
//data class User(
//    val uid: String = "",
//    val name: String = "",
//    val email: String = "",
//    val phone: String = "",
//    val userType: String = "", // "client" or "provider"
//    val profileImageUrl: String? = null,
//    val location: String? = null
//)

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: String = "",
    val profileImageUrl: String? = null,
    val location: String? = null
) : Parcelable
