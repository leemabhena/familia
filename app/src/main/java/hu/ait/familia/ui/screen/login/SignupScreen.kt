package hu.ait.familia.ui.screen.login

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import hu.ait.familia.R


@Composable
fun SignupScreen(
    signUpViewModel: SignUpViewModel = viewModel(),
    onSignInSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var username by remember {
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

    var isUsernameValid by remember {
        mutableStateOf(true)
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val handleImagePicked = { uri: Uri? ->
        imageUri = uri
    }

    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.signup
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
        val topGuide = createGuidelineFromTop(0.2f)
        val bottomGuide = createGuidelineFromBottom(16.dp)

        val (wavesImg, loginHeading, profile, nameField, emailField, passwordField, loginBtn, signUpState, signupText) = createRefs()

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
                .constrainAs(loginHeading) {
                    top.linkTo(topGuide)
                    start.linkTo(startGuide)
                }
        ) {
            Text(
                text = stringResource(id = R.string.sign_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(id = R.string.sign_screen_text))
        }

        // Profile image picker
        ProfileImagePicker(
            imageUri = imageUri,
            onImagePicked = handleImagePicked,
            modifier = Modifier
                .padding(top = 12.dp)
                .constrainAs(profile) {
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                    top.linkTo(loginHeading.bottom)
                }
        )

        // User name field
        InputTextField(
            email = username,
            onEmailChange = { input ->
                username = input
                isUsernameValid = input.length >= 5
            },
            isEmail = false,
            isEmailValid = isUsernameValid,
            modifier = Modifier
                .padding(top = 16.dp)
                .constrainAs(nameField) {
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                    top.linkTo(profile.bottom)
                    width = Dimension.fillToConstraints
                })

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
                    top.linkTo(nameField.bottom)
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

        // Login functionality
        Column(
            modifier = Modifier
                .constrainAs(signUpState) {
                    bottom.linkTo(signupText.top)
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                }
        ) {
            when (signUpViewModel.signUpUiState) {
                is SignUpUiState.Init -> {}
                is SignUpUiState.Loading -> CircularProgressIndicator()
                is SignUpUiState.RegisterSuccess -> {
                    Text(text = "Register OK")
                    // Go to the main screen on success
                    onSignInSuccess()
                }
                is SignUpUiState.Error -> Text(
                    text = "Error: ${(signUpViewModel.signUpUiState as SignUpUiState.Error).error }")
            }
        }

        Button(
            onClick = {
                      if (isValidPassword(password) && isValidEmail(email) && username.length >= 5) {
                          // Create a new account
                          if (imageUri == null) {
                              signUpViewModel.registerUser(
                                  username = username,
                                  email = email,
                                  password = password
                              )
                          } else {
                              signUpViewModel.registerWithProfile(
                                  username = username,
                                  email = email,
                                  password = password,
                                  imageUri = imageUri!!
                              )
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
            Text(text = "Create Account")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .constrainAs(signupText) {
                    bottom.linkTo(bottomGuide)
                    start.linkTo(startGuide)
                }
        ) {
            Text(text = stringResource(id = R.string.sign_screen_login))
            TextButton(onClick = { onNavigateToLogin() }) {
                Text(text = stringResource(id = R.string.login_screen_title))
            }
        }

    }
}


@Composable
fun ProfileImagePicker(
    imageUri: Uri?,
    onImagePicked: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {

    // Launcher for selecting image
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        onImagePicked(uri)
    }

    Box(
        modifier = modifier
    ) {
        // Use AsyncImage to display either the selected image or the default
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(50))
                    .border(
                        color = MaterialTheme.colorScheme.onSurface,
                        width = 4.dp,
                        shape = RoundedCornerShape(50)
                    )
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_profile),
                contentDescription = "Profile image",
                modifier = Modifier
                    .size(72.dp)
                    .border(
                        color = MaterialTheme.colorScheme.onSurface,
                        width = 4.dp,
                        shape = RoundedCornerShape(50)
                    )
            )
        }


        // Button to trigger image selection
        Box(
            modifier = Modifier
                .offset(x = 12.dp, y = 12.dp)
                .align(Alignment.BottomEnd)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                )
                .padding(8.dp)
                .clickable { launcher.launch("image/*") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.camera_add),
                contentDescription = "Select Image",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

