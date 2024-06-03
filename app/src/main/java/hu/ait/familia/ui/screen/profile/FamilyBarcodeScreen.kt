package hu.ait.familia.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import hu.ait.familia.data.firebase.Family

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyBarcodeScreen(profileViewModel: ProfileViewModel = viewModel(), onBack: () -> Unit) {

    val userDetails = profileViewModel.userDetails.observeAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                                 IconButton(onClick = { onBack() }) {
                                     Icon(
                                         imageVector = Icons.Default.ArrowBack,
                                         contentDescription = "Back")
                                 }
                },
                title = { Text(text = "Join Family") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()) {
            if (userDetails.value?.get("currentFamily") == "null") {
                CircularProgressIndicator()
            } else {
                AsyncImage(
                    model = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${userDetails.value?.get("currentFamily")}",
                    contentDescription = "family barcode",
                    modifier = Modifier
                        .size(250.dp)
                )
            }
        }
    }
}