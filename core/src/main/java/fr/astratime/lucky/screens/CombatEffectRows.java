package fr.astratime.lucky.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.CorruptionEffect;
import fr.astratime.lucky.entities.effects.GoldVeinEffect;
import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.views.SidePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Set;
import java.util.TreeSet;

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
                Lang.f("Jauge de Coeur. Le soin au-delà de tes PV max s'y garde, jusqu'à {0}. Tes Coeurs joués en "
                    + "Couleur ou en Suite la remplissent. L'As de Coeur ajoute tout le Sang à ton attaque.",
                    LastingEffects.MAX_BLOOD)));
        }
        if (lasting.getVault() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconVault), null,
                Lang.f("Coffre {0}", lasting.getVault()), Lang.t("Coffre"),
                Lang.t("Jauge de Carreau. Ton bouclier inutilisé s'y garde. Tes Carreaux joués en Couleur ou en Suite "
                    + "la remplissent. L'As de Carreau le vide sur l'ennemi.")));
        }
        for (double[] safe : lasting.getSafes()) {
            int turnsLeft = (int) safe[1];
            rows.add(new SidePanel.EffectRow(new TextureRegion(hud.iconSafe), null,
                "+" + Lang.big(safe[0]) + ", " + turns(turnsLeft), Lang.t("Coffre-fort"),
                Lang.f("{0} gains mis de côté, déjà doublés. Ils reviennent dans {1}.",
                    SidePanel.formatGains(safe[0]), turns(turnsLeft))));
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
        addReelRows(rows, lasting);
        addPlaceRows(rows, lasting);
        return rows;
    }

    /**
     * Les nouveaux rouleaux qui ont une mémoire (2026-11-01) : jauges du Roi démon et
     * de l'Apôtre de la Chance, invincibilité, Bingo imposé, Tirelire, Ancre,
     * Lumière du bingo ; et chez l'ennemi : brûlure de la Flamme, Descente aux enfers, Lumière.
     */
    private void addReelRows(List<SidePanel.EffectRow> rows, LastingEffects lasting) {
        ReelGauges gauges = lasting.getReelGauges();
        List<Symbol> reels = player.getSlotMachine().getReels();
        if (gauges.getDemonization() > 0 || reels.contains(Symbol.DEMON_KING)) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.DEMON_KING), null,
                Lang.f("Démonisation {0}/{1}", gauges.getDemonization(), ReelGauges.GAUGE_MAX), Lang.t("Démonisation"),
                Lang.f("+1 à chaque Roi démon tiré. À {0} : invincible jusqu'à la fin du tour suivant, dont le tirage "
                    + "est un Bingo de Descente aux enfers.", ReelGauges.GAUGE_MAX)));
        }
        if (gauges.getLuckyAngel() > 0 || reels.contains(Symbol.LUCK_APOSTLE)) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.LUCK_APOSTLE), null,
                Lang.f("Ange chanceux {0}/{1}", gauges.getLuckyAngel(), ReelGauges.GAUGE_MAX), Lang.t("Ange chanceux"),
                Lang.f("+1 à chaque Apôtre de la Chance tiré. À {0} : invincible jusqu'à la fin du tour suivant, dont "
                    + "le tirage est un Bingo de Lumière du bingo.", ReelGauges.GAUGE_MAX)));
        }
        if (gauges.getForced() != null) {
            rows.add(new SidePanel.EffectRow(symbols.apply(gauges.getForced()), null, Lang.t("Bingo imposé"),
                gauges.getForced().getDisplayName(), Lang.f("Ton prochain tirage est un Bingo de {0}. Rien ne peut "
                    + "l'empêcher, ni les mauvais sorts de l'ennemi, ni la carte Bingo.", gauges.getForced().getDisplayName())));
        }
        if (gauges.isInvincible()) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.ECU), null,
                Lang.f("Invincible, {0}", turns(gauges.getInvincibleTurns())), Lang.t("Invincible"),
                Lang.f("Les coups de l'ennemi ne t'enlèvent aucun PV. Encore {0}.", turns(gauges.getInvincibleTurns()))));
        }
        if (gauges.getPiggyDraws() > 0) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.PIGGY_BANK), null,
                Lang.f("Tirelire {0}", gauges.getPiggyNext()), Lang.t("Tirelire"),
                Lang.f("Sortie {0} fois ce combat. À la prochaine : {1} gains de base.", gauges.getPiggyDraws(),
                    gauges.getPiggyNext())));
        }
        if (gauges.getAnchorKept() > 0) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.ANCHOR), null,
                Lang.f("Ancre {0}", Lang.big(gauges.getAnchorKept())), Lang.t("Ancre"),
                Lang.f("{0} de bouclier gardé : il revient au début de ton prochain tour.", Lang.big(gauges.getAnchorKept()))));
        }
        if (player.getLightAbsorbed() > 0) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.BINGO_LIGHT), null,
                Lang.f("Coups rendus {0}", Lang.big(player.getLightAbsorbed())), Lang.t("Lumière du bingo"),
                Lang.f("Ses coups absorbés t'ont soigné. Au prochain tirage, ils le frappent : {0} de dégâts.",
                    Lang.big(player.getLightAbsorbed()))));
        }
        Enemy target = controller.getGameState().getEnemy();
        EnemyMaluses maluses = target.getMaluses();
        if (maluses.getBurnTurns() > 0) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.FLAME), null,
                Lang.f("Brûlure ({0})", maluses.getBurnTurns()), Lang.t("Flamme"),
                Lang.f("Au début de ses tours, l'ennemi perd {0} % de ses PV max. Encore {1}.", EnemyMaluses.BURN_PERCENT,
                    turns(maluses.getBurnTurns()))));
        }
        if (maluses.isHellBound()) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.DESCENT), null, Lang.t("Enfers"),
                Lang.t("Descente aux enfers"), Lang.f("L'ennemi est affaibli : à son prochain tour, ses attaques baissent de {0} % et il "
                    + "joue {1} cartes au plus.", EnemyMaluses.HELL_DAMAGE_PERCENT, EnemyMaluses.HELL_MAX_CARDS)));
        }
        if (maluses.isLitUp()) {
            rows.add(new SidePanel.EffectRow(symbols.apply(Symbol.BINGO_LIGHT), null, Lang.t("Illuminé"),
                Lang.t("Lumière du bingo"), Lang.f("L'ennemi est illuminé : à son prochain tirage, chacun de ses rouleaux a une chance sur deux "
                    + "de devenir Lumière. Ce rouleau ne fait rien et l'ennemi perd {0} % de ses PV max.", EnemyMaluses.LIGHT_HP_PERCENT)));
        }
    }

    /** @return {@code count} suivi de « tour » ou « tours ». */
    private static String turns(int count) {
        return Lang.n(count, "{0} tour", "{0} tours", count);
    }

    /**
     * Exploration : la règle du lieu (Scorbut, prochain coup de grisou, niveau de
     * la marée, prochain tour de manège, Soif), les cartes des lieux qui durent (Veine d'or, Bulle d'air,
     * Casque) et les mauvais sorts de l'ennemi qui attendent le prochain tour.
     */
    private void addPlaceRows(List<SidePanel.EffectRow> rows, LastingEffects lasting) {
        GameState state = controller.getGameState();
        TextureRegion skull = new TextureRegion(hud.iconCorruption);
        addPlaceRuleRow(rows, state, skull);
        addProtectionRows(rows, lasting);
        addEnemyCurseRows(rows, lasting, state, skull);
    }

    /** La règle du lieu : Scorbut, prochain coup de grisou, niveau de la marée, prochain tour de manège, Soif. */
    private void addPlaceRuleRow(List<SidePanel.EffectRow> rows, GameState state, TextureRegion skull) {
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
                Lang.f("{0} Niveau actuel : {1}.", rule.getDescription(), PlaceRule.tide(turn))
                    + (!rule.isHighTide(turn) && rule.isHighTide(turn + 1)
                        ? Lang.f(" Marée haute au prochain tour : attaque -{0} %.", PlaceRule.HIGH_TIDE_MALUS) : "")));
            case MANEGE -> {
                int turns = PlaceRule.turnsBeforeCarousel(turn);
                rows.add(new SidePanel.EffectRow(skull, null, turns == 1 ? Lang.t("Manège ce tour")
                    : Lang.f("Manège {0}/{1}", PlaceRule.CAROUSEL_PERIOD - turns + 1, PlaceRule.CAROUSEL_PERIOD),
                    rule.getName(), rule.getDescription()
                    + (turns == 1 ? Lang.t(" C'est ce tour-ci : le manège tourne à ton tirage !")
                        : turns == 2 ? Lang.t(" Le manège tournera au prochain tour.")
                        : Lang.f(" Le manège tournera dans {0}.", turns(turns - 1)))));
            }
            case SOIF -> rows.add(new SidePanel.EffectRow(skull, null,
                Lang.f("Soif : soins /{0}", PlaceRule.THIRST_HEAL_DIVISOR), rule.getName(),
                rule.getDescription() + Lang.t(" Pense à te soigner à chaque tour.")));
            case NONE -> { }
        }
    }

    /** Les cartes des lieux qui durent (Bulle d'air, Veine d'or, Casque), les parades des coffres et les archétypes. */
    private void addProtectionRows(List<SidePanel.EffectRow> rows, LastingEffects lasting) {
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
        if (target.getMaluses().isDazzled()) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Ennemi ébloui"), Lang.t("Rayon du phare"),
            Lang.t("L'ennemi passe son prochain tour.")));
        if (target.getMaluses().getMutinies() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Mutinerie"), Lang.t("Mutinerie"),
            Lang.f("À son prochain tour, l'ennemi joue {0} cartes de moins.", MutinyEffect.CARDS_LESS)));
        if (target.getMaluses().getLoadedCoins() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.t("Pièce truquée"), Lang.t("Pièce truquée"),
            Lang.t("Le prochain tirage de l'ennemi ne peut pas faire de Jackpot.")));
        if (target.getMaluses().getHarpoons() > 0) rows.add(new SidePanel.EffectRow(guard, null, Lang.f("Harpon x{0}",
            target.getMaluses().getHarpoons()),
            Lang.t("Harpon"), Lang.n(target.getMaluses().getHarpoons(), "À son prochain tour, l'ennemi joue {0} carte de moins.",
                "À son prochain tour, l'ennemi joue {0} cartes de moins.", target.getMaluses().getHarpoons())));
        addArchetypeRows(rows, lasting, target, guard);
    }

    /** Les mauvais sorts de l'ennemi qui attendent le prochain tour, ce qu'il a pris ou banni, le temps compté. */
    private void addEnemyCurseRows(List<SidePanel.EffectRow> rows, LastingEffects lasting, GameState state,
                                   TextureRegion skull) {
        Enemy foe = controller.getGameState().getEnemy();
        EnemyKind kind = foe.getKind();
        if (lasting.getNibbles() > 0) rows.add(curse(skull, Lang.f("Grignotage x{0}", lasting.getNibbles()), EnemySymbol.NIBBLE, foe));
        if (lasting.getDrunk() > 0) rows.add(curse(skull, Lang.f("Ivresse x{0}", lasting.getDrunk()), EnemySymbol.DRUNK, foe));
        if (lasting.isBlind()) {
            int blinds = lasting.getBlindCards();
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Aveuglement x{0}", blinds), Lang.t("Aveuglement"),
                Lang.n(blinds, "Au prochain tour, {0} carte de ta main sera face cachée. Les Aveuglements se cumulent.",
                    "Au prochain tour, {0} cartes de ta main seront faces cachées. Les Aveuglements se cumulent.",
                    blinds)));
        }
        if (lasting.hasNugget()) rows.add(curse(skull, Lang.t("Pépite"), EnemySymbol.NUGGET, foe));
        if (lasting.getSongs() > 0) rows.add(curse(skull, Lang.f("Chant x{0}", lasting.getSongs()), EnemySymbol.SONG, foe));
        Set<Integer> voids = new TreeSet<>(lasting.getForbiddenReels());
        voids.remove(lasting.getForbiddenReel()); // le Rouleau interdit a sa propre ligne
        if (!voids.isEmpty()) rows.add(curse(skull, Lang.f("Rien ne va plus x{0}", voids.size()), EnemySymbol.NOTHING_GOES, foe));
        if (lasting.getShortCircuits() > 0) {
            rows.add(curse(skull, Lang.f("Court-circuit x{0}", lasting.getShortCircuits()), EnemySymbol.SHORT_CIRCUIT, foe));
        }
        if (lasting.getHouseEdges() > 0) {
            rows.add(curse(skull, Lang.f("Avantage x{0}", lasting.getHouseEdges()), EnemySymbol.HOUSE_EDGE, foe));
        }
        if (lasting.getRockfalls() > 0) {
            rows.add(curse(skull, Lang.f("Éboulement : -{0}", lasting.getRockfalls()), EnemySymbol.ROCKFALL, foe));
        }
        if (!lasting.getAspirations().isEmpty()) {
            rows.add(curse(skull, Lang.f("Aspiration x{0}", lasting.getAspirations().size()), EnemySymbol.ASPIRATION, foe));
        }
        if (lasting.getFakeGains() > 0) {
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Faux : {0}", Lang.big(lasting.getFakeGains())), Lang.t("Fausse monnaie"),
                Lang.f("{0} de tes gains sont faux. Dépense-les avant ton tirage, ou ils disparaissent.",
                    SidePanel.formatGains(lasting.getFakeGains()))));
        }
        if (lasting.getTaxes() > 0) rows.add(curse(skull, Lang.f("Taxe x{0}", lasting.getTaxes()), EnemySymbol.TAX, foe));
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
            String foretold = foe.getSecondPrediction() == null ? foe.getPrediction().getDisplayName()
                : Lang.f("{0} ou {1}", foe.getPrediction().getDisplayName(), foe.getSecondPrediction().getDisplayName());
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Prédit : {0}", foretold.toLowerCase()),
                Lang.t("Prédiction"), Lang.f("Symbole annoncé : {0}. {1}",
                    foretold, sentence(EnemySymbol.PREDICTION.getDescription(kind, foe.isHard())))));
        }
        if (foe.getStolenReels() > 0) {
            int stolen = foe.getStolenReels();
            String reels = Lang.n(stolen, "{0} rouleau", "{0} rouleaux", stolen);
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Vol : {0}", reels), Lang.t("Rouleaux volés"),
                foe.getTheftTurns() > 0
                    ? Lang.f("L'ennemi t'a volé {0}. Le premier revient dans {1}.", reels, turns(foe.getTheftTurns()))
                    : Lang.f("L'ennemi t'a volé {0}. Ils restent bloqués tant qu'il les garde.", reels)));
        }
        int limit = foe.getTurnLimit(); // moins ses Comptes à rebours
        if (limit > 0) {
            int left = Math.max(0, limit - state.getTurnNumber() + 1);
            rows.add(new SidePanel.EffectRow(skull, null, Lang.f("Temps : {0} tours", left), Lang.t("Temps compté"),
                Lang.f("Le combat dure {0} tours. Après, tu perds. Encore {1}.", limit, turns(left))));
        }
    }

    /** @return la ligne d'un mauvais sort de l'ennemi, décrit comme sur ses rouleaux. */
    private static SidePanel.EffectRow curse(TextureRegion icon, String text, EnemySymbol symbol, Enemy foe) {
        String name = symbol.getDisplayName().charAt(0) + symbol.getDisplayName().substring(1).toLowerCase();
        return new SidePanel.EffectRow(icon, null, text, name, sentence(symbol.getDescription(foe.getKind(), foe.isHard())));
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

    /**
     * Les effets des cartes des archétypes : Créances et malus du Bluffeur sur
     * l'ennemi, Chapeau du Lutin, Rente viagère, Martingale et Rire du Joker.
     */
    private void addArchetypeRows(List<SidePanel.EffectRow> rows, LastingEffects lasting, Enemy target, TextureRegion icon) {
        if (target.getMaluses().getPharaohSealTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Sceau ({0})", target.getMaluses().getPharaohSealTurns()),
            Lang.t("Sceau du Pharaon"), Lang.f("Ses Créances comptent double. Encore {0}.", turns(target.getMaluses().getPharaohSealTurns()))));
        if (target.getMaluses().getClaims() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Créances x{0}", target.getMaluses().getClaims()),
            Lang.t("Créances"), Lang.f("À chacun de ses tours, l'ennemi perd {0} % de ses PV max et tu gagnes {1}.",
                percentText(target.getMaluses().getClaims() * EnemyMaluses.CLAIM_HP_PER_MILLE / 10f),
                SidePanel.formatGains((double) target.getMaluses().getClaims() * EnemyMaluses.CLAIM_SPINS * target.getKind().getSpinCost()))));
        if (target.getMaluses().getCompoundTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null,
            Lang.f("Intérêts ({0})", target.getMaluses().getCompoundTurns()), Lang.t("Intérêts composés"),
            Lang.f("Les Créances de l'ennemi grossissent à chacun de tes tirages. Encore {0}.", turns(target.getMaluses().getCompoundTurns()))));
        if (target.getMaluses().getIntimidation() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.t("Intimidé"), Lang.t("Intimidation"),
            Lang.f("À son prochain tour, ses attaques baissent de {0} %.", target.getMaluses().malusShare(target.getMaluses().getIntimidation()))));
        if (target.getMaluses().getMarkedTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null,
            Lang.f("Cartes marquées ({0})", target.getMaluses().getMarkedTurns()), Lang.t("Cartes marquées"),
            Lang.f("Ses cartes à couleur perdent {0} % de leur effet. Encore {1}.",
                target.getMaluses().malusShare(fr.astratime.lucky.controllers.EnemyTurnResolver.MARKED_CARDS_PERCENT),
                turns(target.getMaluses().getMarkedTurns()))));
        if (target.getMaluses().getRedHerrings() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.t("Fausse piste"), Lang.t("Fausse piste"),
            Lang.t("À son prochain tirage, un de ses rouleaux ne tourne pas (une chance sur deux contre un boss ou un roi).")));
        if (target.getMaluses().getSleights() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Passe-passe x{0}", target.getMaluses().getSleights()),
            Lang.t("Tour de passe-passe"),
            Lang.t("Son prochain effet spécial est annulé (une chance sur deux contre un boss ou un roi).")));
        if (lasting.getHatTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Chapeau ({0})", lasting.getHatTurns()),
            Lang.t("Chapeau du Lutin"), Lang.f("Chaque coup qui touche l'ennemi te rapporte {0} % d'un tirage en gains. Encore {1}.",
                lasting.getHatPercent(), turns(lasting.getHatTurns()))));
        if (lasting.getHoardTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Magot ({0})", lasting.getHoardTurns()),
            Lang.t("Magot du dragon"), Lang.f("Chaque coup qui touche te rapporte {0} tirages de gains et un peu d'attaque. Attaque +{1} %. Encore {2}.",
                lasting.getHoardSpins(), lasting.getHoardPercent(), turns(lasting.getHoardTurns()))));
        if (lasting.getJokerBox() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Diable +{0}", lasting.getJokerBox()),
            Lang.t("Diable en boîte"), Lang.f("Joker +{0} à chaque tirage, jusqu'à la fin du combat.", lasting.getJokerBox())));
        if (lasting.getAnnuityTurns() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Rente ({0})", lasting.getAnnuityTurns()),
            Lang.t("Rente viagère"), Lang.f("À chacun de tes tours : une Créance sur l'ennemi et {0} tirages de gains. Encore {1}.",
                lasting.getAnnuitySpins(), turns(lasting.getAnnuityTurns()))));
        if (lasting.getMartingale() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Martingale x{0}", lasting.getMartingale()),
            Lang.t("Martingale"), Lang.f("Au prochain tirage : attaque +{0} % et paire +{1} %.", lasting.getMartingaleAttack(),
                lasting.getMartingalePair())));
        if (lasting.getNextDraws() > 0) rows.add(new SidePanel.EffectRow(icon, null, Lang.f("Pioche +{0}", lasting.getNextDraws()),
            Lang.t("Rire du Joker"), Lang.f("Tu pioches {0} cartes de plus au prochain tour.", lasting.getNextDraws())));
    }

    /** @return {@code percent} sans décimale inutile, avec la virgule en français (ex : "2,5"). */
    private static String percentText(float percent) {
        String text = percent == (int) percent ? String.valueOf((int) percent) : String.valueOf(percent);
        return !Lang.isEnglish() ? text.replace('.', ',') : text;
    }
}
