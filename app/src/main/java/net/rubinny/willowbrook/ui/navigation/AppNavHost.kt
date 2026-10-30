package net.rubinny.willowbrook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import net.rubinny.willowbrook.audio.Ambience
import net.rubinny.willowbrook.data.content.ContentRepository
import net.rubinny.willowbrook.audio.MusicTrack
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.rubinny.willowbrook.AppContainer
import net.rubinny.willowbrook.ui.screens.casefiles.CaseFilesScreen
import net.rubinny.willowbrook.ui.screens.casefiles.CaseFilesViewModel
import net.rubinny.willowbrook.ui.screens.home.HomeScreen
import net.rubinny.willowbrook.ui.screens.home.HomeViewModel
import net.rubinny.willowbrook.ui.screens.play.ChapterPlayerScreen
import net.rubinny.willowbrook.ui.screens.play.ChapterPlayerViewModel
import net.rubinny.willowbrook.ui.screens.profiles.CreateProfileScreen
import net.rubinny.willowbrook.ui.screens.profiles.CreateProfileViewModel
import net.rubinny.willowbrook.ui.screens.profiles.ProfilePickerScreen
import net.rubinny.willowbrook.ui.screens.profiles.ProfilesViewModel
import net.rubinny.willowbrook.ui.screens.settings.AboutScreen
import net.rubinny.willowbrook.ui.screens.settings.SettingsScreen
import net.rubinny.willowbrook.ui.screens.settings.SettingsViewModel
import net.rubinny.willowbrook.ui.screens.notebook.NotebookScreen
import net.rubinny.willowbrook.ui.screens.notebook.NotebookViewModel
import net.rubinny.willowbrook.ui.screens.settings.SummaryScreen
import net.rubinny.willowbrook.ui.screens.settings.SummaryViewModel

@Composable
fun AppNavHost(
    container: AppContainer,
    onExitApp: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val inStory = backStackEntry?.destination?.route == Routes.PLAY
    LaunchedEffect(inStory) {
        if (!inStory) {
            container.musicPlayer.play(MusicTrack.MENU)
            container.ambiencePlayer.play(null as Ambience?)
        }
    }

    NavHost(navController = navController, startDestination = Routes.PROFILES) {

        composable(Routes.PROFILES) {
            val vm = viewModel { ProfilesViewModel(container.profileRepository, container.settingsRepository) }
            ProfilePickerScreen(
                viewModel = vm,
                onProfileChosen = { id -> navController.navigate(Routes.home(id)) },
                onCreateProfile = { navController.navigate(Routes.CREATE_PROFILE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
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
                HomeViewModel(
                    profileId,
                    container.profileRepository,
                    container.settingsRepository,
                    container.progressRepository,
                    container.contentRepository,
                )
            }
            HomeScreen(
                viewModel = vm,
                onContinue = { navController.navigate(Routes.play(profileId)) },
                onCaseFiles = { navController.navigate(Routes.caseFiles(profileId)) },
                onBeads = { navController.navigate(Routes.beads(profileId)) },
                onNotebook = { navController.navigate(Routes.notebook(profileId)) },
                onChangePlayer = { navController.popBackStack(Routes.PROFILES, inclusive = false) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onProfileMissing = { navController.popBackStack(Routes.PROFILES, inclusive = false) },
                onExitApp = onExitApp,
            )
        }

        composable(
            Routes.PLAY,
            arguments = listOf(
                navArgument(Routes.ARG_PROFILE_ID) { type = NavType.StringType },
                navArgument(Routes.ARG_CHAPTER) { type = NavType.IntType; defaultValue = -1 },
                navArgument(Routes.ARG_CASE) { type = NavType.StringType; defaultValue = ContentRepository.DEFAULT_CASE },
            ),
        ) { entry ->
            val profileId = entry.arguments?.getString(Routes.ARG_PROFILE_ID).orEmpty()
            val chapter = entry.arguments?.getInt(Routes.ARG_CHAPTER)?.takeIf { it >= 0 }
            // Only known case ids are accepted (the content loader reads assets/cases/<id>.json).
            val caseId = entry.arguments?.getString(Routes.ARG_CASE)
                ?.takeIf { it == Routes.BEADS_CASE } ?: ContentRepository.DEFAULT_CASE
            val vm = viewModel(key = "play-$profileId-$chapter-$caseId") {
                ChapterPlayerViewModel(
                    profileId = profileId,
                    replayChapter = chapter,
                    content = container.contentRepository,
                    progressRepo = container.progressRepository,
                    settingsRepo = container.settingsRepository,
                    caseId = caseId,
                )
            }
            ChapterPlayerScreen(
                viewModel = vm,
                narrator = container.narrator,
                soundPlayer = container.soundPlayer,
                musicPlayer = container.musicPlayer,
                ambiencePlayer = container.ambiencePlayer,
                onGoHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }

        composable(
            Routes.CASE_FILES,
            arguments = listOf(navArgument(Routes.ARG_PROFILE_ID) { type = NavType.StringType }),
        ) { entry ->
            val profileId = entry.arguments?.getString(Routes.ARG_PROFILE_ID).orEmpty()
            val vm = viewModel(key = "casefiles-$profileId") {
                CaseFilesViewModel(profileId, container.contentRepository, container.progressRepository)
            }
            CaseFilesScreen(
                viewModel = vm,
                onReplay = { index ->
                    navController.navigate(Routes.play(profileId, index)) { popUpTo(Routes.HOME) }
                },
                onContinue = {
                    navController.navigate(Routes.play(profileId)) { popUpTo(Routes.HOME) }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Routes.NOTEBOOK,
            arguments = listOf(navArgument(Routes.ARG_PROFILE_ID) { type = NavType.StringType }),
        ) { entry ->
            val profileId = entry.arguments?.getString(Routes.ARG_PROFILE_ID).orEmpty()
            val vm = viewModel(key = "notebook-$profileId") { NotebookViewModel(container, profileId) }
            NotebookScreen(viewModel = vm, narrator = container.narrator, onClose = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm = viewModel { SettingsViewModel(container) }
            SettingsScreen(
                viewModel = vm,
                onAddPlayer = { navController.navigate(Routes.CREATE_PROFILE) },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onSummary = { navController.navigate(Routes.SUMMARY) },
                onAllDataDeleted = {
                    navController.navigate(Routes.PROFILES) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }

        composable(Routes.SUMMARY) {
            val vm = viewModel { SummaryViewModel(container) }
            SummaryScreen(viewModel = vm, onClose = { navController.popBackStack() })
        }

        composable(Routes.ABOUT) {
            AboutScreen(onClose = { navController.popBackStack() })
        }
    }
}
