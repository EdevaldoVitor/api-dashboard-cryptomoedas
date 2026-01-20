package com.api.crypto.dashboard.service;

import com.api.crypto.dashboard.model.AiRequestLog;
import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.repository.AiRequestLogRepository;
import com.api.crypto.dashboard.util.OpenAICostCalculator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OpenAIService {

    private final ChatClient chatClient;
    private final AiRequestLogRepository logRepository;

    /**
     * O ChatClient é automaticamente configurado pelo Spring AI
     * com base no application.properties e na dependência OpenAI.
     */
    public OpenAIService(ChatClient.Builder builder,
                         AiRequestLogRepository logRepository) {
        this.chatClient = builder.build();
        this.logRepository = logRepository;
    }

    public String gerarResumo(String texto) {
        var response = chatClient
                .prompt()
                .system("""
                    Você é um especialista em criptomoedas e mercado financeiro.
                    Explique de forma clara e objetiva.
                """)
                .user(texto)
                .call();

        String conteudo = response.content();

        // Metadata
        ChatResponseMetadata meta = response.chatResponse().getMetadata();
        int promptTokens = meta.getUsage().getPromptTokens();
        int completionTokens = meta.getUsage().getCompletionTokens();
        int totalTokens = meta.getUsage().getTotalTokens();

        double custo = OpenAICostCalculator.calcularCusto(
                promptTokens, completionTokens
        );

        // Persistência
        AiRequestLog log = new AiRequestLog();
        log.setPrompt(texto);
        log.setResposta(conteudo);
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(totalTokens);
        log.setModelo("gpt-4o-mini");
        log.setCustoEstimado(custo);
        log.setDataHora(LocalDateTime.now());

        logRepository.save(log);

        return conteudo;
    }

    public String gerarResumoFavoritos(List<Coin> coins) {

        String conteudo = coins.stream()
                .map(c -> """
                Moeda: %s
                Símbolo: %s
                Preço atual: %s
                """.formatted(
                        c.getName(),
                        c.getSymbol(),
                        c.getCurrentPrice()
                ))
                .reduce("", String::concat);

        return chatClient
                .prompt()
                .system("""
                Você é um analista especialista em criptomoedas.
                Analise o conjunto de moedas favoritas do usuário.
                Gere um resumo geral do cenário, destacando tendências,
                diversificação e possíveis riscos.
                Não faça recomendações de investimento.
            """)
                .user("""
                Com base nas moedas abaixo, gere um resumo consolidado:

                %s
            """.formatted(conteudo))
                .call()
                .content();
    }
}
