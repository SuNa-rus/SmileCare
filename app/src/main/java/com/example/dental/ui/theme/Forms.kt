@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dental.ui

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dental.data.*
import kotlinx.coroutines.launch

private const val REQ = "Обязательное поле"
private val Genders = listOf("Женский", "Мужской")
private val Categories = listOf("Без категории", "Вторая", "Первая", "Высшая")
private val ServiceTypes = listOf("Диагностика", "Терапия", "Хирургия", "Имплантация", "Ортопедия", "Ортодонтия", "Гигиена", "Отбеливание")
private fun String.phoneOk() = count { it.isDigit() } >= 10
private fun String.toMoney() = replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

@Composable
private fun FormScreen(title: String, vm: MainViewModel, nav: NavController, content: @Composable ColumnScope.() -> Unit) =
    AppScreen(title, vm, onBack = { nav.popBackStack() }) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp), content = content
        )
    }

// ======================= ПАЦИЕНТ =======================
@Composable
fun PatientFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var last by remember { mutableStateOf("") }; var first by remember { mutableStateOf("") }
    var middle by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<Long?>(null) }; var gender by remember { mutableStateOf<Int?>(null) }
    var policy by remember { mutableStateOf("") }; var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getPatient(id)?.let {
            last = it.lastName; first = it.firstName; middle = it.middleName; phone = it.phoneNumber; email = it.email
            address = it.address; dob = it.dateOfBirth; gender = Genders.indexOf(it.gender).takeIf { i -> i >= 0 }; policy = it.policyNumber
        }
    }
    val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val policyOk = policy.count { it.isDigit() } == 16 && policy.all { it.isDigit() }
    FormScreen(if (id == 0) "Новый пациент" else "Пациент", vm, nav) {
        Field(last, { last = it }, "Фамилия", if (tried && last.isBlank()) REQ else null)
        Field(first, { first = it }, "Имя", if (tried && first.isBlank()) REQ else null)
        Field(middle, { middle = it }, "Отчество", null)
        Dropdown("Пол", Genders.mapIndexed { i, g -> i to g }, gender, if (tried && gender == null) "Выберите пол" else null) { gender = it }
        DateField("Дата рождения", dob, if (tried && dob == null) REQ else null) { dob = it }
        Field(phone, { phone = it }, "Телефон", if (tried && !phone.phoneOk()) "Минимум 10 цифр" else null, KeyboardType.Phone)
        Field(email, { email = it }, "Email", if (tried && !emailOk) "Некорректный email" else null, KeyboardType.Email)
        Field(address, { address = it }, "Адрес", if (tried && address.isBlank()) REQ else null)
        Field(policy, { policy = it }, "Номер полиса (16 цифр)", if (tried && !policyOk) "Ровно 16 цифр" else null, KeyboardType.Number)
        PrimaryButton("Сохранить") {
            tried = true
            if (last.isNotBlank() && first.isNotBlank() && gender != null && dob != null && phone.phoneOk() && emailOk && address.isNotBlank() && policyOk) {
                vm.savePatient(Patient(id, last.trim(), first.trim(), middle.trim(), phone.trim(), email.trim(), address.trim(), dob!!, Genders[gender!!], policy.trim()))
                nav.popBackStack()
            }
        }
    }
}

// ======================= ВРАЧ =======================
@Composable
fun DoctorFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val positions by vm.positions.collectAsState()
    var last by remember { mutableStateOf("") }; var first by remember { mutableStateOf("") }
    var middle by remember { mutableStateOf("") }; var spec by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }; var exp by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<Int?>(null) }; var posId by remember { mutableStateOf<Int?>(null) }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getDoctor(id)?.let {
            last = it.lastName; first = it.firstName; middle = it.middleName; spec = it.specialization; phone = it.phoneNumber
            exp = it.workExperience.toString(); category = Categories.indexOf(it.category).takeIf { i -> i >= 0 }; posId = it.idPositions
        }
    }
    val expVal = exp.toIntOrNull()?.takeIf { it in 0..60 }
    FormScreen(if (id == 0) "Новый врач" else "Врач", vm, nav) {
        Field(last, { last = it }, "Фамилия", if (tried && last.isBlank()) REQ else null)
        Field(first, { first = it }, "Имя", if (tried && first.isBlank()) REQ else null)
        Field(middle, { middle = it }, "Отчество", null)
        Field(spec, { spec = it }, "Специализация", if (tried && spec.isBlank()) REQ else null)
        Dropdown("Должность", positions.map { it.id to it.title }, posId, if (tried && posId == null) "Выберите должность" else null) { posId = it }
        Dropdown("Категория", Categories.mapIndexed { i, c -> i to c }, category, if (tried && category == null) "Выберите категорию" else null) { category = it }
        Field(phone, { phone = it }, "Телефон", if (tried && !phone.phoneOk()) "Минимум 10 цифр" else null, KeyboardType.Phone)
        Field(exp, { exp = it }, "Стаж (лет)", if (tried && expVal == null) "Целое число 0–60" else null, KeyboardType.Number)
        PrimaryButton("Сохранить") {
            tried = true
            if (last.isNotBlank() && first.isNotBlank() && spec.isNotBlank() && posId != null && category != null && phone.phoneOk() && expVal != null) {
                vm.saveDoctor(Doctor(id, last.trim(), first.trim(), middle.trim(), spec.trim(), phone.trim(), expVal, Categories[category!!], posId!!))
                nav.popBackStack()
            }
        }
    }
}

