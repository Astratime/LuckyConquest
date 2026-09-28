package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.DisplaySettings;
import fr.astratime.lucky.settings.ScreenMode;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Panneau d'options d'un menu (menu principal, menu pause) : un cadre, un
 * en-tête facultatif (ex : « OPTIONS ») et une liste d'options
 * ({@link MenuOption}) qui apparaissent l'une après l'autre.
 *
 * L'option sélectionnée (survol de la souris ou flèches du clavier) s'encadre
 * d'or ; Entrée, Espace ou un clic la valide, gauche/droite la règlent si elle
 * se règle (volume, bascule). Échap reste à l'écran qui l'utilise.
 */
public class OptionsMenu implements Disposable {

    private static final String CHIP_PATH     = "menu/chip_spin.png";
    private static final float  OPTION_WIDTH  = 580f;   // assez pour « Affichage : plein écran »
    private static final float  OPTION_HEIGHT = 76f;
    private static final float  OPTION_GAP    = 14f;
    private static final float  PANEL_SIDE    = 100f;   // de chaque côté des options : place des jetons
    private static final float  PANEL_PAD     = 30f;
    private static final float  CAPTION_SPACE = 56f;    // en-tête au-dessus des options
    private static final float  POP_STAGGER   = 0.06f;

    /**
     * Une option : son texte (relu après chaque réglage), son action, et son
     * réglage gauche/droite éventuel ({@code null} : elle ne se règle pas).
     */
    public record Entry(Supplier<String> text, Runnable action, IntConsumer adjust) {

        /** @return une option qui ne se règle pas (ex : « Retour »). */
        public static Entry button(String text, Runnable action) {
            return new Entry(() -> text, action, null);
        }
    }

    private final Stage       stage;
    private final HudTextures hud;
    private final Sound       clickSound;
    private final Texture     chipTexture;
    private final BitmapFont  optionFont;
    private final BitmapFont  captionFont;
    private final Image       panel;
    private final Label       caption;

    private final List<MenuOption> options = new ArrayList<>();
    private final List<Entry>      entries = new ArrayList<>();
    private int     selected;
    private boolean active = true;
    private float   centerX, centerY;

    /**
     * Ajoute le panneau (vide) à {@code stage}.
     *
     * @param clickSound bruitage joué à chaque option validée ou réglée
     */
    public OptionsMenu(Stage stage, HudTextures hud, Sound clickSound) {
        this.stage      = stage;
        this.hud        = hud;
        this.clickSound = clickSound;
        chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
        optionFont  = Fonts.jersey(58, Color.WHITE, 4f, Palette.TEXT_SHADE);
        captionFont = Fonts.jersey(40, Palette.CREAM, 3f, Palette.TEXT_SHADE);

        panel = new Image(hud.panelDrawable());
        panel.setTouchable(Touchable.disabled);
        caption = new Label("", new Label.LabelStyle(captionFont, Color.WHITE));
        stage.addActor(panel);
        stage.addActor(caption);
    }

