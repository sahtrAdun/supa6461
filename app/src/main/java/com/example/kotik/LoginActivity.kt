package com.example.kotik

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Login()
        }
    }
    @Composable
    fun Login() {
        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }
        var isChecked by remember { mutableStateOf(false) }
        val checkedState = remember { mutableStateOf(false) }
        val composableScope = rememberCoroutineScope()

        val users = remember { mutableStateListOf<User>() }

        suspend fun getAllUsers() {
            withContext(Dispatchers.IO) {
                val results = supabase.from("users_public").select().decodeList<User>()
                users.addAll(results)
            }
        }

        suspend fun isAdmin(email: String): Boolean {
            val res = supabase.from("users_public").select {
                select()
                filter { User::email eq email }
            }.decodeSingle<User>()
            return res.isAdmin?: false
        }

        suspend fun logIn(email: String, password: String): Boolean {
            val res = supabase.from("users_public").select {
                select()
                filter { User::email eq email }
                filter { User::password eq password }
            }
            return res.data.isEmpty()?: true
        }

        suspend fun getIdByEmail(email: String): Int {
            val res = supabase.from("users_public").select {
                select()
                filter { User::email eq email }
            }.decodeSingle<User>()
            return res.id?: 0
        }

        var res: Boolean by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            getAllUsers()
            val loggedIn = withContext(Dispatchers.IO) {
                logIn("admin@admin.com", "1234_00")
            }
            res = loggedIn
        }

        Column(
                modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                    modifier = Modifier
                            .padding(start = 25.dp, end = 25.dp),
            ) {
                item {
                    Button(
                            onClick = {
                                startActivity(Intent(this@LoginActivity, MapActivity::class.java))
                                finish()
                            }
                    ) {
                        Text(text = "map")
                    }
                }
                item {
                    Column(
                            modifier = Modifier
                                    .padding(top = 100.dp),
                    ) {
                        Text(
                                text = "Welcome Back",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                        )
                        Text(
                                text = "Fill in your email and password to continue",
                                fontSize = 16.sp,
                                color = Color(android.graphics.Color.parseColor("#A7A7A7"))
                        )
                    }
                }
                item {
                    var isEmailFocused by remember { mutableStateOf(false) }
                    OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text("Email") },
                            placeholder = { Text("Enter your email") },
                            singleLine = true,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp)
                                    .onFocusChanged { isEmailFocused = it.isFocused },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    )
                }
                item {
                    var isPasswordFocused by remember { mutableStateOf(false) }
                    OutlinedTextField(
                            value = userPassword,
                            onValueChange = { userPassword = it },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 15.dp)
                                    .onFocusChanged { isPasswordFocused = it.isFocused },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done),

                            )
                }
                item {
                    Row(
                            modifier = Modifier.padding(top = 15.dp),
                    ) {
                        Checkbox(
                                checked = checkedState.value,
                                onCheckedChange = {
                                    checkedState.value = it
                                    isChecked = !isChecked},
                                colors = CheckboxDefaults.colors(Color(android.graphics.Color.parseColor("#0560FA")))
                        )
                        Text (
                                modifier = Modifier.padding(top = 15.dp),
                                text = "Remember password",
                                fontSize = 14.sp
                        )
                        Text (
                                modifier = Modifier
                                        .padding(top = 15.dp, start = 55.dp)
                                        .clickable {
                                            startActivity(Intent(this@LoginActivity, OtpActivity::class.java))
                                            finish()
                                        },
                                text = "Forgot Password",
                                fontSize = 14.sp,
                                color  = Color(android.graphics.Color.parseColor("#0560FA"))
                        )
                    }
                }
                item {
                    Button(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 195.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                    Color(android.graphics.Color.parseColor("#0560FA"))
                            ),
                            onClick = {
                                composableScope.launch {
                                    if (!logIn(userEmail, userPassword)) {
                                        try {
                                            val pref = getSharedPreferences("firstUserData", Context.MODE_PRIVATE)
                                            pref.edit().putBoolean("isUserSignIn", true).apply()
                                            pref.edit().putBoolean("isUserSignUp", true).apply()

                                            getSharedPreferences("firstUserData", Context.MODE_PRIVATE)
                                                    .edit().putBoolean("isUserFirstTime", false).apply()

                                            val idUser = withContext(Dispatchers.IO) {
                                                getIdByEmail(userEmail)
                                            }

                                            getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                                    .edit().putString("userId", idUser.toString()).apply()


                                            withContext(Dispatchers.IO) {
                                                if (isAdmin(userEmail)) {
                                                    getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                                            .edit().putBoolean("isAdmin", true).apply()

                                                    startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                                                    finish()

                                                    userEmail = ""
                                                    userPassword = ""
                                                } else {
                                                    getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                                            .edit().putBoolean("isAdmin", false).apply()

                                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                                    finish()

                                                    userEmail = ""
                                                    userPassword = ""
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                        this@LoginActivity,
                                                        "Error",
                                                        LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                    this@LoginActivity,
                                                    "Invalid email or password",
                                                    LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                    ) {
                        Text(text = "Sign In")
                    }
                }
                item {
                    Row (
                            modifier = Modifier
                                    .padding(top = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Already have an account?",
                                color = Color(android.graphics.Color.parseColor("#A7A7A7"))
                        )
                        Text(
                                modifier = Modifier
                                        .clickable {
                                            userEmail = ""
                                            userPassword = ""

                                            startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
                                            finish()
                                        }
                                        .padding(start = 5.dp),
                                text = "Sign Up",
                                color = Color(android.graphics.Color.parseColor("#0560FA"))
                        )
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        Login()
    }
}