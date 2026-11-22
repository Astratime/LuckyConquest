package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.ExpressPortraits;
import fr.astratime.lucky.assets.ExpressPortraits.Prop;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Rapide de Minuit (10 lieux, Astra 2026-11-20 ; redessinées le
 * 2026-11-22 pour que l'image montre la phrase : les manomètres qui s'affolent, la Locomotive qui saute des rails, le
 * Grand Chef qui pose sa toque et sa louche et s'en va, la voilette qui se lève, la Dame qui se fond dans la nuit en
 * papillons, les grappins de l'abordage, le Desperado qui saute du train par la porte du fourgon).
 */
final class ExpressBossScenes {

    private ExpressBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case LOCOMOTIVE -> new EnginePhase(kit);
            case GRAND_CHEF -> new ChefPhase(kit);
            case DAME_EN_NOIR -> new LadyPhase(kit);
            case DESPERADO -> new OutlawPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case LOCOMOTIVE -> new EngineDeath(kit);
            case GRAND_CHEF -> new ChefDeath(kit);
            case DAME_EN_NOIR -> new LadyDeath(kit);
            case DESPERADO -> new OutlawDeath(kit);
            default -> null;
        };
    }

    static final Color STEAM = hex("e8eef4"), FIRE = hex("ff7a1e"), EMBER = hex("ffb84a"), VIOLET = hex("b8a0ff");
    static final Color VENOM = hex("6ad86a"), FLASH = hex("fff2c0"), STEEL = hex("8a97a8"), MOON = hex("dfe8ff");
    static final Color NIGHT = hex("0c0c1e"), SLEEPER = hex("4a3020"), RAIL = hex("b8c0cc");
    static final String[] PAN = {".oooooo.....", "oddddddo....", "oddddddooooo", "oddddddo....", ".oooooo....."};

    /** Les yeux des portraits du Rapide (pixels 13-14 et 20-21, lignes 15-16) ; les hublots de la Locomotive. */
    static final float[] LEFT_EYE = {14f, 16f}, RIGHT_EYE = {21f, 16f};
    static final float[] PORTHOLE_LEFT = {11.5f, 19.5f}, PORTHOLE_RIGHT = {23.5f, 19.5f};

    static final Color[] MOTH_COLORS = {hex("d8c8ff"), hex("4a3a5e"), hex("9a86d8")};
    /** Un papillon de nuit, ailes levées puis baissées (w aile, b corps, s ombre de l'aile). */
    static final String[] MOTH_UP = {"w.....w", "ws...sw", "wwsbsww", ".wwbww.", "...b..."};
    static final String[] MOTH_DOWN = {"...b...", ".wwbww.", "wwsbsww", "ws...sw", "w.....w"};

    static final Color[] HOOK_COLORS = {hex("1a1018"), hex("b8c0cc"), hex("6a7484")};
    /** Un grappin d'abordage (o contour, s acier, d ombre). */
    static final String[] GRAPNEL = {
        "..oso..", "..oso..", "..oso..", "o.oso.o", "sooso os", "sssssss", ".osdso.", "..odo.."};

    static final Color[] BANDIT_COLORS = {hex("120c0a"), hex("3a2a1e"), hex("d8202c"), hex("d8a070"), hex("1a1018")};
    /** Un complice du Desperado, en silhouette (o contour, h chapeau et manteau, r foulard, s peau, e yeux). */
    static final String[] BANDIT = {
        "....hhhh....",
        "...hhhhhh...",
        ".hhhhhhhhhh.",
        "....ssss....",
        "....eses....",
        "....rrrr....",
        "...rrrrrr...",
        "..hhhhhhhh..",
        ".hhhhhhhhhh.",
        "hh.hhhhhh.hh",
        "h..hhhhhh..h",
        "...hhhhhh...",
        "...hh..hh...",
        "...hh..hh...",
        "..ohh..hho.."};

    // -------------------------------------------------------------------------
    // La Locomotive vivante
    // -------------------------------------------------------------------------

    /**
     * « Vapeur brûlante : Brûlure ! » : les trois manomètres de la cabine s'affolent, la chaudière rougit et le
     * foyer flambe, des jets de vapeur sortent de partout ; à l'apogée, la soupape lâche un grand panache et ses
     * hublots s'embrasent.
     */
    static final class EnginePhase extends BossScene {
        /** Les manomètres du décor (centres, en pixels du décor) et le foyer. */
        private static final float[][] GAUGES = {{140.5f, 46.5f}, {180.5f, 46.5f}, {220.5f, 46.5f}};
        private static final Color DIAL = hex("e0dac6"), NEEDLE = hex("b01c1c");

        EnginePhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.LOCOMOTIVE, 2); }

        private float pressure() { return Interpolation.pow2In.apply(progress(0.3f, CLIMAX_PHASE)); }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < CLIMAX_PHASE + 1.2f) {
                for (int n = 0; n < 2; n++) if (random.nextFloat() < delta * (8f + 30f * pressure())) {   // les jets de vapeur
                    boolean left = random.nextBoolean();
                    Particle jet = particle(portraitX(left ? 4f : 30f), portraitY(14f + 14f * random.nextFloat()), left ? 180f : 0f,
                        500f + 300f * random.nextFloat(), 0.8f, 40f + 40f * random.nextFloat());
                    jet.color.set(STEAM.r, STEAM.g, STEAM.b, 0.55f);
                    glows.add(jet);
                }
                if (random.nextFloat() < delta * 25f) {                                        // la cheminée qui fume
                    Particle puff = particle(portraitX(17f), portraitY(0f), 80f + 20f * random.nextFloat(), 300f, 1.4f, 60f + 40f * random.nextFloat());
                    puff.color.set(STEAM.r, STEAM.g, STEAM.b, 0.5f);
                    glows.add(puff);
                }
            }
            for (int k = 0; k < 6; k++) if (at(0.6f + k * 0.25f)) rumble(0.08f, 2f + k);
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 14f);
                for (int n = 0; n < 30; n++) {                                                  // la soupape lâche
                    Particle plume = particle(portraitX(17f), portraitY(0f), 60f + 60f * random.nextFloat(), 600f + 500f * random.nextFloat(),
                        1.4f, 90f + 80f * random.nextFloat());
                    plume.color.set(STEAM.r, STEAM.g, STEAM.b, 0.7f);
                    glows.add(plume);
                }
                sparks(bossX(), bossY(), 50, EMBER, 600f, 10f);
            }
        }

        @Override
        protected float decorLight() { return 0.55f + 0.2f * pressure() * pulse(9f, 0.6f); }

        @Override
        protected void back(Batch batch) {
            float glow = 0.4f + 0.6f * pressure();                                              // le foyer du décor flambe
            glow(batch, decorX(181f), decorY(117f), decorScale() * 90f * pulse(11f, 0.75f), FIRE, 0.55f * glow);
            for (int g = 0; g < GAUGES.length; g++) {                                         // les aiguilles qui s'affolent
                float cx = GAUGES[g][0], cy = GAUGES[g][1];
                decorRect(batch, DIAL, cx - 6.5f, cy - 6.5f, 13f, 13f);
                float angle = 210f - 240f * MathUtils.clamp(0.15f + 0.9f * pressure() + 0.08f * MathUtils.sin(time * (23f + 7f * g)), 0f, 1.05f);
                float x = decorX(cx), y = decorY(cy), length = decorScale() * 7.5f;
                line(batch, NEEDLE, 1f, x, y, x + MathUtils.cosDeg(angle) * length, y + MathUtils.sinDeg(angle) * length, decorScale() * 1.2f);
                rect(batch, hex("1a1018"), 1f, x - decorScale(), y - decorScale(), 2f * decorScale(), 2f * decorScale());
            }
            poseTint = 0.6f * pressure() * pulse(10f, 0.5f);
            poseTintColor.set(FIRE);
            poseX = time < CLIMAX_PHASE ? MathUtils.sin(time * 70f) * 5f * pressure() : 0f;
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f) * pulse(8f, 0.7f), FIRE, PORTHOLE_LEFT, PORTHOLE_RIGHT);
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), portraitY(28f), bossWidth() * 0.8f, FIRE, 0.4f * pulse(8f, 0.6f));   // la grille
            flash(batch, FIRE, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /**
     * « La Locomotive a déraillé » : elle fonce sur ses rails (les traverses défilent, les roues crachent des
     * étincelles), un rail se tord, son chasse-buffles (le vrai) s'arrache, elle bascule hors des rails et s'écrase
     * sur le flanc dans un nuage de vapeur ; ses hublots et sa lanterne s'éteignent.
     */
    static final class EngineDeath extends BossScene {
        private static final float TWIST = 1.2f, TEAR = 1.6f, CRASH = CLIMAX_DEATH, DARK = 3.6f;
        private final TextureRegion noCatcher, dark;
        private final Piece catcher;
        private float catcherX, catcherY;
        private boolean torn;

        EngineDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.LOCOMOTIVE, 0);
            int[][] full = ExpressPortraits.pixels(EnemyKind.LOCOMOTIVE);
            int[][] rest = ExpressPortraits.without(EnemyKind.LOCOMOTIVE, Prop.COWCATCHER);
            catcher = lift(full, rest);
            noCatcher = pixels(rest);
            dark = pixels(ExpressPortraits.without(EnemyKind.LOCOMOTIVE, Prop.COWCATCHER, Prop.LIGHTS));
        }

        @Override protected float textAt() { return CRASH + 0.5f; }
        @Override protected void restart() { torn = false; }

        private float run() { return time < TEAR ? time * 2.4f : TEAR * 2.4f + (1f - (float) Math.exp(-(time - TEAR) * 3f)) * 0.6f; }

        /** Un rail en perspective, de l'avant de l'écran jusqu'à ses roues ; le gauche se tord vers l'extérieur. */
        private float railX(int side, float k) {
            float near = bossX() + side * worldWidth() * 0.33f, far = bossX() + side * bossWidth() * 0.32f;
            float x = MathUtils.lerp(near, far, k);
            if (side < 0) x -= worldWidth() * 0.06f * progress(TWIST, TWIST + 0.3f) * MathUtils.sin(MathUtils.clamp((k - 0.55f) / 0.45f, 0f, 1f) * MathUtils.PI);
            return x;
        }
        private float railY(float k) { return MathUtils.lerp(-MARGIN, bossBottom(), (float) Math.pow(k, 0.7f)); }

        @Override
        protected void step(float delta) {
            if (time < TEAR + 0.6f) {                                                       // les roues qui crachent des étincelles
                for (int side = -1; side <= 1; side += 2) if (random.nextFloat() < delta * 30f) {
                    sparks(bossX() + side * bossWidth() * 0.3f, bossBottom() + 6f, 3, EMBER, 400f, 7f);
                }
            }
            if (at(0.2f)) rumble(TEAR, 3f);
            if (at(TWIST)) rumble(0.3f, 8f);
            if (at(TEAR)) {
                rumble(0.3f, 10f);
                if (catcher != null && !torn) {
                    torn = true;
                    catcherX = pieceX(catcher);
                    catcherY = pieceY(catcher);
                    wear(noCatcher);
                }
                sparks(bossX(), bossBottom() + 20f, 40, EMBER, 600f, 9f);
            }
            if (at(CRASH)) {
                rumble(0.8f, 16f);
                for (int n = 0; n < 40; n++) {
                    Particle cloud = particle(bossX() - bossWidth() * 0.6f + (random.nextFloat() - 0.5f) * bossWidth() * 1.6f, groundY() + 40f,
                        40f + 100f * random.nextFloat(), 200f + 300f * random.nextFloat(), 2.0f, 120f + 120f * random.nextFloat());
                    cloud.color.set(STEAM.r, STEAM.g, STEAM.b, 0.45f);
                    glows.add(cloud);
                }
                sparks(bossX() - bossWidth() * 0.5f, groundY() + 20f, 60, EMBER, 700f, 10f);
            }
            if (time > CRASH && time < 4.6f && random.nextFloat() < delta * 20f) {             // elle siffle encore
                Particle hiss = particle(bossX() - bossWidth() * 0.6f, groundY() + 60f, 80f + 40f * random.nextFloat(), 160f, 1.4f, 70f);
                hiss.color.set(STEAM.r, STEAM.g, STEAM.b, 0.35f * (1f - progress(CRASH, 4.6f)));
                glows.add(hiss);
            }
            if (at(DARK)) wear(dark);
        }

        @Override
        protected void back(Batch batch) {
            for (int s = 0; s < 9; s++) {                                                    // les traverses qui défilent
                float k = ((s + run()) % 9f) / 9f, y = railY(k);
                float half = MathUtils.lerp(worldWidth() * 0.42f, bossWidth() * 0.45f, k);
                rect(batch, SLEEPER, 1f, bossX() - half, y - 10f * (1f - k) - 3f, 2f * half, 20f * (1f - k) + 6f);
            }
            for (int side = -1; side <= 1; side += 2) {                                      // les deux rails
                for (int seg = 0; seg < 10; seg++) {
                    float k0 = seg / 10f, k1 = (seg + 1) / 10f;
                    float thick = MathUtils.lerp(18f, 6f, k0);
                    line(batch, hex("4a5260"), 1f, railX(side, k0), railY(k0) - thick * 0.4f, railX(side, k1), railY(k1) - thick * 0.4f, thick);
                    line(batch, RAIL, 1f, railX(side, k0), railY(k0), railX(side, k1), railY(k1), thick * 0.6f);
                }
            }
            float fall = Interpolation.pow2In.apply(progress(TEAR + 0.1f, CRASH));
            poseTilt = 78f * fall;                                                          // elle bascule sur le flanc gauche
            poseX = -bossWidth() * 0.35f * fall + (time < TEAR ? MathUtils.sin(time * 40f) * 4f : 0f);
            poseY = time < TEAR ? Math.abs(MathUtils.sin(time * 18f)) * 6f : -12f * fall;
            if (time > CRASH) poseTilt += 3f * MathUtils.sin((time - CRASH) * 30f) * (float) Math.exp(-(time - CRASH) * 6f);
            poseShadow = 0.35f * progress(CRASH, DARK);
        }

        @Override
        protected void front(Batch batch) {
            if (catcher != null) {
                if (!torn) attached(batch, catcher, 1f);
                else {                                                                        // le chasse-buffles glisse sur les rails, vers nous
                    float k = Interpolation.pow2Out.apply(progress(TEAR, TEAR + 1.4f));
                    float x = catcherX + worldWidth() * 0.12f * k, y = MathUtils.lerp(catcherY, groundY() * 0.35f, k);
                    draw(batch, catcher.region, x, y, bossScale() * (1f + 0.5f * k), -25f * k, WHITE, 1f);
                    if (k < 0.95f && random.nextFloat() < 0.5f) sparks(x, y - 10f, 2, EMBER, 300f, 6f);
                }
            }
            flash(batch, STEAM, CRASH, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Grand Chef
    // -------------------------------------------------------------------------

    /** « Coup de feu en cuisine » : les poêles s'enflamment autour de lui, ses couteaux tournoient, ses crocs sortent. */
    static final class ChefPhase extends BossScene {
        private final TextureRegion pan;

        ChefPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GRAND_CHEF, 2);
            pan = art("od", new Color[] {hex("1a1018"), hex("3a3a4e")}, PAN);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) if (at(0.5f + k * 0.4f)) { rumble(0.1f, 4f); sparks(panX(k), panY(), 30, FIRE, 500f, 12f); }
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 60, FIRE, 700f, 16f); }
        }

        private float panX(int k) { return bossX() + (k - 1) * bossWidth() * 0.9f; }
        private float panY()      { return bossBottom() + bossHeight() * 1.05f; }

        @Override
        protected void back(Batch batch) {
            for (int k = 0; k < 3; k++) {
                float lit = progress(0.5f + k * 0.4f, 0.7f + k * 0.4f);
                if (lit > 0f) glow(batch, panX(k), panY() + 60f, 260f * pulse(16f + k, 0.7f), FIRE, 0.7f * lit);
            }
            poseTint = 0.3f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(FIRE);
            poseY = 20f * gesture(1.8f, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 3; k++) {
                float show = progress(0.3f + k * 0.4f, 0.5f + k * 0.4f);
                if (show > 0f) draw(batch, pan, panX(k), panY(), fit(70f, 5), MathUtils.sin(time * 8f + k) * 8f, WHITE, show);
            }
            float knives = progress(1.2f, 1.6f);                                            // les couteaux qui tournoient
            for (int k = 0; k < 4 && knives > 0f; k++) {
                float angle = time * 400f + k * 90f, x = bossX() + MathUtils.cosDeg(angle) * bossWidth() * 0.8f;
                float y = bossY() + MathUtils.sinDeg(angle) * bossHeight() * 0.3f;
                line(batch, hex("dce4ee"), knives, x, y, x + MathUtils.cosDeg(angle + 90f) * 70f,
                    y + MathUtils.sinDeg(angle + 90f) * 70f, 10f);
            }
            flash(batch, FIRE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le Grand Chef a rendu son tablier » : il soupire, ôte sa toque (la vraie) et la pose sur la table du
     * wagon-restaurant, y dépose sa louche d'or, puis tourne les talons et quitte le wagon d'un pas lourd.
     */
    static final class ChefDeath extends BossScene {
        private static final float TOQUE_OFF = 0.7f, LADLE_OFF = 1.3f, LAND = 0.9f, LEAVE = 2.4f;
        private final TextureRegion noToque, bare;
        private final Piece toque, ladle;
        private float toqueX, toqueY, ladleX, ladleY;
        private boolean toqueFree, ladleFree;

        ChefDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GRAND_CHEF, 0);
            int[][] full = ExpressPortraits.pixels(EnemyKind.GRAND_CHEF);
            int[][] rest = ExpressPortraits.without(EnemyKind.GRAND_CHEF, Prop.TOQUE);
            int[][] none = ExpressPortraits.without(EnemyKind.GRAND_CHEF, Prop.TOQUE, Prop.LADLE);
            toque = lift(full, rest);
            ladle = lift(rest, none);
            noToque = pixels(rest);
            bare = pixels(none);
        }

        @Override protected float textAt() { return 3.1f; }
        @Override protected void restart() { toqueFree = ladleFree = false; }

        /** Où chaque objet se pose sur la nappe de la table de gauche (pixels du décor : la nappe commence ligne 119). */
        private float tableY()     { return decorY(119f); }

        @Override
        protected void step(float delta) {
            if (at(TOQUE_OFF + LAND) || at(LADLE_OFF + LAND)) { rumble(0.06f, 2f); sparks(decorX(time > LADLE_OFF ? 60f : 22f), tableY(), 10, WHITE, 200f, 6f); }
        }

        @Override
        protected void back(Batch batch) {
            float sigh = gesture(0.1f, 0.7f);
            poseStretch = -0.06f * sigh;
            float walk = progress(LEAVE, LEAVE + 1.6f);                                     // il s'en va, d'un pas lourd
            poseX = walk * (worldWidth() * 0.5f + bossWidth());
            poseY = walk > 0f && walk < 1f ? Math.abs(MathUtils.sin(walk * MathUtils.PI * 6f)) * 14f : 0f;
            poseTilt = walk > 0f && walk < 1f ? 3f * MathUtils.sin(walk * MathUtils.PI * 6f) : 0f;
            showBoss = walk < 1f;
        }

        /** Un objet qui quitte le portrait et va se poser sur la nappe, en ({@code px} pixels du décor). */
        private void carry(Batch batch, Piece piece, float off, float px, float startX, float startY, float spin, float lying) {
            float k = progress(off, off + LAND), arc = MathUtils.sin(k * MathUtils.PI) * worldHeight() * 0.12f;
            float x = MathUtils.lerp(startX, decorX(px), Interpolation.pow2.apply(k));
            float y = MathUtils.lerp(startY, tableY() + lying * bossScale() * 0.5f, Interpolation.pow2.apply(k)) + arc;
            draw(batch, piece.region, x, y, bossScale(), spin * k, WHITE, 1f);
        }

        @Override
        protected void front(Batch batch) {
            if (toque != null) {
                if (time < TOQUE_OFF) attached(batch, toque, 1f);
                else {
                    if (!toqueFree) { toqueFree = true; toqueX = pieceX(toque); toqueY = pieceY(toque); wear(noToque); }
                    carry(batch, toque, TOQUE_OFF, 22f, toqueX, toqueY, 0f, toque.height);
                }
            }
            if (ladle != null) {
                if (time < LADLE_OFF) attached(batch, ladle, 1f);
                else {
                    if (!ladleFree) { ladleFree = true; ladleX = pieceX(ladle); ladleY = pieceY(ladle); wear(bare); }
                    carry(batch, ladle, LADLE_OFF, 60f, ladleX, ladleY, 90f, ladle.width);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // La Dame en noir
    // -------------------------------------------------------------------------

    /**
     * « La voilette se lève : Venin ! » : les lampes du couloir faiblissent, sa voilette (la vraie) remonte lentement
     * sur son chapeau et découvre ses yeux, qui s'allument ; une brume verte monte du tapis.
     */
    static final class LadyPhase extends BossScene {
        private static final float LIFT = 0.6f, UP = 1.7f;
        private final TextureRegion bare;
        private final Piece veil;
        private boolean lifted;

        LadyPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DAME_EN_NOIR, 2);
            int[][] rest = ExpressPortraits.without(EnemyKind.DAME_EN_NOIR, Prop.VEIL);
            veil = lift(ExpressPortraits.pixels(EnemyKind.DAME_EN_NOIR), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { lifted = false; }

        @Override
        protected void step(float delta) {
            if (time > 1.0f && random.nextFloat() < delta * 25f) {
                Particle mist = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 2f, groundY(), 90f, 80f, 1.6f, 140f);
                mist.color.set(VENOM.r, VENOM.g, VENOM.b, 0.3f);
                glows.add(mist);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.4f, 8f); sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, VENOM, 600f, 12f); }
        }

        @Override
        protected float decorLight() { return 0.62f - 0.35f * progress(0.2f, 1.2f) + 0.08f * MathUtils.sin(time * 37f) * gesture(0.2f, 1.2f); }

        @Override
        protected void back(Batch batch) {
            poseY = 20f * progress(UP, CLIMAX_PHASE);
            poseTint = 0.25f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(VENOM);
        }

        @Override
        protected void front(Batch batch) {
            if (veil != null) {
                if (time < LIFT) attached(batch, veil, 1f);
                else {                                                                        // la voilette remonte et se replie sur le chapeau
                    if (!lifted) { lifted = true; wear(bare); }
                    float k = Interpolation.pow2.apply(progress(LIFT, UP));
                    float s = bossScale() * poseScale;
                    float x = pieceX(veil), y = pieceY(veil) + 5.5f * s * k + veil.height * s * 0.5f * 0.55f * k;
                    glow(batch, x, y, veil.width * s * 1.4f, VIOLET, 0.35f * gesture(LIFT, UP + 0.3f));   // on la voit se soulever
                    stretch(batch, veil.region, x, y, s, s * (1f - 0.55f * k), 0f, WHITE, 1f);
                }
            }
            eyes(batch, progress(UP - 0.2f, CLIMAX_PHASE) * pulse(7f, 0.6f), VIOLET, LEFT_EYE, RIGHT_EYE);
            flash(batch, VENOM, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /**
     * « La Dame en Noir s'efface dans la nuit » : les lampes du wagon-lit s'éteignent une à une, elle se fond dans
     * le noir (seuls ses yeux luisent encore), la rose de son chapeau (la vraie) tombe sur le tapis ; puis elle se
     * défait en papillons de nuit qui s'envolent par la fenêtre.
     */
    static final class LadyDeath extends BossScene {
        private static final float ROSE_OFF = 0.9f, ROSE_LAND = 1.7f, BURST = CLIMAX_DEATH, HOME = 3.0f;
        private final TextureRegion bare, mothUp, mothDown;
        private final Piece rose;
        private final ManorBossScenes.Flock flock = new ManorBossScenes.Flock(60);
        private float roseX, roseY;
        private boolean fallen;

        LadyDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DAME_EN_NOIR, 0);
            int[][] rest = ExpressPortraits.without(EnemyKind.DAME_EN_NOIR, Prop.ROSE);
            rose = lift(ExpressPortraits.pixels(EnemyKind.DAME_EN_NOIR), rest);
            bare = pixels(rest);
            mothUp = art("wbs", MOTH_COLORS, MOTH_UP);
            mothDown = art("wbs", MOTH_COLORS, MOTH_DOWN);
        }

        @Override protected void restart() { flock.clear(); fallen = false; }

        @Override
        protected void step(float delta) {
            if (at(BURST)) {
                rumble(0.3f, 5f);
                for (int b = 0; b < 44; b++) {
                    float px = bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 0.8f;
                    float py = bossBottom() + bossHeight() * (0.1f + 0.85f * random.nextFloat());
                    float dir = random.nextFloat() * 360f, speed = 150f + 300f * random.nextFloat();
                    flock.add(px, py, MathUtils.cosDeg(dir) * speed, MathUtils.sinDeg(dir) * speed, bossScale() * (0.7f + 0.5f * random.nextFloat()),
                        random.nextFloat() * 6f);
                }
            }
            for (int b = 0; b < flock.count; b++) {                                          // elles s'en vont par la fenêtre, une à une
                if (at(HOME + b * 0.03f)) flock.sendHome(b, decorX(180f) + (random.nextFloat() - 0.5f) * decorScale() * 30f, decorY(56f));
            }
            flock.update(delta, time);
        }

        @Override
        protected float decorLight() {
            float lamps = 3f - Math.min(3f, (float) Math.floor(Math.max(0f, time - 0.3f) / 0.45f));   // trois paliers
            return 0.12f + 0.5f * lamps / 3f;
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, NIGHT, 0.5f * progress(0.3f, BURST));
            poseShadow = 0.85f * progress(0.3f, BURST - 0.3f);
            poseTint = 0.15f * progress(1.2f, BURST);
            poseTintColor.set(VIOLET);
            showBoss = time < BURST;
        }

        @Override
        protected void front(Batch batch) {
            if (rose != null) {
                if (time < ROSE_OFF) attached(batch, rose, 1f);
                else {                                                                         // la rose tombe sur le tapis
                    if (!fallen) { fallen = true; roseX = pieceX(rose); roseY = pieceY(rose); wear(bare); }
                    float k = progress(ROSE_OFF, ROSE_LAND);
                    float x = MathUtils.lerp(roseX, bossX() + bossWidth() * 0.55f, k) + MathUtils.sin(k * MathUtils.PI * 3f) * 30f * (1f - k);
                    float y = MathUtils.lerp(roseY, groundY() + 10f, Interpolation.pow2In.apply(k));
                    draw(batch, rose.region, x, y, bossScale() * 1.2f, 140f * k, WHITE, 1f);
                }
            }
            if (time < BURST + 0.3f) eyes(batch, progress(0.8f, 1.6f) * (1f - progress(BURST, BURST + 0.3f)) * pulse(5f, 0.7f), VIOLET, LEFT_EYE, RIGHT_EYE);
            flock.draw(this, batch, mothUp, mothDown, 1f);
            if (time > BURST) for (int b = 0; b < flock.count; b++) {
                if (!flock.arrived(b)) glow(batch, flock.x[b], flock.y[b], bossScale() * 6f, VIOLET, 0.25f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Le Desperado
    // -------------------------------------------------------------------------

    /**
     * « Tout le monde à terre : Abordage ! » : il tire trois coups vers le toit du fourgon (le clair de lune passe
     * par les trous), deux grappins d'abordage se plantent en haut des parois et ses deux complices descendent en
     * se balançant au bout de leurs cordes.
     */
    static final class OutlawPhase extends BossScene {
        private static final float[] SHOTS = {0.5f, 0.85f, 1.2f};
        private static final float HOOKS = 1.35f, SWING = 1.6f, LAND = CLIMAX_PHASE;
        private final TextureRegion grapnel, bandit;

        OutlawPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DESPERADO, 2);
            grapnel = art("osd", HOOK_COLORS, GRAPNEL);
            bandit = art("ohrse", BANDIT_COLORS, BANDIT);
        }

        private float muzzleX()     { return portraitX(32.5f); }
        private float muzzleY()     { return portraitY(12f); }
        private float holeX(int k)  { return bossX() + bossWidth() * (0.9f + 0.35f * k); }
        private float hookX(int s)  { return bossX() + s * worldWidth() * 0.36f; }
        private float hookY()       { return worldHeight() * 0.93f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < SHOTS.length; k++) {
                if (at(SHOTS[k])) { rumble(0.12f, 8f); sparks(muzzleX(), muzzleY(), 20, FLASH, 600f, 10f); sparks(holeX(k), worldHeight(), 14, STEEL, 300f, 7f); }
            }
            for (int s = -1; s <= 1; s += 2) if (at(HOOKS + (s + 1) * 0.08f)) { rumble(0.1f, 5f); sparks(hookX(s), hookY(), 16, STEEL, 300f, 7f); }
            if (at(LAND)) { rumble(0.5f, 12f); sparks(bossX(), groundY(), 50, hex("8a7a6a"), 500f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            for (int k = 0; k < SHOTS.length; k++) {                                          // les rais de lune par les trous du toit
                float open = progress(SHOTS[k], SHOTS[k] + 0.1f);
                if (open <= 0f) continue;
                float x = holeX(k);
                line(batch, MOON, 0.12f * open, x, worldHeight(), x - worldHeight() * 0.25f, groundY(), 26f);
                rect(batch, hex("05050a"), 1f, x - 7f, worldHeight() - 12f, 14f, 14f);
                glow(batch, x, worldHeight() - 6f, 50f, MOON, 0.6f * open);
            }
            float recoil = 0f;
            for (float shot : SHOTS) recoil = Math.max(recoil, gesture(shot - 0.02f, shot + 0.18f));
            poseTilt = 4f * recoil;
            poseY = 20f * progress(1.8f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            float scale = worldHeight() * 0.07f / GRAPNEL.length;
            for (int s = -1; s <= 1; s += 2) {
                float at = HOOKS + (s + 1) * 0.08f, fly = Interpolation.pow2Out.apply(progress(at - 0.25f, at));
                if (fly <= 0f) continue;
                float fromX = bossX() + s * worldWidth() * 0.7f, fromY = worldHeight() * 0.4f;
                float x = MathUtils.lerp(fromX, hookX(s), fly), y = MathUtils.lerp(fromY, hookY(), fly);
                float swing = Interpolation.sineOut.apply(progress(SWING + (s + 1) * 0.05f, LAND));   // le complice au bout de la corde
                float bx = MathUtils.lerp(bossX() + s * worldWidth() * 0.62f, bossX() + s * bossWidth() * 1.15f, swing);
                float by = swing <= 0f ? y : MathUtils.lerp(worldHeight() * 0.55f, groundY() + worldHeight() * 0.11f, swing)
                    - MathUtils.sin(swing * MathUtils.PI) * worldHeight() * 0.12f;
                float endX = swing > 0f ? bx : fromX, endY = swing > 0f ? by + worldHeight() * 0.1f : fromY;
                line(batch, hex("8a6a3a"), 1f, x, y - 10f, endX, endY, 4f);                     // la corde
                draw(batch, grapnel, x, y, scale, s * 20f, WHITE, 1f);
                if (swing > 0f) {
                    float bs = worldHeight() * 0.2f / BANDIT.length;
                    draw(batch, bandit, bx, by, bs * (s < 0 ? 1f : 1f), -s * 25f * (1f - swing), WHITE, 1f);
                }
            }
            for (float shot : SHOTS) flash(batch, FLASH, shot, 0.08f, 0.25f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le Desperado descend du train » : une balle perdue fait sauter son chapeau (le vrai, qui retombe sur le
     * plancher du fourgon) ; il fait glisser la porte du fourgon, la nuit défile dehors à toute allure ; il recule
     * jusqu'au bord et saute du train en marche.
     */
    static final class OutlawDeath extends BossScene {
        private static final float SHOT = 0.6f, HAT_LAND = 1.4f, DOOR = 1.0f, OPEN = 1.6f, JUMP = CLIMAX_DEATH, GONE = 3.2f;
        private final TextureRegion bare;
        private final Piece hat;
        private float hatX, hatY;
        private boolean hatOff;

        OutlawDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DESPERADO, 0);
            int[][] rest = ExpressPortraits.without(EnemyKind.DESPERADO, Prop.HAT);
            hat = lift(ExpressPortraits.pixels(EnemyKind.DESPERADO), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { hatOff = false; }

        private float doorX()   { return bossX() + bossWidth() * 1.45f; }
        private float doorW()   { return worldWidth() * 0.24f; }
        private float doorBottom() { return bossBottom(); }
        private float doorH()   { return worldHeight() * 0.56f; }
        private float opened()  { return Interpolation.pow2.apply(progress(DOOR, OPEN)); }

        @Override
        protected void step(float delta) {
            if (at(SHOT)) rumble(0.15f, 6f);
            if (at(HAT_LAND)) rumble(0.05f, 2f);
            if (at(DOOR)) rumble(0.6f, 3f);
            if (at(JUMP)) rumble(0.3f, 8f);
            if (time > OPEN && random.nextFloat() < delta * 30f) {                            // le vent de la nuit qui s'engouffre
                Particle wind = particle(doorX() + doorW() * 0.5f, doorBottom() + doorH() * random.nextFloat(), 180f, 900f, 0.5f, 12f);
                wind.color.set(MOON.r, MOON.g, MOON.b, 0.5f);
                glows.add(wind);
            }
        }

        @Override
        protected void back(Batch batch) {
            float open = opened(), w = doorW() * open, x = doorX() - doorW() / 2f, y = doorBottom(), h = doorH();
            rect(batch, hex("05050a"), 1f, x - 10f, y - 10f, doorW() + 20f, h + 20f);       // l'encadrement de la porte
            if (open > 0f) {                                                                 // dehors, la nuit défile
                rect(batch, hex("101634"), 1f, x, y, w, h);
                glow(batch, x + w * 0.7f, y + h * 0.8f, 60f, MOON, 0.8f * open);
                for (int k = 0; k < 4; k++) {
                    float px = x + w - ((time * 1400f + k * doorW() * 0.4f) % (doorW() * 1.6f));
                    if (px > x && px < x + w) {
                        rect(batch, hex("1a1208"), 1f, px - 5f, y, 10f, h * 0.7f);              // un poteau télégraphique
                        rect(batch, hex("1a1208"), 1f, px - 22f, y + h * 0.66f, 44f, 6f);
                    }
                }
                for (int k = 0; k < 6; k++) {
                    float hx = x + w - ((time * 500f + k * 140f) % (w + 200f));
                    if (hx > x - 60f && hx < x + w) rect(batch, hex("0a1a12"), 1f, Math.max(x, hx), y, Math.min(120f, x + w - Math.max(x, hx)), h * (0.18f + 0.05f * (k % 3)));
                }
            }
            rect(batch, hex("3a3e48"), 1f, x + w, y, doorW() - w, h);                         // le battant qui glisse
            rect(batch, hex("2a2e36"), 1f, x + w, y, 8f, h);
            float back = Interpolation.pow2.apply(progress(1.5f, JUMP - 0.1f));         // il recule jusqu'à la porte
            float jump = progress(JUMP, GONE);
            poseX = (doorX() - bossX()) * back + doorW() * 0.25f * jump;
            poseY = MathUtils.sin(Math.min(1f, jump * 2.2f) * MathUtils.PI) * worldHeight() * 0.05f * (jump < 0.45f ? 1f : 0f);
            poseSink = Interpolation.pow2In.apply(progress(JUMP + 0.2f, GONE));               // il tombe hors du train
            poseTilt = -12f * jump;
            showBoss = time < GONE;
        }

        @Override
        protected void front(Batch batch) {
            if (hat != null) {
                if (time < SHOT) attached(batch, hat, 1f);
                else {                                                                         // le chapeau saute et retombe sur le plancher
                    if (!hatOff) { hatOff = true; hatX = pieceX(hat); hatY = pieceY(hat); wear(bare); }
                    float k = progress(SHOT, HAT_LAND);
                    float x = MathUtils.lerp(hatX, bossX() - bossWidth() * 0.9f, k);
                    float y = MathUtils.lerp(hatY, groundY() + hat.height * bossScale() * 0.4f, k) + MathUtils.sin(k * MathUtils.PI) * worldHeight() * 0.18f;
                    draw(batch, hat.region, x, y, bossScale(), k < 1f ? 540f * k : 0f, WHITE, 1f);
                }
            }
            rect(batch, hex("26262e"), 1f, doorX() - doorW() / 2f - 10f, doorBottom() - 18f, doorW() + 20f, 18f);   // le seuil de la porte
            flash(batch, FLASH, SHOT, 0.1f, 0.3f);
        }
    }
}
