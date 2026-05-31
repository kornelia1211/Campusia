package com.example.campusia.components

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusia.ui.theme.PrimaryPurple

@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = PrimaryPurple,
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(14.dp),
    height: Dp = 52.dp,
    fontSize: TextUnit = 17.sp,
    iconSize: Dp = 24.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = contentPadding
    ) {
        if (icon != null){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    maxLines = 1,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize,
                        textAlign = TextAlign.Center
                    )
                )}
            }
        else{
            Text(
                text = text,
                maxLines = 1,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}