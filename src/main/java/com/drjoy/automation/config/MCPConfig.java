package com.drjoy.automation.config;

import com.drjoy.automation.service.MCPAutomationService;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(MCPAutomationService mcpAutomationService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpAutomationService)
                .build();
    }
} 