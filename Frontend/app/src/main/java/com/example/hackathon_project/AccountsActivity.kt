package com.example.hackathon_project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.hackathon_project.ui.theme.Hackathon_projectTheme
import kotlinx.coroutines.launch

class AccountsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AccountsContent(
                        modifier = Modifier.padding(innerPadding),
                        onSubmitAccounts = { accountsList ->
                            lifecycleScope.launch {
                                val success = ApiClient.sendAccounts(accountsList)
                                if (success) {
                                    Toast.makeText(this@AccountsActivity, "Accounts sent successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@AccountsActivity, "Failed to send accounts.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountsContent(modifier: Modifier = Modifier, onSubmitAccounts: (List<String>) -> Unit = {}) {
    var account1 by remember { mutableStateOf("") }
    var account2 by remember { mutableStateOf("") }
    var account3 by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Accounts",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = account1,
                    onValueChange = { account1 = it },
                    label = { Text("Account 1") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = account2,
                    onValueChange = { account2 = it },
                    label = { Text("Account 2") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = account3,
                    onValueChange = { account3 = it },
                    label = { Text("Account 3") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val list = listOf(account1, account2, account3).filter { it.isNotBlank() }
                    if (list.isNotEmpty()) {
                        onSubmitAccounts(list)
                    } else {
                        Toast.makeText(context, "Please enter at least one account", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text("Send Accounts to Backend")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun AccountsPreview() {
    Hackathon_projectTheme {
        AccountsContent()
    }
}
