package edu.epn.wachiteam.moviles.coco_tourism.model

class TourGuide(
    var disponibility: ArrayList<TimeFrame>,
    var contacts: ArrayList<Contact>
){

    inner class Contact(
        var type: String,
        var value: String
    ){}
}