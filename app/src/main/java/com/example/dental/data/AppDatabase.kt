package com.example.dental.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Calendar

@Database(
    entities = [Position::class, Doctor::class, Patient::class, Appointment::class, Service::class,
        ServiceRendered::class, MedicalRecord::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun serviceDao(): ServiceDao
    abstract fun renderedDao(): ServiceRenderedDao
    abstract fun recordDao(): MedicalRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "dental.db")
                .addCallback(object : Callback() {
                    // при создании файла БД: тестовые данные (по 10 записей)
                    override fun onCreate(db: SupportSQLiteDatabase) { super.onCreate(db); seed(db) }
                    // при каждом открытии: триггеры (IF NOT EXISTS)
                    override fun onOpen(db: SupportSQLiteDatabase) { super.onOpen(db); TRIGGERS.forEach { db.execSQL(it) } }
                })
                .build().also { INSTANCE = it }
        }
    }
}

/** Триггеры целостности данных. */
private val TRIGGERS = listOf(
    // 1-2. Количество услуги в приёме должно быть больше нуля
    """CREATE TRIGGER IF NOT EXISTS trg_sr_check_insert BEFORE INSERT ON services_rendered
    BEGIN
        SELECT RAISE(ABORT, 'Количество должно быть больше нуля') WHERE NEW.quantity <= 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_sr_check_update BEFORE UPDATE ON services_rendered
    BEGIN
        SELECT RAISE(ABORT, 'Количество должно быть больше нуля') WHERE NEW.quantity <= 0;
    END""",
    // 3-5. Стоимость приёма = сумма (цена услуги x количество) по оказанным услугам
    """CREATE TRIGGER IF NOT EXISTS trg_sr_total_insert AFTER INSERT ON services_rendered
    BEGIN
        UPDATE appointments SET totalCost = (
            SELECT COALESCE(SUM(s.price * sr.quantity), 0) FROM services_rendered sr
            INNER JOIN services s ON s.id = sr.idServices WHERE sr.idAppointments = NEW.idAppointments)
        WHERE id = NEW.idAppointments;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_sr_total_update AFTER UPDATE ON services_rendered
    BEGIN
        UPDATE appointments SET totalCost = (
            SELECT COALESCE(SUM(s.price * sr.quantity), 0) FROM services_rendered sr
            INNER JOIN services s ON s.id = sr.idServices WHERE sr.idAppointments = OLD.idAppointments)
        WHERE id = OLD.idAppointments;
        UPDATE appointments SET totalCost = (
            SELECT COALESCE(SUM(s.price * sr.quantity), 0) FROM services_rendered sr
            INNER JOIN services s ON s.id = sr.idServices WHERE sr.idAppointments = NEW.idAppointments)
        WHERE id = NEW.idAppointments;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_sr_total_delete AFTER DELETE ON services_rendered
    BEGIN
        UPDATE appointments SET totalCost = (
            SELECT COALESCE(SUM(s.price * sr.quantity), 0) FROM services_rendered sr
            INNER JOIN services s ON s.id = sr.idServices WHERE sr.idAppointments = OLD.idAppointments)
        WHERE id = OLD.idAppointments;
    END""",
    // 6-7. У врача не может быть двух активных приёмов на одно и то же время
    """CREATE TRIGGER IF NOT EXISTS trg_appt_slot_insert BEFORE INSERT ON appointments
    WHEN NEW.status <> 'Отменён'
    BEGIN
        SELECT RAISE(ABORT, 'У врача уже есть приём на это время')
        WHERE EXISTS (SELECT 1 FROM appointments WHERE idDoctors = NEW.idDoctors
            AND appointmentDate = NEW.appointmentDate AND appointmentTime = NEW.appointmentTime AND status <> 'Отменён');
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_appt_slot_update BEFORE UPDATE ON appointments
    WHEN NEW.status <> 'Отменён'
    BEGIN
        SELECT RAISE(ABORT, 'У врача уже есть приём на это время')
        WHERE EXISTS (SELECT 1 FROM appointments WHERE id <> NEW.id AND idDoctors = NEW.idDoctors
            AND appointmentDate = NEW.appointmentDate AND appointmentTime = NEW.appointmentTime AND status <> 'Отменён');
    END""",
    // 8-9. Цена услуги не может быть отрицательной
    """CREATE TRIGGER IF NOT EXISTS trg_service_price_insert BEFORE INSERT ON services
    BEGIN
        SELECT RAISE(ABORT, 'Цена услуги не может быть отрицательной') WHERE NEW.price < 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_service_price_update BEFORE UPDATE ON services
    BEGIN
        SELECT RAISE(ABORT, 'Цена услуги не может быть отрицательной') WHERE NEW.price < 0;
    END"""
)

private fun date(y: Int, m: Int, d: Int): Long = Calendar.getInstance().apply { clear(); set(y, m - 1, d) }.timeInMillis

