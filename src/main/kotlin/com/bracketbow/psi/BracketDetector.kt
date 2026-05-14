package com.bracketbow.psi

import com.intellij.psi.PsiElement

/**
 * Bir PSI elemanının parantez olup olmadığını ve hangi tür parantez
 * olduğunu belirler. Saf (yan etkisiz) yardımcı sınıf.
 */
object BracketDetector {

    private const val OPEN_BRACKETS = "([{"
    private const val CLOSE_BRACKETS = ")]}"

    /**
     * Eleman tek karakterli bir parantez yaprağı (leaf) mı?
     * Yaprak = PSI ağacında alt çocuğu olmayan terminal eleman.
     */
    fun isBracketLeaf(element: PsiElement): Boolean {
        if (element.firstChild != null) return false
        val text = element.text
        if (text.length != 1) return false
        val ch = text[0]
        return ch in OPEN_BRACKETS || ch in CLOSE_BRACKETS
    }

    /**
     * Bir eleman, içine girdiğinde derinlik artıracak bir parantez
     * grubu mu? Yani ilk çocuğu açılış, son çocuğu eşleşen kapanış mı?
     */
    fun isBracketGroup(element: PsiElement): Boolean {
        val first = element.firstChild ?: return false
        val last = element.lastChild ?: return false
        if (first === last) return false

        val firstText = first.text
        val lastText = last.text
        if (firstText.length != 1 || lastText.length != 1) return false

        return matches(firstText[0], lastText[0])
    }

    private fun matches(open: Char, close: Char): Boolean = when (open) {
        '(' -> close == ')'
        '[' -> close == ']'
        '{' -> close == '}'
        else -> false
    }
}
