package edu.epn.wachiteam.moviles.coco_tourism.services

import android.util.Log
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.epn.wachiteam.moviles.coco_tourism.services.Globals.Maps.PLACE_COMMON_FIELDS
import org.chromium.base.Promise

class FireUser(
    val uid: String
){
    val reference = Firebase.firestore.collection("user").document(uid)
    fun toggleFavorite(place: Place){
        isFavorite(place).then(
            {
                if(it) removeFavorite(place)
                else addFavorite(place)
            },
            { Log.e("Cookie",it.toString())}
        )
    }

    fun isFavorite(place: Place):Promise<Boolean>{
        Log.e("Cookie","Is Favorite")
        val promise = Promise<Boolean>()

        reference.collection(FAVORITES_PLACES_FIELD)
            .whereEqualTo(PLACE_ID_FIELD,place.id)
            .get()
            .addOnFailureListener{ promise.fulfill(false); Log.e("Cookie",it.toString())}
            .addOnSuccessListener {
                promise.fulfill(it.size()>0)
            }

        return promise
    }
    fun addFavorite(place:Place){
        Log.e("Cookie","Add Favorite")

        reference.collection(FAVORITES_PLACES_FIELD)
            .document(place.id)
            .set(mapOf(PLACE_ID_FIELD to place.id))
            .addOnFailureListener{ Log.e("Cookie",it.toString()) }
    }
    fun removeFavorite(place: Place){
        Log.e("Cookie","Remove Favorite")
        reference.collection(FAVORITES_PLACES_FIELD)
            .document(place.id)
            .delete()
            .addOnFailureListener{ Log.e("Cookie",it.toString()) }
    }

    fun getFavoritePlaces():Promise<List<Promise<Place>>>{
        Log.i("Cookie","uid: $uid")
        val promise = Promise<List<Promise<Place>>>()
        reference.collection(FAVORITES_PLACES_FIELD).get()
            .addOnFailureListener { promise.reject(it) }
            .addOnSuccessListener {
                Log.i("Cookie",it.toString())
                val l = it.map { MapPlaces.getPlaceById(it.id,PLACE_COMMON_FIELDS) }
                promise.fulfill(l)
            }

        return promise
    }

    companion object{
        private val FAVORITES_PLACES_FIELD = "favorite_places"
        private val PLACE_ID_FIELD = "place_id"
        lateinit var currentUser: FireUser;

        fun initialize(){
            currentUser = FireUser(FirebaseAuth.getInstance().currentUser!!.uid)

        }
    }
}