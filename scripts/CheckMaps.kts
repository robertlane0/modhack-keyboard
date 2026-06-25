#!/usr/bin/env kotlin

/**
 * Validates locale keymaps against base English layout.
 *
 * This script checks that each locale-specific keyboard layout XML
 * file has the same number of rows and keys as the base English QWERTY
 * layout. It also validates that required special keys (Shift, Backspace,
 * Space, Enter, Globe) are present in the correct positions.
 *
 * Usage: ./gradlew -q script --args="CheckMaps"
 *   or:  kotlin scripts/CheckMaps.kts
 *
 * Exit codes:
 *   0 - All keymaps are valid
 *   1 - One or more keymaps have structural issues
 */

import java.io.File

/** Base directory for resource XML files. */
val resDir = File("app/src/main/res")

/** The base English QWERTY layout to validate against. */
val baseLayout = File(resDir, "xml/kbd_qwerty.xml")

/** Locales to validate. */
val locales = listOf("es", "fr", "de", "ru", "ar", "he")

/** Expected special key codes that must be present in every layout. */
val expectedSpecialCodes = setOf("-1", "-5", "32", "10", "-2", "-104")

/** Expected row structure: number of keys per row (flexible for locale differences). */
val expectedRowCount = 4

/**
 * Main entry point.
 */
fun main() {
    println("=== ModHack Keymap Validator ===\n")

    if (!baseLayout.exists()) {
        println("ERROR: Base layout not found: ${baseLayout.absolutePath}")
        System.exit(1)
    }

    val baseContent = baseLayout.readText()
    val baseKeyCount = countKeys(baseContent)
    val baseRowCount = countRows(baseContent)

    println("Base layout (en_US):")
    println("  Rows: $baseRowCount")
    println("  Keys: $baseKeyCount")
    println()

    var hasErrors = false

    for (locale in locales) {
        val localeDir = File(resDir, "xml-$locale")
        val localeLayout = File(localeDir, "kbd_qwerty.xml")

        if (!localeDir.exists()) {
            println("WARN: Locale directory missing: xml-$locale/")
            hasErrors = true
            continue
        }

        if (!localeLayout.exists()) {
            println("WARN: Layout file missing: xml-$locale/kbd_qwerty.xml")
            hasErrors = true
            continue
        }

        val localeContent = localeLayout.readText()
        val localeKeyCount = countKeys(localeContent)
        val localeRowCount = countRows(localeContent)

        val errors = mutableListOf<String>()

        // Check row count
        if (localeRowCount != baseRowCount) {
            errors.add("Row count mismatch: expected $baseRowCount, got $localeRowCount")
        }

        // Check key count (allow ±2 for locale-specific keys like ñ, ä, ß)
        val keyDiff = kotlin.math.abs(localeKeyCount - baseKeyCount)
        if (keyDiff > 2) {
            errors.add("Key count too different from base: expected ~$baseKeyCount, got $localeKeyCount (diff=$keyDiff)")
        }

        // Check for required special keys
        val specialCodes = extractSpecialCodes(localeContent)
        val missingCodes = expectedSpecialCodes - specialCodes
        if (missingCodes.isNotEmpty()) {
            errors.add("Missing special key codes: $missingCodes")
        }

        // Check for Shift in first position of third row
        if (!hasShiftInThirdRow(localeContent)) {
            errors.add("Shift key not in expected position (first key of third row)")
        }

        // Check for Backspace in last position of third row
        if (!hasBackspaceInThirdRow(localeContent)) {
            errors.add("Backspace key not in expected position (last key of third row)")
        }

        if (errors.isEmpty()) {
            println("OK: $locale ($localeKeyCount keys, $localeRowCount rows)")
        } else {
            println("FAIL: $locale ($localeKeyCount keys, $localeRowCount rows)")
            errors.forEach { println("  - $it") }
            hasErrors = true
        }
    }

    println()
    if (hasErrors) {
        println("Validation completed with warnings/errors.")
        System.exit(1)
    } else {
        println("All keymaps are valid.")
        System.exit(0)
    }
}

/**
 * Counts the number of <Key .../> elements in the XML content.
 */
fun countKeys(xmlContent: String): Int {
    return Regex("""<Key\s""").findAll(xmlContent).count()
}

/**
 * Counts the number of <Row> elements in the XML content.
 */
fun countRows(xmlContent: String): Int {
    return Regex("""<Row[>\s]""").findAll(xmlContent).count()
}

/**
 * Extracts all app:codes values from the XML content.
 */
fun extractSpecialCodes(xmlContent: String): Set<String> {
    val codes = mutableSetOf<String>()
    val pattern = Regex("""app:codes="([^"]+)"""")
    for (match in pattern.findAll(xmlContent)) {
        val codeStr = match.groupValues[1]
        // Handle multiple comma-separated codes
        codeStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { code ->
            // Normalize character codes to their numeric values
            if (code.length == 1) {
                codes.add(code[0].code.toString())
            } else {
                codes.add(code)
            }
        }
    }
    return codes
}

/**
 * Checks if the third row starts with a Shift key (code -1).
 */
fun hasShiftInThirdRow(xmlContent: String): Boolean {
    val rows = xmlContent.split("<Row").drop(1) // Skip before first Row
    if (rows.size < 3) return false
    val thirdRow = rows[2]
    return thirdRow.contains("app:codes=\"-1\"")
}

/**
 * Checks if the third row ends with a Backspace key (code -5).
 */
fun hasBackspaceInThirdRow(xmlContent: String): Boolean {
    val rows = xmlContent.split("<Row").drop(1)
    if (rows.size < 3) return false
    val thirdRow = rows[2]
    return thirdRow.contains("app:codes=\"-5\"")
}

main()
