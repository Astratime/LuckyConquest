package fr.astratime.lucky.security;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.io.File;

/**
 * {@link FileHandle} qui rétablit à la volée un fichier d'asset brouillé par
 * {@link AssetScrambler}. Tout est délégué au vrai handle fourni par le backend ;
 * seules les lectures d'octets sont « désbrouillées » si le contenu porte
 * l'en-tête de brouillage. Un fichier en clair (dev) traverse sans changement.
 *
 * <p>Les handles enfants ({@link #child}, {@link #sibling}, {@link #parent},
 * {@link #list}) sont eux aussi enveloppés, pour que les assets résolus par
 * chemin relatif (pages d'atlas, images de particules) passent par la même
 * couche.
 */
final class ObfuscatedFileHandle extends FileHandle {

    private final FileHandle delegate;

    ObfuscatedFileHandle(FileHandle delegate) {
        this.delegate = delegate;
    }

    /** Octets du fichier, rétablis si besoin. */
    @Override
    public byte[] readBytes() {
        byte[] data = delegate.readBytes();
        return AssetScrambler.isScrambled(data) ? AssetScrambler.unscramble(data) : data;
    }

    @Override
    public int readBytes(byte[] bytes, int offset, int size) {
        byte[] all = readBytes();
        int length = Math.min(size, all.length);
        System.arraycopy(all, 0, bytes, offset, length);
        return length;
    }

    @Override
    public String readString() {
        return new String(readBytes());
    }

    @Override
    public String readString(String charset) {
        return new String(readBytes(), Charset.forName(charset));
    }

    @Override
    public InputStream read() {
        // Les assets brouillés sont petits (art, données) : on les lit en entier.
        // Les autres (audio en streaming) gardent le flux natif du backend.
        if (AssetScrambler.shouldScramble(name())) return new ByteArrayInputStream(readBytes());
        return delegate.read();
    }

    @Override
    public Reader reader() {
        return new InputStreamReader(read());
    }

    @Override
    public Reader reader(String charset) {
        return new InputStreamReader(read(), Charset.forName(charset));
    }

    @Override
    public long length() {
        if (AssetScrambler.shouldScramble(name())) return readBytes().length;
        return delegate.length();
    }

    // --- Résolution de chemins : on garde l'enveloppe ------------------------

    @Override
    public FileHandle child(String name) {
        return new ObfuscatedFileHandle(delegate.child(name));
    }

    @Override
    public FileHandle sibling(String name) {
        return new ObfuscatedFileHandle(delegate.sibling(name));
    }

    @Override
    public FileHandle parent() {
        return new ObfuscatedFileHandle(delegate.parent());
    }

    @Override
    public FileHandle[] list() {
        FileHandle[] children = delegate.list();
        FileHandle[] wrapped = new FileHandle[children.length];
        for (int i = 0; i < children.length; i++) wrapped[i] = new ObfuscatedFileHandle(children[i]);
        return wrapped;
    }

    // --- Tout le reste : délégation directe ----------------------------------

    @Override public String path()                 { return delegate.path(); }
    @Override public String name()                 { return delegate.name(); }
    @Override public String extension()            { return delegate.extension(); }
    @Override public String nameWithoutExtension() { return delegate.nameWithoutExtension(); }
    @Override public String pathWithoutExtension() { return delegate.pathWithoutExtension(); }
    @Override public com.badlogic.gdx.Files.FileType type() { return delegate.type(); }
    @Override public File file()           { return delegate.file(); }
    @Override public boolean exists()              { return delegate.exists(); }
    @Override public boolean isDirectory()         { return delegate.isDirectory(); }
    @Override public long lastModified()           { return delegate.lastModified(); }
    @Override public String toString()             { return delegate.toString(); }
}
