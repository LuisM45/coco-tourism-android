package edu.epn.wachiteam.moviles.coco_tourism

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.pipe
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.promisePipe
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toBigPromise
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

    private var places: MutableList<Place> = mutableListOf()
    private var images: MutableList<Bitmap?> = mutableListOf()
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

        with(binding){
            tbtnShowFavorites.setOnClickListener { if(tbtnShowFavorites.isChecked) loadToGoPlaces() else loadNearbyPlaces()}
            btnFilter.setOnClickListener { findNavController().navigate(R.id.action_MainPageFragment_to_FilterFragment) }
            btnRefresh.setOnClickListener { if(tbtnShowFavorites.isChecked) loadToGoPlaces() else loadNearbyPlaces() }
        }
        loadNearbyPlaces()

        return binding.root

    }
    fun loadNearbyPlaces():Promise<Unit>{
        return Location.getLocation()
            .promisePipe { location->
                Log.i("Cookie","Tag1")
                Log.i("Cookie",location.toString())
                initMap(LatLng(location.latitude,location.longitude))
                MapPlaces.getPlacesNearby( MapPlaces.DEFAULT_FIELDS,
                    MapPlaces.ApiParameters(LatLng(location.latitude,location.longitude),MapPlaces.radiusSearch),MapPlaces.QUERY_SIZE) }
            .promisePipe{ places ->
                Log.i("Cookie","Tag2")
                Log.i("Cookie",places.toString())
                setMarkers(places)
                this.places = places.toMutableList()
                places.map { place -> MapImages.getImage(place) }.toBigPromise()
            }
            .pipe {images->
                Log.i("Cookie","Tag3")
                this.images = images.toMutableList()
                reloadRecyclerView()
                return@pipe
            }
    }

    fun loadToGoPlaces():Promise<Unit>{
        return FireUser.currentUser.getFavoritePlaces()
            .promisePipe {places->
                this.places = places.toMutableList()
                places.map(MapImages::getImage).toBigPromise()
            }
            .pipe { images->
                this.images = images.toMutableList()
                reloadRecyclerView()
                return@pipe
            }
    }

    fun reloadRecyclerView(){
        Log.i("Cookie","Tag4")
        placesAdapter = PlaceRecyclerViewAdapater(places, images,::focusOnPlace,::startDetailsOfPlace,requireContext())

        binding.rvPlaces.adapter = placesAdapter
        binding.rvPlaces.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context,LinearLayoutManager.HORIZONTAL,false)
        binding.rvPlaces.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        placesAdapter.notifyDataSetChanged()
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


    @SuppressLint("ResourceType")
    fun startDetailsOfPlace(place: Place){
        MapPlaces.lastPlace = place
        findNavController().navigate(R.id.action_MainPageFragment_to_PointOfInterestFragment)
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
            googleMap.clear()
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