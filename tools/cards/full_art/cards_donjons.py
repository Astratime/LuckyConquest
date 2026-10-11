"""Cartes des donjons de l'Exploration (definitions/donjons.json), par couleur : Pique, Trèfle, Coeur, Carreau."""
import math
import numpy as np
from art import *

CARDS = {}

PIQUE = ['#7a7ab8', '#4e4e8a', '#33335e', '#1e1e3a', '#0e0e1e']
TREFLE = ['#7ad070', '#3fae4a', '#23793a', '#124a24', '#082a14']
COEUR = ['#f07080', '#c8283a', '#8e1420', '#520a14', '#28040a']
CARREAU = ['#9ad8ff', '#4aa0e8', '#2a6ab8', '#18407a', '#0a2044']


def card(name):
    def deco(fn):
        CARDS[name] = fn
        return fn
    return deco


def corner_suits(c, s, col):
    p = Piece()
    for (x, y) in [(4, 4), (71, 4), (4, 103), (71, 103)]:
        suit(p, x, y, s, red=col, black=col)
    p.alpha(170)
    c.add(p)


@card('pierre_a_aiguiser')
def pierre_a_aiguiser():
    c = Card().bg(PIQUE, 'rays', center=(40, 60))
    st = Piece()
    st.spoly([(8, 72), (72, 64), (74, 80), (10, 90)], ['#a8a098', '#88807a', '#686058', '#48403a'], (0.2, 1))
    st.spoly([(10, 90), (74, 80), (74, 86), (12, 96)], ['#585048', '#383028'], (0, 1))
    st.out()
    c.add(st)
    # trois lames aiguisées
    for k, (x, deg) in enumerate([(24, -18), (40, 0), (56, 18)]):
        b = Piece()
        pts = rot([(x - 3, 66), (x + 3, 66), (x + 3, 22), (x, 10), (x - 3, 22)], x, 66, deg)
        b.spoly(pts, STEEL, (1, 0))
        p0 = rot([(x, 14), (x, 64)], x, 66, deg)
        b.line(p0, '#ffffff')
        b.out()
        c.add(b)
    for (x, y, dx, dy) in [(30, 68, -8, -4), (46, 66, 6, -6), (40, 70, 0, -10), (52, 66, 10, -2)]:
        s = Piece()
        s.line([(x, y), (x + dx, y + dy)], '#ffe07a').pt(x + dx, y + dy, '#ffffff')
        c.add(s)
    corner_suits(c, 'S', '#c8c8f0')
    return c.done()


@card('dague_de_l_ombre')
def dague_de_l_ombre():
    c = Card().bg(SHADOW, 'none', center=(40, 40))
    sh = Piece()
    sh.spoly([(32, 104), (48, 104), (54, 40), (40, 6), (26, 40)], ['#3e3058', '#261c3a', '#140e22'], (0, 1))
    sh.alpha(160)
    c.add(sh)
    d = Piece()
    d.spoly([(36, 70), (44, 70), (46, 26), (40, 10), (34, 26)], ['#d8c8ff', '#9a8ac8', '#6a5a98', '#3e3068'], (1, 0.1))
    d.line([(40, 14), (40, 68)], '#ffffff')
    d.spoly([(22, 70), (58, 70), (54, 76), (26, 76)], ['#6a5a98', '#3e3068', '#261c3a'], (0, 1))
    d.cyl([37, 76, 43, 94], ['#5e4e80', '#3e3058', '#261c3a', '#140e22'], hi=0.3)
    d.sph(40, 98, 5, 5, ['#ffb0ff', '#c050e0', '#7a20a0'])
    d.out('#05030a')
    c.add(d)
    # yeux dans l'ombre
    e = Piece()
    for (x, y) in [(16, 30), (24, 30), (56, 46), (64, 46)]:
        e.rect([x - 1, y, x + 1, y + 1], '#ff5ad0')
    c.add(e)
    for (x, y) in [(12, 60), (68, 20), (66, 84), (14, 92)]:
        c.spark(x, y, '#9a8ac8')
    corner_suits(c, 'S', '#9a8ac8')
    return c.done('#05030a', '#9a8ac8')


