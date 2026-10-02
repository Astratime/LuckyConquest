"""
Petite boîte à outils de synthèse sonore pour les bruitages de Lucky Conquest :
oscillateurs, enveloppes, bruits filtrés, résonances métalliques, réverbération,
mixage et export. Tout est en mono, 44,1 kHz, en tableaux numpy de flottants.
"""
import math
import os
import subprocess
import tempfile
import wave
import zlib

import numpy as np
from scipy import signal

SR = 44100

_rng = np.random.default_rng(0)


def seed(name):
    """Graine aléatoire propre à chaque son : il reste identique d'une génération à l'autre."""
    global _rng
    _rng = np.random.default_rng(zlib.crc32(name.encode()))


def rand(lo=0.0, hi=1.0, size=None):
    return _rng.uniform(lo, hi, size)


def n(seconds):
    return max(0, int(round(seconds * SR)))


def times(d):
    return np.arange(n(d)) / SR


# ---------------------------------------------------------------------------
# Hauteurs
# ---------------------------------------------------------------------------

_SEMITONES = {'C': 0, 'D': 2, 'E': 4, 'F': 5, 'G': 7, 'A': 9, 'B': 11}


def hz(note):
    """'A4' -> 440 Hz ; accepte les dièses ('C#5') et les bémols ('Bb3')."""
    name, rest = note[0], note[1:]
    semi = _SEMITONES[name]
    while rest and rest[0] in '#b':
        semi += 1 if rest[0] == '#' else -1
        rest = rest[1:]
    midi = 12 * (int(rest) + 1) + semi
    return 440.0 * 2 ** ((midi - 69) / 12)


def transpose(f, semitones):
    return f * 2 ** (semitones / 12)


# ---------------------------------------------------------------------------
# Oscillateurs et bruits
# ---------------------------------------------------------------------------

def _freq_array(freq, d):
    if np.ndim(freq) == 0:
        return np.full(n(d), float(freq))
    f = np.asarray(freq, dtype=float)
    if len(f) < n(d):
        f = np.concatenate([f, np.full(n(d) - len(f), f[-1])])
    return f[:n(d)]


def osc(freq, d, wave='sine', duty=0.5, phase=None):
    """Oscillateur à fréquence fixe ou variable (tableau de fréquences, une par échantillon)."""
    f = _freq_array(freq, d)
    start = rand(0, 2 * math.pi) if phase is None else phase
    ph = start + 2 * math.pi * np.cumsum(f) / SR
    if wave == 'sine':
        return np.sin(ph)
    p = (ph / (2 * math.pi)) % 1.0
    if wave == 'square':
        return np.where(p < duty, 1.0, -1.0)
    if wave == 'tri':
        return 4 * np.abs(p - 0.5) - 1
    if wave == 'saw':
        return 2 * p - 1
    raise ValueError(wave)


def sweep(f0, f1, d, curve='exp'):
    """Fréquences glissant de f0 à f1 en d secondes."""
    k = np.linspace(0, 1, n(d), endpoint=False) if n(d) else np.zeros(0)
    if curve == 'exp':
        return f0 * (f1 / f0) ** k
    return f0 + (f1 - f0) * k


def vibrato(f, d, rate=5.5, depth=0.01, delay=0.0):
    """Ajoute un vibrato (profondeur relative) à une fréquence fixe ou variable."""
    t = times(d)
    ramp = np.clip((t - delay) / 0.15, 0, 1) if delay > 0 else 1.0
    return _freq_array(f, d) * (1 + depth * ramp * np.sin(2 * math.pi * rate * t))


def noise(d):
    return _rng.uniform(-1, 1, n(d))


# ---------------------------------------------------------------------------
# Enveloppes
# ---------------------------------------------------------------------------

def expdec(d, tau, attack=0.002):
    t = times(d)
    e = np.exp(-t / tau)
    if attack > 0:
        e *= np.clip(t / attack, 0, 1)
    return e


def adsr(d, a=0.005, dc=0.05, s=0.7, r=0.05):
    N = n(d)
    e = np.full(N, float(s))
    A, D, R = n(a), n(dc), n(r)
    A = min(A, N)
    e[:A] = np.linspace(0, 1, A, endpoint=False)
    seg = e[A:A + D]
    e[A:A + D] = np.linspace(1, s, len(seg))
    R = min(R, N)
    if R:
        e[N - R:] *= np.linspace(1, 0, R)
    return e


def ramp(d, a, b, curve=1.0):
    k = np.linspace(0, 1, n(d))
    return a + (b - a) * k ** curve


def fade(x, fin=0.003, fout=0.01):
    x = x.copy()
    i, o = min(n(fin), len(x)), min(n(fout), len(x))
    if i:
        x[:i] *= np.linspace(0, 1, i)
    if o:
        x[len(x) - o:] *= np.linspace(1, 0, o)
    return x


# ---------------------------------------------------------------------------
# Filtres
# ---------------------------------------------------------------------------

def _clamp_fc(fc):
    return min(max(fc, 10.0), SR * 0.45)


