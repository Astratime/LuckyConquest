package fr.astratime.lucky.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.effects.GoldVeinEffect;
import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.views.SidePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Les lignes du panneau « Effets » de l'écran de combat : chaque effet actif
 * (dette, cartes qui durent, jauges, règle du lieu, mauvais sorts et parades)
 * avec son icône, son texte court, son nom et sa description, pour son
 * infobulle et sa fiche. Une instance par rafraîchissement du panneau.
 */
final class CombatEffectRows {

    private final GameController controller;
    private final Player         player;
    private final HudTextures    hud;
    /** L'image d'un symbole des rouleaux du joueur. */
    private final Function<Symbol, TextureRegion> symbols;
    /** La dette qui suit le compteur de gains affiché. */
    private final SpinEconomy.Debt debt;

    CombatEffectRows(GameController controller, HudTextures hud, Function<Symbol, TextureRegion> symbols,
                     SpinEconomy.Debt debt) {
        this.controller = controller;
        this.player     = controller.getGameState().getPlayer();
        this.hud        = hud;
        this.symbols    = symbols;
        this.debt       = debt;
    }

    /**
     * @return les effets actifs : dette, symboles retirés des rouleaux (et
     *         tirages restants), Porte-bonheur, jauges, paris en cours, puis ceux
     *         du lieu et de l'ennemi (voir {@link #addPlaceRows})
     */
    List<SidePanel.EffectRow> rows() {
        List<SidePanel.EffectRow> rows = new ArrayList<>();
        LastingEffects lasting = player.getLastingEffects();
        TextureRegion cross = new TextureRegion(hud.iconCross);
        int cost = controller.getGameState().getEnemy().getKind().getSpinCost();
        if (debt != SpinEconomy.Debt.NONE) {
            String limit = SidePanel.formatGains(SpinEconomy.BAILIFF_SPINS * cost);
            rows.add(debt == SpinEconomy.Debt.BAILIFF
                ? new SidePanel.EffectRow(new TextureRegion(hud.coin), cross, Lang.t("Huissier"),
                    Lang.t("Huissier"), Lang.f("Tu dois plus de {0} gains. Attaque et bouclier des symboles -50 %. "
                        + "L'ennemi joue une carte de plus. Une Paire ou un Bingo te renfloue.", limit))
                : new SidePanel.EffectRow(new TextureRegion(hud.coin), cross, Lang.t("Endetté"),
                    Lang.t("Endetté"), Lang.f("Tes gains sont sous 0. Attaque et bouclier des symboles -25 %. "
                        + "Sous -{0}, l'Huissier arrive.", limit)));
        }
        for (Map.Entry<Symbol, Integer> removed : lasting.getRemovedSymbols().entrySet()) {
            int turns = removed.getValue();
            rows.add(new SidePanel.EffectRow(symbols.apply(removed.getKey()), cross,
                Lang.f("Retiré {0}", turns(turns)), Lang.t("Recyclage"),
                Lang.f("{0} est retiré de tes rouleaux. Il revient dans {1}.",
                    removed.getKey().getDisplayName(), turns(turns))));
        }
        if (lasting.getGainBonus() > 0f) {
            int percent = Math.round(lasting.getGainBonus() * 100f);
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconClover), null,
                Lang.f("Gains +{0} %", percent), Lang.t("Porte-bonheur"),
                Lang.f("Tes gains +{0} %. Jusqu'à la fin du combat.", percent)));
        }
        if (lasting.getCorruptionTurns() > 0) {
            int turns = lasting.getCorruptionTurns();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconCorruption), null,
                Lang.f("Corruption {0}", turns(turns)), Lang.t("Corruption"),
                Lang.f("Attaque et bouclier des symboles x{0}. Chaque tour coûte {1} % de tes gains. Encore {2}.",
                    CorruptionEffect.FACTOR, CorruptionEffect.GAINS_PERCENT, turns(turns))));
        }
        if (lasting.getExtraPlaysTurns() > 0) {
            int turns = lasting.getExtraPlaysTurns();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconSleeve), null,
                Lang.f("{0} cartes, {1}", lasting.getExtraPlays(), turns(turns)), Lang.t("Dans la manche"),
                Lang.f("{0} cartes jouables par tour. Encore {1}.", lasting.getExtraPlays(), turns(turns))));
        }
        if (lasting.getBlades() > 0) {
            int blades = lasting.getBlades();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconBlade), null,
                Lang.f("Lames {0} (+{1})", blades, blades * PreparationResolver.BLADE_ATTACK), Lang.t("Lames"),
                Lang.f("Jauge de Pique. Chaque Lame donne +{0} d'attaque à tes symboles. Tes Piques jouées en Couleur "
                    + "ou en Suite la remplissent. L'As de Pique et la Guillotine l'encaissent.",
                    PreparationResolver.BLADE_ATTACK)));
        }
        if (lasting.getBlood() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconBlood), null,
                Lang.f("Sang {0}", lasting.getBlood()), Lang.t("Sang"),
                Lang.t("Jauge de Coeur. Le soin au-delà de tes PV max s'y garde. Tes Coeurs joués en Couleur ou en "
                    + "Suite la remplissent. L'As de Coeur ajoute tout le Sang à ton attaque.")));
        }
        if (lasting.getVault() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconVault), null,
                Lang.f("Coffre {0}", lasting.getVault()), Lang.t("Coffre"),
                Lang.t("Jauge de Carreau. Ton bouclier inutilisé s'y garde. Tes Carreaux joués en Couleur ou en Suite "
                    + "la remplissent. L'As de Carreau le vide sur l'ennemi.")));
        }
        for (int[] safe : lasting.getSafes()) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconSafe), null,
                "+" + safe[0] + ", " + turns(safe[1]), Lang.t("Coffre-fort"),
                Lang.f("{0} gains mis de côté, déjà doublés. Ils reviennent dans {1}.",
                    PlayerProfile.formatCoins(safe[0]), turns(safe[1]))));
        }
        for (Symbol bet : controller.getBetsThisTurn()) {
            rows.add(new SidePanel.EffectRow(symbols.apply(bet), null, Lang.t("Pari x2 à x4"), Lang.t("Pari"),
                Lang.f("Tu as parié sur {0}. S'il sort 1, 2 ou 3 fois : gains x2, x3 ou x4. Sinon : gains /2.",
                    bet.getDisplayName())));
        }
        if (controller.isDoubleNextPending()) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconDouble), null, Lang.t("Carte suivante x2"),
                Lang.t("Double ou rien"), Lang.t("La prochaine carte jouée ce tour compte deux fois.")));
        }
        addPlaceRows(rows, lasting);
        return rows;
    }

    /** @return {@code count} suivi de « tour » ou « tours ». */
    private static String turns(int count) {
        return Lang.f(Lang.plural(count) ? "{0} tours" : "{0} tour", count);
    }

    /**
     * Exploration : la règle du lieu (Scorbut, prochain coup de grisou, niveau de
     * la marée), les cartes des lieux qui durent (Veine d'or, Bulle d'air,
     * Casque) et les mauvais sorts de l'ennemi qui attendent le prochain tour.
     */
    private void addPlaceRows(List<SidePanel.EffectRow> rows, LastingEffects lasting) {
        GameState state = controller.getGameState();
        TextureRegion skull = new TextureRegion(hud.iconCorruption);
        int turn = state.getTurnNumber();
        PlaceRule rule = state.getPlaceRule();
        switch (state.getActiveRule()) {
            case SCORBUT -> {
                int turns = PlaceRule.SCURVY_PERIOD - (turn - 1) % PlaceRule.SCURVY_PERIOD;
                rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Scorbut : {0}", turns(turns)), rule.getName(),
                    Lang.f("{0} Prochain Scorbut dans {1}.", rule.getDescription(), turns(turns))));
            }
            case GRISOU -> {
                int turns = PlaceRule.turnsBeforeFiredamp(turn);
                rows.add(new SidePanel.EffectRow(skull, null, turns == 1 ? Lang.t("Grisou : ce tour")
                    : Lang.f("Grisou : {0} tours", turns), rule.getName(), rule.getDescription()
                    + (turns == 1 ? Lang.t(" Explosion à la fin de ce tour.") : Lang.f(" Prochaine explosion dans {0}.",
                        turns(turns)))));
            }
            case MAREE -> rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Marée {0}/{1}{2}",
                PlaceRule.tide(turn), PlaceRule.TIDE_CYCLE, (rule.isHighTide(turn) ? Lang.t(" : haute") : "")), rule.getName(),
                Lang.f("{0} Niveau actuel : {1}.", rule.getDescription(), PlaceRule.tide(turn))));
            case NONE -> { }
        }
        if (lasting.getBubbleTurns() > 0) {
            int turns = lasting.getBubbleTurns();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconSleeve), null,
                Lang.f("Bulle : {0}", turns(turns)), Lang.t("Bulle d'air"),
                Lang.f("La règle du lieu ne joue plus. Encore {0}.", turns(turns))));
        }
        if (lasting.getGoldVeinTurns() > 0) {
            int turns = lasting.getGoldVeinTurns();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconClover), null,
                Lang.f("Veine x{0} ({1})", GoldVeinEffect.FACTOR, turns), Lang.t("Veine d'or"),
                Lang.f("Tes gains x{0}. Encore {1}.", GoldVeinEffect.FACTOR, turns(turns))));
        }
        if (player.getHelmets() > 0) {
            int helmets = player.getHelmets();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconVault), null,
                Lang.f("Casque x{0}", helmets), Lang.t("Casque"), Lang.f("Bloque entièrement le prochain coup que tu reçois. {0}",
                    (helmets > 1 ? Lang.f("{0} casques : un par coup.", helmets) : Lang.t("Un seul coup.")))));
        }
        // Parades des cartes des coffres des lieux
        TextureRegion guard = new TextureRegion(hud.iconVault);
        Enemy target = controller.getGameState().getEnemy();
        if (lasting.getTraps() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.f("Piège x{0}",
            lasting.getTraps()),
            Lang.t("Piège à rats"), Lang.t("Le prochain mauvais sort sur ta main est annulé (Grignotage, Aveuglement, Chant, Abordage, "
                + "Fouille).")));
        if (lasting.getLanternDraws() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Lanterne ({0})", lasting.getLanternDraws()), Lang.t("Lanterne"),
            Lang.f("Ta main ne peut pas être cachée. Encore {0}.", turns(lasting.getLanternDraws()))));
        if (lasting.getEarplugDraws() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Bouchons ({0})", lasting.getEarplugDraws()), Lang.t("Bouchons d'oreille"),
            Lang.f("Le Chant n'a pas d'effet. Encore {0}.", turns(lasting.getEarplugDraws()))));
        if (lasting.getPropTurns() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Étai ({0})", lasting.getPropTurns()), Lang.t("Étai"),
            Lang.f("Le Forage ne perce pas ton bouclier. Encore {0}.", turns(lasting.getPropTurns()))));
        if (lasting.getLamps() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.f("Lampe x{0}",
            lasting.getLamps()),
            Lang.t("Lampe à carbure"), Lang.t("Le prochain coup de grisou ne t'atteint pas.")));
        if (lasting.getCageTurns() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Cage ({0})", lasting.getCageTurns()), Lang.t("Cage à requin"),
            Lang.f("Rien ne peut prendre tes gains. Encore {0}.", turns(lasting.getCageTurns()))));
        if (lasting.getAnchorTurns() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Ancre ({0})", lasting.getAnchorTurns()), Lang.t("Ancre"),
            Lang.f("La marée haute ne baisse pas ton attaque. Encore {0}.", turns(lasting.getAnchorTurns()))));
        if (lasting.getTemperPercent() > 0) rows.add(new SidePanel.EffectRow(guard, null,
            Lang.f("Trempe +{0} %", lasting.getTemperPercent()), Lang.t("Trempe"),
            Lang.f("Ton attaque est à +{0} %. Elle monte encore à chaque tour.", lasting.getTemperPercent())));
        if (player.hasRope()) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Corde de rappel"), Lang.t("Corde de rappel"),
            Lang.t("Si un coup devait te tuer, tu restes à 1 PV.")));
        if (target.isDazzled()) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Ennemi ébloui"), Lang.t("Rayon du phare"),
            Lang.t("L'ennemi passe son prochain tour.")));
        if (target.getMutinies() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Mutinerie"), Lang.t("Mutinerie"),
            Lang.f("À son prochain tour, l'ennemi joue {0} cartes de moins.", MutinyEffect.CARDS_LESS)));
        if (target.getLoadedCoins() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Pièce truquée"), Lang.t("Pièce truquée"),
            Lang.t("Le prochain tirage de l'ennemi ne peut pas faire de Jackpot.")));
        if (target.getHarpoons() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.f("Harpon x{0}",
            target.getHarpoons()),
            Lang.t("Harpon"), Lang.f(Lang.plural(target.getHarpoons())
                ? "À son prochain tour, l'ennemi joue {0} cartes de moins."
                : "À son prochain tour, l'ennemi joue {0} carte de moins.", target.getHarpoons())));
        Enemy foe = controller.getGameState().getEnemy();
        EnemyKind kind = foe.getKind();
        if (lasting.getNibbles() > 0) rows.add(curse(skull, Lang.f("Grignotage x{0}", lasting.getNibbles()), EnemySymbol.NIBBLE, kind));
        if (lasting.getDrunk() > 0) rows.add(curse(skull, Lang.f("Ivresse x{0}", lasting.getDrunk()), EnemySymbol.DRUNK, kind));
        if (lasting.isBlind()) rows.add(curse(skull, Lang.t("Aveuglement"), EnemySymbol.BLIND, kind));
        if (lasting.hasNugget()) rows.add(curse(skull, Lang.t("Pépite"), EnemySymbol.NUGGET, kind));
        if (lasting.getSongs() > 0) rows.add(curse(skull, Lang.f("Chant x{0}", lasting.getSongs()), EnemySymbol.SONG, kind));
        if (lasting.getFakeGains() > 0) {
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Faux : {0}", lasting.getFakeGains()), Lang.t("Fausse monnaie"),
                Lang.f("{0} de tes gains sont faux. Dépense-les avant ton tirage, ou ils disparaissent.",
                    PlayerProfile.formatCoins(lasting.getFakeGains()))));
        }
        if (lasting.getTaxes() > 0) rows.add(curse(skull, Lang.f("Taxe x{0}", lasting.getTaxes()), EnemySymbol.TAX, kind));
        if (lasting.getHouseRule() != null) {
            LastingEffects.HouseRule houseRule = lasting.getHouseRule();
            rows.add(new SidePanel.EffectRow(skull, null,
                houseRule.getShortName() + " (" + lasting.getHouseRuleTurns() + ")", Lang.t("Nouvelle règle"),
                Lang.f("{0} Encore {1}.", houseRuleText(houseRule), turns(lasting.getHouseRuleTurns()))));
        }
        if (foe.getBannedFamily() != null) {
            String family = foe.getBannedFamily().getDisplayName();
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Banni : {0}", family), Lang.t("Cartes bannies"),
                Lang.f("L'ennemi laisse dehors les cartes {0}. Tu ne peux pas les jouer. Jusqu'à la fin du combat.",
                    family)));
        }
        if (foe.getPrediction() != null) {
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Prédit : {0}",
                foe.getPrediction().getDisplayName().toLowerCase()),
                Lang.t("Prédiction"), Lang.f("Symbole annoncé : {0}. {1}",
                    foe.getPrediction().getDisplayName(), sentence(EnemySymbol.PREDICTION.getDescription(kind)))));
        }
        if (foe.getStolenReels() > 0) {
            int stolen = foe.getStolenReels();
            String reels = Lang.f(Lang.plural(stolen) ? "{0} rouleaux" : "{0} rouleau", stolen);
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Vol : {0}", reels), Lang.t("Rouleaux volés"),
                Lang.f("L'ennemi t'a volé {0}. Ils restent bloqués tant qu'il les garde.", reels)));
        }
        int limit = kind.getTurnLimit();
        if (limit > 0) {
            int left = Math.max(0, limit - state.getTurnNumber() + 1);
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Temps : {0} tours", left), Lang.t("Temps compté"),
                Lang.f("Le combat dure {0} tours. Après, tu perds. Encore {1}.", limit, turns(left))));
        }
    }

    /** @return la ligne d'un mauvais sort de l'ennemi, décrit comme sur ses rouleaux. */
    private static SidePanel.EffectRow curse(TextureRegion icon, String text, EnemySymbol symbol, EnemyKind kind) {
        String name = symbol.getDisplayName().charAt(0) + symbol.getDisplayName().substring(1).toLowerCase();
        return new SidePanel.EffectRow(icon, null, text, name, sentence(symbol.getDescription(kind)));
    }

    /** @return la description d'un symbole ennemi (« Nom : texte ») sans son nom, terminée par un point. */
    private static String sentence(String description) {
        int colon = description.indexOf(':');
        String text = colon >= 0 ? description.substring(colon + 1).trim() : description;
        text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        return text.endsWith(".") ? text : text + ".";
    }

    /** @return ce que change la règle du Directeur des Jeux. */
    private static String houseRuleText(LastingEffects.HouseRule rule) {
        return switch (rule) {
            case NO_COMBOS   -> Lang.t("Les combinaisons de cartes ne comptent plus.");
            case NO_BINGO    -> Lang.t("Tes rouleaux ne peuvent plus faire de Bingo.");
            case DOUBLE_SPIN -> Lang.t("Chaque rouleau tourne deux fois. Le pire résultat reste.");
        };
    }
}
