package hu.ait.familia.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CameraScreenViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = Firebase.auth

    var cameraUiState: CameraUiState by mutableStateOf(CameraUiState.Init)

    fun joinFamily(familyID: String) {
        cameraUiState = CameraUiState.Loading
        val userId = auth.currentUser?.uid
        if (userId == null) {
            cameraUiState = CameraUiState.Error("User not logged in.")
            return
        }

        // Reference to the families collection
        val familiesCollection = db.collection("families")

        db.runTransaction { transaction ->
            // Add the user to list of members
            val familyDoc = familiesCollection.document(familyID)
            transaction.update(familyDoc, "members", FieldValue.arrayUnion(userId))

            // Add the family ID to the user's list of families
            val userDocument = db.collection("users").document(userId)
            transaction.update(userDocument, "familyIDs", FieldValue.arrayUnion(familyID))
            transaction.update(userDocument, "currentFamily", familyID)

            // This transaction will either completely succeed or fail
            null
        }.addOnSuccessListener {
            // update the user's currentFamily
            cameraUiState = CameraUiState.Success
        }
            .addOnFailureListener { e ->
                cameraUiState = CameraUiState.Error("Error: $e")
            }
    }
}

sealed interface CameraUiState {
    object Init : CameraUiState
    object Loading : CameraUiState
    object Success : CameraUiState
    data class Error(val error: String?) : CameraUiState
}
