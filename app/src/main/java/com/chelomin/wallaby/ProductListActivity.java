package com.chelomin.wallaby;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.chelomin.wallaby.config.C;
import com.chelomin.wallaby.room.ProductEntity;
import com.chelomin.wallaby.viewmodel.ProductListVm;
import com.squareup.picasso.Picasso;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * An activity representing a list of Products. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProductDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProductListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isTwoPane;
    private ProductListVm viewModel;
    private RecyclerView recyclerView;
    private ProductsItemRecyclerViewAdapter adapter;

    @BindView(R.id.debug)
    protected View debugView;

    @BindView(R.id.in_progress)
    protected TextView inProgressTv;

    @BindView(R.id.completed)
    protected TextView completedTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.product_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true;
        }

        viewModel = ViewModelProviders.of(this).get(ProductListVm.class);

        final Observer<Integer> totalItemsObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer totalItems) {
                updateTotalItems(totalItems);
            }
        };

        final Observer<Set<Integer>> pagesLoadedObserver = new Observer<Set<Integer>>() {
            @Override
            public void onChanged(@Nullable Set<Integer> pages) {
                updatePagesLoaded(pages);
            }
        };

        final Observer<Set<Integer>> pagesInProgressObserver = new Observer<Set<Integer>>() {
            @Override
            public void onChanged(@Nullable Set<Integer> pages) {
                updatePagesInProgress(pages);
            }
        };

        viewModel.getPagesCompleted().observe(this, pagesLoadedObserver);
        viewModel.getPagesInProgress().observe(this, pagesInProgressObserver);
        viewModel.getTotalItems().observe(this, totalItemsObserver);

        recyclerView = findViewById(R.id.product_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        debugView.setVisibility(C.SHOW_DEBUG_INFO ? View.VISIBLE : View.GONE);
    }

    private void updateTotalItems(int totalItems) {
        adapter.setTotalItems(totalItems);
    }

    private void updatePagesLoaded(Set<Integer> pages) {
        recyclerView.getAdapter().notifyDataSetChanged();
        completedTv.setText("Pages completed: " + pages.toString());
    }

    private void updatePagesInProgress(Set<Integer> pages) {
        inProgressTv.setText("Pages loading: " + pages);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new ProductsItemRecyclerViewAdapter(this, viewModel, isTwoPane);
        recyclerView.setAdapter(adapter);
    }

    public static class ProductsItemRecyclerViewAdapter
            extends RecyclerView.Adapter<ProductsItemRecyclerViewAdapter.ViewHolder> {

        private final ProductListActivity mParentActivity;
        private final ProductListVm mViewModel;
        private int totalItems = 0;
        private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(C.THREADS_RECYCLER);

        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = (Integer) view.getTag();

                // Check if the item is loaded
                if (!mViewModel.getPagesCompleted().getValue().contains(index/C.PRODUCTS_PER_PAGE+1)) {
                    return;
                }

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ProductDetailFragment.ARG_ITEM_ID, index);
                    ProductDetailFragment fragment = new ProductDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.product_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailFragment.ARG_ITEM_ID, index);

                    context.startActivity(intent);
                }
            }
        };

        ProductsItemRecyclerViewAdapter(ProductListActivity parent,
                                        ProductListVm viewModel,
                                        boolean twoPane) {
            mViewModel = viewModel;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Single<ProductEntity> productEntity = mViewModel.getProduct(position);

            if (productEntity != null) {

                productEntity
                        .subscribeOn(Schedulers.from(threadPoolExecutor))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<ProductEntity>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(ProductEntity productEntity) {
                                holder.name.setText(productEntity.getProductName());
                                holder.price.setText(productEntity.getPrice());
                                holder.loading.setVisibility(View.GONE);
                                holder.loadingBg.setVisibility(View.GONE);

                                Picasso.with(mParentActivity)
                                        .load(productEntity.getProductImage())
                                        .fit()
                                        .into(holder.icon);

                                holder.itemView.setTag(position);
                                holder.itemView.setOnClickListener(mOnClickListener);
                            }

                            @Override
                            public void onError(Throwable e) {
                                // TODO handle error
                            }
                        });
            } else {
                holder.loading.setVisibility(View.VISIBLE);
                holder.loadingBg.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return totalItems;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.name) TextView name;
            @BindView(R.id.price) TextView price;
            @BindView(R.id.thumbnail) ImageView icon;
            @BindView(R.id.loading_pb) ProgressBar loading;
            @BindView(R.id.loading_bg) ImageView loadingBg;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
