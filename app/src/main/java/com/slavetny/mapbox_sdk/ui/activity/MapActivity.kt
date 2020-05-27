package com.slavetny.mapbox_sdk.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.mapbox.mapboxsdk.Mapbox
import com.slavetny.mapbox_sdk.R

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        findNavController(R.id.navHostFragment).setGraph(
            R.navigation.nav_graph
        )
    }
}