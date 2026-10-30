package net.rubinny.willowbrook.ui.screens.play

import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import net.rubinny.willowbrook.R
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.audio.MusicPlayer
import net.rubinny.willowbrook.audio.MusicTrack
import net.rubinny.willowbrook.audio.Narrator
import net.rubinny.willowbrook.audio.SoundPlayer
import net.rubinny.willowbrook.game.model.ChoiceKind
import net.rubinny.willowbrook.game.model.Step
import net.rubinny.willowbrook.game.stage.SceneSpots
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.CompanionBar
import net.rubinny.willowbrook.ui.components.ConfirmDialog
import net.rubinny.willowbrook.ui.components.InfoDialog
import net.rubinny.willowbrook.ui.components.ItemPicture
import net.rubinny.willowbrook.ui.components.OnScene
import net.rubinny.willowbrook.ui.components.Portrait
import net.rubinny.willowbrook.ui.components.SceneBackground
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalTextScale
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

@Composable
fun ChapterPlayerScreen(
    viewModel: ChapterPlayerViewModel,
    narrator: Narrator,
    soundPlayer: SoundPlayer,
    musicPlayer: MusicPlayer,
    onGoHome: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    var showWhat by rememberSaveable { mutableStateOf(false) }
    var chatIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler { confirmLeave = true }

    // Speech and sounds requested by the game.
    val accessibility = LocalContext.current.getSystemService(AccessibilityManager::class.java)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PlayerEffect.Speak -> {
                    val talkBackOn = accessibility?.isTouchExplorationEnabled == true
                    if (!(effect.companion && talkBackOn)) narrator.speak(effect.line, effect.rate)
                }
                is PlayerEffect.Sound -> soundPlayer.play(effect.cue, effect.volume)
            }
        }
    }
    // The finale chapter (and the "case solved" screen) gets brighter music.
    val playing = state as? PlayerUiState.Playing
    val finale = playing != null && (playing.chapterIndex == playing.case.chapters.lastIndex || playing.phase == Phase.CASE_FINISHED)
    LaunchedEffect(finale, playing != null) { if (playing != null) musicPlayer.play(if (finale) MusicTrack.FINALE else MusicTrack.STORY) }
    // Leaving the story always stops the voice.
    DisposableEffect(Unit) { onDispose { narrator.stop() } }
    val openWhat = {
        viewModel.onWhatAreWeDoing()
        showWhat = true
    }

    when (val s = state) {
        PlayerUiState.Loading -> ScreenScaffold(title = "Opening the story…") {}
        is PlayerUiState.Error -> ErrorScreen(s.message, onGoHome)
        is PlayerUiState.Playing -> SceneBackground(
            scene = s.chapter.scene,
            // Objects are placed on the scene while searching, so the camera holds still.
            calmCamera = s.phase == Phase.STEP && s.step is Step.HiddenObject,
        ) {
            when (s.phase) {
                Phase.RECAP -> RecapPhase(s, viewModel::onRecapContinue)
                Phase.CHAPTER_INTRO -> ChapterIntroPhase(s, viewModel::onChapterIntroContinue)
                Phase.STEP -> StepPhase(s, viewModel, onWhatAreWeDoing = openWhat)
                Phase.CHAPTER_COMPLETE -> ChapterCompletePhase(
                    s,
                    onNext = { if (!viewModel.onNextChapter()) onGoHome() },
                    onRest = onGoHome,
                    onChat = { chatIndex = 0; viewModel.onChat(0) },
                )
                Phase.CASE_FINISHED -> CaseFinishedPhase(s, onGoHome)
            }
            chatIndex?.let { index ->
                val questions = s.chapter.chat
                ChatDialog(
                    question = questions[index % questions.size],
                    onAnother = {
                        val next = (index + 1) % questions.size
                        chatIndex = next
                        viewModel.onChat(next)
                    },
                    onDone = { narrator.stop(); chatIndex = null },
                    canAskAnother = questions.size > 1,
                )
            }
            if (showWhat) {
                // Big picture + current chapter. The current task itself is always in Marmalade's bar.
                InfoDialog(
                    title = "What are we doing?",
                    paragraphs = listOf(s.case.premise, "Right now: ${s.chapter.goal}"),
                    buttonText = "Thank you",
                    onDismiss = { narrator.stop(); showWhat = false },
                )
            }
        }
    }

    if (confirmLeave) {
        val replay = (state as? PlayerUiState.Playing)?.replay == true
        ConfirmDialog(
            title = "Would you like a break?",
            message = if (replay) "You can play this chapter again any time." else "Your progress is saved. We can carry on later.",
            confirmText = "Go home",
            dismissText = "Keep playing",
            onConfirm = { confirmLeave = false; onGoHome() },
            onDismiss = { confirmLeave = false },
        )
    }
}

