package com.slavetny.mapbox_sdk.ui.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.slavetny.mapbox_sdk.R
import com.slavetny.mapbox_sdk.utils.Constants
import kotlinx.android.synthetic.main.fragment_map.*
import java.lang.Exception

class MapFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var mapboxMap: MapboxMap
    private var routeCoordinates: ArrayList<Point> = ArrayList()
    private var locationEngine: LocationEngine? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            enableLocationComponent(it)

            mapboxMap.style?.addSource(GeoJsonSource("line-source"))

            it.addLayer(
                LineLayer("line-layer", "line-source").withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineOpacity(.7f),
                    PropertyFactory.lineWidth(7f),
                    PropertyFactory.lineColor(Color.parseColor("#3bb2d0"))
                )
            )
        }
    }

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            if (routeCoordinates.isEmpty()) {
                routeCoordinates.add(Point.fromLngLat(result.lastLocation!!.longitude, result.lastLocation!!.latitude))
            } else if (Point.fromLngLat(result.lastLocation!!.longitude, result.lastLocation!!.latitude) != routeCoordinates.get(routeCoordinates.size - 1)) {
                routeCoordinates.add(Point.fromLngLat(result.lastLocation!!.longitude, result.lastLocation!!.latitude))
                drawLine()
            }
        }

        override fun onFailure(exception: Exception) {
            Log.d("CallbackError", exception.message, exception)
        }
    }

    private fun drawLine() {
        val source = mapboxMap.style?.getSourceAs<GeoJsonSource>("line-source")
        source?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(LineString.fromLngLats(routeCoordinates)))))
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            mapboxMap.locationComponent.apply {

                activateLocationComponent(locationComponentActivationOptions)

                isLocationComponentEnabled = true

                cameraMode = CameraMode.TRACKING

                renderMode = RenderMode.COMPASS
            }

            initLocationEngine()
        } else {
            requestForLocationPermission()
        }
    }

    private fun requestForLocationPermission() {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissions, 0)
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())

        val request =
            LocationEngineRequest.Builder(Constants.DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(Constants.DEFAULT_INTERVAL_IN_MILLISECONDS * 5).build()

        locationEngine?.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine?.getLastLocation(callback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocationComponent(mapboxMap.style!!)
                } else {
                    Toast.makeText(requireContext(), "Permission not granted", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                }
            }
        }
    }
}