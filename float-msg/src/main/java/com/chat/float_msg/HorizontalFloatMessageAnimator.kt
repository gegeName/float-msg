package com.chat.float_msg

import android.view.View

/**
 * 水平方向边缘。
 */
internal enum class HorizontalEdge {
    Left,
    Right
}

/**
 * 入场完成后的停留位置。
 */
internal enum class HorizontalRestPosition {
    Natural,
    ScreenCenter
}

internal enum class HorizontalExitStyle {
    Edge,
    Fade
}

/**
 * 通用水平浮屏动画。
 *
 * @param enterFrom 入场方向,从屏幕左侧或右侧飞入
 * @param restPosition 入场完成后的停留位置,可停在布局原位置或屏幕水平中间
 * @param exitTo 出场方向,飞回屏幕左侧或右侧
 * @param durationIn 入场动画时长,单位毫秒
 * @param durationOut 出场动画时长,单位毫秒
 * @param exitStyle 出场样式,可飞出边缘或原地淡出
 */
internal class HorizontalFloatMessageAnimator(
    private val enterFrom: HorizontalEdge,
    private val restPosition: HorizontalRestPosition,
    private val exitTo: HorizontalEdge,
    private val durationIn: Long,
    private val durationOut: Long,
    private val exitStyle: HorizontalExitStyle = HorizontalExitStyle.Edge
) : FloatMessageAnimator {

    override fun animateIn(view: View, onEnd: () -> Unit) {
        view.alpha = 0f
        view.translationX = 0f
        view.post {
            view.translationX = when (enterFrom) {
                HorizontalEdge.Left -> leftOutsideTranslation(view)
                HorizontalEdge.Right -> rightOutsideTranslation(view)
            }
            view.alpha = 1f
            view.animate()
                .translationX(restTranslation(view))
                .setDuration(durationIn)
                .withEndAction(onEnd)
                .start()
        }
    }

    override fun animateOut(view: View, onEnd: () -> Unit) {
        if (exitStyle == HorizontalExitStyle.Fade) {
            view.animate()
                .translationX(restTranslation(view))
                .alpha(0f)
                .setDuration(durationOut)
                .withEndAction(onEnd)
                .start()
            return
        }

        val targetTranslation = when (exitTo) {
            HorizontalEdge.Left -> leftOutsideTranslation(view)
            HorizontalEdge.Right -> rightOutsideTranslation(view)
        }
        view.animate()
            .translationX(targetTranslation)
            .setDuration(durationOut)
            .withEndAction(onEnd)
            .start()
    }

    override fun cancel(view: View) {
        view.animate().cancel()
    }

    private fun restTranslation(view: View): Float {
        return when (restPosition) {
            HorizontalRestPosition.Natural -> 0f
            HorizontalRestPosition.ScreenCenter -> {
                val naturalLeft = naturalLeftOnScreen(view)
                val centeredLeft = (screenWidth(view) - view.width) / 2f
                centeredLeft - naturalLeft
            }
        }
    }

    private fun leftOutsideTranslation(view: View): Float {
        return -(naturalLeftOnScreen(view) + view.width).coerceAtLeast(1f)
    }

    private fun rightOutsideTranslation(view: View): Float {
        val naturalLeft = naturalLeftOnScreen(view)
        return (screenWidth(view) - naturalLeft).coerceAtLeast(1f)
    }

    private fun naturalLeftOnScreen(view: View): Float {
        val coords = IntArray(2)
        view.getLocationOnScreen(coords)
        return coords[0] - view.translationX
    }

    private fun screenWidth(view: View): Float {
        return view.resources.displayMetrics.widthPixels.toFloat()
    }
}
