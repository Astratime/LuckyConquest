package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.i18n.Lang;

/**
 * Les commandes du tirage, sur la table du joueur : le levier, à gauche de sa
 * machine, et la Mise, juste à droite de son bouclier (son bouton, son palier
 * dessous, et à sa droite les jetons misés posés sur la table).
 */
public class SpinControls {

    /** Secondes où le levier reste tiré au lancer. */
    private static final float  LEVER_HOLD        = 0.5f;
    private static final float  LEVER_GAP         = 28f;  // entre le levier et la machine du joueur
    private static final float  PILE_MAT_PAD      = 16f;  // marge du tapis de la défausse autour de la pile
    private static final float  STAKE_BUTTON_GAP  = 24f;  // entre la Mise et le tapis de la défausse, au plus près
    private static final float  STAKE_SHIELD_GAP  = 16f;  // entre le bouclier du joueur et la Mise, à sa droite
    private static final String STAKE_SHIELD_ROOM = " 0000"; // la Mise laisse au bouclier la place de 4 chiffres
    private static final float  STAKE_LABEL_GAP   = 2f;   // entre la Mise et son palier, dessous
    private static final float  STAKE_CHIPS_GAP   = 10f;  // les jetons misés tombent sur la table, à droite de la Mise

    private final TableView   table;
    private final ShieldBadge shield;
    private final BitmapFont  shieldFont;
    private final Tooltip     tooltip;
    /** Levier de la machine du joueur : lance le tirage. */
    private final IconButton  lever;
    /** Mise du prochain tirage (aucune, 10 %, 25 %, 50 %). */
    private final IconButton  stakeButton;
    /** Palier de la Mise, sous son bouton (« Mise », « 25 % : x3 »). */
    private final Label       stakeLabel;
    /** Jetons de la Mise posés sur la table. */
    private final StakeChips  chips;
    /** Position et largeur du bouclier quand la Mise a été placée (elle le suit s'il bouge ou s'élargit). */
    private float shieldX, shieldWidth;

    /**
     * @param labelFont  police du palier de la Mise
     * @param shieldFont police du bouclier du joueur (pour lui laisser la place de 4 chiffres)
     * @param onSpin     clic sur le levier
     * @param onStake    clic sur la Mise
     */
    public SpinControls(TableView table, ShieldBadge shield, HudTextures hud, BitmapFont labelFont, BitmapFont shieldFont,
                        Tooltip tooltip, Sound leverSound, Sound stakeSound, Runnable onSpin, Runnable onStake) {
        this.table      = table;
        this.shield     = shield;
        this.shieldFont = shieldFont;
        this.tooltip    = tooltip;
        lever = new IconButton(hud.lever, hud.leverOver, hud.leverDown, hud.leverDisabled, true, leverSound, onSpin);
        addTooltip(lever, Lang.t("Lancer la machine"), Lang.t("Tire le levier : les rouleaux tournent. Touche F."));
        stakeButton = new IconButton(hud.stake, hud.stakeOver, hud.stakeDown, hud.stakeDisabled, false, stakeSound, onStake);
        addTooltip(stakeButton, Lang.t("Mise"), Lang.t("Mise une part de tes gains avant de lancer. "
            + "10 % : symboles x2. 25 % : x3. 50 % : x5. La mise est perdue, même sur un mauvais tirage."));
        stakeLabel = new Label(stakeText(SpinEconomy.Stake.NONE), new Label.LabelStyle(labelFont, Color.WHITE));
        stakeLabel.setTouchable(Touchable.disabled);
        chips = new StakeChips(hud.chipStake);
        setDisabled(true);
    }

    /** Ajoute le levier, la Mise, son palier et ses jetons au Stage. */
    public void addTo(Stage stage) {
        stage.addActor(lever);
        stage.addActor(stakeButton);
        stage.addActor(stakeLabel);
        stage.addActor(chips);
    }

    public IconButton getLever()       { return lever; }
    public IconButton getStakeButton() { return stakeButton; }

