package edu.epn.wachiteam.moviles.coco_tourism

import android.text.Html
import android.text.Spanned
import android.text.SpannedString
import android.util.Log
import org.chromium.base.Promise
import java.net.URLEncoder
import java.util.function.Supplier

public class Utils {
    companion object{

        fun buildGetParms(map:Map<String,Any>):String{
            return map.mapValues { (k,v)-> v.toString() }
                .mapKeys { (k,v)-> k.replace("\n","") }
                .mapValues { (k,v)-> v.replace("\n","") }
                .mapKeys { (k,v) -> URLEncoder.encode(k,"utf8") }
                .mapValues { (k,v) -> URLEncoder.encode(v,"utf8") }
                .map { (k,v) -> "$k=$v" }
                .joinToString("&")
        }

        fun <A> toBigPromise(promises: List<Promise<A>>):Promise<List<A?>> {
            val bigPromise = Promise<List<A?>>()
            val aList = ArrayList<A?>(promises.size)
            var remaining = promises.size

            promises.forEachIndexed { index, promise ->
                aList.add(null)
                promise.then({
                    aList[index] = it
                    remaining -= 1
                    if (remaining <= 0){
                        Log.i("Cookie","BigPhoto inner")
                        bigPromise.fulfill(aList)
                    }
                }, {
                    remaining -= 1
                    if (remaining <= 0){
                        Log.i("Cookie","BigPhoto inner")
                        bigPromise.fulfill(aList)
                    }
                })


            }
            return bigPromise
        }
    }
}