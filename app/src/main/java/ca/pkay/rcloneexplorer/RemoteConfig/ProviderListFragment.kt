package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.RemoteConfigListItemAdapter
import ca.pkay.rcloneexplorer.RemoteConfig.ProviderListFragment.SelectionChangedListener
import ca.pkay.rcloneexplorer.rclone.Provider
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ProviderListFragment(private val mPreselection: String?) : Fragment() {
    interface ProviderSelectedListener {
        fun onProviderSelected(provider: Provider)
    }

    fun interface SelectionChangedListener {
        fun onProviderChanged(provider: Provider)
    }

    private var mRclone: Rclone? = null

    private var mProviders: List<Provider> = listOf()
    private var mProviderFilter = ""
    private var mSelectedProvider: Provider? = null
    private var mProviderSelectedListener: ProviderSelectedListener? = null

    private var mSelectionChangeListener = SelectionChangedListener {
        mSelectedProvider = it
        mFab?.visibility = View.VISIBLE
    }

    private var mRootView: View? = null
    private var mFab: FloatingActionButton? = null


    companion object {
        @JvmStatic
        fun newProviderListConfig(): ProviderListFragment {
            return ProviderListFragment(null)
        }

        @JvmStatic
        fun newProviderListConfig(preselection: String?): ProviderListFragment {
            return ProviderListFragment(preselection)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRclone = Rclone(this.context)
        mProviders = mRclone?.providers ?: listOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_config_list, container, false)
        mRootView = view
        mFab = view.findViewById(R.id.next)
        setClickListeners(view)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mProviderSelectedListener = if (context is ProviderSelectedListener) {
            context
        } else {
            throw RuntimeException("$context must implement ProviderSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mProviderSelectedListener = null
    }

    private fun setClickListeners(view: View) {
        mFab?.setOnClickListener {
            if(mSelectedProvider != null) {
                mProviderSelectedListener?.onProviderSelected(mSelectedProvider!!)
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.config_content)
        recyclerView.adapter = updateList()
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun updateList(): RemoteConfigListItemAdapter {
        var filteredProvider = mProviders
        if(mProviderFilter.isNotBlank()) {
            filteredProvider = filteredProvider.filter {
                it.name.contains(mProviderFilter, true) ||
                        it.description.contains(mProviderFilter, true)
            }
        }

        return RemoteConfigListItemAdapter(
            ArrayList(filteredProvider.sortedWith(compareBy { it.name })),
            requireContext(),
            mSelectionChangeListener,
            mPreselection
        )

    }

    fun setSearchterm (term: String) {
        mProviderFilter = term

        if(mRootView != null) {
            val recyclerView: RecyclerView = (mRootView as View).findViewById(R.id.config_content)
            recyclerView.adapter = updateList()
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }
}