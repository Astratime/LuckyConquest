package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.Rival;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes du Joueur Masqué (voir {@link Rival}), rejouées dans le menu Cinématique :
 * <ul>
 *   <li>{@link Scene#ENCOUNTER} : avant chacune des quatre premières rencontres (Foire, Port, Mines, Casino).
 *       Un projecteur s'allume sur le lieu, il entre, soulève son chapeau, et le symbole qu'il a appris
 *       là rejoint sa machine ; ses yeux brillent sous le masque, et sa réplique ;</li>
 *   <li>{@link Scene#UNMASK} : avant le duel final, au Désert de Braise. Il enlève son masque, qui
 *       tombe dans le sable : sous le masque, un autre élève du Croupier ;</li>
 *   <li>{@link Scene#FAREWELL} : après sa défaite au Désert. Il pose son masque sur le tapis, te le laisse,
 *       et s'efface dans le sable en montrant le haut de la Tour.</li>
 * </ul>
 * Les instants sont ceux du son de chaque scène (tools/sounds/generate_sounds.py, sons cutscene/rival_*).
 */
public class RivalCutscene extends Cutscene {

    /** Les trois scènes. */
    public enum Scene { ENCOUNTER, UNMASK, FAREWELL }

    // Rencontre : le projecteur, ses pas, le chapeau, le symbole, les yeux, la réplique.
    public static final float SPOT = 0.5f, WALK_START = 0.7f, WALK_END = 1.5f, TIP = 1.8f, SYMBOL = 2.4f,
        EYES = 3.0f, LINE = 3.2f, SUBTITLE = 4.4f, ENCOUNTER_COVER = 5.6f, ENCOUNTER_FULL = 6.2f;
    // Démasqué : sa première réplique, sa main au masque, le masque arraché, ses deux répliques.
    public static final float UNMASK_LINE = 0.9f, GRAB = 2.4f, MASK_OFF = 3.0f, UNMASK_LINE_2 = 4.0f,
        UNMASK_LINE_3 = 5.6f, UNMASK_COVER = 7.2f, UNMASK_FULL = 7.9f;
    // Adieux : le masque posé, sa réplique, il s'efface, sa dernière réplique.
    public static final float LAY = 0.8f, FAREWELL_LINE = 1.6f, VANISH_START = 3.0f, VANISH_END = 4.6f,
        FAREWELL_LINE_2 = 4.2f, FAREWELL_COVER = 5.6f, FAREWELL_FULL = 6.3f;

    private static final Color GOLD = hex("ffd54a"), TEAL = hex("4af0d8"), SAND = hex("e8b060"), FELT = hex("1e6a3a"),
        FELT_DARK = hex("124a28"), WOOD = hex("4a2a14");
    private static final String[] MASK = {
        "y.............d",
        "yy...........dd",
        ".lllllyyyyyddd.",
        ".yy..yyyyyy..d.",
        ".yy..yy.ddd..d.",
        "..yyyyy.ddddd..",
        "...yyy...ddd...",
    };
    /** Haut du portrait (rangées de pixels) et centre de ses yeux sous le masque, en pixels du portrait. */
    private static final float PORTRAIT_W = 34f, PORTRAIT_H = 42f, EYE_LEFT = 13.5f, EYE_RIGHT = 20.5f, EYE_ROW = 15.5f;

    private final Scene scene;
    private final EnemyKind kind;
    private final int encounter;
    private final TextureRegion masked, unmasked, mask, symbol, map;
    private final BitmapFont font;
    private final Array<Particle> parts = new Array<>(false, 256);
    private float previous;

    /** @param kind la rencontre du Joueur Masqué (pour le lieu, le symbole appris et la réplique) */
    public RivalCutscene(CutsceneKit kit, EnemyKind kind, Scene scene) {
        super(kit.settings(), kit.shake(), kit.sound(soundOf(scene)));
        this.scene = scene;
        this.kind = kind;
        this.encounter = Rival.encounter(kind);
        masked = kit.enemies() == null ? null : kit.portrait(Rival.isFinal(kind) ? EnemyKind.RIVAL_CASINO : kind);
        unmasked = kit.enemies() == null ? null : kit.portrait(EnemyKind.RIVAL_DESERT);
        EnemySymbol learned = learned(kind);
        symbol = kit.enemies() == null || learned == null ? null : kit.symbol(learned);
        Texture place = kit.enemies() == null ? null : kit.enemies().map(Rival.placeOf(kind));
        map = place == null ? null : new TextureRegion(place);
        mask = art("yld", new Color[] {GOLD, hex("fff2a0"), hex("c4891e")}, MASK);
        font = kit.font();
    }

    /** @return la scène qui joue avant le duel contre {@code kind}. */
    public static RivalCutscene before(CutsceneKit kit, EnemyKind kind) {
        return new RivalCutscene(kit, kind, Rival.isFinal(kind) ? Scene.UNMASK : Scene.ENCOUNTER);
    }

    /** @return le nom du son de la scène (sounds/cutscene/). */
    static String soundOf(Scene scene) {
        return switch (scene) {
            case ENCOUNTER -> "rival";
            case UNMASK -> "rival_unmask";
            case FAREWELL -> "rival_farewell";
        };
    }

    /** @return le symbole que le Joueur Masqué a appris dans le lieu de la rencontre {@code kind}. */
    public static EnemySymbol learned(EnemyKind kind) {
        return switch (kind) {
            case RIVAL_FOIRE -> EnemySymbol.RIGGED_GAME;
            case RIVAL_RAPIDE -> EnemySymbol.HOLDUP;
            case RIVAL_MANOIR -> EnemySymbol.BLOOD_PACT;
            case RIVAL_PORT -> EnemySymbol.BOARDING;
            case RIVAL_PALACE -> EnemySymbol.KEY;
            case RIVAL_MINES -> EnemySymbol.DRILL;
            case RIVAL_CASINO -> EnemySymbol.BANK_BITE;
            case RIVAL_TEMPLE -> EnemySymbol.DOUBLE_SIX;
            case RIVAL_DESERT -> EnemySymbol.BILL;
            default -> null;
        };
    }

    /** @return la réplique du Joueur Masqué à la rencontre {@code kind}, avant le duel. */
    public static String line(EnemyKind kind) {
        return switch (kind) {
            case RIVAL_FOIRE -> Lang.t("TOI AUSSI, TU VEUX LA TABLE DU CROUPIER ?");
            case RIVAL_RAPIDE -> Lang.t("MÊME TRAIN, MÊME TABLE. DANS LE FOURGON, J'AI APPRIS À BRAQUER.");
            case RIVAL_MANOIR -> Lang.t("LE COMTE M'A INVITÉ À SA TABLE. J'Y AI APPRIS À BOIRE TON SANG.");
            case RIVAL_PORT -> Lang.t("ENCORE TOI ? AU PORT, J'AI APPRIS À VOLER LES CARTES.");
            case RIVAL_PALACE -> Lang.t("LE CONCIERGE M'A PRÊTÉ SES CLÉS. TES MEILLEURES CARTES RESTERONT AU VESTIAIRE.");
            case RIVAL_MINES -> Lang.t("LA MINE M'A APPRIS À PERCER LES BOUCLIERS.");
            case RIVAL_CASINO -> Lang.t("SOUS L'EAU, J'AI APPRIS À MORDRE LES GAINS.");
            case RIVAL_TEMPLE -> Lang.t("AU TEMPLE, LES MOINES M'ONT APPRIS À FAIRE PARLER LES DÉS.");
            default -> Lang.t("TU M'AS SUIVI JUSQU'ICI.");
        };
    }

    @Override protected float coverStart() {
        return switch (scene) {
            case ENCOUNTER -> ENCOUNTER_COVER;
            case UNMASK -> UNMASK_COVER;
            case FAREWELL -> FAREWELL_COVER;
        };
    }

    @Override protected float coverFull() {
        return switch (scene) {
            case ENCOUNTER -> ENCOUNTER_FULL;
            case UNMASK -> UNMASK_FULL;
            case FAREWELL -> FAREWELL_FULL;
        };
    }

    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        parts.clear();
        previous = 0f;
    }

    private boolean at(float moment) { return previous < moment && time >= moment; }

    // -------------------------------------------------------------------------
    // Ce qui bouge
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        float w = worldWidth(), h = worldHeight();
        if (random.nextFloat() < delta * (scene == Scene.ENCOUNTER ? 12f : 30f)) { // poussière, ou sable du désert
            Particle dust = particle(-20f, h * random.nextFloat(), MathUtils.random(-10f, 10f), MathUtils.random(60f, 160f), 4f,
                MathUtils.random(3f, 7f));
            dust.color.set(scene == Scene.ENCOUNTER ? hex("fff2c0") : SAND);
            dust.color.a = 0.5f;
            dust.drag = 0f;
            parts.add(dust);
        }
        switch (scene) {
            case ENCOUNTER -> {
                if (at(SPOT)) rumble(0.1f, 3f);
                if (at(SYMBOL)) burst(w / 2f + PORTRAIT_W * portraitScale(h) * 0.85f, h * 0.3f, GOLD, 30);
                if (at(EYES)) for (float x : eyes(w, h)) burst(x, eyeY(h), TEAL, 8);
            }
            case UNMASK -> {
                if (at(MASK_OFF)) {
                    rumble(0.35f, 8f);
                    burst(w / 2f, eyeY(h), GOLD, 50);
                }
            }
            case FAREWELL -> {
                if (at(LAY)) burst(w / 2f, h * 0.22f, GOLD, 24);
                if (time > VANISH_START && time < VANISH_END) {                // il s'efface en sable
                    for (int k = 0; k < 4; k++) {
                        float x = w / 2f + MathUtils.random(-PORTRAIT_W, PORTRAIT_W) * portraitScale(h) / 2f;
                        float y = portraitY(h) + random.nextFloat() * PORTRAIT_H * portraitScale(h);
                        Particle grain = particle(x, y, MathUtils.random(-20f, 30f), MathUtils.random(80f, 260f), 1.6f, MathUtils.random(4f, 9f));
                        grain.color.set(SAND);
                        grain.drag = 0.6f;
                        parts.add(grain);
                    }
                }
            }
        }
        update(parts, delta);
        previous = time;
    }

    private void burst(float x, float y, Color color, int count) {
        for (int k = 0; k < count; k++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), 320f * random.nextFloat(), 0.8f, 9f);
            spark.color.set(color);
            spark.drag = 0.2f;
            parts.add(spark);
        }
    }

    // -------------------------------------------------------------------------
    // Ce qui se dessine
    // -------------------------------------------------------------------------

    @Override
    protected void drawScene(Batch batch) {
        float w = worldWidth(), h = worldHeight();
        drawBackdrop(batch, w, h);
        switch (scene) {
            case ENCOUNTER -> drawEncounter(batch, w, h);
            case UNMASK -> drawUnmask(batch, w, h);
            case FAREWELL -> drawFarewell(batch, w, h);
        }
        drawParticles(batch, parts);
        fadeFromBlack(batch, 0.4f);
    }

    /** Le lieu de la rencontre, assombri, et le cône du projecteur. */
    private void drawBackdrop(Batch batch, float w, float h) {
        fill(batch, hex("07050c"), 1f);
        if (map != null) {
            float light = scene == Scene.ENCOUNTER ? 0.18f + 0.17f * progress(SPOT, SPOT + 0.3f) : 0.3f;
            batch.setColor(light, light * 0.95f, light * 1.1f, 1f);
            float mapH = w * map.getRegionHeight() / map.getRegionWidth();
            batch.draw(map, -MARGIN, (h - mapH) / 2f, w + 2f * MARGIN, mapH + 2f * MARGIN);
        }
        if (scene == Scene.FAREWELL) {                                          // le tapis de jeu
            batch.setColor(WOOD);
            batch.draw(pixel, -MARGIN, -MARGIN, w + 2f * MARGIN, h * 0.3f + MARGIN);
            batch.setColor(FELT_DARK);
            batch.draw(pixel, w * 0.05f, -MARGIN, w * 0.9f, h * 0.28f + MARGIN);
            batch.setColor(FELT);
            batch.draw(pixel, w * 0.07f, -MARGIN, w * 0.86f, h * 0.26f + MARGIN);
        }
        float spot = scene == Scene.ENCOUNTER ? progress(SPOT, SPOT + 0.15f) : 1f;
        if (spot > 0f) {
            additive(batch);
            glow(batch, w / 2f, h * 0.45f, h * 1.1f, hex("fff0c8"), 0.22f * spot);
            normal(batch);
        }
    }

    /** Une rencontre : il entre par la droite, salue, et le symbole appris rejoint sa machine. */
    private void drawEncounter(Batch batch, float w, float h) {
        float scale = portraitScale(h);
        float walk = Interpolation.pow2Out.apply(progress(WALK_START, WALK_END));
        float x = MathUtils.lerp(w + PORTRAIT_W * scale, w / 2f, walk);
        float bob = walk < 1f ? Math.abs(MathUtils.sin(time * 9f)) * 10f : 0f;
        if (masked != null && time >= WALK_START) {
            float tip = progress(TIP, TIP + 0.25f) * (1f - progress(TIP + 0.5f, TIP + 0.8f));
            drawPortrait(batch, masked, x, portraitY(h) + bob, scale, -6f * tip, 1f);
        }
        if (symbol != null && time >= SYMBOL) {                                // le symbole appris, au-dessus de lui
            float rise = Interpolation.swingOut.apply(progress(SYMBOL, SYMBOL + 0.5f));
            float sx = w / 2f + PORTRAIT_W * scale * 0.85f;                     // à sa droite, à hauteur d'épaule
            float y = MathUtils.lerp(h * 0.3f, h * 0.55f, rise);
            additive(batch);
            glow(batch, sx, y, 320f, GOLD, 0.5f);
            normal(batch);
            batch.setColor(Color.WHITE);
            sprite(batch, symbol, sx, y, 6f * Math.min(1f, rise + 0.2f), 8f * MathUtils.sin(time * 3f));
        }
        if (time >= EYES) drawEyes(batch, w, h, 1f - progress(EYES + 0.9f, EYES + 1.6f) * 0.6f);
        drawCaption(batch, line(kind), w / 2f, h * 0.9f, progress(LINE, LINE + 0.3f), 2f);
        drawCaption(batch, Lang.f("RENCONTRE {0} SUR {1}", encounter, Place.values().length - 1), w / 2f, h * 0.08f,
            progress(SUBTITLE, SUBTITLE + 0.3f), 1.4f);
    }

    /** Le duel final : il arrache son masque, qui tombe dans le sable. */
    private void drawUnmask(Batch batch, float w, float h) {
        float scale = portraitScale(h), y = portraitY(h);
        boolean off = time >= MASK_OFF;
        float shiver = time > GRAB && !off ? MathUtils.sin(time * 60f) * 2f : 0f;
        TextureRegion face = off ? unmasked : masked;
        if (face != null) drawPortrait(batch, face, w / 2f + shiver, y, scale, 0f, 1f);
        if (!off) {
            if (time >= GRAB) {                                                 // sa main sur le masque, qui brille
                additive(batch);
                glow(batch, w / 2f, eyeY(h), 300f, GOLD, 0.5f * progress(GRAB, MASK_OFF));
                normal(batch);
            }
            drawEyes(batch, w, h, 1f);
        } else {                                                                // le masque vole, puis tombe
            float t = time - MASK_OFF;
            float mx = w / 2f - 420f * Math.min(t, 1.1f), my = eyeY(h) + 520f * t - 520f * t * t;
            my = Math.max(my, h * 0.24f);
            batch.setColor(Color.WHITE);
            sprite(batch, mask, mx, my, scale * 1.1f, t < 1.1f ? t * 540f : 594f);
        }
        flash(batch, Color.WHITE, MASK_OFF, 0.35f, 0.9f);
        drawCaption(batch, line(kind), w / 2f, h * 0.9f,
            progress(UNMASK_LINE, UNMASK_LINE + 0.3f) * (1f - progress(GRAB, GRAB + 0.3f)), 2f);
        drawCaption(batch, Lang.t("LE CROUPIER NOUS A TOUT APPRIS, À TOI COMME À MOI."), w / 2f, h * 0.9f,
            progress(UNMASK_LINE_2, UNMASK_LINE_2 + 0.3f), 1.8f);
        drawCaption(batch, Lang.t("IL NE RESTERA QU'UN SEUL ÉLÈVE."), w / 2f, h * 0.08f,
            progress(UNMASK_LINE_3, UNMASK_LINE_3 + 0.3f), 2.2f);
    }

    /** Après sa défaite : il te laisse son masque sur le tapis, et s'efface en sable. */
    private void drawFarewell(Batch batch, float w, float h) {
        float scale = portraitScale(h);
        float alpha = 1f - progress(VANISH_START, VANISH_END);
        if (unmasked != null && alpha > 0f) drawPortrait(batch, unmasked, w / 2f, portraitY(h), scale, 0f, alpha);
        float down = Interpolation.bounceOut.apply(progress(LAY - 0.4f, LAY));
        float my = MathUtils.lerp(h * 0.6f, h * 0.13f, down);
        additive(batch);
        glow(batch, w / 2f, my, 300f, GOLD, 0.35f + 0.15f * MathUtils.sin(time * 4f));
        normal(batch);
        batch.setColor(Color.WHITE);
        sprite(batch, mask, w / 2f, my, scale * 1.2f, 0f);
        drawCaption(batch, Lang.t("BIEN JOUÉ. GARDE LE MASQUE."), w / 2f, h * 0.9f,
            progress(FAREWELL_LINE, FAREWELL_LINE + 0.3f) * (1f - progress(FAREWELL_LINE_2 - 0.3f, FAREWELL_LINE_2)), 2f);
        drawCaption(batch, Lang.t("LE CROUPIER T'ATTEND, AU BOUT DU CHEMIN."), w / 2f, h * 0.9f,
            progress(FAREWELL_LINE_2, FAREWELL_LINE_2 + 0.3f), 1.8f);
    }

    /** Ses yeux qui brillent sous le masque. */
    private void drawEyes(Batch batch, float w, float h, float alpha) {
        float lit = progress(EYES, EYES + 0.2f);
        if (scene == Scene.UNMASK) lit = 0.6f + 0.4f * MathUtils.sin(time * 5f);
        if (lit <= 0f) return;
        additive(batch);
        for (float x : eyes(w, h)) glow(batch, x, eyeY(h), 90f, TEAL, 0.8f * lit * alpha);
        normal(batch);
    }

    private void drawPortrait(Batch batch, TextureRegion region, float x, float y, float scale, float rotation, float alpha) {
        float width = region.getRegionWidth() * scale, height = region.getRegionHeight() * scale;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(region, x - width / 2f, y, width / 2f, 0f, width, height, 1f, 1f, rotation);
    }

    private void drawCaption(Batch batch, String text, float x, float y, float alpha, float size) {
        if (font == null || alpha <= 0f) return;
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * size);
        caption(batch, font, text, x, y, alpha);
        font.getData().setScale(scale);
    }

    private float portraitScale(float h) { return h * (scene == Scene.FAREWELL ? 0.5f : 0.62f) / PORTRAIT_H; }

    /** @return le bas du portrait : derrière le tapis pour les adieux, le masque posé devant lui. */
    private float portraitY(float h) { return h * (scene == Scene.FAREWELL ? 0.3f : 0.16f); }

    private float eyeY(float h) { return portraitY(h) + (PORTRAIT_H - EYE_ROW) * portraitScale(h); }

    private float[] eyes(float w, float h) {
        float s = portraitScale(h), left = w / 2f - PORTRAIT_W * s / 2f;
        return new float[] {left + EYE_LEFT * s, left + EYE_RIGHT * s};
    }
}
