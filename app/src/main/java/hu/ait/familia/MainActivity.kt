package hu.ait.familia

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import hu.ait.familia.ui.navigation.MainNavigation
import hu.ait.familia.ui.screen.calendar.CalendarScreen
import hu.ait.familia.ui.screen.chat.ChatListScreen
import hu.ait.familia.ui.screen.login.LoginScreen
import hu.ait.familia.ui.screen.login.SignupScreen
import hu.ait.familia.ui.screen.login.WelcomeScreen
import hu.ait.familia.ui.screen.navigation.MainScreen
import hu.ait.familia.ui.screen.profile.ProfileEditScreen
import hu.ait.familia.ui.screen.profile.ProfileScreen
import hu.ait.familia.ui.theme.FamiliaTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val currentUser = auth.currentUser
        val startDestination = if (currentUser != null) {
            MainNavigation.MainScreenGraph.route
        } else {
            MainNavigation.Auth.route
        }

        setContent {
            FamiliaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                   Navigation(startDestination = startDestination)
                }
            }
        }
    }
}


@Composable
fun Navigation(
    startDestination: String = MainNavigation.Auth.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {

        navigation(
            startDestination = MainNavigation.Auth.WelcomeScreen.route,
            route = MainNavigation.Auth.route
        ) {

            composable(MainNavigation.Auth.WelcomeScreen.route) {
                WelcomeScreen {
                    navController.navigate(MainNavigation.Auth.LoginScreen.route)
                }
            }

            composable(MainNavigation.Auth.SignupScreen.route) {
                SignupScreen(
                    onNavigateToLogin = {
                        navController.navigate(MainNavigation.Auth.LoginScreen.route)
                    },
                    onSignInSuccess = {
                        navController.navigate(MainNavigation.MainScreenGraph.route) {
                            // Remove the graph from the back entry stack
                            popUpTo(MainNavigation.Auth.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(MainNavigation.Auth.LoginScreen.route) {
                LoginScreen(
                    onNavigateToSignUp = {
                        navController.navigate(MainNavigation.Auth.SignupScreen.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(MainNavigation.MainScreenGraph.route) {
                            // Remove the graph from the back entry stack
                            popUpTo(MainNavigation.Auth.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable(MainNavigation.MainScreenGraph.route) {
            MainScreen(navController = navController)
        }

    }
}
