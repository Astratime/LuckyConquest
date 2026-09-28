package fr.astratime.lucky.settings;

/** Mode d'affichage de la fenêtre du jeu, réglé dans les options. */
public enum ScreenMode {

    /**
     * Fenêtre agrandie à tout l'écran, avec sa barre de titre (réduire,
     * fermer) et sans cacher la barre des tâches. Mode par défaut.
     */
    WINDOWED("fenêtré"),
    /** Plein écran : le jeu occupe tout l'écran, barre des tâches comprise. */
    FULLSCREEN("plein écran");

    private final String label;

    ScreenMode(String label) {
        this.label = label;
    }

    /** @return le nom du mode, tel qu'affiché dans les options (ex : "plein écran"). */
    public String getLabel() { return label; }

    /** @return l'autre mode. */
    public ScreenMode toggled() {
        return this == WINDOWED ? FULLSCREEN : WINDOWED;
    }

    /** @return le mode nommé {@code name} (voir {@link #name()}), ou le mode par défaut s'il est inconnu. */
    public static ScreenMode parse(String name) {
        for (ScreenMode mode : values()) {
            if (mode.name().equals(name)) return mode;
        }
        return WINDOWED;
    }
}
