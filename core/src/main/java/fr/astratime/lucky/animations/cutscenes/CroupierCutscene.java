package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les cinématiques du Croupier, boss secret, au sommet de la Tour des épreuves.
 * <ul>
 *   <li>{@link Scene#INTRO} : derrière sa table, le Croupier bat les cartes. Les machines du monde
 *       s'allument une à une derrière lui, son gilet vire au pourpre, ses yeux au rouge. Il distribue
 *       cinq cartes : les tiennes.</li>
 *   <li>{@link Scene#RIG} (phase 2) : tes trois rouleaux tournent ; sa main gantée en arrête un sur le
 *       symbole qu'il choisit, et un cadenas d'or le fige.</li>
 *   <li>{@link Scene#OPEN} (phase 3) : cinq cartes faces cachées sur le tapis se retournent une à une.</li>
 *   <li>{@link Scene#ENDING} : la vraie fin. Il s'incline et glisse le sabot vers toi, les machines
 *       s'éteignent une à une, le jour se lève sur la Tour ; il s'efface, seul son chapeau tombe sur la table.</li>
 * </ul>
 * Toutes se rejouent dans le menu Cinématique.
 */
public class CroupierCutscene extends Cutscene {

    /** Les quatre scènes du Croupier. */
    public enum Scene { INTRO, RIG, OPEN, ENDING }

    private static final Color GOLD = hex("ffc93a"), RED = hex("ff2a3a"), FELT = hex("1e6a3a"), FELT_DARK = hex("124a28");
    private static final Color WOOD = hex("5a2a12"), WOOD_DARK = hex("3a1a0a"), MACHINE = hex("2a1a2e"), BULB = hex("ffd54a");
    private static final Color STEEL = hex("8a8a9a"), GLOVE = hex("f4efe4"), DAWN = hex("ffb87a");
    private static final String[] CARDS = {"cards/light/1-P.png", "cards/light/13-H.png", "cards/light/12-C.png",
        "cards/light/11-D.png", "cards/light/10-P.png"};
    private static final String[] HAT = {
        "..kkkkkk..", "..kkkkkk..", "..kkkkkk..", "..yyyyyy..", "kkkkkkkkkk", "kkkkkkkkkk"};
    private static final String[] LOCK = {
        ".yyyy.", "y....y", "y....y", "yyyyyy", "yyddyy", "yyddyy", "yyyyyy"};
    private static final String[] SHOE = {
        "wwwwwwwwwwww", "wbbbbbbbbbbw", "wbbbbbbbbbbw", "wwwwwwwwwwww"};
    private static final int MACHINES = 7;

    private final Scene scene;
    private final TextureRegion croupier, grand, back, hat, lock, shoe;
    private final TextureRegion[] cards = new TextureRegion[CARDS.length];
    private final TextureRegion[] symbols;
    private final BitmapFont font;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private float previous;

    public CroupierCutscene(CutsceneKit kit, Scene scene) {
        super(kit.settings(), kit.shake(), kit.sound(switch (scene) {
            case INTRO -> "house";
            case RIG, OPEN -> "boss_phase";
            case ENDING -> "exploration_ending";
        }));
        this.scene = scene;
        croupier = kit.portrait(EnemyKind.CROUPIER);
        grand = kit.portrait(EnemyKind.GRAND_CROUPIER);
        back = gameCard("cards/light/BACK.png");
        for (int i = 0; i < CARDS.length; i++) cards[i] = gameCard(CARDS[i]);
        symbols = new TextureRegion[] {load(Symbol.CHERRY.getAssetPath()), load(Symbol.SEVEN.getAssetPath()),
            load(Symbol.BELL.getAssetPath()), load(Symbol.BAR.getAssetPath())};
        hat = art("ky", new Color[] {hex("140c18"), GOLD}, HAT);
        lock = art("yd", new Color[] {GOLD, hex("8a5a10")}, LOCK);
        shoe = art("wb", new Color[] {WOOD, hex("c8b89a")}, SHOE);
        font = kit.font();
    }

    /** @return la durée de la scène avant le fondu final. */
    private float length() {
        return switch (scene) {
            case INTRO -> 10.6f;   // le son « house » dure 11,4 s
            case RIG, OPEN -> 4.2f; // « boss_phase » : 4,8 s
            case ENDING -> 8.4f;   // « exploration_ending » : 9 s
        };
    }

    @Override protected float coverStart() { return length(); }
    @Override protected float coverFull()  { return length() + 0.7f; }
    @Override protected Color coverColor() { return scene == Scene.ENDING ? Color.WHITE : hex("1a0204"); }

    @Override
    protected void reset() {
        sparks.clear();
        previous = 0f;
    }

    private boolean at(float moment) { return previous < moment && time >= moment; }

    @Override
    protected void simulate(float delta) {
        float w = worldWidth(), h = worldHeight();
        switch (scene) {
            case INTRO -> {
                if (at(3.8f)) burst(w / 2f, h * 0.62f, RED, 50, 600f);         // il change de couleurs
                if (at(3.8f)) rumble(0.4f, 8f);
                for (int i = 0; i < CARDS.length; i++) if (at(5.7f + i * 0.35f)) rumble(0.08f, 2f);
            }
            case RIG -> {
                if (at(2.0f)) {                                                // la main arrête ton rouleau
                    burst(w / 2f, h * 0.5f, GOLD, 40, 500f);
                    rumble(0.3f, 7f);
                }
            }
            case OPEN -> {
                for (int i = 0; i < CARDS.length; i++) {
                    if (at(1.0f + i * 0.4f)) burst(cardX(i, w), h * 0.36f, GOLD, 14, 260f);
                }
            }
            case ENDING -> {
                if (at(3.8f)) burst(w / 2f + w * 0.12f, h * 0.06f, GOLD, 24, 260f); // le sabot arrive
                if (at(7.1f)) burst(w / 2f, h * 0.24f, GOLD, 30, 300f);        // le chapeau tombe
                for (int i = 0; i < MACHINES; i++) if (at(4.2f + i * 0.35f)) rumble(0.06f, 2f);
            }
        }
        update(sparks, delta);
        previous = time;
    }

    private void burst(float x, float y, Color color, int count, float speed) {
        for (int k = 0; k < count; k++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * random.nextFloat(), 0.9f, 10f);
            spark.color.set(color);
            spark.drag = 0.2f;
            sparks.add(spark);
        }
    }

    @Override
    protected void drawScene(Batch batch) {
        float w = worldWidth(), h = worldHeight();
        switch (scene) {
            case INTRO -> drawIntro(batch, w, h);
            case RIG -> drawRig(batch, w, h);
            case OPEN -> drawOpen(batch, w, h);
            case ENDING -> drawEnding(batch, w, h);
        }
        drawParticles(batch, sparks);
        fadeFromBlack(batch, 0.4f);
    }

    // -------------------------------------------------------------------------
    // Les scènes
    // -------------------------------------------------------------------------

    /** Il bat les cartes, les machines s'allument derrière lui, il change de couleurs, il distribue. */
    private void drawIntro(Batch batch, float w, float h) {
        gradient(batch, hex("05030a"), hex("1a0610"));
        float lit = progress(0.8f, 3.6f) * MACHINES;
        drawMachines(batch, w, h, lit, 1f);
        float change = progress(3.6f, 4.2f);
        float x = w / 2f, y = h * 0.3f, scale = 12f;
        glow(batch, x, y + 42f * scale * 0.6f, 900f, RED, 0.35f * change);
        portrait(batch, croupier, x, y, scale, 0f, 1f - change);
        portrait(batch, grand, x, y, scale, 0f, change);
        drawTable(batch, w, h);
        if (time < 3.6f) {                                                     // il bat les cartes
            float shuffle = MathUtils.sin(time * 14f) * 40f;
            batch.setColor(Color.WHITE);
            sprite(batch, back, x - 60f + shuffle, h * 0.26f, 6f, 0f);
            sprite(batch, back, x + 60f - shuffle, h * 0.26f, 6f, 0f);
        }
        for (int i = 0; i < CARDS.length; i++) {                               // il distribue tes cartes
            float k = Interpolation.pow2Out.apply(progress(5.6f + i * 0.35f, 6.0f + i * 0.35f));
            if (k <= 0f) continue;
            float cx = MathUtils.lerp(x, cardX(i, w), k), cy = MathUtils.lerp(h * 0.26f, h * 0.1f, k);
            batch.setColor(Color.WHITE);
            sprite(batch, time > 7.6f ? cards[i] : back, cx, cy, 7f, (1f - k) * 30f);
        }
        line(batch, Lang.t("« TU AS APPRIS TOUT CE QUE JE SAVAIS. JOUONS. »"), w / 2f, h * 0.92f, progress(8.0f, 8.4f));
    }

    /** Tes trois rouleaux tournent ; sa main en arrête un, un cadenas d'or le fige. */
    private void drawRig(Batch batch, float w, float h) {
        gradient(batch, hex("1a0204"), hex("3a060c"));
        float reelW = w * 0.16f, reelH = h * 0.42f, gap = w * 0.03f;
        float left = w / 2f - reelW * 1.5f - gap, y = h * 0.3f;
        rect(batch, MACHINE, left - 40f, y - 40f, reelW * 3f + gap * 2f + 80f, reelH + 80f);
        rect(batch, GOLD, left - 40f, y + reelH + 30f, reelW * 3f + gap * 2f + 80f, 10f);
        for (int i = 0; i < 3; i++) {
            float x = left + i * (reelW + gap);
            rect(batch, hex("f4ecdc"), x, y, reelW, reelH);
            boolean rigged = i == 1 && time >= 2.0f;
            float offset = rigged ? 1f : time * (9f + i * 2f);
            reel(batch, symbols, x, y, reelW, reelH, offset);
        }
        float middle = left + reelW + gap + reelW / 2f;
        float reach = Interpolation.pow2Out.apply(progress(1.2f, 2.0f));          // sa main gantée descend
        float handY = MathUtils.lerp(h + 60f, y + reelH * 0.55f, reach) + (time > 2.4f ? (time - 2.4f) * 400f : 0f);
        rect(batch, hex("140c18"), middle - 30f, handY + 120f, 60f, h);
        rect(batch, GLOVE, middle - 55f, handY, 110f, 130f);
        if (time >= 2.0f) {
            float pop = Interpolation.swingOut.apply(progress(2.0f, 2.4f));
            batch.setColor(Color.WHITE);
            sprite(batch, lock, middle, y - 10f, 14f * pop, 0f);
            glow(batch, middle, y + reelH / 2f, reelW * 2.2f, GOLD, 0.35f * pop);
        }
        line(batch, Lang.t("IL TRUQUE TA TABLE !"), w / 2f, h * 0.9f, progress(2.4f, 2.8f));
    }

    /** Cinq cartes faces cachées se retournent une à une : il joue à visage découvert. */
    private void drawOpen(Batch batch, float w, float h) {
        gradient(batch, FELT_DARK, hex("05030a"));
        glow(batch, w / 2f, h * 0.4f, w * 0.9f, GOLD, 0.15f);
        portrait(batch, grand, w / 2f, h * 0.55f, 8f, 0f, 1f);
        rect(batch, FELT, w * 0.08f, -MARGIN, w * 0.84f, h * 0.55f + MARGIN);
        rect(batch, GOLD, w * 0.08f, h * 0.55f, w * 0.84f, 8f);
        for (int i = 0; i < CARDS.length; i++) {
            float flip = progress(1.0f + i * 0.4f, 1.3f + i * 0.4f);              // la carte se retourne
            float squash = Math.abs(MathUtils.cos(flip * MathUtils.PI));
            TextureRegion face = flip < 0.5f ? back : cards[i];
            float scale = 7f;
            float cw = face.getRegionWidth() * scale * squash, ch = face.getRegionHeight() * scale;
            batch.setColor(Color.WHITE);
            batch.draw(face, cardX(i, w) - cw / 2f, h * 0.36f - ch / 2f, cw, ch);
        }
        line(batch, Lang.t("CARTES SUR TABLE !"), w / 2f, h * 0.9f, progress(3.0f, 3.4f));
    }

    /** La vraie fin : il te glisse le sabot, les machines s'éteignent, le jour se lève, il disparaît sous son chapeau. */
    private void drawEnding(Batch batch, float w, float h) {
        float dawn = progress(4.0f, 7.5f);
        tmp.set(hex("05030a")).lerp(DAWN, dawn * 0.8f);
        gradient(batch, hex("05030a"), tmp.cpy());
        glow(batch, w / 2f, -h * 0.1f, w * 1.6f, DAWN, 0.5f * dawn);
        float off = progress(4.2f, 4.2f + MACHINES * 0.35f) * MACHINES;
        drawMachines(batch, w, h, MACHINES - off, 1f - 0.6f * dawn);
        float x = w / 2f, y = h * 0.3f, scale = 12f;
        float bow = Interpolation.pow2.apply(progress(1.2f, 2.0f)) - Interpolation.pow2.apply(progress(2.4f, 3.0f));
        float gone = progress(6.0f, 6.8f);                                     // il s'efface...
        portrait(batch, grand, x, y - bow * 30f, scale, 0f, 1f - gone);
        drawTable(batch, w, h);
        float slide = Interpolation.pow2Out.apply(progress(3.0f, 3.8f));        // le sabot glisse vers toi
        batch.setColor(Color.WHITE);
        sprite(batch, shoe, x + w * 0.12f, MathUtils.lerp(h * 0.25f, h * 0.06f, slide), 12f, 0f);
        if (gone > 0f) {                                                       // ...il ne reste que son chapeau
            float fall = Interpolation.bounceOut.apply(progress(6.4f, 7.1f));
            batch.setColor(Color.WHITE);
            sprite(batch, hat, x, MathUtils.lerp(y + 37f * scale, h * 0.24f, fall), 18f, fall * 8f);
        }
        line(batch, Lang.t("« À TOI DE DISTRIBUER. »"), w / 2f, h * 0.9f, progress(7.2f, 7.6f));
    }

    // -------------------------------------------------------------------------
    // Le décor
    // -------------------------------------------------------------------------

    /** Les machines du monde, alignées derrière lui ; les {@code lit} premières allumées. */
    private void drawMachines(Batch batch, float w, float h, float lit, float alpha) {
        float mw = w / (MACHINES + 1f), base = h * 0.45f;
        for (int i = 0; i < MACHINES; i++) {
            float mx = mw * (i + 0.5f) + mw / 2f - mw * 0.35f, mh = h * (0.3f + 0.06f * (i % 3));
            float on = MathUtils.clamp(lit - i, 0f, 1f);
            tmp.set(MACHINE);
            tmp.a = alpha;
            batch.setColor(tmp);
            batch.draw(pixel, mx, base, mw * 0.7f, mh);
            tmp.set(hex("0a0610")).lerp(BULB, on);
            tmp.a = alpha;
            batch.setColor(tmp);
            batch.draw(pixel, mx + mw * 0.1f, base + mh * 0.45f, mw * 0.5f, mh * 0.25f);
            for (int b = 0; b < 4; b++) {
                boolean blink = on > 0.5f && ((int) (time * 6f) + b + i) % 2 == 0;
                tmp.set(blink ? BULB : hex("3a2a1a"));
                tmp.a = alpha;
                batch.setColor(tmp);
                batch.draw(pixel, mx + mw * (0.08f + 0.16f * b), base + mh - 24f, 14f, 14f);
            }
            if (on > 0f) glow(batch, mx + mw * 0.35f, base + mh * 0.6f, mw * 1.4f, BULB, 0.18f * on * alpha);
        }
    }

    /** Sa table de jeu : le bois, le tapis vert, le liseré d'or. */
    private void drawTable(Batch batch, float w, float h) {
        rect(batch, WOOD_DARK, -MARGIN, -MARGIN, w + 2f * MARGIN, h * 0.32f + MARGIN);
        rect(batch, WOOD, -MARGIN, h * 0.3f, w + 2f * MARGIN, h * 0.02f);
        rect(batch, FELT_DARK, w * 0.05f, -MARGIN, w * 0.9f, h * 0.29f + MARGIN);
        rect(batch, FELT, w * 0.07f, -MARGIN, w * 0.86f, h * 0.27f + MARGIN);
    }

    /** @return le centre de la carte {@code i} des cinq posées sur la table. */
    private static float cardX(int i, float w) { return w * (0.22f + 0.14f * i); }

    /** Sa réplique, en grand, en haut de l'écran. */
    private void line(Batch batch, String text, float x, float y, float alpha) {
        if (font == null || alpha <= 0f) return;
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * 2f);
        caption(batch, font, text, x, y, alpha);
        font.getData().setScale(scale);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }
}
