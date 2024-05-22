import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

//function to find in Map key by the value
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
    var windowHeight by remember { mutableStateOf(windowState.size.height) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var circles by remember { mutableStateOf(mutableMapOf<Int, Pair<Float, Float>>()) }
    var lines by remember { mutableStateOf(mutableMapOf<Pair<Int, Int>, Pair<Pair<Float, Float>, Pair<Float, Float>>>()) }

    var selectedCircle by remember { mutableStateOf<Int?>(null) }
    var selectedCircleToMove by remember { mutableStateOf<Int?>(null) }
    var startConnectingPoint by remember { mutableStateOf<Int?>(null) }
    var endConnectingPoint by remember { mutableStateOf<Int?>(null) }
    val circleRadius: Dp = 20.dp

    var expanded by remember { mutableStateOf(false) }
    var additionalOptionsGroup1 by remember { mutableStateOf(false) }
    var additionalOptionsGroup2 by remember { mutableStateOf(false) }
    var additionalOptionsGroup3 by remember { mutableStateOf(false) }
    var openSettings by remember { mutableStateOf(false) }

    var selectedOption by remember { mutableStateOf(1) }
    var nodeCounter by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                expanded = true
                additionalOptionsGroup1 = false
                additionalOptionsGroup2 = false
                additionalOptionsGroup3 = false
            }) {
                Image(
                    painter = painterResource("img/settings.png"), // Замените на путь к вашему изображению
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp) // Размер изображения
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(onClick = {
                    println("Option 1 clicked")
                    additionalOptionsGroup1 = true
                    additionalOptionsGroup2 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Option 1")
                }
                if (additionalOptionsGroup1) {
                    DropdownMenuItem(onClick = {
                        //tyt raskladka norm doljna bit
                        circles.forEach{(key, _) ->
                            circles[key] = Pair(Random.nextInt(0, windowWidth.value.toInt()).toFloat(),
                                Random.nextInt(0, windowHeight.value.toInt()).toFloat())
                        }
                        lines.forEach{(key, _) ->
                            lines[key] = Pair(circles[key.first]!!, circles[key.second]!!)
                        }
                        //tyt raskladka norm doljna bit
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Разложить граф")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Additional Option 2")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Additional Option 3")
                    }
                }
                DropdownMenuItem(onClick = {
                    println("Option 2 clicked")
                    additionalOptionsGroup2 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Option 2")
                }
                if (additionalOptionsGroup2) {
                    DropdownMenuItem(onClick = {
                        println("Additional Option 1 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 1")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 2")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 3")
                    }
                }
                DropdownMenuItem(onClick = {
                    println("Option 3 clicked")
                    additionalOptionsGroup3 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup2 = false
                }) {
                    Text("Option 3")
                }
                if (additionalOptionsGroup3) {
                    DropdownMenuItem(onClick = {
                        println("Additional Option 1 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 1")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 2")
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 3")
                    }
                }
                DropdownMenuItem(onClick = {
                    println("settings")
                    openSettings = true
                }) {
                    Text("settings")
                }
                if (openSettings){
                    DialogWindow(onCloseRequest = { openSettings = false },
                        state = DialogState(position = WindowPosition(200.dp, 200.dp)),
                        content = {
                            Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Всплывающее окно")
                            }
                        }
                    )
                }
            }
            RadioButton(
                selected = selectedOption == 1,
                onClick = { selectedOption = 1 }
            )
            Text("Создать узлы", modifier = Modifier.align(Alignment.CenterVertically))

            RadioButton(
                selected = selectedOption == 2,
                onClick = { selectedOption = 2 }
            )
            Text("Соединить узлы", modifier = Modifier.align(Alignment.CenterVertically))

            RadioButton(
                selected = selectedOption == 3,
                onClick = { selectedOption = 3 }
            )
            Text("Перемещение", modifier = Modifier.align(Alignment.CenterVertically))

            RadioButton(
                selected = selectedOption == 4,
                onClick = { selectedOption = 4 }
            )
            Text("Редактировать", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    when (selectedOption) {
                        1 -> {
                            circles = circles.toMutableMap().apply { this[nodeCounter] = Pair(offset.x, offset.y) }
                            nodeCounter += 1
                        }

                        2 -> {
                            val hitCircle = findInMap(circles, circleRadius, offset)
                            if (hitCircle != null) {
                                if (startConnectingPoint == null) {
                                    startConnectingPoint = hitCircle
                                } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                    endConnectingPoint = hitCircle
                                    if (!(Pair(endConnectingPoint, startConnectingPoint) in lines || Pair(
                                            startConnectingPoint,
                                            endConnectingPoint
                                        ) in lines)
                                    ) {
                                        lines = lines.toMutableMap().apply {
                                            this[Pair(startConnectingPoint!!, endConnectingPoint!!)] =
                                                Pair(circles[startConnectingPoint]!!, circles[endConnectingPoint]!!)
                                        }
                                    }
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                } else {
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                }
                            }
                        }


                        4 -> {
                            selectedCircle = findInMap(circles, circleRadius, offset)
                        }

                        3 -> {
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
                        start = Offset(
                            value.first.first - windowWidth.value / 2,
                            value.first.second - windowHeight.value / 2
                        ),
                        end = Offset(
                            value.second.first - windowWidth.value / 2,
                            value.second.second - windowHeight.value / 2
                        ),
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