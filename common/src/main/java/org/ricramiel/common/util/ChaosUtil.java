package org.ricramiel.common.util;

import java.time.LocalDateTime;
import java.util.Random;

public class ChaosUtil {
    private ChaosUtil() {}
    private static final Random random = new Random();

    public static boolean shouldSimulateError(int evenMinuteThreshold, int oddMinuteThreshold) {
        int currentMinute = LocalDateTime.now().getMinute();
        boolean isEvenMinute = currentMinute % 2 == 0;
        int errorThreshold = isEvenMinute ? evenMinuteThreshold : oddMinuteThreshold;

        int randomValue = random.nextInt(100);
        return randomValue < errorThreshold;
    }

    public static void simulateKafkaSendError(boolean enabled, int evenMinuteThreshold, int oddMinuteThreshold) {
        if (enabled && shouldSimulateError(evenMinuteThreshold, oddMinuteThreshold)) {
            throw new RuntimeException("Simulated Kafka Send Chaos! Service failed to send message.");
        }
    }

    public static void simulateKafkaProcessingError(boolean enabled, int evenMinuteThreshold, int oddMinuteThreshold) {
        if (enabled && shouldSimulateError(evenMinuteThreshold, oddMinuteThreshold)) {
            throw new RuntimeException("Simulated Kafka Processing Chaos! Service failed to process message.");
        }
    }

    public static void simulateKafkaProcessingError() {
        if (shouldSimulateError(70, 30)) {
            throw new RuntimeException("Simulated Kafka Processing Chaos! Service failed to process message.");
        }
    }
}
