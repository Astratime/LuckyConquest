"""Cartes spéciales de la boutique (definitions/special.json) et Joker maudit."""
import math
import numpy as np
from art import *

CARDS = {}


def card(name):
    def deco(fn):
        CARDS[name] = fn
        return fn
    return deco


def deck_fan(c, n):
    """Paquet d'où jaillissent `n` cartes en éventail."""
    # paquet
    p = Piece()
    for k in range(4):
        p.rect([22 - k, 70 - k * 2, 58 - k, 100 - k * 2], ['#3a145e', '#6a2aa0'][k % 2])
    p.rect([19, 64, 55, 94], '#9a4ad8')
    p.rect([21, 66, 53, 92], '#6a2aa0')
    for j in range(68, 92, 4):
        for i in range(23, 53, 4):
            p.pt(i + (j // 4) % 2 * 2, j, '#c07cf0')
    p.out()
    c.add(p)
    angles = {2: [-18, 14], 3: [-26, 0, 24]}[n]
    suits = ['H', 'S', 'D']
    for k, a in enumerate(angles):
        cx = 37 + a * 0.7
        cy = 40 - (6 if a == 0 else 0)
        playing_card(c, cx, cy, 22, 32, a, suits[k])
    for (x, y) in [(10, 20), (70, 24), (66, 58), (12, 56)]:
        c.spark(x, y, '#fff1c2', True)


@card('draw_2')
def draw_2():
    c = Card().bg(PURPLE[1:] + ['#1e0a34'], 'rays', center=(40, 40))
    deck_fan(c, 2)
    return c.done()


@card('draw_3')
def draw_3():
    c = Card().bg(BLUE[1:] + ['#0a1838'], 'rays', center=(40, 40))
    deck_fan(c, 3)
    return c.done()


def coin_stack(c, x, base, n, w=7):
    p = Piece()
    for k in range(n):
        coin(p, x, base - k * 3, w, 2.4)
    p.out()
    c.add(p)


@card('gain_500')
def gain_500():
    c = Card().bg(['#7ad070', '#3fae4a', '#23793a', '#124a24', '#082a14'], 'dots', center=(40, 60))
    c.glow(40, 62, 24, 30, '#f7d76a', 120)
    coin_stack(c, 28, 92, 7)
    coin_stack(c, 50, 94, 4)
    p = Piece()
    coin_face(p, 40, 46, 17)
    p.sprite(34, 38, ['..gg..', '.gggg.', 'gg.g..', '.ggg..', '..ggg.', '..g.gg', '.gggg.', '..gg..'],
             {'g': '#8a5a1c'})
    p.out()
    c.add(p)
    for (x, y, b) in [(16, 24, True), (64, 28, True), (66, 70, False), (14, 70, False)]:
        c.spark(x, y, '#fffbe6', b)
    return c.done()


@card('gain_2000')
def gain_2000():
    c = Card().bg(['#ffe9a0', '#e0a83a', '#a0601e', '#5e3214', '#2a1408'], 'rays', center=(40, 50))
    # coffre débordant
    p = Piece()
    p.spoly([(12, 70), (68, 70), (66, 100), (14, 100)], WOOD, (0.3, 1))
    p.rect([12, 76, 68, 79], '#e0a83a').rect([12, 92, 68, 95], '#e0a83a')
    p.rect([36, 76, 44, 88], '#b0752a').rect([38, 80, 42, 85], '#3a200e')
    p.out()
    c.add(p)
    pile = Piece()
    pile.shade(('ell', [10, 50, 70, 86]), GOLD[1:], (0, 1))
    pile.out()
    rng = np.random.RandomState(4)
    for k in range(34):
        cx, cy = rng.uniform(15, 65), rng.uniform(54, 72)
        if ((cx - 40) / 30) ** 2 + ((cy - 68) / 18) ** 2 < 1:
            coin(pile, cx, cy, 3.4, 2, edge=True)
    c.add(pile)
    q = Piece()
    gem(q, 26, 62, 4, ['#ff9a9a', '#e0303c', '#8e1420'])
    gem(q, 54, 60, 4, ['#9ad0ff', '#3a6fd8', '#1d3a8a'])
    q.out()
    c.add(q)
    # pièces qui volent
    for (x, y) in [(14, 30), (66, 26), (24, 16), (56, 40), (40, 24), (70, 46), (8, 46)]:
        f = Piece()
        coin_face(f, x, y, 4.5)
        f.out()
        c.add(f)
    for (x, y, b) in [(32, 36, True), (50, 14, True), (8, 62, False), (72, 64, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('bingo')
def bingo():
    c = Card().bg(['#ff8ad0', '#d8338a', '#8e1458', '#520a34', '#28041a'], 'rays', center=(40, 52), n=20)
    m = Piece()
    # corps de la machine
    m.spoly([(10, 34), (70, 34), (72, 100), (8, 100)], RED, (0.5, 1))
    m.spoly([(14, 18), (66, 18), (70, 34), (10, 34)], GOLD, (0.3, 1))
    m.rect([14, 40, 66, 66], '#20182a')
    for i in range(3):
        x0 = 16 + i * 17
        m.cyl([x0, 42, x0 + 14, 64], ['#ffffff', '#f4ead8', '#c8bfb2', '#968c80'], hi=0.5)
        slot_seven(m, x0 + 4, 49)
    m.rect([14, 70, 66, 74], '#e0a83a')
    m.rect([20, 80, 60, 94], '#520a14')
    for k in range(5):
        coin(m, 26 + k * 7, 91 - (k % 2), 3, 1.8)
    m.out()
    c.add(m)
    # ampoules du fronton
    lamps = Piece()
    for k in range(7):
        lamps.sph(16 + k * 8, 26, 2.2, 2.2, ['#ffffff', '#fff1a8', '#f2c14e'] if k % 2 == 0 else ['#ffe0f0', '#ff6ab0', '#c8287a'])
    lamps.out()
    c.add(lamps)
    # levier
    lv = Piece()
    lv.rect([72, 44, 75, 70], '#929aa6').sph(73.5, 40, 4, 4, ['#ff9a9a', '#e0303c', '#8e1420'])
    lv.out()
    c.add(lv)
    for (x, y, b) in [(8, 12, True), (72, 12, True), (40, 8, True), (6, 74, False), (40, 106, False)]:
        c.spark(x, y, '#fff1a8', b)
    return c.done()


@card('magnet')
def magnet():
    c = Card().bg(BLUE[1:] + ['#0a1838'], 'waves')
    # lignes de champ
    f = Piece()
    for r in (30, 36, 42):
        f.arc([40 - r, 74 - r, 40 + r, 74 + r], 200, 340, '#9ad0ff')
    f.alpha(150)
    c.add(f)
    p = Piece()
    p.arc([14, 20, 66, 72], 180, 360, '#d8333f', 12)
    p.rect([14, 46, 26, 70], '#d8333f').rect([54, 46, 66, 70], '#d8333f')
    p.shade(('rect', [14, 20, 66, 70]), RED, (0.7, 1), mask=np.array(p.im)[:, :, 3] > 0)
    p.rect([14, 70, 26, 80], '#e8ecf2').rect([54, 70, 66, 80], '#e8ecf2')
    p.line([(15, 71), (15, 79)], '#ffffff').line([(55, 71), (55, 79)], '#ffffff')
    p.line([(25, 71), (25, 79)], '#929aa6').line([(65, 71), (65, 79)], '#929aa6')
    p.line([(18, 30), (24, 25)], '#ffb0a8')
    p.out()
    c.add(p)
    # pièces et symboles attirés
    for (x, y) in [(20, 92), (40, 98), (60, 92), (30, 86), (50, 88)]:
        q = Piece()
        coin_face(q, x, y, 4)
        q.out()
        c.add(q)
    s = Piece()
    cherry(s, 40, 86)
    s.out()
    c.add(s)
    for (x, y) in [(20, 84), (60, 84), (40, 80)]:
        c.spark(x, y, '#ffffff')
    return c.done()


def joker_art(c, hat_cols, face_cols, ink=INK, eyes='happy', collar=None):
    hat = Piece()
    hat.poly([(22, 35), (31, 33), (20, 18), (8, 12), (12, 20)], hat_cols[0][1])
    hat.poly([(30, 34), (50, 34), (46, 18), (40, 6), (34, 18)], hat_cols[1][1])
    hat.poly([(49, 33), (58, 35), (68, 20), (72, 12), (60, 18)], hat_cols[2][1])
    hat.poly([(22, 35), (31, 33), (21, 22)], hat_cols[0][2])
    hat.poly([(40, 34), (50, 34), (46, 18), (40, 6)], hat_cols[1][2])
    hat.poly([(49, 33), (58, 35), (62, 24)], hat_cols[2][2])
    hat.line([(12, 15), (20, 21)], hat_cols[0][0]).line([(39, 9), (35, 20)], hat_cols[1][0]).line([(69, 15), (62, 21)], hat_cols[2][0])
    for i in range(0, 34, 3):
        hat.rect([23 + i, 33, 25 + i, 36], '#f4ead8' if (i // 3) % 2 == 0 else '#20182a')
    hat.out(ink)
    for (bx, by) in [(8, 11), (40, 5), (72, 11)]:
        hat.sph(bx, by, 3.2, 3.2, GOLD[:4])
    hat.out(ink)
    face = Piece()
    face.sph(40, 50, 15, 16, face_cols)
    face.out(ink)
    return hat, face


@card('cursed_joker')
def cursed_joker():
    c = Card().bg(['#5a1420', '#3a0a14', '#24060c', '#140308', '#080104'], 'diamonds', center=(40, 44))
    c.glow(40, 50, 18, 24, '#d8333f', 140)
    hat, face = joker_art(c, [['#7a6a8a', '#3a2e48', '#20182a'], ['#d8333f', '#8e1420', '#520a14'],
                              ['#7a6a8a', '#3a2e48', '#20182a']], ['#e8e0e8', '#b8acc0', '#8a7a98', '#5a4a6a'], ink='#050205')
    # yeux rouges et sourire de crocs
    face.poly([(30, 43), (37, 45), (30, 47)], '#140308').poly([(50, 43), (43, 45), (50, 47)], '#140308')
    face.pt(32, 45, '#ff3a3a').pt(48, 45, '#ff3a3a').pt(33, 45, '#ff8a8a').pt(47, 45, '#ff8a8a')
    face.d.chord([28, 50, 52, 63], 0, 180, fill=hexc('#140308'))
    for k in range(7):
        x = 30 + k * 3
        face.poly([(x, 56), (x + 2, 56), (x + 1, 59)], '#f4ead8')
    face.line([(28, 54), (31, 56)], '#050205').line([(52, 54), (49, 56)], '#050205')
    # larmes noires
    face.line([(31, 48), (31, 53)], '#3a0a14').line([(49, 48), (49, 53)], '#3a0a14')
    ruff = Piece()
    for i in range(8):
        x0 = 14 + i * 6.5
        ruff.poly([(x0, 64), (x0 + 6.5, 64), (x0 + 3.2, 77 + (2 if i % 2 else 0))], ['#3a2e48', '#8e1420'][i % 2])
    ruff.rect([16, 62, 64, 65], '#b8acc0')
    ruff.out('#050205')
    body = Piece()
    body.poly([(4, 112), (18, 70), (62, 70), (76, 112)], '#20182a')
    for j in range(74, 112, 8):
        for i in range(10, 72, 8):
            body.poly([(i, j), (i + 3, j + 4), (i, j + 8), (i - 3, j + 4)], '#3a0a14')
    body.out('#050205')
    c.add(body).add(ruff).add(face).add(hat)
    # cartes qui brûlent
    for (x, y) in [(10, 54), (70, 50)]:
        fl = Piece()
        fl.poly([(x - 3, y + 4), (x, y - 6), (x + 3, y + 4)], '#f05a28').poly([(x - 1, y + 4), (x, y - 2), (x + 1, y + 4)], '#ffe07a')
        fl.out('#050205')
        c.add(fl)
    return c.done('#050205', '#b8acc0')


@card('recycle')
def recycle():
    c = Card().bg(GREEN[1:] + ['#08240f'], 'checker', center=(40, 50))
    r = Piece()
    r.cyl([24, 26, 56, 86], ['#ffffff', '#f4ead8', '#c8bfb2', '#968c80', '#5e564e'], hi=0.45)
    r.rect([24, 26, 56, 29], '#e0a83a').rect([24, 83, 56, 86], '#e0a83a')
    r.out()
    cherry(r, 40, 42)
    slot_seven(r, 37, 60)
    r.line([(26, 52), (54, 52)], '#c8bfb2')
    c.add(r)
    # flèches circulaires
    a = Piece()
    a.arc([8, 14, 72, 98], 200, 330, '#ffe07a', 4)
    a.arc([8, 14, 72, 98], 20, 150, '#ffe07a', 4)
    a.poly([(62, 18), (72, 24), (62, 30)], '#ffe07a')
    a.poly([(18, 94), (8, 88), (18, 82)], '#ffe07a')
    a.out()
    c.add(a)
    return c.done()


@card('bet')
def bet():
    c = Card().bg(['#4ab070', '#23793a', '#155a2a', '#0c3a1a', '#06200e'], 'none', center=(40, 60))
    c.glow(40, 66, 26, 32, '#fff1c2', 90)
    stacks = [(24, 92, 6, ('#d8333f', '#ff8a8f', '#8e1420')), (44, 96, 8, ('#3a6fd8', '#9ad0ff', '#1d3a8a')),
              (58, 84, 4, ('#20182a', '#7a6a8a', '#05030a'))]
    for (x, base, n, cols) in stacks:
        p = Piece()
        for k in range(n):
            chip(p, x, base - k * 4, 9, 3.4, cols[0], cols[1], cols[2])
        p.out()
        c.add(p)
    # dés
    for (x, y, deg, pips) in [(22, 40, -15, [(0, 0)]), (52, 34, 12, [(-2, -2), (2, 2), (0, 0)])]:
        d = Piece()
        sq = rot([(x - 8, y - 8), (x + 8, y - 8), (x + 8, y + 8), (x - 8, y + 8)], x, y, deg)
        d.spoly(sq, ['#ffffff', '#f4ead8', '#dcc8a8', '#b39a78'], (0.5, 1))
        for (px_, py_) in pips:
            q = rot([(x + px_ * 2, y + py_ * 2)], x, y, deg)[0]
            d.rect([q[0] - 1, q[1] - 1, q[0] + 1, q[1] + 1], '#d8333f')
        d.out()
        c.add(d)
    c.spark(38, 18, '#ffffff', True).spark(70, 56, '#ffffff')
    return c.done()


@card('in_the_sleeve')
def in_the_sleeve():
    c = Card().bg(['#8a7aa8', '#5e4e80', '#3e3058', '#261c3a', '#140e22'], 'stripes')
    # bras en manche de costume
    arm = Piece()
    arm.spoly([(-4, 112), (-4, 70), (40, 34), (62, 52), (22, 112)], ['#5a5a7a', '#3a3a58', '#262640', '#16162a'], (1, 0.4))
    arm.poly([(36, 30), (46, 24), (68, 46), (60, 56)], '#f4ead8')  # manchette
    arm.line([(38, 30), (62, 52)], '#c8bfb2')
    arm.sph(52, 38, 2, 2, GOLD[:4])  # bouton de manchette
    arm.out()
    # main
    hand = Piece()
    hand.sph(68, 58, 9, 8, SKIN[:4])
    hand.out()
    # cartes cachées qui dépassent de la manche
    for k, (dx, deg, s) in enumerate([(0, -40, 'S'), (5, -30, 'H'), (-5, -50, 'D')]):
        playing_card(c, 40 + dx, 26 - k * 2, 14, 20, deg, s)
    c.add(arm).add(hand)
    # as qui glisse
    playing_card(c, 24, 66, 16, 23, 25, 'S')
    for (x, y, b) in [(12, 20, True), (66, 16, True), (70, 84, False), (10, 50, False)]:
        c.spark(x, y, '#fff1c2', b)
    return c.done()


@card('lucky_charm')
def lucky_charm():
    c = Card().bg(['#9ae0a0', '#4ab070', '#23793a', '#124a24', '#082a14'], 'rays', center=(40, 46))
    c.glow(40, 50, 26, 32, '#fff1c2', 120)
    h = Piece()
    h.arc([14, 22, 66, 82], 160, 380, '#e0a83a', 11)
    m = np.array(h.im)[:, :, 3] > 0
    h.shade(('rect', [0, 0, 80, 112]), GOLD, (0.6, 1), mask=m)
    for t in range(170, 380, 30):
        a = math.radians(t)
        h.rect([40 + math.cos(a) * 21 - 1, 52 + math.sin(a) * 25 - 1, 40 + math.cos(a) * 21 + 1, 52 + math.sin(a) * 25 + 1], '#6e4214')
    h.out()
    c.add(h)
    # trèfle à quatre feuilles au centre
    cl = Piece()
    for (dx, dy) in [(-5, -5), (5, -5), (-5, 5), (5, 5)]:
        cl.sph(40 + dx, 66 + dy, 5.5, 5.5, GREEN[:4])
    cl.line([(40, 70), (44, 84)], '#23793a', 2)
    cl.out()
    c.add(cl)
    for (x, y, b) in [(10, 14, True), (70, 16, True), (12, 92, False), (68, 94, True), (40, 12, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


RAINBOW = ['#e0303c', '#f08a28', '#f7d76a', '#3fae4a', '#3a6fd8', '#9a4ad8']


def rainbow_arc(p, cx, cy, r, w=3):
    for k, col in enumerate(RAINBOW):
        rr = r - k * w
        p.arc([cx - rr, cy - rr, cx + rr, cy + rr], 180, 360, col, w)
    return p


def cloud(p, cx, cy, s=1.0):
    for (dx, dy, r) in [(-8, 2, 6), (0, -2, 8), (8, 2, 6), (-3, 4, 6), (4, 4, 6)]:
        p.sph(cx + dx * s, cy + dy * s, r * s, r * s, ['#ffffff', '#eef4ff', '#c8d4ea', '#9aa8c8'])
    return p


@card('rainbow')
def rainbow():
    c = Card().bg(['#c4e4ff', '#8ac4ff', '#5a9ae8', '#3a6fc0', '#24459a'], 'none', center=(40, 30))
    p = Piece()
    rainbow_arc(p, 40, 74, 38)
    p.out()
    c.add(p)
    cl = Piece()
    cloud(cl, 12, 76)
    cloud(cl, 68, 76)
    cl.out('#4a5a80')
    c.add(cl)
    # pot d'or qui attend au pied
    pot = Piece()
    pot.sph(40, 96, 13, 10, ['#5e5e6e', '#3a3a48', '#20202a', '#101016'])
    pot.rect([26, 84, 54, 88], '#20202a')
    pot.out()
    for k in range(7):
        coin(pot, 30 + k * 3.4, 83 - (k % 2), 2.6, 1.6)
    c.add(pot)
    for (x, y, b) in [(20, 20, True), (60, 14, True), (40, 50, False), (8, 50, False), (72, 46, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('pot_de_lutin')
def pot_de_lutin():
    c = Card().bg(['#7ad070', '#3fae4a', '#23793a', '#124a24', '#082a14'], 'diamonds', center=(40, 40))
    r = Piece()
    rainbow_arc(r, 40, 64, 46, 3)
    r.out()
    c.add(r)
    pile = Piece()
    pile.shade(('ell', [18, 40, 62, 70]), GOLD[1:], (0, 1))
    pile.out()
    for (x, y) in [(28, 50), (36, 46), (44, 47), (52, 51), (32, 56), (40, 53), (48, 56), (24, 58), (56, 58)]:
        coin(pile, x, y, 3.2, 2)
    c.add(pile)
    pot = Piece()
    pot.sph(40, 82, 24, 22, ['#5e5e6e', '#3a3a48', '#24242e', '#121218', '#08080c'], light=(-0.5, -0.4))
    pot.rect([16, 58, 64, 64], '#24242e').line([(16, 58), (64, 58)], '#7a7a8a')
    pot.rect([16, 70, 64, 74], '#3fae4a').line([(16, 70), (64, 70)], '#8ee58f')
    pot.rect([36, 69, 44, 75], '#e0a83a').rect([38, 71, 42, 73], '#24242e')
    pot.out()
    c.add(pot)
    # trèfles flottants
    for (x, y) in [(10, 30), (70, 34), (8, 92), (72, 94)]:
        cl = Piece()
        for (dx, dy) in [(0, -2), (-2, 0), (2, 0)]:
            cl.ell([x + dx - 2, y + dy - 2, x + dx + 2, y + dy + 2], '#6fd860')
        cl.out()
        c.add(cl)
    c.spark(40, 30, '#ffffff', True).spark(20, 44, '#fffbe6').spark(60, 42, '#fffbe6')
    return c.done()


@card('corruption')
def corruption():
    c = Card().bg(['#7a4ab0', '#4a2475', '#2e1450', '#1a0a30', '#0c0418'], 'bubbles', center=(40, 40))
    s = Piece()
    s.sph(40, 50, 25, 24, BONE[:4])
    s.rect([25, 64, 55, 82], '#ece6dc')
    s.shade(('rect', [25, 64, 55, 82]), BONE[1:4], (0, 1))
    s.out()
    s.ell([24, 42, 37, 56], '#140c14').ell([43, 42, 56, 56], '#140c14')
    s.rect([29, 47, 31, 49], '#8ee58f').rect([49, 47, 51, 49], '#8ee58f').pt(29, 47, '#c9ffb0').pt(49, 47, '#c9ffb0')
    s.poly([(40, 57), (44, 65), (36, 65)], '#140c14')
    s.line([(27, 72), (53, 72)], '#968c80')
    for k in range(6):
        s.line([(28 + k * 5, 68), (28 + k * 5, 81)], '#968c80')
    c.add(s)
    # bave toxique qui coule
    g = Piece()
    g.d.chord([16, 25, 64, 50], 180, 360, fill=hexc('#6fd860'))
    for (x, l) in [(17, 16), (39, 6), (61, 20), (21, 30), (58, 8)]:
        g.rect([x, 37, x + 2, 37 + l], '#3fae4a').ell([x - 1, 37 + l - 1, x + 3, 37 + l + 3], '#6fd860')
    g.arc([16, 25, 64, 50], 200, 340, '#c9ffb0')
    g.out('#0c0418')
    c.add(g)
    # flaque
    f = Piece()
    f.shade(('ell', [6, 92, 74, 106]), ['#c9ffb0', '#6fd860', '#3fae4a', '#23793a'], (0, 1))
    f.out('#0c0418')
    c.add(f)
    for (x, y) in [(12, 20), (68, 24), (66, 84), (14, 80)]:
        b = Piece()
        b.ell([x - 2, y - 2, x + 2, y + 2], '#6fd860').pt(x - 1, y - 1, '#ffffff')
        b.out('#0c0418')
        c.add(b)
    return c.done('#050208', '#c9ffb0')


def die(c, x, y, s, deg, face_pips, cols=('#ffffff', '#f4ead8', '#dcc8a8', '#b39a78'), pip='#d8333f'):
    d = Piece()
    sq = rot([(x - s, y - s), (x + s, y - s), (x + s, y + s), (x - s, y + s)], x, y, deg)
    d.spoly(sq, list(cols), (0.5, 1))
    # côté en relief
    side = rot([(x + s, y - s), (x + s + 4, y - s + 3), (x + s + 4, y + s + 3), (x + s, y + s)], x, y, deg)
    bot = rot([(x - s, y + s), (x + s, y + s), (x + s + 4, y + s + 3), (x - s + 4, y + s + 3)], x, y, deg)
    d.poly(side, cols[3]).poly(bot, cols[2])
    for (px_, py_) in face_pips:
        q = rot([(x + px_ * s * 0.55, y + py_ * s * 0.55)], x, y, deg)[0]
        d.rect([q[0] - 1, q[1] - 1, q[0] + 1, q[1] + 1], pip)
    d.out()
    c.add(d)


@card('reroll')
def reroll():
    c = Card().bg(['#ffb0a8', '#f05a50', '#c8283a', '#8e1420', '#520a14'], 'checker', center=(40, 50))
    a = Piece()
    for r in (30, 24):
        a.arc([40 - r, 54 - r, 40 + r, 54 + r], 120, 300, '#fff1c2', 2)
    a.poly([(56, 28), (66, 30), (60, 38)], '#fff1c2')
    a.out()
    c.add(a)
    die(c, 30, 42, 11, -20, [(-1, -1), (1, 1), (0, 0), (-1, 1), (1, -1)])
    die(c, 50, 72, 11, 25, [(-1, -1), (1, 1), (-1, 1), (1, -1), (-1, 0), (1, 0)], pip='#20182a')
    # traits de vitesse
    m = Piece()
    for (x0, y0, l) in [(8, 30, 10), (6, 40, 14), (10, 50, 8), (62, 90, 10), (60, 98, 14)]:
        m.line([(x0, y0), (x0 + l, y0)], '#ffe0d8')
    c.add(m)
    return c.done()


@card('rigged_reel')
def rigged_reel():
    c = Card().bg(['#c8a070', '#8a6a48', '#5e4630', '#3a2a1c', '#1c140c'], 'planks', center=(40, 50))
    r = Piece()
    r.cyl([22, 22, 58, 90], ['#ffffff', '#f4ead8', '#c8bfb2', '#968c80', '#5e564e'], hi=0.45)
    r.rect([22, 22, 58, 26], '#929aa6').rect([22, 86, 58, 90], '#929aa6')
    r.out()
    slot_seven(r, 37, 32)
    slot_seven(r, 37, 50)
    slot_seven(r, 37, 68)
    c.add(r)
    # engrenages
    for (gx, gy, gr) in [(14, 24, 8), (66, 82, 9), (68, 30, 6)]:
        g = Piece()
        for k in range(8):
            t = k * math.pi / 4
            g.rect([gx + math.cos(t) * gr - 1.5, gy + math.sin(t) * gr - 1.5, gx + math.cos(t) * gr + 1.5, gy + math.sin(t) * gr + 1.5], '#b0752a')
        g.sph(gx, gy, gr - 1, gr - 1, GOLD[1:])
        g.ell([gx - 2, gy - 2, gx + 2, gy + 2], '#3a200e')
        g.out()
        c.add(g)
    # tournevis
    t = Piece()
    t.poly(rot([(10, 92), (36, 92), (36, 96), (10, 96)], 23, 94, -30), '#c3c9d2')
    t.poly(rot([(36, 90), (54, 90), (54, 98), (36, 98)], 23, 94, -30), '#e0303c')
    t.out()
    c.add(t)
    return c.done()


@card('ghost_reel')
def ghost_reel():
    c = Card().bg(['#4a5a8a', '#2e3a64', '#1c2444', '#10162c', '#080a18'], 'stars', center=(40, 40))
    r = Piece()
    r.cyl([24, 30, 56, 92], ['#ffffff', '#d8e4ff', '#a8b8e0', '#7888b8'], hi=0.45)
    r.out('#2e3a64')
    cherry(r, 40, 48)
    slot_seven(r, 37, 70)
    r.alpha(150)
    c.add(r)
    g = Piece()
    g.sph(40, 30, 16, 15, ['#ffffff', '#eef4ff', '#c8d4ea', '#9aa8c8'])
    g.rect([24, 30, 56, 58], '#eef4ff')
    g.shade(('rect', [24, 30, 56, 58]), ['#ffffff', '#eef4ff', '#c8d4ea', '#9aa8c8'], (1, 0.2))
    for k in range(4):
        x = 24 + k * 8
        g.poly([(x, 58), (x + 8, 58), (x + 4, 64)], '#c8d4ea')
    g.ell([31, 25, 37, 33], '#10162c').ell([43, 25, 49, 33], '#10162c').ell([37, 36, 43, 43], '#10162c')
    g.pt(33, 27, '#ffffff').pt(45, 27, '#ffffff')
    g.out('#2e3a64')
    g.alpha(225)
    c.add(g)
    for (x, y) in [(12, 18), (68, 22), (10, 80), (70, 90)]:
        c.spark(x, y, '#c8d4ea')
    return c.done('#05060e', '#c8d4ea')


@card('rank_token')
def rank_token():
    c = Card().bg(['#ffe9a0', '#e0a83a', '#a0601e', '#5e3214', '#2a1408'], 'rays', center=(40, 50), n=24)
    rb = Piece()
    rb.poly([(26, 60), (36, 60), (30, 100), (24, 94), (18, 98)], '#d8333f')
    rb.poly([(44, 60), (54, 60), (62, 98), (56, 94), (50, 100)], '#3a6fd8')
    rb.out()
    c.add(rb)
    m = Piece()
    coin_face(m, 40, 50, 24)
    star = [(40 + math.cos(math.radians(-90 + k * 36)) * (14 if k % 2 == 0 else 6),
             50 + math.sin(math.radians(-90 + k * 36)) * (14 if k % 2 == 0 else 6)) for k in range(10)]
    m.spoly(star, ['#ffffff', '#fff6c4', '#f7d76a'], (0.4, 1))
    m.out()
    c.add(m)
    for (x, y, b) in [(12, 18, True), (68, 16, True), (12, 80, False), (68, 82, False)]:
        c.spark(x, y, '#ffffff', b)
    return c.done()


@card('all_in')
def all_in():
    c = Card().bg(['#3a9a5a', '#23793a', '#155a2a', '#0c3a1a', '#06200e'], 'none', center=(40, 70))
    cols = [('#d8333f', '#ff8a8f', '#8e1420'), ('#3a6fd8', '#9ad0ff', '#1d3a8a'), ('#20182a', '#7a6a8a', '#05030a'),
            ('#f2c14e', '#fff1a8', '#a8691e')]
    for i, x in enumerate([16, 32, 48, 64]):
        p = Piece()
        n = 9 + (i % 2) * 3
        for k in range(n):
            chip(p, x + ((k * 7) % 3 - 1), 100 - k * 4, 8, 3, *cols[(i + k) % 4])
        p.out()
        c.add(p)
    # deux mains qui poussent tout
    for (x, flip) in [(2, 1), (78, -1)]:
        h = Piece()
        h.sph(x + flip * 4, 92, 8, 9, SKIN[:4])
        h.rect([min(x, x - flip * 6), 96, max(x, x - flip * 6), 112], '#3a3a58')
        h.out()
        c.add(h)
    for (x, y, b) in [(40, 20, True), (16, 30, False), (64, 26, False)]:
        c.spark(x, y, '#fff1c2', b)
    return c.done()


@card('safe')
def safe():
    c = Card().bg(IRON[:4] + ['#0c0e12'], 'bricks', center=(40, 50))
    s = Piece()
    s.spoly([(8, 14), (72, 14), (72, 100), (8, 100)], ['#7a828e', '#5a616c', '#40464f', '#2a2e36'], (0.5, 1))
    s.rect([12, 18, 68, 96], '#4a515c')
    s.out()
    # rivets
    for (x, y) in [(11, 17), (69, 17), (11, 97), (69, 97), (40, 17), (40, 97)]:
        s.pt(x, y, '#c3c9d2')
    c.add(s)
    d = Piece()
    d.sph(40, 56, 22, 22, STEEL)
    d.sph(40, 56, 14, 14, IRON[:4])
    for k in range(12):
        t = k * math.pi / 6
        d.pt(40 + math.cos(t) * 18, 56 + math.sin(t) * 18, '#20242a')
    d.sph(40, 56, 6, 6, GOLD[:4])
    for k in range(4):
        t = k * math.pi / 2 + 0.6
        d.line([(40 + math.cos(t) * 6, 56 + math.sin(t) * 6), (40 + math.cos(t) * 12, 56 + math.sin(t) * 12)], '#e0a83a', 2)
    d.out()
    c.add(d)
    # pièces qui dépassent
    for (x, y) in [(66, 90), (60, 94), (70, 96)]:
        q = Piece()
        coin(q, x, y)
        q.out()
        c.add(q)
    c.spark(24, 40, '#ffffff', True)
    return c.done()


@card('insurance')
def insurance():
    c = Card().bg(['#9ad0ff', '#5a9ae8', '#3a6fc0', '#24459a', '#14265a'], 'diamonds', center=(40, 40))
    # pluie
    rain = Piece()
    for k in range(30):
        x, y = (k * 23) % 78, (k * 37) % 60 + 2
        rain.line([(x, y), (x - 1, y + 3)], '#c4e4ff')
    c.add(rain)
    u = Piece()
    u.d.pieslice([8, 14, 72, 74], 180, 360, fill=hexc('#d8333f'))
    for k in range(4):
        x = 8 + k * 16
        u.erase(('ell', [x + 2, 41, x + 14, 47]))
    u.shade(('rect', [0, 0, 80, 112]), RED, (0.8, 0.6), mask=np.array(u.im)[:, :, 3] > 0)
    for x in (24, 40, 56):
        u.line([(40, 15), (x, 44)], '#8e1420')
    u.rect([39, 44, 41, 84], '#5e3618')
    u.arc([33, 80, 41, 90], 0, 180, '#5e3618', 2)
    u.out()
    c.add(u)
    sh = Piece()
    sh.spoly([(26, 64), (54, 64), (54, 82), (40, 100), (26, 82)], GOLD, (0.4, 1))
    sh.spoly([(30, 68), (50, 68), (50, 80), (40, 94), (30, 80)], ['#7ad070', '#3fae4a', '#23793a'], (0.4, 1))
    sh.line([(34, 80), (38, 86), (47, 73)], '#ffffff', 2)
    sh.out()
    c.add(sh)
    return c.done()


@card('bribe')
def bribe():
    c = Card().bg(['#6a5a48', '#4a3e30', '#30281e', '#1c1610', '#0c0a06'], 'vstripes', center=(40, 50))
    c.glow(40, 50, 22, 30, '#f7d76a', 80)
    for (x, flip) in [(6, 1), (74, -1)]:
        a = Piece()
        a.spoly([(x - flip * 10, 112), (x - flip * 10, 70), (x + flip * 16, 52), (x + flip * 24, 62), (x + flip * 6, 112)],
                ['#5a5a7a', '#3a3a58', '#262640', '#16162a'], (flip, 0))
        a.poly([(x + flip * 14, 50), (x + flip * 20, 46), (x + flip * 28, 58), (x + flip * 24, 62)], '#f4ead8')
        a.out()
        c.add(a)
    hands = Piece()
    hands.sph(28, 52, 8, 7, SKIN[:4]).sph(52, 54, 8, 7, SKIN[:4])
    hands.out()
    env = Piece()
    env.spoly([(26, 38), (54, 34), (56, 54), (28, 58)], ['#ffffff', '#f4ead8', '#dcc8a8'], (0.4, 1))
    env.line([(26, 38), (41, 48), (54, 34)], '#b39a78')
    # billets qui dépassent
    env.poly([(30, 34), (52, 30), (53, 36), (31, 40)], '#6fd860').line([(32, 36), (50, 32)], '#23793a')
    env.out()
    c.add(env).add(hands)
    for (x, y, b) in [(40, 20, True), (20, 26, False), (60, 22, False)]:
        c.spark(x, y, '#fff1c2', b)
    return c.done()


@card('double_or_nothing')
def double_or_nothing():
    c = Card()
    x, y = grid()
    left = x < 40 + (y - 56) * 0.25
    c.img = ramp(c.img, left, np.abs(x - 40) / 50, ['#ffe9a0', '#e0a83a', '#a0601e', '#5e3214'], 0.3)
    c.img = ramp(c.img, ~left, np.abs(x - 40) / 50, ['#5e4e80', '#3e3058', '#261c3a', '#140e22'], 0.3)
    p = Piece()
    p.line([(26, 0), (54, 112)], '#fff1c2', 2)
    c.add(p)
    coin_p = Piece()
    coin_p.sph(40, 52, 22, 22, GOLD[:4])
    m = np.array(coin_p.im)[:, :, 3] > 0
    right = ~left & m
    arr = np.array(coin_p.im)
    coin_p.im = Image.fromarray(arr)
    coin_p.shade(('rect', [0, 0, 80, 112]), ['#9a8ab8', '#6a5a88', '#463864', '#2a2040'], (0.6, 1), mask=right)
    coin_p.arc([22, 34, 58, 70], 0, 360, '#6e4214')
    # x2 symbolisé par deux pièces / rien par un vide
    coin_p.sprite(26, 46, ['gg.gg', '.ggg.', '..g..', '.ggg.', 'gg.gg'], {'g': '#6e4214'})
    coin_p.ell([44, 45, 54, 57], '#140e22').ell([46, 47, 52, 55], '#9a8ab8')
    coin_p.out()
    c.add(coin_p)
    for (xx, yy) in [(16, 20), (12, 88), (24, 100)]:
        c.spark(xx, yy, '#ffffff', True)
    for (xx, yy) in [(66, 22), (70, 86)]:
        c.spark(xx, yy, '#6a5a88')
    return c.done()


@card('overheat')
def overheat():
    c = Card().bg(FIRE[1:] + ['#3a0c08'], 'rays', center=(40, 70), n=18)
    m = Piece()
    m.spoly([(12, 38), (68, 38), (70, 102), (10, 102)], ['#9a4040', '#7a2a2a', '#521a1a', '#300c0c'], (0.5, 1))
    m.spoly([(16, 24), (64, 24), (68, 38), (12, 38)], GOLD, (0.3, 1))
    m.rect([16, 44, 64, 66], '#20182a')
    for i in range(3):
        x0 = 18 + i * 15
        m.cyl([x0, 46, x0 + 12, 64], ['#fff1e0', '#ffc8a0', '#f08a50', '#a84a28'], hi=0.5)
    cherry(m, 24, 56)
    slot_seven(m, 36, 51)
    m.sprite(50, 52, ['.bb.', 'bbbb', 'bbbb', '.bb.'], {'b': '#20182a'})
    m.rect([16, 72, 64, 76], '#e0a83a')
    m.out()
    c.add(m)
    # flammes et vapeur
    for (x, h) in [(10, 22), (22, 16), (58, 18), (70, 24), (40, 12)]:
        f = Piece()
        f.poly([(x - 5, 26 + 10), (x, 26 - h + 10), (x + 5, 26 + 10)], '#f05a28')
        f.poly([(x - 3, 36), (x, 36 - h * 0.6), (x + 3, 36)], '#ffe07a')
        f.out('#3a0c08')
        c.add(f)
    st = Piece()
    for (x, y, r) in [(20, 8, 4), (30, 5, 3), (52, 7, 4), (62, 4, 3)]:
        st.sph(x, y, r, r, ['#ffffff', '#e8e2e6', '#b9b0b8'])
    st.alpha(200)
    c.add(st)
    # fissures et fumée sur l'écran
    cr = Piece()
    cr.line([(30, 44), (34, 52), (31, 58), (36, 66)], '#fffbe0')
    c.add(cr)
    return c.done('#1a0604', '#ffe07a')
