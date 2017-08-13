package br.com.diop.livrosfirebase.domain


import com.google.firebase.database.Exclude

import java.io.Serializable

class Book : Serializable {
    var title: String? = null
    var author: String? = null
    var cover: String? = null
    @get:Exclude
    @set:Exclude
    var id: String? = null


    constructor() {}

    constructor(title: String, author: String, cover: String, id: String) {
        this.title = title
        this.author = author
        this.cover = cover
        this.id = id
    }
}
