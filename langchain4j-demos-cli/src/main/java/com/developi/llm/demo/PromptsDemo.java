package com.developi.llm.demo;

import com.developi.jnx.utils.Utils;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import java.util.Arrays;
import java.util.List;

public class PromptsDemo {

    public static void main(String[] args) {
        // Import env file
        Utils.initDotenv();

        DemoAiService demoAiService = AiServices.builder(DemoAiService.class)
                                                .chatModel(OpenAiChatModel.builder()
                                                                          .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                                                                          .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                          .temperature(0.1)
                                                                          .build())
                                                .build();

        List<String> sentences = Arrays.asList(
            "I love this product!",
            "This product is terrible!",
            "I am not sure about this product.",
            "Best product in the hall of shame!"
        );

        sentences.forEach(sentence -> {
            System.out.println("\nSentence: " + sentence);
            System.out.println("> " + demoAiService.ask(sentence));
        });

    }

    interface DemoAiService {

        @SystemMessage("""
            The user will give a sentence from a user comment. Provide a sentiment for the sentence.
            """)
        String ask(String sentence);

    }
}
