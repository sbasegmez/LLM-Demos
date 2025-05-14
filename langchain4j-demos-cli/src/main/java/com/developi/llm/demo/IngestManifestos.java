package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
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

public class IngestManifestos extends AbstractStandaloneJnxApp {

    public static void main(String[] args) {
        new IngestManifestos().run(args);
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
                                   .databaseName("default")
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
        Database database = dominoClient.openDatabase(System.getProperty("DEMO_DB_PATH"));

        Set<Integer> manifestoDocIds = database.openCollection("Manifestos")
                                               .orElseThrow()
                                               .getAllIds(true, false);


        var embeddingModel = getEmbeddingModel();
        var embeddingStore = getEmbeddingStore("manifestos_mxbai_chunk", embeddingModel.dimension());

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(8196, 256))
                                                                .build();

        var metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                            .addString("PartyName")
                                            .addString("Country")
                                            .addInteger("Year")
                                            .build();

        List<Document> docs = DominoDocumentLoader.create(metadataDef)
                                                  .database(database)
                                                  .noteIds(manifestoDocIds)
                                                  .filePattern("*.pdf")
                                                  .documentParser(new ApachePdfBoxDocumentParser())
                                                  .loadDocuments();

        System.out.println("Ingesting set of " + docs.size() + " documents");

        IngestionResult result = ingestor.ingest(docs);

        System.out.println("All ingested" + (result.tokenUsage() == null ? "" : (" : " + result.tokenUsage() + " tokens used!")));

    }

}
