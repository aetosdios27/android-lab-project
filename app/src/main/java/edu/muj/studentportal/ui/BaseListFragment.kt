package edu.muj.studentportal.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.MainActivity
import edu.muj.studentportal.R
import edu.muj.studentportal.data.PortalSnapshot
import edu.muj.studentportal.databinding.FragmentListBinding
import kotlinx.coroutines.launch

abstract class BaseListFragment : Fragment(R.layout.fragment_list) {
    protected val host get() = requireActivity() as MainActivity
    protected val model get() = host.model
    private var binding: FragmentListBinding? = null
    private var rows: PortalRows? = null
    protected abstract fun render(snapshot: PortalSnapshot): List<PortalRow>
    protected open fun primary(snapshot: PortalSnapshot): Pair<String, () -> Unit>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = FragmentListBinding.bind(view); binding = bound
        val adapter = PortalRows(); rows = adapter
        adapter.stateRestorationPolicy = androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        bound.list.layoutManager = LinearLayoutManager(requireContext()); bound.list.adapter = adapter
        bound.list.post {
            val max = view.dp(720)
            if (bound.list.width > max) bound.list.layoutParams = bound.list.layoutParams.apply { width = max }
            if (bound.primaryAction.width > max) bound.primaryAction.layoutParams = bound.primaryAction.layoutParams.apply { width = max }
        }
        var displayed: PortalSnapshot? = null
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    val snapshot = state.snapshot
                    if (snapshot == null) {
                        adapter.submitList(buildList {
                            add(PortalRow("load", if (state.loading) "Loading your workspace…" else "Couldn’t load your data",
                                if (state.loading) "Opening local records." else "Tap to retry.", click = if (state.loading) null else { { model.refresh() } }))
                            if (!state.loading) add(PortalRow("signin", "Return to sign in", "Try another demo account.", click = { host.logout() }))
                        })
                    } else if (displayed != snapshot) {
                        displayed = snapshot
                        adapter.submitList(render(snapshot))
                        val action = primary(snapshot)
                        bound.primaryAction.visibility = if (action == null) View.GONE else View.VISIBLE
                        bound.primaryAction.text = action?.first
                        bound.primaryAction.setOnClickListener { action?.second?.invoke() }
                    }
                    bound.primaryAction.isEnabled = !state.busy
                }
            }
        }
    }
    override fun onDestroyView() { binding?.list?.adapter = null; binding = null; rows = null; super.onDestroyView() }
    protected fun heading(key: String, title: String, body: String = "") = PortalRow(key, title, body, heading = true)
}
