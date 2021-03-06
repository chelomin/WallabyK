package com.chelomin.wallaby.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.persistence.room.Room
import android.os.Handler
import com.chelomin.wallaby.Wallaby
import com.chelomin.wallaby.api.Api
import com.chelomin.wallaby.api.ApiHelper
import com.chelomin.wallaby.config.C
import com.chelomin.wallaby.model.ProductListDto
import com.chelomin.wallaby.room.Db
import com.chelomin.wallaby.room.ProductEntity
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

/**
 * Created by huge on 12/8/17.
 */

class ProductListVm : ViewModel() {
    // Note: tracking all three of the following live data items might be redundant, but i'll keep
    // it this way just to my taste
    val pagesInProgress: MutableLiveData<Set<Int?>> by lazy {
        loadDataIfNeeded()
        var ld: MutableLiveData<Set<Int?>> = MutableLiveData()
        ld.value = HashSet()
        ld
    }

    val pagesCompleted: MutableLiveData<Set<Int?>> by lazy {
        loadDataIfNeeded()
        var ld: MutableLiveData<Set<Int?>> = MutableLiveData()
        ld.value = HashSet()
        ld
    }

    val totalItems: MutableLiveData<Int> by lazy {
        loadDataIfNeeded()
        var ld: MutableLiveData<Int> = MutableLiveData()
        ld.value = 0
        ld
    }

    private val apiHelper: ApiHelper by lazy {
        ApiHelper(retrofit.create(Api::class.java))
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(C.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private val db: Db by lazy {
        Room.databaseBuilder(Wallaby.instance.applicationContext,
                Db::class.java, "cache").build()
    }

    private val handler: Handler = Handler()

    private var firstGet: Boolean = true

    private fun loadDataIfNeeded() {
        if (firstGet) {
            firstGet = false
            handler.post({ loadData() })
        }
    }

    private fun loadData(pageNumber: Int? = 1) {
        apiHelper.getProducts(pageNumber)
                .subscribeOn(Schedulers.newThread()) // TODO thread pool, if makes sense
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ProductListDto> {
                    override fun onSubscribe(d: Disposable) {
                        pagesInProgress.value = pagesInProgress.value!!.plus(pageNumber)
                    }

                    override fun onNext(productListDto: ProductListDto) {
                        cachePageData(productListDto)
                    }

                    override fun onError(e: Throwable) {
                        pagesInProgress.value = pagesInProgress.value!!.minus(pageNumber)
                    }

                    override fun onComplete() {

                    }
                })

    }

    private fun cachePageData(productListDto: ProductListDto) {
        thread {
            cachePageDataSync(productListDto)
            handler.post({
                pagesInProgress.value = pagesInProgress.value!!.minus(productListDto.pageNumber)
                pagesCompleted.value = pagesCompleted.value!!.plus(productListDto.pageNumber)
                totalItems.value = productListDto.totalProducts
            })
        }
    }

    private fun cachePageDataSync(productListDto: ProductListDto) {
        if (productListDto.pageSize != C.PRODUCTS_PER_PAGE) {
            throw IllegalStateException("Page size returned does not match requested size")
        }

        var index = (productListDto.pageNumber!! - 1) * C.PRODUCTS_PER_PAGE

        val entities = productListDto.products!!.map { ProductEntity(index++, it) }

        db.productsDao().insertAll(entities)
    }

    /**
     * returns a product if it's already cached in db, otherwise returns null and:
     * either requests it from the backend
     * or does nothing if already requested
     */
    fun getProduct(index: Int): Single<ProductEntity>? {
        val page = getPageForIndex(index)

        return if (pagesInProgress.value!!.contains(page)) null else {
            if (pagesCompleted.value!!.contains(page)) {
                db.productsDao().loadByIndex(index)
            } else {
                loadData(page)
                null
            }
        }
    }

    companion object {
        fun getPageForIndex(index: Int): Int = index / C.PRODUCTS_PER_PAGE + 1
    }
}