// ======================= УСЛУГА =======================
@Composable
fun ServiceFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var title by remember { mutableStateOf("") }; var type by remember { mutableStateOf<Int?>(null) }
    var desc by remember { mutableStateOf("") }; var price by remember { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getService(id)?.let {
            title = it.title; type = ServiceTypes.indexOf(it.type).takeIf { i -> i >= 0 }; desc = it.description; price = it.price.toString()
        }
    }
    val priceVal = price.toMoney()
    FormScreen(if (id == 0) "Новая услуга" else "Услуга", vm, nav) {
        Field(title, { title = it }, "Название", if (tried && title.isBlank()) REQ else null)
        Dropdown("Тип", ServiceTypes.mapIndexed { i, t -> i to t }, type, if (tried && type == null) "Выберите тип" else null) { type = it }
        Field(desc, { desc = it }, "Описание", null, singleLine = false)
        Field(price, { price = it }, "Цена, ₽", if (tried && priceVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
        PrimaryButton("Сохранить") {
            tried = true
            if (title.isNotBlank() && type != null && priceVal != null) {
                vm.saveService(Service(id, title.trim(), ServiceTypes[type!!], desc.trim(), priceVal))
                nav.popBackStack()
            }
        }
    }
}

// ======================= ДОЛЖНОСТЬ =======================
@Composable
fun PositionFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var title by remember { mutableStateOf("") }; var salary by remember { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) { if (id != 0) vm.getPosition(id)?.let { title = it.title; salary = it.salary.toString() } }
    val salaryVal = salary.toMoney()
    FormScreen(if (id == 0) "Новая должность" else "Должность", vm, nav) {
        Field(title, { title = it }, "Название", if (tried && title.isBlank()) REQ else null)
        Field(salary, { salary = it }, "Оклад, ₽", if (tried && salaryVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
        PrimaryButton("Сохранить") {
            tried = true
            if (title.isNotBlank() && salaryVal != null) { vm.savePosition(Position(id, title.trim(), salaryVal)); nav.popBackStack() }
        }
    }
}

// ======================= МЕДИЦИНСКАЯ КАРТА =======================
@Composable
fun RecordFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val patients by vm.patients.collectAsState()
    var patientId by remember { mutableStateOf<Int?>(null) }
    var history by remember { mutableStateOf("") }; var allergies by remember { mutableStateOf("") }
    var chronic by remember { mutableStateOf("") }; var created by remember { mutableStateOf<Long?>(null) }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getRecord(id)?.let { patientId = it.idPatients; history = it.medicalHistory; allergies = it.allergies; chronic = it.chronicDiseases; created = it.creationDate }
        else created = dayStart(System.currentTimeMillis())
    }
    FormScreen(if (id == 0) "Новая карта" else "Мед. карта", vm, nav) {
        Dropdown("Пациент", patients.map { it.id to "${it.lastName} ${it.firstName}" }, patientId, if (tried && patientId == null) "Выберите пациента" else null) { patientId = it }
        DateField("Дата создания", created, if (tried && created == null) REQ else null) { created = it }
        Field(history, { history = it }, "История болезни", if (tried && history.isBlank()) REQ else null, singleLine = false)
        Field(allergies, { allergies = it }, "Аллергии (если нет — оставьте пустым)", null)
        Field(chronic, { chronic = it }, "Хронические заболевания (если нет — оставьте пустым)", null)
        PrimaryButton("Сохранить") {
            tried = true
            if (patientId != null && created != null && history.isNotBlank()) {
                vm.saveRecord(MedicalRecord(id, patientId!!, history.trim(), allergies.trim().ifBlank { "Нет" }, chronic.trim().ifBlank { "Нет" }, created!!))
                nav.popBackStack()
            }
        }
    }
}

