package com.flashcards;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class AppState {

    private static final AppState INSTANCE = new AppState();

    private static final Path USUARIO_FILE =
            Paths.get(System.getProperty("user.dir"), "Docs", "Usuario.txt");

    private static final String[] AVATAR_TAGS = {"[GEO]", "[INV]", "[LEN]", "[MED]", "[SOF]"};

    private String  selectedDeckPath  = null;
    private boolean randomizarBaraja  = true;
    private int     selectedAvatar    = -1;   // -1 = ninguno
    private String  userName          = "User";

    private AppState() {}

    public static AppState getInstance() {
        return INSTANCE;
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    public void loadProfile() {
        if (!USUARIO_FILE.toFile().exists()) {
            saveProfile();   // crea el archivo con valores por defecto
            return;
        }
        try {
            for (String line : Files.readAllLines(USUARIO_FILE, StandardCharsets.UTF_8)) {
                if (line.startsWith("USUARIO=")) {
                    String val = line.substring("USUARIO=".length()).trim();
                    if (!val.isEmpty()) userName = val;
                } else if (line.startsWith("TAG=")) {
                    String tag = line.substring("TAG=".length()).trim();
                    for (int i = 0; i < AVATAR_TAGS.length; i++) {
                        if (AVATAR_TAGS[i].equals(tag)) { selectedAvatar = i; break; }
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    public void saveProfile() {
        try {
            Files.createDirectories(USUARIO_FILE.getParent());
            String tag = getUserTag().isEmpty() ? "DEFAULT" : getUserTag();
            String content = "USUARIO=" + userName + "\nTAG=" + tag + "\n";
            Files.writeString(USUARIO_FILE, content, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    // ── Deck ──────────────────────────────────────────────────────────────────

    public String getSelectedDeckPath() {
        return selectedDeckPath;
    }

    public void setSelectedDeckPath(String path) {
        this.selectedDeckPath = path;
    }

    public boolean hasSelectedDeck() {
        return selectedDeckPath != null;
    }

    public String getSelectedDeckDisplayName() {
        if (selectedDeckPath == null) return "No hay barajas seleccionadas";
        return Paths.get(selectedDeckPath).getFileName().toString();
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    public boolean isRandomizarBaraja() {
        return randomizarBaraja;
    }

    public void setRandomizarBaraja(boolean randomizarBaraja) {
        this.randomizarBaraja = randomizarBaraja;
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    public int getSelectedAvatar() {
        return selectedAvatar;
    }

    public void setSelectedAvatar(int idx) {
        this.selectedAvatar = idx;
        saveProfile();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
        saveProfile();
    }

    public String getUserTag() {
        return (selectedAvatar >= 0 && selectedAvatar < AVATAR_TAGS.length)
                ? AVATAR_TAGS[selectedAvatar] : "";
    }
}
