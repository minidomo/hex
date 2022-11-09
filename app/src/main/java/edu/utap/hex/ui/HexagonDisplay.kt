package edu.utap.hex.ui

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import edu.utap.hex.HexState
import edu.utap.hex.MainViewModel

// Provides functionality for hexagons
class HexagonDisplay(
    private val col: Int, private val row: Int,
    private val viewModel: MainViewModel,
    frameLayout: FrameLayout,
) {
    companion object {
        val blueColor = Color.parseColor("#336699")
        val redColor = Color.parseColor("#990033")
        const val hexDimDP = 35f
        private const val xSpacingDP = (hexDimDP*0.8).toFloat()
        private const val ySpacingDP = (hexDimDP*0.65).toFloat()
        private const val xOffsetDP = (hexDimDP*0.4).toFloat()
    }
    private val context = frameLayout.context
    private val hexagonMaskView = HexagonMaskView(context)

    private fun circleColor(hexState : HexState) : Int {
        return when( hexState ) {
            HexState.RedCircled, HexState.BlueCircled ->  Color.YELLOW
            else -> Color.TRANSPARENT
        }
    }

    private fun strokeColor(hexState : HexState) : Int {
        return when (hexState) {
            HexState.RedTopBorder, HexState.RedBottomBorder   -> Color.GRAY
            HexState.BlueLeftBorder, HexState.BlueRightBorder -> Color.GRAY
            HexState.NeutralBorder                            -> Color.TRANSPARENT
            HexState.Unclaimed, HexState.Red, HexState.Blue   -> Color.BLACK
            HexState.RedCircled, HexState.BlueCircled         -> Color.BLACK
            HexState.Invalid                                  -> Color.RED
        }
    }
    private fun fillColor(hexState : HexState): Int {
        return when (hexState) {
            HexState.RedTopBorder, HexState.RedBottomBorder   -> redColor
            HexState.BlueLeftBorder, HexState.BlueRightBorder -> blueColor
            HexState.NeutralBorder                            -> Color.TRANSPARENT
            HexState.Unclaimed                                -> Color.TRANSPARENT
            HexState.Red, HexState.RedCircled                 -> redColor
            HexState.Blue, HexState.BlueCircled               -> blueColor
            HexState.Invalid                                  -> Color.YELLOW
        }
    }
    fun newState(hexState: HexState) {
        hexagonMaskView.setStrokeColor(strokeColor(hexState))
        hexagonMaskView.setFillColor(fillColor(hexState))
        hexagonMaskView.setCircleColor(circleColor(hexState))
        hexagonMaskView.gravity = Gravity.CENTER
        // XXX Write me, labels in 12sp
        hexagonMaskView.invalidate()
    }

    private fun pxFromDp(dp :Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp, context.resources.displayMetrics).toInt()
    }

    init {
        val xSpacing = pxFromDp(xSpacingDP).toFloat()
        val ySpacing = pxFromDp(ySpacingDP).toFloat()
        val horizontalOffset = if((row%2) == 1){
            pxFromDp(xOffsetDP).toFloat()
        } else {
            0F
        }
        val layoutParams = ViewGroup.LayoutParams(
            pxFromDp(hexDimDP),
            pxFromDp(hexDimDP)
        )
        hexagonMaskView.layoutParams = layoutParams
        hexagonMaskView.x = col * xSpacing + horizontalOffset
        hexagonMaskView.y = row * ySpacing

        hexagonMaskView.setOnClickListener{
            if(viewModel.game().isReplayGame()) {
                viewModel.flashBackground(frameLayout)
            } else {
                viewModel.game().makeMove(col, row)
            }
        }
        frameLayout.addView(hexagonMaskView)
    }
}