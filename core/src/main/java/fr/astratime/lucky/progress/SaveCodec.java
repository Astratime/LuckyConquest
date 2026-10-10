package fr.astratime.lucky.progress;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Format du fichier de sauvegarde : les valeurs du profil, brouillées (pour
 * qu'elles ne se lisent pas dans un éditeur de texte) puis signées (HMAC-SHA256).
 * Un fichier modifié à la main n'a plus la bonne signature : il est refusé
 * ({@link #decode} lève {@link InvalidSaveException}).
 *
 * C'est une protection contre la triche facile, pas un coffre-fort : la clé est
 * dans le jeu.
 *
 * <pre>
 * LUCKY-CONQUEST-SAVE
 * version=1
 * data=&lt;valeurs brouillées, en Base64&gt;
 * sign=&lt;signature, en hexadécimal&gt;
 * </pre>
 */
public final class SaveCodec {

    /** Version du format, écrite dans chaque sauvegarde (les anciennes versions restent lisibles). */
    public static final int VERSION = 1;

    private static final String HEADER  = "LUCKY-CONQUEST-SAVE";
    private static final String VERSION_LINE = "version=";
    private static final String DATA_LINE    = "data=";
    private static final String SIGN_LINE    = "sign=";

    /** La sauvegarde est absente de ses lignes, abîmée ou modifiée à la main. */
    public static final class InvalidSaveException extends Exception {
        InvalidSaveException(String message) { super(message); }
    }

    private SaveCodec() {}

    /** @return le texte du fichier de sauvegarde des valeurs {@code values}. */
    public static String encode(Map<String, String> values) {
        StringBuilder payload = new StringBuilder();
        Base64.Encoder base64 = Base64.getEncoder();
        for (Map.Entry<String, String> entry : new TreeMap<>(values).entrySet()) {
            payload.append(base64.encodeToString(bytes(entry.getKey()))).append(' ')
                .append(base64.encodeToString(bytes(entry.getValue()))).append('\n');
        }
        String data = base64.encodeToString(scramble(bytes(payload.toString())));
        return HEADER + '\n' + VERSION_LINE + VERSION + '\n' + DATA_LINE + data + '\n'
            + SIGN_LINE + sign(VERSION, data) + '\n';
    }

    /**
     * @return les valeurs enregistrées dans {@code text}
     * @throws InvalidSaveException si le texte n'est pas une sauvegarde, vient d'une
     *         version plus récente du jeu, ou a été modifié (signature fausse)
     */
    public static Map<String, String> decode(String text) throws InvalidSaveException {
        String[] lines = text.strip().split("\r?\n");
        if (lines.length != 4 || !lines[0].equals(HEADER) || !lines[1].startsWith(VERSION_LINE)
            || !lines[2].startsWith(DATA_LINE) || !lines[3].startsWith(SIGN_LINE)) {
            throw new InvalidSaveException("format inconnu");
        }
        int version;
        try {
            version = Integer.parseInt(lines[1].substring(VERSION_LINE.length()));
        } catch (NumberFormatException e) {
            throw new InvalidSaveException("version illisible");
        }
        if (version < 1 || version > VERSION) throw new InvalidSaveException("version " + version + " inconnue");
        String data = lines[2].substring(DATA_LINE.length());
        byte[] expected = bytes(sign(version, data));
        byte[] actual   = bytes(lines[3].substring(SIGN_LINE.length()));
        if (!MessageDigest.isEqual(expected, actual)) throw new InvalidSaveException("signature fausse");

        Map<String, String> values = new TreeMap<>();
        try {
            Base64.Decoder base64 = Base64.getDecoder();
            String payload = new String(scramble(base64.decode(data)), StandardCharsets.UTF_8);
            for (String line : payload.split("\n")) {
                if (line.isEmpty()) continue;
                int space = line.indexOf(' ');
                if (space < 0) throw new InvalidSaveException("ligne abîmée");
                values.put(new String(base64.decode(line.substring(0, space)), StandardCharsets.UTF_8),
                    new String(base64.decode(line.substring(space + 1)), StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidSaveException("données abîmées");
        }
        return values;
    }

    // -------------------------------------------------------------------------
    // Brouillage et signature
    // -------------------------------------------------------------------------

    /** Brouille (ou débrouille : c'est la même opération) {@code data} avec un flux tiré de la clé. */
    private static byte[] scramble(byte[] data) {
        byte[] result = data.clone();
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[0];
            for (int i = 0; i < result.length; i++) {
                if (i % 32 == 0) {
                    sha.update(key(MASK));
                    sha.update(bytes(Integer.toString(i / 32)));
                    block = sha.digest();
                }
                result[i] ^= block[i % 32];
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    /** @return la signature HMAC-SHA256, en hexadécimal, de {@code data} au format {@code version}. */
    private static String sign(int version, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key(SIGNATURE), "HmacSHA256"));
            mac.update(bytes(HEADER + '|' + version + '|'));
            return HexFormat.of().formatHex(mac.doFinal(bytes(data)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    // Les clés ne sont pas écrites en clair dans le jeu : elles sont reconstituées à partir de ces octets.
    private static final int[] SIGNATURE = {0x4c, 0x1f, 0x9a, 0x63, 0xd2, 0x07, 0xb8, 0x3e, 0x71, 0xc5, 0x2a,
        0xe9, 0x56, 0x0b, 0x94, 0xf3, 0x38, 0xad, 0x61, 0x1c, 0xc7, 0x8e, 0x45, 0xfa, 0x13, 0xb6, 0x6d, 0x29};
    private static final int[] MASK = {0xa3, 0x5e, 0x17, 0xc8, 0x62, 0x9d, 0x34, 0xeb, 0x0f, 0x7a, 0xd1, 0x46,
        0xbc, 0x28, 0x93, 0x5f, 0xe4};
    private static final int SALT = 0x5a;

    private static byte[] key(int[] parts) {
        byte[] key = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) key[i] = (byte) (parts[i] ^ (SALT + i * 7));
        return key;
    }

    private static byte[] bytes(String text) { return text.getBytes(StandardCharsets.UTF_8); }
}
