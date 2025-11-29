package com.back.cryptotool.crypto;

/**
 * Базовый интерфейс для всех алгоритмов шифрования.
 * Определяет контракт, который должны реализовывать все шифры.
 */
public interface CryptoService {

    /**
     * Шифрует переданные данные с использованием указанного ключа
     *
     * @param data исходные данные для шифрования
     * @param key ключ шифрования
     * @return зашифрованные данные в виде строки
     * @throws CryptoException если произошла ошибка во время шифрования
     */
    String encrypt(String data, String key) throws CryptoException;

    /**
     * Дешифрует переданные данные с использованием указанного ключа
     *
     * @param encryptedData зашифрованные данные
     * @param key ключ шифрования (должен совпадать с ключом шифрования)
     * @return расшифрованные исходные данные
     * @throws CryptoException если произошла ошибка во время дешифрования
     */
    String decrypt(String encryptedData, String key) throws CryptoException;

    /**
     * Возвращает человеко-читаемое название алгоритма
     *
     * @return название алгоритма (например, "AES-256", "Шифр Цезаря")
     */
    String getName();

    /**
     * Возвращает описание алгоритма и его особенностей
     *
     * @return описание алгоритма
     */
    String getDescription();

    /**
     * Проверяет, требует ли алгоритм кодирования результата в Base64
     * (нужно для бинарных алгоритмов вроде AES)
     *
     * @return true если результат нужно кодировать в Base64
     */
    boolean requiresBase64();

    /**
     * Проверяет валидность ключа для данного алгоритма
     *
     * @param key ключ для проверки
     * @return true если ключ валиден для этого алгоритма
     */
    boolean isValidKey(String key);

    /**
     * Возвращает рекомендации по формату ключа
     *
     * @return строка с рекомендациями (например, "16 символов для AES-128")
     */
    String getKeyRequirements();
}
