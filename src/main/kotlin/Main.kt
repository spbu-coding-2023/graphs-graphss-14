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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
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
fun findInMap(dict: MutableMap<Int, Pair<Dp, Dp>>, circleRadius: Dp, offset: Offset): Int? {
    for ((key, value) in dict) {
        val distance = sqrt(((offset.x) - value.first.value).pow(2) + ((offset.y) - value.second.value).pow(2))
        if (distance <= circleRadius.value) {
            return key
        }
    }
    return null
}

data class Action(val type: Int, val data: Any?)

@Composable
fun app() {
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var circles by remember { mutableStateOf(mutableMapOf<Int, Pair<Dp, Dp>>()) }
    var lines by remember { mutableStateOf(mutableMapOf<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>()) }

    var selectedCircle by remember { mutableStateOf<Int?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var selectedCircleToMove by remember { mutableStateOf<Int?>(null) }
    var startConnectingPoint by remember { mutableStateOf<Int?>(null) }
    var endConnectingPoint by remember { mutableStateOf<Int?>(null) }
    val circleRadius: Dp = 20.dp

    var expanded by remember { mutableStateOf(false) }
    var additionalOptionsGroup1 by remember { mutableStateOf(false) }
    var additionalOptionsGroup2 by remember { mutableStateOf(false) }
    var additionalOptionsGroup3 by remember { mutableStateOf(false) }
    var openSettings by remember { mutableStateOf(false) }
    var switchState by remember { mutableStateOf(false) }
    val colorStates by remember { mutableStateOf(mutableListOf(Color.White, Color.Red, Color.Blue, Color.Gray, Color.Black)) }
    if (switchState){
        colorStates[0] = Color.Black
        colorStates[1] = Color.Red
        colorStates[2] = Color.Yellow
        colorStates[3] = Color.LightGray
        colorStates[4] = Color.White
    } else {
        colorStates[0] = Color.White
        colorStates[1] = Color.Red
        colorStates[2] = Color.Blue
        colorStates[3] = Color.Gray
        colorStates[4] = Color.Black
    }

    val actionStack = remember { mutableStateListOf<Action>() }

    var selectedOption by remember { mutableStateOf(1) }
    var nodeCounter by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorStates[0])
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
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
                modifier = Modifier.background(colorStates[0]).padding(1.dp).waterfallPadding().shadow(elevation = 4.dp,
                    spotColor = colorStates[4],
                    ambientColor = colorStates[0])

            ) {
                DropdownMenuItem(onClick = {
                    println("Option 1 clicked")
                    additionalOptionsGroup1 = true
                    additionalOptionsGroup2 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Option 1", color=colorStates[4])
                }
                if (additionalOptionsGroup1) {
                    DropdownMenuItem(onClick = {
                        //tyt raskladka norm doljna bit
                        circles.forEach{(key, _) ->
                            circles[key] = Pair(Dp(Random.nextFloat() * windowWidth.value.toInt()),
                                Dp(Random.nextInt(0, windowHeight.value.toInt()).toFloat()))
                        }
                        lines.forEach{(key, _) ->
                            lines[key] = Pair(circles[key.first]!!, circles[key.second]!!)
                        }
                        //tyt raskladka norm doljna bit
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Разложить граф", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Additional Option 2", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Additional Option 3", color=colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    println("Option 2 clicked")
                    additionalOptionsGroup2 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Option 2", color=colorStates[4])
                }
                if (additionalOptionsGroup2) {
                    DropdownMenuItem(onClick = {
                        println("Additional Option 1 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 1", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 2", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Additional Option 3", color=colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    println("Option 3 clicked")
                    additionalOptionsGroup3 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup2 = false
                }) {
                    Text("Option 3", color=colorStates[4])
                }
                if (additionalOptionsGroup3) {
                    DropdownMenuItem(onClick = {
                        println("Additional Option 1 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 1", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 2 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 2", color=colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        println("Additional Option 3 clicked")
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Additional Option 3", color=colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    println("settings")
                    openSettings = true
                }) {
                    Text("settings", color=colorStates[4])
                }
                if (openSettings){
                    DialogWindow(onCloseRequest = { openSettings = false },
                        state = DialogState(position = WindowPosition(200.dp, 200.dp)),
                        content = {
                            Box(modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]), contentAlignment = Alignment.Center) {
                                Column {
                                    Text("This is a popup window", color=colorStates[4])
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Dark theme:", color=colorStates[4])
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = switchState,
                                            onCheckedChange = { switchState = it }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
            RadioButton(
                selected = selectedOption == 1,
                onClick = { selectedOption = 1 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )
            )
            Text("Создать узлы", modifier = Modifier.align(Alignment.CenterVertically), color=colorStates[4])

            RadioButton(
                selected = selectedOption == 2,
                onClick = { selectedOption = 2 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )

            )
            Text("Соединить узлы", modifier = Modifier.align(Alignment.CenterVertically), color=colorStates[4])

//            RadioButton(
//                selected = selectedOption == 3,
//                onClick = { selectedOption = 3 },
//                colors = RadioButtonDefaults.colors(
//                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
//                    selectedColor = Color.Cyan // Цвет активного радиобаттона
//                )
//            )
//            Text("Перемещение", modifier = Modifier.align(Alignment.CenterVertically), color=colorStates[4])

            RadioButton(
                selected = selectedOption == 4,
                onClick = { selectedOption = 4 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )
            )
            Text("Редактировать", modifier = Modifier.align(Alignment.CenterVertically), color=colorStates[4])
        }
        Box(modifier = Modifier
            .fillMaxSize()

            .background(colorStates[0])
            .onPreviewKeyEvent{ event ->
                println(event.key)
                println(event.isCtrlPressed)
                if (event.key == Key.Z && event.isCtrlPressed) {
                    println(actionStack)
                    if (actionStack.isNotEmpty()) {
                        val lastAction = actionStack.removeLast()
                        when (lastAction.type) {
                            1 -> {
                                circles.remove(lastAction.data as Int)
                                nodeCounter--
                            }
                            2 -> {
                                val (start, end) = lastAction.data as Pair<Int, Int>
                                lines.remove(Pair(start, end))
                            }
                            // Добавьте обработку для других типов действий, если они есть
                        }
                    }
                    true
                } else {
                    false
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    when (selectedOption) {
                        1 -> {
                            actionStack.add(Action(1, nodeCounter))
                            circles = circles.toMutableMap().apply { this[nodeCounter] = Pair(offset.x.toDp(), offset.y.toDp()) }
                            println(offset)
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
                                        actionStack.add(Action(2, Pair(startConnectingPoint!!, endConnectingPoint!!)))
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

//                        3 -> {
//                            selectedCircleToMove = findInMap(circles, circleRadius, offset)
//                        }
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
                        isDragging = selectedCircleToMove == null
                    },
                    onDragEnd = {
                        selectedCircleToMove = null
                        isDragging = false
                    },
                    onDragCancel = {
                        selectedCircleToMove = null
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        if (isDragging) {
                            // Перемещаем все круги и линии
                            circles = circles.mapValues { (_, value) ->
                                Pair(value.first + dragAmount.x.toDp(), value.second + dragAmount.y.toDp())
                            } as MutableMap<Int, Pair<Dp, Dp>>
                            lines = lines.mapValues { (_, value) ->
                                Pair(
                                    Pair(value.first.first + dragAmount.x.toDp(), value.first.second + dragAmount.y.toDp()),
                                    Pair(value.second.first + dragAmount.x.toDp(), value.second.second + dragAmount.y.toDp())
                                )
                            } as MutableMap<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>
                        }
                        selectedCircleToMove?.let { circleId ->
                            val newX = circles[circleId]!!.first + dragAmount.x.toDp()
                            val newY = circles[circleId]!!.second + dragAmount.y.toDp()
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
                        color = colorStates[3],
                        start = Offset(
                            (value.first.first.toPx() - windowWidth.value / 2),
                            value.first.second.toPx() - windowHeight.value / 2
                        ),
                        end = Offset(
                            value.second.first.toPx() - windowWidth.value / 2,
                            value.second.second.toPx() - windowHeight.value / 2
                        ),
                        strokeWidth = 2f
                    )
                }
            }

            circles.forEach { (key, value) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawCircle(
                        color = colorStates[1],
                        radius = circleRadius.value,
                        center = Offset(value.first.toPx() - windowWidth.value /2, value.second.toPx() - windowHeight.value/2),
                        style = Fill
                    )
                    if (selectedCircle == key || selectedCircleToMove == key || startConnectingPoint == key) {
                        drawCircle(
                            color = colorStates[2],
                            radius = circleRadius.value + 1,
                            center = Offset(value.first.toPx() - windowWidth.value / 2, value.second.toPx() - windowHeight.value / 2),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

            }

// Отображаем всплывающее окно, если круг выбран
            selectedCircle?.let { key ->
                DialogWindow(onCloseRequest = { selectedCircle = null },
                    state = DialogState(position = WindowPosition((circles[key]!!.first), (circles[key]!!.second))),
                    content = {
                        Box(modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]), contentAlignment = Alignment.Center) {
                            Text("Всплывающее окно", color=colorStates[4])
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun mainScreen(onStartClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartClick) {
            Text("Start")
        }
        Button(onClick = { /* Handle other button click */ }) {
            Text("Other Option")
        }
    }
}

fun main() = application {
    val density = LocalDensity.current
    val windowSize = with(density) { DpSize(800.dp.toPx().toInt().toDp(), 600.dp.toPx().toInt().toDp()) }

    var showMainScreen by remember { mutableStateOf(true) }

    if (showMainScreen) {
        Window(onCloseRequest = ::exitApplication) {
            mainScreen(onStartClick = { showMainScreen = false })
        }
    } else {
            Window(onCloseRequest = ::exitApplication, state = WindowState(size = windowSize)) {
        app()
        }
    }
}