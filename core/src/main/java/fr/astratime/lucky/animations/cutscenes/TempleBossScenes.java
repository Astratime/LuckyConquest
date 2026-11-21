package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort des quatre rois du Temple du Destin (10 lieux, Astra 2026-11-20). */
final class TempleBossScenes {

    private TempleBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MAITRE_CLOCHES -> new BellPhase(kit);
            case TISSEUSE -> new WeaverPhase(kit);
            case SEIGNEUR_DES -> new DicePhase(kit);
            case IDOLE_OR -> new IdolPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MAITRE_CLOCHES -> new BellDeath(kit);
            case TISSEUSE -> new WeaverDeath(kit);
            case SEIGNEUR_DES -> new DiceDeath(kit);
            case IDOLE_OR -> new IdolDeath(kit);
            default -> null;
        };
    }

    static final Color BRONZE = hex("c87a3a"), AMBER = hex("ffd27a"), THREAD = hex("fff2a0"), RUBY = hex("e8323a");
    static final Color IVORY = hex("f6f0e2"), DUST = hex("c8b8a0"), STONE = hex("a89a88");
    /** Un dé d'ivoire, face six. */
    static final String[] DIE = {
        "ooooooooo", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "ooooooooo"};
    /** Un éclat de bronze de la cloche brisée. */
    static final String[] SHARD = {".bb.", "bbBb", ".bB."};

    /** Les phases volent le symbole d'un autre ennemi : son image s'affiche à l'apogée, de part et d'autre du roi. */
    private static void stolen(BossScene scene, Batch batch, TextureRegion icon, float alpha) {
        if (icon == null || alpha <= 0f) return;
        for (int k = 0; k < 2; k++) {
            scene.draw(batch, icon, scene.bossX() + (k == 0 ? -1f : 1f) * scene.bossWidth() * 0.75f, scene.bossY(),
                scene.iconScale(0.1f), 0f, BossScene.WHITE, alpha);
        }
    }

    // -------------------------------------------------------------------------
    // Le Maître des cloches
    // -------------------------------------------------------------------------

    /** « Le bourdon te saoule » : il sonne le grand bourdon, l'écran tangue, les ondes de bronze s'enchaînent. */
    static final class BellPhase extends BossScene {
        private final TextureRegion drunk;

        BellPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MAITRE_CLOCHES, 2);
            drunk = symbol(kind.kingPhaseSymbol());
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(0.5f + k * 0.4f)) { rumble(0.2f, 4f + 2f * k); sparks(bossX(), portraitY(4f), 16, AMBER, 500f, 10f); }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 14f); sparks(bossX(), bossY(), 60, BRONZE, 800f, 16f); }
        }

        @Override
        protected void back(Batch batch) {
            poseTilt = 8f * MathUtils.sin(time * 5f) * progress(0.5f, CLIMAX_PHASE);
            poseTint = 0.3f * progress(1.2f, CLIMAX_PHASE);
            poseTintColor.set(BRONZE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 4; k++) wave(batch, 0.5f + k * 0.4f, 1.0f, bossX(), portraitY(4f), AMBER, bossHeight() * (1.6f + 0.4f * k));
            wave(batch, CLIMAX_PHASE, 1.2f, bossX(), bossY(), BRONZE, bossHeight() * 3f);
            stolen(this, batch, drunk, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            flash(batch, AMBER, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Son bourdon se fend : un dernier coup sourd, la cloche éclate en morceaux de bronze, il s'efface dans la poussière. */
    static final class BellDeath extends BossScene {
        private final TextureRegion shard;

        BellDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MAITRE_CLOCHES, 0);
            shard = art("bB", new Color[] {BRONZE, hex("8a4a1e")}, SHARD);
        }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) rumble(0.3f, 6f);
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                spray(shard, bossX(), portraitY(4f), 22, 20f, 160f, 900f, fit(50f, 3), 1300f, 1.6f);
                spray(coin, bossX(), bossY(), 18, 40f, 140f, 800f, coinScale(0.04f), 1400f, 1.6f);
            }
            if (time > CLIMAX_DEATH && time < 4f && random.nextFloat() < 0.4f) {
                Particle dust = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), groundY() + 40f, 90f, 80f, 1.4f, 120f);
                dust.color.set(DUST.r, DUST.g, DUST.b, 0.35f);
                glows.add(dust);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseX = time < CLIMAX_DEATH ? MathUtils.sin(time * 60f) * 4f * progress(0.6f, CLIMAX_DEATH) : 0f;
            poseSink = 0.6f * progress(CLIMAX_DEATH, 3.8f);
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.4f);
            poseAlpha = 1f - progress(3.4f, 4.2f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {                                    // la fêlure qui court sur le bourdon
                crack(batch, bossX(), portraitY(2f), -80f, bossWidth() * 0.4f * progress(0.6f, CLIMAX_DEATH), 7L, hex("3a1a0a"),
                    1f, 6f);
            }
            wave(batch, 0.6f, 1.4f, bossX(), portraitY(4f), AMBER, bossHeight() * 2f);
            flash(batch, AMBER, CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // La Tisseuse de destins
    // -------------------------------------------------------------------------

    /** « Elle lit ton fil » : des fils d'or jaillissent d'elle vers les bords de l'écran, ses yeux-bobines s'allument. */
    static final class WeaverPhase extends BossScene {
        private final TextureRegion prediction;

        WeaverPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.TISSEUSE, 2);
            prediction = symbol(kind.kingPhaseSymbol());
        }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) rumble(0.2f, 4f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 10f); sparks(bossX(), bossY(), 50, THREAD, 700f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            float spread = Interpolation.pow2Out.apply(progress(0.5f, CLIMAX_PHASE));
            for (int k = 0; k < 10; k++) {
                float angle = k * 36f + time * 10f, length = Math.max(worldWidth(), worldHeight()) * spread;
                line(batch, THREAD, 0.8f, bossX(), bossY(), bossX() + MathUtils.cosDeg(angle) * length,
                    bossY() + MathUtils.sinDeg(angle) * length, 4f);
            }
            poseY = 30f * progress(1.0f, CLIMAX_PHASE);
            poseTint = 0.2f * progress(1.4f, CLIMAX_PHASE);
            poseTintColor.set(THREAD);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.4f, CLIMAX_PHASE) * pulse(6f, 0.6f), AMBER, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
            stolen(this, batch, prediction, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            flash(batch, THREAD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Ses fils se rompent un à un et retombent ; elle se défait en brins d'or que le vent emporte. */
    static final class WeaverDeath extends BossScene {
        WeaverDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.TISSEUSE, 0); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 5; k++) if (at(0.4f + k * 0.3f)) { rumble(0.08f, 4f); sparks(threadEnd(k)[0], threadEnd(k)[1], 10, THREAD, 400f, 8f); }
            if (time > CLIMAX_DEATH && time < 4f && random.nextFloat() < delta * 50f) {
                Particle strand = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight()
                    * random.nextFloat(), 20f + 40f * random.nextFloat(), 260f, 1.6f, 12f);
                strand.color.set(THREAD);
                glows.add(strand);
            }
            if (at(CLIMAX_DEATH)) rumble(0.4f, 8f);
        }

        /** @return le bout du fil {@code k}, en haut de l'écran. */
        private float[] threadEnd(int k) { return new float[] {worldWidth() * (0.1f + 0.2f * k), worldHeight()}; }

        @Override
        protected void back(Batch batch) {
            for (int k = 0; k < 5; k++) {                                // les fils qui la retiennent, coupés un à un
                float cut = progress(0.4f + k * 0.3f, 0.6f + k * 0.3f);
                float[] end = threadEnd(k);
                float sag = Interpolation.pow2In.apply(cut);
                line(batch, THREAD, 1f - cut, bossX(), bossY() + bossHeight() * 0.3f, end[0], MathUtils.lerp(end[1], groundY(), sag), 4f);
            }
            poseAlpha = 1f - progress(CLIMAX_DEATH, 3.8f);
            poseTint = 0.4f * progress(CLIMAX_DEATH - 0.4f, CLIMAX_DEATH + 0.6f);
            poseTintColor.set(THREAD);
            showBoss = time < 3.9f;
        }

        @Override
        protected void front(Batch batch) { flash(batch, THREAD, CLIMAX_DEATH, 0.3f, 0.35f); }
    }

    // -------------------------------------------------------------------------
    // Le Seigneur des Dés
    // -------------------------------------------------------------------------

    /** « Les dés sont pipés » : deux dés géants tombent du ciel, rebondissent et s'arrêtent sur six ; il grandit, rouge. */
    static final class DicePhase extends BossScene {
        private final TextureRegion die, loaded;

        DicePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.SEIGNEUR_DES, 2);
            die = art("oir", new Color[] {hex("1a1018"), IVORY, RUBY}, DIE);
            loaded = symbol(kind.kingPhaseSymbol());
        }

        @Override
        protected void step(float delta) {
            if (at(1.2f) || at(1.4f)) rumble(0.25f, 10f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 50, RUBY, 700f, 14f); }
        }

        private float dieX(int k) { return bossX() + (k == 0 ? -1f : 1f) * bossWidth() * 0.9f; }

        @Override
        protected void back(Batch batch) {
            poseScale = 1f + 0.12f * progress(1.6f, CLIMAX_PHASE);
            poseTint = 0.35f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(RUBY);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 2; k++) {
                float land = Interpolation.bounceOut.apply(progress(0.4f + k * 0.2f, 1.2f + k * 0.2f));
                float y = MathUtils.lerp(worldHeight() + 200f, groundY() + 80f, land);
                float spin = (1f - land) * 720f * (k == 0 ? 1f : -1f);
                draw(batch, die, dieX(k), y, fit(150f, 9), spin, WHITE, 1f);
            }
            stolen(this, batch, loaded, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            flash(batch, RUBY, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Il roule comme un dé qui a perdu : il bascule, s'écroule, et ses dés d'or s'éparpillent avec les pièces. */
    static final class DiceDeath extends BossScene {
        private final TextureRegion die;

        DiceDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.SEIGNEUR_DES, 0);
            die = art("oir", new Color[] {hex("1a1018"), hex("ffd54a"), hex("7a1a22")}, DIE);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                spray(die, bossX(), bossY(), 10, 30f, 150f, 900f, fit(60f, 9), 1300f, 1.8f);
                spray(coin, bossX(), bossY(), 24, 30f, 150f, 1000f, coinScale(0.045f), 1400f, 1.8f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float topple = Interpolation.pow2In.apply(progress(0.4f, CLIMAX_DEATH));
            poseTilt = -90f * topple;
            poseX = 140f * topple;
            poseSink = 0.5f * progress(CLIMAX_DEATH, 3.6f);
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.4f);
            poseAlpha = 1f - progress(3.6f, 4.4f);
        }

        @Override
        protected void front(Batch batch) { flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.35f); }
    }

    // -------------------------------------------------------------------------
    // L'Idole d'Or
    // -------------------------------------------------------------------------

    /** « Son or te renvoie ton image » : l'idole s'éveille, son troisième œil s'ouvre, un halo d'or l'entoure. */
    static final class IdolPhase extends BossScene {
        private final TextureRegion mirror;

        IdolPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.IDOLE_OR, 2);
            mirror = symbol(kind.kingPhaseSymbol());
        }

        @Override
        protected void step(float delta) {
            if (at(1.0f)) rumble(0.3f, 6f);
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 70, GOLD, 800f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float halo = progress(0.4f, CLIMAX_PHASE);
            glow(batch, bossX(), bossY(), bossHeight() * (1.2f + 0.4f * halo), GOLD, 0.5f * halo * pulse(4f, 0.7f));
            poseY = 24f * progress(0.8f, CLIMAX_PHASE);
            poseTint = 0.25f * progress(1.0f, CLIMAX_PHASE);
            poseTintColor.set(GOLD);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.0f, 1.4f) * pulse(8f, 0.6f), RUBY, new float[] {17f, 11.5f});
            wave(batch, CLIMAX_PHASE, 1.0f, bossX(), bossY(), GOLD, bossHeight() * 2.6f);
            stolen(this, batch, mirror, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** L'or se ternit, se fend et retombe en poussière ; de la poussière sort une pluie de pièces. */
    static final class IdolDeath extends BossScene {
        IdolDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.IDOLE_OR, 0); }

        @Override
        protected void step(float delta) {
            if (at(0.8f)) rumble(0.3f, 6f);
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                sparks(bossX(), bossY(), 60, STONE, 700f, 16f);
                spray(coin, bossX(), groundY() + 80f, 40, 40f, 140f, 1100f, coinScale(0.05f), 1500f, 1.8f);
            }
            if (time > CLIMAX_DEATH && time < 4f && random.nextFloat() < 0.5f) {
                Particle dust = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.2f, groundY() + 40f, 90f, 100f,
                    1.4f, 140f);
                dust.color.set(DUST.r, DUST.g, DUST.b, 0.35f);
                glows.add(dust);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseTint = 0.7f * progress(0.4f, CLIMAX_DEATH);
            poseTintColor.set(STONE);
            poseSink = progress(CLIMAX_DEATH, 3.8f);
            poseShadow = 0.4f * progress(CLIMAX_DEATH, 3.4f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH + 0.2f) {
                for (int k = 0; k < 3; k++) {
                    crack(batch, bossX() + (k - 1) * bossWidth() * 0.25f, bossY() + bossHeight() * 0.2f, -70f - 20f * k,
                        bossHeight() * 0.5f * progress(0.8f + k * 0.3f, CLIMAX_DEATH), 11L + k, hex("3a2a08"), 1f, 6f);
                }
            }
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }
}
