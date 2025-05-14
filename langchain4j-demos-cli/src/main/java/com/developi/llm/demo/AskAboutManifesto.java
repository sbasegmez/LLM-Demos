package com.developi.llm.demo;

import com.developi.jnx.utils.Utils;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public class AskAboutManifesto {

    interface ManifestoAiService {

        @SystemMessage("""
            Reply question. If you don't know, politely decline replying.
            """)
        String ask(@UserMessage String question);

    }

    public static void main(String[] args) {
        // Import env file
        Utils.initDotenv();

        // Prepare an embedding model
        var embeddingModel= ingestManifestos.getEmbeddingModel();
        var embeddingStore = ingestManifestos.getEmbeddingStore("manifestos_mxbai_chunk", embeddingModel.dimension());

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                                                          .embeddingStore(embeddingStore)
                                                                          .embeddingModel(embeddingModel)
                                                                          .maxResults(10)
                                                                          .minScore(0.75)
                                                                          .build();

        ManifestoAiService service = AiServices.builder(ManifestoAiService.class)
                                               .chatModel(OpenAiChatModel.builder()
                                                                         .modelName(OpenAiChatModelName.GPT_4_1_MINI)
                                                                         .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                         .temperature(0.1)
                                                                         .build())
                                               .contentRetriever(contentRetriever)
                                               .build();

        System.out.println(service.ask("Summarise education policies in given party manifestos"));

    }
}
