package hu.ait.familia.ui.screen.login

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import hu.ait.familia.data.firebase.User
import java.net.URLEncoder
import java.util.Date
import java.util.UUID

class SignUpViewModel: ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore
    var signUpUiState: SignUpUiState by mutableStateOf(SignUpUiState.Init)

    fun registerUser(username: String, email: String, password: String, profile: String? = null) {
        signUpUiState = SignUpUiState.Loading
        try {
            auth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    val newUser = hashMapOf(
                        "userId" to auth.currentUser?.uid!!,
                        "username" to username,
                        "email" to email,
                        "profilePicture" to profile,
                        "createdAt" to Date(),
                        "familyIDs" to listOf<String>()
                    )

                    db.collection("users").document(auth.currentUser?.uid!!)
                        .set(newUser)
                        .addOnSuccessListener { signUpUiState = SignUpUiState.RegisterSuccess }
                        .addOnFailureListener { signUpUiState = SignUpUiState.Error("Error in registration, try again!")}

                }
                .addOnFailureListener{
                    signUpUiState = SignUpUiState.Error(it.message)
                }
        } catch (e: Exception) {
            signUpUiState = SignUpUiState.Error(e.message)
        }
    }

    fun registerWithProfile(username: String, email: String, password: String, imageUri: Uri) {
        // prepare the empty file in the cloud
        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putFile(imageUri)
            .addOnFailureListener { e ->
                signUpUiState = SignUpUiState.Error(e.message)
            }.addOnSuccessListener {

                newImagesRef.downloadUrl.addOnCompleteListener(
                    object : OnCompleteListener<Uri> {
                        override fun onComplete(task: Task<Uri>) {
                            // the public URL of the image is: task.result.toString()
                            registerUser(
                                username = username,
                                email = email,
                                password = password,
                                profile = task.result.toString(),
                            )
                        }
                    })
            }

    }


}

sealed interface SignUpUiState {
    object Init : SignUpUiState
    object Loading : SignUpUiState
    object RegisterSuccess : SignUpUiState
    data class Error(val error: String?) : SignUpUiState
}