private fun seed(db: SupportSQLiteDatabase) {
    val day = 24L * 60 * 60 * 1000
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val positions = listOf(
        "Врач-терапевт" to 90000.0, "Врач-хирург" to 120000.0, "Врач-ортодонт" to 110000.0, "Врач-ортопед" to 115000.0,
        "Гигиенист" to 60000.0, "Ассистент врача" to 55000.0, "Администратор" to 50000.0,
        "Рентген-лаборант" to 58000.0, "Главный врач" to 170000.0, "Старшая медсестра" to 62000.0
    )
    positions.forEachIndexed { i, p -> db.execSQL("INSERT INTO positions(id, title, salary) VALUES (?,?,?)", arrayOf<Any?>(i + 1, p.first, p.second)) }

    // фамилия, имя, отчество, специализация, телефон, стаж, категория, должность
    val doctors = listOf(
        listOf("Андреев", "Сергей", "Викторович", "Терапевт", "+79003000001", 8, "Первая", 1),
        listOf("Белоусова", "Ирина", "Павловна", "Хирург", "+79003000002", 12, "Высшая", 2),
        listOf("Васильев", "Дмитрий", "Олегович", "Ортодонт", "+79003000003", 6, "Вторая", 3),
        listOf("Григорьева", "Анна", "Михайловна", "Ортопед", "+79003000004", 10, "Высшая", 4),
        listOf("Дмитриева", "Елена", "Сергеевна", "Терапевт", "+79003000005", 4, "Без категории", 1),
        listOf("Егоров", "Максим", "Андреевич", "Хирург-имплантолог", "+79003000006", 15, "Высшая", 2),
        listOf("Зайцева", "Ольга", "Николаевна", "Ортодонт", "+79003000007", 9, "Первая", 3),
        listOf("Ильин", "Артём", "Валерьевич", "Ортопед", "+79003000008", 7, "Первая", 4),
        listOf("Козлова", "Мария", "Александровна", "Стоматолог-терапевт", "+79003000009", 18, "Высшая", 9),
        listOf("Лебедева", "Наталья", "Игоревна", "Гигиенист", "+79003000010", 5, "Вторая", 5)
    )
    doctors.forEachIndexed { i, d ->
        db.execSQL(
            "INSERT INTO doctors(id, lastName, firstName, middleName, specialization, phoneNumber, workExperience, category, idPositions) VALUES (?,?,?,?,?,?,?,?,?)",
            arrayOf<Any?>(i + 1, d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7])
        )
    }

    // фамилия, имя, отчество, телефон, email, адрес, дата рождения, пол
    val patients = listOf(
        listOf("Смирнов", "Алексей", "Петрович", "+79104000001", "smirnov@mail.ru", "Москва, ул. Ленина, 10", date(1990, 4, 12), "Мужской"),
        listOf("Кузнецова", "Мария", "Ивановна", "+79104000002", "kuznetsova@mail.ru", "Москва, ул. Тверская, 5", date(1985, 9, 23), "Женский"),
        listOf("Попов", "Дмитрий", "Сергеевич", "+79104000003", "popov@gmail.com", "Химки, ул. Мира, 14", date(1978, 1, 15), "Мужской"),
        listOf("Соколова", "Анна", "Викторовна", "+79104000004", "sokolova@yandex.ru", "Москва, пр. Мира, 22", date(1995, 6, 30), "Женский"),
        listOf("Михайлов", "Игорь", "Олегович", "+79104000005", "mikhailov@mail.ru", "Балашиха, ул. Победы, 3", date(2001, 11, 8), "Мужской"),
        listOf("Новикова", "Екатерина", "Андреевна", "+79104000006", "novikova@gmail.com", "Москва, ул. Арбат, 17", date(1992, 3, 19), "Женский"),
        listOf("Фёдоров", "Павел", "Николаевич", "+79104000007", "fedorov@yandex.ru", "Мытищи, ул. Садовая, 8", date(1969, 12, 2), "Мужской"),
        listOf("Морозова", "Ольга", "Дмитриевна", "+79104000008", "morozova@mail.ru", "Москва, ул. Покровка, 31", date(1988, 7, 27), "Женский"),
        listOf("Волков", "Артём", "Александрович", "+79104000009", "volkov@gmail.com", "Одинцово, ул. Каштановая, 6", date(2005, 2, 14), "Мужской"),
        listOf("Алексеева", "Дарья", "Сергеевна", "+79104000010", "alekseeva@yandex.ru", "Москва, ул. Чехова, 12", date(1999, 10, 5), "Женский")
    )
    patients.forEachIndexed { i, p ->
        db.execSQL(
            "INSERT INTO patients(id, lastName, firstName, middleName, phoneNumber, email, address, dateOfBirth, gender, policyNumber) VALUES (?,?,?,?,?,?,?,?,?,?)",
            arrayOf<Any?>(i + 1, p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], "12345678901234" + "%02d".format(i + 1))
        )
    }

    // название, тип, описание, цена
    val services = listOf(
        listOf("Осмотр и консультация", "Диагностика", "Первичный осмотр, составление плана лечения", 1000.0),
        listOf("Лечение кариеса", "Терапия", "Лечение кариеса с установкой пломбы", 4500.0),
        listOf("Профессиональная гигиена", "Гигиена", "Ультразвуковая чистка и Air Flow", 5500.0),
        listOf("Удаление зуба", "Хирургия", "Простое удаление зуба под анестезией", 3500.0),
        listOf("Установка импланта", "Имплантация", "Имплантат с формирователем десны", 45000.0),
        listOf("Коронка металлокерамика", "Ортопедия", "Изготовление и установка коронки", 18000.0),
        listOf("Отбеливание зубов", "Отбеливание", "Фотоотбеливание в кабинете", 22000.0),
        listOf("Установка брекетов", "Ортодонтия", "Брекет-система на одну челюсть", 40000.0),
        listOf("Рентген зуба", "Диагностика", "Прицельный снимок", 700.0),
        listOf("Лечение пульпита", "Терапия", "Лечение каналов с пломбированием", 8500.0)
    )
    services.forEachIndexed { i, s ->
        db.execSQL("INSERT INTO services(id, title, type, description, price) VALUES (?,?,?,?,?)", arrayOf<Any?>(i + 1, s[0], s[1], s[2], s[3]))
    }

    // пациент, врач, смещение дней, время, статус
    val appts = listOf(
        listOf(1, 1, 0, "10:00", "Запланирован"), listOf(2, 2, 0, "11:30", "Запланирован"),
        listOf(3, 3, 1, "09:30", "Запланирован"), listOf(4, 4, 1, "14:00", "Запланирован"),
        listOf(5, 5, 2, "12:00", "Запланирован"), listOf(6, 6, -1, "15:30", "Завершён"),
        listOf(7, 7, -1, "10:00", "Завершён"), listOf(8, 8, -2, "16:00", "Завершён"),
        listOf(9, 9, -3, "13:30", "Отменён"), listOf(10, 10, 3, "11:00", "Запланирован")
    )
    // приём, услуга, количество
    val rendered = listOf(
        listOf(1, 1, 1), listOf(1, 9, 1), listOf(2, 2, 1), listOf(2, 9, 2), listOf(3, 8, 1), listOf(4, 6, 1), listOf(5, 3, 1),
        listOf(6, 1, 1), listOf(6, 4, 1), listOf(7, 10, 1), listOf(8, 5, 1), listOf(9, 7, 1), listOf(10, 1, 1), listOf(10, 3, 1)
    )
    appts.forEachIndexed { i, a ->
        val total = rendered.filter { it[0] == i + 1 }.sumOf { (services[it[1] - 1][3] as Double) * it[2] }
        db.execSQL(
            "INSERT INTO appointments(id, idPatients, idDoctors, appointmentDate, appointmentTime, status, totalCost) VALUES (?,?,?,?,?,?,?)",
            arrayOf<Any?>(i + 1, a[0], a[1], today + (a[2] as Int) * day, a[3], a[4], total)
        )
    }
    rendered.forEachIndexed { i, r ->
        db.execSQL("INSERT INTO services_rendered(id, idAppointments, idServices, quantity) VALUES (?,?,?,?)", arrayOf<Any?>(i + 1, r[0], r[1], r[2]))
    }

    // история болезни, аллергии, хронические заболевания, дата создания
    val records = listOf(
        listOf("Лечение кариеса, плановые осмотры", "Нет", "Нет", date(2023, 3, 10)),
        listOf("Установка пломб, профессиональная гигиена", "Пенициллин", "Гастрит", date(2023, 6, 21)),
        listOf("Удаление зуба мудрости", "Нет", "Гипертония", date(2022, 11, 2)),
        listOf("Ортодонтическое лечение, брекеты", "Латекс", "Нет", date(2024, 1, 15)),
        listOf("Первичный осмотр", "Нет", "Нет", date(2026, 5, 9)),
        listOf("Лечение пульпита", "Лидокаин", "Сахарный диабет", date(2024, 9, 18)),
        listOf("Протезирование, коронки", "Нет", "Ишемическая болезнь сердца", date(2021, 4, 7)),
        listOf("Отбеливание, гигиена полости рта", "Никель", "Нет", date(2025, 2, 24)),
        listOf("Лечение кариеса, герметизация фиссур", "Нет", "Нет", date(2025, 8, 12)),
        listOf("Консультация имплантолога", "Цитрусовые", "Бронхиальная астма", date(2026, 1, 30))
    )
    records.forEachIndexed { i, r ->
        db.execSQL(
            "INSERT INTO medical_records(id, idPatients, medicalHistory, allergies, chronicDiseases, creationDate) VALUES (?,?,?,?,?,?)",
            arrayOf<Any?>(i + 1, i + 1, r[0], r[1], r[2], r[3])
        )
    }
}
