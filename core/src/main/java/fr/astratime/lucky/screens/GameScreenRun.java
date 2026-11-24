package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.assets.CombatMusic;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.progress.CombatLog;
import fr.astratime.lucky.progress.DescentLog;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CombatRecap;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.CroupierRun;
import fr.astratime.lucky.entities.tower.EndlessRun;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.entities.exploration.Crossroads;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.PlayerStats;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.HealthBarView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * L'écran de combat ({@link GameScreen}) entre deux combats : début d'une partie, enchaînement des combats d'un
 * chapitre ou d'un donjon (choix de l'adversaire, salles, coffre, boss), fin du combat et ses boutons.
 */
abstract class GameScreenRun extends GameScreenBase {

    GameScreenRun(LuckyGame luckyGame, CombatRun run, EnemyKind trainingEnemy) {
        super(luckyGame, run, trainingEnemy);
    }

    /** Règle du lieu, Jeu bonus et sa cagnotte, difficulté, puis le premier combat. */
    void startRun(PlayerProfile profile) {
        if (run != null) gameController.setPlaceRule(run.getPlaceRule()); // Exploration : la règle du lieu
        else if (Place.of(trainingEnemy) != null) gameController.setPlaceRule(Place.of(trainingEnemy).getRule()); // entraînement libre
        gameController.setBonusGameEnabled(run == null || run.hasBonusGame());
        gameController.setAdmin(profile.isAdmin()); // ADMIN : Bingos d'un rouleau absent de la machine permis // pas de Jeu bonus dans le tutoriel
        // Cagnotte du Jeu bonus : celle du lieu (partagée par ses donjons) ou de l'Entraînement, gardées ;
        // celle de l'ascension dans la Tour, où un Bingo ouvre plus souvent le Jeu bonus.
        if (run instanceof DungeonRun dungeonRun) {
            gameController.setPot(profile.getPot(Place.of(dungeonRun.getDungeon()), dungeonRun.isHard()));
        } else if (run instanceof TowerRun tower) {
            gameController.setPot(tower.getPot());
            gameController.setBonusGameChance(BonusGame.TOWER_TRIGGER_CHANCE);
        } else if (run instanceof EndlessRun endless) { // comme dans la Tour
            gameController.setPot(endless.getPot());
            gameController.setBonusGameChance(BonusGame.TOWER_TRIGGER_CHANCE);
        } else if (run instanceof CroupierRun croupier) { // le Croupier : comme dans la Tour
            gameController.setPot(croupier.getPot());
            gameController.setBonusGameChance(BonusGame.TOWER_TRIGGER_CHANCE);
        } else if (run == null) {
            gameController.setPot(profile.getPot(PlayerProfile.POT_TRAINING));
        }
        applyDifficulty(); // Tour : mode difficile ; mode sans fin : force de l'étage
        gameController.restart(firstEnemy()); // le premier ennemi du chapitre, ou le croupier d'entraînement
        rollGolden();
        logCombatStart();
        if (run instanceof TutorialRun) {
            TutorialRun.arrange(player().getDeck().getCards());
            TutorialRun.arrange(gameController.getGameState().getEnemy());
        }
    }

    /**
     * Recommence un combat : réinitialise le GameController et tout
     * l'affichage. Dans la Tour des épreuves, le chapitre repart du premier combat.
     */
    void onRestart() {
        if (cutscene != null) cutscene.cancel();
        music.setDucked(false);
        if (run != null) run.restart();
        applyDifficulty();
        gameController.restart(firstEnemy());
        rollGolden();
        logCombatStart();
        if (tutorial != null) {
            TutorialRun.arrange(player().getDeck().getCards());
            TutorialRun.arrange(gameController.getGameState().getEnemy());
            tutorial.reset();
        }
        resetBoard();
    }

    /**
     * Combat suivant d'un chapitre de la Tour des épreuves, contre {@code kind} :
     * le joueur garde ses PV, ses gains et ses cartes, l'affichage repart de zéro.
     */
    void startNextCombat(EnemyKind kind) {
        applyDifficulty(); // mode sans fin : l'étage suivant est plus fort
        gameController.startCombat(kind);
        // Le roi d'un donjon : l'avantage gagné au carrefour (perdu si le donjon recommence).
        if (run instanceof DungeonRun dungeonRun && run.isBossStage()) {
            gameController.applyBoon(dungeonRun.getCrossroads().getBoon());
        }
        logCombatStart();
        resetBoard();
    }

