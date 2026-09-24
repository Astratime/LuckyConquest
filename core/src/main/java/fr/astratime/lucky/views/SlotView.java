package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.ReelActor;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Les rouleaux de la machine à sous, dans les fenêtres dessinées sur la table
 * ({@link TableView}), et les textes animés des résultats du tirage. Au lancer,
 * les rouleaux défilent puis s'arrêtent l'un après l'autre ; si les deux
 * premiers symboles sont identiques, le dernier ralentit et s'illumine pour
 * faire durer le suspense. Possède les textures des symboles.
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
    private static final Color SUSPENSE_GLOW = Color.valueOf("ffd54aff");

    // Textes des résultats du tirage : ceux de chaque symbole (de gauche à
    // droite, en escalier pour ne pas se chevaucher : les symboles sont plus
    // étroits que les textes), puis le bonus de paire/jackpot au-dessus, puis
    // la riposte de l'ennemi à droite de la barre de vie du joueur.
    private static final float POPUP_SYMBOL_GAP   = 20f;   // au-dessus du symbole
    private static final float POPUP_SLOT_STEP    = 55f;   // décalage vertical d'un symbole au suivant
    private static final float POPUP_SYMBOL_DELAY = 0.3f;  // entre deux symboles
    private static final float POPUP_BONUS_DELAY  = 1.0f;
    private static final float POPUP_BONUS_GAP    = 210f;  // au-dessus de la ligne de symboles (et de l'escalier)
    private static final float POPUP_ENEMY_DELAY  = 1.5f;
    /** Temps entre le texte « JACKPOT ! » (début de sa célébration) et la riposte de l'ennemi. */
    public static final float  RIPOSTE_AFTER_BONUS = POPUP_ENEMY_DELAY - POPUP_BONUS_DELAY;
    public static final float  POPUP_PLAYER_GAP   = 110f;  // à droite de la barre de vie du joueur

    private final TableView                  tableView;
    private final Tooltip                    tooltip;
    private final EffectPopupAnimator        popupAnimator;
    private final Sound                      reelStopSound;
    private final Map<Symbol, Texture>       textures = new EnumMap<>(Symbol.class);
    private final Map<Symbol, TextureRegion> regions  = new EnumMap<>(Symbol.class);
    private final Table                      table    = new Table();
    /** Rouleaux, de gauche à droite. */
    private final List<ReelActor<Symbol>>    reels    = new ArrayList<>();
    /** Rouleaux pas encore arrêtés pendant un lancer. */
    private int                              reelsSpinning;

    /** @param reelStopSound bruitage joué quand un rouleau s'arrête */
    public SlotView(TableView tableView, Tooltip tooltip, EffectPopupAnimator popupAnimator, Sound reelStopSound) {
        this.tableView     = tableView;
        this.tooltip       = tooltip;
        this.popupAnimator = popupAnimator;
        this.reelStopSound = reelStopSound;
        for (Symbol symbol : Symbol.values()) {
            Texture texture = new Texture(Gdx.files.internal(symbol.getAssetPath()));
            textures.put(symbol, texture);
            regions.put(symbol, new TextureRegion(texture));
        }
        for (int i = 0; i < SlotMachine.SYMBOL_COUNT; i++) {
            ReelActor<Symbol> reel = new ReelActor<>(Symbol.values(), regions::get);
            addTooltip(reel);
            table.add(reel).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(SYMBOL_PAD);
            reels.add(reel);
        }
        table.pack();
    }

    /** @return la table contenant la ligne de symboles, à ajouter au Stage. */
    public Table getActor() { return table; }

    /**
     * Fait tourner les rouleaux jusqu'à {@code symbols}. Chaque rouleau s'arrête
     * après le précédent ; si les deux premiers symboles sont identiques, le
     * dernier ralentit et s'illumine avant de s'arrêter.
     *
     * @param onAllStopped appelé quand le dernier rouleau est arrêté
     */
    public void spin(Symbol[] symbols, Runnable onAllStopped) {
        clear();
        int     last     = reels.size() - 1;
        boolean suspense = symbols.length > 2 && symbols[0] == symbols[1];
        reelsSpinning = reels.size();
        for (int i = 0; i < reels.size(); i++) {
            int   reelIndex = i;
            float stopAt    = FIRST_STOP + i * STOP_STEP;
            float slowAt    = -1f;
            if (suspense && i == last) {
                slowAt  = stopAt;
                stopAt += SUSPENSE_TIME;
                table.addAction(Actions.sequence(Actions.delay(slowAt),
                    Actions.run(() -> tableView.highlightReel(reelIndex, SUSPENSE_GLOW, 14f, 0f))));
            }
            reels.get(i).spin(symbols[i], stopAt, slowAt, () -> {
                reelStopSound.play();
                tableView.clearReelHighlight(reelIndex);
                if (--reelsSpinning == 0) onAllStopped.run();
            });
        }
    }

    /** @return le centre (Stage) du rouleau {@code reel}. */
    public Vector2 getReelCenter(int reel) {
        return reels.get(reel).localToStageCoordinates(new Vector2(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT / 2f));
    }

    /** Vide les fenêtres des rouleaux et arrête tout défilement (nouveau combat). */
    public void clear() {
        table.clearActions();
        reels.forEach(ReelActor::empty);
        for (int i = 0; i < reels.size(); i++) tableView.clearReelHighlight(i);
    }

    /** Place la ligne dans les rouleaux de la machine dessinée sur la table (après un redimensionnement). */
    public void layout() {
        table.setPosition(tableView.getReelRowX(), tableView.getReelRowY());
    }

    /**
     * Affiche les résultats du tirage en textes animés, dans l'ordre où ils se
     * produisent : au-dessus de chaque symbole ce qu'il a fait (dégâts, gains,
     * bouclier, vie drainée), puis le bonus de paire/jackpot au-dessus de la
     * ligne, puis la riposte de l'ennemi (vie perdue, renvoi) en {@code riposteAnchor}.
     *
     * @param onEventShown reçoit chaque événement du tirage à l'instant où son
     *                     texte apparaît (tout de suite s'il n'a pas de texte)
     * @param riposteDelay retard supplémentaire de la riposte (ex : laisser passer la célébration d'un jackpot)
     */
    public void playResultPopups(TurnResult result, Vector2 riposteAnchor, Consumer<Event> onEventShown,
                                 float riposteDelay) {
        for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
            int slot = outcome.getSlotIndex();
            if (slot < 0 || slot >= reels.size()) { // symbole hors de la ligne : pas de texte à attendre
                outcome.getEvents().forEach(onEventShown);
                continue;
            }
            Vector2 top = reels.get(slot).localToStageCoordinates(
                new Vector2(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT + POPUP_SYMBOL_GAP + slot * POPUP_SLOT_STEP));
            playEvents(outcome.getEvents(), top.x, top.y, slot * POPUP_SYMBOL_DELAY, onEventShown);
        }

        playEvents(result.getPairOrJackpotEvents(),
            table.getX() + table.getWidth() / 2f,
            table.getY() + table.getHeight() + POPUP_BONUS_GAP,
            POPUP_BONUS_DELAY, onEventShown);

        playEvents(result.getEnemyTurnEvents(), riposteAnchor.x, riposteAnchor.y, POPUP_ENEMY_DELAY + riposteDelay,
            onEventShown);
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

    /** Affiche au survol la description du symbole arrêté sur le rouleau. Pas de clic : un symbole ne se joue pas. */
    private void addTooltip(ReelActor<Symbol> reel) {
        reel.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Symbol symbol = reel.getSymbol();
                if (pointer != -1 || symbol == null) return;
                Vector2 pos = reel.localToStageCoordinates(new Vector2(0, SYMBOL_HEIGHT + TOOLTIP_GAP));
                tooltip.show(symbol.getDescription(), pos.x, pos.y);
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
    }
}
