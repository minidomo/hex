package edu.utap.hex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import edu.utap.hex.FirestoreDB
import edu.utap.hex.MainActivity
import edu.utap.hex.MainViewModel
import edu.utap.hex.R
import edu.utap.hex.databinding.FragmentDashboardBinding
import edu.utap.hex.model.FirestoreGame


class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val firestoreGameList = MutableLiveData<List<FirestoreGame>>()
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var mainActivity: MainActivity
    private lateinit var firestoreGameAdapter: FirestoreGameAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        // XXX Write me.  Lots to hook up.

        mainActivity = requireActivity() as MainActivity
        firestoreGameAdapter = FirestoreGameAdapter(::gamePicked)

        viewModel
            .observeFirebaseAuthLiveData()
            .observe(viewLifecycleOwner) {
                it?.let { user ->
                    // for some reason, observe is being called twice upon each time this
                    // fragment's view is created, so we need to account for that to detect whether
                    // a user performed a sign out and then sign in
                    // the FirebaseUser object will be the same object for those two calls
                    if (user != viewModel.getMostRecentUser()) {
                        // new sign in
                        viewModel.setMostRecentUser(user)
                        viewModel.reset()
                        refreshPreviousGameList()
                        binding.borderHexagonsSwitch.isChecked = viewModel.isBorderLabeled()
                        binding.interiorHexagonsSwitch.isChecked = viewModel.isInteriorLabeled()
                    }

                    binding.userName.text = user.displayName
                    binding.userEmail.text = user.email
                    binding.userUuid.text = user.uid
                }
            }

        binding.signOutButton.setOnClickListener {
            mainActivity.signOut()
        }

        binding.borderHexagonsSwitch.isChecked = viewModel.isBorderLabeled()
        binding.borderHexagonsSwitch.setOnClickListener {
            viewModel.setBorderLabeled(binding.borderHexagonsSwitch.isChecked)
        }

        binding.interiorHexagonsSwitch.isChecked = viewModel.isInteriorLabeled()
        binding.interiorHexagonsSwitch.setOnClickListener {
            viewModel.setInteriorLabeled(binding.interiorHexagonsSwitch.isChecked)
        }

        initPreviousGamesView()
    }

    private fun refreshPreviousGameList() {
        FirestoreDB.updateGameList(firestoreGameList)
        firestoreGameList.observe(viewLifecycleOwner) {
            firestoreGameAdapter.submitList(it)
        }
    }

    private fun initPreviousGamesView() {
        val rv = binding.previousGamesView
        rv.adapter = firestoreGameAdapter
        refreshPreviousGameList()
    }

    private fun gamePicked(game: FirestoreGame) {
        Log.d(javaClass.simpleName, "game: ${game.firestoreID}")
        viewModel.playReplayGame(game)

        val bundle = bundleOf(Pair("replay", true))
        findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_game, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}