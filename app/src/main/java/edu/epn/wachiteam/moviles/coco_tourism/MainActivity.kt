package edu.epn.wachiteam.moviles.coco_tourism

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import edu.epn.wachiteam.moviles.coco_tourism.databinding.ActivityMainBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.Globals
import edu.epn.wachiteam.moviles.coco_tourism.services.Location
import edu.epn.wachiteam.moviles.coco_tourism.services.MapImages
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Location.initialize(this    )
        Globals.Network.initialize(this)
        Globals.Maps.initialize(this)
        MapPlaces.initialize(Places.createClient(this))
        MapImages.initialize(Places.createClient(this))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        with(binding){
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

//        TestInit(this).main()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}