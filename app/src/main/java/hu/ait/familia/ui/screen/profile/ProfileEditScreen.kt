package hu.ait.familia.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import hu.ait.familia.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {}
) {

    // Observe changes and provide empty string as default if null
    val userDetails = profileViewModel.userDetails.observeAsState().value

    val username = userDetails?.get("username")?.toString() ?: ""
    val email = userDetails?.get("email")?.toString() ?: ""
    val profileImage = userDetails?.get("profilePicture")?.toString() ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Edit Profile") },
                navigationIcon = {
                                 IconButton(onClick = { onBack() }) {
                                     Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                                 }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            ProfileEditImage(profileImage = profileImage)
            ProfileEditText(username, email)
        }
    }
}

@Composable
fun ProfileEditImage(profileImage: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
            )
            .padding(bottom = 32.dp)
    ) {
        Box(modifier = Modifier) {
            if (profileImage == null || profileImage == "null") {
                Image(
                    painter = painterResource(id = R.drawable.default_profile),
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(90.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(50)
                        )
                        .clip(RoundedCornerShape(50))

                    ,
                    contentScale = ContentScale.FillBounds
                )
            } else {
                AsyncImage(
                    model = profileImage,
                    contentDescription = "Profile image",
                    modifier = Modifier
                        .size(90.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(50)
                        )
                        .clip(RoundedCornerShape(50))
                    ,
                    contentScale = ContentScale.FillBounds
                )
            }
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun ProfileEditText(username: String, userEmail: String) {

    val currentName = rememberUpdatedState(username)
    val currentEmail = rememberUpdatedState(userEmail)

    var name by remember { mutableStateOf(currentName.value) }
    var email by remember { mutableStateOf(currentEmail.value) }
    var password by remember { mutableStateOf("..........") }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {

        // Fields
        CreateTextField(
            name,
            { name = it },
            "Name",
            leadingIcon = {
                Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null
                )
        })
        CreateTextField(
            email,
            { email = it },
            "Email",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            }
        )
        CreateTextField(
            password,
            { password = it },
            "Password",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null
                )
            },

        )

        // Save Button
        Button(
            onClick = { /* Handle save action here */ },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .height(48.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Save Edit")
        }
    }
}

@Composable
fun CreateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit) = {},
    trailingIcon: @Composable (() -> Unit) = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}