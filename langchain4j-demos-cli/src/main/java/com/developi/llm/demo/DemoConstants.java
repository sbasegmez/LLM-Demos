package com.developi.llm.demo;

public class DemoConstants {

     // Demo database location in Server!!dbFilePath format
    public final static String DEMO_DB_PATH = "maggie/developi!!openntf/DemoLangchain4j.nsf";

    // PMT Metadata database location in Server!!dbFilePath format
    public final static String PMT_METADATA_PATH = "maggie/developi!!demos/pmt_metadata.nsf";

    // OLLAMA instance URI. e.g. http://<hostname>:11434
    public static final String OLLAMA_URI = "http://localhost:11434";

    // Chroma vector database instance URI
    public static final String CHROMA_URI = "http://skaro.developi.info:8000";

    // Milvus Host Name and port
    public static final String MILVUS_HOST = "skaro.developi.info";
    public static final int MILVUS_PORT = 19530;

    // Chat Model to be used in OLLAMA
    public static final String OLLAMA_MODEL_NAME = "deepseek-r1:1.5b";

    // Embedded model to be used in OLLAMA
    public static final String OLLAMA_EMB_MODELNAME = "mxbai-embed-large:latest";

}
