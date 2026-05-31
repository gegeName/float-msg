package com.chat.float_msg

import android.view.View

/**
 * 从屏幕左侧飞入,停留在屏幕中间,再在中间慢慢淡出。
 */
class SlideInLeftToCenterFadeOutAnimator(
    durationIn: Long = 500L,
    durationOut: Long = 1000L
) : FloatMessageAnimator {

    private val delegate = HorizontalFloatMessageAnimator(
        enterFrom = HorizontalEdge.Left,
        restPosition = HorizontalRestPosition.ScreenCenter,
        exitTo = HorizontalEdge.Left,
        durationIn = durationIn,
        durationOut = durationOut,
        exitStyle = HorizontalExitStyle.Fade
    )

    override fun animateIn(view: View, onEnd: () -> Unit) {
        delegate.animateIn(view, onEnd)
    }

    override fun animateOut(view: View, onEnd: () -> Unit) {
        delegate.animateOut(view, onEnd)
    }

    override fun cancel(view: View) {
        delegate.cancel(view)
    }
}
