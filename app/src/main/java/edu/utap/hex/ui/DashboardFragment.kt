package edu.utap.hex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        // XXX Write me.  Lots to hook up.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}