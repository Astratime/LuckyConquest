#!/usr/bin/env python3
"""
Génère les musiques de combat proposées par Claude dans assets/music (une par lieu,
une pour la Tour des épreuves, une pour ses boss). Astra peut remplacer n'importe
laquelle par sa propre musique : même nom de fichier, en .ogg.

    pip install numpy scipy        # ffmpeg (avec libvorbis) doit aussi être installé
    python3 tools/sounds/generate_music.py              # toutes les musiques
    python3 tools/sounds/generate_music.py combat_port  # seulement celles dont le nom commence ainsi

Chaque musique est une boucle : ce qui déborde de la fin (réverbération, notes
tenues) est replié sur son début, elle se raccorde sans blanc.
"""
import os
import sys

import numpy as np

from synth import (
    GLASS_RATIOS, SR, Track, bp, brass, stack, chip, expdec, finish, hz, lp, modal, n, noise, osc, pad, pluck, rand,
    reverb, seed, svf, sweep, thump, transpose, vibrato, write,
)
from generate_sounds import metal_hit

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
OUT = os.path.join(ROOT, 'assets', 'music')

MUSICS = []


def music(name, level=-19):
    def register(fn):
        MUSICS.append((name, level, fn))
        return fn
    return register


# ===========================================================================
# Instruments
# ===========================================================================

def f_of(note):
    return hz(note) if isinstance(note, str) else note


def kick(gain=1.0):
    return thump(120, 42, 0.28, 0.07) * gain


def snare(d=0.18, bright=1.0):
    body = osc(sweep(240, 160, 0.08), 0.08, 'sine') * expdec(0.08, 0.03)
    rattle = bp(noise(d), 1800, 7000 * bright) * expdec(d, d / 3.5)
    return pad(body * 0.5, d) + rattle


def hat(d=0.05, open_=False):
    d = 0.22 if open_ else d
    return bp(noise(d), 7000, 13000) * expdec(d, d / (2.5 if open_ else 4))


def brush(d=0.22):
    return bp(noise(d), 2500, 9000) * expdec(d, d / 2, attack=0.02) * 0.6


def tom(f=110, d=0.35):
    return thump(f * 1.4, f, d, d / 3)


def bass(note, d, bright=900, tau=None):
    f = f_of(note)
    return lp(pluck(f, d, tau=tau or d * 0.6, bright=bright) + 0.4 * osc(f, d, 'sine') * expdec(d, d * 0.7), 2500)


def accordion(note, d):
    """Accordéon : deux anches légèrement désaccordées, souffle qui gonfle et vibrato du soufflet."""
    f = f_of(note)
    env = np.minimum(1, np.arange(n(d)) / n(0.04)) * np.minimum(1, (n(d) - np.arange(n(d))) / n(0.06))
    reeds = osc(vibrato(f, d, 5.0, 0.004), d, 'saw') + osc(vibrato(f * 1.004, d, 5.3, 0.004), d, 'saw')
    return lp(reeds, min(3200, f * 6)) * env * 0.5


def vibes(note, d=1.2):
    """Vibraphone : lames de métal, trémolo du moteur."""
    f = f_of(note)
    tone = modal(f, d, GLASS_RATIOS, [d * 0.6, d * 0.25, 0.12, 0.05], [1, 0.25, 0.08, 0.03])
    return tone * (1 - 0.25 * (1 + np.sin(2 * np.pi * 5.5 * np.arange(len(tone)) / SR)) / 2)


def strings(notes, d):
    """Nappe de cordes : scies douces, entrée et sortie lentes."""
    N = n(d)
    env = np.minimum(1, np.arange(N) / n(min(0.4, d / 3))) * np.minimum(1, (N - np.arange(N)) / n(min(0.5, d / 3)))
    voices = sum(osc(vibrato(f_of(x) * (1 + 0.003 * k), d, 4.8, 0.003), d, 'saw') for k, x in enumerate(notes))
    return lp(voices, 1800) * env / max(1, len(notes))


def brass_chord(notes, d, gain=1.0):
    """Accord de cuivres (noms de notes ou fréquences)."""
    return stack(*(brass(f_of(x), d) for x in notes)) * gain / len(notes) ** 0.5


def lead(note, d, duty=0.25, vib=0.005):
    return chip(f_of(note), d, 'square', duty, a=0.006, dc=0.08, s=0.55, r=0.06, vib=vib)


