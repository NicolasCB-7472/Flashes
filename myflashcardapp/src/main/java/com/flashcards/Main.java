package com.flashcards;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        AppState.getInstance().loadProfile();
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}