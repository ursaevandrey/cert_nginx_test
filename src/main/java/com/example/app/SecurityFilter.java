package com.example.app;

import jakarta.annotation.Nullable;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Builder;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

@Component
public class SecurityFilter implements Filter {

    @Value("${server.ssl.trust-store}")
    private String trustStorePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${server.ssl.trust-store-type}")
    private String trustStoreType;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getScheme().equalsIgnoreCase("https")) {
            X509Certificate clientCert = getCertificate(request);

            try {
                TrustStore trustStore = new TrustStore(TrustStore.Credentials.builder()
                        .path(trustStorePath)
                        .password(trustStorePassword)
                        .type(trustStoreType)
                        .build());
                if (!trustStore.validateCertificate(clientCert)) {
                    ResponseUtils.setBadRequestError(
                            response,
                            "Certificate not trusted"
                    );
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Unknown error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private X509Certificate getCertificate(HttpServletRequest request) {
        String attrName = "jakarta.servlet.request.X509Certificate";
        if (!(request.getAttribute(attrName) instanceof X509Certificate[] certs)) {
            return null;
        }

        if (certs.length < 1) {
            return null;
        }

        return certs[0];
    }

    static private class TrustStore {

        private final TrustManagerFactory trustManagerFactory;

        public TrustStore(Credentials cred) throws Exception {
            KeyStore trustStore = KeyStore.getInstance(cred.getType());

            File tsFile = cred.getFile();
            if (!tsFile.exists()) {
                throw new IllegalArgumentException("Trust store not found: " + cred.getPath());
            }

            try (InputStream trustStoreStream = new FileInputStream(tsFile)) {
                trustStore.load(trustStoreStream, cred.passwordChars());
            }

            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
        }

        public boolean validateCertificate(@Nullable X509Certificate certificate) throws Exception {
            if (certificate == null) {
                return false;
            }

            var trustManagers = trustManagerFactory.getTrustManagers();
            for (var manager : trustManagers) {
                if (manager instanceof X509TrustManager) {
                    ((X509TrustManager) manager).checkClientTrusted(new X509Certificate[]{certificate}, "RSA");
                    return true;
                }
            }

            return false;
        }

        @Getter
        @Builder
        private static class Credentials {
            private final String path;
            private final String password;
            private final String type;

            public File getFile() {
                return new File(path);
            }

            public char[] passwordChars() {
                return password.toCharArray();
            }
        }
    }

}
