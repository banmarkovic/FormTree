package com.ban.formtree.form.data.remote

import com.ban.formtree.form.data.dto.FormNodeDto
import retrofit2.http.GET

interface FormApi {

    @GET("aruana-lumiform/383e1291df3e0cc1d49aeb14d45f5b94/raw/lumiform-android-test.json")
    suspend fun getForm(): List<FormNodeDto>
}