@card('guillotine')
def guillotine():
    c = Card().bg(PIQUE, 'bricks')
    g = Piece()
    g.cyl([10, 8, 18, 104], WOOD, hi=0.3)
    g.cyl([62, 8, 70, 104], WOOD, hi=0.3)
    g.cyl([6, 4, 74, 12], WOOD, vertical=False, hi=0.3)
    g.spoly([(4, 96), (76, 96), (76, 108), (4, 108)], WOOD, (0, 1))
    g.out()
    c.add(g)
    b = Piece()
    b.spoly([(18, 22), (62, 22), (62, 40), (18, 52)], STEEL, (0.3, 1))
    b.line([(18, 52), (62, 40)], '#ffffff')
    b.rect([18, 18, 62, 22], '#4a515c')
    b.rect([38, 12, 42, 18], '#c3c9d2')
    b.out()
    c.add(b)
    # lunette
    l = Piece()
    l.rect([18, 74, 62, 86], '#8a5528')
    l.d.chord([32, 72, 48, 88], 180, 360, fill=hexc('#20182a'))
    l.out()
    c.add(l)
    c.spark(54, 30, '#ffffff', True)
    corner_suits(c, 'S', '#c8c8f0')
    return c.done()


@card('trefle_a_quatre_feuilles')
def trefle_a_quatre_feuilles():
    c = Card().bg(TREFLE, 'rays', center=(40, 46))
    c.glow(40, 46, 26, 34, '#fff1c2', 110)
    cl = Piece()
    for (dx, dy) in [(-11, -11), (11, -11), (-11, 11), (11, 11)]:
        cx, cy = 40 + dx, 46 + dy
        cl.sph(cx - dx * 0.25, cy - dy * 0.05, 9, 9, GREEN[:4])
        cl.sph(cx - dx * 0.05, cy - dy * 0.25, 9, 9, GREEN[:4])
    cl.line([(40, 56), (46, 76), (44, 100)], '#23793a', 3)
    cl.out()
    for (dx, dy) in [(-11, -11), (11, -11), (-11, 11), (11, 11)]:
        cl.line([(40, 46), (40 + dx * 0.8, 46 + dy * 0.8)], '#c9ffb0')
    c.add(cl)
    for (x, y, b) in [(12, 14, True), (68, 14, True), (14, 86, False), (66, 90, True), (40, 12, False)]:
        c.spark(x, y, '#ffffff', b)
    corner_suits(c, 'C', '#c9ffb0')
    return c.done()


@card('bourse_garnie')
def bourse_garnie():
    c = Card().bg(TREFLE, 'dots', center=(40, 60))
    b = Piece()
    b.sph(40, 70, 26, 28, ['#d9a066', '#b5793e', '#8a5528', '#5e3618', '#3a200e'])
    b.poly([(28, 44), (52, 44), (58, 30), (22, 30)], '#b5793e')
    b.rect([26, 42, 54, 46], '#d8333f')
    b.out()
    c.add(b)
    g = Piece()
    for (x, y) in [(28, 28), (36, 24), (44, 25), (52, 28), (32, 20), (46, 18), (40, 14)]:
        coin(g, x, y, 4, 2.4)
    g.out()
    c.add(g)
    # épée brodée (gains -> force)
    e = Piece()
    e.line([(30, 86), (50, 58)], '#f7d76a', 2).line([(29, 74), (39, 81)], '#f7d76a', 2).pt(51, 56, '#fff6c4')
    e.sph(29, 88, 2, 2, GOLD[:3])
    c.add(e)
    for (x, y, b) in [(14, 20, True), (66, 18, True), (12, 90, False), (68, 92, False)]:
        c.spark(x, y, '#ffffff', b)
    corner_suits(c, 'C', '#c9ffb0')
    return c.done()


@card('transfusion')
def transfusion():
    c = Card().bg(COEUR, 'none', center=(40, 40))
    bag = Piece()
    bag.shade(('rect', [22, 14, 58, 62]), ['#ffffff', '#f0e8ec', '#d8c8d0'], (1, 0.3))
    bag.shade(('rect', [24, 30, 56, 60]), RED, (1, 0.3))
    bag.rect([36, 8, 44, 14], '#d8c8d0')
    bag.out()
    hs = Piece()
    hs.sprite(35, 38, ['.ww.ww.', 'wwwwwww', 'wwwwwww', '.wwwww.', '..www..', '...w...'], {'w': '#ffffff'})
    bag.im.alpha_composite(hs.im)
    c.add(bag)
    tube = Piece()
    tube.line([(40, 62), (40, 72), (54, 84), (60, 100)], '#e05060', 2)
    tube.out()
    c.add(tube)
    h = Piece()
    h.sph(28, 88, 7, 6, RED[:4]).sph(38, 88, 7, 6, RED[:4])
    h.poly([(21, 90), (45, 90), (33, 104)], '#c8283a')
    h.out()
    c.add(h)
    for (x, y) in [(64, 104), (68, 96)]:
        d = Piece()
        d.poly([(x, y - 3), (x + 2, y + 1), (x - 2, y + 1)], '#f05a50').ell([x - 2, y - 1, x + 2, y + 3], '#f05a50')
        d.out()
        c.add(d)
    corner_suits(c, 'H', '#ffb0b8')
    return c.done()


