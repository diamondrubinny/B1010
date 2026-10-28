package net.rubinny.willowbrook.ui.screens.play

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import net.rubinny.willowbrook.R
import net.rubinny.willowbrook.game.engine.HintLevel
import net.rubinny.willowbrook.game.model.Step
import net.rubinny.willowbrook.ui.components.BigButton
import net.rubinny.willowbrook.ui.components.CompanionBar
import net.rubinny.willowbrook.ui.components.ConfirmDialog
import net.rubinny.willowbrook.ui.components.InfoDialog
import net.rubinny.willowbrook.ui.components.Portrait
import net.rubinny.willowbrook.ui.components.SceneBackground
import net.rubinny.willowbrook.ui.components.ScreenScaffold
import net.rubinny.willowbrook.ui.components.requestFocusSafely
import net.rubinny.willowbrook.ui.theme.Dimens
import net.rubinny.willowbrook.ui.theme.LocalWillowColors
import net.rubinny.willowbrook.ui.theme.WillowType

@Composable
fun ChapterPlayerScreen(viewModel: ChapterPlayerViewModel, onGoHome: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    var showWhat by rememberSaveable { mutableStateOf(false) }

    BackHandler { confirmLeave = true }

    when (val s = state) {
        PlayerUiState.Loading -> ScreenScaffold(title = "Opening the story…") {}
        is PlayerUiState.Error -> ErrorScreen(s.message, onGoHome)
        is PlayerUiState.Playing -> SceneBackground(scene = s.chapter.scene) {
            when (s.phase) {
                Phase.RECAP -> RecapPhase(s, viewModel::onRecapContinue)
                Phase.CHAPTER_INTRO -> ChapterIntroPhase(s, viewModel::onChapterIntroContinue)
                Phase.STEP -> StepPhase(s, viewModel, onWhatAreWeDoing = { showWhat = true })
                Phase.CHAPTER_COMPLETE -> ChapterCompletePhase(
                    s,
                    onNext = { if (!viewModel.onNextChapter()) onGoHome() },
                    onRest = onGoHome,
                )
                Phase.CASE_FINISHED -> CaseFinishedPhase(onGoHome)
            }
            if (showWhat) {
                // Big picture + current chapter. The current task itself is always in Marmalade's bar.
                InfoDialog(
                    title = "What are we doing?",
                    paragraphs = listOf(s.case.premise, "Right now: ${s.chapter.goal}"),
                    buttonText = "Thank you",
                    onDismiss = { showWhat = false },
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
        Portrait(key = "marmalade", emoji = "🐈", size = 96.dp)
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
private fun ChapterCompletePhase(s: PlayerUiState.Playing, onNext: () -> Unit, onRest: () -> Unit) {
    val focus = remember { FocusRequester() }
    val next = s.case.chapters.getOrNull(s.chapterIndex + 1)?.takeIf { !s.replay }
    CenterCard {
        Icon(painterResource(R.drawable.ic_lantern), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(120.dp))
        Heading("Well done, detective!")
        Body("You finished \"${s.chapter.title}\".")
        if (next == null && !s.replay) Body("More chapters are on the way.", LocalWillowColors.current.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            if (next != null) {
                BigButton("Next: ${next.title}", onClick = onNext, minWidth = 360.dp, modifier = Modifier.focusRequester(focus))
                BigButton("Rest now", onClick = onRest, minWidth = 240.dp)
            } else {
                BigButton("Back home", onClick = onRest, modifier = Modifier.focusRequester(focus))
            }
        }
    }
    LaunchedEffect(s.chapterIndex) { focus.requestFocusSafely() }
}

@Composable
private fun CaseFinishedPhase(onGoHome: () -> Unit) {
    val focus = remember { FocusRequester() }
    CenterCard {
        Icon(painterResource(R.drawable.ic_lantern), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(120.dp))
        Heading("You have finished every chapter so far!")
        Body("More chapters are on the way. You can play any chapter again from Case files.")
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
            is Step.Narration -> NarrationStep(s, step, viewModel::onStepContinue, onWhatAreWeDoing)
            is Step.Reward -> RewardStep(s, step, viewModel::onStepContinue)
            is Step.HiddenObject -> HiddenObjectStep(s, viewModel, onWhatAreWeDoing)
            is Step.MemoryMatch -> MemoryStep(s, step, viewModel, onWhatAreWeDoing)
        }
    }
    }
}

@Composable
private fun ColumnScope.NarrationStep(
    s: PlayerUiState.Playing,
    step: Step.Narration,
    onContinue: () -> Unit,
    onWhatAreWeDoing: () -> Unit,
) {
    val colors = LocalWillowColors.current
    val focus = remember { FocusRequester() }
    Text(s.chapter.location, style = WillowType.label, color = colors.textSecondary)
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
            step.speaker?.let { Text(it, style = WillowType.button, color = colors.accent) }
            Text(step.text, style = WillowType.body, color = colors.textPrimary)
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
private fun ColumnScope.RewardStep(s: PlayerUiState.Playing, step: Step.Reward, onContinue: () -> Unit) {
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
        Text(
            step.text,
            style = WillowType.title,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(Dimens.cornerRadius))
                .padding(28.dp),
        )
    }
    Spacer(Modifier.weight(1f))
    BigButton("Continue", onClick = onContinue, modifier = Modifier.align(Alignment.CenterHorizontally).focusRequester(focus))
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
        Box(Modifier.weight(1f)) { goal() }
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
                Text("Please find", style = WillowType.label, color = colors.textSecondary)
                Text("${ui.found.size} of ${ui.targets.size} found", style = WillowType.button, color = colors.textPrimary)
            }
        },
        complete = ui.complete,
        continueFocus = continueFocus,
        onHelp = viewModel::onHelpMe,
        onContinue = viewModel::onStepContinue,
        onWhatAreWeDoing = onWhatAreWeDoing,
    )
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
                Text("Matching pairs", style = WillowType.label, color = colors.textSecondary)
                Text("$pairsDone of ${ui.cards.size / 2} found", style = WillowType.button, color = colors.textPrimary)
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
