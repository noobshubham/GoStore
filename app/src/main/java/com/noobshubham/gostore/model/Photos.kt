package com.noobshubham.gostore.model

data class Photos(
    var height: Int = 0,
    var width: Int = 0,
    var photo_reference: String? = null,
    var html_attributions: Array<String>? = null,
)