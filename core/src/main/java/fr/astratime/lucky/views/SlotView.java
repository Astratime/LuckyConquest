package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.ReelActor;
import fr.astratime.lucky.animations.SymbolStrikes;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Les rouleaux de la machine à sous, dans les fenêtres dessinées sur la table
 * ({@link TableView}), et les textes animés des résultats du tirage. Au lancer,
 * les rouleaux défilent puis s'arrêtent l'un après l'autre ; si les deux
 * premiers symboles sont identiques, le dernier ralentit et s'illumine pour
 * faire durer le suspense. Un Joker arrêté s'illumine puis se transforme en
 * symbole qu'il remplace. Un rouleau bloqué par le Rouleau interdit de
 * l'ennemi est barré d'une croix rouge et ne tourne pas. Possède les textures
 * des symboles.
 */
public class SlotView implements Disposable {

    private static final float SYMBOL_WIDTH  = 94f;
    private static final float SYMBOL_HEIGHT = 80f;
    private static final float SYMBOL_PAD    = 8f;
    /** Taille d'un rouleau : un symbole et sa marge (les fenêtres de la table ont cette taille). */
    public static final float  CELL_WIDTH    = SYMBOL_WIDTH + SYMBOL_PAD * 2;
    public static final float  CELL_HEIGHT   = SYMBOL_HEIGHT + SYMBOL_PAD * 2;
    private static final float TOOLTIP_GAP   = 5f;

    // Arrêt des rouleaux, en secondes après le lancer.
    private static final float FIRST_STOP    = 0.7f;
    private static final float STOP_STEP     = 0.35f;  // entre deux rouleaux
    private static final float SUSPENSE_TIME = 1.1f;   // arrêt retardé du dernier rouleau
    private static final float SUSPENSE_SPIN_PITCH = 0.6f; // bruit des rouleaux plus grave quand le dernier ralentit
    private static final Color SUSPENSE_GLOW = Palette.GOLD;
    // Transformation d'un Joker : il brille, puis son rouleau repart brièvement vers le symbole remplacé.
    private static final Color JOKER_GLOW       = Palette.VIOLET;
    private static final float JOKER_SHOW_TIME  = 0.45f;  // Joker affiché avant de se transformer
    private static final float JOKER_SPIN_TIME  = 0.25f;
    private static final float JOKER_STEP       = 0.15f;  // entre deux Jokers

    // Textes des résultats du tirage : ceux de chaque symbole (de gauche à
    // droite, en escalier pour ne pas se chevaucher : les symboles sont plus
    // étroits que les textes), puis le bonus de paire/jackpot au-dessus ; le
    // tour de l'ennemi attend la fin de ces textes.
    private static final float POPUP_SYMBOL_GAP   = 20f;   // au-dessus du symbole
    private static final float POPUP_SLOT_STEP    = 55f;   // décalage vertical d'un symbole au suivant
    private static final float POPUP_SYMBOL_DELAY = 0.3f;  // entre deux symboles
    private static final float POPUP_BONUS_DELAY  = 1.0f;
    private static final float POPUP_BONUS_GAP    = 210f;  // au-dessus de la ligne de symboles (et de l'escalier)
    private static final float POPUP_ENEMY_DELAY  = 1.5f;
    /** Textes des cartes révélés au lancer (combos) : ils passent avant ceux des symboles. */
    private static final float POPUP_CARDS_TIME   = 0.7f;
    /** Tir du pistolet, après les textes des symboles ; le bonus et la riposte attendent d'autant. */
    private static final float POPUP_PISTOL_DELAY = 1.1f;
    private static final float POPUP_PISTOL_TIME  = 1.0f;
    /** Temps entre le texte « JACKPOT ! » (début de sa célébration) et la fin des textes du tirage. */
    public static final float  RIPOSTE_AFTER_BONUS = POPUP_ENEMY_DELAY - POPUP_BONUS_DELAY;
    public static final float  POPUP_PLAYER_GAP   = 110f;  // à droite de la barre de vie du joueur
    /** Animation de chaque symbole (voir SymbolStrikes) : son texte apparaît quand elle touche sa cible. */
    private static final float POPUP_STRIKE_LEAD  = SymbolStrikes.IMPACT_TIME;

    /** Croix d'un rouleau bloqué : la fenêtre barrée de la carte Rouleau interdit. */
    private static final String LOCK_ASSET = "cards/dark/FORBIDDEN_REEL.png";
    private static final int    LOCK_X = 40, LOCK_Y = 60, LOCK_SIZE_X = 110, LOCK_SIZE_Y = 120;

