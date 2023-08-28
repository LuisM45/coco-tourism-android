package edu.epn.wachiteam.moviles.coco_tourism.adapters

import android.graphics.Bitmap
import android.util.Log
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
    val photos: List<Bitmap?>
)
    : RecyclerView.Adapter<PlaceRecyclerViewAdapater.PlaceViewHolder>() {
    inner class PlaceViewHolder(view: View): RecyclerView.ViewHolder(view){
        val ivPhoto: ImageView
        val tvName: TextView

        init {
            ivPhoto = view.findViewById(R.id.iv_photo)
            tvName = view.findViewById(R.id.tv_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_layout_place,parent,false)

        return PlaceViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.tvName.setText(places[position].name)
        holder.ivPhoto.setImageBitmap(photos[position])
    }
}