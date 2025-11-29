package com.back.cryptotool.ui;

import com.back.cryptotool.crypto.AesCipher;
import com.back.cryptotool.crypto.CryptoManager;
import com.back.cryptotool.crypto.CryptoException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Главное окно приложения Crypto Tool
 */
public class MainFrame extends JFrame {

    private CryptoManager cryptoManager;
    private JTabbedPane tabbedPane;

    // Компоненты для вкладки текстового шифрования
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JComboBox<String> algorithmComboBox;
    private JTextField keyField;
    private JButton encryptBtn;
    private JButton decryptBtn;
    private JButton clearBtn;
    private JComboBox<String> aesKeySizeComboBox;

    public MainFrame() {
        this.cryptoManager = new CryptoManager();
        initializeFrame();
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    /**
     * Инициализация главного окна
     */
    private void initializeFrame() {
        setTitle("Crypto Tool v1.0 - Шифратор/Дешифратор");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800, 700); // Размер окна
        setResizable(false);

        setLocationRelativeTo(null); // Центрируем окно

        // Устанавливаем иконку
        setIconImage(new ImageIcon("icon.png").getImage());
    }

    /**
     * Инициализация компонентов интерфейса
     */
    private void initializeComponents() {
        // Создаем вкладки
        tabbedPane = new JTabbedPane();

        // Создаем панели для каждой вкладки
        JPanel textPanel = createTextPanel();
        JPanel filePanel = createFilePanel();
        JPanel utilsPanel = createUtilsPanel();

        // Добавляем вкладки
        tabbedPane.addTab("📝 Текст", textPanel);
        tabbedPane.addTab("📁 Файлы", filePanel);
        tabbedPane.addTab("🛠️ Утилиты", utilsPanel);
    }

    /**
     * Создает панель для текстового шифрования с правильной компоновкой
     */
    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Панель ввода (верх)
        JPanel inputPanel = createTextAreaPanel("Ввод текста", true);

        // 2. Панель управления (центр) - используем GridBagLayout для точного позиционирования
        JPanel controlPanel = createControlPanel();

        // 3. Панель вывода (низ)
        JPanel outputPanel = createTextAreaPanel("Результат", false);

