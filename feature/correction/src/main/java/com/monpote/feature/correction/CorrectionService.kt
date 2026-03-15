package com.monpote.feature.correction

import com.monpote.core.network.BuildConfig
import com.monpote.core.network.api.AzureOpenAiService
import com.monpote.core.network.dto.ChatMessage
import com.monpote.core.network.dto.ChatRequest
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CorrectionService @Inject constructor(
    private val openAiService: AzureOpenAiService,
    private val moshi: Moshi,
) {
    companion object {
        private val SYSTEM_PROMPT = """
Tu es un correcteur de français. Analyse le message suivant et retourne un JSON avec les erreurs trouvées.

Catégories possibles :
- "orthographe" : fautes d'orthographe, accents, accords du participe passé
- "grammaire" : genre, nombre, conjugaison, syntaxe
- "style" : choix de mots, registre, suggestions plus naturelles

Pour chaque erreur, donne :
- "type" : la catégorie
- "original" : le texte incorrect tel qu'écrit
- "correction" : la forme correcte
- "explanation" : une explication courte en français

Si le message ne contient aucune erreur, retourne {"errors": []}.

IMPORTANT : Réponds UNIQUEMENT avec le JSON valide, sans texte avant ou après.
        """.trimIndent()
    }

    suspend fun check(text: String): CorrectionResult {
        val response = openAiService.chatCompletion(
            deployment = BuildConfig.AZURE_OPENAI_DEPLOYMENT,
            request = ChatRequest(
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = text),
                ),
                maxCompletionTokens = 4096,
            ),
        )

        val jsonString = response.choices.firstOrNull()?.message?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("No content in response")

        // Strip markdown code fences if LLM wraps JSON in ```json ... ```
        val cleanJson = jsonString
            .replace(Regex("^```json\\s*"), "")
            .replace(Regex("^```\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            .trim()

        val adapter = moshi.adapter(CorrectionResult::class.java)
        return adapter.fromJson(cleanJson)
            ?: throw IllegalStateException("Failed to parse correction JSON")
    }
}
