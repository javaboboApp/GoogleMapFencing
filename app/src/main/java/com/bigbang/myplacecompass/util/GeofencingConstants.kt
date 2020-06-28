package com.bigbang.myplacecompass.util

import android.content.Context
import com.bigbang.myplacecompass.R
import com.google.android.gms.location.GeofenceStatusCodes
import java.util.concurrent.TimeUnit

object GeofencingConstants {

    fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
            )
            else -> resources.getString(R.string.unknown_geofence_error)
        }
    }

    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(2)
}