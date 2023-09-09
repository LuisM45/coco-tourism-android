package edu.epn.wachiteam.moviles.coco_tourism.services

import android.util.Log
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.listen
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.pipe
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.promisePipe
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toBigPromise
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toPromise
import edu.epn.wachiteam.moviles.coco_tourism.services.Globals.Maps.PLACE_COMMON_FIELDS
import org.chromium.base.Promise

class FireUser(
    val uid: String
){
    val reference = Firebase.firestore.collection("user").document(uid)
    fun toggleFavorite(place: Place):Promise<Boolean>{
        val promise = Promise<Boolean>()
        isFavorite(place).then(
            {
                if(it) {
                    removeFavorite(place)
                    promise.fulfill(false)
                }

                else{
                    addFavorite(place)
                    promise.fulfill(true)
                }
            },
            { Log.e("Cookie",it.toString())}
        )
        return promise
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

    fun getFavoritePlaces():Promise<List<Place>>{
        Log.i("Cookie","uid: $uid")

        return reference.collection(FAVORITES_PLACES_FIELD).get().toPromise()
            .promisePipe { querySnap->
                querySnap.documents.map { MapPlaces.getPlaceById(it.id, PLACE_COMMON_FIELDS) }
                    .toBigPromise()
                    .pipe { it.filterNotNull() }
            }
    }

    companion object{
        private val FAVORITES_PLACES_FIELD = "favorite_places"
        private val PLACE_ID_FIELD = "place_id"
        lateinit var currentUser: FireUser;

        fun initialize(){

        }

        fun signInWithEmailAndPassword(email:String,password:String): Promise<Unit>{
            return FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email,password)
                .toPromise()
                .listen { currentUser = FireUser(FirebaseAuth.getInstance().currentUser!!.uid)!! }
                .pipe {  }
        }

        fun createUserWithEmailAndPassword(email:String,password:String): Promise<Unit>{
            return FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password)
                .toPromise()
                .listen { currentUser = FireUser(FirebaseAuth.getInstance().currentUser!!.uid)!! }
                .pipe {  }

        }

        fun sendPasswordResetEmail(email:String):Promise<Unit>{
            return FirebaseAuth.getInstance().sendPasswordResetEmail(email).toPromise().pipe {   }
        }
    }
}