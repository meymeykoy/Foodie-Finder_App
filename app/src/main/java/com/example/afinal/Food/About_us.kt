package com.example.afinal.Food

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.afinal.R
import kotlinx.coroutines.launch

@Composable
fun AboutUs(nc: NavHostController, vm: TheFoodSoViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    fun handleSubmit() {
        isSubmitting = true
        email = ""
        message = ""
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(2000)
            isSubmitting = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding( start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD64174))
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "About Us",
                fontFamily = FontFamily.Cursive,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.girl),
            contentDescription = "Chef Image",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .border(width = 3.dp, color = Color(0xFFD64174))
                .padding(8.dp)
                .background(color = Color.White)
                .fillMaxWidth()
        ) {
            Text(
                text = "We are a team of passionate chefs dedicated to providing the best culinary experiences. Our mission is to delight your taste buds with innovative and delicious dishes, made from the freshest ingredients. Join us in exploring the world of gourmet cuisine!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(8.dp)
            )
        }
        Text(
            text = "Our Location",
            fontFamily = FontFamily.Cursive,
            style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFFD64174)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.qr),
            contentDescription = "Map Image",
            contentScale = ContentScale.Fit,

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)

        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Contact Us",
            fontFamily = FontFamily.Cursive,
            style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFFD64174)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 16.dp),
            maxLines = 5
        )
        Button(
            onClick = { handleSubmit() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD64174),
                contentColor = Color.White
            ),
            enabled = !isSubmitting
        ) {
            Text(text = if (isSubmitting) "Sending..." else "Send")
        }
        if (isSubmitting) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}


