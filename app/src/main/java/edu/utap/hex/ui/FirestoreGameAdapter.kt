package edu.utap.hex.ui

import android.graphics.Color
import android.graphics.ColorFilter
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.hex.databinding.RowGameBinding
import edu.utap.hex.model.FirestoreGame
import java.util.*

class FirestoreGameAdapter(val gamePicked: (FirestoreGame) -> Unit) :
    ListAdapter<FirestoreGame, FirestoreGameAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<FirestoreGame>() {
        override fun areItemsTheSame(oldItem: FirestoreGame, newItem: FirestoreGame): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
        }

        override fun areContentsTheSame(oldItem: FirestoreGame, newItem: FirestoreGame): Boolean {
            return oldItem.boardDim == newItem.boardDim
                    && oldItem.firstPlayerUid == newItem.firstPlayerUid
                    && oldItem.playerNameList == newItem.playerNameList
                    && oldItem.playerUidList == newItem.playerUidList
                    && oldItem.moves == newItem.moves
        }
    }

    companion object {
        private val dimGray = Color.parseColor("#C5C5C5")
        private val dateFormat =
            SimpleDateFormat("MM-dd-yyyy hh:mm:ss", Locale.US)
    }

    // ViewHolder pattern
    inner class VH(val rowGameBinding: RowGameBinding) :
        RecyclerView.ViewHolder(rowGameBinding.root) {
        init {
            // Set on click listener on the root view
            rowGameBinding.root.setOnClickListener {
                val replayGame = getItem(adapterPosition)
                gamePicked(replayGame)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowGameBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // XXX Write me.  Let's bind
        val game = getItem(position)
        val binding = holder.rowGameBinding

        binding.root.background.setTint(dimGray)
        binding.redUserName.background.setTint(HexagonDisplay.redColor)
        binding.blueUserName.background.setTint(HexagonDisplay.blueColor)

        binding.redUserName.text = game.playerNameList[0]
        binding.blueUserName.text = game.playerNameList[1]

        game.timeStamp?.let {
            binding.date.text = dateFormat.format(it.toDate())
        }
    }
}

