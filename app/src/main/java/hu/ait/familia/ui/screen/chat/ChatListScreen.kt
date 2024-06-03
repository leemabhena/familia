package hu.ait.familia.ui.screen.chat

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import hu.ait.familia.R
import hu.ait.familia.ui.navigation.MainNavigation
import hu.ait.familia.ui.screen.profile.FamilyMembersState
import hu.ait.familia.ui.screen.profile.ProfileViewModel
import hu.ait.familia.util.getChatTimeDisplay


@Composable
fun ChatListScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToChat: (String) -> Unit = {}
) {

    val loggedInUser by profileViewModel.userDetails.observeAsState()

    LaunchedEffect(loggedInUser) {
        loggedInUser?.let {
            profileViewModel.fetchAllUsersInFamily()
        }
    }

    var searchValue by remember {
        mutableStateOf("")
    }

    var textFieldFocused by remember {
        mutableStateOf(false)
    }

    // Will help me in removing focus from the text field
    val focusManager = LocalFocusManager.current

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val startGuide = createGuidelineFromStart(0.05f)
        val endGuide = createGuidelineFromEnd(0.05f)

        val (topBar, chatList, searchBar) = createRefs()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.constrainAs(topBar) {
                top.linkTo(parent.top)
                start.linkTo(startGuide)
                end.linkTo(endGuide)
                width = Dimension.fillToConstraints
            }
        ) {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircle,
                        contentDescription = "start new chat"
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchValue,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            onValueChange = {
                searchValue = it
                textFieldFocused = true
            },
            trailingIcon = {
                if (textFieldFocused) {
                    IconButton(onClick = {
                        searchValue = ""
                        textFieldFocused = false
                        // Remove focus from the text field
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "clear",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(searchValue) // Handle the search operation
                }
            ),
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .constrainAs(searchBar) {
                    top.linkTo(topBar.bottom)
                    start.linkTo(startGuide)
                    end.linkTo(endGuide)
                    width = Dimension.fillToConstraints
                }
        )

        // chat list here
        ChatList(profileViewModel, modifier = Modifier.constrainAs(chatList) {
            top.linkTo(searchBar.bottom)
            start.linkTo(startGuide)
            end.linkTo(endGuide)
            width = Dimension.fillToConstraints
        },
            onNavigateToChat
            )



    }

}

@Composable
fun ChatList(
    profileViewModel: ProfileViewModel,
    modifier:Modifier = Modifier,
    onNavigateToChat: (String) -> Unit = {}
    ) {

    if (profileViewModel.userDetails.value?.get("currentFamily") == null) {
        return
    }
    val users = profileViewModel.familyMembersState

    if (users is FamilyMembersState.Success) {
        LazyColumn(
            modifier = modifier
        ) {
            items(users.members) { member ->
                if (member["userId"].toString() != Firebase.auth.currentUser?.uid!!) {
                    val data = ChatListItemData(
                        name = member["username"].toString(),
                        timestamp = "2024-04-25T14:30:00",
                        lastMessage = "Good morning",
                        isOnline = true,
                        userId = member["userId"].toString(),
                        profileImgUrl = member["profilePicture"].toString(),
                    )
                    ChatListItem(data, onNavigateToChat)
                }

            }

        }
    }
}

// Function to call when the search is initiated in the text field
fun onSearch(value: String) {
    Log.d("onSearch", value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListItem(
    chatListItemData: ChatListItemData,
    onNavigateToChat: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onNavigateToChat(MainNavigation.MainScreenGraph.ChatScreen.getRoute(chatListItemData.userId))
            }
    ) {
        Box(modifier = Modifier) {
            if (chatListItemData.profileImgUrl == null || chatListItemData.profileImgUrl.toString() == "null" ) {
                Image(
                    painter = painterResource(id = R.drawable.default_profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = chatListItemData.profileImgUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (chatListItemData.isOnline || chatListItemData.unReadMsg > 0) {
                Badge(
                    modifier = Modifier
                        .border(1.dp, color = Color.White, shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape),
                    containerColor = if (!chatListItemData.isOnline)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        Color.Green
                ) {
                    if (chatListItemData.unReadMsg > 0) {
                        Text(text = chatListItemData.unReadMsg.toString())
                    }
                }
            }

        }
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = chatListItemData.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Time,
                Text(
                    text = getChatTimeDisplay(chatListItemData.timestamp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = chatListItemData.lastMessage,
                    maxLines = 2,  // Set the maximum lines to 2
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }
        }
    }
    Divider()
}


data class ChatListItemData(
    val name: String,
    val userId: String,
    val lastMessage: String,
    val isOnline: Boolean = false,
    val unReadMsg: Int = 0,
    val timestamp: String,
    val profileImgUrl: String? // can be nullable will fix it
)

