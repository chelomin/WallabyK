package com.chelomin.wallaby

import android.app.Application

/**
 * Created by huge on 12/11/17.
 */

class Wallaby : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    companion object {
        lateinit var instance: Wallaby
            private set
    }
}
