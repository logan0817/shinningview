package com.logan.shinningview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView

/**
 * ShimmerTextView (流光文字视图)
 *
 * 这是一个继承自 [AppCompatTextView] 的自定义 View，通过操作 Paint 的 Shader 实现文字扫光效果。
 *
 * ### 核心逻辑：
 * 1. **优先级策略**：优先使用 XML 中配置的 `svColors` 和 `svPositions` 定义复杂的渐变色。
 * 2. **智能回退**：如果用户未设置颜色，代码会自动提取当前的 `currentTextColor`，
 * 构建一个 "底色 -> 白色高亮 -> 底色" 的默认渐变，确保流光始终与文字颜色融合。
 * 3. **高性能动画**：利用 [Matrix] 平移 Shader，避免在 `onDraw` 中重复创建对象，大幅降低内存抖动。
 */
class ShimmerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // ============================================================================================
    // 成员变量
    // ============================================================================================

    /** 流光带的宽度 (像素)。若为 -1 则会在初始化时计算为 View 宽度的 60% */
    private var mWidth = -1

    /** 流光的倾斜斜率 (dy/dx)。默认为 0.2 (轻微向右下倾斜) */
    private var mSlope: Float = -1F

    /** 动画模式：0 = 自动播放 (Auto), 1 = 手动控制 (Manual) */
    private var mAnimMode = 0

    /** 动画单次时长 (毫秒) */
    private var mDuration = 2000L

    /** 动画重复次数，-1 代表无限循环 */
    private var mRepeatCount = -1

    // --------------------------------------------------------------------------------------------
    // 颜色配置 (核心数据源)
    // --------------------------------------------------------------------------------------------

    /** 自定义颜色数组 (ARGB)，解析自 `svColors` 属性 */
    private var mColors: IntArray? = null

    /** 自定义位置数组 (0.0~1.0)，解析自 `svPositions` 属性 */
    private var mPositions: FloatArray? = null

    // --------------------------------------------------------------------------------------------
    // 绘图与动画工具
    // --------------------------------------------------------------------------------------------

    /** 线性渐变渲染器。核心原理是将此 Shader 设置给 TextView 的 Paint */
    private var mLinearGradient: LinearGradient? = null

    /** * 变换矩阵。
     * 动画的核心：我们不修改 Gradient 对象本身，而是通过矩阵平移(Translate)它。
     */
    private val mShaderMatrix = Matrix()

    /** 属性动画执行器 */
    private var mValueAnimator: ValueAnimator? = null

    /** * 脏位标记 (Dirty Flag)。
     * 当颜色、宽度或斜率发生变化时置为 true，提示下一次 onDraw 时重建 Shader。
     */
    private var isShaderDirty = true

    init {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerTextView).apply {
                try {
                    // 读取通用属性 (统一使用 sv 前缀)
                    mWidth = getDimensionPixelSize(R.styleable.ShimmerTextView_svWidth, mWidth)
                    mSlope = getFloat(R.styleable.ShimmerTextView_svSlope, mSlope)
                    mAnimMode = getInt(R.styleable.ShimmerTextView_svAnimMode, mAnimMode)
                    mDuration = getInt(R.styleable.ShimmerTextView_svDuration, mDuration.toInt()).toLong()
                    mRepeatCount = getInt(R.styleable.ShimmerTextView_svRepeat, mRepeatCount)

                    // 解析复杂的颜色字符串
                    val colorsStr = getString(R.styleable.ShimmerTextView_svColors)
                    val positionsStr = getString(R.styleable.ShimmerTextView_svPositions)
                    parseColors(colorsStr, positionsStr)

                } finally {
                    recycle()
                }
            }
        }
    }

    /**
     * 解析 XML 传入的逗号分隔字符串
     * @param colorsStr 例如 "#FF0000,#FFFFFF,#FF0000"
     * @param positionsStr 例如 "0,0.5,1"
     */
    private fun parseColors(colorsStr: String?, positionsStr: String?) {
        if (!colorsStr.isNullOrBlank() && !positionsStr.isNullOrBlank()) {
            try {
                // 过滤空字符串，防止 XML 中多余的逗号导致崩溃
                val colorArr = colorsStr.split(",".toRegex()).filter { it.isNotEmpty() }
                val positionArr = positionsStr.split(",".toRegex()).filter { it.isNotEmpty() }

                if (colorArr.size == positionArr.size) {
                    val size = colorArr.size
                    mColors = IntArray(size) { Color.parseColor(colorArr[it]) }
                    mPositions = FloatArray(size) { positionArr[it].toFloat() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================================================================================
    // 生命周期与初始化
    // ============================================================================================

    /**
     * 当 View 大小确定时调用。
     * 这是初始化参数和启动自动动画的最佳时机。
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0) return

        initSetup(w, h)
        isShaderDirty = true // 标记需要生成 Shader

        // 如果配置为自动模式，则立即开始动画
        if (mAnimMode == 0) {
            startLightingAnimation()
        }
    }

    /**
     * 初始化默认参数
     */
    private fun initSetup(width: Int, height: Int) {
        if (mRepeatCount < 0) mRepeatCount = -1 // ValueAnimator.INFINITE

        // 如果未设置宽度，默认光束宽度为 View 宽度的 60%
        if (mWidth < 0) mWidth = (width * 0.6f).toInt()

        // 如果未设置斜率，默认为 0.2 (稍微倾斜效果更好)
        if (mSlope < 0) mSlope = 0.2f
    }

    override fun onDraw(canvas: Canvas) {
        // 在绘制前检查是否需要更新 Shader (懒加载机制)
        if (isShaderDirty || mLinearGradient == null) {
            updateShader()
        }

        // 调用父类方法，TextView 会使用携带了 Shader 的 Paint 绘制出流光文字
        super.onDraw(canvas)
    }

    /**
     * 核心逻辑：创建或更新 Gradient Shader
     * * 如果 [mColors] 存在，使用自定义颜色。
     * 否则，动态获取当前的文字颜色构建默认的流光效果。
     */
    private fun updateShader() {
        if (width == 0) return

        val shimmerWidth = mWidth.toFloat()

        val colors: IntArray
        val positions: FloatArray?

        if (mColors != null && mPositions != null) {
            // Case A: 用户在 XML 或代码中明确设置了 svColors
            colors = mColors!!
            positions = mPositions
        } else {
            // Case B: 未设置颜色，生成智能默认方案
            // 获取当前 TextView 的文字颜色 (考虑了 textColor 属性)
            val baseColor = currentTextColor
            val highlightColor = Color.WHITE // 默认高亮色为白色

            // 构建三段式渐变：底色 -> 高亮 -> 底色
            colors = intArrayOf(baseColor, highlightColor, highlightColor, baseColor)
            // 调整位置，让高亮部分集中在中间，边缘柔和
            positions = floatArrayOf(0f, 0.45f, 0.55f, 1f)
        }

        // 计算 Gradient 的坐标系统
        // x0, y0 -> x1, y1 定义了静态 Shader 的方向和长度
        // 为了支持斜率 (Slope)，Y 轴终点根据 X 轴长度 * 斜率计算得出
        val x0 = 0f
        val y0 = 0f
        val x1 = shimmerWidth
        val y1 = shimmerWidth * mSlope

        mLinearGradient = LinearGradient(
            x0, y0, x1, y1,
            colors,
            positions,
            Shader.TileMode.CLAMP // 边缘拉伸模式，配合 Matrix 移动使用
        )

        // 【关键步骤】将 Shader 应用到 TextView 的 Paint
        paint.shader = mLinearGradient
        isShaderDirty = false
    }

    // ============================================================================================
    // 动画控制
    // ============================================================================================

    /**
     * 执行流光动画
     * 原理：通过 ValueAnimator 改变 Matrix 的平移量，从而移动 Shader
     */
    private fun showAnimation(width: Int, repeatCount: Int, duration: Long) {
        if (mValueAnimator != null && mValueAnimator!!.isRunning) return

        val offset = mWidth.toFloat()

        // 计算动画移动范围
        // 从屏幕左侧外 (-2*offset) 移动到 屏幕右侧外 (width + 2*offset)
        val startVal = -offset * 2
        val endVal = width + offset * 2

        mValueAnimator?.cancel()
        mValueAnimator = ValueAnimator.ofFloat(startVal, endVal).apply {
            this.repeatCount = repeatCount
            this.duration = duration
            interpolator = LinearInterpolator() // 线性插值，保证匀速移动

            addUpdateListener { animation ->
                val value = animation.animatedValue as Float

                if (mLinearGradient != null) {
                    mShaderMatrix.reset()

                    // 【数学逻辑】
                    // X 轴移动 value
                    // Y 轴移动 value * slope
                    // 必须同时移动 X 和 Y，才能保证光束沿着斜率方向“滑”过文字
                    mShaderMatrix.setTranslate(value, value * mSlope)

                    // 应用矩阵到 Shader
                    mLinearGradient?.setLocalMatrix(mShaderMatrix)

                    // 触发重绘
                    invalidate()
                }
            }
            start()
        }
    }

    /**
     * View 从窗口分离时（如页面销毁），停止动画以防止内存泄漏
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    /**
     * View 附加到窗口时，如果是自动模式，则恢复动画
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mAnimMode == 0 && (mValueAnimator == null || !mValueAnimator!!.isRunning)) {
            // 使用 width > 0 判断 View 是否已测量完毕
            if (width > 0) startLightingAnimation()
        }
    }

    // ============================================================================================
    // 公开 API (供外部 Java/Kotlin 调用)
    // ============================================================================================

    /**
     * 设置自定义颜色和位置数组
     * 数组长度必须一致
     */
    fun setColorAndPositions(colors: IntArray, positions: FloatArray) {
        if (colors.size != positions.size) {
            throw RuntimeException("colors & positions size must match")
        }
        this.mColors = colors
        this.mPositions = positions
        isShaderDirty = true // 标记脏位，下次绘制时生效
        invalidate()
    }

    /**
     * 设置流光斜率
     * @param slope 建议范围 0.2 ~ 0.5
     */
    fun setSlope(slope: Float) {
        this.mSlope = slope
        isShaderDirty = true
        invalidate()
    }

    /**
     * 设置流光束的宽度 (像素)
     * 注意：这不是 TextView 的宽度，而是那道光的宽度
     */
    fun setShimmerWidth(width: Int) {
        this.mWidth = width
        isShaderDirty = true
        invalidate()
    }

    /**
     * 手动启动动画
     */
    fun startLightingAnimation(duration: Long = mDuration, repeatCount: Int = mRepeatCount) {
        if (width == 0) return // View 尚未准备好
        showAnimation(width, repeatCount, duration)
    }

    /**
     * 手动停止动画
     */
    fun stopAnimation() {
        mValueAnimator?.cancel()
    }
}