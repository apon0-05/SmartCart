package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings

@Composable
fun CartConnectedScreen(
    onBackHome: () -> Unit
) {
    val texts = LocalAppStrings.current
    val cartId = CartConnectionSession.connectedCartId ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF7F0)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛒", fontSize = 56.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = texts.cartConnected,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = texts.cartConnectedDesc,
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = texts.cartId,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Text(
                    text = if (cartId.isNotBlank()) cartId else texts.notAvailable,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F2F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6B2D))
        ) {
            Text(
                text = texts.backToHome,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}