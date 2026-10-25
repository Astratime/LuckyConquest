package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.ExplorationArt;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.Textures;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.DungeonRewardsOverlay;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MenuOption;
import fr.astratime.lucky.views.MinimumScreenViewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Exploration : choix d'un lieu, puis d'un donjon sur sa carte, devant le décor
 * du casino assombri (comme la Tour des épreuves).
 *
 * À gauche, un panneau liste les lieux à explorer ; à droite, la carte du lieu
 * sélectionné, avec l'entrée de chacun de ses donjons (Pique, Trèfle, Cœur,
 * Carreau). Un clic sur une entrée (ou les flèches) choisit le donjon : son
 * nom, son récit, ses ennemis et le contenu de son coffre s'affichent à côté de
 * la carte. « Entrer » (ou Entrée, ou un second clic) lance ses deux combats
 * (voir {@link DungeonRun}, {@link GameScreen}). Échap ou « Retour » ramène au
 * menu principal.
 */
public class ExplorationScreen extends ScreenAdapter {

    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    private static final String HOVER_SOUND = "sounds/ui/menu_hover.ogg";
    private static final String CHIP_PATH   = "menu/chip_spin.png";
    private static final String MUSIC       = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL = 0.3f;

    private static final float MIN_WIDTH    = 1600f;
    private static final float MIN_HEIGHT   = 1080f;
    private static final float MARGIN       = 60f;
    private static final float TITLE_TOP    = 110f;    // du haut de l'écran au centre du titre
    private static final float LIST_WIDTH   = 420f;
    private static final float ROW_WIDTH    = 340f;
    private static final float ROW_HEIGHT   = 76f;
    private static final float ROW_GAP      = 18f;
    private static final float PANEL_PAD    = 40f;
    private static final float PANEL_TOP    = 210f;    // du haut de l'écran au haut des panneaux
    private static final float BOTTOM_SPACE = 130f;    // sous les panneaux : boutons
    private static final float INFO_WIDTH   = 300f;    // colonne du donjon choisi, à droite de la carte
    private static final float GATE_HOVER   = 1.12f;
    private static final float RULE_GAP     = 24f;     // entre le bas de la carte et la règle du lieu
    private static final float FADE_TIME    = 0.4f;
    /** Opacité du nom d'un lieu fermé, dans la liste. */
    private static final float LOCKED_ALPHA = 0.4f;
    /** Teinte de la carte et des entrées d'un lieu fermé. */
    private static final Color LOCKED_TINT  = new Color(0.35f, 0.35f, 0.4f, 1f);
    /** Taille de la pastille « vaincu », en part de la taille d'une entrée de donjon. */
    private static final float BADGE_SHARE  = 0.42f;
    /** Taille de la pastille d'un lieu entièrement vidé, dans la liste. */
    private static final float ROW_BADGE    = 36f;

