package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

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
        "La chance n'a plus de maître."),
    MONDE_SANS_MAITRE(4, "Le Monde sans Maître",
        "La chance n'avait plus de maître. Alors chacun voulut le devenir. Les tables brûlèrent. "
            + "Les joueurs se battirent pour les miettes de la comète.",
        EnemyKind.PILLEUR, List.of(EnemyKind.FAUSSAIRE, EnemyKind.CARTOMANCIENNE, EnemyKind.DUELLISTE),
        EnemyKind.PRETENDANT, null),
    LA_MAISON(5, "La Maison",
        "Derrière chaque table, il y a une Maison. Derrière chaque Maison, une seule règle. La Maison gagne toujours.",
        EnemyKind.PORTIER, List.of(EnemyKind.COMPTABLE, EnemyKind.DIRECTEUR, EnemyKind.SECURITE), EnemyKind.MAISON,
        null),
    LE_JACKPOT(6, "Le Jackpot",
        "Au sommet de la Tour, une seule machine. Elle n'a jamais été jouée. On dit qu'elle donne tout. "
            + "On dit aussi qu'elle prend tout.",
        EnemyKind.GARDIEN_LEVIER, List.of(EnemyKind.OMBRE, EnemyKind.BANQUEROUTE, EnemyKind.TEMPS_MORT),
        EnemyKind.MACHINE_ORIGINELLE, "Tu as tiré le levier. Le monde a recommencé.");

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
    public String getLabel() { return Lang.f("Chapitre {0}", number); }
    /** @return le titre du chapitre (ex : "La genèse"). */
    public String getTitle() { return Lang.t(title); }
    /** @return le récit du chapitre. */
    public String getDescription() { return Lang.t(description); }
    /** @return le chapitre d'avant, à terminer pour ouvrir celui-ci ({@code null} pour le premier). */
    public Chapter getPrevious() { return ordinal() == 0 ? null : values()[ordinal() - 1]; }
    /** @return l'ennemi du premier combat. */
    public EnemyKind getFirstEnemy() { return firstEnemy; }
    /** @return les trois adversaires cachés sous les cartes du 2e combat. */
    public List<EnemyKind> getChallengers() { return challengers; }
    /** @return le boss du chapitre. */
    public EnemyKind getBoss() { return boss; }
    /** @return la phrase de fin, quand ce chapitre clôt l'histoire ({@code null} sinon). */
    public String getEnding() { return Lang.t(ending); }
}
