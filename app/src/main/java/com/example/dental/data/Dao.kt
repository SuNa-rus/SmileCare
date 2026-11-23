package com.example.dental.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions ORDER BY title") fun getAll(): Flow<List<Position>>
    @Query("SELECT * FROM positions WHERE id = :id") suspend fun getById(id: Int): Position?
    @Insert suspend fun insert(p: Position): Long
    @Update suspend fun update(p: Position)
    @Delete suspend fun delete(p: Position)
}

@Dao
interface DoctorDao {
    @Query("""SELECT d.*, p.title AS positionTitle FROM doctors d
        INNER JOIN positions p ON p.id = d.idPositions ORDER BY d.lastName""")
    fun getAllWithPosition(): Flow<List<DoctorWithPosition>>
    @Query("SELECT * FROM doctors WHERE id = :id") suspend fun getById(id: Int): Doctor?
    @Insert suspend fun insert(d: Doctor): Long
    @Update suspend fun update(d: Doctor)
    @Delete suspend fun delete(d: Doctor)
}

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY lastName") fun getAll(): Flow<List<Patient>>
    @Query("SELECT * FROM patients WHERE id = :id") suspend fun getById(id: Int): Patient?
    @Insert suspend fun insert(p: Patient): Long
    @Update suspend fun update(p: Patient)
    @Delete suspend fun delete(p: Patient)
}

@Dao
interface AppointmentDao {
    // JOIN: вместо ID выводим ФИО пациента, ФИО и специализацию врача
    @Query("""
        SELECT a.id AS id, a.appointmentDate AS appointmentDate, a.appointmentTime AS appointmentTime,
               a.status AS status, a.totalCost AS totalCost,
               (p.lastName || ' ' || p.firstName) AS patientName,
               (d.lastName || ' ' || d.firstName) AS doctorName,
               d.specialization AS specialization
        FROM appointments a
        INNER JOIN patients p ON p.id = a.idPatients
        INNER JOIN doctors d ON d.id = a.idDoctors
        ORDER BY a.appointmentDate, a.appointmentTime
    """)
    fun getAllDetailed(): Flow<List<AppointmentDetails>>
    @Query("SELECT * FROM appointments WHERE id = :id") suspend fun getById(id: Int): Appointment?
    @Insert suspend fun insert(a: Appointment): Long
    @Update suspend fun update(a: Appointment)
    @Query("DELETE FROM appointments WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY type, title") fun getAll(): Flow<List<Service>>
    @Query("SELECT * FROM services WHERE id = :id") suspend fun getById(id: Int): Service?
    @Insert suspend fun insert(s: Service): Long
    @Update suspend fun update(s: Service)
    @Delete suspend fun delete(s: Service)
}

@Dao
interface ServiceRenderedDao {
    // JOIN с Services: оказанные услуги приёма с названиями и ценами
    @Query("""SELECT sr.id AS id, sr.idServices AS idServices, s.title AS title, s.price AS price, sr.quantity AS quantity
        FROM services_rendered sr INNER JOIN services s ON s.id = sr.idServices
        WHERE sr.idAppointments = :appointmentId ORDER BY sr.id""")
    fun getItems(appointmentId: Int): Flow<List<RenderedItem>>
    @Insert suspend fun insert(sr: ServiceRendered): Long
    @Update suspend fun update(sr: ServiceRendered)
    @Query("DELETE FROM services_rendered WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface MedicalRecordDao {
    @Query("""SELECT m.*, (p.lastName || ' ' || p.firstName) AS patientName FROM medical_records m
        INNER JOIN patients p ON p.id = m.idPatients ORDER BY m.creationDate DESC""")
    fun getAllWithPatient(): Flow<List<RecordWithPatient>>
    @Query("SELECT * FROM medical_records WHERE id = :id") suspend fun getById(id: Int): MedicalRecord?
    @Insert suspend fun insert(r: MedicalRecord): Long
    @Update suspend fun update(r: MedicalRecord)
    @Delete suspend fun delete(r: MedicalRecord)
}
