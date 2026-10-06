package com.example.helloapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.helloapp.ui.theme.HelloAppTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloAppTheme {
                Calendar()
            }
        }
    }
}
@Composable
fun Calendar() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    // настройки: пользователь сам вводит свой график
    var workText by remember { mutableStateOf("2") }
    var restText by remember { mutableStateOf("2") }
    val start = LocalDate.of(2026, 10, 1)
    // ручные изменения дней: дата -> рабочий (true) или выходной (false)
    var overrides by remember { mutableStateOf(mapOf<LocalDate, Boolean>()) }
    // день, для которого открыт диалог
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }
    val daysList = ArrayList<LocalDate?>()
    val workList = ArrayList<Boolean>()
    // пустые клетки перед первым числом месяца
    var emptyDays = currentMonth.atDay(1).dayOfWeek.value - 1
    while (emptyDays > 0) {
        daysList.add(null)
        workList.add(false)
        emptyDays = emptyDays - 1
    }
    // считаем каждый день месяца
    val workDays = workText.toIntOrNull() ?: 2
    val restDays = restText.toIntOrNull() ?: 2
    var day = 1
    while (day <= currentMonth.lengthOfMonth()) {
        val date = currentMonth.atDay(day)
        daysList.add(date)
        val diff = ChronoUnit.DAYS.between(start, date)
        val byCycle = diff >= 0 && workDays + restDays > 0 && diff % (workDays + restDays) < workDays
        // ручное изменение важнее расчёта по циклу
        workList.add(overrides[date] ?: byCycle)
        day = day + 1
    }
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Календарь смен", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = workText, onValueChange = { workText = it },
            label = { Text("Рабочих дней") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = restText, onValueChange = { restText = it },
            label = { Text("Выходных дней") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        // переключение месяцев
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Text("<<") }
            Text(currentMonth.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Text(">>") }
        }
        Spacer(modifier = Modifier.height(10.dp))
        val week = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        Row(modifier = Modifier.fillMaxWidth()) {
            for (name in week) {
                Text(name, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
            items(daysList.size) { index ->
                val date = daysList[index]
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val ov = overrides[date]
                    var color = if (workList[index]) Color(0xFFBBDEFB) else Color(0xFFE0E0E0)
                    if (ov != null) color = if (ov) Color(0xFF81C784) else Color(0xFFE57373)
                    Box(
                        modifier = Modifier.aspectRatio(1f).padding(2.dp)
                            .background(color, RoundedCornerShape(8.dp))
                            .clickable { selectedDay = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(date.dayOfMonth.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    // диалог настройки дня
    val d = selectedDay
    if (d != null) {
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text("Настройка дня") },
            text = { Text("Дата: " + d.toString()) },
            confirmButton = {
                TextButton(onClick = {
                    overrides = overrides + (d to true)
                    selectedDay = null
                }) { Text("Рабочий") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        overrides = overrides + (d to false)
                        selectedDay = null
                    }) { Text("Выходной") }
                    TextButton(onClick = {
                        overrides = overrides - d
                        selectedDay = null
                    }) { Text("Сбросить") }
                }
            }
        )
    }
}