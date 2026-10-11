package edu.muj.studentportal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.muj.studentportal.data.PortalRepository
import edu.muj.studentportal.data.PortalSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PortalState(val snapshot: PortalSnapshot? = null, val loading: Boolean = true,
    val busy: Boolean = false, val message: String? = null, val error: String? = null,
    val completedAction: Long = 0)

class PortalViewModel(val repository: PortalRepository, val userId: Long) : ViewModel() {
    private val mutableState = MutableStateFlow(PortalState())
    val state = mutableState.asStateFlow()
    init { refresh() }
    fun refresh() {
        if (mutableState.value.busy) return
        viewModelScope.launch {
            try { mutableState.value = mutableState.value.copy(snapshot = repository.snapshot(userId), loading = false, error = null) }
            catch (error: Exception) { if (error is CancellationException) throw error
                mutableState.value = mutableState.value.copy(loading = false, error = error.message ?: "Unable to load your records.") }
        }
    }
    fun perform(success: String, action: suspend PortalRepository.() -> Unit) {
        if (mutableState.value.busy) return
        mutableState.value = mutableState.value.copy(busy = true, error = null, message = null)
        viewModelScope.launch {
            try {
                repository.action()
                mutableState.value = mutableState.value.copy(snapshot = repository.snapshot(userId), busy = false,
                    message = success, completedAction = mutableState.value.completedAction + 1)
            } catch (error: Exception) { if (error is CancellationException) throw error
                mutableState.value = mutableState.value.copy(busy = false, error = error.message ?: "Couldn't save. Your changes have not been confirmed.") }
        }
    }
    fun consumeMessage() { mutableState.value = mutableState.value.copy(message = null, error = null) }
    companion object {
        fun factory(repository: PortalRepository, userId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PortalViewModel(repository, userId) as T
        }
    }
}
