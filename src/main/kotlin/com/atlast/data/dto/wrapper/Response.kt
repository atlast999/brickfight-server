package com.atlast.data.dto.wrapper

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppResponse<T>(
    @SerialName("result_code") val code: Int = 200,
    @SerialName("message") val message: String = "success",
    @SerialName("data") val data: T? = null,
)

@Serializable
data class PagingModel<T>(
    @SerialName("page") val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_page") val totalPage: Int,
    @SerialName("items") val items: List<T>,
)