    /** @return {@code true} tant que le tirage ne peut pas être lancé. */
    public boolean isDisabled()      { return lever.isDisabled(); }
    public boolean isStakeDisabled() { return stakeButton.isDisabled(); }

    /** Active ou désactive le levier, et la Mise avec lui (elle se choisit avant le lancer). */
    public void setDisabled(boolean disabled) {
        lever.setDisabled(disabled);
        stakeButton.setDisabled(disabled);
    }

    /** Le palier {@code stake} s'affiche sous la Mise. */
    public void showStake(SpinEconomy.Stake stake) {
        stakeLabel.setText(stakeText(stake));
        placeStake();
    }

    /** Le joueur mise plus (ou plus rien) : les jetons tombent sur la table, ou s'effacent. */
    public void dropChips(SpinEconomy.Stake stake) {
        chips.show(stake.ordinal());
    }

    /** Lancer : le levier reste tiré le temps que les rouleaux partent, les jetons glissent dans la machine. */
    public void pull() {
        lever.hold(LEVER_HOLD);
        chips.spend(table.getPlayerReelsRight() - chips.getX());
    }

    /** @return le point au-dessus de la Mise où surgit un texte (« PAS DE GAINS À MISER »). */
    public Vector2 aboveStake() {
        return new Vector2(stakeButton.getX() + stakeButton.getWidth() / 2f,
            stakeButton.getY() + stakeButton.getHeight() * 1.5f);
    }

    /** Place le levier à gauche des rouleaux du joueur. */
    public void placeLever() {
        lever.setPosition(table.getPlayerReelRowX() - LEVER_GAP - lever.getWidth(),
            table.getReelRowY() + (SlotView.CELL_HEIGHT - lever.getHeight()) / 2f);
    }

    /**
     * Place la Mise juste à droite du bouclier du joueur (sans jamais toucher
     * le tapis de la défausse) : son bouton, son palier dessous, et à sa droite
     * les jetons misés qui tombent sur la table. La place laissée au bouclier
     * tient 4 chiffres ; au-delà, la Mise suit son texte.
     */
    public void placeStake() {
        float room = ShieldBadge.ICON_SIZE + ShieldBadge.LABEL_GAP
            + new GlyphLayout(shieldFont, Lang.t("Bouclier") + STAKE_SHIELD_ROOM).width;
        shieldX     = shield.getX();
        shieldWidth = shield.getWidth();
        float x = Math.min(shieldX + Math.max(room, shieldWidth) + STAKE_SHIELD_GAP,
            table.getEnemyDeckX() - PILE_MAT_PAD - STAKE_BUTTON_GAP - StakeChips.CHIP_SIZE - STAKE_CHIPS_GAP
                - stakeButton.getWidth());
        float y = table.getReelRowY() + (SlotView.CELL_HEIGHT - stakeButton.getHeight()) / 2f;
        stakeButton.setPosition(x, y);
        stakeLabel.pack();
        stakeLabel.setPosition(x + (stakeButton.getWidth() - stakeLabel.getWidth()) / 2f,
            y - stakeLabel.getHeight() - STAKE_LABEL_GAP);
        chips.setPosition(x + stakeButton.getWidth() + STAKE_CHIPS_GAP, y);
    }

    /** La Mise suit le bouclier du joueur s'il a bougé ou s'est élargi depuis qu'elle a été placée. */
    public void followShield() {
        if (shield.getX() != shieldX || shield.getWidth() != shieldWidth) placeStake();
    }

    /** @return le texte sous le bouton de Mise pour le palier {@code stake} (« Mise », « 25 % : x3 »). */
    private static String stakeText(SpinEconomy.Stake stake) {
        return stake == SpinEconomy.Stake.NONE ? Lang.t("Mise") : Lang.f("{0} % : x{1}", stake.percent, stake.factor);
    }

    /** Une infobulle ({@code heading}, {@code text}) au-dessus de {@code actor}, au survol. */
    private void addTooltip(Actor actor, String heading, String text) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 top = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2f, actor.getHeight()));
                tooltip.show(heading, text, top.x, top.y + 6f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) tooltip.hide();
            }
        });
    }
}
