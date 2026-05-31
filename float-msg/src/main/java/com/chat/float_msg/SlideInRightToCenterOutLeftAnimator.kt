package com.chat.float_msg

import android.view.View

/**
 * 从屏幕右侧飞入,停留在屏幕中间,再飞回左侧。
 */
class SlideInRightToCenterOutLeftAnimator(
    durationIn: Long = 500L,
    durationOut: Long = 500L
) : FloatMessageAnimator {

    private val delegate = HorizontalFloatMessageAnimator(
        enterFrom = HorizontalEdge.Right,
        restPosition = HorizontalRestPosition.ScreenCenter,
        exitTo = HorizontalEdge.Left,
        durationIn = durationIn,
        durationOut = durationOut
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
