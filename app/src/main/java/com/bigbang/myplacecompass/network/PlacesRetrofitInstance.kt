package com.bigbang.myplacecompass.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object PlacesRetrofitInstance {


    private fun createRetrofit(): Retrofit =
        Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://maps.googleapis.com/")
            .build()


    fun createPlacesService(): IPlacesService =
        createRetrofit().create(IPlacesService::class.java)

}