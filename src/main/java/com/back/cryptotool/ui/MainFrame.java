package com.back.cryptotool.ui;

import com.back.cryptotool.crypto.AesCipher;
import com.back.cryptotool.crypto.CryptoManager;
import com.back.cryptotool.crypto.CryptoException;
import com.back.cryptotool.util.FileProcessor;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Главное окно приложения Crypto Tool
 */
public class MainFrame extends JFrame {

    private final CryptoManager cryptoManager;
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

    // Компоненты для вкладки работы с файлами
    private JTextField filePathField;
    private JButton fileBrowseBtn;
    private JButton encryptFileBtn;
    private JButton decryptFileBtn;
    private JButton clearFileBtn;
    private JLabel fileInfoLabel;
    private JComboBox<String> fileAlgorithmComboBox;
    private JTextField fileKeyField;
    private JComboBox<String> fileAesKeySizeComboBox;

    private FileProcessor fileProcessor;
    private File selectedFile;

    // Компоненты для вкладки утилит - Генератор ключей
    private JComboBox<String> keySizeComboBox;
    private JButton generateKeyBtn;
    private JButton clearKeyBtn;
    private JTextField generatedKeyField;
    private JLabel keyStatusLabel;

    // Компоненты для вкладки утилит - Base64 кодек
    private JTextArea base64InputArea;
    private JTextArea base64OutputArea;
    private JButton encodeBase64Btn;
    private JButton decodeBase64Btn;
    private JButton clearBase64Btn;
    private JButton copyBase64Btn;

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
     * Создает панель для работы с файлами
     */
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Инициализируем FileProcessor
        fileProcessor = new FileProcessor(cryptoManager);

        // 1. Панель выбора файла
        JPanel fileSelectionPanel = createFileSelectionPanel();

        // 2. Панель управления
        JPanel controlPanel = createFileControlPanel();

        // Собираем главную панель
        panel.add(fileSelectionPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создает панель выбора файла
     */
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Выбор файла"));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // Поле пути и кнопка обзора
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.add(new JLabel("Файл:"), BorderLayout.WEST);

        filePathField = new JTextField();
        filePathField.setEditable(false);
        pathPanel.add(filePathField, BorderLayout.CENTER);

        fileBrowseBtn = new JButton("📁 Обзор...");
        pathPanel.add(fileBrowseBtn, BorderLayout.EAST);

        // Информация о файле
        fileInfoLabel = new JLabel("Выберите файл для обработки");
        fileInfoLabel.setForeground(Color.GRAY);

        topPanel.add(pathPanel, BorderLayout.NORTH);
        topPanel.add(fileInfoLabel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создает панель управления для файлов
     */
    private JPanel createFileControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Настройки шифрования"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Алгоритм
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Алгоритм:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        fileAlgorithmComboBox = new JComboBox<>(new String[]{"CAESAR", "VIGENERE", "AES"});
        panel.add(fileAlgorithmComboBox, gbc);

        // Размер ключа AES (изначально скрыт)
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel aesSizeLabel = new JLabel("Размер ключа AES:");
        panel.add(aesSizeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        fileAesKeySizeComboBox = new JComboBox<>(new String[]{"128 бит", "192 бита", "256 бит"});
        fileAesKeySizeComboBox.setVisible(false);
        aesSizeLabel.setVisible(false);
        panel.add(fileAesKeySizeComboBox, gbc);

        // Ключ
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Ключ:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        fileKeyField = new JTextField();
        panel.add(fileKeyField, gbc);

        // Кнопки
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        encryptFileBtn = new JButton("🔒 Зашифровать файл");
        decryptFileBtn = new JButton("🔓 Расшифровать файл");
        clearFileBtn = new JButton("🗑️ Очистить");

        buttonPanel.add(encryptFileBtn);
        buttonPanel.add(decryptFileBtn);
        buttonPanel.add(clearFileBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    /**
     * Создает панель утилит с генератором ключей и Base64 кодеками
     */
    private JPanel createUtilsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Создаем две колонки
        JPanel keyGeneratorPanel = createKeyGeneratorPanel();
        JPanel base64Panel = createBase64Panel();

        // Используем GridLayout для равного разделения
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        columnsPanel.add(keyGeneratorPanel);
        columnsPanel.add(base64Panel);

        mainPanel.add(columnsPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * Создает панель генератора AES ключей
     */
    private JPanel createKeyGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("🔐 Генератор AES ключей"));

        // Панель управления
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Выбор размера ключа
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Размер ключа:"), gbc);

        gbc.gridy = 1;
        keySizeComboBox = new JComboBox<>(new String[]{"128 бит", "192 бита", "256 бит"});
        controlPanel.add(keySizeComboBox, gbc);

        // Кнопки
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        generateKeyBtn = new JButton("🔁 Сгенерировать");
        clearKeyBtn = new JButton("🗑️ Очистить");

        buttonPanel.add(generateKeyBtn);
        buttonPanel.add(clearKeyBtn);
        controlPanel.add(buttonPanel, gbc);

        // Поле сгенерированного ключа
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(new JLabel("Сгенерированный ключ:"), gbc);

        gbc.gridy = 4;
        generatedKeyField = new JTextField();
        generatedKeyField.setEditable(false);
        controlPanel.add(generatedKeyField, gbc); // Просто поле, без кнопки

        // Статус
        gbc.gridy = 5;
        keyStatusLabel = new JLabel(" ");
        keyStatusLabel.setForeground(Color.GREEN);
        controlPanel.add(keyStatusLabel, gbc);

        panel.add(controlPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Создает панель Base64 кодера/декодера
     */
    private JPanel createBase64Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("🔤 Base64 Кодер/Декодер"));

        // Основная панель с вертикальным расположением
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));

        // Панель ввода
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("Исходный текст:"), BorderLayout.NORTH);

        base64InputArea = new JTextArea(5, 20);
        base64InputArea.setLineWrap(true);
        base64InputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(base64InputArea);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        encodeBase64Btn = new JButton("🔼 Закодировать");
        decodeBase64Btn = new JButton("🔽 Раскодировать");
        clearBase64Btn = new JButton("🗑️ Очистить");

        buttonPanel.add(encodeBase64Btn);
        buttonPanel.add(decodeBase64Btn);
        buttonPanel.add(clearBase64Btn);

        // Панель вывода
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.add(new JLabel("Результат:"), BorderLayout.NORTH);

        base64OutputArea = new JTextArea(5, 20);
        base64OutputArea.setLineWrap(true);
        base64OutputArea.setWrapStyleWord(true);
        base64OutputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(base64OutputArea);

        JPanel outputButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        copyBase64Btn = new JButton("📋 Копировать");
        outputButtonPanel.add(copyBase64Btn);

        outputPanel.add(outputScroll, BorderLayout.CENTER);
        outputPanel.add(outputButtonPanel, BorderLayout.SOUTH);

        // Собираем всё вместе
        mainContent.add(inputPanel, BorderLayout.NORTH);
        mainContent.add(buttonPanel, BorderLayout.CENTER);
        mainContent.add(outputPanel, BorderLayout.SOUTH);

        panel.add(mainContent, BorderLayout.CENTER);
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

        // Обработчики для вкладки файлов
        setupFileEventListeners();

        // Обработчики для вкладки утилит
        setupUtilsEventListeners();
    }

