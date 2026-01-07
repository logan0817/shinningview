package com.logan.shinningview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.LinearInterpolator

/**
 * ShimmerView (流光效果视图 / 骨架屏视图)
 *
 * 这是一个自定义 View，通常用作布局的背景或覆盖层，实现类似 Facebook Shimmer 或 Skeleton（骨架屏）的扫光加载效果。
 *
 * ### 核心性能优化：
 * 1. **零内存抖动 (Zero Memory Churn)**：
 * 不使用传统的 `onDraw` 中重建 `LinearGradient` 的方法。
 * 而是创建一个静态的 Shader，利用 [Matrix] 在动画过程中平移这个 Shader。
 * 这使得 GC（垃圾回收）在动画期间几乎为零，保证了低端机上的流畅度。
 *
 * 2. **硬件加速友好的圆角裁剪**：
 * 使用 [ViewOutlineProvider] 替代 `canvas.clipPath`。
 * `clipPath` 在硬件加速下抗锯齿效果差（边缘有锯齿），而 `Outline` 是系统层面的裁剪，边缘平滑且性能更好。
 */
class ShimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ============================================================================================
    // 成员变量
    // ============================================================================================

    /** 流光带的宽度 (px)。若为 -1，则在初始化时默认为 View 宽度的 1/3 */
    private var mWidth = -1

    /** 流光的倾斜斜率 (dy/dx)。默认为 -1 (初始化时计算为对角线斜率) */
    private var mSlope: Float = -1F

    /** 动画模式：0 = 自动播放 (Auto), 1 = 手动控制 (Manual) */
    private var mAnimMode = 0

    /** 动画单次时长 (毫秒) */
    private var mDuration = 2000L

    /** 动画重复次数。-1 代表无限循环 (ValueAnimator.INFINITE) */
    private var mRepeatCount = -1

    // --------------------------------------------------------------------------------------------
    // 颜色配置
    // --------------------------------------------------------------------------------------------

    /** 渐变颜色数组 (ARGB)，对应 XML 属性 svColors */
    private var mColors = intArrayOf(0x00FFFFFF, 0x5AFFFFFF, 0x5AFFFFFF, 0x00FFFFFF)

    /** 颜色分布位置 (0.0 ~ 1.0)，对应 XML 属性 svPositions */
    private var mPositions = floatArrayOf(0f, 0.5f, 0.51f, 1f)

    /** 圆角半径 (px) */
    private var mRadius = 0

    // --------------------------------------------------------------------------------------------
    // 绘图工具
    // --------------------------------------------------------------------------------------------

    /** 画笔：开启抗锯齿标志位 (ANTI_ALIAS_FLAG) */
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 绘制路径：复用对象，避免在 onDraw 中 new。用于界定流光绘制的区域。 */
    private val mPath: Path = Path()

    /** 变换矩阵：这是性能优化的核心工具，用于平移 Shader */
    private val mShaderMatrix = Matrix()

    /** 属性动画执行器 */
    private var mValueAnimator: ValueAnimator? = null

    /**
     * 脏位标记 (Dirty Flag)。
     * 当颜色、宽度或斜率改变时置为 true，提示下一次绘制时重新生成 Shader。
     */
    private var isShaderDirty = true

    /** 缓存的渐变渲染器 */
    private var mLinearGradient: LinearGradient? = null

    // ============================================================================================
    // 初始化块
    // ============================================================================================

    init {
        attrs?.let {
            // 获取自定义属性 (使用 sv 前缀)
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerView).apply {
                try {
                    mWidth = getDimensionPixelSize(R.styleable.ShimmerView_svWidth, mWidth)
                    mSlope = getFloat(R.styleable.ShimmerView_svSlope, mSlope)
                    mRadius = getDimensionPixelSize(R.styleable.ShimmerView_svRadius, mRadius)
                    mAnimMode = getInt(R.styleable.ShimmerView_svAnimMode, mAnimMode)
                    mDuration = getInt(R.styleable.ShimmerView_svDuration, mDuration.toInt()).toLong()
                    mRepeatCount = getInt(R.styleable.ShimmerView_svRepeat, mRepeatCount)

                    // 解析复杂的颜色和位置字符串
                    val colorsStr = getString(R.styleable.ShimmerView_svColors)
                    val positionsStr = getString(R.styleable.ShimmerView_svPositions)
                    parseColors(colorsStr, positionsStr)
                } finally {
                    recycle()
                }
            }
        }

        // 配置圆角裁剪 (使用 ViewOutlineProvider)
        // 这是一个 Android 5.0+ 的特性，它在 RenderThread 中处理裁剪，
        // 相比于 canvas.clipPath，它能提供完美的抗锯齿圆角。
        if (mRadius > 0) {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    // 设置圆角矩形轮廓
                    outline.setRoundRect(0, 0, view.width, view.height, mRadius.toFloat())
                }
            }
            // 开启裁剪到轮廓 (关键步骤)
            clipToOutline = true
        }
    }

    /**
     * 解析 XML 传入的颜色和位置字符串
     * 格式示例: "#FFFFFF,#000000"
     */
    private fun parseColors(colorsStr: String?, positionsStr: String?) {
        if (!colorsStr.isNullOrBlank() && !positionsStr.isNullOrBlank()) {
            try {
                // 使用 filter 过滤掉空字符串，防止 split 产生的尾部空元素导致崩溃
                val colorArr = colorsStr.split(",".toRegex()).filter { it.isNotEmpty() }
                val positionArr = positionsStr.split(",".toRegex()).filter { it.isNotEmpty() }

                if (colorArr.size == positionArr.size) {
                    val size = colorArr.size
                    val newColors = IntArray(size)
                    val newPositions = FloatArray(size)
                    for (i in 0 until size) {
                        newColors[i] = Color.parseColor(colorArr[i])
                        newPositions[i] = positionArr[i].toFloat()
                    }
                    mColors = newColors
                    mPositions = newPositions
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================================================================================
    // 生命周期方法
    // ============================================================================================

    /**
     * 当 View 大小发生改变时调用。
     * 这里是初始化与尺寸相关参数的最佳位置，而不是在 onMeasure 中，因为 onMeasure 可能会被父布局调用多次。
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        initSetup(w, h)

        // 标记 Shader 需要更新
        isShaderDirty = true

        // 如果配置为自动模式，且 View 已经准备好，则开始动画
        if (mAnimMode == 0) {
            startLightingAnimation()
        }
    }

    /**
     * 初始化路径和默认参数
     */
    private fun initSetup(width: Int, height: Int) {
        if (mRepeatCount < 0) mRepeatCount = -1 // 无限循环

        // 如果未设置宽度，默认为视图宽度的 1/3
        if (mWidth < 0) mWidth = width / 3

        // 如果未设置斜率，默认为对角线斜率 (height / width)
        if (mSlope < 0) mSlope = height / width.toFloat()

        // 重置路径
        mPath.reset()
        // 创建一个覆盖整个 View 的矩形区域
        // 我们不需要画一个倾斜的平行四边形，因为我们会让 LinearGradient 本身带有倾斜角度
        mPath.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 只有当 Shader 需要更新（初始化或参数改变）时才重新创建 LinearGradient
        // 这实现了 Lazy Loading 模式
        if (isShaderDirty || mLinearGradient == null) {
            updateShader()
        }

        // 仅在动画运行时或 View 正常显示时绘制，节省 GPU 资源
        if (mValueAnimator?.isRunning == true || !isAttachedToWindow) {
            canvas.drawPath(mPath, mPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 避免内存泄漏，View 销毁时停止动画
        stopAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 当 View 重新回到窗口（例如 RecyclerView 复用），如果是自动模式则恢复动画
        if (mAnimMode == 0 && (mValueAnimator == null || !mValueAnimator!!.isRunning)) {
            if (width > 0) startLightingAnimation()
        }
    }

    // ============================================================================================
    // 核心动画逻辑
    // ============================================================================================

    /**
     * 创建并更新 Shader
     *
     * 逻辑说明：
     * 这里我们创建一个 **静态** 的 Gradient，它的长度等于流光宽度 (mWidth)。
     * 它的倾斜角度由 (0,0) 到 (x1, y1) 决定。
     * 在动画中，我们通过 Matrix 移动这个“静态条”，而不是不断重新创建不同位置的条。
     */
    private fun updateShader() {
        // 定义渐变的起始和结束点
        val x0 = 0f
        val y0 = 0f
        val x1 = mWidth.toFloat()
        val y1 = mSlope * x1 // 关键：根据斜率计算 Y 轴增量，确立渐变方向


        // 创建线性渐变
        mLinearGradient = LinearGradient(
            x0, y0, x1, y1,
            mColors,
            mPositions,
            Shader.TileMode.CLAMP // 边缘拉伸模式
        )
        mPaint.shader = mLinearGradient
        isShaderDirty = false
    }

    /**
     * 启动动画
     *
     * @param width View 宽度
     * @param repeatCount 重复次数
     * @param duration 动画时长
     */
    private fun showAnimation(width: Int, repeatCount: Int, duration: Long) {
        // 防止重复启动
        if (mValueAnimator != null && mValueAnimator!!.isRunning) return

        val offset = mWidth.toFloat()

        // 计算动画移动范围
        // 从屏幕左侧外 (-offset*2) 移动到 屏幕右侧外 (width + offset*2)
        val startVal = -offset * 2
        val endVal = width + offset * 2

        mValueAnimator?.cancel()
        mValueAnimator = ValueAnimator.ofFloat(startVal, endVal).apply {
            this.repeatCount = repeatCount
            interpolator = LinearInterpolator() // 线性插值，匀速移动
            this.duration = duration

            // 【核心性能优化点】
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float

                if (mLinearGradient != null) {
                    // 重置矩阵
                    mShaderMatrix.reset()

                    // 设置平移变换：
                    // X 轴移动 value 距离
                    // Y 轴移动 value * slope 距离
                    // 必须按照斜率比例同时移动 X 和 Y，才能让光束看起来是沿着倾斜方向“滑”过去的
                    mShaderMatrix.setTranslate(value, mSlope * value)

                    // 将矩阵应用到 Shader，而不是重新创建 Shader
                    mLinearGradient?.setLocalMatrix(mShaderMatrix)

                    // 请求重绘
                    invalidate()
                }
            }
            start()
        }
    }

    // ============================================================================================
    // 公开 API
    // ============================================================================================

    /**
     * 动态设置流光颜色和位置
     */
    fun setColorAndPositions(colors: IntArray, positions: FloatArray) {
        if (colors.size != positions.size) {
            throw RuntimeException("colors 和 positions 的数组长度必须一致")
        }
        this.mColors = colors
        this.mPositions = positions
        isShaderDirty = true // 标记脏位，下次绘制时生效
        invalidate()
    }

    /**
     * 动态设置斜率
     */
    fun setSlope(slope: Float) {
        this.mSlope = slope
        isShaderDirty = true
    }

    /**
     * 动态设置流光宽度
     * (方法名已与 ShimmerTextView 统一)
     */
    fun setShimmerWidth(width: Int) {
        this.mWidth = width
        isShaderDirty = true
    }

    /**
     * 手动开始动画
     */
    fun startLightingAnimation(duration: Long = mDuration, repeatCount: Int = mRepeatCount) {
        if (width == 0) return // 如果 View 还没测量好，暂不启动
        showAnimation(width, repeatCount, duration)
    }

    /**
     * 手动停止动画
     */
    fun stopAnimation() {
        mValueAnimator?.cancel()
    }
}