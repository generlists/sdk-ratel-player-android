package com.sean.ratel.player.core.data.domain.model

enum class MediaStreamTransitionReason(val reason: Int) {
    MEDIA_ITEM_TRANSITION_REASON_REPEAT(0),
    MEDIA_ITEM_TRANSITION_REASON_AUTO(1),
    MEDIA_ITEM_TRANSITION_REASON_SEEK(2),
    MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED(3);

    companion object{
        fun convertToValueToEnum(reason:Int):MediaStreamTransitionReason{
            when(reason){
                0 -> return MEDIA_ITEM_TRANSITION_REASON_REPEAT
                1-> return MEDIA_ITEM_TRANSITION_REASON_AUTO
                2-> return MEDIA_ITEM_TRANSITION_REASON_SEEK
                3-> return MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
            }

            return MEDIA_ITEM_TRANSITION_REASON_AUTO
        }
    }
}