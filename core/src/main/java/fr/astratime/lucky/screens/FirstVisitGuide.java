package fr.astratime.lucky.screens;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.GuideOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * La première fois qu'un écran du menu s'ouvre (Exploration, Table du
 * croupier, Boutique, Tour des épreuves), le Croupier le présente en quelques
 * répliques. Une fois lues (ou passées), elles ne reviennent plus.
 */
final class FirstVisitGuide implements Disposable {

    private final GuideOverlay  overlay;
    private final Texture       portrait;
    private final CasinoButtons buttons;
    private final PlayerProfile profile;
    private final String        guide;
    private final String[]      lines;

    /**
     * @param guide nom du guide dans le profil (voir {@link PlayerProfile#GUIDE_EXPLORATION}...)
     * @param lines répliques du Croupier, dans l'ordre
     */
    FirstVisitGuide(Stage stage, HudTextures hud, Sound clickSound, PlayerProfile profile, String guide, String... lines) {
        this.profile = profile;
        this.guide   = guide;
        this.lines   = lines;
        portrait = EnemyTextures.newCroupierPortrait();
        buttons  = new CasinoButtons();
        overlay  = new GuideOverlay(hud, portrait);
        overlay.setSkipButton(buttons.create("Passer", clickSound, () -> {
            profile.markSeen(guide);
            overlay.stop();
        }));
        stage.addActor(overlay);
        if (!profile.hasSeen(guide)) replay();
    }

    /** Le Croupier redit ses répliques (bouton « Tutoriel » de l'écran), même déjà lues. */
    void replay() {
        if (overlay.isActive()) return;
        overlay.toFront();
        List<GuideOverlay.Step> steps = new ArrayList<>();
        for (String line : lines) steps.add(GuideOverlay.Step.say(line));
        overlay.play(steps, () -> profile.markSeen(guide));
    }

    /** @return {@code true} tant que le Croupier parle : l'écran dessous ne reçoit pas les clics. */
    boolean isActive() { return overlay.isActive(); }

    /** Repasse le Croupier devant les acteurs ajoutés depuis (à appeler avant de remettre le fondu devant). */
    void toFront() { overlay.toFront(); }

    @Override
    public void dispose() {
        overlay.dispose();
        portrait.dispose();
        buttons.dispose();
    }
}
