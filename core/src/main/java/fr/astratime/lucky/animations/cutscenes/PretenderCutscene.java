package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.StoryPortraits;
import fr.astratime.lucky.assets.StoryPortraits.Prop;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Avant le Prétendant (chapitre 4) : gros plan sur une table renversée, une main
 * lâche un éclat qui roule dans la rue. Une ville en flammes, des joueurs qui se
 * battent pour des éclats dorés. Une ombre traverse la foule : les flammes se
 * penchent sur son passage, les jetons au sol fondent. Elle ramasse trois
 * éclats, un, deux, trois, et les pose sur sa couronne, qui flotte au-dessus
 * d'elle : chacun s'allume de sa couleur (Comète, Reine, Éclat). La couronne
 * se pose sur la tête du Prétendant. Il lève les yeux vers toi. Fondu noir.
 *
 * <p>La couronne et les trois éclats sont les vrais morceaux de son portrait
 * ({@link StoryPortraits#without}) : on le montre tête nue, sans ses éclats, et
 * chaque morceau rejoint sa place exacte ; quand la couronne s'y pose, il porte de
 * nouveau son portrait entier, pixel pour pixel, sans fondu ni second dessin.
 */
public class PretenderCutscene extends Cutscene {

    /** Gros plan sur la table renversée (voir le son cutscene/pretender) : une main lâche un éclat. */
    public static final float DROP         = 0.55f;
    public static final float TABLE_END    = 1.5f;
    /** L'ombre traverse la foule. */
    public static final float SHADOW_START = 3.3f;
    public static final float SHADOW_END   = 5.5f;
    /** Les trois éclats rejoignent l'ombre, un par un. */
    public static final float[] PICK       = {5.8f, 6.25f, 6.7f};
    /** Le Prétendant apparaît. */
    public static final float REVEAL       = 7.2f;
    /** La couronne descend et se pose sur sa tête. */
    public static final float CROWN_DOWN   = 7.8f;
    public static final float CROWN_LAND   = 8.7f;
    /** Il lève les yeux : on s'approche de son visage. */
    public static final float STARE        = 9.4f;
    public static final float COVER_START  = 10.1f;
    public static final float COVER_FULL   = 10.8f;

    /** Centre des yeux sur le portrait (34 x 42 pixels), depuis le milieu et le bas : ils ne sont pas symétriques. */
    private static final float[] EYES_X = {-3f, 4f};
    private static final float EYES_Y = 24.5f;

    /** Les couleurs des trois éclats : la Comète, la Reine, l'Éclat Originel. */
    private static final Color[] SHARDS = {hex("ffc93a"), hex("e0283a"), hex("fff6c8")};
    /** Ses trois éclats, dans l'ordre où l'ombre les ramasse : à gauche, à droite, sur la poitrine. */
    private static final Prop[] SHARD_PROPS = {Prop.PRETENDER_SHARD_LEFT, Prop.PRETENDER_SHARD_RIGHT,
        Prop.PRETENDER_SHARD_CHEST};
    /** Les éclats qui flottent avec la couronne jusqu'à ce qu'elle se pose (celui de la poitrine va droit à sa place). */
    private static final boolean[] RIDES_CROWN = {true, true, false};
    /** Où brillent les éclats dans la ville (fractions de l'écran). */
    private static final float[][] SHARD_AT = {{0.22f, 0.42f}, {0.78f, 0.47f}, {0.52f, 0.33f}};
    private static final Color FIRE = hex("ff7a1a"), GOLD = hex("ffc93a");

    private final TextureRegion city, pretender, bare, gem, hand;
    /** Les vrais morceaux de son portrait : sa couronne et ses trois éclats. */
    private final Piece crown;
    private final Piece[] shards = new Piece[3];
    private final Array<Particle> embers = new Array<>(false, 256);
    private final Array<Particle> sparks = new Array<>(false, 128);
    private float emberDebt;
    private int   picked;
    private boolean landed;

    public PretenderCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("pretender"));
        city = kit.chapterArt(Chapter.MONDE_SANS_MAITRE);
        pretender = kit.portrait(EnemyKind.PRETENDANT);
        int[][] full = StoryPortraits.pixels(EnemyKind.PRETENDANT);
        int[][] none = StoryPortraits.without(EnemyKind.PRETENDANT, Prop.PRETENDER_CROWN, Prop.PRETENDER_SHARD_LEFT,
            Prop.PRETENDER_SHARD_RIGHT, Prop.PRETENDER_SHARD_CHEST);
        bare = pixels(none);
        crown = lift(StoryPortraits.without(EnemyKind.PRETENDANT, SHARD_PROPS), none, full);
        for (int i = 0; i < shards.length; i++) {
            Prop[] others = new Prop[3];
            others[0] = Prop.PRETENDER_CROWN;
            for (int k = 0, n = 1; k < SHARD_PROPS.length; k++) if (k != i) others[n++] = SHARD_PROPS[k];
            shards[i] = lift(StoryPortraits.without(EnemyKind.PRETENDANT, others), none, full);
        }
        gem = art("o#w", new Color[] {hex("1a0a0e"), Color.WHITE, hex("ffffff")},
            "..o..",
            ".o#o.",
            "o###o",
            ".o#o.",
            "..o..");
        hand = art("osd", new Color[] {hex("1a0a0e"), hex("f0c8a0"), hex("b88a68")},
            "..oooooooo....",
            ".ossssssssoo..",
            "osssssssssssoo",
            "ossssssssssssso",
            "osssssssssssddo",
            ".osssssssdddoo.",
            "..ooooooooooo..");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        embers.clear();
        sparks.clear();
        emberDebt = 0f;
        picked = 0;
        landed = false;
    }

    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        emberDebt += 70f * delta;
        while (emberDebt >= 1f) {                         // braises qui montent des incendies
            emberDebt -= 1f;
            Particle ember = particle(width * random.nextFloat(), height * (0.05f + 0.5f * random.nextFloat()),
                80f + 20f * random.nextFloat(), 60f + 120f * random.nextFloat(), 1.2f + 1.5f * random.nextFloat(),
                5f + 8f * random.nextFloat());
            ember.color.set(random.nextFloat() < 0.7f ? FIRE : GOLD);
            ember.gravity = -20f;
            embers.add(ember);
        }
        if (!landed && time >= CROWN_LAND) {                 // la couronne se pose
            landed = true;
            rumble(0.2f, 6f);
            for (int i = 0; i < 24; i++) {
                Particle spark = particle(pieceX(crown, width / 2f, 10f), pieceY(crown, height * 0.12f, 10f),
                    360f * random.nextFloat(), 200f + 250f * random.nextFloat(), 0.5f, 12f);
                spark.drag = 0.1f;
                spark.color.set(GOLD);
                sparks.add(spark);
            }
        }
        while (picked < PICK.length && time >= PICK[picked]) {
            for (int i = 0; i < 18; i++) {
                Particle spark = particle(slotX(picked), slotY(picked), 360f * random.nextFloat(),
                    150f + 250f * random.nextFloat(), 0.5f, 12f);
                spark.drag = 0.1f;
                spark.color.set(SHARDS[picked]);
                sparks.add(spark);
            }
            picked++;
        }
        update(embers, delta);
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < TABLE_END) {
            drawTable(batch);
            fadeFromBlack(batch, 0.5f);
            fadeThroughBlack(batch, TABLE_END, 0.25f);
            return;
        }
        float width = worldWidth(), height = worldHeight();
        // La ville en flammes, qui tremble à la chaleur ; elle s'assombrit au passage de l'ombre.
        float zoom = Math.max(width / city.getRegionWidth(), height / city.getRegionHeight()) * 1.08f;
        float drift = MathUtils.lerp(-20f, 20f, progress(TABLE_END, COVER_FULL));
        float dark = 1f - 0.55f * progress(SHADOW_START, SHADOW_END) - 0.2f * progress(REVEAL, STARE);
        float flicker = 0.92f + 0.08f * MathUtils.sin(time * 23f) * MathUtils.sin(time * 7f);
        batch.setColor(dark * flicker, dark * 0.9f * flicker, dark * 0.85f * flicker, 1f);
        float artW = city.getRegionWidth() * zoom, artH = city.getRegionHeight() * zoom;
        batch.draw(city, (width - artW) / 2f + drift, (height - artH) / 2f, artW, artH);
        glow(batch, width / 2f, 0f, width * 1.4f, FIRE, 0.25f * flicker);
        drawParticles(batch, embers);

        // Ses trois éclats, que la foule se dispute, puis qui s'envolent vers leur place sur lui.
        for (int i = 0; i < SHARDS.length; i++) {
            float fly = progress(PICK[i] - 0.35f, PICK[i]);
            if (fly >= 1f) continue;
            float e = Interpolation.pow2In.apply(fly);
            float x = MathUtils.lerp(width * SHARD_AT[i][0], slotX(i), e);
            float y = MathUtils.lerp(height * SHARD_AT[i][1] + 8f * MathUtils.sin(time * 4f + i), slotY(i), e);
            float s = MathUtils.lerp(4f, 10f, e);
            glow(batch, x, y, s * 14f, SHARDS[i], 0.7f);
            batch.setColor(Color.WHITE);
            sprite(batch, shards[i].region, x, y, s, 0f);
        }

        // L'ombre traverse la foule, de gauche à droite : les flammes se penchent, les jetons fondent.
        float shadowX = MathUtils.lerp(-width * 0.2f, width * 1.2f,
            Interpolation.sine.apply(progress(SHADOW_START, SHADOW_END)));
        drawStreet(batch, shadowX);
        if (time >= SHADOW_START && time < SHADOW_END + 0.2f) {
            float x = shadowX;
            batch.setColor(0f, 0f, 0f, 0.45f);
            batch.draw(soft, x - width * 0.35f, -height * 0.2f, width * 0.7f, height * 1.4f);
            portrait(batch, bare, x, -40f, 16f, 1f, 1f);
            // Sa couronne flotte au-dessus de l'ombre, sans personne pour la porter.
            float bob = 8f * 16f + MathUtils.sin(time * 3f) * 16f;
            glow(batch, pieceX(crown, x, 16f), pieceY(crown, -40f, 16f) + bob, 18f * 16f, GOLD, 0.25f);
            batch.setColor(0.35f, 0.35f, 0.35f, 1f);
            drawPiece(batch, crown, x, -40f, 16f, bob);
        }

        // Le Prétendant, en ombre, puis éclairé par ses éclats ; on s'approche de son visage.
        if (time >= PICK[0] - 0.4f) {
            float appear = progress(PICK[0] - 0.4f, PICK[0]);
            float light = progress(REVEAL, REVEAL + 0.5f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            float cx = width / 2f, shadow = 1f - 0.9f * light;
            glow(batch, cx, baseY + 30f * scale, 30f * scale, GOLD, 0.25f * light);
            if (landed) {
                // La couronne posée : il porte de nouveau son portrait entier, aux mêmes pixels.
                portrait(batch, pretender, cx, baseY, scale, shadow, appear);
            } else {
                // Tête nue et sans ses éclats ; sa vraie couronne flotte au-dessus de lui, puis descend se poser.
                portrait(batch, bare, cx, baseY, scale, shadow, appear);
                float hover = hover() * scale;
                float crownLight = Math.max(0.35f, light);
                glow(batch, pieceX(crown, cx, scale), pieceY(crown, baseY, scale) + hover, 22f * scale, GOLD, 0.3f * appear);
                batch.setColor(crownLight, crownLight, crownLight, appear);
                drawPiece(batch, crown, cx, baseY, scale, hover);
                for (int i = 0; i < picked; i++) {        // les éclats ramassés, à leur place (ceux du haut, avec la couronne)
                    float rise = RIDES_CROWN[i] ? hover : 0f;
                    glow(batch, pieceX(shards[i], cx, scale), pieceY(shards[i], baseY, scale) + rise, 9f * scale, SHARDS[i],
                        0.35f * appear);
                    batch.setColor(crownLight, crownLight, crownLight, appear);
                    drawPiece(batch, shards[i], cx, baseY, scale, rise);
                }
            }
        }
        drawParticles(batch, sparks);
        // Le regard : deux lueurs dans les yeux.
        if (time >= STARE) {
            float stare = progress(STARE, STARE + 0.3f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            for (float eye : EYES_X) {
                glow(batch, width / 2f + eye * scale, baseY + EYES_Y * scale, 4f * scale, GOLD, 0.9f * stare);
            }
        }
        fadeThroughBlack(batch, TABLE_END, 0.25f);
    }

    /**
     * Gros plan sur une table de jeu renversée dans la rue en feu : une main
     * lâche un éclat doré, qui rebondit et roule hors de l'écran.
     */
    private void drawTable(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float zoom = Math.max(width / city.getRegionWidth(), height / city.getRegionHeight()) * 2.2f;
        float flicker = 0.92f + 0.08f * MathUtils.sin(time * 23f) * MathUtils.sin(time * 7f);
        batch.setColor(0.45f * flicker, 0.3f * flicker, 0.28f * flicker, 1f);      // la ville, floue derrière
        batch.draw(city, (width - city.getRegionWidth() * zoom) / 2f, -height * 0.4f,
            city.getRegionWidth() * zoom, city.getRegionHeight() * zoom);
        glow(batch, width * 0.5f, height * 0.2f, width * 1.4f, FIRE, 0.3f * flicker);
        float ground = height * 0.25f;
        batch.setColor(hex("1a0f0c"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, ground + MARGIN);
        batch.setColor(hex("2e1c14"));
        for (int i = 0; i < 9; i++) batch.draw(pixel, -MARGIN + i * width / 8f, ground - 8f, width / 8f - 10f, 6f);
        // La table, renversée sur le flanc : le plateau de feutre debout face à nous, les pieds de côté.
        float tx = width * 0.3f, ty = ground - 20f, tw = width * 0.36f, th = height * 0.42f;
        batch.setColor(hex("2a160a"));
        for (int k = 0; k < 2; k++) batch.draw(pixel, tx + tw - 20f, ty + th * (0.2f + 0.55f * k), width * 0.12f, 22f);
        batch.setColor(hex("3a2010"));
        batch.draw(pixel, tx, ty, tw / 2f, 0f, tw, th, 1f, 1f, 4f);
        batch.setColor(hex("1f7a3a"));
        batch.draw(pixel, tx + 16f, ty + 16f, tw / 2f - 16f, -16f, tw - 32f, th - 32f, 1f, 1f, 4f);
        batch.setColor(hex("f4ecdc"));                                          // les lignes du tapis de jeu
        batch.draw(pixel, tx + tw * 0.2f, ty + th * 0.5f, tw * 0.3f, -th * 0.5f, tw * 0.6f, 4f, 1f, 1f, 4f);
        batch.draw(pixel, tx + tw * 0.48f, ty + th * 0.2f, 2f, th * 0.3f, 4f, th * 0.6f, 1f, 1f, 4f);
        for (int k = 0; k < 5; k++) {                                          // des jetons renversés au sol
            Color color = k % 3 == 0 ? hex("c8283c") : k % 3 == 1 ? hex("2a5ad8") : hex("1f9a4a");
            float cx = width * (0.12f + k * 0.18f), cy = ground * (0.35f + 0.2f * (k % 2));
            batch.setColor(color);
            batch.draw(pixel, cx, cy, 70f, 22f);
            batch.setColor(1f, 1f, 1f, 0.8f);
            for (int st = 0; st < 3; st++) batch.draw(pixel, cx + 10f + st * 22f, cy + 3f, 7f, 16f);
        }
        drawParticles(batch, embers);
        // La main, qui dépasse du haut de la table : elle se crispe, puis s'ouvre.
        float scale = height / 70f;
        float hx = tx + tw * 0.6f, hy = ty + th + 3f * scale;
        float open = progress(DROP - 0.15f, DROP);
        batch.setColor(0.85f, 0.7f, 0.65f, 1f);
        sprite(batch, hand, hx, hy, scale, -25f - 40f * open);
        // L'éclat tombe devant la table, rebondit, roule vers la droite et sort.
        if (time >= DROP) {
            float t = time - DROP;
            float fall = Math.min(1f, t / 0.3f);
            float x = hx + 6f * scale + Math.max(0f, t - 0.3f) * width * 0.9f;
            float floor = ground * 0.5f;
            float y = t < 0.3f ? MathUtils.lerp(hy - 4f * scale, floor, fall * fall)
                : floor + Math.abs(MathUtils.sin((t - 0.3f) * 9f)) * 120f * Math.max(0f, 1f - (t - 0.3f) * 1.6f);
            drawGem(batch, x, y, GOLD, scale * 1.6f);
        } else {
            drawGem(batch, hx + 4f * scale, hy - 3f * scale, GOLD, scale * 1.6f);
        }
    }

    /** Les flammes de la rue et les jetons au sol : à son passage, les flammes se penchent et les jetons fondent. */
    private void drawStreet(Batch batch, float shadowX) {
        float width = worldWidth(), height = worldHeight();
        boolean passing = time >= SHADOW_START - 0.3f;
        for (int i = 0; i < 12; i++) {                                     // des flammes le long de la rue
            float x = width * (i + 0.5f) / 12f, base = height * 0.02f + (i % 3) * 8f;
            float dx = passing ? x - shadowX : 99999f;
            float lean = MathUtils.clamp(dx / (width * 0.25f), -1f, 1f) * Math.max(0f, 1f - Math.abs(dx) / (width * 0.5f));
            float flick = 0.8f + 0.2f * MathUtils.sin(time * 15f + i * 1.7f);
            for (int k = 0; k < 4; k++) {
                float y = base + k * 22f * flick, off = lean * k * 26f + MathUtils.sin(time * 9f + i + k) * 4f;
                glow(batch, x + off, y, (70f - k * 13f) * flick, k < 2 ? FIRE : GOLD, 0.55f - k * 0.08f);
            }
        }
        for (int i = 0; i < 9; i++) {                                      // les jetons au sol, qui fondent
            float x = width * (0.08f + i * 0.105f), y = height * 0.035f + (i % 2) * 10f;
            float melt = time < SHADOW_START ? 0f : MathUtils.clamp((shadowX - x) / (width * 0.12f), 0f, 1f);
            float w = 70f * (1f + 0.6f * melt), h = 22f * (1f - 0.65f * melt);
            Color color = i % 3 == 0 ? hex("c8283c") : i % 3 == 1 ? hex("2a5ad8") : hex("1f9a4a");
            float d = 1f - 0.5f * melt;
            batch.setColor(0f, 0f, 0f, 0.6f);
            batch.draw(pixel, x - w / 2f - 2f, y - 2f, w + 4f, h + 4f);
            batch.setColor(color.r * d, color.g * d, color.b * d, 1f);
            batch.draw(pixel, x - w / 2f, y, w, h);
            if (melt < 0.6f) {
                batch.setColor(1f, 1f, 1f, 0.8f * (1f - melt));
                for (int s = 0; s < 3; s++) batch.draw(pixel, x - w / 2f + (0.15f + s * 0.3f) * w, y + 2f, w * 0.1f, h - 4f);
            } else {
                glow(batch, x, y + h, w, FIRE, 0.4f * melt);
            }
        }
    }

    private void drawGem(Batch batch, float x, float y, Color color, float scale) {
        glow(batch, x, y, scale * 14f, color, 0.7f);
        batch.setColor(color);
        sprite(batch, gem, x, y, scale, 0f);
    }

    /** @return de combien de pixels du portrait la couronne flotte au-dessus de sa place (0 une fois posée). */
    private float hover() {
        float bob = MathUtils.sin(time * 3f) * (1f - progress(CROWN_DOWN - 0.3f, CROWN_DOWN));
        return (1f - Interpolation.bounceOut.apply(progress(CROWN_DOWN, CROWN_LAND))) * 12f + bob;
    }

    /** @return où l'éclat {@code i} rejoint sa place sur lui, (avant qu'on s'approche). */
    private float slotX(int i) { return pieceX(shards[i], worldWidth() / 2f, 10f); }
    private float slotY(int i) {
        return pieceY(shards[i], worldHeight() * 0.12f, 10f) + (RIDES_CROWN[i] ? hover() * 10f : 0f);
    }

    /** Un vrai morceau du portrait : ses pixels, et sa place (coin haut-gauche, en pixels du portrait). */
    private record Piece(TextureRegion region, int left, int top) {}

    /**
     * @return le morceau que {@code with} (le portrait {@code rest} plus ce seul morceau) a en plus de {@code rest},
     *         avec les couleurs du portrait entier {@code full}, à sa place exacte
     */
    private Piece lift(int[][] with, int[][] rest, int[][] full) {
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = -1, bottom = -1;
        for (int y = 0; y < with.length; y++) {
            for (int x = 0; x < with[y].length; x++) {
                if (with[y][x] == rest[y][x] || with[y][x] == 0) continue;
                left = Math.min(left, x); right = Math.max(right, x);
                top = Math.min(top, y); bottom = Math.max(bottom, y);
            }
        }
        int[][] grid = new int[bottom - top + 1][right - left + 1];
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) if (with[y][x] != rest[y][x]) grid[y - top][x - left] = full[y][x];
        }
        return new Piece(pixels(grid), left, top);
    }

    /** Dessine {@code piece} à sa place sur le portrait posé en ({@code cx}, {@code baseY}), monté de {@code dy}. */
    private void drawPiece(Batch batch, Piece piece, float cx, float baseY, float scale, float dy) {
        float w = piece.region.getRegionWidth() * scale, h = piece.region.getRegionHeight() * scale;
        float x = cx + (piece.left - pretender.getRegionWidth() / 2f) * scale;
        float y = baseY + (pretender.getRegionHeight() - piece.top) * scale - h + dy;
        batch.draw(piece.region, x, y, w, h);
    }

    /** @return le centre à l'écran de {@code piece} à sa place, le portrait posé en ({@code cx}, {@code baseY}). */
    private float pieceX(Piece piece, float cx, float scale) {
        return cx + (piece.left + piece.region.getRegionWidth() / 2f - pretender.getRegionWidth() / 2f) * scale;
    }
    private float pieceY(Piece piece, float baseY, float scale) {
        return baseY + (pretender.getRegionHeight() - piece.top - piece.region.getRegionHeight() / 2f) * scale;
    }
}
