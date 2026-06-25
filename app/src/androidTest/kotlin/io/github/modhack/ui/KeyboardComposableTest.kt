package io.github.modhack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.modhack.model.Key
import io.github.modhack.model.KeyboardLayout
import io.github.modhack.model.Row
import io.github.modhack.ui.theme.MHTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for keyboard Compose components.
 *
 * Tests rendering of individual composables using Compose testing APIs.
 * These tests verify that the UI components render correctly with
 * provided data, without requiring the full service infrastructure.
 */
@RunWith(AndroidJUnit4::class)
class KeyboardComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `PopupComposable renders key label`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(keyLabel = "A")
            }
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable renders popup keys`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "e",
                    popupKeys = listOf("é", "è", "ê")
                )
            }
        }

        composeTestRule.onNodeWithText("e").assertIsDisplayed()
        composeTestRule.onNodeWithText("é").assertIsDisplayed()
        composeTestRule.onNodeWithText("è").assertIsDisplayed()
        composeTestRule.onNodeWithText("ê").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable renders without popup keys`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "Enter",
                    popupKeys = null
                )
            }
        }

        composeTestRule.onNodeWithText("Enter").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable renders with empty popup keys`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "Space",
                    popupKeys = emptyList()
                )
            }
        }

        composeTestRule.onNodeWithText("Space").assertIsDisplayed()
    }

    @Test
    fun `MHTheme applies dark theme correctly`() {
        composeTestRule.setContent {
            MHTheme(
                themeId = "material_dark",
                darkTheme = true
            ) {
                PopupComposable(keyLabel = "Test")
            }
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }

    @Test
    fun `MHTheme applies light theme correctly`() {
        composeTestRule.setContent {
            MHTheme(
                themeId = "material_light",
                darkTheme = false
            ) {
                PopupComposable(keyLabel = "Test")
            }
        }

        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
    }

    @Test
    fun `MHTheme applies AMOLED black theme`() {
        composeTestRule.setContent {
            MHTheme(themeId = "amoled_black") {
                PopupComposable(keyLabel = "AMOLED")
            }
        }

        composeTestRule.onNodeWithText("AMOLED").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable renders long label`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "Shift",
                    popupKeys = listOf("CAPS")
                )
            }
        }

        composeTestRule.onNodeWithText("Shift").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable handles single character popup key`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "n",
                    popupKeys = listOf("ñ")
                )
            }
        }

        composeTestRule.onNodeWithText("ñ").assertIsDisplayed()
    }

    @Test
    fun `PopupComposable handles unicode popup keys`() {
        composeTestRule.setContent {
            MHTheme(themeId = "material_dark") {
                PopupComposable(
                    keyLabel = "a",
                    popupKeys = listOf("á", "à", "â", "ä", "ã", "å")
                )
            }
        }

        composeTestRule.onNodeWithText("á").assertIsDisplayed()
        composeTestRule.onNodeWithText("ä").assertIsDisplayed()
    }
}
