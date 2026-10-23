package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.i18n.Lang;

/**
 * Les deux grands moments d'un boss ou d'un roi, dans son décor ({@link BossTheme}) :
 * <ul>
 *   <li>{@link Moment#PHASE} (environ 4,8 s) : blessé, il vacille. Les symboles de sa
 *   nouvelle phase convergent vers lui, sa lueur bat de plus en plus vite ; il explose
 *   de puissance (onde de choc, gerbe de symboles), grandit d'un coup et sa phase s'annonce.</li>
 *   <li>{@link Moment#DEATH} (environ 5,6 s) : il tremble, des rayons de lumière le
 *   fendent ; il vole en éclats (son portrait part en morceaux), une pluie de pièces
 *   tombe et sa dernière phrase s'écrit.</li>
 * </ul>
 * Fondu noir, puis la suite du combat. Sons : cutscene/boss_phase et cutscene/boss_death.
 */
public class BossMomentCutscene extends Cutscene {

    /** Le moment joué. */
    public enum Moment { PHASE, DEATH }

    /** Phase : les battements de sa lueur, de plus en plus rapprochés (voir le son cutscene/boss_phase). */
    static final float[] BEATS = {0.5f, 1.0f, 1.4f, 1.75f, 2.0f};
    /** Phase : il explose de puissance. */
    public static final float BURST = 2.2f;
    /** Mort : il vole en éclats. */
    public static final float SHATTER = 2.5f;
    /** Le texte s'imprime. */
    static final float PHASE_TEXT = 2.45f, DEATH_TEXT = 3.0f;
    static final float PHASE_COVER_START = 4.2f, PHASE_COVER_FULL = 4.8f;
    static final float DEATH_COVER_START = 5.0f, DEATH_COVER_FULL = 5.6f;

    /** Côté des morceaux du portrait quand il vole en éclats, en pixels du portrait. */
    private static final int SHARD = 3;

    private static final Color WHITE = c("ffffff"), HOT = c("fff6d8");

    private final Moment        moment;
    private final EnemyKind     kind;
    private final int           phase;
    private final BossTheme     theme;
    private final TextureRegion boss, symbol, coin;
    private final BitmapFont    font;
    private final String        headline, subtitle;
    private final Array<Particle> gather = new Array<>(false, 64);
    private final Array<Particle> sparks = new Array<>(false, 256);
    private final Array<Particle> pieces = new Array<>(false, 512);
    private final Array<Particle> flames = new Array<>(false, 256);
    private final Color sky = new Color(), low = new Color();
    private int     beat;
    private boolean burst;
    private float   gatherDebt, flameDebt, coinDebt;

