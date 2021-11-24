package com.jjuncoder.composetutorial.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun SampleApp() {
    Surface(Modifier.fillMaxSize()) {
        SampleAppContent()
    }
}


@Composable
fun SampleAppContent() {
    ConstraintLayout(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        val (list, inputField) = createRefs()

        val messages = remember { mutableStateListOf<String>() }

        MessageList(
            messages = messages,
            modifier = Modifier
                .constrainAs(list) {
                    top.linkTo(parent.top)
                    bottom.linkTo(inputField.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
        )

        MessageInputField(
            modifier = Modifier
                .constrainAs(inputField) {
                    top.linkTo(list.bottom)
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
        ) {
            messages.add(it)
        }
    }
}


@Composable
fun MessageList(messages: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    Column(modifier = modifier) {

        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            StaggeredGrid(
                modifier = Modifier
                    .padding(20.dp)
                    .background(color = Color.DarkGray)
            ) {
                messages.forEach {
                    Message(it)
                }
            }
        }

        LazyColumn(state = listState, modifier = Modifier) {
            items(messages) {
                Message(it)
            }
        }
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3, content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->

        val rowWidths = IntArray(rows) { 0 }
        val rowHeights = IntArray(rows) { 0 }

        val placeables = measurables.mapIndexed { index, measurable ->

            // STEP 1. measure children
            val placeable = measurable.measure(constraints)

            val row = index % rows
            rowWidths[row] += placeable.width
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)

            placeable
        }

        // STEP 2. calculate my size. (width, height)
        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth))
            ?: constraints.minWidth

        val height = rowHeights.sumOf { it }
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        // STEP 3. calculate children's Y position
        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i - 1] + rowHeights[i - 1]
        }

        // STEP 4. determine my final size.
        layout(width, height) {
            val rowX = IntArray(rows) { 0 }
            placeables.forEachIndexed { index, placeable ->
                val row = index % rows

                // STEP 5-1. place children to draw it on screen.
                placeable.placeRelative(x = rowX[row], y = rowY[row])

                // STEP 5-2. calculate next children's X position (by accumulating width)
                rowX[row] += placeable.width
            }
        }
    }
}

@Composable
fun MessageInputField(modifier: Modifier = Modifier, onDone: (String) -> Unit) {
    var text by remember {
        mutableStateOf("")
    }

    Column(modifier = modifier) {

        Divider(modifier = Modifier.fillMaxWidth())

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            keyboardActions = KeyboardActions(onDone = {
                onDone(text)
                text = ""
            }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}

@Composable
fun Message(message: String) {
    Text(
        message,
        modifier = Modifier
            .padding(8.dp)
            .background(color = Color.LightGray, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White
    )
}

fun Modifier.firstBaselineToTop(firstBaselineToTop: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    check(placeable[FirstBaseline] != AlignmentLine.Unspecified)

    val firstBaseline = placeable[FirstBaseline]
    val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
    val height = placeable.height + placeableY

    layout(placeable.width, height) {
        placeable.placeRelative(0, placeableY)
    }
}