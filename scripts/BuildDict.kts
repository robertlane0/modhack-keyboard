#!/usr/bin/env kotlin

/**
 * Compiles raw word lists into the v200 binary .dict format.
 *
 * Usage:
 *   kotlin BuildDict.kts <input.txt> [output.dict]
 *
 * Input format:
 *   One word per line. Lines starting with '#' are comments.
 *   Optional tab-separated frequency (default: 1).
 *
 * Output format:
 *   v200 binary trie with 22-bit child addresses, compatible with
 *   the Rust dictionary engine (libmhdict.so).
 *
 * Format specification:
 *   - Magic: "MHDT" (4 bytes)
 *   - Version: 200 (2 bytes, big-endian)
 *   - Word count: 4 bytes, big-endian
 *   - Root node address: 3 bytes, big-endian (22-bit)
 *   - Trie nodes: variable length, 22-bit child pointers
 *   - Word data: null-terminated UTF-8 strings with frequency
 */

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

// ── Trie Node ────────────────────────────────────────────────────────

data class TrieNode(
    val children: MutableMap<Char, TrieNode> = mutableMapOf(),
    var frequency: Int = 0,
    var isEndOfWord: Boolean = false
)

// ── Build trie from word list ────────────────────────────────────────

fun buildTrie(words: Map<String, Int>): TrieNode {
    val root = TrieNode()
    for ((word, freq) in words) {
        var node = root
        for (ch in word.lowercase()) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        node.isEndOfWord = true
        node.frequency = maxOf(node.frequency, freq)
    }
    return root
}

// ── Serialize trie to v200 binary format ─────────────────────────────

fun serialize(root: TrieNode, wordCount: Int): ByteArray {
    val nodeAddresses = mutableMapOf<TrieNode, Int>()
    val nodeList = mutableListOf<TrieNode>()
    val dataBuffer = mutableListOf<Byte>()

    // Collect all nodes in DFS order for address assignment
    fun collectNodes(node: TrieNode) {
        if (node in nodeAddresses) return
        nodeAddresses[node] = nodeList.size
        nodeList.add(node)
        for (child in node.children.values) {
            collectNodes(child)
        }
    }
    collectNodes(root)

    // Build the output
    val output = ByteArrayOutputStream()

    // Magic bytes: "MHDT"
    output.write(byteArrayOf('M'.code.toByte(), 'H'.code.toByte(), 'D'.code.toByte(), 'T'.code.toByte()))

    // Version: 200 (big-endian uint16)
    output.write(byteArrayOf(((200 shr 8) and 0xFF).toByte(), (200 and 0xFF).toByte()))

    // Word count (big-endian uint32)
    output.write(byteArrayOf(
        ((wordCount shr 24) and 0xFF).toByte(),
        ((wordCount shr 16) and 0xFF).toByte(),
        ((wordCount shr 8) and 0xFF).toByte(),
        (wordCount and 0xFF).toByte()
    ))

    // Serialize trie nodes
    // Each node: flags (1 byte) + children count (1 byte) + children data
    // For each child: char (2 bytes) + address (3 bytes, 22-bit)
    // If end-of-word: frequency (4 bytes) + word length (1 byte)
    val serializedData = mutableListOf<Byte>()

    fun serializeNode(node: TrieNode) {
        val children = node.children.entries.sortedBy { it.key }
        val childCount = children.size

        // Flags: bit 0 = isEndOfWord, bits 1-7 = reserved
        val flags = if (node.isEndOfWord) 0x01 else 0x00
        serializedData.add(flags.toByte())
        serializedData.add(childCount.toByte())

        // Children
        for ((ch, child) in children) {
            val addr = nodeAddresses[child] ?: 0

            // Character as uint16 (big-endian)
            serializedData.add(((ch.code shr 8) and 0xFF).toByte())
            serializedData.add((ch.code and 0xFF).toByte())

            // Address as 22-bit (3 bytes, big-endian)
            serializedData.add(((addr shr 16) and 0xFF).toByte())
            serializedData.add(((addr shr 8) and 0xFF).toByte())
            serializedData.add((addr and 0xFF).toByte())
        }

        // Word data if end of word
        if (node.isEndOfWord) {
            val freq = node.frequency
            serializedData.add(((freq shr 24) and 0xFF).toByte())
            serializedData.add(((freq shr 16) and 0xFF).toByte())
            serializedData.add(((freq shr 8) and 0xFF).toByte())
            serializedData.add((freq and 0xFF).toByte())
        }

        // Serialize children
        for ((_, child) in children) {
            serializeNode(child)
        }
    }

    serializeNode(root)

    // Write trie data length (big-endian uint32)
    val trieDataLen = serializedData.size
    output.write(byteArrayOf(
        ((trieDataLen shr 24) and 0xFF).toByte(),
        ((trieDataLen shr 16) and 0xFF).toByte(),
        ((trieDataLen shr 8) and 0xFF).toByte(),
        (trieDataLen and 0xFF).toByte()
    ))

    // Write trie data
    output.write(serializedData.toByteArray())

    return output.toByteArray()
}

// ── Simple ByteArrayOutputStream replacement ─────────────────────────

class ByteArrayOutputStream {
    private val buffer = mutableListOf<Byte>()

    fun write(bytes: ByteArray) {
        buffer.addAll(bytes.toList())
    }

    fun toByteArray(): ByteArray = buffer.toByteArray()
}

// ── Main ─────────────────────────────────────────────────────────────

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: BuildDict.kts <input.txt> [output.dict]")
        println()
        println("  input.txt   - Word list, one word per line (freq optional, tab-separated)")
        println("  output.dict - Output binary dictionary (default: same name with .dict)")
        return
    }

    val inputFile = File(args[0])
    if (!inputFile.exists()) {
        System.err.println("Error: Input file not found: ${inputFile.path}")
        System.exit(1)
    }

    val outputFile = if (args.size > 1) File(args[1]) {
        File(inputFile.parent, inputFile.nameWithoutExtension + ".dict")
    }

    // Parse word list
    val words = mutableMapOf<String, Int>()
    var lineNum = 0

    inputFile.forEachLine { line ->
        lineNum++
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine

        val parts = trimmed.split("\t", limit = 2)
        val word = parts[0].trim().lowercase()
        val freq = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 1

        if (word.isNotEmpty() && word.all { it.isLetter() }) {
            words[word] = maxOf(words[word] ?: 0, freq)
        }
    }

    if (words.isEmpty()) {
        System.err.println("Error: No valid words found in input file.")
        System.exit(1)
    }

    println("Loaded ${words.size} words from ${inputFile.name}")

    // Build trie
    val trie = buildTrie(words)
    println("Built trie with ${countNodes(trie)} nodes")

    // Serialize
    val binary = serialize(trie, words.size)
    println("Serialized to ${binary.size} bytes (${binary.size / 1024} KB)")

    // Write output
    outputFile.parentFile?.mkdirs()
    FileOutputStream(outputFile).use { it.write(binary) }
    println("Wrote dictionary to: ${outputFile.path}")
    println("Done.")
}

fun countNodes(node: TrieNode): Int {
    var count = 1
    for (child in node.children.values) {
        count += countNodes(child)
    }
    return count
}

main(args)
