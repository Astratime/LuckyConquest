package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Réactions des ennemis (idée D2 du 2026-11-11) : les rois de l'Exploration et les boss
 * de la Tour disent une courte phrase dans une bulle à trois moments du combat :
 * <ul>
 *   <li>le joueur fait un Bingo ({@link #bingo}, deux phrases qui alternent) ;</li>
 *   <li>ils passent à leur phase suivante ({@link #phase}, une phrase par phase) ;</li>
 *   <li>le joueur tombe sous {@link #LOW_HP_PERCENT} % de ses PV ({@link #lowHp}, une fois par combat).</li>
 * </ul>
 * Aucun effet de jeu. Textes en français, traduits à l'affichage (la police n'a pas de « œ »).
 */
public final class EnemyLines {

    /** Sous cette part de ses PV max (en %), le joueur entend la phrase « tu es presque vaincu ». */
    public static final int LOW_HP_PERCENT = 10;

    /** Les phrases d'un ennemi : ses deux réactions au Bingo, sa phrase sous 10 % et une par phase (2, puis 3). */
    private record Lines(List<String> bingo, String lowHp, List<String> phases) { }

    private static final Map<EnemyKind, Lines> LINES = new EnumMap<>(EnemyKind.class);
    static {
        // ----- Tour des épreuves -----
        say(EnemyKind.COMETE, "Un Bingo ? C'est moi qui ai inventé la règle !",
            "Profite. La chance finit toujours par tomber.",
            "Tu vacilles. Une comète n'attend personne.",
            "Je brûle ! Regarde-moi tomber une seconde fois !");
        say(EnemyKind.REINE, "Joli coup. Ma table s'en souviendra.",
            "Tu triches mieux que moi ? Impossible.",
            "À genoux devant la Reine. C'est l'usage.",
            "Assez joué. Mise royale : tout compte double !");
        say(EnemyKind.PROPHETE, "Un Bingo ? La comète t'a souri. Elle me sourira aussi.",
            "Mes pèlerins ont vu ta chance. Ils la veulent.",
            "Agenouille-toi. La lumière arrive.",
            "La comète reviendra ! Regardez le ciel !");
        say(EnemyKind.ECLAT, "Ce hasard-là m'appartient. Rends-le !",
            "Trois pareils... Le hasard t'aime bien.",
            "Le hasard a choisi. Ce n'est pas toi.",
            "Tu m'as fêlé. Changeons de jeu.");
        say(EnemyKind.COLLECTIONNEUR, "Un Bingo ! Je l'étiquette et je le range.",
            "Joli tirage. Il manquait à ma collection.",
            "Te voilà presque prêt pour ma vitrine.",
            "Une pièce rare ! L'éclat de la Valse, rien que pour toi.",
            "Dernière pièce : le Scellé. Collection complète !");
        say(EnemyKind.PRETENDANT, "Cette chance sera bientôt la mienne.",
            "Tu brilles... Je prendrai ça aussi.",
            "Incline-toi devant ton nouveau maître.",
            "Mon premier éclat ! Tu vas me le payer !",
            "Encore un ! Le maître, c'est moi, pas toi !");
        say(EnemyKind.MAISON, "La Maison note tes gains. Elle les reprendra.",
            "Un Bingo chez moi ? Profite du spectacle.",
            "La Maison gagne toujours. Toujours.",
            "Ma Façade tombe ? Il me reste des murs.",
            "Mon Coffre ! La Maison gagnera quand même !");
        say(EnemyKind.MACHINE_ORIGINELLE, "BINGO DÉTECTÉ. CALCUL DE LA VENGEANCE.",
            "ERREUR : CE TIRAGE N'ÉTAIT PAS PRÉVU.",
            "FIN DE PARTIE IMMINENTE. INSÉREZ UNE PIÈCE.",
            "TES ROULEAUX M'APPARTIENNENT.",
            "ALERTE. ALERTE. JE PRENDS TOUT.");
        say(EnemyKind.GRAND_CROUPIER, "Bien joué. C'est moi qui t'ai appris ça.",
            "Garde tes jetons. Tu en auras besoin.",
            "Faites vos jeux... Rien ne va plus pour toi.",
            "Voyons si tu gagnes sur une table truquée.",
            "Cartes sur table. Plus de secrets entre nous.");
        // ----- La prairie -----
        say(EnemyKind.ROI_PIQUE, "Ta chance ne pare pas mes lames.",
            "Brille tant que tu peux. Ensuite, je frappe.",
            "Une dernière lame, et c'est fini.",
            "Mes lames ont soif. Elles boiront ta vie !");
        say(EnemyKind.ROI_COEUR, "Tant de chance... et si peu de sang.",
            "Garde tes pièces. C'est ton coeur que je veux.",
            "Ton coeur ralentit. Je l'entends.",
            "Tu m'as blessé au coeur. Fureur !");
        say(EnemyKind.ROI_CARREAU, "Ton trésor rejoindra le mien.",
            "Un Bingo ? Mon coffre a de la place.",
            "Tu n'as plus rien à défendre.",
            "Mon trésor rapporte. Toi, tu paies.");
        say(EnemyKind.ROI_TREFLE, "La chance ? C'est ma spécialité.",
            "Joli coup. Ma bonne étoile brille plus fort.",
            "Ta bonne étoile s'éteint.",
            "La table tourne... Trinquons à ta chute !");
        // ----- La Fête foraine -----
        say(EnemyKind.MONSIEUR_LOYAL, "Mesdames et messieurs, un Bingo ! Applaudissez !",
            "Joli numéro. Le mien est meilleur.",
            "Le rideau tombe. Saluez, l'artiste !",
            "Et maintenant, le numéro final !");
        say(EnemyKind.CONDUCTEUR, "Ta chance ne t'emmènera pas loin.",
            "Brille donc. Mon tunnel est très sombre.",
            "Ton billet expire. Ton voyage aussi.",
            "Terminus ! Tout le monde descend !");
        say(EnemyKind.ROUE_FORTUNE, "Gagné ! ... Pour cette fois.",
            "Tourne, tourne... Ça ne durera pas.",
            "Et la roue tombe sur... perdu ! Pour toi.",
            "Je m'emballe ! Plus personne ne m'arrête !");
        say(EnemyKind.ROI_FORAIN, "Un Bingo ? Ce stand était mal réglé.",
            "Tu as gagné une peluche. Bravo.",
            "Perdu ! Rejoue, ce n'est qu'un sou.",
            "Tous mes lots sont truqués. Même celui-là !");
        // ----- Le Rapide de Minuit -----
        say(EnemyKind.LOCOMOTIVE, "Un Bingo ? Ça ne m'arrête pas, moi.",
            "Tu gagnes du terrain. J'en avale des kilomètres.",
            "Terminus pour toi. Le train ne s'arrête pas.",
            "Pression au maximum ! Pleins gaz !");
        say(EnemyKind.GRAND_CHEF, "Un Bingo ? Tu as goûté avant de servir.",
            "Joli plat. Il manque un peu de sel.",
            "Tu es cuit à point. On passe au dessert.",
            "Coup de feu en cuisine ! Tout le monde à son poste !");
        say(EnemyKind.DAME_EN_NOIR, "Un Bingo ? Rêve encore un peu.",
            "Chut. Le wagon dort.",
            "Ferme les yeux. Ce sera plus doux.",
            "Assez de rêves. Regarde-moi, maintenant.");
        say(EnemyKind.DESPERADO, "Un Bingo ? Merci, je le prends.",
            "Tu remplis le coffre. Je le viderai.",
            "Haut les mains ! C'est fini pour toi.",
            "Tout le monde à terre ! C'est un braquage !");
        // ----- Le Palace des Neiges -----
        say(EnemyKind.CONCIERGE, "Un Bingo ? Je le range au coffre de l'hôtel.",
            "Monsieur a de la chance. Monsieur la laissera au vestiaire.",
            "Votre chambre est prête. Pour toujours.",
            "Service d'étage ! Champagne pour tout le monde !");
        say(EnemyKind.IMPERATRICE_GIVRE, "Un Bingo ? La glace garde tout, même la chance.",
            "Tu glisses. Tout le monde glisse sur ma patinoire.",
            "Reste figé ici avec mes autres invités.",
            "Que le bal de glace commence !");
        say(EnemyKind.YETI, "GRRR ! Un Bingo ? La neige l'avalera.",
            "Tu me chatouilles. La neige, elle, ne s'arrête pas.",
            "Encore un joueur sous la neige.",
            "GRAOOOH ! Souffle polaire !");
        say(EnemyKind.PRINCE_NEIGES, "Un Bingo ? Charmant. Trinquons.",
            "Tu danses bien. Personne ne quitte mon bal.",
            "La fête continue sans toi.",
            "Fermez les portes ! Le bal ne finit jamais !");
        // ----- Le Port des Contrebandiers -----
        say(EnemyKind.CAPITAINE_RAT, "Un trésor ! Mes rats vont le ronger.",
            "Couic ! Garde ta chance, je garde ta main.",
            "Tu prends l'eau, matelot !",
            "À l'abordage, mes rats !");
        say(EnemyKind.TAVERNIER, "Une tournée pour le gagnant ! La dernière.",
            "Bingo ? Tu as trop bu, mon gars.",
            "Tu ne tiens plus debout. On ferme !",
            "Bagarre ! Tout le monde dehors !");
        say(EnemyKind.GARDIEN_PHARE, "Ta lumière ne vaut pas la mienne.",
            "Brille ! Je t'aveuglerai quand même.",
            "Je ne vois plus ton navire...",
            "La tempête se lève. Tiens bon la barre !");
        say(EnemyKind.CAPITAINE_NOIR, "Joli butin. Il sera à moi.",
            "Un Bingo ? Ajoute-le à ma rançon.",
            "Fais tes prières, moussaillon.",
            "Feu de tous les canons ! Fumée !");
        // ----- Les Mines d'Or -----
        say(EnemyKind.BARON_OR, "Tout cet or ? Il est déjà à moi.",
            "Tes pièces finiront en cailloux.",
            "Tu ne vaux plus une pépite.",
            "Plus d'or ! Toujours plus d'or !");
        say(EnemyKind.GRAND_FOREUR, "Je creuserai sous ta chance.",
            "Rien ne m'arrête. Pas même un Bingo.",
            "Encore un tour de foret...",
            "La roche vole en éclats !");
        say(EnemyKind.MAITRE_FORGE, "Belle pièce. Je vais la refondre.",
            "Ta chance fond comme du métal.",
            "Le marteau tombe. Pour toujours.",
            "Ma forge s'embrase ! Mon arme grandit !");
        say(EnemyKind.COEUR_MONTAGNE, "La montagne ne craint pas la chance.",
            "Ton Bingo... un caillou de plus.",
            "La montagne t'écrase. Lentement.",
            "Je durcis. Rien ne me fendra.");
        // ----- Le Manoir hanté -----
        say(EnemyKind.COMTE_MINUIT, "Un Bingo ? Ton sang n'en sera que plus doux.",
            "Joli coup. Il est presque minuit, pourtant.",
            "Les douze coups ont sonné pour toi.",
            "Minuit ! L'heure où je ne joue plus pour rire.");
        say(EnemyKind.BIBLIOTHECAIRE, "Un Bingo ? Silence, je l'inscris au registre.",
            "Tu tournes les pages trop vite.",
            "Ton histoire s'arrête ici. Je range le livre.",
            "On éteint les bougies. Lis donc dans le noir.");
        say(EnemyKind.FOSSOYEUR, "Un Bingo ? Je l'enterrerai avec toi.",
            "Creuse, creuse. Ta tombe est déjà prête.",
            "Une pelletée de terre, et on n'en parle plus.",
            "La terre tremble ! Les morts veulent jouer aussi.");
        say(EnemyKind.MARIONNETTISTE, "Un Bingo ? C'est moi qui ai tiré le levier.",
            "Bravo, mon pantin. Encore un tour de piste.",
            "Le spectacle est fini. Je coupe les fils.",
            "Le rideau se lève ! À toi de danser.");
        // ----- Le Casino Englouti -----
        say(EnemyKind.SIRENE, "Quelle chance... Viens plus près.",
            "Écoute mon chant, oublie tes gains.",
            "Laisse-toi couler, mon trésor.",
            "Mon chant devient mortel...");
        say(EnemyKind.JACKPOT_VIVANT, "Un Bingo ? Le Jackpot, c'est MOI !",
            "Ding ding ding ! À mon tour !",
            "Partie terminée. Merci d'avoir joué !",
            "Mise maximale ! Tout ou rien !");
        say(EnemyKind.REQUIN_BANQUIER, "Merci pour le dépôt. Je prends tout.",
            "Tes gains sentent bon. J'ai faim.",
            "Ton compte est à découvert.",
            "L'odeur de l'or ! Frénésie !");
        say(EnemyKind.KRAKEN, "Huit bras pour prendre ta chance.",
            "Un Bingo ? Le fond de la mer s'en moque.",
            "Les abysses t'attendent.",
            "De l'encre ! Tu ne verras plus rien !");
        // ----- Le Temple du Destin -----
        say(EnemyKind.MAITRE_CLOCHES, "Un Bingo ? Les cloches l'avaient annoncé.",
            "Sonne, sonne. Ça ne durera pas.",
            "Écoute bien. C'est ton glas qui sonne.",
            "Le grand bourdon ! Que tout le temple tremble !");
        say(EnemyKind.TISSEUSE, "Un Bingo ? Ce fil-là, je l'avais tissé.",
            "Tu tires sur ton fil. Attention, il casse.",
            "Ton fil est court. Je n'ai plus qu'à couper.",
            "Assez filé. Place aux ciseaux.");
        say(EnemyKind.SEIGNEUR_DES, "Un Bingo ? Les dés t'aiment. Pour l'instant.",
            "Joli coup. Relance, pour voir.",
            "Double six. Comme toujours. Tu as perdu.",
            "Les dés roulent ! Et ils roulent pour moi !");
        say(EnemyKind.IDOLE_OR, "Un Bingo ? Je le garde. Je te le rendrai.",
            "Chaque coup de chance se paie.",
            "Ton karma est lourd. Il t'écrase.",
            "Mon or s'éveille. Contemple ton reflet !");
        // ----- Le Désert de Braise -----
        say(EnemyKind.EMIR, "Tant d'or ! Signe ici, mon ami.",
            "Ta chance a un prix. C'est moi qui le fixe.",
            "Ta dette est trop lourde, mon ami.",
            "Tempête de sable ! Ici, tout se paie.");
        say(EnemyKind.PHARAON, "Cet or appartient au tombeau.",
            "La chance des vivants m'ennuie.",
            "Bientôt, tu dormiras ici avec moi.",
            "Mon sarcophage se ferme. Sois maudit !");
        say(EnemyKind.DJINN, "Ton voeu est exaucé... Pour l'instant.",
            "Un Bingo ? Ce n'est qu'un mirage.",
            "Tu as soif ? Il n'y a plus d'eau.",
            "Voeu exaucé ! Voici la facture.");
        say(EnemyKind.DRAGON, "De l'or ? Mon trésor en a vu d'autres.",
            "Ta chance brûlera comme le reste.",
            "Il ne restera que des cendres.",
            "Je m'envole ! Tout va brûler !");
    }

    private EnemyLines() { }

    private static void say(EnemyKind kind, String bingo1, String bingo2, String lowHp, String... phases) {
        LINES.put(kind, new Lines(List.of(bingo1, bingo2), lowHp, List.of(phases)));
    }

    /** @return les ennemis qui parlent : les rois de l'Exploration et les boss de la Tour (Croupier compris). */
    public static Set<EnemyKind> speakers() { return Collections.unmodifiableSet(LINES.keySet()); }

    /** @return {@code true} si {@code kind} a des réactions. */
    public static boolean speaks(EnemyKind kind) { return kind != null && LINES.containsKey(kind); }

    /**
     * @param count les Bingos du joueur déjà commentés dans ce combat : ses deux phrases alternent
     * @return sa réaction au Bingo du joueur (traduite), ou {@code null} s'il ne parle pas
     */
    public static String bingo(EnemyKind kind, int count) {
        Lines lines = LINES.get(kind);
        return lines == null ? null : Lang.t(lines.bingo().get(Math.floorMod(count, lines.bingo().size())));
    }

    /** @return sa phrase quand le joueur tombe sous {@link #LOW_HP_PERCENT} % de ses PV (traduite), ou {@code null} */
    public static String lowHp(EnemyKind kind) {
        Lines lines = LINES.get(kind);
        return lines == null ? null : Lang.t(lines.lowHp());
    }

    /** @return sa phrase en entrant dans sa phase {@code phase} (2 ou 3, traduite), ou {@code null} s'il n'en a pas */
    public static String phase(EnemyKind kind, int phase) {
        Lines lines = LINES.get(kind);
        if (lines == null || phase < 2 || phase - 2 >= lines.phases().size()) return null;
        return Lang.t(lines.phases().get(phase - 2));
    }

    /** @return le nombre de phrases de phase de {@code kind} (une par phase après la première). */
    public static int phaseLines(EnemyKind kind) {
        Lines lines = LINES.get(kind);
        return lines == null ? 0 : lines.phases().size();
    }

    /** @return toutes ses phrases, en français (tests : traductions, police). */
    public static List<String> all(EnemyKind kind) {
        Lines lines = LINES.get(kind);
        List<String> all = new ArrayList<>();
        if (lines == null) return all;
        all.addAll(lines.bingo());
        all.add(lines.lowHp());
        all.addAll(lines.phases());
        return all;
    }

    /** @return {@code true} si le joueur, à {@code hp} sur {@code maxHp}, est sous {@link #LOW_HP_PERCENT} % de ses PV. */
    public static boolean isLow(long hp, long maxHp) {
        return hp > 0 && hp * 100L < maxHp * (long) LOW_HP_PERCENT;
    }
}
