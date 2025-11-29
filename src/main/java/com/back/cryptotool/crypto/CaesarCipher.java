package com.back.cryptotool.crypto;

/**
 * Реализация шифра Цезаря - классический шифр замены с фиксированным сдвигом
 */
public class CaesarCipher implements CryptoService {

    private static final int ALPHABET_SIZE = 26;

    @Override
    public String encrypt(String data, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("Caesar");
        }

        int shift = Integer.parseInt(key);
        StringBuilder result = new StringBuilder();

        for (char character : data.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                int originalPosition = character - base;
                int newPosition = (originalPosition + shift) % ALPHABET_SIZE;
                character = (char) (base + newPosition);
            }
            result.append(character);
        }

        return result.toString();
    }

    @Override
    public String decrypt(String encryptedData, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("Caesar");
        }

        int shift = Integer.parseInt(key);
        StringBuilder result = new StringBuilder();

        for (char character : encryptedData.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                int originalPosition = character - base;
                int newPosition = (originalPosition - shift + ALPHABET_SIZE) % ALPHABET_SIZE;
                character = (char) (base + newPosition);
            }
            result.append(character);
        }

        return result.toString();
    }

    @Override
    public String getName() {
        return "Шифр Цезаря";
    }

    @Override
    public String getDescription() {
        return "Классический шифр замены с фиксированным сдвигом. Каждая буква сдвигается на фиксированное число позиций в алфавите.";
    }

    @Override
    public boolean requiresBase64() {
        return false;
    }

    @Override
    public boolean isValidKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        try {
            int shift = Integer.parseInt(key.trim());
            return shift >= 1 && shift <= 25;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getKeyRequirements() {
        return "Целое число от 1 до 25 (сдвиг)";
    }
}
