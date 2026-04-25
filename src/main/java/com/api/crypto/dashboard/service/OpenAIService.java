package com.api.crypto.dashboard.service;

import com.api.crypto.dashboard.model.Coin;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    private final ChatClient chatClient;

    /**
     * O ChatClient é automaticamente configurado pelo Spring AI
     * com base no application.properties e na dependência OpenAI.
     */
    public OpenAIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
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
