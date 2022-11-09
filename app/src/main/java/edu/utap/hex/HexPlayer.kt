package edu.utap.hex

import com.google.firebase.auth.FirebaseUser

class HexPlayer(val name : String, val uid : String,
                val email: String) {
    companion object {
        // Factory methods
        fun firebaseToHex(firebaseUser: FirebaseUser) : HexPlayer {
            return HexPlayer(firebaseUser.displayName ?: "Anonymous player",
                firebaseUser.uid,
                firebaseUser.email ?: "Anonymous email")
        }
        private const val aiUid = "aiPlayerUid"
        fun aiPlayer() : HexPlayer {
            return HexPlayer("Artificial Ali", aiUid, "no_email")
        }
    }
    fun isAI() : Boolean {
        return uid == aiUid
    }
}