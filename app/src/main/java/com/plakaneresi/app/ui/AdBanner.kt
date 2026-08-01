package com.plakaneresi.app.ui

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.plakaneresi.app.R

/**
 * Fixed 320x50 banner pinned above the footer.
 *
 * The adaptive sizes were all tried first and each has a catch: every normal-height
 * anchored adaptive call (`getCurrentOrientation…`, `getPortrait…`, `getLandscape…`) is
 * now deprecated, and the only supported replacement,
 * `getLargeAnchoredAdaptiveBannerAdSize`, reserves roughly twice the height — so any
 * creative smaller than the slot gets letterboxed in black by the ad's own WebView.
 *
 * On a screen whose entire job is showing one short answer, that is a bad trade, and
 * [AdSize.BANNER] is a constant that cannot be deprecated out from under us. Switching to
 * the large adaptive size later is a one-line change if the revenue is worth the space.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val adUnitId = stringResource(R.string.admob_banner_unit_id)

    AndroidView(
        // AdSize.BANNER is 320x50dp. Reserving that height up front matters: an AdView
        // measures zero until an ad actually arrives a second or two after launch, and
        // without this the whole list jumps as it appears.
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                // 320dp is narrower than most phones, so the slot has gaps at the sides.
                // Without this they render as black bars instead of the app background.
                setBackgroundColor(Color.TRANSPARENT)
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        // Without this the AdView leaks its polling timers when the screen goes away.
        onRelease = AdView::destroy,
    )
}
