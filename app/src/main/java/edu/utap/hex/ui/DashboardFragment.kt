package edu.utap.hex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
                    binding.userName.text = user.displayName
                    binding.userEmail.text = user.email
                    binding.userUuid.text = user.uid
                }
            }

        firestoreGameList.observe(viewLifecycleOwner) {
            firestoreGameAdapter.submitList(it)
        }

        binding.signOutButton.setOnClickListener {
            mainActivity.signOut()
        }

        binding.borderHexagonsSwitch.setOnClickListener {
            Log.d(javaClass.simpleName, "border hex switch")
        }

        binding.interiorHexagonsSwitch.setOnClickListener {
            Log.d(javaClass.simpleName, "interior hex switch")
        }

        initPreviousGamesView()
    }

    private fun initPreviousGamesView() {
        val rv = binding.previousGamesView
        rv.addItemDecoration(DividerItemDecoration(rv.context, rv.layoutDirection))

        rv.adapter = firestoreGameAdapter
        firestoreGameAdapter.submitList(firestoreGameList.value)
    }

    private fun gamePicked(game: FirestoreGame) {
        Log.d(javaClass.simpleName, "game: ${game.firestoreID}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}