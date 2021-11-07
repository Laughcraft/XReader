package com.laughcraft.android.myreader.di.module

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineExceptionHandler
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.db.XDatabase
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.di.HomePath
import com.laughcraft.android.myreader.net.ConversionApi
import com.laughcraft.android.myreader.permission.Api30PermissionChecker
import com.laughcraft.android.myreader.permission.PermissionChecker
import com.laughcraft.android.myreader.permission.Permissioner
import com.laughcraft.android.myreader.permission.SettingPermissionRequester
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
open class MainModule {
    
    companion object {
        private const val BASE_URL = "https://api.convertio.co/"
        private const val API_KEY = "YOUR_API_KEY_HERE"
        const val DOWNLOADS_DIR_REQUEST: String = "DOWNLOADS"
        const val TEMP_DIR_REQUEST: String = "TEMP"
        const val API_KEY_REQUEST = "API_KEY"
        private val homeDirectoryPath = Environment.getExternalStorageDirectory().absolutePath
    }
    
    @Provides fun provideApplication(): Application = XReader.app
    
    @Provides fun provideApplicationContext(): Context = XReader.app.applicationContext
    
    @Provides @Singleton fun provideConversionApi(): ConversionApi {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient
                            .Builder()
                            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
                            .build())
                .build()
                .create(ConversionApi::class.java)
    }
    
    @Provides @Singleton fun provideDb(): XDatabase = XDatabase.getInstance(provideApplicationContext())
    @Provides @Singleton fun provideFavoritesDao(): FavoritesDao = provideDb().getFavoritesDao()
    @Provides @Singleton fun provideRecentsDao(): RecentsDao = provideDb().getRecentsDao()
    
    @Provides @Singleton fun provideExceptionHandler(): CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("OmegaReader.Coroutine", "Exception in coroutine. App level.", throwable)
    }
    
    @Provides @HomePath
    fun provideHomePath(): String = homeDirectoryPath

    @Provides @Named(TEMP_DIR_REQUEST)
    fun provideTempDir(): File {
        return provideApplicationContext().cacheDir
    }

    @Provides @Named(DOWNLOADS_DIR_REQUEST)
    fun provideConvertedFilesDir(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    @Provides @Named(API_KEY_REQUEST)
    fun provideApiKey(): String = API_KEY

    @Provides
    fun providePermissioner(): Permissioner {
        val checker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Api30PermissionChecker() else PermissionChecker()
        val requester = SettingPermissionRequester()
        return Permissioner(checker, requester)
    }
}