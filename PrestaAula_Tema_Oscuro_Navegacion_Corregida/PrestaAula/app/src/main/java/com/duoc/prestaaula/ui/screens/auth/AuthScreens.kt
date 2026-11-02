package com.duoc.prestaaula.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.viewmodel.LoginViewModel
import com.duoc.prestaaula.viewmodel.RegisterViewModel

@Composable
private fun BrandHeader(subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("PrestaAula", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Colegio Marcela Paz",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun LoginScreen(onRegister: () -> Unit) {
    val vm = appViewModel { LoginViewModel(it.userRepository, it.sessionManager) }
    val ui by vm.ui.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppearAnimated {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandHeader("Inicia sesión")
                Spacer(Modifier.height(16.dp))
                ValidatedTextField(
                    value = ui.email,
                    onValueChange = vm::onEmailChange,
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    error = ui.emailError,
                    keyboardType = KeyboardType.Email
                )
                ValidatedTextField(
                    value = ui.password,
                    onValueChange = vm::onPasswordChange,
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    error = ui.passwordError,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    passwordVisible = ui.passwordVisible,
                    onTogglePassword = vm::togglePasswordVisibility,
                    onImeDone = vm::submit
                )
                FeedbackBanner(ui.generalError)
                LoadingButton(
                    text = "Ingresar",
                    loading = ui.loading,
                    onClick = vm::submit,
                    icon = Icons.AutoMirrored.Filled.Login
                )
                TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("¿Eres docente y no tienes cuenta? Regístrate")
                }
                Spacer(Modifier.height(8.dp))
                DemoAccountsCard()
            }
        }
    }
}

@Composable
private fun DemoAccountsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text("Cuentas de demostración", fontWeight = FontWeight.Bold)
            }
            Text("Encargado CRA: cra@marcelapaz.cl / Cra12345", style = MaterialTheme.typography.bodySmall)
            Text("Docente: docente@marcelapaz.cl / Docente123", style = MaterialTheme.typography.bodySmall)
            Text("Jefe UTP: utp@marcelapaz.cl / Utp12345", style = MaterialTheme.typography.bodySmall)
            Text("Jefe Administración: admin@marcelapaz.cl / Admin12345", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    val vm = appViewModel { RegisterViewModel(it.userRepository, it.sessionManager) }
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Registro de docente", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppearAnimated {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandHeader("Crea tu cuenta")
                    Spacer(Modifier.height(12.dp))
                    ValidatedTextField(
                        value = ui.name,
                        onValueChange = vm::onNameChange,
                        label = "Nombre completo",
                        leadingIcon = Icons.Default.Person,
                        error = ui.nameError
                    )
                    ValidatedTextField(
                        value = ui.email,
                        onValueChange = vm::onEmailChange,
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email,
                        error = ui.emailError,
                        keyboardType = KeyboardType.Email
                    )
                    ValidatedTextField(
                        value = ui.password,
                        onValueChange = vm::onPasswordChange,
                        label = "Contraseña",
                        leadingIcon = Icons.Default.Lock,
                        error = ui.passwordError,
                        helper = "Mínimo 8 caracteres, con mayúscula, minúscula y número",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = ui.passwordVisible,
                        onTogglePassword = vm::togglePasswordVisibility
                    )
                    ValidatedTextField(
                        value = ui.confirm,
                        onValueChange = vm::onConfirmChange,
                        label = "Repetir contraseña",
                        leadingIcon = Icons.Default.LockReset,
                        error = ui.confirmError,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        isPassword = true,
                        passwordVisible = ui.passwordVisible,
                        onTogglePassword = vm::togglePasswordVisibility,
                        onImeDone = vm::submit
                    )
                    FeedbackBanner(ui.generalError)
                    LoadingButton(
                        text = "Crear cuenta",
                        loading = ui.loading,
                        onClick = vm::submit,
                        icon = Icons.Default.PersonAdd
                    )
                }
            }
        }
    }
}
