package edu.epn.wachiteam.moviles.coco_tourism

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.adapters.PlaceRecyclerViewAdapater
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentMainPageBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.FireUser
import edu.epn.wachiteam.moviles.coco_tourism.services.Location
import edu.epn.wachiteam.moviles.coco_tourism.services.MapImages
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces
import org.chromium.base.Promise

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainPageFragment : Fragment() {

    private var _binding: FragmentMainPageBinding? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var placesAdapter: PlaceRecyclerViewAdapater

    private var markerToPlace: MutableMap<Marker,Place> = mutableMapOf()
    private var placeToMarker: MutableMap<Place,Marker> = mutableMapOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainPageBinding.inflate(inflater, container, false)

        FireUser.currentUser.getFavoritePlaces()
        bindPlaceAdapter()

        return binding.root

    }

    fun bindPlaceAdapter(){
        val placesPromise: Promise<List<Place>> = Promise()
        val photosPromise: Promise<List<Bitmap?>> = Promise()
        val locationPromise = Location.getLocation()

        locationPromise.then{ location->
            initMap(LatLng(location.latitude,location.longitude))
            MapPlaces.getPlacesNearby(
                listOf(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS),
                MapPlaces.ApiParameters(
                    LatLng(location.latitude,location.longitude),
                    1000),
                maxcount = 20
            ).then{
                Log.i("Cookie",it.size.toString())
                placesPromise.fulfill(it)
            }
        }

        placesPromise.then{ places->
            val photoPromises = places.map { place->
                if(place.photoMetadatas !=null){
                    setMarkers(places)
                    MapImages.getImage(place)
                }else{
                    Promise.rejected()
                }
            }

            Utils.toBigPromise(photoPromises).then{ it ->
                Log.i("Cookie","BigPhoto fullfiled")
                photosPromise.fulfill(it)
            }
        }

        placesPromise.then{photosPromise.then{
            Log.i("Cookie","Ayayaya")
            placesAdapter = PlaceRecyclerViewAdapater(placesPromise.result,photosPromise.result,::focusOnPlace)
            binding.rvPlaces.adapter = placesAdapter
            binding.rvPlaces.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context,LinearLayoutManager.HORIZONTAL,false)
            binding.rvPlaces.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            placesAdapter.notifyDataSetChanged()
        }}



    }

    @SuppressLint("PotentialBehaviorOverride")
    fun initMap(latLng: LatLng){
        val mapFragment = childFragmentManager.findFragmentById(R.id.fa_map) as SupportMapFragment
        mapFragment.getMapAsync{
                googleMap = it
                centerMinimap(latLng,16f)

            googleMap.setOnMarkerClickListener {
                focusOnMarker(it)
                return@setOnMarkerClickListener false
            }
        }


    }

    // Usually when clicked on RecyclerView
    fun focusOnPlace(place:Place){
        val rvIndex = placesAdapter.getPlacePosition(place)!!
        binding.rvPlaces.scrollToPosition(rvIndex)
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.latLng))
    }

    // Usually when clickd on Map
    fun focusOnMarker(marker: Marker){
        val place = markerToPlace[marker]!!
        val rvIndex = placesAdapter.getPlacePosition(place)!!
        binding.rvPlaces.scrollToPosition(rvIndex)
    }

    fun setMarkers(places: List<Place>){
        places.forEach{place->
            val marker = googleMap.addMarker(
                MarkerOptions().position(place.latLng).title(place.name)
            )
            if(marker==null) return@forEach
            markerToPlace[marker] = place
            placeToMarker[place] = marker
        }
    }

    fun centerMinimap(latLng: LatLng, zoom:Float){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,zoom)
        googleMap.moveCamera(cameraUpdate)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}