package edu.epn.wachiteam.moviles.coco_tourism.model

data class PointOfInterest(
    var id:String,
    var name:String,
    var majorZone: MajorZone? = null,
    var minorZone: MinorZone? = null,
    var type: Type? = null,
    var reviews: Review? = null,
    var products: ArrayList<Product>? = null,
    var tags: ArrayList<String>? = null
){

    enum class Type{
        RESTAURANT, HOTEL, STORE
    }
}