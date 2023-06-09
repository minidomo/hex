package edu.utap.hex

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.hex.model.ChatRow
import edu.utap.hex.model.FirestoreGame

object FirestoreDB {
    private const val TAG = "FirestoreDB"
    private const val allGamesCollection = "allGames"
    private const val chatCollection = "chat"
    private val games = FirebaseFirestore.getInstance().collection(allGamesCollection)
    private var currentGameID = ""
    private var chatList: MutableLiveData<List<ChatRow>>? = null
    private var gameList: MutableLiveData<List<FirestoreGame>>? = null

    private fun redoChatQuery() {
        if (currentGameID.isEmpty()) {
            chatList?.postValue(listOf())
        } else {
            listenChat(chatList)
        }
    }

    fun updateChatList(newList: MutableLiveData<List<ChatRow>>) {
        if (chatList != newList) {
            chatList = newList
            redoChatQuery()
        }
    }

    fun updateGameList(newList: MutableLiveData<List<FirestoreGame>>) {
        gameList = newList
        listenGameList()
    }

    // Can't use live data since we don't have a lifecycle, so build
    // our own.
    fun setCurrentGameID(id: String) {
        if (id != currentGameID) {
            currentGameID = id
            if (chatList != null) {
                redoChatQuery()
            }
        }
    }

    private fun listenGameList() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        // XXX Write me.
        games.addSnapshotListener { value, error ->
            if (error != null) {
                Log.d(TAG, "fail - game snapshot")
                return@addSnapshotListener
            }

            value?.also {
                val list = value.documents
                    .filter {
                        if (!it.exists()) return@filter false

                        val uids = it["playerUidList"] as List<String>
                        uids.contains(uid)
                    }
                    .mapNotNull { it.toObject(FirestoreGame::class.java) }
                    .sortedByDescending { it.timeStamp }

                gameList?.postValue(list)
            }
        }
    }

    private fun listenChat(chatList: MutableLiveData<List<ChatRow>>?) {
        // XXX Write me.
        val chats = games.document(currentGameID).collection(chatCollection)

        chats.addSnapshotListener { value, error ->
            if (error != null) {
                Log.d(TAG, "fail - chat snapshot")
                return@addSnapshotListener
            }

            value?.also {
                val list = value.documents
                    .filter { it.exists() }
                    .mapNotNull { it.toObject(ChatRow::class.java) }
                    .sortedBy { it.timeStamp }

                chatList?.postValue(list)
            }
        }
    }

    fun saveChatRow(chatRow: ChatRow) {
        if (currentGameID.isEmpty()) {
            return
        }
        Log.d("FirestoreDB", "saveChatRow ownerUid(${chatRow.ownerUid})")
        // XXX Write me.
        // https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
        val chats = games.document(currentGameID).collection(chatCollection)

        val doc = chats.document()
        chatRow.firestoreID = doc.id

        doc.set(chatRow)
            .addOnSuccessListener {
                Log.d(TAG, "success - chat")
            }
            .addOnFailureListener {
                Log.d(TAG, "fail - chat")
            }
    }

    fun updateMoves(game: HexGame) {
        assert(currentGameID.isNotEmpty())
        // XXX Write me.
        games.document(currentGameID)
            .update("moves", game.flattenMoves())
            .addOnSuccessListener {
                Log.d(TAG, "success - moves")
            }
            .addOnFailureListener {
                Log.d(TAG, "fail - moves")
            }
    }

    /**
     * @modified added result callback parameter
     */
    fun createGame(hexGame: HexGame, result: (success: Boolean) -> Unit) {
        // XXX Write me
        val doc = games.document()
        val game = hexGame.toFirestoreGame()

        game.firestoreID = doc.id

        doc.set(game)
            .addOnSuccessListener {
                Log.d(TAG, "success - game")
                setCurrentGameID(doc.id)
                result(true)
            }
            .addOnFailureListener {
                Log.d(TAG, "fail - game")
                result(false)
            }
    }
}