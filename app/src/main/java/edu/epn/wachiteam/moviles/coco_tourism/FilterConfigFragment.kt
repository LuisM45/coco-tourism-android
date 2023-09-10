package edu.epn.wachiteam.moviles.coco_tourism

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.databinding.FragmentFilterConfigBinding
import edu.epn.wachiteam.moviles.coco_tourism.services.MapPlaces

class FilterConfigFragment : Fragment() {
    lateinit var binding: FragmentFilterConfigBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterConfigBinding.inflate(inflater, container, false)

        with(binding){
            btnAccept.setOnClickListener {
                setServiceVariables()
                findNavController().navigate(R.id.action_FilterFragment_to_MainPageFragment)
            }
        }

        return binding.root
    }

    fun setServiceVariables(){
        MapPlaces.typeFilters = with(binding){
            mutableListOf<Place.Type>()
                .apply { if(cbCafe.isChecked) add(Place.Type.CAFE) }
                .apply { if(cbMuseum.isChecked) add(Place.Type.MUSEUM) }
                .apply { if(cbRestaurant.isChecked) add(Place.Type.RESTAURANT)}
                .apply { if(cbStore.isChecked) add(Place.Type.STORE) }
                .apply { if(cbArtGallery.isChecked) add(Place.Type.ART_GALLERY) }
                .apply { if(cbShoppingMall.isChecked) add(Place.Type.SHOPPING_MALL) }

        }

        try {
            MapPlaces.radiusSearch = binding.etRadius.text.toString().toInt()
        }catch (e: NumberFormatException) {}


    }
}