import mu.KotlinLogging
import algos.Graph
import algos.WGraph
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.min

fun makeLineKeysFromList(nodes: List<Int>): List<Pair<Int, Int>> {
    return nodes.zipWithNext()
}

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

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun app() {
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var circlesToDraw by remember { mutableStateOf(mutableMapOf<Int, Pair<Dp, Dp>>()) }
    var colorsForBeetweenes by remember { mutableStateOf(mapOf<Int, Float>()) }
    var linesToDraw by remember { mutableStateOf(mutableMapOf<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>()) }
    val wgraph = remember { WGraph() }


    val bridges = remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    val isNodesToFindWay = remember { mutableStateOf(false) }
    val isNodesToFindWayD = remember { mutableStateOf(false) }
    val shortestWay = remember { mutableStateOf(listOf<Int>()) }

    var selectedCircle by remember { mutableStateOf<Int?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var selectedCircleToMove by remember { mutableStateOf<Int?>(null) }
    var startConnectingPoint by remember { mutableStateOf<Int?>(null) }
    var endConnectingPoint by remember { mutableStateOf<Int?>(null) }
    var circleRadius by remember { mutableStateOf(20.dp) }
    var expanded by remember { mutableStateOf(false) }
    var additionalOptionsGroup1 by remember { mutableStateOf(false) }
    var additionalOptionsGroup2 by remember { mutableStateOf(false) }
    var additionalOptionsGroup3 by remember { mutableStateOf(false) }
    var openSettings by remember { mutableStateOf(false) }
    var isColorsForBeetweenes by remember { mutableStateOf(false) }
    var switchState by remember { mutableStateOf(false) }
    var turnBack by remember { mutableStateOf(false) }
    val colorStates by remember {
        mutableStateOf(
            mutableListOf(
                Color.White,
                Color.Red,
                Color.Blue,
                Color.Gray,
                Color.Black
            )
        )
    }
    if (switchState) {
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
                isNodesToFindWay.value = false
                isNodesToFindWayD.value = false
                expanded = true
                additionalOptionsGroup1 = false
                additionalOptionsGroup2 = false
                bridges.value = listOf()
                shortestWay.value = listOf()
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
                modifier = Modifier.background(colorStates[0]).padding(1.dp).waterfallPadding().shadow(
                    elevation = 4.dp,
                    spotColor = colorStates[4],
                    ambientColor = colorStates[0]
                )

            ) {
                DropdownMenuItem(onClick = {
                    logger.info {"Option 1 clicked"}
                    additionalOptionsGroup1 = true
                    additionalOptionsGroup2 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Основная группа", color = colorStates[4])
                }
                if (additionalOptionsGroup1) {
                    DropdownMenuItem(onClick = {
                        //tyt raskladka norm doljna bit пока что тут рандом
                        circlesToDraw.forEach { (key, _) ->
                            circlesToDraw[key] = Pair(
                                Dp(Random.nextFloat() * windowWidth.value.toInt()),
                                Dp(Random.nextInt(0, windowHeight.value.toInt()).toFloat())
                            )
                        }
                        linesToDraw.forEach { (key, _) ->
                            linesToDraw[key] = Pair(circlesToDraw[key.first]!!, circlesToDraw[key.second]!!)
                        }
                        //tyt raskladka norm doljna bit
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Разложить граф случайно", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        val qwerty = Graph.SpringEmbedder().layout(wgraph)
                        //tyt raskladka norm doljna bit
                        circlesToDraw.forEach { (key, _) ->
                            circlesToDraw[key] = Pair(
                                Dp(qwerty[key]!!.first.toFloat() * windowWidth.value / 20 + windowWidth.value / 2),
                                Dp(qwerty[key]!!.second.toFloat() * windowHeight.value / 20 + windowHeight.value / 2)
                            )
                        }
                        linesToDraw.forEach { (key, _) ->
                            linesToDraw[key] = Pair(circlesToDraw[key.first]!!, circlesToDraw[key.second]!!)
                        }
                        //tyt raskladka norm doljna bit
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Разложить граф случайно по умному", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 2 clicked"}
                        colorsForBeetweenes = wgraph.betweennessCentrality()
                        logger.info { colorsForBeetweenes}

                        isColorsForBeetweenes = true
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Ключевые вершины", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 3 clicked"}
                        expanded = false
                        //colorsForClusters = Graph.ClusteredGraph.
//                        //graph.clusterGraph()
//                        //graph.colorClusters()
                        //additionalOptionsGroup1 = false
                    }) {
                        Text("Выделение сообществ", color = colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    logger.info{"Option 2 clicked"}
                    additionalOptionsGroup2 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Группа алгоритмов 1", color = colorStates[4])
                }
                if (additionalOptionsGroup2) {
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 1 clicked"}
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Выделение компонент сильной связности", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 2 clicked"}
                        expanded = false
                        additionalOptionsGroup2 = false
                        bridges.value = wgraph.findBridges()
                    }) {
                        Text("Поиск мостов", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 3 clicked"}
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Поиск циклов для заданной вершины", color = colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    logger.info{"Option 3 clicked"}
                    additionalOptionsGroup3 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup2 = false
                }) {
                    Text("Группа алгоритмов 2", color = colorStates[4])
                }
                if (additionalOptionsGroup3) {
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 1 clicked"}
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Построение минимального остовного дерева", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 2 clicked"}
                        expanded = false
                        isNodesToFindWayD.value = true
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Путь между вершинами (Дейкстра)", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info{"Additional Option 3 clicked"}
                        expanded = false
                        isNodesToFindWay.value = true
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Путь между вершинами (Форд-Беллман)", color = colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    logger.info{"settings"}
                    expanded = false
                    openSettings = true
                }) {
                    Text("settings", color = colorStates[4])
                }
                if (openSettings) {

                    DialogWindow(onCloseRequest = { openSettings = false },
                        focusable = false,
                        enabled = true,
                        title = "settings",
                        state = DialogState(position = WindowPosition(windowWidth / 2, windowHeight/ 2)),
                        content = {
                            Box(
                                modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    Text("This is a popup window", color = colorStates[4])
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Dark theme:", color = colorStates[4])
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
            Text("Создать узлы", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

            RadioButton(
                selected = selectedOption == 2,
                onClick = { selectedOption = 2 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )

            )
            Text("Соединить узлы", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

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
            Text("Редактировать", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

            IconButton(onClick = {
                turnBack = true
            }) {
                Image(
                    painter = painterResource("img/back.png"), // Замените на путь к вашему изображению
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp) // Размер изображения
                )
            }
            if (turnBack) {
                turnBack = false
                if (actionStack.isNotEmpty()) {
                    val lastAction = actionStack.removeLast()
                    when (lastAction.type) {
                        1 -> {
                            circlesToDraw.remove(lastAction.data as Int)
                            nodeCounter--
                            wgraph.removeNode(lastAction.data)
                        }

                        2 -> {
                            val (start, end) = lastAction.data as Pair<*, *>
                            linesToDraw.remove(Pair(start, end))
                            wgraph.removeEdge(start as Int, end as Int)
                        }
                        // Добавьте обработку для других типов действий, если они есть
                    }
                }
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()

            .background(colorStates[0])

            .onPreviewKeyEvent { event ->
                logger.info{event.key}
                logger.info{event.isCtrlPressed}
                if (event.key == Key.Z && event.isCtrlPressed) {
                    logger.info{actionStack}
                    if (actionStack.isNotEmpty()) {
                        val lastAction = actionStack.removeLast()
                        when (lastAction.type) {
                            1 -> {
                                circlesToDraw.remove(lastAction.data as Int)
                                nodeCounter--
                                wgraph.removeNode(lastAction.data)
                            }

                            2 -> {
                                val (start, end) = lastAction.data as Pair<*, *>
                                linesToDraw.remove(Pair(start, end))
                                wgraph.removeEdge(start as Int, end as Int)
                            }
                            // Добавьте обработку для других типов действий, если они есть
                        }
                    }
                    true
                } else {
                    false
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                if (it.changes.first().scrollDelta.y > 0) {
                    circleRadius = (circleRadius.value - 0.3F).toDp()
                    if (circleRadius.value < 4) {
                        circleRadius = 4.dp
                    }
                } else {
                    circleRadius = (circleRadius.value + 0.3F).toDp()
                    if (circleRadius.value > 25) {
                        circleRadius = 25.dp
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    bridges.value = listOf()
                    isColorsForBeetweenes = false
                    shortestWay.value = listOf()
                    if (isNodesToFindWay.value || isNodesToFindWayD.value) {
                        val hitCircle = findInMap(circlesToDraw, circleRadius, offset)
                        if (hitCircle != null) {
                            if (startConnectingPoint == null) {
                                startConnectingPoint = hitCircle
                            } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                endConnectingPoint = hitCircle
                                val temp: List<Int>? = if (isNodesToFindWayD.value) {
                                    wgraph.shortestPathD(startConnectingPoint!!, endConnectingPoint!!)
                                } else {
                                    wgraph.shortestPathBF(startConnectingPoint!!, endConnectingPoint!!)
                                }
                                if (temp != null) {
                                    shortestWay.value = temp
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                    isNodesToFindWay.value = false
                                    isNodesToFindWayD.value = false
                                } else {
                                    startConnectingPoint = null
                                    endConnectingPoint = null
                                }
                            } else {
                                startConnectingPoint = null
                                endConnectingPoint = null
                            }
                        } else {
                            isNodesToFindWay.value = false
                            isNodesToFindWayD.value = false
                        }
                    } else {
                        when (selectedOption) {
                            1 -> {
                                actionStack.add(Action(1, nodeCounter))
                                circlesToDraw = circlesToDraw.toMutableMap()
                                    .apply { this[nodeCounter] = Pair(offset.x.toDp(), offset.y.toDp()) }
                                logger.info{offset}
                                wgraph.addNode(nodeCounter)
                                nodeCounter += 1
                            }

                            2 -> {
                                val hitCircle = findInMap(circlesToDraw, circleRadius, offset)
                                if (hitCircle != null) {
                                    if (startConnectingPoint == null) {
                                        startConnectingPoint = hitCircle
                                    } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                        endConnectingPoint = hitCircle
                                        if (!(Pair(endConnectingPoint, startConnectingPoint) in linesToDraw || Pair(
                                                startConnectingPoint,
                                                endConnectingPoint
                                            ) in linesToDraw)
                                        ) {
                                            actionStack.add(
                                                Action(
                                                    2,
                                                    Pair(startConnectingPoint!!, endConnectingPoint!!)
                                                )
                                            )
                                            linesToDraw = linesToDraw.toMutableMap().apply {
                                                this[Pair(startConnectingPoint!!, endConnectingPoint!!)] =
                                                    Pair(
                                                        circlesToDraw[startConnectingPoint]!!,
                                                        circlesToDraw[endConnectingPoint]!!
                                                    )
                                            }
                                            wgraph.addEdge(startConnectingPoint!!, endConnectingPoint!!, 1)
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
                                selectedCircle = findInMap(circlesToDraw, circleRadius, offset)
                            }

//                        3 -> {
//                            selectedCircleToMove = findInMap(circles, circleRadius, offset)
//                        }
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
                        selectedCircle = null
                        selectedCircleToMove = findInMap(circlesToDraw, circleRadius, offset)
                        isDragging = selectedCircleToMove == null

                    },
                    onDragEnd = {
                        selectedCircle = null
                        selectedCircleToMove = null
                        isDragging = false
                    },
                    onDragCancel = {
                        selectedCircle = null
                        selectedCircleToMove = null
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        if (isDragging) {
                            selectedCircle = null
                            // Перемещаем все круги и линии
                            circlesToDraw = circlesToDraw.mapValues { (_, value) ->
                                Pair(value.first + dragAmount.x.toDp(), value.second + dragAmount.y.toDp())
                            } as MutableMap<Int, Pair<Dp, Dp>>
                            linesToDraw = linesToDraw.mapValues { (_, value) ->
                                Pair(
                                    Pair(
                                        value.first.first + dragAmount.x.toDp(),
                                        value.first.second + dragAmount.y.toDp()
                                    ),
                                    Pair(
                                        value.second.first + dragAmount.x.toDp(),
                                        value.second.second + dragAmount.y.toDp()
                                    )
                                )
                            } as MutableMap<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>
                        }
                        selectedCircleToMove?.let { circleId ->
                            val newX = circlesToDraw[circleId]!!.first + dragAmount.x.toDp()
                            val newY = circlesToDraw[circleId]!!.second + dragAmount.y.toDp()
                            circlesToDraw = circlesToDraw.toMutableMap().apply { this[circleId] = Pair(newX, newY) }
                            dragOffset += change.positionChange()

                            // Update connected lines
                            linesToDraw = linesToDraw.toMutableMap().apply {
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
            val shortway = makeLineKeysFromList(shortestWay.value)
            Canvas(modifier = Modifier.align(Alignment.TopStart)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                // Отрисовка линий
                for ((key, value) in linesToDraw) {
                    var col = colorStates[4]
                    if (Pair(key.first, key.second) in bridges.value || Pair(key.second, key.first) in bridges.value) {
                        col = Color.Magenta
                    }
                    if (Pair(key.first, key.second) in shortway || Pair(key.second, key.first) in shortway) {
                        col = Color.Green
                    }
                    drawLine(
                        color = col,
                        start = Offset(
                            value.first.first.value - canvasWidth / 2,
                            value.first.second.value - canvasHeight / 2
                        ),
                        end = Offset(
                            value.second.first.value - canvasWidth / 2,
                            value.second.second.value - canvasHeight / 2
                        ),
                        strokeWidth = 2f
                    )
                }

                // Отрисовка кругов
                for ((key, value) in circlesToDraw) {
                    var col = colorStates[1]
                    if (isColorsForBeetweenes) {
                        if (key in colorsForBeetweenes && !colorsForBeetweenes[key]!!.isNaN()) {
                            col = Color(red = min((127F + 255 * colorsForBeetweenes[key]!! / 2).toInt(), 255), 0, 0)
                        }
                    } else if (shortestWay.value.isNotEmpty() && (key == shortestWay.value.first() || key == shortestWay.value.last())) {
                        col = Color.Cyan
                    } else if (key in shortestWay.value) {
                        col = Color.Blue
                    }
                    drawCircle(
                        color = col,
                        radius = circleRadius.value,
                        center = Offset(value.first.value - canvasWidth / 2, value.second.value - canvasHeight / 2),
                        style = Fill
                    )
                    // Проверка, является ли круг выбранным или перемещаемым
                    if (selectedCircle == key || selectedCircleToMove == key || startConnectingPoint == key) {
                        drawCircle(
                            color = colorStates[2],
                            radius = circleRadius.value + 1,
                            center = Offset(value.first.value - canvasWidth / 2, value.second.value - canvasHeight / 2),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    //drawText(
                    //    textLayoutResult = TextLayoutResult(layoutInput=(),)
                    //)
                }

            }

// Отображаем всплывающее окно, если круг выбран
            selectedCircle?.let { key ->
                DialogWindow(onCloseRequest = { selectedCircle = null },
                    state = DialogState(
                        position = WindowPosition(
                            (circlesToDraw[key]!!.first),
                            (circlesToDraw[key]!!.second)
                        )
                    ),
                    content = {
                        Box(
                            modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Всплывающее окно", color = colorStates[4])
                            Button(onClick = {
                                //wdgraph.removeNode(key)
                                //circlesToDraw.remove(key)
                                //val newLinesToDraw = linesToDraw.filterNot { (k, _) ->
                                //    k.first == key || k.second == key
                                //}

                                //linesToDraw = newLinesToDraw as MutableMap<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>
                            }) {
                                Text("Удалить вершину", color = colorStates[4])
                            }
                        }
                    }
                )
            }
        }
    }
    if (openSettings) {

        DialogWindow(onCloseRequest = { openSettings = false },
            focusable = false,
            enabled = true,
            title = "settings",
            state = DialogState(position = WindowPosition(windowWidth / 2, windowHeight/ 2)),
            content = {
                Box(
                    modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text("This is a settings", color = colorStates[4])
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Dark theme:", color = colorStates[4])
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
            Text("Load save")
        }

    }
}

fun main() = application {

    val density = LocalDensity.current
    val windowSize = with(density) { DpSize(800.dp.value.toInt().toDp(), 600.dp.value.toInt().toDp()) }

    var showMainScreen by remember { mutableStateOf(true) }

    if (showMainScreen) {
        Window(onCloseRequest = ::exitApplication) {
            mainScreen(onStartClick = { showMainScreen = false })
        }
    } else {
        Window(onCloseRequest = ::exitApplication, state = WindowState(size = windowSize), title = "The best graph visualizator", focusable = true) {
            app()
        }
    }
}