    private final TableView                  tableView;
    private final Tooltip                    tooltip;
    private final EffectPopupAnimator        popupAnimator;
    private final Sound                      reelSpinSound;
    private final Sound                      reelStopSound;
    private final Sound                      suspenseSound;
    private final Map<Symbol, Texture>       textures = new EnumMap<>(Symbol.class);
    private final Map<Symbol, TextureRegion> regions  = new EnumMap<>(Symbol.class);
    private final Table                      table    = new Table();
    /** Rouleaux, de gauche à droite (le 4e ne s'affiche qu'avec la Machine en surchauffe). */
    private final List<ReelActor<Symbol>>    reels    = new ArrayList<>();
    /** Détail du coup de chaque rouleau au dernier tirage (lignes du calcul), affiché au survol. */
    private final Map<Integer, List<String>> hitDetails = new HashMap<>();
    /** Case de chaque rouleau : le rouleau et sa croix. */
    private final List<Stack>                cells    = new ArrayList<>();
    /** Rouleaux affichés (-1 avant le premier affichage). */
    private int                              reelCount = -1;
    /** Croix posée sur chaque rouleau quand il est bloqué (Rouleau interdit). */
    private final List<Image>                locks    = new ArrayList<>();
    private final Texture                    lockTexture;
    /** Rouleaux bloqués (Rouleau interdit, rouleaux volés par la Machine Originelle). */
    private final java.util.Set<Integer>     blockedReels = new java.util.TreeSet<>();
    /** Rouleaux pas encore arrêtés pendant un lancer. */
    private int                              reelsSpinning;
    /** Boucle du bruit des rouleaux et suspense en cours (-1 : aucun). */
    private long                             spinSoundId     = -1;
    private long                             suspenseSoundId = -1;

    /**
     * @param reelSpinSound boucle jouée tant que des rouleaux tournent
     * @param reelStopSound bruitage joué quand un rouleau s'arrête
     * @param suspenseSound bruitage du dernier rouleau qui ralentit, coupé à son arrêt
     */
    public SlotView(TableView tableView, Tooltip tooltip, EffectPopupAnimator popupAnimator, Sound reelSpinSound,
                    Sound reelStopSound, Sound suspenseSound) {
        this(tableView, tooltip, popupAnimator, reelSpinSound, reelStopSound, suspenseSound, Symbol.classicReels());
    }

    /** @param machine symboles de la machine du joueur, qui défilent sur les rouleaux (avec le Joker) */
    public SlotView(TableView tableView, Tooltip tooltip, EffectPopupAnimator popupAnimator, Sound reelSpinSound,
                    Sound reelStopSound, Sound suspenseSound, List<Symbol> machine) {
        this.tableView     = tableView;
        this.tooltip       = tooltip;
        this.reelSpinSound = reelSpinSound;
        this.suspenseSound = suspenseSound;
        this.popupAnimator = popupAnimator;
        this.reelStopSound = reelStopSound;
        for (Symbol symbol : Symbol.values()) {
            Texture texture = new Texture(Gdx.files.internal(symbol.getAssetPath()));
            textures.put(symbol, texture);
            regions.put(symbol, new TextureRegion(texture));
        }
        lockTexture = new Texture(Gdx.files.internal(LOCK_ASSET));
        TextureRegion lockRegion = new TextureRegion(lockTexture, LOCK_X, LOCK_Y, LOCK_SIZE_X, LOCK_SIZE_Y);
        List<Symbol> strip = new ArrayList<>(machine);
        strip.add(Symbol.JOKER);
        for (int i = 0; i < SlotMachine.MAX_SYMBOL_COUNT; i++) {
            ReelActor<Symbol> reel = new ReelActor<>(strip.toArray(new Symbol[0]), regions::get);
            addTooltip(reel);
            Image lock = new Image(lockRegion);
            lock.setTouchable(Touchable.disabled);
            lock.setVisible(false);
            cells.add(new Stack(reel, lock));
            reels.add(reel);
            locks.add(lock);
        }
        setReelCount(SlotMachine.SYMBOL_COUNT);
    }

    /**
     * Affiche {@code count} rouleaux (4 avec la Machine en surchauffe) : la
     * machine de la table s'élargit d'autant ; un rouleau qui apparaît surgit.
     */
    public void setReelCount(int count) {
        if (count == reelCount) return;
        int before = reelCount;
        reelCount = count;
        table.clearChildren();
        for (int i = 0; i < count; i++) {
            table.add(cells.get(i)).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(SYMBOL_PAD);
        }
        for (int i = count; i < cells.size(); i++) reels.get(i).empty();
        table.pack();
        tableView.setPlayerReelCount(count);
        layout();
        for (int i = Math.max(0, before); i < count; i++) { // le rouleau en plus surgit
            Stack cell = cells.get(i);
            cell.setTransform(true);
            cell.setOrigin(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT / 2f);
            cell.setScale(before < 0 ? 1f : 0f);
            cell.addAction(Actions.scaleTo(1f, 1f, 0.35f, Interpolation.swingOut));
        }
    }