// ======================= ПРИЁМ =======================
@Composable
fun AppointmentFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val patients by vm.patients.collectAsState()
    val doctors by vm.doctors.collectAsState()
    val services by vm.services.collectAsState()
    val all by vm.appointments.collectAsState()
    val items by remember(id) { vm.items(id) }.collectAsState(emptyList())
    var patientId by remember { mutableStateOf<Int?>(null) }
    var doctorId by remember { mutableStateOf<Int?>(null) }
    var day by remember { mutableStateOf<Long?>(null) }
    var slot by remember { mutableStateOf<Int?>(null) }
    var status by remember { mutableStateOf<Int?>(0) }
    var tried by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var addItem by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<RenderedItem?>(null) }
    val draft = remember { mutableStateListOf<Pair<Int, Int>>() }   // (id услуги, количество) нового приёма
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        if (id != 0) vm.getAppointment(id)?.let {
            patientId = it.idPatients; doctorId = it.idDoctors; day = it.appointmentDate
            slot = TimeSlots.indexOf(it.appointmentTime).takeIf { i -> i >= 0 }; status = AppointmentStatuses.indexOf(it.status).coerceAtLeast(0)
        }
    }
    val total = if (id == 0) draft.sumOf { (sid, q) -> (services.firstOrNull { it.id == sid }?.price ?: 0.0) * q }
    else all.firstOrNull { it.id == id }?.totalCost ?: 0.0

    FormScreen(if (id == 0) "Новый приём" else "Приём", vm, nav) {
        Dropdown("Пациент", patients.map { it.id to "${it.lastName} ${it.firstName}" }, patientId, if (tried && patientId == null) "Выберите пациента" else null) { patientId = it }
        Dropdown("Врач", doctors.map { it.doctor.id to "${it.doctor.lastName} ${it.doctor.firstName}, ${it.doctor.specialization}" }, doctorId, if (tried && doctorId == null) "Выберите врача" else null) { doctorId = it }
        DateField("Дата приёма", day, if (tried && day == null) REQ else null) { day = it }
        Dropdown("Время", TimeSlots.mapIndexed { i, t -> i to t }, slot, if (tried && slot == null) "Выберите время" else null) { slot = it }
        Dropdown("Статус", AppointmentStatuses.mapIndexed { i, s -> i to s }, status, null) { status = it }

        Spacer(Modifier.height(10.dp))
        Text("Оказанные услуги", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        if (id == 0) {
            draft.forEachIndexed { index, (sid, q) ->
                val s = services.firstOrNull { it.id == sid }
                AppCard(Modifier.padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s?.title ?: "—", fontWeight = FontWeight.SemiBold)
                            Text("$q × ${money(s?.price ?: 0.0)}", color = Muted, fontSize = 13.sp)
                        }
                        IconButton({ draft.removeAt(index) }) { Icon(Icons.Default.Delete, "Убрать", tint = Coral) }
                    }
                }
            }
        } else {
            items.forEach { it ->
                AppCard(Modifier.padding(bottom = 8.dp), onClick = { editItem = it }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(it.title, fontWeight = FontWeight.SemiBold)
                            Text("${it.quantity} × ${money(it.price)}", color = Muted, fontSize = 13.sp)
                        }
                        IconButton({ vm.deleteItem(it.id) }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                    }
                }
            }
        }
        OutlinedButton({ addItem = true }, Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.CircleShape) { Text("Добавить услугу") }
        Text("Стоимость приёма: ${money(total)}", color = Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(
            if (id == 0) "Приём и услуги сохраняются одной транзакцией, стоимость пересчитывают триггеры БД."
            else "Стоимость пересчитывается триггерами при любом изменении услуг.",
            color = Muted, fontSize = 12.sp
        )

        PrimaryButton(if (id == 0) "Оформить приём" else "Сохранить") {
            tried = true
            if (patientId != null && doctorId != null && day != null && slot != null && !saving) {
                val a = Appointment(id, patientId!!, doctorId!!, day!!, TimeSlots[slot!!], AppointmentStatuses[status ?: 0], total)
                saving = true
                scope.launch {
                    val ok = if (id == 0) vm.placeAppointment(a, draft.map { ServiceRendered(idAppointments = 0, idServices = it.first, quantity = it.second) })
                    else vm.saveAppointment(a)
                    saving = false
                    if (ok) nav.popBackStack()
                }
            }
        }
    }

    if (addItem || editItem != null) {
        ServiceDialog(services, editItem, onDismiss = { addItem = false; editItem = null }) { serviceId, qty ->
            if (id == 0) {
                val i = draft.indexOfFirst { it.first == serviceId }
                if (i >= 0) draft[i] = serviceId to (draft[i].second + qty) else draft.add(serviceId to qty)
            } else vm.saveItem(ServiceRendered(editItem?.id ?: 0, id, serviceId, qty))
            addItem = false; editItem = null
        }
    }
}

@Composable
private fun ServiceDialog(services: List<Service>, initial: RenderedItem?, onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var serviceId by remember { mutableStateOf(initial?.idServices) }
    var qty by remember { mutableStateOf(initial?.quantity?.toString() ?: "1") }
    var tried by remember { mutableStateOf(false) }
    val q = qty.toIntOrNull()?.takeIf { it > 0 }
    AlertDialog(
        onDismissRequest = onDismiss, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), containerColor = androidx.compose.ui.graphics.Color.White,
        title = { Text(if (initial == null) "Добавить услугу" else "Изменить услугу", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Dropdown("Услуга", services.map { it.id to "${it.title}, ${money(it.price)}" }, serviceId, if (tried && serviceId == null) "Выберите услугу" else null) { serviceId = it }
                Field(qty, { qty = it }, "Количество", if (tried && q == null) "Целое число > 0" else null, KeyboardType.Number)
            }
        },
        confirmButton = { TextButton({ tried = true; if (serviceId != null && q != null) onSave(serviceId!!, q) }) { Text("Сохранить", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}
