package br.unisul.biblioteca.config;

import br.unisul.biblioteca.util.ArmazenamentoCapas;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Publica as capas enviadas (pasta uploads/capas) no endereço /capas/**. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ArmazenamentoCapas capas;

    public WebConfig(ArmazenamentoCapas capas) {
        this.capas = capas;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // A pasta é criada na inicialização de ArmazenamentoCapas, então o endereço já termina com "/"
        registry.addResourceHandler(ArmazenamentoCapas.PREFIXO_URL + "**")
                .addResourceLocations(capas.pasta().toUri().toString());
    }
}
