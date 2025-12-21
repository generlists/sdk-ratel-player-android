package com.sean.ratel.player.core.com.sean.ratel.player.core.data.domain.model

enum class PlaySpeed(var speed: Float) {
    PlaySpeed_0_5(0.5f),
    PlaySpeed_0_75(0.75f),
    PlaySpeed_1_0(1.0f),
    PlaySpeed_1_25(1.25f),
    PlaySpeed_1_5(1.5f),
    PlaySpeed_1_75(1.75f),
    PlaySpeed_2_0(2.0f);

    companion object{
        fun convertToValueToEnum(speed:Float):PlaySpeed{
            when(speed){
                0.5f -> return PlaySpeed_0_5
                0.75f-> return PlaySpeed_0_75
                1.0f-> return PlaySpeed_1_0
                1.25f-> return PlaySpeed_1_25
                1.5f-> return PlaySpeed_1_5
                1.75f-> return PlaySpeed_1_75
                2.0f-> return PlaySpeed_2_0
            }

            return PlaySpeed_1_0
        }
    }

}