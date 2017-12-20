package com.chelomin.wallaby

import android.app.Activity
import android.arch.persistence.room.Room
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.chelomin.wallaby.room.Db
import com.chelomin.wallaby.room.ProductEntity
import com.squareup.picasso.Picasso
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.product_detail.*

/**
 * A fragment representing a single ProductDto detail screen.
 * This fragment is either contained in a [ProductListActivity]
 * in two-pane mode (on tablets) or a [ProductDetailActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class ProductDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private lateinit var index: Integer
    private var item: ProductEntity? = null
    private var activity: Activity? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null

    private val db: Db by lazy {
        Room.databaseBuilder(Wallaby.instance.applicationContext,
                Db::class.java, "cache").build()
    }

    private var price: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments.containsKey(ARG_ITEM_ID)) {
            index = Integer(arguments.getInt(ARG_ITEM_ID))

            activity = this.getActivity()
            toolbarLayout = activity!!.findViewById(R.id.toolbar_layout)
            price = activity!!.findViewById(R.id.price)
        } else {
            throw IllegalArgumentException("$ARG_ITEM_ID should be passed!")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.product_detail, container, false)

        db.productsDao().loadByIndex(index.toInt())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<ProductEntity> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(productEntity: ProductEntity) {
                        item = productEntity

                        toolbarLayout?.title = productEntity.productName
                        price?.text = productEntity.price

                        product_detail_tv.text = Html.fromHtml(productEntity.longDescription)

                        Picasso.with(activity).load(productEntity.productImage).into(image)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,
                                "Ooops, Product does not seem to be cached yet...",
                                Toast.LENGTH_LONG)
                                .show()
                    }
                })

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val ARG_ITEM_ID = "item_id"
    }
}
