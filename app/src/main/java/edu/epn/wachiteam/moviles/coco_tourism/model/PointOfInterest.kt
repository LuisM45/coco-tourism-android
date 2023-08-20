package edu.epn.wachiteam.moviles.coco_tourism.model

class PointOfInterest(
    var name:String,
    var majorZone: MajorZone,
    var minorZone: MinorZone,
    var type: PointOfInterest.Type,
    var reviews: Review,
    var products: ArrayList<Product>,
    var tags: ArrayList<String>
){

    enum class Type{
        RESTAURANT, HOTEL, STORE
    }
}