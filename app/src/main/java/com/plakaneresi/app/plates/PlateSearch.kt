package com.plakaneresi.app.plates

/** Why a province matched. The order of the constants is the order results are shown in. */
enum class MatchKind {
    /** The query is exactly this province's code: "48" or "48" typed as "048"'s prefix. */
    EXACT_CODE,

    /** The query is the start of this code — "4" while the user is still typing "41". */
    CODE_PREFIX,

    /** The province name starts with the query: "mug" for Muğla. */
    NAME_PREFIX,

    /** The query appears somewhere inside the name: "hisar" for Afyonkarahisar. */
    NAME_CONTAINS,

    /** An old or colloquial name matched: "içel" for Mersin, "urfa" for Şanlıurfa. */
    ALIAS,
}

data class SearchHit(val province: Province, val kind: MatchKind)

/**
 * Matches free text against the province table.
 *
 * The query is treated as a code when it is all digits and as a name otherwise, which
 * covers everything a user can sensibly type: no province name contains a digit and no
 * code contains a letter.
 */
object PlateSearch {

    fun query(raw: String, source: List<Province> = Provinces.all): List<SearchHit> {
        val tokens = TurkishText.tokens(raw)
        if (tokens.isEmpty()) return emptyList()

        val needle = tokens.joinToString(separator = "")
        return if (needle.all(Char::isDigit)) byCode(needle, source) else byName(tokens, needle, source)
    }

    private fun byCode(digits: String, source: List<Province>): List<SearchHit> {
        // Codes are two digits; anything longer cannot be one.
        if (digits.length > 2) return emptyList()

        val exact = digits.toIntOrNull()
        val hits = mutableListOf<SearchHit>()

        source.firstOrNull { it.code == exact }
            ?.let { hits += SearchHit(it, MatchKind.EXACT_CODE) }

        // A single "4" is both Ağrı (04) and the start of 40–49, and we cannot tell
        // which the user meant yet, so offer the exact match first and the rest under it.
        source.asSequence()
            .filter { it.code != exact && it.plateCode.startsWith(digits) }
            .forEach { hits += SearchHit(it, MatchKind.CODE_PREFIX) }

        return hits
    }

    private fun byName(
        tokens: List<String>,
        needle: String,
        source: List<Province>,
    ): List<SearchHit> =
        source.mapNotNull { province ->
            val kind = when {
                province.foldedName.startsWith(needle) -> MatchKind.NAME_PREFIX
                province.foldedName.contains(needle) -> MatchKind.NAME_CONTAINS
                // "K. Maraş" / "kahraman maras": every piece has to be in there somewhere.
                tokens.size > 1 && tokens.all { province.foldedName.contains(it) } -> MatchKind.NAME_CONTAINS
                province.foldedAliases.any { it.contains(needle) } -> MatchKind.ALIAS
                else -> null
            }
            kind?.let { SearchHit(province, it) }
        }.sortedWith(compareBy({ it.kind.ordinal }, { it.province.code }))
}
