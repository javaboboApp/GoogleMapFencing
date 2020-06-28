package com.bigbang.myplacecompass.util

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class PlaceLocationListener(private val locationDelegate: LocationDelegate) : LocationListener {
    lateinit var locationString: String
    lateinit var locationLatLng: Location

    override fun onLocationChanged(location: Location) {
        locationDelegate.setLocation(location)
        locationString = "${location.latitude},${location.longitude}"
        locationLatLng = location
    }
    // Do nothing
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    // Do nothing
    override fun onProviderEnabled(provider: String?)  = Unit

    // Do nothing
    override fun onProviderDisabled(provider: String?) = Unit


    interface LocationDelegate {
        fun setLocation(location: Location)
    }
}