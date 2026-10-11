package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Reflet arc-en-ciel des cartes spéciales en pleine illustration, façon cartes
 * holographiques : une bande irisée balaie la carte en diagonale, et un léger
 * voile arc-en-ciel suit les zones claires. Le calcul se fait par case de la
 * grille du pixel art (80 x 112), pour que le reflet reste pixelisé.
 *
 * Un seul programme partagé, compilé à la première utilisation ; s'il ne
 * compile pas, les cartes s'affichent simplement sans reflet.
 */
public final class FoilShader {

    private static final String VERTEX =
        "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
        + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
        + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
        + "uniform mat4 u_projTrans;\n"
        + "varying vec4 v_color;\n"
        + "varying vec2 v_uv;\n"
        + "void main() {\n"
        + "  v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
        + "  v_color.a = v_color.a * (255.0 / 254.0);\n"
        + "  v_uv = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
        + "  gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
        + "}\n";

    private static final String FRAGMENT =
        "#ifdef GL_ES\n"
        + "precision mediump float;\n"
        + "#endif\n"
        + "varying vec4 v_color;\n"
        + "varying vec2 v_uv;\n"
        + "uniform sampler2D u_texture;\n"
        + "uniform float u_time;\n"
        + "uniform float u_phase;\n"
        + "vec3 hue(float h) {\n"
        + "  return clamp(abs(mod(h * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);\n"
        + "}\n"
        + "void main() {\n"
        + "  vec4 c = texture2D(u_texture, v_uv);\n"
        + "  vec2 cell = floor(v_uv * vec2(80.0, 112.0));\n"
        + "  float d = (cell.x + cell.y * 0.7) / 158.4;\n"
        + "  float t = u_time + u_phase;\n"
        // bande qui traverse la carte puis marque une pause hors de la carte
        + "  float sweep = fract(t * 0.16) * 2.2 - 0.6;\n"
        + "  float band = 1.0 - smoothstep(0.0, 0.16, abs(d - sweep));\n"
        + "  band = floor(band * 4.0) / 4.0;\n"
        + "  float lum = dot(c.rgb, vec3(0.30, 0.59, 0.11));\n"
        + "  float veil = floor((0.05 + 0.10 * lum) * 16.0) / 16.0;\n"
        + "  vec3 rainbow = hue(d * 1.6 - t * 0.10);\n"
        + "  float k = veil + band * 0.65;\n"
        + "  vec3 rgb = mix(c.rgb, min(vec3(1.0), c.rgb * 0.6 + rainbow * 0.7 + 0.15), k);\n"
        + "  gl_FragColor = v_color * vec4(rgb, c.a);\n"
        + "}\n";

    private static ShaderProgram program;
    private static boolean failed;

    private FoilShader() {}

    /**
     * Passe {@code batch} sur le reflet (à rendre ensuite avec {@link #end}).
     *
     * @param phase décalage propre à chaque carte, pour qu'elles ne brillent pas toutes en même temps
     * @return {@code false} si le reflet n'est pas disponible (le batch n'est alors pas modifié)
     */
    public static boolean begin(Batch batch, float phase) {
        ShaderProgram shader = program();
        if (shader == null) return false;
        batch.setShader(shader);
        shader.setUniformf("u_time", (System.nanoTime() / 1_000_000L % 3_600_000L) / 1000f);
        shader.setUniformf("u_phase", phase);
        return true;
    }

    /** Rend au batch son programme par défaut. */
    public static void end(Batch batch) {
        batch.setShader(null);
    }

    private static ShaderProgram program() {
        if (program != null || failed) return program;
        ShaderProgram.pedantic = false;
        ShaderProgram compiled = new ShaderProgram(VERTEX, FRAGMENT);
        if (!compiled.isCompiled()) {
            Gdx.app.error("FoilShader", compiled.getLog());
            compiled.dispose();
            failed = true;
            return null;
        }
        program = compiled;
        return program;
    }
}
