# FloatMsg

Android 浮屏消息队列组件。用于在页面上动态展示一条或多条浮屏消息，支持排队显示、高优先级插队、多 channel 并行显示，以及自定义入场/出场动画。

## 功能

- 按队列顺序显示浮屏消息
- 支持高优先级消息插队
- 支持多个 channel 同时显示，互不阻塞
- 支持绑定 `Lifecycle`，页面销毁时自动释放
- 内置多种左右飞入、居中停留、飞出或淡出的动画
- 支持自定义 `FloatMessageAnimator`

## 接入

### JitPack 依赖

项目根目录 `settings.gradle.kts` 添加 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

App 模块添加依赖：

```kotlin
dependencies {
    implementation("com.github.gegeName:float-msg:0.0.4")
}
```

把 `<version>` 替换成 GitHub/JitPack 上发布的 tag，例如：

```kotlin
implementation("com.github.gegeName:float-msg:0.0.4")
```

### 本地模块依赖

如果是在当前工程内直接使用：

```kotlin
dependencies {
    implementation(project(":float-msg"))
}
```

## 基础用法

布局里准备一个用于承载浮屏消息的容器，通常用 `FrameLayout` 覆盖在页面最上层：

```xml
<FrameLayout
    android:id="@+id/floatContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipChildren="false"
    android:clipToPadding="false" />
```

创建队列并发送消息：

```kotlin
val queue = FloatMessageQueue<String>(
    container = findViewById(R.id.floatContainer),
    onCreateView = { text ->
        TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(32, 20, 32, 20)
            background = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(Color.parseColor("#2563EB"))
            }
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = 120
            }
        }
    },
    animator = SlideInLeftAnimator(),
    holdDurationMs = 1000L,
    lifecycle = lifecycle
)

queue.enqueue("这是一条浮屏消息")
```

## FloatMessageQueue 参数

```kotlin
class FloatMessageQueue<T>(
    private val container: ViewGroup,
    private val onCreateView: (T) -> View,
    private val animator: FloatMessageAnimator = SlideInLeftAnimator(),
    private val holdDurationMs: Long = 1000L,
    lifecycle: Lifecycle? = null
)
```

参数说明：

- `container`：浮屏消息要添加到的父容器。
- `onCreateView`：根据业务数据创建具体的消息 View。
- `animator`：入场/出场动画，不传时默认使用 `SlideInLeftAnimator()`。
- `holdDurationMs`：入场动画结束后停留多久，单位毫秒。
- `lifecycle`：可选，传入后会在页面销毁时自动 `release()`。

## 发送消息

普通入队：

```kotlin
queue.enqueue("普通消息")
```

高优先级入队：

```kotlin
queue.enqueueHighPriority("高优先级消息")
```

指定 channel：

```kotlin
queue.enqueue(
    item = "顶部消息",
    channel = "top"
)

queue.enqueue(
    item = "底部消息",
    channel = "bottom"
)
```

同一个 channel 内按顺序显示，不同 channel 互不阻塞，可以同时显示。

手动释放：

```kotlin
queue.release()
```

如果创建队列时传入了 `lifecycle`，通常不需要手动调用。

## 内置动画

### SlideInLeftAnimator

从屏幕左侧飞入，停留在 View 的布局位置，再飞回左侧。

```kotlin
animator = SlideInLeftAnimator()
```

### SlideInRightAnimator

从屏幕右侧飞入，停留在 View 的布局位置，再飞回右侧。

```kotlin
animator = SlideInRightAnimator()
```

### SlideInLeftToCenterOutRightAnimator

从屏幕左侧飞入，停留在屏幕中间，再飞回右侧。

```kotlin
animator = SlideInLeftToCenterOutRightAnimator()
```

### SlideInRightToCenterOutLeftAnimator

从屏幕右侧飞入，停留在屏幕中间，再飞回左侧。

```kotlin
animator = SlideInRightToCenterOutLeftAnimator()
```

### SlideInLeftToCenterFadeOutAnimator

从屏幕左侧飞入，停留在屏幕中间，再在中间慢慢淡出。

```kotlin
animator = SlideInLeftToCenterFadeOutAnimator()
```

### SlideInRightToCenterFadeOutAnimator

从屏幕右侧飞入，停留在屏幕中间，再在中间慢慢淡出。

```kotlin
animator = SlideInRightToCenterFadeOutAnimator()
```

## 动画时长

内置动画都支持传入入场和出场时长：

```kotlin
SlideInLeftToCenterFadeOutAnimator(
    durationIn = 500L,
    durationOut = 1200L
)
```

`holdDurationMs` 控制的是入场完成后的停留时间，和动画时长分开配置：

```kotlin
FloatMessageQueue(
    container = floatContainer,
    onCreateView = ::createMessageView,
    animator = SlideInRightToCenterFadeOutAnimator(durationOut = 1200L),
    holdDurationMs = 1000L,
    lifecycle = lifecycle
)
```

## 自定义动画

实现 `FloatMessageAnimator` 即可：

```kotlin
class FadeAnimator(
    private val duration: Long = 500L
) : FloatMessageAnimator {

    override fun animateIn(view: View, onEnd: () -> Unit) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction(onEnd)
            .start()
    }

    override fun animateOut(view: View, onEnd: () -> Unit) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(onEnd)
            .start()
    }

    override fun cancel(view: View) {
        view.animate().cancel()
    }
}
```

使用：

```kotlin
FloatMessageQueue(
    container = floatContainer,
    onCreateView = ::createMessageView,
    animator = FadeAnimator(),
    lifecycle = lifecycle
)
```

## 示例 App

当前仓库的 `app` 模块包含完整示例：

- 所有内置动画示例
- 连续入队示例
- 高优先级插队示例
- 多 channel 同时显示示例

入口文件：

```text
app/src/main/java/com/example/float_msg/MainActivity.kt
```

## JitPack 发布

当前仓库已配置 `jitpack.yml`：

```yaml
jdk:
  - openjdk17
install:
  - ./gradlew :float-msg:publishToMavenLocal -PVERSION_NAME=$VERSION
```

`float-msg/build.gradle.kts` 已应用：

```kotlin
apply(from = "jitpack.gradle")
```

发布步骤：

```bash
git add .
git commit -m "release float-msg"
git tag 0.1.0
git push origin main
git push origin 0.1.0
```

然后在 JitPack 使用：

```kotlin
implementation("com.github.gegeName:float-msg:0.1.0")
```

## 环境要求

- minSdk：21
- Java：11
- JitPack 构建 JDK：17

## 许可证

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源。Apache 2.0 自带"按现状提供、不作担保、不承担责任"的条款，并额外包含专利授权与责任限制条款。

## 免责声明 / Disclaimer

本项目（以下简称"本软件"）是一个通用的媒体选择工具，仅供学习、研究和合法用途使用。

1. 本软件按"现状"提供，作者不对其适用性、可靠性、安全性作任何明示或暗示的担保。
2. 使用者应自行遵守所在国家/地区的法律法规。对于使用者利用本软件从事的任何违法、侵权或其他不当行为，作者不承担由此产生的任何责任。
3. 本软件不针对任何违法用途设计，作者不认可、不支持将其用于任何违反法律法规的用途。
4. 在适用法律允许的最大范围内，作者不对因使用或无法使用本软件而导致的任何直接或间接损失承担责任。
5. 使用本软件即表示使用者已知悉并接受以上条款。