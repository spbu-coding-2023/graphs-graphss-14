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
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
// Класс для отслеживания дейсивий пользователя для отмены (1 значение - код действия, 2 - прилагающиеся данные)
data class Action(val type: Int, val data: Any?)


// штука, чтобы не ломалось при различных разрешениях экрана
fun Dp.toPixels(density: Density): Float = this.value * density.density

// инфа про кружочек
@Serializable
data class CircleData(
    val x: Float,
    val y: Float,
)

//инфа про текущее состояние графа (для сохранения) (можно добавить еще какой-то инфы)
@Serializable
data class WindowStateData(
    val circlesToDraw: Map<Int,CircleData>,
    val linesToDraw: Map<Pair<Int, Int>, Pair<CircleData, CircleData>>,
    val switchState: Boolean,
    val nodeCounter: Int
)

//ну собсна сохранение в формат .json
fun saveToFile(
    circlesToDraw: Map<Int, CircleData>,
    linesToDraw: Map<Pair<Int, Int>, Pair<CircleData, CircleData>>,
    switchState: Boolean,
    nodeCounter: Int
) {
    val data = WindowStateData( circlesToDraw, linesToDraw, switchState, nodeCounter)
    val json = Json{
        allowStructuredMapKeys = true
    }.
    encodeToString(value = data)
    val directory = File("src/main/resources/save/")
    directory.mkdirs()

    // Получаем текущее время
    val currentTime = LocalDateTime.now()
    // Форматируем время в строку
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    val formattedTime = currentTime.format(formatter)

    val fileName = "$formattedTime.json"
    val file = File(directory, fileName)
    file.writeText(json)
}
//отладочная информация в консоль (вместо отладочных принтов)
private val logger = KotlinLogging.logger {}
//основной код
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun app() {
    // Вся инфа которую нужно хранить
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
    val cyclesFromNode  = remember { mutableStateOf(listOf<List<Int>>()) }
    var isCyclesFromNode  by remember { mutableStateOf(false) }

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
    val colorStates by remember { // цвет темы
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

    /*

    val firstImage = if (switchState) painterResource(image1) else painterResource(imageBlack)

    Image(painter = firstImage)

    */
    if (switchState) { // тут в зависимости от переключателя в настройках выбирается тема
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
    Column { // начало UI
        Row( // всякие модификаторы для того чтобы было красиво
            modifier = Modifier
                .fillMaxWidth()
                .background(colorStates[0])
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) { //тут уже всякие нажимаемые элементы
            IconButton(onClick = { // в частности это кнопка настроек
                openSettings = false
                isNodesToFindWay.value = false
                isNodesToFindWayD.value = false
                expanded = true // штука отслеживающая открытие DropDownMenu
                additionalOptionsGroup1 = false
                additionalOptionsGroup2 = false
                additionalOptionsGroup3 = false
                bridges.value = listOf()
                shortestWay.value = listOf()
            }) {
                Image( // Кортинка
                    painter = painterResource("img/logo(Black).png"), // Замените на путь к вашему изображению
                    contentDescription = "Параметры",
                    modifier = Modifier.size(30.dp) // Размер изображения
                )
            } // конец кнопки настроек

            DropdownMenu( // само меню после нажатия кнопки настроек
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorStates[0]).padding(1.dp).waterfallPadding().shadow(
                    elevation = 4.dp,
                    spotColor = colorStates[4],
                    ambientColor = colorStates[0]
                )

            ) {
                DropdownMenuItem(onClick = {
                    logger.info { "Option 1 clicked" }
                    additionalOptionsGroup1 = true
                    additionalOptionsGroup2 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Работающие алгоритмы", color = colorStates[4])
                }
                if (additionalOptionsGroup1) {
                    DropdownMenuItem(onClick = {
                        logger.info { "Addtional Option" }
                        expanded = false
                        isNodesToFindWayD.value = true
                        additionalOptionsGroup1 = false
                    }){
                        Text("Дейкстра", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info { "Addtional Option" }
                        expanded = false
                        isNodesToFindWay.value = true
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Форд-Беллман", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        // случайная раскладка графа на плоскости
                        circlesToDraw.forEach { (key, _) ->
                            circlesToDraw[key] = Pair(
                                Dp(Random.nextFloat() * windowWidth.value.toInt()),
                                Dp(Random.nextInt(0, windowHeight.value.toInt()).toFloat())
                            )
                        }
                        linesToDraw.forEach { (key, _) ->
                            linesToDraw[key] = Pair(circlesToDraw[key.first]!!, circlesToDraw[key.second]!!)
                        }
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Разложить граф случайно", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        val qwerty = Graph.SpringEmbedder().layout(wgraph)
                        //раскладка графа алгоритмом от Руслана
                        circlesToDraw.forEach { (key, _) ->
                            circlesToDraw[key] = Pair(
                                Dp(qwerty[key]!!.first.toFloat() * windowWidth.value / 20 + windowWidth.value / 2),
                                Dp(qwerty[key]!!.second.toFloat() * windowHeight.value / 20 + windowHeight.value / 2)
                            )
                        }
                        linesToDraw.forEach { (key, _) ->
                            linesToDraw[key] = Pair(circlesToDraw[key.first]!!, circlesToDraw[key.second]!!)
                        }
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {// название поменять, на название алгоритма
                        Text("Разложить граф случайно по умному", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = {
                        logger.info { "Additional Option 2 clicked" }
                        colorsForBeetweenes = wgraph.betweennessCentrality()
                        logger.info { colorsForBeetweenes }

                        isColorsForBeetweenes = true
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Ключевые вершины", color = colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = {
                    logger.info { "Option 2 clicked" }
                    additionalOptionsGroup2 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Группа нерабочих", color = colorStates[4])
                }
                if (additionalOptionsGroup2) {
                    DropdownMenuItem(onClick = {// выделение компоненты сильной связности (Сделать!)
                        logger.info { "Additional Option 1 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                    }) {
                        Text("Выделение компонент сильной связности", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = { //выделение сообществ (его нет, сделать)
                        logger.info { "Additional Option 3 clicked" }
                        expanded = false
                        //colorsForClusters = Graph.ClusteredGraph.
//                        //graph.clusterGraph()
//                        //graph.colorClusters()
                        //additionalOptionsGroup1 = false
                    }) {
                        Text("Выделение сообществ", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = { //  поиск мостов (как работают - смотреть в /algos)
                        logger.info { "Additional Option 2 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                        bridges.value = wgraph.findBridges()
                    }) {
                        Text("Поиск мостов", color = colorStates[4])
                    }
                    DropdownMenuItem(onClick = { // поиск циклов (Сделать!)
                        logger.info { "Additional Option 3 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                        isCyclesFromNode = true

                    }) {
                        Text("Поиск циклов для заданной вершины", color = colorStates[4])
                    }
                }
                if (additionalOptionsGroup3) {
                    DropdownMenuItem(onClick = { // мин. остовное дерево (сделать!)
                        logger.info { "Additional Option 1 clicked" }
                        expanded = false
                        additionalOptionsGroup3 = false
                    }) {
                        Text("Построение минимального остовного дерева", color = colorStates[4])
                    }
                }
                DropdownMenuItem(onClick = { // просто открывается маленькое меню настроек (там смена темы и сохранение)
                    logger.info { "settings" }
                    expanded = false
                    openSettings = true
                }) {
                    Text("Параметры", color = colorStates[4])
                }

            }
            RadioButton( // это переключатели на главном окне (соединение / создание / редактирование узлов и отмена)
                selected = selectedOption == 1, // это создание
                onClick = { selectedOption = 1 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )
            )
            Text("Создать узлы", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

            RadioButton(
                selected = selectedOption == 2, // соединение
                onClick = { selectedOption = 2 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )

            )
            Text("Соединить узлы", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

            RadioButton( // это редактирование узлов
                selected = selectedOption == 4,
                onClick = { selectedOption = 4 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )
            )
            Text("Редактировать", modifier = Modifier.align(Alignment.CenterVertically), color = colorStates[4])

            IconButton(onClick = { // Отмена
                turnBack = true
            }) {
                Image(
                    painter = painterResource("img/nazad(Black).png"), // Замените на путь к вашему изображению
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp) // Размер изображения
                )
            }
            if (turnBack) { // здесь откат происходит с помощью хранения действий в стеке
                // Pro tips: сделай ограничение размера стека, типа самое глубокое чтобы удалялось при размере стека >50?
                turnBack = false
                if (actionStack.isNotEmpty()) {
                    val lastAction = actionStack.removeLast()
                    when (lastAction.type) {
                        1 -> { // в стеке действий код 1 значит создание узла (значит мы удаляем)
                            circlesToDraw.remove(lastAction.data as Int)
                            nodeCounter--
                            wgraph.removeNode(lastAction.data)
                        }

                        2 -> { // отмена линии
                            val (start, end) = lastAction.data as Pair<*, *>
                            linesToDraw.remove(Pair(start, end))
                            wgraph.removeEdge(start as Int, end as Int)
                        }
                        3 ->{ // отмена передвижения
                            val (key, pos,lines) = lastAction.data as Triple<Int,Pair<Dp,Dp>,List<Pair<Int,Int>>>
                            circlesToDraw[key] = pos
                            wgraph.addNode(key)
                            for (i in lines){
                                wgraph.addEdge(i.first,i.second, 1)
                                linesToDraw[i] = Pair(circlesToDraw[i.first]!!, circlesToDraw[i.second]!!)
                            }

                        }
                    }
                }
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()

            .background(colorStates[0])

            .onPreviewKeyEvent { event -> // обработка действий пользователя на клавиатуре (не работает)
                logger.info { event.key }
                logger.info { event.isCtrlPressed }
                if (event.key == Key.Z && event.isCtrlPressed) {
                    logger.info { actionStack }
                    if (actionStack.isNotEmpty()) {
                        val lastAction = actionStack.removeLast()
                        when (lastAction.type) { // повторение функций кнопки отмена
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
                            3 ->{
                                val (key, pos,lines) = lastAction.data as Triple<Int,Pair<Dp,Dp>,List<Pair<Int,Int>>>
                                circlesToDraw[key] = pos
                                wgraph.addNode(key)
                                for (i in lines){
                                    wgraph.addEdge(i.first,i.second, 1)
                                    linesToDraw[i] = Pair(circlesToDraw[i.first]!!, circlesToDraw[i.second]!!)
                                }

                            }
                        }
                    }
                    true
                } else {
                    false
                }
            }
            .onPointerEvent(PointerEventType.Scroll) { // кручение колесика (уменьшает / увеличивает кружочки)
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
            .pointerInput(Unit) { // нажатие на поле (в зависимости от выбранного радиобаттона либо создаст круг, либо подсветит кружок)
                detectTapGestures(onTap = { offset ->
                    openSettings = false
                    bridges.value = listOf()
                    isColorsForBeetweenes = false
                    cyclesFromNode.value = listOf()
                    shortestWay.value = listOf()
                    val hitCircle = findInMap(circlesToDraw, circleRadius, offset)
                    if (hitCircle != null && isCyclesFromNode){
                        cyclesFromNode.value = wgraph.findCyclesFromNode(hitCircle)
                        logger.info {cyclesFromNode}
                    }
                    else
                        isCyclesFromNode = false
                    if (isNodesToFindWay.value || isNodesToFindWayD.value) {
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
                                logger.info { offset }
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
            }.onSizeChanged { newSize -> // обработка изменения размеров приложения
                val temp = with(density) { DpSize(newSize.width.toDp(), newSize.height.toDp()) }
                if (temp != windowSize) {
                    windowSize = temp
                    windowWidth = with(density) { newSize.width.toDp() }
                    windowHeight = with(density) { newSize.height.toDp() }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures( // обработка перемещательных действий пользователя (кружка или всей плоскости)
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

            Canvas(modifier = Modifier.align(Alignment.TopStart)) { // тут начинается отрисовка всего непотребства на экране
                val canvasWidth = size.width
                val canvasHeight = size.height
                // Отрисовка линий
                for ((key, value) in linesToDraw) {
                    var col = colorStates[4]
                    if (cyclesFromNode.value.isNotEmpty() && (listOf(key.first, key.second) in cyclesFromNode.value ||
                        (key.second == cyclesFromNode.value.last()[0] && key.first == cyclesFromNode.value.last().last()))){
                        col = Color.Magenta
                    }
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
                    if (isCyclesFromNode && cyclesFromNode.value.isNotEmpty()){
                        if (key in cyclesFromNode.value.last()){
                            logger.info {"good"}
                            col = Color.Blue
                        }
                        if (key == cyclesFromNode.value.last()[0])
                            col = Color.Cyan
                    }
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
                    } // если придумаешь как делать текст на кружочках будешь крутым (я не смог)
                    //drawText(
                    //    textLayoutResult = TextLayoutResult(layoutInput=(),)
                    //)
                }

            }

// Отображаем всплывающее окно, если круг выбран
            selectedCircle?.let { key ->
                Window(onCloseRequest = { selectedCircle = null },
                    state = WindowState(
                        position = WindowPosition(
                            (circlesToDraw[key]!!.first),
                            (circlesToDraw[key]!!.second)
                        ), size = DpSize(250.dp, 250.dp)
                    ),
                    content = {
                        Box(
                            modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Редактирование узла", color = colorStates[4])
                            // вот сюда можно добавить еще всякого полезного, типа настройки веса ребра как нибудь, я не придумал)
                            Button(onClick = {
                                wgraph.removeNode(key)
                                val listToRemove = mutableListOf<Pair<Int,Int>>()
                                for ( i in linesToDraw.keys){
                                    if (i.first == key || i.second == key){
                                        listToRemove.add(i)
                                    }
                                }
                                for (i in listToRemove){
                                    linesToDraw.remove(i)
                                }
                                actionStack.add(Action(
                                    3,
                                    Triple(key,circlesToDraw[key] ,listToRemove)
                                ))
                                circlesToDraw.remove(key)
                                selectedCircle = null



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
    if (openSettings) { // всплывающее окно настроек

        Window(onCloseRequest = { openSettings = false },
            focusable = true,
            alwaysOnTop = false,
            title = "settings",
            state = WindowState(
                position = WindowPosition(windowWidth / 2, windowHeight / 2),
                size = DpSize(200.dp, 200.dp)
            ),
            content = {
                Box(
                    modifier = Modifier.padding(1.dp).fillMaxSize().background(colorStates[0]),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text("Выбрать тему", color = colorStates[4])
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Тёмная тема:", color = colorStates[4])
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = switchState,
                                onCheckedChange = { switchState = it }
                            )
                        }
                        Button(onClick = {
                            val circlesToDrawInPixels = circlesToDraw.mapValues { (_, position) ->
                                CircleData(
                                    x = position.first.toPixels(density),
                                    y = position.second.toPixels(density),
                                )
                            }
                            val linesToDrawInPixels = linesToDraw.mapValues { (_, position) ->
                                Pair(CircleData(
                                    x = position.first.first.toPixels(density),
                                    y = position.first.second.toPixels(density)
                                ), CircleData(
                                    x = position.second.first.toPixels(density),
                                    y = position.second.second.toPixels(density)
                                )
                                )
                            }
                            saveToFile(circlesToDrawInPixels,
                                linesToDrawInPixels,
                                switchState,
                                nodeCounter)
                            openSettings = false
                        }){
                            Text("Сохранить граф")
                        }
                    }
                }
            }
        )

    }
}

@Composable
fun mainScreen(onStartClick: () -> Unit) { // стартовое окно с загрузкой или старта с 0
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartClick) {
            Text("Построить граф")
        }
        Button(onClick = { /* Handle other button click */ }) {
            Text("Загрузить сохранение")
        }

    }
}

fun main() = application { // то что запускается и вызывает все остальное

    val density = LocalDensity.current
    val windowSize = with(density) { DpSize(800.dp.value.toInt().toDp(), 600.dp.value.toInt().toDp()) }

    var showMainScreen by remember { mutableStateOf(true) }

    if (showMainScreen) {
        Window(onCloseRequest = ::exitApplication) {
            mainScreen(onStartClick = { showMainScreen = false })
        }
    } else {
        Window(
            onCloseRequest = ::exitApplication,
            state = WindowState(size = windowSize),
            title = "The best graph visualizer",
            focusable = true
        ) {
            app()
        }
    }
}
