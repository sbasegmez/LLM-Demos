package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;

public class ingestNotesHelp extends AbstractStandaloneJnxApp {

    public static void main(String[] args) {
        new ingestNotesHelp().run(args);
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        // Prepare an embedding model
        EmbeddingModel embeddingModelOllama = OllamaEmbeddingModel.builder()
                                                                  .baseUrl(DemoConstants.OLLAMA_URI)
                                                                  .modelName(DemoConstants.OLLAMA_SNOWFLAKE_EMB_MODELNAME)
                                                                  .maxRetries(3)
                                                                  .logRequests(true)
                                                                  .logResponses(true)
                                                                  .build();

        submit(dominoClient, embeddingModelOllama, "client_help_snowflake_nochunk");
    }

    public void submit(DominoClient dominoClient, EmbeddingModel embeddingModel, String collectionName) {
        Database database = dominoClient.openDatabase("", "help/help14_client.nsf");

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                                                                         .baseUrl(DemoConstants.CHROMA_URI)
                                                                         .timeout(Duration.ofSeconds(600))
                                                                         .collectionName(collectionName)
                                                                         .build();

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(8196, 256))
                                                                .build();

        Set<Integer> docIds = database.openCollection("(All)")
                                      .orElseThrow()
                                      .getAllIds(true, false);

        var metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                            .addString("Subject")
                                            .build();

        List<Document> docs = DominoDocumentLoader.create(metadataDef)
                                                  .database(database)
                                                  .noteIds(docIds)
                                                  .fieldName("Subject")
                                                  .fieldName("Body")
                                                  .loadDocuments();

        System.out.println("Ingesting set of " + docs.size() + " documents");

        AtomicInteger counter = new AtomicInteger(0);

        docs.parallelStream().forEach(doc -> {
            ingestor.ingest(doc);
            if (counter.incrementAndGet() % 100 == 0) {
                System.out.println(counter.get() + " docs ingested...");
            }
        });

        System.out.println("All ingested");
    }

}
