package com.example.nlcpapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB), // Синий цвет как в Figma
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF93C5FD),
            disabledContentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}
