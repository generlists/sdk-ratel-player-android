package so.smartlab.video.player.ad.admob.data.repository


interface AdsConfigProvider {

    val admobAppId: String
    val testDeviceHashedId: String
    val bannerUnitId: String
    val adaptiveBannerUnitId: String
    val nativeAdUnitId: String
    val openUnitId: String
    val interstitialUnitId: String

}
