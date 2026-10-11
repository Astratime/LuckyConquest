package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Cartes propres des ennemis : des cartes sombres sans couleur, liées à un de
 * leurs rouleaux (voir {@link EnemyKind#getSignatures()}). Elles sont en
 * {@link #COPIES} exemplaires dans son deck. Il en joue une de chaque sorte
 * piochée, en plus de ses cartes de couleur habituelles. Leur effet vaut :
 * <ul>
 *   <li>{@link Timing#TURN} : pour tous les symboles {@link #getSymbol()} de son tirage du tour ;</li>
 *   <li>{@link Timing#NEXT} : pour le prochain symbole {@link #getSymbol()} qu'il tire, même à un tour suivant ;</li>
 *   <li>{@link Timing#NOW} : tout de suite, une fois la carte jouée.</li>
 * </ul>
 * Plusieurs cartes identiques s'ajoutent.
 */
public enum EnemySignature {

    RONCES("Ronces", EnemySymbol.THORNS, Timing.TURN,
        "Ses Épines de ce tour renvoient +{0} % de tes dégâts", "ÉPINES +{0}%"),
    SAIGNEE("Saignée", EnemySymbol.FANG, Timing.TURN,
        "Ses Crocs de ce tour le soignent de {0} % de plus", "CROC +{0}%"),
    PROVOCATION("Provocation", EnemySymbol.RAGE, Timing.TURN,
        "Chaque Rage de ce tour compte double", "RAGE x2"),
    QUEUE_DE_COMETE("Queue de comète", null, Timing.TURN,
        "Son meilleur symbole du tour agit deux fois", "SYMBOLE x2"),
    DE_CHARGE("Dé chargé", EnemySymbol.LOADED_DIE, Timing.TURN,
        "Ses Dés pipés de ce tour vident {0} % de plus", "DÉ PIPÉ +{0}%"),
    TAUX_USURAIRE("Taux usuraire", EnemySymbol.INTEREST, Timing.TURN,
        "Ses Intérêts de ce tour prennent {0} % de plus", "INTÉRÊTS +{0}%"),
    BILLE_TRUQUEE("Bille truquée", EnemySymbol.ZERO, Timing.NEXT,
        "Son prochain Zéro tombe sur le zéro : ses attaques et ses Boucliers doublent", "ZÉRO ASSURÉ"),
    MAIN_DE_LA_REINE("Main de la Reine", null, Timing.TURN,
        "La Mise royale s'active ce tour : ses Trèfles comptent double", "MISE ROYALE"),
    MIROIR_DEFORMANT("Miroir déformant", EnemySymbol.MIRROR, Timing.NEXT,
        "Son prochain Reflet rejoue ta carte à pleine force", "REFLET x2"),
    REMONTOIR("Remontoir", EnemySymbol.HOURGLASS, Timing.NOW,
        "Son Sablier avance de 1 tout de suite", "SABLIER +1"),
    TOUT_SUR_LE_ROUGE("Tout sur le rouge", EnemySymbol.ALL_IN, Timing.NEXT,
        "Son prochain Tapis tient un coup de plus avant de retomber", "TAPIS SOLIDE"),
    SAC_DE_BUTIN("Sac de butin", null, Timing.TURN,
        "Ses As volés à l'échoppe comptent double ce tour", "AS VOLÉS x2"),
    PLANCHE_A_BILLETS("Planche à billets", EnemySymbol.FAKE_MONEY, Timing.TURN,
        "Sa Fausse monnaie de ce tour touche {0} % de tes gains en plus", "FAUSSE MONNAIE +{0}%"),
    TAROT("Tarot", EnemySymbol.PREDICTION, Timing.NEXT,
        "Sa prochaine Prédiction annonce 2 symboles", "2 PRÉDICTIONS"),
    BOTTE_SECRETE("Botte secrète", EnemySymbol.DUEL, Timing.NEXT,
        "À votre prochain Duel, ta carte compte {0} rangs de moins", "DUEL -{0}"),
    ECLAT_VOLE("Éclat volé", null, Timing.NOW,
        "Il rejoue une carte de la Reine ou de l'Éclat : la Main de la Reine ou le Rouleau interdit", "ÉCLAT VOLÉ"),
    REDRESSEMENT("Redressement", EnemySymbol.TAX, Timing.NEXT,
        "Sa prochaine Taxe dure 2 tours", "TAXE x2 TOURS"),
    REGLEMENT_INTERIEUR("Règlement intérieur", EnemySymbol.NEW_RULE, Timing.NEXT,
        "Sa prochaine Nouvelle règle dure 1 tour de plus", "RÈGLE +1 TOUR"),
    CAMERAS("Caméras", EnemySymbol.FRISK, Timing.NEXT,
        "Sa prochaine Fouille confisque 1 carte de plus", "FOUILLE +1"),
    SAISIE("Saisie", EnemySymbol.BANKRUPTCY, Timing.TURN,
        "Ce tour, le seuil de sa Faillite baisse de {0} points. Jamais sous {1} % de ses PV restants", "SEUIL -{0}"),
    COMPTE_A_REBOURS("Compte à rebours", EnemySymbol.HOURGLASS, Timing.NOW,
        "Le combat perd 1 tour. Une par tour, {0} par combat", "-1 TOUR"),
    DENTS_DE_RAT("Dents de rat", EnemySymbol.NIBBLE, Timing.NEXT,
        "Son prochain Grignotage ronge 2 cartes", "GRIGNOTAGE x2"),
    TOURNEE("Tournée", EnemySymbol.DRUNK, Timing.NEXT,
        "Sa prochaine Ivresse touche 2 rouleaux", "IVRESSE x2"),
    LANTERNE("Lanterne", EnemySymbol.BLIND, Timing.NEXT,
        "Son prochain Aveuglement dure 2 tours", "AVEUGLEMENT x2"),
    GRAPPIN("Grappin", EnemySymbol.BOARDING, Timing.NEXT,
        "Son prochain Abordage vole aussi ta 2e meilleure carte", "ABORDAGE x2"),
    TAMIS_NOIR("Tamis noir", EnemySymbol.NUGGET, Timing.NEXT,
        "Sa prochaine Pépite dure 2 tirages", "PÉPITE x2"),
    MECHE_DE_DIAMANT("Mèche de diamant", EnemySymbol.DRILL, Timing.TURN,
        "Ses Forages de ce tour frappent {0} % plus fort", "FORAGE +{0}%"),
    TREMPE("Trempe", EnemySymbol.ANVIL, Timing.NEXT,
        "Sa prochaine Enclume compte double", "ENCLUME x2"),
    REFRAIN("Refrain", EnemySymbol.SONG, Timing.NEXT,
        "Son prochain Chant joue 2 cartes d'office", "CHANT x2"),
    FRENESIE("Frénésie", EnemySymbol.BANK_BITE, Timing.TURN,
        "Ses Morsures de ce tour dévorent {0} % de tes gains en plus", "MORSURE +{0}%"),
    ENCRE("Encre", EnemySymbol.BLIND, Timing.NOW,
        "Tes cartes en main sont faces cachées au prochain tour", "ENCRE"),

    // Les rouleaux ajoutés aux ennemis qui n'avaient que des rouleaux de base (choix d'Astra).
    ANNONCE("Annonce", EnemySymbol.NOTHING_GOES, Timing.NEXT,
        "Son prochain Rien ne va plus bloque 2 de tes rouleaux", "2 ROULEAUX BLOQUÉS"),
    COUP_DE_RATEAU("Coup de râteau", EnemySymbol.RAKE, Timing.NEXT,
        "Son prochain Râteau ramasse 20 % de tes gains", "RÂTEAU x2"),
    TENUE_EXIGEE("Tenue exigée", EnemySymbol.CLOAKROOM, Timing.NEXT,
        "Son prochain Vestiaire garde 2 de tes cartes", "VESTIAIRE x2"),
    PAS_CE_SOIR("Pas ce soir", EnemySymbol.BOUNCER, Timing.TURN,
        "Ses Videurs de ce tour frappent {0} % plus fort", "VIDEUR +{0}%"),
    MACONNERIE("Maçonnerie", EnemySymbol.LOAD_BEARING, Timing.TURN,
        "Ses Murs porteurs de ce tour agissent deux fois", "MUR PORTEUR x2"),
    DOUBLE_SERRURE("Double serrure", EnemySymbol.SAFE, Timing.TURN,
        "Ses Coffres-forts de ce tour agissent deux fois", "COFFRE-FORT x2"),
    BANQUE_GAGNE("La banque ne saute jamais", EnemySymbol.HOUSE_EDGE, Timing.TURN,
        "Ses Avantages de la maison de ce tour agissent deux fois", "AVANTAGE x2"),
    HUILE_NOIRE("Huile noire", EnemySymbol.GEAR, Timing.TURN,
        "Ses Engrenages de ce tour comptent double", "ENGRENAGES x2"),
    PLUIE_D_ETOILES("Pluie d'étoiles", EnemySymbol.GOLDEN_COMET, Timing.NEXT,
        "Sa prochaine Comète tombe deux fois", "COMÈTE x2"),
    SURTENSION("Surtension", EnemySymbol.SHORT_CIRCUIT, Timing.NEXT,
        "Son prochain Court-circuit touche 2 de tes rouleaux", "COURT-CIRCUIT x2"),
    GRANDE_ASPIRATION("Grande aspiration", EnemySymbol.ASPIRATION, Timing.NEXT,
        "Sa prochaine Aspiration demande une condition de plus", "ASPIRATION +1"),
    OR_MASSIF("Or massif", EnemySymbol.PLATING, Timing.NEXT,
        "Son prochain Placage dure 2 tours", "PLACAGE 2 TOURS"),
    POIDS_DE_L_OR("Poids de l'or", EnemySymbol.INGOT, Timing.TURN,
        "Ses Lingots de ce tour frappent {0} % plus fort", "LINGOT +{0}%"),
    GRONDEMENT("Grondement", EnemySymbol.ROCKFALL, Timing.NEXT,
        "Son prochain Éboulement te retire 2 cartes", "ÉBOULEMENT x2"),
    FILON("Filon", EnemySymbol.HEARTBEAT, Timing.NEXT,
        "Son prochain Battement le soigne deux fois", "BATTEMENT x2"),
    MONNAIE("Monnaie", EnemySymbol.SWALLOWED_COIN, Timing.NEXT,
        "Sa prochaine Pièce avalée compte pour 2", "PIÈCE x2"),
    LEVIER_GRILLE("Levier grillé", EnemySymbol.ARMED_ARM, Timing.NEXT,
        "Son prochain Bras armé relance 2 rouleaux", "BRAS ARMÉ x2");

    /** Exemplaires de chaque carte propre dans le deck d'un ennemi (choix d'Astra). */
    public static final int COPIES = 4;

    /** Ronces : part des dégâts du joueur renvoyée en plus par chaque Épines du tour, en points. */
    public static final int RONCES_PERCENT = 10;
    /** Saignée : soin des Crocs du tour en plus, en %. */
    public static final int SAIGNEE_PERCENT = 50;
    /** Dé chargé : part des jauges vidée en plus par chaque Dé pipé du tour, en points. */
    public static final int DE_CHARGE_PERCENT = 10;
    /** Taux usuraire : part des gains prise en plus par chaque Intérêts du tour, en points. */
    public static final int TAUX_PERCENT = 5;
    /** Planche à billets : part des gains rendue fausse en plus par chaque Fausse monnaie du tour, en points. */
    public static final int PLANCHE_PERCENT = 10;
    /** Botte secrète : rangs retirés à la carte du joueur à son prochain Duel. */
    public static final int BOTTE_RANKS = 3;
    /** Saisie : baisse du seuil de la Faillite, en points de ses PV restants. */
    public static final int SAISIE_POINTS = 10;
    /** La Saisie ne descend jamais le seuil de la Faillite sous ce % de ses PV restants. */
    public static final int SAISIE_FLOOR = 10;
    /** Compte à rebours : tours retirés au combat au plus, sur tout le combat. */
    public static final int COMPTE_MAX = 3;
    /** Mèche de diamant : force en plus des Forages du tour, en %. */
    public static final int MECHE_PERCENT = 25;
    /** Frénésie : part des gains dévorée en plus par chaque Morsure du tour, en points. */
    public static final int FRENESIE_PERCENT = 5;
    /** Pas ce soir, Poids de l'or : force en plus des Videurs, des Lingots du tour, en %. */
    public static final int VIDEUR_PERCENT = 25, LINGOT_PERCENT = 25;

    /** Moment où agit une carte propre. */
    public enum Timing { TURN, NEXT, NOW }

    /** Préfixe de l'identifiant des cartes propres. */
    public static final String ID_PREFIX = "enemy_sig_";

    private final String      displayName;
    private final EnemySymbol symbol;
    private final Timing      timing;
    private final String      description;
    private final String      bonus;

    EnemySignature(String displayName, EnemySymbol symbol, Timing timing, String description, String bonus) {
        this.displayName = displayName;
        this.symbol      = symbol;
        this.timing      = timing;
        this.description = description;
        this.bonus       = bonus;
    }

    /** @return le nom affiché (ex : "Ronces"). */
    public String getDisplayName() { return Lang.t(displayName); }

    /** @return le symbole de ses rouleaux que la carte renforce ou dont elle porte l'image, ou {@code null}. */
    public EnemySymbol getSymbol() { return symbol; }

    /** @return le moment où elle agit. */
    public Timing getTiming() { return timing; }

    /** @return la description de son effet, pour son infobulle. */
    public String getDescription() {
        return switch (this) {
            case RONCES            -> Lang.f(description, RONCES_PERCENT);
            case SAIGNEE           -> Lang.f(description, SAIGNEE_PERCENT);
            case DE_CHARGE         -> Lang.f(description, DE_CHARGE_PERCENT);
            case TAUX_USURAIRE     -> Lang.f(description, TAUX_PERCENT);
            case PLANCHE_A_BILLETS -> Lang.f(description, PLANCHE_PERCENT);
            case BOTTE_SECRETE     -> Lang.f(description, BOTTE_RANKS);
            case SAISIE            -> Lang.f(description, SAISIE_POINTS, SAISIE_FLOOR);
            case COMPTE_A_REBOURS  -> Lang.f(description, COMPTE_MAX);
            case MECHE_DE_DIAMANT  -> Lang.f(description, MECHE_PERCENT);
            case FRENESIE          -> Lang.f(description, FRENESIE_PERCENT);
            case PAS_CE_SOIR       -> Lang.f(description, VIDEUR_PERCENT);
            case POIDS_DE_L_OR     -> Lang.f(description, LINGOT_PERCENT);
            default                -> Lang.t(description);
        };
    }

    /** @return son effet en quelques mots, annoncé quand il la joue (ex : "ÉPINES +10%"). */
    public String getBonusText() {
        return switch (this) {
            case RONCES            -> Lang.f(bonus, RONCES_PERCENT);
            case SAIGNEE           -> Lang.f(bonus, SAIGNEE_PERCENT);
            case DE_CHARGE         -> Lang.f(bonus, DE_CHARGE_PERCENT);
            case TAUX_USURAIRE     -> Lang.f(bonus, TAUX_PERCENT);
            case PLANCHE_A_BILLETS -> Lang.f(bonus, PLANCHE_PERCENT);
            case BOTTE_SECRETE     -> Lang.f(bonus, BOTTE_RANKS);
            case SAISIE            -> Lang.f(bonus, SAISIE_POINTS);
            case MECHE_DE_DIAMANT  -> Lang.f(bonus, MECHE_PERCENT);
            case FRENESIE          -> Lang.f(bonus, FRENESIE_PERCENT);
            case PAS_CE_SOIR       -> Lang.f(bonus, VIDEUR_PERCENT);
            case POIDS_DE_L_OR     -> Lang.f(bonus, LINGOT_PERCENT);
            default                -> Lang.t(bonus);
        };
    }

    /** @return l'identifiant de ses cartes. */
    public String cardId() { return ID_PREFIX + name().toLowerCase(); }

    /** @return le chemin de son image (jeu sombre). */
    public String assetPath() { return "cards/dark/sig/" + name() + ".png"; }

    /** @return une carte neuve de ce type. */
    public Card card() {
        return new Card(cardId(), displayName, assetPath(), List.of(), null, 1);
    }

    /** @return la carte propre {@code card}, ou {@code null} si ce n'en est pas une. */
    public static EnemySignature of(Card card) {
        if (card == null || !card.getId().startsWith(ID_PREFIX)) return null;
        for (EnemySignature signature : values()) {
            if (signature.cardId().equals(card.getId())) return signature;
        }
        return null;
    }
}