/** Centered card used by recap, intro and completion screens: one message, one main action. */
@Composable
private fun CenterCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalWillowColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 820.dp)
                .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
                .padding(Dimens.gapLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.gap),
            content = content,
        )
    }
}

@Composable
private fun Heading(text: String, color: Color = LocalWillowColors.current.textPrimary) {
    Text(text, style = WillowType.title, color = color, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
}

@Composable
private fun Body(text: String, color: Color = LocalWillowColors.current.textPrimary) {
    Text(text, style = WillowType.body, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun RecapPhase(s: PlayerUiState.Playing, onContinue: () -> Unit) {
    val focus = remember { FocusRequester() }
    CenterCard {
        Portrait(key = "marmalade_face", emoji = "🐈", size = 96.dp)
        Heading("Welcome back!")
        s.recapText?.let { Body("Last time: $it", LocalWillowColors.current.textSecondary) }
        Body("Let's carry on.")
        BigButton("Carry on", onClick = onContinue, modifier = Modifier.focusRequester(focus))
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}

@Composable
private fun ChapterIntroPhase(s: PlayerUiState.Playing, onBegin: () -> Unit) {
    val focus = remember { FocusRequester() }
    val colors = LocalWillowColors.current
    CenterCard {
        Text(
            text = if (s.chapterIndex == 0) "Getting started" else "Chapter ${s.chapterIndex}",
            style = WillowType.label,
            color = colors.accent,
        )
        Heading(s.chapter.title)
        Body(s.chapter.location, colors.textSecondary)
        Body(s.chapter.goal)
        BigButton(if (s.replay) "Play again" else "Begin", onClick = onBegin, modifier = Modifier.focusRequester(focus))
    }
    LaunchedEffect(s.chapterIndex) { focus.requestFocusSafely() }
}

@Composable
private fun ChapterCompletePhase(s: PlayerUiState.Playing, onNext: () -> Unit, onRest: () -> Unit, onChat: () -> Unit) {
    val focus = remember { FocusRequester() }
    val next = s.case.chapters.getOrNull(s.chapterIndex + 1)?.takeIf { !s.replay }
    val solvedCase = !s.replay && s.chapterIndex == s.case.chapters.lastIndex
    CenterCard {
        // Lantern beside the heading (not above it) so the "Let's chat" button still fits on a TV screen.
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(painterResource(R.drawable.ic_lantern), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(72.dp))
            Heading(if (solvedCase) "You solved the whole case!" else "Well done, detective!")
        }
        Body("You finished \"${s.chapter.title}\".")
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            if (next != null) {
                BigButton("Next: ${next.title}", onClick = onNext, minWidth = 360.dp, modifier = Modifier.focusRequester(focus))
                BigButton("Rest now", onClick = onRest, minWidth = 240.dp)
            } else {
                BigButton("Back home", onClick = onRest, modifier = Modifier.focusRequester(focus))
            }
        }
        if (s.chapter.chat.isNotEmpty()) {
            BigButton("💬 Let's chat", onClick = onChat, minWidth = 300.dp, minHeight = 72.dp)
        }
    }
    LaunchedEffect(s.chapterIndex) { focus.requestFocusSafely() }
}

@Composable
private fun CaseFinishedPhase(s: PlayerUiState.Playing, onGoHome: () -> Unit) {
    val focus = remember { FocusRequester() }
    CenterCard {
        Icon(painterResource(R.drawable.ic_lantern), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(120.dp))
        Heading("You solved ${s.case.title}!")
        Body("You can play any chapter again from Case files.")
        BigButton("Back home", onClick = onGoHome, modifier = Modifier.focusRequester(focus))
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}

@Composable
private fun ErrorScreen(message: String, onGoHome: () -> Unit) {
    val focus = remember { FocusRequester() }
    ScreenScaffold(title = "Let's go back home", subtitle = message) {
        BigButton("Back home", onClick = onGoHome, modifier = Modifier.focusRequester(focus))
    }
    LaunchedEffect(Unit) { focus.requestFocusSafely() }
}

/**
 * Frame for a story step. Content is spread over at least the full screen height, and scrolls
 * (following focus) when large caregiver text makes it taller — nothing is ever squashed.
 */
@Composable
private fun StepPhase(s: PlayerUiState.Playing, viewModel: ChapterPlayerViewModel, onWhatAreWeDoing: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val screenHeight = maxHeight
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .heightIn(min = screenHeight)
            .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val step = s.step) {
            is Step.Narration -> NarrationStep(s, step, viewModel::onStepContinue, viewModel::onListenAgain, onWhatAreWeDoing)
            is Step.Reward -> RewardStep(s, step, viewModel::onStepContinue, viewModel::onListenAgain)
            is Step.HiddenObject -> HiddenObjectStep(s, viewModel, onWhatAreWeDoing)
            is Step.MemoryMatch -> MemoryStep(s, step, viewModel, onWhatAreWeDoing)
            is Step.Choice -> ChoiceStep(s, viewModel, onWhatAreWeDoing)
            is Step.Sorting -> SortingStep(s, viewModel, onWhatAreWeDoing)
            is Step.Spell -> SpellStep(s, viewModel, onWhatAreWeDoing)
        }
    }
    }
}

