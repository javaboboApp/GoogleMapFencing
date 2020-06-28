package com.bigbang.myplacecompass.util

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest


object GeofencingUtils {

    fun buildGeofencingRequest(lat: Double, lng: Double, requestId:String): GeofencingRequest {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(requestId)

            // Set the circular region of this geofence.
            .setCircularRegion(
                lat,
                lng,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        return GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.

            .addGeofence(geofence)
            .build()


    }
}