def lp(x, fc, order=2):
    return signal.sosfilt(signal.butter(order, _clamp_fc(fc), 'low', fs=SR, output='sos'), x)


def hp(x, fc, order=2):
    return signal.sosfilt(signal.butter(order, _clamp_fc(fc), 'high', fs=SR, output='sos'), x)


def bp(x, lo, hi, order=2):
    return signal.sosfilt(signal.butter(order, [_clamp_fc(lo), _clamp_fc(hi)], 'band', fs=SR, output='sos'), x)


def svf(x, fc, q=0.8, mode='lp'):
    """Filtre à variables d'état (Chamberlin) dont la coupure peut varier dans le temps."""
    fc = np.clip(_freq_array(fc, len(x) / SR), 10, SR / 6.5)
    f = 2 * np.sin(np.pi * fc / SR)
    damp = 1.0 / q
    low = band = 0.0
    out = np.empty(len(x))
    pick = {'lp': 0, 'bp': 1, 'hp': 2}[mode]
    for i, xi in enumerate(x):
        low += f[i] * band
        high = xi - low - damp * band
        band += f[i] * high
        out[i] = low if pick == 0 else band if pick == 1 else high
    return out


# ---------------------------------------------------------------------------
# Timbres
# ---------------------------------------------------------------------------

def modal(freq, d, ratios, decays, amps=None, attack=0.001):
    """Somme de partiels amortis : métal, verre, bois, cloche selon les rapports choisis."""
    t = times(d)
    amps = amps or [1.0 / (k + 1) for k in range(len(ratios))]
    out = np.zeros(len(t))
    for r, tau, a in zip(ratios, decays, amps):
        f = freq * r
        if f >= SR * 0.45:
            continue
        out += a * np.exp(-t / tau) * np.sin(2 * math.pi * f * t + rand(0, 2 * math.pi))
    if attack > 0:
        out *= np.clip(t / attack, 0, 1)
    return out


BAR_RATIOS = [1, 2.756, 5.404, 8.933]
GLASS_RATIOS = [1, 2.32, 4.25, 6.63]
WOOD_RATIOS = [1, 2.57, 4.1]
CHURCH_RATIOS = [0.5, 1, 1.183, 1.506, 2.0, 2.514, 2.662, 3.011, 4.166]


def thump(f0, f1, d, tau=0.12):
    """Coup sourd : sinus dont la hauteur chute (grosse caisse, impact)."""
    return osc(sweep(f0, f1, d), d) * expdec(d, tau, attack=0.001)


def click(d=0.006, lo=2000, hi=8000):
    return bp(noise(d), lo, hi) * expdec(d, d / 3, attack=0.0002)


def whoosh(d, f0, f1, q=1.2, curve=1.0):
    """Souffle : bruit passé dans un filtre passe-bande qui glisse de f0 à f1."""
    fc = sweep(f0, f1, d) if curve == 1.0 else f0 + (f1 - f0) * np.linspace(0, 1, n(d)) ** curve
    env = np.sin(np.linspace(0, math.pi, n(d))) ** 1.5
    return svf(noise(d), fc, q=q, mode='bp') * env


def chip(f, d, wave='square', duty=0.25, a=0.004, dc=0.06, s=0.55, r=0.04, vib=0.0):
    freq = vibrato(f, d, depth=vib, delay=0.08) if vib else f
    return osc(freq, d, wave, duty) * adsr(d, a, dc, s, r)


def pluck(f, d, tau=None, bright=6000):
    """Corde pincée (Karplus-Strong simplifié)."""
    N = n(d)
    period = max(2, int(round(SR / f)))
    buf = _rng.uniform(-1, 1, period)
    out = np.empty(N)
    decay = 0.996 if tau is None else math.exp(-period / (tau * SR))
    for i in range(N):
        j = i % period
        out[i] = buf[j]
        buf[j] = decay * 0.5 * (buf[j] + buf[(j + 1) % period])
    return lp(out, bright)


def brass(f, d, cutoff=(500, 2600, 1400), vib=0.012):
    """Cuivre synthétique : deux dents de scie désaccordées, filtre qui s'ouvre puis se referme."""
    freq = vibrato(f, d, rate=5.5, depth=vib, delay=0.18)
    x = osc(freq, d, 'saw') + 0.7 * osc(freq * 1.004, d, 'saw')
    N = n(d)
    k = np.linspace(0, 1, N)
    att = min(0.06 / max(d, 1e-3), 0.5)
    fc = np.where(k < att, cutoff[0] + (cutoff[1] - cutoff[0]) * k / att,
                  cutoff[1] + (cutoff[2] - cutoff[1]) * (k - att) / (1 - att))
    return svf(x, fc, q=0.9) * adsr(d, 0.02, 0.1, 0.8, 0.08)


def coin(f=1975.5, d=0.45, second=1.335):
    """Pièce : deux notes de cloche claire, la seconde plus haute (« ding-ding »)."""
    a = modal(f, 0.07, BAR_RATIOS, [0.05, 0.03, 0.02, 0.01])
    b = modal(f * second, d, BAR_RATIOS, [0.22, 0.09, 0.05, 0.02])
    return mix((a, 0.0, 0.8), (b, 0.06, 1.0))


