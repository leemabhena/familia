package hu.ait.familia.ui.screen.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hu.ait.familia.ui.navigation.MainNavigation
import hu.ait.familia.ui.screen.home.HomeScreen
import hu.ait.familia.ui.screen.calendar.CalendarScreen
import hu.ait.familia.ui.screen.chat.ChatListScreen
import hu.ait.familia.ui.screen.chat.ChatScreen
import hu.ait.familia.ui.screen.home.CameraScreen
import hu.ait.familia.ui.screen.home.HomeScreenViewModel
import hu.ait.familia.ui.screen.profile.ProfileEditScreen
import hu.ait.familia.ui.screen.profile.ProfileScreen
import hu.ait.familia.ui.screen.tools.ToolsScreen
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import hu.ait.familia.R
import hu.ait.familia.ui.screen.home.UserDetailsState
import hu.ait.familia.ui.screen.profile.FamilyBarcodeScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    homeScreenViewModel: HomeScreenViewModel = viewModel()) {

    val navHostController: NavHostController = rememberNavController()
    val currentBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var selectedIndex by rememberSaveable {
        mutableIntStateOf(2)
    }

    val user by homeScreenViewModel.userDetails.observeAsState()

    val startDestination = if (user?.get("currentFamily") != null) {
        MainNavigation.MainScreenGraph.ChatListScreen.route // if user is part of a family
    } else {
        MainNavigation.MainScreenGraph.HomeScreen.route // default start screen
    }



    // Make this states so that if there is change issue recomposition
    val bottomNavigationItems = listOf(
//        BottomNavigationBarItem(
//            title = "Home",
//            route = MainNavigation.MainScreenGraph.HomeScreen.route,
//            selectedIcon = Icons.Filled.Home,
//            unselectedIcon = Icons.Outlined.Home,
//        ),
        BottomNavigationBarItem(
            title = "Calendar",
            route = MainNavigation.MainScreenGraph.CalendarScreen.route,
            selectedIcon = Icons.Filled.DateRange,
            unselectedIcon = Icons.Outlined.DateRange,
            hasNew = true
        ),
        BottomNavigationBarItem(
            title = "Tools",
            route = MainNavigation.MainScreenGraph.ToolsScreen.route,
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Outlined.Build,
        ),
        BottomNavigationBarItem(
            title = "Chats",
            route = MainNavigation.MainScreenGraph.ChatListScreen.route,
            selectedIcon = Icons.Filled.Email,
            unselectedIcon = Icons.Outlined.Email,
            badgeCount = 9
        ),
        BottomNavigationBarItem(
            title = "Profile",
            route = MainNavigation.MainScreenGraph.ProfileScreen.route,
            selectedIcon = Icons.Filled.AccountCircle,
            unselectedIcon = Icons.Outlined.AccountCircle,
        ),

    )

    Scaffold(
        bottomBar = {
            if (
                currentRoute != MainNavigation.MainScreenGraph.ChatScreen.route &&
                currentRoute != MainNavigation.MainScreenGraph.HomeScreen.route &&
                currentRoute != MainNavigation.MainScreenGraph.CameraScreen.route
                ) {
                NavigationBar {
                    bottomNavigationItems.forEachIndexed { index, bottomNavigationBarItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                // navigate to screen
                                navHostController.navigate(bottomNavigationBarItem.route)
                            },
                            label = {
                                Text(text = bottomNavigationBarItem.title)
                            },
                            icon = {
                                BadgedBox(badge = {
                                    if (bottomNavigationBarItem.badgeCount != null) {
                                        Badge {
                                            Text(text = bottomNavigationBarItem.badgeCount.toString())
                                        }
                                    } else if (bottomNavigationBarItem.hasNew) {
                                        Badge()
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (selectedIndex == index)
                                            bottomNavigationBarItem.selectedIcon
                                        else bottomNavigationBarItem.unselectedIcon,
                                        contentDescription = bottomNavigationBarItem.title
                                    )
                                }
                            })
                    }
                }
            }

        }
    ) {
        when(homeScreenViewModel.userDetailsState) {
            is UserDetailsState.Init -> {}
            is UserDetailsState.Loading -> LoadingAnimation()
            else -> BottomAppBarNavigation(
                parentNavController = navController,
                startDestination = startDestination,
                navHostController = navHostController,
                modifier = Modifier.padding(it)
            )
        }

    }
}

@Composable
fun LoadingAnimation() {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.loading
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
//        modifier = Modifier.size(400.dp)
    )
}


data class BottomNavigationBarItem(
    val title: String,
    val route: String = "",
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    var hasNew: Boolean = false,
    var badgeCount: Int? = null
)

@Composable
fun BottomAppBarNavigation(
    modifier: Modifier = Modifier,
    parentNavController: NavController,
    startDestination: String = MainNavigation.MainScreenGraph.HomeScreen.route,
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = startDestination, modifier = modifier) {

        composable(MainNavigation.MainScreenGraph.HomeScreen.route) {
            HomeScreen(
                onJoiningFamily = {
                    navHostController.navigate(MainNavigation.MainScreenGraph.CameraScreen.route)
                },
                onCreatingFamily = {
                    navHostController.navigate(MainNavigation.MainScreenGraph.ChatListScreen.route)
                }
            )
        }

        composable(MainNavigation.MainScreenGraph.CalendarScreen.route) {
            CalendarScreen()
        }

        composable(MainNavigation.MainScreenGraph.ChatListScreen.route) {
            ChatListScreen(onNavigateToChat = navHostController::navigate)
        }

        composable(
            MainNavigation.MainScreenGraph.ChatScreen.route,
            arguments = listOf(navArgument("userID") { type = NavType.StringType })
            ) { backStackEntry ->
            ChatScreen(
                userID = backStackEntry.arguments?.getString("userID") ?: "",
            ) {
                navHostController.navigate(MainNavigation.MainScreenGraph.ChatListScreen.route)
            }
        }

        composable(MainNavigation.MainScreenGraph.ProfileScreen.route) {
            ProfileScreen(
                onNavigateToEdit = {
                    navHostController.navigate(MainNavigation.MainScreenGraph.ProfileEditScreen.route)
                },
                onStartChat = navHostController::navigate,
                onAddUser = {
                    navHostController.navigate(MainNavigation.MainScreenGraph.FamilyBarcodeScreen.route)
                },
                onSignOut = {
                    parentNavController.navigate(MainNavigation.Auth.LoginScreen.route) {
                        // Remove all previous routes from the back stack
                        popUpTo(MainNavigation.MainScreenGraph.route) { inclusive = true }
                    }
                }
            )
        }

        composable(MainNavigation.MainScreenGraph.ProfileEditScreen.route) {
            ProfileEditScreen {
                navHostController.navigate(MainNavigation.MainScreenGraph.ProfileScreen.route)
            }
        }

        composable(MainNavigation.MainScreenGraph.ToolsScreen.route) {
            ToolsScreen()
        }

        composable(MainNavigation.MainScreenGraph.CameraScreen.route) {
            CameraScreen(onSuccess = {
                navHostController.navigate(MainNavigation.MainScreenGraph.ChatListScreen.route)
            })
        }

        composable(MainNavigation.MainScreenGraph.FamilyBarcodeScreen.route) {
           FamilyBarcodeScreen {
               navHostController.navigate(MainNavigation.MainScreenGraph.ProfileScreen.route)
           }
        }
    }
}