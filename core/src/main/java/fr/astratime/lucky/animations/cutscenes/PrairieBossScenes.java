package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.ExplorationArt;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois de la prairie (choix d'Astra, 2026-10-24 ; refaites le
 * 2026-11-22 pour montrer ce que dit leur phrase). Elles se jouent devant le vrai décor du donjon : le château
 * aux bannières de la couleur du roi, les fanions, la clôture et l'arbre.
 */
final class PrairieBossScenes {

    private PrairieBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case ROI_PIQUE -> new SpadePhase(kit);
            case ROI_TREFLE -> new ClubPhase(kit);
            case ROI_COEUR -> new HeartPhase(kit);
            case ROI_CARREAU -> new DiamondPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case ROI_PIQUE -> new SpadeDeath(kit);
            case ROI_TREFLE -> new ClubDeath(kit);
            case ROI_COEUR -> new HeartDeath(kit);
            case ROI_CARREAU -> new DiamondDeath(kit);
            default -> null;
        };
    }

    // ----- Le décor (pixels de front/decor_prairie_*.png) -----

    /** Le centre des deux grandes bannières du château, frappées du symbole du roi. */
    static final float[][] BANNERS = {{46.5f, 87f}, {92.5f, 87f}};
    /** Le centre du symbole des deux fanions du pré. */
    static final float[][] PENNANTS = {{203.5f, 102.5f}, {267.5f, 102.5f}};

    // ----- Accessoires -----

    static final Color[] METAL = {hex("140a0a"), hex("e8e8f0"), hex("9a9ab0"), hex("ffc93a"), hex("6b3a1e")};
    /** La même lame, gorgée de sang. */
    static final Color[] BLOODY = {hex("140a0a"), hex("ff8a9a"), hex("c8102e"), hex("ffc93a"), hex("6b3a1e")};
    /** Une lame (pointe en bas, garde dorée en haut) : la lame va des lignes 5 à 13. */
    static final String[] BLADE = {
        ".ggg.", ".gog.", "..b..", "..b..", "ggggg", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.",
        ".sws.", "..s.."};
    /** Un cœur (sert aussi de feuille de trèfle, en vert). */
    static final String[] HEART = {
        ".ooo...ooo.", "orrro.orrro", "orwrrorrrro", "orwrrrrrrro", "orrrrrrrrdo", ".orrrrrrdo.", "..orrrrdo..",
        "...orrdo...", "....odo....", ".....o....."};
    static final String[] STAINED_GLASS = {
        ".ooo...ooo.", "oyrro.orrpo", "orrpoooprro", "oprroyorrdo", "ooooooooooo", ".oyrrorpdo.", "..orrordo..",
        "...ooyoo...", "....odo....", ".....o....."};
    /** Le cœur frappé sur le plastron du Roi de Coeur (ExplorationArt.symbol, posé en 14, 32 du portrait). */
    static final String[] CHEST_HEART = {".XX.XX.", "XXXXXXX", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."};
    static final String[] BOTTLE = {
        "..oo..", "..gg..", "..gg..", ".gggg.", "gggggg", "gwgggg", "gwgggg", "gggggg", "gggggg", "oooooo"};
    static final String[] CUP = {
        "ooooooo", "oyyyyyo", "oywyyyo", ".oyyyo.", "..oyo..", "...o...", "..ooo..", ".ooooo."};
    static final String[] BOOK = {
        "oooooooooooo", "oyyyyyoyyyyo", "oywwwyowwwyo", "oyyyyyoyyyyo", "oywwwyowwwyo", "oyyyyyoyyyyo", "oywwwyowwwyo",
        "oyyyyyoyyyyo", "oooooooooooo"};
    static final String[] GEM = {"..o..", ".owo.", "owggo", ".ogo.", "..o.."};

    /** Une pile de pièces de {@code count} pièces, posée en ({@code x}, {@code y}). */
    static void stack(BossScene scene, Batch batch, TextureRegion coin, float x, float y, int count, float size) {
        float scale = size / coin.getRegionWidth(), step = size * 0.28f;
        for (int i = 0; i < count; i++) scene.draw(batch, coin, x, y + i * step, scale, 0f, BossScene.WHITE, 1f);
    }

    /** @return le portrait du roi de {@code suit}, sans les accessoires {@code without}. */
    static int[][] king(Card.Suit suit, ExplorationArt.KingProp... without) { return ExplorationArt.king(suit, without); }

    // -------------------------------------------------------------------------
    // Roi de Pique
    // -------------------------------------------------------------------------

    /**
     * « Lames assoiffées : Croc ! » : le cercle de lames se dresse dans l'herbe, boit le sang qui monte du pré
     * (elles rougissent de la pointe à la garde), puis s'envole en deux rangées de crocs qui mordent l'écran.
     */
    static final class SpadePhase extends BossScene {
        private static final Color RED = hex("ff2a3a");
        private static final int COUNT = 10;
        private static final float RISE = 0.15f, THIRST = 0.75f, FULL = 1.6f, FLY = 1.5f, JAW = 1.95f, REOPEN = 2.4f;
        private final TextureRegion blade;
        private final TextureRegion[] blood = new TextureRegion[10];

        SpadePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_PIQUE, 2);
            blade = art("owsgb", METAL, BLADE);
            TextureRegion red = art("owsgb", BLOODY, BLADE);
            for (int n = 1; n <= 9; n++) blood[n] = new TextureRegion(red, 0, 14 - n, 5, n);
        }

        private float ringAngle(int i) { return 18f + i * 36f; }
        private boolean behind(int i)  { return MathUtils.sinDeg(ringAngle(i)) > 0f; }
        private float ringX(int i)     { return bossX() + MathUtils.cosDeg(ringAngle(i)) * worldWidth() * 0.37f; }
        /** @return où la pointe de la lame est plantée. */
        private float ringY(int i)     { return groundY() + 40f + MathUtils.sinDeg(ringAngle(i)) * worldHeight() * 0.06f; }
        private float small()          { return fit(worldHeight() * 0.2f, 14); }
        private float big()            { return fit(worldHeight() * 0.36f, 14); }
        private boolean upper(int i)   { return i % 2 == 0; }
        private float jawX(int i)      { return worldWidth() * ((upper(i) ? 0.1f : 0.19f) + (i / 2) * 0.18f); }
        private float fill(int i)      { return progress(THIRST + i * 0.05f, FULL + i * 0.03f); }

        /** @return le centre de la lame {@code i}, sa taille et son angle : {x, y, scale, rotation}. */
        private float[] place(int i) {
            float small = small(), smallHalf = 7f * small, bigHalf = 7f * big(), mid = worldHeight() * 0.5f;
            float rise = Interpolation.pow2Out.apply(progress(RISE + i * 0.04f, RISE + 0.5f + i * 0.04f));
            float thirst = progress(THIRST, FLY);
            float x = ringX(i) + MathUtils.sin(time * 45f + i * 2f) * 4f * thirst;
            float y = ringY(i) + smallHalf - 24f - (1f - rise) * 2f * smallHalf;
            float fly = Interpolation.pow2.apply(progress(FLY + i * 0.02f, JAW - 0.05f));
            if (fly <= 0f) return new float[] {x, y, small, 0f};
            // les crocs : en haut, pointe en bas ; en bas, pointe en haut
            float open = upper(i) ? worldHeight() * 0.8f + bigHalf : worldHeight() * 0.2f - bigHalf;
            float shut = upper(i) ? mid - 80f + bigHalf : mid + 80f - bigHalf;
            float rest = upper(i) ? worldHeight() * 0.86f + bigHalf : -bigHalf - 60f;
            float bite = Interpolation.pow3In.apply(progress(JAW, CLIMAX_PHASE));
            float back = Interpolation.pow2Out.apply(progress(REOPEN, REOPEN + 0.7f));
            float jawY = time < CLIMAX_PHASE ? MathUtils.lerp(open, shut, bite) : MathUtils.lerp(shut, rest, back);
            return new float[] {MathUtils.lerp(x, jawX(i), fly), MathUtils.lerp(y, jawY, fly), MathUtils.lerp(small, big(), fly),
                upper(i) ? 0f : 180f * fly};
        }

        @Override
        protected void step(float delta) {
            if (time > THIRST && time < FLY && random.nextFloat() < 0.8f) {   // le sang monte du pré dans les lames
                int i = random.nextInt(COUNT);
                Particle drop = particle(ringX(i) + (random.nextFloat() - 0.5f) * 30f, ringY(i) - 10f, 90f,
                    200f + 120f * random.nextFloat(), 0.45f, 16f);
                drop.color.set(RED);
                glows.add(drop);
            }
            for (int i = 0; i < COUNT; i++) if (at(RISE + 0.5f + i * 0.04f)) sparks(ringX(i), ringY(i), 6, hex("a08060"), 200f, 10f);
            if (at(CLIMAX_PHASE)) {                                     // les crocs se referment
                rumble(0.7f, 14f);
                sparks(worldWidth() / 2f, worldHeight() / 2f, 90, RED, 1100f, 24f);
            }
            if (time > REOPEN + 0.3f && random.nextFloat() < 0.5f) {    // le sang goutte des crocs du haut
                int i = 2 * random.nextInt(COUNT / 2);
                float[] p = place(i);
                Particle drop = particle(p[0], p[1] - 7f * p[2], 270f, 40f, 0.9f, 14f);
                drop.gravity = 1400f;
                drop.color.set(RED);
                glows.add(drop);
            }
        }

        private void drawBlade(Batch batch, int i) {
            float[] p = place(i);
            float red = fill(i);
            draw(batch, blade, p[0], p[1], p[2], p[3], WHITE, 1f);
            int rows = Math.round(9f * red);
            if (rows > 0) {                                             // la lame se remplit de sang, de la pointe vers la garde
                float dy = -((28 - rows) / 2f - 7f) * p[2];
                draw(batch, blood[rows], p[0] - dy * MathUtils.sinDeg(p[3]), p[1] + dy * MathUtils.cosDeg(p[3]), p[2], p[3], WHITE, 1f);
            }
            if (red > 0f) glow(batch, p[0], p[1], 5f * p[2] * 4f, RED, 0.25f * red);
        }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < COUNT; i++) if (behind(i) && time < FLY) drawBlade(batch, i);
            poseTintColor.set(RED);
            poseTint = 0.3f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
            poseY = 10f * gesture(JAW, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(JAW, CLIMAX_PHASE), RED, new float[] {14f, 14.5f}, new float[] {21f, 14.5f});
            for (int i = 0; i < COUNT; i++) if (!behind(i) || time >= FLY) drawBlade(batch, i);
            wave(batch, CLIMAX_PHASE, 0.8f, worldWidth() / 2f, worldHeight() / 2f, RED, worldWidth() * 1.3f);
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /**
     * « Le Roi de Pique brise son épée » : son épée (le sceptre à pique de son portrait, celle qu'il fait tournoyer
     * à son entrée) se fend au milieu, puis casse net : la vraie moitié haute vole et se plante dans l'herbe, il
     * garde le tronçon en main ; les lames plantées autour de lui tombent l'une après l'autre ; il pose un genou.
     */
    static final class SpadeDeath extends BossScene {
        /** La ligne du portrait où l'épée casse. */
        private static final int BREAK = 21;
        private static final float[] CRACKS = {1.1f, 1.6f, 2.05f};
        private static final float GRAVITY = 2600f, LIFT = 1150f, DRIFT = 420f;
        private final TextureRegion blade, broken, shard;
        private final Piece top;
        private boolean snapped, landed;
        private float topX, topY, landT;

        SpadeDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_PIQUE, 0);
            blade = art("owsgb", METAL, BLADE);
            shard = art("yd", new Color[] {hex("ffd54a"), hex("c4891e")}, "yy", "yd");
            int[][] full = king(Card.Suit.PIQUE), rest = king(Card.Suit.PIQUE, ExplorationArt.KingProp.SCEPTER);
            int[][] stump = new int[full.length][];
            for (int y = 0; y < full.length; y++) stump[y] = (y <= BREAK ? rest[y] : full[y]).clone();
            top = lift(full, stump);
            broken = pixels(stump);
        }

        @Override protected void restart() { snapped = false; landed = false; }

        private float breakX() { return portraitX(30f); }
        private float breakY() { return portraitY(BREAK + 1f); }
        private float landY()  { return groundY() + top.height * bossScale() * 0.32f; }

        private int bladeCount()      { return 9; }
        private float bladeAngle(int i) { return 200f + i * 40f; }
        private float bladeX(int i)   { return bossX() + MathUtils.cosDeg(bladeAngle(i)) * worldWidth() * 0.38f; }
        private float bladeY(int i)   { return groundY() + 40f + MathUtils.sinDeg(bladeAngle(i)) * worldHeight() * 0.06f; }

        @Override
        protected void step(float delta) {
            for (float c : CRACKS) if (at(c)) { rumble(0.15f, 4f); sparks(breakX(), breakY(), 12, HOT, 300f, 10f); }
            if (at(CLIMAX_DEATH)) {                                     // l'épée casse net
                rumble(0.7f, 13f);
                sparks(breakX(), breakY(), 90, HOT, 1000f, 22f);
                spray(shard, breakX(), breakY(), 16, 0f, 180f, 900f, bossScale() * 0.9f, 2200f, 1.4f);
            }
            if (snapped && !landed && time >= CLIMAX_DEATH + landT) {   // la moitié se plante dans l'herbe
                landed = true;
                rumble(0.25f, 7f);
                sparks(topX + DRIFT * landT, groundY() + 10f, 30, hex("a0b070"), 400f, 14f);
            }
            for (int i = 0; i < bladeCount(); i++) if (at(fallAt(i) + 0.3f)) rumble(0.08f, 3f);
        }

        private float fallAt(int i) {
            int order = bladeX(i) > bossX() ? i : bladeCount() - 1 - i;
            return CLIMAX_DEATH + 0.5f + order * 0.11f;
        }

        /** Une lame plantée, pointe dans l'herbe, qui bascule de {@code fall} (0 à 1) vers l'extérieur. */
        private void drawBlade(Batch batch, int i) {
            float scale = fit(worldHeight() * 0.2f, 14), half = 7f * scale;
            float fall = Interpolation.bounceOut.apply(progress(fallAt(i), fallAt(i) + 0.35f));
            float rot = (bladeX(i) > bossX() ? -84f : 84f) * fall;
            float tx = bladeX(i), ty = bladeY(i) - 20f;
            float light = MathUtils.sinDeg(bladeAngle(i)) > 0f ? 0.75f : 1f;
            draw(batch, blade, tx - half * MathUtils.sinDeg(rot), ty + half * MathUtils.cosDeg(rot), scale, rot,
                tmp.set(light, light, light, 1f), 1f);
        }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < bladeCount(); i++) if (MathUtils.sinDeg(bladeAngle(i)) > 0f) drawBlade(batch, i);
            float kneel = Interpolation.pow2Out.apply(progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 0.8f));
            poseStretch = -0.2f * kneel;                                // un genou à terre
            poseY = -20f * kneel;
            poseTilt = 6f * kneel;
            poseShadow = 0.5f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
            poseX = (random.nextFloat() - 0.5f) * 6f * progress(CRACKS[0], CLIMAX_DEATH) * (time < CLIMAX_DEATH ? 1f : 0f);
            if (time >= CLIMAX_DEATH && !snapped && top != null) {      // la vraie moitié de l'épée se détache
                snapped = true;
                topX = pieceX(top);
                topY = pieceY(top);
                float drop = topY - landY();
                landT = (LIFT + (float) Math.sqrt(LIFT * LIFT + 2f * GRAVITY * drop)) / GRAVITY;
                wear(broken);
            }
        }

        @Override
        protected void front(Batch batch) {
            for (int i = 0; i < bladeCount(); i++) if (MathUtils.sinDeg(bladeAngle(i)) <= 0f) drawBlade(batch, i);
            if (!snapped) {
                float g = progress(0.3f, 0.9f);                         // un éclat court le long de la lame
                if (g > 0f && g < 1f) glow(batch, portraitX(30f), portraitY(MathUtils.lerp(40f, 7f, g)), 140f, WHITE, 0.8f);
                float crack = progress(CRACKS[0], CLIMAX_DEATH);        // elle se fend au milieu, chauffée à blanc
                if (crack > 0f) {
                    glow(batch, breakX(), breakY(), 80f + 160f * crack, HOT, 0.3f + 0.6f * crack * pulse(30f, 0.6f));
                    additive(batch);
                    crack(batch, breakX(), breakY(), 90f, 70f * crack, 3L, WHITE, 0.9f, 4f);
                    crack(batch, breakX(), breakY(), 270f, 70f * crack, 7L, WHITE, 0.9f, 4f);
                    line(batch, WHITE, crack, breakX() - 16f, breakY() - 4f, breakX() + 16f, breakY() + 4f, 5f);
                    normal(batch);
                }
            } else if (top != null) {                                   // la moitié haute vole et se plante dans l'herbe
                float t = Math.min(time - CLIMAX_DEATH, landT);
                float x = topX + DRIFT * t, y = Math.max(landY(), topY + LIFT * t - 0.5f * GRAVITY * t * t);
                float rot = -375f * Interpolation.pow2Out.apply(t / landT);
                draw(batch, top.region, x, y, bossScale(), rot, WHITE, 1f);
                if (t < landT) glow(batch, x, y, 200f, GOLD, 0.3f);
            }
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.6f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi de Trèfle
    // -------------------------------------------------------------------------

    /** Le dessus de la table ronde du Roi de Trèfle (vu de biais) : feutre vert, bord de bois, et l'épaisseur du plateau. */
    static int[][] roundTable() {
        int w = 64, h = 24, felt = Color.rgba8888(hex("1e7a3e")), feltDark = Color.rgba8888(hex("16602e"));
        int wood = Color.rgba8888(hex("8a5428")), woodDark = Color.rgba8888(hex("5a3216")), line = Color.rgba8888(hex("1a0e06"));
        int[][] grid = new int[h][w];
        for (int pass = 0; pass < 2; pass++) {
            float cy = pass == 0 ? 14.5f : 8.5f;                       // l'épaisseur du plateau, puis le dessus
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    float dx = (x + 0.5f - 32f) / 32f, dy = (y + 0.5f - cy) / 8.5f, d = dx * dx + dy * dy;
                    if (d > 1f) continue;
                    if (pass == 0) grid[y][x] = d > 0.86f ? line : woodDark;
                    else grid[y][x] = d > 0.9f ? line : d > 0.66f ? wood : (x + y) % 7 == 0 ? feltDark : felt;
                }
            }
        }
        return grid;
    }

    /**
     * « La table tourne : Ivresse ! » : sa table de jeu ronde se met à tourner de plus en plus vite (cartes,
     * pièces et bouteille font la ronde dessus), le monde tangue avec elle, puis tout chavire et voit double.
     */
    static final class ClubPhase extends BossScene {
        private static final Color GREEN = hex("3fbf5a");
        private static final float SWAY = 0.9f;
        private final TextureRegion table, bottle, ace, king;
        private float spin, speed;

        ClubPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_TREFLE, 2);
            table = pixels(roundTable());
            bottle = art("ogw", new Color[] {hex("0a1a0c"), hex("2a8a3a"), hex("bfffc8")}, BOTTLE);
            ace = gameCard("cards/light/1-C.png");
            king = gameCard("cards/light/13-C.png");
        }

        @Override protected void restart() { spin = 0f; speed = 0f; }

        private float tableScale() { return bossWidth() * 1.9f / 64f; }
        private float tableX()     { return bossX(); }
        private float tableY()     { return groundY() + 8.5f * tableScale() - 30f; }   // le centre du dessus
        private float rx()         { return 32f * tableScale(); }
        private float ry()         { return 8.5f * tableScale(); }
        private float sway() {
            float k = progress(SWAY, CLIMAX_PHASE);
            return (5f * k + 4f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f)) * MathUtils.sin(time * 2.4f);
        }

        @Override
        protected void step(float delta) {
            speed = time < CLIMAX_PHASE ? MathUtils.lerp(25f, 900f, Interpolation.pow2In.apply(progress(0.3f, CLIMAX_PHASE)))
                : MathUtils.lerp(900f, 260f, progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f));
            spin += speed * delta;
            if (at(CLIMAX_PHASE)) {                                     // tout chavire : les pièces volent de la table
                rumble(0.8f, 12f);
                sparks(tableX(), tableY(), 60, GREEN, 800f, 20f);
                for (int i = 0; i < 12; i++) {
                    float a = spin + i * 30f;
                    Particle c = place(coin, tableX() + MathUtils.cosDeg(a) * rx() * 0.7f, tableY() + MathUtils.sinDeg(a) * ry() * 0.7f,
                        -MathUtils.sinDeg(a) * 900f, 300f + 300f * random.nextFloat(), coinScale(0.05f), 1.6f);
                    c.gravity = 1600f;
                    c.spin = 600f;
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            float sway = sway(), zoom = progress(0.5f, SWAY);
            TextureRegion decor = decorImage();
            if (decor != null && zoom > 0f) {                           // le pré tangue
                float s = decorScale() * (1f + 0.22f * zoom), w = decor.getRegionWidth() * s, h = decor.getRegionHeight() * s;
                float x = worldWidth() / 2f - w / 2f, y = worldHeight() + MARGIN - h + (h - decor.getRegionHeight() * decorScale()) / 2f;
                beginTilt(batch, sway);
                batch.setColor(litLikeDecor(WHITE, tmp));
                batch.draw(decor, x, y, w, h);
                if (time > CLIMAX_PHASE) {                              // il voit double
                    float ghost = 40f * MathUtils.sin(time * 3f) * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
                    batch.setColor(tmp.r, tmp.g, tmp.b, 0.3f);
                    batch.draw(decor, x + ghost, y + ghost * 0.3f, w, h);
                }
                endTilt(batch);
            }
            beginTilt(batch, sway * 0.6f);                              // lui et sa table tanguent aussi (jusqu'à la fin de front)
            poseTilt = 1.5f * sway;
            poseX = 6f * sway;
            poseTintColor.set(GREEN);
            poseTint = 0.2f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        /** Les objets qui font la ronde sur la table : {angle de départ, sorte}. */
        private static final float[][] ITEMS = {{0f, 0f}, {72f, 1f}, {144f, 2f}, {216f, 3f}, {288f, 4f}};

        private void drawItem(Batch batch, int kind, float x, float y, float alpha) {
            switch (kind) {
                case 0 -> draw(batch, ace, x, y + 1.6f * tableScale() * 1.5f, tableScale() * 0.33f, 0f, WHITE, alpha);
                case 1 -> { if (alpha >= 1f) stack(this, batch, coin, x, y, 4, worldHeight() * 0.05f); }
                case 2 -> draw(batch, bottle, x, y + 5f * tableScale() * 0.75f, tableScale() * 0.75f, 0f, WHITE, alpha);
                case 3 -> draw(batch, king, x, y + 1.6f * tableScale() * 1.5f, tableScale() * 0.33f, 0f, WHITE, alpha);
                default -> { if (alpha >= 1f) stack(this, batch, coin, x, y, 2, worldHeight() * 0.05f); }
            }
        }

        @Override
        protected void front(Batch batch) {
            float s = tableScale(), cx = tableX(), cy = tableY();
            rect(batch, hex("3a2010"), 1f, cx - 3f * s, groundY() - 20f, 6f * s, cy - groundY() - 4f * s);   // le pied
            draw(batch, table, cx, cy - 3f * s, s, 0f, WHITE, 1f);
            for (int k = 0; k < 6; k++) {                               // les rayons du feutre qui tournent
                float a = spin + k * 60f;
                line(batch, hex("0e4a22"), 0.8f, cx, cy, cx + MathUtils.cosDeg(a) * rx() * 0.62f, cy + MathUtils.sinDeg(a) * ry() * 0.62f, 5f);
            }
            for (int k = 0; k < 12; k++) {                              // les clous dorés du bord
                float a = spin + k * 30f;
                glow(batch, cx + MathUtils.cosDeg(a) * rx() * 0.83f, cy + MathUtils.sinDeg(a) * ry() * 0.83f, 26f, GOLD, 0.9f);
            }
            // les objets, du fond vers le devant ; à toute vitesse, ils laissent une traînée
            int[] order = {0, 1, 2, 3, 4};
            float[] ys = new float[order.length];
            for (int i = 0; i < order.length; i++) ys[i] = MathUtils.sinDeg(spin + ITEMS[i][0]);
            for (int i = 1; i < order.length; i++) {
                for (int j = i; j > 0 && ys[order[j]] > ys[order[j - 1]]; j--) { int t = order[j]; order[j] = order[j - 1]; order[j - 1] = t; }
            }
            float trail = MathUtils.clamp((speed - 300f) / 600f, 0f, 1f);
            for (int i : order) {
                if (time > CLIMAX_PHASE && (i == 1 || i == 4)) continue; // les pièces ont volé
                for (int g = 2; g >= 0; g--) {
                    if (g > 0 && trail <= 0f) continue;
                    float a = spin + ITEMS[i][0] - g * 14f;
                    drawItem(batch, (int) ITEMS[i][1], cx + MathUtils.cosDeg(a) * rx() * 0.55f, cy + MathUtils.sinDeg(a) * ry() * 0.55f,
                        g == 0 ? 1f : 0.3f * trail / g);
                }
            }
            endTilt(batch);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), GREEN, worldWidth() * 1.3f);
            flash(batch, GREEN, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /**
     * « Le Roi de Trèfle a épuisé sa chance » : au-dessus de sa tête, son trèfle à quatre feuilles brunit et perd
     * ses feuilles une à une ; le vent emporte la dernière ; un petit nuage noir vient pleuvoir sur lui seul.
     */
    static final class ClubDeath extends BossScene {
        private static final float[] DROPS = {0.7f, 1.3f, 1.9f};
        private static final float CLOUD = CLIMAX_DEATH + 0.2f;
        private static final Color LEAF_GREEN = hex("3fbf5a"), BROWN = hex("8a6a3a");
        private final TextureRegion leaf, dead, stem;

        ClubDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_TREFLE, 0);
            leaf = art("orwd", new Color[] {hex("0a1a0c"), hex("3fbf5a"), hex("9af0a8"), hex("1f7a34")}, HEART);
            dead = art("orwd", new Color[] {hex("1a1008"), hex("8a6a3a"), hex("b8985a"), hex("5a4020")}, HEART);
            stem = art("g", new Color[] {hex("2f9a46")}, "g", "g", "g", "g", "g", "g", "g", "g");
        }

        private float cloverX() { return bossX(); }
        private float cloverY() { return worldHeight() * 0.84f; }
        private float leafScale() { return fit(110f, 10); }
        private float leafAngle(int i) { return 45f + i * 90f; }
        private int left() {
            int n = 4;
            for (float d : DROPS) if (time >= d) n--;
            return time >= CLIMAX_DEATH ? 0 : n;
        }
        private float cloudX() { return MathUtils.lerp(-400f, bossX(), Interpolation.pow2Out.apply(progress(CLOUD, CLOUD + 0.8f))); }
        private float cloudY() { return worldHeight() * 0.86f; }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < DROPS.length; i++) {
                if (!at(DROPS[i])) continue;                            // une feuille morte se détache
                float a = leafAngle(i);
                Particle piece = place(dead, cloverX() + MathUtils.cosDeg(a) * 60f, cloverY() + MathUtils.sinDeg(a) * 60f,
                    (random.nextFloat() - 0.5f) * 120f, 40f, leafScale(), 2.6f);
                piece.gravity = 240f;
                piece.rotation = a - 90f;
                piece.spin = (random.nextFloat() - 0.5f) * 260f;
                piece.drag = 0.5f;
                rumble(0.1f, 3f);
                sparks(piece.x, piece.y, 8, BROWN, 200f, 8f);
            }
            if (at(CLIMAX_DEATH)) {                                     // la tige tombe, le vent emporte la dernière feuille
                rumble(0.4f, 7f);
                Particle s = place(stem, cloverX(), cloverY() - 40f, -40f, 0f, fit(80f, 8), 2.2f);
                s.gravity = 500f;
                s.spin = 160f;
            }
            if (left() > 0 && random.nextFloat() < 0.15f * left()) {    // ce qu'il lui reste de chance : des paillettes d'or
                Particle glint = particle(cloverX() + (random.nextFloat() - 0.5f) * 220f, cloverY() + (random.nextFloat() - 0.5f) * 200f,
                    90f, 30f, 0.8f, 18f);
                glint.color.set(GOLD);
                glows.add(glint);
            }
            if (time > CLOUD + 0.6f && random.nextFloat() < 0.5f) {    // la pluie éclabousse sa couronne et ses épaules
                Particle splash = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 0.8f,
                    portraitY(random.nextBoolean() ? 3f : 27f), 90f + (random.nextFloat() - 0.5f) * 120f, 120f, 0.3f, 10f);
                splash.color.set(hex("a8c8ff"));
                glows.add(splash);
            }
        }

        @Override
        protected void back(Batch batch) {
            float wet = progress(CLOUD + 0.5f, CLOUD + 1.5f);
            poseShadow = 0.3f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f) + 0.15f * wet;
            poseY = -14f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.8f);
            poseTilt = -3f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.8f);
            poseTintColor.set(0.4f, 0.55f, 0.9f, 1f);
            poseTint = 0.15f * wet;
        }

        @Override
        protected void front(Batch batch) {
            float x = cloverX(), y = cloverY(), luck = left() / 4f;
            glow(batch, x, y, 420f, GOLD, 0.35f * luck);
            if (time < CLIMAX_DEATH) line(batch, hex("2f9a46"), 1f, x, y, x + 6f, y - 80f, 12f);
            for (int i = 0; i < 4; i++) {
                if (i < DROPS.length && time >= DROPS[i]) continue;
                float a = leafAngle(i), lx = x + MathUtils.cosDeg(a) * 60f, ly = y + MathUtils.sinDeg(a) * 60f, rot = a - 90f;
                float brown = i < DROPS.length ? progress(DROPS[i] - 0.45f, DROPS[i]) : progress(CLIMAX_DEATH - 0.4f, CLIMAX_DEATH);
                if (i == 3 && time >= CLIMAX_DEATH) {                   // la dernière : le vent l'emporte
                    float k = progress(CLIMAX_DEATH, CLIMAX_DEATH + 2.0f);
                    lx += worldWidth() * 0.8f * Interpolation.pow2In.apply(k);
                    ly += worldHeight() * 0.15f * k + 40f * MathUtils.sin(k * 14f);
                    rot += 900f * k;
                }
                draw(batch, leaf, lx, ly, leafScale(), rot, WHITE, 1f - brown);
                draw(batch, dead, lx, ly, leafScale(), rot, WHITE, brown);
            }
            // le petit nuage noir, qui ne pleut que sur lui
            float cloud = progress(CLOUD, CLOUD + 0.4f);
            if (cloud > 0f) {
                float cx = cloudX(), cy = cloudY(), rain = progress(CLOUD + 0.5f, CLOUD + 0.8f);
                if (rain > 0f) {
                    float topY = cy - 40f, bottom = groundY(), span = topY - bottom;
                    batch.setColor(0.65f, 0.75f, 0.95f, 0.7f * rain);
                    for (int i = 0; i < 46; i++) {
                        float rx = cx - 190f + (i * 83.7f % 380f);
                        float ry = topY - ((i * 57.3f + time * 1500f) % span);
                        batch.draw(pixel, rx, ry - 40f, 4f, 40f);
                    }
                }
                for (int k = 0; k < 7; k++) {
                    float px = cx + (k - 3f) * 70f, py = cy + (k % 2 == 0 ? 0f : 40f) - Math.abs(k - 3f) * 12f, size = 260f - Math.abs(k - 3f) * 30f;
                    batch.setColor(0.16f, 0.17f, 0.22f, 0.95f * cloud);
                    batch.draw(soft, px - size / 2f, py - size * 0.35f, size, size * 0.7f);
                }
            }
        }

        @Override
        protected float decorLight() { return 0.62f - 0.22f * progress(CLOUD, CLOUD + 1f); }
    }

    // -------------------------------------------------------------------------
    // Roi de Coeur
    // -------------------------------------------------------------------------

    /** Son vitrail se fissure en rouge et bat plus vite ; les bannières du château battent avec lui ; il écrase sa coupe ; une brume rouge monte. */
    static final class HeartPhase extends BossScene {
        private static final Color RED = hex("e0283a");
        private final TextureRegion glass, cup;
        private float beat, nextBeat;

        HeartPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_COEUR, 2);
            glass = art("orpyd", new Color[] {hex("140a10"), hex("e0283a"), hex("ff8a9a"), hex("ffc93a"), hex("8a1020")}, STAINED_GLASS);
            cup = art("oyw", new Color[] {hex("4a3008"), hex("ffc93a"), hex("fff0a0")}, CUP);
        }

        @Override protected void restart() { beat = 0f; nextBeat = 0.2f; }

        @Override
        protected void step(float delta) {
            beat = Math.max(0f, beat - delta * 4f);
            if (time >= nextBeat) {                                     // le cœur bat de plus en plus vite
                beat = 1f;
                nextBeat += MathUtils.lerp(0.7f, 0.18f, progress(0f, CLIMAX_PHASE));
            }
            if (at(CLIMAX_PHASE)) {                                     // il écrase sa coupe
                rumble(0.6f, 12f);
                sparks(cupX(), cupY(), 60, RED, 700f, 20f);
                spray(cup, cupX(), cupY(), 1, 60f, 120f, 300f, fit(70f, 8), 1600f, 1.2f);
            }
            if (time > CLIMAX_PHASE && random.nextFloat() < 0.6f) {     // la brume rouge
                Particle mist = particle(worldWidth() * random.nextFloat(), groundY(), 90f, 60f + 60f * random.nextFloat(),
                    2.2f, 200f + 160f * random.nextFloat());
                mist.color.set(RED.r, RED.g, RED.b, 0.25f);
                glows.add(mist);
            }
        }

        private float cupX() { return bossX() + bossWidth() * 0.5f; }
        private float cupY() { return bossBottom() + bossHeight() * 0.5f; }

        @Override
        protected void back(Batch batch) {
            for (float[] b : BANNERS) glow(batch, decorX(b[0]), decorY(b[1]), 150f * (1f + 0.3f * beat), RED, 0.2f + 0.6f * beat);
            float x = bossX(), y = worldHeight() * 0.7f, size = worldHeight() * 0.5f;
            float pump = 1f + 0.06f * beat;
            glow(batch, x, y, size * 1.6f * pump, RED, 0.25f + 0.35f * beat);
            draw(batch, glass, x, y, fit(size, 10) * pump, 0f, WHITE, 0.9f);
            float cracks = progress(0.9f, CLIMAX_PHASE);                // les fissures rouges
            additive(batch);
            for (int i = 0; i < 6; i++) {
                float angle = 30f + i * 60f + (i % 2) * 17f, len = size * 0.45f * Math.min(1f, cracks * 6f - i);
                if (len > 0f) crack(batch, x, y, angle, len, 11L * (i + 1), RED, 0.95f, 8f);
            }
            normal(batch);
            poseTintColor.set(RED);
            poseTint = 0.15f * beat + 0.2f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_PHASE) {
                float squeeze = progress(1.6f, CLIMAX_PHASE);
                stretch(batch, cup, cupX(), cupY(), fit(120f, 8) * (1f - 0.4f * squeeze), fit(120f, 8) * (1f - 0.2f * squeeze), 0f, WHITE, 1f);
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.2f), RED, new float[] {14f, 14.5f}, new float[] {21f, 14.5f});
            fill(batch, RED, 0.12f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f));
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /**
     * « Le cœur du roi cesse de battre » : le cœur frappé sur son plastron bat (et les cœurs des bannières avec
     * lui), un grand tracé de battements traverse le ciel ; les battements ralentissent, faiblissent, et le tracé
     * devient plat ; son cœur et les bannières du château pâlissent en gris.
     */
    static final class HeartDeath extends BossScene {
        private static final Color RED = hex("e0283a"), LINE = hex("6aff8a"), ASH = hex("6a6470");
        private static final float[] BEATS = {0.35f, 1.15f, 2.05f};
        private static final float[] STRENGTH = {1f, 0.7f, 0.4f};
        /** Le tracé traverse l'écran en {@code SWEEP} secondes. */
        private static final float SWEEP = 3.4f;
        private final TextureRegion heart, grey;

        HeartDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_COEUR, 0);
            heart = art("X", new Color[] {hex("ff4a5a")}, CHEST_HEART);
            grey = art("X", new Color[] {hex("7a7480")}, CHEST_HEART);
        }

        private float beat() {
            float beat = 0f;
            for (int i = 0; i < BEATS.length; i++) if (time >= BEATS[i]) beat = Math.max(beat, STRENGTH[i] * (1f - (time - BEATS[i]) / 0.35f));
            return Math.max(0f, beat);
        }

        /** @return la hauteur du tracé (de -1 à 1) à l'instant {@code t}. */
        private static float trace(float t) {
            float y = 0f;
            for (int i = 0; i < BEATS.length; i++) {
                float d = t - BEATS[i], a = STRENGTH[i];
                if (d < 0f || d > 0.4f) continue;
                if (d < 0.05f) y += a * d / 0.05f;
                else if (d < 0.1f) y += a * MathUtils.lerp(1f, -0.4f, (d - 0.05f) / 0.05f);
                else if (d < 0.15f) y += a * MathUtils.lerp(-0.4f, 0f, (d - 0.1f) / 0.05f);
                else if (d > 0.22f && d < 0.38f) y += a * 0.18f * MathUtils.sin((d - 0.22f) / 0.16f * MathUtils.PI);
            }
            return y;
        }

        @Override
        protected void step(float delta) {
            for (float b : BEATS) if (at(b)) rumble(0.1f, 4f);
            if (at(CLIMAX_DEATH)) rumble(0.4f, 6f);
        }

        @Override
        protected void back(Batch batch) {
            float beat = beat(), dead = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.8f), s = decorScale();
            for (float[] b : BANNERS) {                                 // les cœurs des bannières battent avec le sien, puis pâlissent
                glow(batch, decorX(b[0]), decorY(b[1]), 170f * (1f + 0.3f * beat), RED, 0.7f * beat);
                rect(batch, ASH, 0.6f * dead, decorX(b[0] - 6.5f), decorY(b[1] + 10f), 13f * s, 20f * s);
            }
            for (float[] p : PENNANTS) glow(batch, decorX(p[0]), decorY(p[1]), 90f, RED, 0.5f * beat);
            poseShadow = 0.15f + 0.45f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f);
            poseY = -18f * progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 1.2f);
            poseTilt = 3f * progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 1.2f);
        }

        @Override
        protected void front(Batch batch) {
            // son cœur, sur son plastron : il gonfle à chaque battement, puis devient gris
            float beat = beat(), dead = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f), swell = 1f + 0.35f * beat;
            glow(batch, portraitX(17.5f), portraitY(35.5f), 160f + 260f * beat, RED, 0.2f + 0.6f * beat * (1f - dead));
            onPortrait(batch, heart, 17.5f - 3.5f * swell, 35.5f - 3.5f * swell, swell, 1f - dead);
            onPortrait(batch, grey, 14f, 32f, 1f, dead);
            // le grand tracé des battements, de gauche à droite dans le ciel
            float w = worldWidth(), y0 = worldHeight() * 0.8f, amp = worldHeight() * 0.14f;
            rect(batch, Color.BLACK, 0.55f, -MARGIN, y0 - amp * 1.25f, w + 2f * MARGIN, amp * 2.5f);
            float head = w * progress(0f, SWEEP), px = 0f, py = y0;
            Color line = time < CLIMAX_DEATH ? LINE : tmp.set(LINE).lerp(RED, 0.5f + 0.5f * MathUtils.sin(time * 9f));
            additive(batch);
            for (float x = 6f; x <= head; x += 6f) {
                float y = y0 + amp * trace(x / w * SWEEP);
                line(batch, line, 0.35f, px, py, x, y, 18f);
                line(batch, line, 1f, px, py, x, y, 6f);
                px = x;
                py = y;
            }
            normal(batch);
            glow(batch, head, py, 90f, line, 0.9f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi de Carreau
    // -------------------------------------------------------------------------

    /** Ses pièces se dédoublent, les piles montent comme des tours ; un grand livre d'or tourne ses pages. */
    static final class DiamondPhase extends BossScene {
        private static final Color ORANGE = hex("ff9a2a");
        private final TextureRegion book, page;

        DiamondPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_CARREAU, 2);
            book = art("oyw", new Color[] {hex("4a2a08"), hex("ffc93a"), hex("fff0c0")}, BOOK);
            page = art("w", new Color[] {hex("fff0c0")}, "wwwww", "wwwww", "wwwww", "wwwww", "wwwww", "wwwww", "wwwww");
        }

        private int doublings() { return Math.min(5, (int) (time / 0.4f)); }

        @Override
        protected void step(float delta) {
            for (int i = 1; i <= 5; i++) if (at(i * 0.4f)) { rumble(0.1f, 3f + i); sparks(bossX(), groundY() + 40f, 10 * i, GOLD, 500f, 14f); }
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 12f); spray(coin, bossX(), groundY() + 60f, 40, 30f, 150f, 1200f, coinScale(0.035f), 1500f, 2f); }
        }

        @Override
        protected void back(Batch batch) {
            // les piles de pièces : de plus en plus nombreuses, de plus en plus hautes
            int piles = 1 << doublings();
            float grow = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE + 0.4f));
            for (int i = 0; i < Math.min(piles, 16); i++) {
                int side = i % 2 == 0 ? 1 : -1;
                float x = bossX() + side * (bossWidth() * 0.65f + (i / 2) * worldWidth() * 0.045f);
                int height = 2 + (i * 7 % 5) + (int) (14 * grow * (1f - (i / 2) * 0.08f));
                stack(this, batch, coin, x, groundY() + 10f, height, worldHeight() * 0.05f);
            }
            poseTintColor.set(GOLD);
            poseTint = 0.25f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            float x = bossX(), y = worldHeight() * 0.8f, scale = fit(worldHeight() * 0.14f, 9);
            float open = progress(0.2f, 0.6f);
            glow(batch, x, y, 320f, GOLD, 0.5f * open);
            draw(batch, book, x, y, scale, 0f, WHITE, open);
            float flip = (time * (1.2f + 3f * progress(0f, CLIMAX_PHASE))) % 1f;      // ses pages tournent de plus en plus vite
            float side = MathUtils.cosDeg(flip * 180f);
            stretch(batch, page, x + side * scale * 3f, y, scale * Math.abs(side) * 1.1f, scale, 0f, WHITE, open);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), ORANGE, worldWidth() * 1.3f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * « Le Roi de Carreau lâche son trésor » : il serre son tas de trésor (coffre ouvert, pièces, gemmes), qui
     * glisse déjà ; puis il le lâche : son vrai sceptre d'or tombe de sa main, le coffre bascule et tout le trésor
     * roule loin de lui dans le pré. Une dernière pièce tourne sur la tranche à ses pieds et tombe à plat.
     */
    static final class DiamondDeath extends BossScene {
        private static final float LET_GO = CLIMAX_DEATH, LAST = 4.3f;
        private static final int ROWS = 6;

        /** Une pièce (ou une gemme) qui roule et rebondit dans le pré. */
        private static final class Roll {
            float x, y, vx, vy, floor, rotation, scale;
            TextureRegion region;
        }

        private final TextureRegion chest, gem, bare;
        private final Piece scepter;
        private final Array<Roll> rolls = new Array<>(false, 64);
        private boolean dropped;
        private float scepterX, scepterBottom;

        DiamondDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_CARREAU, 0);
            chest = pixels(ExplorationArt.chestOpen());
            gem = art("owg", new Color[] {hex("0a2030"), hex("e0faff"), hex("4fd8ff")}, GEM);
            int[][] rest = king(Card.Suit.CARREAU, ExplorationArt.KingProp.SCEPTER);
            scepter = lift(king(Card.Suit.CARREAU), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { dropped = false; rolls.clear(); }

        private float heapY()      { return groundY() - 6f; }
        private float coinSize()   { return worldHeight() * 0.07f; }
        private float chestX()     { return bossX() - bossWidth() * 0.7f; }

        private void roll(TextureRegion region, float x, float y, float vx, float vy, float scale) {
            Roll r = new Roll();
            r.region = region;
            r.x = x; r.y = y; r.vx = vx; r.vy = vy; r.scale = scale;
            r.floor = groundY() - 30f + 60f * random.nextFloat();
            rolls.add(r);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.9f && time < LET_GO && random.nextFloat() < 0.05f) {   // déjà, des pièces glissent du tas
                float side = random.nextBoolean() ? 1f : -1f;
                roll(coin, bossX() + side * 40f, heapY() + ROWS * coinSize() * 0.45f, side * (150f + 150f * random.nextFloat()), 100f,
                    coinSize() / coin.getRegionWidth());
            }
            if (at(LET_GO)) {                                           // il lâche tout : le trésor roule loin de lui
                rumble(0.6f, 10f);
                sparks(bossX(), heapY() + 80f, 60, GOLD, 700f, 18f);
                for (int i = 0; i < 34; i++) {
                    float side = i % 2 == 0 ? 1f : -1f;
                    roll(coin, bossX() + side * random.nextFloat() * 160f, heapY() + random.nextFloat() * ROWS * coinSize() * 0.45f,
                        side * (300f + 700f * random.nextFloat()), 200f + 500f * random.nextFloat(), coinSize() / coin.getRegionWidth());
                }
                for (int i = 0; i < 6; i++) {
                    float side = i % 2 == 0 ? 1f : -1f;
                    roll(gem, bossX() + side * 60f, heapY() + 120f, side * (250f + 400f * random.nextFloat()), 500f + 300f * random.nextFloat(),
                        fit(46f, 5));
                }
            }
            if (at(LET_GO + 0.45f)) { rumble(0.2f, 5f); sparks(scepterX + bossWidth() * 0.4f, groundY(), 20, GOLD, 300f, 10f); }
            if (at(LAST)) rumble(0.12f, 3f);
            for (int i = rolls.size - 1; i >= 0; i--) {
                Roll r = rolls.get(i);
                r.vy -= 1800f * delta;
                r.x += r.vx * delta;
                r.y += r.vy * delta;
                if (r.y < r.floor) { r.y = r.floor; r.vy = Math.abs(r.vy) < 120f ? 0f : -r.vy * 0.35f; }
                r.vx *= (float) Math.pow(0.7f, delta);
                r.rotation -= r.vx * delta * 1.4f;
                if (r.x < -200f || r.x > worldWidth() + 200f) rolls.removeIndex(i);
            }
        }

        @Override
        protected void back(Batch batch) {
            float grip = progress(0.6f, LET_GO);
            poseX = (random.nextFloat() - 0.5f) * 8f * grip * (time < LET_GO ? 1f : 0f);   // il serre son trésor, il tremble
            float slump = progress(LET_GO + 0.2f, LET_GO + 1f);
            poseY = -16f * slump;
            poseTilt = -4f * slump;
            poseShadow = 0.5f * progress(LET_GO, LET_GO + 1.5f);
            if (time >= LET_GO && !dropped && scepter != null) {        // sa main s'ouvre : son vrai sceptre lui échappe
                dropped = true;
                scepterX = pieceX(scepter);
                scepterBottom = pieceY(scepter) - scepter.height * bossScale() / 2f;
                wear(bare);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (dropped && scepter != null) {                           // le sceptre bascule et tombe à plat dans l'herbe
                float k = Interpolation.pow2In.apply(progress(LET_GO, LET_GO + 0.45f));
                float bounce = 14f * MathUtils.sin(progress(LET_GO + 0.45f, LET_GO + 0.7f) * MathUtils.PI);
                float rot = -88f * k, half = scepter.height * bossScale() / 2f;
                float bx = scepterX, by = MathUtils.lerp(scepterBottom, groundY() + 10f, k) + bounce;
                draw(batch, scepter.region, bx - half * MathUtils.sinDeg(rot), by + half * MathUtils.cosDeg(rot), bossScale(), rot, WHITE, 1f);
            }
            // son coffre ouvert, contre le tas : il bascule sur le côté
            float tip = Interpolation.pow2In.apply(progress(LET_GO, LET_GO + 0.5f)), cs = fit(coinSize() * 2.3f, 30);
            draw(batch, chest, chestX() - 160f * tip, groundY() + 15f * cs - 8.5f * cs * tip, cs, 95f * tip, WHITE, 1f);
            // le tas de trésor devant lui : il s'effondre quand il le lâche
            float heap = 1f - Interpolation.pow2Out.apply(progress(LET_GO, LET_GO + 0.5f));
            int shown = MathUtils.ceil(ROWS * heap);
            float step = coinSize() * 0.8f, scale = coinSize() / coin.getRegionWidth();
            for (int row = 0; row < shown; row++) {
                int count = ROWS + 2 - row;
                for (int k = 0; k < count; k++) {
                    float x = bossX() + (k - (count - 1) / 2f) * step + MathUtils.sin(time * 40f + k + row) * 3f * progress(0.6f, LET_GO);
                    draw(batch, coin, x, heapY() + row * coinSize() * 0.45f, scale, (k * 37 + row * 11) % 30 - 15f, WHITE, 1f);
                }
            }
            for (int i = 0; i < 5 && heap > 0.3f; i++) {
                float gx = bossX() + (i - 2) * step * 1.3f, gy = heapY() + 30f + (i % 2) * 50f;
                glow(batch, gx, gy, 90f, hex("4fd8ff"), 0.5f * pulse(5f + i, 0.4f));
                draw(batch, gem, gx, gy, fit(40f, 5), 0f, WHITE, 1f);
            }
            for (Roll r : rolls) draw(batch, r.region, r.x, r.y, r.scale, r.rotation, WHITE, 1f);
            // la dernière pièce, revenue à ses pieds : elle tourne sur la tranche de plus en plus lentement, puis tombe à plat
            if (time > LET_GO + 0.8f) {
                float k = progress(LET_GO + 0.8f, LAST);
                float wide = k < 1f ? Math.abs(MathUtils.cos(k * k * 30f)) * (1f - k) + 0.12f : 1f;
                float flat = k < 1f ? 1f : 0.3f;
                float x = bossX() + bossWidth() * 0.15f, y = groundY() + (k < 1f ? coinSize() * 0.5f : coinSize() * 0.15f);
                stretch(batch, coin, x, y, scale * wide, scale * flat, 0f, WHITE, 1f);
                if (k >= 1f) glow(batch, x, y, 120f, GOLD, 0.4f * (1f - progress(LAST, LAST + 0.6f)));
            }
            flash(batch, GOLD, LET_GO, 0.25f, 0.35f);
        }
    }
}
