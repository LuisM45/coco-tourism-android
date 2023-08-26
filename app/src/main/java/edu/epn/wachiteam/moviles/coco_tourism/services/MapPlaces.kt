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
import edu.epn.wachiteam.moviles.coco_tourism.model.PointOfInterest
import kotlinx.coroutines.yield
import org.chromium.base.Promise
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class MapPlaces {

    companion object{
        lateinit var placesClient:PlacesClient;
        fun initialize(placesClient:PlacesClient){
            this.placesClient = placesClient
        }


        fun getPlacesNearby(
            fields:List<Place.Field>,
            latLng:LatLng,
            radius:Int,
            maxcount: Int = 20,
            keyword: String? = null,
            language: String? = null,
            maxprice: Int? = null,
            minprice: Int? = null,
            opennow: Boolean = false,
            pagetoken: String? = null,
            rankby: RankBy? = null,
            type: Place.Type? = null
        ):Promise<List<Place>>{
            Log.i("Cookie","PlacesNearby Begin")

            val parameters = mutableMapOf<String,Any>(
                "location" to "${latLng.latitude},${latLng.longitude}",
                "radius" to radius,
                "key" to Globals.Maps.API_KEY)

            if(keyword!=null) parameters["keyword"] = keyword
            if(language!=null) parameters["language"] = language
            if(maxprice!=null) parameters["maxprice"] = maxprice
            if(minprice!=null) parameters["minprice"] = minprice
            if(opennow) parameters["opennow"] = ""
            if(pagetoken!=null) parameters["pagetoken"] = pagetoken
            if(rankby!=null) parameters["rankby"] = rankby
            if(type!=null) parameters["type"] = type.toString().lowercase()

            val promise:Promise<List<Place>> = Promise()
            val places:MutableList<Place> = mutableListOf()


            getNearbyPlacesJSON(parameters,maxcount).then{jsonObjects->
                jsonObjects.forEach{ jsonObject->
                    Log.i("Cookie",jsonObject.toString())
                    val results = jsonObject.getJSONArray("results")
                    for(i in 0 until results.length()){
                        val place = results.getJSONObject(i)
                        getPlaceById(place.getString("place_id"), fields)
                            .then{
                                places.add(it)
                                if(places.size>=maxcount){
                                    promise.fulfill(places)
                                }
                            }
                        }
                    }
                }



            Log.i("Cookie","PlacesNearby End")
            return promise
        }



        private fun getNearbyPlacesJSON(parameters: Map<String,Any>,maxcount: Int): Promise<MutableList<JSONObject>> {
                Log.i("Cookie","Seq init")
                var newParameters = parameters
                var currentCount = maxcount
                val promise:Promise<MutableList<JSONObject>> = Promise()

                if(maxcount<=0) return Promise.fulfilled(mutableListOf())


                var queryUrl = Globals.Maps.API_URL+"?"+ Utils.buildGetParms(newParameters)
                Log.i("Cookie",queryUrl)

                newParameters = mutableMapOf("key" to newParameters["key"]!!,)

                val jsonRequest = JsonObjectRequest(
                        Request.Method.GET,
                        queryUrl,
                        null,
                        {obj->
                            Log.i("Cookie",obj.toString())
                            currentCount -= obj.getJSONArray("results").length()
                            if(!obj.has("next_page_token")) currentCount = 0
                            else newParameters["pagetoken"] = obj.getString("next_page_token")

                            getNearbyPlacesJSON(newParameters,currentCount).then {
                                it.add(0,obj)
                                promise.fulfill(it);
                            }


                            },
                        {}
                    )
                Globals.Network.requestQueue.add(jsonRequest)

            return promise
        }
        private fun getPlaceById(placeId:String,placeFields:List<Place.Field>): Promise<Place> {
            val request = FetchPlaceRequest.newInstance(placeId,placeFields)
            val task = placesClient.fetchPlace(request)
            val promise: Promise<Place> = Promise()

            task.addOnSuccessListener { response->
                promise.fulfill(response.place)
            }.addOnFailureListener { Log.e("Cookie",it.toString()) }

            return promise
        }
    }

    enum class RankBy{
        PROMINENCE, DISTANCE;

        override fun toString(): String {
            return super.toString().lowercase()
        }
    }
}