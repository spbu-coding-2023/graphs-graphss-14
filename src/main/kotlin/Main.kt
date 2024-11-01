import algos.DiGraph
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.min
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
import java.awt.FileDialog
import java.awt.Frame
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun DrawScope.drawArrow(color: Color, start: Offset, end: Offset, n: Dp) {
    val arrowHeadSize = 10.dp.toPx()
    val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())

    // Преобразуем n в пиксели
    val nPx = n.toPx()

    // Вычисляем новую конечную точку с учетом отступа n
    val newEnd = Offset(
        (end.x - nPx * Math.cos(angle)).toFloat(),
        (end.y - nPx * Math.sin(angle)).toFloat()
    )

    drawLine(
        color = color,
        start = start,
        end = newEnd,
        strokeWidth = 2.dp.toPx()
    )

    val arrowHead1 = Offset(
        (newEnd.x - arrowHeadSize * Math.cos(angle - Math.PI / 6)).toFloat(),
        (newEnd.y - arrowHeadSize * Math.sin(angle - Math.PI / 6)).toFloat()
    )
    val arrowHead2 = Offset(
        (newEnd.x - arrowHeadSize * Math.cos(angle + Math.PI / 6)).toFloat(),
        (newEnd.y - arrowHeadSize * Math.sin(angle + Math.PI / 6)).toFloat()
    )

    drawLine(
        color = color,
        start = newEnd,
        end = arrowHead1,
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = color,
        start = newEnd,
        end = arrowHead2,
        strokeWidth = 2.dp.toPx()
    )
}

fun generateDistinctColors(n: Int): List<Color> {
    val colors = mutableListOf<Color>()
    val hueStep = 360.0 / n

    for (i in 0 until n) {
        val hue = (i * hueStep).roundToInt()
        val color = hslToColor(hue, 100, 50)
        colors.add(color)
    }

    return colors
}

fun hslToColor(h: Int, s: Int, l: Int): Color {
    val h = h / 360.0
    val s = s / 100.0
    val l = l / 100.0

    val q = if (l < 0.5) l * (1 + s) else l + s - l * s
    val p = 2 * l - q

    val r = (hueToRgb(p, q, h + 1.0 / 3) * 255).roundToInt()
    val g = (hueToRgb(p, q, h) * 255).roundToInt()
    val b = (hueToRgb(p, q, h - 1.0 / 3) * 255).roundToInt()

    return Color(r, g, b)
}

fun hueToRgb(p: Double, q: Double, t: Double): Double {
    var t = t
    if (t < 0) t += 1
    if (t > 1) t -= 1
    if (t < 1.0 / 6) return p + (q - p) * 6 * t
    if (t < 1.0 / 2) return q
    if (t < 2.0 / 3) return p + (q - p) * (2.0 / 3 - t) * 6
    return p
}

fun communityColoring(mp: MutableMap<Int, Int>): MutableMap<Int, Color> {
    val s: MutableSet<Int> = mutableSetOf()
    for (i in mp.keys) {
        s.add(mp[i]!!)
    }
    val colors = generateDistinctColors(s.size)
    val res: MutableMap<Int, Color> = mutableMapOf()
    val comToCol = s.zip(colors).toMap()
    for (i in mp.keys) {
        res[i] = comToCol[mp[i]!!]!!
    }
    return res
}

fun Float.toDp(density: Density): Dp {
    return with(density) { this@toDp.toDp() }
}

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
    val graphMode: Boolean,
    val circlesToDraw: Map<Int, CircleData>,
    val linesToDraw: Map<Pair<Int, Int>, Pair<CircleData, CircleData>>,
    val switchState: Boolean,
    val nodeCounter: Int
)

