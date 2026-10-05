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
    Avant la Comète Dorée (CometCutscene) : nuit calme et étoiles filantes qui tintent,
    puis une météorite en feu qui gronde de plus en plus fort, l'explosion (IMPACT = 5,6 s)
    et un accord de casino qui monte dans le fondu blanc (WHITE_FULL = 6,8 s).
    """
    impact_at, white_full = 5.6, 6.8
    track = Track(9.0)
    night = lp(noise(6.0), 500) * ramp(6.0, 0.0, 1.0, 0.4) * ramp(6.0, 1.0, 0.2, 3.0)   # vent de nuit
    track.add(night, 0.0, 0.18)
    drone = stack(osc(hz('A2'), 6.0, 'sine'), 0.5 * osc(hz('E3'), 6.0, 'sine'), 0.3 * osc(hz('A3') * 1.003, 6.0, 'tri'))
    track.add(drone * ramp(6.0, 0.0, 1.0, 0.5) * ramp(6.0, 1.0, 0.0, 2.0), 0.0, 0.12)
    notes = ['E6', 'B5', 'A6', 'E6', 'C#7', 'B6', 'E7', 'A6', 'B6', 'E7']
    t = 0.6
    for k, note in enumerate(notes):                                  # étoiles filantes : tintements de clochette
        ring = modal(hz(note), 0.9, [1, 2.76, 5.4], [0.5, 0.2, 0.08], [1, 0.3, 0.12])
        swish = whoosh(0.35, 3000, 9000, q=1.5, curve=1.2) * expdec(0.35, 0.15, attack=0.05)
        track.add(swish, t, 0.12)
        track.add(ring, t + 0.12, 0.22)
        t += 0.32 - 0.017 * k
    track.add(sparkle(2.4, count=40, lo=4000, hi=10000), 1.2, 0.18)
    roar_d = impact_at - 3.3                                          # la météorite approche
    fc = sweep(120, 1800, roar_d)
    roar = svf(noise(roar_d), fc, q=0.9) * ramp(roar_d, 0.0, 1.0, 2.2)
    rumble = lp(noise(roar_d), 160) * ramp(roar_d, 0.0, 1.0, 1.5)
    whistle = osc(sweep(1800, 500, roar_d), roar_d, 'tri') * ramp(roar_d, 0.0, 1.0, 2.5)
    crackle = Track(roar_d)
    for _ in range(int(roar_d * 90)):
        at = rand(0, 1) ** 0.6 * (roar_d - 0.01)
        crackle.add(bp(noise(0.006), 1500, 6000) * expdec(0.006, 0.0015), at, rand(0.2, 0.9) * at / roar_d)
    track.add(roar, 3.3, 0.6)
    track.add(rumble, 3.3, 0.9)
    track.add(whistle, 3.3, 0.08)
    track.add(crackle.buf, 3.3, 0.35)
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


if __name__ == '__main__':
    main(sys.argv[1:])
