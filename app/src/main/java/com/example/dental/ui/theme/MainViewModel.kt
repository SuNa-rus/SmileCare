package com.example.dental.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dental.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app))
    private fun <T> Flow<T>.state(init: T) = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), init)

    val messages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val appointments = repo.detailedAppointments.state(emptyList())
    val patients = repo.allPatients.state(emptyList())
    val doctors = repo.doctorsWithPosition.state(emptyList())
    val services = repo.allServices.state(emptyList())
    val positions = repo.allPositions.state(emptyList())
    val records = repo.recordsWithPatient.state(emptyList())
    fun items(appointmentId: Int) = repo.items(appointmentId)

    suspend fun getPatient(id: Int) = repo.getPatient(id)
    suspend fun getDoctor(id: Int) = repo.getDoctor(id)
    suspend fun getAppointment(id: Int) = repo.getAppointment(id)
    suspend fun getService(id: Int) = repo.getService(id)
    suspend fun getPosition(id: Int) = repo.getPosition(id)
    suspend fun getRecord(id: Int) = repo.getRecord(id)

    private fun errorText(e: Exception): String {
        val m = e.message.orEmpty()
        return when {
            "уже есть приём" in m -> "У врача уже есть приём на это время"
            "Количество должно" in m -> "Количество должно быть больше нуля"
            "Цена услуги" in m -> "Цена услуги не может быть отрицательной"
            else -> "Операция невозможна: запись связана с другими данными"
        }
    }
    private fun safe(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() } catch (e: Exception) { messages.emit(errorText(e)) }
    }
    private suspend fun attempt(block: suspend () -> Unit): Boolean =
        try { block(); true } catch (e: Exception) { messages.emit(errorText(e)); false }

    /** «Оформить приём»: приём + оказанные услуги одной транзакцией. */
    suspend fun placeAppointment(a: Appointment, items: List<ServiceRendered>) = attempt { repo.placeAppointment(a, items) }
    suspend fun saveAppointment(a: Appointment) = attempt { repo.saveAppointment(a) }
    fun deleteAppointment(id: Int) = safe { repo.deleteAppointment(id) }
    fun saveItem(sr: ServiceRendered) = safe { repo.saveItem(sr) }
    fun deleteItem(id: Int) = safe { repo.deleteItem(id) }

    fun savePatient(p: Patient) = safe { repo.savePatient(p) }
    fun deletePatient(p: Patient) = safe { repo.deletePatient(p) }
    fun saveDoctor(d: Doctor) = safe { repo.saveDoctor(d) }
    fun deleteDoctor(d: Doctor) = safe { repo.deleteDoctor(d) }
    fun saveService(s: Service) = safe { repo.saveService(s) }
    fun deleteService(s: Service) = safe { repo.deleteService(s) }
    fun savePosition(p: Position) = safe { repo.savePosition(p) }
    fun deletePosition(p: Position) = safe { repo.deletePosition(p) }
    fun saveRecord(r: MedicalRecord) = safe { repo.saveRecord(r) }
    fun deleteRecord(r: MedicalRecord) = safe { repo.deleteRecord(r) }
}
