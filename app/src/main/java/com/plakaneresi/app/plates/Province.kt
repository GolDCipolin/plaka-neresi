package com.plakaneresi.app.plates

/** The seven geographic regions, spelled the way they are taught in school. */
enum class Region(val displayName: String) {
    MARMARA("Marmara"),
    EGE("Ege"),
    AKDENIZ("Akdeniz"),
    IC_ANADOLU("İç Anadolu"),
    KARADENIZ("Karadeniz"),
    DOGU_ANADOLU("Doğu Anadolu"),
    GUNEYDOGU_ANADOLU("Güneydoğu Anadolu"),
}

/**
 * One province and the plate code issued for it.
 *
 * @param aliases other names people still use for the same place — the old official
 *   name (İçel for Mersin), or the everyday short form (Antep, Urfa, Maraş). They are
 *   searchable but never displayed.
 */
data class Province(
    val code: Int,
    val name: String,
    val region: Region,
    val aliases: List<String> = emptyList(),
) {
    /** The code as it is painted on the plate: 1 becomes "01", 48 stays "48". */
    val plateCode: String = code.toString().padStart(2, '0')

    internal val foldedName: String = TurkishText.fold(name)
    internal val foldedAliases: List<String> = aliases.map(TurkishText::fold)
}
