package com.back.cryptotool.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Реализация AES (Advanced Encryption Standard) с поддержкой разных длин ключей
 */
public class AesCipher implements CryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // 128 бит для AES

    // Поддерживаемые размеры ключей в битах
    public enum KeySize {
        AES_128(128, 16),
        AES_192(192, 24),
        AES_256(256, 32);

        private final int bits;
        private final int bytes;

        KeySize(int bits, int bytes) {
            this.bits = bits;
            this.bytes = bytes;
        }

        public int getBits() { return bits; }
        public int getBytes() { return bytes; }
    }

    private KeySize currentKeySize = KeySize.AES_128; // По умолчанию 128 бит

    @Override
    public String encrypt(String data, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("AES");
        }

        try {
            // Подготавливаем ключ нужной длины
            byte[] keyBytes = prepareKey(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // Генерируем случайный вектор инициализации (IV)
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Инициализируем шифр в режиме шифрования
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Шифруем данные
            byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

            // Объединяем IV и зашифрованные данные для хранения
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            // Возвращаем в Base64 для удобства хранения
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new CryptoException("Ошибка AES шифрования: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String encryptedData, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("AES");
        }

        try {
            // Декодируем из Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Извлекаем IV (первые 16 байт)
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Извлекаем зашифрованные данные (остальные байты)
            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // Подготавливаем ключ
            byte[] keyBytes = prepareKey(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // Инициализируем шифр в режиме дешифрования
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Дешифруем данные
            byte[] decryptedData = cipher.doFinal(encryptedBytes);
            return new String(decryptedData, "UTF-8");

        } catch (Exception e) {
            throw new CryptoException("Ошибка AES дешифрования: " + e.getMessage(), e);
        }
    }

    /**
     * Подготавливает ключ нужной длины в зависимости от выбранного размера
     */
    private byte[] prepareKey(String key) throws CryptoException {
        byte[] keyBytes = key.getBytes();
        int requiredLength = currentKeySize.getBytes();

        if (keyBytes.length == requiredLength) {
            return keyBytes;
        }

        // Если ключ неправильной длины - выбрасываем исключение
        throw new CryptoException(
                String.format("Ключ должен быть длиной %d символов (%d бит). Получено: %d символов.",
                        requiredLength, currentKeySize.getBits(), keyBytes.length)
        );
    }

    /**
     * Устанавливает размер ключа для AES
     */
    public void setKeySize(KeySize keySize) {
        this.currentKeySize = keySize;
    }

    /**
     * Возвращает текущий размер ключа
     */
    public KeySize getCurrentKeySize() {
        return currentKeySize;
    }

    @Override
    public String getName() {
        return String.format("AES-%d Encryption", currentKeySize.getBits());
    }

    @Override
    public String getDescription() {
        return String.format("Промышленный стандарт симметричного шифрования. " +
                        "Использует размер блока 128 бит и ключ %d бит. " +
                        "Режим CBC с PKCS5Padding обеспечивает высокую стойкость.",
                currentKeySize.getBits());
    }

    @Override
    public boolean requiresBase64() {
        return true;
    }

    @Override
    public boolean isValidKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        // Проверяем, что ключ имеет правильную длину
        int keyLength = key.getBytes().length;
        return keyLength == currentKeySize.getBytes();
    }

    @Override
    public String getKeyRequirements() {
        return String.format("Ключ длиной %d символов (%d бит)",
                currentKeySize.getBytes(), currentKeySize.getBits());
    }

    /**
     * Генерирует случайный ключ для текущего размера
     */
    public String generateKey() throws CryptoException {
        try {
            byte[] key = new byte[currentKeySize.getBytes()];
            new SecureRandom().nextBytes(key);
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new CryptoException("Ошибка генерации ключа: " + e.getMessage(), e);
        }
    }

    /**
     * Генерирует читаемый ключ для текущего размера
     */
    public String generateReadableKey() throws CryptoException {
        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < currentKeySize.getBytes(); i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new CryptoException("Ошибка генерации читаемого ключа: " + e.getMessage(), e);
        }
    }
}
