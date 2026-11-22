package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Manoir hanté (10 lieux, Astra 2026-11-20 ; redessinées le
 * 2026-11-22 pour que l'image montre la phrase : la cloche des douze coups, le Comte qui se change en chauves-souris
 * puis retourne à son cercueil, les bougies, le grimoire, l'éboulement, le rideau de théâtre).
 */
final class ManorBossScenes {

    private ManorBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case COMTE_MINUIT -> new CountPhase(kit);
            case BIBLIOTHECAIRE -> new LibrarianPhase(kit);
            case FOSSOYEUR -> new DiggerPhase(kit);
            case MARIONNETTISTE -> new PuppeteerPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case COMTE_MINUIT -> new CountDeath(kit);
            case BIBLIOTHECAIRE -> new LibrarianDeath(kit);
            case FOSSOYEUR -> new DiggerDeath(kit);
            case MARIONNETTISTE -> new PuppeteerDeath(kit);
            default -> null;
        };
    }

    static final Color CRIMSON = hex("d8203a"), CANDLE = hex("ffd27a"), VIOLET = hex("b88aff"), DAWN = hex("ffd8a0");
    static final Color GRAVE = hex("6ad8a0"), EARTH = hex("5a3a22"), THREAD = hex("e8e0d0"), DUST = hex("b8a898");
    static final Color SMOKE = hex("8a8494");

    /** Les yeux des portraits du Manoir (pixels 13-14 et 20-21, lignes 15-16 : leurs centres, en bords de pixels). */
    static final float[] LEFT_EYE = {14f, 16f}, RIGHT_EYE = {21f, 16f};

    // ----- Accessoires en pixel art -----

    static final Color[] BAT_COLORS = {hex("1c1226"), hex("4a3a5e"), hex("ff3a3a")};
    /** Une chauve-souris, ailes levées puis ailes baissées (b corps, l reflet, r yeux). */
    static final String[] BAT_UP = {
        "b...........b", "bb.........bb", "lbb..b.b..bbl", ".lbbbbbbbbbl.", "...bbrbrbb...", ".....bbb.....", "......b......"};
    static final String[] BAT_DOWN = {
        ".....b.b.....", "....bbbbb....", "...bbrbrbb...", ".lbbbbbbbbbl.", "lbb..bbb..bbl", "bb.........bb", "b...........b"};

    static final Color[] BELL_COLORS = {hex("1a0f0a"), hex("b07a2a"), hex("f0c060"), hex("6a4214"), hex("3a2410")};
    /** La grande cloche de minuit (o contour, b bronze, h reflet, d ombre, c battant). */
    static final String[] BELL = {
        ".....ooooo.....",
        "....obbbbbo....",
        "...obhbbbbdo...",
        "...obhbbbbdo...",
        "...obhbbbbdo...",
        "..obhbbbbbbdo..",
        "..obhbbbbbbdo..",
        "..obhbbbbbbdo..",
        ".obhbbbbbbbbdo.",
        ".obhbbbbbbbbdo.",
        "obhhbbbbbbbbddo",
        "obbbbbbbbbbbddo",
        "ooooooooooooooo",
        "......occo.....",
        ".......oo......"};

    static final Color[] CANDLE_COLORS = {hex("1a1410"), hex("efe6d0"), hex("c8b898"), hex("3a3028"), hex("c4891e"), hex("8a5a12")};
    /** Une bougie sur son bougeoir de laiton (w cire, s ombre, k mèche, g laiton, d laiton sombre). */
    static final String[] CANDLE_ART = {
        "...k...", "..owo..", "..owso.", ".owwso.", ".owwso.", ".owwso.", ".owwso.", ".owsso.", ".owwso.", ".owwso.",
        "ooooooo", "ogggggo", ".odddo.", "..ogo..", "..ogo..", ".ogggo.", "ogggggo", "ooooooo"};
    static final Color[] FLAME_COLORS = {hex("ff8a1e"), hex("ffd27a"), hex("ffffff")};
    static final String[] FLAME_A = {"..o..", ".ooo.", ".oyo.", "oyyyo", "oywyo", ".oyo."};
    static final String[] FLAME_B = {".o...", ".oo..", ".oyo.", "oyyyo", "oywyo", ".oyo."};

    static final Color[] BOOK_COLORS = {hex("120a14"), hex("5a2a7a"), hex("3a1a52"), hex("efe6d0"), hex("b8ac90"), hex("c4891e")};
    /** Le grand grimoire, ouvert (o contour, p couverture, q couverture sombre, w page, l ligne d'écriture, g fermoir). */
    static final String[] GRIMOIRE_OPEN = {
        "oooooooooo.oooooooooo",
        "oppwwwwwwwowwwwwwwwqo",
        "opwwllllwwowlllllwwqo",
        "opwwwwwwwwowwwwwwwwqo",
        "opwllllllwowllllwwwqo",
        "opwwwwwwwwowwwwwwwwqo",
        "opwwlllllwowlllllwwqo",
        "opwwwwwwwwowwwwwwwwqo",
        "opwllllwwwowwllllwwqo",
        "opwwwwwwwwowwwwwwwwqo",
        "oppppppppppqqqqqqqqqo",
        "ooooooooooooooooooooo"};
    static final String[] GRIMOIRE_SHUT = {
        "oooooooooooooo",
        "oqgppppppppggo",
        "oqgppppppppppo",
        "oqpppgggggpppo",
        "oqppgpppppgppo",
        "oqppgpglgpgppo",
        "oqppgpppppgppo",
        "oqpppgggggpppo",
        "oqgppppppppppo",
        "oqgppppppppggo",
        "owwwwwwwwwwwwo",
        "oooooooooooooo"};

    static final Color[] ROCK_COLORS = {hex("14100c"), hex("6a5a4a"), hex("8a7a68"), hex("4a3e32")};
    static final String[][] ROCKS = {
        {"..oooo..", ".ollllo.", "ollrrrdo", "olrrrrdo", "orrrrddo", ".oddddo.", "..oooo.."},
        {".ooo..", "olllo.", "olrrdo", "orrddo", ".oooo."},
        {"..ooo...", ".ollloo.", "olrrrrdo", "orrrrddo", "oorrddo.", "..oooo.."}};

    static final Color[] STONE_COLORS = {hex("14141a"), hex("7a7a88"), hex("9a9aa8"), hex("5a5a68"), hex("3a3a46"), hex("5a8a4a")};
    /** La pierre tombale (o contour, s pierre, l reflet, d ombre, c croix gravée, m mousse). */
    static final String[] TOMBSTONE = {
        "....oooooo....",
        "..oollllsdoo..",
        ".olssssssssdo.",
        ".olssscccsssdo",
        "olsssscscsssdo",
        "olssccccccssdo",
        "olsssscscsssdo",
        "olsssscscsssdo",
        "olsssscscsssdo",
        "olsssscccsssdo",
        "olssssssssssdo",
        "olssssssssssdo",
        "olssssssssssdo",
        "omsssssssssmdo",
        "ommmssssssmmmo",
        "oooooooooooooo"};

    static final Color[] SCISSORS_COLORS = {hex("14141a"), hex("dce4ee"), hex("8a9ab0"), hex("b01a2a"), hex("ffffff")};
    /** De grands ciseaux, ouverts puis fermés (o contour, s lame, d lame sombre, r anneau, w éclat). */
    static final String[] SCISSORS_OPEN = {
        "ow...........wo",
        "osdo.......odso",
        ".osdo.....odso.",
        "..osdo...odso..",
        "...osdo.odso...",
        "....osdodso....",
        ".....osdso.....",
        "......odo......",
        ".....orororo...",
        "....or..o..ro..",
        "....or.o.o.ro..",
        ".....oro...oro.",
        "......o.....o.."};
    static final String[] SCISSORS_SHUT = {
        "......ow.......",
        "......osdo.....",
        "......osdo.....",
        "......osdo.....",
        "......osdo.....",
        "......osdo.....",
        "......osdo.....",
        "......odo......",
        ".....orororo...",
        "....or..o..ro..",
        "....or.o.o.ro..",
        ".....oro...oro.",
        "......o.....o.."};

    static final Color[] NOTE_COLORS = {hex("1a1410"), hex("ffd27a")};
    static final String[] NOTE = {"...oo", "..ono", "..on.", "..on.", "oonn.", "onnn.", ".oo.."};

    /** Un cercueil debout de {@code 13 x 26} pixels : ouvert (velours rouge dedans) ou fermé (bois et croix d'or). */
    static String[] coffin(boolean open) {
        int width = 13, height = 26, mid = width / 2;
        String[] rows = new String[height];
        for (int y = 0; y < height; y++) {
            float half = y <= 5 ? 3f + y * 0.6f : 6f - (y - 5) * 2.6f / (height - 6);
            int lo = Math.round(mid - half), hi = Math.round(mid + half);
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++) {
                char c = '.';
                if (x >= lo && x <= hi) {
                    boolean edge = x == lo || x == hi || y == 0 || y == height - 1;
                    if (edge) c = 'o';
                    else if (open) c = x == lo + 1 || x == hi - 1 || y == 1 || y == height - 2 ? 'w' : (x < mid ? 'r' : 'q');
                    else {
                        boolean cross = (x == mid && y >= 6 && y <= 15) || (y == 9 && Math.abs(x - mid) <= 2);
                        c = cross ? 'g' : x == lo + 1 ? 'l' : x == hi - 1 ? 'd' : 'w';
                    }
                }
                row.append(c);
            }
            rows[y] = row.toString();
        }
        return rows;
    }

    static final Color[] COFFIN_COLORS = {hex("120a08"), hex("5a3420"), hex("7a4a2c"), hex("3a2014"), hex("ffc93a"), hex("8a1020"), hex("5a0814")};

    /** Un rideau de théâtre en velours rouge, plis verticaux et franges d'or, de {@code 64 x 36} pixels. */
    static Pixmap curtain() {
        int width = 60, height = 34;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        Color[] tones = {hex("2e040a"), hex("4e0a14"), hex("6e1220"), hex("8e1c2c"), hex("a8283a")};
        Color gold = hex("e0a83a"), goldDark = hex("8a5a12");
        for (int x = 0; x < width; x++) {
            float fold = 0.5f + 0.5f * MathUtils.sin(x * MathUtils.PI2 / 12f);           // un pli tous les 12 pixels
            Color c = tones[Math.min(tones.length - 1, (int) (fold * fold * tones.length))];
            for (int y = 0; y < height - 3; y++) {
                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
            for (int y = height - 3; y < height; y++) {
                boolean tassel = (x % 4 == 1 || x % 4 == 2) || y == height - 3;
                if (!tassel) continue;
                pixmap.setColor(y == height - 1 ? goldDark : gold);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    // -------------------------------------------------------------------------
    // Une nuée de chauves-souris qui battent des ailes
    // -------------------------------------------------------------------------

    /** Des chauves-souris : chacune file, ondule et bat des ailes à son rythme ; certaines peuvent rentrer dans un cercueil. */
    static final class Flock {
        final float[] x, y, vx, vy, beat, size, homeX, homeY;
        final boolean[] home;
        int count;

        Flock(int capacity) {
            x = new float[capacity]; y = new float[capacity]; vx = new float[capacity]; vy = new float[capacity];
            beat = new float[capacity]; size = new float[capacity]; homeX = new float[capacity]; homeY = new float[capacity];
            home = new boolean[capacity];
        }

        void clear() { count = 0; }

        void add(float px, float py, float pvx, float pvy, float s, float phase) {
            if (count >= x.length) return;
            x[count] = px; y[count] = py; vx[count] = pvx; vy[count] = pvy; size[count] = s; beat[count] = phase;
            home[count] = false;
            count++;
        }

        /** La chauve-souris {@code k} rentre vers ({@code hx}, {@code hy}). */
        void sendHome(int k, float hx, float hy) {
            home[k] = true;
            homeX[k] = hx;
            homeY[k] = hy;
        }

        void update(float delta, float time) {
            for (int k = 0; k < count; k++) {
                beat[k] += delta * 9f;
                if (home[k]) {                                     // elle plonge vers sa cible
                    float dx = homeX[k] - x[k], dy = homeY[k] - y[k];
                    float dist = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
                    float speed = Math.min(1400f, 300f + dist * 3f);
                    vx[k] = MathUtils.lerp(vx[k], dx / dist * speed, Math.min(1f, delta * 6f));
                    vy[k] = MathUtils.lerp(vy[k], dy / dist * speed, Math.min(1f, delta * 6f));
                } else {
                    vy[k] += MathUtils.sin(time * 5f + k) * 260f * delta;   // le vol ondulé
                }
                x[k] += vx[k] * delta;
                y[k] += vy[k] * delta;
            }
        }

        /** @return {@code true} si la chauve-souris {@code k} est arrivée chez elle. */
        boolean arrived(int k) {
            return home[k] && Math.abs(homeX[k] - x[k]) < 18f && Math.abs(homeY[k] - y[k]) < 18f;
        }

        void draw(BossScene scene, Batch batch, TextureRegion up, TextureRegion down, float alpha) {
            for (int k = 0; k < count; k++) {
                if (arrived(k)) continue;
                TextureRegion frame = MathUtils.sin(beat[k]) > 0f ? up : down;
                float flip = vx[k] < 0f ? -1f : 1f;
                scene.stretch(batch, frame, x[k], y[k], size[k] * flip, size[k], MathUtils.clamp(vy[k] * 0.02f, -20f, 20f) * flip,
                    BossScene.WHITE, alpha);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Le Comte de Minuit
    // -------------------------------------------------------------------------

    /**
     * « Les douze coups sonnent » : la grande cloche de minuit se balance au-dessus du salon et sonne douze fois
     * (son, onde de bronze, une chauve-souris qui s'envole à chaque coup) ; le salon rougit coup après coup ; au
     * douzième, ses yeux flamboient et ses crocs sortent.
     */
    static final class CountPhase extends BossScene {
        static final float FIRST = 0.4f, GAP = 0.28f, LAST = FIRST + 11 * GAP;
        private final TextureRegion bell, batUp, batDown;
        private final Flock flock = new Flock(64);

        CountPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.COMTE_MINUIT, 2);
            bell = art("obhdc", BELL_COLORS, BELL);
            batUp = art("blr", BAT_COLORS, BAT_UP);
            batDown = art("blr", BAT_COLORS, BAT_DOWN);
        }

        static float strike(int k) { return FIRST + k * GAP; }

        @Override protected float textAt()      { return LAST + 0.3f; }
        @Override protected float coverStart()  { return LAST + 1.9f; }
        @Override protected float coverFull()   { return LAST + 2.5f; }
        @Override protected void restart()      { flock.clear(); }

        private float bellX()      { return bossX() - bossWidth() * 1.35f; }
        private float pivotY()     { return worldHeight() * 0.97f; }
        private float bellScale()  { return worldHeight() * 0.26f / BELL.length; }
        private float bellAngle() {
            float swing = 22f * Math.min(1f, time / FIRST) * (time > LAST ? (float) Math.exp(-(time - LAST) * 1.6f) : 1f);
            return swing * MathUtils.cos(MathUtils.PI * (time - FIRST) / GAP);
        }
        private int struck() {
            int n = 0;
            for (int k = 0; k < 12; k++) if (time >= strike(k)) n++;
            return n;
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 12; k++) {
                if (at(strike(k))) {
                    rumble(0.1f, 2f + k * 0.6f);
                    float mouthY = pivotY() - BELL.length * bellScale();
                    for (int b = 0; b < (k == 11 ? 3 : 1); b++) {
                        float dir = random.nextFloat() * 120f + 30f;
                        flock.add(bellX(), mouthY, MathUtils.cosDeg(dir) * 500f, MathUtils.sinDeg(dir) * 300f,
                            bellScale() * 0.9f, random.nextFloat() * 6f);
                    }
                }
            }
            if (at(LAST)) {
                rumble(0.7f, 14f);
                for (int b = 0; b < 22; b++) {
                    float dir = random.nextFloat() * 360f;
                    flock.add(bossX(), bossY(), MathUtils.cosDeg(dir) * (400f + 500f * random.nextFloat()),
                        MathUtils.sinDeg(dir) * (300f + 400f * random.nextFloat()), bellScale() * (0.6f + 0.5f * random.nextFloat()),
                        random.nextFloat() * 6f);
                }
                sparks(bossX(), bossY(), 50, CRIMSON, 700f, 12f);
            }
            flock.update(delta, time);
        }

        @Override
        protected float decorLight() {
            float hit = 0f;
            for (int k = 0; k < 12; k++) hit = Math.max(hit, 1f - Math.abs(time - strike(k)) / 0.08f);
            return 0.6f - 0.3f * struck() / 12f + 0.15f * Math.max(0f, hit);
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, CRIMSON, 0.03f * struck());                                  // le salon rougit à chaque coup
            float red = struck() / 12f;
            poseTint = 0.45f * red * pulse(6f, 0.7f);
            poseTintColor.set(CRIMSON);
            poseY = 26f * Interpolation.pow2Out.apply(progress(LAST - 0.2f, LAST + 0.3f));
            poseScale = 1f + 0.05f * progress(LAST - 0.2f, LAST + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            float scale = bellScale(), w = BELL[0].length() * scale, h = BELL.length * scale;
            float angle = bellAngle();
            line(batch, hex("2a1a10"), 1f, bellX(), worldHeight() + MARGIN, bellX(), pivotY(), scale * 1.2f);   // la corde
            batch.setColor(WHITE);
            batch.draw(bell, bellX() - w / 2f, pivotY() - h, w / 2f, h, w, h, 1f, 1f, angle);
            float mouthX = bellX() + MathUtils.sinDeg(-angle) * h, mouthY = pivotY() - MathUtils.cosDeg(angle) * h;
            for (int k = 0; k < 12; k++) {
                wave(batch, strike(k), 0.5f, mouthX, mouthY + h * 0.4f, k == 11 ? CRIMSON : CANDLE, h * (k == 11 ? 4f : 2.4f));
            }
            flock.draw(this, batch, batUp, batDown, 1f);
            float awake = progress(LAST - 0.1f, LAST + 0.2f);
            if (awake > 0f) {                                                        // ses yeux et ses crocs
                eyes(batch, awake * pulse(7f, 0.7f), CRIMSON, LEFT_EYE, RIGHT_EYE);
                float px = bossScale() * poseScale;
                for (int fx : new int[] {15, 19}) {
                    rect(batch, WHITE, awake, portraitX(fx), portraitY(24f) - px * 0.2f * awake, px, px * (2f + 0.2f * awake));
                }
            }
            flash(batch, CRIMSON, LAST, 0.3f, 0.35f);
        }
    }

    /**
     * L'aube : la lumière du jour entre par la fenêtre, il se protège et noircit ; il éclate en une nuée de
     * chauves-souris qui se dispersent dans tout le salon ; les dernières plongent dans son cercueil ouvert,
     * et le couvercle claque.
     */
    static final class CountDeath extends BossScene {
        static final float BURST = CLIMAX_DEATH, RETURN = 3.2f, SHUT = 4.0f;
        private final TextureRegion batUp, batDown, open, shut;
        private final Flock flock = new Flock(80);
        private final int[] returners = new int[6];

        CountDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.COMTE_MINUIT, 0);
            batUp = art("blr", BAT_COLORS, BAT_UP);
            batDown = art("blr", BAT_COLORS, BAT_DOWN);
            open = art("owldgrq", COFFIN_COLORS, coffin(true));
            shut = art("owldgrq", COFFIN_COLORS, coffin(false));
        }

        @Override protected void restart() { flock.clear(); }

        private float coffinX()      { return bossX() + bossWidth() * 1.55f; }
        private float coffinBottom() { return bossBottom() - worldHeight() * 0.04f; }
        private float coffinScale()  { return bossHeight() * 0.95f / 26f; }

        @Override
        protected void step(float delta) {
            if (time > 1.2f && time < BURST && random.nextFloat() < delta * 25f) {    // il fume au soleil
                sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 2,
                    SMOKE, 160f, 9f);
            }
            if (at(BURST)) {                                                         // il éclate en chauves-souris
                rumble(0.5f, 10f);
                for (int b = 0; b < 56; b++) {
                    float px = bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 0.8f;
                    float py = bossBottom() + bossHeight() * (0.1f + 0.85f * random.nextFloat());
                    float dir = MathUtils.atan2(py - bossY(), px - bossX()) * MathUtils.radiansToDegrees + (random.nextFloat() - 0.5f) * 50f;
                    float speed = 350f + 700f * random.nextFloat();
                    flock.add(px, py, MathUtils.cosDeg(dir) * speed, MathUtils.sinDeg(dir) * speed * 0.8f + 120f,
                        coffinScale() * (0.55f + 0.45f * random.nextFloat()), random.nextFloat() * 6f);
                }
                for (int k = 0; k < returners.length; k++) returners[k] = k * 9;
                sparks(bossX(), bossY(), 40, SMOKE, 500f, 12f);
            }
            if (at(RETURN)) {                                                        // les dernières rentrent au cercueil
                for (int k = 0; k < returners.length; k++) {
                    flock.sendHome(returners[k], coffinX(), coffinBottom() + 26f * coffinScale() * 0.55f);
                }
            }
            if (at(SHUT)) {
                rumble(0.4f, 12f);
                sparks(coffinX(), coffinBottom() + 10f, 30, DUST, 400f, 10f);
            }
            flock.update(delta, time);
        }

        @Override
        protected float decorLight() { return 0.5f + 0.4f * Interpolation.pow2In.apply(progress(0.2f, BURST)); }

        @Override
        protected void back(Batch batch) {
            float dawn = progress(0.2f, BURST);
            glow(batch, bossX(), worldHeight() * 0.85f, worldHeight() * (0.4f + 1.2f * dawn), DAWN, 0.45f * dawn);   // l'aube à la fenêtre
            poseShadow = 0.75f * progress(0.8f, BURST);
            poseX = time > 1.2f && time < BURST ? 4f * MathUtils.sin(time * 70f) * progress(1.2f, BURST) : 0f;
            poseStretch = -0.06f * progress(1.6f, BURST);                             // il se recroqueville
            showBoss = time < BURST;
        }

        @Override
        protected void front(Batch batch) {
            float scale = coffinScale(), w = 13f * scale, h = 26f * scale;
            batch.setColor(WHITE);
            batch.draw(open, coffinX() - w / 2f, coffinBottom(), w, h);              // le cercueil ouvert
            float close = Interpolation.pow2In.apply(progress(SHUT - 0.18f, SHUT));
            float lidX = MathUtils.lerp(coffinX() + w * 0.95f, coffinX(), close);    // le couvercle, posé à côté puis qui claque
            float lidTilt = MathUtils.lerp(-8f, 0f, close);
            batch.draw(shut, lidX - w / 2f, coffinBottom(), w / 2f, 0f, w, h, 1f, 1f, lidTilt);
            flock.draw(this, batch, batUp, batDown, 1f);
            flash(batch, DAWN, BURST, 0.35f, 0.4f);
            flash(batch, DUST, SHUT, 0.15f, 0.15f);
        }
    }

    // -------------------------------------------------------------------------
    // La Bibliothécaire
    // -------------------------------------------------------------------------

    /**
     * « Les bougies s'éteignent » : trois bougies sur leurs bougeoirs, soufflées une à une (un filet de fumée
     * monte) ; la bibliothèque s'assombrit à chaque fois ; dans le noir, ses yeux violets s'allument derrière
     * ses lunettes.
     */
    static final class LibrarianPhase extends BossScene {
        private final TextureRegion candle, flameA, flameB;

        LibrarianPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.BIBLIOTHECAIRE, 2);
            candle = art("owskgd", CANDLE_COLORS, CANDLE_ART);
            flameA = art("oyw", FLAME_COLORS, FLAME_A);
            flameB = art("oyw", FLAME_COLORS, FLAME_B);
        }

        private float candleScale()   { return worldHeight() * 0.2f / CANDLE_ART.length; }
        private float candleX(int k)  { return bossX() + (k - 1) * bossWidth() * 1.05f; }
        private float candleBottom(int k) { return bossBottom() + (k == 1 ? bossHeight() * 1.02f : bossHeight() * 0.35f); }
        private float wickY(int k)    { return candleBottom(k) + CANDLE_ART.length * candleScale(); }
        private float out(int k)      { return 0.6f + k * 0.45f; }
        private int lit() {
            int n = 0;
            for (int k = 0; k < 3; k++) if (time < out(k)) n++;
            return n;
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) {
                if (at(out(k))) {
                    rumble(0.05f, 2f);
                    for (int s = 0; s < 10; s++) {                                    // le filet de fumée
                        Particle puff = particle(candleX(k), wickY(k), 80f + 20f * random.nextFloat(), 60f + 60f * random.nextFloat(),
                            1.2f + 0.5f * random.nextFloat(), 14f + 10f * random.nextFloat());
                        puff.color.set(SMOKE);
                        puff.drag = 0.6f;
                        glows.add(puff);
                    }
                }
            }
            if (at(CLIMAX_PHASE)) { rumble(0.4f, 8f); sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, VIOLET, 600f, 12f); }
        }

        @Override
        protected float decorLight() { return 0.15f + 0.16f * lit(); }

        @Override
        protected void back(Batch batch) {
            poseShadow = 0.6f - 0.2f * lit();
            poseY = 20f * progress(1.6f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            float scale = candleScale();
            for (int k = 0; k < 3; k++) {
                float x = candleX(k), bottom = candleBottom(k);
                float light = 0.35f + 0.65f * (time < out(k) ? 1f : 1f - progress(out(k), out(k) + 0.3f));
                batch.setColor(light, light, light, 1f);
                batch.draw(candle, x - 3.5f * scale, bottom, 7f * scale, CANDLE_ART.length * scale);
                if (time < out(k)) {
                    float shrink = 1f - Interpolation.pow2In.apply(progress(out(k) - 0.15f, out(k)));
                    TextureRegion flame = MathUtils.sin(time * (13f + k * 3f)) > 0f ? flameA : flameB;
                    stretch(batch, flame, x, wickY(k) + 3f * scale * shrink, scale * shrink, scale * shrink, 0f, WHITE, 1f);
                    glow(batch, x, wickY(k) + 3f * scale, worldHeight() * 0.22f * pulse(12f + k, 0.85f) * shrink, CANDLE, 0.5f);
                }
            }
            float dark = progress(out(2), out(2) + 0.3f);
            if (dark > 0f) {
                eyes(batch, dark * pulse(7f, 0.6f), VIOLET, LEFT_EYE, RIGHT_EYE);
                glow(batch, bossX(), bossY() + bossHeight() * 0.25f, bossWidth() * 0.7f, VIOLET, 0.3f * dark * pulse(5f, 0.7f));
            }
            flash(batch, VIOLET, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /**
     * « La Bibliothécaire referme son dernier livre » : un grand grimoire s'ouvre devant elle, ses pages tournent
     * toutes seules ; il l'aspire en tourbillon, puis se referme d'un coup sec et tombe, fermé, au sol.
     */
    static final class LibrarianDeath extends BossScene {
        static final float OPEN = 0.5f, PULL = 1.3f, SLAM = CLIMAX_DEATH;
        private final TextureRegion grimoire, closed;

        LibrarianDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.BIBLIOTHECAIRE, 0);
            grimoire = art("opqwlg", BOOK_COLORS, GRIMOIRE_OPEN);
            closed = art("opqwlg", BOOK_COLORS, GRIMOIRE_SHUT);
        }

        private float bookScale() { return bossWidth() * 1.25f / GRIMOIRE_OPEN[0].length(); }
        private float bookX()     { return bossX(); }
        private float bookY() {
            float fall = Interpolation.bounceOut.apply(progress(SLAM + 0.15f, SLAM + 0.75f));
            return MathUtils.lerp(bossBottom() + bossHeight() * 0.12f, groundY() + GRIMOIRE_SHUT.length * bookScale() * 0.5f, fall);
        }

        @Override
        protected void step(float delta) {
            if (time > OPEN && time < SLAM && random.nextFloat() < delta * 18f) {     // des pages qui volent
                Particle page = particle(bookX() + (random.nextFloat() - 0.5f) * bossWidth(), bookY(), 60f + 60f * random.nextFloat(),
                    300f + 200f * random.nextFloat(), 1.0f, 16f);
                page.color.set(hex("efe6d0"));
                page.spin = 360f;
                glows.add(page);
            }
            if (at(SLAM)) {
                rumble(0.6f, 12f);
                sparks(bookX(), bookY(), 50, DUST, 700f, 12f);
                sparks(bookX(), bookY(), 30, VIOLET, 500f, 10f);
            }
            if (at(SLAM + 0.75f)) { rumble(0.25f, 6f); sparks(bookX(), groundY(), 24, DUST, 400f, 10f); }
        }

        @Override
        protected void back(Batch batch) {
            float pull = Interpolation.pow2In.apply(progress(PULL, SLAM - 0.1f));
            poseScale = 1f - 0.94f * pull;
            poseY = (bookY() - bossBottom() - bossHeight() * poseScale / 2f) * pull;  // son centre glisse vers le cœur du livre
            poseTilt = 25f * MathUtils.sin(time * 18f) * pull;
            poseTint = 0.6f * pull;
            poseTintColor.set(VIOLET);
            showBoss = time < SLAM - 0.08f;
        }

        @Override
        protected void front(Batch batch) {
            float scale = bookScale();
            float appear = Interpolation.swingOut.apply(progress(0.1f, OPEN));
            if (time < SLAM) {
                if (appear > 0f) {
                    float flip = progress(OPEN, PULL);
                    stretch(batch, grimoire, bookX(), bookY(), scale * appear, scale * appear, 0f, WHITE, 1f);
                    if (flip > 0f && flip < 1f) {                                     // une page qui tourne
                        float k = (flip * 4f) % 1f;
                        float pageW = 9f * scale * MathUtils.cos(k * MathUtils.PI);
                        rect(batch, hex("efe6d0"), 1f, bookX() + Math.min(0f, pageW), bookY() - 4f * scale, Math.abs(pageW), 9f * scale);
                    }
                    if (time > PULL) {                                               // le tourbillon qui l'aspire
                        glow(batch, bookX(), bookY(), bossWidth() * (0.6f + 0.4f * pulse(9f, 0.5f)), VIOLET, 0.45f * progress(PULL, PULL + 0.4f));
                    }
                }
            } else {
                stretch(batch, closed, bookX(), bookY(), scale, scale, 0f, WHITE, 1f);
            }
            flash(batch, VIOLET, SLAM, 0.3f, 0.35f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Fossoyeur
    // -------------------------------------------------------------------------

    /**
     * « La terre tremble » : il frappe le sol de sa pelle trois fois, le sol se fend et des lueurs vertes sortent
     * des fissures ; au troisième coup, la voûte de la crypte s'éboule : des pierres et de la poussière pleuvent.
     */
    static final class DiggerPhase extends BossScene {
        private final TextureRegion[] rocks = new TextureRegion[ROCKS.length];

        DiggerPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.FOSSOYEUR, 2);
            for (int k = 0; k < ROCKS.length; k++) rocks[k] = art("olrd", ROCK_COLORS, ROCKS[k]);
        }

        private float hit(int k) { return 0.6f + k * 0.5f; }

        private void fall(int count, float spread) {
            float scale = worldHeight() * 0.1f / 7f;
            for (int r = 0; r < count; r++) {
                Particle rock = place(rocks[random.nextInt(rocks.length)], bossX() + (random.nextFloat() - 0.5f) * spread,
                    worldHeight() + 40f + random.nextFloat() * 200f, (random.nextFloat() - 0.5f) * 80f, -200f * random.nextFloat(),
                    scale * (0.7f + 0.8f * random.nextFloat()), 1.6f);
                rock.gravity = 1800f;
                rock.spin = (random.nextFloat() - 0.5f) * 300f;
            }
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) {
                if (at(hit(k))) {
                    rumble(0.25f, 6f + 3f * k);
                    sparks(bossX(), groundY(), 25, EARTH, 600f, 12f);
                    fall(2 + 2 * k, worldWidth() * 0.6f);
                }
            }
            if (time > hit(0) && time < CLIMAX_PHASE + 1.2f && random.nextFloat() < delta * 30f) {   // la poussière qui tombe
                Particle dust = particle(random.nextFloat() * worldWidth(), worldHeight(), 270f, 200f + 200f * random.nextFloat(), 1.4f, 8f);
                dust.color.set(DUST);
                glows.add(dust);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.9f, 16f);
                sparks(bossX(), groundY(), 60, GRAVE, 800f, 14f);
                fall(16, worldWidth() * 0.9f);
            }
        }

        @Override
        protected float decorLight() { return 0.62f - 0.25f * progress(hit(0), CLIMAX_PHASE) + 0.1f * MathUtils.sin(time * 40f) * progress(hit(0), CLIMAX_PHASE + 0.5f); }

        @Override
        protected void back(Batch batch) {
            float strike = 0f;
            for (int k = 0; k < 3; k++) strike = Math.max(strike, gesture(hit(k) - 0.2f, hit(k) + 0.05f));
            poseStretch = -0.08f * strike;
            poseY = -10f * strike;
            poseTint = 0.3f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(GRAVE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 3; k++) {
                float open = progress(hit(k), hit(k) + 0.3f);
                if (open > 0f) {
                    crack(batch, bossX() + (k - 1) * bossWidth() * 0.7f, groundY() + 10f, -90f + (k - 1) * 30f, worldHeight() * 0.2f * open, 31L + k,
                        GRAVE, 0.9f, 6f);
                    glow(batch, bossX() + (k - 1) * bossWidth() * 0.7f, groundY(), bossWidth() * 0.6f * open, GRAVE, 0.3f * open * pulse(6f, 0.6f));
                }
            }
            if (time >= CLIMAX_PHASE) fill(batch, DUST, 0.25f * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.6f)));   // le nuage de l'éboulement
            flash(batch, GRAVE, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /**
     * « Le Fossoyeur rejoint sa tombe » : la terre s'ouvre sous lui, il s'enfonce dans sa propre fosse ; la terre se
     * referme en tertre, et une pierre tombale moussue sort de terre ; une lueur verte s'en échappe.
     */
    static final class DiggerDeath extends BossScene {
        private final TextureRegion stone;

        DiggerDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.FOSSOYEUR, 0);
            stone = art("osldcm", STONE_COLORS, TOMBSTONE);
        }

        private float stoneScale() { return bossHeight() * 0.7f / TOMBSTONE.length; }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < CLIMAX_DEATH && random.nextFloat() < delta * 20f) sparks(bossX(), groundY(), 4, EARTH, 400f, 10f);
            if (at(0.6f)) rumble(1.8f, 5f);
            if (at(CLIMAX_DEATH + 0.2f)) rumble(0.8f, 8f);
            if (at(3.2f)) { rumble(0.3f, 10f); sparks(bossX(), groundY() + 20f, 30, EARTH, 500f, 12f); }
            if (at(3.4f)) {
                for (int k = 0; k < 6; k++) {                                         // l'âme verte qui s'échappe
                    Particle wisp = particle(bossX() + (random.nextFloat() - 0.5f) * 60f, groundY() + 40f, 90f, 120f + 60f * random.nextFloat(),
                        1.8f, 30f + 20f * random.nextFloat());
                    wisp.color.set(GRAVE);
                    glows.add(wisp);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            poseSink = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH + 0.4f));
            poseShadow = 0.4f * progress(0.6f, CLIMAX_DEATH);
        }

        @Override
        protected void front(Batch batch) {
            float scale = stoneScale(), w = TOMBSTONE[0].length() * scale, full = TOMBSTONE.length * scale;
            float rise = Interpolation.pow2Out.apply(progress(CLIMAX_DEATH + 0.2f, 3.2f));
            if (rise > 0f) {                                                          // la pierre sort de terre
                int rows = Math.max(1, Math.round(TOMBSTONE.length * rise));
                TextureRegion part = new TextureRegion(stone, 0, 0, TOMBSTONE[0].length(), rows);
                batch.setColor(WHITE);
                batch.draw(part, bossX() - w / 2f, groundY() + 6f, w, rows * scale);
            }
            float mound = progress(CLIMAX_DEATH - 0.3f, CLIMAX_DEATH + 0.5f);         // le tertre de terre
            for (int k = 0; k < 9; k++) {
                float x = bossX() + (k - 4) * w * 0.22f, hgt = (18f + 14f * (1f - Math.abs(k - 4) / 4f)) * mound;
                rect(batch, k % 2 == 0 ? EARTH : hex("4a2e1a"), 1f, x - w * 0.12f, groundY() - 4f, w * 0.24f, hgt);
            }
            if (time > 3.2f) glow(batch, bossX(), groundY() + full * 0.5f, w * 1.2f, GRAVE, 0.25f * pulse(4f, 0.6f));
            flash(batch, GRAVE, CLIMAX_DEATH, 0.3f, 0.25f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Marionnettiste
    // -------------------------------------------------------------------------

    /**
     * « Le rideau se lève » : le grand rideau rouge du théâtre se lève sur lui ; un projecteur s'allume, des fils
     * descendent des cintres jusqu'à sa croix et l'agitent comme un pantin ; il chante, des notes s'envolent.
     */
    static final class PuppeteerPhase extends BossScene {
        private final TextureRegion drape, note;

        PuppeteerPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MARIONNETTISTE, 2);
            drape = region(curtain(), false);
            note = art("on", NOTE_COLORS, NOTE);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(1.3f + k * 0.2f)) rumble(0.08f, 3f);
            if (time > 1.5f && random.nextFloat() < delta * 6f) {                    // il chante : des notes s'envolent
                Particle n = place(note, bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossY() + bossHeight() * 0.2f,
                    (random.nextFloat() - 0.5f) * 200f, 160f + 100f * random.nextFloat(), worldHeight() * 0.05f / 7f, 1.6f);
                n.spin = (random.nextFloat() - 0.5f) * 90f;
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY(), 50, GOLD, 700f, 12f);
                for (int k = 0; k < 8; k++) {
                    place(note, bossX(), bossY(), MathUtils.cosDeg(k * 45f) * 500f, MathUtils.sinDeg(k * 45f) * 500f,
                        worldHeight() * 0.06f / 7f, 1.4f);
                }
            }
        }

        @Override
        protected float decorLight() { return 0.6f - 0.3f * progress(0.9f, 1.4f); }

        @Override
        protected void back(Batch batch) {
            float spot = progress(0.9f, 1.3f);
            additive(batch);                                                          // le cône du projecteur
            batch.setColor(CANDLE.r, CANDLE.g, CANDLE.b, 0.18f * spot);
            batch.draw(pixel, bossX() - bossWidth() * 0.9f, groundY(), bossWidth() * 1.8f, worldHeight());
            normal(batch);
            glow(batch, bossX(), bossY(), bossHeight() * 1.2f, CANDLE, 0.45f * spot);
            float jerk = progress(1.3f, CLIMAX_PHASE);
            poseTilt = 10f * MathUtils.sin(time * 14f) * jerk;
            poseY = 30f * Math.abs(MathUtils.sin(time * 7f)) * jerk;
        }

        @Override
        protected void front(Batch batch) {
            float drop = Interpolation.pow2Out.apply(progress(1.0f, 1.4f));
            for (int k = 0; k < 4; k++) {                                             // les fils jusqu'à sa croix
                float x = portraitX(new float[] {5f, 11f, 23f, 29f}[k] + 0.5f);
                line(batch, THREAD, 0.8f, x, worldHeight() + MARGIN, x, MathUtils.lerp(worldHeight(), portraitY(3f), drop), 3f);
            }
            float lift = Interpolation.pow2In.apply(progress(0.25f, 1.2f));           // le rideau qui se lève
            if (lift < 1f) {
                float h = (worldHeight() + 2f * MARGIN) * (1f - lift);
                batch.setColor(WHITE);
                batch.draw(drape, -MARGIN, worldHeight() + MARGIN - h, worldWidth() + 2f * MARGIN, worldHeight() + 2f * MARGIN);
            }
            float valance = worldHeight() * 0.08f;                                    // le lambrequin, toujours en haut
            batch.setColor(0.8f, 0.8f, 0.8f, 1f);
            batch.draw(drape, -MARGIN, worldHeight() - valance, worldWidth() + 2f * MARGIN, valance + MARGIN);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /**
     * « Les fils du Marionnettiste sont coupés » : de grands ciseaux coupent un à un les fils qui le tiennent ;
     * à chaque fil, il pend un peu plus de travers ; au dernier, il s'effondre comme un pantin, et le rideau
     * rouge tombe sur la scène.
     */
    static final class PuppeteerDeath extends BossScene {
        private final TextureRegion open, shut, drape;
        private static final float[] THREADS = {5f, 11f, 23f, 29f};

        PuppeteerDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MARIONNETTISTE, 0);
            open = art("osdrw", SCISSORS_COLORS, SCISSORS_OPEN);
            shut = art("osdrw", SCISSORS_COLORS, SCISSORS_SHUT);
            drape = region(curtain(), false);
        }

        private float cut(int k)     { return 0.6f + k * 0.45f; }
        private int order(int k)     { return new int[] {3, 0, 2, 1}[k]; }
        private float threadX(int k) { return portraitX(THREADS[k] + 0.5f); }
        private float snipY()        { return worldHeight() * 0.82f; }
        private int cutCount() {
            int n = 0;
            for (int k = 0; k < 4; k++) if (time >= cut(k)) n++;
            return n;
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(cut(k))) { sparks(threadX(order(k)), snipY(), 10, THREAD, 300f, 6f); rumble(0.06f, 3f); }
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 12f); sparks(bossX(), groundY() + 40f, 40, DUST, 600f, 12f); }
            if (at(4.4f)) rumble(0.3f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            float sag = cutCount() / 4f;
            float slump = Interpolation.bounceOut.apply(progress(cut(3), CLIMAX_DEATH));
            poseTilt = -6f * sag * MathUtils.sin(time * 5f) * (1f - slump) - 24f * slump;
            poseSink = 0.35f * slump;
            poseY = -20f * sag;
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.6f);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 4; k++) {
                int t = order(k);
                float x = threadX(t), y = portraitY(3f);
                if (time < cut(k)) line(batch, THREAD, 0.8f, x, worldHeight() + MARGIN, x, y, 3f);
                else {                                                                // le bout coupé remonte, le reste tombe
                    float fade = 1f - progress(cut(k), cut(k) + 0.8f);
                    line(batch, THREAD, 0.8f, x, worldHeight() + MARGIN, x, snipY() + 20f * progress(cut(k), cut(k) + 0.3f), 3f);
                    line(batch, THREAD, 0.8f * fade, x, snipY() - 240f * progress(cut(k), cut(k) + 0.6f), x, y, 3f);
                }
            }
            for (int k = 0; k < 4; k++) {                                             // les ciseaux vont d'un fil à l'autre
                if (time > cut(k) - 0.35f && time < cut(k) + 0.12f) {
                    float x = threadX(order(k));
                    TextureRegion frame = time < cut(k) - 0.05f ? open : shut;
                    float s = worldHeight() * 0.16f / SCISSORS_OPEN.length;
                    stretch(batch, frame, x, snipY() - s * 6f, s, s, 0f, WHITE, 1f);
                }
            }
            float curtain = Interpolation.bounceOut.apply(progress(3.6f, 4.4f));     // le rideau tombe
            if (curtain > 0f) {
                float h = (worldHeight() + 2f * MARGIN) * curtain;
                batch.setColor(WHITE);
                batch.draw(drape, -MARGIN, worldHeight() + MARGIN - h, worldWidth() + 2f * MARGIN, h);
            }
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.3f);
        }
    }
}
