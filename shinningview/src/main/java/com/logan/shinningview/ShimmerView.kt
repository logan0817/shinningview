package com.logan.shinningview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class ShimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mWidth = -1
    private var mSlope: Float = -1F
    private var mAnimMode = 0
    private var mDuration = 1600L
    private var mRepeatCount = 0
    private var mColors = intArrayOf(0x00FFFFFF, 0x5AFFFFFF, 0x5AFFFFFF, 0x00FFFFFF)
    private var mPositions = floatArrayOf(0f, 0.5f, 0.51f, 1f)
    private var mRadius = 0

    private var mPaint: Paint = Paint()
    private var mPath: Path? = null
    private var mClipPath: Path? = null

    private var mValueAnimator: ValueAnimator? = null

    init {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerView).apply {
                try {
                    mWidth = getDimensionPixelSize(R.styleable.ShimmerView_csWidth, mWidth)
                    mSlope = getFloat(R.styleable.ShimmerView_csSlope, mSlope)
                    mRadius = getDimensionPixelSize(R.styleable.ShimmerView_csRadius, mRadius)
                    mAnimMode = getInt(R.styleable.ShimmerView_csAnimMode, mAnimMode)
                    mDuration =
                        getInt(R.styleable.ShimmerView_csDuration, mDuration.toInt()).toLong()
                    mRepeatCount = getInt(R.styleable.ShimmerView_csRepeat, mRepeatCount)
                    val colorsStr = getString(R.styleable.ShimmerView_csColors)
                    val positionsStr = getString(R.styleable.ShimmerView_csPositions)
                    if (!colorsStr.isNullOrBlank() && !positionsStr.isNullOrBlank()) {
                        val colorArr =
                            colorsStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val positionArr =
                            positionsStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        val size = colorArr.size
                        if (size == positionArr.size) {
                            mColors = IntArray(size)
                            mPositions = FloatArray(size)
                            for (i in 0 until size) {
                                mColors[i] = Color.parseColor(colorArr[i])
                                mPositions[i] = positionArr[i].toFloat()
                            }
                        }
                    }
                } finally {
                    recycle()
                }
            }
        }
    }


    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        initSetup(widthSize, heightSize)
        if (mAnimMode == 0) {
            showAnimation(widthSize, heightSize, mRepeatCount, mDuration)
        }
    }

    private fun initSetup(width: Int, height: Int) {
        if (mRepeatCount < 0) {
            mRepeatCount = -1
        }
        if (mWidth < 0) {
            mWidth = width / 3
        }
        if (mSlope < 0) {
            mSlope = height / width.toFloat()
        }

        val point1 = Point(0, 0)
        val point2 = Point(width, 0)
        val point3 = Point(width, height)
        val point4 = Point(0, height)
        mPath = Path()
        mPath?.moveTo(point1.x.toFloat(), point1.y.toFloat())
        mPath?.lineTo(point2.x.toFloat(), point2.y.toFloat())
        mPath?.lineTo(point3.x.toFloat(), point3.y.toFloat())
        mPath?.lineTo(point4.x.toFloat(), point4.y.toFloat())
        mPath?.close()



        mClipPath = Path()
        val mRect = RectF()
        mRect[0f, 0f, width.toFloat()] = height.toFloat()
        mClipPath?.addRoundRect(mRect, mRadius.toFloat(), mRadius.toFloat(), Path.Direction.CW)

    }


    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制圆角
        mClipPath?.let { canvas.clipPath(it) }
        //绘制流光
        mPath?.let { canvas.drawPath(it, mPaint) }
    }

    private fun showAnimation(width: Int, height: Int, repeatCount: Int, duration: Long) {
        val offset = mWidth.toFloat()
        mValueAnimator?.cancel()
        mValueAnimator = ValueAnimator.ofFloat(0f - offset * 2, width + offset * 2)
        mValueAnimator?.repeatCount = repeatCount
        mValueAnimator?.interpolator = LinearInterpolator()
        mValueAnimator?.duration = duration
        mValueAnimator?.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue as Float
            mPaint.shader = LinearGradient(
                value,
                mSlope * value,
                value + offset,
                mSlope * (value + offset),
                mColors,
                mPositions,
                Shader.TileMode.CLAMP
            )
            invalidate()
        }
        mValueAnimator?.start()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator?.cancel()
        mValueAnimator = null
    }

    fun setColorAndPositions(colors: IntArray, positions: FloatArray) {
        if (colors.size != positions.size) {
            throw RuntimeException("colors&positions的Array.size必须一致")
        }
        this.mColors = colors
        this.mPositions = positions
    }

    fun setSlope(mSlope: Float) {
        this.mSlope = mSlope
    }

    fun setWidth(mWidth: Int) {
        this.mWidth = mWidth
    }

    fun startLightingAnimation(duration: Long = mDuration, repeatCount: Int = mRepeatCount) {
        showAnimation(width, height, repeatCount, duration)
    }


}