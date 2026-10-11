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
import fr.astratime.lucky.i18n.Lang;
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
 *
 * Une page peut avoir des onglets ({@link Tabs}, ex : Visuel, Audio,
 * Accessibilité) : une rangée de boutons sous l'en-tête ; un clic, Tab ou
 * Page haut/bas en change.
 */
public class OptionsMenu implements Disposable {

    private static final String CHIP_PATH     = "menu/chip_spin.png";
    private static final float  OPTION_WIDTH  = 580f;   // assez pour « Affichage : plein écran »
    private static final float  OPTION_HEIGHT = 76f;
    private static final float  OPTION_GAP    = 14f;
    private static final float  PANEL_SIDE    = 100f;   // de chaque côté des options : place des jetons
    private static final float  PANEL_PAD     = 30f;
    private static final float  CAPTION_SPACE = 56f;    // en-tête au-dessus des options
    private static final float  TABS_SPACE    = CasinoButtons.HEIGHT + 24f; // rangée d'onglets sous l'en-tête
    private static final float  TAB_GAP       = 12f;
    private static final Color  TAB_DIMMED    = new Color(0.55f, 0.55f, 0.55f, 1f); // onglet non choisi (comme la boutique)
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

    /**
     * Les onglets d'une page : leurs noms, celui qui est affiché, l'action qui en
     * affiche un autre (elle redonne les options de la page), et le nombre
     * d'options de l'onglet le plus long (le panneau garde cette hauteur d'un onglet à l'autre).
     */
    public record Tabs(List<String> labels, int selected, IntConsumer onSelect, int rows) { }

    private final Stage       stage;
    private final HudTextures hud;
    private final Sound       clickSound;
    private final Sound       hoverSound;
    private final Texture     chipTexture;
    private final BitmapFont  optionFont;
    private final BitmapFont  captionFont;
    private final Image       panel;
    private final Label       caption;

    private final List<MenuOption> options = new ArrayList<>();
    private final List<Entry>      entries = new ArrayList<>();
    private final CasinoButtons    tabButtons = new CasinoButtons();
    private final List<CasinoButton> tabs   = new ArrayList<>();
    private Tabs    tabsShown;
    private int     selected;
    private boolean active = true;
    private float   centerX, centerY;

    /**
     * Ajoute le panneau (vide) à {@code stage}.
     *
     * @param clickSound bruitage joué à chaque option validée ou réglée
     * @param hoverSound bruitage joué quand une autre option est sélectionnée (survol ou flèches)
     */
    public OptionsMenu(Stage stage, HudTextures hud, Sound clickSound, Sound hoverSound) {
        this.stage      = stage;
        this.hud        = hud;
        this.clickSound = clickSound;
        this.hoverSound = hoverSound;
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
        setEntries(captionText, newEntries, 0);
    }

    /**
     * Comme {@link #setEntries(String, List)}, l'option {@code first} sélectionnée
     * (ex : la page redessinée dans une autre langue garde l'option « Langue »).
     */
    public void setEntries(String captionText, List<Entry> newEntries, int first) {
        setEntries(captionText, null, newEntries, first);
    }

    /**
     * Comme {@link #setEntries(String, List, int)}, avec une rangée d'onglets sous
     * l'en-tête ({@code null} : aucun).
     */
    public void setEntries(String captionText, Tabs newTabs, List<Entry> newEntries, int first) {
        tabs.forEach(Actor::remove);
        tabs.clear();
        tabsShown = newTabs;
        if (newTabs != null) {
            for (int i = 0; i < newTabs.labels().size(); i++) {
                int index = i;
                CasinoButton tab = tabButtons.create(newTabs.labels().get(i), clickSound, () -> showTab(index));
                if (i == newTabs.selected()) tab.getStyle().up = tab.getStyle().down; // l'onglet affiché reste « enfoncé »
                else tab.setColor(TAB_DIMMED);
                tabs.add(tab);
                stage.addActor(tab);
            }
        }
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
        selected = Math.max(0, Math.min(first, entries.size() - 1));
        for (int i = 0; i < options.size(); i++) options.get(i).setSelected(i == selected);
        layout(centerX, centerY);
    }

