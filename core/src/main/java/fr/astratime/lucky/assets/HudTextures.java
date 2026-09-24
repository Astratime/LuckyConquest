package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Textures pixel art de l'interface de combat (assets/hud) : cadres dorés
 * étirables du panneau latéral et des barres de vie, remplissages des barres,
 * jetons de casino et pièce d'or. Les images sont dessinées sur une grille
 * agrandie x3 (x3 aussi pour la pièce) et s'affichent sans lissage.
 */
public class HudTextures implements Disposable {

    /** Épaisseur (en pixels) des bords non étirés de chaque cadre. */
    public static final int PANEL_BORDER     = 15;
    public static final int INSET_BORDER     = 9;
    public static final int BAR_FRAME_BORDER = 9;

    private static final Color TRAIL_COLOR = Color.valueOf("fff2c0ff");

    public final Texture panel           = load("hud/panel.png");
    public final Texture panelInset      = load("hud/panel_inset.png");
    public final Texture barFrame        = load("hud/bar_frame.png");
    public final Texture barFillEnemy    = load("hud/bar_fill_enemy.png");
    public final Texture barFillPlayer   = load("hud/bar_fill_player.png");
    /** Traînée claire qui rattrape la barre de vie après une perte de PV. */
    public final Texture barTrail        = Textures.solidColor(TRAIL_COLOR);
    public final Texture chipEnemy       = load("hud/chip_enemy.png");
    public final Texture chipPlayer      = load("hud/chip_player.png");
    public final Texture coin            = load("hud/coin.png");

    /** @return un fond étirable pour le panneau latéral (fond sombre, liseré doré épais). */
    public NinePatchDrawable panelDrawable() { return nine(panel, PANEL_BORDER); }

    /** @return un fond étirable pour un encadré du panneau (feutre rouge, liseré doré fin). */
    public NinePatchDrawable insetDrawable() { return nine(panelInset, INSET_BORDER); }

    /** @return le cadre étirable d'une barre de vie (piste sombre, liseré doré). */
    public NinePatchDrawable barFrameDrawable() { return nine(barFrame, BAR_FRAME_BORDER); }

    private static NinePatchDrawable nine(Texture texture, int border) {
        return new NinePatchDrawable(new NinePatch(texture, border, border, border, border));
    }

    private static Texture load(String path) {
        return new Texture(Gdx.files.internal(path));
    }

    @Override
    public void dispose() {
        panel.dispose();
        panelInset.dispose();
        barFrame.dispose();
        barFillEnemy.dispose();
        barFillPlayer.dispose();
        barTrail.dispose();
        chipEnemy.dispose();
        chipPlayer.dispose();
        coin.dispose();
    }
}
