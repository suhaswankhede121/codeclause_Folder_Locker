package codeclausefolderlocker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class FolderLocker extends JFrame {
    private JTextField folderPathField;
    private JPasswordField passwordField;
    private JButton encryptButton;
    private JButton decryptButton;

    public FolderLocker() {
        // Set up the JFrame
        super("Folder Locker");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new FlowLayout());

        // Initialize components
        folderPathField = new JTextField(20);
        passwordField = new JPasswordField(20);
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");

        // Add components to the JFrame
        add(new JLabel("Folder Path: "));
        add(folderPathField);
        add(createBrowseButton());
        add(new JLabel("Password: "));
        add(passwordField);
        add(encryptButton);
        add(decryptButton);

        // Encrypt button action listener
        encryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                encryptFolder();
            }
        });

        // Decrypt button action listener
        decryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                decryptFolder();
            }
        });
    }

    // Create a browse button with action listener to select a folder
    private JButton createBrowseButton() {
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(FolderLocker.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFolder = fileChooser.getSelectedFile();
                    folderPathField.setText(selectedFolder.getAbsolutePath());
                }
            }
        });
        return browseButton;
    }

    // Method to encrypt a folder
    private void encryptFolder() {
        String folderPath = folderPathField.getText();
        char[] password = passwordField.getPassword();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a folder!");
            return;
        }
        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a password!");
            return;
        }
        encryptFolder(folderPath, password);
        folderPathField.setText("");
        passwordField.setText("");
    }

    // Method to decrypt a folder
    private void decryptFolder() {
        String folderPath = folderPathField.getText();
        char[] password = passwordField.getPassword();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a folder!");
            return;
        }
        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a password!");
            return;
        }
        decryptFolder(folderPath, password);
        folderPathField.setText("");
        passwordField.setText("");
    }

    // Method to encrypt a folder
    private void encryptFolder(String folderPath, char[] password) {
        try {
            // Validate folder path
            File folder = new File(folderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Invalid folder path!");
                return;
            }

            // Check if the folder is already encrypted
            if (isFolderEncrypted(folder)) {
                JOptionPane.showMessageDialog(this, "Folder is already encrypted!");
                return;
            }

            // Create a temporary encrypted folder
            File encryptedFolder = new File(folder.getParentFile(), folder.getName() + ".encrypted");
            encryptedFolder.mkdir();

            // Encrypt files recursively
            encryptFiles(folder, encryptedFolder, password);

            // Delete the original folder
            deleteFolder(folder);

            // Rename the encrypted folder to the original folder name
            encryptedFolder.renameTo(folder);

            JOptionPane.showMessageDialog(this, "Folder encrypted successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error encrypting folder: " + e.getMessage());
        } finally {
            Arrays.fill(password, ' '); // Clear the password array
        }
    }

    // Method to encrypt files recursively
    private void encryptFiles(File source, File destination, char[] password) throws IOException {
        if (source.isDirectory()) {
            destination.mkdir();
            File[] files = source.listFiles();
            for (File file : files) {
                File encryptedFile = new File(destination, file.getName() + ".encrypted");
                encryptFiles(file, encryptedFile, password);
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) > 0) {
                    // XOR encrypt the bytes with the password
                    for (int i = 0; i < bytesRead; i++) {
                        buffer[i] ^= password[i % password.length];
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    // Method to check if a folder is already encrypted
    private boolean isFolderEncrypted(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".encrypted")) {
                    return true;
                }
            }
        }
        return false;
    }

    // Method to delete a folder and its contents
    private void deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    Files.delete(file.toPath());
                }
            }
        }
        Files.delete(folder.toPath());
    }

    // Method to decrypt a folder
    private void decryptFolder(String folderPath, char[] password) {
        try {
            // Validate folder path
            File folder = new File(folderPath);
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Invalid folder path!");
                return;
            }

            // Check if the folder is already decrypted
            if (!isFolderEncrypted(folder)) {
                JOptionPane.showMessageDialog(this, "Folder is already decrypted!");
                return;
            }

            // Create a temporary decrypted folder
            File decryptedFolder = new File(folder.getParentFile(), folder.getName() + ".decrypted");
            decryptedFolder.mkdir();

            // Decrypt files recursively
            decryptFiles(folder, decryptedFolder, password);

            // Delete the original folder
            deleteFolder(folder);

            // Rename the decrypted folder to the original folder name
            decryptedFolder.renameTo(folder);

            JOptionPane.showMessageDialog(this, "Folder decrypted successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error decrypting folder: " + e.getMessage());
        } finally {
            Arrays.fill(password, ' '); // Clear the password array
        }
    }

    // Method to decrypt files recursively
    private void decryptFiles(File source, File destination, char[] password) throws IOException {
        if (source.isDirectory()) {
            destination.mkdir();
            File[] files = source.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".encrypted")) {
                    String decryptedFileName = fileName.substring(0, fileName.length() - 10);
                    File decryptedFile = new File(destination, decryptedFileName);
                    decryptFiles(file, decryptedFile, password);
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) > 0) {
                    // XOR decrypt the bytes with the password
                    for (int i = 0; i < bytesRead; i++) {
                        buffer[i] ^= password[i % password.length];
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FolderLocker().setVisible(true);
            }
        });
    }
}