    private final LuckyGame       luckyGame;
    private final Stage           stage;
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    /** Première visite : le Croupier présente l'écran. */
    private final FirstVisitGuide firstVisit;
    private final CardTextures    cardTextures = new CardTextures();
    private final EnemyTextures   enemyTextures = new EnemyTextures(cardTextures);
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Texture         chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
    /** Pastille verte cochée : donjon dont le roi est battu, lieu dont les quatre rois le sont. */
    private final Texture         badgeTexture = Textures.clearedBadge();
    private final Sound           clickSound;
    private final Sound           hoverSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont = Fonts.jersey(96, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      rowFont   = Fonts.jersey(50, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      placeFont = Fonts.jersey(64, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      nameFont  = Fonts.jersey(44, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont  = Fonts.jersey(30, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      gateFont  = Fonts.jersey(26, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      ruleFont  = Fonts.jersey(32, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      ruleBody  = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    /** Nom de chaque carte, par id, pour le contenu des coffres. */
    private final Map<String, String> cardNames;

    private final Image            veil;
    private final Label            title;
    private final Image            listPanel;
    private final Image            detailPanel;
    private final List<MenuOption> rows = new ArrayList<>();
    private final Label            placeLabel;
    private final Image            mapFrame;
    private final Image            map;
    private final List<Image>      gates = new ArrayList<>();
    private final List<Label>      gateLabels = new ArrayList<>();
    /** Pastilles des donjons vaincus du lieu montré (suivent leur entrée, voir {@link #badgeFor}). */
    private final List<Image>      badges = new ArrayList<>();
    /** Dans la liste, à droite de chaque lieu ouvert : donjons vidés « 2/4 », ou une pastille s'ils le sont tous. */
    private final List<Label>      rowCounts = new ArrayList<>();
    private final List<Image>      rowBadges = new ArrayList<>();
    private final Label            dungeonName;
    private final Label            dungeonText;
    /** Sous la carte : le nom de la règle du lieu, puis ce qu'elle fait. */
    private final Label            ruleName;
    private final Label            ruleText;
    private final TextButton       enterButton;
    private final TextButton       backButton;
    /** Choix rapide du deck joué parmi les emplacements enregistrés à la Table du croupier. */
    private final TextButton       deckButton;
    /** Rejoue la présentation du Croupier. */
    private final TextButton       tutorialButton;
    private final TextButton       rewardsButton;
    private final DungeonRewardsOverlay rewards;
    private final Image            fade;

    private Place   place = Place.values()[0];
    /** Profil du joueur : les donjons vidés ouvrent les lieux suivants. */
    private final PlayerProfile profile;
    private int     selected = -1;   // donjon choisi sur la carte, -1 : aucun
    private float   mapScale = 4f;
    private boolean leaving;

    public ExplorationScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.profile   = luckyGame.getProfile();
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        hoverSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(HOVER_SOUND)), audio);
        music      = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);
        Function<String, Card> factory = CardLoader.cardFactory();
        cardNames = new HashMap<>();
        for (Place p : Place.values()) {
            for (Dungeon dungeon : p.getDungeons()) {
                for (Dungeon.Loot loot : dungeon.getLoot()) cardNames.put(loot.cardId(), factory.apply(loot.cardId()).getName());
            }
        }

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.6f);
        veil.setTouchable(Touchable.disabled);
        title = new Label(Lang.t("EXPLORATION"), new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        listPanel   = new Image(hud.panelDrawable());
        detailPanel = new Image(hud.panelDrawable());
        listPanel.setTouchable(Touchable.disabled);
        detailPanel.setTouchable(Touchable.disabled);

        Label.LabelStyle rowStyle = new Label.LabelStyle(rowFont, Color.WHITE);
        Place[] places = Place.values();
        for (int i = 0; i < places.length; i++) {
            Place rowPlace = places[i];
            MenuOption row = new MenuOption(rowPlace.getShortName(), rowStyle, hud.insetDrawable(),
                new TextureRegion(chipTexture), ROW_WIDTH, ROW_HEIGHT);
            row.setSelected(rowPlace == place);
            if (!profile.isOpen(rowPlace)) row.getColor().a = LOCKED_ALPHA; // lieu fermé : à peine visible
            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) { selectPlace(rowPlace); }
            });
            rows.add(row);
            int cleared = clearedCount(rowPlace);
            int total = rowPlace.getDungeons().size();
            Label count = new Label(cleared + "/" + total, new Label.LabelStyle(gateFont, Palette.CREAM));
            count.setTouchable(Touchable.disabled);
            count.setVisible(profile.isOpen(rowPlace) && cleared < total);
            rowCounts.add(count);
            Image rowBadge = pixelImage(badgeTexture);
            rowBadge.setTouchable(Touchable.disabled);
            rowBadge.setVisible(cleared == total);
            rowBadges.add(rowBadge);
        }

        placeLabel = new Label("", new Label.LabelStyle(placeFont, Color.WHITE));
        mapFrame   = new Image(hud.insetDrawable());
        map        = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.map(place))));
        map.setTouchable(Touchable.disabled);
        dungeonName = new Label("", new Label.LabelStyle(nameFont, Color.WHITE));
        dungeonName.setWrap(true);
        dungeonText = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        dungeonText.setWrap(true);
        dungeonText.setAlignment(Align.topLeft);
        ruleName = new Label("", new Label.LabelStyle(ruleFont, Color.WHITE));
        ruleText = new Label("", new Label.LabelStyle(ruleBody, Color.WHITE));
        ruleText.setWrap(true);
        ruleText.setAlignment(Align.topLeft);
        enterButton = buttons.createAction(Lang.t("Entrer"), clickSound, this::launch);
        backButton  = buttons.create(Lang.t("Retour"), clickSound, this::onBack);
        tutorialButton = buttons.create(Lang.t("Tutoriel"), clickSound, this::replayGuide);
        rewardsButton  = buttons.create(Lang.t("Récompenses"), clickSound, this::showRewards);
        rewards = new DungeonRewardsOverlay(stage, hud, cardTextures, factory);
        deckButton  = buttons.create(deckText(), clickSound, this::nextDeck);
        // Un seul deck enregistré (ou le mode ADMIN, qui a le sien) : rien à choisir.
        deckButton.setVisible(!profile.isAdmin() && profile.nextUsedDeckSlot(profile.getDeckSlot()) != profile.getDeckSlot());

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
        stage.addActor(listPanel);
        stage.addActor(detailPanel);
        rows.forEach(stage::addActor);
        rowCounts.forEach(stage::addActor);
        rowBadges.forEach(stage::addActor);
        stage.addActor(placeLabel);
        stage.addActor(mapFrame);
        stage.addActor(map);
        stage.addActor(dungeonName);
        stage.addActor(dungeonText);
        stage.addActor(ruleName);
        stage.addActor(ruleText);
        stage.addActor(enterButton);
        stage.addActor(deckButton);
        stage.addActor(backButton);
        stage.addActor(tutorialButton);
        stage.addActor(rewardsButton);
        stage.addActor(rewards.getActor());
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        selectPlace(place);
        firstVisit = new FirstVisitGuide(stage, hud, clickSound, luckyGame.getProfile(), PlayerProfile.GUIDE_EXPLORATION,
            Lang.t("L'Exploration. Choisis un lieu, puis un de ses donjons."),
            Lang.t("Dans chaque donjon, un soldat, puis son roi. Bats le roi : son coffre s'ouvre. Une carte et des "
                + "pièces."),
            Lang.t("Seuls ces combats rapportent des pièces. Vide les quatre donjons d'un lieu pour ouvrir le "
                + "suivant."));
        fade.toFront();
    }

    // -------------------------------------------------------------------------
    // Sélection et lancement
    // -------------------------------------------------------------------------

    /** Montre la carte de {@code newPlace} et l'entrée de ses donjons. */
    private void selectPlace(Place newPlace) {
        place = newPlace;
        for (int i = 0; i < rows.size(); i++) rows.get(i).setSelected(Place.values()[i] == place);
        placeLabel.setText(place.getName().toUpperCase());
        PlaceRule rule = place.getRule();
        boolean hasRule = rule != PlaceRule.NONE;
        ruleName.setText(hasRule ? Lang.f("Règle du lieu : {0}", rule.getName()) : "");
        ruleText.setText(hasRule ? rule.getDescription() : "");
        ruleName.setVisible(hasRule);
        ruleText.setVisible(hasRule);
        ((TextureRegionDrawable) map.getDrawable()).setRegion(new TextureRegion(enemyTextures.map(place)));
        boolean open = profile.isOpen(place);
        map.setColor(open ? Color.WHITE : LOCKED_TINT);
        gates.forEach(Actor::remove);
        gateLabels.forEach(Actor::remove);
        badges.forEach(Actor::remove);
        gates.clear();
        gateLabels.clear();
        badges.clear();
        List<Dungeon> dungeons = place.getDungeons();
        for (int i = 0; i < dungeons.size(); i++) {
            int index = i;
            Dungeon dungeon = dungeons.get(i);
            Image gate = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.gate(dungeon))));
            if (!open) gate.setColor(LOCKED_TINT);
            gate.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer != -1) return;
                    hoverSound.play();
                    if (profile.isOpen(place)) gate.setColor(Palette.GOLD_PALE);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (pointer == -1) gate.setColor(profile.isOpen(place) ? Color.WHITE : LOCKED_TINT);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (rewards.isShown()) return;
                    if (selected == index) {
                        clickSound.play();
                        launch();
                    } else {
                        selectDungeon(index, false);
                    }
                }
            });
            // Clic droit : le détail de ses récompenses (pièces, cartes du coffre et leurs effets).
            ClickListener rightClick = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectDungeon(index, false);
                    clickSound.play();
                    showRewards();
                }
            };
            rightClick.setButton(Input.Buttons.RIGHT);
            gate.addListener(rightClick);
            Label label = new Label(dungeon.getShortName(), new Label.LabelStyle(gateFont, Color.WHITE));
            label.setAlignment(Align.center);
            label.setTouchable(Touchable.disabled);
            gates.add(gate);
            gateLabels.add(label);
            stage.addActor(gate);
            stage.addActor(label);
            if (profile.isCleared(dungeon)) {
                Image badge = badgeFor(gate);
                badges.add(badge);
                stage.addActor(badge);
            }
        }
        if (firstVisit != null) firstVisit.toFront(); // les portes passent sous le Croupier
        fade.toFront();
        selected = -1;
        selectDungeon(0, false);
    }

    /** Choisit le donjon {@code index} de la carte : son entrée s'agrandit, ses informations s'affichent. */
    private void selectDungeon(int index, boolean sound) {
        if (index == selected) return;
        if (sound) hoverSound.play();
        selected = index;
        Dungeon dungeon = place.getDungeons().get(index);
        for (int i = 0; i < gates.size(); i++) {
            gates.get(i).setScale(i == index ? GATE_HOVER : 1f);
            gateLabels.get(i).setColor(i == index ? Palette.GOLD : Color.WHITE);
        }
        dungeonName.setText(dungeon.getName());
        StringBuilder text = new StringBuilder();
        if (!profile.isOpen(place)) {
            // Fermé : la description attendra, place à ce qu'il faut faire pour l'ouvrir.
            String previous = place.getPrevious().getName();
            text.append(Lang.f("Lieu fermé. Termine d'abord {0}.",
                Character.toLowerCase(previous.charAt(0)) + previous.substring(1)));
            text.append('\n').append(Lang.f("Donjons vaincus là-bas : {0}/{1}",
                clearedCount(place.getPrevious()), place.getPrevious().getDungeons().size()));
        } else {
            text.append(dungeon.getDescription());
            if (profile.isCleared(dungeon)) text.append('\n').append(Lang.t("Déjà vidé."));
        }
        text.append("\n\n").append(Lang.f("Combat 1 : {0} ({1} PV)", dungeon.getSoldier().getDisplayName(),
            PlayerProfile.formatCoins(dungeon.getSoldier().getMaxHp())));
        text.append('\n').append(Lang.f("Combat 2 : {0} ({1} PV)", dungeon.getKing().getDisplayName(),
            PlayerProfile.formatCoins(dungeon.getKing().getMaxHp())));
        text.append("\n\n").append(Lang.f("Coffre : {0} pièces + 1 carte",
            PlayerProfile.formatCoins(place.getChestCoins())));
        for (Dungeon.Loot loot : dungeon.getLoot()) {
            text.append('\n').append(Lang.f("{0} {1} %", cardNames.get(loot.cardId()), loot.percent()));
        }
        text.append("\n\n").append(Lang.t("Clic droit ou R : détail des récompenses."));
        dungeonText.setText(text);
        for (Actor actor : List.of(dungeonName, dungeonText)) {
            actor.clearActions();
            actor.getColor().a = 0f;
            actor.addAction(Actions.fadeIn(0.25f, Interpolation.pow2Out));
        }
        layout();
    }

    /** @return le nombre de donjons de {@code p} dont le roi est battu. */
    private int clearedCount(Place p) {
        return (int) p.getDungeons().stream().filter(profile::isCleared).count();
    }

    /** @return une image pixel art affichée sans lissage. */
    private static Image pixelImage(Texture texture) {
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return new Image(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    /**
     * @return la pastille « vaincu » de l'entrée {@code gate} : posée sur son coin
     *         en haut à droite, elle la suit quand elle grandit ou flotte.
     */
    private Image badgeFor(Image gate) {
        Image badge = new Image(new TextureRegionDrawable(new TextureRegion(badgeTexture))) {
            @Override
            public void act(float delta) {
                super.act(delta);
                float scale = gate.getScaleX();
                float size = gate.getWidth() * BADGE_SHARE * scale;
                float right = gate.getX() + gate.getWidth() / 2f + gate.getWidth() * scale / 2f;
                float top = gate.getY() + gate.getHeight() * scale;
                setBounds(right - size * 0.85f, top - size * 0.85f, size, size);
            }
        };
        badge.setTouchable(Touchable.disabled);
        badge.act(0f);
        return badge;
    }

    /** L'entrée du donjon choisi flotte doucement (à relancer après chaque mise en page). */
    private void bobSelectedGate() {
        for (int i = 0; i < gates.size(); i++) {
            Image gate = gates.get(i);
            gate.clearActions();
            if (i == selected) {
                gate.addAction(Actions.forever(Actions.sequence(
                    Actions.moveBy(0f, 6f, 0.6f, Interpolation.sine), Actions.moveBy(0f, -6f, 0.6f, Interpolation.sine))));
            }
        }
    }

    /** Joue le deck enregistré suivant. */
    private void nextDeck() {
        profile.selectDeckSlot(profile.nextUsedDeckSlot(profile.getDeckSlot()));
        deckButton.setText(deckText());
    }

    /** @return le texte du bouton du deck joué (un clic passe au deck enregistré suivant). */
    private String deckText() {
        return Lang.f("Deck joué : {0}", profile.getDeckSlot() + 1);
    }

    /** Entre dans le donjon choisi : fondu au noir puis combat contre son soldat. */
    private void launch() {
        if (leaving || selected < 0 || !profile.isOpen(place)) return;
        Dungeon dungeon = place.getDungeons().get(selected);
        fadeOutThen(() -> {
            luckyGame.setScreen(new GameScreen(luckyGame, new DungeonRun(dungeon)));
            dispose();
        });
    }

    /** Ouvre le détail des récompenses du donjon choisi. */
    private void showRewards() {
        if (leaving || selected < 0) return;
        rewards.show(place.getDungeons().get(selected), profile);
        fade.toFront();
    }

    /** Retour au menu principal. */
    private void onBack() {
        if (leaving) return;
        fadeOutThen(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        });
    }

    private void fadeOutThen(Runnable next) {
        leaving = true;
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le titre, les deux panneaux, la carte, ses entrées, les informations et les boutons. */
    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        decor.layout(width, height);
        decor.setPosition(0f, 0f);
        veil.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);

        float panelTop    = height - PANEL_TOP;
        float panelBottom = BOTTOM_SPACE;
        float panelHeight = panelTop - panelBottom;
        listPanel.setBounds(MARGIN, panelBottom, LIST_WIDTH, panelHeight);
        float rowX = MARGIN + (LIST_WIDTH - ROW_WIDTH) / 2f;
        for (int i = 0; i < rows.size(); i++) {
            float rowY = panelTop - PANEL_PAD - ROW_HEIGHT - i * (ROW_HEIGHT + ROW_GAP);
            rows.get(i).setPosition(rowX, rowY);
            Label count = rowCounts.get(i);
            count.pack();
            count.setPosition(rowX + ROW_WIDTH - 14f - count.getWidth(), rowY + (ROW_HEIGHT - count.getHeight()) / 2f);
            rowBadges.get(i).setBounds(rowX + ROW_WIDTH - 10f - ROW_BADGE, rowY + (ROW_HEIGHT - ROW_BADGE) / 2f,
                ROW_BADGE, ROW_BADGE);
        }

        float detailX     = MARGIN + LIST_WIDTH + MARGIN / 2f;
        float detailWidth = width - detailX - MARGIN;
        detailPanel.setBounds(detailX, panelBottom, detailWidth, panelHeight);
        float innerX     = detailX + PANEL_PAD;
        float innerWidth = detailWidth - PANEL_PAD * 2;

        placeLabel.pack();
        placeLabel.setPosition(innerX, panelTop - PANEL_PAD - placeLabel.getHeight());

        // La carte, à la plus grande taille entière qui laisse la place à la colonne du donjon.
        float mapTop       = placeLabel.getY() - 20f;
        float maxMapWidth  = innerWidth - INFO_WIDTH - PANEL_PAD;
        float maxMapHeight = mapTop - panelBottom - PANEL_PAD;
        // La règle du lieu, sous la carte : on lui garde sa place avant de choisir la taille de la carte.
        float ruleHeight = 0f;
        if (ruleName.isVisible()) {
            ruleName.pack();
            ruleText.setWidth(maxMapWidth);
            ruleText.pack();
            ruleText.setWidth(maxMapWidth);
            ruleHeight = RULE_GAP + ruleName.getHeight() + 4f + ruleText.getHeight();
            maxMapHeight -= ruleHeight;
        }
        mapScale = 1f;
        while ((mapScale + 1) * ExplorationArt.MAP_WIDTH <= maxMapWidth
            && (mapScale + 1) * ExplorationArt.MAP_HEIGHT <= maxMapHeight) {
            mapScale += 1f;
        }
        float mapWidth  = ExplorationArt.MAP_WIDTH * mapScale;
        float mapHeight = ExplorationArt.MAP_HEIGHT * mapScale;
        float mapX = innerX, mapY = mapTop - mapHeight;
        map.setBounds(mapX, mapY, mapWidth, mapHeight);
        float frame = 10f;
        mapFrame.setBounds(mapX - frame, mapY - frame, mapWidth + frame * 2, mapHeight + frame * 2);
        if (ruleName.isVisible()) {
            ruleText.setWidth(mapWidth);
            ruleText.pack();
            ruleText.setWidth(mapWidth);
            ruleName.setPosition(mapX, mapY - RULE_GAP - ruleName.getHeight());
            ruleText.setPosition(mapX, ruleName.getY() - 4f - ruleText.getHeight());
        }

        // Entrées des donjons : le bas de chaque entrée au bout de son chemin.
        float gateSize = ExplorationArt.GATE_SIZE * mapScale * 0.75f;
        for (int i = 0; i < gates.size(); i++) {
            int[][] spots = ExplorationArt.gates(place);
            int[] spot = spots[i % spots.length];
            float centerX = mapX + spot[0] * mapScale;
            float bottomY = mapY + (ExplorationArt.MAP_HEIGHT - spot[1] - 7) * mapScale;
            Image gate = gates.get(i);
            gate.setBounds(centerX - gateSize / 2f, bottomY, gateSize, gateSize);
            gate.setOrigin(Align.bottom);
            Label label = gateLabels.get(i);
            label.pack();
            label.setPosition(centerX - label.getWidth() / 2f, bottomY - label.getHeight());
        }

        float infoX = mapX + mapWidth + PANEL_PAD;
        float infoWidth = detailX + detailWidth - PANEL_PAD - infoX;
        dungeonName.setWidth(infoWidth);
        dungeonName.pack();
        dungeonName.setWidth(infoWidth);
        dungeonName.setPosition(infoX, mapTop - dungeonName.getHeight());
        dungeonText.setWidth(infoWidth);
        float textTop = dungeonName.getY() - 12f;
        dungeonText.setHeight(Math.max(60f, textTop - panelBottom - PANEL_PAD));
        dungeonText.setPosition(infoX, panelBottom + PANEL_PAD);

        enterButton.setPosition(detailX + detailWidth - enterButton.getWidth(), (BOTTOM_SPACE - enterButton.getHeight()) / 2f);
        deckButton.setPosition(enterButton.getX() - 24f - deckButton.getWidth(), enterButton.getY());
        backButton.setPosition(MARGIN, (BOTTOM_SPACE - backButton.getHeight()) / 2f);
        tutorialButton.setPosition(backButton.getX() + backButton.getWidth() + 20f, backButton.getY());
        float rewardsRight = deckButton.isVisible() ? deckButton.getX() : enterButton.getX();
        rewardsButton.setPosition(rewardsRight - 24f - rewardsButton.getWidth(), enterButton.getY());
        if (rewards.isShown()) rewards.layout();
        bobSelectedGate(); // les entrées viennent d'être replacées
    }

    /**
     * Stage d'abord (souris), puis clavier : gauche/droite pour choisir un
     * donjon, Entrée ou Espace pour y entrer, Échap pour revenir au menu principal.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving) return false;
                if (rewards.isShown()) { // le détail des récompenses ouvert : Échap (ou Entrée) le referme
                    if (keycode != Input.Keys.ESCAPE && keycode != Input.Keys.ENTER && keycode != Input.Keys.R) return true;
                    clickSound.play();
                    rewards.hide();
                    return true;
                }
                int count = place.getDungeons().size();
                switch (keycode) {
                    case Input.Keys.LEFT, Input.Keys.A, Input.Keys.UP, Input.Keys.W ->
                        selectDungeon((selected + count - 1) % count, true);
                    case Input.Keys.RIGHT, Input.Keys.D, Input.Keys.DOWN, Input.Keys.S ->
                        selectDungeon((selected + 1) % count, true);
                    case Input.Keys.ENTER, Input.Keys.SPACE -> {
                        clickSound.play();
                        launch();
                    }
                    case Input.Keys.ESCAPE -> {
                        clickSound.play();
                        onBack();
                    }
                    case Input.Keys.R -> {
                        clickSound.play();
                        showRewards();
                    }
                    default -> { return false; }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        music.update();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        firstVisit.dispose();
        decor.dispose();
        enemyTextures.dispose();
        cardTextures.dispose();
        buttons.dispose();
        chipTexture.dispose();
        badgeTexture.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(rowFont);
        Fonts.release(placeFont);
        Fonts.release(nameFont);
        Fonts.release(bodyFont);
        Fonts.release(gateFont);
        rewards.dispose();
    }

    /** Bouton « Tutoriel » : le Croupier présente l'écran de nouveau. */
    private void replayGuide() {
        firstVisit.replay();
        fade.toFront();
    }
}
