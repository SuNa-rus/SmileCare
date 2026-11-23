package com.example.dental.data

import androidx.room.withTransaction

class AppRepository(private val db: AppDatabase) {
    private val positions = db.positionDao()
    private val doctors = db.doctorDao()
    private val patients = db.patientDao()
    private val appointments = db.appointmentDao()
    private val services = db.serviceDao()
    private val rendered = db.renderedDao()
    private val records = db.recordDao()

    val allPositions = positions.getAll()
    val doctorsWithPosition = doctors.getAllWithPosition()
    val allPatients = patients.getAll()
    val detailedAppointments = appointments.getAllDetailed()
    val allServices = services.getAll()
    val recordsWithPatient = records.getAllWithPatient()
    fun items(appointmentId: Int) = rendered.getItems(appointmentId)

    suspend fun getPosition(id: Int) = positions.getById(id)
    suspend fun savePosition(p: Position) { if (p.id == 0) positions.insert(p) else positions.update(p) }
    suspend fun deletePosition(p: Position) = positions.delete(p)

    suspend fun getDoctor(id: Int) = doctors.getById(id)
    suspend fun saveDoctor(d: Doctor) { if (d.id == 0) doctors.insert(d) else doctors.update(d) }
    suspend fun deleteDoctor(d: Doctor) = doctors.delete(d)

    suspend fun getPatient(id: Int) = patients.getById(id)
    suspend fun savePatient(p: Patient) { if (p.id == 0) patients.insert(p) else patients.update(p) }
    suspend fun deletePatient(p: Patient) = patients.delete(p)

    suspend fun getAppointment(id: Int) = appointments.getById(id)
    suspend fun saveAppointment(a: Appointment) { if (a.id == 0) appointments.insert(a) else appointments.update(a) }
    suspend fun deleteAppointment(id: Int) = appointments.deleteById(id)

    suspend fun getService(id: Int) = services.getById(id)
    suspend fun saveService(s: Service) { if (s.id == 0) services.insert(s) else services.update(s) }
    suspend fun deleteService(s: Service) = services.delete(s)

    suspend fun getRecord(id: Int) = records.getById(id)
    suspend fun saveRecord(r: MedicalRecord) { if (r.id == 0) records.insert(r) else records.update(r) }
    suspend fun deleteRecord(r: MedicalRecord) = records.delete(r)

    suspend fun saveItem(sr: ServiceRendered) { if (sr.id == 0) rendered.insert(sr) else rendered.update(sr) }
    suspend fun deleteItem(id: Int) = rendered.deleteById(id)

    /**
     * Аналог хранимой процедуры «Оформить приём».
     * Приём и все оказанные услуги сохраняются ОДНОЙ транзакцией; стоимость пересчитывают триггеры.
     * Если любая вставка отклонена (например, занято время врача), откатывается всё.
     */
    suspend fun placeAppointment(a: Appointment, items: List<ServiceRendered>): Int = db.withTransaction {
        val id = appointments.insert(a.copy(id = 0, totalCost = 0.0)).toInt()
        items.forEach { rendered.insert(it.copy(id = 0, idAppointments = id)) }
        id
    }
}
