package edu.utap.hex

import android.util.Log
import android.widget.FrameLayout
import edu.utap.hex.ui.HexagonDisplay
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class HexState {
    // Border values
    RedTopBorder, RedBottomBorder, BlueLeftBorder, BlueRightBorder, NeutralBorder,
    // Interior values
    Unclaimed, Red, Blue, RedCircled, BlueCircled,
    // Out of bounds
    Invalid,
    ;
    companion object {
        // Only follow hexagons from the "start" side.
        // Stop as soon as we reach any goal
        fun isFollowRed(hexState: HexState) : Boolean {
            return when(hexState) {
                RedTopBorder, Red, RedCircled -> true
                else-> false
            }
        }
        fun isFollowBlue(hexState: HexState) : Boolean {
            return when(hexState) {
                BlueLeftBorder, Blue, BlueCircled -> true
                else-> false
            }
        }
    }
}

// Assume a square board
class HexBoard(val boardDim : Int) {
    private fun makeBoard(numCols: Int, numRows: Int):
            Array<Array<HexState>> {
        return Array(numCols) { x ->
            Array(numRows) { y ->
                when (x) {
                    0 ->
                        if (y == 0 || y == numRows - 1) {
                            HexState.NeutralBorder
                        } else {
                            HexState.BlueLeftBorder
                        }
                    numCols - 1 ->
                        when( y ) {
                            0           -> HexState.RedTopBorder
                            numRows - 1 -> HexState.RedBottomBorder
                            else        -> HexState.BlueRightBorder
                        }
                    else ->
                        when( y ) {
                            0           -> HexState.RedTopBorder
                            numRows - 1 -> HexState.RedBottomBorder
                            else        -> HexState.Unclaimed
                        }
                }
            }
        }
    }
    // Board encodes the state, boardView the view being displayed
    private val board = makeBoard(boardDim, boardDim)
    // Updates go through this function so we can update the view also
    fun update(col: Int, row: Int, hexState: HexState) {
        board[col][row] = hexState
        boardView[col][row].newState(hexState)
    }
    fun read(col: Int, row: Int) : HexState {
        assert(0 <= col)
        assert(col < boardDim)
        assert(0 <= row)
        assert(row < boardDim)
        return board[col][row]
    }
    private lateinit var boardView : MutableList<MutableList<HexagonDisplay>>

    // Useful for the graph representation of the board
    // Only compute it once
    private val vertices by lazy {
        val vert = mutableSetOf<Pair<Int,Int>>()
        for(col in 0 until boardDim) {
            for(row in 0 until boardDim) {
                vert.add(Pair(col, row))
            }
        }
        vert
    }

    // List of all playable counters in the bottom row
    private val redBottomPairs by lazy {
        val pairs = mutableListOf<Pair<Int,Int>>()
        for(col in 1 until boardDim-1) {
            pairs.add(Pair(col,boardDim-2))
        }
        pairs
    }
    // List of all playable counters in the rightmost column
    private val blueRightPairs by lazy {
        val pairs = mutableListOf<Pair<Int,Int>>()
        for(row in 1 until boardDim-1) {
            pairs.add(Pair(boardDim-2, row))
        }
        pairs
    }
    private fun getState(col : Int, row : Int) : HexState {
        if(col < 0 || col >= boardDim) return HexState.Invalid
        if(row < 0 || row >= boardDim) return HexState.Invalid
        return board[col][row]
    }
    private fun matchingNeighbors(col : Int, row : Int,
                                  isColor : (HexState) -> Boolean)
    : Set<Pair<Int, Int>> {
        val neighbors = mutableSetOf<Pair<Int, Int>>()
        // Hexagon has 6 possible neighbors
        if(isColor(getState(col-1, row))) {
            neighbors.add(Pair(col-1, row))
        }
        if(isColor(getState(col+1,row))) {
            neighbors.add(Pair(col+1, row))
        }
        if(isColor(getState(col, row-1))) {
            neighbors.add(Pair(col, row-1))
        }
        // Because we shift our hexagons, each column has
        // row-1, but even rows have the previous col
        // and odd rows have the next col
        // Here even is actually rows 1, 3,.., but because of the border
        // they have an even index.
        val altCol = if(((row)%2) == 0) { col - 1 } else { col + 1}
        if(isColor(getState(altCol, row-1))) {
            neighbors.add(Pair(altCol, row-1))
        }
        if(isColor(getState(col, row+1))) {
            neighbors.add(Pair(col, row+1))
        }
        if(isColor(getState(altCol, row+1))) {
            neighbors.add(Pair(altCol, row+1))
        }
        return neighbors
    }
    private fun boardToGraph(isColor : (HexState) -> Boolean) : Graph<Pair<Int,Int>> {
        val edges = mutableMapOf<BoardPosition, Set<BoardPosition>>()
        for(col in 0 until boardDim) {
            for(row in 0 until boardDim) {
                edges[Pair(col, row)] = matchingNeighbors(col, row, isColor)
            }
        }
        return Graph(vertices, edges)
    }
    ///////////////////////////////////////////////////////////////
    // View
    fun makeView(viewModel: MainViewModel, frameLayout: FrameLayout) {
        boardView = mutableListOf()
        for(col in 0 until boardDim) {
            boardView.add(mutableListOf())
            for( row in 0 until boardDim) {
                Log.d("begin", "$col $row")
                boardView[col].add(
                    HexagonDisplay(col, row, viewModel, frameLayout))
                Log.d("observe", "$col $row")
            }
        }
        redrawBoardView()
    }
    fun clearMarker(col: Int, row: Int) {
        // XXX Write me
    }
    fun clearBoard() {
        // XXX Write me
    }
    private fun redrawBoardView() {
        // Include borders
        for(row in 0 until boardDim) {
            for (col in 0 until boardDim) {
                Log.d("clearFinishedGame", "$col $row ${board[col][row]}")
                boardView[col][row].newState(board[col][row])
            }
        }
    }
    private fun circlefy(hexState: HexState) : HexState {
        return when(hexState) {
            HexState.Red -> HexState.RedCircled
            HexState.Blue -> HexState.BlueCircled
            else -> return hexState
        }
    }
    private fun lightPath(path: MutableList<Pair<Int,Int>>) {
        // XXX Write me, circlefy each element of the path with 100ms delay
    }
    private fun trimBorder(_path: List<Pair<Int,Int>>)
    : MutableList<Pair<Int,Int>> {
        val path = _path.toMutableList()
        if(path[0].first == 0) {
            // col == 0 means blue border
            while(path[0].first == 0) {
                path.removeFirst()
            }
        } else {
            // Red border
            while(path[0].second == 0) {
                path.removeFirst()
            }
        }
        return path
    }
    private fun didWin(graph: Graph<Pair<Int,Int>>,
                       startBorder: Pair<Int,Int>,
                       endPairs: List<Pair<Int,Int>>) : Boolean {
        val shortestPathTree = GraphUtil.dijkstra(graph, startBorder)
        for (endBorder in endPairs) {
            if (GraphUtil.isPath(shortestPathTree, startBorder, endBorder)) {
                lightPath(trimBorder(GraphUtil.shortestPath(
                    shortestPathTree, startBorder, endBorder)))
                return true
            }
        }
        return false
    }
    fun redDidWin() : Boolean {
        val graph = boardToGraph {
            HexState.isFollowRed(it)
        }
        return didWin(graph, Pair(1,0), redBottomPairs)
    }
    fun blueDidWin() : Boolean {
        val graph = boardToGraph {
            HexState.isFollowBlue(it)
        }
        return didWin(graph, Pair(0,1), blueRightPairs)
    }
}