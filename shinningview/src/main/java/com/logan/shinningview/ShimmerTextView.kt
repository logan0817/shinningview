package com.logan.shinningview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class ShimmerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var mLinearGradient: LinearGradient? = null

    private var mGradientMatrix: Matrix? = null

    private var mViewWidth = 0

    private var mTranslate = 0

    private var mAnimating = true

    private val speed = 50
    private var mPaint: Paint? = null

    private var textColor = 0
    private var shimmerColor = 0

    init {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerTextView).apply {
                try {
                    textColor = getColor(R.styleable.ShimmerTextView_stvTextColor, Color.BLACK)
                    shimmerColor =
                        getColor(R.styleable.ShimmerTextView_stvShimmerColor, Color.WHITE)
                    mPaint = paint
                    mGradientMatrix = Matrix()
                } finally {
                    recycle()
                }

            }
        }

    }

    fun setShimmer(isShimmer: Boolean) {
        mAnimating = isShimmer
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measuredWidth
    }

    fun initLinearGradient() {
        mLinearGradient = LinearGradient(
            0f,
            0f,
            mViewWidth.toFloat(),
            0f,
            intArrayOf(textColor, shimmerColor, textColor),
            null,
            Shader.TileMode.CLAMP
        )
        mPaint?.shader = mLinearGradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mAnimating) {
            if (mGradientMatrix != null && mLinearGradient != null) {
                mTranslate += mViewWidth / 10
                if (mTranslate > 2 * mViewWidth) {
                    mTranslate = -mViewWidth
                }
                mGradientMatrix?.setTranslate(mTranslate.toFloat(), 0f)
                mLinearGradient?.setLocalMatrix(mGradientMatrix)
            } else {
                initLinearGradient()
            }
            postInvalidateDelayed(speed.toLong())
        }
    }
}