package edu.utap.hex.ui

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import edu.utap.hex.FirestoreDB
import edu.utap.hex.GameState
import edu.utap.hex.MainViewModel
import edu.utap.hex.R
import edu.utap.hex.databinding.FragmentGameBinding
import edu.utap.hex.model.ChatRow
import java.util.*


class GameFragment : Fragment(R.layout.fragment_game) {
    private var _binding: FragmentGameBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val dateFormat =
        SimpleDateFormat("MM-dd-yyyy hh:mm:ss", Locale.US)
    private val lightGray = Color.parseColor("#ededed")

    private fun pxFromDp(context: Context, dp :Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), context.resources.displayMetrics)
    }
    private lateinit var chatViewMe : List<TextView>
    private lateinit var chatViewThem : List<TextView>

    private fun setReplayDate(timestamp: Timestamp?) {
        if(timestamp != null) {
            val date = timestamp.toDate()
            Log.d("replay", "${dateFormat.format(date)}")
            binding.replayDate.text = dateFormat.format(date)
        }
    }
    private fun replayGameView() {
        // XXX Write me, replay view
    }
    private fun interactiveGameView() {
        // XXX Write me, interactive view
    }
    private fun showChatRow(i: Int, row : ChatRow) {
        if (row.ownerUid == viewModel.currentUser().uid) {
            Log.d("showChatRow", "me $i")
            // We are on the right
            chatViewThem[i].visibility = View.INVISIBLE
            chatViewMe[i].visibility = View.VISIBLE
            chatViewMe[i].text = row.message
            chatViewMe[i].setBackgroundColor(lightGray)
        } else {
            chatViewMe[i].visibility = View.INVISIBLE
            chatViewThem[i].visibility = View.VISIBLE
            chatViewThem[i].text = row.message
            chatViewThem[i].setBackgroundColor(Color.TRANSPARENT)
        }
    }
    private fun notPlaying() {
        binding.turnIndicator.setBackgroundColor(Color.DKGRAY)
        // Make message a bit smaller so it fits in the button
        val str = "Play person or AI"
        val ss = SpannableString(str)
        ss.setSpan(RelativeSizeSpan(0.75f), 0, str.length, 0) // set size
        binding.turnIndicator.text = ss
    }
    private fun redWon() {
        binding.turnIndicator.setBackgroundColor(Color.DKGRAY)
        val str = "Red won (${viewModel.game().moveNumber()})"
        binding.turnIndicator.text = str
    }
    private fun blueWon() {
        binding.turnIndicator.setBackgroundColor(Color.DKGRAY)
        // Make message a bit smaller so it fits in the button
        val str = "Blue won (${viewModel.game().moveNumber()})"
        binding.turnIndicator.text = str
    }
    private fun redTurn() {
        // XXX Write me
    }
    private fun blueTurn() {
        // XXX Write me
    }

    private fun startReplayGame() {
        // XXX Write me, deal with view and call doReplayGameBegin
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameBinding.bind(view)
        val boardDim = viewModel.game().getBoardDim()
        val chatList = MutableLiveData<List<ChatRow>>()
        val lp = binding.playArea.layoutParams
        // Should improve this.  My formula doesn't take into account overlap
        lp.height = 68*boardDim //boardDim * pxFromDp(view.context, HexagonDisplay.hexDimDP)
        binding.playArea.layoutParams = lp

        // TODO: Two player games
        binding.playPerson.setOnClickListener {
            viewModel.game().clearBoard()
            interactiveGameView()
            viewModel.startPersonGame()
            Snackbar.make(binding.root,
                "Person vs. Person play is not supported.\nOnly Person vs. AI is supported",
                Snackbar.LENGTH_LONG).show()
        }
        // XXX Write me, initialize chatViewMe and chatViewThem as a list
        // of text views.  Look at the game layout
        // XXX Write me, hook up controls

        
    }

    // Navigation handled here
    override fun onResume() {
        super.onResume()
        // Very unfortunate misfeature of safeargs 11/6/2022
        // https://stackoverflow.com/questions/62639146/android-navargs-clear-on-back
        val args = requireArguments()
        if (GameFragmentArgs.fromBundle(args).replay) {
            Log.d("GameFragment", "onResume replay")
            startReplayGame()
        }
        // If we are passed replay==true, act on it, then clear it, so
        // we revert to replay==false, which is the default
        args.clear()
        Log.d("GameFragment", "onResume")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}