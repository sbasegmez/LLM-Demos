package com.developi.llm.demo.pmtdemos;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoCollection;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
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
    protected void _run(DominoClient dominoClient) {
        AtomicInteger counter = new AtomicInteger(0);
        Database database = dominoClient.openDatabase(dbPath);

        // Prepare an embedding model
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                                                            .baseUrl("http://localhost:11434")
                                                            .modelName("mxbai-embed-large:latest")
                                                            .maxRetries(3)
                                                            .logRequests(true)
                                                            .logResponses(true)
                                                            .build();

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                                                                         .collectionName("projects_mxbai")
                                                                         .host("skaro.developi.info")
                                                                         .databaseName("pmt")
                                                                         .dimension(embeddingModel.dimension())
                                                                         .build();

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(512, 64))
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

                                            if(counter.incrementAndGet() % 100 == 0) {
                                                System.out.println("Ingested " + counter.intValue() + " documents");
                                            }
                                        });
                });

    }
}
