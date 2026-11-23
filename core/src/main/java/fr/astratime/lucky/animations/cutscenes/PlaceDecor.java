package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.views.FrontScene;

/**
 * Le vrai décor du lieu d'un boss ({@link FrontScene.Decor}, celui du combat), posé dans une cinématique comme en
 * combat : il couvre l'écran (sans lissage), calé en haut, et s'éclaire comme le ciel du lieu. Partagé par les
 * phases et morts ({@link BossScene}) et les entrées des rois ({@link KingEntrance}) : les deux visent les mêmes
 * pixels du décor avec {@link #x} et {@link #y}.
 */
final class PlaceDecor {

    private final Cutscene scene;
    /** L'image du décor ({@code null} sans fichiers, dans les tests). */
    private final TextureRegion image;
    private final Color sky;

    PlaceDecor(Cutscene scene, EnemyKind kind) {
        this.scene = scene;
        String path = FrontScene.Decor.of(kind).getPath();
        boolean exists = com.badlogic.gdx.Gdx.files != null && com.badlogic.gdx.Gdx.files.internal(path).exists();
        image = exists ? scene.load(path) : null;
        sky = BossTheme.of(kind).skyLow();
    }

    /** @return l'image du décor ({@code null} s'il manque). */
    TextureRegion image() { return image; }

    /** @return la taille à l'écran d'un pixel du décor (il couvre tout l'écran, calé en haut). */
    float scale() {
        if (image == null) return 1f;
        float m = 2f * Cutscene.MARGIN;
        return Math.max((scene.worldWidth() + m) / image.getRegionWidth(), (scene.worldHeight() + m) / image.getRegionHeight());
    }

    /** @return l'abscisse à l'écran du bord gauche de la colonne {@code px} du décor. */
    float x(float px) {
        return image == null ? px : scene.worldWidth() / 2f + (px - image.getRegionWidth() / 2f) * scale();
    }

    /** @return l'ordonnée à l'écran du bord haut de la ligne {@code py} du décor (comptée depuis le haut). */
    float y(float py) { return scene.worldHeight() + Cutscene.MARGIN - py * scale(); }

    /** @return {@code out}, la couleur {@code color} éclairée à {@code light} (0 à 1) sous le ciel du lieu. */
    Color lit(Color color, float light, Color out) {
        float l = MathUtils.clamp(light, 0f, 1f);
        return out.set(color.r * l * (0.75f + 0.25f * sky.r), color.g * l * (0.75f + 0.25f * sky.g),
            color.b * l * (0.75f + 0.25f * sky.b), 1f);
    }

    /** Dessine l'image du décor sur tout l'écran, de la couleur déjà mise sur {@code batch}. */
    void drawImage(Batch batch) {
        if (image == null) return;
        float scale = scale(), w = image.getRegionWidth() * scale, h = image.getRegionHeight() * scale;
        batch.draw(image, scene.worldWidth() / 2f - w / 2f, scene.worldHeight() + Cutscene.MARGIN - h, w, h);
    }

    /** Dessine le morceau ({@code px}, {@code py}, {@code w}, {@code h}) du décor à sa place, de la couleur déjà mise. */
    void drawPart(Batch batch, int px, int py, int w, int h, float dx, float dy) {
        if (image == null) return;
        TextureRegion part = new TextureRegion(image, px, py, w, h);
        batch.draw(part, x(px) + dx, y(py + h) + dy, w * scale(), h * scale());
    }
}
