package com.cloudx.demoappjava.dynamic

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.cloudx.demoappjava.R
import io.cloudx.sdk.CloudXAdView
import io.cloudx.sdk.internal.AdViewSize

class AdContainerLayout(context: Context) : FrameLayout(context) {

    private val clRoot: ConstraintLayout
    private val tvPlacement: TextView
    private val flBannerContainer: FrameLayout

    init {
        val root = inflate(context, R.layout.layout_ad_container, this)

        clRoot = root.findViewById(R.id.clRoot)
        tvPlacement = root.findViewById(R.id.tvPlacementName)
        flBannerContainer = root.findViewById(R.id.banner_placeholder)

    }

    fun setPlacement(placementName: String, adViewSize: AdViewSize) {
        tvPlacement.text = placementName

        flBannerContainer.removeAllViews()

        clRoot.setBannerViewSize(R.id.banner_placeholder, adViewSize)
    }

    fun addAdView(adView: CloudXAdView) {
        flBannerContainer.addView(
            adView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun ConstraintLayout.setBannerViewSize(bannerPlaceholderResId: Int, size: AdViewSize) {
        val cl = this
        val cs = ConstraintSet()

        cs.clone(cl)
        with(cs) {
            val koef = resources.displayMetrics?.density ?: 1f

            constrainWidth(bannerPlaceholderResId, (size.w * koef).toInt())
            constrainHeight(bannerPlaceholderResId, (size.h * koef).toInt())
        }
        cs.applyTo(cl)
    }
}