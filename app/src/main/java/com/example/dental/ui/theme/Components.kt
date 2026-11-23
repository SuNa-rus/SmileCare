@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dental.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// ---------- форматирование и даты ----------
private val ru = Locale("ru")
fun fmtDate(ms: Long): String = SimpleDateFormat("d MMM yyyy", ru).format(Date(ms))
fun fmtDay(ms: Long): String = SimpleDateFormat("EEE, d MMM", ru).format(Date(ms))
fun money(v: Double): String = String.format(ru, "%,.0f ₽", v)
fun initialsOf(fullName: String): String {
    val p = fullName.trim().split(" ")
    return "${p.getOrNull(0)?.firstOrNull() ?: ' '}${p.getOrNull(1)?.firstOrNull() ?: ' '}".uppercase()
}
fun dayStart(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis
/** DatePicker работает в UTC, а в БД даты хранятся по локальному времени. */
fun pickerToLocal(utcMs: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
    return Calendar.getInstance().apply { clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH)) }.timeInMillis
}
fun localToPicker(ms: Long): Long {
    val l = Calendar.getInstance().apply { timeInMillis = ms }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH)) }.timeInMillis
}
fun ageOf(dobMs: Long): Int {
    val b = Calendar.getInstance().apply { timeInMillis = dobMs }
    val n = Calendar.getInstance()
    var a = n.get(Calendar.YEAR) - b.get(Calendar.YEAR)
    if (n.get(Calendar.DAY_OF_YEAR) < b.get(Calendar.DAY_OF_YEAR)) a--
    return a
}

val AppointmentStatuses = listOf("Запланирован", "Завершён", "Отменён")
fun statusColor(s: String) = when (s) { "Запланирован" -> Blue; "Завершён" -> MintDark; "Отменён" -> Coral; else -> Muted }
val TimeSlots = (9..19).flatMap { h -> listOf(0, 30).map { m -> "%02d:%02d".format(h, m) } }

private val avatarColors = listOf(Blue, MintDark, Violet, Amber, Coral, Color(0xFF00A3D9))

// ---------- карточки и кнопки ----------
@Composable
fun AppCard(
    modifier: Modifier = Modifier, color: Color = Color.White, onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp), content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier.fillMaxWidth().shadow(6.dp, shape, ambientColor = Blue.copy(alpha = 0.25f), spotColor = Blue.copy(alpha = 0.25f))
            .clip(shape).background(color).let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(padding),
        content = content
    )
}

@Composable
fun Avatar(fullName: String, size: Dp = 48.dp) {
    val c = avatarColors[abs(fullName.hashCode()) % avatarColors.size]
    Box(Modifier.size(size).clip(CircleShape).background(c.copy(alpha = 0.15f)), Alignment.Center) {
        Text(initialsOf(fullName), color = c, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.34f).sp)
    }
}

@Composable
fun RoundIconButton(icon: ImageVector, desc: String, onClick: () -> Unit) = Box(
    Modifier.size(44.dp).shadow(4.dp, CircleShape, ambientColor = Blue.copy(alpha = 0.3f)).clip(CircleShape)
        .background(Color.White).clickable(onClick = onClick), Alignment.Center
) { Icon(icon, desc, tint = Blue) }

@Composable
fun StatusPill(status: String) {
    val c = statusColor(status)
    Text(status, color = c, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(CircleShape).background(c.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 5.dp))
}

@Composable
fun TagPill(text: String, color: Color = Blue) =
    Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(CircleShape).background(color.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 5.dp))

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) = Text(
    text, color = if (selected) Color.White else Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
    modifier = Modifier.clip(CircleShape).background(if (selected) Blue else Color.White).clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 9.dp)
)

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit) = Box(
    Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp)
        .shadow(8.dp, CircleShape, ambientColor = Blue, spotColor = Blue).clip(CircleShape)
        .background(Blue).clickable(onClick = onClick), Alignment.Center
) { Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }

@Composable
fun PillFab(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) = Row(
    modifier.shadow(10.dp, CircleShape, ambientColor = MintDark, spotColor = MintDark).clip(CircleShape)
        .background(Mint).clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(Icons.Default.Add, null, tint = Ink); Spacer(Modifier.width(8.dp))
    Text(label, color = Ink, fontWeight = FontWeight.Bold)
}

