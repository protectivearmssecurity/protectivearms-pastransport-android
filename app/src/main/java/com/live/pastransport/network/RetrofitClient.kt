package com.live.pastransport.network

import android.util.Log

import com.google.gson.GsonBuilder
import com.live.pastransport.base.*
import com.live.pastransport.sockets.SocketManager

import com.live.pastransport.utils.getPrefrence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitClient {

    @Provides
    @Singleton
    fun provideSocketManager(): SocketManager {
        return SocketManager()
    }

    @Singleton
    @Provides
    fun provideRetrofitInterface(): RetrofitInterface {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(provideHttpClient())
            .build()
        return retrofit.create(RetrofitInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(provideHeaderInterceptor())
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->

            // Log the selected language and headers for debugging
            if (getPrefrence(AUTH_TOKEN, "").isNotEmpty()) {
                Log.d("Headers", "Authorization: Bearer ${getPrefrence(AUTH_TOKEN, "")}")
            }


            val requestBuilder = chain.request().newBuilder()
                .header(SECRET_KEY, SECRET_KEY_VALUE)
                .header(PUBLISH_KEY, PUBLISH_KEY_VALUE)

                .header("Accept", "application/json")

            if (getPrefrence(AUTH_TOKEN, "").isNotEmpty()) {
                requestBuilder.header(AUTHORIZATION, "Bearer " +  getPrefrence(AUTH_TOKEN, ""))
            }

            chain.proceed(requestBuilder.build())
        }
    }


}