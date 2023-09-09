package edu.epn.wachiteam.moviles.coco_tourism

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentPointOfInterestBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.MapImages
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PointOfInterestFragment : Fragment() {

    private var _binding: FragmentPointOfInterestBinding? = null
    private lateinit var googleMap: GoogleMap

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var place:Place

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPointOfInterestBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onResume() {
        super.onResume()
        place = MapPlaces.lastPlace
        Log.i("Cookie",place.name!!.toString())
        initMap(place.latLng)

        with(binding){
            MapImages.getImage(place).then{imageView2.setImageBitmap(it)}
            tvName.text = place.name
            tvAddress.text = place.address
            tvType.text = place.types?.toString()
            tvSchedules.text = place.currentOpeningHours?.weekdayText.toString()
            tvReviews.text = place.rating?.toString()
            tvDistance.text = "Idk"
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun initMap(latLng: LatLng){
        val mapFragment = childFragmentManager.findFragmentById(R.id.fa_map) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            centerMinimap(latLng,16f)
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