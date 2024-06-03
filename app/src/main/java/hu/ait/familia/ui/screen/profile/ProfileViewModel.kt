package hu.ait.familia.ui.screen.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.familia.ui.screen.home.UserDetailsState

class ProfileViewModel: ViewModel() {

    private val _userDetails = MutableLiveData<Map<String, Any>>()
    val userDetails: LiveData<Map<String, Any>> = _userDetails

    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    var userDetailsState: UserDetailsState by mutableStateOf(UserDetailsState.Init)

    // Used to get members in a family
    var familyMembersState: FamilyMembersState by mutableStateOf(FamilyMembersState.Init)

    init {
        auth.currentUser?.uid?.let { uid ->
            fetchUserDetails(uid)
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

    fun fetchAllUsersInFamily() {
        // Set the state to loading initially
        familyMembersState = FamilyMembersState.Loading

        // Get the current family ID from userDetails
        val currentFamilyId = userDetails.value?.get("currentFamily") as? String
        if (currentFamilyId == null) {
            familyMembersState = FamilyMembersState.Error("No current family ID found for the user.")
            return
        }

        // Reference to the family document in Firestore
        val familyRef = db.collection("families").document(currentFamilyId)

        // Begin the transaction to get family data
        db.runTransaction { transaction ->
            val familyDoc = transaction.get(familyRef)
            if (!familyDoc.exists()) {
                familyMembersState = FamilyMembersState.Error("Family not found in the database")
                return@runTransaction null // Exit the transaction early
            }

            // Get member IDs from the family document
            val members = familyDoc.data?.get("members") as? List<String>
            if (members.isNullOrEmpty()) {
                familyMembersState = FamilyMembersState.Error("No members found in this family")
                return@runTransaction null // Exit the transaction early
            }
            members // Continue with the member IDs
        }.addOnSuccessListener { members ->
            if (members != null && members.isNotEmpty()) {
                fetchUserDetails(members)
            }
        }.addOnFailureListener { exception ->
            familyMembersState = FamilyMembersState.Error("Transaction failed: ${exception.message}")
        }
    }

    private fun fetchUserDetails(memberIds: List<String>) {
        db.collection("users").whereIn("userId", memberIds).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    familyMembersState = FamilyMembersState.Error("No users found in the current family.")
                    return@addOnSuccessListener
                }
                val users = documents.documents.mapNotNull { it.data }
                familyMembersState = FamilyMembersState.Success(users)
            }
            .addOnFailureListener { exception ->
                familyMembersState = FamilyMembersState.Error("Error fetching user details: ${exception.message}")
            }
    }

}

sealed interface FamilyMembersState {
    object Init: FamilyMembersState
    object Loading : FamilyMembersState
    data class Success(val members: List<Map<String, Any>>) : FamilyMembersState
    data class Error(val message: String) : FamilyMembersState
}