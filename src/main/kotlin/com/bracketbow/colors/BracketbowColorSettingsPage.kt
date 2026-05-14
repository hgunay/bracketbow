package com.bracketbow.colors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * IDE Settings → Editor → Color Scheme → "Bracketbow" sayfasını
 * oluşturur. Kullanıcı buradan her seviyenin rengini değiştirebilir.
 */
class BracketbowColorSettingsPage : ColorSettingsPage {

    private val descriptors: Array<AttributesDescriptor> =
        BracketbowColors.LEVELS.mapIndexed { index, key ->
            AttributesDescriptor("Level $index", key)
        }.toTypedArray()

    override fun getDisplayName(): String = "Bracketbow"

    override fun getIcon(): Icon? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = PlainSyntaxHighlighter()

    override fun getDemoText(): String = """
        function fibonacci(n) {
            if (n < 2) return n;
            return fibonacci(n - 1) + fibonacci(n - 2);
        }

        const data = {
            users: [
                { id: 1, tags: ["admin", "active"] },
                { id: 2, tags: ["guest"] }
            ]
        };
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null
}