@card('baiser_vampire')
def baiser_vampire():
    c = Card().bg(['#5a1430', '#3a0a20', '#240614', '#14030a', '#080104'], 'diamonds', center=(40, 50))
    c.glow(40, 50, 22, 30, '#d8333f', 120)
    l = Piece()
    l.poly([(10, 52), (24, 38), (34, 42), (40, 38), (46, 42), (56, 38), (70, 52), (56, 54), (40, 52), (24, 54)], '#c8283a')
    l.poly([(10, 52), (24, 54), (40, 52), (56, 54), (70, 52), (56, 70), (40, 74), (24, 70)], '#a01828')
    l.shade(('rect', [0, 0, 80, 112]), RED[:4], (0.2, 1), mask=np.array(l.im)[:, :, 3] > 0)
    l.line([(12, 52), (68, 52)], '#3a0410')
    l.line([(22, 44), (32, 44)], '#ffb0a8').line([(28, 62), (36, 66)], '#ffb0a8')
    l.out('#080104')
    c.add(l)
    f = Piece()
    f.poly([(28, 53), (32, 53), (30, 62)], '#ffffff').poly([(48, 53), (52, 53), (50, 62)], '#ffffff')
    f.out('#080104')
    c.add(f)
    for (x, y, l_) in [(30, 64, 10), (50, 64, 14)]:
        d = Piece()
        d.rect([x, y, x + 1, y + l_], '#e0303c').ell([x - 1, y + l_, x + 2, y + l_ + 3], '#e0303c')
        d.out('#080104')
        c.add(d)
    # chauves-souris
    for (x, y) in [(16, 18), (62, 22), (40, 96)]:
        b = Piece()
        b.poly([(x - 7, y), (x - 3, y - 3), (x, y), (x + 3, y - 3), (x + 7, y), (x + 3, y + 2), (x, y + 3), (x - 3, y + 2)], '#140308')
        b.pt(x - 1, y, '#ff3a3a').pt(x + 1, y, '#ff3a3a')
        c.add(b)
    corner_suits(c, 'H', '#ff8a98')
    return c.done('#050102', '#ff8a98')


@card('pacte_de_sang')
def pacte_de_sang():
    c = Card().bg(COEUR, 'rays', center=(40, 50), n=12)
    p = Piece()
    p.spoly([(12, 18), (68, 14), (66, 96), (14, 100)], PAPER, (0.5, 1))
    p.cyl([8, 10, 72, 18], PAPER, vertical=False, hi=0.4)
    p.cyl([10, 96, 70, 104], PAPER, vertical=False, hi=0.4)
    p.out()
    for y in range(28, 70, 6):
        p.line([(20, y), (58, y - 1)], '#b39a78')
    c.add(p)
    # signature en sang et sceau
    s = Piece()
    s.line([(20, 82), (26, 78), (30, 84), (36, 76), (40, 82), (46, 80)], '#c8283a')
    s.sph(54, 82, 7, 7, RED[:4])
    s.sprite(51, 79, ['.r.r.', 'rrrrr', '.rrr.', '..r..'], {'r': '#ffb0a8'})
    s.out()
    c.add(s)
    q = Piece()
    q.spoly([(56, 64), (60, 66), (76, 30), (72, 26)], ['#ffffff', '#f0e8ec', '#c8bfb2'], (1, 0))
    q.line([(56, 64), (54, 70)], '#20182a', 2)
    q.out()
    c.add(q)
    d = Piece()
    d.poly([(40, 4), (44, 12), (36, 12)], '#e0303c').sph(40, 13, 4, 4, RED[:4])
    d.out()
    c.add(d)
    corner_suits(c, 'H', '#ffb0b8')
    return c.done()