def chord_notes(root, kind, octave=4):
    steps = {'m': (0, 3, 7), 'M': (0, 4, 7), 'm7': (0, 3, 7, 10), 'M7': (0, 4, 7, 11), '7': (0, 4, 7, 10),
             'dim': (0, 3, 6)}[kind]
    base = hz(f'{root}{octave}')
    return [transpose(base, s) for s in steps]


def scale_note(root, mode, degree, octave):
    """Degré {@code degree} (0 = tonique, peut dépasser 7) de la gamme {@code mode} sur {@code root}."""
    steps = {'minor': (0, 2, 3, 5, 7, 8, 10), 'harmonic': (0, 2, 3, 5, 7, 8, 11), 'dorian': (0, 2, 3, 5, 7, 9, 10),
             'phrygian': (0, 1, 4, 5, 7, 8, 10)}[mode]
    o, k = divmod(degree, 7)
    return transpose(hz(f'{root}{octave}'), 12 * o + steps[k])


def loop_of(track, d):
    """Replie ce qui dépasse {@code d} secondes sur le début : la boucle se raccorde sans blanc."""
    N = n(d)
    buf = pad(track.buf, d + 4.0)
    out = buf[:N].copy()
    out[:len(buf) - N] += buf[N:]
    return out


def looped_reverb(x, **kw):
    """Réverbération d'une boucle : la queue de la fin retombe sur le début."""
    wet = reverb(np.concatenate([x, x]), **kw)
    N = len(x)
    out = wet[N:2 * N].copy()
    return out


# ===========================================================================
# Musiques
# ===========================================================================

@music('combat_port')
def combat_port():
    """
    Le Port des Contrebandiers : chant de marins en Ré mineur, à 6/8 (une mesure = deux temps de trois croches).
    Accordéon qui tient les accords, basse qui balance, tambour et tambourin, mélodie de flûte en carré,
    vagues et cordages qui grincent en fond. 32 mesures (A A B A', deux fois).
    """
    eighth = 60 / 132 / 3 * 2          # 6/8 à 66 temps (noire pointée) par minute, bien balancé
    bar = 6 * eighth
    bars = 32
    d = bars * bar
    track = Track(d + 4)
    prog = ['Dm', 'Dm', 'C', 'Dm', 'Dm', 'Dm', 'C', 'A', 'F', 'C', 'Dm', 'A', 'Dm', 'Dm', 'C', 'Dm']
    roots = {'Dm': ('D', 'm'), 'C': ('C', 'M'), 'A': ('A', 'M'), 'F': ('F', 'M')}
    # Mélodie : degrés de Ré mineur (harmonique pour la sensible Do#), une liste par mesure, en croches
    a = [[4, None, 4, 5, None, 4], [3, None, 2, 1, None, 0], [2, None, 2, 3, None, 4], [5, None, None, 4, None, None]]
    a2 = [[4, None, 4, 5, None, 4], [3, None, 2, 1, None, 0], [2, None, 2, 3, None, 1], [0, None, None, None, None, None]]
    b = [[5, None, 5, 7, None, 5], [4, None, 3, 2, None, 4], [7, None, 6, 5, None, 4], [3, None, None, 2, None, 1]]
    melody = a + a + b + a2
    for i in range(bars):
        t0 = i * bar
        root, kind = roots[prog[i % 16]]
        chord = chord_notes(root, kind, 4)
        # accordéon : accord tenu sur chaque demi-mesure, le soufflet repousse
        for half in range(2):
            for f in chord:
                track.add(accordion(f, bar / 2 * 0.95), t0 + half * bar / 2, 0.16)
        # basse : fondamentale puis quinte
        track.add(bass(transpose(hz(f'{root}2'), 0), bar / 2 * 0.9), t0, 0.55)
        track.add(bass(transpose(hz(f'{root}2'), 7), bar / 2 * 0.9), t0 + bar / 2, 0.45)
        # tambour sur les deux temps, tambourin sur les croches
        track.add(tom(90, 0.3), t0, 0.55)
        track.add(tom(90, 0.3), t0 + bar / 2, 0.4)
        for k in range(6):
            jingle = bp(noise(0.08), 5000, 11000) * expdec(0.08, 0.025)
            track.add(jingle, t0 + k * eighth, 0.18 if k % 3 == 0 else 0.09)
        # flûte de pont (carré doux)
        for k, deg in enumerate(melody[i % 16]):
            if deg is None:
                continue
            length = 1
            while k + length < 6 and melody[i % 16][k + length] is None:
                length += 1
            track.add(lead(scale_note('D', 'harmonic', deg, 5), length * eighth * 0.92, duty=0.5, vib=0.006),
                      t0 + k * eighth, 0.2)
    # vagues et cordages
    waves = lp(noise(d), 600) * (0.55 + 0.45 * np.sin(2 * np.pi * np.arange(n(d)) / n(bar * 2)) ** 2)
    track.add(waves, 0, 0.12)
    for k in range(8):
        creak = svf(noise(0.5), sweep(300, 520, 0.5), q=6, mode='bp') * expdec(0.5, 0.2, attack=0.08)
        track.add(creak, (k * 4 + 2.5) * bar, 0.25)
    return looped_reverb(loop_of(track, d), wet=0.2, size=1.1, tail=0.6)