    /**
     * Remplace les options affichées ; elles apparaissent l'une après l'autre,
     * la première sélectionnée.
     *
     * @param captionText en-tête du panneau, ou {@code null} pour aucun
     */
    public void setEntries(String captionText, List<Entry> newEntries) {
        options.forEach(Actor::remove);
        options.clear();
        entries.clear();
        entries.addAll(newEntries);
        caption.setVisible(captionText != null);
        caption.setText(captionText == null ? "" : captionText);
        Label.LabelStyle style = new Label.LabelStyle(optionFont, Color.WHITE);
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            MenuOption option = new MenuOption(entries.get(i).text().get(), style, hud.insetDrawable(),
                new TextureRegion(chipTexture), OPTION_WIDTH, OPTION_HEIGHT);
            option.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(index);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    activate(index);
                }
            });
            option.setScale(0f);
            option.addAction(Actions.sequence(Actions.delay(i * POP_STAGGER),
                Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)));
            options.add(option);
            stage.addActor(option);
        }
        selected = -1;
        select(0);
        layout(centerX, centerY);
    }

    /** Centre le panneau en {@code (x, y)} (après un redimensionnement, ou un changement d'options). */
    public void layout(float x, float y) {
        centerX = x;
        centerY = y;
        float listHeight  = options.size() * OPTION_HEIGHT + Math.max(0, options.size() - 1) * OPTION_GAP;
        float captionH    = caption.isVisible() ? CAPTION_SPACE : 0f;
        float panelWidth  = OPTION_WIDTH + PANEL_SIDE * 2f;
        float panelHeight = listHeight + captionH + PANEL_PAD * 2f;
        float panelX      = x - panelWidth / 2f;
        float panelY      = y - panelHeight / 2f;
        panel.setBounds(panelX, panelY, panelWidth, panelHeight);

        caption.pack();
        caption.setPosition(x - caption.getWidth() / 2f, panelY + panelHeight - PANEL_PAD - caption.getHeight());

        float optionY = panelY + panelHeight - PANEL_PAD - captionH - OPTION_HEIGHT;
        for (MenuOption option : options) {
            option.setPosition(x - OPTION_WIDTH / 2f, optionY);
            optionY -= OPTION_HEIGHT + OPTION_GAP;
        }
    }

    /**
     * Navigation au clavier : haut/bas pour choisir, gauche/droite pour
     * régler, Entrée ou Espace pour valider.
     *
     * @return {@code true} si la touche a été utilisée
     */
    public boolean keyDown(int keycode) {
        if (!active || options.isEmpty()) return false;
        switch (keycode) {
            case Input.Keys.UP, Input.Keys.W -> select((selected - 1 + options.size()) % options.size());
            case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % options.size());
            case Input.Keys.LEFT, Input.Keys.A -> adjust(-1);
            case Input.Keys.RIGHT, Input.Keys.D -> adjust(1);
            case Input.Keys.ENTER, Input.Keys.SPACE -> activate(selected);
            default -> { return false; }
        }
        return true;
    }

    /** Active ou ignore les clics et le clavier (ex : pendant le fondu de sortie du menu). */
    public void setActive(boolean active) { this.active = active; }

    /** @return {@code true} si le panneau montre un en-tête (page secondaire, ex : « OPTIONS »). */
    public boolean hasCaption() { return caption.isVisible(); }

    /** Sélectionne l'option {@code index} et désélectionne les autres. */
    private void select(int index) {
        if (index == selected) return;
        selected = index;
        for (int i = 0; i < options.size(); i++) options.get(i).setSelected(i == index);
    }

    /** Valide une option : bruitage du clic puis son action, et mise à jour de son texte (réglage). */
    private void activate(int index) {
        if (!active) return;
        select(index);
        clickSound.play();
        Entry entry = entries.get(index);
        entry.action().run();
        // L'action a pu changer de page : le texte n'est relu que si l'option est toujours là.
        if (index < options.size() && entries.get(index) == entry) options.get(index).setText(entry.text().get());
    }

    /** Règle l'option sélectionnée vers la gauche ({@code -1}) ou la droite ({@code +1}), si elle se règle. */
    private void adjust(int direction) {
        Entry entry = entries.get(selected);
        if (entry.adjust() == null) return;
        entry.adjust().accept(direction);
        options.get(selected).setText(entry.text().get());
        clickSound.play(); // au nouveau volume : on entend le résultat
    }

    // -------------------------------------------------------------------------
    // Réglages du jeu
    // -------------------------------------------------------------------------

    /**
     * @param onEffectsChanged appelé après la bascule des effets (ex : arrêter une secousse en cours)
     * @return les réglages du jeu, communs au menu principal et au menu pause :
     *         affichage, effets visuels, volume de la musique, volume des sons
     */
    public static List<Entry> settingsEntries(LuckyGame game, VisualSettings visual, AudioSettings audio,
                                              DisplaySettings display, Runnable onEffectsChanged) {
        Runnable toggleScreenMode = () -> {
            ScreenMode mode = display.getScreenMode().toggled();
            display.setScreenMode(mode);
            game.applyScreenMode(mode);
        };
        Runnable toggleEffects = () -> {
            visual.setReducedEffects(!visual.isReducedEffects());
            onEffectsChanged.run();
        };
        return List.of(
            new Entry(() -> "Affichage : " + display.getScreenMode().getLabel(), toggleScreenMode,
                direction -> toggleScreenMode.run()),
            new Entry(() -> visual.isReducedEffects() ? "Effets : réduits" : "Effets : normaux", toggleEffects,
                direction -> toggleEffects.run()),
            new Entry(() -> "Musique : " + AudioSettings.percent(audio.getMusicVolume()) + " %",
                () -> cycle(audio::stepMusicVolume, audio.getMusicVolume()), audio::stepMusicVolume),
            new Entry(() -> "Sons : " + AudioSettings.percent(audio.getSoundVolume()) + " %",
                () -> cycle(audio::stepSoundVolume, audio.getSoundVolume()), audio::stepSoundVolume));
    }

    /** Volume au clic : palier suivant, puis retour à 0 après le maximum. */
    private static void cycle(IntConsumer step, float current) {
        if (current >= 1f) {
            for (int i = 0; i < Math.round(1f / AudioSettings.STEP); i++) step.accept(-1);
        } else {
            step.accept(1);
        }
    }

    @Override
    public void dispose() {
        chipTexture.dispose();
        optionFont.dispose();
        captionFont.dispose();
    }
}
