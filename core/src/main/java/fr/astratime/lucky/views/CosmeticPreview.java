package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.progress.Cosmetic;

import java.util.HashMap;
import java.util.Map;

/**
 * Aperçus des cosmétiques (onglet « Cosmétiques » de la Boutique) : un dos de
 * cartes, un morceau de tapis dans sa bordure, ou trois fenêtres de machine.
 * Les images sont gardées ici et libérées par {@link #dispose()}.
 */
public final class CosmeticPreview implements Disposable {

    private static final String CLASSIC_BACK = "cards/light/BACK.png";
    /** Symboles montrés dans l'aperçu d'une machine. */
    private static final Symbol[] PREVIEW_SYMBOLS = {Symbol.SEVEN, Symbol.CHERRY, Symbol.BELL};

    /** Images des aperçus, par chemin d'asset. */
    private final Map<String, Texture> textures = new HashMap<>();

    /** @return l'aperçu de {@code cosmetic}, de taille {@code width} x {@code height}, sans toucher. */
    public Group build(Cosmetic cosmetic, float width, float height) {
        Group group = new Group();
        group.setSize(width, height);
        group.setTouchable(Touchable.disabled);
        String folder = cosmetic.getStyleFolder();
        switch (cosmetic.getKind()) {
            case BACK -> {
                String path = folder == null ? CLASSIC_BACK : cosmetic.getBackPath();
                float h = height, w = h * 190f / 270f;
                Image back = new Image(new TextureRegionDrawable(new TextureRegion(texture(path))));
                back.setBounds((width - w) / 2f, 0f, w, h);
                group.addActor(back);
            }
            case FELT -> {
                String path = folder == null ? "table/felt.png" : cosmetic.getFeltPath();
                NinePatch railPatch = new NinePatch(texture("table/table_rail.png"), TableTextures.RAIL_BORDER,
                    TableTextures.RAIL_BORDER, TableTextures.RAIL_BORDER, TableTextures.RAIL_BORDER);
                railPatch.scale(0.5f, 0.5f);
                Image rail = new Image(new NinePatchDrawable(railPatch));
                rail.setBounds(0f, 0f, width, height);
                Image felt = new Image(new TiledDrawable(new TextureRegion(texture(path))));
                float inset = 12f;
                felt.setBounds(inset, inset, width - inset * 2, height - inset * 2);
                group.addActor(felt);
                group.addActor(rail);
            }
            case MACHINE -> {
                String dir = folder == null ? "table/" : cosmetic.getMachineFolder();
                float frameH = height * 110f / 210f, frameY = (height - frameH) / 2f;
                Image frame = new Image(nine(dir + "reel_frame.png", TableTextures.REEL_FRAME_BORDER));
                frame.setBounds(0f, frameY, width, frameH);
                group.addActor(frame);
                float pad = 18f, gap = 6f;
                float cellW = (width - pad * 2 - gap * (PREVIEW_SYMBOLS.length - 1)) / PREVIEW_SYMBOLS.length;
                float cellH = frameH - pad * 2;
                for (int i = 0; i < PREVIEW_SYMBOLS.length; i++) {
                    float x = pad + i * (cellW + gap);
                    Image cellImage = new Image(nine(dir + "reel_cell.png", TableTextures.REEL_CELL_BORDER));
                    cellImage.setBounds(x, frameY + pad, cellW, cellH);
                    group.addActor(cellImage);
                    float size = Math.min(cellW, cellH) - 10f;
                    Image symbol = new Image(new TextureRegionDrawable(new TextureRegion(
                        texture(PREVIEW_SYMBOLS[i].getAssetPath()))));
                    symbol.setBounds(x + (cellW - size) / 2f, frameY + pad + (cellH - size) / 2f, size, size);
                    group.addActor(symbol);
                }
            }
        }
        return group;
    }

    private Texture texture(String path) {
        return textures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    private NinePatchDrawable nine(String path, int border) {
        return new NinePatchDrawable(new NinePatch(texture(path), border, border, border, border));
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }
}
