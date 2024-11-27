package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoCollection;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.openntf.langchain4j.data.DominoDocumentLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ingestProjectMetadata extends AbstractStandaloneJnxApp {

    /**
     * PMT Metadata database location in Server!!dbFilePath format
     */
    private final static String PMT_METADATA_PATH = "maggie/developi!!demos/pmt_metadata.nsf";

    /**
     * OLLAMA instance URI. e.g. http://<hostname>:11434
     */
    private static final String OLLAMA_URI = "http://localhost:11434";

    /**
     * Embedded model to be used in OLLAMA
     */
    private static final String OLLAMA_EMB_MODELNAME = "mxbai-embed-large:latest";

    /**
     * Chroma vector database instance URI
     */
    private static final String CHROMA_URI = "http://skaro.developi.info:8000";

    public static void main(String[] args) {
        new ingestProjectMetadata().run(args);
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        // Prepare an embedding model
        EmbeddingModel embeddingModelOllama = OllamaEmbeddingModel.builder()
                                                                  .baseUrl(OLLAMA_URI)
                                                                  .modelName(OLLAMA_EMB_MODELNAME)
                                                                  .maxRetries(3)
                                                                  .logRequests(true)
                                                                  .logResponses(true)
                                                                  .build();

        EmbeddingModel embeddingModelOpenAi = OpenAiEmbeddingModel.builder()
                                                                  .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
                                                                  .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                  .build();

//        Disabled tp prevent accidental run...
//        submit(dominoClient, embeddingModelOllama, "projects_mxbai_nochunk");
//        submit(dominoClient, embeddingModelOpenAi, "projects_openai_nochunk");

    }

    public void submit(DominoClient dominoClient, EmbeddingModel embeddingModel, String collectionName) {
        AtomicInteger counter = new AtomicInteger(0);
        Database database = dominoClient.openDatabase(PMT_METADATA_PATH);

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                                                                         .baseUrl(CHROMA_URI)
                                                                         .collectionName(collectionName)
                                                                         .build();

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(4000, 128))
                                                                .build();

        database.openCollection("projects")
                .map(DominoCollection::query)
                .orElseThrow()
                .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) ->
                        DominoDocumentLoader.newLoader()
                                            .fieldName("detailsText")
                                            .dominoDocument(doc)
                                            .addMetaFields(List.of("name", "overview", "chefs", "sourceUrl", "category"))
                                            .loadDocument()
                                            .ifPresent(document -> {
                                                ingestor.ingest(document);

                                                if (counter.incrementAndGet() % 100 == 0) {
                                                    System.out.println("Ingested " + counter.intValue() + " documents");
                                                }
                                            }));
    }

}