@music('combat_mines')
def combat_mines():
    """
    Les Mines d'Or : La phrygien (couleur orientale, inquiétante), 104 battements par minute.
    Enclumes et pioches en rythme, gros tambours qui résonnent dans la galerie, basse qui gronde,
    cuivres qui frappent, petite mélodie de clochettes de wagonnet. 32 mesures.
    """
    beat = 60 / 104
    bar = 4 * beat
    bars = 32
    d = bars * bar
    track = Track(d + 4)
    for i in range(bars):
        t0 = i * bar
        shift = [0, 0, 1, 0][i % 4]      # La, La, Si bémol, La : le demi-ton phrygien
        root = transpose(hz('A1'), shift)
        # basse en croches qui gronde
        for k in range(8):
            f = root if k % 4 != 3 else transpose(root, 7)
            track.add(lp(osc(f, beat * 0.45, 'saw'), 500) * expdec(beat * 0.45, 0.2), t0 + k * beat / 2, 0.35)
        # gros tambours de galerie
        track.add(tom(70, 0.6), t0, 0.8)
        track.add(tom(70, 0.6), t0 + 1.5 * beat, 0.5)
        track.add(tom(95, 0.4), t0 + 2.5 * beat, 0.45)
        # enclume sur le 2 et le 4, pioche en contretemps
        track.add(metal_hit(1400 + 60 * shift, 0.6), t0 + beat, 0.22)
        track.add(metal_hit(1400 + 60 * shift, 0.6), t0 + 3 * beat, 0.26)
        for k in (0.5, 2.5, 3.5):
            pick = bp(noise(0.05), 3000, 9000) * expdec(0.05, 0.012)
            track.add(pick, t0 + k * beat, 0.25)
        # cuivres : accord frappé toutes les deux mesures
        if i % 2 == 0:
            notes = ['A3', 'C4', 'E4'] if shift == 0 else ['Bb3', 'D4', 'F4']
            track.add(brass_chord(notes, beat * 1.4, 0.7), t0, 0.32)
        # clochettes de wagonnet sur la deuxième moitié
        if i >= 8:
            line = [7, 6, 5, 4, 5, 4, 3, 1][(i - 8) % 8]
            for k in range(2):
                f = scale_note('A', 'phrygian', line - k, 5)
                track.add(modal(f, 0.7, GLASS_RATIOS, [0.35, 0.12, 0.05, 0.02]), t0 + (k * 2 + 0.5) * beat, 0.14)
    # gouttes et grondement de la galerie
    track.add(lp(noise(d), 120), 0, 0.25)
    for k in range(20):
        drip = osc(sweep(1800, 900, 0.06), 0.06, 'sine') * expdec(0.06, 0.02)
        track.add(drip, (k * 1.6 + 0.7) * bar, 0.12)
    return looped_reverb(loop_of(track, d), wet=0.3, size=1.6, tail=1.0)


