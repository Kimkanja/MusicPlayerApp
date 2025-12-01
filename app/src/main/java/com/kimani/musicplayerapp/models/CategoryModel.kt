package com.kimani.musicplayerapp.models

data class CategoryModel(
    val name : String,
    val coverUrl : String,
) {
    constructor() : this("","")
}
