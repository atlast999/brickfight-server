package com.atlast.utils

import io.ktor.http.Parameters

fun Parameters.pageAndSize() = run {
    val page = get("page")?.toInt() ?: 1
    val size = get("page_size")?.toInt() ?: 10
    Pair(page, size)
}