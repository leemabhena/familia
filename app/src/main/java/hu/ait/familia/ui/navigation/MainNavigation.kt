package hu.ait.familia.ui.navigation

sealed class MainNavigation(val route: String) {
    object Auth: MainNavigation(route = "auth_graph") {
        object WelcomeScreen: MainNavigation(route = "welcome_screen")
        object LoginScreen: MainNavigation(route = "login_screen")
        object SignupScreen: MainNavigation(route = "sign_screen")
    }

    object MainScreenGraph: MainNavigation(route = "main_graph") {
        object HomeScreen: MainNavigation(route = "home_screen")
        object ChatListScreen: MainNavigation(route = "chat_list_screen")
        object ChatScreen: MainNavigation(route = "chat_screen/{userID}"){
            fun getRoute(userID: String): String {
                return "chat_screen/$userID"
            }
        }
        object CalendarScreen: MainNavigation(route = "calendar_screen")
        object ProfileScreen: MainNavigation(route = "profile_screen")
        object ProfileEditScreen: MainNavigation(route = "profile_edit_screen")
        object ToolsScreen: MainNavigation(route = "tools_screen")
        object CameraScreen: MainNavigation(route = "camera_screen")
        object FamilyBarcodeScreen: MainNavigation(route = "join_family_screen")
    }

}