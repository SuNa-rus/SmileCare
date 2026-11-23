package com.example.dental.data

import androidx.room.*

@Entity(tableName = "positions")
data class Position(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val salary: Double
)

@Entity(
    tableName = "doctors",
    foreignKeys = [ForeignKey(Position::class, ["id"], ["idPositions"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("idPositions")]
)
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val specialization: String,
    val phoneNumber: String,
    val workExperience: Int,
    val category: String,
    val idPositions: Int
)

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val phoneNumber: String,
    val email: String,
    val address: String,
    val dateOfBirth: Long,        // epoch millis
    val gender: String,
    val policyNumber: String
)

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(Patient::class, ["id"], ["idPatients"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(Doctor::class, ["id"], ["idDoctors"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idPatients"), Index("idDoctors")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPatients: Int,
    val idDoctors: Int,
    val appointmentDate: Long,    // начало дня, epoch millis
    val appointmentTime: String,  // "HH:mm"
    val status: String,           // Запланирован / Завершён / Отменён
    val totalCost: Double         // пересчитывается триггерами по оказанным услугам
)

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,
    val description: String,
    val price: Double
)

@Entity(
    tableName = "services_rendered",
    foreignKeys = [
        ForeignKey(Appointment::class, ["id"], ["idAppointments"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Service::class, ["id"], ["idServices"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idAppointments"), Index("idServices")]
)
data class ServiceRendered(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idAppointments: Int,
    val idServices: Int,
    val quantity: Int
)

@Entity(
    tableName = "medical_records",
    foreignKeys = [ForeignKey(Patient::class, ["id"], ["idPatients"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("idPatients")]
)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPatients: Int,
    val medicalHistory: String,
    val allergies: String,
    val chronicDiseases: String,
    val creationDate: Long
)

// ---- результаты JOIN-запросов ----
data class AppointmentDetails(
    val id: Int,
    val appointmentDate: Long,
    val appointmentTime: String,
    val status: String,
    val totalCost: Double,
    val patientName: String,
    val doctorName: String,
    val specialization: String
)

data class DoctorWithPosition(@Embedded val doctor: Doctor, val positionTitle: String)
data class RecordWithPatient(@Embedded val record: MedicalRecord, val patientName: String)
data class RenderedItem(val id: Int, val idServices: Int, val title: String, val price: Double, val quantity: Int)
