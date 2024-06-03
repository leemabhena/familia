package hu.ait.familia.ui.screen.chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import hu.ait.familia.ui.screen.home.UserDetailsState
import java.util.Date

class ChatScreenViewModel: ViewModel() {

    private val _userDetails = MutableLiveData<Map<String, Any>>()
    val userDetails: LiveData<Map<String, Any>> = _userDetails

    var messages: LiveData<List<Message>> = MutableLiveData()

    var userDetailsState: UserDetailsState by mutableStateOf(UserDetailsState.Init)

    private val db = Firebase.firestore
    val auth = Firebase.auth

    init {
        auth.currentUser?.uid?.let { uid ->
            fetchUserDetails(uid)
        }
    }

    fun loadMessages(otherUserId: String) {
        messages = getChatMessages(otherUserId)
    }

    fun fetchUserDetails(uid: String) {
        userDetailsState = UserDetailsState.Loading

        val usersCollection = db.collection("users")
        usersCollection.document(uid).get()
            .addOnFailureListener {exception ->
                userDetailsState = UserDetailsState.Error("Error: $exception")
            }
            .addOnSuccessListener {document ->
                if (document != null) {
                    _userDetails.value = document.data
                    userDetailsState = UserDetailsState.UserDetailsSuccess
                } else {
                    userDetailsState = UserDetailsState.Error("Error: File not found")
                }

            }
    }

    private fun getChatMessages(otherUserId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()

        // Assuming the current user is logged in and has a valid ID.
        val currentUserId = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in.")

        // Reference to the chat messages between the current user and the other user.
        val messagesRef = db.collection("users")
            .document(currentUserId)
            .collection("chats")
            .document(otherUserId)
            .collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)  // Assuming messages should be ordered by time.

        // Listen for real-time updates
        messagesRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w("ChatService", "Listen failed.", exception)
                messagesLiveData.value = emptyList()  // Optionally handle the error more robustly
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Message::class.java)  // Mapping the document to a Message object
            } ?: emptyList()

            messagesLiveData.value = messages
        }

        return messagesLiveData
    }

    fun startChat(senderID: String, recipientID: String, message: String) {

        // References to the sender and recipient's messages sub-collection
        val senderMessagesRef = db.collection("users").document(senderID)
            .collection("chats").document(recipientID)
            .collection("messages")
        val recipientMessagesRef = db.collection("users").document(recipientID)
            .collection("chats").document(senderID)
            .collection("messages")

        // Create the chat data to be stored
        val chatData = mapOf(
            "sender" to senderID,
            "message" to message,
            "time" to Date()
        )

        // Start a batch write to ensure atomic updates
        val batch = db.batch()

        // Create a new document in both messages collections
        val newSenderMessageDoc = senderMessagesRef.document()
        val newRecipientMessageDoc = recipientMessagesRef.document()

        // Set data for both the sender and recipient
        batch.set(newSenderMessageDoc, chatData)
        batch.set(newRecipientMessageDoc, chatData)

        // Commit the batch write
        batch.commit().addOnSuccessListener {
            Log.d("ChatOperation", "Chat messages successfully updated for both sender and recipient.")
        }.addOnFailureListener { e ->
            Log.e("ChatOperation", "Failed to update chat messages: ${e.message}")
        }
    }
}

data class Message(
    val sender: String = "",
    val message: String = "",
    val time: Date = Date()
) {
    val isFromMe: Boolean
        get() = sender == Firebase.auth.currentUser?.uid!!
}