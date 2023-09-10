package edu.epn.wachiteam.moviles.coco_tourism.services

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import edu.epn.wachiteam.moviles.coco_tourism.Utils
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.list
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.pipe
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toBigPromise
import org.chromium.base.Promise
import org.json.JSONObject

class MapPlaces {

    companion object {
        val DEFAULT_FIELDS: List<Place.Field> = Globals.Maps.PLACE_COMMON_FIELDS
        var QUERY_SIZE = 20
        var locationSearch = LatLng(0.0, 0.0)
        var radiusSearch = 1000
        var typeFilters = listOf<Place.Type>()

        lateinit var placesClient: PlacesClient;
        lateinit var lastPlace: Place // Had to do this sorry, fragment
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
                        .let { it.toBigPromise() }
                        .then{ promise.fulfill(it.map {p-> p!! }) }
                },
                {e->promise.reject(e); Log.e("Cookie",e.toString())}
            )



            return promise
        }



        fun getNearbyPlaces(apiParameters: ApiParameters, maxcount: Int = QUERY_SIZE): Promise<List<JSONObject>> {
            Log.i("Cookie","Nearby Place")
            Log.i("Cookie",if(apiParameters.type==null) "null" else apiParameters.type.toString()!!)
            if(apiParameters.type == null && typeFilters.isNotEmpty()){
                return typeFilters
                    .map {type-> getNearbyPlaces(apiParameters.copy(type = type),maxcount)}
                    .toBigPromise()
                    .pipe {listOfLists-> listOfLists.filterNotNull().flatten() }
            }

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
        var location: LatLng? = null,
        var radius: Int? = null,
        var keyword: String? = null,
        var language: String? = null,
        var maxprice: Int? = null,
        var minprice: Int? = null,
        var opennow: Boolean = false,
        var pagetoken: String? = null,
        var rankby: RankBy? = null,
        var type: Place.Type? = null,
        var key: String = Globals.Maps.API_KEY
    ){
        fun map():Map<String,Any> {
            val thisMap = mutableMapOf<String,Any>()
            if(location!=null) thisMap["location"] = "${location!!.latitude},${location!!.longitude}"
            if(radius!=null) thisMap["radius"] = radius!!
            if(keyword!=null) thisMap["keyword"] = keyword!!
            if(language!=null) thisMap["language"] = language!!
            if(maxprice!=null) thisMap["maxprice"] = maxprice!!
            if(minprice!=null) thisMap["minprice"] = minprice!!
            if(opennow) thisMap["opennow"] = ""
            if(pagetoken!=null) thisMap["pagetoken"] = pagetoken!!
            if(rankby!=null) thisMap["rankby"] = rankby!!
            if(type!=null) thisMap["type"] = type!!.toString().lowercase()
            thisMap["key"] = key
    


            return thisMap
        }
    }
}