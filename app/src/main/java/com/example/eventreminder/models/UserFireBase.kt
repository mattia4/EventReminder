package com.example.eventreminder.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserFireBase {
    @SerializedName("uid")
    @Expose
    private val Uid: String? = null

    @SerializedName("userEmail")
    @Expose
    private val UserEmail: String? = null

    @SerializedName("userName")
    @Expose
    private val UserName: String? = null

}