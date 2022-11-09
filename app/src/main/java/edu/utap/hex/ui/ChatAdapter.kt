package edu.utap.hex.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.hex.MainActivity
import edu.utap.hex.MainViewModel
import edu.utap.hex.databinding.RowChatBinding
import edu.utap.hex.model.ChatRow
import java.util.*

class ChatAdapter(private val viewModel: MainViewModel,
                  private val mainActivity: MainActivity)
    : ListAdapter<ChatRow, ChatAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<ChatRow>() {
        override fun areItemsTheSame(oldItem: ChatRow, newItem: ChatRow): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
        }

        override fun areContentsTheSame(oldItem: ChatRow, newItem: ChatRow): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.message == newItem.message
                    && oldItem.pictureUUID == newItem.pictureUUID
                    && oldItem.timeStamp == newItem.timeStamp
        }
    }
    companion object {
        private val iphoneTextBlue = Color.parseColor("#1982FC")
        private val iphoneMessageGreen = ColorDrawable(Color.parseColor("#43CC47"))
        private val dimGrey = Color.parseColor("#C5C5C5")
        private val dateFormat =
            SimpleDateFormat("hh:mm:ss MM-dd-yyyy", Locale.US)
        private val transparentDrawable = ColorDrawable(Color.TRANSPARENT)
    }

    // ViewHolder pattern
    inner class VH(private val rowPostBinding: RowChatBinding)
        : RecyclerView.ViewHolder(rowPostBinding.root) {
        init {
            rowPostBinding.chatTextTV.isLongClickable = true
        }

        private fun bindElements(item: ChatRow, backgroundColor: Int, textColor: Int,
                                 userTV: TextView, timeTV: TextView, textTV: TextView,
                                 textCV: CardView, picIV: ImageView) {
            // Set background on CV, not TV because...layout is weird
            textCV.setCardBackgroundColor(backgroundColor)
            textTV.setTextColor(textColor)
            userTV.text = item.name
            textTV.text = item.message

            val uuid = item.pictureUUID
            if(uuid == null) {
                picIV.setImageDrawable(transparentDrawable)
                picIV.visibility = View.GONE
            } else {
                //Log.d(javaClass.simpleName, "downloadJpg uuid $uuid message ${item.message}")
                picIV.visibility = View.VISIBLE
                mainActivity.glideFetch(uuid, picIV)
            }

            if (item.timeStamp == null) {
                timeTV.text = ""
            } else {
                //Log.d(javaClass.simpleName, "date ${item.timeStamp}")
                timeTV.text = dateFormat.format(item.timeStamp.toDate())
            }
        }
        fun bind(item: ChatRow?) {
            if (item == null) return
            if (viewModel.currentUser().uid == item.ownerUid) {
                rowPostBinding.otherContentCL.visibility = View.GONE
                rowPostBinding.contentCL.visibility = View.VISIBLE
                bindElements(
                    item, iphoneTextBlue, Color.WHITE,
                    rowPostBinding.chatUserTV, rowPostBinding.chatTimeTV,
                    rowPostBinding.chatTextTV, rowPostBinding.textCV,
                    rowPostBinding.picIV)
            } else {
                rowPostBinding.otherContentCL.visibility = View.VISIBLE
                rowPostBinding.contentCL.visibility = View.GONE
                bindElements(
                    item, dimGrey, Color.BLACK,
                    rowPostBinding.otherChatUserTV, rowPostBinding.otherChatTimeTV,
                    rowPostBinding.otherChatTextTV, rowPostBinding.otherTextCV,
                    rowPostBinding.otherPicIV)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowChatBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        //Log.d(MainActivity.TAG, "Bind pos $position")
        holder.bind(getItem(position))
    }
}
