package it.polimi.rewatch

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val options = FirebaseOptions.Builder()
            .setProjectId("app-rewatch")
            .setApplicationId("1:57189479251:android:23c118b07b08572eede7a6")   // from google-services.json
            .setApiKey("***REMOVED_FIREBASE_KEY***")                      // from google-services.json
            .setStorageBucket("app-rewatch.appspot.com")
            .build()

        FirebaseApp.initializeApp(this, options)
    }
}