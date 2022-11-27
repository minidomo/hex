package edu.utap.hex

import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import edu.utap.hex.HexStrategy.Companion.aiMove
import edu.utap.hex.model.FirestoreGame

/**
 * @modified added CreatingGame state
 */
enum class GameState(val value: Int) {
    NotPlaying(0), RedTurn(1), BlueTurn(2), RedWon(3), BlueWon(4), CreatingGame(5), ;

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

    fun getGameState(): GameState {
        return gameState.value!!
    }

    //////////////////////////////////////////////////////////////////////
    // Game dynamics
    fun reset() {
        clearReplayGame()
        gameState.value = GameState.NotPlaying
        moveNumber = 0
        moves.clear()
        clearBoard()
    }

    fun startGame(
        _redPlayer: HexPlayer, _bluePlayer: HexPlayer, _firstMove: GameState
    ) {
        // XXX Write me, initial state, create in firestore if not replay game
        gameState.value = GameState.CreatingGame

        firstMove = _firstMove
        redPlayer = _redPlayer
        bluePlayer = _bluePlayer

        moveNumber = 0
        moves.clear()
        clearBoard()

        if (isReplayGame()) {
            gameState.value = _firstMove
        } else {
            FirestoreDB.createGame(this) { success ->
                if (success) {
                    gameState.value = _firstMove
                } else {
                    gameState.value = GameState.NotPlaying
                }
            }
        }
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

    private fun turnToHexState(turn: GameState): HexState {
        return when (turn) {
            GameState.RedTurn -> HexState.Red
            GameState.BlueTurn -> HexState.Blue
            else -> HexState.Invalid
        }
    }

    fun makeMove(col: Int, row: Int) {
        // XXX Write me
        var hexState = HexState.Invalid

        // check for valid game state
        gameState.value?.also {
            when (it) {
                GameState.RedTurn, GameState.BlueTurn -> hexState = turnToHexState(it)
                else -> {
                    badPress.value = false
                    return
                }
            }
        }

        if (!legalMove(col, row)) {
            badPress.value = false
            return
        }

        hexBoard.update(col, row, hexState)

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
        firestoreGame: FirestoreGame, redPlayer: HexPlayer, bluePlayer: HexPlayer
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
        val replayMoves = replayGame!!.moves
        val index = moveNumber * 2

        return if (index < replayMoves.size) {
            BoardPosition(
                replayMoves[index],
                replayMoves[index + 1]
            )
        } else {
            null
        }
    }

    private fun prevReplayMove(): BoardPosition? {
        // XXX Write me
        val replayMoves = replayGame!!.moves
        val index = (moveNumber - 1) * 2

        return if (index >= 0) {
            BoardPosition(
                replayMoves[index],
                replayMoves[index + 1]
            )
        } else {
            null
        }
    }

    fun startChosenReplayGame() {
        assert(isReplayGame())
        // XXX Write me
        var prevMoveNumber = moveNumber + 1
        while (prevMoveNumber != moveNumber) {
            prevMoveNumber = moveNumber
            replayMovePrev()
        }
    }

    fun replayMovePrev() {
        assert(isReplayGame())
        // XXX Write me
        prevReplayMove()?.also {
            moveNumber--
            hexBoard.update(it.first, it.second, HexState.Unclaimed)
            completeMove()
        }
    }

    fun replayMoveNext() {
        assert(isReplayGame())
        // XXX Write me
        nextReplayMove()?.also {
            hexBoard.update(it.first, it.second, turnToHexState(whoseReplayTurn(moveNumber)))
            moveNumber++
            completeMove()
        }
    }

    fun replayGameEnd() {
        assert(isReplayGame())
        // XXX Write me
        var prevMoveNumber = moveNumber - 1
        while (prevMoveNumber != moveNumber) {
            prevMoveNumber = moveNumber
            replayMoveNext()
        }
    }

    fun replayTimestamp(): Timestamp? {
        assert(isReplayGame())
        return replayGame?.timeStamp
    }

    // Is this move legal?
    fun legalMove(col: Int, row: Int): Boolean {
        // XXX Write me.
        val state = hexBoard.read(col, row)
        return state == HexState.Unclaimed
    }
}