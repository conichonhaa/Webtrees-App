package com.genealogie.webtrees

data class Person(
    val xref: String,
    val tree: String,
    val givenName: String = "",
    val surname: String = "",
    val birthDate: String = "",
    val birthPlace: String = "",
    val deathDate: String = "",
    val deathPlace: String = "",
    val gender: String = ""
) {
    val fullName: String get() = "$givenName $surname".trim()
}
