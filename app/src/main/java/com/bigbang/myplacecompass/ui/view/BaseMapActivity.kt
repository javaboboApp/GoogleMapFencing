package com.bigbang.myplacecompass.ui.view

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bigbang.myplacecompass.BuildConfig
import com.bigbang.myplacecompass.R
import com.bigbang.myplacecompass.broadcastreceivers.GeofenceBroadcastReceiver
import com.bigbang.myplacecompass.util.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*

open abstract class BaseMapActivity : AppCompatActivity(), OnMapReadyCallback,
    PlaceLocationListener.LocationDelegate {

    private lateinit var locationManager: LocationManager
    protected  var map: GoogleMap? = null
    protected  var currentLocation: LatLng? = null
    protected val placeLocationListener: PlaceLocationListener = PlaceLocationListener(this)
    private  var userMarker: Marker? = null

    private lateinit var geofencingClient: GeofencingClient

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT

        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create channel for notifications from api 26
        createChannel(this)
        initGeofencingClient()
        initLocationManager()


    }

    private fun initLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun initGeofencingClient() {
        geofencingClient = LocationServices.getGeofencingClient(this)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setUpMapListener()
    }


    protected fun addUserMarker() {

        var icon = BitmapFactory.decodeResource(resources, R.drawable.me_icon)
        icon = Bitmap.createScaledBitmap(icon, 150, 150, false)
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        userMarker?.let { marker ->
            marker.remove()
        }
        currentLocation?.let {currentLocation->
            userMarker =
                map?.addMarker(
                    MarkerOptions().position(currentLocation)
                        .title(getString(R.string.yout_current_position_market_title)).icon(
                            BitmapDescriptorFactory.fromBitmap(icon)
                        )
                )

            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, Constants.ZOOM_CAMERA))
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpMapListener() {
        map?.setOnMarkerClickListener {
            if (currentLocation != it.position) {
                map?.clear()
                map?.addCircle(
                    CircleOptions().center(it.position)
                        .radius(GeofencingConstants.GEOFENCE_RADIUS_IN_METERS.toDouble())
                        .fillColor(resources.getColor(R.color.red_alpha75, resources.newTheme()))
                )
                addUserMarker()
                setOnMarkerClickListener(it)

            }

            true
        }
    }

    abstract fun setOnMarkerClickListener(marker: Marker)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setLocation(location: Location) {
        val auxLocation = LatLng(location.latitude, location.longitude)
        if(currentLocation == null) {
            map?.addCircle(
                CircleOptions().center(auxLocation).radius(Constants.RADIUS_CURRENT_LOCATION)
                    .fillColor(resources.getColor(R.color.blue_alpha75, resources.newTheme()))
            )
        }
        currentLocation = auxLocation
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        //Request Permission
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@BaseMapActivity,
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            //fine location is int the position of the array...
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            //background location is the second position of the array
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // Permission denied.
            SnackBarHelper.makeSnackBar(
                this,
                activity_maps,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()


        } else {

            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    interface IGeofenceListener {
        fun addOnSuccessListener();
        fun addOnFailListener();
    }

    @SuppressLint("MissingPermission")
    protected fun addGeofencePointIntereset(
        geofencingRequest: GeofencingRequest,
        iGeofenceListener: IGeofenceListener
    ) {
        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
                        iGeofenceListener.addOnSuccessListener()

                    }
                    addOnFailureListener {
                        // Failed to add geofences.
                        iGeofenceListener.addOnFailListener()

                    }
                }
            }
        }
    }


    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(applicationContext, R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    protected fun checkPermissionsAndStartGeofencing() {

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    override fun onDestroy() {
        removeGeofences()
        unRegisterLocationManager()
        super.onDestroy()

    }

    private fun unRegisterLocationManager() {
        locationManager.removeUpdates(placeLocationListener)
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            200,
            5f,
            placeLocationListener
        )
    }

    /*
      *  When we get the result from asking the user to turn on device location, we call
      *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
      *  we don't resolve the check to keep the user from seeing an endless loop.
      */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }


    /*
       *  Uses the Location Client to check the current state of location settings, and gives the user
       *  the opportunity to turn on location services within our app.
       */
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@BaseMapActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                SnackBarHelper.makeSnackBar(
                    this,
                    activity_maps,
                    R.string.location_required_error,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                registerLocationListener()
            }
        }
    }




}


//constants properties
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private val runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

private const val TAG = "BaseMapActivity"