package com.developi.llm.demo;

import com.developi.jnx.utils.Utils;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.util.List;

public class AskAboutNotes {

    interface NotesHelpAiService {

        @SystemMessage("""
            Reply question solely based on the given information.
            """)
        String ask(@UserMessage String question);

    }

    public static void main(String[] args) {
        // Import env file
        Utils.initDotenv();

        // Prepare an embedding model
        var embeddingModel = IngestNotesHelp.getEmbeddingModel();
        var embeddingStore = IngestNotesHelp.getEmbeddingStore("noteshelp_openai_chunk");

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                                                          .embeddingStore(embeddingStore)
                                                                          .embeddingModel(embeddingModel)
                                                                          .maxResults(5)
                                                                          .minScore(0.75)
                                                                          .build();

        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                                                                .contentRetriever(contentRetriever)
                                                                .contentInjector(DefaultContentInjector.builder()
                                                                                                       .metadataKeysToInclude(List.of("Subject"))
                                                                                                       .promptTemplate(PromptTemplate.from("{{userMessage}}\n{{contents}}"))
                                                                                                       .build())
                                                                .build();

        NotesHelpAiService service = AiServices.builder(NotesHelpAiService.class)
                                               .chatModel(OpenAiChatModel.builder()
                                                                         .modelName(OpenAiChatModelName.GPT_4_1_MINI)
                                                                         .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                         .temperature(0.1)
                                                                         .build())
                                               .retrievalAugmentor(augmentor)
                                               .build();

        System.out.println(service.ask("How can I set out of office message?"));


    }
}
