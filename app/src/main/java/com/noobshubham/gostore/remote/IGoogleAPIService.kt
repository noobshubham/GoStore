package com.noobshubham.gostore.remote

import com.noobshubham.gostore.model.MyPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url: String): Call<MyPlaces>
}