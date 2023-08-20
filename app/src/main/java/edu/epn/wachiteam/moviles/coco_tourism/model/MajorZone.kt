package edu.epn.wachiteam.moviles.coco_tourism.model

data class MajorZone(
    public var name: String,
    public var minorZones: ArrayList<MinorZone>
) {
}