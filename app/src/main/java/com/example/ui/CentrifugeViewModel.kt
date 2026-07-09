package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CentrifugeViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {

    val ciltChecks: StateFlow<List<CiltCheck>> = repository.allCiltChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reliabilityPmChecks: StateFlow<List<ReliabilityPmCheck>> = repository.allReliabilityPmChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val abnormalityReports: StateFlow<List<AbnormalityReport>> = repository.allAbnormalityReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mentorPairingLogs: StateFlow<List<MentorPairingLog>> = repository.allMentorPairingLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vibrationLogs: StateFlow<List<VibrationLog>> = repository.vibrationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val syncStatus: StateFlow<String> = repository.syncStatus

    init {
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
        }
    }

    fun toggleOnlineMode() {
        viewModelScope.launch {
            repository.setOnlineMode(!isOnline.value)
        }
    }

    fun syncData(onSyncComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.syncWithJakarta()
            onSyncComplete(success)
        }
    }

    fun addCiltCheck(check: CiltCheck) {
        viewModelScope.launch {
            repository.insertCiltCheck(check)
        }
    }

    fun addReliabilityCheck(check: ReliabilityPmCheck) {
        viewModelScope.launch {
            repository.insertReliabilityCheck(check)
        }
    }

    fun addAbnormalityReport(report: AbnormalityReport) {
        viewModelScope.launch {
            repository.insertAbnormalityReport(report)
        }
    }

    fun deleteReport(report: AbnormalityReport) {
        viewModelScope.launch {
            repository.deleteAbnormalityReport(report)
        }
    }

    fun addMentorLog(log: MentorPairingLog) {
        viewModelScope.launch {
            repository.insertMentorPairingLog(log)
        }
    }

    fun addVibrationLog(log: VibrationLog) {
        viewModelScope.launch {
            repository.insertVibrationLog(log)
        }
    }
}

class CentrifugeViewModelFactory(
    private val application: Application,
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CentrifugeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CentrifugeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
