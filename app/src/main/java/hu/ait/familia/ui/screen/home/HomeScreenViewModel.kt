package hu.ait.familia.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.familia.data.firebase.Family
import java.util.Date
import java.util.UUID

class HomeScreenViewModel: ViewModel() {

    private val _userDetails = MutableLiveData<Map<String, Any>>()
    val userDetails: LiveData<Map<String, Any>> = _userDetails

    var userDetailsState: UserDetailsState by mutableStateOf(UserDetailsState.Init)

    var familyUiState: FamilyUiState by mutableStateOf(FamilyUiState.Init)

    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    init {
        auth.currentUser?.uid?.let { uid ->
            fetchUserDetails(uid)
        }
    }

    fun createNewFamily(familyName: String) {
        familyUiState = FamilyUiState.Loading

        val userId = auth.currentUser?.uid
        if (userId == null) {
            familyUiState = FamilyUiState.Error("User not logged in.")
            return
        }

        // Creating the new family object
        val newFamily = Family(
            familyId = UUID.randomUUID().toString(),
            familyName = familyName,
            createdAt = Date(),
            members = listOf(userId)
        )

        // Reference to the families collection
        val familiesCollection = db.collection("families")

        db.runTransaction { transaction ->
            // Add the new family to the families collection
            val familyDocument = familiesCollection.document(newFamily.familyId)
            transaction.set(familyDocument, newFamily)

            // Add the family ID to the user's list of families
            val userDocument = db.collection("users").document(userId)

            transaction.update(userDocument, "familyIDs", FieldValue.arrayUnion(newFamily.familyId))
            transaction.update(userDocument, "currentFamily", newFamily.familyId)

            // This transaction will either completely succeed or fail
            null  // Kotlin transactions must return something, null here means nothing to return
        }.addOnSuccessListener {
            familyUiState = FamilyUiState.UploadSuccess
        }.addOnFailureListener { e ->
            familyUiState = FamilyUiState.Error("Family creation failed: ${e.message}")
        }
    }

    // Fetch the user details using the email address
    private fun fetchUserDetails(uid: String) {
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

    fun updateProfile(uid: String, username: String, email: String) {
        val userRef = db.collection("users").document(uid)
        userRef.update(
            mapOf(
                "username" to username,
                "email" to email
            )
        ).addOnSuccessListener {  }
            .addOnFailureListener {  }

    }
}

sealed interface UserDetailsState {
    object Init: UserDetailsState
    object Loading: UserDetailsState
    object UserDetailsSuccess: UserDetailsState
    data class Error(val error: String?) : UserDetailsState
}

sealed interface FamilyUiState {
    object Init : FamilyUiState
    object Loading : FamilyUiState
    object UploadSuccess : FamilyUiState
    data class Error(val error: String?) : FamilyUiState
}
