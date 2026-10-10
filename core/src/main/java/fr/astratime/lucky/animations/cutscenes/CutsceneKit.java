package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.settings.VisualSettings;

/**
 * Ce dont les cinématiques ont besoin, prêté par l'écran de jeu (rien n'est possédé) :
 * réglages, secousse, sons, portraits des ennemis et une police pour les répliques.
 */
public record CutsceneKit(VisualSettings settings, ScreenShake shake, GameSounds sounds,
                          EnemyTextures enemies, BitmapFont font) {

    /** @return le son de la cinématique {@code name} (sounds/cutscene/{@code name}.ogg), {@code null} sans sons. */
    public Sound sound(String name) { return sounds == null ? null : sounds.cutscene(name); }

    /** @return la boucle de la cinématique {@code name} (sounds/cutscene/{@code name}.wav), {@code null} sans sons. */
    public Sound loop(String name) { return sounds == null ? null : sounds.cutsceneLoop(name); }

    /** @return les bruitages du jeu, {@code null} sans sons. */
    public GameSounds gameSounds() { return sounds; }

    /** @return l'illustration en pixel art du chapitre {@code chapter}. */
    public TextureRegion chapterArt(Chapter chapter) { return new TextureRegion(enemies.chapterArt(chapter)); }

    /** @return le symbole en pixel art {@code symbol} des machines ennemies. */
    public TextureRegion symbol(EnemySymbol symbol) { return new TextureRegion(enemies.symbol(symbol)); }

    /** @return le portrait en pixel art de l'ennemi {@code kind}. */
    public TextureRegion portrait(EnemyKind kind) { return new TextureRegion(enemies.portrait(kind)); }
}
