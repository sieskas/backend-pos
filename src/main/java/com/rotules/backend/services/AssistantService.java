package com.rotules.backend.services;

import com.rotules.backend.ai.DataFetcherTool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Service
public class AssistantService {

    private final ChatLanguageModel model;
    private final ConcurrentMap<String, Assistant> assistantsBySessionId;

    @Autowired
    public AssistantService(ChatLanguageModel model) {
        this.model = model;
        this.assistantsBySessionId = new ConcurrentHashMap<>();
    }

    /**
     * Génère une réponse pour un message utilisateur
     * @param sessionId ID de session pour maintenir l'historique des conversations
     * @param userMessage Message de l'utilisateur
     * @return Réponse générée
     */
    public String generateResponse(String sessionId, String userMessage) {
        // Obtient ou crée un assistant pour cette session
        Assistant assistant = assistantsBySessionId.computeIfAbsent(
                sessionId,
                id -> createAssistant(getToolList(), id)
        );

        return assistant.chat(userMessage);
    }

    private List<ToolSpecification> getToolList() {
        List<ToolSpecification> tools = new ArrayList<>();

        // Ajouter les outils comme DataFetcherTool
        for (Method method : DataFetcherTool.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                dev.langchain4j.agent.tool.Tool annotation = method.getAnnotation(dev.langchain4j.agent.tool.Tool.class);

                // Obtenir la valeur comme tableau et prendre le premier élément
                String[] values = annotation.value();
                String description = values.length > 0 ? values[0] : method.getName();

                tools.add(ToolSpecification.builder()
                        .name(method.getName())
                        .description(description)
                        .build());
            }
        }

        return tools;
    }

    private Assistant createAssistant(List<ToolSpecification> tools, String sessionId) {
        // Créer une mémoire de chat avec une fenêtre de 10 messages
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(10)
                .build();

        // Créer une fonction qui retourne le message système
        Function<Object, String> systemMessageProvider = (obj) -> getSystemPrompt(tools);

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .systemMessageProvider(systemMessageProvider)
                .tools(new DataFetcherTool()) // Passer l'instance de l'outil directement
                .build();
    }

    private String getSystemPrompt(List<ToolSpecification> tools) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un assistant commercial qui aide à analyser les ventes. ");
        prompt.append("Tu as accès aux outils suivants:\n\n");

        for (ToolSpecification tool : tools) {
            prompt.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
        }

        prompt.append("\nQuand tu as besoin d'informations qui ne sont pas dans la conversation, ");
        prompt.append("utilise systématiquement l'outil approprié avant de répondre. ");
        prompt.append("Ne tente jamais de deviner les données de ventes ou d'inventaire. ");
        prompt.append("Utilise toujours les outils pour obtenir ces informations.\n\n");
        prompt.append("Réponds toujours en français sauf si on te demande explicitement une autre langue.");
        return prompt.toString();
    }

    private interface Assistant {
        String chat(@UserMessage String userInput);
    }
}