@music('combat_casino')
def combat_casino():
    """
    Le Casino Englouti : jazz de salle de jeu noyée, Mi mineur, swing à 116 battements par minute.
    Vibraphone, contrebasse qui marche, balais sur la caisse claire, piano de bar étouffé ;
    tout passe sous l'eau puis remonte (filtre qui s'ouvre et se ferme), bulles. 32 mesures (ii-V-i).
    """
    beat = 60 / 116
    swing = beat * 2 / 3               # croche swinguée : longue puis courte
    bar = 4 * beat
    bars = 32
    d = bars * bar
    track = Track(d + 4)
    prog = [('E', 'm7'), ('E', 'm7'), ('A', '7'), ('A', '7'), ('F#', 'm7'), ('B', '7'), ('E', 'm7'), ('B', '7')] * 4
    walk = {('E', 'm7'): ['E2', 'G2', 'B2', 'D3'], ('A', '7'): ['A2', 'C#3', 'E3', 'G2'],
            ('F#', 'm7'): ['F#2', 'A2', 'C#3', 'E3'], ('B', '7'): ['B2', 'A2', 'F#2', 'D#2']}
    vib_line = [['B4', 'D5', 'E5', None], ['G5', None, 'F#5', 'E5'], ['C#5', 'E5', 'G5', None], ['A5', 'G5', 'E5', None],
                ['F#5', 'A5', 'C#6', None], ['B5', 'A5', 'F#5', 'D#5'], ['E5', None, 'G5', 'B5'], ['A5', None, None, None]]
    for i, (root, kind) in enumerate(prog):
        t0 = i * bar
        for k, note in enumerate(walk[(root, kind)]):           # contrebasse qui marche
            track.add(bass(note, beat * 0.95, bright=700, tau=0.35), t0 + k * beat, 0.6)
        for k in range(4):                                       # balais et charleston au pied
            track.add(brush(), t0 + k * beat, 0.25)
            if k % 2 == 1:
                track.add(hat(0.05), t0 + k * beat, 0.25)
            track.add(hat(0.04), t0 + k * beat + swing, 0.12)
        chord = chord_notes(root, kind, 3)                       # piano de bar : accords sur le 2 et le 4
        for k in (1, 3):
            stab = sum(pluck(f, 0.4, tau=0.18, bright=2400) for f in chord)
            track.add(stab, t0 + k * beat + (swing if i % 4 == 3 and k == 3 else 0), 0.12)
        line = vib_line[i % 8] if i < 8 else vib_line[(i + 4) % 8]
        for k, note in enumerate(line):                          # vibraphone
            if note:
                track.add(vibes(note, 1.4), t0 + k * beat + (swing - beat / 2 if k % 2 else 0) * 0, 0.3)
    x = track.buf
    # sous l'eau : le filtre se ferme puis se rouvre lentement, deux fois par boucle
    cut = 900 + 3800 * (0.5 + 0.5 * np.cos(2 * np.pi * np.arange(len(x)) / n(d / 2)))
    x = svf(x, cut, q=0.9, mode='lp') * 0.8 + lp(x, 600) * 0.4
    out = Track(d + 4)
    out.add(x, 0, 1.0)
    for k in range(44):                                          # bulles
        bub = osc(sweep(400 + 300 * rand(), 1400 + 600 * rand(), 0.07), 0.07, 'sine') * expdec(0.07, 0.025)
        out.add(bub, rand(0, d), 0.12)
    out.add(lp(noise(d), 300), 0, 0.06)
    return looped_reverb(loop_of(out, d), wet=0.28, size=1.4, tail=0.9)


@music('combat_tour')
def combat_tour():
    """
    La Tour des épreuves : marche tendue en Do mineur, 120 battements par minute.
    Caisse claire militaire, cordes qui tiennent, ostinato de cuivres, cloches de l'horloge de la Tour
    toutes les quatre mesures. 32 mesures.
    """
    beat = 0.5
    bar = 4 * beat
    bars = 32
    d = bars * bar
    track = Track(d + 4)
    prog = [('C', 'm'), ('C', 'm'), ('Ab', 'M'), ('G', 'M'), ('C', 'm'), ('F', 'm'), ('G', 'M'), ('G', 'M')] * 4
    for i, (root, kind) in enumerate(prog):
        t0 = i * bar
        track.add(strings(chord_notes(root, kind, 3), bar * 1.02), t0, 0.35)
        for k in range(4):
            track.add(kick(0.8 if k % 2 == 0 else 0.5), t0 + k * beat, 0.6)
            track.add(snare(0.12), t0 + k * beat + beat / 2, 0.18)
        track.add(snare(0.2), t0 + beat, 0.4)
        track.add(snare(0.2), t0 + 3 * beat, 0.45)
        if i % 4 == 3:                                           # roulement avant la reprise
            for k in range(8):
                track.add(snare(0.08), t0 + 3 * beat + k * beat / 8, 0.12 + 0.03 * k)
        base = hz(f'{root}3')                                    # ostinato de cuivres en croches
        pattern = [0, 0, 7, 0, 3 if kind == 'm' else 4, 0, 7, 12]
        for k, s in enumerate(pattern):
            track.add(lp(chip(transpose(base, s), beat * 0.4, 'saw', 0.5, dc=0.05, s=0.5, r=0.04), 1800),
                      t0 + k * beat / 2, 0.14)
        track.add(bass(f'{root}2', bar * 0.9, tau=0.6), t0, 0.5)
        if i % 4 == 0:                                           # l'horloge de la Tour
            track.add(metal_hit(hz('C5'), 1.6), t0, 0.15)
        if i >= 8:                                               # thème au cor dans la deuxième moitié
            theme = [[0, None, 2, 3], [4, None, 3, 2], [0, None, -2, 0], [2, None, None, None]][i % 4]
            for k, deg in enumerate(theme):
                if deg is not None:
                    f = scale_note('C', 'harmonic', deg + 7, 3)
                    track.add(lp(osc(vibrato(f, beat * 0.95, 5, 0.006), beat * 0.95, 'saw'), 1400)
                              * expdec(beat * 0.95, 0.5, attack=0.03), t0 + k * beat, 0.22)
    return looped_reverb(loop_of(track, d), wet=0.25, size=1.5, tail=0.9)


