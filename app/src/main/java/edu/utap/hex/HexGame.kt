package edu.utap.hex

import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import edu.utap.hex.HexStrategy.Companion.aiMove
import edu.utap.hex.model.FirestoreGame

enum class GameState(val value: Int) {
    NotPlaying(0),
    RedTurn(1),
    BlueTurn(2),
    RedWon(3),
    BlueWon(4),
    ;

    companion object {
        private val map = values().associateBy(GameState::value)
        operator fun get(value: Int) = map[value]
    }
}
typealias BoardPosition = Pair<Int, Int>

// Collect the state required to play a Hex game
class HexGame {
    private var moveNumber = 0
    private lateinit var firstMove: GameState
    private lateinit var redPlayer: HexPlayer
    private lateinit var bluePlayer: HexPlayer
    private val gameState = MutableLiveData(GameState.NotPlaying)
    private val boardDim = 13
    private val hexBoard = HexBoard(boardDim)
    private var moves = mutableListOf<BoardPosition>()
    private var replayGame: FirestoreGame? = null

    // Live data to inform view to flash background
    private var badPress = MutableLiveData(false)

    fun flattenMoves(): List<Int> {
        val accumulator = mutableListOf<Int>()
        moves.forEach {
            accumulator.add(it.first)
            accumulator.add(it.second)
        }
        return accumulator
    }

    fun toFirestoreGame(): FirestoreGame {
        return FirestoreGame(
            boardDim = boardDim,
            firstPlayerUid = if (firstMove == GameState.RedTurn) {
                redPlayer.uid
            } else {
                bluePlayer.uid
            },
            playerUidList = listOf(redPlayer.uid, bluePlayer.uid),
            playerNameList = listOf(redPlayer.name, bluePlayer.name),
            moves = flattenMoves(),
        )
    }

    fun isReplayGame(): Boolean {
        return replayGame != null
    }

    fun moveNumber(): Int {
        return moveNumber
    }

    // Only for limited use
    fun getBoardDim(): Int {
        return hexBoard.boardDim
    }

    ////////////////////////////////////////////////////////////////////////
    // Board view
    fun makeView(frameLayout: FrameLayout, viewModel: MainViewModel) {
        hexBoard.makeView(viewModel, frameLayout)
    }

    fun clearBoard() {
        return hexBoard.clearBoard()
    }

    fun observeGameState(): LiveData<GameState> {
        return gameState
    }

    fun observeBadPress(): LiveData<Boolean> {
        return badPress
    }

    //////////////////////////////////////////////////////////////////////
    // Game dynamics
    fun startGame(
        _redPlayer: HexPlayer, _bluePlayer: HexPlayer,
        _firstMove: GameState
    ) {
        // XXX Write me, initial state, create in firestore if not replay game
    }

    private fun whoseReplayTurn(givenMoveNumber: Int): GameState {
        assert(isReplayGame())
        val game = replayGame!!
        var redRemainder = 0 // If red was first
        if (game.firstPlayerUid == game.playerUidList[1]) {
            // But blue is first
            redRemainder = 1
        }
        return if (redRemainder == (givenMoveNumber % 2)) {
            GameState.RedTurn
        } else {
            GameState.BlueTurn
        }
    }

    private fun completeMove() {
        if (!isReplayGame()) {
            // Modify move number before changing game state.
            moveNumber += 1
        }
        Log.d("completeMove", "move number $moveNumber")
        if (hexBoard.redDidWin()) {
            gameState.value = GameState.RedWon
            return
        }
        if (hexBoard.blueDidWin()) {
            gameState.value = GameState.BlueWon
            return
        }
        if (gameState.value == GameState.BlueTurn) {
            gameState.value = GameState.RedTurn
        } else {
            gameState.value = GameState.BlueTurn
        }
    }

    fun makeMove(col: Int, row: Int) {
        // XXX Write me
        gameState.value?.let {
            if (it == GameState.NotPlaying) return
        }

        Log.d("makeMove", "XXX col $col row $row val ${hexBoard.read(col, row)}")
        if (!isReplayGame()) {
            moves.add(BoardPosition(col, row))
            FirestoreDB.updateMoves(this)
        }
        completeMove()
    }

    fun doTurn(viewModel: MainViewModel) {
        when (gameState.value) {
            GameState.RedTurn -> {
                if (redPlayer.isAI()) {
                    aiMove(viewModel, this)
                }
            }
            GameState.BlueTurn -> {
                if (bluePlayer.isAI()) {
                    aiMove(viewModel, this)
                }
            }
            // Expect input
            else -> {}
        }
    }

    /////////////////////////////////////////////////////////////
    // Replay games
    fun startReplayGame(
        firestoreGame: FirestoreGame,
        redPlayer: HexPlayer, bluePlayer: HexPlayer
    ) {
        replayGame = firestoreGame
        FirestoreDB.setCurrentGameID(firestoreGame.firestoreID)
        val firstMove = whoseReplayTurn(0)
        startGame(redPlayer, bluePlayer, firstMove)
    }

    fun clearReplayGame() {
        replayGame = null
        FirestoreDB.setCurrentGameID("")
    }

    private fun nextReplayMove(): BoardPosition? {
        // XXX Write me
        return null
    }

    private fun prevReplayMove(): BoardPosition? {
        // XXX Write me
        return null
    }

    fun startChosenReplayGame() {
        assert(isReplayGame())
        // XXX Write me
    }

    fun replayMovePrev() {
        assert(isReplayGame())
        // XXX Write me
    }

    fun replayMoveNext() {
        assert(isReplayGame())
        // XXX Write me
    }

    fun replayGameEnd() {
        assert(isReplayGame())
        // XXX Write me
    }

    fun replayTimestamp(): Timestamp? {
        assert(isReplayGame())
        return replayGame?.timeStamp
    }

    // Is this move legal?
    fun legalMove(col: Int, row: Int): Boolean {
        // XXX Write me.
        return false
    }
}