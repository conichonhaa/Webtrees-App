package com.genealogie.webtrees

data class PersonRef(val xref: String, val name: String)

data class Marriage(
    val date: String = "",
    val place: String = "",
    val spouseXref: String = "",
    val spouseName: String = ""
)

data class Person(
    val xref: String,
    val tree: String,
    val givenName: String = "",
    val surname: String = "",
    val birthDate: String = "",
    val birthPlace: String = "",
    val deathDate: String = "",
    val deathPlace: String = "",
    val gender: String = "",
    val marriages: List<Marriage> = emptyList(),
    val parents: List<PersonRef> = emptyList(),
    val children: List<PersonRef> = emptyList()
) {
    val fullName: String get() = "$givenName $surname".trim()
}
