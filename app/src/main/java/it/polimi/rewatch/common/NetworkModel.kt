package it.polimi.rewatch.common

import android.os.Build
import androidx.annotation.RequiresApi
import app.moviebase.tmdb.Tmdb3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.polimi.rewatch.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton // Ensures only one instance is ever created
    fun provideTmdbClient(): Tmdb3 {
        // Hilt will call this function whenever something needs Tmdb3
        return Tmdb3(BuildConfig.TMDB_API_KEY)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton // <-- THIS IS THE MAGIC WORD
    fun provideFakeRepo(): FakeRepository {
        return FakeRepository()
    }
}