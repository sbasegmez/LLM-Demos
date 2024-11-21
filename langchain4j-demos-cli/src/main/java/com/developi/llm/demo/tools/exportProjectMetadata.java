package com.developi.llm.demo.tools;

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

public class exportProjectMetadata extends AbstractStandaloneJnxApp {

    private String dbPath;

    public static void main(String[] args) {
        new exportProjectMetadata().run(args);
    }

    @Override
    protected void _init() {
        this.dbPath = "maggie/developi!!demos/pmt_metadata.nsf";
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        // Prepare an embedding model
        EmbeddingModel embeddingModelOllama = OllamaEmbeddingModel.builder()
                                                            .baseUrl("http://localhost:11434")
                                                            .modelName("mxbai-embed-large:latest")
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
        Database database = dominoClient.openDatabase(dbPath);


        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                                                                         .baseUrl("http://skaro.developi.info:8000")
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
                .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
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
                                        });
                });
    }

}
