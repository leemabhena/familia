package hu.ait.familia.ui.screen.login

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import hu.ait.familia.R
import kotlinx.coroutines.launch
import kotlin.math.sign

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onNavigateToSignUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var showPassword by remember {
        mutableStateOf(false)
    }
    
    var isEmailValid by remember {
        mutableStateOf(true)
    }

    var isValidPassword by remember {
        mutableStateOf(true)
    }

    val coroutineScope = rememberCoroutineScope()

    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.login
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    ConstraintLayout(
        modifier = Modifier.
        fillMaxWidth()
    ) {
        val startGuide = createGuidelineFromStart(0.1f)
        val endGuide = createGuidelineFromEnd(0.1f)
        val topGuide = createGuidelineFromTop(0.3f)
        val bottomGuide = createGuidelineFromBottom(16.dp)

        val (wavesImg, loginHeading, emailField, passwordField, loginBtn, loginState, signupText) = createRefs()

        LottieAnimation(
            composition = preloaderLottieComposition,
            progress = preloaderProgress,
            modifier = Modifier
                .constrainAs(wavesImg) {
                    top.linkTo(parent.top)
                    bottom.linkTo(topGuide)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
            }
        )


        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .constrainAs(loginHeading) {
                    top.linkTo(topGuide)
                    start.linkTo(startGuide)
                }

        ) {
            Text(
                text = stringResource(id = R.string.login_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(id = R.string.login_screen_text))
        }

        // Email field
        InputTextField(
            email = email,
            onEmailChange = { input ->
                email = input
                isEmailValid = isValidEmail(input)
            },
            isEmailValid = isEmailValid,
            modifier = Modifier
                .padding(top = 16.dp)
                .constrainAs(emailField) {
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                    top.linkTo(loginHeading.bottom)
                    width = Dimension.fillToConstraints
                })

        // Password field
        PasswordInput(
            password = password,
            onPasswordChange = { input: String ->
                password = input
                isValidPassword = isValidPassword(input)
            },
            isValidPassword = isValidPassword,
            showPassword = showPassword,
            onToggleShowPassword = {
                showPassword = !showPassword
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .constrainAs(passwordField) {
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                    top.linkTo(emailField.bottom)
                    width = Dimension.fillToConstraints
                }
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    if (isValidEmail(email) && isValidPassword(password)) {
                        val result = loginViewModel.loginUser(email, password)
                        if (result?.user != null) {
                            onLoginSuccess()
                        }
                    }
                }
                      },
            modifier = Modifier
                .height(64.dp)
                .width(180.dp)
                .padding(top = 16.dp)
                .constrainAs(loginBtn) {
                    top.linkTo(passwordField.bottom)
                    end.linkTo(endGuide)
                }
        ) {
            Text(text = "LOGIN")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .constrainAs(signupText) {
                    bottom.linkTo(bottomGuide)
                    start.linkTo(startGuide)
                }
        ) {
            Text(text = stringResource(id = R.string.login_screen_signup))
            TextButton(onClick = { onNavigateToSignUp() }) {
                Text(text = stringResource(id = R.string.sign_screen_title))
            }
        }

        // Login functionality
        Column(
            modifier = Modifier
                .constrainAs(loginState) {
                    bottom.linkTo(signupText.top)
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                }
        ) {
            when (loginViewModel.loginUiState) {
                is LoginUiState.Init -> {}
                is LoginUiState.Loading -> CircularProgressIndicator()
                is LoginUiState.LoginSuccess -> Text(text = "Login successful") // animation here
                is LoginUiState.Error -> Text(
                    text = "Error: ${(loginViewModel.loginUiState as LoginUiState.Error).error }")
            }
        }
        
    }
}

@Composable
fun InputTextField(
    email: String,
    onEmailChange: (String) -> Unit,
    isEmail: Boolean = true,
    isEmailValid: Boolean,
    modifier: Modifier
) {
    TextField(
        value = email,
        onValueChange = {
            onEmailChange(it)
        },
        singleLine = true,
        isError = !isEmailValid,
        supportingText = {
            if (!isEmailValid) {
                if (isEmail) Text(text = "Invalid email address") else Text(text = "Username should be at least 5 characters")
            }
        },
        trailingIcon = {
            if (!isEmailValid) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            } else if (isEmailValid && email.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = if (isEmail) Icons.Default.Email else Icons.Default.AccountCircle,
                contentDescription = "Email"
            )
        },
        label = {
            Text(text = if (isEmail) "Email" else "Username")
        },
        modifier = modifier
    )
}

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    isValidPassword: Boolean,
    showPassword: Boolean,
    modifier: Modifier,
    onToggleShowPassword: () -> Unit
) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        singleLine = true,
        isError = !isValidPassword,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password"
            )
        },
        supportingText = {
            if (!isValidPassword) {
                Text(text = "Password should be at least 6 characters.")
            }
        },
        label = { Text(text = "Password") },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleShowPassword) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.Clear else Icons.Default.Info,
                    contentDescription = if (showPassword) "Hide password" else "Show password"
                )
            }
        },
        modifier = modifier
    )
}


// check if email is valid
fun isValidEmail(email: String): Boolean {
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    return password.isNotEmpty() && password.length >= 6
}