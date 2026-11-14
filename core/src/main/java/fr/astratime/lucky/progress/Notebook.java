package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le carnet du Croupier : une page par boss de la Tour des épreuves et par roi de
 * l'Exploration, laissée la première fois qu'il est vaincu, lisible dans sa fiche
 * du bestiaire. Les pages racontent le monde des machines et mènent au Croupier,
 * le boss secret. Aucun effet de jeu.
 */
public final class Notebook {

    private Notebook() {}

    /** Les pages, dans l'ordre du carnet : la Tour, puis l'Exploration lieu par lieu (textes en français, traduits à l'affichage). */
    private static final Map<EnemyKind, String> PAGES = new LinkedHashMap<>();
    static {
        // ----- Tour des épreuves -----
        page(EnemyKind.COMETE, "La comète n'est pas tombée. Je l'ai lancée. Il fallait une règle au monde, "
            + "j'ai choisi la plus simple : le plus chanceux gagne.");
        page(EnemyKind.REINE, "La Reine a cru que ses tables lui appartenaient. Aucune table n'appartient à "
            + "celui qui s'y assoit. Elles appartiennent à celui qui distribue.");
        page(EnemyKind.ECLAT, "L'Éclat était un morceau de ma comète. Il voulait décider du prochain tirage. "
            + "Il n'a jamais compris que le tirage était déjà décidé.");
        page(EnemyKind.PRETENDANT, "Sans maître, ils se sont tous battus pour le trône. Je les ai regardés faire. "
            + "Un trône vide, c'est la meilleure des mises.");
        page(EnemyKind.MAISON, "La Maison gagne toujours, disent-ils. Ils oublient de demander qui a bâti la Maison.");
        page(EnemyKind.MACHINE_ORIGINELLE, "Ma toute première machine. Chaque fois qu'on tire son levier, le monde "
            + "recommence. Toi, tu l'as tiré. Et je me souviens de chaque tour.");
        // ----- La prairie -----
        page(EnemyKind.ROI_PIQUE, "Les quatre rois de la prairie gardaient mes premières cartes. Le Roi de Pique "
            + "croyait que la lame suffisait. La chance, elle, ne saigne pas.");
        page(EnemyKind.ROI_TREFLE, "Le Roi de Trèfle comptait sur sa bonne étoile. Je lui ai prêté. "
            + "Toute chance prêtée se rembourse, un jour, à la table.");
        page(EnemyKind.ROI_COEUR, "Le Roi de Coeur a donné son sang pour chaque partie. Je lui avais promis qu'il "
            + "gagnerait la dernière. Je ne lui ai pas dit laquelle.");
        page(EnemyKind.ROI_CARREAU, "Le Roi de Carreau gardait son trésor dans un coffre. Le mien tient dans une "
            + "seule carte, et personne ne l'a jamais vue.");
        // ----- La Fête foraine -----
        page(EnemyKind.MONSIEUR_LOYAL, "Monsieur Loyal annonçait mes numéros. Il n'a jamais su que le plus "
            + "beau numéro du cirque, c'était toi.");
        page(EnemyKind.CONDUCTEUR, "Le Conducteur fait tourner son train fantôme depuis le premier tour du monde. "
            + "Il ne sait plus où descendre. Moi non plus.");
        page(EnemyKind.ROUE_FORTUNE, "J'ai fabriqué la Roue de la Fortune un soir d'ennui. Elle tourne toujours. "
            + "C'est le seul jouet qui m'ait jamais surpris.");
        page(EnemyKind.ROI_FORAIN, "Le Roi des Forains vend du rêve à un sou. Il m'en doit des milliers. "
            + "Je ne réclame jamais : j'attends qu'on vienne payer.");
        // ----- Le Port des Contrebandiers -----
        page(EnemyKind.CAPITAINE_RAT, "Le Capitaine Rat a fui chaque naufrage. Il fuira aussi la dernière partie. "
            + "Tous fuient la dernière partie. Sauf toi, peut-être.");
        page(EnemyKind.TAVERNIER, "Le Tavernier remplit les chopes, je remplis les tables. Nous faisons le même métier : "
            + "on garde les joueurs assis.");
        page(EnemyKind.GARDIEN_PHARE, "Le phare montre le port aux bateaux perdus. Je lui ai demandé d'éclairer "
            + "ton chemin. Il l'a fait, sans savoir où il menait.");
        page(EnemyKind.CAPITAINE_NOIR, "Le Capitaine Noir a volé la moitié de mes machines et les a cachées en mer. "
            + "Elles me reviennent toujours. Le hasard aussi revient.");
        // ----- Les Mines d'Or -----
        page(EnemyKind.BARON_OR, "Le Baron croit que l'or fait la chance. C'est l'inverse. "
            + "J'ai semé l'or dans la montagne pour voir qui creuserait.");
        page(EnemyKind.GRAND_FOREUR, "Le Grand Foreur cherche le fond de la mine. Il n'y en a pas. "
            + "Sous la dernière pierre, il y a une autre table.");
        page(EnemyKind.MAITRE_FORGE, "Le Maître de Forge a coulé chaque rouleau de chaque machine. "
            + "Il m'a demandé un jour pour qui. Je lui ai répondu : pour le joueur qui viendra.");
        page(EnemyKind.COEUR_MONTAGNE, "La montagne a un coeur, et ce coeur bat au rythme des rouleaux. "
            + "Quand il s'arrête, j'entends enfin le silence. Je n'aime pas le silence.");
        // ----- Le Casino Englouti -----
        page(EnemyKind.SIRENE, "La Sirène chante pour attirer les joueurs au fond. Je lui ai appris la chanson. "
            + "Tu ne l'as pas écoutée : c'est rare.");
        page(EnemyKind.JACKPOT_VIVANT, "Le Jackpot Vivant est né d'un Bingo que personne n'a ramassé. "
            + "Les gains oubliés finissent toujours par se lever.");
        page(EnemyKind.REQUIN_BANQUIER, "Le Requin prête à tout le monde. Même à moi. "
            + "C'est la seule dette que je n'ai jamais remboursée.");
        page(EnemyKind.KRAKEN, "J'ai englouti mon propre casino pour que personne ne le trouve. Le Kraken le garde. "
            + "Tu l'as trouvé quand même.");
        // ----- Le Désert de Braise -----
        page(EnemyKind.EMIR, "L'Émir compte ses grains de sable comme des jetons. Il en manque toujours un. "
            + "C'est moi qui l'ai pris : c'est mon porte-bonheur.");
        page(EnemyKind.PHARAON, "Le Pharaon s'est fait enterrer avec sa machine. Il pensait jouer encore "
            + "dans l'autre monde. Il n'y a pas d'autre monde. Il n'y a que la table.");
        page(EnemyKind.DJINN, "Le Djinn exauce trois voeux. Je lui en ai demandé un seul : un adversaire à ma hauteur. "
            + "Il m'a montré ton visage.");
        page(EnemyKind.DRAGON, "Ignar dort sur la dernière braise de ma comète. Si tu lis ceci, il ne dort plus. "
            + "Monte au sommet de la Tour une dernière fois, au plus dur. Je t'attendrai derrière ma table.");
    }

    private static void page(EnemyKind kind, String text) { PAGES.put(kind, text); }

    /** @return les ennemis qui laissent une page, dans l'ordre du carnet. */
    public static List<EnemyKind> owners() { return new ArrayList<>(PAGES.keySet()); }

    /** @return le nombre de pages du carnet. */
    public static int size() { return PAGES.size(); }

    /** @return {@code true} si {@code kind} laisse une page du carnet quand il est vaincu. */
    public static boolean hasPage(EnemyKind kind) { return PAGES.containsKey(kind); }

    /** @return le numéro de la page de {@code kind}, à partir de 1 (0 s'il n'en laisse pas). */
    public static int number(EnemyKind kind) { return owners().indexOf(kind) + 1; }

    /** @return le texte de la page de {@code kind}, dans la langue du jeu ({@code null} s'il n'en laisse pas). */
    public static String text(EnemyKind kind) {
        String text = PAGES.get(kind);
        return text == null ? null : Lang.t(text);
    }

    /** @return les pages trouvées par {@code profile} : celles des boss et rois déjà vaincus. */
    public static int found(PlayerProfile profile) {
        int found = 0;
        for (EnemyKind kind : PAGES.keySet()) if (profile.hasDefeated(kind)) found++;
        return found;
    }
}
