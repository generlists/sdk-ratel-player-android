package com.sean.ratel.player.demo.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NotControl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WithControl