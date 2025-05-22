package com.developi.llm;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;
import org.openntf.misc.jnx.DominoClientRunner;
import org.openntf.misc.utils.XspUtils;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("chatterService")
public class ChatterService {

    @Inject
    @Named("openAiApiKey")
    private String openAiApiKey;

    // private static final EmbeddingModel model;
    private ChatModel chatModel;
    private HelpfulAssistant helpfulAssistant;
    private HelpdeskAssistant helpDeskAssistant;

    private EmbeddingStore<TextSegment> guideEmbeddingStore;

    public ChatterService() {
    }

    ChatModel getChatModel() {
        if (this.chatModel == null) {
            System.out.println("Creating a new chat model");

            this.chatModel = OpenAiChatModel.builder()
                                            .apiKey(openAiApiKey)
                                            .timeout(Duration.ofSeconds(30))
                                            .modelName(OpenAiChatModelName.GPT_4_1_MINI) // Mini would be enough!
                                            .temperature(0.1) // Keep it low for the demo
                                            .logRequests(true)
                                            .logResponses(true)
                                            .build();
        }

        return this.chatModel;
    }

    HelpfulAssistant getHelpfulAssistant() {
        if (helpfulAssistant == null) {
            this.helpfulAssistant = DominoClientRunner.runOnDominoClient(dc -> createHelpfulAssistant(dc));
        } else {
            // debug memory
        }

        return helpfulAssistant;
    }

    HelpdeskAssistant getHelpdeskAssistant() {
        if (helpDeskAssistant == null) {
            this.helpDeskAssistant = DominoClientRunner.runOnDominoClient(dc -> createHelpdeskAssistant(dc));
        } else {
            // debug memory
        }

        return helpDeskAssistant;
    }

    private HelpfulAssistant createHelpfulAssistant(DominoClient dc) {
        System.out.println("Creating a new helpful assistant");

        // We can't get current db yet.
        String dbPath = XspUtils.getCurrentDatabase();
        Database database = dc.openDatabase(dbPath);

        this.guideEmbeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                                                            .baseUrl(XspUtils.getXspProperty("OLLAMA_URI", "http://deepthought:11434"))
                                                            .modelName(XspUtils.getXspProperty("OLLAMA_MODEL", "mxbai-embed-large:latest"))
                                                            .build();

        Set<Integer> noteIds = database.openCollection("Guides")
                                       .orElseThrow()
                                       .getAllIds(true, false);

        MetadataDefinition metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                                           .addString("Subject")
                                                           .build();

        List<Document> documents = DominoDocumentLoader.create(metadataDef)
                                                       .fieldName("Content")
                                                       .database(database)
                                                       .noteIds(noteIds)
                                                       .loadDocuments();

        List<TextSegment> segments = DocumentSplitters.recursive(300, 0)
                                                      .splitAll(documents);

        List<Embedding> embeddings = embeddingModel.embedAll(segments)
                                                   .content();
        this.guideEmbeddingStore.addAll(embeddings, segments);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                                                          .embeddingStore(guideEmbeddingStore)
                                                                          .embeddingModel(embeddingModel)
                                                                          .maxResults(2) // on each interaction we
                                                                          // will retrieve the 2 most
                                                                          // relevant segments
                                                                          .minScore(0.8) // we want to retrieve
                                                                          // segments at least
                                                                          // somewhat similar to user
                                                                          // query
                                                                          .build();

        return AiServices.builder(HelpfulAssistant.class)
                         .chatModel(getChatModel())
                         .contentRetriever(contentRetriever)
                         .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                         .tools(new BugReportTool())
                         .build();

    }

    private HelpdeskAssistant createHelpdeskAssistant(DominoClient dc) {
        System.out.println("Creating a new helpdesk assistant");

        // All these models and retrievers and etc don't need to be created every time. TBF           
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                                                            .apiKey(openAiApiKey)
                                                            .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
                                                            .maxRetries(3)
                                                            .logRequests(true)
                                                            .logResponses(true)
                                                            .build();

        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                                                                         .baseUrl(ExtLibUtil.getXspProperty("CHROMA_URI"))
                                                                         .logRequests(true)
                                                                         .logResponses(true)
                                                                         .collectionName("noteshelp_openai_chunk")
                                                                         .build();

        QueryTransformer queryTransformer = CompressingQueryTransformer.builder()
                                                                     .chatModel(getChatModel())
                                                                     .build();

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                                                          .embeddingStore(embeddingStore)
                                                                          .embeddingModel(embeddingModel)
                                                                          .maxResults(4)
                                                                          .minScore(0.75)
                                                                          .build();

        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                                                                .contentRetriever(contentRetriever)
                                                                .queryTransformer(queryTransformer)
                                                                .contentInjector(DefaultContentInjector.builder()
                                                                                                       .metadataKeysToInclude(List.of("Subject"))
                                                                                                       .promptTemplate(PromptTemplate.from("{{userMessage}}\n{{contents}}"))
                                                                                                       .build())
                                                                .build();

        return AiServices.builder(HelpdeskAssistant.class)
                         .chatModel(getChatModel())
                         .retrievalAugmentor(augmentor)
                         .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                         .tools(new HelpdeskTicketTool())
                         .build();

    }

}