def sparkle(d, count=14, lo=3000, hi=9000, tau=0.08):
    """Scintillement : petites notes cristallines au hasard."""
    track = Track(d)
    for _ in range(count):
        f = rand(lo, hi)
        track.add(modal(f, 0.3, [1, 2.0], [tau, tau / 2]), rand(0, max(d - 0.1, 0.01)), rand(0.3, 1))
    return track.buf


def reverb(x, wet=0.25, size=1.0, damp=4500, tail=1.0):
    """Réverbération de Schroeder (peignes en parallèle puis passe-tout en série)."""
    dry = np.concatenate([x, np.zeros(n(tail))])
    acc = np.zeros(len(dry))
    for ms, g in ((29.7, 0.805), (37.1, 0.827), (41.1, 0.783), (43.7, 0.764)):
        D = int(ms * size * SR / 1000)
        a = np.zeros(D + 1)
        a[0], a[D] = 1, -g
        acc += signal.lfilter([1], a, dry)
    for ms, g in ((5.0, 0.7), (1.7, 0.7)):
        D = int(ms * SR / 1000)
        b = np.zeros(D + 1)
        b[0], b[D] = -g, 1
        a = np.zeros(D + 1)
        a[0], a[D] = 1, -g
        acc = signal.lfilter(b, a, acc)
    acc = lp(acc, damp)
    return dry + wet * acc / 4


def drive(x, amount=2.0):
    return np.tanh(x * amount) / np.tanh(amount)


# ---------------------------------------------------------------------------
# Mixage
# ---------------------------------------------------------------------------

class Track:
    """Piste de mixage qui s'allonge à mesure qu'on y pose des sons."""

    def __init__(self, d=0.0):
        self.buf = np.zeros(n(d))

    def add(self, sound, at=0.0, gain=1.0):
        i = n(at)
        end = i + len(sound)
        if end > len(self.buf):
            self.buf = np.concatenate([self.buf, np.zeros(end - len(self.buf))])
        self.buf[i:end] += gain * sound
        return self


def mix(*parts):
    """mix((son, instant, gain), ...)"""
    track = Track()
    for sound, at, gain in parts:
        track.add(sound, at, gain)
    return track.buf


def stack(*sounds):
    """Additionne des sons qui commencent ensemble, quelle que soit leur durée."""
    return mix(*((s, 0.0, 1.0) for s in sounds))


def pad(x, d):
    """Allonge (silence) ou coupe {@code x} à exactement d secondes."""
    N = n(d)
    return np.concatenate([x, np.zeros(N - len(x))]) if len(x) < N else x[:N]


# ---------------------------------------------------------------------------
# Finition et export
# ---------------------------------------------------------------------------

def active_rms_db(x):
    """Niveau moyen (dBFS) des passages audibles : les silences ne comptent pas."""
    frame = n(0.02)
    frames = len(x) // frame
    if frames == 0:
        return 20 * math.log10(max(np.sqrt(np.mean(x ** 2)), 1e-9))
    power = np.mean(x[:frames * frame].reshape(frames, frame) ** 2, axis=1)
    loud = power[power > power.max() * 0.05]
    return 10 * math.log10(max(loud.mean(), 1e-12))


def finish(x, level_db, peak_db=-1.0, trim=True, fin=0.002, fout=0.02, loop=False):
    """
    Retire le continu, règle le niveau moyen sur level_db, limite les crêtes, coupe le silence final.
    Une boucle ({@code loop}) n'est ni coupée ni fondue, et reste périodique.
    """
    x = np.asarray(x, dtype=float)
    if loop:
        x = hp(np.tile(x, 3), 25)[len(x):2 * len(x)]
        trim, fin, fout = False, 0.0, 0.0
    else:
        x = hp(x, 25)
    if trim:
        loud = np.nonzero(np.abs(x) > np.abs(x).max() * 10 ** (-60 / 20))[0]
        if len(loud):
            x = x[:loud[-1] + n(0.01)]
    x = x * 10 ** ((level_db - active_rms_db(x)) / 20)
    limit = 10 ** (peak_db / 20)
    knee = 0.7 * limit
    over = np.abs(x) > knee
    x[over] = np.sign(x[over]) * (knee + (limit - knee) * np.tanh((np.abs(x[over]) - knee) / (limit - knee)))
    return fade(x, fin, fout)


def write(path, x, fmt='ogg'):
    """Écrit le son en .ogg (Vorbis, via ffmpeg) ou en .wav (16 bits, pour les boucles sans blanc)."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    pcm = (np.clip(x, -1, 1) * 32767).astype('<i2')
    if fmt == 'wav':
        with wave.open(path, 'wb') as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes(pcm.tobytes())
        return
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp:
        tmp_path = tmp.name
    try:
        write(tmp_path, x, 'wav')
        subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', tmp_path, '-c:a', 'libvorbis', '-q:a', '6',
                        path], check=True)
    finally:
        os.remove(tmp_path)
