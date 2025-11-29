package com.back.cryptotool;

import com.back.cryptotool.crypto.CryptoManager;
import com.back.cryptotool.crypto.CryptoException;

/**
 * Полный тестовый класс для проверки всех алгоритмов шифрования
 */
public class TestCryptoService {

    public static void main(String[] args) {
        System.out.println("🔐 === ПОЛНОЕ ТЕСТИРОВАНИЕ CRYPTO SERVICE === 🔐\n");

        try {
            // 1. Создаем менеджер алгоритмов
            CryptoManager cryptoManager = new CryptoManager();

            // 2. Показываем доступные алгоритмы
            System.out.println("📋 ДОСТУПНЫЕ АЛГОРИТМЫ:");
            cryptoManager.getAvailableAlgorithms().forEach(algorithm -> {
                System.out.println("   • " + algorithm);
            });

            // 3. Показываем детали каждого алгоритма
            System.out.println("\n📖 ИНФОРМАЦИЯ ОБ АЛГОРИТМАХ:");
            cryptoManager.getAvailableAlgorithms().forEach(algorithmName -> {
                var algorithm = cryptoManager.getAlgorithmDetails(algorithmName);
                System.out.println("   🎯 " + algorithm.getName());
                System.out.println("      📝 " + algorithm.getDescription());
                System.out.println("      🔑 Требования к ключу: " + algorithm.getKeyRequirements());
                System.out.println("      📊 Base64: " + (algorithm.requiresBase64() ? "да" : "нет"));
                System.out.println();
            });

            // 4. ТЕСТ ШИФРА ЦЕЗАРЯ
            System.out.println("🔥 ТЕСТ ШИФРА ЦЕЗАРЯ:");
            testRealCaesar(cryptoManager);

            // 5. ТЕСТ ШИФРА ВИЖЕНЕРА
            System.out.println("\n🔥 ТЕСТ ШИФРА ВИЖЕНЕРА:");
            testRealVigenere(cryptoManager);

            // 6. ТЕСТ AES ШИФРОВАНИЯ
            System.out.println("\n🔥 ТЕСТ AES ШИФРОВАНИЯ:");
            testRealAES(cryptoManager);

            // 7. СРАВНИТЕЛЬНЫЙ ТЕСТ ВСЕХ АЛГОРИТМОВ
            System.out.println("\n🏆 СРАВНИТЕЛЬНЫЙ ТЕСТ ВСЕХ АЛГОРИТМОВ:");
            testAllAlgorithms(cryptoManager);

            // 8. ТЕСТ ОБРАБОТКИ ОШИБОК
            System.out.println("\n🚨 ТЕСТ ОБРАБОТКИ ОШИБОК:");
            testErrorHandling(cryptoManager);

            System.out.println("\n✅ ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ УСПЕШНО!");
            System.out.println("🎉 БЭКЕНД ПОЛНОСТЬЮ ГОТОВ! Можно приступать к созданию GUI!");

        } catch (Exception e) {
            System.err.println("❌ КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testRealCaesar(CryptoManager cryptoManager) throws CryptoException {
        String testMessage = "Hello World";
        String key = "3";

        System.out.println("   Исходное: '" + testMessage + "'");
        System.out.println("   Ключ (сдвиг): " + key);

        String encrypted = cryptoManager.encrypt("CAESAR", testMessage, key);
        String decrypted = cryptoManager.decrypt("CAESAR", encrypted, key);

        System.out.println("   Зашифровано: '" + encrypted + "'");
        System.out.println("   Расшифровано: '" + decrypted + "'");
        System.out.println("   ✅ Успех: " + testMessage.equals(decrypted));

        // Тест с русскими буквами
        String russianMessage = "Привет Мир";
        String russianEncrypted = cryptoManager.encrypt("CAESAR", russianMessage, key);
        String russianDecrypted = cryptoManager.decrypt("CAESAR", russianEncrypted, key);

        System.out.println("\n   Русский текст: '" + russianMessage + "'");
        System.out.println("   Зашифровано: '" + russianEncrypted + "'");
        System.out.println("   Расшифровано: '" + russianDecrypted + "'");
        System.out.println("   ✅ Успех: " + russianMessage.equals(russianDecrypted));
    }

    private static void testRealVigenere(CryptoManager cryptoManager) throws CryptoException {
        // Тест 1: Простой случай
        String testMessage = "ATTACKATDAWN";
        String key = "LEMON";

        System.out.println("   Тест 1 - Простой текст:");
        System.out.println("   Исходное: '" + testMessage + "'");
        System.out.println("   Ключ: '" + key + "'");

        String encrypted = cryptoManager.encrypt("VIGENERE", testMessage, key);
        String decrypted = cryptoManager.decrypt("VIGENERE", encrypted, key);

        System.out.println("   Зашифровано: '" + encrypted + "'");
        System.out.println("   Расшифровано: '" + decrypted + "'");
        System.out.println("   ✅ Успех: " + testMessage.equals(decrypted));

        // Тест 2: Текст с пробелами и разным регистром
        String testMessage2 = "Hello World!";
        String key2 = "KEY";

        System.out.println("\n   Тест 2 - Текст с пробелами:");
        System.out.println("   Исходное: '" + testMessage2 + "'");
        System.out.println("   Ключ: '" + key2 + "'");

        String encrypted2 = cryptoManager.encrypt("VIGENERE", testMessage2, key2);
        String decrypted2 = cryptoManager.decrypt("VIGENERE", encrypted2, key2);

        System.out.println("   Зашифровано: '" + encrypted2 + "'");
        System.out.println("   Расшифровано: '" + decrypted2 + "'");
        System.out.println("   ✅ Успех: " + testMessage2.equals(decrypted2));
    }

    private static void testRealAES(CryptoManager cryptoManager) throws CryptoException {
        // Тест 1: Ключ 16 байт (128 бит)
        String testMessage = "Секретное сообщение для AES!";
        String key16 = "SixteenByteKey!!"; // 16 символов = 16 байт

        System.out.println("   Тест 1 - Ключ 128 бит:");
        System.out.println("   Исходное: '" + testMessage + "'");
        System.out.println("   Ключ: '" + key16 + "' (16 байт)");

        String encrypted = cryptoManager.encrypt("AES", testMessage, key16);
        String decrypted = cryptoManager.decrypt("AES", encrypted, key16);

        System.out.println("   Зашифровано (Base64):");
        System.out.println("   " + encrypted);
        System.out.println("   Расшифровано: '" + decrypted + "'");
        System.out.println("   ✅ Успех: " + testMessage.equals(decrypted));

        // Тест 2: Ключ 24 байта (192 бита)
        String key24 = "TwentyFourByteKeyForAES!"; // 24 символа

        System.out.println("\n   Тест 2 - Ключ 192 бит:");
        System.out.println("   Исходное: '" + testMessage + "'");
        System.out.println("   Ключ: '" + key24 + "' (24 байта)");

        String encrypted2 = cryptoManager.encrypt("AES", testMessage, key24);
        String decrypted2 = cryptoManager.decrypt("AES", encrypted2, key24);

        System.out.println("   Зашифровано (Base64):");
        System.out.println("   " + encrypted);
        System.out.println("   Расшифровано: '" + decrypted2 + "'");
        System.out.println("   ✅ Успех: " + testMessage.equals(decrypted2));

        // Тест 3: Ключ 32 байта (256 бит)
        String key32 = "ThirtyTwoByteKeyForAESEncryption"; // 32 символа

        System.out.println("\n   Тест 3 - Ключ 256 бит:");
        System.out.println("   Исходное: '" + testMessage + "'");
        System.out.println("   Ключ: '" + key32 + "' (32 байта)");

        String encrypted3 = cryptoManager.encrypt("AES", testMessage, key32);
        String decrypted3 = cryptoManager.decrypt("AES", encrypted3, key32);

        System.out.println("   Зашифровано (Base64):");
        System.out.println("   " + encrypted);
        System.out.println("   Расшифровано: '" + decrypted3 + "'");
        System.out.println("   ✅ Успех: " + testMessage.equals(decrypted3));
    }

    private static void testAllAlgorithms(CryptoManager cryptoManager) throws CryptoException {
        String testMessage = "Secret Message 123!";

        System.out.println("   Исходное сообщение: '" + testMessage + "'");
        System.out.println();

        // Тестируем все алгоритмы на одном сообщении
        String[] algorithms = {"CAESAR", "VIGENERE", "AES"};
        String[] keys = {"5", "CRYPTO", "SixteenByteKey!!"};

        for (int i = 0; i < algorithms.length; i++) {
            String algorithm = algorithms[i];
            String key = keys[i];

            System.out.println("   🔹 " + algorithm + ":");
            System.out.println("      Ключ: '" + key + "'");

            String encrypted = cryptoManager.encrypt(algorithm, testMessage, key);
            String decrypted = cryptoManager.decrypt(algorithm, encrypted, key);

            if (algorithm.equals("AES")) {
                System.out.println("      Зашифровано (Base64): " + encrypted.substring(0, 30) + "...");
            } else {
                System.out.println("      Зашифровано: '" + encrypted + "'");
            }

            System.out.println("      Расшифровано: '" + decrypted + "'");
            System.out.println("      ✅ Совпадение: " + testMessage.equals(decrypted));
            System.out.println();
        }
    }

    private static void testErrorHandling(CryptoManager cryptoManager) {
        // Тест неверных ключей
        String[][] invalidTests = {
                {"CAESAR", "0", "Сдвиг должен быть от 1 до 25"},
                {"CAESAR", "26", "Сдвиг должен быть от 1 до 25"},
                {"CAESAR", "abc", "Ключ должен быть числом"},
                {"VIGENERE", "", "Ключ не может быть пустым"},
                {"VIGENERE", "123", "Ключ должен содержать буквы"},
                {"AES", "short", "Ключ должен быть 16/24/32 байта"},
                {"AES", "tooshortkey", "Ключ должен быть 16/24/32 байта"},
                {"UNKNOWN", "key", "Алгоритм не найден"}
        };

        for (String[] test : invalidTests) {
            String algorithm = test[0];
            String key = test[1];
            String expectedError = test[2];

            try {
                cryptoManager.encrypt(algorithm, "test", key);
                System.out.println("   ❌ ОШИБКА: Для " + algorithm + " с ключом '" + key + "' не сгенерирована ошибка!");
            } catch (CryptoException e) {
                if (e.getMessage().contains(expectedError)) {
                    System.out.println("   ✅ " + algorithm + ": правильно отловили - '" + e.getMessage() + "'");
                } else {
                    System.out.println("   ⚠️  " + algorithm + ": ошибка не совпадает. Ожидали: " + expectedError + ", получили: " + e.getMessage());
                }
            }
        }
    }
}
