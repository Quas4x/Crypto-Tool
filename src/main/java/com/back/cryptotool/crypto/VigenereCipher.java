package com.back.cryptotool.crypto;

/**
 * Реализация шифра Виженера - полиалфавитный шифр замены
 * Использует ключевое слово для сдвига каждой буквы на разную величину
 */
public class VigenereCipher implements CryptoService {

    private static final int ALPHABET_SIZE = 26;

    @Override
    public String encrypt(String data, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("Vigenere");
        }

        String processedKey = preprocessKey(key);
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < data.length(); i++) {
            char character = data.charAt(i);

            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';

                // Получаем текущий символ ключа и его сдвиг
                char keyChar = processedKey.charAt(keyIndex % processedKey.length());
                int shift = keyChar - 'A';  // A=0, B=1, C=2, ..., Z=25

                // Вычисляем новую позицию символа
                int originalPosition = character - base;
                int newPosition = (originalPosition + shift) % ALPHABET_SIZE;
                char newChar = (char) (base + newPosition);

                result.append(newChar);

                // Переходим к следующему символу ключа (только для букв)
                keyIndex++;
            } else {
                // Не-буквенные символы добавляем без изменений
                result.append(character);
            }
        }

        return result.toString();
    }

    @Override
    public String decrypt(String encryptedData, String key) throws CryptoException {
        if (!isValidKey(key)) {
            throw CryptoException.invalidKey("Vigenere");
        }

        String processedKey = preprocessKey(key);
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < encryptedData.length(); i++) {
            char character = encryptedData.charAt(i);

            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';

                char keyChar = processedKey.charAt(keyIndex % processedKey.length());
                int shift = keyChar - 'A';

                // Для дешифровки используем обратный сдвиг
                int originalPosition = character - base;
                int newPosition = (originalPosition - shift + ALPHABET_SIZE) % ALPHABET_SIZE;
                char newChar = (char) (base + newPosition);

                result.append(newChar);
                keyIndex++;
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }

    /**
     * Подготавливает ключ: переводит в верхний регистр и оставляет только буквы
     */
    private String preprocessKey(String key) {
        return key.toUpperCase().replaceAll("[^A-Z]", "");
    }

    @Override
    public String getName() {
        return "Шифр Виженера";
    }

    @Override
    public String getDescription() {
        return "Полиалфавитный шифр замены. Каждая буква текста сдвигается на величину, " +
                "определяемую соответствующей буквой ключевого слова. Более стойкий, чем шифр Цезаря.";
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

        // Ключ должен содержать хотя бы одну букву
        String lettersOnly = key.replaceAll("[^a-zA-Z]", "");
        return !lettersOnly.isEmpty();
    }

    @Override
    public String getKeyRequirements() {
        return "Ключевое слово (только буквы, минимум 1 символ)";
    }
}
