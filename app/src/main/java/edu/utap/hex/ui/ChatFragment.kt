package edu.utap.hex.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import edu.utap.hex.*
import edu.utap.hex.databinding.FragmentChatBinding
import edu.utap.hex.model.ChatRow

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    private fun scrollToEnd() {
        if (_binding == null) return
        (chatAdapter.itemCount - 1).takeIf { it > 0 }?.let(binding.chatRV::smoothScrollToPosition)
    }

    private fun initRecyclerView() {
        val mainActivity = requireActivity() as MainActivity
        chatAdapter = ChatAdapter(viewModel, mainActivity)
        binding.chatRV.adapter = chatAdapter
        //https://stackoverflow.com/questions/26580723/how-to-scroll-to-the-bottom-of-a-recyclerview-scrolltoposition-doesnt-work
        binding.chatRV.viewTreeObserver.addOnGlobalLayoutListener {
            scrollToEnd()
        }
    }

    private fun clearCompose() {
        binding.composeMessageET.text.clear()
    }

    private fun initComposeSendIB() {
        // Send message button
        // XXX Write me
        val sendButton = binding.composeSendIB
        val game = viewModel.game()

        sendButton.setOnClickListener {
            when (game.getGameState()) {
                GameState.NotPlaying, GameState.CreatingGame -> {
                    clearCompose()
                }
                else -> {
                    val msg = binding.composeMessageET.text.toString()

                    if (game.isReplayGame()) {
                        Snackbar.make(
                            binding.root,
                            "Can't chat during replay game",
                            Snackbar.LENGTH_LONG,
                        ).show()
                        return@setOnClickListener
                    }

                    if (msg.isEmpty()) {
                        Snackbar.make(
                            binding.root,
                            "Can't send an empty message",
                            Snackbar.LENGTH_LONG,
                        ).show()
                        return@setOnClickListener
                    }

                    val user = viewModel.currentUser()

                    val chatRow = ChatRow(
                        user.displayName,
                        user.uid,
                        msg,
                        moveNumber = game.moveNumber()
                    )

                    FirestoreDB.saveChatRow(chatRow)

                    clearCompose()
                    scrollToEnd()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)
        val chatList = MutableLiveData<List<ChatRow>>()
        initRecyclerView()
        initComposeSendIB()
        FirestoreDB.updateChatList(chatList)
        chatList.observe(viewLifecycleOwner) {
            Log.d(javaClass.simpleName, "Observe Chat $it")
            chatAdapter.submitList(it)
        }

        binding.composeMessageET.setOnEditorActionListener { /*v*/_, actionId, event ->
            // If user has pressed enter, or if they hit the soft keyboard "send" button
            // (which sends DONE because of the XML)
            if ((event != null
                        && (event.action == KeyEvent.ACTION_DOWN)
                        && (event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)
            ) {
                (requireActivity() as MainActivity).hideKeyboard()
                binding.composeSendIB.callOnClick()
            }
            true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}