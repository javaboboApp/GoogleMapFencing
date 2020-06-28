package com.bigbang.myplacecompass.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bigbang.myplacecompass.model.IPlacesRepository
import com.bigbang.myplacecompass.model.data.Result
import com.google.android.gms.maps.model.Marker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CompassViewModel(private val placesRepository: IPlacesRepository) : BaseViewModel() {

    private val _placesMutableData: MutableLiveData<List<Result>> = MutableLiveData()
    val placesLiveData: LiveData<List<Result>> = _placesMutableData
    private val TAG = "CompassViewModel"

    fun getGetNearbyPlaces(location: String, radius: Double, type: String) {

        subscribe(
            placesRepository.getPlacesNearby(
                location, radius, type
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ placesList ->
                    _placesMutableData.value = placesList
                }, { throwable ->
                    Log.d(TAG, "${throwable.localizedMessage}")
                })
        )
    }



    override fun onCleared() {
        cleanCompositeDisposable()
        super.onCleared()
    }
}