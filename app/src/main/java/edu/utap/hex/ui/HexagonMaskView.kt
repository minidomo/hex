package edu.utap.hex.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sqrt

// Draws the hexagon images
// https://stackoverflow.com/questions/22601400/how-to-give-hexagon-shape-to-imageview/22987264#22987264
class HexagonMaskView(context: Context)
    : androidx.appcompat.widget.AppCompatTextView(context) {
    private val hexagonPath  = Path()
    private val hexagonBorderPath = Path()

    private val strokePaint = Paint().apply {
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
    fun setStrokeColor(color : Int) {
        strokePaint.color = color
    }
    private val circlePaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1f
        style = Paint.Style.FILL
    }
    fun setCircleColor(color : Int) {
        circlePaint.color = color
    }
    private val fillPaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        style = Paint.Style.FILL
    }
    fun setFillColor(color : Int) {
        fillPaint.color = color
    }

    private fun calculatePath(radius : Float) {
        val halfRadius = radius / 2f
        val triangleHeight : Float = (sqrt(3.0) * halfRadius).toFloat()
        val centerX = measuredWidth / 2f
        val centerY = measuredHeight / 2f

        this.hexagonPath.reset();
        this.hexagonPath.moveTo(centerX, centerY + radius);
        this.hexagonPath.lineTo(centerX - triangleHeight, centerY + halfRadius);
        this.hexagonPath.lineTo(centerX - triangleHeight, centerY - halfRadius);
        this.hexagonPath.lineTo(centerX, centerY - radius);
        this.hexagonPath.lineTo(centerX + triangleHeight, centerY - halfRadius);
        this.hexagonPath.lineTo(centerX + triangleHeight, centerY + halfRadius);
        this.hexagonPath.close();

        val radiusBorder = radius - 5f;
        val halfRadiusBorder = radiusBorder / 2f;
        val triangleBorderHeight : Float =
            (sqrt(3.0) * halfRadiusBorder).toFloat()

        this.hexagonBorderPath.reset()
        this.hexagonBorderPath.moveTo(centerX, centerY + radiusBorder)
        this.hexagonBorderPath.lineTo(centerX - triangleBorderHeight, centerY + halfRadiusBorder)
        this.hexagonBorderPath.lineTo(centerX - triangleBorderHeight, centerY - halfRadiusBorder)
        this.hexagonBorderPath.lineTo(centerX, centerY - radiusBorder)
        this.hexagonBorderPath.lineTo(centerX + triangleBorderHeight, centerY - halfRadiusBorder)
        this.hexagonBorderPath.lineTo(centerX + triangleBorderHeight, centerY + halfRadiusBorder)
        this.hexagonBorderPath.close()
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        c.drawPath(hexagonBorderPath, fillPaint)
        c.drawPath(hexagonBorderPath, strokePaint)
        c.drawCircle(measuredWidth / 2f, measuredHeight / 2f, 8F, circlePaint)
        super.onDraw(c)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec);
        val height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        calculatePath(Math.min(width / 2f, height / 2f));
    }
}