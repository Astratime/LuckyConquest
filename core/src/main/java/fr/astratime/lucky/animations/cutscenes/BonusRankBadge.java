package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Le rang d'un Jeu bonus (1 à 4, la grille est au rang 4), sur une plaque dorée :
 * « RANG n » et quatre losanges, dont n allumés. Plus le rang est haut, plus le jeu rapporte.
 */
final class BonusRankBadge {

    private static final Color GOLD = hex("ffc93a"), GOLD_LIGHT = hex("ffe58a"), GOLD_DARK = hex("b8801f");
    private static final Color INK = hex("1c0f24"), OFF = hex("3a2a44");

    private BonusRankBadge() {}

    /**
     * Dessine la plaque centrée sur ({@code x}, {@code y}). Elle apparaît à partir de {@code from}
     * (secondes de la scène), les losanges s'allument un par un. Sans {@code halo}, la plaque reste en pur pixel art.
     */
    static void draw(Cutscene scene, Batch batch, BitmapFont font, int rank, float x, float y, float from, float alpha, boolean halo) {
        float pop = Interpolation.swingOut.apply(scene.progress(from, from + 0.4f));
        if (pop <= 0.01f || alpha <= 0f) return;
        float w = 150f * pop, h = 72f * pop;
        float pulse = 0.5f + 0.5f * MathUtils.sin(scene.time * 3f);
        if (halo) scene.glow(batch, x, y, w * 1.9f, GOLD, (0.12f + 0.08f * pulse) * alpha);
        plate(scene, batch, x - w / 2f - 4f, y - h / 2f - 4f, w + 8f, h + 8f, GOLD_DARK, alpha);
        plate(scene, batch, x - w / 2f - 1f, y - h / 2f - 1f, w + 2f, h + 2f, GOLD, alpha);
        plate(scene, batch, x - w / 2f + 2f, y - h / 2f + 2f, w - 4f, h - 4f, INK, alpha);
        // petites vis dorées aux coins
        for (int corner = 0; corner < 4; corner++) {
            float cx = x + (corner % 2 == 0 ? -1 : 1) * (w / 2f - 8f), cy = y + (corner < 2 ? -1 : 1) * (h / 2f - 8f);
            plate(scene, batch, cx - 2.5f, cy - 2.5f, 5f, 5f, GOLD_DARK, alpha);
        }
        font.getData().setScale(1.15f * pop);
        scene.caption(batch, font, Lang.f("RANG {0}", rank), x, y + h * 0.17f, alpha);
        font.getData().setScale(1f);
        float size = 13f * pop, step = 26f * pop;
        for (int i = 0; i < BonusGame.Kind.values().length; i++) {
            float px = x + (i - 1.5f) * step, py = y - h * 0.22f;
            boolean lit = i < rank && scene.time >= from + 0.35f + i * 0.12f;
            if (lit && halo) scene.glow(batch, px, py, size * 3.4f, GOLD_LIGHT, (0.3f + 0.25f * pulse) * alpha);
            Color color = lit ? GOLD_LIGHT : OFF;
            batch.setColor(color.r, color.g, color.b, alpha);
            batch.draw(scene.pixel, px - size / 2f, py - size / 2f, size / 2f, size / 2f, size, size, 1f, 1f, 45f);
        }
    }

    private static void plate(Cutscene scene, Batch batch, float x, float y, float w, float h, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(scene.pixel, x, y, w, h);
    }
}
