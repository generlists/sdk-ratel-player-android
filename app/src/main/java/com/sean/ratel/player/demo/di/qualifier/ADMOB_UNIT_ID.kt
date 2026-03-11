package com.sean.ratel.player.demo.di.qualifier

import javax.inject.Qualifier



@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ADMOB_UNIT_ID


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TEST_DEVICE_HASHED_ID


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AD_OPEN_UNIT_ID


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BANNER_UNIT_ID

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NATIVE_AD_UNIT_ID

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ADAPTIVE_BANNER_UNIT_ID


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class INTERSTITIAL_UNIT_ID

