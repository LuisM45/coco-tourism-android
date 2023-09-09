package edu.epn.wachiteam.moviles.coco_tourism.services

import android.app.Activity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.ktx.firestore

class Globals {
    companion object{

    }
    object Maps{
        val API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
        val API_KEY = "AIzaSyCZnHvMtkadW64vde1xUHNfG2xWw6awITs"

        val PLACE_COMMON_FIELDS = listOf(
            Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS, Place.Field.CURRENT_OPENING_HOURS, Place.Field.RATING, Place.Field.TYPES
        )

        fun initialize(activity:Activity){
            Places.initialize(activity, API_KEY)
        }

    }

    object Network{
        lateinit var cache:DiskBasedCache
        lateinit var network: BasicNetwork
        lateinit var requestQueue: RequestQueue
        fun initialize(activity: Activity){
            cache = DiskBasedCache(activity.cacheDir, 1024 * 1024) // 1MB cap
            network = BasicNetwork(HurlStack())
            requestQueue = RequestQueue(cache, network).apply {start()}
        }

    }

    object Firebase{
        val firebaseFirestore = com.google.firebase.ktx.Firebase.firestore
    }
}