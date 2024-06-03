package hu.ait.familia.ui.screen.chat


import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import hu.ait.familia.R
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userID: String,
    chatScreenViewModel: ChatScreenViewModel = viewModel(),
    onBack: () -> Unit = {}
) {

    LaunchedEffect(Unit) {
        chatScreenViewModel.fetchUserDetails(userID)
    }

    LaunchedEffect(userID) {
        chatScreenViewModel.loadMessages(userID)
    }

    val messages by chatScreenViewModel.messages.observeAsState(initial = emptyList())

    val userDetails = chatScreenViewModel.userDetails.observeAsState().value

    val listState = rememberLazyListState()

    val initialMessageCount by remember { mutableStateOf(messages.size) }
    var lastMessageCount by remember { mutableStateOf(messages.size) }

    val context = LocalContext.current

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(messages) {
        if (messages.size > lastMessageCount && messages.size != initialMessageCount) {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        }
        lastMessageCount = messages.size  // Update the last message count
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        if (userDetails?.get("profilePicture") != null || userDetails?.get("profilePicture").toString() != "null") {
                            AsyncImage(
                                model = userDetails?.get("profilePicture").toString(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                                    .padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                                    .padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        }


                        Column (
                            modifier = Modifier
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = userDetails?.get("username").toString() ,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                    }
                        },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                          IconButton(onClick = { /*TODO*/ }) {
                              Icon(
                                  imageVector = Icons.Filled.Call,
                                  contentDescription = "call",
                                  tint = MaterialTheme.colorScheme.onPrimary
                              )
                          }

                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "more",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                ChatBottomBar(chatScreenViewModel, userID)
            }
        }
    ){
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(it)
                ) {
                    items(messages) { message ->
                        ChatMessageBox(message = message)
                    }
                }

        }

}


@Composable
fun ChatMessageBox(message: Message) {
    val timeFormat = SimpleDateFormat("hh:mma", Locale.US)  // 'a' for AM/PM marker
    val time = timeFormat.format(message.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .align(if (message.isFromMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 48f,
                        topEnd = 48f,
                        bottomStart = if (message.isFromMe) 48f else 0f,
                        bottomEnd = if (message.isFromMe) 0f else 48f
                    )
                )
                .background(
                    if (message.isFromMe) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.message,
                color = if (message.isFromMe) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ChatBottomBar(
    chatScreenViewModel: ChatScreenViewModel,
    recipientId: String,
    modifier: Modifier = Modifier
) {
    var message by remember {
        mutableStateOf("")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
//        modifier = modifier
//            .background(color =  MaterialTheme.colorScheme.primaryContainer)
//            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        //
        OutlinedTextField(
            value = message,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            placeholder = {
                          Text(text = "Type here...")
            },
            trailingIcon = {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(12.dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "send",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                if (message.isNotEmpty()) {
                                    chatScreenViewModel.startChat(
                                        senderID = chatScreenViewModel.auth.currentUser?.uid!!,
                                        recipientID = recipientId,
                                        message = message,
                                    )
                                    message = ""
                                }
                            }
                    )
                }
            },
            onValueChange = {
                message = it
                            },
            modifier = Modifier
                .weight(1f)
        )

        Image(
            painter = painterResource(id = R.drawable.microphone),
            contentDescription = "microphone",
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(32.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = "camera",
            modifier = Modifier
                .size(32.dp)
        )
    }
}

