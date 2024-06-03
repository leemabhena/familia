package hu.ait.familia.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import hu.ait.familia.data.firebase.User
import kotlinx.coroutines.tasks.await
import java.util.Date

class LoginViewModel: ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Init)


    suspend fun loginUser(email: String, password: String): AuthResult? {
        loginUiState = LoginUiState.Loading

        try {
            val result = auth.signInWithEmailAndPassword(email,password).await()
            if (result.user != null) {
                loginUiState = LoginUiState.LoginSuccess
            } else {
                loginUiState = LoginUiState.Error("Login failed")
            }
            return result
        } catch (e: Exception) {
            loginUiState = LoginUiState.Error(e.message)
            return null
        }
    }



}

sealed interface LoginUiState {
    object Init : LoginUiState
    object Loading : LoginUiState
    object LoginSuccess : LoginUiState
    data class Error(val error: String?) : LoginUiState
}