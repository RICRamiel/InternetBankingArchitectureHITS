package org.ricramiel.common.feign.headers_propagation;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class RequiredHeadersPropagationInterceptor implements RequestInterceptor {
    private final HeadersPropagationConfig config;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();

        if (attributes == null)
            return;

        HttpServletRequest request = attributes.getRequest();

        config.getHeadersToPropagate().forEach(header ->
            propagateHeader(header, request, template)
        );
    }

    private void propagateHeader(String headerName, HttpServletRequest request, RequestTemplate template) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            template.header(headerName, headerValue);
        }
    }
}