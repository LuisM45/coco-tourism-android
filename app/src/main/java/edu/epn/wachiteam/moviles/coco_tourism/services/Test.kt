package edu.epn.wachiteam.moviles.coco_tourism.services

import org.chromium.base.Promise

class Test {
    companion object{
        fun <A> List<Promise<A>>.aBigPromise(promises: List<Promise<A>>):Promise<List<A?>>{
            val bigPromise = Promise<List<A?>>()

            val aList = ArrayList<A?>(promises.size)
            var remaining = aList.size
            promises.forEachIndexed{ index, promise ->
                promise.then({
                    aList[index]=it
                    remaining -= 1
                    if(remaining<=0) bigPromise.fulfill(aList)
                },{
                    aList[index]=null
                    remaining -= 1
                    if(remaining<=0) bigPromise.fulfill(aList)
                })


            }
        return  bigPromise
    }
}

}