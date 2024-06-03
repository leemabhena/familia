package hu.ait.familia.ui.screen.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import hu.ait.familia.R
import hu.ait.familia.data.firebase.User


@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    onJoiningFamily: () -> Unit = {},
    onCreatingFamily: () -> Unit = {}
) {

    var isDialogOpen by remember {
        mutableStateOf(false)
    }

    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.homescreen_lottie
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {

        LottieAnimation(
            composition = preloaderLottieComposition,
            progress = preloaderProgress,
            modifier = Modifier.size(400.dp)
        )

        Text(
            text = "Welcome, ${homeScreenViewModel.userDetails.value?.get("username") ?: ""}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "To get started, create a new family or join an existing family.",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(.9f)
        )

        OutlinedButton(
            onClick = { onJoiningFamily() },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(.9f)
                .height(40.dp)

        ) {
            Text(text = "Join existing.")
        }

        Button(
            onClick = { isDialogOpen = true },
            modifier = Modifier
                .fillMaxWidth(.9f)
                .height(40.dp)
            ) {
            Text(text = "Create new.")
        }

        if (isDialogOpen) {
            CreateFamilyDialog(homeScreenViewModel) {
                isDialogOpen = false
            }
        }

        when(homeScreenViewModel.familyUiState) {
            is FamilyUiState.Init -> {}
            is FamilyUiState.Loading -> CircularProgressIndicator()
            is FamilyUiState.UploadSuccess -> {
                Text(text = "Family created successfully")
                onCreatingFamily()
            }
            is FamilyUiState.Error -> {
                (homeScreenViewModel.familyUiState as FamilyUiState.Error).error?.let {
                    Text(
                        text =
                        it
                    )
                } ?: "Error creating family. Please try again."
            }
        }

    }
}

@Composable
fun CreateFamilyDialog(
    homeScreenViewModel: HomeScreenViewModel,
    //onCreatingFamily: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    var familyName by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp)
        ) {
            Text(text = "Create new family")
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = familyName,
                onValueChange = {familyName = it},
                label = {
                    Text(text = "Family Name")
                },
                singleLine = true
            )
            Row {
                TextButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = "Cancel")
                }

                TextButton(
                    onClick = {
                        homeScreenViewModel.createNewFamily(familyName)
                        //onCreatingFamily()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = "Add")
                }
            }

        }
    }
}

