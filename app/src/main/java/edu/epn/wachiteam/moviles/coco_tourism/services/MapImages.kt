package edu.epn.wachiteam.moviles.coco_tourism.services

import android.graphics.Bitmap
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import org.chromium.base.Promise

class MapImages {
    companion object{
        lateinit var placesClient: PlacesClient;
        fun initialize(placesClient: PlacesClient){
            this.placesClient = placesClient
        }

        fun getImages(photosMetadata: List<PhotoMetadata>): List<Promise<Bitmap>>{
            return photosMetadata.map(::getImage)
        }

        fun getImage(photoMetadata: PhotoMetadata): Promise<Bitmap>{
            val promise = Promise<Bitmap>()
            val request = FetchPhotoRequest.builder(photoMetadata)
//                .setMaxHeight()
//                .setMaxWidth()
                .build()
                placesClient.fetchPhoto(request)
                    .addOnSuccessListener {promise.fulfill(it.bitmap)}
                    .addOnFailureListener {promise.reject(it)}

            return promise
        }
    }
}