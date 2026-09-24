package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.assets.HudTextures;

/**
 * Barre de vie au thème casino : un jeton (à gauche, par-dessus la barre), un
 * cadre doré, un remplissage en dégradé proportionnel aux PV, le nom du camp à
 * gauche et "PV / PV max" à droite. Après une perte de PV, une traînée claire
 * reste un instant à l'ancienne valeur puis rattrape le remplissage, pour que
 * les dégâts se voient. Réutilisée pour l'ennemi et pour le joueur (seuls le
 * nom, le jeton et la couleur du remplissage diffèrent). Un coup reçu fait
 * trembler la barre sous un flash ({@link #hit}).
 *
 * Ne possède aucune texture : elles restent possédées (et disposées) par {@link HudTextures}.
 */
public class HealthBarView extends Group {

    /** Hauteur totale : celle du jeton, qui dépasse du cadre en haut et en bas. */
    public static final float  HEIGHT         = 60f;
    private static final float CHIP_SIZE      = 60f;
    private static final float FRAME_HEIGHT   = 42f;
    private static final float TEXT_PAD       = 10f;   // marge des textes à l'intérieur de la piste
    private static final float TRAIL_DELAY    = 0.35f;
    private static final float TRAIL_DURATION = 0.5f;
    private static final float SHAKE          = 6f;    // décalage maximal du tremblement, en pixels
    private static final float FLASH_ALPHA    = 0.75f;
    private static final float FLASH_DURATION = 0.25f;

    private final float trackWidth;
    private final float trackHeight;

    private final Image trail;
    private final Image fill;
    private final Label hpLabel;
    private final Image flash;
    private float       shakeTime;

    /**
     * @param name        nom du camp, affiché à gauche dans la barre (ex : "ENNEMI")
     * @param font        police des textes (avec contour, lisible sur le remplissage)
     * @param fillTexture dégradé vertical du remplissage (étiré horizontalement)
     * @param chipTexture jeton affiché à gauche de la barre
     * @param frameWidth  largeur du cadre doré
     */
    public HealthBarView(String name, BitmapFont font, HudTextures hud, Texture fillTexture, Texture chipTexture,
                         float frameWidth) {
        int   border = HudTextures.BAR_FRAME_BORDER;
        float frameX = CHIP_SIZE / 2f;
        float frameY = (HEIGHT - FRAME_HEIGHT) / 2f;
        // La piste commence au bord droit du jeton, pour que même une barre presque vide reste visible.
        float trackX = CHIP_SIZE - border / 3f;
        trackWidth  = frameX + frameWidth - border - trackX;
        trackHeight = FRAME_HEIGHT - border * 2;
        float trackY = frameY + border;

        Image frame = new Image(hud.barFrameDrawable());
        frame.setBounds(frameX, frameY, frameWidth, FRAME_HEIGHT);

        trail = new Image(new TextureRegionDrawable(new TextureRegion(hud.barTrail)));
        trail.setBounds(trackX, trackY, trackWidth, trackHeight);
        fill = new Image(new TextureRegionDrawable(new TextureRegion(fillTexture)));
        fill.setBounds(trackX, trackY, trackWidth, trackHeight);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label nameLabel = new Label(name, style);
        nameLabel.setBounds(trackX + TEXT_PAD, trackY, trackWidth - TEXT_PAD * 2, trackHeight);
        nameLabel.setAlignment(Align.left);
        hpLabel = new Label("", style);
        hpLabel.setBounds(trackX + TEXT_PAD, trackY, trackWidth - TEXT_PAD * 2, trackHeight);
        hpLabel.setAlignment(Align.right);

        Image chip = new Image(new TextureRegionDrawable(new TextureRegion(chipTexture)));
        chip.setBounds(0, 0, CHIP_SIZE, CHIP_SIZE);

        addActor(frame);
        addActor(trail);
        addActor(fill);
        addActor(nameLabel);
        addActor(hpLabel);
        addActor(chip); // le jeton recouvre le début du cadre

        flash = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        flash.setBounds(frameX, frameY, frameWidth, FRAME_HEIGHT);
        flash.getColor().a = 0f;
        addActor(flash); // en dernier : par-dessus toute la barre
        setSize(frameX + frameWidth, HEIGHT);
    }

    /**
     * Met à jour la largeur du remplissage et le texte "PV / PV max". Une perte
     * de PV laisse la traînée à l'ancienne valeur avant de la faire rattraper le
     * remplissage ; un gain (soin, nouveau combat) la place directement.
     */
    public void refresh(int hp, int maxHp) {
        float width = trackWidth * Math.clamp((float) hp / maxHp, 0f, 1f);
        fill.setWidth(width);
        trail.clearActions();
        if (width < trail.getWidth()) {
            trail.addAction(Actions.sequence(
                Actions.delay(TRAIL_DELAY),
                Actions.sizeTo(width, trackHeight, TRAIL_DURATION, Interpolation.pow2In)));
        } else {
            trail.setWidth(width);
        }
        hpLabel.setText(hp + " / " + maxHp);
    }

    /**
     * Marque un coup reçu (ou un soin) : la barre tremble et un flash de
     * {@code flashColor} passe dessus.
     *
     * @param shake durée du tremblement, en secondes (0 : aucun)
     */
    public void hit(Color flashColor, float shake) {
        shakeTime = Math.max(shakeTime, shake);
        flash.clearActions();
        flash.setColor(flashColor.r, flashColor.g, flashColor.b, FLASH_ALPHA);
        flash.addAction(Actions.fadeOut(FLASH_DURATION));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        shakeTime = Math.max(0f, shakeTime - delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shakeTime <= 0f) {
            super.draw(batch, parentAlpha);
            return;
        }
        // Tremblement : décalage aléatoire le temps du dessin, sans toucher à la position de la barre.
        float x = getX(), y = getY();
        setPosition(x + MathUtils.random(-SHAKE, SHAKE), y + MathUtils.random(-SHAKE, SHAKE));
        super.draw(batch, parentAlpha);
        setPosition(x, y);
    }
}
