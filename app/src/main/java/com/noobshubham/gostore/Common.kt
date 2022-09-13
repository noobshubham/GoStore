package com.noobshubham.gostore

import com.noobshubham.gostore.model.Results
import com.noobshubham.gostore.remote.IGoogleAPIService
import com.noobshubham.gostore.remote.RetrofitClient

const val GOOGLE_API_URL = "https://maps.googleapis.com/"

object Common {
    var currentResult: Results? = null
    val googleApiService: IGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}