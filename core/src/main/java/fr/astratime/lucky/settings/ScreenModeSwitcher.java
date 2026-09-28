package fr.astratime.lucky.settings;

/**
 * Change le mode d'affichage de la fenêtre pendant le jeu. Propre à chaque
 * plateforme (agrandir une fenêtre n'existe que sur ordinateur) : fourni au
 * jeu par son lanceur.
 */
@FunctionalInterface
public interface ScreenModeSwitcher {

    /** Ne fait rien : pour les plateformes (et les tests) sans fenêtre à régler. */
    ScreenModeSwitcher NONE = mode -> { };

    /** Passe la fenêtre du jeu en mode {@code mode}. */
    void apply(ScreenMode mode);
}
