package org.example;

// elements.json’ı okur
// Her locator’ı Java objesine çevirir
// Hepsini hafızada HashMap’te tutar
//“searchBox” gibi key’lerle Selenium By üretir
// İstenen locator’ı Selenium By() formatına dönüştürüp verir
// Locator değişikliklerini tek yerden yönetmeni sağlar

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocatorRepository {

    private static final Map<String, LocatorDefinition> LOCATORS = new HashMap<>();

    static {
        // 1) Önce classpath'ten dene “test/resources içindeki elements.json’ı bul, oku.”
        InputStream is = LocatorRepository.class
                .getClassLoader()
                .getResourceAsStream("elements-info/elements.json");

        try {
            // 2) Classpath'ten gelmediyse, file system'den dene
            if (is == null) {
                // Proje root'u: gauge komutunu buradan çalıştırıyorsun
                String projectRoot = System.getProperty("user.dir");
                String path = projectRoot + File.separator + "src"
                        + File.separator + "test"
                        + File.separator + "resources"
                        + File.separator + "elements-info"
                        + File.separator + "elements.json";

                File file = new File(path);
                if (!file.exists()) {
                    throw new RuntimeException(
                            "elements.json bulunamadı. Denenen path: " + file.getAbsolutePath()
                    );
                }

                is = new FileInputStream(file);
                System.out.println("elements.json dosya sisteminden yüklendi: " + file.getAbsolutePath());
            } else {
                System.out.println("elements.json classpath üzerinden yüklendi.");
            }

            ObjectMapper mapper = new ObjectMapper(); //JSON’daki bilgileri Java objelerine dönüştürüyor

            // JSON array -> List<LocatorDefinition>
            List<LocatorDefinition> defs =
                    mapper.readValue(is, new TypeReference<List<LocatorDefinition>>() {});

            for (LocatorDefinition def : defs) {
                LOCATORS.put(def.getKey(), def); // Tüm locator objelerini bir HashMap’e koyuyor
            }

        } catch (IOException e) {
            throw new RuntimeException("elements.json okunurken hata oluştu.", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static By by(String key) {   // Selenium’a uygun By nesnesi döndürüyor
        LocatorDefinition def = LOCATORS.get(key);

        //Locator yoksa hata versin
        if (def == null) {
            throw new IllegalArgumentException("Unknown locator key: " + key);
        }

        String type = def.getType();
        String value = def.getValue();

        //Type’a göre Selenium locator üretir
        return switch (type) {
            case "id" -> By.id(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            default -> throw new IllegalArgumentException(
                    "Unsupported locator type: " + type + " (key: " + key + ")"
            );
        };
    }

    // Jackson için mapping class'ı
    public static class LocatorDefinition {
        private String key;
        private String value;
        private String type;

        public LocatorDefinition() {
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
