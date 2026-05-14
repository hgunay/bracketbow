package com.bracketbow.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.bracketbow.colors.BracketbowColors
import com.bracketbow.psi.BracketDetector
import com.bracketbow.psi.DepthCalculator
import com.bracketbow.settings.BracketbowSettings

/**
 * IntelliJ tarafından her PSI elemanı için çağrılan annotator.
 *
 * İş akışı:
 *   1. Plugin etkin mi? Değilse atla.
 *   2. Eleman parantez yaprağı mı? Değilse atla.
 *   3. Dosyanın dili etkin mi? Değilse atla.
 *   4. Parantez türü etkin mi? Değilse atla.
 *   5. Derinliği hesapla ve rengi uygula.
 *
 * Tüm gerçek mantık psi/ ve colors/ paketlerinde; bu sınıf sadece
 * bunları birbirine bağlıyor.
 */
class BracketbowAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val settings = BracketbowSettings.getInstance(element.project).state
        if (!settings.enabled) return

        if (!BracketDetector.isBracketLeaf(element)) return

        // Dil kontrolü
        val languageId = element.containingFile?.language?.id?.uppercase() ?: ""
        val langEnabled = when (languageId) {
            "JAVA"   -> settings.enableJava
            "KOTLIN" -> settings.enableKotlin
            "XML"    -> settings.enableXml
            "HTML"   -> settings.enableHtml
            "JSON"   -> settings.enableJson
            else     -> true
        }
        if (!langEnabled) return

        // Parantez türü kontrolü
        val ch = element.text.firstOrNull() ?: return
        val typeEnabled = when (ch) {
            '(', ')' -> settings.colorRoundBrackets
            '[', ']' -> settings.colorSquareBrackets
            '{', '}' -> settings.colorCurlyBrackets
            else     -> false
        }
        if (!typeEnabled) return

        val depth = DepthCalculator.depthOf(element)
        val colorKey = BracketbowColors.forDepth(depth, settings.colorLevels)

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .textAttributes(colorKey)
            .create()
    }
}
