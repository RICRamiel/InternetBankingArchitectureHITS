package org.ricramiel.common.util;

import java.time.LocalDateTime;
import java.util.Random;

public class ChaosUtil {
    private ChaosUtil() {}
    private static final Random random = new Random();
    public static void simulateKafkaProcessingError() {
        int currentMinute = LocalDateTime.now().getMinute();
        boolean isEvenMinute = currentMinute % 2 == 0;
        int errorThreshold = isEvenMinute ? 70 : 30;

        int randomValue = random.nextInt(100);

        if (randomValue < errorThreshold) {
            throw new RuntimeException("Simulated Kafka Processing Chaos! Service failed to process message.");
        }
    }
}
