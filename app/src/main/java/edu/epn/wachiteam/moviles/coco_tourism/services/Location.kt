package edu.epn.wachiteam.moviles.coco_tourism.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import org.chromium.base.Promise

class Location {
    companion object{
        lateinit var context: Context

        fun initialize(context:Context) {
            this.context = context
        }

        fun getLocation(): Promise<Location> {
            var promise: Promise<Location> = Promise()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permisons block
            }
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                promise.fulfill(it)
            }.addOnFailureListener{
                promise.reject(it)
            }

            return promise
        }
    }
}