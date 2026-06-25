package io.github.modhack.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.modhack.R

/**
 * Main Activity — Setup wizard that walks the user through enabling
 * and selecting ModHack Keyboard as their input method.
 *
 * Steps:
 * 1. Enable ModHack in system IME settings
 * 2. Switch current keyboard to ModHack
 * 3. Choose language subtypes (future)
 *
 * The wizard checks the current state on resume and advances
 * automatically when a step is completed externally.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SetupWizard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The wizard will recompose and check IME status
    }
}

/**
 * Root composable for the setup wizard flow.
 */
@Composable
fun SetupWizard() {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    // Check IME status to determine starting step
    val isImeEnabled = remember { isModHackEnabled(context) }
    val isImeSelected = remember { isModHackSelected(context) }

    if (isImeEnabled && isImeSelected) {
        currentStep = 3
    } else if (isImeEnabled) {
        currentStep = 2
    } else {
        currentStep = 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Step indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepIndicator(step = 1, label = "Enable", isActive = currentStep == 1, isComplete = currentStep > 1)
            Spacer(modifier = Modifier.width(16.dp))
            StepIndicator(step = 2, label = "Select", isActive = currentStep == 2, isComplete = currentStep > 2)
            Spacer(modifier = Modifier.width(16.dp))
            StepIndicator(step = 3, label = "Done", isActive = currentStep == 3, isComplete = false)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Step content
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "step_content"
        ) { step ->
            when (step) {
                1 -> EnableStep(context) { currentStep = 2 }
                2 -> SelectStep(context) { currentStep = 3 }
                3 -> CompleteStep(context)
            }
        }
    }
}

/**
 * Step 1: Enable ModHack Keyboard in system settings.
 */
@Composable
private fun EnableStep(context: Context, onComplete: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.setup_step1_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.setup_step1_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Create, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_enable_ime))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Re-check button
        OutlinedButton(
            onClick = {
                if (isModHackEnabled(context)) {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("I've enabled it — Continue")
        }
    }
}

/**
 * Step 2: Switch the current keyboard to ModHack.
 */
@Composable
private fun SelectStep(context: Context, onComplete: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.setup_step2_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.setup_step2_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_select_ime))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (isModHackSelected(context)) {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("I've selected it — Continue")
        }
    }
}

/**
 * Step 3: Setup complete — offers to open settings or finish.
 */
@Composable
private fun CompleteStep(context: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.setup_complete),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, io.github.modhack.activity.SettingsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.btn_open_settings))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { (context as? ComponentActivity)?.finish() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done")
        }
    }
}

/**
 * Circular step indicator with label.
 */
@Composable
private fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isComplete: Boolean
) {
    val bgColor = when {
        isComplete -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isComplete -> MaterialTheme.colorScheme.onPrimary
        isActive -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

/**
 * Checks if ModHack Keyboard is enabled in the system's input method list.
 */
private fun isModHackEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val enabledIds = imm.enabledInputMethodList.map { it.id }
    return enabledIds.any { it.contains("io.github.modhack") }
}

/**
 * Checks if ModHack Keyboard is the currently selected input method.
 */
private fun isModHackSelected(context: Context): Boolean {
    val currentId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    )
    return currentId?.contains("io.github.modhack") == true
}
