package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.animations.cutscenes.BonusGameCutscene;
import fr.astratime.lucky.animations.cutscenes.SmallBonusGameCutscene;
import fr.astratime.lucky.animations.cutscenes.BossTheme;
import fr.astratime.lucky.animations.cutscenes.CroupierCutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.CutsceneKit;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.animations.cutscenes.LastDrawCutscene;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.LastDrawEvent;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.Rival;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Bestiary;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.MenuOption;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Mode Cinématique : rejoue n'importe quelle cinématique du jeu en mode ADMIN,
 * ou celles déjà vues depuis le Bestiaire (voir {@link Bestiary}).
 *
 * <p>À gauche, les catégories : Histoire, Exploration, Jeux bonus, Joueur Masqué
 * et Le Croupier. Au centre, les boss de la catégorie (pour l'Exploration, on
 * choisit d'abord le lieu, puis ses rois). À droite, comme à la Boutique, les
 * onglets du boss choisi : Entrée, Phase 2, Phase 3 (s'il en a une) et Fin ;
 * dessous, la miniature et l'explication de la cinématique. Un onglet qui a
 * plusieurs cinématiques (la fin de la Machine Originelle : le duel gagné,
 * perdu, puis la fin du chapitre) les liste au-dessus de la miniature.
 *
 * <p>Un clic sur le boss, sur la miniature ou sur « Lancer » (ou Entrée) joue la
 * cinématique par-dessus l'écran ; finie ou passée, on revient au même endroit.
 * Haut/bas : le boss ; gauche/droite (ou Page haut/bas) : les onglets ; Tab :
 * la catégorie ; Échap ou « Retour » : la liste des lieux, puis le menu principal.
 */
public class CinematicScreen extends CasinoMenuScreen {

    private static final String CHIP_PATH   = "menu/chip_spin.png";

    private static final float TITLE_TOP    = 110f;    // du haut de l'écran au centre du titre
    private static final float LIST_WIDTH   = 420f;
    private static final float ROW_WIDTH    = 340f;
    private static final float ROW_HEIGHT   = 76f;
    private static final float ROW_GAP      = 18f;
    private static final float ITEM_WIDTH   = 480f;
    private static final float ITEM_HEIGHT  = 58f;
    private static final float ITEM_GAP     = 10f;
    private static final float VARIANT_HEIGHT = 50f;
    private static final float VARIANT_GAP  = 8f;
    private static final float CHIP_ROOM    = 64f;     // jeton d'une option sélectionnée, de chaque côté
    private static final float TAB_GAP      = 16f;
    private static final float PANEL_PAD    = 40f;
    private static final float MORE_SPACE   = 50f;
    private static final float PANEL_TOP    = 210f;    // du haut de l'écran au haut des panneaux
    private static final float BOTTOM_SPACE = 130f;    // sous les panneaux : boutons
    private static final float ART_SCALE    = 4f;      // pixels du portrait
    private static final Color DIMMED       = new Color(0.55f, 0.55f, 0.55f, 1f); // onglet non choisi, comme à la Boutique

    /** Les onglets d'un boss : son entrée en scène, ses nouvelles phases, sa fin. */
    enum Moment {
        ENTRANCE, PHASE_2, PHASE_3, END;

        String label() {
            return switch (this) {
                case ENTRANCE -> Lang.t("Entrée");
                case PHASE_2 -> Lang.f("Phase {0}", 2);
                case PHASE_3 -> Lang.f("Phase {0}", 3);
                case END -> Lang.t("Fin");
            };
        }
    }

    /** Une cinématique : son nom, une ligne d'explication, son image et de quoi la créer. */
    private record Entry(String name, String description, Texture art, Function<CutsceneKit, Cutscene> create) { }

    /** Un boss (ou un jeu bonus) de la liste du centre, et ses cinématiques onglet par onglet (jamais vide). */
    private record Subject(String name, Map<Moment, List<Entry>> moments) {
        /** @return les onglets qui ont une cinématique, dans l'ordre. */
        List<Moment> tabs() { return new ArrayList<>(moments.keySet()); }
    }

    /** Un lieu de l'Exploration et ses rois. */
    private record PlaceGroup(Place place, Texture art, List<Subject> kings) { }

    /**
     * Une catégorie (une ligne du panneau de gauche) : ses boss, ou pour l'Exploration ses lieux.
     * {@code tabs} faux : pas d'onglets (les jeux bonus n'ont qu'une cinématique chacun).
     */
    private record Category(String label, String title, boolean tabs, List<Subject> subjects, List<PlaceGroup> places) {
        boolean hasPlaces() { return !places.isEmpty(); }
    }

    private final VisualSettings  settings = new VisualSettings();
    private final CardTextures    cardTextures = assets.cardTextures();
    private final EnemyTextures   enemyTextures = assets.enemyTextures();
    private final GameSounds      sounds = new GameSounds(assets.audio);
    private final ScreenShake     shake  = new ScreenShake(settings);
    private final Texture         chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
    private final BitmapFont      rowFont     = Fonts.jersey(50, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      itemFont    = Fonts.jersey(40, Color.WHITE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      variantFont = Fonts.jersey(32, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      labelFont   = Fonts.jersey(36, Palette.CREAM, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      groupFont   = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont    = Fonts.jersey(36, Color.WHITE, 2f, Palette.TEXT_SHADE);
    /** Police des répliques des cinématiques. */
    private final BitmapFont      cutsceneFont = Fonts.jersey(30, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final CutsceneKit     kit;

    private final Image             listPanel;
    private final Image             detailPanel;
    private final List<Category>    categories = new ArrayList<>();
    private final List<MenuOption>  categoryRows = new ArrayList<>();
    private final Label             categoryTitle;
    /** Les lignes du centre : les boss de la catégorie, ou les lieux de l'Exploration (refaites à chaque changement). */
    private final List<MenuOption>  itemRows = new ArrayList<>();
    /** Les onglets, un par {@link Moment} (seuls ceux du boss choisi sont visibles). */
    private final Map<Moment, TextButton> tabButtons = new EnumMap<>(Moment.class);
    /** Trait doré sous l'onglet choisi. */
    private final Image             tabMark;
    /** Les cinématiques de l'onglet, quand il en a plusieurs. */
    private final List<MenuOption>  variantRows = new ArrayList<>();
    private final Image             art;
    private final Image             artFrame;
    private final Label             description;
    private final TextButton        playButton;
    private final TextButton        backButton;
    private final Label             more;

    private int      category = -1;
    /** Lieu ouvert de l'Exploration ({@code -1} : la liste des lieux). */
    private int      place = -1;
    /** Dernier lieu choisi : la liste des lieux s'y replace. */
    private int      lastPlace;
    /** Boss choisi dans la liste du centre (ou lieu, dans la liste des lieux). */
    private int      selected = -1;
    private Moment   moment = Moment.ENTRANCE;
    /** Cinématique choisie dans l'onglet. */
    private int      variant;
    /** Première ligne visible au centre (la liste défile quand elle est trop longue). */
    private int      scroll;
    private boolean  wheel;
    private Cutscene cutscene;

    public CinematicScreen(LuckyGame luckyGame) {
        super(luckyGame, Lang.t("CINÉMATIQUES"), 96, VEIL_ALPHA);
        kit        = new CutsceneKit(settings, shake, sounds, enemyTextures, cutsceneFont);
        listPanel   = new Image(hud.panelDrawable());
        detailPanel = new Image(hud.panelDrawable());
        listPanel.setTouchable(Touchable.disabled);
        detailPanel.setTouchable(Touchable.disabled);

        buildCategories();
        Label.LabelStyle rowStyle = new Label.LabelStyle(rowFont, Color.WHITE);
        for (int i = 0; i < categories.size(); i++) {
            int index = i;
            MenuOption row = new MenuOption(categories.get(i).label(), rowStyle, hud.insetDrawable(),
                new TextureRegion(chipTexture), ROW_WIDTH, ROW_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1 && !isPlaying()) selectCategory(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isPlaying()) return;
                    if (category == index && place >= 0) closePlace(); // l'Exploration recliquée : retour aux lieux
                    else selectCategory(index, false);
                }
            });
            categoryRows.add(row);
        }

        categoryTitle = new Label("", new Label.LabelStyle(groupFont, Color.WHITE));
        for (Moment each : Moment.values()) {
            TextButton tab = buttons.create(each.label(), clickSound, () -> selectMoment(each, 0, false));
            tabButtons.put(each, tab);
        }
        tabMark     = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        tabMark.setColor(Palette.GOLD);
        tabMark.setTouchable(Touchable.disabled);
        artFrame    = new Image(hud.insetDrawable());
        art         = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.portrait(EnemyKind.COMETE))));
        art.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activate();
            }
        });
        description = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        description.setWrap(true);
        description.setAlignment(Align.topLeft);
        description.setTouchable(Touchable.disabled);
        more = new Label("", new Label.LabelStyle(labelFont, Color.WHITE));
        more.setTouchable(Touchable.disabled);
        playButton  = buttons.createAction(Lang.t("Lancer"), clickSound, this::activate);
        backButton  = buttons.create(Lang.t("Retour"), clickSound, this::back);

        stage.getRoot().addActorAt(0, shake);   // invisible : met à jour la caméra (avant le décor, comme avant)
        stage.addActor(listPanel);
        stage.addActor(detailPanel);
        categoryRows.forEach(stage::addActor);
        stage.addActor(categoryTitle);
        tabButtons.values().forEach(stage::addActor);
        stage.addActor(tabMark);
        stage.addActor(more);
        stage.addActor(artFrame);
        stage.addActor(art);
        stage.addActor(description);
        stage.addActor(playButton);
        stage.addActor(backButton);
        reveal();

        art.setVisible(false);                  // rien de vu (hors mode ADMIN) : ni image ni texte
        artFrame.setVisible(false);
        if (!categories.isEmpty()) selectCategory(0, false);
        layout();
    }

    // -------------------------------------------------------------------------
    // Les cinématiques
    // -------------------------------------------------------------------------

    /** Les catégories, dans l'ordre du panneau de gauche. Une catégorie sans cinématique vue n'apparaît pas. */
    private void buildCategories() {
        PlayerProfile profile = luckyGame.getProfile();
        addCategory(Lang.t("Histoire"), Lang.t("Les boss de l'Histoire"), true, story(profile), List.of());
        addCategory(Lang.t("Exploration"), Lang.t("Choisis un lieu"), true, List.of(), exploration(profile));
        addCategory(Lang.t("Jeux bonus"), Lang.t("Le Jeu bonus"), false, bonusGames(profile), List.of());
        addCategory(Lang.t("Joueur Masqué"), Lang.t("Le Joueur Masqué"), true, rival(profile), List.of());
        addCategory(Lang.t("Le Croupier"), Lang.t("Le boss secret"), true, croupier(profile), List.of());
    }

    private void addCategory(String label, String title, boolean tabs, List<Subject> subjects, List<PlaceGroup> places) {
        if (subjects.isEmpty() && places.isEmpty()) return; // aucune encore vue (hors mode ADMIN)
        categories.add(new Category(label, title, tabs, subjects, places));
    }

    /**
     * L'Histoire : le boss de chaque chapitre. La fin de l'Éclat Originel est celle du chapitre « Le Dernier
     * Tirage » ; celle de la Machine Originelle, le duel des leviers (gagné, perdu) puis la fin du « Jackpot ».
     */
    private List<Subject> story(PlayerProfile profile) {
        List<Subject> bosses = new ArrayList<>();
        Symbol[] reels = Symbol.classicReels().toArray(new Symbol[0]);
        for (Chapter chapter : Chapter.values()) {
            EnemyKind boss = chapter.getBoss();
            Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
            if (Bestiary.bossCutsceneSeen(profile, boss)) {
                add(moments, Moment.ENTRANCE, new Entry(boss.getDisplayName(),
                    Lang.f("{0}, « {1} » : avant le combat contre le boss.", chapter.getLabel(), chapter.getTitle()),
                    enemyTextures.portrait(boss), kit -> Cutscenes.beforeBoss(kit, boss)));
            }
            addMoments(moments, profile, boss);
            if (boss == EnemyKind.MACHINE_ORIGINELLE && Bestiary.duelSeen(profile)) {
                add(moments, Moment.END, new Entry(Lang.t("Duel des leviers : victoire"),
                    Lang.t("Le Dernier tirage de la Machine Originelle. Tire ton levier à la souris. Une égalité, puis tu "
                        + "gagnes."),
                    enemyTextures.portrait(EnemyKind.MACHINE_ORIGINELLE),
                    kit -> new LastDrawCutscene(kit, new LastDrawEvent(List.of(reels),
                        List.of(Symbol.BELL, Symbol.SEVEN), List.of(Symbol.BELL, Symbol.GOLD_BAR), true))));
                add(moments, Moment.END, new Entry(Lang.t("Duel des leviers : défaite"),
                    Lang.t("Le Dernier tirage de la Machine Originelle. Tire ton levier à la souris. La Machine gagne."),
                    enemyTextures.portrait(EnemyKind.MACHINE_ORIGINELLE),
                    kit -> new LastDrawCutscene(kit, new LastDrawEvent(List.of(reels),
                        List.of(Symbol.CHERRY), List.of(Symbol.DIAMOND), false))));
            }
            if (Bestiary.ENDINGS.contains(chapter) && Bestiary.endingSeen(profile, chapter)) { // voir Cutscenes.ending
                add(moments, Moment.END, new Entry(Lang.f("Fin du {0}", chapter.getLabel().toLowerCase()),
                    Lang.f("La fin de « {0} », après la victoire contre {1}.", chapter.getTitle(), boss.getDisplayName()),
                    enemyTextures.chapterArt(chapter), kit -> Cutscenes.ending(kit, chapter)));
            }
            addSubject(bosses, chapter.getNumber() + ". " + boss.getDisplayName(), moments);
        }
        return bosses;
    }

    /** L'Exploration : les rois de chaque lieu ; le dernier roi du Désert de Braise a aussi la fin de l'Exploration. */
    private List<PlaceGroup> exploration(PlayerProfile profile) {
        List<PlaceGroup> places = new ArrayList<>();
        for (Place each : Place.values()) {
            List<Subject> kings = new ArrayList<>();
            List<Dungeon> dungeons = each.getDungeons();
            for (int i = 0; i < dungeons.size(); i++) {
                Dungeon dungeon = dungeons.get(i);
                EnemyKind king = dungeon.getKing();
                Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
                if (Bestiary.bossCutsceneSeen(profile, king)) {
                    add(moments, Moment.ENTRANCE, new Entry(king.getDisplayName(),
                        Lang.f("{0}, « {1} » : l'entrée en scène du roi, après le soldat.", each.getName(), dungeon.getName()),
                        enemyTextures.portrait(king), kit -> Cutscenes.beforeBoss(kit, king)));
                }
                addMoments(moments, profile, king);
                if (each == Place.DESERT && i == dungeons.size() - 1 && Bestiary.explorationEndingSeen(profile)) {
                    add(moments, Moment.END, new Entry(Lang.t("Fin de l'Exploration"),
                        Lang.t("Les six lieux vidés : le Croupier fait le tour du monde et pose l'écaille d'or d'Ignar sur la table."),
                        enemyTextures.portrait(EnemyKind.CROUPIER), Cutscenes::explorationEnding));
                }
                addSubject(kings, king.getDisplayName(), moments);
            }
            if (!kings.isEmpty()) places.add(new PlaceGroup(each, enemyTextures.map(each), kings));
        }
        return places;
    }

    /** Les jeux bonus : une cinématique chacun, sans onglets. */
    private List<Subject> bonusGames(PlayerProfile profile) {
        List<Subject> games = new ArrayList<>();
        if (!Bestiary.bonusGameSeen(profile)) return games;
        List<Symbol> machine = profile.getMachine();
        addSubject(games, new Entry(Lang.t("Grille de la cagnotte"),
            Lang.t("Rang 4, le plus rare : la grille de 6 x 6 et ses tirages, sur ta machine. Chaque alignement paie "
                + "toute la cagnotte. Une nouvelle grille à chaque fois."),
            cardTextures.get("cards/test/jeu_bonus.png"),
            kit -> new BonusGameCutscene(kit, BonusGame.play(machine, SpinEconomy.BASE_SPIN_COST, new Random()), machine)));
        addSubject(games, new Entry(Lang.t("Roue de la cagnotte"),
            Lang.t("Rang 1 : un lancer de roue, la case où elle s'arrête multiplie la cagnotte."),
            cardTextures.get("cards/test/jeu_bonus_roue.png"), kit -> new SmallBonusGameCutscene(kit,
                BonusGame.playSmall(BonusGame.Kind.WHEEL, 0, SpinEconomy.BASE_SPIN_COST, 1, new Random()))));
        addSubject(games, new Entry(Lang.t("Plinko de la cagnotte"),
            Lang.t("Rang 2 : une bille rebondit sur les clous et tombe dans une case qui multiplie la cagnotte."),
            cardTextures.get("cards/test/jeu_bonus_plinko.png"), kit -> new SmallBonusGameCutscene(kit,
                BonusGame.playSmall(BonusGame.Kind.PLINKO, 0, SpinEconomy.BASE_SPIN_COST, 1, new Random()))));
        addSubject(games, new Entry(Lang.t("Coup de poing de la cagnotte"),
            Lang.t("Rang 3 : un gant de boxe frappe la cible, le score de la machine multiplie la cagnotte."),
            cardTextures.get("cards/test/jeu_bonus_poing.png"), kit -> new SmallBonusGameCutscene(kit,
                BonusGame.playSmall(BonusGame.Kind.PUNCH, 0, SpinEconomy.BASE_SPIN_COST, 1, new Random()))));
        return games;
    }

    /** Le Joueur Masqué : une ligne par rencontre (son entrée) ; la dernière, au Désert, a ses adieux pour fin. */
    private List<Subject> rival(PlayerProfile profile) {
        List<Subject> encounters = new ArrayList<>();
        for (Place each : Place.values()) {
            EnemyKind kind = Rival.of(each);
            if (kind == null) continue;
            Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
            String name = Rival.isFinal(kind) ? Lang.t("Le masque tombe")
                : Lang.f("Rencontre {0} : {1}", Rival.encounter(kind), each.getShortName());
            if (Bestiary.bossCutsceneSeen(profile, kind)) {
                add(moments, Moment.ENTRANCE, new Entry(name, Rival.isFinal(kind)
                        ? Lang.t("Le duel final, au Désert de Braise : il enlève son masque. Lui aussi est un élève du Croupier.")
                        : Lang.f("{0} : il prend la place du soldat. Sa machine a appris un nouveau symbole.", each.getName()),
                    enemyTextures.portrait(kind), kit -> Cutscenes.beforeBoss(kit, kind)));
            }
            if (Rival.isFinal(kind) && Bestiary.deathSeen(profile, kind)) {
                add(moments, Moment.END, new Entry(Lang.t("Le masque est à toi"),
                    Lang.t("Battu, il pose son masque sur le tapis, te le laisse et s'efface dans le sable."),
                    enemyTextures.portrait(kind), Cutscenes::rivalFarewell));
            }
            addSubject(encounters, name, moments);
        }
        EnemyKind story = EnemyKind.RIVAL_HISTOIRE; // sa rencontre de l'Histoire, à la Maison du Croupier
        if (Bestiary.bossCutsceneSeen(profile, story)) {
            Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
            String name = Lang.f("Histoire : {0}", Chapter.RIVAL_CHAPTER.getTitle());
            add(moments, Moment.ENTRANCE, new Entry(name,
                Lang.t("Il se cachait sous une carte des challengers, à la Maison du Croupier : il veut hériter de la table."),
                enemyTextures.portrait(story), kit -> Cutscenes.beforeBoss(kit, story)));
            addSubject(encounters, name, moments);
        }
        return encounters;
    }

    /** Le Croupier, boss secret : rien n'apparaît avant de l'avoir affronté. Sa fin est la vraie fin du jeu. */
    private List<Subject> croupier(PlayerProfile profile) {
        List<Subject> secret = new ArrayList<>();
        EnemyKind croupier = EnemyKind.GRAND_CROUPIER;
        if (!Bestiary.bossCutsceneSeen(profile, croupier)) return secret;
        Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
        add(moments, Moment.ENTRANCE, new Entry(Lang.t("Le Croupier entre en scène"),
            Lang.t("Une porte cachée au bout de l'Histoire mène à sa salle des machines. Il te distribue tes propres cartes."),
            enemyTextures.portrait(croupier), kit -> withProfile(Cutscenes.beforeBoss(kit, croupier), profile)));
        addMoments(moments, profile, croupier);
        if (Bestiary.deathSeen(profile, croupier)) {
            add(moments, Moment.END, new Entry(Lang.t("La vraie fin"),
                Lang.t("Il perd sa dernière main, te laisse son chapeau et le sabot, puis s'efface dans l'aube. C'est toi qui distribues."),
                enemyTextures.portrait(croupier), Cutscenes::trueEnding));
        }
        addSubject(secret, croupier.getDisplayName(), moments);
        return secret;
    }

    private static void add(Map<Moment, List<Entry>> moments, Moment moment, Entry entry) {
        moments.computeIfAbsent(moment, m -> new ArrayList<>()).add(entry);
    }

    private static void addSubject(List<Subject> subjects, String name, Map<Moment, List<Entry>> moments) {
        if (!moments.isEmpty()) subjects.add(new Subject(name, moments)); // rien de vu : le boss n'apparaît pas
    }

    /** Un jeu bonus : une seule cinématique, rangée sous l'entrée (les onglets sont cachés). */
    private static void addSubject(List<Subject> subjects, Entry entry) {
        Map<Moment, List<Entry>> moments = new EnumMap<>(Moment.class);
        add(moments, Moment.ENTRANCE, entry);
        subjects.add(new Subject(entry.name(), moments));
    }

    /**
     * @return {@code cutscene} ; celles du Croupier rejouées depuis ce menu montrent des cartes tirées au hasard
     *         du deck de {@code profile} et un de ses rouleaux truqué au hasard (au combat : les vraies)
     */
    private static Cutscene withProfile(Cutscene cutscene, PlayerProfile profile) {
        if (!(cutscene instanceof CroupierCutscene croupier)) return cutscene;
        Random random = new Random();
        List<String> cards = new ArrayList<>();
        for (fr.astratime.lucky.entities.Card card : fr.astratime.lucky.loaders.CardLoader.loadDeck(profile.getDeck())) {
            cards.add(card.getAssetPath());
        }
        java.util.Collections.shuffle(cards, random);
        croupier.withCards(cards, false);
        List<Symbol> machine = profile.getMachine();
        List<Symbol> riggable = new ArrayList<>();
        for (Symbol symbol : machine) if (symbol != Symbol.JOKER) riggable.add(symbol);
        croupier.withMachine(machine, random.nextInt(3), riggable.isEmpty() ? null : riggable.get(random.nextInt(riggable.size())));
        return croupier;
    }

    /**
     * Ajoute à {@code moments} les cinématiques de phase du boss {@code boss} (vues dès
     * qu'il a été rencontré) et celle de sa mort (vue une fois qu'il a été battu).
     */
    private void addMoments(Map<Moment, List<Entry>> moments, PlayerProfile profile, EnemyKind boss) {
        Texture portrait = enemyTextures.portrait(boss);
        float[] thresholds = boss.phaseThresholds();
        if (Bestiary.bossCutsceneSeen(profile, boss)) {
            for (int phase = 2; phase <= Math.min(3, boss.phaseCount()); phase++) {
                int shown = phase;
                add(moments, phase == 2 ? Moment.PHASE_2 : Moment.PHASE_3,
                    new Entry(Lang.f("{0} : phase {1}", boss.getDisplayName(), phase),
                        phaseDescription(boss, phase, Math.round(thresholds[phase - 2] * 100)),
                        portrait, kit -> withProfile(Cutscenes.phase(kit, boss, shown), profile)));
            }
        }
        if (Cutscenes.hasDeath(boss) && Bestiary.deathSeen(profile, boss)) {
            add(moments, Moment.END, new Entry(Lang.f("{0} : la fin", boss.getDisplayName()),
                Lang.f("Le coup final. « {0} »", BossTheme.of(boss).deathLine()),
                portrait, kit -> Cutscenes.death(kit, boss)));
        }
    }

    /** @return l'explication de la cinématique de la phase {@code phase} du boss {@code boss}, à {@code percent} % de ses PV. */
    private static String phaseDescription(EnemyKind boss, int phase, int percent) {
        if (boss == EnemyKind.COMETE) return Lang.t("À la moitié de ses PV, elle s'embrase.");
        if (boss == EnemyKind.GRAND_CROUPIER) {
            return phase == 2 ? Lang.t("À la moitié de ses PV, il truque un de tes rouleaux.")
                : Lang.t("À 10 % de ses PV, il retourne ses cartes : il joue à visage découvert.");
        }
        if (boss == EnemyKind.MACHINE_ORIGINELLE) {
            return phase == 2 ? Lang.t("À la moitié de ses PV, elle t'arrache un rouleau.")
                : Lang.t("À 10 % de ses PV, elle se fend et verrouille un rouleau pour de bon.");
        }
        return Lang.f("À {0} % de ses PV, phase {1} : « {2} »", percent, phase, boss.phaseText(phase));
    }

    // -------------------------------------------------------------------------
    // Ce qui est montré
    // -------------------------------------------------------------------------

    private Category current() { return categories.get(category); }

    /** @return {@code true} quand le centre montre les lieux de l'Exploration (pas encore de lieu ouvert). */
    private boolean choosingPlace() {
        return category >= 0 && current().hasPlaces() && place < 0;
    }

    /** @return les boss montrés au centre (vide pendant le choix du lieu). */
    private List<Subject> subjects() {
        if (category < 0 || choosingPlace()) return List.of();
        Category shown = current();
        return shown.hasPlaces() ? shown.places().get(place).kings() : shown.subjects();
    }

    /** @return le nombre de lignes du centre. */
    private int itemCount() {
        if (category < 0) return 0;
        return choosingPlace() ? current().places().size() : subjects().size();
    }

    private Subject subject() {
        List<Subject> shown = subjects();
        return selected >= 0 && selected < shown.size() ? shown.get(selected) : null;
    }

    /** @return la cinématique choisie ({@code null} pendant le choix du lieu). */
    private Entry entry() {
        Subject shown = subject();
        if (shown == null) return null;
        List<Entry> entries = shown.moments().get(moment);
        return entries == null ? null : entries.get(Math.min(variant, entries.size() - 1));
    }

    private boolean showsTabs() {
        return category >= 0 && current().tabs() && !choosingPlace();
    }

    // -------------------------------------------------------------------------
    // Sélection et lecture
    // -------------------------------------------------------------------------

    /** Sélectionne la catégorie {@code index} : ses boss (le premier choisi), ou la liste des lieux. */
    private void selectCategory(int index, boolean sound) {
        if (index == category) return;
        if (sound) hoverSound.play();
        category = index;
        place = -1;
        for (int i = 0; i < categoryRows.size(); i++) categoryRows.get(i).setSelected(i == index);
        rebuildItems(choosingPlace() ? lastPlace : 0);
    }

    /** Ouvre le lieu {@code index} de l'Exploration : ses rois remplacent la liste des lieux. */
    private void openPlace(int index) {
        lastPlace = index;
        place = index;
        rebuildItems(0);
    }

    /** Revient de la liste des rois d'un lieu à la liste des lieux. */
    private void closePlace() {
        place = -1;
        rebuildItems(lastPlace);
    }

    /** Refait les lignes du centre (boss ou lieux) et le titre, puis choisit la ligne {@code first}. */
    private void rebuildItems(int first) {
        itemRows.forEach(Actor::remove);
        itemRows.clear();
        Category shown = current();
        categoryTitle.setText(choosingPlace() || !shown.hasPlaces() ? shown.title() : shown.places().get(place).place().getName());
        Label.LabelStyle style = new Label.LabelStyle(itemFont, Color.WHITE);
        int count = itemCount();
        for (int i = 0; i < count; i++) {
            int index = i;
            String text = choosingPlace() ? shown.places().get(i).place().getName() : subjects().get(i).name();
            MenuOption row = new MenuOption(text, style, hud.insetDrawable(), new TextureRegion(chipTexture),
                ITEM_WIDTH, ITEM_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1 && !isPlaying()) select(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isPlaying()) return;
                    select(index, false);
                    activate();
                }
            });
            itemRows.add(row);
            stage.getRoot().addActorBefore(more, row); // sous une cinématique en cours
        }
        buttons.setText(playButton, choosingPlace() ? Lang.t("Choisir") : Lang.t("Lancer"));
        selected = -1;
        scroll = 0;
        moment = Moment.ENTRANCE; // nouvelle liste : on repart de l'entrée (la liste garde l'onglet d'un boss à l'autre)
        select(Math.min(first, count - 1), false);
        fadeIn(categoryTitle);
    }

    /** Choisit la ligne {@code index} du centre ; un boss garde l'onglet en cours s'il l'a. */
    private void select(int index, boolean sound) {
        if (index == selected || index < 0) return;
        if (sound) hoverSound.play();
        selected = index;
        for (int i = 0; i < itemRows.size(); i++) itemRows.get(i).setSelected(i == index);
        Subject shown = subject();
        Moment next = shown == null || shown.moments().containsKey(moment) ? moment : shown.tabs().get(0);
        selectMoment(next, 0, true);
    }

    /** Choisit l'onglet {@code shown} et sa cinématique {@code index}, puis montre sa miniature et son explication. */
    private void selectMoment(Moment shown, int index, boolean force) {
        if (!force && shown == moment && index == variant) return;
        moment = shown;
        variant = index;
        rebuildVariants();
        refreshDetail();
    }

    /** Les cinématiques de l'onglet, quand il en a plusieurs (sinon aucune ligne). */
    private void rebuildVariants() {
        variantRows.forEach(Actor::remove);
        variantRows.clear();
        Subject shown = subject();
        List<Entry> entries = shown == null ? null : shown.moments().get(moment);
        if (entries == null || entries.size() < 2) return;
        Label.LabelStyle style = new Label.LabelStyle(variantFont, Color.WHITE);
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            MenuOption row = new MenuOption(entries.get(i).name(), style, hud.insetDrawable(),
                new TextureRegion(chipTexture), variantWidth(), VARIANT_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1 && variant != index && !isPlaying()) {
                        hoverSound.play();
                        selectMoment(moment, index, false);
                    }
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isPlaying()) return;
                    selectMoment(moment, index, false);
                    activate();
                }
            });
            row.setSelected(i == variant);
            variantRows.add(row);
            stage.getRoot().addActorBefore(artFrame, row); // sous une cinématique en cours
        }
    }

    /** Onglets, miniature et explication de la ligne choisie. */
    private void refreshDetail() {
        Subject shown = subject();
        for (Map.Entry<Moment, TextButton> tab : tabButtons.entrySet()) {
            boolean has = showsTabs() && shown != null && shown.moments().containsKey(tab.getKey());
            tab.getValue().setVisible(has);
            tab.getValue().setColor(tab.getKey() == moment ? Color.WHITE : DIMMED);
        }
        for (int i = 0; i < variantRows.size(); i++) variantRows.get(i).setSelected(i == variant);
        Texture texture;
        String text;
        if (choosingPlace()) {
            PlaceGroup group = current().places().get(selected);
            texture = group.art();
            text = group.place().getDescription();
        } else {
            Entry chosen = entry();
            texture = chosen == null ? null : chosen.art();
            text = chosen == null ? "" : chosen.description();
        }
        art.setVisible(texture != null);
        artFrame.setVisible(texture != null);
        if (texture != null) ((TextureRegionDrawable) art.getDrawable()).setRegion(new TextureRegion(texture));
        description.setText(text);
        fadeIn(art);
        fadeIn(description);
        layout();
    }

    private static void fadeIn(Actor actor) {
        actor.clearActions();
        actor.getColor().a = 0f;
        actor.addAction(Actions.fadeIn(0.25f, Interpolation.pow2Out));
    }

    /** Entrée, clic ou « Lancer » : ouvre le lieu choisi, ou joue la cinématique choisie. */
    private void activate() {
        if (leaving || isPlaying() || category < 0) return;
        if (choosingPlace()) openPlace(selected);
        else launch();
    }

    /** Joue la cinématique choisie par-dessus l'écran (la musique s'efface pendant la scène). */
    private void launch() {
        Entry chosen = entry();
        if (chosen == null) return;
        Cutscene next = chosen.create().apply(kit);
        if (next == null) return;
        if (cutscene != null) {
            cutscene.remove();
            cutscene.dispose();
        }
        cutscene = next;
        stage.addActor(cutscene);
        music.setDucked(true);
        // Écran couvert : l'écran de choix est déjà dessous (même catégorie, lieu, boss et onglet), le fondu
        // final s'estompe par-dessus.
        cutscene.play(() -> {
            music.setDucked(false);
            shake.stop();
        });
    }

    /** @return {@code true} tant qu'une cinématique joue (jusqu'à son fondu final). */
    private boolean isPlaying() {
        return cutscene != null && cutscene.isPlaying();
    }

    /** Échap ou « Retour » : d'un lieu de l'Exploration à la liste des lieux, sinon au menu principal. */
    private void back() {
        if (isPlaying() || leaving) return;
        if (category >= 0 && current().hasPlaces() && place >= 0) closePlace();
        else onBack();
    }

    /** Retour au menu principal (pas pendant une cinématique). */
    @Override
    protected void onBack() {
        if (isPlaying()) return;
        super.onBack();
    }

    /** L'onglet (ou la cinématique de l'onglet) précédent ou suivant, dans l'ordre Entrée, Phase 2, Phase 3, Fin. */
    private void stepMoment(int step) {
        Subject shown = subject();
        if (!showsTabs() || shown == null) return;
        List<Moment> order = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (Moment each : shown.tabs()) {
            for (int i = 0; i < shown.moments().get(each).size(); i++) {
                order.add(each);
                indices.add(i);
            }
        }
        int at = 0;
        for (int i = 0; i < order.size(); i++) if (order.get(i) == moment && indices.get(i) == variant) at = i;
        int next = (at + step + order.size()) % order.size();
        if (next == at) return;
        hoverSound.play();
        selectMoment(order.get(next), indices.get(next), false);
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le titre, les deux panneaux, leur contenu et les boutons selon la taille de l'écran. */
    @Override
    protected void layoutContent(float width, float height) {
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);

        float panelTop    = height - PANEL_TOP;
        float panelBottom = BOTTOM_SPACE;
        float panelHeight = panelTop - panelBottom;
        listPanel.setBounds(MARGIN, panelBottom, LIST_WIDTH, panelHeight);
        float rowX = MARGIN + (LIST_WIDTH - ROW_WIDTH) / 2f;
        for (int i = 0; i < categoryRows.size(); i++) {
            categoryRows.get(i).setPosition(rowX, panelTop - PANEL_PAD - ROW_HEIGHT - i * (ROW_HEIGHT + ROW_GAP));
        }

        float detailX     = MARGIN + LIST_WIDTH + MARGIN / 2f;
        float detailWidth = width - detailX - MARGIN;
        float detailRight = detailX + detailWidth - PANEL_PAD;
        detailPanel.setBounds(detailX, panelBottom, detailWidth, panelHeight);
        float innerX = detailX + PANEL_PAD;
        float listX  = listX();
        float sideX  = sideX();
        float sideWidth = detailRight - sideX;

        categoryTitle.pack();
        categoryTitle.setPosition(innerX, panelTop - PANEL_PAD + 8f - categoryTitle.getHeight());
        float contentTop = categoryTitle.getY() - 14f;

        // Les onglets du boss choisi, comme à la Boutique : au-dessus de sa miniature, ou calés à droite du panneau
        // quand la colonne est trop étroite pour eux.
        List<TextButton> tabs = new ArrayList<>();
        for (TextButton tab : tabButtons.values()) if (tab.isVisible()) tabs.add(tab);
        TextButton chosenTab = tabButtons.get(moment);
        tabMark.setVisible(chosenTab.isVisible());
        if (!tabs.isEmpty()) {
            float tabsWidth = -TAB_GAP;
            for (TextButton tab : tabs) tabsWidth += tab.getWidth() + TAB_GAP;
            float x = sideX + tabsWidth <= detailRight ? sideX : detailRight - tabsWidth;
            for (TextButton tab : tabs) {
                tab.setPosition(x, contentTop - tab.getHeight());
                x += tab.getWidth() + TAB_GAP;
            }
            tabMark.setBounds(chosenTab.getX() + 10f, chosenTab.getY() - 9f, chosenTab.getWidth() - 20f, 5f);
            contentTop -= tabs.get(0).getHeight() + 24f;
        }

        // Les boss (ou les lieux), en colonne. Trop de lignes pour la hauteur : la liste défile avec la
        // sélection (ou la molette).
        float itemsTop = contentTop;
        int count = itemRows.size();
        int rows = visibleRows(itemsTop, panelBottom);
        if (count > rows) rows = visibleRows(itemsTop, panelBottom + MORE_SPACE); // la place du texte « au-dessus / en dessous »
        if (!wheel && selected >= 0 && selected < scroll) scroll = selected;
        if (!wheel && selected >= scroll + rows) scroll = selected - rows + 1;
        scroll = Math.max(0, Math.min(scroll, count - rows));
        for (int i = 0; i < count; i++) {
            int slot = i - scroll;
            itemRows.get(i).setPosition(listX, itemsTop - ITEM_HEIGHT - slot * (ITEM_HEIGHT + ITEM_GAP));
            itemRows.get(i).setVisible(slot >= 0 && slot < rows);
        }
        int hidden = count - rows;
        more.setVisible(hidden > 0);
        if (hidden > 0) {
            int above = scroll, below = hidden - scroll;
            more.setText(above > 0 && below > 0 ? Lang.f("{0} au-dessus, {1} en dessous (molette ou flèches)", above, below)
                : above > 0 ? Lang.f("{0} au-dessus (molette ou flèches)", above)
                : Lang.f("{0} en dessous (molette ou flèches)", below));
            more.setFontScale(1f);
            more.pack();
            if (more.getWidth() > ITEM_WIDTH) {                       // il tient dans la colonne des boss
                more.setFontScale(ITEM_WIDTH / more.getWidth());
                more.pack();
            }
            more.setPosition(listX + (ITEM_WIDTH - more.getWidth()) / 2f, panelBottom + PANEL_PAD);
        }

        // À droite : les cinématiques de l'onglet s'il en a plusieurs, la miniature, puis l'explication.
        float sideTop = itemsTop;
        for (MenuOption row : variantRows) {
            row.setPosition(sideX + CHIP_ROOM, sideTop - VARIANT_HEIGHT);
            sideTop -= VARIANT_HEIGHT + VARIANT_GAP;
        }
        if (!variantRows.isEmpty()) sideTop -= 12f;

        description.setWidth(sideWidth);
        float textHeight = Math.max(80f, description.getPrefHeight());
        float frame     = 10f;
        Drawn texture   = artSize();
        float artScale  = ART_SCALE;
        float maxArtHeight = sideTop - panelBottom - PANEL_PAD - textHeight - 24f - frame * 2;
        if (texture != null) {
            while (artScale > 1f && (texture.height() * artScale > maxArtHeight
                || texture.width() * artScale > sideWidth - frame * 2)) {
                artScale -= 1f;
            }
            if (texture.width() * artScale > sideWidth - frame * 2) {      // une grande image (carte d'un lieu) : réduite
                artScale = (sideWidth - frame * 2) / texture.width();
            }
            if (texture.height() * artScale > maxArtHeight) artScale = Math.max(0.25f, maxArtHeight / texture.height());
        }
        float artWidth  = texture == null ? 0f : texture.width() * artScale;
        float artHeight = texture == null ? 0f : texture.height() * artScale;
        float artX = sideX + (sideWidth - artWidth) / 2f, artY = sideTop - frame - artHeight;
        art.setBounds(artX, artY, artWidth, artHeight);
        artFrame.setBounds(artX - frame, artY - frame, artWidth + frame * 2, artHeight + frame * 2);

        float descriptionTop = texture == null ? sideTop : artY - frame - 24f;
        description.setHeight(Math.max(60f, descriptionTop - panelBottom - PANEL_PAD));
        description.setPosition(sideX, panelBottom + PANEL_PAD);

        playButton.setPosition(detailX + detailWidth - playButton.getWidth(), (BOTTOM_SPACE - playButton.getHeight()) / 2f);
        backButton.setPosition(MARGIN, (BOTTOM_SPACE - backButton.getHeight()) / 2f);
    }

    /** @return le bord gauche des lignes du centre (leur jeton de gauche tient dans le panneau). */
    private static float listX() {
        return MARGIN + LIST_WIDTH + MARGIN / 2f + PANEL_PAD + CHIP_ROOM - 24f;
    }

    /** @return le bord gauche de la colonne de la miniature. */
    private static float sideX() {
        return listX() + ITEM_WIDTH + CHIP_ROOM + 16f; // place des jetons de la ligne choisie
    }

    /** @return la largeur des lignes des cinématiques d'un onglet (leurs jetons tiennent dans la colonne). */
    private float variantWidth() {
        float width = stage.getViewport().getWorldWidth();
        return width - MARGIN - PANEL_PAD - sideX() - CHIP_ROOM * 2f;
    }

    /** La largeur change : les lignes des cinématiques de l'onglet sont refaites à la nouvelle largeur. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        rebuildVariants();
        layout();
    }

    /** Taille en pixels de l'image montrée. */
    private record Drawn(float width, float height) { }

    private Drawn artSize() {
        if (!art.isVisible() || !(art.getDrawable() instanceof TextureRegionDrawable drawable)) return null;
        TextureRegion region = drawable.getRegion();
        return new Drawn(region.getRegionWidth(), region.getRegionHeight());
    }

    /** @return le nombre de lignes qui tiennent en colonne entre {@code itemsTop} et le bas du panneau. */
    private static int visibleRows(float itemsTop, float panelBottom) {
        float room = itemsTop - panelBottom - PANEL_PAD;
        return Math.max(1, (int) ((room + ITEM_GAP) / (ITEM_HEIGHT + ITEM_GAP)));
    }

    /** La molette fait défiler la liste du centre, sans changer la sélection. */
    private void scrollBy(int rows) {
        if (category < 0) return;
        scroll += rows;
        wheel = true; // la sélection ne ramène pas la liste à elle pendant ce défilement
        layout();
        wheel = false;
    }

    /** Molette : la liste du centre (pas pendant une cinématique). */
    @Override
    protected boolean onScroll(float amountY) {
        if (isPlaying() || amountY == 0f) return false;
        Vector2 mouse = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (mouse.x < MARGIN + LIST_WIDTH + MARGIN / 4f) return false;
        scrollBy(amountY > 0f ? 1 : -1);
        return true;
    }

    /**
     * Pendant une cinématique, une touche la passe. Sinon haut/bas choisissent le boss (ou le lieu),
     * gauche/droite et Page haut/bas les onglets, Tab (Maj+Tab : en arrière) la catégorie, Entrée ou
     * Espace ouvre le lieu ou lance la cinématique, Échap revient aux lieux puis au menu.
     */
    @Override
    protected boolean onKey(int keycode) {
        if (isPlaying()) {
            if (!cutscene.keyDown(keycode)) cutscene.skip(); // le levier du duel se tire aussi au clavier
            return true;
        }
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACKSPACE) {
            clickSound.play();
            back();
            return true;
        }
        if (categories.isEmpty()) return false;
        int count = itemCount();
        int categoryCount = categories.size();
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        switch (keycode) {
            case Input.Keys.UP, Input.Keys.W -> select((selected + count - 1) % count, true);
            case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % count, true);
            case Input.Keys.LEFT, Input.Keys.A, Input.Keys.PAGE_UP -> stepMoment(-1);
            case Input.Keys.RIGHT, Input.Keys.D, Input.Keys.PAGE_DOWN -> stepMoment(1);
            case Input.Keys.TAB -> selectCategory((category + (shift ? categoryCount - 1 : 1)) % categoryCount, true);
            case Input.Keys.ENTER, Input.Keys.SPACE -> {
                clickSound.play();
                activate();
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public void dispose() {
        if (cutscene != null) cutscene.cancel();
        super.dispose();
        if (cutscene != null) cutscene.dispose();
        sounds.dispose();
        chipTexture.dispose();
        Fonts.release(rowFont);
        Fonts.release(itemFont);
        Fonts.release(variantFont);
        Fonts.release(labelFont);
        Fonts.release(groupFont);
        Fonts.release(bodyFont);
        Fonts.release(cutsceneFont);
    }
}
