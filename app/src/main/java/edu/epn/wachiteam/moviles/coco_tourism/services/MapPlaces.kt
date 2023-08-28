package edu.epn.wachiteam.moviles.coco_tourism.services

import android.app.Activity
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import edu.epn.wachiteam.moviles.coco_tourism.Utils
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.list
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toBigPromise
import edu.epn.wachiteam.moviles.coco_tourism.model.PointOfInterest
import kotlinx.coroutines.yield
import org.chromium.base.Promise
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class MapPlaces {

    companion object {
        lateinit var placesClient: PlacesClient;
        fun initialize(placesClient: PlacesClient) {
            this.placesClient = placesClient
        }

        fun getPlacesById(ids: List<String>, fields: List<Place.Field>): List<Promise<Place>> {
            return ids.map { id -> getPlaceById(id, fields) }
        }

        fun getPlaceById(id: String, fields: List<Place.Field>): Promise<Place> {
            val promise = Promise<Place>()
            val request = FetchPlaceRequest.newInstance(id, fields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener { promise.fulfill(it.place) }
                .addOnFailureListener { e -> promise.reject(e); ;Log.e("Cookie",e.toString()) }

            return promise
        }

        fun getPlacesNearby(
            fields: List<Place.Field>, apiParameters: ApiParameters, maxcount: Int = 20): Promise<List<Place>> {
            val promise = Promise<List<Place>>()

            val placesJson = getNearbyPlaces(apiParameters,maxcount)
            placesJson.then(
                { jsonList->
                    jsonList
                        .map { it.getJSONArray("results") }
                        .map { it.list<JSONObject>() }
                        .flatMap { it.asIterable() }
                        .map { it.getString("place_id") }
                        .map { getPlaceById(it, fields) }
                        .let { toBigPromise(it) }
                        .then{ promise.fulfill(it.map {p-> p!! }) }
                },
                {e->promise.reject(e); Log.e("Cookie",e.toString())}
            )



            return promise
        }



        fun getNearbyPlaces(apiParameters: ApiParameters, maxcount: Int = 20): Promise<List<JSONObject>> {
            val promise = Promise<List<JSONObject>>()
            val places = mutableListOf<JSONObject>()
            var remainingCount = maxcount

            fun getNextPages(oldResponse: JSONObject, apiParameters: ApiParameters){
                Log.i("Cookie","Pages: $remainingCount, ${oldResponse.has("next_page_token")}")
                if(remainingCount<=0 || !oldResponse.has("next_page_token")){
                    promise.fulfill(places)
                    return
                }

                val newParameters = ApiParameters(
                    pagetoken = oldResponse.getString("next_page_token"),
                    key = apiParameters.key
                )

                getPlacesNearbyBatch(newParameters).then(
                    {
                        places.add(it)
                        remainingCount -= it.getJSONArray("results").length()
                        getNextPages(it,newParameters)
                    },
                    {promise.fulfill(places)}
                )
            }

            getPlacesNearbyBatch(apiParameters).then(
                {
                    places.add(it)
                    remainingCount -= it.getJSONArray("results").length()
                    getNextPages(it,apiParameters)
                },
                {e-> promise.reject(e); ;Log.e("Cookie",e.toString())}
            )

            return promise
        }


        fun getPlacesNearbyBatch(apiParameters: ApiParameters): Promise<JSONObject> {
            val promise: Promise<JSONObject> = Promise()
            var queryUrl = Globals.Maps.API_URL + "?" + Utils.buildGetParms(apiParameters.map())
            Log.i("Cookie","Query: $queryUrl")

            val request = JsonObjectRequest(
                Request.Method.GET,
                queryUrl,
                null,
                { obj -> promise.fulfill(obj) },
                { e -> promise.reject(e);Log.e("Cookie",e.toString()) }
            )
            Globals.Network.requestQueue.add(request)

            return promise
        }


    }
    enum class RankBy {
        PROMINENCE, DISTANCE;

        override fun toString(): String {
            return super.toString().lowercase()
        }
    }

    data class ApiParameters(
        val location: LatLng? = null,
        val radius: Int? = null,
        val keyword: String? = null,
        val language: String? = null,
        val maxprice: Int? = null,
        val minprice: Int? = null,
        val opennow: Boolean = false,
        val pagetoken: String? = null,
        val rankby: RankBy? = null,
        val type: Place.Type? = null,
        val key: String = Globals.Maps.API_KEY
    ){
        fun map():Map<String,Any> {
            val thisMap = mutableMapOf<String,Any>()
            if(location!=null) thisMap["location"] = "${location.latitude},${location.longitude}"
            if(radius!=null) thisMap["radius"] = radius
            if(keyword!=null) thisMap["keyword"] = keyword
            if(language!=null) thisMap["language"] = language
            if(maxprice!=null) thisMap["maxprice"] = maxprice
            if(minprice!=null) thisMap["minprice"] = minprice
            if(opennow) thisMap["opennow"] = ""
            if(pagetoken!=null) thisMap["pagetoken"] = pagetoken
            if(rankby!=null) thisMap["rankby"] = rankby
            if(type!=null) thisMap["type"] = type
            thisMap["key"] = key
    


            return thisMap
        }
    }
}