//ну собсна сохранение в формат .json
fun saveToFile(
    graphMode: Boolean,
    circlesToDraw: Map<Int, CircleData>,
    linesToDraw: Map<Pair<Int, Int>, Pair<CircleData, CircleData>>,
    switchState: Boolean,
    nodeCounter: Int,
    fileName: String?,
    flag: Int
) {
    if (flag == 1) {
        val data = WindowStateData(graphMode, circlesToDraw, linesToDraw, switchState, nodeCounter)
        val json = Json {
            allowStructuredMapKeys = true
        }.encodeToString(value = data)
        val directory = File("src/main/resources/save/")
        directory.mkdirs()

        if (fileName == null) {
            // Получаем текущее время
            val currentTime = LocalDateTime.now()
            // Форматируем время в строку
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val formattedTime = currentTime.format(formatter)

            val newFileName = "$formattedTime.json"
            val file = File(directory, newFileName)
            file.writeText(json)
        } else {
            if (".db" in fileName) {
                val file = File(directory, fileName.substring(0, fileName.length - 3) + ".json")
                file.writeText(json)
            } else {
                val file = File(directory, fileName)
                file.writeText(json)
            }
        }
    }
    if (flag == 2) {
        val data = WindowStateData(graphMode, circlesToDraw, linesToDraw, switchState, nodeCounter)
        val json = Json {
            allowStructuredMapKeys = true
        }.encodeToString(value = data)
        val directory = File("src/main/resources/save/")
        directory.mkdirs()
        var url: String
        if (fileName == null) {
            val currentTime = LocalDateTime.now()
            // Форматируем время в строку
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            val formattedTime = currentTime.format(formatter)

            val newFileName = "$formattedTime.db"
            val file = File(directory, newFileName)
            url = "jdbc:sqlite:src/main/resources/save/$newFileName"
        } else {
            if (".json" in fileName) {
                val newFileName = fileName.substring(0, fileName.length - 5)
                url = "jdbc:sqlite:src/main/resources/save/$newFileName.db"
            } else {
                url = "jdbc:sqlite:src/main/resources/save/$fileName"
            }
        }
        val conn = DriverManager.getConnection(url)
        val stmt = conn.createStatement()
        val sql = ("CREATE TABLE IF NOT EXISTS graph ("
                + "	json text"
                + ");");
        stmt.execute(sql)
        val sql1 = "INSERT INTO graph(json) VALUES(?)"
        val pstmt = conn.prepareStatement(sql1)
        pstmt.setString(1, json)
        pstmt.executeUpdate()
        conn.close()
    }
}

fun chooseFile(initialDirectory: File): File? {
    val fileDialog = FileDialog(Frame(), "Выберите файл", FileDialog.LOAD)
    fileDialog.isMultipleMode = false
    fileDialog.directory = initialDirectory.absolutePath
    fileDialog.file = "*.db;*.json"
    fileDialog.isVisible = true

    return if (fileDialog.file != null) {
        File(fileDialog.directory, fileDialog.file)
    } else {
        null
    }
}

fun loadFromFile(fileName: String): WindowStateData {
    val directory = File("src/main/resources/save/")
    val file = File(directory, fileName)

    if (!file.exists()) {
        throw IllegalArgumentException("File not found: $fileName")
    }
    var jsonContent: String = ""
    if (".json" in fileName) {
        jsonContent = file.readText()
    }
    if (".db" in fileName) {
        val url = "jdbc:sqlite:" + directory + "/" + fileName
        println(url)
        val conn = DriverManager.getConnection(url)
        val stmt = conn.createStatement()
        val r = "SELECT json\n" +
                "FROM graph\n" +
                "ORDER BY ROWID DESC\n" +
                "LIMIT 1;"
        val res = stmt.executeQuery(r)
        jsonContent = res.getString("json")
        conn.close()
    }
    return Json {
        allowStructuredMapKeys = true
    }.decodeFromString(jsonContent)
}

//отладочная информация в консоль (вместо отладочных принтов)
private val logger = KotlinLogging.logger {}


