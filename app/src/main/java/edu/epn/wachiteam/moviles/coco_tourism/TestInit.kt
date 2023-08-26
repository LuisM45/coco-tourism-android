package edu.epn.wachiteam.moviles.coco_tourism

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import edu.epn.wachiteam.moviles.coco_tourism.services.Globals.Maps.API_KEY
import edu.epn.wachiteam.moviles.coco_tourism.services.Location.Companion.getLocation
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces
import org.chromium.base.Promise
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class TestInit(val activity: Activity) {




    fun main(){
        Log.e("Cookie","Start")
        val location = getLocation()
        init()
        requestPermissions()


//        location.then{Log.i("Cookie",it.toString())}

        val placesPromise: Promise<List<Place>> = Promise()
        location.then{location->
            Log.i("Cookie",location.toString())
            MapPlaces.getPlacesNearby(
                listOf(Place.Field.NAME,Place.Field.ID,Place.Field.LAT_LNG),
                LatLng(location.latitude,location.longitude),
                1000,
                maxcount = 20
                ).then{ places-> placesPromise.fulfill(places) }
        }

//        placesPromise.then{places->
//            val placesImages = places.map { it.name }.toString()
//            Log.i("Cookie",placesNames)
//        }


//        MapPlaces.getPlacesNearby(activity,)
//        requestPermissions()
//        findMe()
    }

    fun init(){
        Places.initialize(activity,API_KEY)
    }


//    fun findAroundMe(){
//        var latLng = LatLng(0.0,0.0)
//        if (ActivityCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        LocationServices.getFusedLocationProviderClient(activity).lastLocation.addOnSuccessListener { lastLocation->
//
//            val query_url = API_URL+"?"+ Utils.buildGetParms(mapOf(
//                "location" to "${lastLocation.latitude},${lastLocation.longitude}",
//                "radius" to "1500",
//                "key" to API_KEY
//            ))
//            Log.i("Cookie",query_url)
//
//            val jsonReq = JsonObjectRequest(Request.Method.GET,
//                query_url,
//                null,
//                {json-> },
//                {}
//            )
//            requestQueue.add(jsonReq)
//        }
//
//
//    }

    fun findMe(){


        val fcpr = FindCurrentPlaceRequest.newInstance(listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
        ))

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val queryRes = Places.createClient(activity).findCurrentPlace(fcpr)
        queryRes.addOnSuccessListener {
            Log.i("LIKELY",it.placeLikelihoods.toString())
        }
        .addOnFailureListener {
                Log.e("LIKELY",it.toString())
        }
    }

     fun requestPermissions(){
        val context = activity.applicationContext
        val permissionFine = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionCoarse = Manifest.permission.ACCESS_COARSE_LOCATION
        val finePermisionState = ContextCompat.checkSelfPermission(context,permissionFine)

        val hasFinePermision = finePermisionState == PackageManager.PERMISSION_GRANTED
        if(!hasFinePermision){
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    permissionFine, permissionCoarse
                )
                ,1)
        }
    }
}