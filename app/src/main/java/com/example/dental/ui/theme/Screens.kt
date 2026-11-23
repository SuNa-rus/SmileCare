@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dental.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dental.data.*

// ======================= ПРИВЕТСТВИЕ =======================
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Blue)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.06f), size.width * 0.55f, Offset(size.width * 0.95f, size.height * 0.06f))
            drawCircle(Color.White.copy(alpha = 0.18f), size.width * 0.30f, Offset(size.width * 0.92f, size.height * 0.12f), style = Stroke(2.dp.toPx()))
            drawCircle(Mint, 6.dp.toPx(), Offset(size.width * 0.80f, size.height * 0.20f))
            drawCircle(BlueDark, size.width * 0.55f, Offset(0f, size.height))
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.padding(top = 72.dp)) {
                Text("Smile", color = Mint, fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                Text("Care.", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Информационная система стоматологической клиники: пациенты, врачи, приёмы и услуги в одном приложении.",
                    color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, lineHeight = 22.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(76.dp).shadow(14.dp, CircleShape, ambientColor = Mint, spotColor = Mint).clip(CircleShape)
                        .background(Mint).clickable(onClick = onStart), Alignment.Center
                ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Начать", tint = Blue, modifier = Modifier.size(34.dp)) }
                Spacer(Modifier.width(16.dp))
                Text("Начать\nработу", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

// ======================= ГЛАВНАЯ =======================
@Composable
fun HomeScreen(vm: MainViewModel, nav: NavController) {
    val appts by vm.appointments.collectAsState()
    val patients by vm.patients.collectAsState()
    val doctors by vm.doctors.collectAsState()
    val services by vm.services.collectAsState()
    val positions by vm.positions.collectAsState()
    val records by vm.records.collectAsState()
    val todayStart = remember { dayStart(System.currentTimeMillis()) }
    val upcoming = remember(appts) { appts.filter { it.status == "Запланирован" && it.appointmentDate >= todayStart }.take(6) }
    val todayCount = remember(appts) { appts.count { it.appointmentDate == todayStart && it.status != "Отменён" } }
    val income = remember(appts) { appts.filter { it.status == "Завершён" }.sumOf { it.totalCost } }

    Column(Modifier.fillMaxSize().background(BgColor).verticalScroll(rememberScrollState()).statusBarsPadding().padding(bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 16.dp, 20.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(Blue), Alignment.Center) {
                Icon(Icons.Default.MedicalServices, null, tint = Color.White)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Добро пожаловать,", color = Muted, fontSize = 13.sp)
                Text("SmileCare", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.size(44.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White).clickable { nav.navigate("appointments") }, Alignment.Center) {
                Icon(Icons.Default.Notifications, "Приёмы", tint = Blue)
                if (todayCount > 0) Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp).clip(CircleShape).background(Coral), Alignment.Center
                ) { Text("$todayCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }

        SectionTitle("Ближайшие приёмы") { nav.navigate("appointments") }
        if (upcoming.isEmpty()) Text("Запланированных приёмов нет", color = Muted, modifier = Modifier.padding(horizontal = 20.dp))
        else LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(upcoming, key = { _, a -> a.id }) { i, a -> UpcomingCard(a, i == 0) { nav.navigate("appointment_edit/${a.id}") } }
        }

        SectionTitle("Пациенты") { nav.navigate("patients") }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Box(Modifier.size(56.dp).shadow(8.dp, CircleShape, ambientColor = MintDark, spotColor = MintDark).clip(CircleShape)
                    .background(Mint).clickable { nav.navigate("patient_edit/0") }, Alignment.Center) { Icon(Icons.Default.Add, "Новый пациент", tint = Ink) }
            }
            items(patients.take(8), key = { it.id }) { p ->
                Box(Modifier.clickable { nav.navigate("patient_edit/${p.id}") }) { Avatar("${p.lastName} ${p.firstName}", 56.dp) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Врачи", doctors.size, Icons.Default.MedicalServices, Modifier.weight(1f)) { nav.navigate("doctors") }
                StatTile("Услуги", services.size, Icons.Default.Healing, Modifier.weight(1f)) { nav.navigate("services") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Должности", positions.size, Icons.Default.Work, Modifier.weight(1f)) { nav.navigate("positions") }
                StatTile("Мед. карты", records.size, Icons.Default.Description, Modifier.weight(1f)) { nav.navigate("records") }
            }
            AppCard(color = Blue, onClick = { nav.navigate("appointments") }, padding = PaddingValues(20.dp)) {
                Text("Выручка по завершённым приёмам", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Text(money(income), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Приёмов сегодня: $todayCount", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(CircleShape).background(Mint).padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp, 18.dp, 20.dp, 12.dp), verticalAlignment = Alignment.CenterVertically
) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Blue)
}

@Composable
private fun UpcomingCard(a: AppointmentDetails, primary: Boolean, onClick: () -> Unit) {
    val fg = if (primary) Color.White else Ink
    Column(
        Modifier.width(216.dp).clip(RoundedCornerShape(28.dp)).background(if (primary) Blue else Sky)
            .clickable(onClick = onClick).padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(a.patientName, 44.dp)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(a.appointmentTime, color = if (primary) Mint else Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(fmtDay(a.appointmentDate), color = fg.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(a.patientName, color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${a.specialization}, ${a.doctorName}", color = fg.copy(alpha = 0.75f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(14.dp))
        Text("Открыть", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            modifier = Modifier.clip(CircleShape).background(if (primary) Mint else Color.White).padding(horizontal = 18.dp, vertical = 8.dp))
    }
}

@Composable
private fun StatTile(title: String, count: Int, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) =
    AppCard(modifier, onClick = onClick) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Sky), Alignment.Center) { Icon(icon, null, tint = Blue) }
        Spacer(Modifier.height(10.dp))
        Text("$count", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(title, color = Muted, fontSize = 13.sp)
    }

// ======================= ПРИЁМЫ =======================
@Composable
fun AppointmentsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.appointments.collectAsState()
    var q by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q, status) {
        all.filter { a ->
            (status == null || a.status == status) &&
                (q.isBlank() || "${a.patientName} ${a.doctorName} ${a.specialization}".contains(q.trim(), ignoreCase = true))
        }
    }
    AppScreen("Приёмы", vm, "${all.size} в базе", null, "Новый приём" to { nav.navigate("appointment_edit/0") }) {
        SearchBar(q, { q = it }, "Пациент, врач или специализация")
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("Все", status == null) { status = null }
            AppointmentStatuses.forEach { s -> FilterPill(s, status == s) { status = s } }
        }
        ListBody(list, { it.id }, if (all.isEmpty()) "Приёмов пока нет" else "Ничего не найдено") { a ->
            val c = statusColor(a.status)
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("appointment_edit/${a.id}") }, padding = PaddingValues(0.dp)) {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(6.dp).fillMaxHeight().background(c))
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(a.appointmentTime, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Blue)
                                Text(fmtDay(a.appointmentDate), color = Muted, fontSize = 13.sp)
                            }
                            StatusPill(a.status)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(a.patientName, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(a.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${a.specialization}, ${a.doctorName}", color = Muted, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(money(a.totalCost), color = Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                            IconButton({ pending = { vm.deleteAppointment(a.id) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                        }
                    }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить приём? Оказанные услуги этого приёма тоже будут удалены.", { action(); pending = null }, { pending = null }) }
}

// ======================= ПАЦИЕНТЫ =======================
@Composable
fun PatientsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.patients.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) {
        all.filter { q.isBlank() || "${it.lastName} ${it.firstName} ${it.middleName} ${it.phoneNumber} ${it.policyNumber}".contains(q.trim(), ignoreCase = true) }
    }
    AppScreen("Пациенты", vm, "${all.size} в базе", null, "Новый пациент" to { nav.navigate("patient_edit/0") }) {
        SearchBar(q, { q = it }, "ФИО, телефон или полис")
        ListBody(list, { it.id }, if (all.isEmpty()) "Пациентов пока нет" else "Ничего не найдено") { p ->
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("patient_edit/${p.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar("${p.lastName} ${p.firstName}", 54.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${p.lastName} ${p.firstName} ${p.middleName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${p.gender}, ${ageOf(p.dateOfBirth)} лет", color = Muted, fontSize = 13.sp)
                        Text(p.phoneNumber, color = Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Полис: ${p.policyNumber}", color = Muted, fontSize = 12.sp)
                    }
                    IconButton({ pending = { vm.deletePatient(p) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action ->
        ConfirmDialog("Удалить пациента? Его медицинские карты тоже будут удалены. Если у пациента есть приёмы, удаление будет отклонено.", { action(); pending = null }, { pending = null })
    }
}

// ======================= ВРАЧИ =======================
@Composable
fun DoctorsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.doctors.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) {
        all.filter { q.isBlank() || "${it.doctor.lastName} ${it.doctor.firstName} ${it.doctor.specialization} ${it.positionTitle}".contains(q.trim(), ignoreCase = true) }
    }

    // В параметр onBack (третий аргумент) передана функция возврата назад:
    AppScreen("Врачи", vm, "${all.size} в штате", onBack = { nav.popBackStack() }, fab = "Новый врач" to { nav.navigate("doctor_edit/0") }) {
        SearchBar(q, { q = it }, "ФИО, специализация или должность")
        ListBody(list, { it.doctor.id }, if (all.isEmpty()) "Врачей пока нет" else "Ничего не найдено") { item ->
            val d = item.doctor
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("doctor_edit/${d.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar("${d.lastName} ${d.firstName}", 54.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${d.lastName} ${d.firstName} ${d.middleName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(d.specialization, color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${item.positionTitle}, стаж ${d.workExperience} лет", color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        TagPill(d.category, MintDark)
                    }
                    IconButton({ pending = { vm.deleteDoctor(d) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить врача? Если у врача есть приёмы, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= УСЛУГИ =======================
@Composable
fun ServicesScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.services.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) { all.filter { q.isBlank() || "${it.title} ${it.type} ${it.description}".contains(q.trim(), ignoreCase = true) } }

    AppScreen("Услуги", vm, "${all.size} в прайсе", onBack = { nav.popBackStack() }, fab = "Новая услуга" to { nav.navigate("service_edit/0") }) {
        SearchBar(q, { q = it }, "Название или тип услуги")
        ListBody(list, { it.id }, if (all.isEmpty()) "Услуг пока нет" else "Ничего не найдено") { s ->
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("service_edit/${s.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        TagPill(s.type)
                        Spacer(Modifier.height(6.dp))
                        Text(s.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(s.description, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(money(s.price), color = Blue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    IconButton({ pending = { vm.deleteService(s) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить услугу? Если она есть в приёмах, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= ДОЛЖНОСТИ =======================
@Composable
fun PositionsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.positions.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) { all.filter { q.isBlank() || it.title.contains(q.trim(), ignoreCase = true) } }
    AppScreen("Должности", vm, "${all.size} в справочнике", { nav.popBackStack() }, "Новая должность" to { nav.navigate("position_edit/0") }) {
        SearchBar(q, { q = it }, "Название должности")
        ListBody(list, { it.id }, if (all.isEmpty()) "Должностей пока нет" else "Ничего не найдено") { p ->
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("position_edit/${p.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Оклад", color = Muted, fontSize = 12.sp)
                        Text(money(p.salary), color = Blue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    IconButton({ pending = { vm.deletePosition(p) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить должность? Если она назначена врачам, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= МЕДИЦИНСКИЕ КАРТЫ =======================
@Composable
fun RecordsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.records.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) {
        all.filter { q.isBlank() || "${it.patientName} ${it.record.medicalHistory} ${it.record.allergies} ${it.record.chronicDiseases}".contains(q.trim(), ignoreCase = true) }
    }
    AppScreen("Мед. карты", vm, "${all.size} в базе", { nav.popBackStack() }, "Новая карта" to { nav.navigate("record_edit/0") }) {
        SearchBar(q, { q = it }, "Пациент, диагноз или аллергия")
        ListBody(list, { it.record.id }, if (all.isEmpty()) "Карт пока нет" else "Ничего не найдено") { item ->
            val r = item.record
            AppCard(Modifier.animateItem(), onClick = { nav.navigate("record_edit/${r.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(item.patientName, 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Создана ${fmtDate(r.creationDate)}", color = Muted, fontSize = 12.sp)
                    }
                    IconButton({ pending = { vm.deleteRecord(r) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
                Spacer(Modifier.height(8.dp))
                Text(r.medicalHistory, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (r.allergies.isNotBlank() && r.allergies != "Нет") TagPill("Аллергия: ${r.allergies}", Coral)
                    if (r.chronicDiseases.isNotBlank() && r.chronicDiseases != "Нет") TagPill(r.chronicDiseases, Amber)
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить медицинскую карту?", { action(); pending = null }, { pending = null }) }
}
