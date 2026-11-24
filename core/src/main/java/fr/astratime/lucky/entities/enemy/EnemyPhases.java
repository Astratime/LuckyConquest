package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.i18n.Lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static fr.astratime.lucky.entities.enemy.EnemyKind.*;

/**
 * Les phases des boss et des rois (voir {@link EnemyKind#phaseFor(float)}) : leurs seuils, le texte qui les annonce et
 * les symboles de leurs rouleaux à chaque phase.
 */
final class EnemyPhases {

    private EnemyPhases() { }

    /** Symboles de l'Éclat Originel dans sa deuxième phase : ceux du chapitre 3. */
    private static final Map<EnemySymbol, Integer> ECLAT_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SHIELD, 3, EnemySymbol.MIRROR, 1, EnemySymbol.HOURGLASS, 1, EnemySymbol.ALL_IN, 1));

    /**
     * Symboles du Prophète du Cratère dans sa deuxième phase (valeurs par défaut de Claude) : il appelle la Comète Dorée,
     * qu'il a vue tomber, et ses pèlerins prêtent serment.
     */
    private static final Map<EnemySymbol, Integer> PROPHET_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.PILGRIMAGE, 2, EnemySymbol.SWORD, 1, EnemySymbol.SHIELD, 1, EnemySymbol.GOLDEN_COMET, 1,
        EnemySymbol.IRON_VOW, 1));

    /**
     * Symboles de la Maison sans sa Façade (plus de Boucliers ni de Mur porteur), puis sans
     * son Coffre (plus de Potions ni de Coffre-fort) : la Salle de jeu garde son Avantage.
     */
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 2, EnemySymbol.POTION, 1, EnemySymbol.SAFE, 1, EnemySymbol.HOUSE_EDGE, 1));
    private static final Map<EnemySymbol, Integer> HOUSE_PHASE_THREE = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 2, EnemySymbol.HOUSE_EDGE, 1));
    /** Le Général Va-Tout, sous les deux tiers de ses PV : ses lieutenants le rejoignent (l'Espionne et le Mercenaire). */
    private static final Map<EnemySymbol, Integer> GENERAL_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 2, EnemySymbol.POTION, 1, EnemySymbol.RALLY, 1, EnemySymbol.CANNON, 1,
        EnemySymbol.SPY, 1, EnemySymbol.CONTRACT, 1, EnemySymbol.ALL_OUT, 1));
    /** Le Général Va-Tout, sous le tiers de ses PV : il fait Va-tout, plus de Bouclier ni de Potion, tout sur l'attaque. */
    private static final Map<EnemySymbol, Integer> GENERAL_PHASE_THREE = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 3, EnemySymbol.RALLY, 1, EnemySymbol.CANNON, 1, EnemySymbol.CONTRACT, 1, EnemySymbol.ALL_OUT, 2));

    /**
     * Symboles du Collectionneur à ses phases 2 et 3 : à chaque tiers de ses PV perdu, il sort un nouvel éclat
     * de sa vitrine, et gagne son pouvoir (la Valse, puis le Scellé), en plus des siens.
     */
    private static final Map<EnemySymbol, Integer> COLLECTOR_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 1, EnemySymbol.LEGACY, 1,
        EnemySymbol.WALTZ, 1));
    private static final Map<EnemySymbol, Integer> COLLECTOR_PHASE_THREE = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 2, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.SHARD, 1, EnemySymbol.LEGACY, 1,
        EnemySymbol.WALTZ, 1, EnemySymbol.SEAL, 1));

    /**
     * Symboles de l'Architecte : ses échafaudages montent (phase 2, l'Écho de l'Ascension), puis la clé de voûte
     * (phase 3, le Vertige) ; il garde ses Plans.
     */
    private static final Map<EnemySymbol, Integer> ARCHITECT_PHASE_TWO = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.BLUEPRINT, 2, EnemySymbol.ECHO, 1));
    private static final Map<EnemySymbol, Integer> ARCHITECT_PHASE_THREE = Collections.unmodifiableMap(EnemyKind.weights(
        EnemySymbol.SWORD, 3, EnemySymbol.SHIELD, 1, EnemySymbol.POTION, 1, EnemySymbol.BLUEPRINT, 2, EnemySymbol.ECHO, 1,
        EnemySymbol.VERTIGO, 1));

    /**
     * Seuils de phase (choix d'Astra) : la part de ses PV max sous laquelle il ne descend
     * pas avant d'être passé à la phase suivante, à son tour. Le surplus d'un coup qui
     * franchit un seuil est perdu. Tous les boss et rois en ont : la moitié de leurs PV ;
     * le Prétendant et la Maison, les deux tiers puis le tiers ; la Machine Originelle,
     * la moitié puis {@link EnemyKind#MACHINE_LAST_RATIO}.
     *
     * @return ses seuils, de la phase 1 à la dernière (vide s'il n'a qu'une phase)
     */
    static float[] thresholds(EnemyKind kind) {
        if (!kind.isBoss()) return new float[0];
        return switch (kind) {
            case COLLECTIONNEUR, PRETENDANT, GENERAL, MAISON, ARCHITECTE -> new float[] {2f / 3f, 1f / 3f};
            case MACHINE_ORIGINELLE, GRAND_CROUPIER -> new float[] {PHASE_TWO_RATIO, MACHINE_LAST_RATIO};
            default -> new float[] {PHASE_TWO_RATIO};
        };
    }


    /**
     * @return le symbole qu'un roi de l'Exploration ajoute à ses rouleaux en phase 2, sous
     *         {@link EnemyKind#PHASE_TWO_RATIO} de vie (poids {@link EnemyKind#KING_PHASE_WEIGHT}), ou {@code null}
     */
    static EnemySymbol kingSymbol(EnemyKind kind) {
        return switch (kind) {
            case ROI_PIQUE, SIRENE, BARON_OR -> EnemySymbol.FANG;
            case ROI_COEUR, TAVERNIER, GARDIEN_PHARE, MAITRE_FORGE, REQUIN_BANQUIER -> EnemySymbol.RAGE;
            case ROI_CARREAU, JACKPOT_VIVANT -> EnemySymbol.INTEREST;
            case ROI_TREFLE -> EnemySymbol.DRUNK;
            case CAPITAINE_RAT -> EnemySymbol.BOARDING;
            case CAPITAINE_NOIR, KRAKEN -> EnemySymbol.BLIND;
            case GRAND_FOREUR -> EnemySymbol.THORNS;
            case COEUR_MONTAGNE -> EnemySymbol.ANVIL;
            case MONSIEUR_LOYAL -> EnemySymbol.JUGGLE;
            case CONDUCTEUR -> EnemySymbol.SCARE;
            case ROUE_FORTUNE -> EnemySymbol.TURNSTILE;
            case ROI_FORAIN -> EnemySymbol.RIGGED_PRIZE;
            case EMIR -> EnemySymbol.TOLL;
            case PHARAON -> EnemySymbol.VENOM;
            case DJINN -> EnemySymbol.BILL;
            case DRAGON -> EnemySymbol.BURN;
            case LOCOMOTIVE -> EnemySymbol.BURN;
            case GRAND_CHEF -> EnemySymbol.FANG;
            case DAME_EN_NOIR -> EnemySymbol.VENOM;
            case DESPERADO -> EnemySymbol.BOARDING;
            case CONCIERGE -> EnemySymbol.CHAMPAGNE;
            case IMPERATRICE_GIVRE -> EnemySymbol.CHAMPAGNE;
            case YETI -> EnemySymbol.FREEZE;
            case PRINCE_NEIGES -> EnemySymbol.KEY;
            case MAITRE_CLOCHES -> EnemySymbol.DRUNK;
            case TISSEUSE -> EnemySymbol.PREDICTION;
            case SEIGNEUR_DES -> EnemySymbol.LOADED_DIE;
            case IDOLE_OR -> EnemySymbol.MIRROR;
            case COMTE_MINUIT -> EnemySymbol.FANG;
            case BIBLIOTHECAIRE -> EnemySymbol.BLIND;
            case FOSSOYEUR -> EnemySymbol.ROCKFALL;
            case MARIONNETTISTE -> EnemySymbol.SONG;
            default -> null;
        };
    }


    /**
     * @return sa phase quand il lui reste {@code hpRatio} de ses PV : l'Éclat
     *         Originel, la Comète Dorée et la Reine des Tables (sa Mise royale) passent à 2 sous la moitié ; le Prétendant perd un éclat et
     *         la Maison une de ses parties à chaque tiers ; la Machine
     *         Originelle vole un rouleau sous la moitié, puis un autre sous
     *         {@link EnemyKind#MACHINE_LAST_RATIO} ; les rois de l'Exploration passent à 2
     *         sous la moitié. Les autres restent en phase 1.
     */
    static int phaseFor(EnemyKind kind, float hpRatio) {
        return switch (kind) {
            case ECLAT, COMETE, REINE -> hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            case COLLECTIONNEUR, PRETENDANT, GENERAL, MAISON, ARCHITECTE -> hpRatio * 3f <= 1f ? 3 : hpRatio * 3f <= 2f ? 2 : 1;
            case MACHINE_ORIGINELLE, GRAND_CROUPIER -> hpRatio < MACHINE_LAST_RATIO ? 3 : hpRatio < PHASE_TWO_RATIO ? 2 : 1;
            default -> kingSymbol(kind) != null && hpRatio < PHASE_TWO_RATIO ? 2 : 1;
        };
    }


    /** @return le texte qui annonce sa phase {@code phase}, quand il y entre. */
    static String text(EnemyKind kind, int phase) {
        return switch (kind) {
            case PRETENDANT -> phase == 2 ? Lang.t("ÉCLAT PERDU : FINI LE ROULEAU INTERDIT") : Lang.t("ÉCLAT PERDU : FINIE LA MISE ROYALE");
            case COLLECTIONNEUR -> phase == 2 ? Lang.t("NOUVEL ÉCLAT : VALSE !") : Lang.t("NOUVEL ÉCLAT : SCELLÉ !");
            case MAISON -> phase == 2 ? Lang.t("LA FAÇADE TOMBE : PLUS DE DÉFENSE") : Lang.t("LE COFFRE TOMBE : PLUS DE SOINS");
            case ARCHITECTE -> phase == 2 ? Lang.t("LES ÉCHAFAUDAGES MONTENT : ÉCHO !")
                : Lang.t("LA CLÉ DE VOÛTE : SA TOUR FRAPPE, VERTIGE !");
            case MACHINE_ORIGINELLE -> Lang.t("ELLE TE VOLE UN ROULEAU !");
            case GRAND_CROUPIER -> phase == 2 ? Lang.t("IL TRUQUE TA TABLE !") : Lang.t("CARTES SUR TABLE !");
            case COMETE -> Lang.t("LA COMÈTE S'EMBRASE !");
            case GENERAL -> phase == 2 ? Lang.t("SES LIEUTENANTS ARRIVENT : ESPIONNAGE ET CONTRAT !")
                : Lang.t("VA-TOUT : PLUS DE DÉFENSE, TOUT SUR L'ATTAQUE !");
            case PROPHETE -> Lang.t("LA COMÈTE RÉPOND À SON APPEL : COMÈTE !");
            case REINE -> Lang.t("MISE ROYALE : SES TRÈFLES COMPTENT DOUBLE !");
            case ECLAT -> Lang.t("L'ÉCLAT SE FEND : NOUVEAUX SYMBOLES !");
            case ROI_PIQUE -> Lang.t("LAMES ASSOIFFÉES : CROC !");
            case ROI_COEUR -> Lang.t("COEUR FURIEUX : RAGE !");
            case ROI_CARREAU -> Lang.t("LE TRÉSOR RAPPORTE : INTÉRÊTS !");
            case ROI_TREFLE -> Lang.t("LA TABLE TOURNE : IVRESSE !");
            case CAPITAINE_RAT -> Lang.t("LES RATS MONTENT À BORD : ABORDAGE !");
            case TAVERNIER -> Lang.t("BAGARRE DE TAVERNE : RAGE !");
            case GARDIEN_PHARE -> Lang.t("LA TEMPÊTE SE LÈVE : RAGE !");
            case CAPITAINE_NOIR -> Lang.t("BORDÉE DE FUMÉE : AVEUGLEMENT !");
            case BARON_OR -> Lang.t("AVIDITÉ : CROC !");
            case GRAND_FOREUR -> Lang.t("ÉCLATS DE ROCHE : ÉPINES !");
            case MAITRE_FORGE -> Lang.t("FORGE ARDENTE : RAGE !");
            case COEUR_MONTAGNE -> Lang.t("LE COEUR DURCIT : ENCLUME !");
            case SIRENE -> Lang.t("CHANT MORTEL : CROC !");
            case JACKPOT_VIVANT -> Lang.t("MISE MAXIMALE : INTÉRÊTS !");
            case REQUIN_BANQUIER -> Lang.t("FRÉNÉSIE : RAGE !");
            case KRAKEN -> Lang.t("NUAGE D'ENCRE : AVEUGLEMENT !");
            case MONSIEUR_LOYAL -> Lang.t("NUMÉRO FINAL : JONGLAGE !");
            case CONDUCTEUR -> Lang.t("TERMINUS : FRAYEUR !");
            case ROUE_FORTUNE -> Lang.t("LA ROUE S'EMBALLE : TOURNIQUET !");
            case ROI_FORAIN -> Lang.t("TOUT EST TRUQUÉ : LOT TRUQUÉ !");
            case EMIR -> Lang.t("TEMPÊTE DE SABLE : PÉAGE !");
            case PHARAON -> Lang.t("LE SARCOPHAGE SE FERME : VENIN !");
            case DJINN -> Lang.t("VOEU EXAUCÉ : LETTRE DE CHANGE !");
            case DRAGON -> Lang.t("ENVOL : BRÛLURE !");
            case LOCOMOTIVE -> Lang.t("VAPEUR BRÛLANTE : BRÛLURE !");
            case GRAND_CHEF -> Lang.t("COUP DE FEU EN CUISINE : CROC !");
            case DAME_EN_NOIR -> Lang.t("LA VOILETTE SE LÈVE : VENIN !");
            case DESPERADO -> Lang.t("TOUT LE MONDE À TERRE : ABORDAGE !");
            case CONCIERGE -> Lang.t("SERVICE D'ÉTAGE : CHAMPAGNE GLACÉ !");
            case IMPERATRICE_GIVRE -> Lang.t("LE BAL DE GLACE COMMENCE : CHAMPAGNE GLACÉ !");
            case YETI -> Lang.t("SOUFFLE POLAIRE : GEL !");
            case PRINCE_NEIGES -> Lang.t("LES PORTES DU BAL SE FERMENT : CLÉ !");
            case MAITRE_CLOCHES -> Lang.t("LE BOURDON TE SAOULE : IVRESSE !");
            case TISSEUSE -> Lang.t("ELLE LIT TON FIL : PRÉDICTION !");
            case SEIGNEUR_DES -> Lang.t("LES DÉS SONT PIPÉS : DÉ PIPÉ !");
            case IDOLE_OR -> Lang.t("SON OR TE RENVOIE TON IMAGE : REFLET !");
            case COMTE_MINUIT -> Lang.t("LES DOUZE COUPS SONNENT : CROC !");
            case BIBLIOTHECAIRE -> Lang.t("LES BOUGIES S'ÉTEIGNENT : AVEUGLEMENT !");
            case FOSSOYEUR -> Lang.t("LA TERRE TREMBLE : ÉBOULEMENT !");
            case MARIONNETTISTE -> Lang.t("LE RIDEAU SE LÈVE : CHANT !");
            default -> Lang.f("PHASE {0} !", phase);
        };
    }


    /** @return les symboles de ses rouleaux et leur poids dans la phase {@code phase} (1 ou 2). */
    static Map<EnemySymbol, Integer> weights(EnemyKind kind, int phase) {
        if (phase >= 2 && kind == ECLAT) return ECLAT_PHASE_TWO;
        if (phase >= 2 && kind == PROPHETE) return PROPHET_PHASE_TWO;
        if (phase >= 2 && kingSymbol(kind) != null) {
            Map<EnemySymbol, Integer> king = new LinkedHashMap<>(kind.getWeights());
            king.merge(kingSymbol(kind), KING_PHASE_WEIGHT, Integer::sum);
            return Collections.unmodifiableMap(king);
        }
        if (kind == ARCHITECTE && phase >= 3) return ARCHITECT_PHASE_THREE;
        if (kind == ARCHITECTE && phase == 2) return ARCHITECT_PHASE_TWO;
        if (kind == COLLECTIONNEUR && phase >= 3) return COLLECTOR_PHASE_THREE;
        if (kind == COLLECTIONNEUR && phase == 2) return COLLECTOR_PHASE_TWO;
        if (kind == GENERAL && phase >= 3) return GENERAL_PHASE_THREE;
        if (kind == GENERAL && phase == 2) return GENERAL_PHASE_TWO;
        if (kind == MAISON && phase >= 3) return HOUSE_PHASE_THREE;
        if (kind == MAISON && phase == 2) return HOUSE_PHASE_TWO;
        return kind.getWeights();
    }
}
