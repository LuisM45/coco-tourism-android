package edu.epn.wachiteam.moviles.coco_tourism

import android.util.Log
import com.google.android.gms.tasks.Task
import org.chromium.base.Promise
import org.json.JSONArray
import java.net.URLEncoder

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

        fun <A> JSONArray.list():List<A>{
            val returnList = mutableListOf<A>()
            for(i in 0 until this.length()){
                returnList.add(this.get(i) as A)
            }

            return returnList
        }

        fun <E,F> Promise<E>.pipe(function:(E)->F):Promise<F>{
            val promise = Promise<F>()
            this.then(
                {e-> promise.fulfill(function(e))},
                {e ->promise.reject(e)}
            )
            return promise
        }

        fun <E,F> Promise<E>.pipeExcept(function:(Exception)->Exception):Promise<E>{
            val promise = Promise<E>()
            this.then(
                {e-> promise.fulfill(e)},
                {e ->promise.reject(function(e))}
            )
            return promise
        }

        fun <E> Promise<E>.listen(function:(E)->Unit):Promise<E>{
            val promise = Promise<E>()
            this.then(
                {e->
                    function(e)
                    promise.fulfill(e)
                },
                {e ->promise.reject(e)}
            )
            return promise
        }

        fun <E,F> Promise<E>.promisePipe(function:(E)->Promise<F>):Promise<F>{
            val promise = Promise<F>()
            this.then(
                {e->
                    function(e).then(
                        {f-> promise.fulfill(f)}
                        ,{ex ->promise.reject(ex)})

                },
                {e ->promise.reject(e)}
            )
            return promise
        }

        fun <E> List<Promise<E>>.toBigPromise():Promise<List<E?>>{
            var remaining = this.size
            val solvedValues = ArrayList<E?>(remaining)
            if (remaining==0) return Promise.fulfilled(solvedValues)


            val bigPromise = Promise<List<E?>>()

            this.forEachIndexed { idx, promise ->
                solvedValues.add(null)
                promise.then(
                    { v ->
                        solvedValues[idx] = v
                        remaining -= 1
                        if(remaining <= 0){
                            bigPromise.fulfill(solvedValues)
                        }
                    },{e->
                        remaining -= 1
                        if(remaining <= 0){
                            bigPromise.fulfill(solvedValues)
                        }
                    }
                )
            }

            return bigPromise
        }

        fun <E> Task<E>.toPromise():Promise<E>{
            val promise = Promise<E>()
            addOnSuccessListener { promise.fulfill(it) }
            addOnFailureListener { promise.reject(it) }
            addOnCanceledListener { Log.i("cookie","uncatched") }

            return promise
        }

        fun <E> Promise<E>.toPromiseObserver():PromiseObserver<E>{
            return PromiseObserver(this)
        }
    }

    class PromiseObserver<T>(private val promise: Promise<T>) {
        private val fulfillCallbacks: MutableList<(T)->Unit> = mutableListOf()
        private val errorCallbacks: MutableList<(Exception)->Unit> = mutableListOf()
        private var rejectException: Exception? = null
        constructor() : this(Promise())

        init {
            promise.then(
                {t->fulfillCallbacks.forEach { it(t) }},
                {ex->rejectException=ex; errorCallbacks.forEach { it(ex) }}
            )
        }

        fun then(callback:(T)->Unit):PromiseObserver<T>{
            if(promise.isFulfilled){
                callback(promise.result)
                return this
            }
            if(promise.isRejected) return this
            fulfillCallbacks.add(callback)
            return this
        }
        fun catch(callback:(Exception)->Unit):PromiseObserver<T>{
            if(promise.isFulfilled){
                return this
            }
            if(promise.isRejected){
                callback(rejectException!!)
                return this
            }
            errorCallbacks.add(callback)
            return this
        }
    }
}