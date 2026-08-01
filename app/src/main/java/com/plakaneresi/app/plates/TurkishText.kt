package com.plakaneresi.app.plates

/**
 * Folds text down to plain lowercase ASCII so that what a tourist types on a phone
 * keyboard matches how the province is actually spelled.
 *
 * [String.lowercase] is a trap for Turkish and cannot be used here:
 *  - under a Turkish locale it maps `I` to `ı`, so "ISPARTA" stops matching "Isparta";
 *  - under the root locale it maps `İ` to `i` + U+0307 (combining dot), so "İZMİR"
 *    becomes a 7-character string that never equals the 5-character "izmir".
 *
 * Mapping every character by hand avoids both, and makes results identical no matter
 * what language the phone is set to.
 */
object TurkishText {

    private val FOLD: Map<Char, Char> = buildMap {
        put('ç', 'c'); put('Ç', 'c')
        put('ğ', 'g'); put('Ğ', 'g')
        put('ı', 'i'); put('I', 'i')
        put('i', 'i'); put('İ', 'i')
        put('ö', 'o'); put('Ö', 'o')
        put('ş', 's'); put('Ş', 's')
        put('ü', 'u'); put('Ü', 'u')
        // Circumflexes appear in official spellings such as Hakkâri.
        put('â', 'a'); put('Â', 'a')
        put('î', 'i'); put('Î', 'i')
        put('û', 'u'); put('Û', 'u')
    }

    private val SEPARATORS = Regex("[^\\p{L}\\p{N}]+")

    /** Returns [input] as lowercase ASCII letters and digits only; everything else is dropped. */
    fun fold(input: String): String {
        val out = StringBuilder(input.length)
        for (ch in input) {
            val mapped = FOLD[ch]
            when {
                mapped != null -> out.append(mapped)
                ch in 'A'..'Z' -> out.append(ch + 32)
                ch in 'a'..'z' || ch in '0'..'9' -> out.append(ch)
                else -> Unit
            }
        }
        return out.toString()
    }

    /**
     * Splits [input] on spaces and punctuation and folds each piece.
     *
     * Province names are stored as one run of letters ("kahramanmaras"), so a query has
     * to be broken up before it can be matched piecewise — otherwise "K. Maraş" folds to
     * "kmaras" and matches nothing.
     */
    fun tokens(input: String): List<String> =
        input.split(SEPARATORS).map(::fold).filter(String::isNotEmpty)
}