@card('rempart')
def rempart():
    c = Card().bg(CARREAU, 'none', center=(40, 30))
    w = Piece()
    w.spoly([(4, 46), (76, 46), (76, 108), (4, 108)], ['#c8c0b8', '#a8a098', '#88807a', '#686058'], (0.3, 1))
    for k in range(5):
        x = 4 + k * 16
        w.spoly([(x, 30), (x + 10, 30), (x + 10, 46), (x, 46)], ['#c8c0b8', '#a8a098', '#88807a'], (0.3, 1))
    for y in range(52, 108, 8):
        w.line([(4, y), (76, y)], '#585048')
        for x in range(4 + (y // 8 % 2) * 6, 76, 12):
            w.line([(x, y), (x, y + 8)], '#585048')
    w.out()
    c.add(w)
    s = Piece()
    s.spoly([(24, 54), (56, 54), (56, 78), (40, 96), (24, 78)], CARREAU, (0.4, 1))
    s.spoly([(24, 54), (56, 54), (56, 58), (24, 58)], GOLD, (0, 1))
    gem(s, 40, 72, 7, ['#c4e4ff', '#4aa0e8', '#18407a'])
    s.out()
    c.add(s)
    fl = Piece()
    fl.rect([38, 6, 40, 30], '#5e3618').poly([(40, 6), (54, 10), (40, 14)], '#4aa0e8')
    fl.out()
    c.add(fl)
    c.spark(64, 20, '#ffffff', True)
    corner_suits(c, 'D', '#c4e4ff')
    return c.done()


@card('miroir_taille')
def miroir_taille():
    c = Card().bg(CARREAU, 'rays', center=(40, 50), n=24)
    m = Piece()
    pts = [(40, 12), (66, 30), (66, 70), (40, 92), (14, 70), (14, 30)]
    m.poly(pts, '#e0a83a')
    inner = [(40, 18), (60, 32), (60, 68), (40, 86), (20, 68), (20, 32)]
    m.spoly(inner, GLASS, (0.5, 1))
    for (a, b) in [((40, 18), (40, 86)), ((20, 32), (60, 68)), ((60, 32), (20, 68))]:
        m.line([a, b], '#ffffff')
    m.poly([(40, 18), (60, 32), (40, 50)], '#d8f4ff')
    m.out()
    c.add(m)
    # rayon renvoyé
    r = Piece()
    r.line([(0, 100), (30, 60)], '#fff6c4', 2).line([(30, 60), (80, 96)], '#fff6c4', 2)
    r.alpha(200)
    c.add(r)
    for (x, y, b) in [(28, 26, True), (54, 60, False), (66, 14, True)]:
        c.spark(x, y, '#ffffff', b)
    corner_suits(c, 'D', '#c4e4ff')
    return c.done()


@card('diamant_brut')
def diamant_brut():
    c = Card().bg(ROCK, 'none', center=(40, 50))
    c.glow(40, 52, 24, 30, '#9ad8ff', 120)
    r = Piece()
    r.sph(40, 72, 32, 22, ROCK[:4])
    r.out()
    c.add(r)
    d = Piece()
    pts = [(40, 14), (60, 30), (56, 62), (40, 74), (22, 60), (18, 32)]
    d.poly(pts, '#4aa0e8')
    d.poly([(40, 14), (60, 30), (40, 40)], '#c4e4ff')
    d.poly([(40, 14), (40, 40), (18, 32)], '#9ad8ff')
    d.poly([(18, 32), (40, 40), (22, 60)], '#4aa0e8')
    d.poly([(60, 30), (56, 62), (40, 40)], '#2a6ab8')
    d.poly([(40, 40), (56, 62), (40, 74)], '#18407a')
    d.poly([(40, 40), (40, 74), (22, 60)], '#2a6ab8')
    d.out()
    c.add(d)
    for (x, y) in [(14, 84), (64, 80), (28, 92)]:
        s = Piece()
        gem(s, x, y, 3, ['#c4e4ff', '#4aa0e8', '#18407a'])
        s.out()
        c.add(s)
    for (x, y, b) in [(32, 22, True), (58, 18, False), (14, 40, False)]:
        c.spark(x, y, '#ffffff', b)
    corner_suits(c, 'D', '#c4e4ff')
    return c.done()