@Composable
private fun ColumnScope.NarrationStep(
    s: PlayerUiState.Playing,
    step: Step.Narration,
    onContinue: () -> Unit,
    onListenAgain: () -> Unit,
    onWhatAreWeDoing: () -> Unit,
) {
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    OnScene { Text(s.chapter.location, style = WillowType.label, color = colors.textSecondary) }
    Spacer(Modifier.weight(1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapLarge),
    ) {
        if (step.portrait != null || step.portraitEmoji != null) Portrait(step.portrait, step.portraitEmoji, size = 180.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Speaker and "Listen again" share one row, so a four-line speech still fits a TV screen
            // with "Continue" fully visible.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                Text(step.speaker.orEmpty(), style = WillowType.button, color = colors.accent, modifier = Modifier.weight(1f))
                if (s.canListen) {
                    BigButton("🔊 Listen again", onClick = onListenAgain, minWidth = 300.dp, minHeight = 72.dp)
                }
            }
            StoryText(s, step.text, WillowType.body)
        }
    }
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap), modifier = Modifier.align(Alignment.CenterHorizontally)) {
        BigButton("Continue", onClick = onContinue, modifier = Modifier.focusRequester(focus))
        BigButton("What are we doing?", onClick = onWhatAreWeDoing, minWidth = 360.dp)
    }
    LaunchedEffect(step.id) { focus.requestFocusSafely() }
}

@Composable
private fun ColumnScope.RewardStep(s: PlayerUiState.Playing, step: Step.Reward, onContinue: () -> Unit, onListenAgain: () -> Unit) {
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    Spacer(Modifier.weight(1f))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.gap),
    ) {
        if (step.charm) {
            Icon(painterResource(R.drawable.ic_lantern), contentDescription = "A glowing light charm", tint = Color.Unspecified, modifier = Modifier.size(140.dp))
        }
        Box(
            modifier = Modifier
                .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
                .padding(28.dp),
        ) { StoryText(s, step.text, WillowType.title, TextAlign.Center) }
    }
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap), modifier = Modifier.align(Alignment.CenterHorizontally)) {
        BigButton("Continue", onClick = onContinue, modifier = Modifier.focusRequester(focus))
        if (s.canListen) BigButton("🔊 Listen again", onClick = onListenAgain, minWidth = 300.dp)
    }
    LaunchedEffect(step.id) { focus.requestFocusSafely() }
}

/**
 * Header shared by activities: goal on the left; "Help me" (or "Continue" once done) and
 * "What are we doing?" on the right. Keeping them in one row lets the activity fit a TV screen.
 */
