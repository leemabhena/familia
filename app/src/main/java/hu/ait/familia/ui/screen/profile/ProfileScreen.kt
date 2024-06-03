package hu.ait.familia.ui.screen.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import hu.ait.familia.R
import hu.ait.familia.ui.navigation.MainNavigation
import hu.ait.familia.ui.screen.home.UserDetailsState


@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onStartChat: (String) -> Unit,
    onAddUser: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    var currentFilter by remember { mutableStateOf("Members") }

    val loggedInUser by profileViewModel.userDetails.observeAsState()

    LaunchedEffect(loggedInUser) {
        loggedInUser?.let {
            profileViewModel.fetchAllUsersInFamily()
        }
    }


    Scaffold(
        topBar = {
            ProfileAppBar(onAddUser, onSignOut, onNavigateToEdit)
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            when(profileViewModel.userDetailsState) {
                is UserDetailsState.Init -> {}
                is UserDetailsState.Loading -> CircularProgressIndicator()
                is UserDetailsState.UserDetailsSuccess -> {
                    ProfileDetailsContainer(
                        name = profileViewModel.userDetails.value?.get("username").toString(),
                        email = profileViewModel.userDetails.value?.get("email").toString(),
                        profileImage = profileViewModel.userDetails.value?.get("profilePicture").toString()
                    )

                    ProfileFilterChips(
                        onFilterSelected = { filter ->
                            currentFilter = filter
                        }
                    )

                    when (currentFilter) {
                        "Members" -> MemberList(profileViewModel, onStartChat = onStartChat)
                        "Families" -> FamiliesContent()
                        "Settings" -> SettingsContent()
                    }
                }
                is UserDetailsState.Error -> {
                    Text(text = "Error fetching user details")
                }
            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    onAddUser: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = "Profile") },
        
        actions = {
            Image(
                painter = painterResource(id = R.drawable.add_user),
                contentDescription = "Add new user",
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(4.dp)
                    .clickable {
                        onAddUser()
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.edit_profile),
                contentDescription = "Edit profile",
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(4.dp)
                    .clickable {
                        onNavigateToEdit()
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.sign_out),
                contentDescription = "logout",
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(4.dp)
                    .clickable {
                        Firebase.auth.signOut()
                        // Navigate back to login
                        onSignOut()
                    }
            )
        }
        ,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ProfileDetailsContainer(
    profileImage: String? = null,
    name: String = "Lee Mabhena",
    email: String = "lnm1@williams.edu",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
            )
            .padding(16.dp)
    ) {
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

        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = email,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 4.dp)
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFilterChips(onFilterSelected: (String) -> Unit = {}) {
    var memberState by remember { mutableStateOf(true) }
    var familyState by remember { mutableStateOf(false) }
    var settingState by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
        .padding(top = 16.dp)
        .fillMaxWidth()) {
        FilterChip(
            selected = memberState,
            onClick = {
                memberState = true
                familyState = false
                settingState = false
                onFilterSelected("Members")
            },
            label = { Text("Members") },
            border = FilterChipDefaults.filterChipBorder(borderColor = Color.Transparent),
            modifier = Modifier.padding(end = 8.dp)
        )
        FilterChip(
            selected = familyState,
            onClick = {
                memberState = false
                familyState = true
                settingState = false
                onFilterSelected("Families")
            },
            label = { Text("Families") },
            border = FilterChipDefaults.filterChipBorder(borderColor = Color.Transparent),
            modifier = Modifier.padding(end = 8.dp)
        )
        FilterChip(
            selected = settingState,
            onClick = {
                memberState = false
                familyState = false
                settingState = true
                onFilterSelected("Settings")
            },
            label = { Text("Settings") },
            border = FilterChipDefaults.filterChipBorder(borderColor = Color.Transparent)
        )
    }
}

@Composable
fun MemberItem(
    name: String,
    imgUrl: String?,
    userId: String,
    role: String = "1 groups in common",
    onStartChat: (String) -> Unit,
){
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (imgUrl == "null" || imgUrl == null) {
            Image(
                painter = painterResource(id = R.drawable.default_profile),
                contentDescription = "profile",
                modifier = Modifier
                    .size(60.dp)
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
                model = imgUrl,
                contentDescription = "profile",
                modifier = Modifier
                    .size(60.dp)
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(text = name,  fontWeight = FontWeight.Bold)
            Text(text = role)
        }
        OutlinedButton(onClick = {
            onStartChat(MainNavigation.MainScreenGraph.ChatScreen.getRoute(userId))
        }) {
            Text(text = "Chat")
        }
    }
}

@Composable
fun MemberList(
    profileViewModel: ProfileViewModel,
    onStartChat: (String) -> Unit = {}
    ) {
    if (profileViewModel.userDetails.value?.get("currentFamily") == null) {
        return
    }
    val users = profileViewModel.familyMembersState
    if (users is FamilyMembersState.Success) {
        LazyColumn(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(users.members) { member ->
                if (member["userId"].toString() != Firebase.auth.currentUser?.uid!!) {
                    MemberItem(
                        name = member["username"].toString(),
                        imgUrl = member["profilePicture"].toString(),
                        userId = member["userId"].toString(),
                        onStartChat = onStartChat,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Settings")
    }
}

@Composable
fun FamiliesContent() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
            .shadow(elevation = 1.dp,)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Test 1 Family",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "3 Members")
        }
        OutlinedButton(onClick = { /*TODO*/ }) {
            Text(text = "Leave")
        }
    }
}