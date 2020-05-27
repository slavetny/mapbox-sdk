package com.slavetny.mapbox_sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.mapbox.mapboxsdk.Mapbox

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this,
                getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_map)

        findNavController(R.id.navHostFragment).setGraph(R.navigation.nav_graph)
    }
}