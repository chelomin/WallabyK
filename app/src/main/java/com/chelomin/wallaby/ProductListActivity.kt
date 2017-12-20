package com.chelomin.wallaby


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.chelomin.wallaby.config.C
import com.chelomin.wallaby.room.ProductEntity
import com.chelomin.wallaby.viewmodel.ProductListVm
import com.squareup.picasso.Picasso
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

import kotlinx.android.synthetic.main.activity_product_list.*

/**
 * An activity representing a list of Products. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ProductDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ProductListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var isTwoPane: Boolean = false
    private lateinit var viewModel: ProductListVm
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductsItemRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        if (findViewById<View>(R.id.product_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true
        }

        viewModel = ViewModelProviders.of(this).get(ProductListVm::class.java)

        val totalItemsObserver = Observer<Int> { totalItems -> updateTotalItems(totalItems!!) }

        val pagesLoadedObserver = Observer<Set<Int?>> { pages -> updatePagesLoaded(pages) }

        val pagesInProgressObserver = Observer<Set<Int?>> { pages -> updatePagesInProgress(pages) }

        viewModel.pagesCompleted.observe(this, pagesLoadedObserver)
        viewModel.pagesInProgress.observe(this, pagesInProgressObserver)
        viewModel.totalItems.observe(this, totalItemsObserver)

        recyclerView = findViewById(R.id.product_list)
        setupRecyclerView(recyclerView)

        debug.visibility = if (C.SHOW_DEBUG_INFO) View.VISIBLE else View.GONE
    }

    private fun updateTotalItems(totalItems: Int) {
        adapter.setTotalItems(totalItems)
    }

    private fun updatePagesLoaded(pages: Set<Int?>?) {
        recyclerView.adapter.notifyDataSetChanged()
        completed.text = "Pages completed: " + pages?.toString()
    }

    private fun updatePagesInProgress(pages: Set<Int?>?) {
        in_progress.text = "Pages loading: " + pages?.toString()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = ProductsItemRecyclerViewAdapter(this, viewModel, isTwoPane)
        recyclerView.adapter = adapter
    }

    class ProductsItemRecyclerViewAdapter internal constructor(private val mParentActivity: ProductListActivity,
                                                               private val mViewModel: ProductListVm,
                                                               private val mTwoPane: Boolean) : RecyclerView.Adapter<ProductsItemRecyclerViewAdapter.ViewHolder>() {
        private var totalItems = 0
        private val mOnClickListener = View.OnClickListener { view ->
            val index = view.tag as Int

            // Check if the item is loaded
            if (!mViewModel.pagesCompleted.value!!.contains(index / C.PRODUCTS_PER_PAGE + 1)) {
                return@OnClickListener
            }

            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(ProductDetailFragment.ARG_ITEM_ID, index)
                val fragment = ProductDetailFragment()
                fragment.arguments = arguments
                mParentActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.product_detail_container, fragment)
                        .commit()
            } else {
                val context = view.context
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra(ProductDetailFragment.ARG_ITEM_ID, index)

                context.startActivity(intent)
            }
        }

        fun setTotalItems(totalItems: Int) {
            this.totalItems = totalItems
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val productEntity = mViewModel.getProduct(position)

            if (productEntity != null) {

                productEntity
                        .subscribeOn(Schedulers.from(threadPoolExecutor))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<ProductEntity> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(productEntity: ProductEntity) {
                                holder.name.text = productEntity.productName
                                holder.price.text = productEntity.price
                                holder.loading.visibility = View.GONE
                                holder.loadingBg.visibility = View.GONE

                                Picasso.with(mParentActivity)
                                        .load(productEntity.productImage)
                                        .fit()
                                        .into(holder.icon)

                                holder.itemView.tag = position
                                holder.itemView.setOnClickListener(mOnClickListener)
                            }

                            override fun onError(e: Throwable) {
                                // TODO handle error
                            }
                        })
            } else {
                holder.loading.visibility = View.VISIBLE
                holder.loadingBg.visibility = View.VISIBLE
            }
        }

        override fun getItemCount(): Int {
            return totalItems
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById(R.id.name)
            var price: TextView = view.findViewById(R.id.price)
            var icon: ImageView = view.findViewById(R.id.thumbnail)
            var loading: ProgressBar = view.findViewById(R.id.loading_pb)
            var loadingBg: ImageView = view.findViewById(R.id.loading_bg)
        }

        companion object {
            private val threadPoolExecutor = Executors.newFixedThreadPool(C.THREADS_RECYCLER)
        }
    }
}
