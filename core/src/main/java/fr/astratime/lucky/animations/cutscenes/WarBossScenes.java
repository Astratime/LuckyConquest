package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort du Général Va-Tout, boss du chapitre « La Guerre des Tables » :
 * ses lieutenants arrivent (phase 2), il fait Va-tout (phase 3), il hisse le drapeau blanc (mort).
 */
final class WarBossScenes {

    private WarBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        if (kind != EnemyKind.GENERAL) return null;
        return phase == 2 ? new GeneralLieutenants(kit) : new GeneralAllOut(kit);
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.GENERAL ? new GeneralDeath(kit) : null;
    }

    static final Color POWDER = hex("8a7e76"), FIRE = hex("ff8a1e"), FLAG = hex("f6f0e2");
    static final Color[] CHIPS = {hex("d8283c"), hex("2a5ad8"), hex("1f9a4a"), hex("ffd54a")};
    static final String[] CHIP = {".ooo.", "occco", "ocwco", "occco", ".ooo."};

    /** Un jeton de la couleur {@code color}, détouré de sombre. */
    static TextureRegion chip(BossScene scene, Color color) {
        return scene.art("ocw", new Color[] {hex("1a0f0f"), color, hex("f6f0e2")}, CHIP);
    }

    // -------------------------------------------------------------------------
    // Phase 2 : ses lieutenants arrivent
    // -------------------------------------------------------------------------

    /**
     * « Ses lieutenants arrivent » : un coup de clairon, l'Espionne glisse depuis la gauche et le Mercenaire depuis la
     * droite ; ils se postent à ses côtés, leur symbole (Espionnage, Contrat) s'allume au-dessus d'eux.
     */
    static final class GeneralLieutenants extends BossScene {
        private final TextureRegion spy, mercenary, spyIcon, contractIcon;

        GeneralLieutenants(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GENERAL, 2);
            spy = kit.enemies() == null ? null : kit.portrait(EnemyKind.ESPIONNE);
            mercenary = kit.enemies() == null ? null : kit.portrait(EnemyKind.MERCENAIRE);
            spyIcon = symbol(EnemySymbol.SPY);
            contractIcon = symbol(EnemySymbol.CONTRACT);
        }

        private float side(int s) {
            float arrive = Interpolation.pow2Out.apply(progress(0.4f + (s > 0 ? 0.3f : 0f), 1.5f + (s > 0 ? 0.3f : 0f)));
            return bossX() + s * MathUtils.lerp(worldWidth() * 0.7f, bossWidth() * 0.95f, arrive);
        }

        @Override
        protected void step(float delta) {
            if (at(0.3f)) rumble(0.15f, 4f);
            if (at(1.5f)) sparks(side(-1), bossY(), 20, POWDER, 300f, 10f);
            if (at(1.8f)) sparks(side(1), bossY(), 20, POWDER, 300f, 10f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, GOLD, 700f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.3f * progress(0.2f, 1.0f));
            poseY = 14f * gesture(1.6f, CLIMAX_PHASE + 0.4f);
            float scale = bossScale() * 0.8f;
            for (int s = -1; s <= 1; s += 2) {
                TextureRegion who = s < 0 ? spy : mercenary;
                if (who == null) continue;
                float x = side(s);
                glow(batch, x, bossBottom() + who.getRegionHeight() * scale * 0.5f, 160f, s < 0 ? hex("7ab8ff") : GOLD, 0.3f);
                draw(batch, who, x, bossBottom() + who.getRegionHeight() * scale / 2f, scale, 0f, WHITE, 1f);
            }
        }

        @Override
        protected void front(Batch batch) {
            float icons = progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.2f);
            for (int s = -1; s <= 1; s += 2) {
                TextureRegion icon = s < 0 ? spyIcon : contractIcon;
                if (icon == null || icons <= 0f) continue;
                float x = side(s), y = bossBottom() + bossHeight() * 1.05f + 10f * pulse(4f, 0.5f);
                glow(batch, x, y, 140f, GOLD, 0.4f * icons);
                draw(batch, icon, x, y, iconScale(0.09f), 0f, WHITE, icons);
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    // -------------------------------------------------------------------------
    // Phase 3 : Va-tout
    // -------------------------------------------------------------------------

    /**
     * « Va-tout » : il jette son bouclier, qui tourne et se brise au sol ; deux grosses piles de jetons glissent
     * devant lui et se rejoignent ; une lueur rouge le couvre, il se gonfle, prêt à tout miser.
     */
    static final class GeneralAllOut extends BossScene {
        private final TextureRegion shield;
        private final TextureRegion[] chips = new TextureRegion[CHIPS.length];

        GeneralAllOut(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GENERAL, 3);
            shield = symbol(EnemySymbol.SHIELD);
            for (int i = 0; i < CHIPS.length; i++) chips[i] = chip(this, CHIPS[i]);
        }

        private float throwT() { return progress(0.4f, 1.3f); }

        @Override
        protected void step(float delta) {
            if (at(1.3f)) {                                                  // le bouclier se brise
                rumble(0.3f, 8f);
                sparks(bossX() + worldWidth() * 0.32f, groundY(), 40, hex("b0b8c8"), 600f, 10f);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(bossX(), groundY() + 40f, 80, GOLD, 900f, 14f);
                sparks(bossX(), bossY(), 40, BLOOD, 700f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.45f * progress(0.2f, 1.0f));
            glow(batch, bossX(), bossY(), bossHeight() * 1.6f, BLOOD, 0.35f * progress(1.4f, CLIMAX_PHASE) * pulse(5f, 0.6f));
            poseTint = 0.4f * progress(1.4f, CLIMAX_PHASE);
            poseTintColor.set(BLOOD);
            poseStretch = 0.08f * progress(1.6f, CLIMAX_PHASE);
            poseTilt = -8f * gesture(0.3f, 0.9f);
        }

        @Override
        protected void front(Batch batch) {
            if (shield != null && time < 1.3f) {                              // le bouclier jeté, qui tournoie
                float t = throwT();
                float x = MathUtils.lerp(bossX(), bossX() + worldWidth() * 0.32f, t);
                float y = MathUtils.lerp(bossY(), groundY(), t) + MathUtils.sin(t * MathUtils.PI) * worldHeight() * 0.2f;
                draw(batch, shield, x, y, iconScale(0.12f), time * 720f, WHITE, 1f);
            }
            float push = Interpolation.pow2In.apply(progress(1.4f, CLIMAX_PHASE));
            for (int s = -1; s <= 1; s += 2) {                                // les deux piles de jetons poussées au milieu
                float x = bossX() + s * MathUtils.lerp(worldWidth() * 0.4f, worldWidth() * 0.04f, push);
                for (int k = 0; k < 10; k++) {
                    draw(batch, chips[(k + (s > 0 ? 2 : 0)) % chips.length], x, groundY() + 20f + k * 14f, fit(40f, 5), 0f, WHITE,
                        progress(1.2f, 1.5f));
                }
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), groundY() + 90f, 300f, GOLD, 0.5f * (1f - progress(CLIMAX_PHASE, 3.6f)));
            flash(batch, BLOOD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Mort : le drapeau blanc
    // -------------------------------------------------------------------------

    /**
     * Il a tout misé, il a tout perdu : ses piles de jetons s'écroulent et roulent au loin, son bicorne s'envole,
     * un drapeau blanc monte à côté de lui ; il s'efface dans la fumée de poudre.
     */
    static final class GeneralDeath extends BossScene {
        private final TextureRegion[] chips = new TextureRegion[CHIPS.length];

        GeneralDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GENERAL, 0);
            for (int i = 0; i < CHIPS.length; i++) chips[i] = chip(this, CHIPS[i]);
        }

        private float flagX()   { return bossX() + bossWidth() * 0.85f; }
        private float flagTop() { return groundY() + MathUtils.lerp(0f, bossHeight() * 1.2f, Interpolation.pow2Out.apply(progress(1.0f, CLIMAX_DEATH))); }

        @Override
        protected void step(float delta) {
            if (at(0.5f)) {                                                   // les piles s'écroulent
                rumble(0.4f, 8f);
                for (int i = 0; i < 30; i++) {
                    Cutscene.Particle falling = place(chips[i % chips.length], bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.4f,
                        groundY() + 40f + random.nextFloat() * 120f, (random.nextBoolean() ? 1 : -1) * (300f + 400f * random.nextFloat()),
                        200f + 300f * random.nextFloat(), fit(36f, 5), 2.2f);
                    falling.gravity = 900f;
                    falling.spin = 360f * (random.nextFloat() - 0.5f) * 4f;
                }
            }
            if (time > 1.2f && time < 3.6f && random.nextFloat() < delta * 20f) {
                sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 3,
                    POWDER, 150f, 14f);
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.5f, 8f);
                spray(coin, bossX(), bossY(), 20, 40f, 140f, 800f, coinScale(0.04f), 1400f, 1.5f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, POWDER, 0.2f * progress(1.0f, 3.0f));
            poseTilt = -6f * progress(0.3f, 1.2f);
            poseTint = 0.5f * progress(1.6f, 3.0f);
            poseTintColor.set(POWDER);
            poseAlpha = 1f - progress(2.4f, 3.8f);
            showBoss = time < 3.9f;
        }

        @Override
        protected void front(Batch batch) {
            float x = flagX(), top = flagTop();                               // le drapeau blanc qui monte
            rect(batch, hex("4a2e1a"), 1f, x - 3f, groundY(), 6f, top - groundY());
            float wave = MathUtils.sin(time * 8f) * 6f;
            float flagW = bossWidth() * 0.5f, flagH = bossHeight() * 0.22f;
            if (top > groundY() + flagH) {
                for (int k = 0; k < 6; k++) {
                    float w = flagW / 6f;
                    rect(batch, k % 2 == 0 ? FLAG : hex("d8d0c4"), 1f, x + 3f + k * w, top - flagH + MathUtils.sin(time * 8f + k) * 4f + wave * 0.2f,
                        w + 1f, flagH);
                }
            }
            flash(batch, FLAG, CLIMAX_DEATH, 0.3f, 0.35f);
        }
    }
}
