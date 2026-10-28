package net.rubinny.willowbrook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.rubinny.willowbrook.AppContainer
import net.rubinny.willowbrook.ui.screens.caseplaceholder.CasePlaceholderScreen
import net.rubinny.willowbrook.ui.screens.home.HomeScreen
import net.rubinny.willowbrook.ui.screens.home.HomeViewModel
import net.rubinny.willowbrook.ui.screens.profiles.CreateProfileScreen
import net.rubinny.willowbrook.ui.screens.profiles.CreateProfileViewModel
import net.rubinny.willowbrook.ui.screens.profiles.ProfilePickerScreen
import net.rubinny.willowbrook.ui.screens.profiles.ProfilesViewModel
import net.rubinny.willowbrook.ui.screens.settings.AboutScreen
import net.rubinny.willowbrook.ui.screens.settings.PinGateScreen
import net.rubinny.willowbrook.ui.screens.settings.PinViewModel
import net.rubinny.willowbrook.ui.screens.settings.SettingsScreen
import net.rubinny.willowbrook.ui.screens.settings.SettingsViewModel

@Composable
fun AppNavHost(
    container: AppContainer,
    onExitApp: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.PROFILES) {

        composable(Routes.PROFILES) {
            val vm = viewModel { ProfilesViewModel(container.profileRepository, container.settingsRepository) }
            ProfilePickerScreen(
                viewModel = vm,
                onProfileChosen = { id -> navController.navigate(Routes.home(id)) },
                onCreateProfile = { navController.navigate(Routes.CREATE_PROFILE) },
                onOpenSettings = { navController.navigate(Routes.PIN_GATE) },
                onExitApp = onExitApp,
            )
        }

        composable(Routes.CREATE_PROFILE) {
            val vm = viewModel { CreateProfileViewModel(container.profileRepository) }
            CreateProfileScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() },
            )
        }

        composable(
            Routes.HOME,
            arguments = listOf(navArgument(Routes.ARG_PROFILE_ID) { type = NavType.StringType }),
        ) { entry ->
            val profileId = entry.arguments?.getString(Routes.ARG_PROFILE_ID).orEmpty()
            val vm = viewModel(key = "home-$profileId") {
                HomeViewModel(profileId, container.profileRepository, container.settingsRepository)
            }
            HomeScreen(
                viewModel = vm,
                onContinue = { navController.navigate(Routes.case(profileId)) },
                onCaseFiles = { navController.navigate(Routes.case(profileId)) },
                onChangePlayer = { navController.popBackStack(Routes.PROFILES, inclusive = false) },
                onOpenSettings = { navController.navigate(Routes.PIN_GATE) },
                onProfileMissing = { navController.popBackStack(Routes.PROFILES, inclusive = false) },
                onExitApp = onExitApp,
            )
        }

        composable(
            Routes.CASE,
            arguments = listOf(navArgument(Routes.ARG_PROFILE_ID) { type = NavType.StringType }),
        ) {
            CasePlaceholderScreen(onBackHome = { navController.popBackStack() })
        }

        composable(Routes.PIN_GATE) {
            val vm = viewModel { PinViewModel(container.settingsRepository) }
            PinGateScreen(
                viewModel = vm,
                onUnlocked = {
                    navController.navigate(Routes.SETTINGS) {
                        popUpTo(Routes.PIN_GATE) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val vm = viewModel { SettingsViewModel(container) }
            SettingsScreen(
                viewModel = vm,
                onAddPlayer = { navController.navigate(Routes.CREATE_PROFILE) },
                onChangePin = { navController.navigate(Routes.CHANGE_PIN) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onAllDataDeleted = {
                    navController.navigate(Routes.PROFILES) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }

        composable(Routes.CHANGE_PIN) {
            val vm = viewModel { PinViewModel(container.settingsRepository, forceCreate = true) }
            PinGateScreen(
                viewModel = vm,
                onUnlocked = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onClose = { navController.popBackStack() })
        }
    }
}
