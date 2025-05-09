package com.b2s.apple.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class UtilConfig {

    public static final String MERCHANDISE = "MERCHANDISE";
    public static final String GIFT_CARD = "giftcard";
    public static final String SERVICE_PLAN = "SERVICE_PLAN";

    @Bean("defaultLocale")
    public Locale defaultLocale(){
        return new Locale("en","US");
    }

    @Bean("canadianEnglishLocale")
    public Locale canadianEnglishLocale(){
        return new Locale("en","CA");
    }

    @Bean("canadianFrenchLocale")
    public Locale canadianFrenchLocale(){
        return new Locale("fr","CA");
    }

    @Bean("ukEnglishLocale")
    public Locale ukEnglishLocale(){
        return new Locale("en","GB");
    }

    @Bean("zaEnglishLocale")
    public Locale zaEnglishLocale(){
        return new Locale("en","ZA");
    }

    @Bean("australianEnglishLocale")
    public Locale australianEnglishLocale(){
        return new Locale("en","AU");
    }

    @Bean("singaporeEnglishLocale")
    public Locale singaporeEnglishLocale(){
        return new Locale("en","SG");
    }

    @Bean("hongkongEnglishLocale")
    public Locale hongkongEnglishLocale(){
        return new Locale("en","HK");
    }

    @Bean("hongkongchaineseLocale")
    public Locale hongkongchaineseLocale(){
        return new Locale("zh","HK");
    }

    @Bean("philippinesEnglishLocale")
    public Locale philippinesEnglishLocale(){
        return new Locale("en","PH");
    }

    @Bean("malaysiaEnglishLocale")
    public Locale malaysiaEnglishLocale(){
        return new Locale("en","MY");
    }

    @Bean("mexicoSpanishLocale")
    public Locale mexicoSpanishLocale(){
        return new Locale("es","MX");
    }

    @Bean("taiwanChineseLocale")
    public Locale taiwanChineseLocale(){
        return new Locale("zh","TW");
    }

    @Bean("unitedArabEmiratesEnglishLocale")
    public Locale unitedArabEmiratesEnglishLocale(){
        return new Locale("en","AE");
    }

    @Bean("thailandEnglishLocale")
    public Locale thailandEnglishLocale(){
        return new Locale("en","TH");
    }

    @Bean("thailandThaiLocale")
    public Locale thailandThaiLocale(){
        return new Locale("th","TH");
    }

    @Bean("russiaLocale")
    public Locale russiaLocale(){
        return new Locale("ru","RU");
    }

    @Bean("franceLocale")
    public Locale franceLocale(){
        return new Locale("fr","FR");
    }

    @Bean("switzerlandFrenchLocale")
    public Locale switzerlandFrenchLocale(){
        return new Locale("fr","CH");
    }

    @Bean("netherlandsLocale")
    public Locale netherlandsLocale(){
        return new Locale("nl","NL");
    }

    @Bean("supportedLocales")
    public Set<Locale> supportedLocales(@Autowired @Qualifier("defaultLocale") final Locale locale){
        final Set<Locale> supportedLocales = new HashSet<>();
        supportedLocales.add(locale);
        return supportedLocales;
    }

    @Bean("shipmentTrackingUrls")
    public Map<String,String> shipmentTrackingUrls(){
        final Map<String,String> shipmentTrackingUrls = new HashMap<>();
        shipmentTrackingUrls.put("FEDERAL EXPRESS (FOR CANADA ONLY)",
            "https://www.fedex.com/apps/fedextrack/?action=track&amp;trackingnumber={0}&amp;cntry_code=ca");
        shipmentTrackingUrls.put("UPS","https://wwwapps.ups.com/tracking/tracking.cgi?tracknum={0}");
        shipmentTrackingUrls.put("FEDERAL EXPRESS",
            "https://www.fedex.com/apps/fedextrack/?action=track&amp;trackingnumber={0}&amp;cntry_code=us");
        shipmentTrackingUrls.put("ONTRAC","https://www.ontrac.com/tracking.asp?trackingres=submit&amp;" +
            "tracking_number={0}&amp;trackBtn.x=11&amp;trackBtn.y=6&amp;trackBtn=trackingres_submit");
        shipmentTrackingUrls.put("FED EX GROUND",
            "https://www.fedex.com/apps/fedextrack/?action=track&amp;trackingnumber={0}&amp;cntry_code=us");
        shipmentTrackingUrls.put("UPS (FOR CANADA ONLY)","https://wwwapps.ups.com/tracking/tracking.cgi?tracknum={0}");
        return shipmentTrackingUrls;
    }

    @Bean("catalogDefaultVarIdMapping")
    public Map<String,String> catalogDefaultVarIdMapping(){
        final Map<String,String> catalogDefaultVarIdMapping = new HashMap<>();
        catalogDefaultVarIdMapping.put("apple-us-en","1");
        return catalogDefaultVarIdMapping;
    }

    @Bean("supplierProductMapping")
    public Map<String,String> supplierProductMapping(){
        final Map<String,String> supplierProductMapping = new HashMap<>();
        supplierProductMapping.put("200", MERCHANDISE);
        supplierProductMapping.put("600", GIFT_CARD);
        supplierProductMapping.put("700","digital");
        supplierProductMapping.put("90000", GIFT_CARD);
        supplierProductMapping.put(MERCHANDISE,"200");
        supplierProductMapping.put(GIFT_CARD,"600");
        supplierProductMapping.put("digital","700");
        supplierProductMapping.put("default_supplier_product", MERCHANDISE);
        supplierProductMapping.put(SERVICE_PLAN, "50000");
        return supplierProductMapping;
    }

    @Bean("blockFacetSearchForNodesStartingWith")
    public Map<String,String> blockFacetSearchForNodesStartingWith() {
        final Map<String, String> blockFacetSearchForNodesStartingWith = new HashMap<>();
        blockFacetSearchForNodesStartingWith.put("15", "Books");
        blockFacetSearchForNodesStartingWith.put("24", "CDs");
        blockFacetSearchForNodesStartingWith.put("17", "DVDs");
        return blockFacetSearchForNodesStartingWith;
    }

    @Bean("legacyMerchantCodeMappings")
    public Map<String,String> legacyMerchantCodeMappings() {
        final Map<String, String> legacyMerchantCodeMappings = new HashMap<>();
        //BestBuy
        legacyMerchantCodeMappings.put("200", "30002");
        legacyMerchantCodeMappings.put("30002", "200");
        //Buydotcom
        legacyMerchantCodeMappings.put("500", "30005");
        legacyMerchantCodeMappings.put("30005", "500");
        //replink
        legacyMerchantCodeMappings.put("300", "30003");
        legacyMerchantCodeMappings.put("30003", "300");
        return legacyMerchantCodeMappings;
    }

    //Configure Translations for User Country
    @Bean("TranslationsForUS")
    public List<String> TranslationsForUS(){
        final List<String> translations = new ArrayList<>();
        translations.add("US");
        translations.add("USA");
        translations.add("UNITED STATES");
        return translations;
    }

    @Bean("TranslationsForUS")
    public List<String> TranslationsForCA(){
        final List<String> translations = new ArrayList<>();
        translations.add("CA");
        translations.add("CANADA");
        return translations;
    }


}
