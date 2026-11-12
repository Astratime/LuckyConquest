package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.Cosmetic;

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

    public final Texture rail;
    public final Texture felt;
    public final Texture cardSlot;
    public final Texture pileMat;
    public final Texture reelFrame;
    public final Texture reelCell;
    public final Texture feltLine;
    public final Texture feltEmblem;
    /** Ampoules de la guirlande du rebord : éteinte et allumée, côte à côte. */
    public final Texture bulbs;
    /** Halo d'une ampoule allumée et éclairage de lampe sur le feutre (dégradés lisses). */
    public final Texture bulbGlow;
    public final Texture feltLight;
    /** Pixel blanc, teinté pour les contours animés (bordure arc-en-ciel des rouleaux). */
    public final Texture pixel      = Textures.solidColor(Color.WHITE);

    /** La table de casino d'origine (Casino, Tour des épreuves, Entraînement). */
    public TableTextures() { this("table"); }

    /**
     * @param folder dossier des textures : {@code table} pour la table d'origine,
     *               {@code table/<lieu>} pour la table d'un lieu ({@link #forPlace})
     */
    public TableTextures(String folder) { this(folder, null, null); }

    /**
     * @param folder        dossier des textures (voir {@link #TableTextures(String)})
     * @param feltPath      feutre d'un cosmétique « Tapis », {@code null} pour celui de {@code folder}
     * @param machineFolder dossier (avec la barre finale) de la carrosserie et des fenêtres d'un
     *                      cosmétique « Machine », {@code null} pour celles de {@code folder}
     */
    public TableTextures(String folder, String feltPath, String machineFolder) {
        String machine = machineFolder != null ? machineFolder : folder + "/";
        rail       = load(folder + "/table_rail.png");
        felt       = load(feltPath != null ? feltPath : folder + "/felt.png");
        cardSlot   = load(folder + "/card_slot.png");
        pileMat    = load(folder + "/pile_mat.png");
        reelFrame  = load(machine + "reel_frame.png");
        reelCell   = load(machine + "reel_cell.png");
        feltLine   = load(folder + "/felt_line.png");
        feltEmblem = load(folder + "/felt_emblem.png");
        bulbs      = load(folder + "/bulbs.png");
        bulbGlow   = loadSmooth(folder + "/bulb_glow.png");
        feltLight  = loadSmooth(folder + "/felt_light.png");
    }

    /**
     * @return la table d'un lieu de l'Exploration (rebord, feutre et emblème à son
     *         air), ou la table de casino d'origine pour le Casino et hors Exploration
     */
    public static TableTextures forPlace(Place place) {
        return forPlace(place, null, null);
    }

    /**
     * @param felt    cosmétique « Tapis » choisi ({@code null} ou d'origine : le feutre du lieu)
     * @param machine cosmétique « Machine » choisi ({@code null} ou d'origine : la machine du lieu)
     * @return la table du lieu {@code place} (voir {@link #forPlace(Place)}), habillée des cosmétiques choisis
     */
    public static TableTextures forPlace(Place place, Cosmetic felt, Cosmetic machine) {
        String folder = place == null || place == Place.CASINO ? "table"
            : "table/" + place.name().toLowerCase(java.util.Locale.ROOT);
        return new TableTextures(folder, felt == null ? null : felt.getFeltPath(),
            machine == null ? null : machine.getMachineFolder());
    }

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

    /** Charge une texture de dégradé, lissée quand elle est étirée (contrairement au pixel art). */
    private static Texture loadSmooth(String path) {
        Texture texture = load(path);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
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
        bulbs.dispose();
        bulbGlow.dispose();
        feltLight.dispose();
        pixel.dispose();
    }
}
