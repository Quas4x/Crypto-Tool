package com.back.cryptotool.util;

import com.back.cryptotool.crypto.CryptoManager;
import com.back.cryptotool.crypto.CryptoException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Утилита для обработки файлов - шифрование и дешифрование
 */
public class FileProcessor {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 МБ
    private static final String ENCRYPTED_EXTENSION = ".enc";

    private final CryptoManager cryptoManager;

    public FileProcessor(CryptoManager cryptoManager) {
        this.cryptoManager = cryptoManager;
    }

    /**
     * Шифрует файл
     */
    public File encryptFile(File inputFile, String algorithm, String key)
            throws CryptoException, IOException {

        validateFile(inputFile);

        // Читаем файл в байты
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

        // Преобразуем байты в Base64 строку для шифрования
        String base64Data = java.util.Base64.getEncoder().encodeToString(fileBytes);

        // Шифруем данные
        String encryptedData = cryptoManager.encrypt(algorithm, base64Data, key);

        // Создаем выходной файл
        File outputFile = new File(inputFile.getAbsolutePath() + ENCRYPTED_EXTENSION);

        // Сохраняем зашифрованные данные
        Files.write(outputFile.toPath(), encryptedData.getBytes());

        return outputFile;
    }

    /**
     * Дешифрует файл
     */
    public File decryptFile(File inputFile, String algorithm, String key)
            throws CryptoException, IOException {

        validateFile(inputFile);

        if (!isEncryptedFile(inputFile)) {
            throw new CryptoException("Файл не является зашифрованным (отсутствует расширение .enc)");
        }

        // Читаем зашифрованный файл
        byte[] encryptedBytes = Files.readAllBytes(inputFile.toPath());
        String encryptedData = new String(encryptedBytes);

        // Дешифруем данные
        String decryptedBase64 = cryptoManager.decrypt(algorithm, encryptedData, key);

        // Преобразуем из Base64 обратно в байты
        byte[] decryptedBytes = java.util.Base64.getDecoder().decode(decryptedBase64);

        // Восстанавливаем имя файла
        File outputFile = restoreOriginalFileName(inputFile);

        // Сохраняем расшифрованные данные
        Files.write(outputFile.toPath(), decryptedBytes);

        return outputFile;
    }

    /**
     * Проверяет файл перед обработкой
     */
    private void validateFile(File file) throws CryptoException, IOException {
        if (!file.exists()) {
            throw new CryptoException("Файл не существует: " + file.getName());
        }

        if (file.length() > MAX_FILE_SIZE) {
            throw new CryptoException(
                    String.format("Файл слишком большой: %.1f МБ. Максимум: 50 МБ",
                            file.length() / (1024.0 * 1024.0))
            );
        }

        if (file.length() == 0) {
            throw new CryptoException("Файл пуст: " + file.getName());
        }
    }

    /**
     * Проверяет, является ли файл зашифрованным
     */
    private boolean isEncryptedFile(File file) {
        return file.getName().toLowerCase().endsWith(ENCRYPTED_EXTENSION);
    }

    /**
     * Восстанавливает оригинальное имя файла (публичный метод для предварительной проверки)
     */
    public File restoreOriginalFileName(File encryptedFile) {
        String fileName = encryptedFile.getName();

        if (fileName.toLowerCase().endsWith(ENCRYPTED_EXTENSION)) {
            // Убираем .enc
            String originalName = fileName.substring(0, fileName.length() - ENCRYPTED_EXTENSION.length());
            return new File(encryptedFile.getParent(), originalName);
        } else {
            // Если нет .enc, создаем имя с .decrypted
            return new File(encryptedFile.getParent(), fileName + ".decrypted");
        }
    }

    /**
     * Форматирует размер файла для отображения
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " Б";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f КБ", size / 1024.0);
        } else {
            return String.format("%.1f МБ", size / (1024.0 * 1024.0));
        }
    }

    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
}