    /** Passe à la musique du combat en cours, ou à celle du lieu si son fichier manque. */
    void switchMusic() {
        if (run == null) { // Entraînement : la musique de l'ennemi choisi, ou celle de son lieu
            String place = CombatMusic.ofTrainingPlace(trainingEnemy);
            music.switchTo(CombatMusic.ofTraining(gameController.getGameState().getEnemy()),
                Gdx.files.internal(place).exists() ? place : CombatMusic.DEFAULT);
            return;
        }
        music.switchTo(CombatMusic.of(run, gameController.getGameState().getEnemy()),
            Gdx.files.internal(CombatMusic.of(run)).exists() ? CombatMusic.of(run) : CombatMusic.DEFAULT);
    }

    /** Remet tout l'affichage à l'état d'un début de combat, puis lance le tour du joueur. */
    void resetBoard() {
        switchMusic(); // nouveau combat : sa musique (celle de l'ennemi s'il en a une)
        if (tutorial == null) luckyGame.getProfile().meet(gameController.getGameState().getEnemy().getKind());
        recap.reset();
        journal.reset();
        speech.hide();
        bingoLines = 0;
        lowHpLineSaid = false;
        refreshJournal();
        stage.getRoot().clearActions(); // la suite du tour en cours (tour de l'ennemi, fin du tour) n'a plus lieu
        hand.reset();
        effectPopupAnimator.cancel(); // les gains en attente ne seront jamais affichés
        if (jackpotCelebration != null) jackpotCelebration.cancel();
        turnBanner.hide();
        bingoAnimation.cancel();
        pistolAnimation.cancel();
        fullPotentialAnimation.skip();
        symbolStrikes.cancel();
        rainbowAnimation.cancel();
        shopOverlay.hide();
        cardDetail.hide();
        choiceOverlay.hide();
        hand.setLocked(false);
        table.setReelsRainbow(false);
        table.setLightsParty(false);
        combatEnd.reset();
        hud.getEnemyHealthBar().clearActions();
        hud.getEnemyHealthBar().getColor().a = 1f;
        hud.clearHeldBack();
        confetti.removeAll();
        gainsNotYetShown = 0;
        piles.resetInFlight();
        pileOverlay.hide();
        slots.clear();
        playerMachine.putAway();
        enemyMachine.putAway();
        showReelCount(gameController.getReelCount());
        enemyView.reset();
        playerShield.setValue(0);
        pickOverlay.hide();
        crossroadsOverlay.hide();
        chestOverlay.hide();
        refreshAll();
        refreshStageLabel();
        layout();
        endButtons.setVisible(false);
        announceGolden();
        startPlayerTurn();
    }

    /** Début d'un combat d'Exploration : son soldat arrive peut-être doré (PV x2, un rouleau de plus). */
    void rollGolden() {
        if (tutorial != null || !(run instanceof DungeonRun dungeonRun)) return;
        if (dungeonRun.rollGolden(goldenRandom)) gameController.getGameState().getEnemy().gild();
    }

