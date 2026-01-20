package com.api.crypto.dashboard.util;

public class OpenAICostCalculator {

    private static final double INPUT_COST_PER_TOKEN = 0.15 / 1_000_000;
    private static final double OUTPUT_COST_PER_TOKEN = 0.60 / 1_000_000;

    public static double calcularCusto(int promptTokens, int completionTokens) {
        return (promptTokens * INPUT_COST_PER_TOKEN)
                + (completionTokens * OUTPUT_COST_PER_TOKEN);
    }

}