    /** Hauteur d'une option et de l'espace qui la suit. */
    public static final float OPTION_STEP = OPTION_HEIGHT + OPTION_GAP;

    /** @return le nombre d'options affichées. */
    public int size() { return options.size(); }

    /** @return l'option {@code index} affichée (le guide du menu l'éclaire), ou {@code null}. */
    public Actor getOption(int index) {
        return index >= 0 && index < options.size() ? options.get(index) : null;
    }

    /** Centre le panneau en {@code (x, y)} (après un redimensionnement, ou un changement d'options). */
    public void layout(float x, float y) {
        centerX = x;
        centerY = y;
        int   rows        = Math.max(options.size(), tabsShown == null ? 0 : tabsShown.rows());
        float listHeight  = rows * OPTION_HEIGHT + Math.max(0, rows - 1) * OPTION_GAP;
        float tabsH       = tabs.isEmpty() ? 0f : TABS_SPACE;
        float captionH    = (caption.isVisible() ? CAPTION_SPACE : 0f) + tabsH;
        float panelWidth  = OPTION_WIDTH + PANEL_SIDE * 2f;
        float panelHeight = listHeight + captionH + PANEL_PAD * 2f;
        float panelX      = x - panelWidth / 2f;
        float panelY      = y - panelHeight / 2f;
        panel.setBounds(panelX, panelY, panelWidth, panelHeight);

        caption.pack();
        caption.setPosition(x - caption.getWidth() / 2f, panelY + panelHeight - PANEL_PAD - caption.getHeight());

        float tabsWidth = -TAB_GAP;
        for (CasinoButton tab : tabs) tabsWidth += tab.getWidth() + TAB_GAP;
        float tabX = x - tabsWidth / 2f;
        float tabY = panelY + panelHeight - PANEL_PAD - captionH + (TABS_SPACE - CasinoButtons.HEIGHT) / 2f;
        for (CasinoButton tab : tabs) {
            tab.setPosition(tabX, tabY);
            tabX += tab.getWidth() + TAB_GAP;
        }

        float optionY = panelY + panelHeight - PANEL_PAD - captionH - OPTION_HEIGHT;
        for (MenuOption option : options) {
            option.setPosition(x - OPTION_WIDTH / 2f, optionY);
            optionY -= OPTION_HEIGHT + OPTION_GAP;
        }
    }

