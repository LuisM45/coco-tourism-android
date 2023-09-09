package edu.epn.wachiteam.moviles.coco_tourism.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import edu.epn.wachiteam.moviles.coco_tourism.R
import edu.epn.wachiteam.moviles.coco_tourism.Utils.Companion.toPromiseObserver
import edu.epn.wachiteam.moviles.coco_tourism.services.FireUser

class PlaceRecyclerViewAdapater(
    val places: List<Place>,
    val photos: List<Bitmap?>,
    var onPlaceClickListener: (place:Place)-> Unit = {},
    var moreinfoConsumer: (place:Place)-> Unit ={},
    val context: Context
)
    : RecyclerView.Adapter<PlaceRecyclerViewAdapater.PlaceViewHolder>() {
    inner class PlaceViewHolder(view: View): RecyclerView.ViewHolder(view){
        val ivPhoto: ImageView
        val tvName: TextView
        var ibtnFavoritos: ImageButton
        var btnVerMas: Button
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
            ibtnFavoritos = view.findViewById(R.id.ibtn_favorite)
            btnVerMas = view.findViewById(R.id.btn_verMas)

            ibtnFavoritos.setOnClickListener { toggleFavorite() }
            btnVerMas.setOnClickListener { moreinfoConsumer(place) }
        }

        fun updateStyle(){
            FireUser.currentUser.isFavorite(place).toPromiseObserver()
                .then { isFav -> if(isFav) setFavoriteStyle() else removeFavoriteStyle() }
                .catch {e-> Log.i("Cookie",e.toString()) }
        }

        fun setFavoriteStyle(){
            ibtnFavoritos.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.heart_solid,null))
        }

        fun removeFavoriteStyle(){
            ibtnFavoritos.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.heart_regular,null))

        }

        fun toggleFavorite(){
            FireUser.currentUser.toggleFavorite(place).toPromiseObserver()
                .then { isFav -> if(isFav) setFavoriteStyle() else removeFavoriteStyle() }
                .catch {e-> Log.i("Cookie",e.toString()) }

        }

        fun fill(place:Place,photo:Bitmap?){
            this.place = place
            tvName.setText(place.name)
            ivPhoto.setImageBitmap(photo)

        }

        fun toggleSelect(){ isSelected = !isSelected}

        fun openDetails(){

        }
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
        val photo = photos[position]

        holder.fill(place,photo)
        holder.updateStyle()
    }
}