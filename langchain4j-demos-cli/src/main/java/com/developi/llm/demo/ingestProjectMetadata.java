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
import dev.langchain4j.store.embedding.IngestionResult;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import java.util.List;
import java.util.Set;
import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;

public class ingestProjectMetadata extends AbstractStandaloneJnxApp {

    public static void main(String[] args) {
        new ingestProjectMetadata().run(args);
    }

    public static EmbeddingModel getEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                                   .baseUrl(System.getProperty("OLLAMA_URI"))
                                   .modelName(System.getProperty("OLLAMA_MXBAI_EMB_MODELNAME"))
                                   .maxRetries(3)
                                   .logRequests(true)
                                   .logResponses(true)
                                   .build();
    }

    public static EmbeddingStore<TextSegment> getEmbeddingStore(String collectionName, int dimension) {
        return MilvusEmbeddingStore.builder()
                                   .host(System.getProperty("MILVUS_HOST"))
                                   .port(Integer.parseInt(System.getProperty("MILVUS_PORT")))
                                   .databaseName("pmt")
                                   .collectionName(collectionName)
                                   .dimension(dimension)
                                   .build();
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        // Pull relevant doc ids
        Database database = dominoClient.openDatabase(System.getProperty("PMT_METADATA_PATH"));

        Set<Integer> projectNoteIds = database.openCollection("projects")
                                              .orElseThrow()
                                              .getAllIds(true, false);


        var embeddingModel = getEmbeddingModel();
        var embeddingStore = getEmbeddingStore("projects_mxbai_chunk", embeddingModel.dimension());

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(4000, 128))
                                                                .build();

        var metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                            .addString("name")
                                            .addString("overview")
                                            .addString("chefs")
                                            .addString("sourceUrl")
                                            .addString("category")
                                            .build();

        List<Document> docs = DominoDocumentLoader.create(metadataDef)
                                                  .database(database)
                                                  .fieldName("name")
                                                  .fieldName("details")
                                                  .noteIds(projectNoteIds)
                                                  .loadDocuments();

        System.out.println("Ingesting set of " + docs.size() + " documents");

        IngestionResult result = ingestor.ingest(docs.stream()
                                                     .filter(doc -> doc.text().length()>256)
                                                     .toList());

        System.out.println("All ingested" + (result.tokenUsage() == null ? "" : (" : " + result.tokenUsage() + " tokens used!")));

    }

}
