package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.entities.events.Event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * La ligne de symboles tirés par la machine à sous, centrée à l'écran, et les
 * textes animés des résultats du tirage. Possède les textures des symboles.
 */
public class SlotView implements Disposable {

    private static final float SYMBOL_WIDTH  = 94f;
    private static final float SYMBOL_HEIGHT = 80f;
    private static final float SYMBOL_PAD    = 8f;
    private static final float ROW_Y         = 200f;
    private static final float TOOLTIP_GAP   = 5f;

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
    public static final float  POPUP_PLAYER_GAP   = 110f;  // à droite de la barre de vie du joueur

    private final Stage               stage;
    private final Tooltip             tooltip;
    private final EffectPopupAnimator popupAnimator;
    private final Map<Symbol, Texture> textures = new EnumMap<>(Symbol.class);
    private final Table               table  = new Table();
    /** Images des symboles affichés, dans l'ordre de la ligne tirée. */
    private final List<Image>         images = new ArrayList<>();

    public SlotView(Stage stage, Tooltip tooltip, EffectPopupAnimator popupAnimator) {
        this.stage         = stage;
        this.tooltip       = tooltip;
        this.popupAnimator = popupAnimator;
        for (Symbol symbol : Symbol.values()) {
            textures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
        }
    }

    /** @return la table contenant la ligne de symboles, à ajouter au Stage. */
    public Table getActor() { return table; }

    /** Reconstruit la ligne de symboles affichés après un spin, centrée horizontalement. */
    public void show(Symbol[] symbols) {
        clear();
        for (Symbol symbol : symbols) {
            Image img = new Image(new TextureRegionDrawable(new TextureRegion(textures.get(symbol))));
            addTooltip(img, symbol);
            table.add(img).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(SYMBOL_PAD);
            images.add(img);
        }
        table.pack();
        layout();
        table.validate(); // positions des symboles connues pour placer les textes du tirage
    }

    /** Efface la ligne de symboles. */
    public void clear() {
        table.clearChildren();
        images.clear();
    }

    /** Recentre la ligne horizontalement (après un redimensionnement). */
    public void layout() {
        table.setPosition((stage.getViewport().getWorldWidth() - table.getWidth()) / 2f, ROW_Y);
    }

    /**
     * Affiche les résultats du tirage en textes animés, dans l'ordre où ils se
     * produisent : au-dessus de chaque symbole ce qu'il a fait (dégâts, gains,
     * bouclier, vie drainée), puis le bonus de paire/jackpot au-dessus de la
     * ligne, puis la riposte de l'ennemi (vie perdue, renvoi) en {@code riposteAnchor}.
     */
    public void playResultPopups(TurnResult result, Vector2 riposteAnchor) {
        for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
            int slot = outcome.getSlotIndex();
            if (slot < 0 || slot >= images.size()) continue;
            Vector2 top = images.get(slot).localToStageCoordinates(
                new Vector2(SYMBOL_WIDTH / 2f, SYMBOL_HEIGHT + POPUP_SYMBOL_GAP + slot * POPUP_SLOT_STEP));
            popupAnimator.play(popupsOf(outcome.getEvents()), top.x, top.y, slot * POPUP_SYMBOL_DELAY);
        }

        popupAnimator.play(popupsOf(result.getPairOrJackpotEvents()),
            table.getX() + table.getWidth() / 2f,
            table.getY() + table.getHeight() + POPUP_BONUS_GAP,
            POPUP_BONUS_DELAY);

        popupAnimator.play(popupsOf(result.getEnemyTurnEvents()), riposteAnchor.x, riposteAnchor.y, POPUP_ENEMY_DELAY);
    }

    /** @return les textes de tous les événements donnés, dans l'ordre. */
    private static List<EffectPopup> popupsOf(List<Event> events) {
        return events.stream().flatMap(event -> event.getPopups().stream()).toList();
    }

    /** Affiche la description du symbole au survol. Pas de clic : un symbole ne se joue pas. */
    private void addTooltip(Image symbolImage, Symbol symbol) {
        symbolImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = symbolImage.localToStageCoordinates(new Vector2(0, SYMBOL_HEIGHT + TOOLTIP_GAP));
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
