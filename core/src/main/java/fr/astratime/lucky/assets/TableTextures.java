package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Textures pixel art de la table de casino (assets/table) : rebord en cuir,
 * feutre (motif répété), emplacements imprimés sur le feutre, tapis des piles,
 * machine à sous et filet de séparation. Dessinées sur une grille agrandie x3,
 * elles s'affichent sans lissage ; les cadres sont étirables (9-patch).
 */
public class TableTextures implements Disposable {

    /** Épaisseur (en pixels) des bords non étirés de chaque cadre. */
    public static final int RAIL_BORDER       = 51;
    public static final int CARD_SLOT_BORDER  = 9;
    public static final int PILE_MAT_BORDER   = 18;
    public static final int REEL_FRAME_BORDER = 15;
    public static final int REEL_CELL_BORDER  = 6;

    public final Texture rail       = load("table/table_rail.png");
    public final Texture felt       = load("table/felt.png");
    public final Texture cardSlot   = load("table/card_slot.png");
    public final Texture pileMat    = load("table/pile_mat.png");
    public final Texture reelFrame  = load("table/reel_frame.png");
    public final Texture reelCell   = load("table/reel_cell.png");
    public final Texture feltLine   = load("table/felt_line.png");
    public final Texture feltEmblem = load("table/felt_emblem.png");

    /** @return le rebord de la table (centre transparent, le feutre est dessous). */
    public NinePatchDrawable railDrawable()      { return nine(rail, RAIL_BORDER); }
    /** @return le feutre, répété pour couvrir toute la surface sans être étiré. */
    public TiledDrawable     feltDrawable()      { return new TiledDrawable(new TextureRegion(felt)); }
    /** @return le contour d'un emplacement de carte imprimé sur le feutre. */
    public NinePatchDrawable cardSlotDrawable()  { return nine(cardSlot, CARD_SLOT_BORDER); }
    /** @return le tapis du deck ou de la défausse. */
    public NinePatchDrawable pileMatDrawable()   { return nine(pileMat, PILE_MAT_BORDER); }
    /** @return la carrosserie de la machine à sous, autour des rouleaux. */
    public NinePatchDrawable reelFrameDrawable() { return nine(reelFrame, REEL_FRAME_BORDER); }
    /** @return la fenêtre d'un rouleau, sur laquelle s'affiche un symbole. */
    public NinePatchDrawable reelCellDrawable()  { return nine(reelCell, REEL_CELL_BORDER); }

    private static NinePatchDrawable nine(Texture texture, int border) {
        return new NinePatchDrawable(new NinePatch(texture, border, border, border, border));
    }

    private static Texture load(String path) {
        return new Texture(Gdx.files.internal(path));
    }

    @Override
    public void dispose() {
        rail.dispose();
        felt.dispose();
        cardSlot.dispose();
        pileMat.dispose();
        reelFrame.dispose();
        reelCell.dispose();
        feltLine.dispose();
        feltEmblem.dispose();
    }
}
