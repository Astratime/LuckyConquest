#!/usr/bin/env python3
"""
Génère tous les bruitages synthétiques de Lucky Conquest dans assets/sounds.

    pip install numpy scipy        # ffmpeg (avec libvorbis) doit aussi être installé
    python3 tools/sounds/generate_sounds.py            # tous les sons
    python3 tools/sounds/generate_sounds.py bingo/bell # seulement ceux dont le nom commence ainsi

Chaque son est une fonction décorée par @sound(nom, niveau) : le nom donne le
fichier (assets/sounds/<nom>.ogg), le niveau le volume moyen visé en dBFS.
Les sons d'un même son restent identiques d'une génération à l'autre (graine
tirée de leur nom).

Les instants des sons de Bingo, de la machine et du suspense reprennent les
constantes des animations du jeu (indiquées en commentaire) : si une animation
change de rythme, mettre à jour l'instant ici puis régénérer.
"""
import math
import os
import sys

import numpy as np

from synth import (
    BAR_RATIOS, CHURCH_RATIOS, GLASS_RATIOS, WOOD_RATIOS, SR, Track, adsr, bp, brass, chip, click, coin,
    drive, expdec, fade, stack, finish, hp, hz, lp, mix, modal, n, noise, osc, pad, pluck, rand, ramp, reverb, seed,
    sparkle, svf, sweep, thump, times, transpose, vibrato, whoosh, write,
)

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
OUT = os.path.join(ROOT, 'assets', 'sounds')

SOUNDS = []


def sound(name, level, fmt='ogg', trim=True, loop=False):
    def register(fn):
        SOUNDS.append((name, level, fmt, trim, loop, fn))
        return fn
    return register


# ===========================================================================
# Briques communes
# ===========================================================================

def card_slap(d=0.18):
    """Carte posée d'un coup sec sur le feutre."""
    snap = bp(noise(0.05), 900, 5000) * expdec(0.05, 0.012, attack=0.0005)
    body = lp(noise(d), 700) * expdec(d, 0.03)
    return mix((snap, 0, 1.0), (body, 0.002, 0.8), (thump(180, 70, 0.12, 0.03), 0, 0.6))


def card_flick(d=0.06):
    """Carte qui glisse rapidement (frottement de papier)."""
    return svf(noise(d), sweep(2500, 6000, d), q=1.0, mode='bp') * expdec(d, d / 3, attack=0.004)


def metal_hit(f, d=0.8, bright=1.0):
    """Choc métallique : partiels inharmoniques d'une barre + attaque bruitée."""
    ring = modal(f, d, [1, 2.4, 3.9, 5.6, 7.3], [d * 0.6, d * 0.4, d * 0.28, d * 0.18, d * 0.1],
                 [1, 0.6 * bright, 0.45 * bright, 0.3 * bright, 0.2 * bright])
    hit = bp(noise(0.03), 1500, 7000) * expdec(0.03, 0.006)
    return mix((ring, 0, 1.0), (hit, 0, 0.6))


def blade_swish(d=0.16, f0=700, f1=4500):
    return whoosh(d, f0, f1, q=1.6)


def blade_ring(f=3100, d=0.9):
    """Lame qui chante (« shing »)."""
    ring = modal(f, d, [1, 1.51, 2.43, 3.2], [d * 0.5, d * 0.35, d * 0.2, d * 0.12], [1, 0.5, 0.35, 0.2])
    scrape = svf(noise(0.12), sweep(5000, 9000, 0.12), q=2.0, mode='bp') * expdec(0.12, 0.05, attack=0.01)
    return mix((scrape, 0, 0.7), (ring * 0.6, 0.02, 1.0))


def impact(d=0.35, f0=140, f1=45):
    body = thump(f0, f1, d, tau=0.1)
    crack = lp(noise(0.08), 3000) * expdec(0.08, 0.018)
    return mix((body, 0, 1.0), (crack, 0, 0.7))


def heartbeat(at_list, f=60, gain=1.0):
    track = Track()
    for t in at_list:
        track.add(thump(f * 1.6, f, 0.16, 0.05), t, gain)
        track.add(thump(f * 1.4, f * 0.9, 0.14, 0.04), t + 0.13, gain * 0.7)
    return track.buf


def chips_clatter(count=7, d=0.35, f_lo=2200, f_hi=3600):
    """Jetons de casino qui s'entrechoquent (claquements de céramique)."""
    track = Track(d)
    for k in range(count):
        at = (k / count) * d * 0.8 + rand(0, 0.03)
        clack = stack(modal(rand(f_lo, f_hi), 0.07, [1, 1.83, 2.9], [0.03, 0.015, 0.008]), click(0.004, 3000, 9000))
        track.add(clack, at, rand(0.5, 1.0))
    return track.buf


def coin_shower(d, count, lo=1700, hi=2900):
    track = Track(d)
    for _ in range(count):
        f = rand(lo, hi)
        ping = modal(f, 0.25, BAR_RATIOS, [0.12, 0.05, 0.03, 0.015], [1, 0.5, 0.3, 0.15])
        track.add(ping, rand(0, max(d - 0.1, 0.01)), rand(0.25, 0.8))
    return track.buf


def cash_register_bell():
    return modal(hz('A6'), 0.6, BAR_RATIOS, [0.35, 0.15, 0.07, 0.03], [1, 0.45, 0.25, 0.1])


def power_up(notes, step=0.045, wave='square', duty=0.25, last=0.25):
    track = Track()
    for k, note in enumerate(notes):
        d = last if k == len(notes) - 1 else step * 1.6
        track.add(chip(hz(note), d, wave, duty, dc=0.03, s=0.5, r=0.03), k * step, 0.8)
    return track.buf


# Le son de Bingo classique du jeu (assets/sounds/bingo_3_symbols.wav) est joué en même temps que chaque
# scène : l'arpège « BINGO ! » des scènes reste en retrait pour ne pas se marcher dessus.
SCENE_JINGLE = 0.55


def bingo_jingle(root='C5', wave='square', duty=0.25, step=0.065, hold=0.75, chord=(0, 4, 7, 12)):
    """« BINGO ! » : arpège qui monte puis accord tenu en tremolo, transposé et colorié selon la scène."""
    f0 = hz(root)
    track = Track()
    for k, semi in enumerate(chord):
        track.add(chip(transpose(f0, semi), step * 1.5, wave, duty, dc=0.03, s=0.6, r=0.02), k * step, 0.7)
    at = len(chord) * step
    tremolo = 1 - 0.35 * (0.5 + 0.5 * np.sin(2 * math.pi * 12 * times(hold)))
    for semi in chord:
        tone = chip(transpose(f0, semi + 12 if semi == 0 else semi), hold, wave, duty, a=0.005, dc=0.1, s=0.7,
                    r=0.25, vib=0.006)
        track.add(tone * tremolo, at, 0.42)
    track.add(sparkle(hold, count=10, tau=0.07), at, 0.35)
    return track.buf * SCENE_JINGLE


def fire_burst(d=1.2):
    """Embrasement : souffle grave qui s'ouvre puis crépite."""
    fc = np.concatenate([sweep(250, 3500, 0.12), sweep(3500, 600, d - 0.12)])
    roar = svf(noise(d), fc, q=0.7) * expdec(d, d * 0.35, attack=0.01)
    crackle = Track(d)
    for _ in range(int(d * 60)):
        at = rand(0, d) ** 1.6 * d / max(d ** 0.6, 1e-3)
        crackle.add(bp(noise(0.006), 1800, 6000) * expdec(0.006, 0.0015), min(at, d - 0.01), rand(0.2, 0.9))
    return mix((roar, 0, 1.0), (crackle.buf * expdec(d, d * 0.45), 0, 0.5), (thump(90, 40, 0.4, 0.15), 0, 0.8))


# ===========================================================================
# Interface
# ===========================================================================

@sound('ui/menu_hover', -30)
def menu_hover():
    """Survol d'une option du menu : petit « tic » feutré, à peine une note."""
    tone = osc(sweep(1400, 1800, 0.05), 0.05, 'tri') * expdec(0.05, 0.015, attack=0.001)
    return mix((tone, 0, 1.0), (click(0.004, 2500, 7000), 0, 0.5))


def chip_tap(f, d=0.12):
    """Jeton de casino effleuré : petit claquement de céramique, net et boisé."""
    body = modal(f, d, [1, 1.83, 2.9, 4.1], [0.035, 0.018, 0.009, 0.005], [1, 0.6, 0.35, 0.2])
    return mix((body, 0, 1.0), (click(0.003, 2500, 9000), 0, 0.5), (thump(300, 180, 0.04, 0.01), 0, 0.25))


@sound('ui/card_hover', -30)
def card_hover():
    """Survol d'une carte : deux jetons de casino qui se touchent (« tic-tac » de table de jeu)."""
    return mix((chip_tap(2900), 0, 0.8), (chip_tap(3400), 0.045, 1.0))


@sound('ui/pile_hover', -30)
def pile_hover():
    """Survol du deck ou de la défausse : pile de jetons égrenée du pouce."""
    track = Track()
    for k, f in enumerate((2500, 2750, 3000, 3300)):
        track.add(chip_tap(f, 0.08), k * 0.03, 0.6 + 0.12 * k)
    return track.buf


@sound('ui/shop_hover', -27)
def shop_hover():
    """Survol de l'échoppe : clochette de porte de boutique (« ding-ding »)."""
    bell = lambda f: modal(f, 0.45, [1, 2.76, 5.4], [0.18, 0.08, 0.04], [1, 0.4, 0.15])
    return mix((bell(hz('E6')), 0, 0.8), (bell(hz('A6')), 0.09, 1.0))


@sound('ui/card_inspect', -26)
def card_inspect():
    """Clic droit sur une carte : la fiche s'ouvre avec un « whoop » qui monte."""
    d = 0.18
    tone = osc(sweep(380, 1300, d), d) * adsr(d, 0.01, 0.05, 0.6, 0.08)
    air = whoosh(d, 800, 4000, q=0.8)
    return mix((tone, 0, 0.8), (air, 0, 0.5), (click(0.004), 0, 0.3))


@sound('ui/card_play', -20)
def card_play():
    """Clic gauche : la carte est jouée et claque sur la table."""
    return mix((card_flick(0.05), 0, 0.5), (card_slap(), 0.035, 1.0))


# ===========================================================================
# Machine à sous
# ===========================================================================

@sound('slots/spin_button', -19)
def spin_button():
    """Bouton « Lancer machine » d'une machine moderne : clic du gros bouton, souffle qui monte et carillon de départ."""
    track = Track()
    track.add(mix((click(0.006, 1500, 6000), 0, 1.0), (thump(260, 120, 0.08, 0.02), 0, 0.7)), 0.0, 0.9)
    track.add(whoosh(0.45, 400, 5000, q=0.9, curve=2.0), 0.02, 0.6)
    rise = osc(sweep(300, 1200, 0.35), 0.35, 'tri') * adsr(0.35, 0.02, 0.05, 0.6, 0.12)
    track.add(lp(rise, 4000), 0.03, 0.35)
    for k, note in enumerate(['C6', 'G6', 'C7']):                    # carillon : la machine démarre
        track.add(modal(hz(note), 0.5, GLASS_RATIOS, [0.25, 0.12, 0.06, 0.03]), 0.12 + k * 0.05, 0.35)
    track.add(sparkle(0.35, count=6), 0.2, 0.25)
    return reverb(track.buf, wet=0.2, tail=0.4)


# Une seconde exacte, jouée en boucle pendant que les rouleaux tournent, façon machine moderne : défilement
# électronique feutré (souffle rythmé au passage des symboles), ronronnement doux et petite boucle
# d'arpège en clochettes. Ce qui déborde de la seconde est replié sur son début, et les filtres passent sur
# trois copies dont on garde celle du milieu : la boucle se raccorde sans blanc ni saut.
@sound('slots/reel_spin', -27, fmt='wav', loop=True)
def reel_spin():
    d, steps = 1.0, 16
    N = n(d)
    track = Track(2 * d)
    for k in range(steps):
        tick = stack(bp(noise(0.02), 1500, 5000) * expdec(0.02, 0.005), modal(rand(2300, 2500), 0.04, [1, 2.0], [0.01, 0.005]))
        track.add(tick, k * d / steps, 0.35 if k % 2 == 0 else 0.22)
    for k, note in enumerate(['C6', 'E6', 'G6', 'C7', 'A6', 'G6', 'E6', 'D6']):
        bell = modal(hz(note), 0.3, GLASS_RATIOS, [0.12, 0.05, 0.025, 0.01], [1, 0.3, 0.12, 0.05])
        track.add(bell, k * d / 8, 0.32)
    buf = pad(track.buf, 2 * d)
    loop = buf[:N] + buf[N:]
    whir = noise(d) * (0.55 + 0.45 * np.sin(2 * math.pi * steps * times(d)) ** 2)
    hum = osc(np.full(N, 110.0), d, phase=0.0) + 0.4 * osc(np.full(N, 220.0), d, phase=0.0)
    tiled = np.tile(loop + 0.12 * hum, 3) + 0.18 * bp(np.tile(whir, 3), 700, 2600)
    return lp(tiled, 7000)[N:2 * N]


@sound('slots/reel_stop', -22)
def reel_stop():
    """Un rouleau s'arrête sur son symbole : cran qui tombe, petit choc et tintement."""
    return mix((thump(220, 90, 0.12, 0.03), 0, 1.0), (click(0.005, 1500, 6000), 0, 0.8),
               (modal(hz('E6'), 0.25, BAR_RATIOS, [0.07, 0.03, 0.015, 0.008]), 0.004, 0.35))


# Joué quand le dernier rouleau ralentit (SlotView.SUSPENSE_TIME = 1,1 s avant son arrêt) : roulement de
# caisse claire qui accélère et note qui monte pendant 1,1 s, puis tenus au sommet le temps que le symbole
# se pose. Le jeu coupe le son à l'arrêt du rouleau.
@sound('slots/reel_suspense', -21, trim=False)
def reel_suspense():
    build, d = 1.1, 2.2
    track = Track(d)
    t, interval = 0.0, 0.11
    while t < d - 0.02:
        snare = stack(bp(noise(0.05), 1200, 7000) * expdec(0.05, 0.015), 0.3 * thump(240, 180, 0.04, 0.015))
        track.add(snare, t, 0.4 + 0.6 * min(t / build, 1.0))
        t += interval
        interval = max(0.03, interval * 0.9)
    f = np.concatenate([sweep(260, 780, build), np.full(n(d - build), 780.0)])
    level = np.concatenate([ramp(build, 0.1, 0.9, 1.5), np.full(n(d - build), 0.9)])
    track.add(lp(osc(vibrato(f, d, rate=7, depth=0.015), d, 'tri') * level, 3000), 0, 0.45)
    return fade(pad(track.buf, d), 0.002, 0.15)


@sound('slots/result_none', -23)
def result_none():
    """Tirage sans paire : deux notes neutres, sans déception marquée."""
    return mix((chip(hz('E5'), 0.12, 'tri', s=0.5), 0, 0.8), (chip(hz('C5'), 0.22, 'tri', s=0.5, r=0.1), 0.11, 0.8))


@sound('slots/result_pair', -20)
def result_pair():
    """Paire : petit arpège joyeux et une pièce."""
    return mix((power_up(['C5', 'E5', 'G5', 'C6'], step=0.06, last=0.3), 0, 1.0), (coin(), 0.2, 0.45))


# ===========================================================================
# Effets de carte (joués à l'apparition du premier texte de l'effet)
# ===========================================================================

@sound('effects/attack', -18)
def fx_attack():
    """Attaque : épée qui fend l'air, puis touche."""
    return mix((blade_swish(0.17), 0, 1.0), (impact(0.3, 160, 55), 0.13, 0.9), (blade_ring(2800, 0.4), 0.13, 0.25))


@sound('effects/defense', -18)
def fx_defense():
    """Défense : bouclier levé, choc métallique sourd."""
    return reverb(mix((metal_hit(520, 0.7, bright=0.7), 0, 1.0), (thump(150, 80, 0.2, 0.06), 0, 0.8)),
                  wet=0.15, tail=0.4)


@sound('effects/gain', -18)
def fx_gain():
    """Gains immédiats : « ka-tching » de caisse enregistreuse et pièces."""
    return mix((chips_clatter(3, 0.08), 0, 0.5), (coin(), 0.05, 1.0), (cash_register_bell(), 0.16, 0.5))


@sound('effects/multiplier', -18)
def fx_multiplier():
    """Gains x+ : montée rapide et brillante, comme un bonus qui s'empile."""
    return mix((power_up(['G5', 'B5', 'D6', 'G6'], step=0.04), 0, 1.0), (coin(hz('G6')), 0.16, 0.35))


@sound('effects/extra_draw', -19)
def fx_extra_draw():
    """Pioche : deux cartes tirées du paquet, puis une note qui monte."""
    return mix((card_flick(0.07), 0, 1.0), (card_flick(0.07), 0.09, 0.9),
               (chip(hz('A5'), 0.08, 'tri', s=0.5), 0.17, 0.5), (chip(hz('E6'), 0.14, 'tri', s=0.5), 0.22, 0.5))


@sound('effects/boost_symbol', -18)
def fx_boost_symbol():
    """Boost d'un symbole : décharge électrique qui grimpe."""
    d = 0.3
    zap = osc(sweep(260, 1900, d), d, 'square', duty=0.3) * adsr(d, 0.005, 0.05, 0.7, 0.06)
    return mix((lp(zap, 6000), 0, 0.8), (sparkle(0.35, count=8), 0.2, 0.5))


@sound('effects/club_gain_attack', -18)
def fx_club_gain_attack():
    """Trèfle (gains et attaque) : pièce, puis coup de lame."""
    return mix((coin(), 0, 0.8), (blade_swish(0.14), 0.12, 0.9), (impact(0.25, 150, 60), 0.24, 0.7))


@sound('effects/heart_drain', -18)
def fx_heart_drain():
    """Drain de vie : aspiration qui descend et cœur qui bat."""
    d = 0.55
    suck = svf(noise(d), sweep(3500, 400, d), q=2.0, mode='bp') * ramp(d, 1.0, 0.2)
    tone = osc(sweep(950, 320, d), d) * adsr(d, 0.02, 0.1, 0.6, 0.1)
    return mix((suck, 0, 0.8), (tone, 0, 0.35), (heartbeat([0.45]), 0, 1.0))


@sound('effects/diamond_reflect', -18)
def fx_diamond_reflect():
    """Renvoi : souffle qui monte puis éclat de cristal."""
    rise = whoosh(0.25, 600, 5000, q=1.0, curve=2.0)
    crystal = modal(hz('A6'), 1.0, GLASS_RATIOS, [0.5, 0.3, 0.15, 0.08], [1, 0.5, 0.3, 0.2])
    return reverb(mix((rise, 0, 0.7), (crystal, 0.22, 1.0), (sparkle(0.4, count=6), 0.24, 0.4)), wet=0.25, tail=0.6)


@sound('effects/ace_of_clubs', -17)
def fx_ace_clubs():
    """As de Trèfle : les gains sont aspirés, une puissance monte."""
    d = 0.75
    sucked = Track(0.4)
    for k in range(8):
        sucked.add(modal(2600 - 120 * k, 0.12, BAR_RATIOS, [0.05, 0.02, 0.01, 0.005]), 0.4 * (k / 8) ** 0.6, 0.6)
    chord = sum(osc(transpose(hz('C3'), s), d, 'saw') for s in (0, 7, 12))
    swell = svf(chord, sweep(300, 3500, d), q=1.2) * ramp(d, 0.0, 1.0, 2.0)
    return mix((sucked.buf, 0, 0.8), (swell, 0.1, 0.35), (impact(0.3, 120, 50), d + 0.08, 0.8))


@sound('effects/ace_of_diamonds', -17)
def fx_ace_diamonds():
    """As de Carreau (contre-attaque) : choc sur le bouclier, puis riposte."""
    return mix((metal_hit(480, 0.5, bright=0.8), 0, 1.0), (whoosh(0.18, 4000, 900, q=1.4), 0.22, 0.8),
               (impact(0.3, 170, 60), 0.38, 1.0))


@sound('effects/ace_of_hearts', -17)
def fx_ace_hearts():
    """As de Cœur (frénésie) : cœur qui s'emballe et grognement qui enfle."""
    d = 0.9
    growl_f = vibrato(sweep(70, 150, d), d, rate=11, depth=0.06)
    growl = drive(lp(osc(growl_f, d, 'saw') + 0.6 * osc(growl_f * 1.01, d, 'saw'), 900), 3.0) * ramp(d, 0.1, 1.0, 1.5)
    return mix((heartbeat([0.0, 0.32, 0.56, 0.74]), 0, 1.0), (growl, 0.05, 0.45))


@sound('effects/ace_of_spades', -16)
def fx_ace_spades():
    """As de Pique (exécution) : grondement grave, puis lame qui tranche."""
    boom = mix((thump(70, 32, 1.0, 0.35), 0, 1.0), (lp(noise(0.8), 400) * expdec(0.8, 0.25), 0, 0.6))
    return reverb(mix((boom, 0, 1.0), (blade_swish(0.14, 900, 6000), 0.32, 0.8), (blade_ring(3300, 1.0), 0.44, 0.7)),
                  wet=0.2, tail=0.6)


@sound('effects/bet', -18)
def fx_bet():
    """Pari : pile de jetons poussée sur le tapis."""
    return mix((chips_clatter(8, 0.38), 0, 1.0), (svf(noise(0.3), 900, q=0.7) * expdec(0.3, 0.1), 0, 0.3))


@sound('effects/bet_on_symbol', -19)
def fx_bet_on_symbol():
    """Pari placé sur un symbole : un jeton claque, la mise est validée."""
    return mix((chips_clatter(2, 0.06), 0, 1.0), (chip(hz('E5'), 0.08, 'square', s=0.5), 0.07, 0.5),
               (chip(hz('A5'), 0.18, 'square', s=0.5, r=0.08), 0.14, 0.5))


# La carte Bingo s'élève dans ses faisceaux (BingoCardAnimation) : nappe qui monte et cloche claire.
@sound('effects/bingo', -17)
def fx_bingo():
    d = 1.0
    beam = osc(sweep(220, 1760, d), d) * (0.6 + 0.4 * np.sin(2 * math.pi * 9 * times(d))) * ramp(d, 0.1, 1.0)
    shimmer = Track(d)
    for k, note in enumerate(['C5', 'E5', 'G5', 'C6', 'E6', 'G6', 'C7']):
        shimmer.add(chip(hz(note), 0.2, 'tri', s=0.4, r=0.1), k * 0.13, 0.4)
    ding = modal(hz('C7'), 1.2, BAR_RATIOS, [0.6, 0.25, 0.1, 0.05])
    return reverb(mix((beam, 0, 0.35), (shimmer.buf, 0, 1.0), (ding, d, 0.7), (sparkle(0.8), d, 0.4)), wet=0.3, tail=0.8)


@sound('effects/corruption', -17)
def fx_corruption():
    """Corruption : accord sombre et dissonant qui s'effondre, souffle inversé."""
    d = 1.0
    voices = sum(osc(sweep(transpose(220, s), transpose(110, s), d), d, 'saw') for s in (0, 1, 6))
    dark = drive(svf(voices, sweep(2500, 300, d), q=1.5), 2.0) * adsr(d, 0.02, 0.1, 0.8, 0.3)
    swell = (lp(noise(0.5), 3000) * ramp(0.5, 0.0, 1.0, 3.0))
    return reverb(mix((swell, 0, 0.35), (dark, 0.4, 0.6), (thump(80, 35, 0.6, 0.25), 0.45, 0.8)), wet=0.3, tail=0.8)


@sound('effects/extra_plays', -18)
def fx_extra_plays():
    """Dans la manche : carte sortie de la manche (froissement) et « ta-da » espiègle."""
    return mix((whoosh(0.15, 1500, 6000, q=1.0), 0, 0.8), (card_flick(0.06), 0.1, 0.6),
               (pluck(hz('G5'), 0.3), 0.18, 0.8), (pluck(hz('C6'), 0.5), 0.29, 0.9), (sparkle(0.4, count=8), 0.3, 0.4))


@sound('effects/gains_multiplier', -17)
def fx_gains_multiplier():
    """Gains xN : grande montée, accord et pluie de pièces."""
    run = power_up(['C5', 'E5', 'G5', 'C6', 'E6', 'G6'], step=0.045, last=0.15)
    chord = sum(chip(hz(n_), 0.45, 'square', s=0.6, r=0.2) for n_ in ('C6', 'E6', 'G6')) / 3
    return mix((run, 0, 1.0), (chord, 0.27, 0.8), (coin_shower(0.5, 8), 0.3, 0.6))


@sound('effects/lucky_charm', -18)
def fx_lucky_charm():
    """Porte-bonheur : harpe magique en gamme pentatonique et scintillement."""
    track = Track()
    for k, note in enumerate(['C5', 'D5', 'E5', 'G5', 'A5', 'C6', 'D6', 'E6']):
        track.add(pluck(hz(note), 0.6, bright=8000), k * 0.055, 0.6)
    track.add(sparkle(0.6, count=12), 0.3, 0.5)
    return reverb(track.buf, wet=0.3, tail=0.8)


@sound('effects/magnet', -18)
def fx_magnet():
    """Aimant : bourdonnement qui ondule de plus en plus vite, puis « clink » collé."""
    d = 0.6
    wobble = np.sin(2 * math.pi * np.cumsum(sweep(6, 24, d)) / SR)
    hum = (osc(sweep(110, 160, d), d) + 0.5 * osc(sweep(220, 320, d), d, 'tri')) * (0.6 + 0.4 * wobble)
    hum *= adsr(d, 0.05, 0.1, 0.8, 0.05)
    return mix((hum, 0, 0.6), (metal_hit(1900, 0.35, bright=0.6), d - 0.02, 0.7))


@sound('effects/pistol', -18)
def fx_pistol():
    """Pistolet chargé : barillet qui tourne, puis chien armé (« clic-clac »)."""
    track = Track()
    for k in range(6):
        track.add(stack(modal(2300, 0.04, [1, 2.1, 3.4], [0.012, 0.006, 0.003]), click(0.003)), k * 0.045, 0.6)
    track.add(stack(click(0.006, 1000, 5000), modal(1400, 0.06, WOOD_RATIOS, [0.02, 0.01, 0.005])), 0.36, 1.0)
    track.add(stack(click(0.006, 1500, 7000), modal(2100, 0.08, WOOD_RATIOS, [0.025, 0.012, 0.006])), 0.46, 1.0)
    return track.buf


@sound('effects/rainbow', -18)
def fx_rainbow():
    """Arc-en-ciel : glissando sur deux octaves, en voix légèrement désaccordées."""
    track = Track()
    notes = ['C5', 'D5', 'E5', 'F5', 'G5', 'A5', 'B5', 'C6', 'D6', 'E6', 'F6', 'G6', 'A6', 'B6', 'C7']
    for k, note in enumerate(notes):
        f = hz(note)
        tone = (osc(f, 0.25) + 0.5 * osc(f * 1.006, 0.25, 'tri')) * expdec(0.25, 0.09)
        track.add(tone, k * 0.035, 0.45)
    track.add(sparkle(0.6, count=14), 0.3, 0.4)
    return reverb(track.buf, wet=0.3, tail=0.7)


@sound('effects/recycle', -18)
def fx_recycle():
    """Recyclage : symbole aspiré, mécanisme qui cliquette, note qui descend."""
    return mix((whoosh(0.2, 4000, 800, q=1.2), 0, 0.8), (click(0.006, 1500, 6000), 0.18, 0.9),
               (click(0.006, 1500, 6000), 0.24, 0.8), (chip(sweep(900, 300, 0.25), 0.25, 'square', duty=0.5), 0.26, 0.4))


@sound('effects/russian_roulette', -17)
def fx_russian_roulette():
    """Roulette russe : barillet lancé qui ralentit sur fond de grondement, puis chien armé."""
    track = Track()
    t, interval = 0.0, 0.03
    while t < 0.9:
        track.add(stack(modal(2200, 0.04, [1, 2.1, 3.4], [0.012, 0.006, 0.003]), click(0.003)), t, 0.6)
        t += interval
        interval *= 1.17
    drone = (osc(55, 1.3) + 0.5 * osc(58.3, 1.3)) * adsr(1.3, 0.2, 0.2, 0.7, 0.3)
    track.add(lp(drone, 300), 0, 0.5)
    track.add(stack(click(0.008, 1000, 5000), modal(1500, 0.08, WOOD_RATIOS, [0.03, 0.015, 0.007])), 1.05, 1.0)
    return track.buf


@sound('effects/spade_ignore_defense', -18)
def fx_spade_ignore_defense():
    """Perce-défense : sifflement perçant, puis armure qui se fend comme du verre."""
    d = 0.2
    zing = osc(sweep(4200, 900, d), d) * expdec(d, 0.08)
    crack = Track()
    for k in range(5):
        crack.add(hp(noise(0.02), 3000) * expdec(0.02, 0.005), k * 0.025 + rand(0, 0.01), rand(0.5, 1))
    shatter = modal(hz('D7'), 0.5, GLASS_RATIOS, [0.2, 0.12, 0.07, 0.04])
    return mix((zing, 0, 0.6), (crack.buf, d - 0.02, 1.0), (shatter, d, 0.6), (impact(0.25, 180, 70), d - 0.02, 0.6))



@sound('effects/reroll', -18)
def fx_reroll():
    """Relance : levier tiré (cliquetis qui monte), puis petite note d'espoir."""
    track = Track()
    for k in range(5):
        track.add(stack(click(0.004, 1500, 6000), modal(1500 + 180 * k, 0.05, WOOD_RATIOS, [0.02, 0.01, 0.005])),
                  k * 0.05, 0.7)
    track.add(chip(sweep(hz('C5'), hz('G5'), 0.18), 0.22, 'square', duty=0.25, s=0.5), 0.28, 0.6)
    return track.buf


@sound('effects/rigged_reel', -18)
def fx_rigged_reel():
    """Rouleau truqué : tournevis qui grince, déclic sec du rouleau calé, rire de note basse."""
    d = 0.3
    screw = svf(noise(d), sweep(1800, 2600, d), q=6.0, mode='bp') * (0.5 + 0.5 * np.sin(2 * math.pi * 18 * np.arange(n(d)) / SR))
    return mix((screw * adsr(d, 0.02, 0.05, 0.8, 0.05), 0, 0.6),
               (stack(click(0.006, 1200, 6000), modal(1300, 0.08, WOOD_RATIOS, [0.03, 0.015, 0.007])), 0.32, 1.0),
               (chip(hz('E4'), 0.1, 'square', duty=0.5, s=0.5), 0.42, 0.4), (chip(hz('C4'), 0.16, 'square', duty=0.5, s=0.5), 0.52, 0.4))


@sound('effects/ghost_reel', -18)
def fx_ghost_reel():
    """Rouleau fantôme : « hoouu » de fantôme qui ondule, puis scintillement du Joker."""
    d = 0.8
    f = vibrato(sweep(hz('A4'), hz('D4'), d), d, rate=6, depth=0.03)
    ghost = (osc(f, d) + 0.3 * osc(f * 2, d, 'tri')) * adsr(d, 0.15, 0.1, 0.8, 0.3)
    return reverb(mix((lp(ghost, 2500), 0, 0.7), (sparkle(0.5, count=10), 0.45, 0.5)), wet=0.4, tail=0.8)


@sound('effects/rank_token', -18)
def fx_rank_token():
    """Jeton de rang : jeton posé sur le tapis, puis petite fanfare de cuivres."""
    return mix((chips_clatter(2, 0.06), 0, 0.7),
               (brass(hz('G4'), 0.12), 0.08, 0.6), (brass(hz('C5'), 0.12), 0.2, 0.6), (brass(hz('E5'), 0.35), 0.32, 0.7))


@sound('effects/all_in', -17)
def fx_all_in():
    """Tapis : toutes les piles de jetons poussées d'un coup, et un grondement de tension."""
    drone = lp(osc(55, 0.9, 'saw') + osc(55.6, 0.9, 'saw'), 400) * adsr(0.9, 0.1, 0.1, 0.7, 0.3)
    return mix((chips_clatter(18, 0.6), 0, 1.0), (whoosh(0.3, 3000, 600, q=1.0), 0, 0.5), (drone, 0.2, 0.5),
               (impact(0.3, 120, 50), 0.6, 0.8))


@sound('effects/safe', -18)
def fx_safe():
    """Coffre-fort : molette qui tourne (clics), lourde porte qui se ferme."""
    track = Track()
    for k in range(6):
        track.add(stack(click(0.003, 2000, 7000), modal(2600, 0.03, [1, 2.1, 3.4], [0.01, 0.005, 0.002])), k * 0.06, 0.6)
    track.add(metal_hit(180, 0.6, bright=0.4), 0.42, 1.0)
    track.add(thump(90, 45, 0.4, 0.12), 0.42, 0.8)
    return track.buf


@sound('effects/insurance', -18)
def fx_insurance():
    """Assurance : tampon apposé sur le contrat, puis accord rassurant."""
    chord = sum(osc(hz(n_), 0.6, 'tri') for n_ in ('C5', 'E5', 'G5')) / 3 * adsr(0.6, 0.03, 0.1, 0.6, 0.3)
    return mix((thump(200, 90, 0.15, 0.04), 0, 1.0), (click(0.01, 600, 3000), 0, 0.6), (chord, 0.12, 0.6))


@sound('effects/bribe', -18)
def fx_bribe():
    """Pot-de-vin : billets froissés glissés en douce, puis « ka-tching » étouffé."""
    rustle = Track(0.4)
    for k in range(10):
        rustle.add(bp(noise(0.03), 1800, 5200) * expdec(0.03, 0.01), k * 0.035 + rand(0, 0.01), rand(0.4, 1))
    return mix((rustle.buf, 0, 0.9), (lp(cash_register_bell(), 2500), 0.38, 0.5), (coin(), 0.36, 0.5))


@sound('effects/double_or_nothing', -17)
def fx_double_or_nothing():
    """Double ou rien : deux dés qui roulent, puis deux notes montantes en écho."""
    track = Track()
    for k in range(7):
        track.add(stack(click(0.004, 800, 4000), modal(rand(900, 1400), 0.05, WOOD_RATIOS, [0.02, 0.01, 0.005])),
                  k * 0.045 + rand(0, 0.015), rand(0.5, 1))
    track.add(power_up(['C5', 'G5'], step=0.09, last=0.12), 0.36, 0.8)
    track.add(power_up(['C6', 'G6'], step=0.09, last=0.25), 0.56, 0.6)
    return track.buf


@sound('effects/overheat', -17)
def fx_overheat():
    """Machine en surchauffe : sifflement de vapeur, moteur qui s'emballe et flamme."""
    d = 0.9
    steam = hp(noise(d), 3000) * ramp(d, 0.2, 1.0) * adsr(d, 0.05, 0.1, 0.8, 0.2)
    motor = drive(lp(osc(sweep(60, 240, d), d, 'saw'), 1500), 2.0) * ramp(d, 0.3, 1.0)
    return mix((steam, 0, 0.5), (motor, 0, 0.5), (fire_burst(0.7), 0.35, 0.8))

# ===========================================================================
# Bingo : un son par célébration (JackpotCelebration), aligné sur sa mise en scène.
# Les instants en commentaire sont ceux des constantes des scènes.
# ===========================================================================

@sound('bingo/casino', -16)
def bingo_casino():
    """Triple Sept et Joker : sonnerie de jackpot et pluie de pièces (les fusées ont leurs propres sons, fx/firework_*)."""
    track = Track(3.0)
    track.add(bingo_jingle('C5', 'square', duty=0.25), 0.0, 0.9)
    alarm = Track()
    for k in range(int(1.4 * 16)):                                   # sonnerie, COIN_SPAWN_TIME = 1,4 s
        alarm.add(modal(1480, 0.08, BAR_RATIOS, [0.04, 0.02, 0.01, 0.005]), k / 16, 1.0 - k / 30)
    track.add(alarm.buf, 0.0, 0.35)
    track.add(coin_shower(1.6, 45), 0.05, 0.7)
    return track.buf


@sound('bingo/seven', -16)
def bingo_seven():
    """Sept : un dé roule, retombe sur le 7 qui s'embrase (FlamingDieScene.LAND_TIME = 1,0 s)."""
    land = 1.0
    track = Track(3.0)
    t, interval = 0.15, 0.07
    while t < land - 0.04:                                           # le dé rebondit de moins en moins
        knock = stack(modal(rand(900, 1300), 0.06, WOOD_RATIOS, [0.02, 0.01, 0.006]), click(0.004, 1500, 6000))
        track.add(knock, t, 0.5 + 0.4 * (1 - t / land))
        t += interval
        interval *= 1.12
    track.add(impact(0.3, 130, 50), land, 0.8)
    track.add(fire_burst(1.4), land, 0.9)
    track.add(bingo_jingle('D5', 'saw', step=0.06), land + 0.12, 0.5)
    return track.buf


@sound('bingo/double_bar', -16)
def bingo_double_bar():
    """Double Bar : presse hydraulique qui descend en sifflant, puis écrase (SLAM_TIME = 0,8 s)."""
    slam = 0.8
    track = Track(3.0)
    motor_f = vibrato(sweep(80, 150, slam), slam, rate=30, depth=0.01)
    motor = lp(osc(motor_f, slam, 'saw') + 0.5 * osc(motor_f * 2, slam, 'square'), 1200) * ramp(slam, 0.2, 1.0)
    track.add(motor, 0.0, 0.35)
    track.add(hp(noise(slam), 3500) * ramp(slam, 0.1, 0.8), 0.0, 0.3)
    track.add(mix((thump(90, 30, 0.9, 0.3), 0, 1.0), (lp(noise(0.5), 1500) * expdec(0.5, 0.1), 0, 0.8)), slam, 1.0)
    track.add(metal_hit(160, 1.2, bright=0.6), slam, 0.7)
    track.add(hp(noise(1.0), 2500) * expdec(1.0, 0.35, attack=0.05), slam + 0.1, 0.35)   # vapeur relâchée
    track.add(bingo_jingle('C5', 'square', duty=0.5), slam + 0.3, 0.5)
    return track.buf


@sound('bingo/bar', -16)
def bingo_bar():
    """Bar : le marteau frappe trois fois l'enclume (ForgeScene.HITS), puis la pièce est trempée."""
    hits = (0.55, 1.0, 1.45)
    track = Track(3.0)
    for k, at in enumerate(hits):
        anvil = modal(1180 + 40 * k, 1.0, [1, 1.58, 2.31, 2.97, 4.12], [0.6, 0.45, 0.3, 0.2, 0.12],
                      [1, 0.7, 0.5, 0.35, 0.2])
        track.add(mix((anvil, 0, 1.0), (impact(0.2, 200, 90), 0, 0.6)), at, 0.6 + 0.2 * k)
    quench = hp(noise(1.2), 1800) * expdec(1.2, 0.4, attack=0.02)
    track.add(quench, hits[-1] + 0.15, 0.35)
    track.add(bingo_jingle('E5', 'square', duty=0.25), hits[-1] + 0.12, 0.5)
    return track.buf


@sound('bingo/cherry', -16)
def bingo_cherry():
    """Cerise : l'arbre pousse, ses feuilles frémissent (SHAKE_TIME = 1,35 s) et les cerises tombent (DROP_TIME = 1,5 s)."""
    track = Track(3.0)
    grow = osc(vibrato(sweep(180, 720, 1.1), 1.1, rate=6, depth=0.02), 1.1, 'tri') * adsr(1.1, 0.1, 0.2, 0.7, 0.2)
    track.add(grow, 0.0, 0.3)
    track.add(bingo_jingle('F5', 'tri', step=0.07), 1.2, 0.6)        # mot « BINGO! » à 1,2 s
    rustle = Track(0.5)
    for _ in range(40):
        rustle.add(bp(noise(0.02), 2500, 7000) * expdec(0.02, 0.006), rand(0, 0.45), rand(0.3, 1))
    track.add(rustle.buf, 1.35, 0.5)
    for _ in range(16):                                              # cerises qui tombent, « ploc »
        at = 1.5 + rand(0, 1.0)
        f = rand(500, 900)
        track.add(osc(sweep(f * 1.6, f, 0.08), 0.08) * expdec(0.08, 0.025), at, rand(0.3, 0.6))
    track.add(coin_shower(0.8, 10), 1.5, 0.4)
    return track.buf


@sound('bingo/triple_cherry', -16)
def bingo_triple_cherry():
    """Triple Cerise : les cerises filent vers la cible et s'y plantent (BullseyeAnimation.IMPACT_TIME = 0,95 s)."""
    hit = 0.95
    track = Track(3.0)
    for k in range(3):
        track.add(whoosh(0.45, 600, 3500, q=1.5), 0.35 + k * 0.08, 0.45)
    knock = mix((thump(130, 60, 0.3, 0.08), 0, 1.0), (modal(320, 0.3, WOOD_RATIOS, [0.12, 0.06, 0.03]), 0, 0.8))
    wobble = osc(170, 0.7) * expdec(0.7, 0.25) * (0.5 + 0.5 * np.sin(2 * math.pi * 22 * times(0.7)))
    track.add(knock, hit, 1.0)
    track.add(wobble, hit + 0.02, 0.4)
    for k in range(3):                                               # anneaux de la cible, toutes les 0,12 s
        track.add(modal(hz(['C6', 'E6', 'G6'][k]), 0.6, BAR_RATIOS, [0.3, 0.12, 0.06, 0.03]), hit + k * 0.12, 0.4)
    track.add(bingo_jingle('G5', 'square', duty=0.125), hit + 0.12, 0.5)
    return track.buf


@sound('bingo/grape', -16)
def bingo_grape():
    """Raisin : le pressoir grince en descendant puis écrase la grappe (WinePressScene.CRUSH_TIME = 0,95 s)."""
    crush = 0.95
    track = Track(3.0)
    creak = Track(crush)
    t = 0.05
    while t < crush - 0.05:                                          # bois qui grince par à-coups
        f = rand(70, 110)
        creak.add(bp(osc(f, 0.09, 'saw'), 300, 1800) * expdec(0.09, 0.04, attack=0.01), t, rand(0.5, 1))
        t += rand(0.03, 0.08)
    track.add(creak.buf, 0, 0.5)
    squish = lp(noise(0.5), 900) * expdec(0.5, 0.15)
    track.add(squish, crush, 0.9)
    track.add(thump(110, 50, 0.3, 0.1), crush, 0.7)
    for _ in range(12):                                              # jus qui gicle en bulles
        f = rand(250, 600)
        track.add(osc(sweep(f, f * 2.2, 0.06), 0.06) * expdec(0.06, 0.02), crush + rand(0.02, 0.7), rand(0.25, 0.6))
    track.add(bingo_jingle('A4', 'square', duty=0.5), crush + 0.12, 0.5)
    return track.buf


@sound('bingo/bell', -15)
def bingo_bell():
    """Cloche : la cloche d'église balance et sonne à toute volée (ChurchBellAnimation.STRIKE_TIME = 0,85 s)."""
    strike = 0.85
    track = Track(3.0)
    track.add(whoosh(0.5, 300, 900, q=0.8), strike - 0.5, 0.3)
    bell = modal(hz('A3') * 2, 2.6, CHURCH_RATIOS, [2.4, 1.8, 1.2, 1.0, 0.9, 0.6, 0.5, 0.35, 0.25],
                 [0.6, 1.0, 0.7, 0.5, 0.6, 0.4, 0.3, 0.25, 0.15])
    track.add(mix((bell, 0, 1.0), (impact(0.2, 300, 120), 0, 0.5)), strike, 1.0)
    track.add(bingo_jingle('A5', 'tri', step=0.06, hold=0.9), strike + 0.12, 0.45)
    return reverb(track.buf, wet=0.35, size=1.3, tail=0.5)[:n(3.0)]


@sound('bingo/diamond', -16)
def bingo_diamond():
    """Diamant : la meule tourne, puis la pierre taillée éclate en cristal (GemCutScene.CUT_TIME = 1,0 s)."""
    cut = 1.0
    track = Track(3.0)
    grind_f = sweep(160, 320, cut)
    grind = (lp(osc(grind_f, cut, 'saw'), 1500) + bp(noise(cut), 2500, 6000)) * ramp(cut, 0.2, 1.0)
    track.add(grind, 0, 0.3)
    chord = sum(modal(hz(n_), 1.6, GLASS_RATIOS, [0.8, 0.4, 0.2, 0.1]) for n_ in ('E6', 'G#6', 'B6', 'E7'))
    track.add(chord, cut, 0.5)
    track.add(sparkle(1.4, count=24, lo=4000, hi=10000), cut, 0.6)
    track.add(bingo_jingle('E6', 'tri', step=0.05), cut + 0.12, 0.45)
    return reverb(track.buf, wet=0.3, tail=0.4)[:n(3.0)]


@sound('bingo/gold_bar', -16)
def bingo_gold_bar():
    """Lingot : le coffre tombe (SafeScene.LAND_TIME = 0,35 s), son cadran tourne, la porte s'ouvre (OPEN_TIME = 1,15 s)."""
    land, open_ = 0.35, 1.15
    track = Track(3.0)
    track.add(mix((thump(90, 35, 0.6, 0.18), 0, 1.0), (metal_hit(140, 0.5, bright=0.4), 0, 0.5)), land, 1.0)
    for k in range(7):                                               # cadran du coffre
        track.add(stack(modal(2600, 0.03, [1, 2.2], [0.01, 0.005]), click(0.003, 2000, 8000)), 0.5 + k * 0.08, 0.5)
    track.add(mix((click(0.01, 800, 4000), 0, 1.0), (thump(160, 80, 0.15, 0.04), 0, 0.8)), open_ - 0.06, 0.8)
    squeak = osc(vibrato(sweep(550, 850, 0.45), 0.45, rate=9, depth=0.03), 0.45, 'saw')
    track.add(bp(squeak, 500, 2500) * adsr(0.45, 0.05, 0.1, 0.6, 0.1), open_, 0.25)
    track.add(coin_shower(1.2, 40), open_ + 0.05, 0.7)
    track.add(bingo_jingle('Bb4', 'square', duty=0.25), open_ + 0.12, 0.5)
    return track.buf


@sound('bingo/watermelon', -16)
def bingo_watermelon():
    """Pastèque : le katana est dégainé, puis tranche le fruit (KatanaScene.SLICE_TIME = 0,9 s)."""
    slice_ = 0.9
    track = Track(3.0)
    track.add(blade_ring(3000, 0.6), 0.3, 0.5)                       # dégainé
    track.add(whoosh(0.14, 900, 7000, q=2.0), slice_ - 0.1, 1.0)
    track.add(blade_ring(3600, 1.0), slice_, 0.6)
    splat = mix((lp(noise(0.4), 1600) * expdec(0.4, 0.08), 0, 1.0), (thump(140, 60, 0.25, 0.06), 0, 0.6))
    track.add(splat, slice_ + 0.05, 0.8)
    for _ in range(8):
        f = rand(300, 700)
        track.add(osc(sweep(f, f * 1.8, 0.05), 0.05) * expdec(0.05, 0.015), slice_ + rand(0.05, 0.5), rand(0.2, 0.5))
    track.add(bingo_jingle('D5', 'square', duty=0.125), slice_ + 0.25, 0.5)
    return track.buf


# ===========================================================================
# Fin du combat
# ===========================================================================

@sound('combat/victory', -16)
def victory():
    """Victoire : fanfare de cuivres, timbales, cymbale et pièces."""
    track = Track()
    for k, note in enumerate(['G4', 'C5', 'E5']):
        track.add(brass(hz(note), 0.14), k * 0.13, 0.6)
    chord_at = 0.42
    for note in ('C4', 'G4', 'C5', 'E5', 'G5'):
        track.add(brass(hz(note), 1.5, cutoff=(600, 3200, 1600)), chord_at, 0.35)
    for k in range(6):                                               # roulement de timbales
        track.add(thump(110, 70, 0.3, 0.12), k * 0.07, 0.25 + 0.06 * k)
    track.add(thump(100, 50, 0.8, 0.3), chord_at, 0.8)
    cymbal = hp(noise(2.0), 5000) * expdec(2.0, 0.6, attack=0.002)
    track.add(cymbal, chord_at, 0.25)
    track.add(coin_shower(1.2, 18), chord_at + 0.2, 0.4)
    return reverb(track.buf, wet=0.25, tail=0.8)


@sound('combat/defeat', -17)
def defeat():
    """Défaite : trombone bouché qui descend (« wah wah wah waaah »), puis un coup sourd."""
    track = Track()
    notes = [('G3', 0.42), ('F#3', 0.42), ('F3', 0.42), ('E3', 1.5)]
    at = 0.0
    for k, (note, d) in enumerate(notes):
        last = k == len(notes) - 1
        f = vibrato(hz(note), d, rate=6 if last else 4, depth=0.03 if last else 0.008, delay=0.2 if last else 0.3)
        tone = osc(f, d, 'saw') + 0.5 * osc(f * 1.003, d, 'saw')
        wah = svf(tone, 300 + 1200 * np.sin(np.linspace(0, math.pi, n(d))) ** 2, q=2.0)
        track.add(wah * adsr(d, 0.03, 0.1, 0.8, 0.12 if not last else 0.5), at, 0.6)
        at += d + 0.04
    track.add(thump(80, 35, 0.8, 0.3), at - 0.6, 0.6)
    return reverb(track.buf, wet=0.2, tail=0.6)


# ===========================================================================
# Combat : coups encaissés, bouclier
# ===========================================================================

def crack(d=0.05, tau=0.006, lo=500, hi=9000):
    """Claquement sec et large bande : l'attaque d'un impact ou d'une détonation."""
    return drive(bp(noise(d), lo, hi) * expdec(d, tau, attack=0.0002), 3.0)


def wood_break():
    """Planche brisée : craquement, éclats de bois qui se détachent et résonance sèche."""
    track = Track()
    track.add(crack(0.06, 0.008, 300, 7000), 0, 1.0)
    track.add(modal(220, 0.25, WOOD_RATIOS, [0.06, 0.03, 0.015], [1, 0.6, 0.3]), 0, 0.7)
    for _ in range(9):                                               # éclats
        at = rand(0.01, 0.12)
        track.add(stack(click(0.004, 1200, 6000), modal(rand(500, 1400), 0.05, WOOD_RATIOS, [0.012, 0.006, 0.003])),
                  at, rand(0.25, 0.6))
    return track.buf


@sound('combat/player_hurt', -15)
def player_hurt():
    """Le joueur encaisse un coup : poing qui brise une planche, choc lourd dans la poitrine."""
    punch = lp(noise(0.25), 220) * expdec(0.25, 0.06, attack=0.001)
    body = thump(95, 45, 0.3, 0.07)
    return reverb(mix((drive(stack(punch * 2.5, body), 2.0), 0, 1.0), (wood_break(), 0.003, 0.9)), wet=0.1, tail=0.25)


@sound('combat/enemy_hurt', -15)
def enemy_hurt():
    """L'ennemi encaisse un coup : détonation sèche, comme un coup de pistolet, et jetons du croupier qui volent."""
    shot = mix((crack(0.04, 0.004, 800, 10000), 0, 1.0), (drive(lp(noise(0.3), 900) * expdec(0.3, 0.05), 2.0), 0, 0.9),
               (thump(120, 50, 0.2, 0.05), 0, 0.6))
    tail = lp(noise(0.6), 2500) * expdec(0.6, 0.12) * ramp(0.6, 0.0, 1.0, 0.3)
    return reverb(mix((shot, 0, 1.0), (tail, 0.02, 0.25), (chips_clatter(4, 0.15), 0.05, 0.3)), wet=0.12, size=1.2, tail=0.3)


@sound('combat/shield_gain', -19)
def shield_gain():
    """Bouclier gagné : la protection s'élève (« shwing » qui monte), tinte et une pile de jetons s'empile."""
    d = 0.3
    rise = (osc(sweep(330, 990, d), d) + 0.5 * osc(sweep(332, 995, d) * 1.5, d, 'tri')) * adsr(d, 0.02, 0.05, 0.7, 0.08)
    ring = modal(hz('E6'), 0.7, [1, 1.51, 2.43, 3.2], [0.35, 0.22, 0.12, 0.08], [1, 0.5, 0.3, 0.2])
    return reverb(mix((whoosh(d, 500, 3000, q=0.9, curve=1.5), 0, 0.5), (rise, 0, 0.45),
                      (metal_hit(1050, 0.5, bright=0.5), d - 0.03, 0.45), (ring, d - 0.03, 0.5),
                      (sparkle(0.35, count=5), d, 0.3), (chips_clatter(4, 0.16, 2600, 3400), d - 0.02, 0.35)),
                  wet=0.2, tail=0.4)


@sound('combat/shield_block', -16)
def shield_block():
    """Coup bloqué par un bouclier : « clang » d'acier lourd, choc sourd, et jetons qui tressautent sur le feutre."""
    clang = mix((metal_hit(330, 0.9, bright=0.9), 0, 1.0), (metal_hit(497, 0.6, bright=0.6), 0.002, 0.55))
    body = thump(160, 70, 0.22, 0.05)
    scrape = bp(noise(0.12), 2500, 6500) * expdec(0.12, 0.03, attack=0.001)
    return reverb(mix((crack(0.03, 0.004, 1200, 9000), 0, 0.7), (clang, 0, 0.9), (body, 0, 0.8), (scrape, 0.004, 0.3),
                      (chips_clatter(3, 0.12, 2400, 3300), 0.03, 0.35)), wet=0.18, size=1.1, tail=0.45)


@sound('combat/shield_break', -15)
def shield_break():
    """Bouclier brisé : l'acier claque et se fend, des éclats tintent en tombant, et une pile de jetons s'écroule."""
    track = Track()
    track.add(crack(0.06, 0.007, 600, 10000), 0, 1.0)
    track.add(metal_hit(280, 0.5, bright=1.2), 0, 0.8)
    track.add(thump(140, 50, 0.3, 0.07), 0, 0.8)
    for k in range(6):                                               # fissure qui court
        track.add(hp(noise(0.02), 2500) * expdec(0.02, 0.005), 0.02 + k * 0.018 + rand(0, 0.008), rand(0.4, 0.9))
    track.add(modal(hz('A6'), 0.6, GLASS_RATIOS, [0.25, 0.14, 0.08, 0.05]), 0.05, 0.45)
    for _ in range(8):                                               # éclats qui retombent
        track.add(modal(rand(1800, 4200), 0.15, [1, 2.4, 3.9], [0.06, 0.03, 0.015]), rand(0.12, 0.5), rand(0.15, 0.4))
    track.add(chips_clatter(7, 0.4, 2200, 3600), 0.1, 0.45)
    return reverb(track.buf, wet=0.2, size=1.2, tail=0.5)


# ===========================================================================
# Pièces, échoppe, combinaisons
# ===========================================================================

@sound('coins/gain', -20)
def coins_gain():
    """Des gains tombent dans la caisse : trois pièces qui tintent en montant."""
    track = Track()
    for k, f in enumerate((1760, 2093, 2637)):
        track.add(modal(f, 0.3, BAR_RATIOS, [0.12, 0.05, 0.03, 0.015], [1, 0.5, 0.3, 0.15]), k * 0.055, 0.7 + 0.1 * k)
    track.add(chips_clatter(3, 0.1), 0.0, 0.3)
    return track.buf


@sound('coins/loss', -21)
def coins_loss():
    """Des gains s'envolent : pièces qui glissent en descendant et note qui s'affaisse."""
    track = Track()
    for k, f in enumerate((2349, 1976, 1661, 1397)):
        track.add(modal(f, 0.22, BAR_RATIOS, [0.09, 0.04, 0.02, 0.01], [1, 0.5, 0.3, 0.15]), k * 0.06, 0.8 - 0.12 * k)
    sag = chip(sweep(520, 260, 0.35), 0.35, 'tri', s=0.6, r=0.15)
    track.add(sag, 0.05, 0.5)
    return track.buf


@sound('shop/purchase', -18)
def shop_purchase():
    """Carte achetée à l'échoppe : touches de caisse, tiroir qui s'ouvre, « ka-tching » et petit « ta-da »."""
    track = Track()
    for k in range(2):                                               # touches de la caisse
        track.add(stack(click(0.006, 1200, 5000), modal(1300 + 200 * k, 0.04, WOOD_RATIOS, [0.015, 0.008, 0.004])), k * 0.07, 0.6)
    track.add(mix((thump(180, 80, 0.15, 0.04), 0, 0.8), (svf(noise(0.12), 1800, q=0.8) * expdec(0.12, 0.04), 0, 0.5)), 0.16, 1.0)
    track.add(cash_register_bell(), 0.2, 0.7)
    track.add(coin_shower(0.35, 6), 0.22, 0.5)
    track.add(pluck(hz('G5'), 0.35), 0.36, 0.6)
    track.add(pluck(hz('C6'), 0.6), 0.46, 0.7)
    track.add(sparkle(0.4, count=8), 0.46, 0.35)
    return track.buf


@sound('combo/formed', -18)
def combo_formed():
    """Combinaison formée : deux accords piqués, puis l'accord tenu qui brille et la sonnette d'une machine à sous."""
    track = Track()
    stab = lambda notes, d: sum(chip(hz(x), d, 'square', duty=0.25, dc=0.04, s=0.5, r=0.05) for x in notes) / len(notes)
    track.add(stab(('E5', 'G5', 'C6'), 0.09), 0.0, 0.8)
    track.add(stab(('F5', 'A5', 'D6'), 0.09), 0.1, 0.8)
    held = stab(('G5', 'C6', 'E6', 'G6'), 0.5) * (1 - 0.3 * (0.5 + 0.5 * np.sin(2 * math.pi * 10 * times(0.5))))
    track.add(held, 0.2, 0.9)
    track.add(card_flick(0.05), 0.0, 0.4)
    for k in range(4):                                               # sonnette de machine à sous
        track.add(modal(1480, 0.1, BAR_RATIOS, [0.05, 0.025, 0.012, 0.006]), 0.22 + k * 0.06, 0.3)
    track.add(sparkle(0.5, count=10), 0.22, 0.4)
    return reverb(track.buf, wet=0.2, tail=0.4)


# ===========================================================================
# Fêtes : confettis et feux d'artifice (joués en direct par Confetti et Fireworks)
# ===========================================================================

@sound('party/confetti_pop', -19)
def confetti_pop():
    """Gerbe de confettis : « pop » de canon à confettis, puis papiers qui froufroutent en retombant."""
    pop = mix((bp(noise(0.03), 600, 5000) * expdec(0.03, 0.006, attack=0.0005), 0, 1.0), (thump(320, 110, 0.08, 0.02), 0, 0.8))
    paper = Track(0.7)
    for _ in range(45):
        paper.add(bp(noise(0.012), 3000, 9000) * expdec(0.012, 0.004), rand(0.0, 0.65) ** 1.3, rand(0.15, 0.6))
    return mix((pop, 0, 1.0), (paper.buf * ramp(len(paper.buf) / SR, 1.0, 0.2), 0.02, 0.5),
               (whoosh(0.2, 1500, 5000, q=0.8), 0, 0.3))


@sound('party/confetti_rain', -24)
def confetti_rain():
    """Pluie de confettis : froissement de papier léger qui tombe longtemps et s'éteint."""
    d = 3.0
    paper = Track(d)
    for _ in range(260):
        paper.add(bp(noise(0.015), 2500, 9000) * expdec(0.015, 0.005), rand(0, d - 0.05), rand(0.15, 0.6))
    return fade(paper.buf * adsr(d, 0.3, 0.2, 0.8, 1.5), 0.002, 0.3)


@sound('party/firework_launch', -24)
def firework_launch():
    """Départ d'une fusée : petit souffle de poudre et sifflement qui monte."""
    rise = 0.6
    whistle = osc(vibrato(sweep(1000, 2800, rise), rise, rate=18, depth=0.02), rise) * ramp(rise, 0.1, 0.5) * ramp(rise, 1.0, 0.3)
    hiss = hp(noise(rise), 3000) * expdec(rise, 0.25)
    puff = lp(noise(0.1), 1200) * expdec(0.1, 0.03)
    return mix((puff, 0, 0.8), (hiss, 0, 0.3), (whistle, 0.02, 0.25))


@sound('party/firework_burst', -18)
def firework_burst():
    """Explosion d'une fusée : détonation qui claque et résonne au loin, puis gerbe d'étincelles qui crépite."""
    bang = mix((crack(0.05, 0.007, 300, 9000), 0, 1.0), (lp(noise(1.2), 900) * expdec(1.2, 0.3, attack=0.002), 0, 0.8))
    echo = lp(noise(1.0), 1500) * expdec(1.0, 0.35) * ramp(1.0, 0.0, 1.0, 0.2)
    crackle = Track(2.0)
    for _ in range(170):                                             # crépitement : dense, puis qui s'éteint
        at = 0.18 + rand(0, 1.0) ** 1.6 * 1.5
        pop = bp(noise(0.006), rand(1500, 3500), rand(5000, 9000)) * expdec(0.006, rand(0.0008, 0.002), attack=0.0001)
        crackle.add(pop, at, rand(0.2, 1.0) * (1.0 - 0.5 * (at - 0.18) / 1.5))
    sizzle = hp(noise(1.6), 4000) * expdec(1.6, 0.5, attack=0.15)
    return reverb(mix((bang, 0, 1.0), (echo, 0.08, 0.3), (crackle.buf, 0, 0.55), (sizzle, 0.15, 0.12)),
                  wet=0.3, size=1.5, tail=0.8)


# ===========================================================================

# ===========================================================================
# Cinématiques
# ===========================================================================

@sound('cutscene/comet', -17)
def cutscene_comet():
    """
    Avant la Comète Dorée (CometCutscene) : la rue de casinos, calme (0 à 1,5 s), néons qui grésillent et une machine
    à sous qui tinte ; puis la nuit et les étoiles filantes qui tintent (dont deux dés qui roulent, 4,6 s), une
    météorite en feu qui gronde de plus en plus fort (5,8 s), les néons qui grillent un à un (7,6 s), la foule qui
    murmure, l'explosion (IMPACT = 9,6 s) et un accord de casino qui monte dans le fondu blanc (WHITE_FULL = 10,8 s).
    """
    street, meteor_at, impact_at, white_full = 1.5, 5.8, 9.6, 10.8
    track = Track(13.0)
    hum = stack(osc(120, 1.8, 'saw'), 0.5 * osc(240, 1.8, 'saw'))              # la rue : néons qui bourdonnent
    track.add(bp(hum, 200, 2000) * fade_out(1.8, 0.4), 0.0, 0.04)
    track.add(ambience(1.8, 300, 1500, 0.2), 0.0, 1.0)
    for k in range(5):                                                          # la machine à sous qui tinte
        track.add(chip(hz('E6') * 2 ** ((k % 2) * 4 / 12), 0.08, 'square', dc=0.02, s=0.4), 0.2 + k * 0.25, 0.12)
    night = lp(noise(9.0), 500) * ramp(9.0, 0.0, 1.0, 0.4) * ramp(9.0, 1.0, 0.2, 3.0)    # vent de nuit
    track.add(night, street, 0.18)
    drone = stack(osc(hz('A2'), 9.0, 'sine'), 0.5 * osc(hz('E3'), 9.0, 'sine'), 0.3 * osc(hz('A3') * 1.003, 9.0, 'tri'))
    track.add(drone * ramp(9.0, 0.0, 1.0, 0.5) * ramp(9.0, 1.0, 0.0, 2.0), street, 0.12)
    notes = ['E6', 'B5', 'A6', 'E6', 'C#7', 'B6', 'E7', 'A6', 'B6', 'E7', 'A6', 'E7', 'B6']
    t = street + 0.6
    for k, note in enumerate(notes):                                  # étoiles filantes : tintements de clochette
        ring = modal(hz(note), 0.9, [1, 2.76, 5.4], [0.5, 0.2, 0.08], [1, 0.3, 0.12])
        swish = whoosh(0.35, 3000, 9000, q=1.5, curve=1.2) * expdec(0.35, 0.15, attack=0.05)
        track.add(swish, t, 0.12)
        track.add(ring, t + 0.12, 0.22)
        t += 0.32 - 0.012 * k
    track.add(sparkle(3.4, count=55, lo=4000, hi=10000), street + 1.2, 0.18)
    track.add(rattle(0.9, 18, 10, 1500, 3000, wood=True), 4.6, 0.3)             # l'étoile qui tombe en dés
    roar_d = impact_at - meteor_at                                    # la météorite approche
    fc = sweep(120, 1800, roar_d)
    roar = svf(noise(roar_d), fc, q=0.9) * ramp(roar_d, 0.0, 1.0, 2.2)
    rumble = lp(noise(roar_d), 160) * ramp(roar_d, 0.0, 1.0, 1.5)
    whistle = osc(sweep(1800, 500, roar_d), roar_d, 'tri') * ramp(roar_d, 0.0, 1.0, 2.5)
    crackle = Track(roar_d)
    for _ in range(int(roar_d * 90)):
        at = rand(0, 1) ** 0.6 * (roar_d - 0.01)
        crackle.add(bp(noise(0.006), 1500, 6000) * expdec(0.006, 0.0015), at, rand(0.2, 0.9) * at / roar_d)
    track.add(roar, meteor_at, 0.6)
    track.add(rumble, meteor_at, 0.9)
    track.add(whistle, meteor_at, 0.08)
    track.add(crackle.buf, meteor_at, 0.35)
    for k in range(4):                                                          # les néons grillent un à un
        at = 7.6 + k * 0.35
        track.add(mix((crack(0.04, 0.008, 2000, 9000), 0, 1.0), (bp(noise(0.25), 3000, 8000) * expdec(0.25, 0.08), 0, 0.5)),
                  at, 0.35)
    for _ in range(8):                                                          # la foule lève la tête : « oh... »
        f = rand(200, 380)
        oh = bp(osc(sweep(f, f * 0.85, 0.6), 0.6, 'saw'), 300, 1600) * adsr(0.6, 0.1, 0.1, 0.6, 0.3)
        track.add(oh, rand(7.8, 8.6), rand(0.06, 0.12))
    boom = mix((crack(0.08, 0.01, 200, 9000), 0, 1.0), (thump(70, 25, 2.0, 0.6), 0, 1.2),
               (drive(lp(noise(3.0), 700) * expdec(3.0, 0.8, attack=0.004), 2.5), 0, 1.0),
               (fire_burst(2.4), 0.03, 0.7))
    track.add(reverb(boom, wet=0.35, size=1.8, tail=1.5), impact_at, 1.0)
    rise = white_full - impact_at + 0.4                              # le blanc : glissando qui monte et accord
    gliss = stack(*(osc(sweep(hz(n) / 2, hz(n), rise), rise, 'tri') for n in ('A4', 'C#5', 'E5')))
    track.add(gliss * ramp(rise, 0.0, 1.0, 1.5), impact_at + 0.3, 0.12)
    track.add(bingo_jingle('A5', 'square', duty=0.25, step=0.06, hold=1.2) * 0.8, white_full - 0.25, 0.6)
    track.add(sparkle(1.5, count=24), white_full, 0.3)
    return reverb(track.buf, wet=0.18, size=1.4, tail=1.0)



# --- Briques des cinématiques ------------------------------------------------

def drone(notes, d, wave='sine', fin=0.6, fout=1.5):
    """Nappe tenue sur plusieurs notes, qui entre et sort en douceur."""
    x = stack(*(osc(hz(note) * (1.002 if k % 2 else 1.0), d, wave) / (k + 1) ** 0.5 for k, note in enumerate(notes)))
    return x * np.clip(times(d) / fin, 0, 1) * fade_out(d, fout)


def fade_out(d, length):
    """Enveloppe à 1, qui retombe à 0 sur les {length} dernières secondes."""
    env = np.ones(n(d))
    k = min(n(length), len(env))
    env[len(env) - k:] = np.linspace(1, 0, k)
    return env


def brass_chord(notes, d, gain=1.0):
    """Accord de cuivres (menace ou triomphe selon les notes)."""
    return stack(*(brass(hz(note), d) for note in notes)) * gain / len(notes) ** 0.5


def swell(d, f0=200, f1=2400):
    """Montée de souffle et de lumière vers le fondu final."""
    rise = svf(noise(d), sweep(f0, f1, d), q=0.8) * ramp(d, 0.0, 1.0, 2.0)
    return rise


def boom(d=2.0, f=60):
    """Explosion grave avec un claquement."""
    return mix((crack_hit(), 0, 1.0), (thump(f * 1.6, f * 0.5, d, d * 0.3), 0, 1.2),
               (lp(noise(d), 600) * expdec(d, d * 0.35, attack=0.004), 0, 0.9))


def crack_hit():
    return bp(noise(0.06), 600, 9000) * expdec(0.06, 0.012, attack=0.0005)


def ambience(d, lo=200, hi=1200, gain=1.0):
    """Fond de bruit filtré (vent, mer, foule, eau)."""
    return bp(noise(d), lo, hi) * fade_out(d, 0.6) * ramp(d, 0.0, 1.0, 0.3) * gain


def bubbles(d, count, lo=300, hi=900):
    """Bulles sous l'eau : petits « bloups » qui montent."""
    track = Track(d)
    for _ in range(count):
        f = rand(lo, hi)
        blip = osc(sweep(f, f * 2.2, 0.06), 0.06) * expdec(0.06, 0.02)
        track.add(blip, rand(0, d - 0.1), rand(0.2, 0.7))
    return track.buf


def rattle(d, rate0, rate1, lo=1500, hi=5000, wood=False):
    """Cliquetis réguliers qui accélèrent ou ralentissent (roue, rails, rouleaux)."""
    track = Track(d)
    t = 0.0
    while t < d:
        k = t / d
        rate = rate0 + (rate1 - rate0) * k
        tick = modal(rand(lo, hi), 0.05, WOOD_RATIOS if wood else [1, 2.3], [0.015, 0.008]) if wood \
            else click(0.005, lo, hi)
        track.add(tick, t, rand(0.5, 1.0))
        t += 1.0 / max(rate, 1.0)
    return track.buf


def screech(d, f=2600):
    """Grincement aigu (freins, ferraille)."""
    tone = osc(vibrato(f, d, rate=17, depth=0.02), d, 'saw')
    return bp(tone, f * 0.8, f * 2.5) * 0.4 + bp(noise(d), 2000, 7000) * 0.6


# --- Tour des épreuves --------------------------------------------------------

@sound('cutscene/queen', -17)
def cutscene_queen():
    """
    Avant la Reine des Tables (QueenCutscene) : le vent, une carte qui tournoie et se colle à la roulette (1,4 s) ;
    la roulette se met à tourner (2,4 s), la bille roule (3,0 s), saute de case en case, tombe dans le zéro
    (ZERO = 6,5 s), les jetons jaillissent (6,7 s), une couronne de cartes se pose (7,8 s), la Reine rit (8,5 s),
    les dés roulent (9,0 s) et s'allument : la Reine (EYES = 9,7 s), puis le fondu rouge (10,9 s).
    """
    shift = 2.5                                                                 # les instants de la scène d'origine
    track = Track(12.0)
    track.add(bp(noise(2.6), 300, 1600) * ramp(2.6, 0.3, 1.0) * fade_out(2.6, 1.0), 0.0, 0.35)   # le vent
    for at in (0.3, 0.55, 0.8, 1.05):                                           # la carte qui tournoie
        track.add(card_flick(0.06), at, 0.4)
    track.add(card_slap(0.15), 1.4, 0.7)
    track.add(drone(['D2', 'A2', 'F3'], 10.5, 'tri', fout=1.2), 0.5, 0.12)
    track.add(ambience(10.5, 150, 600, 0.12), 0.5, 1.0)
    track.add(rattle(3.1, 4, 22, 1800, 4000, wood=True), 0.9 + 1.5, 0.35)    # la roue
    roll = svf(noise(2.7), sweep(2600, 1400, 2.7), q=3.0, mode='bp') * ramp(2.7, 0.6, 1.0)
    track.add(roll, 3.0, 0.25)                                                  # la bille qui roule
    track.add(sparkle(2.6, count=30, lo=2500, hi=6000), 3.0, 0.15)             # les cases qui s'allument
    for at in (3.2, 3.38, 3.54, 3.68, 3.8, 3.9):
        track.add(modal(rand(2600, 3400), 0.08, [1, 2.4], [0.02, 0.01]), at + shift, 0.6)
    track.add(mix((modal(2200, 0.3, WOOD_RATIOS, [0.08, 0.04, 0.02]), 0, 1.0), (thump(150, 60, 0.3), 0, 0.8)),
              4.0 + shift, 0.9)
    track.add(chips_clatter(26, 1.0, 1800, 4200), 4.2 + shift, 0.7)
    track.add(whoosh(0.9, 400, 3000, q=1.0), 4.2 + shift, 0.35)
    track.add(mix((card_slap(0.15), 0, 1.0), (chips_clatter(5, 0.2), 0.05, 0.5)), 8.2, 0.6)   # la couronne se pose
    for k in range(6):                                                          # le rire de la Reine
        f = hz('E5') * 2 ** (-(k % 3) / 12)
        ha = bp(osc(vibrato(f, 0.13, rate=28, depth=0.05), 0.13, 'saw'), 700, 3500) * adsr(0.13, 0.01, 0.04, 0.5, 0.05)
        track.add(ha, 8.5 + k * 0.15, 0.35)
    track.add(rattle(0.6, 25, 8, 1500, 3000, wood=True), 9.0, 0.4)             # les dés
    track.add(brass_chord(['D3', 'F3', 'G#3', 'D4'], 1.4, 0.6), 9.7, 1.0)      # la Reine
    track.add(thump(80, 40, 0.8, 0.3), 9.7, 0.9)
    track.add(swell(1.0, 300, 2500), 9.9, 0.25)
    return reverb(track.buf, wet=0.22, size=1.4, tail=1.0)


@sound('cutscene/comet_phase_2', -17)
def cutscene_comet_phase_2():
    """
    La Comète Dorée s'embrase (CometPhaseCutscene) : un vent de nuit, sa lueur qui bat de plus en plus vite (BEATS),
    puis l'embrasement (IGNITE = 2,7 s) : boum, souffle de feu, onde de choc, et des météores qui sifflent jusqu'au
    fondu orange (COVER_FULL = 5,3 s).
    """
    beats, ignite, end = [0.6, 1.2, 1.7, 2.1, 2.4], 2.7, 5.3
    track = Track(end + 1.2)
    track.add(ambience(end, 150, 900, 0.25), 0.0, 1.0)                          # vent de nuit
    track.add(heartbeat(beats, f=55), 0.0, 0.9)                                 # sa lueur qui bat
    for k, at in enumerate(beats):
        track.add(sparkle(0.3, count=3 + k, lo=3000, hi=8000), at, 0.12 + 0.04 * k)
    rise = ignite - 0.5                                                         # le feu qui monte en elle
    track.add(svf(noise(rise), sweep(200, 2500, rise), q=0.8) * ramp(rise, 0.0, 1.0, 2.0), 0.5, 0.35)
    boom = mix((crack(0.06, 0.01, 300, 9000), 0, 1.0), (thump(80, 30, 1.6, 0.5), 0, 1.2),
               (fire_burst(2.6), 0.02, 1.0))
    track.add(reverb(boom, wet=0.3, size=1.5, tail=1.2), ignite, 1.0)
    for k in range(8):                                                          # les météores qui sifflent
        d = 0.5
        whistle = osc(sweep(rand(2200, 3000), rand(600, 900), d), d, 'tri') * expdec(d, 0.25, attack=0.05)
        track.add(whistle, ignite + 0.2 + k * 0.22, 0.05)
        track.add(impact(0.25, 120, 50), ignite + 0.5 + k * 0.22, 0.25)
    gliss = stack(*(osc(sweep(hz(n) / 2, hz(n), 1.6), 1.6, 'tri') for n in ('A4', 'C#5', 'E5')))
    track.add(gliss * ramp(1.6, 0.0, 1.0, 1.5), end - 1.4, 0.12)                # le fondu orange
    return track.buf


@sound('cutscene/shard', -17)
def cutscene_shard():
    """
    Avant l'Éclat Originel (ShardCutscene) : au bord du cratère, la Comète fume et grésille (0 à 1,5 s) ; le cœur
    doré bat (ShardCutscene.BEATS), les veines tintent, les rouleaux sortent (4,7 s) et tournent (5,5 s), ratent deux
    fois (7,5 et 8,25 s), puis tout s'arrête net sur trois 7 (STOP = 9,0 s), le soleil éclate (SUN = 9,45 s).
    """
    beats = [2.5, 2.78, 3.5, 3.76, 4.5, 4.76, 5.35, 5.57, 6.05, 6.23, 6.6, 6.75, 7.0, 7.12, 7.28, 7.38, 8.0, 8.12,
             8.72, 8.8, 8.88]
    track = Track(12.0)
    track.add(hp(noise(1.7), 2500) * ramp(1.7, 0.6, 1.0) * fade_out(1.7, 0.4), 0.0, 0.18)   # la fumée qui siffle
    track.add(fire_burst(1.7) * fade_out(1.7, 0.4), 0.0, 0.3)
    track.add(lp(noise(1.7), 120) * fade_out(1.7, 0.4), 0.0, 0.5)
    track.add(drone(['C2', 'G2', 'C3'], 8.0, 'sine', fout=0.6), 1.5, 0.18)
    for k, at in enumerate(beats):
        track.add(thump(90, 45, 0.25, 0.08), at, 0.9 + 0.015 * k)
    for at in beats[:6]:                                                        # les veines s'allument
        track.add(modal(hz('G6'), 0.5, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.03]), at + 0.02, 0.12)
    track.add(whoosh(0.8, 200, 1800, q=1.0), 4.7, 0.4)
    for start, end in ((5.5, 7.5), (7.9, 8.25), (8.65, 9.0)):                   # les rouleaux tournent
        d = end - start
        track.add(rattle(d, 10 if start < 6 else 30, 40, 2000, 5000), start, 0.4)
        track.add(lp(osc(sweep(300 if start < 6 else 900, 1200, d), d, 'saw') * ramp(d, 0.0, 1.0), 2000), start, 0.08)
    for at in (7.5, 8.25):                                                      # les ratés : clac, clac... bzz
        for k in range(3):
            track.add(mix((thump(220, 90, 0.2, 0.05), 0, 1.0), (click(0.01, 1500, 6000), 0, 0.8)), at + k * 0.05, 0.6)
        track.add(chip(sweep(hz('E3'), hz('C3'), 0.3), 0.3, 'square', dc=0.03, s=0.6), at + 0.12, 0.25)
    for k in range(3):                                                          # trois 7 : clac, clac, clac
        track.add(mix((thump(220, 90, 0.2, 0.05), 0, 1.0), (click(0.01, 1500, 6000), 0, 0.8)), 9.0 + k * 0.05, 0.8)
    track.add(boom(2.0, 55), 9.45, 1.0)
    track.add(bingo_jingle('C5', 'square', step=0.05, hold=1.0) * 1.2, 9.55, 0.8)
    track.add(sparkle(1.3, count=30), 9.5, 0.4)
    return reverb(track.buf, wet=0.25, size=1.6, tail=1.0)


@sound('cutscene/pretender', -17)
def cutscene_pretender():
    """
    Avant le Prétendant (PretenderCutscene) : une main lâche un éclat (0,55 s) qui rebondit et roule ; la ville brûle,
    une ombre passe (3,3 à 5,5 s) et les jetons fondent, elle ramasse trois éclats (PICK = 5,8 / 6,25 / 6,7 s), le
    Prétendant apparaît (REVEAL = 7,2 s), sa couronne se pose (8,7 s) et il lève les yeux (STARE = 9,4 s).
    """
    track = Track(11.8)
    track.add(fire_burst(11.0) * fade_out(11.0, 1.0), 0.0, 0.35)
    for k, at in enumerate((0.55, 0.8, 1.0, 1.15)):                           # l'éclat qui tombe et rebondit
        track.add(modal(hz('E7'), 0.4, GLASS_RATIOS, [0.4, 0.2, 0.1, 0.05]), at, 0.35 * 0.7 ** k)
    track.add(rattle(0.4, 30, 20, 3000, 6000), 1.15, 0.15)
    track.add(ambience(8.5, 400, 1800, 0.15), 1.5, 1.0)                       # cris lointains de la foule
    for _ in range(10):
        shout = svf(noise(0.4), sweep(rand(500, 900), rand(300, 500), 0.4), q=4.0, mode='bp') * expdec(0.4, 0.15)
        track.add(shout, rand(1.7, 3.5), rand(0.1, 0.25))
    track.add(whoosh(2.4, 120, 600, q=0.8), 3.2, 0.7)                         # l'ombre
    sizzle = hp(noise(2.0), 3000) * ramp(2.0, 0.0, 1.0) * fade_out(2.0, 0.6)  # les jetons fondent
    track.add(sizzle, 3.6, 0.12)
    for k, (at, note) in enumerate(zip((5.8, 6.25, 6.7), ('E6', 'G6', 'B6'))):
        track.add(modal(hz(note), 1.0, GLASS_RATIOS, [0.5, 0.3, 0.15, 0.08]), at, 0.45)
    track.add(brass_chord(['E2', 'B2', 'E3', 'G3'], 1.6, 0.7), 7.2, 1.0)
    track.add(sparkle(0.9, count=14), 7.8, 0.3)                               # la couronne descend
    track.add(mix((metal_hit(600, 0.6), 0, 0.6), (thump(140, 70, 0.2, 0.05), 0, 0.8)), 8.7, 0.7)
    track.add(thump(70, 35, 1.0, 0.4), 9.4, 1.0)
    track.add(drone(['E2', 'A#2'], 1.4, 'saw', fin=0.3, fout=0.6) * 0.3, 9.4, 0.4)
    return reverb(track.buf, wet=0.2, size=1.3, tail=0.8)


@sound('cutscene/collector', -17)
def cutscene_collector():
    """
    Avant le Collectionneur (CollectorCutscene) : la plaine la nuit, vent et grillons, les éclats tintent (0 à 1,4 s),
    se soulèvent (LIFT = 1,4 s) puis filent (FLY = 2,6 s) ; dans la vitrine, chacun se range d'un clic de verre
    (SHELVE = 3,9 … 4,95 s) ; une boîte à musique, il s'allume derrière la vitre (REVEAL = 5,6 s), la porte grince
    (DOOR = 6,3 s), trois éclats se sertissent (SET = 7,0 / 7,3 / 7,6 s), le bleu s'allume (LIGHT = 8,1 s), éclair
    de sa loupe (GLINT = 8,6 s). Fondu blanc à 9,2 s.
    """
    track = Track(10.6)
    track.add(ambience(5.0, 300, 1400, 0.12) * fade_out(5.0, 1.2), 0.0, 1.0)                 # le vent sur la plaine
    for _ in range(14):                                                                     # les éclats qui tintent
        track.add(modal(hz(['C7', 'E7', 'G7', 'B6'][int(rand(0, 4))]), 0.6, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]),
                  rand(0.1, 2.4), rand(0.05, 0.12))
    track.add(sparkle(1.2, count=16), 1.4, 0.25)                                            # ils se soulèvent
    track.add(whoosh(1.0, 300, 2600, q=0.9), 2.6, 0.5)                                      # ils filent
    for k, at in enumerate((3.9, 4.15, 4.4, 4.6, 4.8, 4.95)):                               # clic, clic, clic
        track.add(modal(hz(['E6', 'G6', 'B6', 'D7', 'E7', 'G7'][k]), 0.5, GLASS_RATIOS, [0.5, 0.25, 0.1, 0.05]), at, 0.4)
        track.add(click(0.01, 2000, 7000), at, 0.3)
    for k, note in enumerate(['E5', 'G5', 'B5', 'E6', 'D6', 'B5', 'G5', 'A5', 'B5']):        # sa boîte à musique
        track.add(pluck(hz(note), 0.8, bright=5000), 5.2 + k * 0.3, 0.22)
    track.add(drone(['E2', 'B2', 'E3'], 4.0, 'sine', fin=0.6, fout=0.8), 5.6, 0.18)
    track.add(svf(noise(0.6), sweep(900, 400, 0.6), q=6.0, mode='bp') * fade_out(0.6, 0.3), 6.3, 0.2)   # la porte grince
    for at, note in zip((7.0, 7.3, 7.6), ('B6', 'D7', 'F#7')):                              # trois éclats sertis
        track.add(mix((metal_hit(1800, 0.4), 0, 0.4), (modal(hz(note), 0.8, GLASS_RATIOS, [0.5, 0.3, 0.15, 0.08]), 0, 0.6)), at, 0.5)
    track.add(boom(1.4, 70), 8.1, 0.6)                                                      # le bleu s'allume
    track.add(brass_chord(['E2', 'B2', 'E3', 'G3'], 1.6, 0.7), 8.1, 0.8)
    track.add(sparkle(0.7, count=12, lo=4000, hi=9000), 8.6, 0.35)                          # l'éclair de sa loupe
    track.add(swell(0.7, 300, 2600), 9.1, 0.25)
    return reverb(track.buf, wet=0.25, size=1.5, tail=1.0)


@sound('cutscene/general', -17)
def cutscene_general():
    """
    Avant le Général Va-Tout (GeneralCutscene) : les canons tirent un jeton (SHOTS = 0,4 / 1,0 / 1,5 / 2,0 s) qui
    explose 0,6 s plus tard ; ses soldats entrent au pas (2,8 à 4,8 s, un coup de tambour toutes les 0,25 s) ; il se
    lève (RISE = 5,2 s), pousse ses piles de jetons (PUSH = 6,4 s) qui se rejoignent (ALL_IN = 7,2 s) ; son regard
    (STARE = 8,0 s).
    """
    track = Track(10.2)
    track.add(ambience(9.5, 250, 1400, 0.2), 0.0, 1.0)
    for at in (0.4, 1.0, 1.5, 2.0):                                            # les canons
        track.add(mix((thump(110, 40, 0.4, 0.08), 0, 1.0), (click(0.02, 600, 2500), 0, 0.7)), at, 0.8)
        track.add(whoosh(0.6, 400, 1600, q=1.2), at + 0.02, 0.25)
        track.add(mix((impact(0.5, 120, 40), 0, 1.0), (chips_clatter(6, 0.3), 0.02, 0.6)), at + 0.6, 0.8)
    for k in range(9):                                                         # le tambour de la marche
        hit = mix((bp(noise(0.12), 180, 1800) * expdec(0.12, 0.04), 0, 1.0), (thump(160, 90, 0.12, 0.03), 0, 0.8))
        track.add(hit, 2.8 + k * 0.25, 0.5 if k % 2 else 0.7)
    track.add(brass_chord(['D3', 'A3', 'D4', 'F#4'], 1.4, 0.7), 5.2, 1.0)    # il se lève
    for k in range(8):                                                         # les piles qui glissent
        track.add(chips_clatter(3, 0.12), 6.4 + k * 0.1, 0.35)
    track.add(mix((impact(0.8, 100, 35), 0, 1.2), (chips_clatter(12, 0.5), 0.0, 0.9)), 7.2, 1.0)
    track.add(sparkle(1.0, count=20), 7.25, 0.35)
    track.add(thump(70, 35, 1.0, 0.4), 8.0, 1.0)
    track.add(drone(['D2', 'G#2'], 1.4, 'saw', fin=0.3, fout=0.6) * 0.3, 8.0, 0.4)
    return reverb(track.buf, wet=0.22, size=1.4, tail=0.8)


@sound('cutscene/house', -17)
def cutscene_house():
    """
    Avant la Maison (HouseCutscene) : on gravit l'escalier de jetons (0 à 1,5 s, un clac par marche), vent au-dessus
    des nuages ; les rideaux s'ouvrent (2,5 s), le majordome s'incline (3,0 s) ; les fenêtres-cartes se retournent
    (3,5 s, une toutes les 0,13 s), la porte s'ouvre (5,2 s), la pièce roule (5,7 s) et entre dans la fente (6,8 s),
    le manoir s'allume (6,9 s) ; les As rient (8,0 à 9,6 s), les portes claquent (SLAM = 9,9 s).
    """
    shift = 2.5                                                                 # les instants de la scène d'origine
    track = Track(11.0)
    track.add(ambience(10.0, 300, 1500, 0.25), 0.0, 1.0)
    for k in range(7):                                                          # nos pas sur les jetons
        track.add(chips_clatter(3, 0.12), 0.1 + k * 0.2, 0.5)
    curtain = bp(noise(0.5), 400, 3000) * ramp(0.5, 0.0, 1.0) * fade_out(0.5, 0.3)
    track.add(curtain, 2.5, 0.4)
    track.add(stack(chip(hz('G4'), 0.25, 'tri', s=0.6), chip(hz('C5'), 0.35, 'tri', s=0.6)), 3.05, 0.2)   # sa révérence
    for k in range(12):
        track.add(card_flick(0.07), 1.0 + shift + k * 0.13, 0.6)
        track.add(modal(hz('A5') * 2 ** (k / 12), 0.25, GLASS_RATIOS, [0.12, 0.06, 0.03, 0.02]),
                  1.05 + shift + k * 0.13, 0.15)
    creak = svf(osc(sweep(90, 140, 0.5), 0.5, 'saw'), 900, q=3.0, mode='bp') * ramp(0.5, 1.0, 0.3)
    track.add(creak, 2.7 + shift, 0.5)
    track.add(rattle(1.1, 12, 26, 2500, 4500), 3.2 + shift, 0.25)            # la pièce qui roule
    track.add(coin(1500, 0.6), 4.3 + shift, 0.7)
    track.add(mix((thump(160, 70, 0.3, 0.08), 0, 1.0), (click(0.02, 800, 3000), 0, 0.6)), 4.32 + shift, 0.8)
    track.add(brass_chord(['C3', 'E3', 'G3', 'C4'], 1.5, 0.6), 4.4 + shift, 1.0)
    track.add(bingo_jingle('C5', 'square', hold=0.9), 4.45 + shift, 0.6)
    for k in range(12):                                                         # les As se retournent
        track.add(card_flick(0.05), 8.0 + (k % 6) * 0.05, 0.4)
    for k in range(8):                                                          # et rient : « ha ! ha ! »
        f = hz('A3') * 2 ** (-(k % 4) / 12) * (1.0 if k < 4 else 1.5)
        ha = bp(osc(vibrato(f, 0.14, rate=30, depth=0.04), 0.14, 'saw'), 500, 2500) * adsr(0.14, 0.01, 0.04, 0.5, 0.06)
        track.add(ha, 8.3 + k * 0.17, 0.5)
    track.add(mix((impact(0.6, 120, 40), 0, 1.2), (crack_hit(), 0, 0.8)), 9.9, 1.0)
    return reverb(track.buf, wet=0.28, size=1.7, tail=1.0)


@sound('cutscene/architect', -17)
def cutscene_architect():
    """
    Avant l'Architecte (ArchitectCutscene) : on monte le long de la Tour (0 à 1,6 s), vent et notes qui montent ;
    le plan se déroule (1,6 s) ; huit étages se dessinent à la craie (2,3 s, un toutes les 0,28 s), la Machine au
    sommet (4,64 s) ; l'Architecte sort de l'ombre (5,4 s), son compas trace un cercle (6,0 à 6,9 s) ; les traits
    s'embrasent d'or (IGNITE = 8,4 s).
    """
    track = Track(11.0)
    track.add(ambience(1.8, 400, 2400, 0.35), 0.0, 1.0)
    for k, note in enumerate(('C4', 'E4', 'G4', 'C5')):                       # la montée
        track.add(modal(hz(note), 0.6, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), 0.2 + k * 0.35, 0.3)
    rustle = bp(noise(0.6), 800, 5000) * ramp(0.6, 0.2, 1.0) * fade_out(0.6, 0.25)
    track.add(rustle, 1.6, 0.5)                                                 # le plan qui se déroule
    for k in range(8):                                                          # la craie, étage par étage
        scratch = bp(noise(0.22), 2500 + k * 200, 7000) * adsr(0.22, 0.01, 0.05, 0.6, 0.08)
        track.add(scratch, 2.3 + k * 0.28, 0.35)
        track.add(modal(hz('G4') * 2 ** (k / 12), 0.3, GLASS_RATIOS, [0.12, 0.06, 0.03, 0.02]), 2.3 + k * 0.28 + 0.2, 0.15)
    track.add(brass_chord(['G2', 'D3', 'G3', 'B3'], 1.2, 0.5), 4.64, 0.6)     # la Machine, au sommet
    track.add(drone(['G1', 'D2'], 2.6, 'saw', fin=0.6, fout=0.8) * 0.3, 5.4, 0.5)   # l'Architecte
    circle = bp(osc(sweep(300, 900, 0.9), 0.9, 'saw'), 1500, 5000) * 0.3 * fade_out(0.9, 0.2)
    track.add(circle, 6.0, 0.35)                                                # le compas
    track.add(mix((impact(0.7, 120, 40), 0, 1.0), (metal_hit(700, 0.8), 0, 0.5)), 8.4, 0.9)   # tout s'embrase
    track.add(brass_chord(['D3', 'G3', 'B3', 'D4'], 1.8, 0.8), 8.45, 0.9)
    track.add(sparkle(1.2, count=18), 8.5, 0.35)
    return reverb(track.buf, wet=0.3, size=1.6, tail=1.0)


@sound('cutscene/machine', -17)
def cutscene_machine():
    """
    Avant la Machine Originelle (MachineCutscene) : on traverse les étages de la Tour (0 à 1,5 s), un tintement de
    verre à chaque boss figé ; on monte le long de la machine (1,5 à 4,5 s), ses tuyaux battent ; la fente s'ouvre
    (4,4 s), une pièce tombe et tinte (5,2 s), silence ; le levier descend (5,9 à 7,2 s) et cogne, les rouleaux
    s'allument (7,35 s, un toutes les 0,2 s), tournent (8,3 s) et s'arrêtent (9,5 s, un toutes les 0,14 s) sur cinq
    yeux ; fondu blanc à 11,2 s.
    """
    shift = 4.0                                                                 # la scène d'origine commence à 4 s
    track = Track(12.5)
    track.add(lp(noise(1.6), 300) * ramp(1.6, 0.2, 1.0) * fade_out(1.6, 0.3), 0.0, 0.5)   # on monte, vite
    track.add(whoosh(1.5, 200, 1400), 0.0, 0.3)
    for k, at in enumerate((0.1, 0.38, 0.65, 0.92, 1.2)):                       # chaque boss figé dans le verre
        track.add(modal(hz(['E6', 'G6', 'B6', 'D7', 'E7'][k]), 0.8, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), at, 0.3)
    track.add(drone(['A1', 'E2', 'A2'], 10.5, 'tri', fout=1.0), 1.5, 0.15)
    track.add(ambience(10.0, 200, 900, 0.18), 1.5, 1.0)
    track.add(heartbeat([1.5 + k / 1.4 for k in range(4)], f=55, gain=0.8), 0.0, 0.7)   # les tuyaux battent
    track.add(mix((click(0.01, 1500, 5000), 0, 0.8), (metal_hit(900, 0.3), 0, 0.3)), 4.4, 0.5)   # la fente s'ouvre
    fall = 0.55
    track.add(osc(sweep(2400, 900, fall), fall, 'sine') * ramp(fall, 0.0, 1.0) * 0.3, 4.65, 0.15)
    track.add(mix((coin(), 0, 1.0), (modal(hz('E7'), 1.4, [1, 2.76, 5.4], [0.5, 0.2, 0.08], [1, 0.3, 0.12]), 0, 0.5)),
              5.2, 0.8)                                                         # clinc
    track.add(rattle(1.3, 6, 3, 600, 1500, wood=True), 1.9 + shift, 0.6)        # le levier qui grince
    track.add(mix((metal_hit(180, 1.2), 0, 1.0), (impact(0.5, 100, 35), 0, 1.0)), 3.2 + shift, 0.9)
    for k in range(5):
        track.add(chip(hz('A3') * 2 ** (k * 3 / 12), 0.18, 'square', dc=0.05, s=0.5), 3.35 + shift + k * 0.2, 0.35)
        track.add(thump(60, 40, 0.3, 0.1), 3.35 + shift + k * 0.2, 0.6)
    track.add(lp(noise(2.6), 120) * ramp(2.6, 0.3, 1.0), 3.35 + shift, 0.8)   # le sol tremble
    track.add(rattle(1.2, 18, 34, 2000, 5000), 4.3 + shift, 0.45)
    for k in range(5):
        track.add(mix((thump(240, 90, 0.18, 0.05), 0, 1.0), (click(0.01, 1500, 6000), 0, 0.7)),
                  5.5 + shift + k * 0.14, 0.8)
    track.add(brass_chord(['A2', 'C3', 'D#3', 'A3'], 1.4, 0.7), 6.2 + shift, 1.0)
    track.add(swell(1.0, 400, 4000), 6.3 + shift, 0.3)
    buf = reverb(track.buf, wet=0.25, size=1.6, tail=1.0)
    hush = np.ones(len(buf))                                                    # le silence après le clinc
    a, b, c = n(5.45), n(5.6), n(6.0)
    hush[a:b] = np.linspace(1, 0.25, b - a)
    hush[b:c] = np.linspace(0.25, 1, c - b)
    return buf * hush


def machine_phase(phase):
    """
    Changement de phase de la Machine Originelle (MachinePhaseCutscene) : l'alarme à deux tons à partir de 0,6 s (un
    battement toutes les 0,5 s) ; la chaîne descend (1,5 à 2,4 s), la pince se referme (2,4 s), la chaîne remonte
    (2,7 à 3,9 s) ; ton rouleau entre dans son flanc et cogne (3,9 s), il tourne pour elle. Phase 3 : grondement,
    craquements et étincelles des fissures, puis les chaînes rouges qui verrouillent le rouleau (4,3 s). Fondu à 6 s.
    """
    track = Track(6.6)
    root = ['A1', 'E2', 'A2'] if phase == 2 else ['F1', 'C2', 'F#2']
    track.add(drone(root, 6.0, 'tri', fout=0.8), 0.0, 0.18 if phase == 2 else 0.26)
    track.add(ambience(6.0, 150, 700, 0.15), 0.0, 1.0)
    for k in range(10):                                                        # l'alarme, deux tons
        at = 0.6 + k * 0.5
        f = hz('E5') if k % 2 == 0 else hz('C5')
        track.add(chip(f, 0.22, 'square', dc=0.1, s=0.4), at, 0.22)
        track.add(thump(70, 40, 0.25, 0.08), at, 0.5)
    track.add(rattle(0.9, 10, 22, 1800, 4500), 1.5, 0.5)                       # la chaîne descend
    track.add(mix((metal_hit(520, 0.5), 0, 1.0), (click(0.01, 1500, 6000), 0, 0.8)), 2.4, 0.8)   # la pince
    track.add(rattle(1.2, 22, 12, 1800, 4500), 2.7, 0.5)                       # la chaîne remonte
    track.add(mix((metal_hit(160, 1.1), 0, 1.0), (impact(0.6, 110, 35), 0, 1.0)), 3.9, 1.0)      # dans son flanc
    track.add(rattle(0.9, 26, 6, 2000, 5000), 3.95, 0.45)                     # ton rouleau tourne pour elle
    if phase >= 3:
        track.add(lp(noise(4.6), 140) * ramp(4.6, 0.4, 1.0), 0.6, 0.8)          # le sol gronde
        for k in range(14):                                                    # les fissures craquent
            track.add(crack_hit(), 0.7 + k * 0.33 + rand(0.0, 0.12), rand(0.25, 0.5))
        track.add(hp(noise(4.4), 4000) * 0.12, 0.7, 0.4)                       # étincelles
        for k in range(3):                                                     # les chaînes rouges claquent
            track.add(metal_hit(300 - k * 40, 0.7), 4.3 + k * 0.09, 0.7)
        track.add(brass_chord(['F2', 'G#2', 'B2', 'F3'], 1.3, 0.7), 4.4, 1.0)
    else:
        track.add(brass_chord(['A2', 'C3', 'D#3', 'A3'], 1.2, 0.6), 4.1, 0.9)
    track.add(swell(0.8, 300, 3000), 4.9, 0.3)
    return reverb(track.buf, wet=0.25, size=1.6, tail=0.8)


@sound('cutscene/machine_phase_2', -18)
def cutscene_machine_phase_2():
    return machine_phase(2)


@sound('cutscene/machine_phase_3', -17)
def cutscene_machine_phase_3():
    return machine_phase(3)


@sound('cutscene/boss_phase', -17)
def cutscene_boss_phase():
    """
    Un boss ou un roi passe à sa phase suivante (BossMomentCutscene, PHASE) : un bourdon grave, sa lueur qui bat de
    plus en plus vite (BEATS), le souffle qui monte, puis l'explosion de puissance (BURST = 2,2 s) : boum, souffle,
    onde de choc et un accord de cuivres menaçant pendant que la phase s'annonce. Fondu noir à 4,8 s.
    """
    beats, burst, end = [0.5, 1.0, 1.4, 1.75, 2.0], 2.2, 4.8
    track = Track(end + 1.0)
    track.add(drone(['D1', 'A1', 'D2'], end, 'tri', fout=0.8), 0.0, 0.2)
    track.add(ambience(end, 150, 800, 0.2), 0.0, 1.0)
    track.add(heartbeat(beats, f=55), 0.0, 0.9)
    for k, at in enumerate(beats):
        track.add(sparkle(0.25, count=2 + k, lo=2500, hi=7000), at, 0.1 + 0.03 * k)
    rise = burst - 0.4
    track.add(svf(noise(rise), sweep(150, 2200, rise), q=0.8) * ramp(rise, 0.0, 1.0, 2.0), 0.4, 0.3)
    boom = mix((crack(0.06, 0.01, 300, 9000), 0, 1.0), (thump(70, 28, 1.5, 0.5), 0, 1.2), (fire_burst(1.8), 0.02, 0.8))
    track.add(reverb(boom, wet=0.3, size=1.5, tail=1.2), burst, 1.0)
    track.add(brass_chord(['D2', 'F2', 'G#2', 'D3'], 1.8, 0.8), burst + 0.25, 0.9)
    track.add(swell(0.8, 300, 2600), end - 0.9, 0.25)
    return track.buf


@sound('cutscene/boss_death', -17)
def cutscene_boss_death():
    """
    Un boss ou un roi meurt (BossMomentCutscene, DEATH) : il tremble (grondement qui monte, craquements de plus en
    plus serrés, deux secousses à 1,2 et 2,0 s), il vole en éclats (SHATTER = 2,5 s) : explosion de verre et de
    métal, puis une pluie de pièces et un accord qui descend jusqu'au fondu noir (5,6 s).
    """
    shatter, end = 2.5, 5.6
    track = Track(end + 1.2)
    track.add(lp(noise(shatter), 140) * ramp(shatter, 0.2, 1.0, 1.5), 0.0, 0.8)    # il tremble
    at = 0.3
    while at < shatter - 0.1:                                                     # des craquements de plus en plus serrés
        track.add(crack_hit(), at, rand(0.2, 0.4))
        at += max(0.08, 0.45 - 0.16 * at)
    for hit in (1.2, 2.0):
        track.add(impact(0.4, 120, 40), hit, 0.6)
    boom = mix((crack(0.08, 0.012, 400, 10000), 0, 1.0), (thump(60, 25, 1.8, 0.6), 0, 1.3),
               (hp(noise(1.2), 3000) * expdec(1.2, 0.25), 0.01, 0.6), (metal_hit(420, 1.0), 0.02, 0.6))
    track.add(reverb(boom, wet=0.35, size=1.7, tail=1.5), shatter, 1.0)
    for k in range(18):                                                           # la pluie de pièces
        track.add(coin(rand(1700, 2600), 0.35), shatter + 0.2 + k * 0.15 + rand(0.0, 0.08), rand(0.08, 0.16))
    fall = stack(*(osc(sweep(hz(note), hz(note) / 2, 2.2), 2.2, 'tri') for note in ('A3', 'C4', 'E4')))
    track.add(fall * expdec(2.2, 1.2, attack=0.05), shatter + 0.4, 0.12)
    track.add(drone(['A1', 'E2'], end - shatter, 'sine', fin=0.3, fout=1.0), shatter, 0.2)
    return track.buf


@sound('cutscene/last_draw', -18)
def cutscene_last_draw():
    """
    Dernier tirage (LastDrawCutscene), l'intro : la Machine fissurée grésille, ses rouleaux tournent au ralenti et
    s'arrêtent (2,0 s) ; ta machine monte du sol (2,0 à 3,4 s) et se pose, son œil tinte. Puis roulement de caisse
    claire qui monte, « DERNIER TIRAGE » s'imprime (4,4 à 4,7 s) sur un accord de cuivres, les noms s'allument
    (4,8 s) ; elle parle à 5,6 s.
    """
    arrive = 4.0
    track = Track(arrive + 2.2)
    track.add(ambience(arrive, 150, 700, 0.12), 0.0, 1.0)
    for _ in range(70):                                                         # elle grésille
        at = rand(0, 1) ** 1.6 * (arrive + 0.8)
        track.add(bp(noise(0.008), 1500, 7000) * expdec(0.008, 0.002), at, rand(0.2, 0.8))
    t, gap = 0.3, 0.06                                                          # ses rouleaux, au ralenti
    while t < 2.0:
        track.add(click(0.006, 900, 3000), t, 0.5)
        t += gap
        gap *= 1.12
    track.add(mix((thump(200, 80, 0.2, 0.06), 0, 1.0), (metal_hit(320, 0.4), 0, 0.4)), 2.0, 0.6)
    rise = 1.4                                                                  # ta machine monte du sol
    track.add(lp(noise(rise), 250) * ramp(rise, 0.3, 1.0), 2.0, 0.7)
    track.add(osc(sweep(70, 140, rise), rise, 'saw') * ramp(rise, 0.0, 1.0) * fade_out(rise, 0.2), 2.0, 0.08)
    track.add(impact(0.5, 120, 40), 3.4, 1.0)
    track.add(modal(hz('A6'), 1.0, [1, 2.76, 5.4], [0.5, 0.2, 0.08], [1, 0.3, 0.12]), 3.7, 0.25)   # son œil
    t, interval = 0.0, 0.09
    while t < 0.7:
        snare = stack(bp(noise(0.05), 1200, 7000) * expdec(0.05, 0.015), 0.3 * thump(240, 180, 0.04, 0.015))
        track.add(snare, arrive + t, 0.3 + t)
        t += interval
        interval = max(0.035, interval * 0.9)
    track.add(mix((impact(0.4, 150, 50), 0, 1.0), (metal_hit(220, 0.6), 0, 0.4)), arrive + 0.7, 0.9)
    track.add(brass_chord(['A2', 'E3', 'A3', 'C4'], 0.9, 0.7), arrive + 0.7, 0.9)
    for k in range(4):                                                          # les ampoules s'allument
        track.add(chip(hz('A5') * 2 ** (k * 3 / 12), 0.1, 'square', dc=0.03, s=0.5), arrive + 0.85 + k * 0.08, 0.25)
    track.add(heartbeat([arrive + 1.2, arrive + 1.45]), 0.0, 0.6)
    return reverb(track.buf, wet=0.25, size=1.4, tail=0.8)


def lever_pull(d, heavy=False):
    """Un levier qu'on tire : cliquetis du cran qui descend, puis le choc en bas ({d} secondes plus tard)."""
    track = Track(d + 0.6)
    track.add(rattle(d, 10, 22, 500, 1400, wood=True) if heavy else rattle(d, 14, 30, 1500, 4000), 0.0, 0.7)
    hit = mix((metal_hit(140 if heavy else 320, 0.9 if heavy else 0.4), 0, 1.0),
              (impact(0.5 if heavy else 0.25, 110 if heavy else 200, 35 if heavy else 70), 0, 1.0))
    track.add(hit, d, 1.0 if heavy else 0.75)
    return track.buf


@sound('cutscene/last_draw_round', -18)
def cutscene_last_draw_round():
    """
    Une manche du Dernier tirage, à partir du moment où ton levier touche le fond (0 s) : ton rouleau tourne ;
    son levier descend tout seul (0,35 à 0,65 s, plus lourd), son rouleau tourne ; le tien s'arrête (1,6 s) en
    ralentissant, suspense, puis le sien (2,45 s).
    """
    track = Track(3.0)
    track.add(mix((metal_hit(320, 0.4), 0, 1.0), (impact(0.25, 200, 70), 0, 1.0)), 0.0, 0.75)
    track.add(rattle(1.6, 30, 6, 2000, 5000), 0.0, 0.4)                       # ton rouleau qui ralentit
    track.add(lever_pull(0.3, heavy=True), 0.35, 1.0)
    track.add(rattle(1.8, 30, 4, 900, 2600), 0.65, 0.4)                       # le sien, plus grave
    track.add(lp(noise(1.8), 110) * ramp(1.8, 0.6, 0.2), 0.65, 0.7)           # le sol tremble
    stop = mix((thump(220, 90, 0.12, 0.03), 0, 1.0), (click(0.005, 1500, 6000), 0, 0.8),
               (modal(hz('E6'), 0.25, BAR_RATIOS, [0.07, 0.03, 0.015, 0.008]), 0.004, 0.35))
    track.add(stop, 1.6, 0.9)
    t, interval = 1.65, 0.1                                                    # suspense : la caisse claire accélère
    while t < 2.43:
        snare = stack(bp(noise(0.05), 1200, 7000) * expdec(0.05, 0.015), 0.3 * thump(240, 180, 0.04, 0.015))
        track.add(snare, t, 0.3 + 0.6 * (t - 1.65) / 0.8)
        t += interval
        interval = max(0.03, interval * 0.85)
    rise = osc(vibrato(sweep(260, 700, 0.8), 0.8, rate=7, depth=0.015), 0.8, 'tri') * ramp(0.8, 0.1, 0.8, 1.5)
    track.add(lp(rise, 3000), 1.65, 0.35)
    track.add(mix((thump(160, 60, 0.25, 0.06), 0, 1.0), (click(0.01, 1200, 5000), 0, 0.8),
                  (metal_hit(260, 0.5), 0, 0.4)), 2.45, 1.0)
    return reverb(track.buf, wet=0.2, size=1.2, tail=0.5)


@sound('cutscene/last_draw_notch', -24)
def cutscene_last_draw_notch():
    """Dernier tirage : un cran de ton levier, pendant que tu le tires à la souris."""
    return mix((click(0.005, 1500, 5000), 0, 1.0), (modal(rand(900, 1100), 0.06, WOOD_RATIOS, [0.02, 0.01, 0.006]), 0, 0.6))


@sound('cutscene/last_draw_tie', -20)
def cutscene_last_draw_tie():
    """Égalité au Dernier tirage : deux notes en suspens et des jetons qui retombent ; on relance."""
    track = Track(1.2)
    track.add(chip(hz('E5'), 0.16, 'tri', s=0.5), 0.0, 0.8)
    track.add(chip(hz('E5'), 0.3, 'tri', s=0.5, r=0.15), 0.18, 0.8)
    track.add(chips_clatter(6, 0.4), 0.3, 0.5)
    return reverb(track.buf, wet=0.2, tail=0.4)


@sound('cutscene/last_draw_win', -16)
def cutscene_last_draw_win():
    """Dernier tirage gagné : sa vitre se fend, BINGO, ta machine crache des pièces, sa machine grésille et s'éteint."""
    track = Track(3.0)
    track.add(mix((crack_hit(), 0, 1.0), (modal(3200, 0.5, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.03]), 0, 0.5)), 0.0, 0.9)
    track.add(bingo_jingle('C5', 'square', hold=0.9), 0.05, 0.6)
    track.add(coin_shower(1.8, 40), 0.1, 0.6)
    for k in range(4):
        track.add(cash_register_bell(), 0.2 + k * 0.18, 0.3)
    hum = osc(sweep(hz('A2'), hz('A1'), 1.2), 1.2, 'saw') * ramp(1.2, 0.6, 0.0)
    track.add(lp(hum, 900), 0.2, 0.35)
    track.add(brass_chord(['C3', 'E3', 'G3', 'C4'], 1.2, 0.6), 0.9, 0.8)
    return reverb(track.buf, wet=0.25, size=1.3, tail=0.8)


@sound('cutscene/last_draw_lose', -16)
def cutscene_last_draw_lose():
    """Dernier tirage perdu : ta machine grille dans un grésillement, son œil flambe, accord grave qui descend."""
    track = Track(3.0)
    fizz = bp(noise(1.2), 1500, 7000) * (0.5 + 0.5 * (rand(0, 1, n(1.2)) > 0.6)) * ramp(1.2, 1.0, 0.0)
    track.add(fizz, 0.0, 0.5)
    track.add(mix((impact(0.5, 120, 40), 0, 1.0), (metal_hit(150, 0.8), 0, 0.5)), 0.0, 0.9)
    power = osc(sweep(hz('A3'), hz('A1'), 1.0), 1.0, 'saw') * ramp(1.0, 0.8, 0.0)
    track.add(lp(power, 1200), 0.05, 0.4)
    track.add(brass_chord(['A1', 'C2', 'D#2', 'A2'], 1.8, 0.7), 0.3, 1.0)
    track.add(lp(noise(1.6), 90) * ramp(1.6, 1.0, 0.0), 0.3, 0.9)
    return reverb(track.buf, wet=0.3, size=1.6, tail=1.0)


@sound('cutscene/shard_ending', -18)
def cutscene_shard_ending():
    """
    Fin du chapitre 3 (ShardEndingCutscene) : le soleil bourdonne et s'éteint (0,8 à 2,0 s), se brise (BREAK
    = 2,0 s), puis la neige dorée tombe sur une boîte à musique ; les habitants sortent sur les toits (2,6 s) et
    s'émerveillent (3,6 s) ; trois éclats filent vers l'horizon (6,2 à 8,5 s) ; fondu noir (10,9 s).
    """
    track = Track(12.0)
    hum = stack(osc(sweep(hz('C4'), hz('C3'), 2.0), 2.0, 'tri'), 0.5 * osc(sweep(hz('G4'), hz('G3'), 2.0), 2.0))
    track.add(hum * ramp(2.0, 1.0, 0.4), 0.0, 0.25)
    shatter = Track(1.2)
    for _ in range(40):
        shatter.add(modal(rand(2500, 7000), 0.5, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.03]), rand(0, 0.5), rand(0.2, 0.7))
    track.add(mix((crack_hit(), 0, 1.0), (shatter.buf, 0, 0.6), (thump(90, 40, 0.6, 0.2), 0, 0.8)), 2.0, 1.0)
    melody = ['E6', 'C6', 'G5', 'C6', 'D6', 'G5', 'E6', 'D6', 'C6', 'G5', 'A5', 'C6',
              'E6', 'G6', 'E6', 'D6', 'C6', 'D6', 'E6', 'C6', 'G5', 'C6', 'E6', 'C6']
    for k, note in enumerate(melody):                                          # boîte à musique
        track.add(modal(hz(note), 1.2, BAR_RATIOS, [0.6, 0.2, 0.1, 0.05], [1, 0.3, 0.1, 0.05]), 2.4 + k * 0.32, 0.3)
    track.add(drone(['C3', 'G3', 'E4'], 8.6, 'sine', fin=1.0, fout=1.5), 2.3, 0.15)
    track.add(ambience(2.5, 300, 1400, 0.3), 3.5, 0.6)                         # la foule s'émerveille : « oh ! »
    for _ in range(9):
        f = rand(250, 420)
        oh = bp(osc(sweep(f, f * 1.15, 0.5), 0.5, 'saw'), 300, 1800) * adsr(0.5, 0.08, 0.1, 0.6, 0.25)
        track.add(oh, rand(3.6, 4.6), rand(0.08, 0.16))
    for k, note in enumerate(('E6', 'G6', 'B6')):                               # trois éclats filent au loin
        track.add(whoosh(1.4, 1500, 6000, q=1.5) * fade_out(1.4, 0.6), 6.2 + k * 0.15, 0.25)
        track.add(modal(hz(note), 1.4, GLASS_RATIOS, [0.4, 0.2, 0.1, 0.05]), 6.2 + k * 0.15, 0.3)
    return reverb(track.buf, wet=0.35, size=1.8, tail=1.5)


@sound('cutscene/jackpot_ending', -17)
def cutscene_jackpot_ending():
    """
    Fin du chapitre 6 (JackpotEndingCutscene) : la machine s'éteint (0,6 s), ses yeux se ferment un par un (1,3 s,
    toutes les 0,25 s ; le dernier cligne à 2,35 s et se ferme à 3,05 s), elle s'effondre en pièces et en symboles
    (3,4 s), une comète jaillit (5,1 s) et file au ciel ; la nuit du chapitre 1 revient (6,8 s), une fenêtre s'allume
    (7,9 s), le levier de la petite machine descend (8,5 s) ; fondu blanc (10,3 s).
    """
    track = Track(11.5)
    track.add(chip(sweep(hz('A4'), hz('A1'), 0.9), 0.9, 'square', s=0.6), 0.6, 0.25)       # extinction
    for k in range(4):                                                          # les yeux se ferment
        track.add(chip(sweep(hz('E4') * 2 ** (-k * 2 / 12), hz('E3') * 2 ** (-k * 2 / 12), 0.15), 0.15, 'square',
                       dc=0.02, s=0.4), 1.3 + k * 0.25, 0.3)
    for at in (2.35, 2.65):                                                     # le dernier cligne
        track.add(click(0.01, 800, 2500), at, 0.5)
        track.add(click(0.01, 1200, 3500), at + 0.13, 0.4)
    track.add(chip(sweep(hz('A3'), hz('A1'), 0.4), 0.4, 'square', dc=0.02, s=0.5), 3.05, 0.35)
    collapse = 3.4
    track.add(mix((boom(1.5, 50), 0, 1.0), (coin_shower(2.0, 60), 0.05, 0.7), (chips_clatter(30, 1.6), 0.1, 0.5)),
              collapse, 1.0)
    comet = 5.1
    rise = 1.8
    whistle = osc(sweep(500, 2200, rise), rise, 'tri') * ramp(rise, 0.3, 1.0)
    track.add(whistle * fade_out(rise, 0.5), comet, 0.12)
    track.add(fire_burst(1.8), comet, 0.6)
    notes = ['E6', 'B5', 'A6', 'E6', 'C#7', 'B6', 'E7']                       # les étoiles filantes du chapitre 1
    for k, note in enumerate(notes):
        track.add(modal(hz(note), 0.9, [1, 2.76, 5.4], [0.5, 0.2, 0.08], [1, 0.3, 0.12]), comet + 1.4 + k * 0.22, 0.25)
    track.add(ambience(3.0, 200, 900, 0.12), 6.6, 1.0)                          # la ville, la nuit
    track.add(modal(hz('A5'), 1.2, [1, 2.0, 3.0], [0.4, 0.2, 0.1], [1, 0.5, 0.3]), 7.9, 0.35)   # la fenêtre s'allume
    for k in range(6):                                                          # ses ampoules
        track.add(chip(hz('E6') * 2 ** ((k % 2) * 4 / 12), 0.06, 'square', dc=0.02, s=0.4), 8.0 + k * 0.17, 0.12)
    track.add(lever_pull(0.25), 8.5, 0.6)
    track.add(rattle(0.9, 20, 26, 2000, 5000), 8.75, 0.35)                     # le tout premier tirage
    gliss = stack(*(osc(sweep(hz(note) / 2, hz(note), 1.4), 1.4, 'tri') for note in ('A4', 'C#5', 'E5')))
    track.add(gliss * ramp(1.4, 0.0, 1.0, 1.5), 8.8, 0.12)
    track.add(bingo_jingle('A5', 'square', duty=0.25, step=0.06, hold=1.2) * 0.8, 9.9, 0.6)
    return reverb(track.buf, wet=0.25, size=1.5, tail=1.0)


# --- Exploration : les rois ---------------------------------------------------
# Chaque roi : le décor (0 à KING_PRELUDE s), puis son entrée (entrance_*, décalée de KING_PRELUDE),
# puis son geste (KING_PRELUDE + 3,4 s environ). Voir KingEntrance.

KING_PRELUDE = 2.0


def king(prelude, entrance, gesture, d=9.0):
    """Assemble le son d'un roi : le décor et le geste (avec un peu d'écho), et son entrée décalée."""
    track = Track(d)
    track.add(entrance, KING_PRELUDE, 1.0)
    extra = Track(d)
    for sound, at, gain in prelude:
        extra.add(sound, at, gain)
    for sound, at, gain in gesture:
        extra.add(sound, KING_PRELUDE + at, gain)
    track.add(reverb(extra.buf, wet=0.25, size=1.4), 0.0, 1.0)
    return track.buf


def thunder(d=1.6):
    """Coup de tonnerre : un claquement, puis le grondement qui roule."""
    return mix((crack_hit(), 0, 1.0), (lp(noise(d), 220) * expdec(d, d * 0.4, attack=0.02), 0.02, 1.2),
               (lp(noise(d), 90) * expdec(d, d * 0.6, attack=0.1), 0.1, 1.0))


def rain_noise(d):
    """La pluie : un souffle aigu."""
    return bp(noise(d), 2500, 8000) * fade_out(d, 0.3) * ramp(d, 0.3, 1.0)


def squeak(f=None):
    """Couinement de rat."""
    f = f or rand(2500, 4200)
    return osc(sweep(f, f * rand(1.1, 1.4), 0.08), 0.08, 'tri') * expdec(0.08, 0.04)


def splash(d=0.8):
    """Une vague qui se brise sur les rochers."""
    return mix((lp(noise(d), 1800) * expdec(d, d * 0.35, attack=0.03), 0, 1.0),
               (bp(noise(d), 3000, 9000) * expdec(d, d * 0.25, attack=0.05), 0.05, 0.4))


def hiss(d=1.2):
    """Vapeur : le sifflement de la lame trempée."""
    return hp(noise(d), 3500) * expdec(d, d * 0.4, attack=0.01)


def rustle(d=0.8):
    """Papier qui se déroule."""
    return svf(noise(d), sweep(1200, 3000, d), q=1.2, mode='bp') * (0.6 + 0.4 * np.abs(np.sin(times(d) * 23))) \
        * fade_out(d, 0.2)


def gulp():
    """Une gorgée."""
    return svf(noise(0.12), sweep(300, 900, 0.12), q=3.0, mode='bp') * expdec(0.12, 0.05, attack=0.01)


def drip():
    """Une goutte qui tombe : « plic »."""
    return osc(sweep(900, 2200, 0.05), 0.05, 'sine') * expdec(0.05, 0.02)


def bell_toll(f, d=2.4):
    """La cloche du port."""
    return modal(f, d, CHURCH_RATIOS, [1.2, 1.0, 0.8, 0.6, 0.5, 0.4, 0.3, 0.25, 0.2])


def footsteps(d, rate, lo=200, hi=900):
    """Des pas pressés."""
    track = Track(d)
    t = 0.0
    while t < d:
        track.add(mix((thump(rand(120, 180), 60, 0.08, 0.03), 0, 0.6), (click(0.01, lo, hi), 0, 0.5)), t, rand(0.5, 1.0))
        t += 1.0 / rate
    return track.buf

def entrance_roi_pique():
    """Roi de Pique (PrairieKings) : huit lames se plantent (0,47 s puis toutes les 0,17 s), le roi sort (1,8 s)."""
    track = Track(4.5)
    track.add(ambience(4.0, 200, 700, 0.15), 0.0, 1.0)
    for k in range(8):
        at = 0.25 + k * 0.17
        track.add(blade_swish(0.2, 3000, 800), at, 0.25)
        track.add(mix((metal_hit(rand(1800, 2600), 0.5), 0, 0.6), (thump(200, 80, 0.12, 0.03), 0, 0.6)), at + 0.22, 0.6)
    track.add(whoosh(0.5, 150, 900, q=0.9), 1.8, 0.5)
    track.add(blade_ring(3100, 1.2), 2.3, 0.7)
    track.add(brass_chord(['D3', 'A3', 'D4'], 1.0, 0.6), 2.3, 0.9)
    return reverb(track.buf, wet=0.25, size=1.3)


def entrance_roi_trefle():
    """Roi de Trèfle (PrairieKings) : pluie de pièces (0 à 1,7 s), le tas éclate (1,9 s), la dernière pièce (2,9 s)."""
    track = Track(4.5)
    track.add(coin_shower(1.7, 60), 0.0, 0.7)
    track.add(mix((impact(0.5, 140, 50), 0, 1.0), (coin_shower(0.8, 30), 0, 0.8)), 1.9, 0.9)
    track.add(brass_chord(['G3', 'B3', 'D4'], 0.8, 0.6), 1.95, 0.8)
    track.add(coin(1975.5, 0.7), 2.9, 0.9)
    return reverb(track.buf, wet=0.22, size=1.2)


def entrance_roi_coeur():
    """Roi de Coeur (PrairieKings) : les murs battent (0,3 s...), le cœur géant s'ouvre (2,0 s)."""
    track = Track(4.5)
    for at in (0.3, 0.95, 1.5):
        track.add(heartbeat([at], f=55, gain=1.0), 0.0, 1.0)
    track.add(drone(['A2', 'E3'], 2.0, 'tri', fin=0.3, fout=0.2), 0.0, 0.12)
    rip = svf(noise(0.5), sweep(400, 2500, 0.5), q=1.5, mode='bp') * expdec(0.5, 0.2)
    track.add(rip, 2.0, 0.7)
    track.add(brass_chord(['A2', 'C3', 'E3', 'A3'], 1.2, 0.6), 2.05, 1.0)
    return reverb(track.buf, wet=0.25, size=1.3)


def entrance_roi_carreau():
    """Roi de Carreau (PrairieKings) : le trésor tremble (0,5 s), le roi en sort (1,7 s), éclat du diamant (2,55 s)."""
    track = Track(4.5)
    track.add(coin_shower(1.2, 25, 1200, 2200), 0.5, 0.4)
    track.add(lp(noise(1.2), 150) * ramp(1.2, 0.2, 1.0), 0.5, 0.6)
    track.add(mix((impact(0.5, 120, 45), 0, 1.0), (whoosh(0.5, 200, 1500), 0, 0.5)), 1.7, 0.8)
    track.add(modal(hz('E7'), 1.2, GLASS_RATIOS, [0.6, 0.3, 0.15, 0.08]), 2.55, 0.5)
    track.add(sparkle(1.0, count=20, lo=5000, hi=10000), 2.55, 0.5)
    track.add(brass_chord(['E3', 'G#3', 'B3'], 0.9, 0.5), 2.55, 0.7)
    return reverb(track.buf, wet=0.25, size=1.3)


def entrance_capitaine_rat():
    """Capitaine Rat (PortKings) : couinements et grattements dans le noir, le capitaine surgit (2,2 s)."""
    track = Track(4.5)
    track.add(ambience(3.8, 100, 400, 0.2), 0.0, 1.0)
    for _ in range(14):
        f = rand(2500, 4200)
        squeak = osc(sweep(f, f * rand(1.1, 1.4), 0.08), 0.08, 'tri') * expdec(0.08, 0.04)
        track.add(squeak, rand(0.2, 2.0), rand(0.15, 0.35))
    scratch = Track(2.0)
    for _ in range(80):
        scratch.add(click(0.004, 3000, 8000), rand(0, 1.9), rand(0.1, 0.4))
    track.add(scratch.buf, 0.3, 0.5)
    track.add(mix((impact(0.5, 110, 40), 0, 1.0), (brass_chord(['C3', 'D#3', 'F#3'], 0.9, 0.6), 0, 1.0)), 2.2, 0.9)
    return reverb(track.buf, wet=0.25, size=1.0)


def entrance_tavernier():
    """Tavernier (PortKings) : brouhaha, les chopes glissent (0,15 s, toutes les 0,4 s), la chope posée (2,55 s)."""
    track = Track(4.5)
    track.add(ambience(3.6, 300, 1400, 0.3), 0.0, 1.0)
    for k in range(4):
        track.add(svf(noise(1.0), 900, q=1.5, mode='bp') * ramp(1.0, 1.0, 0.0) * 0.5, 0.15 + k * 0.4, 0.4)
        track.add(modal(rand(1200, 1600), 0.3, GLASS_RATIOS, [0.12, 0.06, 0.03, 0.02]), 0.2 + k * 0.4, 0.15)
    track.add(mix((impact(0.5, 160, 50), 0, 1.2), (modal(1100, 0.5, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.02]), 0, 0.5),
                  (hp(noise(0.4), 2000) * expdec(0.4, 0.12), 0.02, 0.4)), 2.55, 1.0)
    return reverb(track.buf, wet=0.2, size=1.0)


def entrance_gardien_phare():
    """Gardien du Phare (PortKings) : la mer, le faisceau qui tourne, puis se tourne vers toi : corne de brume (2,2 s)."""
    track = Track(4.8)
    waves = lp(noise(4.2), 500) * (0.6 + 0.4 * np.sin(2 * math.pi * 0.5 * times(4.2)))
    track.add(waves * fade_out(4.2, 0.6), 0.0, 0.5)
    track.add(osc(sweep(300, 900, 0.5), 0.5, 'sine') * ramp(0.5, 0.0, 1.0), 1.8, 0.15)
    horn = svf(stack(osc(hz('A1'), 1.6, 'saw'), osc(hz('A1') * 1.006, 1.6, 'saw')), 500, q=0.8) * adsr(1.6, 0.1, 0.2, 0.8, 0.4)
    track.add(horn, 2.2, 0.6)
    track.add(sparkle(1.0, count=12), 2.3, 0.3)
    return reverb(track.buf, wet=0.3, size=1.8, tail=1.2)


def entrance_capitaine_noir():
    """Capitaine Noir (PortKings) : mer et bois qui grince, coup de canon (1,9 s), il atterrit sur le pont (2,65 s)."""
    track = Track(4.8)
    track.add(lp(noise(3.6), 400) * fade_out(3.6, 0.5), 0.0, 0.4)
    for at in (0.3, 1.1):
        creak = svf(osc(sweep(110, 80, 0.6), 0.6, 'saw'), 700, q=4.0, mode='bp') * expdec(0.6, 0.3, attack=0.1)
        track.add(creak, at, 0.25)
    track.add(reverb(boom(1.6, 45), wet=0.4, size=2.0, tail=1.2), 1.9, 1.0)
    track.add(impact(0.4, 150, 50), 2.65, 0.9)
    track.add(brass_chord(['D3', 'F3', 'A3', 'D4'], 0.9, 0.6), 2.7, 0.8)
    return reverb(track.buf, wet=0.15, size=1.2)


def entrance_baron_or():
    """Baron de l'Or (MinesKings) : les rails qui claquent, le freinage (1,35 à 1,9 s), l'arrêt, l'or qui brille (2,3 s)."""
    track = Track(4.5)
    track.add(rattle(1.35, 8, 18, 900, 2500, wood=True), 0.0, 0.6)
    track.add(lp(noise(1.9), 300) * ramp(1.9, 0.2, 1.0), 0.0, 0.4)
    track.add(screech(0.55, 2800) * ramp(0.55, 1.0, 0.3), 1.35, 0.35)
    track.add(mix((metal_hit(140, 0.8), 0, 0.8), (impact(0.5, 120, 40), 0, 1.0)), 1.9, 0.9)
    track.add(mix((sparkle(1.0, count=20), 0, 1.0), (coin(), 0, 0.6)), 2.3, 0.6)
    return reverb(track.buf, wet=0.25, size=1.4)


def entrance_grand_foreur():
    """Grand Foreur (MinesKings) : le sol vibre (0 à 1,4 s), la foreuse perce le mur (1,4 s) et hurle jusqu'à 2,1 s."""
    track = Track(4.5)
    track.add(lp(noise(1.4), 100) * ramp(1.4, 0.2, 1.0, 1.5), 0.0, 1.0)
    pebbles = Track(1.4)
    for _ in range(25):
        pebbles.add(modal(rand(600, 1400), 0.1, WOOD_RATIOS, [0.03, 0.015, 0.01]), rand(0, 1.3), rand(0.2, 0.6))
    track.add(pebbles.buf, 0.0, 0.5)
    track.add(boom(1.5, 50), 1.4, 1.0)
    drill = svf(osc(vibrato(180, 0.7, rate=30, depth=0.05), 0.7, 'saw'), 1600, q=1.2) * adsr(0.7, 0.02, 0.1, 0.9, 0.15)
    track.add(drill, 1.4, 0.4)
    track.add(brass_chord(['C3', 'G3', 'C4'], 0.9, 0.5), 2.3, 0.7)
    return reverb(track.buf, wet=0.25, size=1.4)


def entrance_maitre_forge():
    """Maître de Forge (MinesKings) : trois coups d'enclume (0,6 / 1,15 / 1,7 s), il lève la lame rouge (2,4 s)."""
    track = Track(4.5)
    track.add(fire_burst(3.6) * fade_out(3.6, 0.5), 0.0, 0.2)
    for at in (0.6, 1.15, 1.7):
        track.add(mix((metal_hit(1250, 1.0), 0, 1.0), (thump(180, 80, 0.15, 0.04), 0, 0.8)), at, 0.8)
        track.add(sparkle(0.4, count=10, lo=4000, hi=9000), at, 0.3)
    track.add(blade_ring(2600, 1.0), 2.4, 0.5)
    track.add(hp(noise(0.8), 3000) * expdec(0.8, 0.3), 2.4, 0.2)
    track.add(brass_chord(['F2', 'C3', 'F3'], 1.0, 0.6), 2.4, 0.8)
    return reverb(track.buf, wet=0.25, size=1.4)


def entrance_coeur_montagne():
    """Coeur de la Montagne (MinesKings) : les rochers roulent (0 à 1,4 s), s'assemblent (2,0 s), le cœur s'allume (2,6 s)."""
    track = Track(4.5)
    track.add(lp(noise(1.6), 140), 0.0, 0.8)
    track.add(rattle(1.4, 9, 5, 200, 500, wood=True), 0.0, 0.6)
    track.add(whoosh(0.6, 150, 700), 1.4, 0.5)
    track.add(impact(0.6, 100, 35), 2.0, 1.0)
    track.add(heartbeat([2.6, 3.2], f=50, gain=0.9), 0.0, 1.0)
    track.add(modal(hz('C6'), 1.5, CHURCH_RATIOS, [0.8, 0.6, 0.5, 0.4, 0.3, 0.2, 0.2, 0.15, 0.1]), 2.6, 0.25)
    return reverb(track.buf, wet=0.3, size=1.6)


def entrance_sirene():
    """Sirène du Bar (CasinoKings) : sous l'eau, les bulles, le chant (0,5 s), les verres qui tintent, elle apparaît (2,0 s)."""
    track = Track(4.6)
    track.add(lp(noise(3.8), 400) * fade_out(3.8, 0.5), 0.0, 0.3)
    track.add(bubbles(3.6, 40), 0.0, 0.4)
    for k, note in enumerate(['E5', 'G5', 'B5', 'A5', 'G5', 'E5', 'D5', 'E5']):
        voice = osc(vibrato(hz(note), 0.45, rate=5.5, depth=0.015, delay=0.1), 0.45, 'sine') * adsr(0.45, 0.08, 0.1, 0.8, 0.12)
        track.add(bp(voice + 0.3 * osc(hz(note) * 2, 0.45), 300, 3000), 0.5 + k * 0.4, 0.35)
    for _ in range(8):
        track.add(modal(rand(1800, 2600), 0.3, GLASS_RATIOS, [0.15, 0.07, 0.04, 0.02]), rand(0.8, 2.2), rand(0.1, 0.3))
    track.add(sparkle(1.0, count=16), 2.0, 0.4)
    return reverb(track.buf, wet=0.35, size=1.8, tail=1.2)


def entrance_jackpot_vivant():
    """Jackpot Vivant (CasinoKings) : les machines s'allument (0,3 s, toutes les 0,13 s), JACKPOT (1,3 s), il marche (2,1 s)."""
    track = Track(4.5)
    for k in range(7):
        track.add(chip(hz('C4') * 2 ** (k * 2 / 12), 0.12, 'square', dc=0.03, s=0.5), 0.3 + k * 0.13, 0.35)
    for k in range(6):
        track.add(cash_register_bell(), 1.3 + k * 0.15, 0.3)
    track.add(bingo_jingle('C5', 'square', hold=0.6), 1.3, 0.6)
    for k in range(4):
        track.add(mix((thump(120, 50, 0.3, 0.08), 0, 1.0), (metal_hit(300, 0.3), 0, 0.3)), 2.25 + k * 0.22, 0.9)
    track.add(brass_chord(['C3', 'E3', 'G3', 'C4'], 0.9, 0.6), 3.0, 0.8)
    return reverb(track.buf, wet=0.2, size=1.2)


def entrance_requin_banquier():
    """Grand Requin Banquier (CasinoKings) : les pièces coulent, l'ombre tourne (0,4 s) sur deux notes graves, il surgit (2,1 s)."""
    track = Track(4.5)
    track.add(lp(noise(3.6), 350) * fade_out(3.6, 0.5), 0.0, 0.3)
    track.add(bubbles(3.4, 20), 0.0, 0.3)
    for _ in range(8):
        track.add(lp(coin(rand(1400, 2000), 0.4), 2500), rand(0.1, 1.8), rand(0.15, 0.3))
    t, gap = 0.4, 0.42
    while t < 2.05:                                                            # deux notes graves qui accélèrent
        for k, note in enumerate(('E2', 'F2')):
            tone = svf(osc(hz(note), 0.22, 'saw'), 600, q=1.0) * adsr(0.22, 0.01, 0.05, 0.7, 0.08)
            track.add(tone, t + k * gap / 2, 0.45)
        t += gap
        gap = max(0.16, gap * 0.82)
    track.add(mix((hp(noise(0.5), 600) * expdec(0.5, 0.15), 0, 0.8), (impact(0.4, 150, 50), 0, 1.0),
                  (click(0.03, 1500, 6000), 0.05, 0.8)), 2.1, 1.0)
    track.add(brass_chord(['E2', 'G2', 'A#2', 'E3'], 1.0, 0.6), 2.15, 0.8)
    return reverb(track.buf, wet=0.25, size=1.4)


def entrance_kraken():
    """Kraken (CasinoKings) : la table se fend (0,5 s), huit bras sortent (0,8 s, tous les 0,18 s), l'œil s'ouvre (2,45 s)."""
    track = Track(4.6)
    track.add(lp(noise(3.8), 300) * fade_out(3.8, 0.5), 0.0, 0.3)
    track.add(mix((crack_hit(), 0, 1.0), (modal(500, 0.4, WOOD_RATIOS, [0.1, 0.05, 0.03]), 0, 0.8),
                  (thump(100, 40, 0.4, 0.15), 0, 0.8)), 0.5, 0.9)
    for k in range(8):
        slither = svf(noise(0.35), sweep(200, 700, 0.35), q=2.0, mode='bp') * expdec(0.35, 0.15, attack=0.03)
        track.add(slither, 0.8 + k * 0.18, 0.5)
        track.add(card_flick(0.06), 1.05 + k * 0.18, 0.3)
    horn = svf(stack(osc(hz('D1'), 1.6, 'saw'), osc(hz('A1'), 1.6, 'saw')), 400, q=0.9) * adsr(1.6, 0.2, 0.2, 0.8, 0.4)
    track.add(horn, 2.45, 0.6)
    track.add(lp(noise(1.5), 90) * ramp(1.5, 1.0, 0.2), 2.45, 0.9)
    return reverb(track.buf, wet=0.3, size=1.8, tail=1.2)


@sound('cutscene/king_roi_pique', -17)
def king_roi_pique():
    """Roi de Pique : l'orage (éclairs à 0,6 et 1,4 s), son entrée ; il fait tourner son épée, les lames se lèvent (4,2 s)."""
    prelude = [(rain_noise(2.0), 0.0, 0.25), (lp(noise(2.0), 300) * fade_out(2.0, 0.3), 0.0, 0.4),
               (thunder(1.4), 0.6, 0.9), (thunder(1.2), 1.4, 0.8)]
    spin = Track(1.0)
    for k in range(6):
        spin.add(blade_swish(0.14, 1200, 4000), k * 0.15, 0.4)
    gesture = [(spin.buf, 3.4, 1.0), (blade_ring(2800, 1.0), 4.2, 0.4), (swell(0.6, 400, 3000) * fade_out(0.6, 0.2), 4.2, 0.4),
               (brass_chord(['D3', 'A3', 'D4', 'F#4'], 0.9, 0.5), 4.3, 0.6)]
    return king(prelude, entrance_roi_pique(), gesture)


@sound('cutscene/king_roi_trefle', -17)
def king_roi_trefle():
    """Roi de Trèfle : le trèfle pousse (feuilles à 0,85 s, toutes les 0,18 s ; éclat 1,65 s) ; la pièce dans la poche, son signe."""
    prelude = [(ambience(2.0, 300, 1500, 0.4), 0.0, 0.6), (osc(sweep(200, 600, 0.7), 0.7, 'sine') * ramp(0.7, 0, 1) * fade_out(0.7, 0.2), 0.2, 0.15)]
    for k, note in enumerate(['G5', 'B5', 'D6', 'G6']):
        prelude.append((pluck(hz(note), 0.5), 0.85 + k * 0.18, 0.35))
    prelude += [(sparkle(0.8, count=14), 1.65, 0.4), (modal(hz('G6'), 1.0, GLASS_RATIOS, [0.5, 0.2, 0.1, 0.05]), 1.65, 0.3)]
    gesture = [(lp(coin(1975.5, 0.4), 1500), 3.9, 0.6), (click(0.02, 300, 1200), 3.92, 0.5)]
    for k in range(2):
        gesture.append((chip(hz('G4') * (1.0 if k == 0 else 1.335), 0.12, 'tri', s=0.4), 4.25 + k * 0.6, 0.3))
    return king(prelude, entrance_roi_trefle(), gesture)


@sound('cutscene/king_roi_coeur', -17)
def king_roi_coeur():
    """Roi de Coeur : le vitrail s'éclaire (0,3 à 1,4 s) ; il boit (3,4 s), une goutte tombe (4,55 s)."""
    prelude = [(drone(['A3', 'C#4', 'E4', 'A4'], 2.0, 'tri', fin=1.0, fout=0.3), 0.0, 0.12),
               (sparkle(0.8, count=16), 1.4, 0.4), (modal(hz('A5'), 1.2, GLASS_RATIOS, [0.6, 0.3, 0.15, 0.08]), 1.4, 0.3)]
    gesture = [(gulp(), 3.6 + k * 0.22, 0.6) for k in range(3)]
    gesture += [(drip(), 4.55, 0.7), (reverb(drip(), wet=0.6, size=1.0), 4.6, 0.3),
                (brass_chord(['A2', 'E3', 'A3'], 0.8, 0.4), 4.7, 0.5)]
    return king(prelude, entrance_roi_coeur(), gesture)


@sound('cutscene/king_roi_carreau', -17)
def king_roi_carreau():
    """Roi de Carreau : la carte se déroule (0,15 s), la croix (1,5 s) ; il pose son écu sur le tas (3,85 s), les pièces glissent."""
    prelude = [(rustle(0.8), 0.15, 0.5), (click(0.02, 1000, 4000), 0.95, 0.4)]
    for k in range(2):
        prelude.append((svf(noise(0.12), 2500, q=1.5, mode='bp') * expdec(0.12, 0.05), 1.5 + k * 0.1, 0.5))
    prelude.append((brass_chord(['E3', 'B3', 'E4'], 0.5, 0.4), 1.55, 0.4))
    gesture = [(mix((impact(0.4, 140, 50), 0, 1.0), (metal_hit(600, 0.4), 0, 0.4)), 3.85, 0.8),
               (coin_shower(1.0, 30, 1500, 2600), 3.9, 0.6)]
    return king(prelude, entrance_roi_carreau(), gesture)


@sound('cutscene/king_capitaine_rat', -17)
def king_capitaine_rat():
    """Capitaine Rat : le quai, l'eau ; le fromage disparaît (1,35 s) ; il ajuste son chapeau, les rats saluent (4,4 s)."""
    prelude = [(lp(noise(2.0), 400) * (0.6 + 0.4 * np.sin(times(2.0) * 3)) * fade_out(2.0, 0.3), 0.0, 0.4),
               (squeak(3200), 0.9, 0.3), (squeak(3600), 1.25, 0.4),
               (mix((whoosh(0.25, 500, 3000), 0, 1.0), (click(0.02, 800, 3000), 0, 0.6)), 1.35, 0.6)]
    gesture = [(whoosh(0.2, 800, 2500, q=1.5), 3.5, 0.3), (whoosh(0.2, 800, 2500, q=1.5), 3.75, 0.25)]
    gesture += [(squeak(), 4.0 + k * 0.06, 0.25) for k in range(6)]
    gesture.append((power_up(['C5', 'E5', 'G5', 'C6'], step=0.06, last=0.3) * 0.6, 4.4, 0.4))
    return king(prelude, entrance_capitaine_rat(), gesture)


@sound('cutscene/king_tavernier', -17)
def king_tavernier():
    """Tavernier : les marins chantent et trinquent (0,6 et 1,35 s) ; il essuie le comptoir, te sert une chope (5,0 s)."""
    prelude = [(ambience(2.0, 300, 1400, 0.3), 0.0, 0.8)]
    for k, note in enumerate(['D4', 'D4', 'F#4', 'A4', 'G4', 'F#4', 'E4', 'D4']):
        voice = osc(vibrato(hz(note), 0.24, rate=6, depth=0.02), 0.24, 'saw') * adsr(0.24, 0.02, 0.05, 0.7, 0.05)
        prelude.append((bp(voice, 300, 2000), 0.1 + k * 0.22, 0.25))
    for at in (0.6, 1.35):
        prelude.append((modal(rand(1100, 1400), 0.5, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), at, 0.4))
        prelude.append((modal(rand(1500, 1800), 0.4, GLASS_RATIOS, [0.25, 0.12, 0.06, 0.03]), at + 0.02, 0.3))
    gesture = [(svf(noise(0.25), 1800, q=2.0, mode='bp') * expdec(0.25, 0.1, attack=0.05), 3.45 + k * 0.29, 0.3)
               for k in range(3)]
    gesture += [(svf(noise(0.5), 900, q=1.5, mode='bp') * ramp(0.5, 0.3, 1.0), 4.5, 0.4),
                (mix((impact(0.4, 160, 60), 0, 1.0), (modal(1000, 0.4, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.02]), 0, 0.5)), 5.0, 0.8)]
    return king(prelude, entrance_tavernier(), gesture)


@sound('cutscene/king_gardien_phare', -17)
def king_gardien_phare():
    """Gardien du Phare : l'orage, les vagues sur les rochers (0,3 / 0,95 / 1,6 s), l'éclair (1,25 s) ; la lanterne t'éblouit (4,15 s)."""
    prelude = [(rain_noise(2.0), 0.0, 0.25), (lp(noise(2.0), 500) * fade_out(2.0, 0.3), 0.0, 0.4), (thunder(1.3), 1.25, 0.8)]
    prelude += [(splash(0.9), at, 0.8) for at in (0.3, 0.95, 1.6)]
    gesture = [(osc(sweep(400, 1600, 0.75), 0.75, 'sine') * ramp(0.75, 0.0, 1.0, 2.0), 3.4, 0.15),
               (sparkle(0.9, count=20, lo=4000, hi=10000), 4.15, 0.5),
               (drone(['A4', 'E5', 'A5'], 1.0, 'tri', fin=0.05, fout=0.6), 4.15, 0.1)]
    return king(prelude, entrance_gardien_phare(), gesture)


@sound('cutscene/king_capitaine_noir', -17)
def king_capitaine_noir():
    """Capitaine Noir : la cloche du port dans le brouillard (0,35 et 1,15 s) ; il plante son sabre (3,8 s), le pavillon claque."""
    prelude = [(lp(noise(2.0), 400) * fade_out(2.0, 0.3), 0.0, 0.35), (bell_toll(hz('D4')), 0.35, 0.5),
               (bell_toll(hz('D4')), 1.15, 0.45)]
    flaps = Track(1.6)
    for k in range(7):
        flaps.add(svf(noise(0.08), 900, q=1.0, mode='bp') * expdec(0.08, 0.03), k * 0.2, 0.6 - k * 0.06)
    gesture = [(blade_swish(0.3, 800, 3000), 3.5, 0.3),
               (mix((metal_hit(900, 0.6), 0, 0.6), (thump(160, 60, 0.2, 0.05), 0, 0.9), (click(0.02, 400, 2000), 0, 0.6)), 3.8, 0.8),
               (flaps.buf, 3.85, 0.6), (brass_chord(['D3', 'F3', 'A3'], 0.7, 0.4), 3.9, 0.5)]
    return king(prelude, entrance_capitaine_noir(), gesture)


@sound('cutscene/king_baron_or', -17)
def king_baron_or():
    """Baron de l'Or : les rails brillent dans le noir (0,2 et 1,0 s) ; il mord une pépite (3,9 s), elle tinte."""
    prelude = [(lp(noise(2.0), 200) * fade_out(2.0, 0.3), 0.0, 0.3)]
    for at in (0.2, 1.0):
        prelude.append((modal(hz('E7'), 0.7, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), at + 0.5, 0.3))
        prelude.append((osc(sweep(2000, 5000, 0.7), 0.7, 'sine') * ramp(0.7, 0.0, 1.0) * fade_out(0.7, 0.2), at, 0.06))
    gesture = [(mix((click(0.015, 600, 2000), 0, 1.0), (thump(300, 150, 0.06, 0.02), 0, 0.5)), 3.9, 0.7),
               (coin(2400, 0.5), 3.95, 0.5), (sparkle(0.5, count=8), 3.95, 0.3)]
    return king(prelude, entrance_baron_or(), gesture)


@sound('cutscene/king_grand_foreur', -17)
def king_grand_foreur():
    """Grand Foreur : les lanternes tremblent au plafond (0 à 2 s) ; il retire ses lunettes (3,9 s) et crache (4,4 s)."""
    chains = Track(2.0)
    for _ in range(30):
        chains.add(click(0.008, 2500, 7000), rand(0, 1.9), rand(0.2, 0.6))
    prelude = [(lp(noise(2.0), 100) * ramp(2.0, 0.2, 1.0, 1.5), 0.0, 0.8), (chains.buf, 0.0, 0.5)]
    gesture = [(click(0.03, 300, 1500), 3.9, 0.5), (modal(900, 0.2, WOOD_RATIOS, [0.04, 0.02, 0.01]), 4.6, 0.4),
               (mix((svf(noise(0.5), 700, q=0.8) * expdec(0.5, 0.15, attack=0.02), 0, 1.0),
                    (thump(220, 120, 0.12, 0.04), 0, 0.4)), 4.4, 0.8)]
    return king(prelude, entrance_grand_foreur(), gesture)


@sound('cutscene/king_maitre_forge', -17)
def king_maitre_forge():
    """Maître de Forge : le soufflet attise les braises (0,3 / 0,9 / 1,5 s) ; il trempe la lame (3,85 s) dans un nuage de vapeur."""
    prelude = [(fire_burst(2.0) * fade_out(2.0, 0.3), 0.0, 0.25)]
    for at in (0.3, 0.9, 1.5):
        prelude.append((whoosh(0.45, 150, 900, q=0.8), at, 0.6))
        prelude.append((sparkle(0.4, count=8, lo=2000, hi=6000), at + 0.2, 0.25))
    gesture = [(blade_swish(0.4, 2000, 600), 3.45, 0.3), (hiss(1.4), 3.85, 0.7),
               (svf(noise(0.6), 1200, q=0.8) * expdec(0.6, 0.2), 3.85, 0.4)]
    return king(prelude, entrance_maitre_forge(), gesture)


@sound('cutscene/king_coeur_montagne', -17)
def king_coeur_montagne():
    """Coeur de la Montagne : les mineurs lâchent leurs pioches (0,5 s) et fuient ; trois battements (3,6 / 4,15 / 4,7 s), les murs se fendent."""
    prelude = [(lp(noise(2.0), 120) * ramp(2.0, 0.3, 1.0), 0.0, 0.6)]
    prelude += [(metal_hit(rand(700, 1100), 0.4), 0.5 + k * 0.05, 0.4) for k in range(3)]
    prelude += [(chip(sweep(hz('A4'), hz('E4'), 0.25), 0.25, 'tri', s=0.4), 0.7 + k * 0.1, 0.15) for k in range(3)]
    prelude.append((footsteps(1.2, 9), 0.75, 0.6))
    gesture = [(heartbeat([3.6, 4.15, 4.7], f=48, gain=1.2), 0.0, 1.0)]
    gesture += [(mix((crack_hit(), 0, 1.0), (modal(rand(300, 600), 0.3, WOOD_RATIOS, [0.08, 0.04, 0.02]), 0, 0.6)), at + 0.05, 0.6)
                for at in (3.6, 4.15, 4.7)]
    return king(prelude, entrance_coeur_montagne(), gesture)


@sound('cutscene/king_sirene', -17)
def king_sirene():
    """Sirène du Bar : un verre glisse seul sur le comptoir (0,1 à 1,4 s) ; elle souffle une bulle (3,4 s), une carte dedans."""
    prelude = [(lp(noise(2.0), 400) * fade_out(2.0, 0.3), 0.0, 0.3), (bubbles(2.0, 14), 0.0, 0.3),
               (svf(noise(1.3), sweep(1500, 800, 1.3), q=2.0, mode='bp') * ramp(1.3, 1.0, 0.2), 0.1, 0.4),
               (modal(2200, 0.4, GLASS_RATIOS, [0.15, 0.07, 0.04, 0.02]), 1.4, 0.4)]
    blow = osc(sweep(250, 700, 0.9), 0.9, 'sine') * (0.6 + 0.4 * np.sin(times(0.9) * 60)) * ramp(0.9, 0.2, 1.0)
    gesture = [(lp(blow, 1200), 3.4, 0.3), (bubbles(0.9, 10, 500, 1200), 3.4, 0.4),
               (card_flick(0.06), 4.3, 0.3), (sparkle(0.8, count=12), 4.3, 0.35)]
    return king(prelude, entrance_sirene(), gesture)


@sound('cutscene/king_jackpot_vivant', -17)
def king_jackpot_vivant():
    """Jackpot Vivant : un fantôme tire le levier (0,55 s), rien (1,1 s) ; la machine te crache des pièces (3,6 s), en garde (4,3 s)."""
    ghost = drone(['E4', 'B4'], 2.0, 'sine', fin=0.3, fout=0.5) * (0.7 + 0.3 * np.sin(times(2.0) * 9))
    prelude = [(lp(noise(2.0), 400) * fade_out(2.0, 0.3), 0.0, 0.3), (ghost, 0.0, 0.08),
               (lever_pull(0.25), 0.3, 0.5), (rattle(0.5, 20, 8, 2000, 5000), 0.6, 0.3)]
    prelude += [(chip(hz(note), 0.25, 'square', s=0.4), 1.1 + k * 0.25, 0.2) for k, note in enumerate(['E4', 'D#4', 'D4'])]
    gesture = [(coin_shower(1.0, 45, 1500, 3000), 3.6, 0.7), (impact(0.3, 200, 80), 3.6, 0.5),
               (mix((impact(0.4, 120, 45), 0, 1.0), (metal_hit(300, 0.4), 0, 0.4)), 4.3, 0.8),
               (brass_chord(['C3', 'E3', 'G3'], 0.6, 0.5), 4.35, 0.5)]
    return king(prelude, entrance_jackpot_vivant(), gesture)


@sound('cutscene/king_requin_banquier', -17)
def king_requin_banquier():
    """Grand Requin Banquier : la banque engloutie, le coffre ouvert ; il tamponne REFUSÉ (3,9 s) et sourit (4,5 s)."""
    creak = svf(osc(sweep(140, 90, 1.2), 1.2, 'saw'), 600, q=4.0, mode='bp') * expdec(1.2, 0.5, attack=0.2)
    prelude = [(lp(noise(2.0), 350) * fade_out(2.0, 0.3), 0.0, 0.35), (bubbles(2.0, 10), 0.0, 0.3), (creak, 0.3, 0.2),
               (sparkle(1.4, count=12, lo=3000, hi=7000), 0.5, 0.25)]
    gesture = [(whoosh(0.3, 300, 1200), 3.6, 0.3),
               (mix((thump(180, 70, 0.2, 0.05), 0, 1.0), (click(0.02, 500, 2500), 0, 0.6)), 3.9, 0.9),
               (sparkle(0.6, count=10, lo=6000, hi=11000), 4.5, 0.4),
               (svf(stack(osc(hz('E2'), 0.6, 'saw'), osc(hz('B2'), 0.6, 'saw')), 500, q=0.8) * adsr(0.6, 0.05, 0.1, 0.7, 0.2), 4.5, 0.3)]
    return king(prelude, entrance_requin_banquier(), gesture)


@sound('cutscene/king_kraken', -17)
def king_kraken():
    """Kraken : l'eau devient noire, des cartes coulent ; les bras battent les cartes (3,4 s), l'éventail (4,3 s), le Joker (4,85 s)."""
    prelude = [(drone(['D2', 'A2'], 2.0, 'saw', fin=1.2, fout=0.3), 0.0, 0.08), (lp(noise(2.0), 300) * fade_out(2.0, 0.3), 0.0, 0.3)]
    prelude += [(card_flick(0.06) * 0.6, rand(0.1, 1.8), 0.2) for _ in range(6)]
    riffle = Track(1.0)
    for k in range(18):
        riffle.add(card_flick(0.04), k * 0.05, rand(0.3, 0.6))
    gesture = [(riffle.buf, 3.4, 0.6), (whoosh(0.4, 600, 3000, q=1.0), 4.3, 0.4)]
    gesture += [(card_flick(0.05), 4.35 + k * 0.05, 0.4) for k in range(8)]
    gesture += [(bingo_jingle('D5', 'square', hold=0.5) * 0.6, 4.85, 0.4), (sparkle(0.8, count=14), 4.85, 0.4)]
    return king(prelude, entrance_kraken(), gesture)


# --- Jeu bonus ---------------------------------------------------------------
# Les instants reprennent BonusGameCutscene : « JEU BONUS ! » à 0 s, les lumières s'éteignent (LIGHTS_OUT 1,0 à
# 2,0 s, une rangée d'ampoules par 0,33 s), les portes de la machine s'ouvrent (DOORS 2,0 à 3,0 s) sur la grille.

@sound('cutscene/bonus_game', -17)
def cutscene_bonus_game():
    """Ouverture du Jeu bonus : fanfare, les lumières du casino s'éteignent une à une, la machine s'ouvre."""
    track = Track(3.6)
    track.add(bingo_jingle('C5', 'square', hold=0.7), 0.0, 1.0)
    track.add(brass_chord(['C4', 'E4', 'G4', 'C5'], 0.9, 0.6), 0.25, 0.8)
    for k, at in enumerate((1.0, 1.33, 1.66)):                                  # clac : une rangée d'ampoules s'éteint
        track.add(mix((thump(160 - 30 * k, 50, 0.25, 0.06), 0, 1.0), (click(0.008, 800, 4000), 0, 0.8)), at, 0.8)
        track.add(osc(sweep(220 - 40 * k, 60, 0.3), 0.3, 'saw') * expdec(0.3, 0.1) * 0.15, at, 1.0)
    track.add(lp(noise(1.0), 400) * ramp(1.0, 0.0, 1.0) * fade_out(1.0, 0.2), 2.0, 0.4)   # les portes glissent
    track.add(rattle(1.0, 30, 12, 1500, 4000), 2.0, 0.35)
    track.add(whoosh(1.0, 300, 2400), 2.0, 0.4)
    track.add(mix((metal_hit(520, 0.6), 0, 0.6), (impact(0.4, 120, 50), 0, 0.8)), 3.0, 0.7)
    track.add(sparkle(0.8, count=16), 3.0, 0.5)
    return reverb(track.buf, wet=0.2, size=1.2, tail=0.6)


@sound('cutscene/bonus_game_music', -22, fmt='wav', loop=True)
def cutscene_bonus_game_music():
    """
    Musique du Jeu bonus, en boucle tant que la grille tourne : casino rétro à 132 battements par minute,
    basse qui saute, accords sur les contretemps, petite mélodie en carré (Do, La mineur, Ré mineur, Sol, deux fois).
    Ce qui déborde de la boucle est replié sur son début : elle se raccorde sans blanc.
    """
    beat = 60 / 132
    bars = 8
    d = bars * 4 * beat
    N = n(d)
    track = Track(d + 2.0)
    chords = [('C3', ['C4', 'E4', 'G4']), ('A2', ['A3', 'C4', 'E4']), ('D3', ['D4', 'F4', 'A4']),
              ('G2', ['G3', 'B3', 'D4', 'F4'])] * 2
    melody = [['E5', 'G5', 'C6', 'G5'], ['E5', 'A5', 'C6', 'A5'], ['F5', 'A5', 'D6', 'A5'], ['G5', 'B5', 'D6', 'F6'],
              ['G5', 'E5', 'C6', 'E6'], ['C6', 'A5', 'E5', 'A5'], ['D6', 'F5', 'A5', 'F6'], ['D6', 'B5', 'G5', 'B5']]
    for bar, (root, chord) in enumerate(chords):
        t0 = bar * 4 * beat
        for k in range(4):
            note = root if k % 2 == 0 else transpose(hz(root), 7)
            f = hz(note) if isinstance(note, str) else note
            track.add(pluck(f, beat * 0.9, tau=0.25, bright=1800), t0 + k * beat, 0.55)   # basse
            track.add(thump(110, 45, 0.2, 0.05), t0 + k * beat, 0.5 if k % 2 == 0 else 0.3)  # grosse caisse
            stab = stack(*(chip(hz(c), beat * 0.35, 'square', 0.5, dc=0.04, s=0.4, r=0.03) for c in chord))
            track.add(lp(stab, 3000), t0 + (k + 0.5) * beat, 0.16)                       # accords sur le contretemps
            hat = bp(noise(0.04), 6000, 12000) * expdec(0.04, 0.01)
            track.add(hat, t0 + (k + 0.5) * beat, 0.12)
            track.add(hat, t0 + k * beat, 0.06)
        for k, note in enumerate(melody[bar]):
            track.add(chip(hz(note), beat * 0.8, 'square', 0.25, dc=0.05, s=0.5, r=0.08, vib=0.004),
                      t0 + k * beat, 0.22)
        if bar % 2 == 1:
            track.add(sparkle(0.5, count=6), t0 + 3.5 * beat, 0.12)
    buf = pad(track.buf, d + 2.0)
    loop = buf[:N].copy()
    loop[:len(buf) - N] += buf[N:]
    return loop


@sound('cutscene/bonus_game_freeze', -20)
def cutscene_bonus_game_freeze():
    """Des symboles alignés se figent en doré : carillon qui monte et scintillement."""
    track = Track(0.9)
    for k, note in enumerate(['G5', 'C6', 'E6', 'G6']):
        track.add(modal(hz(note), 0.6, GLASS_RATIOS, [0.25, 0.1, 0.05, 0.02], [1, 0.3, 0.12, 0.05]), k * 0.05, 0.4)
    track.add(sparkle(0.7, count=12), 0.05, 0.5)
    return track.buf


@sound('cutscene/bonus_game_total', -16)
def cutscene_bonus_game_total():
    """Les gains du Jeu bonus s'affichent : caisse enregistreuse, fanfare et pluie de pièces."""
    track = Track(2.2)
    track.add(cash_register_bell(), 0.0, 0.8)
    track.add(bingo_jingle('G5', 'square', hold=0.9), 0.1, 1.0)
    track.add(brass_chord(['C4', 'G4', 'C5', 'E5'], 1.2, 0.7), 0.35, 0.8)
    track.add(coin_shower(1.6, 18), 0.3, 0.5)
    return reverb(track.buf, wet=0.2, size=1.0, tail=0.6)


@sound('shop/card_upgrade', -17)
def shop_card_upgrade():
    """Fusion à la Table du croupier : trois cartes glissées l'une sur l'autre, puis la carte « + » brille."""
    track = Track(2.0)
    for k in range(3):
        track.add(card_flick(0.07), k * 0.12, 0.7)
        track.add(card_slap(0.14), k * 0.12 + 0.05, 0.5)
    track.add(power_up(['C5', 'E5', 'G5', 'C6', 'E6', 'G6'], step=0.05, last=0.35), 0.42, 0.7)
    track.add(cash_register_bell(), 0.75, 0.6)
    track.add(sparkle(1.0), 0.75, 0.5)
    return reverb(track.buf, wet=0.22, size=0.9, tail=0.5)



@sound('ui/achievement', -17)
def ui_achievement():
    """Un succès atteint : petite fanfare de casino, clochette de caisse et quelques pièces."""
    track = Track(1.6)
    track.add(power_up(['G5', 'C6', 'E6', 'G6'], step=0.06, last=0.3), 0.0, 0.6)
    track.add(brass_chord(['C4', 'G4', 'C5', 'E5'], 0.8, 0.6), 0.22, 0.6)
    track.add(cash_register_bell(), 0.3, 0.5)
    track.add(coin_shower(0.9, 8), 0.35, 0.35)
    track.add(sparkle(0.8, count=8), 0.25, 0.35)
    return reverb(track.buf, wet=0.2, size=0.9, tail=0.5)


# --- Fête foraine et Désert de Braise (2026-10-24) : les temps suivent FairKings et DesertKings. ---

def calliope(notes, step=0.16, d=None):
    """Un orgue de barbarie : des notes carrées un peu tremblées."""
    d = d or step * len(notes) + 0.4
    track = Track(d)
    for k, note in enumerate(notes):
        f = hz(note)
        track.add(chip(vibrato(f, step * 1.2, 6.0, 0.008), step * 1.2, 'square', 0.35), k * step, 0.35)
        track.add(chip(f * 2, step * 1.2, 'tri'), k * step, 0.15)
    return track.buf


def whip_crack():
    """Un coup de fouet : le sifflement, puis le claquement."""
    return mix((whoosh(0.18, 600, 4000, q=1.5), 0, 0.5), (crack(0.05, 0.004, 1500, 10000), 0.16, 1.2))


@sound('cutscene/king_monsieur_loyal', -17)
def king_monsieur_loyal():
    """Monsieur Loyal : le projecteur (1,3 s), le fouet (1,1 s), les bannières (1,5 s), il lève son chapeau (3,6 / 4,5 s)."""
    prelude = [(ambience(2.0, 300, 1400, 0.3), 0.0, 0.5), (calliope(['C5', 'E5', 'G5', 'E5', 'C5', 'G4'], 0.15), 0.3, 0.4)]
    entrance = Track(5.0)
    entrance.add(whip_crack(), 1.1, 0.8)
    entrance.add(mix((thump(120, 50, 0.3), 0, 1.0), (hp(noise(0.4), 4000) * expdec(0.4, 0.1), 0, 0.3)), 1.3, 0.8)
    entrance.add(whoosh(0.6, 300, 1500, q=0.8), 1.5, 0.5)
    entrance.add(brass_chord(['C3', 'G3', 'C4', 'E4'], 1.2, 0.6), 1.5, 0.7)
    gesture = [(whoosh(0.3, 400, 2000), 3.6, 0.4), (brass_chord(['F3', 'C4', 'F4', 'A4'], 0.5, 0.5), 3.6, 0.5),
               (brass_chord(['C3', 'G3', 'C4', 'E4', 'G4'], 1.4, 0.6), 4.5, 0.8), (sparkle(1.0), 4.5, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_conducteur', -17)
def king_conducteur():
    """Le Conducteur : la porte du wagon (0,35 s), le train s'arrête (1,4 s), il tire la cloche (3,6 s)."""
    prelude = [(rattle(2.0, 6, 9, 300, 1500, wood=True), 0.0, 0.5), (lp(noise(2.0), 250) * ramp(2.0, 0.3, 1.0), 0.0, 0.5)]
    entrance = Track(5.0)
    entrance.add(rattle(1.4, 9, 3, 300, 1500, wood=True), 0.0, 0.5)
    entrance.add(mix((wood_break(), 0, 0.5), (thump(160, 70, 0.2), 0, 0.8)), 0.35, 0.7)
    entrance.add(screech(1.0, 3200), 0.6, 0.35)
    entrance.add(hiss(1.2), 1.4, 0.4)
    entrance.add(impact(0.5, 120, 40), 1.4, 0.8)
    gesture = [(bell_toll(hz('A5'), 1.6), 3.6, 0.6), (bell_toll(hz('E6'), 1.4), 3.85, 0.4),
               (brass_chord(['A2', 'E3', 'A3', 'C#4'], 1.2, 0.5), 3.7, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_roue_fortune', -17)
def king_roue_fortune():
    """La Roue de la Fortune : les ampoules s'allument (0,2 à 1,8 s), l'œil s'ouvre (0,4 s), elle roule (1,6 à 2,8 s)."""
    prelude = [(ambience(2.0, 400, 1600, 0.3), 0.0, 0.5), (calliope(['G4', 'C5', 'E5', 'G5'], 0.2), 0.8, 0.3)]
    entrance = Track(5.0)
    for k in range(12):
        entrance.add(chip(hz('C6') * 2 ** (k / 12), 0.08, 'square', 0.25), 0.2 + k * 0.133, 0.25)
    entrance.add(osc(sweep(150, 600, 0.6), 0.6, 'sine') * expdec(0.6, 0.3), 0.4, 0.5)
    entrance.add(rattle(1.2, 22, 6, 1800, 4200), 1.6, 0.7)
    entrance.add(thump(180, 60, 0.3), 2.8, 0.8)
    entrance.add(bingo_jingle('G5'), 2.9, 0.5)
    gesture = [(rattle(1.0, 18, 4, 2000, 4500), 3.4, 0.5), (cash_register_bell(), 4.4, 0.6), (coin_shower(1.0, 10), 4.5, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_roi_forain', -17)
def king_roi_forain():
    """Le Roi forain : il sort de sa boîte (0,5 s), son maillet frappe (3,5 s), la cloche du « tester » sonne (3,85 s)."""
    prelude = [(calliope(['E5', 'D5', 'C5', 'D5', 'E5', 'E5', 'E5'], 0.18), 0.4, 0.4), (ambience(2.0, 300, 1200, 0.3), 0.0, 0.4)]
    entrance = Track(5.0)
    entrance.add(osc(sweep(300, 1400, 0.3), 0.3, 'square', 0.3) * expdec(0.3, 0.15), 0.4, 0.4)
    entrance.add(mix((thump(200, 80, 0.15), 0, 0.8), (pluck(hz('C4'), 0.6), 0.02, 0.6)), 0.5, 0.8)
    for k in range(5):
        entrance.add(osc(sweep(400 - k * 50, 200, 0.12), 0.12, 'tri') * expdec(0.12, 0.05), 0.6 + k * 0.12, 0.3)
    entrance.add(brass_chord(['C3', 'G3', 'C4', 'E4'], 1.0, 0.5), 1.2, 0.6)
    gesture = [(whoosh(0.3, 200, 1200), 3.2, 0.5), (impact(0.4, 160, 50), 3.5, 1.0), (wood_break(), 3.5, 0.4),
               (osc(sweep(300, 1500, 0.35), 0.35, 'sine') * expdec(0.35, 0.2), 3.5, 0.3),
               (bell_toll(hz('C6'), 1.6), 3.85, 0.7), (sparkle(0.9), 3.9, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


def desert_wind(d):
    """Le vent chaud du désert : un souffle sablé qui monte et descend."""
    return bp(noise(d), 300, 1800) * (0.6 + 0.4 * osc(0.5, d, 'sine')) * fade_out(d, 0.4)


@sound('cutscene/king_emir', -17)
def king_emir():
    """L'Émir : les serviteurs s'agenouillent (0,2 s), il appose son sceau (4,5 s)."""
    prelude = [(desert_wind(2.0), 0.0, 0.5), (drone(['D3', 'A3'], 2.0, 'tri'), 0.0, 0.25)]
    entrance = Track(5.0)
    entrance.add(rustle(0.8), 0.2, 0.6)
    entrance.add(thump(140, 60, 0.15), 0.3, 0.5)
    entrance.add(coin_shower(1.0, 8), 0.8, 0.3)
    entrance.add(brass_chord(['D3', 'A3', 'D4', 'F4'], 1.4, 0.5), 1.0, 0.6)
    gesture = [(whoosh(0.4, 300, 1200), 4.1, 0.4), (impact(0.4, 130, 45), 4.5, 1.0), (metal_hit(1400, 0.6), 4.5, 0.4),
               (brass_chord(['D3', 'A3', 'D4', 'F4', 'A4'], 1.0, 0.5), 4.55, 0.6)]
    return king(prelude, reverb(entrance.buf, wet=0.25, size=1.4), gesture)


@sound('cutscene/king_pharaon', -17)
def king_pharaon():
    """Le Pharaon : le couvercle du sarcophage glisse (0,2 s) puis tombe (0,9 s), il se lève (1,3 s), ses yeux s'allument (3,6 s)."""
    prelude = [(desert_wind(2.0), 0.0, 0.3), (drone(['A2', 'E3'], 2.0, 'sine'), 0.0, 0.4)]
    entrance = Track(5.0)
    entrance.add(lp(noise(0.7), 600) * ramp(0.7, 0.3, 1.0), 0.2, 0.6)
    entrance.add(impact(0.6, 110, 35), 0.9, 1.0)
    entrance.add(lp(noise(1.0), 400) * expdec(1.0, 0.4), 0.95, 0.4)
    entrance.add(rustle(1.0), 1.3, 0.4)
    entrance.add(drone(['A2', 'C3', 'E3'], 2.0, 'tri', 0.4, 0.8), 1.3, 0.3)
    gesture = [(osc(sweep(800, 2400, 0.4), 0.4, 'sine') * expdec(0.4, 0.2), 3.6, 0.4), (swell(0.6, 300, 2000) * fade_out(0.6, 0.2), 3.6, 0.3),
               (brass_chord(['A2', 'E3', 'A3', 'C4'], 1.2, 0.5), 3.7, 0.6)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.6), gesture)


@sound('cutscene/king_djinn', -17)
def king_djinn():
    """Le Djinn : la lampe est frottée (0,8 s), il en jaillit (0,8 s de son entrée), le mirage tremble (3,6 s)."""
    prelude = [(desert_wind(2.0), 0.0, 0.3)]
    for k in range(4):
        prelude.append((hp(noise(0.2), 2000) * expdec(0.2, 0.08, attack=0.05), 0.8 + k * 0.25, 0.3))
    entrance = Track(5.0)
    entrance.add(whoosh(1.2, 200, 3000, q=0.8), 0.6, 0.7)
    entrance.add(sparkle(1.4, count=20), 0.8, 0.5)
    entrance.add(power_up(['D5', 'F5', 'A5', 'C6', 'D6'], step=0.08, wave='tri', last=0.4), 0.8, 0.4)
    entrance.add(drone(['D3', 'G#3', 'D4'], 2.4, 'sine', 0.4, 1.0), 1.6, 0.35)
    gesture = [(osc(vibrato(hz('D5'), 1.4, 7.0, 0.04), 1.4, 'sine') * fade_out(1.4, 0.6), 3.6, 0.3),
               (swell(1.0, 400, 2400) * fade_out(1.0, 0.4), 3.6, 0.3), (sparkle(1.0), 3.8, 0.3)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.6), gesture)


def roar(d=1.6, f=90):
    """Le rugissement du dragon : un grondement bourdonnant et râpeux."""
    body = drive(osc(sweep(f * 1.6, f * 0.8, d), d, 'saw') * expdec(d, d * 0.5, attack=0.12), 3.0)
    grit = bp(noise(d), 200, 1400) * expdec(d, d * 0.5, attack=0.1)
    return mix((lp(body, 900), 0, 0.8), (grit, 0, 0.7), (boom(d, 45), 0, 0.5))


def wing_flap():
    """Un battement d'ailes : un grand souffle sourd."""
    return lp(noise(0.5), 500) * expdec(0.5, 0.15, attack=0.08)


@sound('cutscene/king_dragon', -17, trim=False)
def king_dragon():
    """Ignar : son œil s'ouvre (0,3 s), il sort de la lave (0,9 s), ses ailes (2 s), l'or tombe (2,3 s), il rugit (3,6 s)."""
    prelude = [(lp(noise(2.0), 180) * ramp(2.0, 0.4, 1.0), 0.0, 0.7), (bubbles(2.0, 10, 80, 300), 0.0, 0.4)]
    entrance = Track(7.0)
    entrance.add(osc(sweep(60, 180, 0.4), 0.4, 'saw') * expdec(0.4, 0.2) * 0.3, 0.3, 0.5)
    entrance.add(fire_burst(1.4), 0.9, 0.7)
    entrance.add(boom(1.6, 50), 0.9, 0.8)
    entrance.add(bubbles(1.2, 14, 80, 400), 0.9, 0.5)
    entrance.add(wing_flap(), 2.0, 0.9)
    entrance.add(wing_flap(), 2.45, 0.7)
    entrance.add(coin_shower(1.2, 14), 2.3, 0.5)
    gesture = [(roar(1.8), 3.6, 1.0), (fire_burst(1.2), 3.9, 0.5), (brass_chord(['C2', 'G2', 'C3', 'D#3'], 1.6, 0.6), 3.7, 0.6)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.6), gesture, d=9.0)


def steam_whistle(d=1.4):
    """Le sifflet d'une locomotive : trois notes qui glissent un peu, un souffle de vapeur dessous."""
    tone = sum(osc(sweep(f * 0.96, f, 0.2), d, 'sine') for f in (hz('A5'), hz('C#6'), hz('E6')))
    return tone * expdec(d, d * 0.6, attack=0.05) * 0.4 + hp(noise(d), 3000) * expdec(d, d * 0.4) * 0.3


def gunshot():
    """Un coup de revolver : le claquement sec et sa détonation sourde."""
    return mix((crack(0.04, 0.003, 1200, 10000), 0, 1.3), (thump(180, 50, 0.25), 0, 0.9))


@sound('cutscene/king_locomotive', -17)
def king_locomotive():
    """La Locomotive : le phare approche, elle entre (0 à 1 s), freine (1 s), siffle (3,4 s)."""
    prelude = [(rattle(2.0, 4, 10, 300, 1500), 0.0, 0.5), (lp(noise(2.0), 220) * ramp(2.0, 0.2, 1.0), 0.0, 0.6)]
    entrance = Track(5.0)
    entrance.add(rattle(1.0, 12, 6, 300, 1500), 0.0, 0.6)
    entrance.add(screech(0.9, 3000), 0.9, 0.4)
    entrance.add(impact(0.5, 120, 40), 1.0, 0.8)
    entrance.add(hiss(1.0), 1.2, 0.4)
    gesture = [(steam_whistle(1.6), 3.4, 0.8), (hiss(1.4), 3.4, 0.5), (brass_chord(['A2', 'E3', 'A3', 'C#4'], 1.2, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_grand_chef', -17)
def king_grand_chef():
    """Le Grand Chef : le wagon-restaurant, il surgit (0,5 s), il soulève sa cloche (3,4 s)."""
    prelude = [(ambience(2.0, 300, 1400, 0.3), 0.0, 0.5), (rattle(2.0, 5, 5, 300, 1200), 0.0, 0.3)]
    entrance = Track(5.0)
    entrance.add(whoosh(0.4, 300, 2000), 0.3, 0.5)
    entrance.add(thump(160, 60, 0.3), 0.5, 0.8)
    entrance.add(fire_burst(0.8), 0.6, 0.3)
    gesture = [(cash_register_bell(), 3.4, 0.6), (sparkle(1.0), 3.45, 0.5),
               (brass_chord(['F3', 'A3', 'C4', 'F4'], 1.2, 0.6), 3.5, 0.6)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_dame_en_noir', -17)
def king_dame_en_noir():
    """La Dame en noir : le couloir du wagon-lit, elle sort du brouillard (0,3 s), les lampes s'éteignent (1,6 s), sa voilette (3,4 s)."""
    prelude = [(rattle(2.0, 4, 4, 300, 1200), 0.0, 0.3), (ambience(2.0, 200, 900, 0.3), 0.0, 0.5)]
    entrance = Track(5.0)
    entrance.add(whoosh(1.2, 200, 900, q=0.8), 0.3, 0.4)
    for k in range(4):
        entrance.add(crack(0.03, 0.004, 800, 4000), 1.6 + k * 0.18, 0.4)
    gesture = [(modal(hz('E6'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 3.4, 0.4),
               (brass_chord(['E3', 'G3', 'B3', 'D#4'], 1.6, 0.4), 3.4, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.5), gesture)


@sound('cutscene/king_desperado', -17)
def king_desperado():
    """Le Desperado : le toit du fourgon qui file, il atterrit (0,6 s), deux coups de feu (3,4 et 3,75 s), les pièces."""
    prelude = [(rattle(2.0, 10, 10, 300, 1500), 0.0, 0.5), (bp(noise(2.0), 400, 2000) * 0.3, 0.0, 0.4)]
    entrance = Track(5.0)
    entrance.add(rattle(5.0, 10, 10, 300, 1500), 0.0, 0.3)
    entrance.add(whoosh(0.5, 200, 1500), 0.1, 0.4)
    entrance.add(impact(0.5, 140, 45), 0.6, 0.9)
    gesture = [(gunshot(), 3.4, 0.9), (gunshot(), 3.75, 0.9), (coin_shower(1.2, 14), 3.85, 0.5),
               (brass_chord(['D3', 'A3', 'D4', 'F4'], 1.2, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


def service_bell(f='A6'):
    """La sonnette de réception : « ding » clair et long."""
    return modal(hz(f), 1.6, GLASS_RATIOS, [1.4, 0.8, 0.5, 0.3])


def ice_crack(d=0.3):
    """De la glace qui craque : un claquement sec et aigu, un peu vitré."""
    return mix((crack(0.05, 0.005, 2000, 12000), 0, 1.0), (modal(hz('E7'), d, GLASS_RATIOS, [0.2, 0.15, 0.1, 0.08]), 0, 0.3))


def skate_glide(d=1.2):
    """Une lame de patin qui file sur la glace : un frottement aigu qui glisse."""
    return svf(noise(d), sweep(4000, 2500, d), q=2.0, mode='bp') * expdec(d, d * 0.6, attack=0.05) * 0.8


@sound('cutscene/king_concierge', -17)
def king_concierge():
    """Le Concierge : le hall, il se lève (0,6 s), il frappe la sonnette (3,4 s), la carte glisse."""
    prelude = [(ambience(2.0, 200, 900, 0.3), 0.0, 0.5), (service_bell('E6') * 0.3, 1.2, 0.3)]
    entrance = Track(5.0)
    entrance.add(whoosh(0.6, 200, 900), 0.2, 0.4)
    entrance.add(thump(150, 60, 0.3), 0.6, 0.6)
    gesture = [(service_bell('A6'), 3.4, 0.8), (card_flick(), 3.7, 0.6), (sparkle(1.0), 3.45, 0.4),
               (brass_chord(['D3', 'F#3', 'A3', 'D4'], 1.2, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.4), gesture)


@sound('cutscene/king_imperatrice_givre', -17)
def king_imperatrice_givre():
    """L'Impératrice du Givre : la patinoire, elle patine (0 à 1,2 s), freine (1,2 s), lève son sceptre (3,4 s)."""
    prelude = [(ambience(2.0, 1500, 5000, 0.2), 0.0, 0.4), (sparkle(2.0, 10), 0.0, 0.2)]
    entrance = Track(5.0)
    entrance.add(skate_glide(1.2), 0.0, 0.7)
    entrance.add(screech(0.4, 5000), 1.1, 0.3)
    entrance.add(ice_crack(), 1.2, 0.5)
    gesture = [(modal(hz('B6'), 2.4, GLASS_RATIOS, [1.6, 1.0, 0.7, 0.4]), 3.4, 0.5), (sparkle(1.6, 24), 3.4, 0.5),
               (brass_chord(['B2', 'F#3', 'B3', 'D4'], 1.6, 0.4), 3.5, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.35, size=1.8), gesture)


@sound('cutscene/king_yeti', -17, trim=False)
def king_yeti():
    """Le Yéti : la tempête, il saute et atterrit (0,7 s), il rugit (3,4 s), les boules de neige dévalent."""
    prelude = [(lp(noise(2.0), 600) * ramp(2.0, 0.3, 0.8), 0.0, 0.6), (boom(1.0, 40), 1.4, 0.3)]
    entrance = Track(5.0)
    entrance.add(lp(noise(5.0), 500), 0.0, 0.3)
    entrance.add(whoosh(0.6, 300, 1500), 0.1, 0.5)
    entrance.add(impact(0.6, 110, 35), 0.7, 1.0)
    gesture = [(roar(1.8, 75), 3.4, 0.9), (boom(1.5, 45), 3.5, 0.5), (lp(noise(1.5), 400) * expdec(1.5, 0.6), 3.6, 0.6),
               (brass_chord(['C2', 'G2', 'C3', 'Eb3'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.25, size=1.6), gesture)


@sound('cutscene/king_prince_neiges', -17)
def king_prince_neiges():
    """Le Prince des Neiges : la salle de bal, il descend l'escalier (0 à 1,4 s), lève sa coupe (3,4 s)."""
    prelude = [(ambience(2.0, 300, 1500, 0.3), 0.0, 0.4), (modal(hz('G5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 0.5, 0.2)]
    entrance = Track(5.0)
    for k in range(5):
        entrance.add(footstep(220, 0.8), 0.1 + k * 0.28, 0.5)
    gesture = [(modal(hz('C7'), 1.6, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 3.4, 0.6), (bubbles(1.4, 18, 800, 2400), 3.45, 0.4),
               (sparkle(1.2), 3.5, 0.4), (brass_chord(['G3', 'B3', 'D4', 'G4'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.35, size=1.8), gesture)


def temple_gong(d=3.0):
    """Un grand gong de temple : un coup sourd, puis un chatoiement de bronze qui enfle et s'éteint."""
    strike = thump(90, 50, 0.4) * 0.8
    shimmer = modal(hz('A2'), d, [1.0, 1.47, 2.09, 2.56, 3.2, 4.1], [1.0, 0.8, 0.6, 0.5, 0.35, 0.25])
    return mix((strike, 0, 1.0), (shimmer * ramp(d, 0.4, 1.0), 0, 0.8))


def dice_roll(d=0.6, count=6):
    """Des dés qui roulent sur la pierre : des petits chocs de plus en plus serrés."""
    track = Track(d + 0.1)
    for k in range(count):
        track.add(click(0.015, 1500, 5000), d * (1 - (1 - k / count) ** 1.6), rand(0.5, 1.0))
    return track.buf


@sound('cutscene/king_maitre_cloches', -17)
def king_maitre_cloches():
    """Le Maître des cloches : il monte les marches (0 à 1,2 s), le bourdon oscille, il frappe (3,4 s)."""
    prelude = [(ambience(2.0, 300, 1200, 0.3), 0.0, 0.5), (bell_toll(hz('D3'), 2.0), 0.4, 0.2)]
    entrance = Track(5.0)
    entrance.add(footsteps(1.2, 5), 0.0, 0.5)
    entrance.add(whoosh(0.5, 200, 1200), 1.0, 0.3)
    gesture = [(temple_gong(3.0), 3.4, 0.9), (bell_toll(hz('A2'), 2.4), 3.45, 0.5),
               (brass_chord(['D3', 'A3', 'D4', 'F4'], 1.2, 0.5), 3.5, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.6), gesture)


@sound('cutscene/king_tisseuse', -17)
def king_tisseuse():
    """La Tisseuse de destins : le jardin la nuit, elle descend le long de ses fils (0 à 1,6 s), ses yeux s'ouvrent (3,4 s)."""
    prelude = [(ambience(2.0, 200, 900, 0.3), 0.0, 0.5), (rustle(2.0), 0.0, 0.3)]
    entrance = Track(5.0)
    entrance.add(whoosh(1.6, 300, 1500, q=0.8), 0.0, 0.4)
    for k in range(5):
        entrance.add(modal(hz('E6') * (1 + k * 0.12), 0.8, GLASS_RATIOS, [1.0, 0.5, 0.3, 0.2]), 0.2 + k * 0.3, 0.15)
    gesture = [(modal(hz('B5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 3.4, 0.4),
               (brass_chord(['E3', 'G3', 'B3', 'D4'], 1.6, 0.4), 3.4, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.35, size=1.6), gesture)


@sound('cutscene/king_seigneur_des', -17)
def king_seigneur_des():
    """Le Seigneur des Dés : le grand dé tombe (0,8 s), il en sort (1,4 s), lance ses deux dés d'or (3,4 s)."""
    prelude = [(ambience(2.0, 200, 1000, 0.3), 0.0, 0.5), (rattle(2.0, 6, 6, 300, 1200, wood=True), 0.0, 0.2)]
    entrance = Track(5.0)
    entrance.add(whoosh(0.6, 200, 1500), 0.2, 0.4)
    entrance.add(impact(0.6, 120, 40), 0.8, 1.0)
    entrance.add(thump(160, 60, 0.3), 1.4, 0.6)
    gesture = [(dice_roll(0.7, 7), 3.4, 0.9), (dice_roll(0.7, 6), 3.5, 0.7),
               (brass_chord(['A2', 'E3', 'A3', 'C4'], 1.2, 0.5), 4.0, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.2, size=1.2), gesture)


@sound('cutscene/king_idole_or', -17)
def king_idole_or():
    """L'Idole d'Or : les bougies s'allument (0,2 s), l'idole sort de l'ombre (1 s), son oeil s'ouvre et les pièces jaillissent (3,4 s)."""
    prelude = [(ambience(2.0, 150, 700, 0.3), 0.0, 0.5), (drone(['D2', 'A2'], 2.0), 0.0, 0.3)]
    entrance = Track(5.0)
    for k in range(8):
        entrance.add(hiss(0.2), 0.2 + k * 0.1, 0.15)
    entrance.add(whoosh(1.2, 150, 900, q=0.8), 1.0, 0.4)
    gesture = [(temple_gong(2.4), 3.4, 0.6), (coin_shower(1.2, 16), 3.5, 0.6),
               (brass_chord(['D3', 'F3', 'A3', 'D4'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.5), gesture)


# --- Manoir hanté (10 lieux, 2026-11-20) ---

def creak(d=0.8, f=180):
    """Une porte ou un couvercle qui grince : un frottement grave qui monte."""
    return svf(noise(d), sweep(f, f * 2.2, d), q=8.0, mode='bp') * (0.5 + 0.5 * np.abs(np.sin(times(d) * 40))) \
        * fade_out(d, 0.2)


@sound('cutscene/king_comte_minuit', -17)
def king_comte_minuit():
    """Le Comte de Minuit : le salon, le couvercle du cercueil grince (0,5 s), la grande horloge sonne (3,4 s), les chauves-souris."""
    prelude = [(ambience(2.0, 150, 700, 0.3), 0.0, 0.5), (bell_toll(hz('C3'), 2.0), 0.6, 0.15)]
    entrance = Track(5.0)
    entrance.add(creak(1.0, 140), 0.5, 0.6)
    entrance.add(thump(120, 50, 0.3), 1.4, 0.6)
    gesture = [(bell_toll(hz('D3'), 2.4), 3.4, 0.8), (bell_toll(hz('A2'), 2.0), 3.9, 0.5),
               (wing_flap(), 3.5, 0.5), (brass_chord(['D3', 'F3', 'A3', 'C#4'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.6), gesture)


@sound('cutscene/king_bibliothecaire', -17)
def king_bibliothecaire():
    """La Bibliothécaire : des pages qui volent, elle apparaît (0,3 s), « chut » (3,4 s), le livre s'ouvre (3,9 s)."""
    prelude = [(ambience(2.0, 200, 900, 0.3), 0.0, 0.5), (rustle(1.2), 0.4, 0.3)]
    entrance = Track(5.0)
    entrance.add(whoosh(1.0, 200, 1200, q=0.8), 0.3, 0.4)
    for k in range(5):
        entrance.add(rustle(0.4), 0.6 + k * 0.3, 0.3)
    shush = bp(noise(0.8), 3000, 7000) * expdec(0.8, 0.5, attack=0.08)
    gesture = [(shush, 3.4, 0.7), (rustle(0.8), 3.9, 0.5),
               (modal(hz('E5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 3.95, 0.3),
               (brass_chord(['B2', 'D3', 'F3', 'A3'], 1.4, 0.4), 4.0, 0.4)]
    return king(prelude, reverb(entrance.buf, wet=0.35, size=1.6), gesture)


@sound('cutscene/king_fossoyeur', -17)
def king_fossoyeur():
    """Le Fossoyeur : le cimetière, la main sort de terre (0,3 s), il se hisse (0,9 à 1,5 s), il plante sa pelle (3,4 s)."""
    prelude = [(desert_wind(2.0), 0.0, 0.3),
               (bell_toll(hz('G2'), 2.0), 0.4, 0.15)]
    entrance = Track(5.0)
    entrance.add(lp(noise(0.5), 500) * expdec(0.5, 0.3), 0.3, 0.7)
    entrance.add(lp(noise(0.9), 400) * expdec(0.9, 0.6), 0.9, 0.6)
    entrance.add(thump(90, 40, 0.4), 1.5, 0.8)
    gesture = [(metal_hit(hz('E4'), 0.6), 3.4, 0.6),
               (lp(noise(0.4), 600) * expdec(0.4, 0.2), 3.42, 0.6),
               (brass_chord(['G2', 'Bb2', 'D3', 'F3'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.3, size=1.4), gesture)


@sound('cutscene/king_marionnettiste', -17)
def king_marionnettiste():
    """Le Marionnettiste : le rideau s'ouvre (0,2 s), il descend sur ses fils (0,8 s), il tire ses fils et les pièces tombent (3,4 s)."""
    prelude = [(ambience(2.0, 250, 1100, 0.3), 0.0, 0.4)]
    for k, n in enumerate(['E5', 'G5', 'B5', 'C6', 'B5', 'G5']):                       # la boîte à musique
        prelude.append((modal(hz(n), 0.8, GLASS_RATIOS, [1.0, 0.5, 0.3, 0.2]), 0.2 + k * 0.28, 0.18))
    entrance = Track(5.0)
    entrance.add(whoosh(0.8, 300, 1500), 0.2, 0.5)
    entrance.add(creak(1.2, 260), 0.8, 0.35)
    entrance.add(thump(150, 60, 0.3), 1.6, 0.6)
    gesture = [(whip_crack(), 3.4, 0.5), (whip_crack(), 3.6, 0.4), (coin_shower(1.2, 14), 3.6, 0.5),
               (brass_chord(['E3', 'G3', 'B3', 'D4'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.25, size=1.3), gesture)


@sound('cutscene/exploration_ending', -17, trim=False)
def cutscene_exploration_ending():
    """Fin de l'Exploration : six lieux s'allument (0,6 s puis toutes les 0,55 s), la table (4,2 s), l'écaille d'or (5 s), le sourire (5,5 s)."""
    track = Track(8.0)
    track.add(drone(['C3', 'G3'], 4.2, 'sine', 0.4, 0.6), 0.0, 0.3)
    for k, note in enumerate(['C5', 'D5', 'E5', 'G5', 'A5', 'C6']):
        track.add(chip(hz(note), 0.3, 'square', 0.25), 0.6 + k * 0.55, 0.4)
        track.add(sparkle(0.6, count=8), 0.6 + k * 0.55, 0.3)
    track.add(whoosh(0.5, 300, 1500), 4.0, 0.4)
    track.add(ambience(3.8, 300, 1200, 0.3), 4.2, 0.4)
    track.add(mix((thump(200, 70, 0.2), 0, 1.0), (metal_hit(1800, 0.8), 0, 0.5)), 5.0, 0.8)
    track.add(coin_shower(1.0, 10), 5.0, 0.4)
    track.add(brass_chord(['C3', 'G3', 'C4', 'E4', 'G4'], 1.8, 0.7), 5.5, 0.8)
    track.add(cash_register_bell(), 5.55, 0.5)
    return reverb(track.buf, wet=0.25, size=1.3)


# --- Le Croupier, boss secret (CroupierCutscene) --------------------------------
# Les instants ci-dessous reprennent les constantes de CroupierCutscene : si l'une change là-bas, la changer ici.

def card_riffle(d=0.35, count=16):
    """Paquet battu : une rafale de cartes qui s'entrecroisent, puis le paquet qui se referme."""
    track = Track(d + 0.15)
    for k in range(count):
        track.add(card_flick(0.04), k * d / count + rand(0, 0.006), rand(0.4, 0.8))
    track.add(card_slap(0.12), d, 0.6)
    return track.buf


def footstep(f=170, gain=1.0):
    """Un pas sur un parquet ciré."""
    return mix((modal(f, 0.12, WOOD_RATIOS, [0.04, 0.02, 0.01]), 0, gain), (click(0.01, 600, 2500), 0, 0.4 * gain))


def snap():
    """Claquement de doigts."""
    return mix((bp(noise(0.03), 1800, 6000) * expdec(0.03, 0.006, attack=0.0003), 0, 1.0), (click(0.006, 3000, 9000), 0, 0.6))


def ratchet(f):
    """Un cran de clé ou de cliquet : clac métallique sec."""
    return mix((metal_hit(f, 0.25), 0, 0.7), (click(0.008, 2000, 8000), 0, 1.0), (thump(260, 140, 0.06, 0.02), 0, 0.4))


def chuckle(at, track, count=6, root='G3', gain=0.5):
    """Son rire grave, « ha… ha… ha », qui descend."""
    for k in range(count):
        f = hz(root) * 2 ** (-(k % 3) / 12)
        ha = bp(osc(vibrato(f, 0.16, rate=28, depth=0.05), 0.16, 'saw'), 300, 1800) * adsr(0.16, 0.01, 0.05, 0.5, 0.07)
        track.add(ha, at + k * 0.19, gain)


@sound('cutscene/croupier', -17)
def cutscene_croupier():
    """
    Entrée du Croupier : nuit et vent au sommet de la Tour ; la porte se dessine en or (TRACE 0,3 à 1,2 s), s'ouvre
    (DOOR_OPEN 1,3 s) ; tes pas (1,6 à 2,3 s). La salle des machines (HALL_CUT 2,6 s) : tes pas sur le parquet
    (STEP_START 2,8 s, un toutes les 0,38 s), une contrebasse qui rôde ; il bat les cartes (RIFFLE 4,1 et 4,6 s) ;
    la caméra recule (5,0 s), il claque des doigts (SNAP 5,5 s) ; les six machines s'allument (LIGHT_START 5,65 s,
    une toutes les 0,15 s) ; il se révèle (CHANGE 6,6 s) : boum, cuivres, son ombre qui gronde jusqu'à 7,4 s.
    Gros plan (DEAL_CUT 7,6 s) : cinq cartes posées (7,85 s, une toutes les 0,28 s), retournées (9,3 s, une toutes
    les 0,1 s) ; ses deux répliques (9,75 et 10,3 s) ; fondu rouge à 10,9 s.
    """
    track = Track(12.0)
    # Le sommet de la Tour.
    track.add(ambience(2.8, 250, 1400, 0.3), 0.0, 1.0)
    track.add(drone(['A1', 'E2', 'A2'], 2.8, 'sine', 0.4, 0.4), 0.0, 0.18)
    trace = 0.9
    track.add(osc(sweep(700, 2100, trace), trace, 'tri') * ramp(trace, 0.2, 1.0) * fade_out(trace, 0.1), 0.3, 0.07)
    track.add(sparkle(trace, count=14, lo=3500, hi=8000), 0.3, 0.35)
    creak = svf(osc(sweep(110, 160, 0.45), 0.45, 'saw'), 900, q=3.0, mode='bp') * ramp(0.45, 1.0, 0.3)
    track.add(creak, 1.3, 0.35)
    track.add(modal(hz('E6'), 1.2, GLASS_RATIOS, [0.5, 0.25, 0.1, 0.05]), 1.3, 0.25)
    track.add(drone(['A3', 'C#4', 'E4'], 1.4, 'sine', 0.3, 0.5), 1.3, 0.12)        # la lumière chaude
    for at in (1.6, 1.95, 2.3):
        track.add(thump(150, 80, 0.12, 0.03), at, 0.5)
    track.add(whoosh(0.4, 400, 2000), 2.35, 0.25)
    # La salle des machines.
    track.add(lp(noise(5.0), 380) * fade_out(5.0, 0.3) * ramp(5.0, 0.0, 1.0, 0.2), 2.6, 0.35)
    bass = ['A2', 'C3', 'E3', 'D#3', 'D3']
    for k in range(5):                                                          # tes pas, et la contrebasse
        at = 2.8 + k * 0.38
        track.add(footstep(), at, 0.7)
        track.add(pluck(hz(bass[k]), 0.5, bright=1400), at, 0.45)
    for at in (4.1, 4.6):                                                       # il bat les cartes
        track.add(card_riffle(0.35), at, 0.8)
    track.add(whoosh(0.45, 300, 1500), 5.0, 0.3)                               # la caméra recule
    track.add(snap(), 5.5, 1.0)
    track.add(reverb(snap(), wet=0.6, size=1.8, tail=0.8), 5.5, 0.4)
    for k, note in enumerate(['A4', 'C5', 'E5', 'A5', 'C6', 'E6']):            # les machines s'allument
        at = 5.65 + k * 0.15
        track.add(chip(hz(note), 0.22, 'square', 0.25, s=0.4), at, 0.28)
        track.add(thump(90, 50, 0.15, 0.05), at, 0.5)
        track.add(hp(osc(120, 0.4, 'saw'), 600) * expdec(0.4, 0.15), at, 0.06)
    track.add(drone(['A1', 'A2'], 1.0, 'saw', 0.05, 0.8) * 0.5, 5.65, 0.08)   # le courant qui monte
    # Il se révèle.
    track.add(mix((impact(0.8, 110, 35), 0, 1.3), (crack_hit(), 0, 0.9)), 6.6, 1.0)
    track.add(brass_chord(['A2', 'C3', 'D#3', 'A3'], 1.6, 0.9), 6.62, 1.0)
    shadow = 0.9
    track.add(svf(noise(shadow), sweep(80, 500, shadow), q=1.0) * ramp(shadow, 0.3, 1.0) * fade_out(shadow, 0.2), 6.6, 0.6)
    track.add(fire_burst(1.0), 6.62, 0.35)
    # Gros plan sur le tapis : il distribue tes cartes.
    track.add(drone(['A1', 'E2'], 3.5, 'tri', 0.2, 0.6), 7.6, 0.16)
    track.add(heartbeat([7.7, 8.5, 9.3], f=50, gain=0.6), 0.0, 0.6)
    for k in range(5):
        land = 7.85 + k * 0.28
        track.add(card_flick(0.08), land - 0.2, 0.5)
        track.add(card_slap(0.16), land, 0.9)
    for k in range(5):                                                          # il les retourne
        at = 9.3 + k * 0.1
        track.add(card_flick(0.05), at, 0.6)
        track.add(modal(hz('A5') * 2 ** (k * 2 / 12), 0.35, GLASS_RATIOS, [0.15, 0.07, 0.03, 0.02]), at + 0.06, 0.2)
    track.add(mix((brass_chord(['A2', 'E3'], 0.6, 0.7), 0, 1.0), (thump(70, 40, 0.4, 0.15), 0, 0.8)), 9.75, 0.8)
    track.add(mix((brass_chord(['A2', 'C3', 'E3', 'A3'], 1.2, 0.9), 0, 1.0), (impact(0.6, 90, 35), 0, 1.0)), 10.3, 0.9)
    chuckle(10.45, track, 4, 'E3', 0.3)
    track.add(swell(0.9, 200, 2200), 10.6, 0.3)
    return reverb(track.buf, wet=0.25, size=1.6, tail=1.0)


@sound('cutscene/croupier_rig', -17)
def cutscene_croupier_rig():
    """
    Phase 2 du Croupier (table truquée) : tes trois rouleaux tournent ; éclair rouge (RIG_FLASH 0,9 s). Sous la
    table (UNDER_CUT 1,5 s) : engrenages, sa main (1,55 à 1,95 s), la clé entre (KEY_IN 2,0 s), trois crans (2,2,
    2,45, 2,7 s). Retour sur ta machine (BACK_CUT 3,0 s) : ton rouleau s'arrête net (RIG_STOP 3,15 s), la chaîne
    s'enroule (3,2 à 3,45 s), le cadenas claque (LOCK 3,5 s) ; les deux autres rouleaux s'arrêtent (3,75 et 3,9 s).
    Fondu rouge à 4,2 s.
    """
    track = Track(5.6)
    track.add(drone(['D1', 'A1', 'D2'], 4.8, 'tri', 0.2, 0.8), 0.0, 0.2)
    track.add(rattle(1.5, 24, 24, 1800, 4500), 0.0, 0.4)                       # tes rouleaux tournent
    track.add(hp(osc(110, 1.5, 'saw'), 800) * fade_out(1.5, 0.1), 0.0, 0.05)
    zap = osc(sweep(1800, 160, 0.35), 0.35, 'saw') * expdec(0.35, 0.12)
    track.add(mix((bp(zap, 200, 4000), 0, 1.0), (crack_hit(), 0, 0.8)), 0.9, 0.6)
    track.add(brass_chord(['D2', 'G#2', 'D3'], 0.6, 0.5), 0.92, 0.6)
    # Sous la table.
    for k in range(6):                                                          # les engrenages, au ralenti
        track.add(click(0.01, 800, 2500), 1.5 + k * 0.25, 0.35)
    track.add(whoosh(0.4, 250, 1200), 1.55, 0.35)
    track.add(mix((metal_hit(1800, 0.2), 0, 0.5), (click(0.006, 3000, 9000), 0, 1.0)), 2.0, 0.6)
    for k, at in enumerate((2.2, 2.45, 2.7)):
        track.add(ratchet(700 + 90 * k), at, 0.9)
    track.add(svf(noise(1.4), sweep(200, 900, 1.4), q=1.2) * ramp(1.4, 0.0, 1.0), 1.6, 0.15)   # la tension monte
    # Retour sur ta machine.
    track.add(rattle(0.75, 26, 22, 1800, 4500), 3.0, 0.4)
    track.add(mix((thump(240, 90, 0.25, 0.07), 0, 1.0), (click(0.012, 1500, 6000), 0, 0.8)), 3.15, 1.0)
    track.add(rattle(0.25, 40, 40, 3000, 7000), 3.2, 0.5)                      # la chaîne
    track.add(mix((metal_hit(520, 0.6), 0, 1.0), (impact(0.5, 120, 40), 0, 1.0), (click(0.01, 1500, 6000), 0, 0.8)),
              3.5, 1.0)
    track.add(sparkle(0.6, count=10), 3.5, 0.3)
    track.add(brass_chord(['D2', 'F2', 'G#2', 'D3'], 1.2, 0.8), 3.55, 0.9)
    for at in (3.75, 3.9):
        track.add(mix((thump(220, 100, 0.15, 0.05), 0, 0.8), (click(0.01, 1500, 6000), 0, 0.5)), at, 0.6)
    track.add(swell(0.6, 300, 2600), 4.2, 0.25)
    return reverb(track.buf, wet=0.22, size=1.4, tail=0.8)


@sound('cutscene/croupier_open', -17)
def cutscene_croupier_open():
    """
    Phase 3 du Croupier (cartes sur table) : la lampe tombe (LAMP_DROP 0,45 s) et frappe le tapis de lumière
    (LIGHT_HIT 0,85 s) ; il jette sa main (THROW 1,2 à 1,6 s) ; ses huit cartes retombent (LAND_START 1,8 s, une
    toutes les 0,14 s) ; son chapeau glisse, son ombre monte (GLARE 3,0 à 3,6 s), il rit (3,25 s). Fondu à 4,2 s.
    """
    track = Track(5.6)
    track.add(ambience(0.9, 200, 900, 0.25), 0.0, 1.0)
    track.add(rattle(0.4, 18, 30, 2500, 6000), 0.45, 0.35)                     # la chaîne de la lampe
    track.add(whoosh(0.4, 300, 1200), 0.45, 0.25)
    track.add(mix((thump(90, 40, 0.6, 0.2), 0, 1.0), (crack_hit(), 0, 0.5)), 0.85, 0.9)
    buzz = hp(osc(vibrato(100, 3.4, rate=8, depth=0.01), 3.4, 'saw'), 300) * fade_out(3.4, 0.4) * ramp(3.4, 1.0, 0.6)
    track.add(buzz, 0.85, 0.05)                                                # l'ampoule qui grésille
    track.add(drone(['D1', 'A1', 'D2'], 3.9, 'tri', 0.1, 0.8), 0.85, 0.2)
    track.add(whoosh(0.45, 400, 3000), 1.2, 0.5)                               # il jette ses cartes
    for k in range(10):
        track.add(card_flick(0.05), 1.2 + k * 0.045, 0.4)
    for k in range(8):
        land = 1.8 + k * 0.14
        track.add(card_slap(0.15), land, 0.8)
        track.add(modal(hz('D5') * 2 ** (k * 2 / 12), 0.3, GLASS_RATIOS, [0.12, 0.06, 0.03, 0.02]), land, 0.12)
    track.add(mix((impact(0.9, 100, 30), 0, 1.2), (crack_hit(), 0, 0.8)), 3.0, 1.0)
    track.add(brass_chord(['D2', 'F2', 'G#2', 'D3'], 1.5, 0.9), 3.02, 1.0)
    track.add(svf(noise(0.6), sweep(80, 600, 0.6), q=1.0) * ramp(0.6, 0.3, 1.0), 3.0, 0.5)   # son ombre monte
    track.add(whoosh(0.3, 800, 2500), 3.0, 0.2)                                # le chapeau glisse
    chuckle(3.25, track, 5, 'D3', 0.45)
    track.add(swell(0.6, 300, 2600), 4.2, 0.25)
    return reverb(track.buf, wet=0.25, size=1.5, tail=0.8)


@sound('cutscene/croupier_ending', -17)
def cutscene_croupier_ending():
    """
    La vraie fin : il pose sa main perdante (LAY_START 0,4 s, une carte toutes les 0,16 s) ; il ôte son chapeau
    (HAT_OFF 1,25 s) et s'incline (1,6 à 2,7 s), le pose sur la table (HAT_ON_TABLE 3,2 s), le sabot glisse jusqu'à
    toi (3,4 à 4,0 s) ; les machines s'éteignent (OFF_START 4,3 s, une toutes les 0,22 s), le toit s'ouvre sur l'aube
    (4,8 à 6,0 s), il s'efface (5,3 à 6,2 s). Contrechamp au blanc (REVERSE_CUT 6,5 s) : toi, sa réplique
    (7,1 s), fondu blanc à 8,4 s.
    """
    track = Track(9.6)
    track.add(drone(['C3', 'G3'], 4.6, 'sine', 0.4, 0.8), 0.0, 0.18)
    track.add(lp(noise(4.6), 400) * fade_out(4.6, 0.5), 0.0, 0.25)
    for k in range(5):
        land = 0.4 + k * 0.16
        track.add(card_flick(0.06), land - 0.12, 0.4)
        track.add(card_slap(0.14), land, 0.6)
    track.add(whoosh(0.35, 600, 2000), 1.25, 0.25)                             # il ôte son chapeau
    for k, note in enumerate(['G5', 'E5', 'C5', 'G4', 'E4', 'C4']):            # sa révérence, à la harpe
        track.add(pluck(hz(note), 0.9, bright=5000), 1.6 + k * 0.09, 0.35)
    track.add(mix((thump(160, 90, 0.12, 0.04), 0, 0.8), (lp(noise(0.08), 900) * expdec(0.08, 0.02), 0, 0.5)), 3.2, 0.8)
    scrape = bp(noise(0.6), 300, 1800) * ramp(0.6, 1.0, 0.3)
    track.add(scrape, 3.4, 0.35)                                               # le sabot glisse
    track.add(footstep(220, 0.8), 4.0, 0.8)
    for k, note in enumerate(['C6', 'A5', 'F5', 'D5', 'A4', 'F4']):            # les machines s'éteignent
        at = 4.3 + k * 0.22
        down = osc(sweep(hz(note), hz(note) * 0.5, 0.3), 0.3, 'square', 0.25) * expdec(0.3, 0.1)
        track.add(down, at, 0.18)
        track.add(thump(80, 40, 0.2, 0.06), at, 0.4)
    track.add(lp(noise(1.2), 160) * ramp(1.2, 0.3, 1.0) * fade_out(1.2, 0.3), 4.8, 0.8)   # le toit s'ouvre
    track.add(rattle(1.2, 8, 4, 500, 1400, wood=True), 4.8, 0.35)
    track.add(drone(['C4', 'E4', 'G4', 'C5'], 4.6, 'sine', 1.0, 1.0), 4.8, 0.14)   # l'aube
    track.add(sparkle(0.9, count=18, lo=3000, hi=9000), 5.3, 0.3)              # il s'efface
    track.add(osc(sweep(800, 2600, 0.9), 0.9, 'sine') * ramp(0.9, 0.0, 1.0) * fade_out(0.9, 0.2), 5.3, 0.05)
    track.add(swell(0.5, 600, 5000), 6.0, 0.35)                                # passage au blanc
    track.add(brass_chord(['C3', 'G3', 'C4', 'E4', 'G4'], 2.2, 0.8), 7.1, 0.9)
    track.add(modal(hz('C6'), 1.6, BAR_RATIOS, [0.6, 0.3, 0.15, 0.06], [1, 0.45, 0.25, 0.1]), 7.1, 0.35)
    track.add(coin_shower(0.8, 8), 7.15, 0.25)
    track.add(swell(0.8, 400, 4000), 8.2, 0.3)
    return reverb(track.buf, wet=0.28, size=1.6, tail=1.0)


# --- Le Joueur Masqué (RivalCutscene) -------------------------------------------
# Les instants ci-dessous reprennent les constantes de RivalCutscene : si l'une change là-bas, la changer ici.

def lamp_on():
    """Un projecteur qui s'allume : clac du levier, puis le bourdon de l'ampoule."""
    return mix((click(0.01, 1500, 6000), 0, 1.0), (thump(120, 60, 0.25, 0.08), 0, 0.8), (metal_hit(900, 0.3), 0, 0.3))


@sound('cutscene/rival', -17)
def cutscene_rival():
    """
    Une rencontre du Joueur Masqué : le lieu dans le noir ; le projecteur s'allume (SPOT 0,5 s) ; ses pas
    (WALK_START 0,7 à WALK_END 1,5 s) ; il soulève son chapeau (TIP 1,8 s) ; le symbole appris jaillit et
    rejoint sa machine (SYMBOL 2,4 s) ; ses yeux brillent sous le masque (EYES 3,0 s) ; sa réplique (LINE 3,2 s),
    une valse qui grince ; « Rencontre n sur 5 » (SUBTITLE 4,4 s). Fondu au noir à 5,6 s.
    """
    track = Track(6.6)
    track.add(ambience(5.8, 200, 900, 0.3), 0.0, 0.8)
    track.add(drone(['D2', 'A2'], 5.8, 'tri', 0.4, 0.8), 0.0, 0.15)
    track.add(lamp_on(), 0.5, 0.9)
    hum = hp(osc(vibrato(100, 5.0, rate=7, depth=0.01), 5.0, 'saw'), 400) * fade_out(5.0, 0.5)
    track.add(hum, 0.55, 0.03)
    for at in (0.75, 1.05, 1.35):                                               # ses pas
        track.add(footstep(200, 0.9), at, 0.7)
    track.add(whoosh(0.3, 500, 2200), 1.8, 0.35)                                 # le chapeau
    track.add(chip(hz('A5'), 0.3, 'square', 0.25), 2.4, 0.3)                     # le symbole appris
    track.add(sparkle(0.7, count=12), 2.4, 0.35)
    track.add(mix((thump(160, 80, 0.15, 0.05), 0, 0.7), (click(0.01, 1500, 6000), 0, 0.5)), 2.9, 0.6)
    for k, note in enumerate(['E6', 'B6']):                                     # ses yeux
        track.add(modal(hz(note), 0.6, GLASS_RATIOS, [0.3, 0.15, 0.07, 0.03]), 3.0 + k * 0.08, 0.25)
    waltz = ['D3', 'A3', 'F4', 'A2', 'A3', 'E4', 'D3', 'A3', 'F4']               # une valse de manège désaccordée
    for k, note in enumerate(waltz):
        at = 3.2 + k * 0.24
        track.add(pluck(hz(note) * (1.0 + 0.012 * ((k % 3) - 1)), 0.45, bright=2400), at, 0.4 if k % 3 == 0 else 0.25)
    track.add(brass_chord(['D3', 'F3', 'A3'], 1.2, 0.5), 4.4, 0.5)
    track.add(swell(0.6, 200, 1800), 5.4, 0.25)
    return reverb(track.buf, wet=0.25, size=1.4, tail=0.8)


@sound('cutscene/rival_unmask', -17)
def cutscene_rival_unmask():
    """
    Le duel final du Joueur Masqué, au Désert : vent et sable ; sa première réplique (UNMASK_LINE 0,9 s) ; sa main
    sur le masque, qui tremble (GRAB 2,4 s) ; il l'arrache (MASK_OFF 3,0 s) : éclair, le masque vole puis tombe
    dans le sable (vers 4,1 s) ; ses deux répliques (UNMASK_LINE_2 4,0 s, UNMASK_LINE_3 5,6 s). Fondu au noir à 7,2 s.
    """
    track = Track(8.6)
    track.add(desert_wind(7.6), 0.0, 0.6)
    track.add(drone(['C2', 'G2'], 3.0, 'sine', 0.5, 0.6), 0.0, 0.2)
    track.add(chip(hz('C4'), 0.4, 'tri', 0.2), 0.9, 0.2)
    shiver = svf(noise(0.6), sweep(300, 1600, 0.6), q=2.0, mode='bp') * ramp(0.6, 0.2, 1.0)
    track.add(shiver, 2.4, 0.3)
    track.add(heartbeat([2.4, 2.7], f=55, gain=0.7), 0.0, 0.6)
    track.add(mix((impact(0.8, 120, 40), 0, 1.2), (crack_hit(), 0, 0.8), (metal_hit(1400, 0.6), 0, 0.4)), 3.0, 1.0)
    track.add(brass_chord(['C3', 'D#3', 'G3', 'C4'], 1.8, 0.9), 3.02, 0.9)
    track.add(whoosh(0.6, 800, 3000), 3.05, 0.4)                                # le masque vole
    track.add(mix((thump(200, 90, 0.12, 0.04), 0, 0.6), (lp(noise(0.3), 1200) * expdec(0.3, 0.08), 0, 0.5)), 4.1, 0.6)
    track.add(drone(['C3', 'G3', 'C4'], 3.4, 'sine', 0.4, 0.8), 4.0, 0.14)
    track.add(mix((brass_chord(['C2', 'G2', 'C3', 'D#3'], 1.4, 0.9), 0, 1.0), (thump(70, 40, 0.4, 0.15), 0, 0.8)), 5.6, 0.8)
    track.add(swell(0.7, 200, 2000), 7.0, 0.25)
    return reverb(track.buf, wet=0.25, size=1.5, tail=0.9)


@sound('cutscene/rival_farewell', -17)
def cutscene_rival_farewell():
    """
    Les adieux du Joueur Masqué : il pose son masque sur le tapis (LAY 0,8 s) ; sa réplique (FAREWELL_LINE 1,6 s) ;
    il s'efface en sable (VANISH_START 3,0 à VANISH_END 4,6 s) ; sa dernière réplique (FAREWELL_LINE_2 4,2 s).
    Fondu au noir à 5,6 s.
    """
    track = Track(6.8)
    track.add(desert_wind(6.0), 0.0, 0.35)
    track.add(drone(['F3', 'C4', 'A4'], 5.8, 'sine', 0.6, 1.0), 0.0, 0.14)
    track.add(whoosh(0.3, 400, 1500), 0.45, 0.25)
    track.add(mix((card_slap(0.16), 0, 0.8), (metal_hit(1600, 0.5), 0, 0.35)), 0.8, 0.9)
    track.add(sparkle(0.5, count=8), 0.8, 0.25)
    for k, note in enumerate(['A4', 'C5', 'F5', 'E5', 'C5']):
        track.add(pluck(hz(note), 0.9, bright=4000), 1.6 + k * 0.22, 0.3)
    sand = bp(noise(1.6), 2500, 8000) * ramp(1.6, 0.0, 1.0) * fade_out(1.6, 0.6)
    track.add(sand, 3.0, 0.3)                                                   # il s'efface en sable
    track.add(sparkle(1.4, count=16, lo=3000, hi=8000), 3.0, 0.2)
    track.add(brass_chord(['F2', 'C3', 'F3', 'A3'], 1.6, 0.8), 4.2, 0.7)
    track.add(swell(0.6, 300, 2400), 5.4, 0.25)
    return reverb(track.buf, wet=0.3, size=1.6, tail=1.0)


# ===========================================================================
# Manoir hanté : phases et morts des rois (ManorBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def bat_squeak():
    """Petit cri aigu de chauve-souris."""
    d = 0.07
    return osc(sweep(rand(4200, 5200), rand(2600, 3400), d), d) * expdec(d, 0.03, attack=0.003)


def music_box(note, d=1.2):
    """Une note de boîte à musique."""
    return modal(hz(note), d, GLASS_RATIOS, [d * 0.6, d * 0.3, d * 0.2, d * 0.1], [1, 0.4, 0.25, 0.1])


def wood_knock(f=220):
    """Un morceau de bois qui tombe et claque."""
    return mix((modal(f, 0.25, WOOD_RATIOS, [0.12, 0.08, 0.05, 0.03]), 0, 1.0), (crack_hit(), 0, 0.4))


def snip():
    """Un coup de ciseaux : deux lames qui se croisent."""
    return mix((crack(0.03, 0.004, 3000, 12000), 0, 0.8), (metal_hit(2400, 0.25, 0.6), 0.005, 0.5))


@sound('cutscene/phase2_comte_minuit', -17)
def phase2_comte_minuit():
    """Le Comte de Minuit, phase 2 (CountPhase) : la cloche sonne douze coups (0,4 s puis tous les 0,28 s), le dernier à 3,48 s."""
    first, gap = 0.4, 0.28
    last, end = first + 11 * gap, first + 11 * gap + 2.5
    track = Track(end + 1.0)
    track.add(ambience(end, 120, 600, 0.25), 0.0, 1.0)
    track.add(drone(['D1', 'A1'], end, 'tri', fin=1.5, fout=1.0), 0.0, 0.2)
    for k in range(12):
        at = first + k * gap
        track.add(bell_toll(hz('D3') if k < 11 else hz('A2'), 2.4), at, 0.32 + 0.02 * k)
        track.add(wing_flap(), at + 0.05, 0.25)
        track.add(bat_squeak(), at + rand(0.05, 0.15), 0.12)
    boom = mix((thump(80, 30, 1.4, 0.4), 0, 1.0), (crack(0.06, 0.01, 300, 8000), 0, 0.6))
    track.add(reverb(boom, wet=0.3, size=1.6, tail=1.2), last, 0.9)
    track.add(brass_chord(['D3', 'F3', 'G#3', 'C#4'], 1.6, 0.8), last + 0.1, 0.8)
    for k in range(10):
        track.add(wing_flap(), last + 0.05 + k * 0.08, 0.3)
        track.add(bat_squeak(), last + rand(0.05, 0.9), 0.15)
    return track.buf


@sound('cutscene/death_comte_minuit', -17)
def death_comte_minuit():
    """Le Comte de Minuit meurt (CountDeath) : l'aube monte, il grésille, il éclate en chauves-souris (2,5 s),
    elles se dispersent, les dernières plongent dans le cercueil (3,2 s) et le couvercle claque (4,0 s)."""
    burst, home, shut, end = 2.5, 3.2, 4.0, 5.6
    track = Track(end + 1.0)
    track.add(ambience(end, 150, 700, 0.2), 0.0, 1.0)
    dawn = stack(*(osc(hz(note), burst, 'sine') for note in ('A3', 'C#4', 'E4', 'A4')))
    track.add(dawn * ramp(burst, 0.0, 1.0, 1.5), 0.2, 0.18)
    track.add(hp(noise(burst - 1.2), 4000) * ramp(burst - 1.2, 0.0, 1.0, 1.0), 1.2, 0.12)    # il grésille au soleil
    track.add(mix((crack(0.06, 0.01, 400, 9000), 0, 1.0), (whoosh(0.8, 300, 2500), 0, 0.8)), burst, 0.8)
    for k in range(26):                                                    # la nuée qui se disperse
        track.add(wing_flap(), burst + rand(0.0, 1.2), rand(0.15, 0.3))
    for k in range(12):
        track.add(bat_squeak(), burst + rand(0.0, 1.4), rand(0.08, 0.16))
    for k in range(3):
        track.add(whoosh(0.4, 500, 1800), home + 0.15 * k, 0.25)
    track.add(creak(0.3, 120), shut - 0.3, 0.4)
    track.add(mix((thump(110, 45, 0.5, 0.12), 0, 1.0), (wood_knock(160), 0, 0.8)), shut, 1.0)
    track.add(brass_chord(['A2', 'C3', 'E3', 'A3'], 1.4, 0.5), shut + 0.05, 0.4)
    return track.buf


@sound('cutscene/phase2_bibliothecaire', -17)
def phase2_bibliothecaire():
    """La Bibliothécaire, phase 2 (LibrarianPhase) : trois bougies soufflées (0,6 / 1,05 / 1,5 s), « chut », ses yeux (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    for at in (0.6, 1.05, 1.5):
        puff = bp(noise(0.25), 400, 2500) * expdec(0.25, 0.08, attack=0.02)
        track.add(puff, at - 0.05, 0.7)
        track.add(hp(noise(0.4), 5000) * expdec(0.4, 0.15), at, 0.08)
    shush = bp(noise(0.7), 3000, 7000) * expdec(0.7, 0.45, attack=0.08)
    track.add(shush, 1.75, 0.5)
    track.add(mix((thump(70, 30, 1.2, 0.35), 0, 1.0)), 2.2, 0.8)
    track.add(modal(hz('E5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 2.2, 0.3)
    track.add(brass_chord(['B2', 'D3', 'F3', 'A3'], 1.6, 0.5), 2.25, 0.6)
    return track.buf


@sound('cutscene/death_bibliothecaire', -17)
def death_bibliothecaire():
    """La Bibliothécaire meurt (LibrarianDeath) : le grimoire s'ouvre (0,5 s), ses pages tournent, il l'aspire (1,3 s),
    il claque (2,5 s) et tombe au sol (3,25 s)."""
    slam, end = 2.5, 5.6
    track = Track(end + 1.0)
    track.add(ambience(end, 200, 900, 0.2), 0.0, 1.0)
    track.add(whoosh(0.5, 300, 1500), 0.05, 0.4)
    for k in range(4):
        track.add(rustle(0.3), 0.5 + k * 0.2, 0.4)
    track.add(svf(noise(1.2), sweep(200, 3000, 1.2), q=1.5) * ramp(1.2, 0.0, 1.0, 1.5), 1.3, 0.5)    # il l'aspire
    slam_sound = mix((crack(0.05, 0.008, 500, 9000), 0, 1.0), (thump(120, 40, 0.6, 0.15), 0, 1.2), (wood_knock(180), 0, 0.6))
    track.add(reverb(slam_sound, wet=0.3, size=1.4, tail=1.0), slam, 1.0)
    track.add(mix((thump(100, 40, 0.4, 0.1), 0, 1.0), (wood_knock(140), 0, 0.5)), slam + 0.75, 0.6)
    fall = stack(*(osc(sweep(hz(note), hz(note) / 2, 2.0), 2.0, 'tri') for note in ('B3', 'D4', 'F4')))
    track.add(fall * expdec(2.0, 1.0, attack=0.05), slam + 0.3, 0.1)
    return track.buf


@sound('cutscene/phase2_fossoyeur', -17)
def phase2_fossoyeur():
    """Le Fossoyeur, phase 2 (DiggerPhase) : trois coups de pelle (0,6 / 1,1 / 1,6 s), la terre gronde, l'éboulement (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 500, 0.2), 0.0, 1.0)
    track.add(lp(noise(3.6), 160) * ramp(3.6, 0.1, 1.0, 1.0), 0.6, 0.9)                         # la terre gronde
    for k, at in enumerate((0.6, 1.1, 1.6)):
        track.add(mix((metal_hit(380, 0.5, 0.7), 0, 0.6), (thump(130, 50, 0.4, 0.1), 0, 1.0)), at, 0.6 + 0.1 * k)
        for r in range(2 + 2 * k):
            track.add(crack_hit(), at + rand(0.3, 0.9), rand(0.15, 0.3))
    boom = mix((thump(70, 25, 1.6, 0.5), 0, 1.2), (crack(0.08, 0.012, 300, 8000), 0, 0.8), (lp(noise(1.5), 900) * expdec(1.5, 0.5), 0, 0.8))
    track.add(reverb(boom, wet=0.3, size=1.6, tail=1.2), 2.2, 1.0)
    for r in range(16):
        track.add(mix((crack_hit(), 0, 0.8), (thump(rand(150, 260), 60, 0.15, 0.04), 0, 0.6)), 2.3 + rand(0.0, 1.3), rand(0.15, 0.35))
    return track.buf


@sound('cutscene/death_fossoyeur', -17)
def death_fossoyeur():
    """Le Fossoyeur meurt (DiggerDeath) : la terre s'ouvre (0,6 s), il s'enfonce, la terre se referme (2,7 s),
    sa pierre tombale sort (jusqu'à 3,2 s), son âme verte s'échappe (3,4 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 500, 0.2), 0.0, 1.0)
    track.add(lp(noise(2.3), 200) * ramp(2.3, 0.3, 1.0, 1.0), 0.6, 0.8)
    for k in range(7):
        track.add(bp(noise(0.2), 300, 1500) * expdec(0.2, 0.08), 0.7 + k * 0.28, rand(0.3, 0.5))   # la terre qui coule
    track.add(thump(100, 40, 0.5, 0.15), 2.7, 0.8)
    track.add(creak(0.5, 70), 2.7, 0.6)                                                        # la pierre qui sort de terre
    track.add(mix((thump(90, 35, 0.6, 0.15), 0, 1.0), (crack_hit(), 0, 0.5)), 3.2, 0.8)
    wail = osc(sweep(hz('E5'), hz('B4'), 1.6), 1.6, 'sine') * expdec(1.6, 0.8, attack=0.2)
    track.add(reverb(wail, wet=0.5, size=1.8, tail=1.0), 3.4, 0.2)
    track.add(bell_toll(hz('A2'), 2.0), 3.5, 0.2)
    return track.buf


@sound('cutscene/phase2_marionnettiste', -17)
def phase2_marionnettiste():
    """Le Marionnettiste, phase 2 (PuppeteerPhase) : le rideau se lève (0,25 à 1,2 s), le projecteur (0,9 s), les fils,
    il danse et chante (boîte à musique), l'éclat de la phase (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    track.add(whoosh(1.0, 150, 900, q=0.8), 0.25, 0.6)
    track.add(mix((crack(0.04, 0.006, 800, 6000), 0, 0.6), (thump(160, 80, 0.2, 0.05), 0, 0.5)), 0.9, 0.5)   # le projecteur
    for k in range(4):
        track.add(pluck(hz(['E4', 'G4', 'B4', 'E5'][k]), 0.4), 1.0 + k * 0.1, 0.3)          # les fils tendus
    melody = ['E5', 'G5', 'B5', 'A5', 'G5', 'E5', 'F#5', 'B5', 'E6']
    for k, note in enumerate(melody):
        track.add(music_box(note), 1.3 + k * 0.32, 0.4)
    track.add(mix((thump(90, 40, 1.0, 0.3), 0, 1.0)), 2.2, 0.7)
    track.add(brass_chord(['E3', 'G3', 'A#3', 'D4'], 1.4, 0.5), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_marionnettiste', -17)
def death_marionnettiste():
    """Le Marionnettiste meurt (PuppeteerDeath) : quatre coups de ciseaux (0,6 / 1,05 / 1,5 / 1,95 s), il s'effondre
    comme un pantin (2,5 s), le rideau tombe (3,6 à 4,4 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.2), 0.0, 1.0)
    for k, at in enumerate((0.6, 1.05, 1.5, 1.95)):
        track.add(snip(), at, 0.7)
        track.add(pluck(hz(['B4', 'G4', 'E4', 'C4'][k]), 0.5), at + 0.01, 0.35)
    for k in range(5):
        track.add(wood_knock(rand(180, 320)), 2.5 + k * rand(0.07, 0.14), rand(0.4, 0.7))
    for k, note in enumerate(['E5', 'D5', 'B4', 'G4']):
        track.add(music_box(note, 1.4), 2.8 + k * (0.28 + 0.08 * k), 0.35)
    track.add(whoosh(0.8, 120, 700, q=0.8), 3.6, 0.6)
    track.add(mix((thump(90, 35, 0.6, 0.15), 0, 1.0), (lp(noise(0.3), 600) * expdec(0.3, 0.1), 0, 0.6)), 4.35, 0.8)
    return track.buf


# ===========================================================================
# Palace des Neiges : phases et morts des rois (PalaceBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def desk_bell():
    """La sonnette de réception : un « ding » clair qui tient."""
    return mix((metal_hit(2637, 1.4, 0.7), 0, 1.0), (metal_hit(3951, 0.8, 0.5), 0, 0.3), (click(0.004, 3000, 9000), 0, 0.4))


def glass_clink(f=None):
    """Deux coupes de cristal qui se touchent."""
    f = f or rand(2600, 3400)
    return modal(f, 0.9, GLASS_RATIOS, [0.6, 0.35, 0.2, 0.12], [1, 0.5, 0.3, 0.15])


def fizz(d=1.2):
    """Le pétillement du champagne."""
    x = sparkle(d, count=int(40 * d), lo=4000, hi=11000, tau=0.02)
    return x * np.linspace(1.0, 0.0, len(x)) ** 1.5


def key_jingle():
    """Un trousseau de clés qui tinte."""
    return mix(*((metal_hit(rand(3200, 5200), 0.25, 0.8), rand(0.0, 0.12), rand(0.3, 0.6)) for _ in range(6)))


def ice_shatter():
    """Une statue de glace qui vole en éclats."""
    parts = [(crack(0.08, 0.012, 1500, 12000), 0, 1.0), (thump(120, 50, 0.5, 0.12), 0, 0.8)]
    parts += [(ice_crack(0.25), rand(0.0, 0.5), rand(0.2, 0.5)) for _ in range(10)]
    parts += [(glass_clink(rand(3000, 6000)), rand(0.05, 0.8), rand(0.1, 0.25)) for _ in range(8)]
    return mix(*parts)


def icy_wind(d):
    """Un vent glacé qui hurle."""
    return svf(noise(d), sweep(600, 2200, d), q=2.5, mode='bp') * adsr(d, 0.25, 0.2, 0.8, 0.6)


def snore(d=0.95):
    """La respiration lente d'un gros dormeur : on inspire, puis un grondement doux."""
    inhale = bp(noise(d * 0.45), 300, 1200) * ramp(d * 0.45, 0.0, 1.0, 0.8)
    rumble = lp(osc(sweep(55, 48, d * 0.55), d * 0.55, 'saw'), 300) * expdec(d * 0.55, d * 0.35, attack=0.05)
    return mix((inhale, 0, 0.5), (rumble, d * 0.45, 0.8))


def waltz(start, bars, track, gain=0.35, notes=('A4', 'C#5', 'E5', 'F#5', 'E5', 'C#5')):
    """Une petite valse de boîte à musique, à trois temps (une mesure = 0,6 s)."""
    for b in range(bars):
        root = ['A2', 'D3', 'E3', 'A2'][b % 4]
        track.add(music_box(root, 0.9), start + b * 0.6, gain * 0.6)
        for beat in (1, 2):
            chord = modal(transpose(hz(root), 19), 0.4, GLASS_RATIOS, [0.24, 0.12, 0.08, 0.04], [1, 0.4, 0.25, 0.1])
            track.add(chord, start + b * 0.6 + beat * 0.2, gain * 0.35)
        track.add(music_box(notes[b % len(notes)], 1.0), start + b * 0.6, gain)


@sound('cutscene/phase2_concierge', -17)
def phase2_concierge():
    """Le Concierge, phase 2 (ConciergePhase) : trois coups de sonnette (0,5 / 0,95 / 1,4 s), un plateau arrive à chaque
    coup, les coupes gèlent (2,2 s) et pétillent."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    for k, at in enumerate((0.5, 0.95, 1.4)):
        track.add(desk_bell(), at, 0.55)
        track.add(whoosh(0.45, 400, 2400), at + 0.02, 0.2)
        track.add(glass_clink(), at + 0.5, 0.35)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0), (ice_crack(0.4), 0, 0.6)), 2.2, 0.8)
    for k in range(3):
        track.add(ice_crack(0.3), 2.2 + rand(0.0, 0.2), 0.3)
    track.add(fizz(1.6), 2.25, 0.5)
    track.add(brass_chord(['C3', 'E3', 'G3', 'B3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_concierge', -17)
def death_concierge():
    """Le Concierge meurt (ConciergeDeath) : ses clés quittent son revers (0,6 / 1,0 s) et se pendent au tableau
    (1,3 / 1,7 s), il s'incline (1,9 s), le givre craque (2,3 à 2,9 s), il vole en éclats (3,1 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.2), 0.0, 1.0)
    for leave in (0.6, 1.0):
        track.add(key_jingle(), leave, 0.5)
        track.add(whoosh(0.6, 500, 2000), leave + 0.05, 0.2)
        track.add(mix((metal_hit(1800, 0.4, 0.6), 0, 1.0), (key_jingle(), 0.02, 0.5)), leave + 0.7, 0.5)
    track.add(rustle(0.4), 1.9, 0.25)                                                       # la révérence
    for k in range(4):
        track.add(ice_crack(0.3), 2.3 + k * 0.2, 0.35 + 0.08 * k)
    track.add(reverb(ice_shatter(), wet=0.3, size=1.4, tail=1.0), 3.1, 1.0)
    track.add(desk_bell(), 3.8, 0.25)                                                      # un dernier « ding », pour la route
    track.add(brass_chord(['A2', 'C3', 'E3', 'G3'], 1.4, 0.5), 3.2, 0.4)
    return track.buf


@sound('cutscene/phase2_imperatrice_givre', -17)
def phase2_imperatrice_givre():
    """L'Impératrice du Givre, phase 2 (EmpressPhase) : le lustre descend et s'allume (0,1 à 0,8 s), la valse des
    danseurs fantômes, la pyramide de coupes (1,3 à 1,9 s) gèle d'un coup (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1500, 0.2), 0.0, 1.0)
    track.add(whoosh(0.7, 200, 1200), 0.1, 0.3)
    track.add(sparkle(0.6, count=16, lo=4000, hi=10000), 0.7, 0.4)
    waltz(0.6, 7, track)
    for k in range(6):
        track.add(glass_clink(rand(2800, 3600)), 1.3 + k * 0.12, 0.3)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0), (ice_crack(0.5), 0, 0.8)), 2.2, 0.8)
    track.add(skate_glide(1.4), 2.3, 0.3)
    track.add(brass_chord(['A2', 'C#3', 'E3', 'G#3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_imperatrice_givre', -17)
def death_imperatrice_givre():
    """L'Impératrice du Givre meurt (EmpressDeath) : elle ruisselle et fond (0,6 à 3,6 s), sa couronne glisse (2,4 s)
    et tombe dans la flaque (3,1 s), des ronds dans l'eau."""
    track = Track(6.6)
    track.add(ambience(5.6, 300, 1500, 0.2), 0.0, 1.0)
    track.add(lp(noise(3.0), 900) * ramp(3.0, 0.1, 1.0, 1.0), 0.6, 0.25)                 # elle ruisselle
    for k in range(18):
        f = rand(900, 1800)
        drip = osc(sweep(f, f * 1.8, 0.06), 0.06, 'sine') * expdec(0.06, 0.02)
        track.add(drip, 0.7 + rand(0.0, 2.9), rand(0.15, 0.35))
    track.add(whoosh(0.5, 800, 3000), 2.4, 0.15)
    track.add(mix((modal(hz('E6'), 1.6, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 0, 0.6), (splash(0.6), 0, 0.5)), 3.1, 0.6)
    track.add(glass_clink(hz('B5')), 3.25, 0.3)
    for k, note in enumerate(['E5', 'C#5', 'A4', 'E4']):
        track.add(music_box(note, 1.4), 3.5 + k * 0.35, 0.3)
    return track.buf


@sound('cutscene/phase2_yeti', -17)
def phase2_yeti():
    """Le Yéti, phase 2 (YetiPhase) : il se frappe la poitrine (0,45 / 0,73 / 1,01 / 1,29 s), inspire, souffle la tempête
    (1,75 s), le givre craque aux coins de l'écran, l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 600, 0.2), 0.0, 1.0)
    for k in range(4):
        track.add(mix((thump(90, 45, 0.3, 0.08), 0, 1.0), (lp(noise(0.12), 500) * expdec(0.12, 0.04), 0, 0.5)), 0.45 + k * 0.28, 0.8)
    track.add(bp(noise(0.45), 400, 1800) * ramp(0.45, 0.0, 1.0, 0.7), 1.3, 0.35)            # il inspire
    roar = lp(osc(vibrato(sweep(110, 80, 1.4), 1.4, rate=7, depth=0.04), 1.4, 'saw'), 900) * adsr(1.4, 0.08, 0.2, 0.7, 0.5)
    track.add(roar, 1.75, 0.45)
    track.add(icy_wind(2.4), 1.75, 0.7)
    for k in range(10):
        track.add(ice_crack(0.3), 1.95 + rand(0.0, 0.6), rand(0.2, 0.4))
    track.add(mix((thump(70, 30, 1.2, 0.35), 0, 1.0), (crack(0.06, 0.01, 1000, 10000), 0, 0.5)), 2.2, 0.9)
    track.add(brass_chord(['D2', 'A2', 'D3', 'F3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_yeti', -17)
def death_yeti():
    """Le Yéti meurt (YetiDeath) : il bâille (0,2 s), se couche (1,0 s), la neige le recouvre (jusqu'à 2,8 s),
    puis il ronfle doucement sous son tas (3,2 / 4,2 / 5,1 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 600, 0.15), 0.0, 1.0)
    yawn = lp(osc(sweep(140, 70, 0.9), 0.9, 'saw'), 700) * adsr(0.9, 0.15, 0.2, 0.7, 0.3)
    track.add(yawn, 0.2, 0.45)
    track.add(mix((thump(70, 35, 0.6, 0.2), 0, 1.0), (lp(noise(1.2), 600) * expdec(1.2, 0.5), 0, 0.6)), 1.6, 0.6)   # il se couche
    track.add(lp(noise(1.8), 1500) * ramp(1.8, 1.0, 0.0, 1.0), 1.0, 0.25)                  # la neige qui tombe
    lullaby = stack(*(osc(hz(note), 3.5, 'sine') for note in ('D4', 'F#4', 'A4')))
    track.add(lullaby * ramp(3.5, 0.0, 1.0, 1.0) * ramp(3.5, 1.0, 0.0, 3.0), 2.4, 0.06)
    for k in range(3):
        track.add(snore(), 3.2 + k * 0.98 - 0.45, 0.7)
    for k, note in enumerate(['A5', 'F#5', 'D5']):
        track.add(music_box(note, 1.6), 2.8 + k * 0.5, 0.25)
    return track.buf


@sound('cutscene/phase2_prince_neiges', -17)
def phase2_prince_neiges():
    """Le Prince des Neiges, phase 2 (PrincePhase) : la valse du bal derrière les portes, les battants grincent et
    claquent (1,5 s), la barre d'or verrouille (1,95 s), ses yeux s'allument (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 1000, 0.2), 0.0, 1.0)
    waltz(0.0, 3, track, gain=0.3)
    track.add(creak(1.1, 140), 0.4, 0.5)
    slam = mix((thump(80, 35, 0.9, 0.25), 0, 1.2), (wood_knock(150), 0, 0.8), (crack(0.05, 0.01, 400, 8000), 0, 0.5))
    track.add(reverb(slam, wet=0.35, size=1.6, tail=1.2), 1.5, 1.0)
    track.add(svf(noise(0.4), sweep(800, 2500, 0.4), q=3.0, mode='bp') * expdec(0.4, 0.2), 1.65, 0.3)   # la barre qui glisse
    track.add(mix((metal_hit(520, 0.6, 0.6), 0, 1.0), (thump(120, 60, 0.3, 0.06), 0, 0.8)), 1.95, 0.7)
    track.add(mix((thump(70, 30, 1.0, 0.3), 0, 1.0)), 2.2, 0.6)
    track.add(modal(hz('B5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 2.2, 0.3)
    track.add(brass_chord(['B2', 'D3', 'F3', 'A3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_prince_neiges', -17)
def death_prince_neiges():
    """Le Prince des Neiges meurt (PrinceDeath) : sa coupe tombe (0,5 s) et se brise (1,05 s), les bougies du lustre
    s'éteignent (1,4 / 1,7 / 2,0 / 2,3 s), il salue, il s'effondre en neige (2,7 s), sa couronne se pose (4,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 1000, 0.15), 0.0, 1.0)
    waltz(0.0, 2, track, gain=0.28)
    track.add(whoosh(0.5, 900, 2500), 0.55, 0.15)
    shatter = mix((crack(0.05, 0.008, 2000, 12000), 0, 1.0), *((glass_clink(rand(3000, 6500)), rand(0.0, 0.25), rand(0.2, 0.45)) for _ in range(6)))
    track.add(shatter, 1.05, 0.8)
    for k in range(4):
        puff = bp(noise(0.22), 400, 2500) * expdec(0.22, 0.07, attack=0.02)
        track.add(puff, 1.4 + k * 0.3, 0.55)
    track.add(rustle(0.5), 1.8, 0.25)                                                       # la révérence
    track.add(lp(noise(1.3), 1200) * ramp(1.3, 1.0, 0.0, 1.0), 2.7, 0.6)                   # il s'effondre en neige
    track.add(thump(90, 40, 0.5, 0.15), 2.75, 0.5)
    track.add(mix((metal_hit(1400, 0.6, 0.5), 0, 1.0)), 4.0, 0.35)
    for k, note in enumerate(['E5', 'C#5', 'A4']):
        track.add(music_box(note, 1.6), 4.1 + k * 0.4, 0.25)
    return track.buf


# ===========================================================================
# Rapide de Minuit : phases et morts des rois (ExpressBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def moth_flutter():
    """Le froissement léger d'un papillon de nuit."""
    d = 0.12
    return bp(noise(d), rand(1500, 2500), rand(4000, 6000)) * (0.5 + 0.5 * np.sin(np.linspace(0, rand(18, 26), n(d)))) * expdec(d, 0.05)


@sound('cutscene/phase2_locomotive', -17)
def phase2_locomotive():
    """La Locomotive, phase 2 (EnginePhase) : la pression monte (les aiguilles s'affolent, la chaudière cogne de
    0,6 à 1,85 s), des jets de vapeur, la soupape lâche à l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 600, 0.2), 0.0, 1.0)
    track.add(rattle(2.2, 6, 14, 300, 1500), 0.0, 0.4)
    track.add(lp(noise(2.0), 200) * ramp(2.0, 0.2, 1.0, 1.0), 0.2, 0.6)
    for k in range(6):
        track.add(mix((metal_hit(180 + 30 * k, 0.4, 0.5), 0, 0.6), (thump(90, 40, 0.2, 0.05), 0, 0.8)), 0.6 + k * 0.25, 0.4 + 0.06 * k)
    for k in range(7):
        track.add(hiss(0.5), 0.8 + rand(0.0, 1.3), rand(0.2, 0.4))
    track.add(mix((hiss(2.0), 0, 1.0), (steam_whistle(1.8), 0, 0.8)), 2.2, 0.8)
    track.add(thump(70, 30, 1.0, 0.3), 2.2, 0.8)
    track.add(brass_chord(['E2', 'B2', 'E3', 'G3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_locomotive', -17)
def death_locomotive():
    """La Locomotive meurt (EngineDeath) : elle fonce sur les rails, un rail se tord (1,2 s), le chasse-buffles
    s'arrache (1,6 s) et glisse, elle bascule et s'écrase (2,5 s), elle siffle et s'éteint (3,6 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 600, 0.2), 0.0, 1.0)
    track.add(rattle(1.8, 10, 18, 300, 1500), 0.0, 0.6)
    track.add(screech(1.0, 3200), 1.2, 0.45)
    track.add(mix((metal_hit(300, 0.8, 0.8), 0, 1.0), (crack(0.05, 0.01, 600, 8000), 0, 0.7)), 1.6, 0.6)
    track.add(screech(1.2, 2200), 1.7, 0.35)                                                # le chasse-buffles qui glisse
    crash = mix((thump(60, 25, 1.6, 0.5), 0, 1.3), (crack(0.1, 0.02, 300, 7000), 0, 0.8),
                (metal_hit(140, 1.2, 0.6), 0, 0.5), (lp(noise(1.5), 1200) * expdec(1.5, 0.5), 0, 0.8))
    track.add(reverb(crash, wet=0.3, size=1.6, tail=1.2), 2.5, 1.0)
    track.add(hiss(2.2), 2.7, 0.5)
    whistle = osc(sweep(hz('A5'), hz('A4'), 1.4), 1.4, 'sine') * expdec(1.4, 0.8, attack=0.05)
    track.add(whistle, 3.4, 0.15)
    track.add(mix((metal_hit(900, 0.3, 0.4), 0, 1.0)), 3.6, 0.25)                         # la lanterne s'éteint
    return track.buf


@sound('cutscene/phase2_grand_chef', -17)
def phase2_grand_chef():
    """Le Grand Chef, phase 2 (ChefPhase) : trois poêles s'enflamment (0,5 / 0,9 / 1,3 s), les couteaux tournoient,
    ses crocs (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    for at in (0.5, 0.9, 1.3):
        track.add(fire_burst(0.9), at, 0.5)
        track.add(metal_hit(700, 0.4, 0.6), at - 0.05, 0.3)
    for k in range(8):
        track.add(blade_swish(), 1.2 + k * 0.12, 0.3)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0), (blade_ring(2800, 0.8), 0, 0.5)), 2.2, 0.8)
    track.add(brass_chord(['C3', 'E3', 'G#3', 'B3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_grand_chef', -17)
def death_grand_chef():
    """Le Grand Chef meurt (ChefDeath) : il soupire, sa toque va sur la table (0,7 à 1,6 s), sa louche (1,3 à 2,2 s),
    puis il s'en va d'un pas lourd (2,4 à 4,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.2), 0.0, 1.0)
    sigh = bp(noise(0.8), 300, 1400) * adsr(0.8, 0.2, 0.2, 0.6, 0.4)
    track.add(sigh, 0.1, 0.45)
    track.add(rustle(0.4), 0.7, 0.3)
    track.add(mix((thump(160, 80, 0.15, 0.04), 0, 0.6), (rustle(0.2), 0, 0.5)), 1.6, 0.4)
    track.add(mix((metal_hit(1500, 0.6, 0.6), 0, 1.0), (glass_clink(2200), 0.02, 0.4)), 2.2, 0.45)
    for k in range(6):
        track.add(footstep(rand(140, 170), 1.0), 2.45 + k * 0.27, 0.6 - 0.07 * k)
    track.add(mix((wood_knock(160), 0, 1.0), (thump(100, 50, 0.3, 0.08), 0, 0.6)), 4.1, 0.35)   # la porte du wagon
    for k, note in enumerate(['G4', 'E4', 'C4']):
        track.add(music_box(note, 1.4), 4.3 + k * 0.4, 0.25)
    return track.buf


@sound('cutscene/phase2_dame_en_noir', -17)
def phase2_dame_en_noir():
    """La Dame en noir, phase 2 (LadyPhase) : les lampes faiblissent, la voilette se lève (0,6 à 1,7 s), ses yeux
    s'allument, la brume verte, l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    for k in range(5):
        track.add(click(0.01, 1000, 5000), 0.2 + k * 0.18 + rand(0.0, 0.05), 0.25)            # les lampes qui grésillent
    track.add(rustle(1.1), 0.6, 0.35)
    track.add(modal(hz('F#5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 1.6, 0.3)
    track.add(svf(noise(2.5), sweep(300, 900, 2.5), q=1.2) * ramp(2.5, 0.0, 1.0, 1.0), 1.0, 0.3)   # la brume
    track.add(mix((thump(70, 30, 1.0, 0.3), 0, 1.0)), 2.2, 0.7)
    track.add(brass_chord(['F#2', 'A2', 'C3', 'E3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_dame_en_noir', -17)
def death_dame_en_noir():
    """La Dame en noir meurt (LadyDeath) : les lampes s'éteignent (0,3 / 0,75 / 1,2 s), la rose tombe (0,9 à 1,7 s),
    elle se défait en papillons de nuit (2,5 s) qui s'envolent par la fenêtre (3,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.15), 0.0, 1.0)
    for at in (0.3, 0.75, 1.2):
        track.add(mix((click(0.01, 800, 4000), 0, 1.0), (metal_hit(1200, 0.2, 0.4), 0, 0.3)), at, 0.35)
    track.add(mix((pluck(hz('D5'), 0.5), 0, 0.6), (rustle(0.2), 0, 0.5)), 1.7, 0.3)           # la rose sur le tapis
    track.add(whoosh(0.8, 400, 2400), 2.4, 0.3)
    for k in range(40):
        track.add(moth_flutter(), 2.5 + rand(0.0, 2.0), rand(0.1, 0.25))
    for k, note in enumerate(['D5', 'A4', 'F4', 'D4']):
        track.add(music_box(note, 1.6), 2.6 + k * 0.45, 0.25)
    return track.buf


@sound('cutscene/phase2_desperado', -17)
def phase2_desperado():
    """Le Desperado, phase 2 (OutlawPhase) : trois coups de feu vers le toit (0,5 / 0,85 / 1,2 s), deux grappins se
    plantent (1,35 / 1,51 s), ses complices descendent en se balançant et atterrissent (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 700, 0.2), 0.0, 1.0)
    track.add(rattle(4.5, 4, 10, 300, 1500), 0.0, 0.25)
    for at in (0.5, 0.85, 1.2):
        track.add(reverb(gunshot(), wet=0.25, size=1.0, tail=0.6), at, 0.8)
        track.add(metal_hit(2400, 0.3, 0.6), at + 0.05, 0.2)                                    # la balle dans le toit
    for at in (1.35, 1.51):
        track.add(whoosh(0.3, 800, 3000), at - 0.25, 0.3)
        track.add(mix((metal_hit(800, 0.5, 0.8), 0, 1.0), (crack(0.03, 0.005, 1000, 8000), 0, 0.5)), at, 0.5)
    for k in range(2):
        track.add(whoosh(0.6, 200, 1200), 1.6 + 0.05 * k, 0.35)
    track.add(mix((thump(80, 35, 0.8, 0.25), 0, 1.0), (footstep(150), 0.04, 0.8), (footstep(170), 0.09, 0.8)), 2.2, 0.8)
    track.add(brass_chord(['G2', 'D3', 'G3', 'A#3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_desperado', -17)
def death_desperado():
    """Le Desperado meurt (OutlawDeath) : une balle fait sauter son chapeau (0,6 s), qui retombe (1,4 s) ; la porte du
    fourgon glisse (1,0 à 1,6 s), le vent de la nuit et le train qui roule ; il recule et saute (2,5 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 700, 0.2), 0.0, 1.0)
    track.add(rattle(6.0, 4, 10, 300, 1500), 0.0, 0.3)
    track.add(reverb(gunshot(), wet=0.25, size=1.0, tail=0.6), 0.6, 0.8)
    track.add(mix((thump(140, 70, 0.12, 0.03), 0, 0.6), (rustle(0.15), 0, 0.5)), 1.4, 0.35)
    door = svf(noise(0.6), sweep(300, 900, 0.6), q=2.0) * adsr(0.6, 0.05, 0.1, 0.8, 0.1)
    track.add(mix((door, 0, 0.6), (metal_hit(220, 0.6, 0.5), 0.55, 0.5)), 1.0, 0.6)
    wind = bp(noise(4.5), 300, 2500) * ramp(4.5, 0.0, 1.0, 0.5)
    track.add(wind, 1.3, 0.35)
    track.add(rattle(4.5, 14, 18, 400, 2000), 1.3, 0.4)                                     # le train, plus fort porte ouverte
    for k in range(3):
        track.add(footstep(160), 1.6 + k * 0.28, 0.4)
    track.add(whoosh(0.9, 300, 1800), 2.5, 0.5)
    track.add(steam_whistle(1.8), 3.4, 0.35)
    return track.buf


# ===========================================================================
# Temple du Destin : phases et morts des rois (TempleBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def golden_thread(d=0.6, f=1800):
    """Un fil d'or qui se tend : une corde pincée, longue et claire."""
    return mix((pluck(f, d), 0, 0.7), (modal(f * 2, d, GLASS_RATIOS, [0.5, 0.3, 0.2, 0.1]), 0, 0.3))


def sand_pour(d):
    """Du sable qui coule et s'écroule : un souffle grainé."""
    grains = bp(noise(d), 1500, 6000) * (np.random.rand(n(d)) > 0.93)
    return mix((lp(noise(d), 900) * 0.5, 0, 1.0), (grains, 0, 0.8))


@sound('cutscene/phase2_maitre_cloches', -17)
def phase2_maitre_cloches():
    """Le Maître des cloches, phase 2 (BellPhase) : il tire la corde du grand bourdon, quatre coups (0,5 / 0,95 /
    1,4 / 1,85 s) qui tanguent et se dédoublent, l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1200, 0.25), 0.0, 1.0)
    for k, at in enumerate((0.5, 0.95, 1.4, 1.85)):
        track.add(rustle(0.15), at - 0.15, 0.25)
        toll = bell_toll(hz('D3') * (1.0 - 0.02 * k), 2.4)
        track.add(toll, at, 0.45)
        track.add(toll, at + 0.07 + 0.03 * k, 0.2)                                              # l'écho dédoublé, l'ivresse
    track.add(mix((temple_gong(3.0), 0, 1.0), (thump(70, 30, 1.0, 0.3), 0, 0.6)), 2.2, 0.7)
    track.add(brass_chord(['D2', 'A2', 'D3', 'F3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_maitre_cloches', -17)
def death_maitre_cloches():
    """Le Maître des cloches meurt (BellDeath) : un dernier coup (0,6 s) dont l'écho s'éteint ; il se fige en bronze,
    le bourdon de sa tête se fend et tombe (2,9 s), roule et s'arrête, muet (3,9 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 300, 1200, 0.2), 0.0, 1.0)
    track.add(rustle(0.2), 0.4, 0.25)
    track.add(reverb(bell_toll(hz('D3'), 3.5), wet=0.5, size=2.0, tail=2.0), 0.6, 0.6)
    for k in range(4):
        track.add(metal_hit(600 - 60 * k, 0.4, 0.4), 1.1 + k * 0.5, 0.12 - 0.02 * k)            # il se fige, un peu plus à chaque écho
    track.add(crack(0.15, 0.03, 400, 6000), 2.3, 0.3)
    track.add(mix((metal_hit(320, 0.6, 0.6), 0, 1.0), (thump(140, 60, 0.2, 0.06), 0, 0.6)), 3.1, 0.5)
    for k in range(5):
        track.add(metal_hit(380 + 20 * k, 0.15, 0.4), 3.25 + k * 0.13 * (1 - k * 0.1), 0.25 - 0.04 * k)   # il roule
    track.add(thump(120, 60, 0.2, 0.05), 3.9, 0.3)
    return track.buf


@sound('cutscene/phase2_tisseuse', -17)
def phase2_tisseuse():
    """La Tisseuse de destins, phase 2 (WeaverPhase) : des fils d'or jaillissent vers les bords de l'écran (de 0,6 s),
    ses yeux-bobines s'allument, l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 400, 1500, 0.2), 0.0, 1.0)
    notes = ['A4', 'C5', 'E5', 'G5', 'A5', 'C6', 'E6', 'G6']
    for k, note in enumerate(notes):
        track.add(whoosh(0.25, 1500, 5000), 0.55 + k * 0.18, 0.15)
        track.add(golden_thread(0.8, hz(note)), 0.6 + k * 0.18, 0.25)
    track.add(modal(hz('E6'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 1.5, 0.2)
    track.add(mix((thump(70, 30, 1.0, 0.3), 0, 1.0), (golden_thread(1.5, hz('A3')), 0, 0.6)), 2.2, 0.7)
    track.add(brass_chord(['A2', 'E3', 'A3', 'C4'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_tisseuse', -17)
def death_tisseuse():
    """La Tisseuse meurt (WeaverDeath) : les ciseaux du destin coupent ses cinq fils (0,5 s puis tous les 0,35 s),
    puis elle se détricote, rang après rang, en brins d'or (2,2 à 4,2 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 400, 1500, 0.15), 0.0, 1.0)
    for k in range(5):
        at = 0.5 + k * 0.35
        track.add(snip(), at, 0.6)
        track.add(golden_thread(0.5, hz(['E5', 'C5', 'A4', 'E4', 'A3'][k])), at + 0.01, 0.35)  # le fil qui lâche, de plus en plus grave
    track.add(whoosh(0.8, 300, 2000), 2.2, 0.3)
    for k in range(18):
        track.add(golden_thread(0.4, rand(1200, 3200)), 2.2 + k * 0.1 + rand(0.0, 0.05), 0.12)
    for k, note in enumerate(['E5', 'C5', 'A4']):
        track.add(music_box(note, 1.6), 3.9 + k * 0.4, 0.2)
    return track.buf


@sound('cutscene/phase2_seigneur_des', -17)
def phase2_seigneur_des():
    """Le Seigneur des Dés, phase 2 (DicePhase) : deux dés géants tombent du ciel, rebondissent (1,2 / 1,4 s) et
    s'arrêtent sur six ; il grandit, rouge (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    track.add(whoosh(0.7, 300, 2000), 0.5, 0.35)
    for at in (1.2, 1.4):
        track.add(mix((thump(90, 40, 0.4, 0.1), 0, 1.0), (wood_knock(300), 0, 0.6)), at, 0.7)
        track.add(dice_roll(0.5, 5), at + 0.08, 0.4)
    track.add(mix((thump(70, 30, 1.0, 0.3), 0, 1.0), (temple_gong(2.5), 0, 0.5)), 2.2, 0.7)
    track.add(brass_chord(['C2', 'G2', 'C3', 'D#3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_seigneur_des', -17)
def death_seigneur_des():
    """Le Seigneur des Dés meurt (DiceDeath) : il chancelle, ses deux dés d'or tombent de sa couronne (0,8 s) et
    roulent seuls sur le damier pendant qu'il s'effondre dans ses robes (1,6 à 3,1 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.15), 0.0, 1.0)
    groan = bp(noise(0.8), 200, 900) * adsr(0.8, 0.2, 0.2, 0.6, 0.4)
    track.add(groan, 0.1, 0.4)
    for k in range(2):
        track.add(click(0.02, 1500, 5000), 0.8 + k * 0.12, 0.4)
        for b in range(4):
            track.add(mix((wood_knock(rand(500, 700)), 0, 0.8), (click(0.015, 1500, 5000), 0, 0.6)),
                      0.8 + 0.5 + k * 0.12 + b * 0.55, 0.45 - 0.08 * b)
        track.add(dice_roll(2.5, 10), 1.4 + k * 0.12, 0.2)
    track.add(rustle(1.2), 1.6, 0.4)
    track.add(thump(80, 35, 0.8, 0.25), 2.5, 0.6)
    return track.buf


@sound('cutscene/phase2_idole_or', -17)
def phase2_idole_or():
    """L'Idole d'Or, phase 2 (IdolPhase) : elle s'éveille (0,5 s), deux miroirs d'or se dressent, un éclat les balaie
    et montre ta main à l'envers, son troisième oeil s'ouvre, l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1200, 0.2), 0.0, 1.0)
    track.add(mix((thump(90, 40, 0.6, 0.2), 0, 1.0), (metal_hit(500, 0.8, 0.5), 0, 0.5)), 0.5, 0.6)
    track.add(svf(noise(0.6), sweep(200, 2000, 0.6), q=2.0) * ramp(0.6, 0.0, 1.0, 0.3), 0.4, 0.3)
    track.add(mix((sparkle(1.2), 0, 1.0), (modal(hz('B5'), 1.6, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 0, 0.6)), 1.1, 0.35)
    track.add(modal(hz('F#6'), 1.4, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 1.6, 0.25)
    track.add(mix((thump(70, 30, 1.0, 0.3), 0, 1.0), (temple_gong(2.5), 0, 0.5)), 2.2, 0.7)
    track.add(brass_chord(['B2', 'F#3', 'B3', 'D4'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_idole_or', -17)
def death_idole_or():
    """L'Idole d'Or meurt (IdolDeath) : son or se ternit et se fend (0,6 à 1,9 s), puis elle s'effrite grain à grain
    du haut vers le bas (1,9 s) en un tas de sable (jusqu'à 4,2 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 300, 1200, 0.15), 0.0, 1.0)
    for k in range(3):
        track.add(crack(0.12, 0.02, 400, 6000), 0.6 + k * 0.3, 0.35 + 0.1 * k)
    track.add(lp(noise(1.3), 300) * ramp(1.3, 0.0, 1.0, 1.0), 0.6, 0.2)
    track.add(mix((thump(80, 35, 0.8, 0.3), 0, 1.0), (crack(0.2, 0.04, 300, 5000), 0, 0.6)), 1.9, 0.6)
    track.add(sand_pour(2.4) * ramp(2.4, 1.0, 0.4, 1.0), 1.95, 0.5)
    for k, note in enumerate(['B4', 'F#4', 'D4']):
        track.add(music_box(note, 1.6), 4.2 + k * 0.4, 0.2)
    return track.buf


# ===========================================================================
# Fête foraine : phases et morts des rois (FairBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def fade(buf, a, b):
    """Le son buf, dont le volume passe de a à b."""
    return buf * np.linspace(a, b, len(buf))


def crowd_boo(d):
    """La foule qui hue : des « hou » graves et tremblés, mêlés."""
    track = Track(d)
    for k in range(9):
        f = rand(110, 210)
        voice = bp(osc(vibrato(f, d, rand(4, 7), 0.03), d, 'saw'), 250, 900) * adsr(d, rand(0.1, 0.4), 0.2, 0.8, 0.5)
        track.add(voice, rand(0.0, 0.2), 0.18)
    return track.buf


def splat():
    """Une tomate qui s'écrase : « splotch » mou et mouillé."""
    return mix((lp(noise(0.12), 1400) * expdec(0.12, 0.04), 0, 1.0), (thump(180, 90, 0.08, 0.02), 0, 0.6),
               (bp(noise(0.2), 2000, 6000) * expdec(0.2, 0.06), 0.02, 0.3))


def ghost_wail(d=0.9, f=520):
    """Le « hououou » d'un fantôme de foire : une plainte qui glisse et tremble."""
    return osc(vibrato(sweep(f, f * 1.6, d), d, 6.0, 0.03), d, 'sine') * adsr(d, 0.15, 0.2, 0.7, 0.35)


def can_tumble(count=9, d=1.0):
    """Des boîtes de conserve qui dégringolent et roulent."""
    track = Track(d + 0.4)
    for k in range(count):
        at = rand(0.0, d * 0.7)
        track.add(metal_hit(rand(500, 900), 0.25, 0.5), at, rand(0.3, 0.6))
        track.add(metal_hit(rand(600, 1000), 0.2, 0.4), at + rand(0.1, 0.3), rand(0.15, 0.3))
    return track.buf


@sound('cutscene/phase2_monsieur_loyal', -17)
def phase2_monsieur_loyal():
    """Monsieur Loyal, phase 2 (LoyalPhase) : roulement de tambour (0,2 à 1,1 s), il arrache son habit (1,2 s),
    ses massues s'enflamment (0,8 s), elles deviennent quatre As à l'apogée (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1400, 0.25), 0.0, 1.0)
    for k in range(12):
        track.add(mix((thump(rand(150, 190), 90, 0.06, 0.02), 0, 0.8), (bp(noise(0.05), 1500, 5000) * expdec(0.05, 0.015), 0, 0.5)),
                  0.2 + k * 0.08, 0.35 + 0.04 * k)
    track.add(mix((whoosh(0.3, 600, 3000), 0, 0.6), (rustle(0.25), 0, 0.6)), 1.15, 0.6)
    track.add(sparkle(1.0), 1.2, 0.35)
    track.add(fire_burst(1.4), 0.8, 0.35)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0), (card_slap(0.18), 0, 0.8)), 2.2, 0.8)
    track.add(brass_chord(['C3', 'G3', 'C4', 'E4', 'G4'], 1.6, 0.6), 2.25, 0.55)
    track.add(calliope(['C5', 'E5', 'G5', 'C6'], 0.12), 2.4, 0.3)
    return track.buf


@sound('cutscene/death_monsieur_loyal', -17)
def death_monsieur_loyal():
    """Monsieur Loyal meurt (LoyalDeath) : le projecteur grésille, son fouet l'enroule (0,5 à 1,4 s), dernier salut,
    le rideau tombe (2,1 à 2,5 s) puis se relève (3,3 à 3,9 s) : son chapeau, et le lapin en sort (4,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 300, 1400, 0.2), 0.0, 1.0)
    for k in range(14):
        track.add(click(0.01, 1500, 6000), 0.1 + k * 0.17 + rand(0.0, 0.05), 0.2)                 # le projecteur grésille
    track.add(whip_crack(), 0.4, 0.6)
    track.add(rustle(0.9), 0.5, 0.4)
    track.add(mix((whoosh(0.45, 200, 1200), 0, 0.8), (thump(90, 40, 0.5, 0.15), 0.4, 1.0)), 2.05, 0.7)
    track.add(rustle(0.6), 3.3, 0.35)
    track.add(mix((osc(sweep(400, 1200, 0.12), 0.12, 'sine') * expdec(0.12, 0.05), 0, 0.8), (sparkle(0.8), 0.02, 0.5)), 4.0, 0.5)
    track.add(calliope(['G4', 'E4', 'C4', 'G3'], 0.22), 4.3, 0.3)
    return track.buf


@sound('cutscene/phase2_conducteur', -17)
def phase2_conducteur():
    """Le Conducteur, phase 2 (ConductorPhase) : son wagonnet sort du tunnel (0 à 1,2 s), freine contre le heurtoir
    (1,2 s), tout s'éteint (1,35 s) et les fantômes jaillissent, il flambe en bleu (2,2 s), poinçonne (3,1 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 700, 0.2), 0.0, 1.0)
    track.add(fade(rattle(1.2, 8, 14, 300, 1500, wood=True), 0.3, 1.0), 0.0, 0.6)
    track.add(screech(0.4, 3000), 0.95, 0.4)
    track.add(mix((impact(0.4, 150, 50), 0, 1.0), (metal_hit(400, 0.5, 0.6), 0, 0.5)), 1.2, 0.8)
    track.add(click(0.02, 400, 2000), 1.35, 0.6)
    for k in range(4):
        track.add(ghost_wail(0.9, rand(420, 640)), 1.45 + k * 0.15, 0.25)
        track.add(whoosh(0.5, 400, 2500), 1.45 + k * 0.15, 0.2)
    track.add(mix((fire_burst(1.2), 0, 0.7), (thump(70, 30, 1.0, 0.3), 0, 0.8)), 2.2, 0.7)
    track.add(brass_chord(['A2', 'E3', 'A3', 'C4'], 1.6, 0.6), 2.25, 0.45)
    track.add(mix((click(0.01, 2000, 8000), 0, 1.0), (metal_hit(1800, 0.2, 0.5), 0, 0.5)), 3.1, 0.5)
    return track.buf


@sound('cutscene/death_conducteur', -17)
def death_conducteur():
    """Le Conducteur meurt (ConductorDeath) : sa lanterne s'éteint (0,2 à 0,5 s), les freins lâchent (0,9 s), le
    wagonnet repart à reculons, sa casquette s'envole (1,5 s) et retombe (2,6 s), le tunnel l'avale (3,3 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 700, 0.2), 0.0, 1.0)
    track.add(mix((hiss(0.4), 0, 0.6), (click(0.01, 800, 3000), 0.3, 0.6)), 0.2, 0.4)          # la lanterne s'éteint
    track.add(mix((metal_hit(300, 0.6, 0.6), 0, 1.0), (click(0.02, 1000, 5000), 0, 0.6)), 0.9, 0.6)
    track.add(fade(screech(1.5, 2600), 1.0, 0.2), 0.9, 0.3)
    track.add(fade(rattle(2.4, 6, 14, 300, 1500, wood=True), 1.0, 0.1), 1.0, 0.5)
    track.add(whoosh(0.6, 500, 2500), 1.5, 0.35)
    track.add(mix((thump(150, 80, 0.12, 0.03), 0, 0.6), (rustle(0.15), 0, 0.5)), 2.6, 0.35)
    track.add(reverb(mix((impact(0.6, 90, 35), 0, 1.0), (ghost_wail(1.4, 380), 0.1, 0.5)), wet=0.5, size=2.0, tail=1.5), 3.3, 0.6)
    track.add(hiss(1.2), 3.4, 0.25)
    return track.buf


@sound('cutscene/phase2_roue_fortune', -17)
def phase2_roue_fortune():
    """La Roue de la Fortune, phase 2 (WheelPhase) : sa roue tourne de plus en plus vite, ses nacelles se détachent
    (1,0 à 1,8 s), toutes ses cases deviennent des épées et sa flèche une lame (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 400, 1600, 0.2), 0.0, 1.0)
    t = 0.0
    while t < 2.2:                                                                              # le cliquet qui s'emballe
        track.add(ratchet(rand(1800, 2200)), t, 0.35)
        t += max(0.03, 0.3 - 0.13 * t)
    track.add(whoosh(1.0, 300, 2000), 1.0, 0.35)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0), (blade_ring(2600, 0.9), 0, 0.6)), 2.2, 0.8)
    track.add(brass_chord(['G2', 'D3', 'G3', 'A#3'], 1.6, 0.6), 2.25, 0.5)
    return track.buf


@sound('cutscene/death_roue_fortune', -17)
def death_roue_fortune():
    """La Roue de la Fortune meurt (WheelDeath) : la foule hue et la bombarde de tomates (lancées de 0,15 à 2,05 s,
    elles s'écrasent 0,45 s plus tard) pendant qu'elle ralentit ; la dernière, énorme, la fait voler en éclats (2,5 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 400, 1600, 0.15), 0.0, 1.0)
    track.add(crowd_boo(2.8), 0.0, 0.6)
    for at in (0.15, 0.45, 0.7, 0.95, 1.15, 1.35, 1.5, 1.65, 1.8, 1.95, 2.05):
        track.add(splat(), at + 0.45, rand(0.35, 0.55))
    t = 0.0
    while t < 2.5:                                                                              # elle ralentit
        track.add(ratchet(rand(1800, 2200)), t, 0.2)
        t += 0.05 + 0.12 * t
    track.add(whoosh(0.5, 200, 1200), 2.0, 0.4)
    track.add(mix((wood_break(), 0, 1.0), (impact(0.6, 120, 40), 0, 1.0), (splat(), 0, 1.0), (crack(0.15, 0.03, 500, 8000), 0, 0.6)), 2.5, 0.9)
    track.add(can_tumble(10, 1.4), 2.6, 0.3)
    track.add(crowd_boo(1.6), 2.7, 0.35)
    return track.buf


@sound('cutscene/phase2_roi_forain', -17)
def phase2_roi_forain():
    """Le Roi des Forains, phase 2 (FairKingPhase) : deux balles sur son chamboule-tout (impacts 0,7 / 1,35 s) qui
    rebondissent sans rien faire tomber, les boulons rougissent (1,5 s), les peluches ouvrent l'oeil (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1200, 0.2), 0.0, 1.0)
    for at in (0.7, 1.35):
        track.add(whoosh(0.35, 500, 2500), at - 0.35, 0.3)
        track.add(mix((metal_hit(700, 0.5, 0.7), 0, 1.0), (thump(200, 100, 0.08, 0.02), 0, 0.5)), at, 0.6)
    for k in range(3):
        track.add(ratchet(rand(900, 1200)), 1.5 + k * 0.12, 0.4)
    track.add(mix((thump(80, 35, 1.0, 0.3), 0, 1.0)), 2.2, 0.8)
    track.add(brass_chord(['C3', 'D#3', 'G3', 'A#3'], 1.6, 0.6), 2.25, 0.45)
    track.add(calliope(['E5', 'D#5', 'D5', 'C#5'], 0.14), 2.5, 0.3)
    return track.buf


@sound('cutscene/death_roi_forain', -17)
def death_roi_forain():
    """Le Roi des Forains meurt (FairKingDeath) : sa balle rate (0,3 s), le chamboule-tout s'écroule tout seul (1,1 s),
    ses manches se vident de cartes (1,5 s), sa carte truquée tombe (2,0 s), sa couronne roule (2,5 s), le ticket doré (3,4 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 300, 1200, 0.15), 0.0, 1.0)
    track.add(whoosh(0.6, 400, 2000), 0.3, 0.35)
    track.add(can_tumble(9, 1.0), 1.1, 0.55)
    for k in range(8):
        track.add(card_flick(0.06), 1.5 + k * 0.06, 0.35)
    for k in range(6):
        track.add(card_slap(0.14), 1.9 + k * 0.12 + rand(0.0, 0.05), 0.2)
    track.add(card_slap(0.18), 2.7, 0.35)
    track.add(mix((metal_hit(900, 0.4, 0.6), 0, 1.0), (metal_hit(1300, 0.3, 0.5), 0.15, 0.6), (metal_hit(1100, 0.3, 0.5), 0.35, 0.4)), 3.05, 0.5)
    track.add(mix((sparkle(1.4), 0, 1.0)), 3.4, 0.35)
    track.add(calliope(['C5', 'B4', 'A#4', 'A4'], 0.25), 3.6, 0.3)
    return track.buf


# ===========================================================================
# Désert de Braise : phases et morts des rois (DesertBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def stone_slide(d=0.4):
    """Un lourd couvercle de pierre qui glisse : un frottement grave et grainé."""
    return lp(noise(d), 700) * adsr(d, 0.05, 0.1, 0.8, 0.1) + 0.4 * bp(noise(d), 1500, 4000) * adsr(d, 0.05, 0.1, 0.4, 0.1)


def quill_scratch(d=0.25):
    """Une plume qui gratte le parchemin."""
    return bp(noise(d), 3000, 8000) * (0.5 + 0.5 * np.sin(np.linspace(0, 40, n(d)))) * adsr(d, 0.02, 0.05, 0.6, 0.05)


@sound('cutscene/phase2_emir', -17)
def phase2_emir():
    """L'Émir, phase 2 (EmirPhase) : la tempête de sable se lève (0,2 à 1,0 s), le contrat se déchire en créances
    (1,0 s) qui tournent, son ombrelle devient un cimeterre (2,2 s)."""
    track = Track(5.8)
    track.add(fade(desert_wind(4.8), 0.3, 1.0), 0.0, 0.8)
    track.add(mix((rustle(0.3), 0, 1.0), (crack(0.05, 0.01, 2000, 8000), 0.05, 0.5)), 1.0, 0.6)  # le contrat déchiré
    for k in range(10):
        track.add(rustle(0.12), 1.2 + k * 0.09, 0.2)
    track.add(mix((blade_swish(), 0, 1.0), (blade_ring(2600, 1.0), 0.05, 0.6)), 2.2, 0.8)
    track.add(brass_chord(['D3', 'F3', 'G#3', 'C4'], 1.6, 0.6), 2.25, 0.45)
    return track.buf


@sound('cutscene/death_emir', -17)
def death_emir():
    """L'Émir meurt (EmirDeath) : la tempête tombe (0 à 1,2 s), les créances retombent et brûlent en pièces d'or
    (2,5 s), il s'enfonce dans la dune (2,5 à 3,8 s), son ombrelle reste (3,6 s), les chameaux s'en vont (3,9 s)."""
    track = Track(6.6)
    track.add(fade(desert_wind(2.0), 1.0, 0.0), 0.0, 0.7)
    for k in range(8):
        track.add(rustle(0.1), 0.4 + k * 0.25, 0.15)
    track.add(mix((fire_burst(0.8), 0, 0.5), (coin_shower(1.2, 10), 0.1, 0.6)), 2.5, 0.6)
    track.add(sand_pour(1.3), 2.5, 0.45)
    track.add(thump(140, 70, 0.15, 0.04), 3.6, 0.4)
    for k in range(6):                                                                         # les chameaux qui s'en vont
        track.add(footstep(rand(110, 140), 1.0), 3.9 + k * 0.28, 0.3 - 0.04 * k)
    return track.buf


@sound('cutscene/phase2_pharaon', -17)
def phase2_pharaon():
    """Le Pharaon, phase 2 (PharaohPhase) : des rouleaux sur le couvercle (0,4 s), il tire le levier (0,85 s), les
    rouleaux s'arrêtent (1,45 / 1,65 / 1,85 s), le couvercle se ferme sur lui (1,95 à 2,2 s), le venin suinte,
    le couvercle se rouvre (3,3 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    track.add(sparkle(0.6), 0.4, 0.3)
    track.add(mix((ratchet(900), 0, 1.0), (metal_hit(500, 0.3, 0.4), 0.1, 0.5)), 1.1, 0.5)
    track.add(rattle(0.8, 22, 6, 1800, 4200), 1.1, 0.4)
    for at in (1.45, 1.65, 1.85):
        track.add(mix((thump(180, 90, 0.1, 0.03), 0, 0.8), (click(0.02, 1500, 5000), 0, 0.6)), at, 0.5)
    track.add(stone_slide(0.3), 1.95, 0.7)
    track.add(reverb(mix((impact(0.6, 100, 40), 0, 1.0), (crack(0.1, 0.02, 300, 5000), 0, 0.5)), wet=0.4, size=1.6, tail=1.0), 2.2, 0.9)
    track.add(svf(noise(1.0), sweep(300, 900, 1.0), q=2.0) * adsr(1.0, 0.2, 0.2, 0.6, 0.3), 2.4, 0.3)   # le venin qui suinte
    track.add(brass_chord(['E2', 'B2', 'E3', 'G3'], 1.6, 0.6), 2.25, 0.4)
    track.add(stone_slide(0.5), 3.3, 0.6)
    return track.buf


@sound('cutscene/death_pharaon', -17)
def death_pharaon():
    """Le Pharaon meurt (PharaohDeath) : il se fend (0,8 à 2,5 s), part en poussière (2,5 s), le couvercle glisse
    et se ferme sur le vide (3,9 à 4,25 s), les torches s'éteignent (4,3 à 5,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.2), 0.0, 1.0)
    for k in range(4):
        track.add(crack(0.12, 0.02, 400, 6000), 0.8 + k * 0.4, 0.3 + 0.08 * k)
    track.add(sand_pour(1.6), 2.5, 0.6)
    track.add(desert_wind(1.6), 2.5, 0.3)
    track.add(stone_slide(0.4), 3.9, 0.7)
    track.add(reverb(impact(0.6, 100, 40), wet=0.4, size=1.6, tail=1.0), 4.25, 0.9)
    for k in range(4):
        track.add(hiss(0.3), 4.3 + k * 0.2, 0.25)
    return track.buf


@sound('cutscene/phase2_djinn', -17)
def phase2_djinn():
    """Le Djinn, phase 2 (DjinnPhase) : la lampe fume, il s'élève, trois étoiles de voeu (0,6 s), le parchemin se
    déroule (1,0 s), la plume écrit (1,4 à 2,1 s), le sceau s'écrase (2,2 s), la lettre s'envole (2,9 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 400, 1500, 0.2), 0.0, 1.0)
    track.add(fade(hiss(1.2), 0.2, 1.0), 0.0, 0.3)
    track.add(mix((sparkle(1.0), 0, 1.0), (music_box('E6', 1.0), 0, 0.4), (music_box('B6', 1.0), 0.15, 0.4)), 0.6, 0.4)
    track.add(mix((rustle(0.4), 0, 1.0), (whoosh(0.4, 300, 1500), 0, 0.5)), 1.0, 0.5)
    for k in range(7):
        track.add(quill_scratch(0.09), 1.4 + k * 0.1, 0.35)
    track.add(mix((thump(90, 40, 0.5, 0.15), 0, 1.0), (card_slap(0.18), 0, 0.6)), 2.2, 0.8)
    track.add(brass_chord(['A2', 'E3', 'A3', 'C#4'], 1.6, 0.6), 2.25, 0.4)
    track.add(mix((whoosh(0.7, 400, 2500), 0, 1.0), (sparkle(0.6), 0, 0.4)), 2.9, 0.4)
    return track.buf


@sound('cutscene/death_djinn', -17)
def death_djinn():
    """Le Djinn meurt (DjinnDeath) : il est aspiré en spirale dans sa lampe (1,2 à 3,1 s), la lampe se renverse et
    roule (3,1 à 4,6 s), l'oasis s'évapore (3,6 à 4,8 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 400, 1500, 0.15), 0.0, 1.0)
    track.add(sparkle(0.6), 0.5, 0.3)
    track.add(svf(noise(1.9), sweep(200, 2400, 1.9), q=2.0) * adsr(1.9, 0.3, 0.2, 0.8, 0.2), 1.2, 0.5)  # l'aspiration
    track.add(osc(sweep(800, 200, 0.4), 0.4, 'sine') * expdec(0.4, 0.2), 3.0, 0.4)                     # « plop » dans la lampe
    track.add(metal_hit(1400, 0.3, 0.5), 3.15, 0.35)
    for k in range(6):
        track.add(metal_hit(rand(1200, 1600), 0.15, 0.4), 3.4 + k * 0.2, 0.15)
    track.add(fade(hiss(1.4), 0.6, 0.0), 3.6, 0.35)
    return track.buf


@sound('cutscene/phase2_dragon', -17)
def phase2_dragon():
    """Ignar, phase 2 (DragonPhase) : il déplie ses ailes (0,1 à 0,5 s), s'arrache du sol (0,5 s) à grands
    battements, le volcan crache sa lave, il plonge (1,85 s) et crache son souffle (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 60, 400, 0.3), 0.0, 1.0)
    track.add(rustle(0.4), 0.1, 0.5)
    track.add(mix((thump(70, 30, 0.6, 0.2), 0, 1.0), (sand_pour(0.6), 0, 0.4)), 0.5, 0.7)
    for k in range(6):
        track.add(wing_flap(), 0.5 + k * 0.26, 0.6)
    track.add(fade(lp(noise(1.6), 200), 0.2, 1.0), 0.6, 0.5)                                    # le volcan gronde
    track.add(whoosh(0.4, 200, 1500), 1.85, 0.6)
    track.add(mix((roar(1.6, 90), 0, 1.0), (fire_burst(1.6), 0, 0.8)), 2.15, 0.9)
    return track.buf


@sound('cutscene/death_dragon', -17)
def death_dragon():
    """Ignar meurt (DragonDeath) : il s'écrase dans le cratère (2,5 s), le volcan gronde (2,6 s) et crache son
    trésor (3,0 à 5,0 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 60, 400, 0.3), 0.0, 1.0)
    track.add(roar(1.4, 70), 0.3, 0.6)
    track.add(whoosh(0.8, 200, 1200), 1.7, 0.4)
    track.add(mix((impact(0.8, 80, 30), 0, 1.0), (fire_burst(1.0), 0, 0.6)), 2.5, 0.9)
    track.add(fade(lp(noise(0.8), 150), 0.0, 1.0), 2.6, 0.7)
    track.add(mix((boom(1.4, 50), 0, 1.0), (fire_burst(1.4), 0, 0.5)), 3.0, 0.8)
    track.add(coin_shower(2.0, 30), 3.1, 0.6)
    for k in range(8):
        track.add(metal_hit(rand(1200, 2400), 0.3, 0.5), 3.2 + rand(0.0, 1.8), 0.2)
    return track.buf


# ===========================================================================
# Mines d'Or : phases et morts des rois (MinesBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def rock_fall(d=1.2, count=14):
    """Des cailloux et des rochers qui tombent du plafond et roulent."""
    track = Track(d + 0.4)
    for _ in range(count):
        track.add(mix((thump(rand(90, 200), rand(40, 80), 0.12, 0.03), 0, 1.0),
                      (click(0.02, 600, 3000), 0, 0.5)), rand(0.0, d), rand(0.2, 0.6))
    return track.buf


def drill_whine(d, lo=180, hi=900):
    """Le moteur de la foreuse qui monte en régime."""
    f = sweep(lo, hi, d)
    return (0.6 * osc(f, d, 'saw') + 0.4 * bp(noise(d), 800, 4000)) * adsr(d, 0.1, 0.1, 0.8, 0.15)


@sound('cutscene/phase2_baron_or', -17)
def phase2_baron_or():
    """Le Baron, phase 2 (BaronPhase) : il croque une pièce d'or (1,4 s), ses dents deviennent dorées, les veines
    d'or courent dans la roche (1,9 à 3,1 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 80, 600, 0.25), 0.0, 1.0)
    track.add(coin(2400, 0.5), 0.5, 0.4)
    track.add(mix((click(0.015, 600, 2000), 0, 1.0), (thump(300, 150, 0.06, 0.02), 0, 0.6), (coin(2000, 0.4), 0.02, 0.5)), 1.4, 0.8)
    track.add(sparkle(0.6), 1.45, 0.3)
    track.add(fade(osc(sweep(400, 1600, 1.2), 1.2, 'sine') * 0.5, 0.0, 1.0), 1.9, 0.25)   # les veines qui courent
    track.add(mix((boom(1.4, 60), 0, 1.0), (coin_shower(1.0, 12), 0.05, 0.5)), 2.2, 0.8)
    track.add(brass_chord(['D3', 'F#3', 'A3', 'D4'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_baron_or', -17)
def death_baron_or():
    """Le Baron meurt (BaronDeath) : son or se ternit en pierre (0,4 à 2,2 s), il tremble (1,8 s), s'effrite (2,5 s) ;
    son vrai haut-de-forme tombe, rebondit (2,85 s) et roule."""
    track = Track(6.6)
    track.add(ambience(5.6, 80, 600, 0.2), 0.0, 1.0)
    for k in range(5):
        track.add(crack(0.1, 0.02, 400, 5000), 0.5 + k * 0.35, 0.2 + 0.05 * k)
    track.add(fade(lp(noise(0.7), 300), 0.2, 1.0), 1.8, 0.4)
    track.add(mix((impact(0.6, 120, 50), 0, 1.0), (rock_fall(1.0, 18), 0, 0.7)), 2.5, 0.9)
    track.add(thump(240, 120, 0.08, 0.03), 2.85, 0.5)
    track.add(thump(240, 120, 0.06, 0.02), 3.15, 0.3)
    track.add(fade(bp(noise(1.0), 300, 1500), 0.6, 0.0), 3.2, 0.25)                        # le chapeau qui roule
    track.add(coin(1800, 0.6), 3.9, 0.3)
    return track.buf


@sound('cutscene/phase2_grand_foreur', -17)
def phase2_grand_foreur():
    """Le Grand Foreur, phase 2 (DrillPhase) : sa foreuse monte en régime et surchauffe au rouge (0,3 à 2,2 s), la
    roche éclate en pics (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 60, 500, 0.25), 0.0, 1.0)
    track.add(drill_whine(2.0, 160, 1100), 0.2, 0.5)
    track.add(rattle(1.6, 18, 6, 1500, 4000), 0.5, 0.25)
    track.add(fade(hiss(1.0), 0.0, 1.0), 1.2, 0.3)
    track.add(mix((impact(0.8, 90, 35), 0, 1.0), (rock_fall(1.0, 16), 0, 0.7), (crack_hit(), 0, 0.6)), 2.2, 0.9)
    for k in range(6):
        track.add(crack(0.08, 0.02, 800, 6000), 2.25 + k * 0.05, 0.3)
    track.add(brass_chord(['C3', 'D#3', 'G3', 'C4'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_grand_foreur', -17)
def death_grand_foreur():
    """Le Grand Foreur meurt (DrillDeath) : sa foreuse tousse (0,6 / 1,2 / 1,7 / 2,1 s), explose en vapeur (2,5 s), le
    tunnel s'effondre sur lui (3,0 s), la lampe de son casque grésille et s'éteint (4,3 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 60, 500, 0.2), 0.0, 1.0)
    for at in (0.6, 1.2, 1.7, 2.1):
        track.add(mix((drill_whine(0.25, 300, 120), 0, 1.0), (thump(120, 60, 0.1, 0.03), 0.1, 0.6)), at, 0.45)
    track.add(mix((boom(1.2, 70), 0, 1.0), (hiss(1.4), 0, 0.7), (metal_hit(900, 0.4), 0, 0.4)), 2.5, 0.9)
    track.add(mix((lp(noise(1.6), 120) * adsr(1.6, 0.1, 0.2, 0.7, 0.4), 0, 1.0), (rock_fall(1.4, 26), 0, 0.9)), 3.0, 0.8)
    for k in range(5):                                                                           # la lampe grésille
        track.add(click(0.02, 2000, 7000), 3.9 + k * 0.08 + rand(0, 0.03), 0.3)
    track.add(click(0.04, 400, 2000), 4.3, 0.5)
    return track.buf


@sound('cutscene/phase2_maitre_forge', -17)
def phase2_maitre_forge():
    """Le Maître de Forge, phase 2 (ForgePhase) : il plonge son arme dans le foyer (0,2 s), le feu s'embrase, il la
    porte à l'enclume qui chauffe à blanc ; ses coups de marteau (0,5 / 1,0 / 1,4 / 1,75 / 2,0 / 2,2 s)."""
    track = Track(5.8)
    track.add(fade(fire_burst(4.0), 0.4, 1.0), 0.0, 0.4)
    track.add(whoosh(0.4, 150, 900, q=0.8), 0.2, 0.5)
    for at in (0.5, 1.0, 1.4, 1.75, 2.0):
        track.add(mix((metal_hit(rand(1500, 1800), 0.5), 0, 1.0), (sparkle(0.3, count=6, lo=2000, hi=6000), 0, 0.3)), at, 0.45)
    track.add(reverb(mix((metal_hit(1300, 1.2), 0, 1.0), (boom(1.0, 80), 0, 0.6)), wet=0.35, size=1.4, tail=0.8), 2.2, 0.9)
    track.add(fire_burst(1.6), 2.25, 0.6)
    track.add(brass_chord(['D2', 'A2', 'D3', 'F3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_maitre_forge', -17)
def death_maitre_forge():
    """Le Maître de Forge meurt (ForgeDeath) : le feu du foyer tombe en braises (0,2 à 2,5 s), son marteau lui
    échappe et tombe sur l'enclume (2,5 s, un dernier « ting »), il s'incline ; l'arme refroidit en grésillant."""
    track = Track(6.6)
    track.add(fade(fire_burst(2.6), 1.0, 0.0), 0.0, 0.4)
    for k in range(8):
        track.add(crack(0.04, 0.01, 2000, 7000), 0.4 + rand(0, 2.6), 0.15)                    # les braises qui craquent
    track.add(whoosh(0.5, 300, 900), 1.9, 0.3)
    track.add(reverb(metal_hit(2200, 1.6, 1.2), wet=0.4, size=1.6, tail=1.4), 2.5, 0.8)
    track.add(fade(hiss(2.6), 0.5, 0.0), 2.7, 0.3)                                           # l'arme qui refroidit
    return track.buf


@sound('cutscene/phase2_coeur_montagne', -17)
def phase2_coeur_montagne():
    """Le Coeur de la Montagne, phase 2 (MountainPhase) : un coeur bat dans la roche, les plaques de cristal
    poussent une à une (1,0 à 2,2 s), l'armure se referme (2,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 50, 400, 0.25), 0.0, 1.0)
    track.add(heartbeat([0.3, 0.9, 1.4, 1.8, 2.15], f=48, gain=1.1), 0.0, 1.0)
    for i in range(10):
        track.add(modal(rand(1800, 3200), 0.4, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), 1.0 + i * 0.13, 0.3)
    track.add(mix((impact(0.7, 80, 35), 0, 1.0), (modal(2600, 1.2, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), 0, 0.6)), 2.2, 0.9)
    track.add(brass_chord(['B1', 'F#2', 'B2', 'D3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_coeur_montagne', -17)
def death_coeur_montagne():
    """Le Coeur de la Montagne meurt (MountainDeath) : ses battements ralentissent (0,3 / 1,0 / 1,9 s), la montagne
    se fend en deux (2,5 s), la grotte s'effondre, une veine d'or brille dans la faille (3,4 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 50, 400, 0.2), 0.0, 1.0)
    track.add(heartbeat([0.3, 1.0, 1.9], f=44, gain=1.1), 0.0, 1.0)
    track.add(mix((boom(2.0, 45), 0, 1.0), (crack_hit(), 0, 0.8), (stone_slide(1.0), 0, 0.6)), 2.5, 0.9)
    track.add(rock_fall(1.8, 30), 2.6, 0.7)
    track.add(mix((sparkle(1.0), 0, 1.0), (modal(hz('E6'), 1.2, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), 0.1, 0.5)), 3.4, 0.35)
    return track.buf


# ===========================================================================
# Port des Contrebandiers : phases et morts des rois (PortBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def sea_swell(d, gain=1.0, rate=0.5):
    """La mer : une houle grave qui va et vient."""
    return lp(noise(d), 500) * (0.6 + 0.4 * np.sin(2 * math.pi * rate * times(d))) * fade_out(d, 0.5) * ramp(d, 0.0, 1.0, 0.3) * gain


def water_rush(d, lo=300, hi=1800):
    """De l'eau qui entre à flots : un grondement mouillé qui enfle."""
    return mix((lp(noise(d), 700) * ramp(d, 0.3, 1.0), 0, 1.0), (bp(noise(d), lo, hi) * ramp(d, 0.2, 0.8), 0, 0.5)) * fade_out(d, 0.3)


def hull_groan(d=0.9, f=90):
    """La coque qui gémit : un bois grave qui force."""
    return svf(osc(sweep(f, f * 0.75, d), d, 'saw'), 600, q=4.0, mode='bp') * adsr(d, 0.15, 0.2, 0.7, 0.3)


def blow(d=0.35):
    """Un souffle : on éteint une flamme."""
    return bp(noise(d), 600, 3000) * adsr(d, 0.05, 0.1, 0.6, 0.15)


def cannon(d=1.2):
    """Un coup de canon : la détonation et son écho sur l'eau."""
    return reverb(mix((boom(d, 50), 0, 1.0), (crack(0.05, 0.01, 800, 8000), 0, 0.6)), wet=0.35, size=1.8, tail=0.8)


def punch():
    """Un coup de poing dans la bagarre."""
    return mix((thump(rand(150, 220), 70, 0.1, 0.03), 0, 1.0), (click(0.01, 500, 3000), 0, 0.5))


@sound('cutscene/phase2_capitaine_rat', -17)
def phase2_capitaine_rat():
    """Le Capitaine Rat, phase 2 (RatPhase) : quatre grappins se plantent (0,6 / 0,85 / 1,1 / 1,35 s), les rats
    descendent les cordes en couinant, déferlent sur le pont (1,2 à 3,6 s) ; à l'abordage (2,2 s)."""
    track = Track(5.8)
    track.add(sea_swell(4.8, 0.6), 0.0, 0.5)
    for k in range(4):
        at = 0.3 + 0.25 * k
        track.add(whoosh(0.3, 300, 1400), at, 0.35)
        track.add(mix((metal_hit(rand(1300, 1700), 0.25, 0.5), 0, 1.0), (wood_knock(200), 0, 0.8)), at + 0.3, 0.5)
    for _ in range(26):
        track.add(squeak(), rand(0.7, 4.2), rand(0.12, 0.3))
    scurry = Track(3.0)
    for _ in range(140):
        scurry.add(click(0.004, 2500, 8000), rand(0, 2.9), rand(0.1, 0.4))
    track.add(fade(scurry.buf, 0.2, 1.0), 1.2, 0.5)
    track.add(mix((impact(0.6, 120, 45), 0, 1.0), (brass_chord(['C3', 'D#3', 'F#3', 'C4'], 1.6, 0.6), 0, 0.5)), 2.2, 0.9)
    return track.buf


@sound('cutscene/death_capitaine_rat', -17)
def death_capitaine_rat():
    """Le Capitaine Rat quitte le navire (RatDeath) : la coque heurte un récif (0,15 s) et gîte, l'eau monte ; le
    hublot s'ouvre (0,4 s), ses rats y filent en couinant ; il bondit (1,7 s), son tricorne tombe à l'eau (2,25 s) ;
    il se faufile par le hublot (2,5 s) et la mer s'y engouffre (3,9 s)."""
    track = Track(6.6)
    track.add(mix((impact(0.7, 90, 35), 0, 1.0), (wood_break(), 0.02, 0.6)), 0.15, 0.8)
    track.add(hull_groan(1.2, 80), 0.3, 0.4)
    track.add(water_rush(5.0), 0.2, 0.35)
    track.add(creak(0.4, 600), 0.4, 0.45)                                                      # le hublot s'ouvre
    for k in range(9):
        track.add(squeak(), 0.6 + k * 0.13 + rand(0, 0.05), 0.3)
    track.add(footsteps(1.2, 14, 2000, 6000), 0.6, 0.25)
    track.add(whoosh(0.5, 200, 1500), 1.7, 0.45)
    track.add(splash(0.5), 2.25, 0.45)
    track.add(mix((osc(sweep(700, 250, 0.15), 0.15, 'sine') * expdec(0.15, 0.06), 0, 1.0), (squeak(3600), 0.05, 0.6)), 2.5, 0.6)
    track.add(hull_groan(1.0, 70), 3.0, 0.3)
    track.add(fade(water_rush(1.2, 500, 3000), 0.3, 1.0), 3.9, 0.6)
    return track.buf


@sound('cutscene/phase2_tavernier', -17)
def phase2_tavernier():
    """Le Tavernier, phase 2 (TavernPhase) : il lève sa chope (1,2 à 2,0 s) et l'abat (2,2 s) : la bière gicle, les
    tabourets volent, la bagarre éclate (2,2 à 4,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1400, 0.25), 0.0, 1.0)
    track.add(whoosh(0.6, 200, 900), 1.4, 0.3)
    track.add(mix((impact(0.5, 160, 50), 0, 1.0), (modal(1100, 0.5, GLASS_RATIOS, [0.2, 0.1, 0.05, 0.02]), 0, 0.6),
                  (splash(0.6), 0.02, 0.5)), 2.2, 0.9)
    for k in range(6):
        track.add(wood_knock(rand(160, 260)), 2.4 + k * 0.18 + rand(0, 0.05), 0.4)               # les tabourets retombent
    for _ in range(14):
        track.add(punch(), rand(2.5, 4.2), rand(0.3, 0.6))
    track.add(crowd_boo(2.0), 2.4, 0.25)
    track.add(brass_chord(['G2', 'D3', 'G3', 'A#3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_tavernier', -17)
def death_tavernier():
    """La taverne ferme ses portes (TavernDeath) : il souffle les lanternes (0,6 / 1,25 s), le feu s'éteint (2,1 s) ;
    les battants claquent (2,5 s), la barre tombe en travers (3,7 s), la fenêtre s'éteint (4,3 s)."""
    track = Track(6.6)
    track.add(fade(fire_burst(2.4), 0.5, 0.0), 0.0, 0.35)
    for at in (0.6, 1.25):
        track.add(blow(0.35), at - 0.3, 0.5)
        track.add(click(0.01, 800, 3000), at, 0.25)
    track.add(hiss(0.8), 2.1, 0.25)
    track.add(creak(0.45, 220), 2.05, 0.5)
    track.add(reverb(mix((impact(0.7, 110, 40), 0, 1.0), (wood_knock(120), 0, 0.8), (crack_hit(), 0, 0.5)), wet=0.3, size=1.2), 2.5, 1.0)
    track.add(wood_knock(150), 2.65, 0.3)
    track.add(mix((thump(140, 60, 0.25, 0.06), 0, 1.0), (wood_knock(180), 0, 0.7)), 3.7, 0.8)
    track.add(click(0.02, 300, 1500), 4.3, 0.3)
    return track.buf


@sound('cutscene/phase2_gardien_phare', -17)
def phase2_gardien_phare():
    """Le Gardien du Phare, phase 2 (LighthousePhase) : la tempête se lève (vent, pluie), les vagues se brisent sur le
    phare (0,5 / 1,2 / 1,85 / 2,5 / 3,3 s), son faisceau tourne de plus en plus vite ; tonnerre (0,7 / 1,5 / 3,3 s), la
    foudre frappe le phare (2,2 s)."""
    track = Track(5.8)
    track.add(fade(rain_noise(5.2), 0.2, 1.0), 0.0, 0.35)
    track.add(fade(icy_wind(5.2), 0.3, 1.0), 0.0, 0.3)
    track.add(sea_swell(5.2, 1.0, 0.7), 0.0, 0.4)
    for at in (0.5, 1.2, 1.85, 2.5, 3.3):
        track.add(splash(0.9), at, 0.5)
    for at, gain in ((0.7, 0.4), (1.5, 0.5), (3.3, 0.4)):
        track.add(thunder(1.4), at, gain)
    track.add(osc(sweep(200, 900, 2.0), 2.0, 'sine') * ramp(2.0, 0.0, 1.0) * 0.4, 0.3, 0.2)       # le faisceau qui s'emballe
    track.add(mix((thunder(2.2), 0, 1.2), (crack_hit(), 0, 0.8)), 2.2, 0.9)
    track.add(brass_chord(['A2', 'C3', 'E3', 'A3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_gardien_phare', -17)
def death_gardien_phare():
    """Le phare s'éteint (LighthouseDeath) : la mer calme, la lampe grésille et bégaie (0,9 / 1,45 / 1,95 / 2,2 s),
    s'éteint (2,5 s), une fumée monte ; une cloche au loin (3,4 s)."""
    track = Track(6.6)
    track.add(sea_swell(5.6, 0.8, 0.4), 0.0, 0.5)
    hum = osc(hz('A2'), 2.5, 'saw') * 0.3
    track.add(lp(hum, 400) * ramp(2.5, 1.0, 0.6), 0.0, 0.2)
    for at in (0.9, 1.45, 1.95, 2.2):
        track.add(mix((click(0.02, 2000, 7000), 0, 1.0), (click(0.015, 1500, 5000), 0.04, 0.6)), at, 0.4)
    track.add(mix((click(0.04, 400, 2000), 0, 1.0), (thump(90, 45, 0.3, 0.08), 0, 0.6)), 2.5, 0.7)
    track.add(hiss(1.0), 2.55, 0.2)
    track.add(reverb(bell_toll(hz('D3'), 1.8), wet=0.5, size=1.6, tail=0.8), 3.4, 0.3)
    return track.buf


@sound('cutscene/phase2_capitaine_noir', -17)
def phase2_capitaine_noir():
    """Le Capitaine Noir, phase 2 (BlackPhase) : la bordée, sept canons l'un après l'autre (0,6 à 1,8 s), la fumée
    couvre tout, son œil s'allume (2,2 s)."""
    track = Track(5.8)
    track.add(sea_swell(4.8, 0.7), 0.0, 0.4)
    for k in range(7):
        track.add(cannon(1.2), 0.6 + 0.2 * k, 0.5 + 0.05 * k)
    track.add(fade(hiss(2.0), 0.6, 0.0), 1.0, 0.2)
    track.add(mix((boom(1.4, 45), 0, 0.7), (sparkle(0.6, count=6, lo=1500, hi=4000), 0.05, 0.3)), 2.2, 0.6)
    track.add(brass_chord(['D2', 'F2', 'G#2', 'D3'], 1.8, 0.6), 2.25, 0.45)
    return track.buf


@sound('cutscene/death_capitaine_noir', -17)
def death_capitaine_noir():
    """Le Capitaine Noir sombre avec son navire (BlackDeath) : la coque craque (0,4 s), la mer monte sur le pont
    (0,2 à 3,0 s), le galion gîte et s'enfonce, son tricorne reste à flotter (vers 2,4 s), les mâts passent sous
    l'eau dans un remous de bulles (3,9 s)."""
    track = Track(6.6)
    track.add(mix((impact(0.8, 80, 30), 0, 1.0), (wood_break(), 0.02, 0.5)), 0.4, 0.7)
    track.add(water_rush(3.4, 200, 1200), 0.2, 0.5)
    for at in (0.9, 1.7, 2.6, 3.4):
        track.add(hull_groan(rand(0.8, 1.1), rand(60, 90)), at, 0.35)
    track.add(bubbles(3.0, 30, 200, 700), 1.0, 0.35)
    track.add(splash(0.4), 2.4, 0.35)
    track.add(mix((splash(1.2), 0, 1.0), (bubbles(1.5, 40, 150, 500), 0, 0.8)), 3.9, 0.6)
    track.add(sea_swell(2.2, 0.6, 0.3), 4.2, 0.4)
    return track.buf


# ===========================================================================
# Histoire : phases et morts des boss (TowerBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def glass_burst(count=10, lo=2600, hi=6500):
    """Un éclat de cristal qui se brise : le claquement, puis les morceaux qui tintent en tombant."""
    parts = [(crack(0.07, 0.01, 1500, 12000), 0, 1.0)]
    parts += [(glass_clink(rand(lo, hi)), rand(0.0, 0.5), rand(0.1, 0.3)) for _ in range(count)]
    return mix(*parts)


def choir(notes, d, gain=1.0):
    """Une nappe claire, comme un choeur lointain : des voix douces et tremblées."""
    x = stack(*(osc(vibrato(hz(note), d, 4.5 + k, 0.006), d, 'tri') / (k + 1) ** 0.4 for k, note in enumerate(notes)))
    return lp(x, 2400) * np.clip(times(d) / 0.5, 0, 1) * fade_out(d, 0.8) * gain


def roll_rumble(d, f=90):
    """Une lourde roue de fer qui roule sur un plancher et ralentit."""
    rate = np.linspace(14, 4, n(d))
    wobble = 0.6 + 0.4 * np.abs(np.sin(np.cumsum(rate) / SR * np.pi))
    return lp(noise(d), f * 3) * wobble * np.linspace(1.0, 0.2, n(d))


@sound('cutscene/death_comete', -17)
def death_comete():
    """La Comète Dorée meurt (CometDeath) : elle remonte vers le ciel (1,2 à 2,5 s), se défait en étoiles filantes
    (2,5 s), ses étoiles s'allument une à une à sa place (2,9 à 3,7 s) puis les traits de la constellation (3,4 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 1200, 0.2), 0.0, 1.0)
    track.add(swell(1.3, 300, 3000), 1.2, 0.5)
    track.add(fade(osc(sweep(220, 880, 1.3), 1.3, 'tri'), 0.0, 0.6), 1.2, 0.2)
    track.add(mix((boom(1.6, 70), 0, 1.0), (sparkle(1.2, count=30), 0.02, 0.7)), 2.5, 0.8)
    for k in range(6):                                                                       # les étoiles filantes
        track.add(whoosh(0.5, rand(2500, 4000), rand(600, 1200), q=1.4), 2.55 + k * 0.12, 0.25)
    for i, note in enumerate(['A5', 'C#6', 'E6', 'F#6', 'A6', 'B6', 'C#7', 'E7', 'F#7', 'A7']):
        track.add(modal(hz(note), 1.0, GLASS_RATIOS, [0.5, 0.3, 0.2, 0.1]), 2.9 + i * 0.08, 0.18)
    track.add(choir(['A3', 'E4', 'A4', 'C#5'], 2.6), 3.4, 0.25)
    return track.buf


@sound('cutscene/phase2_reine', -17)
def phase2_reine():
    """La Reine des Tables, phase 2 (QueenPhase) : elle renverse sa table d'un revers (0,9 s), ses cartes volent ;
    une carte Trèfle géante tombe du ciel et se plaque (2,2 s), puis se dédouble (2,5 à 3,1 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 200, 900, 0.2), 0.0, 1.0)
    track.add(whoosh(0.3, 300, 1500), 0.65, 0.5)
    track.add(mix((thump(140, 60, 0.3, 0.08), 0, 1.0), (modal(160, 0.4, WOOD_RATIOS, [0.12, 0.06, 0.03]), 0, 0.8)), 0.9, 0.8)
    track.add(card_riffle(0.6, 22), 0.95, 0.5)
    track.add(whoosh(0.7, 200, 2000, q=0.8), 1.5, 0.4)
    track.add(mix((card_slap(0.2), 0, 1.0), (boom(1.2, 80), 0, 0.8)), 2.2, 0.9)
    track.add(card_flick(0.12), 2.5, 0.6)
    track.add(card_slap(0.15), 2.85, 0.6)
    track.add(sparkle(0.8), 2.6, 0.3)
    track.add(brass_chord(['G3', 'B3', 'D4', 'G4'], 1.6, 0.6), 2.25, 0.45)
    return track.buf


@sound('cutscene/death_reine', -17)
def death_reine():
    """La Reine meurt (QueenDeath) : sa couronne glisse (0,5 s), tombe et tinte au sol (1,15 s), se fend (1,45 à
    2,5 s) et se brise en deux (2,5 s) ; son château de cartes s'écroule (2,7 s), la couronne part en poussière d'or
    (3,0 s). Elle se retourne en carte (3,5 à 3,9 s) et tombe à plat (4,3 à 4,75 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 200, 900, 0.2), 0.0, 1.0)
    track.add(whoosh(0.4, 800, 2500), 0.5, 0.3)
    track.add(metal_hit(2100, 0.6), 1.15, 0.6)
    track.add(metal_hit(2300, 0.3), 1.35, 0.3)
    for k in range(4):
        track.add(crack(0.05, 0.01, 1500, 7000), 1.5 + k * 0.25, 0.2 + 0.05 * k)
    track.add(mix((metal_hit(1700, 1.0), 0, 1.0), (crack_hit(), 0, 0.8), (thump(200, 80, 0.3), 0, 0.6)), 2.5, 0.8)
    track.add(card_riffle(0.9, 26), 2.7, 0.55)
    track.add(fade(sparkle(1.4, count=30), 1.0, 0.0), 3.0, 0.35)
    track.add(mix((card_flick(0.15), 0, 1.0), (whoosh(0.4, 600, 3000), 0, 0.5)), 3.5, 0.6)
    track.add(whoosh(0.45, 300, 1200), 4.3, 0.4)
    track.add(mix((card_slap(0.25), 0, 1.0), (thump(110, 50, 0.3), 0, 0.6)), 4.75, 0.8)
    return track.buf


@sound('cutscene/phase2_eclat', -17)
def phase2_eclat():
    """L'Éclat Originel, phase 2 (ShardPhase) : il se fissure (0,4 à 2,2 s), se fend en six éclats (2,2 s) qui
    tournent en montrant leurs symboles, puis se ressoudent dans une autre couleur (3,9 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 300, 1500, 0.2), 0.0, 1.0)
    for k in range(7):
        track.add(ice_crack(0.25), 0.4 + k * 0.26, 0.3 + 0.05 * k)
    track.add(mix((glass_burst(14), 0, 1.0), (boom(1.2, 90), 0, 0.7)), 2.2, 0.8)
    for i, note in enumerate(['E5', 'G5', 'B5', 'D6', 'F#6', 'A6']):                         # les six symboles
        track.add(modal(hz(note), 1.2, GLASS_RATIOS, [0.6, 0.35, 0.2, 0.12]), 2.4 + i * 0.2, 0.25)
    track.add(fade(osc(vibrato(hz('E5'), 1.4, 7, 0.02), 1.4, 'sine'), 0.0, 1.0), 2.4, 0.15)
    track.add(whoosh(0.4, 3000, 600), 3.6, 0.4)
    track.add(mix((modal(hz('E6'), 1.6, GLASS_RATIOS, [0.8, 0.5, 0.3, 0.2]), 0, 1.0), (sparkle(1.0), 0, 0.6)), 3.9, 0.5)
    track.add(brass_chord(['E3', 'B3', 'E4', 'G#4'], 1.4, 0.5), 3.95, 0.35)
    return track.buf


@sound('cutscene/phase2_pretendant', -17)
def phase2_pretendant():
    """Le Prétendant, phase 2 (PretenderShard) : un fil de lumière relie son éclat à sa carte Rouleau interdit
    (0,3 s) ; l'éclat se fend (1,0 à 2,2 s) et se brise (2,2 s), le cadenas saute et tombe, la carte se fend en deux
    et part en cendres (2,75 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 600, 0.25), 0.0, 1.0)
    track.add(fade(osc(vibrato(hz('A4'), 1.9, 9, 0.01), 1.9, 'sine'), 0.0, 1.0), 0.3, 0.12)   # le fil qui vibre
    for k in range(5):
        track.add(crack(0.05, 0.01, 2000, 9000), 1.0 + k * 0.24, 0.2 + 0.05 * k)
    track.add(mix((glass_burst(12), 0, 1.0), (boom(1.2, 80), 0, 0.7), (metal_hit(900, 0.5), 0.02, 0.5)), 2.2, 0.8)
    track.add(metal_hit(1300, 0.3), 2.75, 0.4)                                               # le cadenas tombe
    track.add(metal_hit(1100, 0.25), 2.95, 0.25)
    track.add(mix((fire_burst(1.0), 0, 0.7), (hiss(1.2), 0, 0.6)), 2.75, 0.5)                # la carte en cendres
    track.add(brass_chord(['D3', 'F3', 'A3', 'D4'], 1.6, 0.5), 2.25, 0.4)
    return track.buf


@sound('cutscene/phase3_pretendant', -17)
def phase3_pretendant():
    """Le Prétendant, phase 3 (PretenderQueen) : le fantôme de la Reine paraît (0,3 à 1,3 s) et tire sur l'éclat
    rouge (1,0 s) ; il se brise (2,2 s), son pouvoir la rejoint ; elle remonte au ciel (2,7 à 4,2 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 100, 600, 0.25), 0.0, 1.0)
    track.add(ghost_wail(1.2, 440), 0.3, 0.25)
    track.add(fade(svf(noise(1.2), sweep(300, 1400, 1.2), q=6.0, mode='bp'), 0.2, 1.0), 1.0, 0.25)   # elle tire
    track.add(mix((glass_burst(10), 0, 1.0), (boom(1.0, 90), 0, 0.6)), 2.2, 0.8)
    track.add(sparkle(0.7, count=20), 2.25, 0.4)
    track.add(choir(['D4', 'F4', 'A4', 'D5'], 2.2), 2.6, 0.3)
    track.add(brass_chord(['D3', 'F3', 'A3', 'C4'], 1.4, 0.45), 2.3, 0.35)
    return track.buf


@sound('cutscene/death_pretendant', -17)
def death_pretendant():
    """Le Prétendant meurt (PretenderDeath) : le trône sous son rayon de lumière (0 à 2,5 s), il marche vers lui
    (0,3 à 2,0 s) ; son dernier éclat se brise (2,5 s), il s'écroule (3,05 s), sa couronne rebondit et roule (3,3 s) ;
    le trône se fend et s'effondre en poussière d'or (3,15 s)."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 600, 0.2), 0.0, 1.0)
    track.add(choir(['C4', 'E4', 'G4', 'C5'], 2.7), 0.0, 0.25)
    track.add(footsteps(1.7, 5.5, 300, 1200), 0.3, 0.45)
    track.add(mix((glass_burst(12), 0, 1.0), (boom(1.4, 70), 0, 0.8)), 2.5, 0.8)
    track.add(fade(osc(sweep(800, 200, 0.6), 0.6, 'sine'), 0.6, 0.0), 2.5, 0.2)              # la lumière s'éteint
    track.add(mix((thump(110, 45, 0.4, 0.12), 0, 1.0), (rustle(0.4), 0, 0.5)), 3.05, 0.8)
    track.add(metal_hit(1900, 0.5), 3.3, 0.45)
    track.add(metal_hit(2100, 0.3), 3.55, 0.3)
    track.add(crack(0.08, 0.015, 600, 6000), 2.6, 0.3)
    track.add(mix((boom(1.4, 60), 0, 0.8), (rock_fall(1.2, 22), 0, 0.8), (metal_hit(600, 0.8), 0, 0.4)), 3.15, 0.85)
    track.add(fade(sparkle(1.4, count=24), 1.0, 0.0), 3.2, 0.3)
    return track.buf


@sound('cutscene/phase2_maison', -17)
def phase2_maison():
    """La Maison, phase 2 (HouseFacade) : sa façade se fissure tout autour (0,45 à 1,35 s), se décolle en grinçant
    (1,35 s), bascule et s'écrase en morceaux (2,2 s) ; ses machines aux ailes d'ange tintent dans la salle ouverte."""
    track = Track(5.8)
    track.add(ambience(4.8, 150, 800, 0.2), 0.0, 1.0)
    for k in range(5):
        track.add(crack(0.06, 0.012, 500, 6000), 0.45 + k * 0.2, 0.25 + 0.05 * k)
    track.add(creak(0.8, 120), 1.35, 0.6)
    track.add(whoosh(0.6, 150, 700, q=0.8), 1.6, 0.4)
    track.add(mix((impact(0.8, 100, 35), 0, 1.0), (wood_break(), 0, 0.8), (rock_fall(1.0, 18), 0.05, 0.7)), 2.2, 0.9)
    track.add(mix((sparkle(1.0), 0, 0.6), (bell_toll(hz('E5'), 1.6), 0, 0.3)), 2.5, 0.4)
    track.add(brass_chord(['C3', 'E3', 'G3', 'C4'], 1.6, 0.5), 2.25, 0.4)
    return track.buf


@sound('cutscene/phase3_maison', -17)
def phase3_maison():
    """La Maison, phase 3 (HouseVault) : les verrous de la porte du coffre sautent (0,35 / 0,65 / 0,95 / 1,25 s),
    sa petite porte de coffre tombe (1,0 s), la grande tombe du mur (1,8 à 2,2 s), roule et cogne le rideau (3,2 s) ;
    les potions se brisent au sol (vers 3,0 s) ; la Maison vire au démon (2,7 s)."""
    track = Track(5.8)
    track.add(ambience(4.8, 150, 800, 0.2), 0.0, 1.0)
    for at in (0.35, 0.65, 0.95, 1.25):
        track.add(metal_hit(rand(1400, 1800), 0.35), at, 0.4)
    track.add(mix((metal_hit(2400, 0.3), 0, 0.6), (thump(260, 120, 0.08, 0.03), 0.4, 0.5)), 1.0, 0.5)
    track.add(creak(0.5, 90), 1.4, 0.5)
    track.add(whoosh(0.4, 200, 900, q=0.8), 1.8, 0.5)
    track.add(mix((metal_hit(110, 1.6, 0.6), 0, 1.0), (impact(0.8, 90, 30), 0, 1.0)), 2.2, 0.9)
    track.add(roll_rumble(1.0), 2.25, 0.7)
    track.add(mix((metal_hit(160, 1.0, 0.5), 0, 1.0), (thump(120, 50, 0.4), 0, 0.8)), 3.2, 0.7)
    for at in (2.95, 3.0, 3.05, 3.1, 3.15):
        track.add(glass_burst(4, 3000, 7000), at + rand(-0.03, 0.03), 0.3)
    track.add(fade(drone(['D2', 'G#2', 'D3'], 2.6, 'saw'), 0.0, 1.0), 2.7, 0.12)
    track.add(brass_chord(['D2', 'F2', 'G#2', 'D3'], 1.6, 0.5), 2.75, 0.35)
    return track.buf


@sound('cutscene/death_maison', -17)
def death_maison():
    """La Maison meurt (HouseDeath) : elle tremble (1,0 à 2,5 s) et explose (2,5 s) ; ses machines aux ailes
    d'ange s'envolent (2,8 s), la lumière tombe des lustres, des plumes."""
    track = Track(6.6)
    track.add(ambience(5.6, 100, 600, 0.25), 0.0, 1.0)
    track.add(fade(lp(noise(1.5), 200), 0.0, 1.0), 1.0, 0.5)
    track.add(fade(drone(['D2', 'G#2'], 2.5, 'saw'), 1.0, 0.3), 0.0, 0.1)
    track.add(mix((boom(2.0, 55), 0, 1.0), (wood_break(), 0, 0.7), (rock_fall(1.4, 24), 0.05, 0.7)), 2.5, 0.9)
    for k in range(4):
        track.add(wing_flap(), 2.8 + k * 0.35, 0.35)
    track.add(choir(['C4', 'E4', 'G4', 'C5', 'E5'], 3.2), 2.9, 0.35)
    track.add(mix((bell_toll(hz('C5'), 2.0), 0, 0.5), (sparkle(1.2), 0, 0.5)), 3.0, 0.4)
    return track.buf


# ===========================================================================
# Prairie : phases et morts des rois (PrairieBossScenes, 2026-11-22)
# Les instants reprennent les constantes des scènes : si une scène change de rythme, mettre à jour ici.
# ===========================================================================

def meadow(d, gain=0.2):
    """Le vent doux du pré, devant le château."""
    return ambience(d, 300, 1400, gain)


def slurp(d=0.5):
    """Un liquide aspiré : un gargouillis qui monte."""
    wobble = 0.6 + 0.4 * np.abs(np.sin(times(d) * 31))
    return svf(noise(d), sweep(250, 1100, d), q=4.0, mode='bp') * wobble * adsr(d, 0.05, 0.1, 0.8, 0.15)


def beep(d=0.14, f=1000):
    """Le bip d'un moniteur cardiaque."""
    return osc(f, d, 'sine') * adsr(d, 0.004, 0.02, 0.8, 0.03)


@sound('cutscene/phase2_roi_pique', -17)
def phase2_roi_pique():
    """Le Roi de Pique, phase 2 (SpadePhase) : le cercle de lames se dresse dans l'herbe (0,15 à 1,0 s), boit le
    sang qui monte du pré (0,75 à 1,6 s), s'envole en deux rangées de crocs (1,5 s) qui mordent (2,2 s)."""
    track = Track(5.8)
    track.add(meadow(4.8), 0.0, 1.0)
    for i in range(10):
        track.add(blade_ring(rand(2600, 3600), 0.5), 0.65 + i * 0.04, 0.12)
    track.add(fade(slurp(0.9), 0.4, 1.0), 0.75, 0.5)
    track.add(slurp(0.5), 1.2, 0.35)
    for i in range(5):
        track.add(blade_swish(0.25, 500, 3500), 1.5 + i * 0.06, 0.3)
    track.add(fade(osc(sweep(300, 900, 0.3), 0.3, 'saw') * 0.3, 0.0, 1.0), 1.9, 0.2)
    track.add(mix((metal_hit(1400, 0.9, 1.2), 0, 1.0), (metal_hit(1900, 0.7), 0.01, 0.8), (boom(1.4, 60), 0, 0.9),
                  (crack_hit(), 0, 0.7)), 2.2, 0.9)
    track.add(brass_chord(['E2', 'B2', 'E3', 'G3'], 1.6, 0.6), 2.25, 0.4)
    for at in (2.9, 3.3, 3.8, 4.2):
        track.add(drip(), at, 0.25)
    return track.buf


@sound('cutscene/death_roi_pique', -17)
def death_roi_pique():
    """Le Roi de Pique meurt (SpadeDeath) : un éclat court sur son épée (0,3 s), elle se fend (1,1 / 1,6 / 2,05 s),
    casse net (2,5 s) ; la moitié vole et se plante dans l'herbe (3,6 s) ; les lames tombent l'une après l'autre."""
    track = Track(6.6)
    track.add(meadow(5.6), 0.0, 1.0)
    track.add(blade_ring(3400, 0.8), 0.3, 0.3)
    for k, at in enumerate((1.1, 1.6, 2.05)):
        track.add(mix((crack(0.08, 0.015, 1500, 8000), 0, 1.0), (metal_hit(2600 + 300 * k, 0.3, 0.6), 0, 0.4)), at, 0.4 + 0.1 * k)
    track.add(fade(svf(noise(0.9), sweep(1500, 4000, 0.9), q=6.0, mode='bp'), 0.0, 1.0), 1.6, 0.15)   # le métal qui gémit
    track.add(mix((metal_hit(2200, 1.4, 1.4), 0, 1.0), (crack_hit(), 0, 1.0), (boom(1.2, 70), 0, 0.7),
                  (sparkle(0.6, count=12, lo=3000, hi=8000), 0.02, 0.4)), 2.5, 0.9)
    track.add(whoosh(0.9, 400, 1800, q=1.4), 2.6, 0.35)
    track.add(mix((thump(160, 70, 0.15, 0.04), 0, 1.0), (metal_hit(1700, 0.4, 0.6), 0, 0.5)), 3.58, 0.6)
    for k in range(9):
        track.add(mix((metal_hit(rand(1100, 1600), 0.3, 0.7), 0, 1.0), (thump(140, 70, 0.08, 0.02), 0, 0.5)),
                  3.12 + k * 0.11 + rand(0, 0.02), 0.25)
    return track.buf


@sound('cutscene/phase2_roi_trefle', -17)
def phase2_roi_trefle():
    """Le Roi de Trèfle, phase 2 (ClubPhase) : sa table ronde tourne de plus en plus vite (0,3 à 2,2 s), le pré
    tangue (dès 0,9 s), tout chavire et les pièces volent (2,2 s), il voit double."""
    track = Track(5.8)
    track.add(meadow(4.8), 0.0, 1.0)
    track.add(rattle(1.9, 4, 30, 400, 1500, wood=True), 0.3, 0.35)
    track.add(fade(svf(noise(1.9), sweep(200, 1400, 1.9), q=2.0, mode='bp'), 0.1, 1.0), 0.3, 0.3)   # la table qui siffle en tournant
    track.add(gulp(), 1.0, 0.4)
    track.add(fade(osc(vibrato(hz('A3'), 2.4, 3, 0.04), 2.4, 'tri'), 0.0, 1.0), 0.9, 0.12)            # l'ivresse qui monte
    track.add(mix((boom(1.2, 70), 0, 1.0), (coin_shower(1.0, 18), 0.02, 0.6), (glass_clink(2800), 0.05, 0.4)), 2.2, 0.85)
    wobble = osc(vibrato(hz('D4'), 1.8, 2.5, 0.06), 1.8, 'tri') + osc(vibrato(hz('D#4'), 1.8, 2.1, 0.06), 1.8, 'tri')
    track.add(wobble * adsr(1.8, 0.05, 0.2, 0.6, 0.6), 2.3, 0.15)
    track.add(rattle(1.4, 14, 4, 400, 1500, wood=True), 2.3, 0.25)
    return track.buf


@sound('cutscene/death_roi_trefle', -17)
def death_roi_trefle():
    """Le Roi de Trèfle meurt (ClubDeath) : son trèfle perd ses feuilles (0,7 / 1,3 / 1,9 s), le vent emporte la
    dernière (2,5 s), un nuage noir arrive (2,7 s) et pleut sur lui seul (dès 3,2 s)."""
    track = Track(6.6)
    track.add(meadow(5.6), 0.0, 1.0)
    track.add(sparkle(1.4, count=10), 0.0, 0.2)
    for k, at in enumerate((0.7, 1.3, 1.9)):
        track.add(rustle(0.4), at, 0.3)
        track.add(chip(hz(['E5', 'C5', 'A4'][k]), 0.3, 'tri', a=0.01, s=0.5, r=0.15), at, 0.25)
    track.add(whoosh(1.4, 200, 1200, q=0.8), 2.4, 0.5)
    track.add(chip(hz('F4'), 0.6, 'tri', a=0.01, s=0.5, r=0.3), 2.5, 0.3)
    track.add(thunder(1.6), 2.75, 0.35)
    track.add(rain_noise(2.6), 3.2, 0.35)
    for k in range(8):
        track.add(drip(), 3.3 + k * 0.3 + rand(0, 0.1), 0.2)
    return track.buf


@sound('cutscene/phase2_roi_coeur', -17)
def phase2_roi_coeur():
    """Le Roi de Coeur, phase 2 (HeartPhase) : son coeur bat de plus en plus vite (dès 0,2 s), le vitrail se fend
    (0,9 à 2,2 s), il écrase sa coupe (2,2 s), une brume rouge monte."""
    track = Track(5.8)
    track.add(meadow(4.8), 0.0, 1.0)
    beats, t = [], 0.2
    while t < 4.2:
        beats.append(t)
        t += 0.7 + (0.18 - 0.7) * min(1.0, t / 2.2)
    track.add(heartbeat(beats, f=55, gain=1.0), 0.0, 1.0)
    for k in range(6):
        track.add(crack(0.06, 0.012, 2000, 9000), 0.9 + k * 0.22, 0.25)
    track.add(mix((metal_hit(900, 0.5, 0.6), 0, 1.0), (crack_hit(), 0, 0.8), (boom(1.4, 55), 0, 0.9),
                  (modal(2400, 0.8, GLASS_RATIOS, [0.3, 0.15, 0.08, 0.04]), 0.01, 0.5)), 2.2, 0.9)
    track.add(brass_chord(['C2', 'G2', 'C3', 'D#3'], 1.8, 0.6), 2.25, 0.45)
    return track.buf


@sound('cutscene/death_roi_coeur', -17)
def death_roi_coeur():
    """Le Roi de Coeur meurt (HeartDeath) : trois battements de plus en plus faibles (0,35 / 1,15 / 2,05 s), chacun
    avec son bip, puis le tracé devient plat (2,5 s) : le long bip continu."""
    track = Track(6.6)
    track.add(meadow(5.6, 0.15), 0.0, 1.0)
    for at, g in ((0.35, 1.0), (1.15, 0.7), (2.05, 0.4)):
        track.add(heartbeat([at], f=52, gain=g), 0.0, 1.0)
        track.add(beep(), at, 0.35)
    track.add(beep(2.6) * fade_out(2.6, 1.2), 2.5, 0.35)
    track.add(thump(90, 40, 0.5, 0.15), 2.5, 0.4)
    return track.buf


@sound('cutscene/phase2_roi_carreau', -17)
def phase2_roi_carreau():
    """Le Roi de Carreau, phase 2 (DiamondPhase) : ses piles de pièces se dédoublent (0,4 / 0,8 / 1,2 / 1,6 / 2,0 s),
    les pages du livre d'or tournent de plus en plus vite, les tours d'or montent (2,2 s)."""
    track = Track(5.8)
    track.add(meadow(4.8), 0.0, 1.0)
    for i in range(1, 6):
        track.add(coin_shower(0.3, 2 * i), i * 0.4, 0.25 + 0.05 * i)
    t = 0.3
    while t < 2.2:
        track.add(card_flick(0.06), t, 0.2)
        t += 0.8 / (1.2 + 3 * t / 2.2)
    track.add(mix((cash_register_bell(), 0, 0.8), (boom(1.2, 65), 0, 0.9), (coin_shower(1.4, 30), 0.03, 0.7)), 2.2, 0.85)
    track.add(brass_chord(['F2', 'C3', 'F3', 'A3'], 1.6, 0.6), 2.25, 0.4)
    return track.buf


@sound('cutscene/death_roi_carreau', -17)
def death_roi_carreau():
    """Le Roi de Carreau meurt (DiamondDeath) : des pièces glissent déjà du tas (0,9 à 2,5 s) ; il lâche tout
    (2,5 s) : le trésor roule loin de lui, le coffre bascule, son sceptre tombe dans l'herbe (2,95 s) ; une dernière
    pièce tourne sur la tranche et tombe à plat (4,3 s)."""
    track = Track(6.6)
    track.add(meadow(5.6), 0.0, 1.0)
    for k in range(6):
        track.add(coin(rand(1800, 2600), 0.4), 0.9 + k * 0.27 + rand(0, 0.05), 0.2)
    track.add(mix((coin_shower(1.6, 40), 0, 1.0), (thump(130, 60, 0.25, 0.06), 0, 0.8), (wood_knock(180), 0.3, 0.6)), 2.5, 0.8)
    track.add(rattle(1.4, 30, 6, 2000, 4500), 2.7, 0.25)                                           # les pièces qui roulent
    track.add(mix((metal_hit(1500, 0.6, 0.8), 0, 1.0), (thump(150, 80, 0.08, 0.02), 0, 0.5)), 2.95, 0.5)
    track.add(rattle(1.0, 8, 40, 2500, 5000), 3.3, 0.2)                                            # la dernière pièce tourne
    track.add(coin(2200, 0.6), 4.3, 0.45)
    return track.buf


def main(prefixes):
    done = 0
    for name, level, fmt, trim, loop, fn in SOUNDS:
        if prefixes and not any(name.startswith(p) for p in prefixes):
            continue
        seed(name)
        x = finish(fn(), level, trim=trim, fout=0.02 if trim else 0.0, fin=0.002 if trim else 0.0, loop=loop)
        path = os.path.join(OUT, f'{name}.{fmt}')
        write(path, x, fmt)
        print(f'{name:32s} {len(x) / SR:5.2f} s')
        done += 1
    if done == 0:
        sys.exit(f'Aucun son ne correspond à {prefixes}')


# --- Tour, chapitre 3 : Les Pèlerins de la Comète (Tour à 10 chapitres, 2026-11-22) ---

@sound('cutscene/king_prophete', -17)
def king_prophete():
    """Le Prophète du Cratère : la route de nuit, il monte sur son rocher (0,4 s), les pèlerins s'agenouillent (2 s),
    il lève son bâton et la comète passe (3,4 s)."""
    prelude = [(ambience(2.0, 150, 700, 0.3), 0.0, 0.5), (drone(['D2', 'A2'], 2.0), 0.0, 0.3)]
    entrance = Track(5.0)
    entrance.add(whoosh(1.0, 120, 700, q=0.8), 0.4, 0.4)
    entrance.add(thump(110, 45, 0.3), 2.0, 0.5)
    entrance.add(rustle(0.8), 2.0, 0.3)
    gesture = [(whoosh(1.6, 300, 2400, q=1.2), 3.0, 0.6), (bell_toll(hz('D3'), 2.4), 3.4, 0.5),
               (modal(hz('A5'), 2.0, GLASS_RATIOS, [1.0, 0.6, 0.4, 0.3]), 3.45, 0.3),
               (brass_chord(['D3', 'F#3', 'A3', 'D4'], 1.4, 0.5), 3.5, 0.5)]
    return king(prelude, reverb(entrance.buf, wet=0.35, size=1.8), gesture)


if __name__ == '__main__':
    main(sys.argv[1:])
