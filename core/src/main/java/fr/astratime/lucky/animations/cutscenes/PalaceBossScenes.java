package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort des quatre rois du Palace des Neiges (10 lieux, Astra 2026-11-20). */
final class PalaceBossScenes {

    private PalaceBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CONCIERGE -> new ConciergePhase(kit);
            case IMPERATRICE_GIVRE -> new EmpressPhase(kit);
            case YETI -> new YetiPhase(kit);
            case PRINCE_NEIGES -> new PrincePhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CONCIERGE -> new ConciergeDeath(kit);
            case IMPERATRICE_GIVRE -> new EmpressDeath(kit);
            case YETI -> new YetiDeath(kit);
            case PRINCE_NEIGES -> new PrinceDeath(kit);
            default -> null;
        };
    }

    static final Color ICE = hex("bfe8ff"), ICE_DEEP = hex("6aa8d8"), SNOW = hex("f2f8ff"), BUBBLE = hex("ffe08a");
    static final Color WATER = hex("4a8ac0"), WOOD = hex("3a2418");
    static final String[] BELL = {"....oo....", "...oggo...", "..oggggo..", ".oggggsgo.", "oooooooooo"};
    static final String[] CROWN = {"i.i.i.i", "iiiiiii", "iwwiwwi", "iiiiiii"};

    // -------------------------------------------------------------------------
    // Le Concierge
    // -------------------------------------------------------------------------

    /** « Service d'étage » : il frappe trois fois la sonnette, des plateaux de champagne glacé arrivent de partout. */
    static final class ConciergePhase extends BossScene {
        private final TextureRegion bell, champagne;

        ConciergePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.CONCIERGE, 2);
            bell = art("ogs", new Color[] {hex("3a2a10"), GOLD, hex("c4891e")}, BELL);
            champagne = symbol(EnemySymbol.CHAMPAGNE);
        }

        private float bellX() { return bossX() + bossWidth() * 0.75f; }
        private float bellY() { return bossBottom() + bossHeight() * 0.15f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) if (at(0.5f + k * 0.45f)) { rumble(0.1f, 4f); sparks(bellX(), bellY() + 40f, 24, GOLD, 500f, 10f); }
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY(), 50, BUBBLE, 700f, 12f);
                if (champagne != null) spray(champagne, bossX(), bossY(), 6, 20f, 160f, 900f, iconScale(0.06f), 1200f, 1.2f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float strike = 0f;
            for (int k = 0; k < 3; k++) strike = Math.max(strike, gesture(0.4f + k * 0.45f, 0.6f + k * 0.45f));
            poseTilt = 4f * strike;
            poseY = 20f * progress(1.8f, CLIMAX_PHASE);
            poseTint = 0.25f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            draw(batch, bell, bellX(), bellY(), fit(90f, 10), 0f, WHITE, progress(0.2f, 0.4f));
            for (int k = 0; k < 3; k++) flash(batch, GOLD, 0.5f + k * 0.45f, 0.06f, 0.15f);
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Il gèle sur place, se fendille, puis vole en éclats ; ses clés et des pièces tombent sur le marbre. */
    static final class ConciergeDeath extends BossScene {
        private final TextureRegion key;

        ConciergeDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CONCIERGE, 0);
            key = symbol(EnemySymbol.KEY);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(1.0f + k * 0.3f)) { rumble(0.08f, 3f + k); sparks(bossX(), bossY(), 10, SNOW, 300f, 8f); }
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                sparks(bossX(), bossY(), 90, ICE, 900f, 16f);
                sparks(bossX(), bossY(), 40, SNOW, 600f, 10f);
                if (key != null) spray(key, bossX(), bossY(), 6, 30f, 150f, 800f, iconScale(0.05f), 1500f, 1.6f);
                spray(coin, bossX(), bossY(), 24, 40f, 140f, 900f, coinScale(0.04f), 1400f, 1.6f);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseTint = 0.7f * progress(0.4f, 1.4f);
            poseTintColor.set(ICE);
            poseX = time < CLIMAX_DEATH ? MathUtils.sin(time * 60f) * 3f * progress(1.0f, CLIMAX_DEATH) : 0f;
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            float cracks = progress(1.0f, CLIMAX_DEATH);                                  // les fêlures du givre
            if (time < CLIMAX_DEATH) for (int k = 0; k < 4 && k < cracks * 5f; k++) {
                float a = 40f + k * 85f;
                line(batch, WHITE, 0.9f, bossX(), bossY(), bossX() + MathUtils.cosDeg(a) * bossWidth() * 0.45f,
                    bossY() + MathUtils.sinDeg(a) * bossHeight() * 0.4f, 6f);
            }
            flash(batch, ICE, CLIMAX_DEATH, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // L'Impératrice du Givre
    // -------------------------------------------------------------------------

    /** « Le bal de glace commence » : elle tourne sur elle-même, des cristaux tournoient, des coupes glacées apparaissent. */
    static final class EmpressPhase extends BossScene {
        private final TextureRegion champagne;

        EmpressPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.IMPERATRICE_GIVRE, 2);
            champagne = symbol(EnemySymbol.CHAMPAGNE);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.4f && time < CLIMAX_PHASE && random.nextFloat() < delta * 30f) {
                float a = random.nextFloat() * 360f;
                sparks(bossX() + MathUtils.cosDeg(a) * bossWidth() * 0.7f, bossY() + MathUtils.sinDeg(a) * bossHeight() * 0.4f, 4,
                    SNOW, 200f, 8f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 10f); sparks(bossX(), bossY(), 70, ICE, 800f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float spin = progress(0.4f, CLIMAX_PHASE);
            poseStretch = 0.5f * MathUtils.sin(spin * MathUtils.PI * 4f) * (1f - progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE));
            poseTint = 0.4f * spin;
            poseTintColor.set(ICE);
            poseY = 30f * spin;
        }

        @Override
        protected void front(Batch batch) {
            float circle = progress(0.4f, 0.8f) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            for (int k = 0; k < 6 && circle > 0f; k++) {                                     // les cristaux qui tournoient
                float a = time * 220f + k * 60f;
                glow(batch, bossX() + MathUtils.cosDeg(a) * bossWidth() * 0.8f, bossY() + MathUtils.sinDeg(a) * bossHeight() * 0.35f,
                    80f, ICE, 0.8f * circle);
            }
            if (champagne != null && time >= CLIMAX_PHASE) {
                for (int k = 0; k < 2; k++) draw(batch, champagne, bossX() + (k == 0 ? -1f : 1f) * bossWidth() * 0.7f, bossY(),
                    iconScale(0.1f), 0f, WHITE, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            }
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Elle fond : elle s'affaisse dans une flaque qui s'étend ; il ne reste que sa couronne de glace, posée dans l'eau. */
    static final class EmpressDeath extends BossScene {
        private final TextureRegion crown;

        EmpressDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.IMPERATRICE_GIVRE, 0);
            crown = art("iw", new Color[] {ICE, WHITE}, CROWN);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < 3.4f && random.nextFloat() < delta * 20f) {             // les gouttes
                Particle drop = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight()
                    * random.nextFloat() * 0.6f, 270f, 200f, 0.8f, 10f);
                drop.color.set(ICE);
                drop.gravity = 900f;
                glows.add(drop);
            }
            if (at(CLIMAX_DEATH)) rumble(0.4f, 6f);
        }

        @Override
        protected void back(Batch batch) {
            float melt = Interpolation.pow2In.apply(progress(0.6f, 3.4f));
            poseSink = 0.9f * melt;
            poseStretch = -0.3f * melt;
            poseTint = 0.5f * progress(0.4f, 1.4f);
            poseTintColor.set(WATER);
            poseAlpha = 1f - progress(3.0f, 3.6f);
        }

        @Override
        protected void front(Batch batch) {
            float pool = progress(0.8f, 3.4f);
            if (pool > 0f) {                                                                 // la flaque
                rect(batch, WATER, 0.7f, bossX() - bossWidth() * pool, groundY() - 6f, bossWidth() * 2f * pool, 16f);
                glow(batch, bossX(), groundY(), bossWidth() * 2f * pool, ICE, 0.3f * pool);
            }
            float fall = Interpolation.bounceOut.apply(progress(2.6f, 3.4f));
            if (fall > 0f) draw(batch, crown, bossX(), MathUtils.lerp(bossY() + bossHeight() * 0.3f, groundY() + 30f, fall),
                fit(110f, 7), 12f * fall, WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Yéti
    // -------------------------------------------------------------------------

    /** « Souffle polaire » : il se frappe la poitrine, puis souffle un vent de glace ; ses Gels apparaissent. */
    static final class YetiPhase extends BossScene {
        private final TextureRegion freeze;

        YetiPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.YETI, 2);
            freeze = symbol(EnemySymbol.FREEZE);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(0.5f + k * 0.3f)) rumble(0.12f, 8f);
            if (time > 1.8f && time < CLIMAX_PHASE + 0.6f && random.nextFloat() < delta * 40f) {
                Particle wind = particle(portraitX(17f), portraitY(22f), -90f + (random.nextFloat() - 0.5f) * 80f, 700f, 1.0f, 60f);
                wind.color.set(ICE.r, ICE.g, ICE.b, 0.4f);
                glows.add(wind);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 14f);
                if (freeze != null) spray(freeze, bossX(), bossY(), 6, 20f, 160f, 900f, iconScale(0.06f), 1200f, 1.2f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float beat = 0f;
            for (int k = 0; k < 4; k++) beat = Math.max(beat, gesture(0.45f + k * 0.3f, 0.6f + k * 0.3f));
            poseStretch = -0.08f * beat + 0.1f * gesture(1.8f, CLIMAX_PHASE + 0.4f);
            poseTint = 0.3f * progress(1.8f, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            if (time > 1.8f) fill(batch, ICE, 0.15f * gesture(1.8f, CLIMAX_PHASE + 0.8f));
            flash(batch, SNOW, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Il bascule en arrière et l'avalanche l'ensevelit ; une pluie de pièces jaillit de la neige. */
    static final class YetiDeath extends BossScene {
        YetiDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.YETI, 0); }

        @Override
        protected void step(float delta) {
            if (at(0.4f)) rumble(2.2f, 6f);
            if (time > 1.2f && time < CLIMAX_DEATH && random.nextFloat() < delta * 40f) {
                Particle flake = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 2f, worldHeight(), 270f, 600f, 1.5f, 24f);
                flake.color.set(SNOW);
                glows.add(flake);
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 14f);
                sparks(bossX(), groundY() + 100f, 70, SNOW, 800f, 20f);
                spray(coin, bossX(), groundY() + 120f, 28, 40f, 140f, 1000f, coinScale(0.04f), 1400f, 1.6f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float tip = Interpolation.pow2In.apply(progress(0.4f, CLIMAX_DEATH));
            poseTilt = 20f * tip;
            poseX = -60f * tip;
            poseSink = 0.6f * progress(1.4f, 3.4f);
            poseShadow = 0.4f * progress(CLIMAX_DEATH, 3.4f);
            poseAlpha = 1f - progress(3.4f, 4.0f);
        }

        @Override
        protected void front(Batch batch) {
            float heap = Interpolation.pow2Out.apply(progress(1.2f, 3.2f));                    // la neige qui monte
            if (heap > 0f) {
                float top = groundY() + bossHeight() * 0.55f * heap;
                for (int k = -3; k <= 3; k++) glow(batch, bossX() + k * bossWidth() * 0.3f, top - 40f, 260f, SNOW, 0.9f * heap);
                rect(batch, SNOW, 1f, bossX() - bossWidth() * 1.4f, groundY() - 20f, bossWidth() * 2.8f, top - groundY());
            }
            flash(batch, SNOW, CLIMAX_DEATH, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Prince des Neiges
    // -------------------------------------------------------------------------

    /** « Les portes du bal se ferment » : deux grandes portes givrées claquent de chaque côté ; ses Clés apparaissent. */
    static final class PrincePhase extends BossScene {
        private final TextureRegion key;

        PrincePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PRINCE_NEIGES, 2);
            key = symbol(EnemySymbol.KEY);
        }

        @Override
        protected void step(float delta) {
            if (at(1.6f)) { rumble(0.5f, 12f); sparks(worldWidth() * 0.5f, groundY() + 200f, 40, ICE, 600f, 12f); }
            if (at(CLIMAX_PHASE)) {
                rumble(0.4f, 8f);
                if (key != null) spray(key, bossX(), bossY(), 6, 20f, 160f, 900f, iconScale(0.06f), 1200f, 1.2f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float close = Interpolation.pow3In.apply(progress(0.5f, 1.6f));                 // les portes, derrière lui
            float width = worldWidth(), half = width * 0.5f * close;
            rect(batch, hex("e8eef8"), 1f, -40f, 0f, half + 40f, worldHeight());
            rect(batch, hex("c8d0e0"), 1f, width - half, 0f, half + 40f, worldHeight());
            if (close > 0.95f) line(batch, GOLD, 1f, width * 0.5f, 0f, width * 0.5f, worldHeight(), 10f);
            poseY = 20f * progress(1.8f, CLIMAX_PHASE);
            poseTint = 0.2f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            flash(batch, WHITE, 1.6f, 0.1f, 0.3f);
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Sa coupe lui échappe et se brise ; il s'efface en flocons qui montent vers le lustre, avec les pièces du bal. */
    static final class PrinceDeath extends BossScene {
        PrinceDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.PRINCE_NEIGES, 0); }

        private float glassY() {
            return MathUtils.lerp(portraitY(12f), groundY() + 10f, Interpolation.pow2In.apply(progress(0.6f, 1.2f)));
        }

        @Override
        protected void step(float delta) {
            if (at(1.2f)) { rumble(0.15f, 5f); sparks(portraitX(29f), groundY() + 10f, 30, ICE, 500f, 10f); }
            if (time > 1.6f && time < 3.6f && random.nextFloat() < delta * 50f) {
                Particle flake = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight()
                    * random.nextFloat(), 80f + 20f * random.nextFloat(), 260f, 1.8f, 14f);
                flake.color.set(SNOW);
                glows.add(flake);
            }
            if (at(CLIMAX_DEATH)) spray(coin, bossX(), bossY(), 24, 40f, 140f, 900f, coinScale(0.04f), 1400f, 1.6f);
        }

        @Override
        protected void back(Batch batch) {
            poseAlpha = 1f - progress(1.6f, 3.6f);
            poseTint = 0.5f * progress(1.2f, 2.4f);
            poseTintColor.set(ICE);
            poseY = 40f * progress(1.6f, 3.6f);
            showBoss = time < 3.7f;
        }

        @Override
        protected void front(Batch batch) {
            if (time < 1.2f) {                                                                // la coupe qui tombe
                float x = portraitX(29f);
                line(batch, BUBBLE, 1f, x - 20f, glassY() + 30f, x + 20f, glassY() + 30f, 16f);
                line(batch, ICE, 1f, x, glassY(), x, glassY() + 24f, 6f);
            }
            flash(batch, ICE, 1.2f, 0.08f, 0.2f);
            flash(batch, SNOW, CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }
}