        // Собираем главную панель
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);
        panel.add(outputPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Создает панель управления с правильной компоновкой
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Настройки шифрования"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Первая строка: Алгоритм
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Алгоритм:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1;
        algorithmComboBox = new JComboBox<>(new String[]{"CAESAR", "VIGENERE", "AES"});
        algorithmComboBox.setToolTipText("Выберите алгоритм шифрования");
        panel.add(algorithmComboBox, gbc);

        // Вторая строка: Размер ключа AES (изначально скрыт)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel aesSizeLabel = new JLabel("Размер ключа AES:");
        panel.add(aesSizeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1;
        aesKeySizeComboBox = new JComboBox<>(new String[]{"128 бит", "192 бита", "256 бит"});
        aesKeySizeComboBox.setToolTipText("Выберите размер ключа для AES");
        aesKeySizeComboBox.setVisible(false);
        aesSizeLabel.setVisible(false); // Скрываем и label
        panel.add(aesKeySizeComboBox, gbc);

        // Третья строка: Ключ
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Ключ:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 1;
        keyField = new JTextField();
        keyField.setToolTipText("Введите ключ шифрования");
        panel.add(keyField, gbc);

        // Четвертая строка: Кнопки (занимают всю ширину)
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2; // Занимает 2 колонки
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        encryptBtn = new JButton("🔒 Зашифровать");
        decryptBtn = new JButton("🔓 Расшифровать");
        clearBtn = new JButton("🗑️ Очистить");

        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    /**
     * Создает панель с текстовой областью
     */
    private JPanel createTextAreaPanel(String title, boolean editable) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTextArea textArea = new JTextArea(6, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(editable);

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Сохраняем ссылку
        if (editable) {
            inputTextArea = textArea;
        } else {
            outputTextArea = textArea;

            // Добавляем кнопки только для панели результата
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            JButton copyBtn = new JButton("📋 Копировать");
            JButton saveBtn = new JButton("💾 Сохранить в файл");

            copyBtn.addActionListener(e -> copyToClipboard());
            saveBtn.addActionListener(e -> saveToFile());

            copyBtn.setToolTipText("Скопировать результат в буфер обмена");
            saveBtn.setToolTipText("Сохранить результат в текстовый файл");

            buttonPanel.add(copyBtn);
            buttonPanel.add(saveBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Создает панель для работы с файлами (заглушка)
     */
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Работа с файлами - в разработке", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создает панель утилит (заглушка)
     */
    private JPanel createUtilsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Утилиты - в разработке", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Настройка компоновки элементов
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        // Добавляем статус бар внизу
        JLabel statusBar = new JLabel(" Готов к работе ");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Настройка обработчиков событий
     */
    private void setupEventListeners() {
        // Кнопка шифрования
        encryptBtn.addActionListener(this::onEncrypt);

        // Кнопка дешифрования
        decryptBtn.addActionListener(this::onDecrypt);

        // Кнопка очистки
        clearBtn.addActionListener(e -> onClear());

        // Изменение алгоритма - обновляем подсказку для ключа
        algorithmComboBox.addActionListener(e -> updateKeyTooltip());

        // Изменение алгоритма - показываем/скрываем выбор размера ключа для AES
        algorithmComboBox.addActionListener(e -> onAlgorithmChanged());

        // Изменение размера ключа AES
        aesKeySizeComboBox.addActionListener(e -> onAesKeySizeChanged());

        // Изначально обновляем подсказку
        updateKeyTooltip();
    }

    /**
     * Обработчик шифрования
     */
    private void onEncrypt(ActionEvent e) {
        try {
            String text = inputTextArea.getText().trim();
            if (text.isEmpty()) {
                showError("Введите текст для шифрования");
                return;
            }

            String algorithm = (String) algorithmComboBox.getSelectedItem();
            String key = keyField.getText().trim();

            if (key.isEmpty()) {
                showError("Введите ключ шифрования");
                return;
            }

            // Выполняем шифрование
            String result = cryptoManager.encrypt(algorithm, text, key);
            outputTextArea.setText(result);

            showInfo("Текст успешно зашифрован!");

        } catch (CryptoException ex) {
            showError("Ошибка шифрования: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Неожиданная ошибка: " + ex.getMessage());
        }
    }

    /**
     * Обработчик дешифрования
     */
    private void onDecrypt(ActionEvent e) {
        try {
            String text = inputTextArea.getText().trim();
            if (text.isEmpty()) {
                showError("Введите текст для дешифрования");
                return;
            }

            String algorithm = (String) algorithmComboBox.getSelectedItem();
            String key = keyField.getText().trim();

            if (key.isEmpty()) {
                showError("Введите ключ дешифрования");
                return;
            }

            // Выполняем дешифрование
            String result = cryptoManager.decrypt(algorithm, text, key);
            outputTextArea.setText(result);

            showInfo("Текст успешно расшифрован!");

        } catch (CryptoException ex) {
            showError("Ошибка дешифрования: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Неожиданная ошибка: " + ex.getMessage());
        }
    }

    /**
     * Обработчик изменения алгоритма
     */
    private void onAlgorithmChanged() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        boolean isAes = "AES".equals(algorithm);

        // Находим компоненты в панели управления
        Component[] components = ((JPanel)tabbedPane.getComponentAt(0)).getComponents();
        JPanel controlPanel = (JPanel) components[1]; // controlPanel это второй компонент

        // Ищем label и comboBox для размера ключа AES
        for (Component comp : controlPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("Размер ключа AES:".equals(label.getText())) {
                    label.setVisible(isAes);
                }
            }
        }

        // Показываем/скрываем выбор размера ключа для AES
        aesKeySizeComboBox.setVisible(isAes);

        // Обновляем подсказку для ключа
        updateKeyTooltip();

        // Если выбран AES, обновляем требования к ключу
        if (isAes) {
            onAesKeySizeChanged();
        }

        // Перерисовываем панель
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    /**
     * Обработчик изменения размера ключа AES
     */
    private void onAesKeySizeChanged() {
        try {
            String selectedSize = (String) aesKeySizeComboBox.getSelectedItem();
            AesCipher.KeySize keySize = null;

            switch (selectedSize) {
                case "128 бит":
                    keySize = AesCipher.KeySize.AES_128;
                    break;
                case "192 бита":
                    keySize = AesCipher.KeySize.AES_192;
                    break;
                case "256 бит":
                    keySize = AesCipher.KeySize.AES_256;
                    break;
            }

            if (keySize != null) {
                cryptoManager.setAesKeySize(keySize);
                updateKeyTooltip(); // Обновляем подсказку
            }
        } catch (CryptoException e) {
            showError("Ошибка настройки AES: " + e.getMessage());
        }
    }

    /**
     * Копирует текст из поля результата в буфер обмена
     */
        private void copyToClipboard() {
        String textToCopy = outputTextArea.getText().trim();

        if (textToCopy.isEmpty()) {
            showError("Нет текста для копирования. Сначала выполните шифрование/дешифрование.");
            return;
        }

        try {
            // Создаем объект для передачи в буфер обмена
            StringSelection stringSelection = new StringSelection(textToCopy);

            // Получаем системный буфер обмена
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Устанавливаем текст в буфер обмена
            clipboard.setContents(stringSelection, null);

            // Показываем подтверждение
            showInfo("Текст скопирован в буфер обмена!\n\n" +
                    getPreviewText(textToCopy));

        } catch (Exception e) {
            showError("Ошибка при копировании в буфер обмена: " + e.getMessage());
        }
    }

    /**
     * Создает сокращенный preview текста для показа в сообщении
     */
    private String getPreviewText(String text) {
        if (text.length() <= 50) {
            return text;
        } else {
            return text.substring(0, 47) + "...";
        }
    }

    /**
     * Очистка полей
     */
    private void onClear() {
        inputTextArea.setText("");
        outputTextArea.setText("");
        keyField.setText("");
        showInfo("Поля очищены");
    }

    /**
     * Сохраняет текст результата в файл
     */
    private void saveToFile() {
        String textToSave = outputTextArea.getText().trim();

        if (textToSave.isEmpty()) {
            showError("Нет текста для сохранения. Сначала выполните шифрование/дешифрование.");
            return;
        }

        // Создаем диалог выбора файла
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить результат в файл");

        // Устанавливаем фильтр для текстовых файлов
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Текстовые файлы (*.txt)", "txt");
        fileChooser.setFileFilter(filter);

        // Предлагаем осмысленное имя файла по умолчанию
        String defaultFileName = generateDefaultFileName();
        fileChooser.setSelectedFile(new File(defaultFileName));

        // Показываем диалог сохранения
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Добавляем расширение .txt если его нет
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }

            // Проверяем, существует ли файл
            if (fileToSave.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                        "Файл \"" + fileToSave.getName() + "\" уже существует.\nПерезаписать его?",
                        "Файл существует",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (overwrite != JOptionPane.YES_OPTION) {
                    return; // Пользователь отказался от перезаписи
                }
            }

            // Сохраняем файл
            try {
                saveTextToFile(textToSave, fileToSave);

                // Показываем подтверждение
                showSaveSuccess(fileToSave, textToSave.length());

            } catch (IOException e) {
                showError("Ошибка при сохранении файла: " + e.getMessage());
            }
        }
    }

    /**
     * Генерирует осмысленное имя файла по умолчанию
     */
    private String generateDefaultFileName() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        if (algorithm != null) {
            return String.format("crypto_result_%s_%s.txt", algorithm.toLowerCase(), timestamp);
        } else {
            return String.format("crypto_result_%s.txt", timestamp);
        }
    }

    /**
     * Сохраняет текст в файл
     */
    private void saveTextToFile(String text, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }

    /**
     * Показывает сообщение об успешном сохранении
     */
    private void showSaveSuccess(File savedFile, int textLength) {
        String fileSize;
        try {
            long size = Files.size(Paths.get(savedFile.getAbsolutePath()));
            if (size < 1024) {
                fileSize = size + " байт";
            } else {
                fileSize = String.format("%.1f КБ", size / 1024.0);
            }
        } catch (IOException e) {
            fileSize = "неизвестно";
        }

        String message = "✅ Файл успешно сохранен!\n\n" +
                "Имя файла: " + savedFile.getName() + "\n" +
                "Размер: " + fileSize + "\n" +
                "Символов: " + textLength + "\n" +
                "Путь: " + savedFile.getParent();

        JOptionPane.showMessageDialog(this, message, "Файл сохранен",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Обновляет подсказку для поля ключа в зависимости от алгоритма
     */
    private void updateKeyTooltip() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        String tooltip = "";

        switch (algorithm) {
            case "CAESAR":
                tooltip = "Целое число от 1 до 25 (сдвиг)";
                break;
            case "VIGENERE":
                tooltip = "Ключевое слово (только буквы)";
                break;
            case "AES":
                try {
                    AesCipher.KeySize keySize = cryptoManager.getAesKeySize();
                    tooltip = String.format("Ключ длиной %d символов (%d бит)",
                            keySize.getBytes(), keySize.getBits());
                } catch (CryptoException e) {
                    tooltip = "Ключ для AES шифрования";
                }
                break;
        }

        keyField.setToolTipText(tooltip);
    }

    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Показывает информационное сообщение
     */
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Запуск приложения
     */
    public static void main(String[] args) {
        // Устанавливаем красивый внешний вид (Nimbus)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Если Nimbus не доступен, используем системный стиль
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Используем стандартный стиль
            }
        }

        // Запускаем приложение в потоке обработки событий
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
