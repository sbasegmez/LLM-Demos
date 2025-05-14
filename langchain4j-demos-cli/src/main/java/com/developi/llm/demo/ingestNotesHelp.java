package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
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

    public static OpenAiEmbeddingModel getEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                                   .apiKey(System.getProperty("OPENAI_API_KEY"))
                                   .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
                                   .timeout(Duration.ofSeconds(30))
                                   .maxRetries(3)
                                   .logRequests(true)
                                   .logResponses(true)
                                   .build();
    }

    public static ChromaEmbeddingStore getEmbeddingStore(String collectionName) {
        return ChromaEmbeddingStore.builder()
                                   .baseUrl(System.getProperty("CHROMA_URI"))
                                   .timeout(Duration.ofSeconds(600))
                                   .collectionName(collectionName)
                                   .build();
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        Database database = dominoClient.openDatabase("", "help/help14_client.nsf");

        // Decide what gets in...
        Set<Integer> docIds = database.openCollection("(All)")
                                      .orElseThrow()
                                      .getAllIds(true, false);

        // Prepare an embedding model
        EmbeddingModel embeddingModel = getEmbeddingModel();

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = getEmbeddingStore("noteshelp_openai_chunk");

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(8196, 256))
                                                                .build();

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
