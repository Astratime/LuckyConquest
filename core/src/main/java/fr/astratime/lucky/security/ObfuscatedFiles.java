package fr.astratime.lucky.security;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Façade posée sur {@link Gdx#files} au lancement : les fichiers lus depuis le
 * jeu ({@link FileType#Internal} et {@link FileType#Classpath}) reviennent
 * enveloppés dans un {@link ObfuscatedFileHandle} qui rétablit les assets
 * brouillés. Les autres emplacements (sauvegardes dans {@code External} /
 * {@code Local} / {@code Absolute}) sont rendus tels quels.
 *
 * <p>Transparente en développement : sans asset brouillé, chaque lecture passe
 * sans changement. On l'installe une fois via {@link #install()}.
 */
public final class ObfuscatedFiles implements Files {

    private final Files delegate;

    private ObfuscatedFiles(Files delegate) {
        this.delegate = delegate;
    }

    /**
     * Enveloppe {@link Gdx#files} si ce n'est pas déjà fait. À appeler au tout
     * début de {@code create()}, avant toute lecture d'asset.
     */
    public static void install() {
        if (Gdx.files == null || Gdx.files instanceof ObfuscatedFiles) return;
        Gdx.files = new ObfuscatedFiles(Gdx.files);
    }

    private FileHandle wrap(FileHandle handle, FileType type) {
        if (type == FileType.Internal || type == FileType.Classpath) {
            return new ObfuscatedFileHandle(handle);
        }
        return handle;
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        return wrap(delegate.getFileHandle(path, type), type);
    }

    @Override
    public FileHandle classpath(String path) {
        return wrap(delegate.classpath(path), FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path) {
        return wrap(delegate.internal(path), FileType.Internal);
    }

    @Override public FileHandle external(String path) { return delegate.external(path); }
    @Override public FileHandle absolute(String path) { return delegate.absolute(path); }
    @Override public FileHandle local(String path)    { return delegate.local(path); }

    @Override public String getExternalStoragePath()       { return delegate.getExternalStoragePath(); }
    @Override public boolean isExternalStorageAvailable()   { return delegate.isExternalStorageAvailable(); }
    @Override public String getLocalStoragePath()           { return delegate.getLocalStoragePath(); }
    @Override public boolean isLocalStorageAvailable()      { return delegate.isLocalStorageAvailable(); }
}
