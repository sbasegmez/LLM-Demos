# Large Language Models - Demo Project

This repository supports my sessions on large language models and their integration into Domino apps. You can find the slides for the sessions below:

- [Experimenting with Integrating Large Language Models into Domino Apps" at Engage 2024.](https://speakerdeck.com/sbasegmez/engage-2024-experimenting-with-integrating-large-language-models-into-domino-apps)
- [Integrating Large Language Models into Domino Applications: A Primer at OpenNTF Webinars](https://speakerdeck.com/sbasegmez/openntf-webinars-2024-integrating-large-language-models-into-domino-applications-a-primer)
    - [Youtube](https://www.youtube.com/watch?v=TtdR1sX1Kbg)




### Prerequisites

-   **Project Metadata Database**: Some of the code requires a project metadata database, which is publicly available as the [OpenNTF project](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Projects%20Dataset).
-   **Langchain4j-Domino Integration**: The project utilizes langchain4j-domino, which will be made publicly available in the near future. Experimental version is [here](https://github.com/sbasegmez/langchain4j-domino).
-   **LLM Model Container**: The ingestion code leverages the [Ollama](https://ollama.com/) LLM management tool, preferably installed locally.
-   **Vector Database**: The ingestion target is a [Chroma](https://www.trychroma.com/) vector database, preferably installed locally.
-   **OpenAI API**: The AiService demos require access to the OpenAI API, along with a valid API key.
    -   Copy the .env.example file to your user directory, rename it to .env, and add your OpenAI API key.
-   **Other XPages plugins:** 
    -   [OpenNTF Domino API](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Domino%20API)
    -   [Domino JNX - XPages](https://github.com/HCL-TECH-SOFTWARE/domino-jnx)
    -   [Jakarta EE Project](https://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20Jakarta%20EE%20Support)



### Demos

#### Module: langchain4j-demos-cli

-   **Prompts and Fewshots Demos**: Demonstrations of the basic functionality of the AiService.
-   **ingestProjectMetadata**: Demonstrates how project metadata can be ingested into a vector database.

#### Module: nsf/odp.langchain4j-xsp-demo

This XPages application highlights the capabilities of langchain4j through three distinct demonstrations:

	1.	**Few Shots Example**: A simple use case of the AiService.
	1.	**Semantic Search**: Demonstrates two variations of semantic search using OLLAMA and OpenAI.
	1.	**Helpful Assistant**: An advanced example featuring a chat service enhanced with Retrieval-Augmented Generation (RAG) and tooling addons.



To configure the demos:

1.   **Create an NSF Database**: Generate an NSF database from the provided ODP file.

2.   **Deploy to Domino Server**: Place the NSF database on a Domino server and sign it.

3.   **Prepare Metadata Dataset**: Download the [OpenNTF Projects Dataset](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Projects%20Dataset), deploy it to the same server, sign it, and create a full-text index.

4.   **Set Up Server Properties**: Open the xsp.properties file in your server’s data directory and define the following variables:

```properties
PMT_METADATA_DB=demos/pmt_metadata.nsf
OLLAMA_URI=http://<ollama-server>:11434
OLLAMA_MODEL=mxbai-embed-large:latest
CHROMA_URI=http://<chroma-server>:8000
langchain4j.OPENAI_API_KEY=<Openai-api-key>
```



### Compatibility

#### Java

This project has been tested on Java 17 or higher.

#### Apple Silicon

If you’re running Apple Silicon computers, use a x86_64 JVM (e.g. IBM Semeru OpenSDK 17) to run the demo databases.
