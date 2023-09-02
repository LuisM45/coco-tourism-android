package edu.epn.wachiteam.moviles.coco_tourism.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.R

class PlaceRecyclerViewAdapater(
    val places: List<Place>,
    val photos: List<Bitmap?>,
    var onPlaceClickListener: (place:Place)-> Unit = {}
)
    : RecyclerView.Adapter<PlaceRecyclerViewAdapater.PlaceViewHolder>() {
    inner class PlaceViewHolder(view: View): RecyclerView.ViewHolder(view){
        val ivPhoto: ImageView
        val tvName: TextView
        lateinit var place: Place

        var onChangeSelect:(Boolean)->Unit = {}
        var isSelected: Boolean = false;set(value) {
            onChangeSelect(value)
            field = value
        }

        init {
            isSelected = false
            ivPhoto = view.findViewById(R.id.iv_photo)
            ivPhoto.setOnClickListener{onPlaceClickListener(place)}
            tvName = view.findViewById(R.id.tv_name)
        }

        fun toggleSelect(){ isSelected = !isSelected}

    }


    private val placeToPosition: Map<Place,Int> =
        places.mapIndexed { index, place -> place to index }.toMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_layout_place,parent,false)

        return PlaceViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    fun getPlacePosition(place: Place): Int? {
        return placeToPosition[place]
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.place = place
        holder.tvName.setText(place.name)
        holder.ivPhoto.setImageBitmap(photos[position])
    }
}