package com.back.cryptotool.crypto;

/**
 * Кастомное исключение для ошибок шифрования/дешифрования
 */
public class CryptoException extends Exception {

    /**
     * Создает новое исключение с сообщением об ошибке
     *
     * @param message описание ошибки
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с сообщением и причиной
     *
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Создает исключение для случая неверного ключа
     *
     * @param algorithm название алгоритма
     * @return исключение с соответствующим сообщением
     */
    public static CryptoException invalidKey(String algorithm) {
        return new CryptoException(
                String.format("Неверный ключ для алгоритма %s. Проверьте длину и формат.", algorithm)
        );
    }

    /**
     * Создает исключение для случая поврежденных данных
     *
     * @return исключение с соответствующим сообщением
     */
    public static CryptoException corruptedData() {
        return new CryptoException(
                "Данные повреждены или были зашифрованы с другим ключом"
        );
    }
}
