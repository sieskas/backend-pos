package com.rotules.backend.api.v1.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/api/v1/data")
public class SalesDataController {

    // Base de données en mémoire des ventes
    private static final Map<String, StoreInfo> STORE_DATABASE = initStoreDatabase();
    private static final String[] WEATHER_CONDITIONS = {"ensoleillé", "nuageux", "pluvieux", "neigeux", "venteux", "brumeux"};
    private static final String[] PRODUCT_CATEGORIES = {"Électronique", "Vêtements", "Alimentation", "Meubles", "Sports", "Beauté", "Livres"};

    @GetMapping
    public Map<String, Object> getSalesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Vérification des dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        // Limiter la plage à 1 an maximum
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
            endDate = startDate.plusDays(365);
        }

        // Générer les données de vente pour chaque jour dans la plage
        List<Map<String, Object>> dailySales = generateDailySalesData(startDate, endDate);

        // Calculer les statistiques
        double totalSales = dailySales.stream()
                .mapToDouble(sale -> (double) sale.get("montantTotal"))
                .sum();

        Map<String, Double> salesByStore = dailySales.stream()
                .collect(Collectors.groupingBy(
                        sale -> (String) sale.get("magasin"),
                        Collectors.summingDouble(sale -> (double) sale.get("montantTotal"))
                ));

        Map<String, Double> salesByCategory = dailySales.stream()
                .collect(Collectors.groupingBy(
                        sale -> (String) sale.get("categorie"),
                        Collectors.summingDouble(sale -> (double) sale.get("montantTotal"))
                ));

        // Identifier le jour avec les meilleures ventes
        Optional<Map<String, Object>> bestDay = dailySales.stream()
                .max(Comparator.comparingDouble(sale -> (double) sale.get("montantTotal")));

        // Construire la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("periode", Map.of(
                "debut", startDate,
                "fin", endDate,
                "nombreJours", ChronoUnit.DAYS.between(startDate, endDate) + 1
        ));
        response.put("ventesTotales", totalSales);
        response.put("ventesParJour", dailySales);
        response.put("ventesParMagasin", salesByStore);
        response.put("ventesParCategorie", salesByCategory);
        response.put("meilleurJour", bestDay.orElse(null));

        return response;
    }

    @GetMapping("/products/search")
    public List<Map<String, Object>> searchProducts(@RequestParam String term) {
        // Créer une liste de produits fictifs qui correspondent au terme de recherche
        return Stream.of(
                        createProduct(1, "Smartphone Galaxy X20", 799.99, "Électronique", 45),
                        createProduct(2, "Ordinateur portable Pro", 1299.99, "Électronique", 18),
                        createProduct(3, "Téléviseur 4K 65 pouces", 899.99, "Électronique", 12),
                        createProduct(4, "Jeans premium coupe droite", 89.99, "Vêtements", 120),
                        createProduct(5, "Chemise en lin premium", 69.99, "Vêtements", 85),
                        createProduct(6, "Veste en cuir premium", 249.99, "Vêtements", 30),
                        createProduct(7, "Café premium en grains 1kg", 24.99, "Alimentation", 200),
                        createProduct(8, "Thé vert premium bio", 18.99, "Alimentation", 150),
                        createProduct(9, "Canapé d'angle premium", 999.99, "Meubles", 5),
                        createProduct(10, "Bureau premium en chêne", 449.99, "Meubles", 10)
                )
                .filter(product -> ((String) product.get("nom")).toLowerCase().contains(term.toLowerCase()) ||
                        ((String) product.get("categorie")).toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Génère des données de vente pour chaque jour dans la plage donnée
    private List<Map<String, Object>> generateDailySalesData(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Pour chaque magasin, générer des ventes pour cette journée
            for (StoreInfo store : STORE_DATABASE.values()) {
                boolean isWeekend = currentDate.getDayOfWeek().getValue() > 5;

                // Les ventes varient selon le jour de la semaine, avec un bonus le weekend
                double baseSales = store.baseRevenue * (isWeekend ? 1.3 : 1.0);

                // Variation aléatoire de ±15%
                double randomFactor = 0.85 + (ThreadLocalRandom.current().nextDouble() * 0.3);

                // Facteur saisonnier: plus de ventes en décembre, moins en janvier
                double seasonalFactor = getSeasonalFactor(currentDate);

                double finalSales = baseSales * randomFactor * seasonalFactor;

                // Attribuer une catégorie de produit pour cette vente
                String category = PRODUCT_CATEGORIES[ThreadLocalRandom.current().nextInt(PRODUCT_CATEGORIES.length)];

                // Déterminer la météo pour cette journée (simplifiée)
                String weather = WEATHER_CONDITIONS[ThreadLocalRandom.current().nextInt(WEATHER_CONDITIONS.length)];

                // Ajouter cette vente à la liste
                result.add(createSale(
                        store.name,
                        finalSales,
                        store.city,
                        weather,
                        currentDate,
                        category,
                        ThreadLocalRandom.current().nextInt(10, 100)
                ));
            }

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    // Facteur saisonnier pour les ventes
    private double getSeasonalFactor(LocalDate date) {
        int month = date.getMonthValue();

        // Décembre (Noël): +40%
        if (month == 12) return 1.4;

        // Janvier (post-Noël): -20%
        if (month == 1) return 0.8;

        // Juillet-Août (été): +15%
        if (month == 7 || month == 8) return 1.15;

        // Novembre (Black Friday): +25%
        if (month == 11) return 1.25;

        // Autres mois: facteur normal
        return 1.0;
    }

    private Map<String, Object> createSale(String magasin, double ventes, String ville, String meteo,
                                           LocalDate date, String categorie, int nombreArticles) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("magasin", magasin);
        entry.put("montantTotal", Math.round(ventes * 100) / 100.0);
        entry.put("ville", ville);
        entry.put("meteo", meteo);
        entry.put("date", date);
        entry.put("categorie", categorie);
        entry.put("nombreArticles", nombreArticles);
        entry.put("prixMoyen", Math.round((ventes / nombreArticles) * 100) / 100.0);
        return entry;
    }

    private Map<String, Object> createProduct(int id, String nom, double prix, String categorie, int stock) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("nom", nom);
        product.put("prix", prix);
        product.put("categorie", categorie);
        product.put("stock", stock);
        product.put("disponible", stock > 0);
        return product;
    }

    // Initialiser la base de données des magasins
    private static Map<String, StoreInfo> initStoreDatabase() {
        Map<String, StoreInfo> stores = new HashMap<>();

        stores.put("MTL-CENTRE", new StoreInfo("Magasin Montréal Centre", "Montréal", 12500.0));
        stores.put("MTL-OUEST", new StoreInfo("Magasin Montréal Ouest", "Montréal", 9800.0));
        stores.put("QC-CENTRE", new StoreInfo("Magasin Québec Centre", "Québec", 8700.0));
        stores.put("LAVAL-NORD", new StoreInfo("Magasin Laval Nord", "Laval", 7500.0));
        stores.put("LONGUE-EST", new StoreInfo("Magasin Longueuil Est", "Longueuil", 6900.0));
        stores.put("BROSSE-CENTRE", new StoreInfo("Magasin Brossard Centre", "Brossard", 5800.0));

        return stores;
    }

    // Classe pour stocker les informations des magasins
    private static class StoreInfo {
        String name;
        String city;
        double baseRevenue;  // Chiffre d'affaires quotidien de base

        StoreInfo(String name, String city, double baseRevenue) {
            this.name = name;
            this.city = city;
            this.baseRevenue = baseRevenue;
        }
    }
}