@Composable
private fun ActivityHeader(
    goal: @Composable () -> Unit,
    complete: Boolean,
    continueFocus: FocusRequester,
    onHelp: () -> Unit,
    onContinue: () -> Unit,
    onWhatAreWeDoing: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OnScene(Modifier.weight(1f)) { goal() }
        if (complete) {
            BigButton("Continue", onClick = onContinue, minWidth = 220.dp, minHeight = 80.dp, modifier = Modifier.focusRequester(continueFocus))
        } else {
            BigButton("Help me", onClick = onHelp, minWidth = 180.dp, minHeight = 80.dp)
        }
        BigButton("What are we doing?", onClick = onWhatAreWeDoing, minWidth = 300.dp, minHeight = 80.dp)
    }
}

@Composable
private fun ColumnScope.HiddenObjectStep(s: PlayerUiState.Playing, viewModel: ChapterPlayerViewModel, onWhatAreWeDoing: () -> Unit) {
    val ui = s.hiddenObject ?: return
    val colors = LocalWillowColors.current
    val cellFocus = remember(s.step.id, ui.cells.size) { List(ui.cells.size) { FocusRequester() } }
    val continueFocus = remember { FocusRequester() }

    ActivityHeader(
        goal = {
            Column {
                Text("Found", style = WillowType.label, color = colors.textSecondary)
                Text("${ui.found.size} of ${ui.targets.size}", style = WillowType.button, color = colors.textPrimary)
            }
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
    // Objects lie on the scene itself. Labels must fit two full lines, so with enlarged caregiver text
    // the familiar tile grid is used instead.
    if (LocalTextScale.current <= 1.05f && ui.cells.size <= SceneSpots.MAX) {
        SceneObjects(
            scene = s.chapter.scene,
            cells = ui.cells,
            found = ui.found,
            glowingIndex = s.hintIndex,
            focus = cellFocus,
            onSelect = viewModel::onHiddenCellSelected,
            modifier = Modifier.weight(1f).heightIn(min = SCENE_OBJECTS_MIN_HEIGHT),
        )
    } else {
        Spacer(Modifier.weight(1f))
        TileGrid(count = ui.cells.size) { index, modifier ->
            val item = ui.cells[index]
            HiddenObjectCell(
                item = item,
                found = item.id in ui.found,
                glowing = s.hintIndex == index,
                onClick = { viewModel.onHiddenCellSelected(index) },
                modifier = modifier.focusRequester(cellFocus[index]),
            )
        }
        Spacer(Modifier.weight(1f))
    }
    CompanionBar(s.companionMessage, Modifier.fillMaxWidth())

    LaunchedEffect(s.step.id) { cellFocus.firstOrNull()?.requestFocusSafely() }
    LaunchedEffect(s.hintLevel, s.hintIndex) {
        if (s.hintLevel == HintLevel.GUIDE) s.hintIndex?.let { cellFocus.getOrNull(it)?.requestFocusSafely() }
    }
    LaunchedEffect(ui.complete) { if (ui.complete) continueFocus.requestFocusSafely() }
}

@Composable
private fun ColumnScope.MemoryStep(
    s: PlayerUiState.Playing,
    step: Step.MemoryMatch,
    viewModel: ChapterPlayerViewModel,
    onWhatAreWeDoing: () -> Unit,
) {
    val ui = s.memory ?: return
    val colors = LocalWillowColors.current
    val cardFocus = remember(s.step.id, ui.cards.size) { List(ui.cards.size) { FocusRequester() } }
    val continueFocus = remember { FocusRequester() }
    val pairsDone = ui.cards.count { it.matched } / 2

    ActivityHeader(
        goal = {
            Column {
                Text("Pairs found", style = WillowType.label, color = colors.textSecondary)
                Text("$pairsDone of ${ui.cards.size / 2}", style = WillowType.button, color = colors.textPrimary)
            }
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
    Spacer(Modifier.weight(1f))
    TileGrid(count = ui.cards.size) { index, modifier ->
        MemoryCard(
            position = index + 1,
            card = ui.cards[index],
            glowing = s.hintIndex == index,
            onClick = { viewModel.onMemoryCardSelected(index) },
            modifier = modifier.focusRequester(cardFocus[index]),
        )
    }
    Spacer(Modifier.weight(1f))
    CompanionBar(s.companionMessage, Modifier.fillMaxWidth())

    LaunchedEffect(s.step.id) { cardFocus.firstOrNull()?.requestFocusSafely() }
    LaunchedEffect(s.hintLevel, s.hintIndex) {
        if (s.hintLevel == HintLevel.GUIDE) s.hintIndex?.let { cardFocus.getOrNull(it)?.requestFocusSafely() }
    }
    LaunchedEffect(ui.complete) { if (ui.complete) continueFocus.requestFocusSafely() }
}

/**
 * Story text. Hidden only when the caregiver turned captions off while narration is on,
 * so there is never a moment with neither words nor voice.
 */
@Composable
private fun StoryText(s: PlayerUiState.Playing, text: String, style: TextStyle, align: TextAlign? = null) {
    val colors = LocalWillowColors.current
    if (s.showStoryText) {
        Text(text, style = style, color = colors.textPrimary, textAlign = align)
    } else {
        Text("🔊 Listen…", style = style, color = colors.textSecondary, textAlign = align)
    }
}

@Composable
private fun ColumnScope.ChoiceStep(s: PlayerUiState.Playing, viewModel: ChapterPlayerViewModel, onWhatAreWeDoing: () -> Unit) {
    val ui = s.choice ?: return
    val colors = LocalWillowColors.current
    val optionFocus = remember(s.step.id, ui.options.size) { List(ui.options.size) { FocusRequester() } }
    val continueFocus = remember { FocusRequester() }

    ActivityHeader(
        goal = {
            Text(
                if (ui.kind == ChoiceKind.RECALL) "Memory question" else "A riddle",
                style = WillowType.button,
                color = colors.accent,
            )
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
    Spacer(Modifier.weight(1f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
            .padding(horizontal = 28.dp, vertical = 16.dp),
    ) {
        Text(ui.question, style = WillowType.title, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
    }
    TileGrid(count = ui.options.size) { index, modifier ->
        val item = ui.options[index]
        OptionTile(
            item = item,
            tried = item.id in ui.tried,
            glowing = s.hintIndex == index,
            onClick = { viewModel.onChoiceSelected(index) },
            modifier = modifier.focusRequester(optionFocus[index]),
        )
    }
    Spacer(Modifier.weight(1f))
    CompanionBar(s.companionMessage, Modifier.fillMaxWidth())

    LaunchedEffect(s.step.id) { optionFocus.firstOrNull()?.requestFocusSafely() }
    GuideFocus(s, optionFocus)
    LaunchedEffect(ui.complete) { if (ui.complete) continueFocus.requestFocusSafely() }
}

@Composable
private fun ColumnScope.SortingStep(s: PlayerUiState.Playing, viewModel: ChapterPlayerViewModel, onWhatAreWeDoing: () -> Unit) {
    val ui = s.sorting ?: return
    val colors = LocalWillowColors.current
    val binFocus = remember(s.step.id, ui.bins.size) { List(ui.bins.size) { FocusRequester() } }
    val continueFocus = remember { FocusRequester() }

    ActivityHeader(
        goal = {
            Column {
                Text("Sorted", style = WillowType.label, color = colors.textSecondary)
                Text("${ui.placedCount} of ${ui.total}", style = WillowType.button, color = colors.textPrimary)
            }
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
    Spacer(Modifier.weight(1f))
    ui.current?.let { item ->
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
                .padding(horizontal = 32.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gap),
        ) {
            // Sized so the item, the bins (even when full) and Marmalade's bar all fit one TV screen.
            ItemPicture(item.id, item.emoji, size = 76.dp)
            Column {
                Text(item.label, style = WillowType.title, color = colors.textPrimary)
                Text("Where does it belong?", style = WillowType.body, color = colors.textSecondary)
            }
        }
    }
    TileGrid(count = ui.bins.size) { index, modifier ->
        BinTile(
            bin = ui.bins[index],
            glowing = s.hintIndex == index,
            onClick = { viewModel.onSortingBinSelected(index) },
            modifier = modifier.focusRequester(binFocus[index]),
        )
    }
    Spacer(Modifier.weight(1f))
    CompanionBar(s.companionMessage, Modifier.fillMaxWidth())

    LaunchedEffect(s.step.id) { binFocus.firstOrNull()?.requestFocusSafely() }
    GuideFocus(s, binFocus)
    LaunchedEffect(ui.complete) { if (ui.complete) continueFocus.requestFocusSafely() }
}

@Composable
private fun ColumnScope.SpellStep(s: PlayerUiState.Playing, viewModel: ChapterPlayerViewModel, onWhatAreWeDoing: () -> Unit) {
    val ui = s.spell ?: return
    val colors = LocalWillowColors.current
    val symbolFocus = remember(s.step.id) { List(ui.symbols.size) { FocusRequester() } }
    val continueFocus = remember { FocusRequester() }

    ActivityHeader(
        goal = {
            Column {
                Text("A kind spell", style = WillowType.label, color = colors.textSecondary)
                Text(
                    (0 until ui.length).joinToString(" ") { if (it < ui.position || ui.complete) "●" else "○" },
                    style = WillowType.button,
                    color = colors.accent,
                    modifier = Modifier.semantics { contentDescription = "${ui.position} of ${ui.length} done" },
                )
            }
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
    Spacer(Modifier.weight(1f))
    TileGrid(count = ui.symbols.size) { index, modifier ->
        SymbolTile(
            symbol = ui.symbols[index],
            lit = ui.showingSymbol == index,
            glowing = s.hintIndex == index,
            onClick = { viewModel.onSpellSymbolSelected(index) },
            modifier = modifier.focusRequester(symbolFocus[index]),
        )
    }
    if (!ui.complete) {
        BigButton(
            if (ui.showing) "Watching…" else "Show me again",
            onClick = viewModel::onSpellShowAgain,
            minWidth = 320.dp,
            minHeight = 72.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
    Spacer(Modifier.weight(1f))
    CompanionBar(s.companionMessage, Modifier.fillMaxWidth())

    LaunchedEffect(s.step.id) { symbolFocus.firstOrNull()?.requestFocusSafely() }
    // When the magic has been shown, put the player on the first symbol, ready to repeat it.
    LaunchedEffect(ui.showing) { if (!ui.showing && !ui.complete) symbolFocus.firstOrNull()?.requestFocusSafely() }
    GuideFocus(s, symbolFocus)
    LaunchedEffect(ui.complete) { if (ui.complete) continueFocus.requestFocusSafely() }
}

/**
 * A conversation starter for the caregiver and player. Nothing is recorded or stored;
 * the questions are open and gentle, never "Do you remember…?".
 */
@Composable
private fun ChatDialog(question: String, onAnother: () -> Unit, onDone: () -> Unit, canAskAnother: Boolean) {
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDone,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        LaunchedEffect(Unit) { focus.requestFocusSafely() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.92f))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.safeHorizontal, vertical = Dimens.safeVertical),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .background(colors.surface, RoundedCornerShape(Dimens.cornerRadius))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("💬 Let's chat", style = WillowType.title, color = colors.accent, modifier = Modifier.semantics { heading() })
                Text(
                    "For the caregiver: a gentle way to start a conversation. There are no right or wrong answers.",
                    style = WillowType.label,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Text(question, style = WillowType.title, color = colors.textPrimary, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                    if (canAskAnother) {
                        BigButton("Another question", onClick = onAnother, minWidth = 320.dp, modifier = Modifier.focusRequester(focus))
                        BigButton("Done", onClick = onDone, minWidth = 200.dp)
                    } else {
                        BigButton("Done", onClick = onDone, minWidth = 200.dp, modifier = Modifier.focusRequester(focus))
                    }
                }
            }
        }
    }
}

/** At the last hint level, focus jumps to the answer so pressing OK always succeeds. */
@Composable
private fun GuideFocus(s: PlayerUiState.Playing, requesters: List<FocusRequester>) {
    LaunchedEffect(s.hintLevel, s.hintIndex) {
        if (s.hintLevel == HintLevel.GUIDE) s.hintIndex?.let { requesters.getOrNull(it)?.requestFocusSafely() }
    }
}

/** 1–4 tiles: one row. 5–6: 3 per row (wide tiles keep labels on one line). 7–8: 4 per row. */
@Composable
private fun TileGrid(count: Int, modifier: Modifier = Modifier, tile: @Composable (Int, Modifier) -> Unit) {
    val columns = when {
        count <= 4 -> count.coerceAtLeast(1)
        count <= 6 -> 3
        else -> 4
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        (0 until count).chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { index -> tile(index, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f).height(1.dp)) }
            }
        }
    }
}
