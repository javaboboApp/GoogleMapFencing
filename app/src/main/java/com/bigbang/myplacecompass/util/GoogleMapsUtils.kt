package com.bigbang.myplacecompass.util

import android.icu.text.CaseMap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

fun GoogleMap.addDefaultMarket(title: String, position:LatLng){

   addMarker( MarkerOptions().position(position).title(title).icon(
        BitmapDescriptorFactory.defaultMarker(150f)

    ))
}