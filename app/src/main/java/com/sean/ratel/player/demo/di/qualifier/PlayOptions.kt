package com.sean.ratel.player.demo.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AutoPlay

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Mute

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Control

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EnableJsApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FS

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Rel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IVLoadPolicy

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CCLoadPolicy

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CCLangPref
