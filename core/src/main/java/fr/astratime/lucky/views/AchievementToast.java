package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Achievement;
import fr.astratime.lucky.progress.PlayerProfile;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * Annonce d'un succès atteint : un cadre glisse depuis le haut de l'écran
 * (« SUCCÈS ! », son nom, sa récompense), reste un moment, puis remonte.
 * Plusieurs succès d'un coup passent l'un après l'autre. Ne bloque aucun clic.
 */
public class AchievementToast extends Group implements Disposable {

    private static final float WIDTH  = 560f;
    private static final float HEIGHT = 128f;
    private static final float MARGIN = 24f;
    private static final float SLIDE  = 0.35f;
    private static final float HOLD   = 3f;
    private static final float PAD    = 22f;
    private static final float COIN   = 56f;

    private final BitmapFont titleFont = Fonts.jersey(26, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont  = Fonts.jersey(34, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont rewardFont = Fonts.jersey(26, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final Label  title;
    private final Label  name;
    private final Label  reward;
    private final Image  coin;
    private final Sound  sound;
    private final Deque<Achievement> waiting = new ArrayDeque<>();
    private boolean showing;

    /** @param sound bruitage du succès (l'appelant le dispose) */
    public AchievementToast(HudTextures hud, Sound sound) {
        this.sound = sound;
        setTouchable(Touchable.disabled);
        setSize(WIDTH, HEIGHT);
        Image panel = new Image(hud.panelDrawable());
        panel.setSize(WIDTH, HEIGHT);
        coin = new Image(hud.coin);
        coin.setSize(COIN, COIN);
        coin.setPosition(PAD, (HEIGHT - COIN) / 2f);
        title  = new Label(Lang.t("SUCCÈS !"), new Label.LabelStyle(titleFont, Color.WHITE));
        name   = new Label("", new Label.LabelStyle(nameFont, Color.WHITE));
        reward = new Label("", new Label.LabelStyle(rewardFont, Color.WHITE));
        addActor(panel);
        addActor(coin);
        addActor(title);
        addActor(name);
        addActor(reward);
        setVisible(false);
    }

    /** Annonce les succès {@code achievements}, à la suite de ceux déjà en attente. */
    public void show(Collection<Achievement> achievements) {
        waiting.addAll(achievements);
        if (!showing) next();
    }

    private void next() {
        Achievement achievement = waiting.poll();
        if (achievement == null || getStage() == null) {
            showing = false;
            setVisible(false);
            return;
        }
        showing = true;
        toFront();
        float textX = PAD + COIN + PAD;
        title.setText(Lang.t("SUCCÈS !"));
        title.pack();
        title.setPosition(textX, HEIGHT - PAD - title.getHeight() + 4f);
        name.setText(achievement.getName());
        name.pack();
        name.setPosition(textX, (HEIGHT - name.getHeight()) / 2f - 2f);
        reward.setText(Lang.f("+{0} pièces", PlayerProfile.formatCoins(achievement.getReward())));
        reward.pack();
        reward.setPosition(textX, PAD - 6f);

        float stageWidth  = getStage().getViewport().getWorldWidth();
        float stageHeight = getStage().getViewport().getWorldHeight();
        float x = stageWidth - WIDTH - MARGIN;
        setPosition(x, stageHeight);
        setVisible(true);
        sound.play();
        addAction(Actions.sequence(
            Actions.moveTo(x, stageHeight - HEIGHT - MARGIN, SLIDE, Interpolation.swingOut),
            Actions.delay(HOLD),
            Actions.moveTo(x, stageHeight, SLIDE, Interpolation.pow2In),
            Actions.run(this::next)));
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(nameFont);
        Fonts.release(rewardFont);
    }
}
