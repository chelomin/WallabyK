package com.chelomin.wallaby.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.support.annotation.UiThread;

import com.chelomin.wallaby.Wallaby;
import com.chelomin.wallaby.api.Api;
import com.chelomin.wallaby.api.ApiHelper;
import com.chelomin.wallaby.config.C;
import com.chelomin.wallaby.model.ProductDto;
import com.chelomin.wallaby.model.ProductListDto;
import com.chelomin.wallaby.room.Db;
import com.chelomin.wallaby.room.ProductEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by huge on 12/8/17.
 */

public class ProductListVm extends ViewModel {
    // Note: tracking all three of the following live data items might be redundant, but i'll keep
    // it this way just to my taste
    private MutableLiveData<Set<Integer>> pagesInProgress;
    private MutableLiveData<Set<Integer>> pagesCompleted;
    private MutableLiveData<Integer> totalItems;

    private ApiHelper apiHelper;
    private Retrofit retrofit;
    private Db db;

    // Note: here and below UiThread is used to make sure that these two getters are not called at
    // the same time by two different threads. I can imagine some better solutions but for now it
    // should work
    @UiThread
    public MutableLiveData<Set<Integer>> getPagesInProgress() {
        initIfNeeded();
        return pagesInProgress;
    }

    @UiThread
    public MutableLiveData<Set<Integer>> getPagesCompleted() {
        initIfNeeded();
        return pagesCompleted;
    }

    @UiThread
    public MutableLiveData<Integer> getTotalItems() {
        initIfNeeded();
        return totalItems;
    }

    private void initIfNeeded() {
        if (pagesInProgress == null || pagesCompleted == null || totalItems == null) {
            pagesInProgress = new MutableLiveData<>();
            pagesCompleted = new MutableLiveData<>();
            totalItems = new MutableLiveData<>();

            pagesCompleted.setValue(new HashSet<Integer>());
            pagesInProgress.setValue(new HashSet<Integer>());

            loadData();
        }
    }

    private void loadData() {
        loadData(1);
    }

    private void loadData(final Integer pageNumber) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(C.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        if (apiHelper == null) {
            apiHelper = new ApiHelper(retrofit.create(Api.class));
        }

        apiHelper.getProducts(pageNumber)
                .subscribeOn(Schedulers.newThread()) // TODO thread pool
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ProductListDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        pagesInProgress.getValue().add(pageNumber);
                    }

                    @Override
                    public void onNext(ProductListDto productListDto) {
                        cachePageData(productListDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        pagesInProgress.getValue().remove(pageNumber);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void cachePageData(final ProductListDto productListDto) {
        // TODO instead of spawning a new thread here it could be better to apply some RxJava
        // mastery above. Will do if have time
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cachePageDataSync(productListDto);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Note: if we just modify the existing set, subscribers will not be notified
                // about it. There should definitely be a better way to do it
                Set<Integer> inProgress = new HashSet<>(pagesInProgress.getValue());
                inProgress.remove(productListDto.pageNumber);
                pagesInProgress.setValue(inProgress);

                Set<Integer> completed = new HashSet<>(pagesCompleted.getValue());
                completed.add(productListDto.pageNumber);
                pagesCompleted.setValue(completed);
                totalItems.setValue(productListDto.totalProducts);
            }
        }.execute();
    }

    private void cachePageDataSync(ProductListDto productListDto) {
        if (productListDto.pageSize != C.PRODUCTS_PER_PAGE) {
            throw new IllegalStateException("Page size returned does not match requested size");
        }

        initDbIfNeeded();

        List<ProductEntity> entities = new ArrayList<>();
        int index = (productListDto.pageNumber-1) * C.PRODUCTS_PER_PAGE;

        for (ProductDto dto : productListDto.products) {
            ProductEntity entity = new ProductEntity(index++, dto);
            entities.add(entity);
        }
        db.productsDao().insertAll(entities);
    }

    private void initDbIfNeeded() {
        if (db == null) {
            db = Room.databaseBuilder(Wallaby.getInstance().getApplicationContext(),
                    Db.class, "cache").build();
        }
    }

    public Single<ProductEntity> getProduct (int index) {
        final int page = getPageForIndex(index);

        if (pagesInProgress.getValue().contains(page)) {
            // Do nothing: page is already loading. client will be updated once done
            return null;
        } else {
            if (pagesCompleted.getValue().contains(page)) {
                return db.productsDao().loadByIndex(index);
            } else {
                loadData(page);
                return null;
            }
        }
    }

    public static int getPageForIndex(final int index) {
        return index/C.PRODUCTS_PER_PAGE+1;
    }
}
