package com.bigbang.myplacecompass.model

import com.bigbang.myplacecompass.model.data.Result
import com.bigbang.myplacecompass.network.IPlacesService
import com.bigbang.myplacecompass.util.Constants.API_KEY
import io.reactivex.Observable

interface IPlacesRepository {
    fun getPlacesNearby(userLocation: String, radius: Double, type: String): Observable<List<Result>>
}

class PlacesRepository(private val placesService: IPlacesService) : IPlacesRepository {

    override fun getPlacesNearby(
        userLocation: String,
        radius: Double,
        type: String
    ): Observable<List<Result>> {
        return placesService.getNearByPlaces(userLocation, radius, type, API_KEY).map {
            it.results
        }
    }

}