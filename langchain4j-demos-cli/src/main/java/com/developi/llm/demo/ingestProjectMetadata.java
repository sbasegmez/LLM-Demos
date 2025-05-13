package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import java.util.List;
import java.util.Set;
import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ingestProjectMetadata extends AbstractStandaloneJnxApp {

    private static final Logger log = LoggerFactory.getLogger(ingestProjectMetadata.class);

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
                                                                  .baseUrl(DemoConstants.OLLAMA_URI)
                                                                  .modelName(DemoConstants.OLLAMA_EMB_MODELNAME)
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
        submit(dominoClient, embeddingModelOllama, "projects_mxbai_chunk");
    }

    public void submit(DominoClient dominoClient, EmbeddingModel embeddingModel, String collectionName) {
        Database database = dominoClient.openDatabase(DemoConstants.PMT_METADATA_PATH);

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                                                                         .host(DemoConstants.MILVUS_HOST)
                                                                         .port(DemoConstants.MILVUS_PORT)
                                                                         .databaseName("pmt")
                                                                         .collectionName(collectionName)
                                                                         .dimension(embeddingModel.dimension())
                                                                         .build();

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(4000, 128))
                                                                .build();

        Set<Integer> projectNoteIds = database.openCollection("projects")
                                              .orElseThrow()
                                              .getAllIds(true, false);

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
