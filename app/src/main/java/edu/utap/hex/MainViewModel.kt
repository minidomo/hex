package edu.utap.hex

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import edu.utap.hex.model.FirestoreGame
import kotlin.random.Random

class MainViewModel : ViewModel() {
    private var firebaseAuthLiveData = FirestoreAuthLiveData()
    private var bluePlayer = MutableLiveData("")
    private var redPlayer = MutableLiveData("")
    private var isBorderLabeled = true
    private var isInteriorLabeled = false
    private val hexGame = HexGame()
    private val random = Random(3)// System.currentTimeMillis())
    private var mostRecentUser: FirebaseUser? = null

    fun getMostRecentUser(): FirebaseUser? {
        return mostRecentUser
    }

    fun setMostRecentUser(user: FirebaseUser?) {
        mostRecentUser = user
    }

    fun reset() {
        hexGame.reset()
        bluePlayer.value = ""
        redPlayer.value = ""
        isBorderLabeled = true
        isInteriorLabeled = false
    }

    fun observeBluePlayer(): LiveData<String> {
        return bluePlayer
    }

    fun observeRedPlayer(): LiveData<String> {
        return redPlayer
    }

    // Everyone should use our random object
    fun random(): Random {
        return random
    }

    // A display task, but done in multiple places
    fun flashBackground(view: View) {
        val colorToWarn: Animator = ValueAnimator
            .ofObject(ArgbEvaluator(), Color.TRANSPARENT, Color.RED)
            .apply { duration = 70 } // milliseconds
            .apply {
                addUpdateListener { animator ->
                    view.setBackgroundColor(animator.animatedValue as Int)
                }
            }
        val colorFromWarn = ValueAnimator
            .ofObject(ArgbEvaluator(), Color.RED, Color.TRANSPARENT)
            .apply { duration = 100 }
            .apply {
                addUpdateListener { animator ->
                    view.setBackgroundColor(animator.animatedValue as Int)
                }
            }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            colorToWarn,
            colorFromWarn
        )
        animatorSet.start()
    }

    fun startAIGame() {
        hexGame.clearReplayGame()
        // red player index 0, blue player index 1
        val players = arrayOfNulls<HexPlayer>(2) as Array<HexPlayer>

        val aiIndex = random.nextInt(players.size)
        val playerIndex = (aiIndex + 1).mod(players.size)
        players[aiIndex] = HexPlayer.aiPlayer()

        firebaseAuthLiveData.value?.also {
            players[playerIndex] =
                HexPlayer(it.displayName!!, it.uid, it.email!!)

            redPlayer.value = players[0].name
            bluePlayer.value = players[1].name

            val first = if (random.nextBoolean()) GameState.RedTurn else GameState.BlueTurn
            hexGame.startGame(players[0], players[1], first)
        }
        // XXX Write me
    }

    fun playReplayGame(firestoreGame: FirestoreGame) {
        // XXX Write me
        hexGame.clearReplayGame()
        val redHexPlayer =
            HexPlayer(firestoreGame.playerNameList[0], firestoreGame.playerUidList[0], "")
        val blueHexPlayer =
            HexPlayer(firestoreGame.playerNameList[1], firestoreGame.playerUidList[1], "")

        redPlayer.value = redHexPlayer.name
        bluePlayer.value = blueHexPlayer.name

        hexGame.startReplayGame(firestoreGame, redHexPlayer, blueHexPlayer)
    }

    fun doTurn() {
        assert(!hexGame.isReplayGame())
        hexGame.doTurn(this)
    }

    // Allowing this to escape encapsulation so we don't indirect
    // a bunch of methods through the view model
    fun game(): HexGame {
        return hexGame
    }

    fun startPersonGame() {
        // TODO: Implement
        hexGame.clearReplayGame()
    }

    fun isBorderLabeled(): Boolean {
        return isBorderLabeled
    }

    fun isInteriorLabeled(): Boolean {
        return isInteriorLabeled
    }

    fun setBorderLabeled(value: Boolean) {
        isBorderLabeled = value
    }

    fun setInteriorLabeled(value: Boolean) {
        isInteriorLabeled = value
    }

    /////////////////////////////////////////////////////////////
    // Authentication
    fun updateUser() {
        firebaseAuthLiveData.updateUser()
    }

    fun currentUser(): FirebaseUser {
        return firebaseAuthLiveData.currentUser()
    }

    fun signOut() {
        firebaseAuthLiveData.signOut()
    }

    fun observeFirebaseAuthLiveData(): LiveData<FirebaseUser?> {
        return firebaseAuthLiveData
    }
}