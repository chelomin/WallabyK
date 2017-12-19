package com.chelomin.wallaby;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chelomin.wallaby.room.Db;
import com.chelomin.wallaby.room.ProductEntity;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A fragment representing a single ProductDto detail screen.
 * This fragment is either contained in a {@link ProductListActivity}
 * in two-pane mode (on tablets) or a {@link ProductDetailActivity}
 * on handsets.
 */
public class ProductDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Integer index;
    private Db db;
    private ProductEntity item;
    private Activity activity;
    private CollapsingToolbarLayout toolbarLayout;

    @BindView(R.id.product_detail) TextView productDetail;
    @BindView(R.id.image) ImageView image;

    private TextView price;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProductDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            index = getArguments().getInt(ARG_ITEM_ID);

            activity = this.getActivity();
            toolbarLayout = activity.findViewById(R.id.toolbar_layout);
            price = activity.findViewById(R.id.price);

            db = Room.databaseBuilder(Wallaby.getInstance().getApplicationContext(),
                    Db.class, "cache").build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.product_detail, container, false);

        ButterKnife.bind(this, rootView);
        db.productsDao().loadByIndex(index)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ProductEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ProductEntity productEntity) {
                        item = productEntity;
                        if (toolbarLayout != null) {
                            toolbarLayout.setTitle(item.getProductName());
                        }

                        if (price != null) {
                            price.setText(productEntity.getPrice());
                        }
                        productDetail.setText(Html.fromHtml(item.getLongDescription()));

                        Picasso.with(activity).load(item.getProductImage()).into(image);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(activity,
                                "Ooops, Product does not seem to be cached yet...",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

        return rootView;
    }
}
