import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlin.math.pow
import kotlin.math.sqrt

fun findInMap(dict: MutableMap<Int, Pair<Float, Float>>, circleRadius: Dp, offset: Offset): Int? {
    for ((key, value) in dict) {
        val distance = sqrt(((offset.x) - value.first).pow(2) + ((offset.y) - value.second).pow(2))
        if (distance <= circleRadius.value) {
            return key
        }
    }
    return null
}

@Composable
fun app() {
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }

    var circles by remember { mutableStateOf(mutableMapOf<Int, Pair<Float, Float>>()) }
    var lines by remember { mutableStateOf(mutableMapOf<Pair<Int, Int>, Pair<Pair<Float, Float>, Pair<Float, Float>>>()) }

    var selectedCircle by remember { mutableStateOf<Int?>(null) }
    var selectedCircleToMove by remember { mutableStateOf<Int?>(null) }
    var startConnectingPoint by remember { mutableStateOf<Int?>(null) }
    var endConnectingPoint by remember { mutableStateOf<Int?>(null) }
    val circleRadius: Dp = 20.dp

    var selectedOption by remember { mutableStateOf("Создать узлы") }
    var nodeCounter by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RadioButton(
                selected = selectedOption == "Создать узлы",
                onClick = { selectedOption = "Создать узлы" }
            )
            Text("Создать узлы")

            RadioButton(
                selected = selectedOption == "Соединить узлы",
                onClick = { selectedOption = "Соединить узлы" }
            )
            Text("Соединить узлы")

            RadioButton(
                selected = selectedOption == "Перемещение",
                onClick = { selectedOption = "Перемещение" }
            )
            Text("Перемещение")

            RadioButton(
                selected = selectedOption == "Редактировать",
                onClick = { selectedOption = "Редактировать" }
            )
            Text("Редактировать")
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    when (selectedOption) {
                        "Создать узлы" -> {
                            circles = circles.toMutableMap().apply { this[nodeCounter] = Pair(offset.x, offset.y) }
                            nodeCounter += 1
                        }
                        "Соединить узлы" -> {
                            val hitCircle = findInMap(circles, circleRadius, offset)
                            if (hitCircle != null) {
                                if (startConnectingPoint == null) {
                                    startConnectingPoint = hitCircle
                                } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                    endConnectingPoint = hitCircle
                                    lines = lines.toMutableMap().apply {
                                        this[Pair(startConnectingPoint!!, endConnectingPoint!!)] = Pair(circles[startConnectingPoint]!!, circles[endConnectingPoint]!!)
                                    }
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                } else {
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                }
                            }
                        }


                        "Редактировать" -> {
                            selectedCircle = findInMap(circles, circleRadius, offset)
                        }

                        "Перемещение" -> {
                            selectedCircleToMove = findInMap(circles, circleRadius, offset)
                        }
                    }
                })
            }.onSizeChanged { newSize ->
                val temp = with(density) { DpSize(newSize.width.toDp(), newSize.height.toDp()) }
                if (temp != windowSize) {
                    windowSize = temp
                    windowWidth = with(density) { newSize.width.toDp() }
                    windowHeight = with(density) { newSize.height.toDp() }
                }
            }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedCircleToMove = findInMap(circles, circleRadius, offset)
                        },
                        onDragEnd = {
                            selectedCircleToMove = null
                        },
                        onDragCancel = {
                            selectedCircleToMove = null
                        },
                        onDrag = { change, dragAmount ->
                            selectedCircleToMove?.let { circleId ->
                                val newX = circles[circleId]!!.first + dragAmount.x
                                val newY = circles[circleId]!!.second + dragAmount.y
                                circles = circles.toMutableMap().apply { this[circleId] = Pair(newX, newY) }
                                dragOffset += change.positionChange()

                                // Update connected lines
                                lines = lines.toMutableMap().apply {
                                    for ((key, value) in this) {
                                        if (key.first == circleId) {
                                            this[key] = Pair(Pair(newX, newY), value.second)
                                        } else if (key.second == circleId) {
                                            this[key] = Pair(value.first, Pair(newX, newY))
                                        }
                                    }
                                }
                            }
                        }
                    )
            }.onSizeChanged { newSize ->
                val temp = with(density) { DpSize(newSize.width.toDp(), newSize.height.toDp()) }
                if (temp != windowSize) {
                    windowSize = temp
                    windowWidth = with(density) { newSize.width.toDp() }
                    windowHeight = with(density) { newSize.height.toDp() }
                }
            })
        {
            lines.forEach { (_, value) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(value.first.first - windowWidth.value / 2 , value.first.second - windowHeight.value / 2 ),
                        end = Offset(value.second.first - windowWidth.value / 2, value.second.second - windowHeight.value / 2),
                        strokeWidth = 2f
                    )
                }
            }

            circles.forEach { (key, value) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawCircle(
                        color = Color.Red,
                        radius = circleRadius.value,
                        center = Offset(value.first - windowWidth.value / 2, value.second - windowHeight.value / 2),
                        style = Fill
                    )
                    if (selectedCircle == key || selectedCircleToMove == key || startConnectingPoint == key) {
                        drawCircle(
                            color = Color.Blue,
                            radius = circleRadius.value + 1,
                            center = Offset(value.first - windowWidth.value / 2, value.second - windowHeight.value / 2),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

// Отображаем всплывающее окно, если круг выбран
            selectedCircle?.let { key ->
                DialogWindow(onCloseRequest = { selectedCircle = null },
                    state = DialogState(position = WindowPosition(Dp(circles[key]!!.first), Dp(circles[key]!!.second))),
                    content = {
                        Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Всплывающее окно")
                        }
                    }
                )
            }
        }
    }
}



fun main() = application {
    Window(onCloseRequest = ::exitApplication, state = rememberWindowState()) {
        app()
    }
}