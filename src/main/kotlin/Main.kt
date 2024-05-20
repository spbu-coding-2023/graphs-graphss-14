import androidx.compose.desktop.ui.tooling.preview.Preview
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

@Composable
@Preview
fun app() {
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }

    var circles by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }
    var lines by remember { mutableStateOf(listOf<Pair<Pair<Float, Float>, Pair<Float, Float>>>()) }
    var selectedCircle by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var selectedCircleToMove by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var startConnectingPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var endConnectingPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
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
                    if (selectedOption == "Создать узлы") {
                        circles += Pair(offset.x, offset.y)
                        nodeCounter += 1
                    } else if (selectedOption == "Редактировать") {
                        val hitCircle = circles.find { (x, y) ->
                            val distance = sqrt(((offset.x) - x).pow(2) + ((offset.y) - y).pow(2))
                            distance <= circleRadius.value
                        }
                        selectedCircle = hitCircle
                    } else if (selectedOption == "Перемещение") {
                        val hitCircle = circles.find { (x, y) ->
                            val distance = sqrt(((offset.x) - x).pow(2) + ((offset.y) - y).pow(2))
                            distance <= circleRadius.value
                        }
                        selectedCircleToMove = hitCircle
                    } else if (selectedOption == "Соединить узлы") {
                        val hitCircle = circles.find { (x, y) ->
                            val distance = sqrt(((offset.x) - x).pow(2) + ((offset.y) - y).pow(2))
                            distance <= circleRadius.value
                        }
                        if (hitCircle != null) {
                            if (startConnectingPoint == null) {
                                startConnectingPoint = hitCircle
                            } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                endConnectingPoint = hitCircle
                                lines += Pair(startConnectingPoint!!, endConnectingPoint!!)
                                startConnectingPoint = null
                                endConnectingPoint = null
                            } else {
                                startConnectingPoint = null
                                endConnectingPoint = null
                            }
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
            .pointerInput(Unit){
                detectDragGestures(
                    onDragStart = { offset ->
                        val hitCircle = circles.find { (x, y) ->
                            val distance = sqrt(((offset.x) - x).pow(2) + ((offset.y) - y).pow(2))
                            distance <= circleRadius.value
                        }
                        selectedCircleToMove = hitCircle
                    },
                    onDragEnd = {
                        selectedCircleToMove = null
                    },
                    onDragCancel = {
                        selectedCircleToMove = null
                    },
                    onDrag = { change, dragAmount ->
                        if (selectedCircleToMove != null && selectedOption == "Перемещение") {
                            print(selectedCircleToMove)
                            print(Pair(dragAmount.x, dragAmount.y))
                            val newX = selectedCircleToMove!!.first + dragAmount.x
                            val newY = selectedCircleToMove!!.second + dragAmount.y
                            circles = circles.map { if (it == selectedCircleToMove) {Pair(newX, newY)} else {it} }
                            println(circles)
                            lines = lines.map { (start, end) ->
                                if (start == selectedCircleToMove) Pair(Pair(newX, newY), end) else if (end == selectedCircleToMove) Pair(start, Pair(newX, newY)) else Pair(start, end)
                            }
                            selectedCircleToMove = Pair(newX,newY)
                            dragOffset += change.positionChange()
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
            lines.forEach { (start, end) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(start.first - windowWidth.value / 2 + dragOffset.x, start.second - windowHeight.value / 2 + dragOffset.y),
                        end = Offset(end.first - windowWidth.value / 2 + dragOffset.x, end.second - windowHeight.value / 2 + dragOffset.y),
                        strokeWidth = 2f
                    )
                }
            }
            circles.forEach { (x, y) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawCircle(
                        color = Color.Red,
                        radius = circleRadius.value,
                        center = Offset(x - windowWidth.value / 2 + dragOffset.x, y - windowHeight.value / 2 + dragOffset.y),
                        style = Fill
                    )
                    if (selectedCircle == Pair(x,y) || selectedCircleToMove == Pair(x,y) || startConnectingPoint == Pair(x,y)){
                        drawCircle(
                            color = Color.Blue,
                            radius = circleRadius.value + 1,
                            center = Offset(x - windowWidth.value / 2 + dragOffset.x, y - windowHeight.value / 2 + dragOffset.y),
                            style = Stroke(width = 2.dp.toPx()) // Ширина окантовки в пикселях
                        )
                    }
                }
            }

            // Отображаем всплывающее окно, если круг выбран
            selectedCircle?.let { (x, y) ->
                DialogWindow(onCloseRequest = { selectedCircle = null },
                    state = DialogState(position = WindowPosition(Dp(x), Dp(y))),
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