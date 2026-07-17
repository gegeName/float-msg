package com.chat.float_msg

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.ArrayDeque

/**
 * 通用浮屏消息队列。
 *
 * 同一个 channel 内按顺序执行: addView -> animateIn -> 停留 -> animateOut -> removeView,
 * 当前消息执行完后再执行下一条。高优先级消息会插入对应 channel 的队头,等待当前正在执行的
 * 消息结束后立刻执行。不同 channel 互不阻塞,允许多个浮屏同时出现。
 *
 * @param T 消息载荷类型
 * @param container 已经在父布局中定位好的空容器,例如 FrameLayout
 * @param onCreateView 根据 T 创建并绑定一个 View,每次入场都会调用
 * @param animator 入/出场动画,不传则用 [SlideInLeftAnimator]
 * @param holdDurationMs 完整入场后停留毫秒数,默认 1000ms
 * @param lifecycle 可选,传入则自动绑定生命周期 ON_DESTROY 释放
 */
class FloatMessageQueue<T>(
    private val container: ViewGroup,
    private val onCreateView: (T) -> View,
    private val animator: FloatMessageAnimator = SlideInLeftAnimator(),
    private val holdDurationMs: Long = 1000L,
    lifecycle: Lifecycle? = null
) {
    companion object {
        const val DEFAULT_CHANNEL = "default"
        private const val TAG = "FloatMessageQueue"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val channels = LinkedHashMap<String, ChannelState<T>>()
    private var released = false

    private val lifecycleObserver: DefaultLifecycleObserver? = lifecycle?.let { lc ->
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                release()
                owner.lifecycle.removeObserver(this)
            }
        }.also { lc.addObserver(it) }
    }

    fun enqueue(
        item: T,
        highPriority: Boolean = false,
        channel: String = DEFAULT_CHANNEL
    ) {
        runOnMain { enqueueInternal(item, highPriority, channel) }
    }

    fun enqueueHighPriority(
        item: T,
        channel: String = DEFAULT_CHANNEL
    ) {
        enqueue(item = item, highPriority = true, channel = channel)
    }

    /**
     * 取消当前正在显示或正在执行动画的消息。
     *
     * @param channel 传入 channel 时只取消该 channel 当前消息;不传则取消所有 channel 当前消息。
     * 取消后不会释放队列,对应 channel 如果还有等待消息会继续执行下一条。
     */
    fun cancelCurrent(channel: String? = null) {
        runOnMain { cancelCurrentInternal(channel) }
    }

    /**
     * 清除等待队列。
     *
     * @param channel 传入 channel 时只清除该 channel 的等待消息;不传则清除所有 channel 的等待消息。
     * 当前正在显示或正在执行动画的消息不会被取消,如需同时取消可再调用 [cancelCurrent]。
     */
    fun clearQueue(channel: String? = null) {
        runOnMain { clearQueueInternal(channel) }
    }

    fun release() {
        runOnMain { releaseInternal() }
    }

    private fun enqueueInternal(item: T, highPriority: Boolean, channel: String) {
        if (released) return
        val state = channels.getOrPut(channel) { ChannelState() }
        if (highPriority) {
            state.queue.addFirst(item)
        } else {
            state.queue.addLast(item)
        }
        if (!state.isShowing) {
            state.isShowing = true
            consumeNext(channel, state)
        }
    }

    private fun releaseInternal() {
        if (released) return
        released = true
        channels.values.forEach { state ->
            state.queue.clear()
            state.currentToken++
            state.currentView?.let { view ->
                animator.cancel(view)
                detachView(view)
            }
        }
        channels.clear()
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun cancelCurrentInternal(channel: String?) {
        if (released) return
        if (channel == null) {
            channels.keys.toList().forEach { key ->
                channels[key]?.let { state -> cancelCurrentInternal(key, state) }
            }
        } else {
            channels[channel]?.let { state -> cancelCurrentInternal(channel, state) }
        }
    }

    private fun cancelCurrentInternal(channel: String, state: ChannelState<T>) {
        val view = state.currentView ?: return
        state.currentToken++
        animator.cancel(view)
        detachView(view)
        state.currentView = null

        if (state.queue.isEmpty()) {
            state.isShowing = false
            channels.remove(channel)
        } else {
            state.isShowing = true
            consumeNext(channel, state)
        }
    }

    private fun clearQueueInternal(channel: String?) {
        if (released) return
        if (channel == null) {
            channels.values.forEach { state -> state.queue.clear() }
        } else {
            channels[channel]?.queue?.clear()
        }
    }

    private fun consumeNext(channel: String, state: ChannelState<T>) {
        if (released) return
        while (true) {
            if (isHostDying()) {
                releaseInternal()
                return
            }
            val item = state.queue.pollFirst()
            if (item == null) {
                state.isShowing = false
                state.currentView = null
                channels.remove(channel)
                return
            }
            val view = try {
                onCreateView(item)
            } catch (e: Exception) {
                SLog.e(TAG, e) { "onCreateView failed, skip this item" }
                continue
            }
            val added = try {
                container.addView(view)
                true
            } catch (e: Exception) {
                SLog.e(TAG, e) { "addView failed, skip this item" }
                false
            }
            if (!added) continue
            state.currentView = view
            val currentToken = ++state.currentToken
            val started = try {
                animator.animateIn(view) {
                    if (released || state.currentToken != currentToken || state.currentView !== view) {
                        return@animateIn
                    }
                    mainHandler.postDelayed({
                        if (released || state.currentToken != currentToken || state.currentView !== view) {
                            return@postDelayed
                        }
                        animator.animateOut(view) {
                            if (released || state.currentToken != currentToken || state.currentView !== view) {
                                return@animateOut
                            }
                            detachView(view)
                            if (state.currentView === view) {
                                state.currentView = null
                            }
                            consumeNext(channel, state)
                        }
                    }, holdDurationMs)
                }
                true
            } catch (e: Exception) {
                SLog.e(TAG, e) { "animateIn failed, skip this item" }
                detachView(view)
                if (state.currentView === view) {
                    state.currentView = null
                }
                false
            }
            if (started) return
        }
    }

    private fun detachView(view: View) {
        if (view.parent != null) container.removeView(view)
    }

    private fun isHostDying(): Boolean {
        val ctx = container.context
        return ctx is Activity && (ctx.isFinishing || ctx.isDestroyed)
    }

    private inline fun runOnMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post { block() }
        }
    }

    private class ChannelState<T> {
        val queue = ArrayDeque<T>()
        var isShowing = false
        var currentView: View? = null
        var currentToken = 0
    }
}
