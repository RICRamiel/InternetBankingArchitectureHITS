package org.ricramiel.transactionservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@Order(1)
public class ChaosFilter implements Filter {
    private final Random random = new Random();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        int currentMinute = LocalDateTime.now().getMinute();
        boolean isEvenMinute = currentMinute % 2 == 0;
        int errorThreshold = isEvenMinute ? 70 : 30;

        int randomValue = random.nextInt(100);
        if (randomValue < errorThreshold) {
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Simulated Chaos Error (Threshold: " + errorThreshold + "%)");
            return;
        }
        chain.doFilter(request, response);
    }
}