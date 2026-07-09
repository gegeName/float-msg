# FloatMsg 对外 API 保留规则。
# 这些规则会随 AAR 传递给使用方,避免开启 R8/ProGuard 后公开调用入口被过度混淆。
# 内部实现类不在这里 keep,仍允许 R8 正常压缩和优化。

-keepattributes Signature,InnerClasses,EnclosingMethod

-keep class com.chat.float_msg.FloatMessageAnimator {
    public *;
}

-keep class com.chat.float_msg.FloatMessageQueue {
    public *;
}

-keep class com.chat.float_msg.SlideInLeftAnimator {
    public *;
}

-keep class com.chat.float_msg.SlideInRightAnimator {
    public *;
}

-keep class com.chat.float_msg.SlideInLeftToCenterOutRightAnimator {
    public *;
}

-keep class com.chat.float_msg.SlideInRightToCenterOutLeftAnimator {
    public *;
}

-keep class com.chat.float_msg.SlideInLeftToCenterFadeOutAnimator {
    public *;
}

-keep class com.chat.float_msg.SlideInRightToCenterFadeOutAnimator {
    public *;
}
