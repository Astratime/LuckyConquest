package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.ShardsArt;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort du Collectionneur, boss des Éclats dispersés (Tour à 10 chapitres, 2026-11-22).
 * Ses trois éclats sertis sur la poitrine ({@link ShardsArt#VITRINE_AT}) s'allument un par phase : l'Héritage dès
 * le début, la Valse à la phase 2, le Scellé à la phase 3. À sa mort, ils se fendent et retournent à la plaine.
 * L'éclat qui vole est toujours le vrai, découpé dans son portrait : son serti reste vide tant qu'il n'y est pas.
 */
final class ShardsBossScenes {

    private ShardsBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        return kind == EnemyKind.COLLECTIONNEUR ? new CollectorPhase(kit, phase) : null;
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.COLLECTIONNEUR ? new CollectorDeath(kit) : null;
    }

    static final Color GLASS = hex("b8d8f0"), INK = hex("1a0a0e");

    /** Socle des scènes du Collectionneur : ses éclats sertis, éteints ou allumés. */
    abstract static class CollectorScene extends BossScene {
        protected final Color[] colors = new Color[ShardsArt.VITRINE_COLORS.length];
        /** Ses vrais éclats, seuls, et leurs sertis vides (découpés dans son portrait sans éclats). */
        protected final TextureRegion[] chestGems = new TextureRegion[ShardsArt.VITRINE_AT.length];
        protected final TextureRegion[] emptySockets = new TextureRegion[ShardsArt.VITRINE_AT.length];

        CollectorScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.COLLECTIONNEUR, phase);
            for (int i = 0; i < colors.length; i++) colors[i] = hex(ShardsArt.VITRINE_COLORS[i]);
            TextureRegion bare = pixels(ShardsArt.collectionneur(0));
            for (int i = 0; i < chestGems.length; i++) {
                chestGems[i] = pixels(ShardsArt.chestGem(i));
                emptySockets[i] = new TextureRegion(bare, ShardsArt.chestGemLeft(i), ShardsArt.chestGemTop(i),
                    ShardsArt.CHEST_GEM_W, ShardsArt.CHEST_GEM_H);
            }
        }

        /** @return le centre de l'éclat {@code i} sur sa poitrine, à l'écran. */
        protected float chestX(int i) { return portraitX(ShardsArt.chestGemLeft(i) + ShardsArt.CHEST_GEM_W / 2f); }
        protected float chestY(int i) { return portraitY(ShardsArt.chestGemTop(i) + ShardsArt.CHEST_GEM_H / 2f); }

        /** Le serti {@code i} vide : son éclat est ailleurs. */
        protected void emptySocket(Batch batch, int i) {
            onPortrait(batch, emptySockets[i], ShardsArt.chestGemLeft(i), ShardsArt.chestGemTop(i), 1f, 1f);
        }

        /** @return la taille d'un pixel de son portrait, à l'écran (celle de ses éclats quand ils volent). */
        protected float gemScale() { return bossScale() * poseScale; }

        /** Sa lueur sur l'éclat {@code i} de sa poitrine, s'il est allumé ({@code light} de 0 à 1). */
        protected void lightGem(Batch batch, int i, float light) {
            if (light <= 0f) return;
            float size = gemScale();
            glow(batch, chestX(i), chestY(i), size * 10f * (0.7f + 0.3f * light), colors[i], 0.7f * light);
            glow(batch, chestX(i), chestY(i), size * 3f, WHITE, 0.6f * light);
        }
    }

    /**
     * « Nouvel éclat » : l'éclat encore éteint de sa poitrine (rose pour la Valse, vert pour le Scellé) s'arrache de
     * son serti, qui reste vide, fait le tour de la scène en tournoyant, et replonge dans son serti ; il s'allume,
     * ses autres éclats répondent. Le symbole que cet éclat lui donne sort de lui et se pose à côté de lui : la Valse
     * en tournant sur elle-même comme une danseuse, le Scellé en tombant comme un tampon de cire.
     */
    static final class CollectorPhase extends CollectorScene {
        static final float LEAVE = 0.15f, OUT = 0.6f, DIVE = 1.7f;
        private final int index;
        /** Le symbole que l'éclat lui donne : la Valse (phase 2) ou le Scellé (phase 3). */
        private final TextureRegion gift;

        CollectorPhase(CutsceneKit kit, int phase) {
            super(kit, Moment.PHASE, phase);
            index = Math.min(colors.length - 1, Math.max(1, phase - 1));
            gift = symbol(phase == 2 ? EnemySymbol.WALTZ : EnemySymbol.SEAL);
        }

        /** @return la place du symbole gagné, à côté de lui. */
        private float giftX() { return bossX() + bossWidth() * 0.5f + worldHeight() * 0.12f; }
        private float giftY() { return bossY() + bossHeight() * 0.25f; }

        private float leave() { return Interpolation.pow2Out.apply(progress(LEAVE, OUT)); }
        private float dive()  { return Interpolation.pow3In.apply(progress(DIVE, CLIMAX_PHASE)); }

        private float gemX() {
            float orbit = progress(OUT, DIVE);
            float x = bossX() + MathUtils.cos(orbit * MathUtils.PI2 * 1.25f) * worldWidth() * 0.32f;
            return MathUtils.lerp(MathUtils.lerp(chestX(index), x, leave()), chestX(index), dive());
        }

        private float gemY() {
            float orbit = progress(OUT, DIVE);
            float y = bossY() + bossHeight() * 0.6f + MathUtils.sin(orbit * MathUtils.PI2 * 1.25f) * worldHeight() * 0.12f;
            return MathUtils.lerp(MathUtils.lerp(chestY(index), y, leave()), chestY(index), dive());
        }

        /** @return sa taille : celle du portrait dans son serti, plus grand pendant qu'il tournoie. */
        private float gemSize() { return gemScale() * (1f + 1.2f * leave() * (1f - dive())); }

        @Override
        protected void step(float delta) {
            if (at(LEAVE)) { rumble(0.15f, 4f); sparks(chestX(index), chestY(index), 20, colors[index], 400f, 10f); }
            if (time > OUT && time < CLIMAX_PHASE && random.nextFloat() < delta * 40f) sparks(gemX(), gemY(), 1, colors[index], 120f, 10f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 12f);
                sparks(chestX(index), chestY(index), 80, colors[index], 900f, 14f);
                sparks(chestX(index), chestY(index), 30, WHITE, 600f, 10f);
            }
            if (phase == 3 && at(CLIMAX_PHASE + 0.75f)) {                  // le tampon du Scellé
                rumble(0.2f, 6f);
                sparks(giftX(), giftY() - iconScale(0.16f) * 7f, 16, colors[index], 300f, 8f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.35f * progress(0f, 0.8f));
            poseY = 14f * gesture(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.5f);
            poseTintColor.set(colors[index]);
            poseTint = 0.35f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f) * pulse(5f, 0.5f);
            glow(batch, bossX(), bossY(), bossWidth() * 2.4f, colors[index], 0.2f + 0.2f * progress(DIVE, CLIMAX_PHASE));
        }

        @Override
        protected void front(Batch batch) {
            for (int i = 0; i < index; i++) lightGem(batch, i, pulse(4f + i, 0.6f) * (time > CLIMAX_PHASE ? 1f : 0.7f));
            if (time >= CLIMAX_PHASE) lightGem(batch, index, 1f);
            if (time >= LEAVE && time < CLIMAX_PHASE) {                    // le vrai éclat, hors de son serti
                emptySocket(batch, index);
                float size = gemSize(), spin = (time - LEAVE) * 360f * (1f - dive());
                glow(batch, gemX(), gemY(), size * 12f, colors[index], 0.8f);
                draw(batch, chestGems[index], gemX(), gemY(), size, spin, Color.WHITE, 1f);
            }
            wave(batch, CLIMAX_PHASE, 0.8f, chestX(index), chestY(index), colors[index], worldWidth() * 1.3f);
            drawGift(batch);
            flash(batch, colors[index], CLIMAX_PHASE, 0.3f, 0.45f);
        }

        /** Le symbole gagné sort de l'éclat serti et se pose à côté de lui, où il reste. */
        private void drawGift(Batch batch) {
            if (gift == null || time < CLIMAX_PHASE) return;
            float out = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f));
            float x = MathUtils.lerp(chestX(index), giftX(), out), y = MathUtils.lerp(chestY(index), giftY(), out);
            float size = iconScale(0.16f), rotation = 0f, scale = size * (0.3f + 0.7f * out);
            if (phase == 2) {                                              // la Valse tourne, comme une danseuse
                rotation = 14f * MathUtils.sin((time - CLIMAX_PHASE) * 5f);
                x += MathUtils.sin((time - CLIMAX_PHASE) * 2.5f) * size * 2f * out;
            } else {                                                       // le Scellé tombe comme un tampon
                float stamp = progress(CLIMAX_PHASE + 0.5f, CLIMAX_PHASE + 0.75f);
                scale *= 1f + 0.6f * (1f - stamp) * out;
            }
            glow(batch, x, y, size * 34f * pulse(4f, 0.7f), colors[index], 0.8f * out);
            glow(batch, x, y, size * 14f, WHITE, 0.35f * out);
            draw(batch, gift, x, y, scale, rotation, WHITE, Math.min(1f, out * 3f));
        }
    }

    /**
     * « La vitrine vole en éclats » : ses trois éclats se fendent, la vitre derrière lui se fêle de partout puis
     * éclate ; ses trois éclats quittent leurs sertis et s'envolent dans la nuit, et lui pâlit et s'efface.
     */
    static final class CollectorDeath extends CollectorScene {
        static final float CRACK = 0.8f;
        private final TextureRegion shardGlass;
        private boolean freed;

        CollectorDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            shardGlass = art("g", new Color[] {GLASS}, "g..", "gg.", "ggg");
        }

        @Override protected void restart() { freed = false; }

        @Override
        protected void step(float delta) {
            if (at(CRACK)) rumble(0.3f, 5f);
            if (at(CLIMAX_DEATH)) {                                        // la vitre éclate
                rumble(0.8f, 16f);
                spray(shardGlass, bossX(), bossY() + bossHeight() * 0.2f, 60, 0f, 360f, 1300f, bossScale() * 1.4f, 1500f, 1.6f);
                for (int i = 0; i < colors.length; i++) sparks(chestX(i), chestY(i), 40, colors[i], 800f, 14f);
            }
            if (!freed && time >= CLIMAX_DEATH + 0.2f) {                    // les éclats retournent à la plaine
                freed = true;
                for (int i = 0; i < colors.length; i++) {                  // chacun le sien, pas de copie
                    Particle piece = place(chestGems[i], chestX(i), chestY(i), (i - 1) * 380f + (random.nextFloat() - 0.5f) * 200f,
                        700f + 300f * random.nextFloat(), gemScale(), 2.2f);
                    piece.gravity = -200f;
                    piece.spin = (random.nextFloat() - 0.5f) * 600f;
                    sparks(chestX(i), chestY(i), 12, colors[i], 300f, 8f);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            float k = progress(CLIMAX_DEATH, 4.2f);
            poseShadow = 0.5f * k;
            poseAlpha = 1f - progress(3.2f, 4.4f);
            poseSink = 0.2f * Interpolation.pow2In.apply(k);
            poseTilt = -4f * k;
            poseX = time < CLIMAX_DEATH ? (random.nextFloat() - 0.5f) * 6f * progress(CRACK, CLIMAX_DEATH) : 0f;
            showBoss = time < 4.5f;
            if (time < CLIMAX_DEATH) {                                     // la vitre derrière lui, qui se fêle
                float w = bossWidth() * 2.2f, h = bossHeight() * 1.1f, x = bossX() - w / 2f, y = bossBottom();
                rect(batch, hex("5a4224"), 1f, x - 10f, y - 10f, w + 20f, h + 20f);
                rect(batch, hex("1e2a3a"), 1f, x, y, w, h);
                float cracks = progress(CRACK, CLIMAX_DEATH);
                for (int i = 0; i < 6; i++) {
                    crack(batch, bossX(), bossY() + bossHeight() * 0.3f, i * 60f + 15f, w * 0.6f * cracks, 11L + i, GLASS,
                        0.8f * cracks, 3f);
                }
            }
        }

        @Override
        protected void front(Batch batch) {
            float dim = 1f - progress(CRACK, CLIMAX_DEATH);
            for (int i = 0; i < colors.length; i++) {
                if (freed) emptySocket(batch, i);
                if (time < CLIMAX_DEATH) {
                    lightGem(batch, i, 0.4f + 0.6f * dim);
                    crack(batch, chestX(i), chestY(i) + bossScale() * 2f, -80f, bossScale() * 5f * (1f - dim), 3L + i, INK,
                        1f, 3f);
                }
            }
            wave(batch, CLIMAX_DEATH, 0.9f, bossX(), bossY(), GLASS, worldWidth() * 1.5f);
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.6f);
        }
    }
}
