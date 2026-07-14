package com.sean.ratel.player.demo.ui.cast.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sean.ratel.player.demo.MainActivity
import com.sean.ratel.player.demo.R
import com.sean.ratel.player.demo.ui.cast.YouTubePlayersManager
import com.sean.ratel.player.demo.ui.cast.notifications.CastNotificationAction.ACTION_STOP_CAST
import com.sean.ratel.player.demo.ui.cast.notifications.CastNotificationAction.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class CastForegroundService : Service() {
    @Inject
    lateinit var castNotificationManager: CastNotificationManager

    @Inject
    lateinit var youTubePlayersManager: YouTubePlayersManager

    private lateinit var mediaSession: MediaSessionCompat

    private val serviceJob = SupervisorJob()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var playbackSpeed = 1f
    private var playbackCurrentTime = 0L
    private var currentState = PlaybackStateCompat.STATE_PAUSED

    override fun onCreate() {
        super.onCreate()
        Log.d("CastForegroundService", "onCreate")

        createChannel()
        setupMediaSession()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.d("CastForegroundService", "onStartCommand , action : ${intent?.action}")
        if (intent?.action == ACTION_STOP_CAST) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification =
            buildNotification(
                CastNotificationState(
                    isVisible = true,
                    title = "로딩중...",
                    channelName = "",
                    deviceName = "",
                    thumbnail = null,
                    thumbUrl = null,
                    isPlaying = false,
                ),
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        observeState()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CastForegroundService", "onDestroy")
        castNotificationManager.release()
        mediaSession.release()
        serviceScope.cancel()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun observeState() {
        castNotificationManager.state
            .onEach { state ->
                Log.d("CastForegroundService", "observeState : $state")
                if (state.isVisible) {
                    updateNotification(state)
                } else {
                    hideNotification()
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.duration
            .onEach { duration ->
                duration?.let {
                    Log.d("LLLLLLLffff", "ddddd : $it")
                    if (it > 0) updateDuration(it.toLong())
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.currentTime
            .onEach { currentTime ->
                currentTime?.let {
                    Log.d("LLLLLLLffff", "ddddd : $it")
                    playbackCurrentTime = it.toLong()
                    if (it > 0) updatePlaybackState(it.toLong())
                }
            }.launchIn(serviceScope)

        youTubePlayersManager.currentPlaySpeed
            .onEach { speed ->
                speed.let {
                    Log.d("LLLLLLLffff", "speed : $it")
                    playbackSpeed = speed.rate
                    updatePlaybackSpeed(playbackSpeed)
                }
            }.launchIn(serviceScope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(state: CastNotificationState) {
        val notification = buildNotification(state)
        NotificationManagerCompat
            .from(this)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun hideNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(state: CastNotificationState): Notification {
        val openIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    action = CastNotificationAction.ACTION_OPEN_CAST_SCREEN
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat
            .Builder(this, CastNotificationAction.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cast_connected_24dp)
            .setLargeIcon(state.thumbnail)
            .setContentTitle(state.title)
            .setContentText("${state.deviceName}에 캐스팅 중")
            .setSubText(state.channelName)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat
                    .MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
            )
            // 액션 1: 이전
            .addAction(
                buildAction(
                    R.drawable.ic_skip_previous,
                    "이전",
                    CastNotificationAction.ACTION_PREVIOUS,
                ),
            )
            // 액션 2: 재생/정지
            .addAction(
                buildAction(
                    if (state.isPlaying) R.drawable.ayp_ic_pause_36dp else R.drawable.ayp_ic_play_36dp,
                    if (state.isPlaying) "일시정지" else "재생",
                    if (state.isPlaying) CastNotificationAction.ACTION_PAUSE else CastNotificationAction.ACTION_PLAY,
                ),
            )
            // 액션 3: 다음
            .addAction(
                buildAction(
                    R.drawable.ic_skip_next,
                    "다음",
                    CastNotificationAction.ACTION_NEXT,
                ),
            ).build()
    }

    private fun buildAction(
        iconRes: Int,
        title: String,
        action: String,
    ): NotificationCompat.Action {
        Log.d("CastForegroundService", "buildAction :  $action")
        val intent =
            Intent().apply {
                this.action = action
                `package` = packageName
            }

        val pi =
            PendingIntent.getBroadcast(
                this,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        Log.d("CastForegroundService", "pi=$pi action=$action")

        return NotificationCompat.Action.Builder(iconRes, title, pi).build()
    }

    private fun createChannel() {
        val channel =
            NotificationChannel(
                CastNotificationAction.CHANNEL_ID,
                "Cast 재생 컨트롤",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "TV로 캐스팅 중일 때 표시"
                setShowBadge(false)
            }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private val actions =

        PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

    private fun setupMediaSession() {
        mediaSession =
            MediaSessionCompat(this, "CastMediaSession").apply {
                isActive = true

                // 초기 상태
                setPlaybackState(
                    PlaybackStateCompat
                        .Builder()
                        .setActions(actions)
                        .setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            0L,
                            1f,
                        ).build(),
                )

                // metadata (duration 필수)
                setMetadata(
                    MediaMetadataCompat
                        .Builder()
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0L) // 처음엔 0
                        .build(),
                )

                setCallback(
                    object : MediaSessionCompat.Callback() {
                        override fun onPlay() {
                            sync(true)
                        }

                        override fun onPause() {
                            sync(false)
                        }

                        override fun onSkipToNext() {
                            youTubePlayersManager.castNextPlay()
                        }

                        override fun onSkipToPrevious() {
                            youTubePlayersManager.castPrevPlay()
                        }
                    },
                )
            }
    }

    private fun updatePlaybackState(position: Long) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    position,
                    playbackCurrentTime.toFloat(),
                ).build(),
        )
    }

    private fun updatePlaybackSpeed(speed: Float) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    playbackCurrentTime,
                    speed,
                ).build(),
        )
    }

    private fun sync(isPlaying: Boolean) {
        val position = 0L // UI용이면 일단 0으로 둬도 됨
        val speed = 1f
        currentState = PlaybackStateCompat.STATE_PLAYING

        if (isPlaying) {
            currentState = PlaybackStateCompat.STATE_PLAYING
        } else {
            currentState = PlaybackStateCompat.STATE_PAUSED
        }
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setActions(actions)
                .setState(
                    currentState,
                    position,
                    speed,
                ).build(),
        )

        youTubePlayersManager.castPlay(isPlaying)
    }

    fun updateDuration(duration: Long) {
        mediaSession.setMetadata(
            MediaMetadataCompat
                .Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build(),
        )
    }
}
