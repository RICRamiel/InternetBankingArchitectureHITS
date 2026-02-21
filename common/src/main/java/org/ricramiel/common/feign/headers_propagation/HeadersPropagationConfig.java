package org.ricramiel.common.feign.headers_propagation;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class HeadersPropagationConfig {
    private final List<String> headersToPropagate;
}