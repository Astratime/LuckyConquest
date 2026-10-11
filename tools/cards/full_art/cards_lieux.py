"""Cartes des lieux de l'Exploration (definitions/lieux.json) : Port, Mines d'Or, Casino Englouti."""
import math
import numpy as np
from art import *
from cards_boutique import cherry, slot_seven, die

CARDS = {}

SEA = ['#6fb0e0', '#3a7ab8', '#24548a', '#163660', '#0a1c38']
DUSK = ['#f0a070', '#c06048', '#7a3448', '#40203e', '#1c1024']
TAVERN = ['#c8904a', '#8a5a2a', '#5e3a1c', '#3a2210', '#1c1008']
MINE = ['#a07850', '#6e4e34', '#4a3424', '#2e2018', '#16100c']
FORGE = ['#ffb070', '#e06a30', '#9a3a1c', '#5a1e10', '#2a0c06']
ABYSS = ['#5ae0c0', '#20a890', '#127066', '#0a4048', '#04202a']
DEEP = ['#4a8ab8', '#24608a', '#143e66', '#0a2444', '#041226']


def card(name):
    def deco(fn):
        CARDS[name] = fn
        return fn
    return deco


def sea_floor(c, y=96):
    p = Piece()
    p.shade(('ell', [-20, y, 100, y + 40]), ['#e8d8a0', '#c8b070', '#9a8048', '#6a5430'], (0, 1))
    p.out('#0a1c38')
    c.add(p)


def seaweed(c, x, base, h, col='#3fae4a', dark='#23793a'):
    p = Piece()
    for k in range(h):
        dx = math.sin(k / 3.0) * 2
        p.rect([x + dx - 1, base - k, x + dx + 1, base - k], col if k % 4 < 2 else dark)
    p.out('#04202a')
    c.add(p)


def bubbles(c, pts):
    p = Piece()
    for (x, y, r) in pts:
        p.arc([x - r, y - r, x + r, y + r], 0, 360, '#c8fff0')
        p.pt(x - r * 0.4, y - r * 0.4, '#ffffff')
    c.add(p)


def bottle(p, cx, top, bottom, w, glass, label=None):
    p.cyl([cx - w, top + 14, cx + w, bottom], glass, hi=0.3)
    p.poly([(cx - w, top + 14), (cx - 3, top + 6), (cx + 3, top + 6), (cx + w, top + 14)], glass[2])
    p.cyl([cx - 3, top, cx + 3, top + 7], glass, hi=0.3)
    p.rect([cx - 3, top - 3, cx + 3, top], '#b5793e')
    if label:
        p.rect([cx - w, top + 22, cx + w, top + 34], label[0])
        p.line([(cx - w, top + 22), (cx + w, top + 22)], label[1]).line([(cx - w, top + 34), (cx + w, top + 34)], label[1])
    return p


def mug(p, x, y, w=20, h=26, foam=True):
    p.cyl([x, y, x + w, y + h], WOOD, hi=0.3)
    for k in (5, h - 5):
        p.rect([x, y + k, x + w, y + k + 2], '#929aa6')
    p.arc([x + w - 4, y + 5, x + w + 9, y + h - 5], 270, 450, '#5e3618', 3)
    if foam:
        for (dx, r) in [(3, 4), (9, 5), (15, 4), (w - 1, 3)]:
            p.sph(x + dx, y - 1, r, 3.5, ['#ffffff', '#fff6e0', '#e8d8b0'])
        p.rect([x + 4, y, x + 6, y + 8], '#fff6e0')
    return p


# ====================== PORT ======================

@card('scorbut')
def scorbut():
    c = Card().bg(['#a8c070', '#6e8a40', '#4a5e28', '#2e3a18', '#161c0a'], 'waves')
    f = Piece()
    f.sph(40, 50, 22, 24, ['#d8f0a0', '#a8c870', '#7a9a48', '#4e6a2a'])
    f.out()
    # bandana rouge
    f.d.chord([18, 24, 62, 56], 180, 360, fill=hexc('#c8283a'))
    f.line([(18, 40), (62, 40)], '#8e1420')
    for (x, y) in [(28, 32), (40, 30), (52, 33)]:
        f.pt(x, y, '#ffffff')
    f.poly([(60, 38), (72, 34), (70, 44)], '#c8283a')
    # yeux cernés, malade
    f.ell([27, 44, 36, 52], '#4e6a2a').ell([44, 44, 53, 52], '#4e6a2a')
    f.ell([29, 46, 34, 50], '#ffffff').ell([46, 46, 51, 50], '#ffffff')
    f.pt(31, 48, '#140c14').pt(48, 48, '#140c14')
    # bouche aux dents manquantes
    f.d.chord([28, 56, 52, 70], 0, 180, fill=hexc('#3a1018'))
    for k in range(5):
        if k != 1 and k != 3:
            f.rect([31 + k * 4, 63, 33 + k * 4, 65], '#e8e0a0')
    # boutons
    for (x, y) in [(24, 58), (55, 56), (38, 72)]:
        f.pt(x, y, '#c8283a')
    c.add(f)
    # citron vert rejeté
    lm = Piece()
    lm.sph(16, 92, 8, 6, ['#d8ff90', '#8ad048', '#4e9a28', '#2a6014'])
    lm.out()
    lm.line([(10, 86), (22, 98)], '#c8283a', 2).line([(22, 86), (10, 98)], '#c8283a', 2)
    c.add(lm)
    for (x, y) in [(66, 80), (60, 90), (70, 96)]:
        b = Piece()
        b.ell([x - 2, y - 2, x + 2, y + 2], '#a8c870')
        b.out()
        c.add(b)
    return c.done()