    /**
     * Настройка обработчиков для вкладки файлов
     */
    private void setupFileEventListeners() {
        // Кнопка выбора файла
        fileBrowseBtn.addActionListener(e -> onFileBrowse());

        // Кнопки шифрования/дешифрования файлов
        encryptFileBtn.addActionListener(e -> onEncryptFile());
        decryptFileBtn.addActionListener(e -> onDecryptFile());

        // Кнопка очистки файлов
        clearFileBtn.addActionListener(e -> onClearFile());

        // Изменение алгоритма для файлов
        fileAlgorithmComboBox.addActionListener(e -> onFileAlgorithmChanged());

        // Изменение размера ключа AES для файлов
        fileAesKeySizeComboBox.addActionListener(e -> onFileAesKeySizeChanged());
    }

    // Находим кнопку копирования ключа и добавляем обработчик
    private void setupUtilsEventListeners() {
        // Генератор ключей
        generateKeyBtn.addActionListener(e -> onGenerateKey());
        clearKeyBtn.addActionListener(e -> onClearKey());

        // Base64 кодек
        encodeBase64Btn.addActionListener(e -> onEncodeBase64());
        decodeBase64Btn.addActionListener(e -> onDecodeBase64());
        clearBase64Btn.addActionListener(e -> onClearBase64());
        copyBase64Btn.addActionListener(e -> onCopyBase64());
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
     * Обработчик выбора файла
     */
    private void onFileBrowse() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл для обработки");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Настраиваем фильтр файлов
        setupFileFilters(fileChooser);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            updateFileInfo();
        }
    }

    /**
     * Настраивает фильтры файлов
     */
    private void setupFileFilters(JFileChooser fileChooser) {
        // Разрешённые расширения
        String[] allowedExtensions = {
                "txt", "pdf", "doc", "docx", "rtf",  // Документы
                "jpg", "jpeg", "png", "gif", "bmp",  // Изображения
                "mp3", "wav", "flac",                // Аудио
                "mp4", "avi", "mkv",                 // Видео
                "zip", "rar", "7z",                  // Архивы
                "enc"                                // Зашифрованные файлы
        };

        FileNameExtensionFilter allAllowedFilter = new FileNameExtensionFilter(
                "Все разрешённые файлы", allowedExtensions);

        FileNameExtensionFilter documentsFilter = new FileNameExtensionFilter(
                "Документы (*.txt, *.pdf, *.doc, *.docx)", "txt", "pdf", "doc", "docx", "rtf");

        FileNameExtensionFilter imagesFilter = new FileNameExtensionFilter(
                "Изображения (*.jpg, *.png, *.gif)", "jpg", "jpeg", "png", "gif", "bmp");

        FileNameExtensionFilter encryptedFilter = new FileNameExtensionFilter(
                "Зашифрованные файлы (*.enc)", "enc");

        FileNameExtensionFilter allFilesFilter = new FileNameExtensionFilter(
                "Все файлы (*.*)", "*");

        fileChooser.addChoosableFileFilter(documentsFilter);
        fileChooser.addChoosableFileFilter(imagesFilter);
        fileChooser.addChoosableFileFilter(encryptedFilter);
        fileChooser.addChoosableFileFilter(allAllowedFilter);
        fileChooser.addChoosableFileFilter(allFilesFilter);
        fileChooser.setFileFilter(allAllowedFilter);
    }

    /**
     * Обновляет информацию о выбранном файле
     */
    private void updateFileInfo() {
        if (selectedFile != null && selectedFile.exists()) {
            filePathField.setText(selectedFile.getAbsolutePath());

            String fileSize = FileProcessor.formatFileSize(selectedFile.length());
            String fileType = getFileType(selectedFile);
            String status = "✅ " + fileType + " (" + fileSize + ")";

            // Проверяем размер файла
            if (selectedFile.length() > FileProcessor.getMaxFileSize()) {
                status = "❌ Слишком большой: " + fileSize + " (максимум 50 МБ)";
                fileInfoLabel.setForeground(Color.RED);
                encryptFileBtn.setEnabled(false);
                decryptFileBtn.setEnabled(false);
            } else {
                fileInfoLabel.setForeground(Color.BLACK);

                // Автоматически определяем операцию по расширению
                if (selectedFile.getName().toLowerCase().endsWith(".enc")) {
                    decryptFileBtn.setEnabled(true);
                    encryptFileBtn.setEnabled(false);
                } else {
                    encryptFileBtn.setEnabled(true);
                    decryptFileBtn.setEnabled(true);
                }
            }

            fileInfoLabel.setText(status);
        } else {
            // Очистка полей
            filePathField.setText("");
            fileInfoLabel.setText("Файл не выбран");
            fileInfoLabel.setForeground(Color.GRAY);
            encryptFileBtn.setEnabled(false);
            decryptFileBtn.setEnabled(false);
        }
    }

    /**
     * Определяет тип файла для отображения
     */
    private String getFileType(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".enc")) return "Зашифрованный файл";
        if (name.endsWith(".txt")) return "Текстовый файл";
        if (name.endsWith(".pdf")) return "PDF документ";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "Word документ";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) return "Изображение";
        if (name.endsWith(".mp3") || name.endsWith(".wav")) return "Аудио файл";
        if (name.endsWith(".mp4") || name.endsWith(".avi")) return "Видео файл";
        if (name.endsWith(".zip") || name.endsWith(".rar")) return "Архив";

        return "Файл";
    }

    /**
     * Обработчик очистки для вкладки файлов
     */
    private void onClearFile() {
        // Сбрасываем выбранный файл
        selectedFile = null;
        filePathField.setText("");
        fileInfoLabel.setText("Файл не выбран");
        fileInfoLabel.setForeground(Color.GRAY);

        // Очищаем поле ключа
        fileKeyField.setText("");

        // Сбрасываем алгоритм к значению по умолчанию
        fileAlgorithmComboBox.setSelectedIndex(0);

        // Скрываем выбор размера ключа AES
        fileAesKeySizeComboBox.setVisible(false);

        // Отключаем кнопки операций
        encryptFileBtn.setEnabled(false);
        decryptFileBtn.setEnabled(false);

        // Показываем сообщение
        showInfo("Поля вкладки файлов очищены");
    }

    /**
     * Обработчик шифрования файла
     */
    private void onEncryptFile() {
        if (selectedFile == null) {
            showError("Сначала выберите файл для шифрования");
            return;
        }

        try {
            String algorithm = (String) fileAlgorithmComboBox.getSelectedItem();
            String key = fileKeyField.getText().trim();

            if (key.isEmpty()) {
                showError("Введите ключ для шифрования");
                return;
            }

            // Проверяем, не является ли файл уже зашифрованным
            if (selectedFile.getName().toLowerCase().endsWith(".enc")) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Этот файл уже зашифрован. Вы уверены, что хотите зашифровать его повторно?",
                        "Подтверждение повторного шифрования",
                        JOptionPane.YES_NO_OPTION);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Выполняем шифрование
            File encryptedFile = fileProcessor.encryptFile(selectedFile, algorithm, key);

            // Показываем результат
            showFileOperationSuccess("шифрования", encryptedFile, selectedFile);

        } catch (CryptoException e) {
            showError("Ошибка шифрования: " + e.getMessage());
        } catch (IOException e) {
            showError("Ошибка работы с файлом: " + e.getMessage());
        } catch (Exception e) {
            showError("Неожиданная ошибка: " + e.getMessage());
        }
    }

    /**
     * Копирует текст в буфер обмена (для утилит)
     */
    private void copyTextToClipboard(String text) {
        if (text == null || text.trim().isEmpty()) {
            showError("Нет текста для копирования");
            return;
        }

        try {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        } catch (Exception e) {
            showError("Ошибка при копировании в буфер обмена: " + e.getMessage());
        }
    }

    /**
     * Генерирует AES ключ в Base64 формате
     */
    private String generateAesKey(int keySizeBits) {
        try {
            int keySizeBytes = keySizeBits / 8;
            byte[] key = new byte[keySizeBytes];

            // Криптографически безопасный генератор
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);

            // Кодируем в Base64
            return Base64.getEncoder().encodeToString(key);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации ключа: " + e.getMessage(), e);
        }
    }

    /**
     * Обработчик генерации ключа
     */
    private void onGenerateKey() {
        try {
            // Получаем выбранный размер ключа
            String selectedSize = (String) keySizeComboBox.getSelectedItem();
            int keySizeBits = getKeySizeFromSelection(selectedSize);

            // Генерируем ключ
            String generatedKey = generateAesKey(keySizeBits);

            // Устанавливаем ключ в поле
            generatedKeyField.setText(generatedKey);

            // Копируем в буфер обмена
            copyTextToClipboard(generatedKey);

            // Показываем статус
            showKeyStatus("✅ Ключ скопирован в буфер обмена", Color.GREEN.darker());

        } catch (Exception e) {
            showKeyStatus("❌ Ошибка генерации ключа", Color.RED);
            showError("Ошибка генерации ключа: " + e.getMessage());
        }
    }

    /**
     * Преобразует выбор в размер ключа в битах
     */
    private int getKeySizeFromSelection(String selection) {
        switch (selection) {
            case "128 бит": return 128;
            case "192 бита": return 192;
            case "256 бит": return 256;
            default: return 128; // По умолчанию
        }
    }

    /**
     * Показывает статус генерации ключа
     */
    private void showKeyStatus(String message, Color color) {
        keyStatusLabel.setText(message);
        keyStatusLabel.setForeground(color);

        // Автоматически очищаем статус через 3 секунды
        Timer timer = new Timer(3000, e -> keyStatusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Обработчик очистки генератора ключей
     */
    private void onClearKey() {
        generatedKeyField.setText("");
        keyStatusLabel.setText(" ");
        showInfo("Поле ключа очищено");
    }

    /**
     * Кодирует текст в Base64
     */
    private String encodeToBase64(String text) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка кодирования Base64: " + e.getMessage(), e);
        }
    }

    /**
     * Декодирует текст из Base64
     */
    private String decodeFromBase64(String base64Text) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Text);
            return new String(bytes, "UTF-8");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректный Base64 формат", e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка декодирования Base64: " + e.getMessage(), e);
        }
    }

    /**
     * Обработчик кодирования в Base64
     */
    private void onEncodeBase64() {
        try {
            String inputText = base64InputArea.getText().trim();

            if (inputText.isEmpty()) {
                showError("Введите текст для кодирования");
                return;
            }

            String encodedText = encodeToBase64(inputText);
            base64OutputArea.setText(encodedText);

            showInfo("Текст успешно закодирован в Base64");

        } catch (Exception e) {
            showError("Ошибка кодирования: " + e.getMessage());
        }
    }

    /**
     * Обработчик декодирования из Base64
     */
    private void onDecodeBase64() {
        try {
            String inputText = base64InputArea.getText().trim();

            if (inputText.isEmpty()) {
                showError("Введите Base64 текст для декодирования");
                return;
            }

            String decodedText = decodeFromBase64(inputText);
            base64OutputArea.setText(decodedText);

            showInfo("Текст успешно раскодирован из Base64");

        } catch (Exception e) {
            showError("Ошибка декодирования: " + e.getMessage());
        }
    }

    /**
     * Обработчик очистки Base64 кодера
     */
    private void onClearBase64() {
        base64InputArea.setText("");
        base64OutputArea.setText("");
        showInfo("Поля Base64 кодера очищены");
    }

    /**
     * Обработчик копирования результата Base64
     */
    private void onCopyBase64() {
        String resultText = base64OutputArea.getText().trim();

        if (resultText.isEmpty()) {
            showError("Нет результата для копирования");
            return;
        }

        copyTextToClipboard(resultText);
        showInfo("Результат скопирован в буфер обмена");
    }

    /**
     * Обработчик дешифрования файла
     */
    private void onDecryptFile() {
        if (selectedFile == null) {
            showError("Сначала выберите файл для дешифрования");
            return;
        }

        try {
            String algorithm = (String) fileAlgorithmComboBox.getSelectedItem();
            String key = fileKeyField.getText().trim();

            if (key.isEmpty()) {
                showError("Введите ключ для дешифрования");
                return;
            }

            // Проверяем, является ли файл зашифрованным
            if (!selectedFile.getName().toLowerCase().endsWith(".enc")) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Этот файл не имеет расширения .enc. Вы уверены, что хотите попытаться его дешифровать?",
                        "Подтверждение дешифрования",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Проверяем существование выходного файла
            File outputFile = fileProcessor.restoreOriginalFileName(selectedFile);
            if (outputFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                        "Файл \"" + outputFile.getName() + "\" уже существует. Перезаписать его?",
                        "Подтверждение перезаписи",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Выполняем дешифрование
            File decryptedFile = fileProcessor.decryptFile(selectedFile, algorithm, key);

            // Показываем результат
            showFileOperationSuccess("дешифрования", decryptedFile, selectedFile);

        } catch (CryptoException e) {
            showError("Ошибка дешифрования: " + e.getMessage());
        } catch (IOException e) {
            showError("Ошибка работы с файлом: " + e.getMessage());
        } catch (Exception e) {
            showError("Неожиданная ошибка: " + e.getMessage());
        }
    }

    /**
     * Показывает сообщение об успешной операции с файлом
     */
    private void showFileOperationSuccess(String operation, File resultFile, File originalFile) {
        String originalSize = FileProcessor.formatFileSize(originalFile.length());
        String resultSize = FileProcessor.formatFileSize(resultFile.length());

        String message = String.format("""
        Файл успешно обработан!
        
        Операция: %s
        Исходный файл: %s (%s)
        Результат: %s (%s)
        Путь: %s
        """,
                operation,
                originalFile.getName(),
                originalSize,
                resultFile.getName(),
                resultSize,
                resultFile.getParent()
        );

        JOptionPane.showMessageDialog(this, message, "Операция завершена",
                JOptionPane.INFORMATION_MESSAGE);

        // Обновляем информацию о файле
        updateFileInfo();
    }

    /**
     * Обработчик изменения алгоритма для файлов
     */
    private void onFileAlgorithmChanged() {
        String algorithm = (String) fileAlgorithmComboBox.getSelectedItem();
        boolean isAes = "AES".equals(algorithm);

        // Находим компоненты в панели управления файлами
        Component[] components = ((JPanel)tabbedPane.getComponentAt(1)).getComponents();
        JPanel controlPanel = (JPanel) components[1];

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
        fileAesKeySizeComboBox.setVisible(isAes);

        // Если выбран AES, обновляем требования к ключу
        if (isAes) {
            onFileAesKeySizeChanged();
        }

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    /**
     * Обработчик изменения размера ключа AES для файлов
     */
    private void onFileAesKeySizeChanged() {
        try {
            String selectedSize = (String) fileAesKeySizeComboBox.getSelectedItem();
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
            }
        } catch (CryptoException e) {
            showError("Ошибка настройки AES: " + e.getMessage());
        }
    }

    /**
     * Копирует текст из поля результата в буфер обмена (для текста)
     */
    private void copyToClipboard() {
        String textToCopy = outputTextArea.getText().trim();

        if (textToCopy.isEmpty()) {
            showError("Нет текста для копирования. Сначала выполните шифрование или дешифрование.");
            return;
        }

        copyTextToClipboard(textToCopy);
        showInfo("Текст скопирован в буфер обмена!");
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
    static void main(String[] args) {
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
