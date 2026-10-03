package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.List;

/**
 * Chapitres de la Tour des épreuves. Chacun enchaîne trois combats (voir
 * {@link TowerRun}) : son premier ennemi, l'adversaire choisi parmi ses trois
 * challengers, puis son boss.
 */
public enum Chapter {

    GENESE(1, "La genèse",
        "Un éclat de lumière frappa le monde entier. Les conflits devinrent des jeux obéissant à une règle : "
            + "celui qui a le plus de chance gagne.",
        EnemyKind.CROUPIER, List.of(EnemyKind.GARDIEN, EnemyKind.SANGSUE, EnemyKind.BRETTEUR), EnemyKind.COMETE,
        null),
    TABLES_SACREES(2, "Les Tables Sacrées",
        "Les rois tombèrent. Les croupiers prirent leurs trônes. Chaque cité devint une table de jeu. "
            + "Ici, on ne se bat plus. On mise.",
        EnemyKind.CHEF, List.of(EnemyKind.TRICHEUR, EnemyKind.USURIER, EnemyKind.ROULETTE), EnemyKind.REINE,
        null),
    DERNIER_TIRAGE(3, "Le Dernier Tirage",
        "La comète n'était pas tombée par hasard. Elle était le hasard. Au fond du cratère, quelque chose bat "
            + "encore. Celui qui le touche décidera du prochain tirage.",
        EnemyKind.GARDIENNE, List.of(EnemyKind.MIROIR, EnemyKind.HORLOGER, EnemyKind.FOU), EnemyKind.ECLAT,
        "La chance n'a plus de maître.");

    private final int             number;
    private final String          title;
    private final String          description;
    private final EnemyKind       firstEnemy;
    private final List<EnemyKind> challengers;
    private final EnemyKind       boss;
    private final String          ending;

    Chapter(int number, String title, String description, EnemyKind firstEnemy, List<EnemyKind> challengers,
            EnemyKind boss, String ending) {
        this.number      = number;
        this.title       = title;
        this.description = description;
        this.firstEnemy  = firstEnemy;
        this.challengers = challengers;
        this.boss        = boss;
        this.ending      = ending;
    }

    /** @return le numéro du chapitre (à partir de 1). */
    public int getNumber() { return number; }
    /** @return "Chapitre N". */
    public String getLabel() { return "Chapitre " + number; }
    /** @return le titre du chapitre (ex : "La genèse"). */
    public String getTitle() { return title; }
    /** @return le récit du chapitre. */
    public String getDescription() { return description; }
    /** @return {@code true} si le chapitre peut être joué. */
    public boolean isOpen() { return true; }
    /** @return l'ennemi du premier combat. */
    public EnemyKind getFirstEnemy() { return firstEnemy; }
    /** @return les trois adversaires cachés sous les cartes du 2e combat. */
    public List<EnemyKind> getChallengers() { return challengers; }
    /** @return le boss du chapitre. */
    public EnemyKind getBoss() { return boss; }
    /** @return la phrase de fin, quand ce chapitre clôt l'histoire ({@code null} sinon). */
    public String getEnding() { return ending; }
}
