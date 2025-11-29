package com.back.cryptotool.crypto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Фасад для управления всеми доступными алгоритмами шифрования.
 * Предоставляет единую точку доступа к функциональности шифрования.
 */
public class CryptoManager {

    private final Map<String, CryptoService> algorithms;

    /**
     * Создает менеджер и регистрирует все доступные алгоритмы
     */
    public CryptoManager() {
        algorithms = new HashMap<>();
        registerAlgorithms();
    }

    /**
     * Регистрирует все доступные алгоритмы шифрования
     */
    private void registerAlgorithms() {
        // Эти классы мы создадим следующими
        algorithms.put("CAESAR", new CaesarCipher());
        algorithms.put("VIGENERE", new VigenereCipher());
        algorithms.put("AES", new AesCipher());
        // Можно добавить больше алгоритмов позже
    }

    /**
     * Шифрует данные с использованием указанного алгоритма
     *
     * @param algorithmName название алгоритма (например, "AES")
     * @param data данные для шифрования
     * @param key ключ шифрования
     * @return зашифрованные данные
     * @throws CryptoException если алгоритм не найден или произошла ошибка шифрования
     */
    public String encrypt(String algorithmName, String data, String key) throws CryptoException {
        CryptoService algorithm = getAlgorithm(algorithmName);

        if (!algorithm.isValidKey(key)) {
            throw CryptoException.invalidKey(algorithmName);
        }

        String encrypted = algorithm.encrypt(data, key);

        // Если алгоритм требует Base64 кодирования - применяем его
        if (algorithm.requiresBase64()) {
            return java.util.Base64.getEncoder().encodeToString(encrypted.getBytes());
        }

        return encrypted;
    }

    /**
     * Дешифрует данные с использованием указанного алгоритма
     *
     * @param algorithmName название алгоритма (например, "AES")
     * @param encryptedData зашифрованные данные
     * @param key ключ шифрования
     * @return расшифрованные данные
     * @throws CryptoException если алгоритм не найден или произошла ошибка дешифрования
     */
    public String decrypt(String algorithmName, String encryptedData, String key) throws CryptoException {
        CryptoService algorithm = getAlgorithm(algorithmName);

        if (!algorithm.isValidKey(key)) {
            throw CryptoException.invalidKey(algorithmName);
        }

        String dataToDecrypt = encryptedData;

        // Если алгоритм требует Base64 - сначала декодируем
        if (algorithm.requiresBase64()) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(encryptedData);
                dataToDecrypt = new String(decodedBytes);
            } catch (IllegalArgumentException e) {
                throw CryptoException.corruptedData();
            }
        }

        return algorithm.decrypt(dataToDecrypt, key);
    }

    /**
     * Возвращает алгоритм по имени
     *
     * @param algorithmName название алгоритма
     * @return реализация CryptoService
     * @throws CryptoException если алгоритм не найден
     */
    private CryptoService getAlgorithm(String algorithmName) throws CryptoException {
        CryptoService algorithm = algorithms.get(algorithmName.toUpperCase());
        if (algorithm == null) {
            throw new CryptoException("Алгоритм не найден: " + algorithmName);
        }
        return algorithm;
    }

    /**
     * Возвращает множество доступных алгоритмов
     *
     * @return множество названий алгоритмов
     */
    public Set<String> getAvailableAlgorithms() {
        return algorithms.keySet();
    }

    /**
     * Возвращает объект алгоритма по имени
     *
     * @param algorithmName название алгоритма
     * @return алгоритм или null если не найден
     */
    public CryptoService getAlgorithmDetails(String algorithmName) {
        return algorithms.get(algorithmName.toUpperCase());
    }

    /**
     * Устанавливает размер ключа для AES алгоритма
     */
    public void setAesKeySize(AesCipher.KeySize keySize) throws CryptoException {
        CryptoService aesAlgorithm = algorithms.get("AES");
        if (aesAlgorithm instanceof AesCipher) {
            ((AesCipher) aesAlgorithm).setKeySize(keySize);
        } else {
            throw new CryptoException("AES алгоритм не найден");
        }
    }

    /**
     * Возвращает текущий размер ключа AES
     */
    public AesCipher.KeySize getAesKeySize() throws CryptoException {
        CryptoService aesAlgorithm = algorithms.get("AES");
        if (aesAlgorithm instanceof AesCipher) {
            return ((AesCipher) aesAlgorithm).getCurrentKeySize();
        } else {
            throw new CryptoException("AES алгоритм не найден");
        }
    }

    /**
     * Генерирует ключ для AES
     */
    public String generateAesKey() throws CryptoException {
        CryptoService aesAlgorithm = algorithms.get("AES");
        if (aesAlgorithm instanceof AesCipher) {
            return ((AesCipher) aesAlgorithm).generateKey();
        } else {
            throw new CryptoException("AES алгоритм не найден");
        }
    }

    /**
     * Генерирует читаемый ключ для AES
     */
    public String generateAesReadableKey() throws CryptoException {
        CryptoService aesAlgorithm = algorithms.get("AES");
        if (aesAlgorithm instanceof AesCipher) {
            return ((AesCipher) aesAlgorithm).generateReadableKey();
        } else {
            throw new CryptoException("AES алгоритм не найден");
        }
    }
}
