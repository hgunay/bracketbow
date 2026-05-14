package com.bracketbow.psi

import com.intellij.psi.PsiElement

/**
 * Bir parantez yaprağının iç içe geçme derinliğini hesaplar.
 *
 * Yaklaşım: PSI ağacında parent'ları yukarı doğru gez. Her ata,
 * eğer ilk-son çocuğu eşleşen parantez çifti ise, bir "parantez
 * grubu"dur ve derinliği bir artırır.
 *
 * Örnek: ((()))  içindeki en içteki '(' karakteri için
 *   - parent #1: en içteki paren ifadesi  → grup → depth 0
 *   - parent #2: ortadaki paren ifadesi   → grup → depth 1
 *   - parent #3: en dıştaki paren ifadesi → grup → depth 2
 * Sonuç: 2 (yani 3. seviye renk)
 */
object DepthCalculator {

    fun depthOf(bracketLeaf: PsiElement): Int {
        var depth = -1 // Kendi sarmalayıcımızı saymamak için -1'den başla
        var current: PsiElement? = bracketLeaf.parent

        while (current != null) {
            if (BracketDetector.isBracketGroup(current)) {
                depth++
            }
            current = current.parent
        }

        return if (depth < 0) 0 else depth
    }
}
