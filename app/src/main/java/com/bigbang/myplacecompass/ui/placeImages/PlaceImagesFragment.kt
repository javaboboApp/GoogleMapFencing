package com.bigbang.myplacecompass.ui.placeImages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bigbang.myplacecompass.R
import com.bigbang.myplacecompass.model.data.Result
import com.bigbang.myplacecompass.ui.adapter.PlaceImageAdapter
import com.bigbang.myplacecompass.util.ToastHelpers
import com.bigbang.myplacecompass.util.ToastHelpers.showToast
import kotlinx.android.synthetic.main.place_details_fragment_layout.*

class PlaceImagesFragment : Fragment() {

    companion object {
        const val PLACE_KEY = "place.key"
    }

    private val imageAdapter: PlaceImageAdapter = PlaceImageAdapter(mutableListOf())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.place_details_fragment_layout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getSerializable(PLACE_KEY)?.let { place ->
            (place as Result)
            initSlider(place)
            initListeners(place)
        }
    }

    private fun initListeners(place: Result) {
        open_maps_button.setOnClickListener {
            val locationURI =
                Uri.parse("geo:${place.geometry.location.lat},${place.geometry.location.lng}")
            val intent = Intent(Intent.ACTION_VIEW, locationURI)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    private fun initSlider(place: Result) {
        val images = place.photos

        image_recyclerview.adapter = imageAdapter
        image_recyclerview.layoutManager = LinearLayoutManager(context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(image_recyclerview)

        images?.let {
            if (images.isNotEmpty()) {
                imageAdapter.imageList = images
                imageAdapter.notifyDataSetChanged()
            } else {
                showToast(requireContext(), getString(R.string.no_photos),Toast.LENGTH_LONG)
            }
        }
    }


}