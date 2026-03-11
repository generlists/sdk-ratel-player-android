package so.smartlab.video.player.ad.admob.ui.utils

import android.app.Activity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleBus @Inject constructor() {

    private val _events = MutableSharedFlow<ActivityEvent>(
        extraBufferCapacity = 64
    )
    val events: SharedFlow<ActivityEvent> = _events.asSharedFlow()

    fun emit(event: ActivityEvent) {
        _events.tryEmit(event)
    }
}

sealed class ActivityEvent {
    data class Started(val activity: Activity) : ActivityEvent()
    data class Resumed(val activity: Activity) : ActivityEvent()
    data class Paused(val activity: Activity) : ActivityEvent()
    data class Stopped(val activity: Activity) : ActivityEvent()
    data class Destroyed(val activity: Activity) : ActivityEvent()
}