    /** Un ennemi doré entre en scène : « ENNEMI DORÉ ! » au-dessus de lui, et des confettis. */
    void announceGolden() {
        if (!gameController.getGameState().getEnemy().isGolden()) return;
        Vector2 center = enemyView.getCroupierCenter();
        effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("ENNEMI DORÉ !"), EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), center.x, center.y);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
        sounds.resultPair.play();
    }

    /**
     * Retour au menu principal : l'écran du menu remplace celui du combat, qui
     * est libéré ensuite (hors de la boucle d'animation du Stage, qu'il ne faut
     * pas disposer pendant qu'il s'exécute).
     */
    void onBackToMenu() {
        endButtons.setTouchable(Touchable.disabled); // un seul retour, même en cliquant plusieurs fois
        Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        });
    }

    /** Tour des épreuves : retour à l'écran des chapitres ; Exploration : retour à la carte. */
    void onBackToChapters() {
        endButtons.setTouchable(Touchable.disabled);
        Gdx.app.postRunnable(() -> {
            // Retour sur le lieu (et le donjon) ou le chapitre (et son mode) qui vient d'être joué.
            if (run instanceof DungeonRun dungeonRun) {
                luckyGame.setScreen(new ExplorationScreen(luckyGame, dungeonRun.getDungeon(), dungeonRun.isHard()));
            } else if (run instanceof TowerRun tower) {
                luckyGame.setScreen(new TowerScreen(luckyGame, tower.getChapter(), tower.isHard()));
            } else if (run instanceof EndlessRun) {
                luckyGame.setScreen(TowerScreen.onEndless(luckyGame));
            } else if (run instanceof CroupierRun) {
                luckyGame.setScreen(TowerScreen.onCroupier(luckyGame));
            } else {
                luckyGame.setScreen(new TowerScreen(luckyGame));
            }
            dispose();
        });
    }

    /**
     * @return le bouton du menu pause qui ramène à l'écran du mode joué : « Exploration »
     *         dans un donjon, « Tour des épreuves » dans un chapitre de la Tour ; {@code null}
     *         ailleurs (Entraînement, tutoriel), où seul le menu principal est proposé.
     */
    String modeMenuLabel() {
        if (run instanceof DungeonRun) return Lang.t("Exploration");
        if (run instanceof TowerRun || run instanceof EndlessRun || run instanceof CroupierRun) return Lang.t("Histoire");
        return null;
    }

    /** @return {@code true} dans un donjon de l'Exploration. */
    boolean isDungeon() { return run instanceof DungeonRun; }

    /**
     * Donjon terminé : le coffre au trésor. Son contenu est tiré et enregistré
     * tout de suite (une carte du donjon et des pièces) ; le joueur l'ouvre d'un clic.
     */
    void openChest() {
        DungeonRun dungeonRun = (DungeonRun) run;
        // Ouvre peut-être le lieu suivant ; le dernier donjon des Mines donne le Rouleau de la Mine.
        PlayerProfile profile = luckyGame.getProfile();
        if (dungeonRun.isHard()) { // Exploration difficile : sa progression à part, ni rouleau ni cinématique de fin
            profile.clearHardDungeon(dungeonRun.getDungeon().name());
            announceAchievements();
            openChest(dungeonRun.getChestCards());
            return;
        }
        boolean desertBefore = profile.isPlaceCleared(Place.DESERT);
        Symbol earnedReel = profile.clearDungeon(dungeonRun.getDungeon().name());
        chestOverlay.setEarnedReel(earnedReel);
        announceAchievements();
        // Le Désert de Braise vidé pour la première fois : la fin de l'Exploration, puis le coffre.
        Cutscene ending = !desertBefore && profile.isPlaceCleared(Place.DESERT)
            ? Cutscenes.explorationEnding(cutsceneKit) : null;
        if (ending == null) openChest(dungeonRun.getChestCards());
        else playCutscene(ending, () -> openChest(dungeonRun.getChestCards()));
    }

    /**
     * Ouvre le coffre : une carte tirée et enregistrée tout de suite ; s'il en
     * reste {@code left - 1} (Carte au trésor), le bouton mène au coffre suivant.
     */
    void openChest(int left) {
        DungeonRun dungeonRun = (DungeonRun) run;
        String cardId = dungeonRun.getDungeon().rollLoot(new Random());
        PlayerProfile.ChestReward reward = luckyGame.getProfile().openChest(cardId,
            Place.of(dungeonRun.getDungeon()), dungeonRun.isHard());
        chestOverlay.getActor().toFront();
        TextButton next = left > 1
            ? buttons.createAction(Lang.t("Coffre suivant"), sounds.buttonClick, () -> openChest(left - 1))
            : buttons.createAction(Lang.t("Exploration"), sounds.buttonClick, this::onBackToChapters);
        chestOverlay.show(dungeonRun.getDungeon(), reward, CardLoader.cardFactory().apply(cardId), next, dungeonRun.isHard());
    }

    /**
     * Tour des épreuves, combat gagné : vers le choix de l'adversaire (après le
     * 1er combat) ou l'apparition du boss (après le 2e).
     */
    void onContinue() {
        endButtons.setVisible(false);
        combatEnd.reset();
        table.setLightsParty(false);
        confetti.removeAll();
        CombatRun.Next next = run.win();
        refreshStageLabel();
        switch (next) {
            case CHOOSE_ENEMY -> pickOverlay.showChoice(run.getChoices(), this::createButton, index -> {
                EnemyKind chosen = run.choose(index);
                // Le Joueur Masqué se cachait sous cette carte (Histoire, la Maison) : sa scène d'abord.
                if (chosen == EnemyKind.RIVAL_HISTOIRE) playCutscene(Cutscenes.beforeBoss(cutsceneKit, chosen), () -> startNextCombat(chosen));
                else startNextCombat(chosen);
            });
            case BOSS -> {
                if (isDungeon()) enterCrossroads(); // Exploration : le carrefour, puis le roi
                else introduceBoss();
            }
            case CLEARED -> {
                if (isDungeon()) openChest();
                else onBackToChapters();
            }
        }
    }

    /**
     * Exploration, soldat battu : deux portes, chacune vers une salle du carrefour
     * (voir {@link Crossroads}). La salle choisie s'ouvre, puis vient le roi.
     */
    void enterCrossroads() {
        Crossroads crossroads = ((DungeonRun) run).getCrossroads();
        crossroadsOverlay.showDoors(crossroads.openDoors(), this::enterRoom);
    }

    /** Le joueur ouvre la porte de {@code room} : ce qui s'y passe, et ses choix. */
    void enterRoom(Crossroads.Room room) {
        Crossroads crossroads = ((DungeonRun) run).getCrossroads();
        Player player = player();
        switch (room) {
            case MACHINE -> {
                Symbol found = crossroads.searchMachine(player);
                showRoom(room, found == null ? Lang.t("La machine est vide. Pas un symbole à en tirer.")
                        : Lang.f("Le levier grince, les rouleaux s'arrêtent : {0} ! Ce symbole sortira 2 fois plus souvent contre le roi.",
                            found.getDisplayName()),
                    found == null ? null : slots.regionOf(found), leaveButton());
            }
            case DEN -> showRoom(room, Lang.f("Le tenancier lance une pièce en l'air. Tu mises {0} PV ?",
                    Crossroads.denStake(player)), null,
                buttons.createAction(Lang.t("Miser"), sounds.buttonClick, () -> {
                    int stake = Crossroads.denStake(player);
                    String won = crossroads.gamble(player, gameController.getStallCardIds());
                    refreshAll();
                    if (won == null) {
                        showRoom(room, Lang.f("Perdu ! Tu laisses {0} PV sur la table.", stake), null, leaveButton());
                    } else {
                        Card card = CardLoader.cardFactory().apply(won);
                        showRoom(room, Lang.f("Gagné ! {0} t'attend dans ta main contre le roi.", card.getName()),
                            new TextureRegion(cardTextures.get(card)), leaveButton());
                    }
                }),
                leaveButton());
            case FOUNTAIN -> showRoom(room, Lang.t("Une eau claire coule dans une vasque de marbre."), null,
                buttons.createAction(Lang.t("Boire"), sounds.buttonClick, () -> {
                    int healed = crossroads.drink(player);
                    refreshAll();
                    showRoom(room, Lang.f("Tu bois. +{0} PV.", healed), null, leaveButton());
                }),
                buttons.createAction(Lang.t("Offrir tes PV"), sounds.buttonClick, () -> {
                    int lost = crossroads.offer(player);
                    refreshAll();
                    showRoom(room, Lang.f("La fontaine boit {0} PV. Contre le roi, tu joueras 1 carte de plus au premier tour.",
                        lost), null, leaveButton());
                }),
                leaveButton());
            case CHEST -> {
                int before = player.getHp();
                Card card = CardLoader.cardFactory().apply(crossroads.openTrappedChest(player));
                refreshAll();
                showRoom(room, Lang.f("Clac ! Le piège te coûte {0} PV, mais tu trouves {1}. Elle sera dans ta main contre le roi.",
                        before - player.getHp(), card.getName()),
                    new TextureRegion(cardTextures.get(card)), leaveButton());
            }
            case EMPTY -> showRoom(room, Lang.t("Rien. De la poussière, et l'écho de tes pas."), null, leaveButton());
        }
    }

    /** Affiche la salle {@code room} et ses boutons. */
    void showRoom(Crossroads.Room room, String text, TextureRegion image, TextButton... choices) {
        crossroadsOverlay.showRoom(room, text, image);
        crossroadsOverlay.setButtons(List.of(choices));
    }

    /** @return le bouton qui quitte le carrefour vers le roi. */
    TextButton leaveButton() {
        return buttons.createAction(Lang.t("Vers le roi"), sounds.buttonClick, () -> {
            crossroadsOverlay.hide();
            introduceBoss();
        });
    }

    /**
     * Après le 2e combat de la Tour ou le soldat d'un donjon, « Continuer » : la
     * cinématique du boss ou du roi joue d'abord, et sa présentation apparaît sous
     * le fondu final. « Affronter » lance ensuite le combat.
     */
    void introduceBoss() {
        playCutscene(Cutscenes.beforeBoss(cutsceneKit, run.getEnemy()), this::showBoss);
    }

    /**
     * Joue {@code next} par-dessus tout l'écran (la musique s'efface pendant la
     * scène), puis {@code then} quand l'écran est couvert ; sans cinématique,
     * {@code then} tout de suite.
     */
    void playCutscene(Cutscene next, Runnable then) {
        if (next == null) {
            then.run();
            return;
        }
        if (cutscene != null) {
            cutscene.remove();
            cutscene.dispose();
        }
        cutscene = next;
        stage.addActor(cutscene);
        tooltip.hide();
        music.setDucked(true);
        Cutscene playing = cutscene;
        cutscene.play(() -> {
            music.setDucked(false);
            then.run();
            playing.toFront(); // le fondu final s'estompe par-dessus l'écran suivant
        });
    }

    /** La présentation du boss, avec « Affronter ». */
    void showBoss() {
        pickOverlay.showBoss(run.getEnemy(), this::createButton, () -> startNextCombat(run.getEnemy()));
    }

    /** @return le texte du bouton qui ramène au choix du chapitre ou du donjon. */
    String backText() { return isDungeon() ? Lang.t("Exploration") : Lang.t("Chapitres"); }

    /** @return un bouton du casino ({@code text}), qui joue le clic et lance {@code action}. */
    TextButton createButton(String text, Runnable action) {
        return buttons.create(text, sounds.buttonClick, action);
    }

    /**
     * Boutons de fin de combat : Recommencer et Menu principal pour un combat
     * seul ; dans la Tour des épreuves, Continuer (victoire), Réessayer
     * (défaite) ou Chapitres (chapitre terminé), et Chapitres.
     */
    void buildEndButtons(boolean victory) {
        endButtons.clearChildren();
        List<TextButton> shown = new ArrayList<>();
        if (run == null) {
            shown.add(restartButton);
            shown.add(menuButton);
        } else if (run instanceof TutorialRun) {
            if (!victory) shown.add(restartButton);
            shown.add(menuButton);
        } else if (run instanceof EndlessRun endless) {
            // Mode sans fin : chaque étage gagné peut battre le record ; une défaite fait remonter depuis l'étage 1.
            int floor = endless.getFloor();
            boolean record = victory && luckyGame.getProfile().recordEndlessFloor(floor);
            shown.add(victory ? buttons.createAction(Lang.t("Étage suivant"), sounds.buttonClick, this::onContinue)
                : buttons.createAction(Lang.t("Recommencer à l'étage 1"), sounds.buttonClick, this::onRestart));
            shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
            String text = record ? Lang.f("NOUVEAU RECORD : ÉTAGE {0} !", floor)
                : Lang.f("RECORD : ÉTAGE {0}", Math.max(luckyGame.getProfile().getEndlessBest(), victory ? floor : 0));
            Label recordLabel = new Label(text, new Label.LabelStyle(shopFont, Palette.GOLD));
            endButtons.add(recordLabel).colspan(shown.size()).padBottom(14f).row();
        } else if (run instanceof CroupierRun) { // le Croupier : un seul combat
            shown.add(victory ? buttons.createAction(Lang.t("Retour à l'Histoire"), sounds.buttonClick, this::onBackToChapters)
                : buttons.createAction(Lang.t("Réessayer"), sounds.buttonClick, this::onRestart));
            if (!victory) shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
        } else if (!victory) {
            shown.add(buttons.createAction(isDungeon() ? Lang.t("Réessayer le donjon") : Lang.t("Réessayer le chapitre"),
                sounds.buttonClick, this::onRestart));
            shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
        } else if (run.isBossStage() && isDungeon()) {
            shown.add(buttons.createAction(Lang.t("Ouvrir le coffre !"), sounds.buttonClick, this::onContinue));
        } else if (run.isBossStage()) {
            // Boss battu : le chapitre est terminé dans ce mode, le suivant s'ouvre.
            if (run instanceof TowerRun tower) luckyGame.getProfile().clearChapter(tower.getChapter(), tower.isHard());
            shown.add(buttons.createAction(Lang.t("Chapitre terminé !"), sounds.buttonClick, this::onBackToChapters));
        } else {
            shown.add(buttons.createAction(Lang.t("Continuer"), sounds.buttonClick, this::onContinue));
            shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
        }
        // Les gains affichés, versés en pièces pour la boutique après le roi d'un donjon (une part après une défaite).
        long earned = combatReward(victory);
        if (earned > 0) {
            String coinsText = victory ? Lang.f("PIÈCES +{0}", PlayerProfile.formatCoins(earned))
                : Lang.f("PIÈCES +{0} (PART DES GAINS GARDÉE)", PlayerProfile.formatCoins(earned));
            int percent = luckyGame.getProfile().coinsPercent(Place.of(((DungeonRun) run).getDungeon()), run.isHard());
            if (percent < 100) coinsText += "\n" + Lang.f("TON RANG EST AU-DESSUS DE CE LIEU : {0} % DES PIÈCES", percent);
            Label coinsLabel = new Label(coinsText, new Label.LabelStyle(shopFont, Palette.GOLD));
            coinsLabel.setAlignment(Align.center);
            endButtons.add(coinsLabel).colspan(shown.size()).padBottom(14f).row();
        }
        String ending = run != null && victory && run.isBossStage() ? run.getEnding() : null;
        if (ending != null) {
            // Le dernier chapitre clôt l'histoire : sa phrase de fin, au-dessus du bouton. Il ouvre le mode difficile.
            if (run instanceof TowerRun && luckyGame.getProfile().openTowerHard()) {
                ending += "\n" + Lang.t("L'Histoire recommence en mode difficile.");
            }
            Label endingLabel = new Label(ending, new Label.LabelStyle(shopFont, Palette.GOLD));
            endingLabel.setAlignment(Align.center);
            endButtons.add(endingLabel).colspan(shown.size()).padBottom(18f).row();
        }
        for (int i = 0; i < shown.size(); i++) {
            TextButton button = shown.get(i);
            endButtons.add(button).size(button.getWidth(), button.getHeight()).padLeft(i == 0 ? 0f : END_BUTTONS_GAP);
        }
        // Sous les boutons, le récapitulatif du combat dans un cadre (lisible sur la main et la bannière de fin).
        Label recapLabel = new Label(recapText(), new Label.LabelStyle(playsFont, Color.WHITE));
        recapLabel.setAlignment(Align.center);
        Table recapBox = new Table();
        recapBox.setBackground(hudTextures.panelDrawable());
        recapBox.add(recapLabel).pad(14f, 28f, 14f, 28f);
        endButtons.row();
        endButtons.add(recapBox).colspan(shown.size()).padTop(RECAP_GAP);
        endButtons.pack();
        // Les boutons restent à leur place : le cadre descend en dessous.
        endButtons.setPosition(playArea.getCenterX() - endButtons.getWidth() / 2f,
            playArea.getHeight() * END_BUTTONS_HEIGHT - recapBox.getPrefHeight() - RECAP_GAP);
    }

    /** @return le récapitulatif du combat, sur deux lignes (voir {@link CombatRecap}). */
    String recapText() {
        return Lang.f("Tours {0}  ·  Dégâts infligés {1}  ·  Plus gros coup {2}",
                recap.getTurns(), Lang.big(recap.getDamageDealt()), Lang.big(recap.getBestHit()))
            + "\n" + Lang.f("Dégâts subis {0}  ·  Bingos {1}  ·  Cartes jouées {2}",
                Lang.big(recap.getDamageTaken()), recap.getBingos(), recap.getCardsPlayed());
    }

    /** Étape du donjon ou de la Tour, en bas à droite de la zone de jeu (rien pour un combat seul). */
    void refreshStageLabel() {
        if (run == null) {
            stageLabel.setText("");
            return;
        }
        if (run instanceof TutorialRun) {
            stageLabel.setText(run.getLabel().toUpperCase());
            placeStageLabel();
            return;
        }
        if (run instanceof EndlessRun endless) {
            String floor = Lang.f("ÉTAGE {0}", endless.getFloor());
            stageLabel.setText(run.getLabel().toUpperCase() + " · " + (run.isBossStage() ? floor + " · " + Lang.t("BOSS") : floor));
            placeStageLabel();
            return;
        }
        String step = run.isBossStage() ? (isDungeon() ? Lang.t("ROI") : Lang.t("BOSS"))
            : Lang.f("COMBAT {0}/{1}", (run.getStage() + 1), run.getStageCount());
        stageLabel.setText(run.getLabel().toUpperCase() + " · " + step);
        placeStageLabel();
    }

    /** Étape en bas à droite (à la place de l'ancien bouton pause), alignée à droite : replacée quand son texte change. */
    void placeStageLabel() {
        stageLabel.pack();
        stageLabel.setPosition(playArea.getX() + playArea.getWidth() - stageLabel.getWidth() - BUTTON_MARGIN,
            BUTTON_MARGIN + (CasinoButtons.HEIGHT - stageLabel.getHeight()) / 2f);
    }

    // -------------------------------------------------------------------------
    // Fin de combat et sons
    // -------------------------------------------------------------------------

    /** Début d'un combat, au journal détaillé : le mode, l'ennemi, le rang, le deck et la machine. */
    void logCombatStart() {
        if (!combatLogOn) return;
        PlayerProfile profile = luckyGame.getProfile();
        GameState state = gameController.getGameState();
        String mode;
        if (run == null) mode = "entrainement";
        else if (run instanceof DungeonRun dungeonRun) {
            mode = "exploration lieu=" + Place.of(dungeonRun.getDungeon()).name() + " donjon=" + dungeonRun.getDungeon().name()
                + " difficile=" + dungeonRun.isHard() + " etape=" + (run.isBossStage() ? "roi" : "soldat");
        } else if (run instanceof TowerRun tower) {
            mode = "tour chapitre=" + tower.getChapter() + " difficile=" + tower.isHard();
        } else mode = run.getClass().getSimpleName();
        List<String> deck = new ArrayList<>();
        profile.getDeck().forEach((id, copies) -> deck.add(id + ':' + copies));
        List<String> machine = new ArrayList<>();
        for (Symbol symbol : profile.getMachine()) machine.add(symbol.name());
        CombatLog.append(CombatLog.DEFAULT_FILE, String.format(java.util.Locale.ROOT,
            "COMBAT date=%s mode=%s ennemi=%s dore=%b rang=%s prestige=%d pv=%d/%d gains=%.0f cout_tirage=%.0f deck=%s machine=%s",
            java.time.LocalDateTime.now().withNano(0), mode, state.getEnemy().getKind().name(), state.getEnemy().isGolden(),
            profile.getRank() == null ? "-" : profile.getRank().name(), profile.getPrestige(), state.getPlayer().getHp(),
            state.getPlayer().getMaxHp(), state.getPlayer().getGains(), (double) gameController.getSpinCost(),
            String.join(",", deck), String.join(",", machine)));
    }

    /** Combat de l'Exploration terminé : une ligne au journal des descentes (ni ADMIN, ni tutoriel). */
    void logDescent(boolean won, long reward) {
        if (!isDungeon() || tutorial != null || luckyGame.getProfile().isAdmin()) return;
        DungeonRun dungeonRun = (DungeonRun) run;
        PlayerProfile profile = luckyGame.getProfile();
        GameState state = gameController.getGameState();
        profile.recordFight(Place.of(dungeonRun.getDungeon()), dungeonRun.isHard(), run.isBossStage(), won, reward);
        List<String> machine = new ArrayList<>();
        for (Symbol symbol : profile.getMachine()) machine.add(symbol.name());
        DescentLog.append(DescentLog.DEFAULT_FILE, new DescentLog.Entry(
            Place.of(dungeonRun.getDungeon()).name(), dungeonRun.getDungeon().name(), dungeonRun.isHard(),
            profile.getRank() == null ? null : profile.getRank().name(), profile.getPrestige(), run.isBossStage(), won,
            state.getTurnNumber(), state.getPlayer().getGains(), reward, state.getEnemy().isGolden(),
            gameController.getStake().percent, profile.getDeck(), machine));
    }

    /** @return les pièces gagnées à la fin de ce combat (voir {@link PlayerProfile#combatReward}). */
    long combatReward(boolean victory) {
        if (!isDungeon()) return 0; // seule l'Exploration rapporte des pièces (ni Entraînement, ni Tour)
        Place place = Place.of(((DungeonRun) run).getDungeon());
        long reward = PlayerProfile.combatReward(victory, run.isBossStage(),
            gameController.getGameState().getPlayer().getGains(), place, run.isHard(),
            PlayerProfile.dungeonProgress(run.isBossStage(), gameController.getGameState().getEnemy().getHpRatio()));
        // Un lieu en dessous de celui du rang joué rapporte moins (Astra, 2026-11-15).
        return PlayerProfile.share(reward, luckyGame.getProfile().coinsPercent(place, run.isHard()));
    }

    /** Règle la difficulté du combat qui vient : celle de la Tour ou de l'étage du mode sans fin (normale sans épreuve). */
    void applyDifficulty() {
        gameController.setDifficulty(run != null ? run.getDifficulty() : Difficulty.NORMAL);
    }

    /** @return l'ennemi du combat en cours de la Tour ou du donjon, ou le croupier d'entraînement. */
    EnemyKind firstEnemy() {
        return run != null ? run.getEnemy() : trainingEnemy;
    }

    /** @return pourquoi {@code card} ne se joue pas maintenant (règles du combat, ou le Croupier du tutoriel), ou {@code null}. */
    String unplayableReason(Card card) {
        String refusal = tutorial != null ? tutorial.refusal(card) : null;
        return refusal != null ? refusal : gameController.unplayableReason(card);
    }

    /** « Passer le tutoriel » : il ne sera plus proposé ; retour au menu (où le Croupier présente les modes). */
    void skipTutorial() {
        luckyGame.getProfile().markSeen(PlayerProfile.GUIDE_TUTORIAL);
        guide.stop();
        onBackToMenu();
    }

    /** Le combat est terminé dès que le joueur ou l'ennemi n'a plus de points de vie. */
    boolean isCombatOver() {
        GameState gameState = gameController.getGameState();
        return gameState.getEnemy().isDefeated() || gameState.getPlayer().isDefeated();
    }

    /**
     * Fige la partie (plus de pioche ni de spin), met en scène la victoire ou la
     * défaite, puis affiche les boutons pour recommencer ou revenir au menu principal.
     */
    void endCombat() {
        setSpinDisabled(true);
        speech.hide();
        boolean won = gameController.getGameState().getEnemy().isDefeated();
        long reward = combatReward(won);
        luckyGame.getProfile().addCoins(reward);
        logDescent(won, reward);
        if (combatLogOn) {
            CombatLog.append(CombatLog.DEFAULT_FILE, String.format(java.util.Locale.ROOT,
                "FIN victoire=%b tours=%d pv=%d/%d gains=%.0f pieces=%d", won,
                gameController.getGameState().getTurnNumber(), gameController.getGameState().getPlayer().getHp(),
                gameController.getGameState().getPlayer().getMaxHp(), gameController.getGameState().getPlayer().getGains(),
                reward));
        }
        buildEndButtons(won);
        if (won && tutorial == null) {
            EnemyKind defeated = gameController.getGameState().getEnemy().getKind();
            luckyGame.getProfile().defeat(defeated);
            if (gameController.getGameState().getEnemy().isGolden()) { // sa version dorée, au bestiaire
                luckyGame.getProfile().defeatGolden(defeated);
            }
            if (run instanceof DungeonRun dungeonRun && dungeonRun.getDungeon().getKing() == defeated) {
                luckyGame.getProfile().winAgainstKing(defeated);
            }
            Player player = gameController.getGameState().getPlayer();
            boolean close    = player.getHp() > 0 && player.getHp() * 10L < player.getMaxHp();
            boolean flawless = player.getHp() >= player.getMaxHp();
            luckyGame.getProfile().recordStats(stats -> {
                stats.add(PlayerStats.Stat.WINS, 1);
                stats.add(PlayerStats.Stat.COINS, reward);
                if (close)    stats.add(PlayerStats.Stat.CLOSE_WINS, 1);
                if (flawless) stats.add(PlayerStats.Stat.FLAWLESS_WINS, 1);
            });
        } else if (reward > 0) { // défaite dans un donjon : une part des gains est gardée
            luckyGame.getProfile().recordStats(stats -> stats.add(PlayerStats.Stat.COINS, reward));
        }
        announceAchievements();
        Runnable showRestart = () -> {
            endButtons.setTouchable(Touchable.childrenOnly);
            endButtons.setVisible(true);
            endButtons.getColor().a = 0f;
            endButtons.addAction(Actions.fadeIn(END_BUTTONS_FADE));
            endButtons.toFront();
        };
        if (gameController.getGameState().getEnemy().isDefeated()) {
            // Un boss ou un roi vaincu : sa cinématique de mort d'abord, la victoire apparaît sous son fondu.
            Cutscene death = tutorial == null ? Cutscenes.death(cutsceneKit, gameController.getGameState().getEnemy().getKind()) : null;
            playCutscene(death, () -> showVictory(showRestart));
        } else {
            combatEnd.playDefeat(showRestart);
            sounds.defeat.play();
        }
    }

    /** L'ennemi est vaincu : il part en jetons, la fin du chapitre s'il y en a une, puis {@code showRestart}. */
    void showVictory(Runnable showRestart) {
        table.setLightsParty(true);
        HealthBarView enemyBar = hud.getEnemyHealthBar();
        enemyBar.hit(Color.WHITE, 0.4f);
        enemyBar.addAction(Actions.fadeOut(0.5f)); // l'ennemi part en jetons
        enemyView.defeat();
        // Fin de l'histoire (chapitres 3 et 6) : sa cinématique, puis la phrase de fin et les boutons.
        Cutscene ending = run instanceof TowerRun tower && run.isBossStage()
            ? Cutscenes.ending(cutsceneKit, tower.getChapter())
            : run instanceof CroupierRun ? Cutscenes.trueEnding(cutsceneKit) : null; // le Croupier : la vraie fin
        Runnable afterVictory = ending == null ? showRestart : () -> playCutscene(ending, showRestart);
        if (tutorial != null) { // tutoriel réussi : le Croupier conclut, puis le menu
            luckyGame.getProfile().markSeen(PlayerProfile.GUIDE_TUTORIAL);
            afterVictory = () -> tutorial.beat(TutorialDirector.Beat.VICTORY, showRestart);
        }
        combatEnd.playVictory(hud.getEnemyChipCenter(), afterVictory);
        sounds.victory.play();
    }

    // Implémentées par GameScreen
    abstract void announceAchievements();
    abstract void setSpinDisabled(boolean disabled);
    abstract void showReelCount(int count);
}
