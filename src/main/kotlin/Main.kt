import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
@Preview
fun app() {
    val windowState = rememberWindowState()
    val density = LocalDensity.current
    var windowSize by remember { mutableStateOf(windowState.size) }
    var windowWidth by remember { mutableStateOf(windowState.size.width) }
    var windowHeight by remember { mutableStateOf(windowState.size.height) }

    var circles by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }
    val circleRadius: Dp = 20.dp

    var selectedOption by remember { mutableStateOf("Создать узлы") }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.White).pointerInput(Unit) {
            detectTapGestures(onTap = { offset ->
                if (selectedOption == "Создать узлы") {
                    circles += Pair(offset.x - windowWidth.value / 2, offset.y - windowHeight.value / 2)
                }
            })
        }.onSizeChanged { newSize ->
            val temp = with(density) { DpSize(newSize.width.toDp(), newSize.height.toDp()) }
            if (temp != windowSize) {
                windowSize = temp
                windowWidth = with(density) { newSize.width.toDp() }
                windowHeight = with(density) { newSize.height.toDp() }
            }
        }) {
            circles.forEach { (x, y) ->
                Canvas(modifier = Modifier.align(Alignment.Center)) {
                    drawCircle(
                        color = Color.Red,
                        radius = circleRadius.value,
                        center = Offset(x, y),
                        style = Fill
                    )
                }
            }
        }
    }
}
fun main() = application {
    Window(onCloseRequest = ::exitApplication, state = rememberWindowState()) {
        app()
    }
}