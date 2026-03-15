package com.monpote.core.network.api

import com.monpote.core.network.dto.ChatRequest
import com.monpote.core.network.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AzureOpenAiService {
    @POST("openai/deployments/{deployment}/chat/completions")
    suspend fun chatCompletion(
        @Path("deployment") deployment: String,
        @Query("api-version") apiVersion: String = "2024-05-01-preview",
        @Body request: ChatRequest,
    ): ChatResponse
}
