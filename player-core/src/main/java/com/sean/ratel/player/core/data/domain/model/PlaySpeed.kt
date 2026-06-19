package com.sean.ratel.player.core.data.domain.model

enum class PlaySpeed(
    var speed: Float,
) {
    PlaySpeed0Dot5(0.5f),
    PlaySpeed0Dot75(0.75f),
    PlaySpeed1Dot0(1.0f),
    PlaySpeed1Dot25(1.25f),
    PlaySpeed1Dot5(1.5f),
    PlaySpeed1Dot75(1.75f),
    PlaySpeed2Dot0(2.0f),
    ;

    companion object {
        fun convertToValueToEnum(speed: Float): PlaySpeed {
            when (speed) {
                0.5f -> return PlaySpeed0Dot5
                0.75f -> return PlaySpeed0Dot75
                1.0f -> return PlaySpeed1Dot0
                1.25f -> return PlaySpeed1Dot25
                1.5f -> return PlaySpeed1Dot5
                1.75f -> return PlaySpeed1Dot75
                2.0f -> return PlaySpeed2Dot0
            }

            return PlaySpeed1Dot0
        }
    }
}
