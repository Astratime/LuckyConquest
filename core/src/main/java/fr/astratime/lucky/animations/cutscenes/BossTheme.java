package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;

/**
 * Le décor des cinématiques de phase et de mort d'un boss ({@link BossMomentCutscene}) :
 * le ciel et le sol de son chapitre ou de son lieu, sa couleur, le symbole qui
 * l'accompagne et sa dernière phrase.
 *
 * @param skyTop    haut du ciel
 * @param skyLow    bas du ciel
 * @param ground    le sol
 * @param accent    sa couleur : lueur, étincelles, onde de choc
 * @param death     le symbole de ses machines qui vole avec ses éclats à sa mort
 * @param deathLine sa dernière phrase (clé française, traduite à l'affichage)
 */
public record BossTheme(Color skyTop, Color skyLow, Color ground, Color accent, EnemySymbol death, String deathLine) {

    /** @return sa dernière phrase, dans la langue du jeu. */
    public String deathLine() { return Lang.t(deathLine); }

    /** @return le symbole de ses machines qui accompagne son passage à la phase {@code phase}. */
    public EnemySymbol phaseSymbol(EnemyKind kind, int phase) {
        if (kind.kingPhaseSymbol() != null) return kind.kingPhaseSymbol(); // le symbole qu'il gagne
        return switch (kind) {
            case COMETE -> EnemySymbol.GOLDEN_COMET;
            case REINE -> EnemySymbol.LOADED_DIE;
            case ECLAT -> EnemySymbol.HOURGLASS;
            case PRETENDANT -> phase == 2 ? EnemySymbol.MIRROR : EnemySymbol.LOADED_DIE; // l'éclat de l'Éclat, puis celui de la Reine
            case MAISON -> phase == 2 ? EnemySymbol.LOAD_BEARING : EnemySymbol.SAFE;    // la façade, puis le coffre
            case MACHINE_ORIGINELLE -> EnemySymbol.GEAR;
            default -> death;
        };
    }

    /** @return le décor du boss ou du roi {@code kind}. */
    public static BossTheme of(EnemyKind kind) {
        return switch (kind) {
            // ----- Tour des épreuves -----
            case COMETE -> tower("05071a", "2b1a4a", "0e0a12", "ffc93a", EnemySymbol.GOLDEN_COMET, "LA COMÈTE S'ÉTEINT...");
            case REINE -> tower("1a0612", "5a0f2a", "12040c", "ff3a5a", EnemySymbol.LOADED_DIE, "LA REINE PERD SA COURONNE");
            case ECLAT -> tower("080614", "2a1650", "0a0814", "7ae8ff", EnemySymbol.MIRROR, "L'ÉCLAT VOLE EN MILLE MORCEAUX");
            case PRETENDANT -> tower("06080f", "1c2440", "080a12", "c8d4ff", EnemySymbol.PREDICTION, "LE PRÉTENDANT N'AURA JAMAIS LE TRÔNE");
            case MAISON -> tower("120606", "4a0e0e", "140606", "ffb02a", EnemySymbol.HOUSE_EDGE, "LA MAISON A PERDU !");
            case MACHINE_ORIGINELLE -> tower("050505", "2a1a08", "0a0804", "ff4a1a", EnemySymbol.GEAR, "LA MACHINE ORIGINELLE S'ARRÊTE");
            // ----- La prairie -----
            case ROI_PIQUE -> king(kind, "6a8ad8", EnemySymbol.SWORD, "LE ROI DE PIQUE BRISE SON ÉPÉE");
            case ROI_TREFLE -> king(kind, "3fbf5a", EnemySymbol.INTEREST, "LE ROI DE TRÈFLE A ÉPUISÉ SA CHANCE");
            case ROI_COEUR -> king(kind, "e0283a", EnemySymbol.HEARTBEAT, "LE COEUR DU ROI CESSE DE BATTRE");
            case ROI_CARREAU -> king(kind, "ff9a2a", EnemySymbol.NUGGET, "LE ROI DE CARREAU LÂCHE SON TRÉSOR");
            // ----- Le Port des Contrebandiers -----
            case CAPITAINE_RAT -> king(kind, "c8c080", EnemySymbol.NIBBLE, "LE CAPITAINE RAT QUITTE LE NAVIRE");
            case TAVERNIER -> king(kind, "ff9a3a", EnemySymbol.DRUNK, "LA TAVERNE FERME SES PORTES");
            case GARDIEN_PHARE -> king(kind, "fff07a", EnemySymbol.BLIND, "LE PHARE S'ÉTEINT");
            case CAPITAINE_NOIR -> king(kind, "9a5aff", EnemySymbol.BOARDING, "LE CAPITAINE NOIR SOMBRE AVEC SON NAVIRE");
            // ----- Les Mines d'Or -----
            case BARON_OR -> king(kind, "ffd23a", EnemySymbol.INGOT, "LE BARON PERD SON OR");
            case GRAND_FOREUR -> king(kind, "c8a070", EnemySymbol.DRILL, "LA FOREUSE SE TAIT");
            case MAITRE_FORGE -> king(kind, "ff5a1a", EnemySymbol.ANVIL, "LA FORGE REFROIDIT");
            case COEUR_MONTAGNE -> king(kind, "ff3a3a", EnemySymbol.ROCKFALL, "LA MONTAGNE S'EFFONDRE");
            // ----- Le Casino Englouti -----
            case SIRENE -> king(kind, "ff7ad8", EnemySymbol.SONG, "LE CHANT DE LA SIRÈNE SE TAIT");
            case JACKPOT_VIVANT -> king(kind, "ffd23a", EnemySymbol.SWALLOWED_COIN, "LE JACKPOT EST VIDÉ !");
            case REQUIN_BANQUIER -> king(kind, "7ad0ff", EnemySymbol.BANK_BITE, "LA BANQUE DU REQUIN FAIT FAILLITE");
            case KRAKEN -> king(kind, "b05aff", EnemySymbol.ARMED_ARM, "LE KRAKEN RETOURNE AUX ABYSSES");
            default -> tower("05071a", "2b1a4a", "0e0a12", "ffc93a", EnemySymbol.SWORD, "VAINCU !");
        };
    }

    private static BossTheme tower(String top, String low, String ground, String accent, EnemySymbol death, String line) {
        return new BossTheme(Color.valueOf(top), Color.valueOf(low), Color.valueOf(ground), Color.valueOf(accent), death, line);
    }

    /** Un roi : le ciel et le sol de son lieu, sa couleur à lui. */
    private static BossTheme king(EnemyKind kind, String accent, EnemySymbol death, String line) {
        Place place = Place.of(kind);
        String top, low, ground;
        switch (place == null ? Place.PRAIRIE : place) {
            case PORT -> { top = "040a18"; low = "0f2a44"; ground = "06101c"; }       // la nuit sur la mer
            case MINES -> { top = "120a04"; low = "3a2210"; ground = "140c06"; }      // la roche et l'or
            case CASINO -> { top = "02101a"; low = "0a3a4a"; ground = "031016"; }     // sous la mer
            default -> { top = "1c1a28"; low = "2e2a3e"; ground = "141220"; }         // le donjon de pierre
        }
        return tower(top, low, ground, accent, death, line);
    }
}