// ---------- каркас экрана ----------
@Composable
fun ScreenHeader(title: String, subtitle: String?, onBack: (() -> Unit)?) = Row(
    Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp, 12.dp, 20.dp, 8.dp), verticalAlignment = Alignment.CenterVertically
) {
    if (onBack != null) { RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Назад", onBack); Spacer(Modifier.width(14.dp)) }
    Column {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun AppScreen(
    title: String, vm: MainViewModel, subtitle: String? = null, onBack: (() -> Unit)? = null,
    fab: Pair<String, () -> Unit>? = null, content: @Composable ColumnScope.() -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    Box(Modifier.fillMaxSize().background(BgColor)) {
        Column(Modifier.fillMaxSize()) { ScreenHeader(title, subtitle, onBack); content() }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
        if (fab != null) PillFab(fab.first, fab.second, Modifier.align(Alignment.BottomEnd).padding(20.dp))
    }
}

@Composable
fun BottomBar(current: String?, onSelect: (String) -> Unit) {
    val items = listOf(
        "home" to Icons.Default.Home, "appointments" to Icons.Default.CalendarMonth, "patients" to Icons.Default.Groups,
        "doctors" to Icons.Default.MedicalServices, "services" to Icons.Default.Healing
    )
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp, 4.dp, 16.dp, 12.dp)) {
        Row(
            Modifier.fillMaxWidth().shadow(14.dp, RoundedCornerShape(32.dp), ambientColor = Blue, spotColor = Blue)
                .clip(RoundedCornerShape(32.dp)).background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, icon) ->
                val sel = route == current
                Box(
                    Modifier.size(50.dp).clip(CircleShape).background(if (sel) Mint else Color.Transparent).clickable { onSelect(route) },
                    Alignment.Center
                ) { Icon(icon, route, tint = if (sel) Ink else Blue.copy(alpha = 0.65f)) }
            }
        }
    }
}

// ---------- поля ввода ----------
@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Blue, unfocusedBorderColor = Color(0xFFC5D5EA),
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedLabelColor = Blue, unfocusedLabelColor = Muted, cursorColor = Blue,
    errorBorderColor = Coral, errorLabelColor = Coral, errorSupportingTextColor = Coral, errorContainerColor = Color.White
)

@Composable
fun SearchBar(value: String, onChange: (String) -> Unit, hint: String) = OutlinedTextField(
    value, onChange, placeholder = { Text(hint, color = Muted) }, singleLine = true,
    leadingIcon = { Icon(Icons.Default.Search, null, tint = Blue) },
    trailingIcon = { if (value.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Очистить") } },
    shape = CircleShape, colors = fieldColors(), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
)

@Composable
fun Field(value: String, onChange: (String) -> Unit, label: String, error: String?, keyboard: KeyboardType = KeyboardType.Text, singleLine: Boolean = true) =
    OutlinedTextField(
        value, onChange, label = { Text(label) }, isError = error != null, singleLine = singleLine,
        supportingText = { error?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
    )

@Composable
fun Dropdown(label: String, options: List<Pair<Int, String>>, selectedId: Int?, error: String?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selectedId }?.second ?: "", onValueChange = {}, readOnly = true,
            label = { Text(label) }, isError = error != null, supportingText = { error?.let { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { DropdownMenuItem({ Text(it.second) }, { onSelect(it.first); expanded = false }) }
        }
    }
}

@Composable
fun DateField(label: String, millis: Long?, error: String?, onPick: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = millis?.let { fmtDate(it) } ?: "", onValueChange = {}, readOnly = true, label = { Text(label) },
            isError = error != null, supportingText = { error?.let { Text(it) } },
            trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = Blue) },
            shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable { show = true })
    }
    if (show) {
        val st = rememberDatePickerState(initialSelectedDateMillis = millis?.let(::localToPicker))
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = { TextButton({ st.selectedDateMillis?.let { onPick(pickerToLocal(it)) }; show = false }) { Text("Готово") } },
            dismissButton = { TextButton({ show = false }) { Text("Отмена") } }
        ) { DatePicker(st) }
    }
}

@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp), containerColor = Color.White,
    title = { Text("Удаление", fontWeight = FontWeight.Bold) }, text = { Text(text, color = Muted) },
    confirmButton = { TextButton(onConfirm) { Text("Удалить", color = Coral, fontWeight = FontWeight.Bold) } },
    dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
)

@Composable
fun <T> ListBody(data: List<T>, key: (T) -> Any, empty: String, row: @Composable LazyItemScope.(T) -> Unit) {
    if (data.isEmpty()) Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(empty, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Измените запрос или добавьте запись кнопкой внизу", color = Muted, fontSize = 14.sp)
        }
    } else LazyColumn(
        contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
    ) { items(data, key = key) { row(this, it) } }
}
