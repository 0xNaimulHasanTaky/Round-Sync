package ca.pkay.rcloneexplorer.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.Activities.MainActivity
import ca.pkay.rcloneexplorer.databinding.FragmentPermissionsBinding
import ca.pkay.rcloneexplorer.util.PermissionManager


class PermissionFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mPermissionManager: PermissionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        mPermissionManager = PermissionManager(requireContext())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateVisibilities()
        binding.buttonAlarms.setOnClickListener {
            mPermissionManager.requestAlarms()
        }
        binding.buttonStorage.setOnClickListener {
            mPermissionManager.requestStorage(this.requireActivity())
        }
        binding.buttonNotifications.setOnClickListener {
            startActivity(PermissionManager.getNotificationSettingsIntent(requireContext()))
        }
        binding.buttonBatteryOptimizations.setOnClickListener {
            mPermissionManager.requestBatteryOptimizationException()
        }
    }

    override fun onResume() {
        super.onResume()
        updateVisibilities()
    }

    fun updateVisibilities() {
        if(mPermissionManager.grantedStorage()) {
            binding.cardStorage.visibility = View.GONE
        }
        if(mPermissionManager.grantedAlarms()) {
            binding.cardAlarms.visibility = View.GONE
        }
        if(mPermissionManager.grantedNotifications()) {
            binding.cardNotifications.visibility = View.GONE
        }
        if(mPermissionManager.grantedBatteryOptimizationExemption()) {
            binding.cardBatteryOptimizations.visibility = View.GONE
        }

        if(mPermissionManager.grantedStorage() &&
            mPermissionManager.grantedNotifications() &&
            mPermissionManager.grantedBatteryOptimizationExemption() &&
            mPermissionManager.grantedStorage()) {
            (requireActivity() as MainActivity).startRemotesFragment()
        }
    }

    companion object {
        fun newInstance(): PermissionFragment {
            return PermissionFragment()
        }
    }
}