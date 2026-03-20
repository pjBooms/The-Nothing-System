package com.excelsior.nothing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.excelsior.nothing.DynamicComponent
import com.excelsior.nothing.FrameWindowState
import com.excelsior.nothing.Kernel

/**
 * Renders a FrameWindowState as a panel with absolute-positioned components.
 */
@Composable
fun DynamicPanel(state: FrameWindowState, onFocusGain: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD4D0C8))
            .clickable { onFocusGain() }
    ) {
        for (component in state.components) {
            key(component.name) {
                when (component) {
                    is DynamicComponent.ButtonComp -> {
                        Button(
                            onClick = { Kernel.executeCommand(component.cmd) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .offset(component.x.dp, component.y.dp)
                                .size(component.w.dp, component.h.dp)
                        ) {
                            Text(
                                text = component.text,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                    is DynamicComponent.TextFieldComp -> {
                        Box(
                            modifier = Modifier
                                .offset(component.x.dp, component.y.dp)
                                .size(component.w.dp, component.h.dp)
                                .background(Color.White)
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = component.value.value,
                                onValueChange = { component.value.value = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}
