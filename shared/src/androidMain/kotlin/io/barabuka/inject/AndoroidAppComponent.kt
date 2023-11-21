package io.barabuka.inject

import android.app.Application
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class AndroidAppComponent(
    @get:Provides val application: Application
) {

    @Provides
    fun someClass(app: Application): SomeClass = SomeClass(app)

    companion object
}

data class SomeClass(
    val app: Application
)
