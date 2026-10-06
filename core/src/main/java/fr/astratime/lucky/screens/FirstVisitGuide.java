package fr.astratime.lucky.screens;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.GuideOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
    /** Le script du Croupier, refait à chaque lecture (les étapes se souviennent de leur départ). */
    private final Supplier<List<GuideOverlay.Step>> script;

    /**
     * @param guide nom du guide dans le profil (voir {@link PlayerProfile#GUIDE_EXPLORATION}...)
     * @param lines répliques du Croupier, dans l'ordre
     */
    FirstVisitGuide(Stage stage, HudTextures hud, Sound clickSound, PlayerProfile profile, String guide, String... lines) {
        this(stage, hud, clickSound, profile, guide, () -> {
            List<GuideOverlay.Step> steps = new ArrayList<>();
            for (String line : lines) steps.add(GuideOverlay.Step.say(line));
            return steps;
        });
    }

    /**
     * Comme {@link #FirstVisitGuide(Stage, HudTextures, Sound, PlayerProfile, String, String...)}, avec des étapes
     * où le Croupier montre une partie de l'écran ou attend que le joueur la manipule.
     *
     * @param script fabrique les étapes, à chaque lecture
     */
    FirstVisitGuide(Stage stage, HudTextures hud, Sound clickSound, PlayerProfile profile, String guide,
                    Supplier<List<GuideOverlay.Step>> script) {
        this.profile = profile;
        this.guide   = guide;
        this.script  = script;
        portrait = EnemyTextures.newCroupierPortrait();
        buttons  = new CasinoButtons();
        overlay  = new GuideOverlay(hud, portrait);
        overlay.setSkipButton(buttons.create(Lang.t("Passer"), clickSound, () -> {
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
        overlay.play(script.get(), () -> profile.markSeen(guide));
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