    /** @return le nombre de rouleaux affichés. */
    public int getReelCount() { return reelCount; }

    /** @return l'image du symbole (pour l'afficher ailleurs : panneau latéral, choix du pari). */
    public TextureRegion regionOf(Symbol symbol) { return regions.get(symbol); }

    /** @return la table contenant la ligne de symboles, à ajouter au Stage. */
    public Table getActor() { return table; }

    /**
     * Fait tourner les rouleaux jusqu'à {@code symbols}. Chaque rouleau s'arrête
     * après le précédent ; si les deux premiers symboles sont identiques (ou si
     * l'un d'eux est un Joker), le dernier ralentit et s'illumine avant de
     * s'arrêter. Les Jokers se transforment ensuite en {@code resolved}.
     *
     * @param symbols      symboles sur lesquels les rouleaux s'arrêtent (Jokers compris)
     * @param resolved     ce que vaut chaque symbole, Jokers remplacés
     * @param onJoker      appelé pour chaque Joker, au moment où il se transforme (indice du rouleau)
     * @param onAllStopped appelé quand les rouleaux sont arrêtés et les Jokers transformés
     */
    public void spin(Symbol[] symbols, Symbol[] resolved, IntConsumer onJoker, Runnable onAllStopped) {
        java.util.Set<Integer> blocked = new java.util.TreeSet<>(blockedReels);
        clear();
        setReelCount(symbols.length);
        setBlockedReels(blocked); // les rouleaux bloqués restent barrés pendant le tirage
        int     last     = symbols.length - 1;
        boolean suspense = symbols.length > 2 && symbols[last] != null && pairOrJokerBefore(symbols, last);
        reelsSpinning = 0;
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] != null) reelsSpinning++;
        }
        if (reelsSpinning == 0) {
            onAllStopped.run();
            return;
        }
        int spinning = reelsSpinning;
        spinSoundId   = reelSpinSound.loop();
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] == null) continue; // rouleau bloqué : il ne tourne pas
            int   reelIndex = i;
            float stopAt    = FIRST_STOP + i * STOP_STEP;
            float slowAt    = -1f;
            if (suspense && i == last) {
                slowAt  = stopAt;
                stopAt += SUSPENSE_TIME;
                table.addAction(Actions.sequence(Actions.delay(slowAt), Actions.run(() -> {
                    tableView.highlightReel(reelIndex, SUSPENSE_GLOW, 14f, 0f);
                    suspenseSoundId = suspenseSound.play();
                    reelSpinSound.setPitch(spinSoundId, SUSPENSE_SPIN_PITCH); // le rouleau ralentit
                })));
            }
            reels.get(i).spin(symbols[i], stopAt, slowAt, () -> {
                reelStopSound.play();
                tableView.clearReelHighlight(reelIndex);
                if (--reelsSpinning == 0) {
                    stopSpinSounds();
                    transformJokers(symbols, resolved, onJoker, onAllStopped);
                } else {
                    reelSpinSound.setVolume(spinSoundId, reelsSpinning / (float) spinning); // moins de rouleaux, moins de bruit
                }
            });
        }
    }

    /** @return {@code true} si les symboles avant le rouleau {@code last} font déjà une paire ou montrent un Joker. */
    private static boolean pairOrJokerBefore(Symbol[] symbols, int last) {
        for (int i = 0; i < last; i++) {
            if (symbols[i] == null) continue;
            if (symbols[i] == Symbol.JOKER) return true;
            for (int j = i + 1; j < last; j++) {
                if (symbols[i] == symbols[j]) return true;
            }
        }
        return false;
    }

    /** Coupe la boucle des rouleaux et le suspense s'ils sont en cours. */
    private void stopSpinSounds() {
        if (spinSoundId != -1) reelSpinSound.stop(spinSoundId);
        if (suspenseSoundId != -1) suspenseSound.stop(suspenseSoundId);
        spinSoundId     = -1;
        suspenseSoundId = -1;
    }

    /** Rouleaux arrêtés : chaque Joker brille, puis repart brièvement jusqu'au symbole qu'il remplace. */
    private void transformJokers(Symbol[] symbols, Symbol[] resolved, IntConsumer onJoker, Runnable onDone) {
        List<Integer> jokers = new ArrayList<>();
        for (int i = 0; i < symbols.length && i < reels.size(); i++) {
            if (symbols[i] == Symbol.JOKER && resolved[i] != Symbol.JOKER) jokers.add(i);
        }
        if (jokers.isEmpty()) {
            onDone.run();
            return;
        }
        int[] remaining = { jokers.size() };
        for (int k = 0; k < jokers.size(); k++) {
            int reelIndex = jokers.get(k);
            tableView.highlightReel(reelIndex, JOKER_GLOW, 12f, 0f);
            table.addAction(Actions.delay(JOKER_SHOW_TIME + k * JOKER_STEP, Actions.run(() -> {
                onJoker.accept(reelIndex);
                reels.get(reelIndex).spin(resolved[reelIndex], JOKER_SPIN_TIME, -1f, () -> {
                    reelStopSound.play();
                    tableView.clearReelHighlight(reelIndex);
                    if (--remaining[0] == 0) onDone.run();
                });
            })));
        }
    }

    /** @return le centre (Stage) du rouleau {@code reel}. */
    public Vector2 getReelCenter(int reel) {
        return reels.get(reel).localToStageCoordinates(new Vector2(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT / 2f));
    }

    /** Vide les fenêtres des rouleaux, les libère et arrête tout défilement (nouveau combat). */
    public void clear() {
        stopSpinSounds();
        hitDetails.clear();
        table.clearActions();
        reels.forEach(ReelActor::empty);
        for (int i = 0; i < reelCount; i++) tableView.clearReelHighlight(i);
        setBlockedReel(-1);
    }

    /**
     * Barre le rouleau {@code reel} (Rouleau interdit de l'ennemi) : il se vide et
     * ne tournera pas au prochain tirage ; -1 libère tous les rouleaux.
     */
    public void setBlockedReel(int reel) {
        setBlockedReels(reel >= 0 ? java.util.Set.of(reel) : java.util.Set.of());
    }

    /** Comme {@link #setBlockedReel(int)}, pour plusieurs rouleaux à la fois ; un ensemble vide les libère tous. */
    public void setBlockedReels(java.util.Set<Integer> reelsToBlock) {
        java.util.Set<Integer> wanted = new java.util.TreeSet<>(reelsToBlock);
        blockedReels.clear();
        blockedReels.addAll(wanted);
        for (int i = 0; i < locks.size(); i++) {
            boolean blocked = wanted.contains(i);
            Image lock = locks.get(i);
            if (blocked && !lock.isVisible()) {
                reels.get(i).empty();
                lock.setVisible(true);
                lock.getColor().a = 0f;
                lock.addAction(Actions.fadeIn(0.25f));
            } else if (!blocked) {
                lock.clearActions();
                lock.setVisible(false);
            }
        }
    }

    /** Place la ligne dans les rouleaux de la machine dessinée sur la table (après un redimensionnement). */
    public void layout() {
        table.setPosition(tableView.getPlayerReelRowX(), tableView.getReelRowY());
    }

    /**
     * Affiche les résultats du tirage en textes animés, dans l'ordre où ils se
     * produisent : les cartes révélées au lancer (combos), au-dessus de chaque
     * symbole ce qu'il a fait (dégâts, gains, bouclier, vie drainée), le tir du
     * pistolet en {@code pistolAnchor}, puis le bonus de paire/jackpot et les
     * paris au-dessus de la ligne (le tour de l'ennemi, qui suit, est joué par
     * {@link EnemyView}).
     *
     * Le texte de chaque symbole attend que son animation (lancée par
     * {@code onSymbolStrike}) touche sa cible.
     *
     * @param onEventShown   reçoit chaque événement du tirage à l'instant où son
     *                       texte apparaît (tout de suite s'il n'a pas de texte)
     * @param onSymbolStrike reçoit chaque symbole de la ligne, le centre de son
     *                       rouleau et le délai avant le départ de son animation
     * @return le temps (en secondes) au bout duquel le pistolet tire, ou -1 s'il ne tire pas
     */
    public float playResultPopups(TurnResult result, Vector2 pistolAnchor, Consumer<Event> onEventShown,
                                  SymbolStrike onSymbolStrike) {
        float bonusX = table.getX() + table.getWidth() / 2f;
        float bonusY = table.getY() + table.getHeight() + POPUP_BONUS_GAP;
        float start  = 0f;
        if (result.getCardEvents().stream().anyMatch(e -> !e.getPopups().isEmpty())) {
            playEvents(result.getCardEvents(), bonusX, bonusY, 0f, onEventShown);
            start = POPUP_CARDS_TIME;
        } else {
            result.getCardEvents().forEach(onEventShown);
        }

        for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
            int slot = outcome.getSlotIndex();
            rememberHit(outcome);
            if (slot < 0 || slot >= reelCount) { // symbole hors de la ligne : pas de texte à attendre
                outcome.getEvents().forEach(onEventShown);
                continue;
            }
            Vector2 top = reels.get(slot).localToStageCoordinates(
                new Vector2(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT + POPUP_SYMBOL_GAP + slot * POPUP_SLOT_STEP));
            float strikeAt = start + slot * POPUP_SYMBOL_DELAY;
            onSymbolStrike.play(outcome, getReelCenter(slot), strikeAt);
            playEvents(outcome.getEvents(), top.x, top.y, strikeAt + POPUP_STRIKE_LEAD, onEventShown);
        }
        start += POPUP_STRIKE_LEAD;

        float shotAt = -1f;
        if (!result.getPistolEvents().isEmpty()) {
            shotAt = start + POPUP_PISTOL_DELAY;
            playEvents(result.getPistolEvents(), pistolAnchor.x, pistolAnchor.y, shotAt, onEventShown);
            start += POPUP_PISTOL_TIME;
        }

        playEvents(result.getPairOrJackpotEvents(), bonusX, bonusY, start + POPUP_BONUS_DELAY, onEventShown);
        return shotAt;
    }

    /** @return le temps que prennent les textes d'un tirage (sans la célébration d'un jackpot). */
    public static float popupsDuration(TurnResult result) {
        float duration = POPUP_ENEMY_DELAY + POPUP_STRIKE_LEAD;
        if (!result.getCardEvents().isEmpty()) duration += POPUP_CARDS_TIME;
        if (!result.getPistolEvents().isEmpty()) duration += POPUP_PISTOL_TIME;
        return duration;
    }

    /**
     * Affiche les textes de {@code events} et signale chaque événement à
     * {@code onEventShown} quand son premier texte apparaît (tout de suite s'il
     * n'a pas de texte).
     */
    private void playEvents(List<Event> events, float x, float y, float delay, Consumer<Event> onEventShown) {
        List<EffectPopup>   popups       = new ArrayList<>();
        Map<Integer, Event> eventByPopup = new HashMap<>();
        for (Event event : events) {
            List<EffectPopup> eventPopups = event.getPopups();
            if (eventPopups.isEmpty()) onEventShown.accept(event);
            else eventByPopup.put(popups.size(), event);
            popups.addAll(eventPopups);
        }
        popupAnimator.play(popups, x, y, delay, index -> {
            Event event = eventByPopup.get(index);
            if (event != null) onEventShown.accept(event);
        });
    }

    /** Lance l'animation d'un symbole du tirage. */
    @FunctionalInterface
    public interface SymbolStrike {
        /**
         * @param outcome le symbole et ce qu'il a fait
         * @param reel    centre (Stage) de son rouleau
         * @param delay   secondes avant le départ de l'animation
         */
        void play(SymbolOutcome outcome, Vector2 reel, float delay);
    }

    /** Garde le détail des coups de {@code outcome} sur l'ennemi, pour l'infobulle de son rouleau. */
    private void rememberHit(SymbolOutcome outcome) {
        if (outcome.getSlotIndex() < 0) return;
        List<String> lines = new ArrayList<>();
        for (Event event : outcome.getEvents()) {
            if (event instanceof EnemyDamagedEvent hit && hit.getBreakdown() != null) {
                if (!lines.isEmpty()) lines.add("");
                lines.addAll(hit.getBreakdown().lines(outcome.getSymbol()));
            }
        }
        if (!lines.isEmpty()) hitDetails.put(outcome.getSlotIndex(), lines);
    }

    /**
     * Affiche au survol la description du symbole arrêté sur le rouleau et,
     * après un tirage, le détail de son coup sur l'ennemi. Pas de clic : un
     * symbole ne se joue pas.
     */
    private void addTooltip(ReelActor<Symbol> reel) {
        reel.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Symbol symbol = reel.getSymbol();
                if (pointer != -1 || symbol == null) return;
                Vector2 pos = reel.localToStageCoordinates(new Vector2(0, SYMBOL_HEIGHT + TOOLTIP_GAP));
                List<String> hit = hitDetails.get(reels.indexOf(reel));
                String text = hit == null ? symbol.getDescription()
                    : symbol.getDescription() + "\n\n" + Lang.t("Détail du coup") + "\n" + String.join("\n", hit);
                tooltip.show(symbol.getDisplayName(), text, pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }
        });
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        lockTexture.dispose();
    }
}
