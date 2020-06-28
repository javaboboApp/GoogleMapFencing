package com.bigbang.myplacecompass.ui.view

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bigbang.myplacecompass.R
import com.bigbang.myplacecompass.model.data.Result
import com.bigbang.myplacecompass.ui.adapter.PlaceAdapter
import com.bigbang.myplacecompass.ui.placeImages.PlaceImagesFragment
import com.bigbang.myplacecompass.ui.placeImages.PlaceImagesFragment.Companion.PLACE_KEY
import com.bigbang.myplacecompass.util.Constants.RADIUS_CURRENT_LOCATION
import com.bigbang.myplacecompass.util.Constants.ZOOM_CAMERA
import com.bigbang.myplacecompass.util.GeofencingConstants
import com.bigbang.myplacecompass.util.GeofencingUtils.buildGeofencingRequest
import com.bigbang.myplacecompass.util.ToastHelpers.showToast
import com.bigbang.myplacecompass.util.addDefaultMarket
import com.bigbang.myplacecompass.viewmodel.CompassViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class MapActivity : BaseMapActivity(), PopupMenu.OnMenuItemClickListener,
    PlaceAdapter.PlaceClickListener {

    private val placeAdapter: PlaceAdapter = PlaceAdapter(mutableListOf(), this)
    private val placeImageFragment: PlaceImagesFragment = PlaceImagesFragment()


    //di by koin...
    private val compassViewModel: CompassViewModel by viewModel()




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initViews()
        setUpMap()
        subscribeObserver()
    }



    override fun setOnMarkerClickListener(marker: Marker) {
        hideRecyclerView()
        val position = marker.position
        addGeofencePointIntereset(
            buildGeofencingRequest(position.latitude, position.longitude, marker.title),
            object : IGeofenceListener {
                override fun addOnSuccessListener() {
                    // Geofences added.
                    showToast(
                        this@MapActivity,
                        getString(R.string.geofences_added),
                        Toast.LENGTH_LONG
                    )
                }

                override fun addOnFailListener() {
                    // Failed to add geofences.
                    showToast(
                        this@MapActivity,
                        getString(R.string.geofences_not_added),
                        Toast.LENGTH_LONG
                    )

                }

            })

    }

    private fun hideRecyclerView() {
        recyclerView.visibility = View.INVISIBLE
    }


    private fun subscribeObserver() {
        compassViewModel.placesLiveData.observe(this, Observer { displayResults(it) })
    }

    private fun setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initViews() {
        recyclerView.apply {
            isClickable = true
            isFocusable = true
            adapter = placeAdapter
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if ((recyclerView.adapter as PlaceAdapter).itemCount > 0)
                            moveToPosition((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                    }
                }
            })
        }


        map_menu_imageview.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.place_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        my_location_imageview.setOnClickListener {
            initMap()
        }

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    private fun displayResults(resultList: List<Result>) {
        Log.d("TAG_X", "${resultList?.size}")
        resultList?.let { results ->
            placeAdapter.placeList = resultList
            placeAdapter.notifyDataSetChanged()

            drawOnMap(results)
        }
    }


    /*
     *  When the user clicks on the notification, this method will be called, letting us know that
     *  the geofence has been triggered, restart everithing do something else in the next version.
     */

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if (extras != null) {
            if (extras.containsKey(GeofencingConstants.EXTRA_GEOFENCE_INDEX)) {
                initMap()
            }
        }
    }


    private fun drawOnMap(results: List<Result>?) {
        results?.forEach { placeItem ->
            val latLng = LatLng(placeItem.geometry.location.lat, placeItem.geometry.location.lng)
            map?.addDefaultMarket(placeItem.name, latLng)
        }


    }

    fun moveToPosition(position: Int) {
        placeAdapter.placeList[position].let {
            val latLng = LatLng(it.geometry.location.lat, it.geometry.location.lng)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_CAMERA))
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initMap() {
        showRecyclerView()
        map?.clear()
        drawOnMap(compassViewModel.placesLiveData.value)
        addUserMarker()
        map?.addCircle(
            CircleOptions().center(currentLocation).radius(RADIUS_CURRENT_LOCATION)
                .fillColor(resources.getColor(R.color.blue_alpha75, resources.newTheme()))
        )
    }

    private fun showRecyclerView() {
        recyclerView.visibility = View.VISIBLE
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val placeType = item.title.toString().toLowerCase(Locale.ROOT)
        compassViewModel.getGetNearbyPlaces(
            placeLocationListener.locationString,
            RADIUS_CURRENT_LOCATION,
            placeType
        )
        return true
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun setLocation(location: Location) {

        super.setLocation(location)
        addUserMarker()

    }



    override fun selectPlace(place: Result) {

        if (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate(placeImageFragment.tag, 0)

        supportFragmentManager.beginTransaction()
            .add(R.id.place_frame, placeImageFragment.also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putSerializable(PLACE_KEY, place)
                }
            })
            .addToBackStack(placeImageFragment.tag)
            .commit()

    }

}

//constants
private const val TAG = "HomeActivity"

