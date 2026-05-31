package com.chat.float_msg

import android.view.View

/**
 * 从屏幕右侧飞入,停留在 View 的布局位置,再飞回右侧。
 */
class SlideInRightAnimator(
    durationIn: Long = 500L,
    durationOut: Long = 500L
) : FloatMessageAnimator {

    private val delegate = HorizontalFloatMessageAnimator(
        enterFrom = HorizontalEdge.Right,
        restPosition = HorizontalRestPosition.Natural,
        exitTo = HorizontalEdge.Right,
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
