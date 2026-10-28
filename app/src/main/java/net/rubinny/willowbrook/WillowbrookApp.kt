package net.rubinny.willowbrook

import android.app.Application

class WillowbrookApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
