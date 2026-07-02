package com.example.nlcpapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getBlueButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = Color(0xFF2563EB),
    contentColor = Color.White,
    disabledContainerColor = Color(0xFF93C5FD),
    disabledContentColor = Color.White
)
