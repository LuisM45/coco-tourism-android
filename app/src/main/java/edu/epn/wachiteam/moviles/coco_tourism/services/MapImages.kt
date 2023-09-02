package edu.epn.wachiteam.moviles.coco_tourism.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import org.chromium.base.Log
import org.chromium.base.Promise
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MapImages {
    companion object {
        lateinit var placesClient: PlacesClient;
        lateinit var context: Context;
        fun initialize(placesClient: PlacesClient, context: Context) {
            this.placesClient = placesClient
            this.context = context
        }

        fun getImages(photosMetadata: List<PhotoMetadata>): List<Promise<Bitmap>> {
            return photosMetadata.map(::getImage)
        }

        fun getImage(place:Place): Promise<Bitmap> {
            return if (place.photoMetadatas==null) Promise.rejected()
            else getImage(place.photoMetadatas!!.first(),place.id!!)
        }

        private fun getImage(photoMetadata: PhotoMetadata, localLookupName: String): Promise<Bitmap>{
            val localImage = getImageLocal(localLookupName)
            if(localImage != null) return  Promise.fulfilled(localImage)

            val funPromise = Promise<Bitmap>()
            getImageAPI(photoMetadata).then(
                { bitmap->
                    saveImageLocal(localLookupName,bitmap)
                    funPromise.fulfill(bitmap)
                },
                { e -> funPromise.reject(e) }
            )

            return funPromise
        }

        @Deprecated("Use getImage(place: Place) instead")
        fun getImage(photoMetadata: PhotoMetadata): Promise<Bitmap> {
            return getImage(photoMetadata,photoMetadata.zza())
        }
        private fun getImageLocal(localLookupName: String):Bitmap?{
            Log.i("Cookie","Image local query: $localLookupName")
            return try {
                context
                    .openFileInput(localLookupName)
                    .run { ObjectInputStream(this) }
                    .readObject()
                    .run {
                        if (this == null) return@run null
                        val bytes = this as ByteArray
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
            } catch (e:FileNotFoundException){
                null
            }
        }

        private fun saveImageLocal(localLookupName: String,bitmap: Bitmap){
            val bytes = ByteArrayOutputStream()
                .also { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                .toByteArray()

            Location.context.openFileOutput(localLookupName,Context.MODE_PRIVATE)
                .let { ObjectOutputStream(it) }
                .apply{writeObject(bytes)}
                .close()
        }

        private fun getImageAPI(photoMetadata: PhotoMetadata):Promise<Bitmap>{
            Log.i("Cookie","Image api query: ${photoMetadata.zza()}")
            val promise = Promise<Bitmap>()
            val request = FetchPhotoRequest.builder(photoMetadata)
//                .setMaxHeight()
//                .setMaxWidth()
                .build()
            placesClient.fetchPhoto(request)
                .addOnSuccessListener { promise.fulfill(it.bitmap) }
                .addOnFailureListener { promise.reject(it) }

            return promise
        }
    }
}