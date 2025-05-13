package com.developi.llm.demo;

import static dev.langchain4j.model.openai.OpenAiModerationModelName.TEXT_MODERATION_LATEST;

import com.developi.jnx.utils.Utils;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public class ModerationDemo {

    interface HelpfulAiService {

        @SystemMessage("""
            You are a helpful responder. Reply all question in detail.
            """)
        @Moderate
        String ask(@UserMessage String question);

    }


    public static void main(String[] args) {
        // Import env file
        Utils.initDotenv();

        OpenAiModerationModel moderationModel = OpenAiModerationModel.builder()
                                                                     .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                     .modelName(TEXT_MODERATION_LATEST)
                                                                     .build();

        HelpfulAiService service = AiServices.builder(HelpfulAiService.class)
                                             .chatModel(OllamaChatModel.builder()
                                                                       .baseUrl(DemoConstants.OLLAMA_URI)
                                                                       .modelName(DemoConstants.OLLAMA_MODEL_NAME)
                                                                       .temperature(0.1)
                                                                       .build())
                                             .moderationModel(moderationModel)
                                             .build();

        // Supposed to throw Moderation Exception
        System.out.println(service.ask("I'll kill you"));

    }
}
