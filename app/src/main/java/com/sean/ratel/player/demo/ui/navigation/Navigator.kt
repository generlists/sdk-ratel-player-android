package com.sean.ratel.player.demo.ui.navigation

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@ActivityRetainedScoped
class
Navigator
@Inject
constructor() {
    private val _navigate =
        MutableSharedFlow<NavTarget>(extraBufferCapacity = 1)

    private val _back =
        MutableSharedFlow<NavBack>(extraBufferCapacity = 1)


    val navigate = _navigate.asSharedFlow()
    val back = _back.asSharedFlow()


    fun navigateTo(
        route: String,
        popBackstack: Boolean = false,
    ) {
        _navigate.tryEmit(NavTarget(route, popBackstack))
    }


    fun navigateBack(recreate: Boolean = false) {
        _back.tryEmit(NavBack(recreate))
    }

    data class NavTarget(
        val route: String,
        val popBackstack: Boolean = false,
    )

    data class NavBack(
        val recreate: Boolean,
    )
}
