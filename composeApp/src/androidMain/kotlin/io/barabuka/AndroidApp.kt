package io.barabuka

import android.app.Application
import io.barabuka.inject.AndroidAppComponent
import io.barabuka.inject.create
import io.barabuka.util.unsafeLazy

class AndroidApp : Application() {

    val component: AndroidAppComponent by unsafeLazy {
        AndroidAppComponent.create(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
