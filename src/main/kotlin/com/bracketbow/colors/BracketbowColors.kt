package com.bracketbow.colors

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

/**
 * Her iç içe geçme seviyesi için bir TextAttributesKey tutar.
 * Bu key'ler Settings → Editor → Color Scheme → Bracketbow
 * altında kullanıcı tarafından özelleştirilebilir.
 *
 * Varsayılan renkler resources/colorSchemes/ altındaki XML dosyalarından
 * yüklenir.
 */
object BracketbowColors {

    val LEVELS: Array<TextAttributesKey> = arrayOf(
        level(0),
        level(1),
        level(2),
        level(3),
        level(4),
        level(5),
        level(6)
    )

    private fun level(index: Int): TextAttributesKey =
        TextAttributesKey.createTextAttributesKey(
            "BRACKETBOW_LEVEL_$index",
            DefaultLanguageHighlighterColors.BRACKETS
        )

    /**
     * Verilen derinlik için uygun rengi döndürür.
     * [levels] parametresi kaç seviyenin döngüye gireceğini belirler;
     * belirtilmezse tanımlı tüm seviyeler kullanılır.
     */
    fun forDepth(depth: Int, levels: Int = LEVELS.size): TextAttributesKey {
        val effectiveLevels = levels.coerceIn(3, LEVELS.size)
        val safeDepth = if (depth < 0) 0 else depth
        return LEVELS[safeDepth % effectiveLevels]
    }
}
