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

    GENESE("La genèse",
        "Un éclat de lumière frappa le monde entier. Les conflits devinrent des jeux obéissant à une règle : "
            + "celui qui a le plus de chance gagne.",
        EnemyKind.CROUPIER, List.of(EnemyKind.GARDIEN, EnemyKind.SANGSUE, EnemyKind.BRETTEUR), EnemyKind.COMETE,
        null, new Stats(100, 125, 100, 72, 100, 600, 1_200_000L, 1)),
    TABLES_SACREES("Les Tables Sacrées",
        "Les rois tombèrent. Les croupiers prirent leurs trônes. Chaque cité devint une table de jeu. "
            + "Ici, on ne se bat plus. On mise.",
        EnemyKind.CHEF, List.of(EnemyKind.TRICHEUR, EnemyKind.USURIER, EnemyKind.ROULETTE), EnemyKind.REINE,
        null, new Stats(200, 110, 100, 72, 100, 880, 2_500_000L, 1)),
    PELERINS("Les Pèlerins de la Comète",
        "Les Tables Sacrées sont tombées. Alors les foules se mirent en marche vers le cratère, pour toucher la lumière. "
            + "Un prophète les mène. Il dit que la comète reviendra.",
        EnemyKind.PASSEUR, List.of(EnemyKind.PELERIN_FER, EnemyKind.DISEUSE, EnemyKind.PORTEUR), EnemyKind.PROPHETE,
        null, new Stats(250, 30, 120, 71, 300, 1_290, 4_500_000L, 1)),
    DERNIER_TIRAGE("Le Dernier Tirage",
        "La comète n'était pas tombée par hasard. Elle était le hasard. Au fond du cratère, quelque chose bat "
            + "encore. Celui qui le touche décidera du prochain tirage.",
        EnemyKind.GARDIENNE, List.of(EnemyKind.MIROIR, EnemyKind.HORLOGER, EnemyKind.FOU), EnemyKind.ECLAT,
        "La chance n'a plus de maître.", new Stats(300, 40, 167, 70, 500, 1_900, 8_000_000L, 1)),
    ECLATS_DISPERSES("Les Éclats dispersés",
        "L'Éclat Originel s'est brisé. Ses morceaux sont tombés partout. Chacun donne un pouvoir à qui le ramasse. "
            + "Quelqu'un les ramasse tous.",
        EnemyKind.RAMASSEUR, List.of(EnemyKind.HERITIER, EnemyKind.VEUVE, EnemyKind.NOTAIRE), EnemyKind.COLLECTIONNEUR,
        null, new Stats(350, 45, 140, 65, 500, 2_780, 18_000_000L, 1)),
    MONDE_SANS_MAITRE("Le Monde sans Maître",
        "La chance n'avait plus de maître. Alors chacun voulut le devenir. Les tables brûlèrent. "
            + "Les joueurs se battirent pour les miettes de la comète.",
        EnemyKind.PILLEUR, List.of(EnemyKind.FAUSSAIRE, EnemyKind.CARTOMANCIENNE, EnemyKind.DUELLISTE),
        EnemyKind.PRETENDANT, null, new Stats(400, 50, 217, 60, 500, 4_080, 40_000_000L, 1)),
    LA_MAISON("La Maison",
        "Derrière chaque table, il y a une Maison. Derrière chaque Maison, une seule règle. La Maison gagne toujours.",
        EnemyKind.PORTIER, List.of(EnemyKind.COMPTABLE, EnemyKind.DIRECTEUR, EnemyKind.SECURITE), EnemyKind.MAISON,
        null, new Stats(500, 65, 300, 58, 2_000, 8_800, 100_000_000L, 1)),
    L_ASCENSION("L'Ascension",
        "La Maison est vaincue. Il reste à monter la Tour jusqu'à la Machine. Chaque étage a son gardien. "
            + "Tout en haut, celui qui a tout bâti t'attend.",
        EnemyKind.LIFTIER_TOUR, List.of(EnemyKind.ECHO, EnemyKind.VERTIGE, EnemyKind.GARGOUILLE), EnemyKind.ARCHITECTE,
        null, new Stats(550, 110, 270, 55, 3_000, 12_900, 220_000_000L, 1)),
    LE_JACKPOT("Le Jackpot",
        "Au sommet de la Tour, une seule machine. Elle n'a jamais été jouée. On dit qu'elle donne tout. "
            + "On dit aussi qu'elle prend tout.",
        EnemyKind.GARDIEN_LEVIER, List.of(EnemyKind.OMBRE, EnemyKind.BANQUEROUTE, EnemyKind.TEMPS_MORT),
        EnemyKind.MACHINE_ORIGINELLE, "Tu as tiré le levier. Le monde a recommencé.",
        new Stats(600, 125, 167, 52, 5_000, 19_200, 500_000_000L, 3));

    /**
     * Les réglages d'un chapitre, attachés à lui et non à son numéro (2026-11-22, Tour à 10 chapitres : un chapitre
     * inséré ne décale pas les réglages des autres).
     *
     * @param power       force de ses ennemis en mode normal, en % (multiplie leurs attaques, défenses et effets)
     * @param hpPercent   PV de ses ennemis en mode normal, en % de leur fiche (modèle de Puissance, 2026-11-18)
     * @param damage      part de leurs coups qui porte en mode normal, en % (PR #181 : Épée 134 au Dernier Tirage…)
     * @param drainScale  part de leurs coups gardée depuis le drain de vie relatif, en % (2026-11-20)
     * @param spinCost    coût d'un tirage, en gains (mode difficile compris)
     * @param hardPower   force de ses ennemis en mode difficile, en % (x1,47 par chapitre, de la Comète x6 à la
     *                    Machine x192 : plan des 10 chapitres validé par Astra le 2026-11-20)
     * @param hardBossHp  PV de son boss en mode difficile (les autres ennemis gardent leur rapport de PV avec lui)
     * @param ownPlays    cartes propres que ses ennemis jouent au plus à chaque tour (3 au dernier chapitre)
     */
    public record Stats(int power, int hpPercent, int damage, int drainScale, int spinCost, int hardPower,
                        long hardBossHp, int ownPlays) { }

    private final String          title;
    private final String          description;
    private final EnemyKind       firstEnemy;
    private final List<EnemyKind> challengers;
    private final EnemyKind       boss;
    private final String          ending;
    private final Stats           stats;

    Chapter(String title, String description, EnemyKind firstEnemy, List<EnemyKind> challengers,
            EnemyKind boss, String ending, Stats stats) {
        this.title       = title;
        this.description = description;
        this.firstEnemy  = firstEnemy;
        this.challengers = challengers;
        this.boss        = boss;
        this.ending      = ending;
        this.stats       = stats;
    }

    /**
     * @return le chapitre de la Tour où l'on affronte {@code kind} (premier ennemi,
     *         challenger ou boss), ou {@code null} s'il n'est pas de la Tour
     */
    public static Chapter of(EnemyKind kind) {
        for (Chapter chapter : values()) {
            if (chapter.firstEnemy == kind || chapter.boss == kind || chapter.challengers.contains(kind)) return chapter;
        }
        return null;
    }

    /** @return le numéro du chapitre (à partir de 1). */
    public int getNumber() { return ordinal() + 1; }
    /** @return "Chapitre N". */
    public String getLabel() { return Lang.f("Chapitre {0}", getNumber()); }
    /** @return ses réglages (force, PV, dégâts, coût de tirage, mode difficile). */
    public Stats getStats() { return stats; }
    /** @return le dernier chapitre, celui de la Machine Originelle. */
    public static Chapter last() { return values()[values().length - 1]; }
    /**
     * @return la force de ses ennemis en mode difficile en multiplicateur affichable (ex : "8,8"), sans le "x"
     */
    public String hardPowerText() {
        int power = stats.hardPower();
        String text = power % 100 == 0 ? String.valueOf(power / 100) : String.valueOf(power / 100f);
        return Lang.decimal(text);
    }
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