//основной код
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun app(savesData: WindowStateData, selectedFile: String?) {
    val firstTime = remember { mutableStateOf(true) }
    val saves by remember { mutableStateOf(savesData) }
    // Вся инфа которую нужно хранить
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var circlesToDraw by remember { mutableStateOf(mutableMapOf<Int, Pair<Dp, Dp>>()) }
    var colorsForBeetweenes by remember { mutableStateOf(mapOf<Int, Float>()) }
    var louvainCommunity by remember { mutableStateOf(mapOf<Int, Color>()) }
    var linesToDraw by remember { mutableStateOf(mutableMapOf<Pair<Int, Int>, Pair<Pair<Dp, Dp>, Pair<Dp, Dp>>>()) }
    var graph by remember { mutableStateOf(WGraph()) }

    var louvainFlag by remember { mutableStateOf(false) }

    val bridges = remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    val isNodesToFindWay = remember { mutableStateOf(false) }
    val isNodesToFindWayD = remember { mutableStateOf(false) }
    val shortestWay = remember { mutableStateOf(listOf<Int>()) }
    val cyclesFromNode = remember { mutableStateOf(listOf<List<Int>>()) }
    var isCyclesFromNode by remember { mutableStateOf(false) }
    var isNodesClustering by remember { mutableStateOf(false) }
    var nodesInClusters by remember { mutableStateOf(mapOf<Int, Float>()) }

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
    var sccs by remember { mutableStateOf(mapOf<List<Int>, Color>()) }
    var switchState by remember { mutableStateOf(false) }
    var turnBack by remember { mutableStateOf(false) }
    var iconStates by remember { mutableStateOf(false) }
    var sccsFlag by remember { mutableStateOf(false) }
    var spanningTreeFlag by remember { mutableStateOf(false) }
    var spanningTreeEdges by remember { mutableStateOf(mutableListOf<Pair<Int, Int>>()) }
    val scaleFactor = windowHeight / 600.dp

    val colorStates by remember { // цвет темы
        mutableStateOf(
            mutableListOf(
                Color.White,
                Color.Red,
                Color.Blue,
                Color.Gray,
                Color.Black,
            )
        )
    }

    fun findSCCsInGraph(): Map<List<Int>, Color> {
        val diGraph = DiGraph()
        circlesToDraw.keys.forEach { diGraph.addNode(it) }
        linesToDraw.keys.forEach { diGraph.addEdge(it.first, it.second) }
        linesToDraw.keys.forEach { diGraph.addEdge(it.second, it.first) }

        logger.info { "Nodes: ${diGraph.nodes}" }
        logger.info { "Edges: ${diGraph.edges}" }

        val sccst = diGraph.findSCCs().filter { it.size > 1 }.sortedBy { it.size }.associateWith {
            Color(
                Random.nextInt(50, 200),
                Random.nextInt(50, 200),
                Random.nextInt(50, 200)
            )
        }

        logger.info { "SCCs: ${sccst.keys}" }
        return sccst
    }

    if (switchState) { // тут в зависимости от переключателя в настройках выбирается тема
        colorStates[0] = Color.Black
        colorStates[1] = Color.Red
        colorStates[2] = Color.Yellow
        colorStates[3] = Color.LightGray
        colorStates[4] = Color.White
        iconStates = false
    } else {
        colorStates[0] = Color.White
        colorStates[1] = Color.Red
        colorStates[2] = Color.Blue
        colorStates[3] = Color.Gray
        colorStates[4] = Color.Black
        iconStates = true
    }

    val actionStack = remember { mutableStateListOf<Action>() }

    var selectedOption by remember { mutableStateOf(1) }
    var nodeCounter by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    if (firstTime.value) {
        if (saves.graphMode) {
            graph = DiGraph()
        }
        switchState = saves.switchState
        nodeCounter = saves.nodeCounter
        saves.circlesToDraw.forEach { (key, data) ->
            circlesToDraw[key] = Pair(data.x.toDp(density), data.y.toDp(density))
            graph.addNode(key)
        }
        saves.linesToDraw.forEach { (key, data) ->
            linesToDraw[key] = Pair(
                Pair(data.first.x.toDp(density), data.first.y.toDp(density)),
                Pair(data.second.x.toDp(density), data.second.y.toDp(density))
            )
            graph.addEdge(key.first, key.second, 1)

        }
        firstTime.value = false
    }
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
                louvainFlag = false
                spanningTreeFlag = false
                bridges.value = listOf()
                shortestWay.value = listOf()

            }) {
                val imageResource = if (iconStates) {
                    painterResource("img/logo(Black).png")
                } else {
                    painterResource("img/logo(white).png")
                }
                Image( // Кортинка
                    painter = imageResource,
                    contentDescription = "Параметры",
                    modifier = Modifier.size(30.dp * scaleFactor) // Размер изображения
                )
            } // конец кнопки настроек

            DropdownMenu( // само меню после нажатия кнопки настроек
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorStates[0]).padding(1.dp).waterfallPadding().shadow(
                    elevation = 8.dp,
                    spotColor = colorStates[4],
                    ambientColor = colorStates[0],
                )

            ) {
                DropdownMenuItem(onClick = {
                    logger.info { "Option 1 clicked" }
                    additionalOptionsGroup1 = true
                    additionalOptionsGroup2 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Первая группа алгоритмов", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                }
                if (additionalOptionsGroup1) {
                    DropdownMenuItem(onClick = {
                        logger.info { "Addtional Option" }
                        expanded = false
                        isNodesToFindWayD.value = true
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Дейкстра", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                    }
                    DropdownMenuItem(onClick = {
                        logger.info { "Addtional Option" }
                        expanded = false
                        isNodesToFindWay.value = true
                        additionalOptionsGroup1 = false
                    }) {
                        Text("Форд-Беллман", color = colorStates[4], fontSize = 12.sp * scaleFactor)
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
                        Text("Разложить граф случайно", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                    }
                    DropdownMenuItem(onClick = {
                        val qwerty = Graph.SpringEmbedder().layout(graph)
                        //раскладка графа алгоритмом от Руслана
                        circlesToDraw.forEach { (key, _) ->
                            circlesToDraw[key] = Pair(
                                Dp(qwerty[key]!!.first.toFloat() * windowWidth.value / 30 + windowWidth.value / 2),
                                Dp(qwerty[key]!!.second.toFloat() * windowHeight.value / 30 + windowHeight.value / 2)
                            )
                        }
                        linesToDraw.forEach { (key, _) ->
                            linesToDraw[key] = Pair(circlesToDraw[key.first]!!, circlesToDraw[key.second]!!)
                        }
                        expanded = false
                        additionalOptionsGroup1 = false
                    }) {// название поменять, на название алгоритма
                        Text(
                            "Spring Embedder (Раскладка графа)",
                            color = colorStates[4],
                            fontSize = 12.sp * scaleFactor
                        )
                    }
                    DropdownMenuItem(onClick = {
                        logger.info { "Additional Option 2 clicked" }
                        louvainCommunity = communityColoring(graph.LouvainAlgorithm())
                        logger.info { louvainCommunity }

                        louvainFlag = true
                        expanded = false
                        additionalOptionsGroup1 = false

                    }) {
                        Text("Выделение сообществ", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                    }
                }
                DropdownMenuItem(onClick = {
                    logger.info { "Option 2 clicked" }
                    additionalOptionsGroup2 = true
                    additionalOptionsGroup1 = false
                    additionalOptionsGroup3 = false
                }) {
                    Text("Вторая группа алгоритмов", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                }
                if (additionalOptionsGroup2) {
                    DropdownMenuItem(onClick = {// выделение компоненты сильной связности
                        logger.info { "Additional Option 1 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                        sccs = findSCCsInGraph()
                        sccsFlag = true
                    }) {
                        Text(
                            "Выделение компонент сильной связности",
                            color = colorStates[4],
                            fontSize = 12.sp * scaleFactor
                        )
                    }
                    DropdownMenuItem(onClick = { //выделение сообществ
                        logger.info { "Additional Option 3 clicked" }
                        expanded = false
                        nodesInClusters = graph.betweennessCentrality()
                        isNodesClustering = true
                        logger.info { nodesInClusters }
                    }) {
                        Text("Ключевые вершины", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                    }
                    DropdownMenuItem(onClick = { //  поиск мостов (как работают - смотреть в /algos)
                        logger.info { "Additional Option 2 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                        bridges.value = graph.findBridges()
                        selectedOption = 0
                    }) {
                        Text("Поиск мостов", color = colorStates[4], fontSize = 12.sp * scaleFactor)
                    }
                    DropdownMenuItem(onClick = { // поиск циклов (Сделать!) Сделал)))
                        logger.info { "Additional Option 3 clicked" }
                        expanded = false
                        additionalOptionsGroup2 = false
                        isCyclesFromNode = true
                        selectedOption = 0

                    }) {
                        Text(
                            "Поиск циклов для заданной вершины",
                            color = colorStates[4],
                            fontSize = 12.sp * scaleFactor
                        )
                    }
                    DropdownMenuItem(onClick = { // мин. остовное дерево (сделать!) Это пусть тоже Олег делает
                        logger.info { "Additional Option 1 clicked" }
                        expanded = false
                        additionalOptionsGroup3 = false
                        selectedOption = 0
                        spanningTreeFlag = true
                        spanningTreeEdges = graph.getSpanningTree().edges
                        logger.info { spanningTreeEdges }
                    }) {
                        Text(
                            "Построение минимального остовного дерева",
                            color = colorStates[4],
                            fontSize = 12.sp * scaleFactor
                        )
                    }
                }

                DropdownMenuItem(onClick = { // просто открывается маленькое меню настроек (там смена темы и сохранение)
                    logger.info { "settings" }
                    expanded = false
                    openSettings = true
                }) {
                    Text("Параметры", color = colorStates[4], fontSize = 12.sp * scaleFactor)
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
            Text(
                "Создать узлы",
                modifier = Modifier.align(Alignment.CenterVertically),
                color = colorStates[4],
                fontSize = 12.sp * scaleFactor
            )

            RadioButton(
                selected = selectedOption == 2, // соединение
                onClick = { selectedOption = 2 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Magenta // Цвет активного радиобаттона
                )

            )
            Text(
                "Соединить узлы",
                modifier = Modifier.align(Alignment.CenterVertically),
                color = colorStates[4],
                fontSize = 12.sp * scaleFactor
            )

            RadioButton( // это редактирование узлов
                selected = selectedOption == 4,
                onClick = { selectedOption = 4 },
                colors = RadioButtonDefaults.colors(
                    unselectedColor = colorStates[4], // Цвет неактивного радиобаттона
                    selectedColor = Color.Cyan // Цвет активного радиобаттона
                )
            )
            Text(
                "Редактировать",
                modifier = Modifier.align(Alignment.CenterVertically),
                color = colorStates[4],
                fontSize = 12.sp * scaleFactor
            )

            IconButton(onClick = { // Отмена
                turnBack = true
            }) {

                val imageResource = if (iconStates) {
                    painterResource("img/nazad(Black).png")
                } else
                    painterResource("img/nazad(White).png")

                Image(
                    painter = imageResource, // Замените на путь к вашему изображению
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp * scaleFactor) // Размер изображения
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
                            graph.removeNode(lastAction.data)
                        }

                        2 -> { // отмена линии
                            val (start, end) = lastAction.data as Pair<*, *>
                            linesToDraw.remove(Pair(start, end))
                            graph.removeEdge(start as Int, end as Int)
                        }

                        3 -> { // отмена передвижения
                            val (key, pos, lines) = lastAction.data as Triple<Int, Pair<Dp, Dp>, List<Pair<Int, Int>>>
                            circlesToDraw[key] = pos
                            graph.addNode(key)
                            for (i in lines) {
                                graph.addEdge(i.first, i.second, 1)
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
                                graph.removeNode(lastAction.data)
                            }

                            2 -> {
                                val (start, end) = lastAction.data as Pair<*, *>
                                linesToDraw.remove(Pair(start, end))
                                graph.removeEdge(start as Int, end as Int)
                            }

                            3 -> {
                                val (key, pos, lines) = lastAction.data as Triple<Int, Pair<Dp, Dp>, List<Pair<Int, Int>>>
                                circlesToDraw[key] = pos
                                graph.addNode(key)
                                for (i in lines) {
                                    graph.addEdge(i.first, i.second, 1)
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
                    louvainFlag = false
                    spanningTreeFlag = false
                    shortestWay.value = listOf()
                    val hitCircle = findInMap(circlesToDraw, circleRadius, offset)
                    if (hitCircle != null && isCyclesFromNode) {
                        cyclesFromNode.value = graph.findCyclesFromNode(hitCircle)
                        logger.info { graph.findCyclesFromNode(hitCircle) }
                    } else
                        isCyclesFromNode = false
                    if (isNodesToFindWay.value || isNodesToFindWayD.value) {
                        if (hitCircle != null) {
                            if (startConnectingPoint == null) {
                                startConnectingPoint = hitCircle
                            } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                endConnectingPoint = hitCircle
                                val temp: List<Int>? = if (isNodesToFindWayD.value) {
                                    graph.shortestPathD(startConnectingPoint!!, endConnectingPoint!!)
                                } else {
                                    graph.shortestPathBF(startConnectingPoint!!, endConnectingPoint!!)
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
                                sccsFlag = false
                                isNodesClustering = false
                                actionStack.add(Action(1, nodeCounter))
                                circlesToDraw = circlesToDraw.toMutableMap()
                                    .apply { this[nodeCounter] = Pair(offset.x.toDp(), offset.y.toDp()) }
                                logger.info { offset }
                                graph.addNode(nodeCounter)
                                nodeCounter += 1

                            }

                            2 -> {
                                sccsFlag = false
                                isNodesClustering = false

                                val hitCircle = findInMap(circlesToDraw, circleRadius, offset)
                                if (hitCircle != null) {
                                    if (startConnectingPoint == null) {
                                        startConnectingPoint = hitCircle
                                    } else if (endConnectingPoint == null && startConnectingPoint != hitCircle) {
                                        endConnectingPoint = hitCircle
                                        if ((!(Pair(
                                                endConnectingPoint,
                                                startConnectingPoint
                                            ) in linesToDraw && !saves.graphMode) || Pair(
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
                                            graph.addEdge(startConnectingPoint!!, endConnectingPoint!!, 1)

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
                        }
                    }
                })
            }
            .onSizeChanged { newSize -> // обработка изменения размеров приложения
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
                val allPairs = cyclesFromNode.value.toMutableList().flatMap { cycle ->
                    val pairs = cycle.zipWithNext().map { (a, b) -> Pair(a, b) }
                    if (cycle.size > 1) {
                        pairs + Pair(cycle.last(), cycle.first())
                    } else {
                        pairs
                    }
                }
                for ((key, value) in linesToDraw) {
                    var col = colorStates[4]
                    if (spanningTreeFlag) {
                        if (key in spanningTreeEdges || Pair(key.second, key.first) in spanningTreeEdges) {
                            col = Color.Green
                        }
                    }
                    if (key in allPairs || Pair(key.second, key.first) in allPairs) {
                        col = Color.Magenta
                    }
                    if (Pair(key.first, key.second) in bridges.value || Pair(key.second, key.first) in bridges.value) {
                        col = Color.Magenta
                    }
                    if (Pair(key.first, key.second) in shortway || Pair(key.second, key.first) in shortway) {
                        col = Color.Green
                    }
                    if (!saves.graphMode) {
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
                    } else {
                        drawArrow(
                            color = col,
                            start = Offset(
                                value.first.first.value - canvasWidth / 2,
                                value.first.second.value - canvasHeight / 2
                            ),
                            end = Offset(
                                value.second.first.value - canvasWidth / 2,
                                value.second.second.value - canvasHeight / 2
                            ),
                            circleRadius
                        )
                    }
                }

                // Отрисовка кругов
                val uniqueVertices = cyclesFromNode.value.flatten().toSet()
                for ((key, value) in circlesToDraw) {
                    var col = colorStates[1]

                    if (louvainFlag) {
                        col = louvainCommunity[key]!!
                    }

                    if (isNodesClustering) {
                        val maxValue = nodesInClusters.values.maxOf { it }
                        val onePartValue = maxValue / 200
                        col = Color(
                            red = min((75F + nodesInClusters[key]!! / onePartValue).toInt(), 255),
                            min((10F + nodesInClusters[key]!! / onePartValue).toInt(), 255),
                            min((10F + nodesInClusters[key]!! / onePartValue).toInt(), 255)
                        )
                    }
                    if (isCyclesFromNode && cyclesFromNode.value.isNotEmpty()) {
                        if (key in uniqueVertices) {
                            col = Color.Blue
                        }
                        if (key == cyclesFromNode.value.first()[0])
                            col = Color.Cyan
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
                            center = Offset(
                                value.first.value - canvasWidth / 2,
                                value.second.value - canvasHeight / 2
                            ),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    } // если придумаешь как делать текст на кружочках будешь крутым (я не смог)
                    //drawText(
                    //    textLayoutResult = TextLayoutResult(layoutInput=(),)
                    //)

                }
                if (sccsFlag) {
                    sccs.keys.forEach { scc ->
                        val rColor = sccs[scc]!!
                        scc.forEach { node ->
                            val position = circlesToDraw[node]!!
                            drawCircle(
                                color = rColor,
                                radius = circleRadius.value + 1,
                                center = Offset(
                                    position.first.value - canvasWidth / 2,
                                    position.second.value - canvasHeight / 2
                                ),
                                style = Fill
                            )
                        }
                    }
                }
            }
            cyclesFromNode.value = listOf()

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
                                graph.removeNode(key)
                                val listToRemove = mutableListOf<Pair<Int, Int>>()
                                for (i in linesToDraw.keys) {
                                    if (i.first == key || i.second == key) {
                                        listToRemove.add(i)
                                    }
                                }
                                for (i in listToRemove) {
                                    linesToDraw.remove(i)
                                }
                                actionStack.add(
                                    Action(
                                        3,
                                        Triple(key, circlesToDraw[key], listToRemove)
                                    )
                                )
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
                size = DpSize(300.dp, 300.dp)
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
                                Pair(
                                    CircleData(
                                        x = position.first.first.toPixels(density),
                                        y = position.first.second.toPixels(density)
                                    ), CircleData(
                                        x = position.second.first.toPixels(density),
                                        y = position.second.second.toPixels(density)
                                    )
                                )
                            }
                            saveToFile(
                                graphMode = saves.graphMode,
                                circlesToDrawInPixels,
                                linesToDrawInPixels,
                                switchState,
                                nodeCounter,
                                selectedFile,
                                flag = 1
                            )
                            openSettings = false
                        }) {
                            Text("Сохранить граф в .json")
                        }
                        Button(onClick = {
                            val circlesToDrawInPixels = circlesToDraw.mapValues { (_, position) ->
                                CircleData(
                                    x = position.first.toPixels(density),
                                    y = position.second.toPixels(density),
                                )
                            }
                            val linesToDrawInPixels = linesToDraw.mapValues { (_, position) ->
                                Pair(
                                    CircleData(
                                        x = position.first.first.toPixels(density),
                                        y = position.first.second.toPixels(density)
                                    ), CircleData(
                                        x = position.second.first.toPixels(density),
                                        y = position.second.second.toPixels(density)
                                    )
                                )
                            }
                            saveToFile(
                                graphMode = saves.graphMode, circlesToDrawInPixels,
                                linesToDrawInPixels,
                                switchState,
                                nodeCounter,
                                selectedFile,
                                flag = 2
                            )
                            openSettings = false
                        }) {
                            Text("Сохранить граф в .db")
                        }
                    }
                }
            }
        )

    }
}

@Composable
fun mainScreen(onStartClick: () -> Unit, onFileSelected: (File?) -> Unit, onGraphModeSelected: (Boolean) -> Unit) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    val saveDirectory = File("src/main/resources/save/")
    var graphMode by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text("Выберите режим графа для создания")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = graphMode,
                onClick = {
                    graphMode = true
                    onGraphModeSelected(true)
                }
            )
            Text("Ориентированный")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = !graphMode,
                onClick = {
                    graphMode = false
                    onGraphModeSelected(false)
                }
            )
            Text("Неориентированный")
        }

        Button(onClick = onStartClick) {
            Text("Построить граф")
        }
        Button(onClick = { selectedFile = chooseFile(saveDirectory); onFileSelected(selectedFile) }) {
            Text("Загрузить сохранение")
        }
        selectedFile?.let { file ->
            onFileSelected(file)
        }
    }
}


fun main() = application {
    val density = LocalDensity.current
    val windowSize = with(density) { DpSize(800.dp.value.toInt().toDp(), 600.dp.value.toInt().toDp()) }

    var showMainScreen by remember { mutableStateOf(true) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var graphMode by remember { mutableStateOf(false) }

    if (showMainScreen) {
        Window(onCloseRequest = ::exitApplication) {
            mainScreen(
                onStartClick = { showMainScreen = false },
                onFileSelected = { file -> selectedFile = file; showMainScreen = false },
                onGraphModeSelected = { mode -> graphMode = mode }
            )
        }
    } else {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = windowSize),
            title = "Connect-a-Lot",
            focusable = true
        ) {
            val initialData = if (selectedFile != null) {
                loadFromFile(selectedFile!!.name)
            } else {
                WindowStateData(
                    graphMode = graphMode,
                    circlesToDraw = mapOf(),
                    linesToDraw = mapOf(),
                    switchState = false,
                    nodeCounter = 0
                )
            }
            app(initialData, selectedFile?.name)
        }
    }
}