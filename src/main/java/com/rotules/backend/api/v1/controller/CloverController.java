package com.rotules.backend.api.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clover")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CloverController {

    @Autowired
    private Environment environment;

    @GetMapping("/connect")
    public void connect(HttpServletResponse response) throws IOException {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.clover.client-id");
        String redirectUri = environment.getProperty("spring.security.oauth2.client.registration.clover.redirect-uri");

        String authorizeUrl = "https://apisandbox.dev.clover.com/oauth/v2/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=" + "code"
                + "&scope=" + "INVENTORY_READ PAYMENTS_READ PAYMENTS_WRITE";

        response.sendRedirect(authorizeUrl);
    }

    /**
     * 2) Callback appelé par Clover (redirection).
     * On échange le "code" contre un "access_token".
     * On envoie ensuite du HTML qui va faire un postMessage(...) vers le parent.
     */
    @GetMapping("/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam(value = "merchant_id", required = false) String merchantId,
            HttpServletResponse response, HttpSession session
    ) throws IOException {

        String clientId = environment.getProperty("spring.security.oauth2.client.registration.clover.client-id");
        String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.clover.client-secret");
        String redirectUri = environment.getProperty("spring.security.oauth2.client.registration.clover.redirect-uri");

        // Préparation de la requête pour échanger le "code" contre un "access_token"
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("code", code);
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("redirect_uri", redirectUri);

        try {
            // On envoie la requête POST vers https://apisandbox.dev.clover.com/oauth/v2/token
            ResponseEntity<Map> tokenResponse =
                    restTemplate.postForEntity("https://apisandbox.dev.clover.com/oauth/v2/token",
                            requestBody,
                            Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get token: " + tokenResponse.getStatusCode());
            }

            Map<String, Object> tokenData = tokenResponse.getBody();
            System.out.println("Token data reçu: " + tokenData);

            session.setAttribute("cloverToken", tokenData);

            String tokenJson = new ObjectMapper().writeValueAsString(tokenData);

            String safeJson = tokenJson
                    .replace("\\", "\\\\")   // échapper backslash
                    .replace("\"", "\\\"");  // échapper guillemets

            String htmlResponse = ""
                    + "<html>\n"
                    + "  <head>\n"
                    + "    <title>Clover Callback</title>\n"
                    + "    <meta charset='utf-8'/>\n"
                    + "  </head>\n"
                    + "  <body style='text-align:center; margin-top: 50px; font-family: sans-serif;'>\n"
                    + "    <h1>Connexion à Clover réussie</h1>\n"
                    + "    <p>Votre compte Clover est maintenant connecté.</p>\n"
                    + "    <p>Vous pouvez fermer cette fenêtre manuellement.</p>\n"
                    + "\n"
                    + "    <script>\n"
                    + "      (function() {\n"
                    + "         try {\n"
                    + "           const tokenData = JSON.parse(\"" + safeJson + "\");\n"
                    + "\n"
                    + "           // Vérifier si on a un opener (fenêtre parente)\n"
                    + "           if (window.opener && !window.opener.closed) {\n"
                    + "             // Envoyer le token au parent\n"
                    + "             window.opener.postMessage({ status: 'success', data: tokenData }, 'http://localhost:5173');\n"
                    + "           } else {\n"
                    + "             console.warn('Pas de window.opener, impossible d\\'envoyer le message.');\n"
                    + "           }\n"
                    + "         } catch(e) {\n"
                    + "           console.error('Erreur dans la popup:', e);\n"
                    + "         }\n"
                    + "      })();\n"
                    + "    </script>\n"
                    + "  </body>\n"
                    + "</html>";


              response.setContentType("text/html");
            response.getWriter().write(htmlResponse);
            response.getWriter().flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erreur lors de l'échange du code OAuth2");
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getCloverOrders(@RequestParam String merchantId, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();
        Object tokenData = session.getAttribute("cloverToken");
        if (tokenData == null || !(tokenData instanceof Map)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No token found in session"));
        }

        String accessToken = (String) ((Map<?, ?>) tokenData).get("access_token");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token in session"));
        }

        LocalDateTime startOfJanuary = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endOfJanuary = LocalDateTime.of(2025, 1, 31, 23, 59);

        long startTime = startOfJanuary.toInstant(ZoneOffset.UTC).toEpochMilli();
        long endTime = endOfJanuary.toInstant(ZoneOffset.UTC).toEpochMilli();

        // Construire l'URL de l'API Clover avec les paramètres de temps
        String apiUrl = String.format("%s/v3/merchants/%s/orders",
                "https://sandbox.dev.clover.com/", merchantId);
//https://sandbox.dev.clover.com

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Appeler l'API Clover
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch Clover orders"));
        }
    }
}
