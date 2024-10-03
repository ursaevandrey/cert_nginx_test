package com.example.app;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.valves.SSLValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return factory -> {
            factory.addAdditionalTomcatConnectors(httpConnector());
            factory.addContextValves(createSSLValve());
        };
    }

    private SSLValve createSSLValve() {
        SSLValve sslValve = new SSLValve();
        sslValve.setSslClientCertHeader("X-Client-Cert");
        return sslValve;
    }

    private Connector httpConnector() {
        int httpPort = 9001;
        if (System.getenv("APP_SERVICE_PORT") != null) {
            httpPort = Integer.parseInt(System.getenv("APP_SERVICE_PORT"));
        }


        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);

        return connector;
    }

}
