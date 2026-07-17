package com.example.float_msg

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chat.float_msg.FloatMessageAnimator
import com.chat.float_msg.FloatMessageQueue
import com.chat.float_msg.SlideInLeftAnimator
import com.chat.float_msg.SlideInLeftToCenterFadeOutAnimator
import com.chat.float_msg.SlideInLeftToCenterOutRightAnimator
import com.chat.float_msg.SlideInRightAnimator
import com.chat.float_msg.SlideInRightToCenterFadeOutAnimator
import com.chat.float_msg.SlideInRightToCenterOutLeftAnimator

class MainActivity : AppCompatActivity() {

    private lateinit var slideInLeftQueue: FloatMessageQueue<DemoMessage>
    private lateinit var slideInRightQueue: FloatMessageQueue<DemoMessage>
    private lateinit var leftCenterOutRightQueue: FloatMessageQueue<DemoMessage>
    private lateinit var rightCenterOutLeftQueue: FloatMessageQueue<DemoMessage>
    private lateinit var leftCenterFadeQueue: FloatMessageQueue<DemoMessage>
    private lateinit var rightCenterFadeQueue: FloatMessageQueue<DemoMessage>
    private lateinit var channelQueue: FloatMessageQueue<DemoMessage>
    private lateinit var controlQueue: FloatMessageQueue<DemoMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val floatContainer = findViewById<FrameLayout>(R.id.floatContainer)
        initQueues(floatContainer)
        initButtons()
    }

    private fun initQueues(floatContainer: FrameLayout) {
        slideInLeftQueue = createQueue(
            container = floatContainer,
            animator = SlideInLeftAnimator(),
            holdDurationMs = 900L
        )
        slideInRightQueue = createQueue(
            container = floatContainer,
            animator = SlideInRightAnimator(),
            holdDurationMs = 900L
        )
        leftCenterOutRightQueue = createQueue(
            container = floatContainer,
            animator = SlideInLeftToCenterOutRightAnimator(),
            holdDurationMs = 900L
        )
        rightCenterOutLeftQueue = createQueue(
            container = floatContainer,
            animator = SlideInRightToCenterOutLeftAnimator(),
            holdDurationMs = 900L
        )
        leftCenterFadeQueue = createQueue(
            container = floatContainer,
            animator = SlideInLeftToCenterFadeOutAnimator(durationOut = 1200L),
            holdDurationMs = 900L
        )
        rightCenterFadeQueue = createQueue(
            container = floatContainer,
            animator = SlideInRightToCenterFadeOutAnimator(durationOut = 1200L),
            holdDurationMs = 900L
        )
        channelQueue = createQueue(
            container = floatContainer,
            animator = SlideInLeftToCenterFadeOutAnimator(durationOut = 1200L),
            holdDurationMs = 1200L
        )
        controlQueue = createQueue(
            container = floatContainer,
            animator = SlideInRightToCenterFadeOutAnimator(durationOut = 1200L),
            holdDurationMs = 4000L
        )
    }

    private fun initButtons() {
        findViewById<View>(R.id.btnSlideInLeft).setOnClickListener {
            slideInLeftQueue.enqueue(
                DemoMessage(
                    text = "SlideInLeftAnimator\n左侧飞入，原位停留，左侧飞出",
                    lane = 0,
                    backgroundColor = "#2563EB"
                )
            )
        }

        findViewById<View>(R.id.btnSlideInRight).setOnClickListener {
            slideInRightQueue.enqueue(
                DemoMessage(
                    text = "SlideInRightAnimator\n右侧飞入，原位停留，右侧飞出",
                    lane = 1,
                    backgroundColor = "#059669"
                )
            )
        }

        findViewById<View>(R.id.btnLeftCenterOutRight).setOnClickListener {
            leftCenterOutRightQueue.enqueue(
                DemoMessage(
                    text = "SlideInLeftToCenterOutRightAnimator\n左侧飞入，中间停留，右侧飞出",
                    lane = 2,
                    backgroundColor = "#7C3AED"
                )
            )
        }

        findViewById<View>(R.id.btnRightCenterOutLeft).setOnClickListener {
            rightCenterOutLeftQueue.enqueue(
                DemoMessage(
                    text = "SlideInRightToCenterOutLeftAnimator\n右侧飞入，中间停留，左侧飞出",
                    lane = 3,
                    backgroundColor = "#DB2777"
                )
            )
        }

        findViewById<View>(R.id.btnLeftCenterFade).setOnClickListener {
            leftCenterFadeQueue.enqueue(
                DemoMessage(
                    text = "SlideInLeftToCenterFadeOutAnimator\n左侧飞入，中间停留，中间淡出",
                    lane = 4,
                    backgroundColor = "#EA580C"
                )
            )
        }

        findViewById<View>(R.id.btnRightCenterFade).setOnClickListener {
            rightCenterFadeQueue.enqueue(
                DemoMessage(
                    text = "SlideInRightToCenterFadeOutAnimator\n右侧飞入，中间停留，中间淡出",
                    lane = 5,
                    backgroundColor = "#0F766E"
                )
            )
        }

        findViewById<View>(R.id.btnQueue).setOnClickListener {
            repeat(3) { index ->
                leftCenterFadeQueue.enqueue(
                    DemoMessage(
                        text = "普通队列示例 ${index + 1}/3\n同一个 channel 会按顺序显示",
                        lane = 4,
                        backgroundColor = "#EA580C"
                    )
                )
            }
        }

        findViewById<View>(R.id.btnHighPriority).setOnClickListener {
            leftCenterFadeQueue.enqueue(
                DemoMessage(
                    text = "普通消息 1\n先入队，正在显示",
                    lane = 4,
                    backgroundColor = "#64748B"
                )
            )
            leftCenterFadeQueue.enqueue(
                DemoMessage(
                    text = "普通消息 2\n会排在高优先级后面",
                    lane = 4,
                    backgroundColor = "#64748B"
                )
            )
            leftCenterFadeQueue.enqueueHighPriority(
                DemoMessage(
                    text = "高优先级消息\n当前消息结束后优先显示",
                    lane = 4,
                    backgroundColor = "#DC2626"
                )
            )
        }

        findViewById<View>(R.id.btnMultiChannel).setOnClickListener {
            channelQueue.enqueue(
                item = DemoMessage(
                    text = "channel = top\n和 bottom 不互相阻塞",
                    lane = 2,
                    backgroundColor = "#4338CA"
                ),
                channel = "top"
            )
            channelQueue.enqueue(
                item = DemoMessage(
                    text = "channel = bottom\n可以同时显示",
                    lane = 5,
                    backgroundColor = "#047857"
                ),
                channel = "bottom"
            )
        }

        findViewById<View>(R.id.btnCancelCurrent).setOnClickListener { button ->
            controlQueue.enqueue(
                DemoMessage(
                    text = "cancelCurrent 示例\n这条会在 0.8 秒后被取消",
                    lane = 1,
                    backgroundColor = "#B45309"
                )
            )
            controlQueue.enqueue(
                DemoMessage(
                    text = "取消当前后\n队列继续执行下一条",
                    lane = 1,
                    backgroundColor = "#2563EB"
                )
            )
            button.postDelayed({
                controlQueue.cancelCurrent()
            }, 800L)
        }

        findViewById<View>(R.id.btnClearQueue).setOnClickListener { button ->
            repeat(5) { index ->
                controlQueue.enqueue(
                    DemoMessage(
                        text = "clearQueue 示例 ${index + 1}/5\n0.8 秒后清空等待队列",
                        lane = 1,
                        backgroundColor = "#7C2D12"
                    )
                )
            }
            button.postDelayed({
                controlQueue.clearQueue()
            }, 800L)
        }
    }

    private fun createQueue(
        container: FrameLayout,
        animator: FloatMessageAnimator,
        holdDurationMs: Long
    ): FloatMessageQueue<DemoMessage> {
        return FloatMessageQueue(
            container = container,
            onCreateView = ::createMessageView,
            animator = animator,
            holdDurationMs = holdDurationMs,
            lifecycle = lifecycle
        )
    }

    private fun createMessageView(message: DemoMessage): View {
        return TextView(this).apply {
            text = message.text
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxWidth = dp(280)
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor(message.backgroundColor))
            }
            elevation = dp(8).toFloat()
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = dp(96 + message.lane * 58)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private data class DemoMessage(
        val text: String,
        val lane: Int,
        val backgroundColor: String
    )
}
