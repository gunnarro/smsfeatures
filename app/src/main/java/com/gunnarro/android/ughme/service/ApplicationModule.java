package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.service.impl.WordCloudServiceImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.components.SingletonComponent;

/**
 * Register all services with interfaces here
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class ApplicationModule {

    @Singleton
    @Binds
    public abstract WordCloudService bindWordCloudService(
            WordCloudServiceImpl wordCloudServiceImpl
    );

}
