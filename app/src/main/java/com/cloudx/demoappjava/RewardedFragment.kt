package com.cloudx.demoappjava

import io.cloudx.sdk.BasePublisherListener
import io.cloudx.sdk.CloudX
import io.cloudx.sdk.CloudXAd
import io.cloudx.sdk.RewardedInterstitialListener
import io.cloudx.sdk.internal.CloudXLogger

class RewardedFragment : FullPageAdFragment() {

    override fun createAd(listener: BasePublisherListener) = CloudX.createRewardedInterstitial(
        requireActivity(),
        placementName,
        object : RewardedInterstitialListener, BasePublisherListener by listener {
            override fun onUserRewarded(cloudXAd: CloudXAd) {
                CloudXLogger.info(
                    logTag,
                    "REWARD; placement: $placementName; network: ${cloudXAd.networkName}"
                )
            }
        }
    )

    override val adType: String = "Rewarded"
    override val logTag: String = "RewardedFragment"
}