@card('longue_vue')
def longue_vue():
    c = Card().bg(SEA, 'waves', center=(40, 30))
    # horizon et bateau lointain
    hz = Piece()
    hz.rect([0, 74, 79, 111], '#163660')
    for k in range(0, 80, 6):
        hz.line([(k, 78 + (k % 12) // 3), (k + 3, 78 + (k % 12) // 3)], '#3a7ab8')
    hz.poly([(56, 72), (68, 72), (66, 75), (58, 75)], '#3a2210').line([(62, 60), (62, 72)], '#3a2210')
    hz.poly([(62, 60), (68, 68), (62, 68)], '#f4ead8')
    c.add(hz)
    s = Piece()
    pts = [(8, 92), (16, 100), (64, 30), (56, 22)]
    s.spoly(pts, GOLD, (1, -0.6))
    s.spoly([(46, 40), (54, 48), (70, 24), (62, 16)], WOOD, (1, -0.6))
    s.spoly([(64, 22), (72, 30), (76, 24), (68, 16)], GOLD, (1, -0.6))
    for (x, y) in [(30, 64), (44, 44)]:
        s.line([(x - 4, y - 4), (x + 4, y + 4)], '#6e4214', 2)
    s.out()
    c.add(s)
    lens = Piece()
    lens.ell([66, 12, 78, 24], '#9ad8f0').pt(70, 15, '#ffffff').pt(71, 16, '#ffffff')
    lens.out()
    c.add(lens)
    c.spark(72, 10, '#ffffff', True).spark(20, 30, '#ffffff')
    return c.done()


@card('rhum')
def rhum():
    c = Card().bg(TAVERN, 'planks')
    c.glow(40, 56, 22, 28, '#ffc870', 110)
    b = Piece()
    bottle(b, 40, 12, 98, 15, ['#c87a3a', '#9a4a1c', '#6a2a10', '#3a1408', '#1c0a04'], label=('#f4ead8', '#b39a78'))
    b.sprite(34, 38, ['..kk..', '.kkkk.', 'k.kk.k', '.kkkk.', '..kk..', '.k..k.'], {'k': '#20182a'})
    b.line([(29, 50), (31, 90)], '#f0a060')
    b.out()
    c.add(b)
    # verre et trèfle (gains bonus)
    g = Piece()
    g.cyl([60, 84, 72, 100], ['#ffffff', '#e8d8b0', '#c8904a', '#8a5a2a'], hi=0.3)
    g.rect([60, 90, 72, 100], '#c87a3a')
    g.out()
    c.add(g)
    cl = Piece()
    for (dx, dy) in [(-2, -2), (2, -2), (-2, 2), (2, 2)]:
        cl.ell([12 + dx - 2, 90 + dy - 2, 12 + dx + 2, 90 + dy + 2], '#6fd860')
    cl.out()
    c.add(cl)
    return c.done()


@card('carte_au_tresor')
def carte_au_tresor():
    c = Card().bg(TAVERN, 'planks')
    m = Piece()
    m.spoly([(6, 14), (74, 10), (72, 100), (8, 104)], PAPER, (0.5, 1))
    m.out()
    c.add(m)
    d = Piece()
    # île
    d.shade(('ell', [14, 30, 66, 84]), ['#c8e0a0', '#9ac070', '#6a9a48'], (0, 1))
    d.arc([12, 28, 68, 86], 0, 360, '#4a8ab8')
    # chemin en pointillés
    pts = [(22, 72), (28, 64), (26, 56), (34, 50), (44, 54), (50, 46)]
    for i in range(len(pts) - 1):
        (x0, y0), (x1, y1) = pts[i], pts[i + 1]
        for t in (0.0, 0.5):
            d.pt(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, '#8e1420')
    d.line([(46, 38), (54, 46)], '#d8333f', 2).line([(54, 38), (46, 46)], '#d8333f', 2)
    # palmier
    d.line([(30, 44), (32, 36)], '#5e3618', 2)
    d.line([(32, 36), (26, 34)], '#3fae4a', 2).line([(32, 36), (38, 33)], '#3fae4a', 2)
    # rose des vents
    d.line([(60, 82), (60, 96)], '#5e3618').line([(53, 89), (67, 89)], '#5e3618')
    d.poly([(60, 82), (62, 89), (60, 88), (58, 89)], '#d8333f')
    # vague
    for k in range(3):
        d.arc([12 + k * 6, 90, 18 + k * 6, 96], 180, 360, '#4a8ab8')
    c.add(d)
    for (x, y) in [(6, 14), (74, 10), (72, 100), (8, 104)]:
        r = Piece()
        r.sph(x, y, 3, 3, ['#ff9a9a', '#c8283a', '#8e1420'])
        r.out()
        c.add(r)
    return c.done()


@card('piege_a_rats')
def piege_a_rats():
    c = Card().bg(['#7a6a5a', '#5a4c40', '#3e342c', '#28201a', '#14100c'], 'planks')
    t = Piece()
    t.spoly([(8, 76), (72, 76), (76, 92), (4, 92)], WOOD, (0, 1))
    t.rect([4, 92, 76, 96], '#3a200e')
    t.line([(14, 76), (14, 66), (66, 66), (66, 76)], '#c3c9d2', 2)
    t.line([(14, 66), (40, 40)], '#c3c9d2', 2).line([(66, 66), (40, 40)], '#929aa6', 2)
    t.out()
    c.add(t)
    # fromage
    ch = Piece()
    ch.poly([(32, 70), (50, 64), (52, 76), (32, 76)], '#f7d76a')
    ch.poly([(32, 70), (50, 64), (48, 62), (30, 68)], '#fff6c4')
    ch.ell([38, 70, 41, 73], '#e0a83a').ell([45, 68, 47, 70], '#e0a83a')
    ch.out()
    c.add(ch)
    # rat
    r = Piece()
    r.sph(48, 40, 16, 11, ['#a89888', '#7a6a5a', '#544638', '#342a20'])
    r.sph(28, 36, 9, 7, ['#a89888', '#7a6a5a', '#544638', '#342a20'])
    r.ell([24, 26, 31, 33], '#e8a8a8').ell([30, 24, 37, 31], '#e8a8a8')
    r.pt(24, 35, '#ff3a3a').pt(19, 38, '#e86a8a')
    r.line([(19, 38), (12, 36)], '#c8bfb2').line([(19, 39), (12, 41)], '#c8bfb2')
    r.line([(64, 42), (72, 50), (70, 60)], '#e8a8a8', 2)
    r.out()
    c.add(r)
    c.spark(40, 18, '#fff1c2')
    return c.done()


@card('chope')
def chope():
    c = Card().bg(TAVERN, 'bricks')
    c.glow(36, 56, 22, 28, '#ffc870', 110)
    m = Piece()
    mug(m, 18, 36, 34, 52)
    m.out()
    c.add(m)
    # bière qui déborde
    s = Piece()
    for (x, y) in [(12, 40), (62, 34), (66, 46)]:
        s.ell([x - 2, y - 3, x + 2, y + 3], '#ffd060').pt(x - 1, y - 2, '#ffffff')
    s.out()
    c.add(s)
    for (x, y, b) in [(14, 20, True), (68, 18, True), (70, 80, False)]:
        c.spark(x, y, '#fff1c2', b)
    return c.done()


@card('tournee_generale')
def tournee_generale():
    c = Card().bg(TAVERN, 'bricks')
    c.glow(40, 40, 20, 26, '#ffc870', 120)
    for (x, y, deg) in [(4, 50, 0), (30, 58, 0), (54, 50, 0)]:
        m = Piece()
        mug(m, x, y, 20, 28)
        m.out()
        c.add(m)
    # éclaboussure de trinquage
    s = Piece()
    for k in range(9):
        t = math.radians(200 + k * 18)
        s.ell([40 + math.cos(t) * 14 - 2, 40 + math.sin(t) * 12 - 2, 40 + math.cos(t) * 14 + 2, 40 + math.sin(t) * 12 + 2], '#ffd060')
    s.out()
    c.add(s)
    c.spark(40, 22, '#ffffff', True).spark(20, 30, '#fff1c2').spark(62, 30, '#fff1c2')
    return c.done()


@card('fut_de_poudre')
def fut_de_poudre():
    c = Card().bg(['#c06048', '#8a3a2a', '#5a2018', '#34100c', '#180604'], 'planks')
    b = Piece()
    b.sph(40, 66, 26, 34, WOOD)
    b.rect([14, 40, 66, 44], '#4a515c').rect([14, 88, 66, 92], '#4a515c').rect([12, 64, 68, 68], '#4a515c')
    b.ell([18, 28, 62, 40], '#8a5528')
    b.ell([20, 30, 60, 38], '#3a200e')
    b.out()
    c.add(b)
    # crâne peint
    sk = Piece()
    sk.sprite(32, 70, ['..wwww..', '.wwwwww.', 'ww..ww..', 'wwwwwwww', '.ww..ww.', '..wwww..', '..w.w.w.'], {'w': '#f4ead8'})
    sk.pt(36, 72, '#f4ead8').pt(38, 72, '#140c14').pt(39, 72, '#140c14')
    c.add(sk)
    # mèche allumée
    m = Piece()
    m.line([(40, 34), (46, 22), (52, 18), (56, 10)], '#c8bfb2', 2)
    m.out()
    c.add(m)
    f = Piece()
    f.sph(56, 9, 5, 5, FIRE[:4])
    f.out('#3a0c08')
    c.add(f)
    for (x, y) in [(62, 6), (50, 4), (64, 14)]:
        c.spark(x, y, '#ffe07a')
    return c.done()


@card('lanterne')
def lanterne():
    c = Card().bg(['#4a5a8a', '#2e3a64', '#1c2444', '#10162c', '#080a18'], 'stars')
    c.glow(40, 58, 16, 30, '#ffd070', 150)
    l = Piece()
    l.arc([30, 6, 50, 26], 180, 360, '#4a515c', 2)
    l.spoly([(26, 22), (54, 22), (50, 30), (30, 30)], IRON, (0.5, 1))
    l.shade(('rect', [28, 30, 52, 86]), ['#fffbe0', '#ffe07a', '#ffa83a', '#c86a20'], (0.6, 1))
    for x in (28, 40, 52):
        l.line([(x, 30), (x, 86)], '#30353e', 2)
    l.spoly([(24, 86), (56, 86), (58, 94), (22, 94)], IRON, (0.5, 1))
    l.out()
    c.add(l)
    fl = Piece()
    fl.poly([(36, 66), (40, 46), (44, 66)], '#f05a28').poly([(38, 66), (40, 54), (42, 66)], '#ffffff')
    c.add(fl)
    for (x, y, b) in [(12, 40, True), (68, 44, True), (14, 82, False), (66, 86, False)]:
        c.spark(x, y, '#ffe07a', b)
    return c.done()


@card('rayon_du_phare')
def rayon_du_phare():
    c = Card().bg(['#3a4a7a', '#24305a', '#161e40', '#0c1228', '#060814'], 'stars', center=(40, 30))
    beam = Piece()
    beam.poly([(46, 30), (80, 6), (80, 40)], '#fff6c4')
    beam.poly([(34, 30), (0, 10), (0, 44)], '#fff6c4')
    beam.alpha(140)
    c.add(beam)
    sea = Piece()
    sea.rect([0, 92, 79, 111], '#163660')
    for k in range(0, 80, 8):
        sea.arc([k, 92, k + 8, 98], 180, 360, '#3a7ab8')
    c.add(sea)
    t = Piece()
    t.spoly([(30, 36), (50, 36), (56, 96), (24, 96)], ['#ffffff', '#f4ead8', '#c8bfb2', '#968c80'], (1, 0.2))
    for y in (50, 72):
        t.poly([(30 - (y - 36) * 0.1, y), (50 + (y - 36) * 0.1, y), (51 + (y - 36) * 0.1, y + 10), (29 - (y - 36) * 0.1, y + 10)], '#d8333f')
    t.rect([28, 32, 52, 36], '#30353e')
    t.rect([32, 20, 48, 32], '#fff6c4')
    t.line([(36, 20), (36, 32)], '#30353e').line([(44, 20), (44, 32)], '#30353e')
    t.poly([(30, 20), (40, 10), (50, 20)], '#d8333f')
    t.rect([36, 82, 44, 96], '#3a200e')
    t.out()
    c.add(t)
    rock = Piece()
    rock.shade(('ell', [10, 90, 70, 108]), ROCK, (0.5, 1))
    rock.out()
    c.add(rock)
    c.spark(40, 26, '#ffffff', True)
    return c.done()


@card('sabre_d_abordage')
def sabre_d_abordage():
    c = Card().bg(SEA, 'rays', center=(40, 50))
    s = Piece()
    blade = [(30, 76), (36, 80), (66, 24), (70, 8), (60, 18)]
    s.spoly(blade, STEEL, (1, -0.3))
    s.line([(34, 77), (63, 24)], '#ffffff')
    # garde en coquille
    g = Piece()
    g.d.chord([16, 70, 44, 94], 120, 330, fill=hexc('#e0a83a'))
    g.shade(('rect', [16, 70, 44, 94]), GOLD, (0.5, 1), mask=np.array(g.im)[:, :, 3] > 0)
    s.im.alpha_composite(g.im)
    s.line([(22, 92), (14, 104)], '#5e3618', 4)
    s.sph(12, 106, 3, 3, GOLD[:4])
    s.out()
    c.add(s)
    for (x, y, b) in [(68, 6, True), (54, 40, False), (14, 30, True)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('pavillon_noir')
def pavillon_noir():
    c = Card().bg(DUSK, 'none', center=(40, 80))
    f = Piece()
    f.rect([8, 8, 11, 108], '#5e3618')
    f.sph(9.5, 7, 3, 3, GOLD[:4])
    pts = [(11, 14)]
    for k in range(0, 61, 4):
        pts.append((11 + k, 14 + math.sin(k / 9) * 4))
    for k in range(60, -1, -4):
        pts.append((11 + k, 64 + math.sin(k / 9) * 4))
    f.spoly(pts, ['#3a3448', '#26202e', '#18141e', '#0c0a10'], (1, 0.3))
    f.out()
    c.add(f)
    sk = Piece()
    sk.sph(41, 34, 10, 9, BONE[:4])
    sk.rect([35, 40, 47, 46], '#ece6dc')
    sk.ell([35, 31, 40, 36], '#18141e').ell([42, 31, 47, 36], '#18141e')
    sk.poly([(41, 37), (42, 40), (40, 40)], '#18141e')
    for k in range(3):
        sk.line([(37 + k * 3, 43), (37 + k * 3, 46)], '#968c80')
    sk.line([(24, 46), (58, 60)], '#ece6dc', 3).line([(58, 46), (24, 60)], '#ece6dc', 3)
    c.add(sk)
    sea = Piece()
    sea.rect([0, 92, 79, 111], '#40203e')
    for k in range(0, 80, 8):
        sea.arc([k, 92, k + 8, 98], 180, 360, '#7a3448')
    c.add(sea)
    return c.done()


@card('mutinerie')
def mutinerie():
    c = Card().bg(FIRE[1:] + ['#2a0806'], 'rays', center=(40, 40))
    # chaînes brisées
    ch = Piece()
    for (x0, y0, dx, dy) in [(4, 84, 5, -3), (76, 84, -5, -3)]:
        for k in range(4):
            x, y = x0 + dx * k, y0 + dy * k
            ch.ell([x - 3, y - 2, x + 3, y + 2], '#929aa6')
            ch.erase(('ell', [x - 1, y - 1, x + 1, y + 1]))
    ch.out()
    c.add(ch)
    # deux sabres croisés levés
    for flip in (1, -1):
        s = Piece()
        cx = 40
        pts = [(cx - flip * 22, 96), (cx - flip * 18, 98), (cx + flip * 20, 18), (cx + flip * 16, 14)]
        s.spoly(pts, STEEL, (flip, -0.5))
        s.line([(cx - flip * 26, 88), (cx - flip * 12, 94)], '#e0a83a', 3)
        s.out()
        c.add(s)
    # poing levé
    p = Piece()
    p.sph(40, 48, 11, 12, SKIN[:4])
    for k in range(4):
        p.line([(32 + k * 5, 40), (32 + k * 5, 46)], '#b07a5a')
    p.rect([34, 58, 46, 80], '#5e3618')
    p.out()
    c.add(p)
    return c.done()


# ====================== MINES ======================

def mine_bg(c, glow=True):
    c.bg(MINE, 'bricks', center=(40, 40))
    if glow:
        c.glow(40, 50, 26, 32, '#ffc870', 70)
    return c


@card('pioche_du_mineur')
def pioche_du_mineur():
    c = mine_bg(Card())
    p = Piece()
    p.spoly(rot([(36, 14), (44, 14), (44, 106), (36, 106)], 40, 60, 30), WOOD, (1, 0))
    p.out()
    c.add(p)
    h = Piece()
    head = rot([(4, 30), (40, 18), (76, 30), (74, 34), (40, 26), (6, 34)], 40, 26, 30)
    h.spoly(head, IRON, (0.3, 1))
    h.rect([36, 20, 46, 34], '#4a515c')
    h.out()
    c.add(h)
    # pépites
    for (x, y) in [(14, 92), (24, 98), (64, 90), (58, 100)]:
        g = Piece()
        g.sph(x, y, 4, 3, GOLD[:4])
        g.out()
        c.add(g)
    for (x, y, b) in [(68, 70, True), (12, 64, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('casque')
def casque():
    c = mine_bg(Card(), glow=False)
    beam = Piece()
    beam.poly([(40, 30), (0, 0), (80, 0)], '#fff6c4')
    beam.alpha(110)
    c.add(beam)
    h = Piece()
    h.d.chord([10, 30, 70, 90], 180, 360, fill=hexc('#f2c14e'))
    h.shade(('rect', [10, 30, 70, 60]), ['#fff1a8', '#f7d76a', '#e0a83a', '#b0752a'], (0.5, 1), mask=np.array(h.im)[:, :, 3] > 0)
    h.spoly([(4, 58), (76, 58), (72, 66), (8, 66)], ['#f7d76a', '#e0a83a', '#b0752a'], (0, 1))
    h.rect([38, 30, 42, 58], '#e0a83a')
    h.out()
    c.add(h)
    lamp = Piece()
    lamp.rect([32, 34, 48, 46], '#4a515c')
    lamp.sph(40, 40, 5, 5, ['#ffffff', '#fffbe0', '#ffe07a'])
    lamp.out()
    c.add(lamp)
    # cailloux qui rebondissent
    for (x, y) in [(18, 22), (60, 18), (30, 12)]:
        r = Piece()
        r.sph(x, y, 4, 3, ROCK[:4])
        r.out()
        c.add(r)
    sh = Piece()
    sh.shade(('ell', [10, 74, 70, 86]), ['#4a3424', '#2e2018', '#16100c'], (0, 1))
    c.add(sh)
    return c.done()


@card('veine_d_or')
def veine_d_or():
    c = Card().bg(ROCK, 'none')
    x, y = grid()
    yy, xx = np.mgrid[0:H, 0:W]
    v = np.abs(y - (60 + 14 * np.sin(x / 11) - (x - 40) * 0.6))
    arr = np.array(c.img)
    from px import ramp as rp
    c.img = rp(c.img, v < 7, v / 7, ['#fff6c4', '#f7d76a', '#e0a83a', '#b0752a', '#6e4214'], 0.4)
    for (bx, by, br) in [(14, 20, 9), (60, 26, 11), (20, 96, 10), (66, 92, 8), (36, 18, 6)]:
        p = Piece()
        p.sph(bx, by, br, br * 0.8, ROCK[:4])
        p.out('#2c2420')
        c.add(p)
    for (gx, gy) in [(18, 70), (40, 58), (62, 44), (30, 66), (52, 50)]:
        g = Piece()
        g.sph(gx, gy, 3.5, 3, GOLD[:4])
        g.out('#6e4214')
        c.add(g)
    for (sx, sy, b) in [(24, 62, True), (46, 54, False), (66, 40, True)]:
        c.spark(sx, sy, '#ffffff', b)
    return c.done()


@card('tamis')
def tamis():
    c = Card().bg(['#6fb0e0', '#3a7ab8', '#24548a', '#163660', '#0a1c38'], 'waves', center=(40, 70))
    t = Piece()
    t.ell([6, 40, 74, 82], '#8a5528')
    t.ell([10, 44, 70, 78], '#5e3618')
    m = np.array(t.im)
    t.shade(('ell', [12, 46, 68, 76]), ['#9a8a70', '#7a6a50', '#5a4c38'], (0, 1))
    for k in range(14, 68, 4):
        t.line([(k, 46), (k, 76)], '#4a3e30')
    for k in range(48, 76, 4):
        t.line([(12, k), (68, k)], '#4a3e30')
    t.arc([6, 40, 74, 82], 0, 360, '#b5793e', 3)
    t.out()
    c.add(t)
    for (x, y) in [(30, 58), (42, 62), (50, 56), (36, 68), (24, 64)]:
        g = Piece()
        g.sph(x, y, 3.2, 2.6, GOLD[:4])
        g.out('#6e4214')
        c.add(g)
    # eau qui coule
    w = Piece()
    for x in (20, 32, 46, 58):
        w.line([(x, 82), (x - 1, 96)], '#c4e4ff')
    c.add(w)
    for (x, y, b) in [(36, 50, True), (56, 64, False), (20, 30, True)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('etai')
def etai():
    c = mine_bg(Card())
    p = Piece()
    p.cyl([10, 20, 20, 108], WOOD, hi=0.3)
    p.cyl([60, 20, 70, 108], WOOD, hi=0.3)
    p.cyl([4, 12, 76, 22], WOOD, vertical=False, hi=0.3)
    p.line([(20, 30), (30, 22)], '#8a5528', 3).line([(60, 30), (50, 22)], '#8a5528', 3)
    for (x, y) in [(15, 17), (65, 17)]:
        p.sph(x, y, 2, 2, IRON[:3])
    p.out()
    c.add(p)
    # rochers retenus
    for (x, y, r) in [(30, 8, 8), (46, 6, 9), (60, 4, 6), (16, 4, 6)]:
        q = Piece()
        q.sph(x, y, r, r * 0.8, ROCK[:4])
        q.out()
        c.add(q)
    # bouclier au centre
    s = Piece()
    s.spoly([(28, 44), (52, 44), (52, 64), (40, 80), (28, 64)], ['#9ad0ff', '#3a6fd8', '#24459a', '#14265a'], (0.4, 1))
    s.line([(40, 46), (40, 76)], '#c4e4ff')
    s.out()
    c.add(s)
    return c.done()


@card('corde_de_rappel')
def corde_de_rappel():
    c = mine_bg(Card(), glow=False)
    r = Piece()
    for k in range(5):
        r.ell([14 - k, 50 + k * 6, 66 + k, 66 + k * 6], ['#d9a066', '#b5793e'][k % 2])
        r.erase(('ell', [20 - k, 54 + k * 6, 60 + k, 62 + k * 6]))
    r.shade(('rect', [0, 0, 80, 112]), ['#f0c890', '#d9a066', '#b5793e', '#8a5528'], (0.4, 1), mask=np.array(r.im)[:, :, 3] > 0)
    for k in range(0, 60, 3):
        r.pt(14 + k, 58 + (k % 6), '#8a5528')
    r.line([(40, 50), (40, 18)], '#d9a066', 3)
    r.out()
    c.add(r)
    hook = Piece()
    hook.rect([38, 8, 42, 20], '#929aa6')
    hook.arc([28, 2, 40, 14], 90, 270, '#c3c9d2', 3)
    hook.arc([40, 2, 52, 14], 270, 450, '#c3c9d2', 3)
    hook.out()
    c.add(hook)
    c.spark(24, 30, '#ffffff', True).spark(60, 36, '#ffffff')
    return c.done()


@card('marteau_de_forge')
def marteau_de_forge():
    c = Card().bg(FORGE, 'rays', center=(40, 80))
    an = Piece()
    an.spoly([(8, 78), (72, 78), (64, 88), (16, 88)], IRON, (0, 1))
    an.spoly([(26, 88), (54, 88), (58, 106), (22, 106)], IRON, (0, 1))
    an.poly([(72, 78), (80, 80), (72, 84)], '#4a515c')
    an.out()
    c.add(an)
    h = Piece()
    h.spoly(rot([(37, 30), (43, 30), (43, 92), (37, 92)], 40, 50, -35), WOOD, (1, 0))
    h.spoly(rot([(22, 14), (58, 14), (58, 34), (22, 34)], 40, 50, -35), IRON, (0.5, 1))
    h.out()
    c.add(h)
    for (x, y, dx, dy) in [(40, 74, -10, -8), (44, 74, 8, -10), (36, 76, -14, -2), (48, 76, 14, -4)]:
        s = Piece()
        s.line([(x, y), (x + dx, y + dy)], '#ffe07a')
        s.pt(x + dx, y + dy, '#ffffff')
        c.add(s)
    return c.done()


@card('trempe')
def trempe():
    c = Card().bg(['#8ab8d8', '#5a88b0', '#3a6088', '#243e60', '#14223a'], 'none', center=(40, 30))
    st = Piece()
    for (x, y, r) in [(30, 20, 7), (44, 14, 8), (54, 24, 6), (36, 8, 5), (24, 30, 5)]:
        st.sph(x, y, r, r, ['#ffffff', '#eef4ff', '#c8d4ea', '#9aa8c8'])
    st.alpha(210)
    c.add(st)
    b = Piece()
    b.cyl([10, 64, 70, 104], WOOD, hi=0.3)
    b.rect([10, 70, 70, 73], '#4a515c').rect([10, 96, 70, 99], '#4a515c')
    b.ell([10, 58, 70, 70], '#3a6fd8')
    b.ell([14, 60, 66, 68], '#6fb0ff')
    b.out()
    c.add(b)
    s = Piece()
    blade = [(36, 64), (44, 64), (46, 30), (40, 22), (34, 30)]
    s.spoly(blade, ['#fff6c4', '#ffe07a', '#ffa83a', '#f05a28', '#a82818'], (0, -1))
    s.rect([30, 62, 50, 64], '#4a515c')
    s.out()
    c.add(s)
    for (x, y) in [(26, 62), (54, 62), (40, 56)]:
        c.spark(x, y, '#ffffff')
    return c.done()


@card('lame_forgee')
def lame_forgee():
    c = Card().bg(FORGE, 'rays', center=(40, 40), n=20)
    c.glow(40, 44, 20, 28, '#ffe07a', 130)
    s = Piece()
    s.spoly([(34, 76), (46, 76), (46, 20), (40, 6), (34, 20)], STEEL, (1, 0.1))
    s.line([(40, 10), (40, 74)], '#ffffff')
    s.spoly([(18, 76), (62, 76), (62, 82), (18, 82)], GOLD, (0, 1))
    s.cyl([36, 82, 44, 100], WOOD, hi=0.3)
    s.sph(40, 103, 5, 4, GOLD[:4])
    s.out()
    c.add(s)
    for (x, y, b) in [(40, 4, True), (20, 30, False), (60, 24, True), (14, 60, False), (66, 62, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('dynamite')
def dynamite():
    c = Card().bg(FORGE, 'stripes', center=(40, 60))
    d = Piece()
    for k, x in enumerate([18, 32, 46]):
        d.cyl([x, 40 + (k % 2) * 4, x + 14, 100], RED, hi=0.3)
        d.rect([x, 40 + (k % 2) * 4, x + 14, 44 + (k % 2) * 4], '#f4ead8')
    d.rect([16, 60, 62, 66], '#5e3618').rect([16, 82, 62, 88], '#5e3618')
    d.out()
    c.add(d)
    m = Piece()
    m.line([(39, 44), (42, 30), (52, 22), (56, 12)], '#c8bfb2', 2)
    m.out()
    c.add(m)
    f = Piece()
    f.sph(57, 10, 5, 5, FIRE[:4])
    f.out('#3a0c08')
    c.add(f)
    for (x, y) in [(64, 6), (50, 4), (66, 16), (60, 20)]:
        c.spark(x, y, '#ffe07a')
    return c.done()


@card('lampe_a_carbure')
def lampe_a_carbure():
    c = mine_bg(Card(), glow=False)
    c.glow(40, 40, 14, 30, '#fff1a8', 150)
    l = Piece()
    l.cyl([26, 54, 54, 98], ['#fff1a8', '#f7d76a', '#e0a83a', '#b0752a', '#6e4214'], hi=0.3)
    l.cyl([30, 38, 50, 54], ['#fff1a8', '#f7d76a', '#e0a83a', '#b0752a'], hi=0.3)
    l.rect([24, 52, 56, 56], '#b0752a').rect([24, 94, 56, 98], '#b0752a')
    l.ell([20, 26, 44, 50], '#c3c9d2')
    l.ell([24, 30, 40, 46], '#ffffff')
    l.arc([50, 40, 66, 70], 270, 450, '#b0752a', 2)
    l.out()
    c.add(l)
    f = Piece()
    f.poly([(30, 38), (32, 24), (34, 38)], '#ffffff').poly([(29, 38), (32, 30), (35, 38)], '#9ad0ff')
    c.add(f)
    for (x, y, b) in [(12, 20, True), (64, 18, True), (14, 60, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('coeur_d_or')
def coeur_d_or():
    c = Card().bg(ROCK, 'none', center=(40, 50))
    c.glow(40, 54, 24, 32, '#ffe07a', 130)
    h = Piece()
    h.sph(28, 44, 14, 13, GOLD[:4])
    h.sph(52, 44, 14, 13, GOLD[:4])
    h.spoly([(14, 48), (66, 48), (40, 84)], GOLD[1:], (0.4, 1))
    h.line([(40, 40), (40, 80)], '#b0752a')
    h.line([(22, 40), (30, 50), (40, 46)], '#fff6c4')
    h.out()
    c.add(h)
    for (bx, by, br) in [(10, 14, 8), (70, 18, 9), (12, 98, 9), (68, 100, 10)]:
        p = Piece()
        p.sph(bx, by, br, br * 0.8, ROCK[:4])
        p.out('#2c2420')
        c.add(p)
    for (x, y, b) in [(24, 36, True), (58, 34, False), (40, 92, True)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('bouchons_d_oreille')
def bouchons_d_oreille():
    c = Card().bg(['#c090d0', '#8a5aa0', '#5e3a74', '#3a2048', '#1c0e24'], 'none')
    w = Piece()
    for r in (16, 24, 32):
        w.arc([40 - r, 56 - r, 40 + r, 56 + r], 200, 340, '#e8c8f0', 2)
        w.arc([40 - r, 56 - r, 40 + r, 56 + r], 20, 160, '#e8c8f0', 2)
    w.line([(8, 20), (72, 92)], '#d8333f', 4)
    w.alpha(200)
    c.add(w)
    for (x, y, deg) in [(26, 52, -30), (54, 60, 25)]:
        p = Piece()
        pts = rot([(x - 6, y - 10), (x + 6, y - 10), (x + 5, y + 10), (x - 5, y + 10)], x, y, deg)
        p.spoly(pts, ['#fff1a8', '#f7d76a', '#f08a28', '#c86a20'], (1, 0.3))
        p.out()
        c.add(p)
    return c.done()


# ====================== CASINO ENGLOUTI ======================

def abyss(c, pattern='bubbles'):
    c.bg(ABYSS, pattern, center=(40, 20))
    return c


@card('bulle_d_air')
def bulle_d_air():
    c = abyss(Card(), 'waves')
    seaweed(c, 8, 108, 40)
    seaweed(c, 72, 108, 34)
    b = Piece()
    x, y = grid()
    r = np.sqrt((x - 40) ** 2 + (y - 50) ** 2)
    m = r < 26
    b.im = ramp(b.im, m, np.clip((r - 10) / 18, 0, 1) * 0.6 + np.clip(((x - 28) + (y - 38)) / 60, 0, 1) * 0.4,
                ['#c8fff0', '#8ae8d8', '#4ac0b0'], 0.4)
    arr = np.array(b.im)
    arr[m, 3] = np.where(r[m] > 22, 255, 110)
    b.im = Image.fromarray(arr)
    b.arc([28, 36, 44, 52], 180, 270, '#ffffff', 2)
    b.pt(30, 40, '#ffffff')
    c.add(b)
    # petit plongeur à l'abri
    d = Piece()
    d.sph(40, 52, 7, 7, ['#fff1a8', '#f7d76a', '#e0a83a'])
    d.ell([36, 49, 44, 55], '#9ad8f0').pt(38, 50, '#ffffff')
    d.rect([36, 58, 44, 66], '#d8333f')
    d.out('#04202a')
    c.add(d)
    bubbles(c, [(20, 24, 3), (60, 18, 4), (66, 70, 2), (14, 64, 2)])
    return c.done()


@card('perle_noire')
def perle_noire():
    c = abyss(Card())
    sea_floor(c, 92)
    o = Piece()
    o.d.chord([6, 22, 74, 78], 180, 360, fill=hexc('#9a7aa8'))
    o.shade(('rect', [6, 22, 74, 50]), ['#e8d8f0', '#b8a0c8', '#8a6aa0', '#5e4474'], (0, 1), mask=np.array(o.im)[:, :, 3] > 0)
    for k in range(5):
        o.arc([10 + k * 4, 26 + k * 2, 70 - k * 4, 74], 200, 340, '#5e4474')
    o.d.chord([6, 50, 74, 100], 0, 180, fill=hexc('#7a5a8a'))
    o.shade(('rect', [6, 75, 74, 100]), ['#b8a0c8', '#8a6aa0', '#5e4474', '#3a2a4a'], (0, 1), mask=(np.array(o.im)[:, :, 3] > 0) & (grid()[1] > 75))
    o.shade(('ell', [12, 64, 68, 86]), ['#f4e0f0', '#e0c0d8', '#c8a0c0'], (0, 1))
    o.out('#04202a')
    c.add(o)
    p = Piece()
    p.sph(40, 70, 11, 11, ['#9a9ab8', '#5a5a78', '#30304a', '#16162a', '#08080e'])
    p.pt(36, 65, '#ffffff').pt(37, 65, '#e0e0ff').pt(36, 66, '#e0e0ff')
    p.out('#04202a')
    c.add(p)
    c.glow(40, 70, 12, 16, '#c8a0ff', 120)
    bubbles(c, [(20, 18, 3), (64, 12, 2), (56, 22, 2)])
    return c.done()


@card('trident')
def trident():
    c = abyss(Card(), 'rays')
    t = Piece()
    t.cyl([37, 34, 43, 108], GOLD, hi=0.3)
    t.spoly([(20, 30), (60, 30), (56, 38), (24, 38)], GOLD, (0, 1))
    for x in (22, 40, 58):
        t.spoly([(x - 3, 30), (x + 3, 30), (x, 6 if x == 40 else 12)], GOLD, (1, 0.3))
        t.poly([(x - 5, 18 if x != 40 else 14), (x - 2, 20 if x != 40 else 16), (x - 3, 24 if x != 40 else 20)], '#e0a83a')
    t.sph(40, 34, 4, 4, ['#c8fff0', '#5ae0c0', '#20a890'])
    t.out('#04202a')
    c.add(t)
    for (x, y, b) in [(40, 4, True), (18, 10, False), (62, 10, False)]:
        c.spark(x, y, '#ffffff', b)
    bubbles(c, [(14, 60, 3), (66, 52, 4), (60, 84, 2), (18, 90, 2)])
    return c.done()


@card('cocktail_des_abysses')
def cocktail_des_abysses():
    c = abyss(Card(), 'dots')
    g = Piece()
    g.poly([(12, 30), (68, 30), (42, 62), (38, 62)], '#c8fff0')
    g.shade(('poly', [(14, 34), (66, 34), (42, 60), (38, 60)]), ['#d8a0ff', '#9a4ad8', '#6a2aa0', '#3a145e'], (0.3, 1))
    g.line([(16, 34), (64, 34)], '#ecc8ff')
    g.rect([39, 62, 41, 92], '#c8fff0')
    g.ell([24, 90, 56, 98], '#c8fff0')
    g.line([(16, 31), (36, 56)], '#ffffff')
    g.out('#04202a')
    c.add(g)
    # tentacule qui sort du verre
    t = Piece()
    pts = [(50, 38), (56, 30), (60, 20), (58, 12), (52, 10), (50, 16)]
    for i in range(len(pts) - 1):
        t.line([pts[i], pts[i + 1]], '#f05a78', 4)
    for (x, y) in [(55, 30), (59, 22), (57, 14)]:
        t.pt(x, y, '#ffb0c0')
    t.out('#04202a')
    c.add(t)
    # ombrelle et cerise
    u = Piece()
    u.d.pieslice([10, 16, 32, 36], 180, 360, fill=hexc('#f7d76a'))
    u.line([(21, 26), (30, 40)], '#5e3618')
    u.out('#04202a')
    c.add(u)
    ch = Piece()
    ch.sph(46, 30, 3, 3, ['#ff9a9a', '#e0303c', '#8e1420'])
    ch.out('#04202a')
    c.add(ch)
    bubbles(c, [(36, 46, 2), (44, 50, 1), (30, 42, 1)])
    return c.done()


@card('piece_truquee')
def piece_truquee():
    c = Card().bg(['#7ab8d8', '#3a7ab8', '#24548a', '#163660', '#0a1c38'], 'diamonds')
    p = Piece()
    p.sph(40, 52, 24, 24, GOLD[:4])
    p.arc([20, 32, 60, 72], 0, 360, '#b0752a', 2)
    # deux faces identiques : une tête de chaque côté
    p.sprite(30, 40, ['..ggg...', '.ggggg..', 'gg.gggg.', 'ggggggg.', '.gggggg.', '..gggg..', '.gggggg.', 'gggggggg'], {'g': '#b0752a'})
    p.out()
    c.add(p)
    # poids caché (plomb)
    w = Piece()
    w.sph(52, 64, 5, 5, IRON[:4])
    w.out()
    c.add(w)
    # flèches de rotation
    a = Piece()
    a.arc([10, 22, 70, 82], 210, 300, '#fff1c2', 2).arc([10, 22, 70, 82], 30, 120, '#fff1c2', 2)
    a.poly([(54, 22), (60, 26), (54, 30)], '#fff1c2').poly([(26, 82), (20, 78), (26, 74)], '#fff1c2')
    c.add(a)
    c.spark(22, 36, '#ffffff', True)
    return c.done()


@card('levier_rouille')
def levier_rouille():
    c = abyss(Card(), 'bubbles')
    sea_floor(c, 98)
    l = Piece()
    l.spoly([(30, 90), (50, 90), (54, 108), (26, 108)], ['#c87a50', '#9a5030', '#6a3420', '#3a1c10'], (0.5, 1))
    l.spoly(rot([(37, 20), (43, 20), (43, 92), (37, 92)], 40, 92, 22), ['#c87a50', '#9a5030', '#6a3420', '#3a1c10'], (1, 0))
    l.out('#04202a')
    c.add(l)
    k = Piece()
    k.sph(67, 22, 10, 10, ['#ffb0a8', '#e0303c', '#8e1420', '#520a14'])
    k.out('#04202a')
    c.add(k)
    # rouille et coquillages
    r = Piece()
    for (x, y) in [(46, 70), (52, 50), (58, 36), (34, 96), (44, 100)]:
        r.ell([x - 2, y - 1, x + 2, y + 1], '#e08a40')
    r.sph(30, 94, 3, 3, ['#ffffff', '#e8d8c8', '#b8a090'])
    r.sph(62, 32, 2, 2, ['#ffffff', '#e8d8c8', '#b8a090'])
    c.add(r)
    seaweed(c, 12, 104, 30)
    bubbles(c, [(70, 44, 2), (66, 52, 3), (20, 30, 2)])
    return c.done()


@card('jackpot_englouti')
def jackpot_englouti():
    c = abyss(Card())
    sea_floor(c, 94)
    m = Piece()
    pts = rot([(14, 30), (66, 30), (68, 98), (12, 98)], 40, 64, -8)
    m.spoly(pts, ['#5ab0a0', '#2a8070', '#1a5a50', '#0c3a34'], (0.5, 1))
    top = rot([(18, 16), (62, 16), (66, 30), (14, 30)], 40, 64, -8)
    m.spoly(top, GOLD, (0.3, 1))
    scr = rot([(18, 40), (62, 40), (62, 62), (18, 62)], 40, 64, -8)
    m.poly(scr, '#04202a')
    for i in range(3):
        x0 = 20 + i * 14
        reel = rot([(x0, 42), (x0 + 12, 42), (x0 + 12, 60), (x0, 60)], 40, 64, -8)
        m.spoly(reel, ['#ffffff', '#f4ead8', '#c8bfb2'], (1, 0))
    m.out('#04202a')
    for i in range(3):
        q = rot([(23 + i * 14, 47)], 40, 64, -8)[0]
        slot_seven(m, q[0], q[1])
    c.add(m)
    # pièces qui s'échappent
    for (x, y) in [(18, 84), (58, 90), (66, 80), (30, 92)]:
        q = Piece()
        coin_face(q, x, y, 4)
        q.out('#04202a')
        c.add(q)
    seaweed(c, 72, 100, 36)
    seaweed(c, 6, 100, 26)
    f = Piece()
    f.sph(60, 22, 6, 4, ['#ffd0a0', '#f08a28', '#c05a14'])
    f.poly([(66, 22), (72, 18), (72, 26)], '#f08a28').pt(57, 21, '#140c14')
    f.out('#04202a')
    c.add(f)
    bubbles(c, [(54, 12, 2), (50, 8, 1)])
    return c.done()


@card('cage_a_requin')
def cage_a_requin():
    c = Card().bg(DEEP, 'waves')
    cage = Piece()
    for x in range(10, 72, 12):
        cage.rect([x, 50, x + 2, 106], '#c3c9d2')
    cage.rect([6, 48, 74, 52], '#929aa6').rect([6, 104, 74, 108], '#929aa6').rect([6, 76, 74, 79], '#929aa6')
    cage.out('#041226')
    c.add(cage)
    d = Piece()
    d.sph(40, 88, 7, 7, ['#fff1a8', '#f7d76a', '#e0a83a'])
    d.ell([36, 85, 44, 91], '#9ad8f0').pt(38, 86, '#ffffff')
    d.out('#041226')
    c.add(d)
    sh = Piece()
    sh.sph(42, 30, 34, 14, ['#c8d4e0', '#8a9aac', '#5a6a80', '#384658'], light=(-0.2, -0.8))
    sh.poly([(40, 18), (50, 2), (54, 18)], '#5a6a80')
    sh.poly([(72, 30), (80, 16), (80, 44)], '#5a6a80')
    sh.poly([(36, 40), (46, 50), (48, 40)], '#5a6a80')
    sh.d.chord([10, 30, 40, 48], 0, 180, fill=hexc('#e8eef4'))
    sh.out('#041226')
    sh.d.chord([12, 32, 34, 46], 0, 180, fill=hexc('#520a14'))
    for k in range(6):
        sh.poly([(13 + k * 3.5, 38), (15.5 + k * 3.5, 38), (14 + k * 3.5, 42)], '#ffffff')
    sh.pt(20, 26, '#140c14').pt(21, 26, '#140c14')
    c.add(sh)
    bubbles(c, [(30, 60, 2), (52, 66, 3), (60, 92, 2)])
    return c.done()


@card('coffre_de_l_epave')
def coffre_de_l_epave():
    c = abyss(Card())
    sea_floor(c, 88)
    ch = Piece()
    ch.spoly([(10, 56), (70, 56), (68, 96), (12, 96)], ['#7a8a6a', '#5a6a48', '#3e4a30', '#26301c'], (0.3, 1))
    ch.spoly([(10, 30), (70, 22), (72, 36), (10, 44)], ['#7a8a6a', '#5a6a48', '#3e4a30'], (0.3, 1))
    ch.rect([10, 64, 70, 67], '#b0752a').rect([10, 86, 70, 89], '#b0752a')
    ch.rect([34, 62, 46, 76], '#e0a83a').rect([38, 66, 42, 72], '#26301c')
    ch.out('#04202a')
    c.add(ch)
    g = Piece()
    g.shade(('poly', [(12, 56), (68, 56), (66, 40), (14, 44)]), GOLD, (0, 1))
    for (x, y) in [(20, 50), (30, 46), (44, 47), (56, 46), (26, 54), (50, 54), (38, 52), (62, 52)]:
        coin(g, x, y, 3, 1.8)
    g.out('#04202a')
    c.add(g)
    q = Piece()
    gem(q, 34, 48, 3, ['#ff9a9a', '#e0303c', '#8e1420'])
    gem(q, 58, 48, 3, ['#c9ffb0', '#3fae4a', '#1f6a2a'])
    q.out('#04202a')
    c.add(q)
    # coquillages et algues
    seaweed(c, 6, 104, 34)
    seaweed(c, 74, 104, 40)
    c.glow(40, 48, 16, 24, '#ffe07a', 110)
    bubbles(c, [(40, 16, 3), (46, 8, 2), (30, 24, 1)])
    return c.done()


@card('harpon')
def harpon():
    c = Card().bg(DEEP, 'rays', center=(70, 10))
    h = Piece()
    h.spoly([(8, 104), (14, 108), (62, 28), (56, 24)], WOOD, (1, -0.6))
    h.spoly([(52, 30), (66, 22), (74, 4), (58, 20)], STEEL, (1, -0.6))
    h.poly([(56, 28), (50, 34), (58, 30)], '#929aa6').poly([(64, 24), (70, 26), (66, 20)], '#929aa6')
    h.out('#041226')
    c.add(h)
    rope = Piece()
    pts = [(12, 104)] + [(12 + k * 2, 104 - k * 2 + math.sin(k / 2) * 3) for k in range(1, 16)]
    for i in range(len(pts) - 1):
        rope.line([pts[i], pts[i + 1]], '#e8d8b0', 1)
    c.add(rope)
    # traits de vitesse
    m = Piece()
    for (x, y) in [(20, 70), (26, 60), (14, 80)]:
        m.line([(x, y), (x - 10, y + 16)], '#9ad0ff')
    c.add(m)
    bubbles(c, [(40, 62, 2), (34, 72, 3), (46, 54, 1)])
    c.spark(74, 4, '#ffffff', True)
    return c.done()


@card('ancre')
def ancre():
    c = abyss(Card(), 'waves')
    sea_floor(c, 98)
    a = Piece()
    a.cyl([37, 22, 43, 92], IRON, hi=0.3)
    a.arc([30, 6, 50, 26], 0, 360, '#6c7480', 4)
    a.cyl([22, 30, 58, 36], IRON, vertical=False, hi=0.3)
    a.arc([10, 54, 70, 98], 20, 160, '#6c7480', 6)
    a.poly([(8, 70), (16, 62), (16, 76)], '#6c7480').poly([(72, 70), (64, 62), (64, 76)], '#6c7480')
    a.out('#04202a')
    c.add(a)
    ch = Piece()
    for k in range(6):
        x, y = 44 + k * 5, 10 + k * 3
        ch.ell([x - 3, y - 2, x + 3, y + 2], '#929aa6')
    ch.out('#04202a')
    c.add(ch)
    r = Piece()
    for (x, y) in [(36, 50), (44, 76), (20, 82)]:
        r.ell([x - 2, y - 1, x + 2, y + 1], '#3fae4a')
    c.add(r)
    seaweed(c, 70, 104, 30)
    bubbles(c, [(20, 30, 2), (16, 40, 3), (62, 44, 2)])
    return c.done()
