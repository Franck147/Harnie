package com.harnie.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harnie.app.core.model.OrderStatus
import com.harnie.app.ui.theme.InfoBlue
import com.harnie.app.ui.theme.LossRed
import com.harnie.app.ui.theme.ProfitGreen
import com.harnie.app.ui.theme.WarningAmber

@Composable
fun StatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.OPEN -> InfoBlue.copy(alpha = 0.15f) to InfoBlue
        OrderStatus.IN_PROGRESS -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
        OrderStatus.COMPLETED -> ProfitGreen.copy(alpha = 0.15f) to ProfitGreen
        OrderStatus.CANCELLED -> Color.Gray.copy(alpha = 0.15f) to Color.Gray
        OrderStatus.DISPUTED -> LossRed.copy(alpha = 0.15f) to LossRed
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}