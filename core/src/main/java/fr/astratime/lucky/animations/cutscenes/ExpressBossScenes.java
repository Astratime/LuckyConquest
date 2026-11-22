package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort des quatre rois du Rapide de Minuit (10 lieux, Astra 2026-11-20). */
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
    static final Color VENOM = hex("6ad86a"), FLASH = hex("fff2c0");
    static final String[] CLOCHE = {
        ".....oo.....", "....owwo....", "...owwwso...", "..owwwwwso..", ".owwwwwwwso.", ".owwwwwwwwso", "oooooooooooo"};
    static final String[] ROSE = {"..rr..", ".rRrr.", "rrRRrr", ".rrrr.", "..gg..", "...g..", "..gg.."};
    static final String[] PAN = {".oooooo.....", "oddddddo....", "oddddddooooo", "oddddddo....", ".oooooo....."};

    // -------------------------------------------------------------------------
    // La Locomotive vivante
    // -------------------------------------------------------------------------

    /** La chaudière rougit, la pression monte (le manomètre s'affole), des jets de vapeur partout ; elle s'embrase. */
    static final class EnginePhase extends BossScene {
        private final TextureRegion gauge;

        EnginePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.LOCOMOTIVE, 2);
            gauge = symbol(EnemySymbol.PRESSURE);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.5f && time < CLIMAX_PHASE && random.nextFloat() < delta * 20f) {      // les jets de vapeur
                float side = random.nextBoolean() ? -1f : 1f;
                sparks(bossX() + side * bossWidth() * 0.45f, bossBottom() + bossHeight() * random.nextFloat(), 10, STEAM,
                    500f, 18f);
            }
            for (int k = 0; k < 6; k++) if (at(0.6f + k * 0.25f)) rumble(0.08f, 2f + k);
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 14f);
                sparks(bossX(), bossY(), 70, FIRE, 800f, 16f);
                sparks(bossX(), bossY(), 40, EMBER, 600f, 10f);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseTint = 0.6f * progress(0.4f, CLIMAX_PHASE) * pulse(10f, 0.5f);
            poseTintColor.set(FIRE);
            poseX = time < CLIMAX_PHASE ? MathUtils.sin(time * 70f) * 5f * progress(0.4f, CLIMAX_PHASE) : 0f;
        }

        @Override
        protected void front(Batch batch) {
            if (gauge != null && time < CLIMAX_PHASE + 0.4f) {                               // le manomètre qui s'affole
                float show = progress(0.3f, 0.6f) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f));
                float wobble = MathUtils.sin(time * 40f) * 6f * progress(1.2f, CLIMAX_PHASE);
                draw(batch, gauge, bossX() + bossWidth() * 0.7f, bossY() + bossHeight() * 0.25f, iconScale(0.14f), wobble,
                    WHITE, show);
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), bossY(), bossHeight() * 1.5f, FIRE, 0.35f * pulse(8f, 0.6f));
            flash(batch, FIRE, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Elle déraille : elle penche, crisse, s'enfonce dans un nuage de vapeur ; il reste une roue qui roule et des pièces. */
    static final class EngineDeath extends BossScene {
        EngineDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.LOCOMOTIVE, 0); }

        @Override
        protected void step(float delta) {
            if (time > 0.4f && time < CLIMAX_DEATH) sparks(bossX() + bossWidth() * 0.4f, groundY(), 3, EMBER, 500f, 8f);
            if (at(0.4f)) rumble(1.6f, 5f);
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                sparks(bossX(), groundY() + 80f, 60, STEAM, 700f, 24f);
                spray(coin, bossX(), groundY() + 100f, 24, 40f, 140f, 900f, coinScale(0.04f), 1400f, 1.6f);
            }
            if (time > CLIMAX_DEATH && time < 4f && random.nextFloat() < 0.5f) {
                Particle cloud = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.4f, groundY() + 60f, 90f,
                    120f, 1.6f, 160f);
                cloud.color.set(0.9f, 0.93f, 0.97f, 0.4f);
                glows.add(cloud);
            }
        }

        @Override
        protected void back(Batch batch) {
            float tip = Interpolation.pow2In.apply(progress(0.4f, CLIMAX_DEATH));
            poseTilt = -25f * tip;
            poseX = 120f * tip;
            poseSink = progress(CLIMAX_DEATH, 3.6f);
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.4f);
        }

        @Override
        protected void front(Batch batch) { flash(batch, STEAM, CLIMAX_DEATH, 0.3f, 0.5f); }
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

    /** La cloche d'argent tombe sur lui ; quand elle se relève, il n'y a plus qu'un plat de pièces fumant. */
    static final class ChefDeath extends BossScene {
        private final TextureRegion cloche;

        ChefDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GRAND_CHEF, 0);
            cloche = art("ows", new Color[] {hex("3a3a4e"), hex("dce4ee"), hex("8a97a8")}, CLOCHE);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 12f); sparks(bossX(), groundY() + 40f, 40, WHITE, 600f, 10f); }
            if (at(3.6f)) spray(coin, bossX(), groundY() + 80f, 20, 60f, 120f, 700f, coinScale(0.04f), 1400f, 1.4f);
        }

        private float clocheY() {
            float fall = Interpolation.bounceOut.apply(progress(1.4f, CLIMAX_DEATH));
            float rise = Interpolation.pow2Out.apply(progress(3.4f, 4.0f));
            float down = bossBottom() - 40f, up = worldHeight() + 200f;
            return MathUtils.lerp(MathUtils.lerp(up, down, fall), up, rise);
        }

        @Override
        protected void back(Batch batch) {
            poseStretch = -0.1f * gesture(0.4f, 1.4f);                                       // il chancelle
            poseTilt = 6f * MathUtils.sin(time * 6f) * (1f - progress(1.2f, 1.4f));
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            if (time >= 3.4f) {                                                              // le plat fumant
                rect(batch, hex("dce4ee"), 1f, bossX() - 220f, groundY() + 10f, 440f, 24f);
                for (int k = 0; k < 5; k++) draw(batch, coin, bossX() - 120f + k * 60f, groundY() + 60f, coinScale(0.05f), 0f,
                    WHITE, progress(3.5f, 3.8f));
                glow(batch, bossX(), groundY() + 160f, 220f, STEAM, 0.3f * pulse(5f, 0.5f));
            }
            draw(batch, cloche, bossX(), clocheY() + bossHeight() * 0.5f, fit(bossHeight() * 1.2f, 7), 0f, WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // La Dame en noir
    // -------------------------------------------------------------------------

    /** « La voilette se lève » : les lampes du couloir meurent, une brume verte monte, ses yeux s'allument. */
    static final class LadyPhase extends BossScene {
        private final TextureRegion venom;

        LadyPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DAME_EN_NOIR, 2);
            venom = symbol(EnemySymbol.VENOM);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.8f && random.nextFloat() < delta * 25f) {
                Particle mist = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 2f, groundY(), 90f, 80f, 1.6f, 140f);
                mist.color.set(VENOM.r, VENOM.g, VENOM.b, 0.3f);
                glows.add(mist);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.4f, 8f); sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, VENOM, 600f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.6f * progress(0.3f, 1.0f));
            poseY = 30f * progress(1.2f, CLIMAX_PHASE);
            poseTint = 0.25f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(VENOM);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.8f, CLIMAX_PHASE) * pulse(7f, 0.6f), VIOLET, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
            if (venom != null && time >= CLIMAX_PHASE) {
                for (int k = 0; k < 2; k++) draw(batch, venom, bossX() + (k == 0 ? -1f : 1f) * bossWidth() * 0.7f, bossY(),
                    iconScale(0.1f), 0f, WHITE, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            }
            flash(batch, VENOM, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /** Elle s'efface en papillons de nuit qui s'envolent par la fenêtre ; il ne reste que sa rose, sur le tapis. */
    static final class LadyDeath extends BossScene {
        private final TextureRegion rose;

        LadyDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DAME_EN_NOIR, 0);
            rose = art("rRg", new Color[] {hex("8a1a1a"), hex("d8202c"), hex("2a8a3a")}, ROSE);
        }

        @Override
        protected void step(float delta) {
            if (time > 1.2f && time < 3.4f && random.nextFloat() < delta * 40f) {             // les papillons de nuit
                Particle moth = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight()
                    * random.nextFloat(), 40f + 100f * random.nextFloat(), 300f, 1.6f, 14f);
                moth.color.set(VIOLET);
                glows.add(moth);
            }
            if (at(CLIMAX_DEATH)) rumble(0.4f, 6f);
        }

        @Override
        protected void back(Batch batch) {
            poseAlpha = 1f - progress(1.2f, 3.2f);
            poseTint = 0.4f * progress(1.0f, 2.0f);
            poseTintColor.set(VIOLET);
            showBoss = time < 3.3f;
        }

        @Override
        protected void front(Batch batch) {
            float drop = Interpolation.bounceOut.apply(progress(2.8f, 3.4f));
            if (drop > 0f) draw(batch, rose, bossX(), MathUtils.lerp(bossY(), groundY() + 30f, drop), fit(90f, 7), 70f * drop,
                WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Desperado
    // -------------------------------------------------------------------------

    /** « Tout le monde à terre ! » : il tire en l'air, encore et encore, et lance ses grappins (Abordage). */
    static final class OutlawPhase extends BossScene {
        private final TextureRegion boarding;

        OutlawPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DESPERADO, 2);
            boarding = symbol(EnemySymbol.BOARDING);
        }

        private float gunX() { return portraitX(30f); }
        private float gunY() { return portraitY(12f); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) {
                if (at(0.6f + k * 0.4f)) { rumble(0.12f, 8f); sparks(gunX(), gunY(), 20, FLASH, 600f, 10f); }
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 12f);
                spray(boarding, bossX(), bossY(), 6, 20f, 160f, 900f, iconScale(0.06f), 1200f, 1.2f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float recoil = 0f;
            for (int k = 0; k < 4; k++) recoil = Math.max(recoil, gesture(0.6f + k * 0.4f, 0.8f + k * 0.4f));
            poseTilt = 4f * recoil;
            poseY = 30f * progress(1.8f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 4; k++) flash(batch, FLASH, 0.6f + k * 0.4f, 0.08f, 0.25f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Une balle perdue fait sauter son chapeau ; il tombe à genoux et son sac d'or crève : une pluie de pièces. */
    static final class OutlawDeath extends BossScene {
        /** Le chapeau du portrait : ses lignes du haut (bords compris), et la tête dessous. */
        private static final int HAT_ROWS = 12, SCALP_LEFT = 11, SCALP_RIGHT = 23;
        private static final Color HAIR = hex("3a2a1e");
        private static final float SHOT = 0.6f;
        private final TextureRegion hat;
        /** Le portrait entier (chapeau compris) : sa taille à l'écran ne change pas quand le chapeau part. */
        private final int fullRegionY, fullRows;
        private boolean hatOff;

        OutlawDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DESPERADO, 0);
            hat = new TextureRegion(boss, 0, 0, boss.getRegionWidth(), HAT_ROWS);
            fullRegionY = boss.getRegionY();
            fullRows = boss.getRegionHeight();
        }

        @Override
        protected void restart() {
            if (hatOff) boss.setRegion(boss.getRegionX(), fullRegionY, boss.getRegionWidth(), fullRows);
            hatOff = false;
        }

        /** Sans son chapeau, le portrait a moins de lignes : l'échelle reste celle du portrait entier (il ne grandit pas). */
        @Override
        protected float bossScale() { return super.bossScale() * boss.getRegionHeight() / fullRows; }

        @Override
        protected void step(float delta) {
            if (at(SHOT)) {
                rumble(0.15f, 6f);
                // son propre chapeau s'envole : on le retire du portrait (le bas du portrait ne bouge pas)
                place(hat, bossX(), portraitY(HAT_ROWS / 2f), 200f, 900f, bossScale(), 2.2f).gravity = 1300f;
                if (!hatOff) {
                    boss.setRegion(boss.getRegionX(), boss.getRegionY() + HAT_ROWS, boss.getRegionWidth(),
                        boss.getRegionHeight() - HAT_ROWS);
                    hatOff = true;
                }
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.6f, 12f);
                spray(coin, bossX(), bossY(), 40, 30f, 150f, 1100f, coinScale(0.05f), 1500f, 1.8f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float kneel = Interpolation.pow2In.apply(progress(1.2f, CLIMAX_DEATH));
            poseSink = 0.25f * kneel;
            poseY = -40f * kneel;
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.6f);
            poseAlpha = 1f - progress(3.6f, 4.4f);
        }

        @Override
        protected void front(Batch batch) {
            if (hatOff) {                                               // le haut de sa tête, sans le chapeau
                float scale = bossScale() * poseScale, full = boss.getRegionHeight();
                float rows = Math.max(1, Math.round(full * (1f - MathUtils.clamp(poseSink, 0f, 1f))));
                float x0 = bossX() + poseX - boss.getRegionWidth() * scale / 2f;
                float y0 = bossBottom() + poseY - (full - rows) * scale;
                cap(batch, x0, y0, scale, -1, SCALP_LEFT, SCALP_RIGHT, HAIR, 1f - poseShadow, poseAlpha);
            }
            flash(batch, FLASH, SHOT, 0.1f, 0.3f);
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.35f);
        }
    }
}
