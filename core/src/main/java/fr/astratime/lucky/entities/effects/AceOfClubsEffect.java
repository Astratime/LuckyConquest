package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.Random;

/**
 * As de Trèfle et ses trois cousins de la boutique (Étoile filante, Étoile du berger,
 * Étoile d'or) : la carte ajoute {@link #OWN_STARS} à la Bonne étoile, puis la vide pour
 * tenter un Bingo à ce tirage. À {@link #GUARANTEED} points ou plus (moins pour la
 * version « + »), le Bingo est garanti ; en dessous, la chance est proportionnelle.
 * L'As tire son symbole au hasard dans la machine ; chaque Étoile le choisit parmi les
 * symboles d'attaque, de défense ou de gains de la machine (Astra, 2026-11-13).
 * Le sort est jeté à la pose de la carte : le joueur sait tout de suite.
 */
public class AceOfClubsEffect extends Effect {

    /** Ce que vise le Bingo. */
    public enum Kind {
        /** N'importe quel symbole de la machine (As de Trèfle). */
        ANY,
        /** Un symbole d'attaque (Étoile filante). */
        ATTACK,
        /** Un symbole de défense (Étoile du berger). */
        DEFENSE,
        /** Un symbole de gains (Étoile d'or). */
        GAINS
    }

    /** Points de Bonne étoile que la carte ajoute avant de la vider. */
    public static final int OWN_STARS  = 10;
    /** Points de Bonne étoile qui garantissent le Bingo (carte normale). */
    public static final int GUARANTEED = 30;
    private static final Random RANDOM = new Random();

    private final Kind  kind;
    /** Puissance de la carte : 1, ou {@link fr.astratime.lucky.entities.Card#UPGRADE_FACTOR} pour sa version « + ». */
    private final float power;

    public AceOfClubsEffect() { this(Kind.ANY, 1f); }

    /** @param power puissance : le Bingo est garanti avec moins de points (version « + » de la carte) */
    public AceOfClubsEffect(float power) { this(Kind.ANY, power); }

    /**
     * @param kind  ce que vise le Bingo
     * @param power puissance : le Bingo est garanti avec moins de points (version « + » de la carte)
     */
    public AceOfClubsEffect(Kind kind, float power) {
        this.kind  = kind;
        this.power = power;
    }

    /** @return les points de Bonne étoile qui garantissent le Bingo de cette carte. */
    public int getGuaranteed() { return Math.round(GUARANTEED / power); }

    /** @return la chance (0 à 1) du Bingo avec {@code stars} points de Bonne étoile. */
    public float chance(int stars) { return Math.min(1f, stars / (float) getGuaranteed()); }

    /** Remplit puis vide la Bonne étoile, jette le sort et, s'il réussit, impose le Bingo au tirage. */
    @Override
    public void onPlay(PlayContext context) {
        LastingEffects lasting = context.getLastingEffects();
        lasting.addLuckyStar(OWN_STARS);
        int stars = lasting.consumeLuckyStar();
        float chance = chance(stars);
        boolean bingo = roll(chance);
        int percent = Math.round(chance * 100);
        if (!bingo) {
            context.addPopups(List.of(new EffectPopup(Lang.f("BONNE ÉTOILE {0} % : PAS DE BINGO", percent),
                EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY)));
            return;
        }
        Symbol symbol = pick(context);
        context.addPopups(List.of(new EffectPopup(symbol == null ? Lang.t("BONNE ÉTOILE : BINGO !")
            : Lang.f("BONNE ÉTOILE : BINGO {0} !", symbol.getDisplayName()), EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)));
        context.queueForSpin(new StarBingo(symbol));
    }

    /** @return {@code true} si le sort réussit (toujours à 100 %). Ouvert aux tests. */
    protected boolean roll(float chance) { return chance >= 1f || RANDOM.nextFloat() < chance; }

    /** @return le symbole du Bingo, ou {@code null} : tiré au hasard dans la machine (As, ou machine sans symbole du bon genre). */
    private Symbol pick(PlayContext context) {
        List<Symbol> pool = switch (kind) {
            case ANY     -> List.of();
            case ATTACK  -> SymbolRegistry.getAttackSymbols(context.getPlayer());
            case DEFENSE -> SymbolRegistry.getDefenseSymbols(context.getPlayer());
            case GAINS   -> SymbolRegistry.getGainSymbols(context.getPlayer());
        };
        return pool.isEmpty() ? null : pool.get(RANDOM.nextInt(pool.size()));
    }

    /** Aucun effet propre au tirage : le Bingo obtenu à la pose y est imposé par {@link StarBingo}. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        String target = switch (kind) {
            case ANY     -> Lang.t("d'un symbole de ta machine");
            case ATTACK  -> Lang.t("d'un symbole d'attaque de ta machine");
            case DEFENSE -> Lang.t("d'un symbole de défense de ta machine");
            case GAINS   -> Lang.t("d'un symbole de gains de ta machine");
        };
        return Lang.f("Bonne étoile +{0}, puis la vide. À {1} points ou plus : Bingo garanti {2} à ce tirage. "
            + "En dessous, la chance suit les points ({3} points : 50 %). Ce Bingo : attaque et bouclier x{4}, "
            + "il traverse la défense, sans Jeu bonus", OWN_STARS, getGuaranteed(), target,
            Math.round(getGuaranteed() / 2f), LastingEffects.STAR_BINGO_POWER);
    }

    /** Textes fixes : aucun, tout dépend de la Bonne étoile (voir {@link #onPlay}). */
    @Override
    public List<EffectPopup> getPopups() { return List.of(); }

    /** Le sort est jeté une fois : doublée, la carte viderait une jauge déjà vide. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_CLUBS; }

    /** Le Bingo de la Bonne étoile, imposé au tirage : attaque et bouclier multipliés, défense traversée, pas de Jeu bonus. */
    private static final class StarBingo extends Effect {
        private final Symbol symbol;

        StarBingo(Symbol symbol) { this.symbol = symbol; }

        @Override
        public void apply(TurnContext context) {
            if (symbol != null) context.getSpinContext().forceJackpot(symbol);
            else context.getSpinContext().forceJackpot();
            context.getCombatContext().multiplyAttack(LastingEffects.STAR_BINGO_POWER, "Bonne étoile");
            context.getCombatContext().multiplyDefense(LastingEffects.STAR_BINGO_POWER, "Bonne étoile");
            context.getCombatContext().pierceDefense(100);
            context.getSpinContext().markStarBingo();
        }

        @Override
        public String getDescription() { return ""; }

        @Override
        public List<EffectPopup> getPopups() { return List.of(); }

        @Override
        public EffectSound getSound() { return EffectSound.ACE_OF_CLUBS; }
    }
}