    /**
     * Navigation au clavier : haut/bas pour choisir, gauche/droite pour
     * régler, Entrée ou Espace pour valider, Tab (Maj+Tab) ou Page bas/haut pour changer d'onglet.
     *
     * @return {@code true} si la touche a été utilisée
     */
    public boolean keyDown(int keycode) {
        if (!active || options.isEmpty()) return false;
        if (tabsShown != null && (keycode == Input.Keys.TAB || keycode == Input.Keys.PAGE_DOWN
                || keycode == Input.Keys.PAGE_UP)) {
            boolean back = keycode == Input.Keys.PAGE_UP || (keycode == Input.Keys.TAB
                && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)));
            int count = tabsShown.labels().size();
            clickSound.play();
            showTab((tabsShown.selected() + (back ? count - 1 : 1)) % count);
            return true;
        }
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

    /** Affiche l'onglet {@code index} (rien si c'est déjà lui, ou si le menu est inactif). */
    private void showTab(int index) {
        if (!active || tabsShown == null || index == tabsShown.selected()) return;
        tabsShown.onSelect().accept(index);
    }

    /** @return l'onglet {@code index} de la page (le guide peut l'éclairer), ou {@code null}. */
    public Actor getTab(int index) {
        return index >= 0 && index < tabs.size() ? tabs.get(index) : null;
    }

    /** Active ou ignore les clics et le clavier (ex : pendant le fondu de sortie du menu). */
    public void setActive(boolean active) { this.active = active; }

    /** @return {@code true} si le panneau montre un en-tête (page secondaire, ex : « OPTIONS »). */
    public boolean hasCaption() { return caption.isVisible(); }

    /**
     * Sélectionne l'option {@code index} et désélectionne les autres, avec le
     * bruitage de survol (sauf pour la sélection initiale d'une page).
     */
    private void select(int index) {
        if (index == selected) return;
        if (selected >= 0) hoverSound.play();
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

    /** Onglets des réglages, dans l'ordre (le menu principal y ajoute « Jeu »). */
    public static final String TAB_VISUAL = "Visuel", TAB_AUDIO = "Audio", TAB_ACCESSIBILITY = "Accessibilité",
        TAB_GAME = "Jeu";

    /**
     * @return l'onglet « Visuel », commun au menu principal et au menu pause : affichage,
     *         vitesse des animations, cartes spéciales blanches ou colorées
     */
    public static List<Entry> visualEntries(LuckyGame game, VisualSettings visual, DisplaySettings display) {
        Runnable toggleScreenMode = () -> {
            ScreenMode mode = display.getScreenMode().toggled();
            display.setScreenMode(mode);
            game.applyScreenMode(mode);
        };
        Runnable toggleColors = () -> visual.setColoredCards(!visual.isColoredCards());
        return List.of(
            new Entry(() -> Lang.f("Affichage : {0}", display.getScreenMode().getLabel()), toggleScreenMode,
                direction -> toggleScreenMode.run()),
            new Entry(() -> visual.getAnimationSpeed().label(), () -> visual.stepAnimationSpeed(1),
                visual::stepAnimationSpeed),
            new Entry(() -> visual.isColoredCards() ? Lang.t("Cartes colorées : oui") : Lang.t("Cartes colorées : non"),
                toggleColors, direction -> toggleColors.run()));
    }

    /** @return l'onglet « Audio » : volume de la musique, volume des sons. */
    public static List<Entry> audioEntries(AudioSettings audio) {
        return List.of(
            new Entry(() -> Lang.f("Musique : {0} %", AudioSettings.percent(audio.getMusicVolume())),
                () -> cycle(audio::stepMusicVolume, audio.getMusicVolume()), audio::stepMusicVolume),
            new Entry(() -> Lang.f("Sons : {0} %", AudioSettings.percent(audio.getSoundVolume())),
                () -> cycle(audio::stepSoundVolume, audio.getSoundVolume()), audio::stepSoundVolume));
    }

    /**
     * @param onEffectsChanged appelé après la bascule des effets (ex : arrêter une secousse en cours)
     * @return les réglages d'accessibilité : effets réduits, enseignes renforcées (jeu à quatre
     *         couleurs), grand texte des infobulles
     */
    public static List<Entry> accessibilityEntries(VisualSettings visual, Runnable onEffectsChanged) {
        Runnable toggleEffects = () -> {
            visual.setReducedEffects(!visual.isReducedEffects());
            onEffectsChanged.run();
        };
        Runnable toggleSuits    = () -> visual.setStrongSuitColors(!visual.isStrongSuitColors());
        Runnable toggleTooltips = () -> visual.setLargeTooltips(!visual.isLargeTooltips());
        return List.of(
            new Entry(() -> visual.isReducedEffects() ? Lang.t("Effets : réduits") : Lang.t("Effets : normaux"), toggleEffects,
                direction -> toggleEffects.run()),
            new Entry(() -> visual.isStrongSuitColors() ? Lang.t("Enseignes : renforcées") : Lang.t("Enseignes : normales"),
                toggleSuits, direction -> toggleSuits.run()),
            new Entry(() -> visual.isLargeTooltips() ? Lang.t("Infobulles : grand texte") : Lang.t("Infobulles : texte normal"),
                toggleTooltips, direction -> toggleTooltips.run()));
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
        tabButtons.dispose();
        Fonts.release(optionFont);
        Fonts.release(captionFont);
    }
}