@music('combat_boss')
def combat_boss():
    """
    Boss de la Tour : Fa dièse mineur, 152 battements par minute, sans répit.
    Grosse caisse à chaque temps, basse en croches, arpèges en carré, accords de cuivres qui claquent,
    sirène de jackpot qui monte toutes les huit mesures. 32 mesures.
    """
    beat = 60 / 152
    bar = 4 * beat
    bars = 32
    d = bars * bar
    track = Track(d + 4)
    prog = [('F#', 'm'), ('F#', 'm'), ('D', 'M'), ('E', 'M'), ('F#', 'm'), ('C#', 'M'), ('D', 'M'), ('C#', 'M')] * 4
    for i, (root, kind) in enumerate(prog):
        t0 = i * bar
        chord = chord_notes(root, kind, 4)
        for k in range(4):
            track.add(kick(1.0), t0 + k * beat, 0.7)
            track.add(hat(0.04, open_=(k % 2 == 1)), t0 + k * beat + beat / 2, 0.2)
        track.add(snare(0.2, 1.2), t0 + beat, 0.5)
        track.add(snare(0.2, 1.2), t0 + 3 * beat, 0.55)
        for k in range(8):                                       # basse en croches, octave sur les contretemps
            f = transpose(hz(f'{root}1'), 12 if k % 2 else 0)
            track.add(lp(osc(f, beat * 0.45, 'saw'), 700) * expdec(beat * 0.45, 0.15), t0 + k * beat / 2, 0.42)
        arp = chord + [transpose(chord[0], 12)]                  # arpèges en doubles croches
        for k in range(16):
            f = transpose(arp[[0, 1, 2, 3, 2, 1][k % 6] if k % 8 < 6 else k % 4], 12)
            track.add(lead(f, beat / 4 * 0.85, duty=0.125, vib=0.0), t0 + k * beat / 4, 0.07)
        if i % 2 == 0:                                           # cuivres qui claquent
            track.add(brass_chord([chord[0], chord[1], chord[2]], beat * 0.7, 0.8), t0, 0.3)
            track.add(brass_chord([chord[0], chord[1], chord[2]], beat * 0.4, 0.8), t0 + 2.5 * beat, 0.22)
        if i % 8 == 6:                                           # sirène de jackpot
            siren = osc(sweep(400, 1600, bar * 2, curve='exp'), bar * 2, 'tri') * np.linspace(0, 1, n(bar * 2))
            track.add(lp(siren, 3000), t0, 0.1)
        if i >= 8:                                               # le thème au carré passe devant
            theme = [[0, 2, 4, 2], [5, 4, 2, 0], [6, 5, 4, 2], [4, None, None, None]][i % 4]
            for k, deg in enumerate(theme):
                if deg is not None:
                    track.add(lead(scale_note('F#', 'harmonic', deg, 5), beat * 0.9, duty=0.25),
                              t0 + k * beat, 0.16)
    return looped_reverb(loop_of(track, d), wet=0.16, size=0.9, tail=0.5)


def main(prefixes):
    done = 0
    for name, level, fn in MUSICS:
        if prefixes and not any(name.startswith(p) for p in prefixes):
            continue
        seed(name)
        x = finish(fn(), level, loop=True)
        write(os.path.join(OUT, f'{name}.ogg'), x, 'ogg')
        print(f'{name:16s} {len(x) / SR:5.1f} s')
        done += 1
    if done == 0:
        sys.exit(f'Aucune musique ne correspond à {prefixes}')


if __name__ == '__main__':
    main(sys.argv[1:])
