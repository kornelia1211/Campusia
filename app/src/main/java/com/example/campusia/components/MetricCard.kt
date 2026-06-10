package com.example.campusia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = ScreenBackground,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = FieldBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(23.dp),
                    tint = PrimaryPurple
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}