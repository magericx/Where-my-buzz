package com.example.wheremybuzz

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wheremybuzz.model.StatusEnum
import com.example.wheremybuzz.utils.helper.permission.ILocationCallback
import com.example.wheremybuzz.utils.helper.permission.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped

@AndroidEntryPoint
@FragmentScoped
class MapsFragment : Fragment(), OnMapReadyCallback, ILocationCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    companion object {
        private val TAG = "MapsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.activity_maps, container, false)
        mapView = view.findViewById(R.id.map) as MapView
        var mapViewBundle: Bundle? = null
        savedInstanceState.let { bundle ->
            bundle?.let {
                it.getBundle(MAPVIEW_BUNDLE_KEY)?.apply {
                    mapViewBundle = this
                }
            }
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this)
        return view;
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val currentLocation = LatLng(1.3604548926903592, 103.98978075592542)
        initMarkerListeners()
        updateMarkerLocation(currentLocation)
        mapView.onResume()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle: Bundle?
        mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun updateOnResult(location: Location?, statusEnum: StatusEnum) {
        location?.let {
            val currentLocation = LatLng(location.latitude, location.longitude)
            updateMarkerLocation(currentLocation)
//            activity?.let {
//                Toast.makeText(
//                    it.applicationContext, "Received location here in mapFragment",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        }
    }

    private fun updateMarkerLocation(latLng: LatLng) {
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(context?.getString(R.string.current_Location))
                .draggable(true)
        )
        updateCameraView(latLng)
    }

    private fun updateCameraView(latLng: LatLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
    }

    private fun initMarkerListeners() {
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                //do nothing here
            }

            override fun onMarkerDrag(marker: Marker) {
                //do nothing here
            }

            override fun onMarkerDragEnd(marker: Marker) {
                updateCameraView(marker.position)
//                activity?.let {
//                    Toast.makeText(
//                        it.applicationContext,
//                        "New location of marker is ${marker.position.latitude} + ${marker.position.longitude}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
                val location = com.example.wheremybuzz.model.Location(marker.position.latitude,marker.position.longitude)
                (requireActivity() as LocationListener).updateOnResult(location)
            }
        })
    }
}