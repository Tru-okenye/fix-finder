//package com.example.fix_finder.data.model
//
//data class Service(
//    val id: String = "",
//    val title: String = "",
//    val description: String = "",
//    val price: String = "",
//    val providerId: String = ""
//)

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Service(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val providerId: String = ""
) : Parcelable
