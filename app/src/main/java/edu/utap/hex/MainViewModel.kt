package edu.utap.hex

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
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

    fun observeBluePlayer(): LiveData<String> {
        return bluePlayer
    }
    fun observeRedPlayer(): LiveData<String> {
        return redPlayer
    }
    // Everyone should use our random object
    fun random() : Random {
        return random
    }

    // A display task, but done in multiple places
    fun flashBackground(view: View) {
        val colorToWarn : Animator = ValueAnimator
            .ofObject(ArgbEvaluator(), Color.TRANSPARENT, Color.RED)
            .apply{duration = 70} // milliseconds
            .apply{addUpdateListener { animator ->
                view.setBackgroundColor(animator.animatedValue as Int) }}
        val colorFromWarn = ValueAnimator
            .ofObject(ArgbEvaluator(), Color.RED, Color.TRANSPARENT)
            .apply{duration = 100}
            .apply{addUpdateListener { animator ->
                view.setBackgroundColor(animator.animatedValue as Int) }}
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            colorToWarn,
            colorFromWarn
        )
        animatorSet.start()
    }

    fun startAIGame() {
        hexGame.clearReplayGame()
        val redHexPlayer : HexPlayer
        val blueHexPlayer : HexPlayer
        // XXX Write me
    }
    fun playReplayGame(firestoreGame : FirestoreGame) {
        // XXX Write me
    }
    fun doTurn() {
        assert(!hexGame.isReplayGame())
        hexGame.doTurn(this)
    }
    // Allowing this to escape encapsulation so we don't indirect
    // a bunch of methods through the view model
    fun game() : HexGame {
        return hexGame
    }

    fun startPersonGame() {
        // TODO: Implement
        hexGame.clearReplayGame()
    }

    fun isBorderLabeled() : Boolean {
        return isBorderLabeled
    }
    fun isInteriorLabeled() : Boolean {
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
    fun currentUser() :FirebaseUser {
        return firebaseAuthLiveData.currentUser()
    }
    fun signOut() {
        firebaseAuthLiveData.signOut()
    }
    fun observeFirebaseAuthLiveData(): LiveData<FirebaseUser?> {
        return firebaseAuthLiveData
    }
}