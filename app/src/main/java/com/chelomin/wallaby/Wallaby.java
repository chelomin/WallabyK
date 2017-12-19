package com.chelomin.wallaby;

import android.app.Application;

/**
 * Created by huge on 12/11/17.
 */

public class Wallaby extends Application {
    private static Wallaby instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static Wallaby getInstance() {
        return instance;
    }
}
