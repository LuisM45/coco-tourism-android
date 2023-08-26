package edu.epn.wachiteam.moviles.coco_tourism

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.adapters.PlaceRecyclerViewAdapater
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentMainPageBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.Location
import edu.epn.wachiteam.moviles.coco_tourism.services.MapImages
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces
import org.chromium.base.Promise

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainPageFragment : Fragment() {

    private var _binding: FragmentMainPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainPageBinding.inflate(inflater, container, false)

        bindPlaceAdapter()

        return binding.root

    }

    fun bindPlaceAdapter(){
        val location = Location.getLocation()

        val placesDataPromise = Promise<List<Place>>()
        location.then{location->
            MapPlaces.getPlacesNearby(
                listOf(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS),
                LatLng(location.latitude,location.longitude),
                1000
            ).then{
                placesDataPromise.fulfill(it)
            }
        }

        val photosPromise = Promise<List<Bitmap?>>()
        placesDataPromise.then{places->

            val photoPromises = places.map { place->
                if(place.photoMetadatas !=null){
                    MapImages.getImage(place.photoMetadatas.first())
                }else{
                    Promise.rejected()
                }
            }

            Utils.toBigPromise(photoPromises).then{ photos ->
                Log.i("Cookie","BigPhoto fullfiled")
                photosPromise.fulfill(photos)
            }
        }



        photosPromise.then{ photos->
            val places = placesDataPromise.result
            Log.i("Cookie","Ayayaya")
            val placesAdapter = PlaceRecyclerViewAdapater(places,photos)
            binding.rvPlaces.adapter = placesAdapter
            binding.rvPlaces.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context,LinearLayoutManager.HORIZONTAL,false)
            binding.rvPlaces.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            placesAdapter.notifyDataSetChanged()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}