    /** Le passage du boss {@code kind} à sa phase {@code phase}. */
    public static BossMomentCutscene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        return new BossMomentCutscene(kit, Moment.PHASE, kind, phase);
    }

    /** La mort du boss {@code kind}. */
    public static BossMomentCutscene death(CutsceneKit kit, EnemyKind kind) {
        return new BossMomentCutscene(kit, Moment.DEATH, kind, 0);
    }

    private BossMomentCutscene(CutsceneKit kit, Moment moment, EnemyKind kind, int phase) {
        super(kit.settings(), kit.shake(), kit.sound(moment == Moment.PHASE ? "boss_phase" : "boss_death"));
        this.moment = moment;
        this.kind   = kind;
        this.phase  = phase;
        theme  = BossTheme.of(kind);
        boss   = kit.portrait(kind);
        EnemySymbol motif = moment == Moment.PHASE ? theme.phaseSymbol(kind, phase) : theme.death();
        symbol = kit.enemies() == null ? null : kit.symbol(motif);
        coin   = load("hud/coin.png");
        font   = kit.font();
        if (moment == Moment.PHASE) {
            headline = kind.phaseText(phase);
            subtitle = Lang.f("{0} : PHASE {1}", kind.getBarName(), phase);
        } else {
            headline = theme.deathLine();
            subtitle = kind.getBarName();
        }
    }

    /** @return le moment joué. */
    public Moment getMoment() { return moment; }
    /** @return la phase où il entre (0 pour sa mort). */
    public int getPhase() { return phase; }
    /** @return le boss. */
    public EnemyKind getKind() { return kind; }
    /** @return la grande phrase qui s'écrit (l'annonce de sa phase, ou sa dernière phrase). */
    public String getHeadline() { return headline; }

    @Override protected float coverStart() { return moment == Moment.PHASE ? PHASE_COVER_START : DEATH_COVER_START; }
    @Override protected float coverFull()  { return moment == Moment.PHASE ? PHASE_COVER_FULL : DEATH_COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        beat = 0;
        burst = false;
        gatherDebt = flameDebt = coinDebt = 0f;
        gather.clear();
        sparks.clear();
        pieces.clear();
        flames.clear();
    }

    // -------------------------------------------------------------------------
    // Vie de la scène
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        if (moment == Moment.PHASE) simulatePhase(delta);
        else simulateDeath(delta);
        update(gather, delta);
        update(sparks, delta);
        update(pieces, delta);
        update(flames, delta);
    }

    private void simulatePhase(float delta) {
        while (beat < BEATS.length && time >= BEATS[beat]) {               // sa lueur bat
            rumble(0.12f, 2f + beat * 1.5f);
            sparks(bossX(), bossY(), 5 + 3 * beat, theme.accent(), 260f + 60f * beat, 22f);
            beat++;
        }
        if (time < BURST - 0.15f && symbol != null) {                       // les symboles convergent vers lui
            gatherDebt += 9f * delta;
            while (gatherDebt >= 1f) {
                gatherDebt -= 1f;
                float angle = 360f * random.nextFloat(), distance = worldWidth() * (0.45f + 0.2f * random.nextFloat());
                float x = bossX() + MathUtils.cosDeg(angle) * distance, y = bossY() + MathUtils.sinDeg(angle) * distance;
                float life = 0.7f + 0.3f * random.nextFloat();
                Particle part = particle(x, y, angle + 180f, distance / life, life, symbolScale() * 0.8f);
                part.region = symbol;
                part.spin = (random.nextFloat() - 0.5f) * 360f;
                gather.add(part);
            }
        }
        if (!burst && time >= BURST) {                                      // il explose de puissance
            burst = true;
            gather.clear();
            rumble(0.8f, 16f);
            sparks(bossX(), bossY(), 140, theme.accent(), 1300f, 30f);
            sparks(bossX(), bossY(), 60, HOT, 900f, 20f);
            if (symbol != null) {
                for (int i = 0; i < 16; i++) {
                    Particle part = particle(bossX(), bossY(), 360f * random.nextFloat(), 700f + 700f * random.nextFloat(),
                        1.4f + 0.6f * random.nextFloat(), symbolScale() * (0.8f + 0.6f * random.nextFloat()));
                    part.region = symbol;
                    part.gravity = 900f;
                    part.drag = 0.5f;
                    part.spin = (random.nextFloat() - 0.5f) * 720f;
                    pieces.add(part);
                }
            }
        }
        if (burst && time < PHASE_COVER_FULL) {                             // son aura brûle autour de lui
            flameDebt += 60f * delta;
            while (flameDebt >= 1f) {
                flameDebt -= 1f;
                flames.add(flame(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.2f,
                    bossBottom() + bossHeight() * 0.1f * random.nextFloat()));
            }
        }
    }

    private void simulateDeath(float delta) {
        if (time < SHATTER && random.nextFloat() < delta * (4f + 10f * time)) {   // des étincelles le quittent
            sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 0.7f,
                bossBottom() + bossHeight() * random.nextFloat(), 3, theme.accent(), 300f, 18f);
        }
        if (beat == 0 && time >= 1.2f) { beat = 1; rumble(0.3f, 4f); }
        if (beat == 1 && time >= 2.0f) { beat = 2; rumble(0.4f, 7f); }
        if (!burst && time >= SHATTER) {                                    // il vole en éclats
            burst = true;
            rumble(1.0f, 20f);
            sparks(bossX(), bossY(), 160, HOT, 1500f, 30f);
            sparks(bossX(), bossY(), 90, theme.accent(), 1100f, 26f);
            shatter();
            for (int i = 0; i < 30; i++) {                                  // il part en pièces
                Particle piece = particle(bossX(), bossY(), 30f + 120f * random.nextFloat(), 900f + 700f * random.nextFloat(),
                    2.2f + 0.6f * random.nextFloat(), coinScale() * (0.7f + 0.5f * random.nextFloat()));
                piece.region = coin;
                piece.gravity = 1600f;
                piece.spin = (random.nextFloat() - 0.5f) * 720f;
                pieces.add(piece);
            }
            if (symbol != null) {
                for (int i = 0; i < 8; i++) {
                    Particle part = particle(bossX(), bossY(), 360f * random.nextFloat(), 600f + 600f * random.nextFloat(),
                        1.8f, symbolScale() * (0.7f + 0.5f * random.nextFloat()));
                    part.region = symbol;
                    part.gravity = 1100f;
                    part.spin = (random.nextFloat() - 0.5f) * 720f;
                    pieces.add(part);
                }
            }
        }
        if (burst && time < DEATH_COVER_FULL) {                             // la pluie de pièces
            coinDebt += 22f * delta;
            while (coinDebt >= 1f) {
                coinDebt -= 1f;
                Particle piece = particle(worldWidth() * random.nextFloat(), worldHeight() + 40f, 270f,
                    300f + 300f * random.nextFloat(), 2.4f, coinScale() * (0.5f + 0.5f * random.nextFloat()));
                piece.region = coin;
                piece.gravity = 900f;
                piece.spin = (random.nextFloat() - 0.5f) * 540f;
                pieces.add(piece);
            }
        }
    }

    /** Son portrait part en morceaux carrés, projetés depuis son centre. */
    private void shatter() {
        float scale = bossScale();
        int width = boss.getRegionWidth(), height = boss.getRegionHeight();
        float left = bossX() - width * scale / 2f, bottom = bossBottom();
        for (int py = 0; py < height; py += SHARD) {
            for (int px = 0; px < width; px += SHARD) {
                int w = Math.min(SHARD, width - px), h = Math.min(SHARD, height - py);
                // y du portrait compté du haut ; à l'écran, du bas
                float x = left + (px + w / 2f) * scale, y = bottom + (height - py - h / 2f) * scale;
                float angle = MathUtils.atan2(y - bossY(), x - bossX()) * MathUtils.radiansToDegrees
                    + (random.nextFloat() - 0.5f) * 40f;
                Particle part = particle(x, y, angle, 400f + 900f * random.nextFloat(), 1.4f + 0.8f * random.nextFloat(), scale);
                part.region = new TextureRegion(boss, px, py, w, h);
                part.gravity = 1300f;
                part.drag = 0.6f;
                part.spin = (random.nextFloat() - 0.5f) * 600f;
                pieces.add(part);
            }
        }
    }

    private Particle flame(float x, float y) {
        Particle flame = particle(x, y, 80f + 20f * random.nextFloat(), 140f + 240f * random.nextFloat(),
            0.6f + 0.5f * random.nextFloat(), 40f + 60f * random.nextFloat());
        flame.color.set(random.nextFloat() < 0.5f ? theme.accent() : HOT);
        flame.drag = 0.6f;
        return flame;
    }

    private void sparks(float x, float y, int count, Color color, float speed, float size) {
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
                0.5f + 0.6f * random.nextFloat(), size * (0.5f + random.nextFloat()));
            spark.drag = 0.2f;
            spark.color.set(color);
            sparks.add(spark);
        }
    }

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float heat = moment == Moment.PHASE ? Interpolation.pow2Out.apply(progress(BURST, BURST + 0.6f)) : 0f;
        float dim  = moment == Moment.DEATH ? progress(SHATTER, SHATTER + 1.5f) * 0.5f : 0f;
        low.set(theme.skyLow()).lerp(theme.accent(), 0.3f * heat).lerp(Color.BLACK, dim);
        sky.set(theme.skyTop()).lerp(Color.BLACK, dim);
        gradient(batch, low, sky);
        drawRays(batch);
        batch.setColor(theme.ground());                                      // le sol
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.18f + MARGIN);

        float x = bossX(), y = bossY();
        float pulse = pulse();
        glow(batch, x, y, bossWidth() * (1.6f + 0.6f * pulse + 2.4f * heat), theme.accent(), 0.2f + 0.3f * pulse + 0.4f * heat);
        drawParticles(batch, flames);
        drawBoss(batch, pulse, heat);
        drawWave(batch, x, y);
        drawParticles(batch, gather);
        drawParticles(batch, pieces);
        drawParticles(batch, sparks);
        drawTexts(batch);
        flash(batch, moment == Moment.PHASE ? theme.accent() : WHITE, burst ? (moment == Moment.PHASE ? BURST : SHATTER) : 99f,
            0.4f, 0.8f);
        fadeFromBlack(batch, 0.4f);
    }

    /** Le boss : il vacille (phase) ou tremble et blanchit (mort) ; en phase, il grandit d'un coup. */
    private void drawBoss(Batch batch, float pulse, float heat) {
        if (moment == Moment.DEATH && burst) {                             // il a volé en éclats : son ombre blanche s'élève
            float k = progress(SHATTER, SHATTER + 2.2f);
            if (k >= 1f) return;
            float scale = bossScale() * (1f + 0.15f * k);
            float width = boss.getRegionWidth() * scale, height = boss.getRegionHeight() * scale;
            additive(batch);
            batch.setColor(HOT.r, HOT.g, HOT.b, 0.35f * (1f - k));
            batch.draw(boss, bossX() - width / 2f, bossBottom() + k * worldHeight() * 0.12f, width, height);
            normal(batch);
            return;
        }
        float scale = bossScale(), dx = 0f, shadow = 0f, white = 0f;
        if (moment == Moment.PHASE) {
            float swell = 1f + 0.15f * Interpolation.swingOut.apply(progress(BURST, BURST + 0.5f));
            scale *= swell;
            if (!burst) shadow = MathUtils.clamp(0.45f - 0.35f * pulse + (random.nextFloat() < 0.08f ? 0.15f : 0f), 0f, 1f);
            dx = burst ? 0f : MathUtils.sin(time * 9f) * 4f;
        } else {
            float k = progress(0.2f, SHATTER);
            dx = (random.nextFloat() - 0.5f) * 30f * k;
            white = k * k;
        }
        float bob = MathUtils.sin(time * 2.5f) * 6f;
        portrait(batch, boss, bossX() + dx, bossBottom() + bob, scale, shadow, 1f);
        float tint = moment == Moment.PHASE ? 0.35f * heat * (0.7f + 0.3f * MathUtils.sin(time * 10f)) : white;
        if (tint > 0f) {                                                    // sa couleur (phase) ou le blanc (mort) le gagne
            float width = boss.getRegionWidth() * scale, height = boss.getRegionHeight() * scale;
            Color color = moment == Moment.PHASE ? theme.accent() : WHITE;
            additive(batch);
            batch.setColor(color.r, color.g, color.b, tint);
            batch.draw(boss, bossX() + dx - width / 2f, bossBottom() + bob, width, height);
            normal(batch);
        }
    }

    /** Mort : des rayons de lumière le fendent, de plus en plus nombreux. */
    private void drawRays(Batch batch) {
        if (moment != Moment.DEATH || burst) return;
        float k = progress(0.4f, SHATTER);
        int count = (int) (2 + 12 * k);
        additive(batch);
        for (int i = 0; i < count; i++) {
            float angle = i * 137.5f + time * (i % 2 == 0 ? 12f : -9f);
            float length = worldWidth() * (0.4f + 0.5f * k);
            float thick = 30f + 50f * k;
            Color color = i % 3 == 0 ? theme.accent() : HOT;
            batch.setColor(color.r, color.g, color.b, 0.25f + 0.35f * k);
            batch.draw(trail, bossX(), bossY() - thick / 2f, 0f, thick / 2f, length, thick, 1f, 1f, angle);
        }
        normal(batch);
    }

    /** L'onde de choc, au moment fort. */
    private void drawWave(Batch batch, float x, float y) {
        if (!burst) return;
        float k = progress(moment == Moment.PHASE ? BURST : SHATTER, (moment == Moment.PHASE ? BURST : SHATTER) + 0.9f);
        if (k >= 1f) return;
        float size = worldWidth() * 2.2f * Interpolation.pow2Out.apply(k);
        Color color = moment == Moment.PHASE ? theme.accent() : HOT;
        additive(batch);
        batch.setColor(color.r, color.g, color.b, 0.8f * (1f - k));
        batch.draw(ring, x - size / 2f, y - size / 2f, size, size);
        normal(batch);
    }

    /** La grande phrase (annonce de sa phase, ou sa dernière phrase), qui s'imprime en bas, et son nom au-dessus. */
    private void drawTexts(Batch batch) {
        if (font == null) return;
        float at = moment == Moment.PHASE ? PHASE_TEXT : DEATH_TEXT;
        float show = progress(at, at + 0.3f);
        if (show <= 0f) return;
        float width = worldWidth(), height = worldHeight();
        batch.setColor(0f, 0f, 0f, 0.6f * show);
        batch.draw(soft, width * 0.05f, -height * 0.02f, width * 0.9f, height * 0.26f);
        float base = font.getData().scaleX;
        font.getData().setScale(base * 1.8f);
        caption(batch, font, subtitle, width / 2f, height * 0.16f, show * 0.85f);
        float stamp = MathUtils.lerp(4.0f, 2.8f, Interpolation.pow2Out.apply(show));
        font.getData().setScale(base * fit(headline, stamp, width * 0.9f, base));
        caption(batch, font, headline, width / 2f, height * 0.08f, show);
        font.getData().setScale(base);
    }

    /** @return l'agrandissement {@code wanted} (en fois la taille de base), réduit pour que {@code text} tienne dans {@code max}. */
    private float fit(String text, float wanted, float max, float base) {
        font.getData().setScale(base * wanted);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
        return layout.width <= max ? wanted : wanted * max / layout.width;
    }

    /** @return l'éclat du dernier battement de sa lueur (1 au battement, puis il retombe). */
    private float pulse() {
        if (moment != Moment.PHASE || beat == 0 || burst) return 0f;
        return Math.max(0f, 1f - (time - BEATS[beat - 1]) / 0.3f);
    }

    private float bossScale()   { return worldHeight() * 0.46f / boss.getRegionHeight(); }
    private float bossWidth()   { return boss.getRegionWidth() * bossScale(); }
    private float bossHeight()  { return boss.getRegionHeight() * bossScale(); }
    private float bossBottom()  { return worldHeight() * 0.24f; }
    private float bossX()       { return worldWidth() / 2f; }
    private float bossY()       { return bossBottom() + bossHeight() / 2f; }
    private float symbolScale() { return symbol == null ? 1f : worldHeight() * 0.07f / symbol.getRegionHeight(); }
    private float coinScale()   { return worldHeight() * 0.035f / coin.getRegionHeight(); }
}
