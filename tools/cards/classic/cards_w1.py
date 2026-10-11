"""Cartes spéciales (lot 1) au style des cartes classiques : Prairie (pique, trèfle, coeur, carreau) et gains.
Aucun texte : les quantités se lisent au dessin. Voir CONSIGNES.md et base.py.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401,F403
from base import INK, WHITE, hexc, card, clip, layer, sprite, outline, corners  # noqa: E402
from PIL import Image, ImageDraw  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
P, PD, PL = hexc('#9a4ad8'), hexc('#5e2c8f'), hexc('#d8a8ff')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec8ff')
CY, CYL = hexc('#5ab0f0'), hexc('#d8f4ff')
O = hexc('#f08a2a')
ST, STL, STD = hexc('#c4ccd8'), hexc('#f2f6fa'), hexc('#7c869c')
GR, GRL, GRD = hexc('#8a8a96'), hexc('#b4b4c0'), hexc('#5c5c6a')
WD, WDL, WDD = hexc('#a0663a'), hexc('#c88a54'), hexc('#6a3e22')
BR, BRL, BRD = hexc('#8a5a34'), hexc('#b07a48'), hexc('#5a3820')
PA, PAD = hexc('#f4e4bc'), hexc('#d4b47c')
DK, DK2 = hexc('#2e2a40'), hexc('#4a4560')
PNK = hexc('#ff5ab4')

# index d'angle : l'enseigne de la famille, ou un petit motif pour les cartes de gains
SPADE = ['..k..', '.kkk.', 'kkkkk', 'kkkkk', '..k..', '.kkk.']
CLUB = ['.kkk.', '.kkk.', 'kkkkk', 'kkkkk', '..k..', '.kkk.']
HEART = ['kk.kk', 'kkkkk', 'kkkkk', '.kkk.', '..k..']
DIAM = ['..k..', '.kkk.', '.kkk.', 'kkkkk', '.kkk.', '.kkk.', '..k..']
SUITP = {'k': INK}
SUITR = {'k': R}


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def ob(fn):
    """Dessine un objet sur un calque à part et l'entoure d'encre."""
    L = layer()
    fn(ImageDraw.Draw(L))
    return outline(L)


def stack(*layers):
    L = layer()
    for lay in layers:
        L.alpha_composite(lay)
    return L


def finish(pal, L, idx, ipal, dx=0, dy=0):
    if dx or dy:
        L2 = layer()
        L2.alpha_composite(L, (max(dx, 0), max(dy, 0)), (max(-dx, 0), max(-dy, 0)))
        L = L2
    im = card(*pal)
    clip(im, L)
    corners(im, idx, ipal)
    return im


def sparkle(d, x, y, col=WHITE):
    d.point([(x, y), (x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)


def coin_stack(d, cx, yb, n, w=4, c=(Y, YD, YL), stripe=None):
    """Pile de n pièces vue de face (2 cases par pièce), centre cx, bas yb."""
    main, dark, light = c
    for i in range(n):
        y = yb - 2 * i
        d.line([(cx - w, y - 1), (cx + w, y - 1)], fill=main)
        d.line([(cx - w, y), (cx + w, y)], fill=dark)
        if stripe is not None:
            for sx in (cx - w + 1, cx, cx + w - 1):
                d.point((sx, y - 1), fill=stripe)
    yt = yb - 2 * n
    d.line([(cx - w + 1, yt - 1), (cx + w - 1, yt - 1)], fill=light)
    d.line([(cx - w, yt), (cx + w, yt)], fill=main)
    d.point((cx - w + 2, yt), fill=light)


def big_coin(d, x0, y0, s):
    """Pièce vue de face, carré englobant (x0, y0, s)."""
    d.ellipse([x0, y0, x0 + s, y0 + s], fill=YD)
    d.ellipse([x0, y0, x0 + s - 1, y0 + s - 1], fill=Y)
    d.ellipse([x0 + 2, y0 + 2, x0 + s - 2, y0 + s - 2], fill=YD)
    d.ellipse([x0 + 2, y0 + 2, x0 + s - 3, y0 + s - 3], fill=Y)
    c = x0 + s // 2
    m = y0 + s // 2
    # étoile en relief
    d.point([(c, m - 2), (c - 1, m - 1), (c, m - 1), (c + 1, m - 1), (c - 2, m), (c - 1, m), (c, m),
             (c + 1, m), (c + 2, m), (c - 1, m + 1), (c + 1, m + 1)], fill=YL)
    d.point([(x0 + 2, y0 + s // 2 - 2), (x0 + 3, y0 + 3)], fill=YL)


# =============================================================== Pique
def pierre_a_aiguiser(pal):
    """Pierre à aiguiser : trois lames plantées dans une pierre, étincelles."""
    def blade(d, bx, by, tx, ty, w=1.6):
        ang = math.atan2(ty - by, tx - bx)
        nx, ny = -math.sin(ang), math.cos(ang)
        L_ = math.hypot(tx - bx, ty - by)
        ux, uy = math.cos(ang), math.sin(ang)
        k = L_ - 4
        pts = [(bx + nx * w, by + ny * w), (bx + ux * k + nx * w, by + uy * k + ny * w), (tx, ty),
               (bx + ux * k - nx * w, by + uy * k - ny * w), (bx - nx * w, by - ny * w)]
        poly(d, pts, ST)
        poly(d, [(bx, by), (bx + ux * k, by + uy * k), (tx, ty), (bx + ux * k - nx * w, by + uy * k - ny * w),
                 (bx - nx * w, by - ny * w)], STL)
        d.line([(round(bx), round(by)), (round(bx + ux * (k - 1)), round(by + uy * (k - 1)))], fill=STD)

    def guards(d):
        for (x, y) in [(11, 31), (19, 30), (27, 31)]:
            d.rectangle([x - 3, y, x + 3, y + 1], fill=YD)
            d.line([(x - 3, y), (x + 3, y)], fill=Y)

    bl = ob(lambda d: [blade(d, 12, 32, 8, 15), blade(d, 26, 32, 30, 15)])
    bc = ob(lambda d: blade(d, 19, 32, 19, 9))
    gd = ob(guards)

    def stone(d):
        poly(d, [(8, 34), (30, 32), (30, 38), (8, 40)], GR)
        poly(d, [(8, 34), (30, 32), (30, 34), (8, 36)], GRL)
        poly(d, [(8, 39), (30, 37), (30, 38), (8, 40)], GRD)
        d.point([(12, 37), (13, 37), (20, 36), (25, 36), (26, 36), (17, 38)], fill=GRD)
    st = ob(stone)
    L = stack(bl, bc, gd, st)
    d = ImageDraw.Draw(L)
    for (x, y) in [(8, 29), (30, 28), (19, 27)]:
        sparkle(d, x, y, Y)
    d.point([(9, 27), (29, 26), (6, 31), (32, 30)], fill=YL)
    return finish(pal, L, SPADE, SUITP)


def scale(pts, k, cx=19, cy=26):
    return [(cx + (x - cx) * k, cy + (y - cy) * k) for x, y in pts]


def dague_de_l_ombre(pal):
    """Dague de l'ombre : poignard violet entouré d'une ombre aux yeux roses."""
    def shadow(d):
        poly(d, scale([(19, 6), (27, 12), (31, 22), (29, 32), (31, 40), (25, 44), (19, 42), (13, 44), (7, 40),
                       (9, 32), (7, 22), (11, 12)], 0.85), DK)
        poly(d, scale([(19, 10), (25, 15), (27, 24), (25, 33), (19, 38), (13, 33), (11, 24), (13, 15)], 0.85),
             DK2)
    sh = ob(shadow)

    def dagger(d):
        poly(d, [(17, 31), (17, 13), (19, 9), (21, 13), (21, 31)], PL)
        poly(d, [(19, 9), (21, 13), (21, 31), (19, 31)], hexc('#a890e0'))
        d.line([(19, 12), (19, 30)], fill=WHITE)
        d.rectangle([12, 31, 26, 33], fill=PD)
        d.line([(12, 31), (26, 31)], fill=P)
        d.rectangle([18, 34, 20, 37], fill=hexc('#3a2050'))
        d.point([(18, 35)], fill=P)
        d.ellipse([16, 37, 22, 43], fill=P)
        d.point([(18, 39), (17, 40)], fill=PL)
    dg = ob(dagger)
    L = stack(sh, dg)
    d = ImageDraw.Draw(L)
    for (x, y) in [(10, 23), (25, 19)]:
        d.line([(x, y), (x + 1, y)], fill=PNK)
        d.line([(x + 3, y), (x + 4, y)], fill=PNK)
    return finish(pal, L, SPADE, SUITP)


def guillotine(pal):
    """Guillotine : montants de bois, lame en biais et lunette."""
    def frame(d):
        for x in (10, 26):
            d.rectangle([x, 14, x + 2, 38], fill=WD)
            d.line([(x, 14), (x, 38)], fill=WDL)
            d.line([(x + 2, 16), (x + 2, 38)], fill=WDD)
        d.rectangle([8, 12, 30, 15], fill=WD)
        d.line([(8, 12), (30, 12)], fill=WDL)
        d.line([(8, 15), (30, 15)], fill=WDD)
        d.rectangle([8, 38, 30, 40], fill=WD)
        d.line([(8, 38), (30, 38)], fill=WDL)
        d.line([(8, 40), (30, 40)], fill=WDD)
    fr = ob(frame)

    def blade(d):
        d.rectangle([13, 17, 25, 18], fill=GRD)
        poly(d, [(13, 19), (25, 19), (25, 22), (13, 28)], ST)
        poly(d, [(13, 26), (25, 20), (25, 22), (13, 28)], STL)
        d.point([(15, 21), (16, 21)], fill=STL)
    bl = ob(blade)

    def lunette(d):
        d.rectangle([13, 30, 25, 32], fill=WD)
        d.rectangle([13, 33, 25, 35], fill=WD)
        d.line([(13, 30), (25, 30)], fill=WDL)
        d.line([(13, 35), (25, 35)], fill=WDD)
        d.line([(13, 32), (25, 32)], fill=WDD)
        d.rectangle([16, 32, 22, 34], fill=INK)
        d.rectangle([17, 31, 21, 35], fill=INK)
    lu = ob(lunette)
    rope = ob(lambda d: [d.line([(19, 9), (19, 11)], fill=PAD), d.rectangle([18, 7, 20, 8], fill=GRD)])
    L = stack(rope, fr, bl, lu)
    d = ImageDraw.Draw(L)
    sparkle(d, 22, 24, WHITE)
    return finish(pal, L, SPADE, SUITP)


# =============================================================== Trèfle
def trefle_a_quatre_feuilles(pal):
    """Trèfle à quatre feuilles : quatre feuilles en coeur et une tige courbée."""
    cx, cy = 19, 25

    def rot(x, y, k):
        dx, dy = x - cx, y - cy
        for _ in range(k):
            dx, dy = -dy, dx
        return cx + dx, cy + dy

    def stem(d):
        d.line([(19, 31), (19, 36), (21, 39), (23, 41)], fill=GD, width=2)
        d.line([(20, 31), (20, 36), (22, 39), (24, 40)], fill=G)
    sm = ob(stem)

    def leaves(d):
        for k in range(4):
            for (ex, ey) in [(16, 17), (22, 17)]:
                x, y = rot(ex, ey, k)
                d.ellipse([x - 4, y - 4, x + 4, y + 4], fill=G)
            poly(d, [rot(12, 18, k), rot(26, 18, k), rot(19, 25, k)], G)
    lv = ob(leaves)
    L = stack(sm, lv)
    d = ImageDraw.Draw(L)
    for k in range(4):
        d.line([rot(19, 24, k), rot(19, 17, k)], fill=GD)
        d.point(rot(19, 14, k), fill=INK)
        d.point(rot(19, 13, k), fill=INK)
        d.point([rot(15, 15, k), rot(16, 14, k), rot(21, 14, k)], fill=GL)
        d.line([rot(20, 24, k), rot(27, 17, k)], fill=INK)
    d.point((19, 25), fill=GD)
    for (x, y) in [(9, 40), (30, 34)]:
        sparkle(d, x, y, WHITE)
    return finish(pal, L, CLUB, SUITP)


def bourse_garnie(pal):
    """Bourse garnie : sac de cuir lié de rouge, pièces qui dépassent, épée dorée brodée."""
    def coins(d):
        for (x, y) in [(14, 15), (24, 16), (21, 13), (17, 13)]:
            d.ellipse([x - 3, y - 2, x + 3, y + 2], fill=Y)
            d.line([(x - 2, y - 1), (x + 1, y - 1)], fill=YL)
            d.line([(x - 2, y + 2), (x + 2, y + 2)], fill=YD)
    cn = ob(coins)

    def sack(d):
        d.ellipse([9, 23, 29, 41], fill=BR)
        poly(d, [(14, 24), (15, 20), (23, 20), (24, 24)], BR)
        poly(d, [(11, 17), (15, 19), (19, 16), (23, 19), (27, 17), (25, 21), (13, 21)], BRL)
        d.rectangle([13, 21, 25, 23], fill=R)
        d.line([(13, 23), (25, 23)], fill=RD)
        d.point([(14, 21), (15, 21)], fill=RL)
        d.arc([9, 23, 29, 41], 20, 120, fill=BRD, width=2)
        d.point([(12, 29), (12, 30), (13, 28)], fill=BRL)
    sk = ob(sack)
    L = stack(cn, sk)
    d = ImageDraw.Draw(L)
    d.line([(14, 37), (23, 27)], fill=Y)
    d.line([(15, 37), (23, 28)], fill=YD)
    d.line([(15, 31), (18, 34)], fill=Y)
    d.point([(13, 38)], fill=YL)
    d.point((23, 27), fill=YL)
    sparkle(d, 30, 13, WHITE)
    sparkle(d, 8, 21, WHITE)
    return finish(pal, L, CLUB, SUITP)


def fortune_du_roi(pal):
    """Fortune du Roi : couronne posée sur un tas d'or."""
    def heap(d):
        poly(d, [(8, 40), (10, 34), (14, 30), (19, 28), (24, 30), (28, 34), (30, 40)], Y)
        for row, y in enumerate(range(31, 40, 2)):
            off = row % 2 * 2
            for x in range(8 + off, 30, 4):
                d.line([(x, y), (x + 2, y)], fill=YD)
                d.point((x + 1, y - 1), fill=YL)
        d.point([(12, 37), (25, 35), (19, 38)], fill=R)
        d.point([(16, 35), (22, 39)], fill=B)
        d.point([(27, 38)], fill=G)
    hp = ob(heap)

    def crown(d):
        poly(d, [(9, 29), (9, 16), (13, 22), (16, 14), (19, 21), (22, 14), (25, 22), (29, 16), (29, 29)], Y)
        d.rectangle([9, 26, 29, 29], fill=YD)
        d.line([(9, 26), (29, 26)], fill=Y)
        d.line([(10, 20), (10, 25)], fill=YL)
        d.line([(16, 17), (16, 25)], fill=YL)
        d.line([(22, 17), (22, 25)], fill=YL)
    cr = ob(crown)
    L = stack(hp, cr)
    d = ImageDraw.Draw(L)
    for (x, y) in [(9, 14), (16, 12), (22, 12), (29, 14)]:
        d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=WHITE)
        d.point((x + 1, y + 1), fill=ST)
    for x, c in [(13, R), (19, B), (25, G)]:
        d.rectangle([x - 1, 27, x + 1, 28], fill=c)
        d.point((x - 1, 27), fill=WHITE)
    d.rectangle([18, 21, 20, 23], fill=R)
    d.point((18, 21), fill=RL)
    sparkle(d, 31, 26, WHITE)
    sparkle(d, 7, 32, WHITE)
    return finish(pal, L, CLUB, SUITP)


# =============================================================== Coeur
def transfusion(pal):
    """Transfusion : poche de sang reliée par un tube à un coeur."""
    def tube(d):
        d.line([(18, 25), (18, 27), (23, 28), (27, 28), (29, 30), (28, 32)], fill=RD)
    tb = ob(tube)

    def bag(d):
        d.rectangle([17, 7, 21, 9], fill=ST)
        d.rounded_rectangle([12, 9, 26, 24], radius=2, fill=STL)
        d.rounded_rectangle([12, 14, 26, 24], radius=2, fill=R)
        d.line([(13, 14), (25, 14)], fill=RL)
        d.line([(25, 15), (25, 23)], fill=RD)
        d.line([(14, 11), (14, 12)], fill=WHITE)
        d.rectangle([17, 24, 19, 25], fill=ST)
    bg = ob(bag)

    def heart(d):
        d.ellipse([10, 29, 19, 38], fill=R)
        d.ellipse([19, 29, 28, 38], fill=R)
        poly(d, [(10, 34), (28, 34), (19, 43)], R)
        d.point([(13, 31), (12, 32), (14, 31)], fill=RL)
        d.line([(25, 36), (20, 41)], fill=RD)
    ht = ob(heart)
    L = stack(tb, bg, ht)
    d = ImageDraw.Draw(L)
    d.point([(18, 17), (20, 17), (17, 18), (18, 18), (19, 18), (20, 18), (21, 18), (18, 19), (19, 19),
             (20, 19), (19, 20)], fill=WHITE)
    return finish(pal, L, HEART, SUITR)


def baiser_vampire(pal):
    """Baiser vampire : lèvres rouges entrouvertes, deux crocs, gouttes de sang."""
    def drips(d):
        for x, n in ((14, 5), (24, 3)):
            d.line([(x, 33), (x, 34 + n)], fill=R)
            d.rectangle([x - 1, 34 + n, x + 1, 36 + n], fill=R)
            d.point((x - 1, 34 + n), fill=RL)
    dp = ob(drips)

    def lips(d):
        poly(d, [(6, 27), (10, 22), (15, 21), (19, 23), (23, 21), (28, 22), (32, 27)], R)
        poly(d, [(6, 27), (32, 27), (28, 32), (22, 34), (16, 34), (10, 32)], R)
        d.line([(10, 23), (14, 22)], fill=RL)
        d.line([(24, 22), (27, 23)], fill=RL)
        d.line([(14, 30), (18, 31)], fill=RL)
        d.line([(10, 32), (16, 34), (22, 34), (28, 32)], fill=RD)
        poly(d, [(9, 27), (14, 26), (19, 26), (24, 26), (29, 27), (24, 30), (14, 30)], hexc('#3a0812'))
        d.line([(11, 27), (14, 26), (24, 26), (27, 27)], fill=WHITE)
    lp = ob(lips)

    def fangs(d):
        for x in (14, 24):
            poly(d, [(x - 2, 26), (x + 1, 26), (x - 0.5, 32)], WHITE)
    fg = ob(fangs)
    L = stack(dp, lp, fg)
    # le sang coule sur la pointe des crocs
    d = ImageDraw.Draw(L)
    d.point([(13, 31), (13, 32)], fill=R)
    d.point([(23, 31), (23, 32)], fill=R)
    return finish(pal, L, HEART, SUITR)


def pacte_de_sang(pal):
    """Pacte de sang : parchemin roulé, sceau et plume, goutte de sang."""
    def scroll(d):
        d.rectangle([10, 16, 26, 37], fill=PA)
        d.line([(26, 16), (26, 37)], fill=PAD)
        for i, y in enumerate(range(20, 31, 3)):
            d.line([(13, y), (23 if i % 2 else 21, y)], fill=PAD)
        d.line([(18, 34), (20, 32), (21, 34), (23, 32)], fill=RD)
    sc = ob(scroll)

    def rolls(d):
        for y in (13, 36):
            d.rounded_rectangle([8, y, 28, y + 3], radius=1, fill=PA)
            d.line([(9, y + 3), (27, y + 3)], fill=PAD)
            d.line([(9, y), (27, y)], fill=WHITE)
    rl = ob(rolls)

    def quill(d):
        poly(d, [(31, 15), (29, 14), (26, 21), (23, 30), (24, 31), (29, 23)], WHITE)
        d.line([(30, 15), (24, 30)], fill=ST)
        d.line([(24, 31), (23, 33)], fill=INK)
    ql = ob(quill)

    def seal(d):
        d.ellipse([10, 28, 17, 35], fill=R)
        d.point([(12, 30), (13, 30)], fill=RL)
        d.rectangle([13, 35, 14, 38], fill=R)
        d.point((13, 39), fill=R)
    sl = ob(seal)

    def drop(d):
        poly(d, [(19, 5), (22, 9), (16, 9)], R)
        d.ellipse([16, 7, 22, 12], fill=R)
        d.point([(17, 9), (17, 8)], fill=RL)
    dp = ob(drop)
    L = stack(sc, rl, ql, sl, dp)
    return finish(pal, L, HEART, SUITR)


# =============================================================== Carreau
def rempart(pal):
    """Rempart : muraille crénelée à fanion et grand écu bleu au losange."""
    def flag(d):
        d.line([(19, 8), (19, 13)], fill=WDD)
        poly(d, [(20, 8), (24, 10), (20, 12)], R)
    fl = ob(flag)

    def wall(d):
        d.rectangle([8, 18, 30, 39], fill=GRL)
        for x in (8, 13, 18, 23, 28):
            d.rectangle([x, 14, x + 2, 18], fill=GRL)
            d.line([(x, 14), (x + 2, 14)], fill=WHITE)
        for row, y in enumerate(range(21, 40, 4)):
            d.line([(8, y), (30, y)], fill=GR)
            off = 3 if row % 2 else 0
            for x in range(10 + off, 30, 6):
                d.line([(x, y - 3), (x, y - 1)], fill=GR)
        d.line([(8, 18), (30, 18)], fill=GR)
        d.line([(30, 18), (30, 39)], fill=GR)
    wl = ob(wall)

    def shield(d):
        poly(d, [(12, 22), (26, 22), (26, 32), (19, 40), (12, 32)], Y)
        poly(d, [(14, 24), (24, 24), (24, 31), (19, 37), (14, 31)], B)
        poly(d, [(19, 24), (24, 24), (24, 31), (19, 37)], BD)
        d.line([(12, 22), (26, 22)], fill=YL)
    sh = ob(shield)
    L = stack(fl, wl, sh)
    d = ImageDraw.Draw(L)
    poly(d, [(19, 25), (22, 29), (19, 33), (16, 29)], BL)
    poly(d, [(19, 25), (22, 29), (19, 33)], CY)
    d.point((18, 28), fill=WHITE)
    return finish(pal, L, DIAM, SUITR)


def miroir_taille(pal):
    """Miroir taillé : miroir à main en hexagone, cadre d'or, reflets."""
    hexa = [(19, 8), (28, 13), (28, 28), (19, 33), (10, 28), (10, 13)]

    def handle(d):
        d.rectangle([17, 33, 21, 39], fill=Y)
        d.line([(21, 34), (21, 39)], fill=YD)
        d.rectangle([15, 39, 23, 41], fill=Y)
        d.line([(15, 41), (23, 41)], fill=YD)
        d.point([(18, 35), (18, 37)], fill=YL)
    hd = ob(handle)

    def mirror(d):
        poly(d, hexa, Y)
        poly(d, [(19, 8), (28, 13), (28, 28), (19, 33)], YD)
        inner = [(19, 11), (25, 14.5), (25, 26.5), (19, 30), (13, 26.5), (13, 14.5)]
        poly(d, inner, CY)
        poly(d, [(19, 11), (25, 14.5), (25, 20), (13, 20), (13, 14.5)], BL)
        poly(d, [(13, 14.5), (19, 11), (19, 20), (13, 20)], CYL)
        d.line([(14, 25), (22, 13)], fill=WHITE)
        d.line([(16, 28), (24, 16)], fill=WHITE)
        d.line([(20, 29), (24, 23)], fill=CYL)
        d.point([(19, 8), (10, 13)], fill=YL)
    mr = ob(mirror)
    L = stack(hd, mr)
    d = ImageDraw.Draw(L)
    for (x, y) in [(31, 10), (7, 33), (30, 34)]:
        sparkle(d, x, y, WHITE)
    d.point([(7, 20), (31, 22)], fill=WHITE)
    return finish(pal, L, DIAM, SUITR)


def diamant_brut(pal):
    """Diamant brut : gros cristal bleu à facettes pris dans une roche."""
    def crystal(d):
        poly(d, [(13, 16), (19, 9), (25, 14), (27, 30), (20, 35), (12, 32)], B)
        poly(d, [(13, 16), (19, 9), (18, 19), (12, 32)], BL)
        poly(d, [(19, 9), (25, 14), (20, 19), (18, 19)], CYL)
        poly(d, [(18, 19), (20, 19), (20, 35), (12, 32)], CY)
        poly(d, [(20, 19), (25, 14), (27, 30), (20, 35)], B)
        poly(d, [(23, 25), (27, 30), (20, 35)], BD)
    cr = ob(crystal)

    def rock(d):
        d.ellipse([7, 30, 31, 41], fill=BR)
        d.ellipse([8, 30, 30, 39], fill=BRL)
        d.ellipse([11, 31, 27, 37], fill=BR)
        d.point([(11, 39), (24, 40), (27, 38), (15, 40)], fill=BRD)
    rk = ob(rock)
    L = stack(cr, rk)
    import numpy as np
    a = np.array(cr)
    a[33:, :] = 0
    L = stack(L, Image.fromarray(a))
    d = ImageDraw.Draw(L)
    d.line([(13, 33), (25, 33)], fill=INK)
    d.line([(14, 34), (24, 34)], fill=BR)
    for (x, y) in [(10, 36), (28, 35), (23, 39)]:
        poly(d, [(x, y - 1), (x + 1, y), (x, y + 1), (x - 1, y)], CY)
        d.point((x, y), fill=CYL)
    sparkle(d, 29, 11, WHITE)
    sparkle(d, 8, 20, WHITE)
    return finish(pal, L, DIAM, SUITR)


# =============================================================== Gains
COIN_I = ['.yyy.', 'yylyy', 'ylyyy', 'yyyyy', '.ddd.']
COIN_P = {'y': Y, 'l': YL, 'd': YD}
STACK_I = ['.lll.', 'yyyyy', 'ddddd', 'yyyyy', 'ddddd']


def gain_500(pal):
    """Gains +500 : une pièce posée sur une petite pile de trois pièces."""
    sk = ob(lambda d: coin_stack(d, 19, 39, 3, w=6))
    cn = ob(lambda d: big_coin(d, 12, 17, 14))
    L = stack(sk, cn)
    d = ImageDraw.Draw(L)
    sparkle(d, 29, 16, WHITE)
    sparkle(d, 9, 28, WHITE)
    return finish(pal, L, COIN_I, COIN_P)


def gain_2000(pal):
    """Gains +2000 : quatre piles de pièces, une grosse pièce devant."""
    bk = ob(lambda d: [coin_stack(d, 11, 36, 7), coin_stack(d, 27, 36, 8)])
    md = ob(lambda d: coin_stack(d, 19, 38, 11))
    fr = ob(lambda d: [coin_stack(d, 10, 41, 2), coin_stack(d, 28, 39, 2)])
    cn = ob(lambda d: big_coin(d, 14, 30, 10))
    L = stack(bk, md, fr, cn)
    d = ImageDraw.Draw(L)
    for (x, y) in [(8, 16), (31, 14), (25, 11)]:
        sparkle(d, x, y, WHITE)
    return finish(pal, L, STACK_I, COIN_P)


def lucky_charm(pal):
    """Porte-bonheur : fer à cheval doré tourné vers le haut, petit trèfle au milieu."""
    def shoe(d):
        d.arc([8, 17, 30, 41], 0, 180, fill=Y, width=5)
        d.rectangle([8, 16, 12, 30], fill=Y)
        d.rectangle([26, 16, 30, 30], fill=Y)
        d.rectangle([7, 14, 13, 16], fill=Y)
        d.rectangle([25, 14, 31, 16], fill=Y)
        d.line([(12, 17), (12, 30)], fill=YD)
        d.line([(30, 17), (30, 30)], fill=YD)
        d.arc([8, 17, 30, 41], 10, 90, fill=YD, width=1)
        d.line([(9, 17), (9, 28)], fill=YL)
        d.line([(27, 17), (27, 28)], fill=YL)
    L = ob(shoe)
    d = ImageDraw.Draw(L)
    for (x, y) in [(10, 19), (10, 24), (28, 19), (28, 24), (11, 34), (27, 34), (15, 38), (23, 38)]:
        d.point((x, y), fill=INK)
    leaves = [ob(lambda d, c=c: d.ellipse([c[0] - 2, c[1] - 2, c[0] + 2, c[1] + 2], fill=G))
              for c in [(19, 23), (16, 26), (22, 26), (19, 29)]]
    stem = ob(lambda d: d.line([(19, 31), (20, 34)], fill=GD))
    L = stack(L, stem, *leaves)
    d = ImageDraw.Draw(L)
    for (x, y) in [(19, 22), (16, 25), (22, 25), (19, 28)]:
        d.point((x - 1, y), fill=GL)
    for (x, y) in [(19, 12), (7, 34), (31, 36)]:
        sparkle(d, x, y, WHITE)
    return finish(pal, L, ['y...y', 'y...y', 'yy.yy', '.yyy.'], {'y': YD})


def pot_de_lutin(pal):
    """Pot de Lutin : chaudron noir rempli d'or sous un arc-en-ciel."""
    def rainbow(d):
        for i, c in enumerate([R, O, Y, G, B, P]):
            r = 13 - i
            d.arc([19 - r, 28 - r, 19 + r, 28 + r], 180, 360, fill=c, width=1)
    rb = ob(rainbow)

    def gold(d):
        d.ellipse([13, 22, 25, 29], fill=Y)
        for (x, y) in [(15, 25), (18, 23), (21, 25), (17, 27)]:
            d.line([(x, y), (x + 2, y)], fill=YL)
            d.line([(x, y + 1), (x + 2, y + 1)], fill=YD)
    gd = ob(gold)

    def pot(d):
        d.ellipse([10, 27, 28, 41], fill=DK)
        d.rectangle([9, 26, 29, 28], fill=DK2)
        d.line([(9, 26), (29, 26)], fill=GRD)
        d.rectangle([11, 31, 27, 33], fill=G)
        d.line([(11, 33), (27, 33)], fill=GD)
        d.rectangle([17, 30, 21, 34], fill=Y)
        d.rectangle([18, 31, 20, 33], fill=DK)
        d.point([(13, 36), (14, 37)], fill=DK2)
    pt = ob(pot)
    L = stack(rb, gd, pt)
    d = ImageDraw.Draw(L)
    for (x, y) in [(8, 37), (30, 35), (19, 10)]:
        sparkle(d, x, y, WHITE)
    return finish(pal, L, ['.yyy.', 'kkkkk', 'kkkkk', '.kkk.'], {'y': Y, 'k': INK})


def bet(pal):
    """Pari : deux dés et trois piles de jetons."""
    def die(d, x, y, pips):
        d.rectangle([x, y, x + 8, y + 8], fill=WHITE)
        d.line([(x, y + 8), (x + 8, y + 8)], fill=ST)
        d.line([(x + 8, y), (x + 8, y + 8)], fill=ST)
        for (px, py) in pips:
            d.rectangle([x + px, y + py, x + px + 1, y + py + 1], fill=R)
    d1 = ob(lambda d: die(d, 10, 12, [(1, 1), (3, 3), (5, 5)]))
    d2 = ob(lambda d: die(d, 20, 15, [(1, 1), (5, 1), (1, 5), (5, 5)]))
    chips = [(11, 39, 5, (R, RD, RL)), (27, 38, 4, (DK2, DK, GRL)), (19, 41, 7, (B, BD, BL))]
    cl = [ob(lambda d, c=c: coin_stack(d, c[0], c[1], c[2], w=4, c=c[3], stripe=WHITE)) for c in chips]
    L = stack(d1, d2, *cl)
    d = ImageDraw.Draw(L)
    sparkle(d, 31, 11, WHITE)
    sparkle(d, 7, 24, WHITE)
    return finish(pal, L, ['kkkkk', 'k.kwk', 'kkkkk', 'kwk.k', 'kkkkk'], {'k': R, 'w': WHITE})


def safe(pal):
    """Coffre-fort : caisse d'acier, porte à cadran et poignée dorée."""
    def box(d):
        d.rectangle([8, 11, 30, 39], fill=GR)
        d.line([(8, 11), (30, 11)], fill=GRL)
        d.line([(8, 11), (8, 39)], fill=GRL)
        d.line([(30, 12), (30, 39)], fill=GRD)
        d.line([(11, 40), (14, 40)], fill=GRD)
        d.line([(24, 40), (27, 40)], fill=GRD)
    bx = ob(box)

    def door(d):
        d.rectangle([11, 14, 27, 36], fill=GRL)
        d.line([(27, 14), (27, 36)], fill=GR)
        d.line([(11, 36), (27, 36)], fill=GR)
        for y in (17, 31):
            d.rectangle([9, y, 11, y + 2], fill=STD)
    dr = ob(door)

    def dial(d):
        d.ellipse([13, 18, 25, 30], fill=ST)
        d.ellipse([16, 21, 22, 27], fill=GRD)
        d.line([(19, 18), (19, 30)], fill=Y)
        d.line([(13, 24), (25, 24)], fill=Y)
        d.ellipse([17, 22, 21, 26], fill=Y)
        d.point((18, 23), fill=YL)
    dl = ob(dial)
    L = stack(bx, dr, dl)
    d = ImageDraw.Draw(L)
    d.rectangle([22, 32, 25, 34], fill=Y)
    d.point((22, 32), fill=YL)
    sparkle(d, 31, 8, WHITE)
    return finish(pal, L, ['kkkkk', 'k...k', 'k.y.k', 'k...k', 'kkkkk'], {'k': GRD, 'y': Y})


def insurance(pal):
    """Assurance : parapluie rouge au-dessus d'un écu vert coché, gouttes de pluie."""
    def umbrella(d):
        d.line([(19, 22), (19, 31)], fill=WDD)
        d.chord([7, 11, 31, 33], 180, 360, fill=R)
        for x in range(7, 31, 6):
            d.ellipse([x, 20, x + 6, 24], fill=(0, 0, 0, 0))
        poly(d, [(19, 11), (15, 13), (12, 21), (15, 22)], RD)
        poly(d, [(19, 11), (23, 13), (26, 21), (23, 22)], RD)
        d.line([(11, 16), (14, 13)], fill=RL)
        d.rectangle([18, 9, 20, 11], fill=WDD)
    um = ob(umbrella)

    def shield(d):
        poly(d, [(12, 28), (26, 28), (26, 37), (19, 43), (12, 37)], Y)
        poly(d, [(14, 30), (24, 30), (24, 36), (19, 40), (14, 36)], G)
        poly(d, [(19, 30), (24, 30), (24, 36), (19, 40)], GD)
        d.line([(12, 28), (26, 28)], fill=YL)
    sh = ob(shield)
    L = stack(um, sh)
    d = ImageDraw.Draw(L)
    d.line([(15, 34), (17, 36), (22, 31)], fill=WHITE)
    d.line([(15, 35), (17, 37), (22, 32)], fill=WHITE)
    for (x, y) in [(8, 27), (30, 28), (9, 36), (29, 38), (6, 32), (32, 33)]:
        d.line([(x, y), (x, y + 1)], fill=CY)
        d.point((x, y - 1), fill=BL)
    return finish(pal, L, ['yyyyy', 'ygggy', 'ygggy', '.ygy.', '..y..'], {'y': Y, 'g': G})


DESIGNS = {
    'pierre_a_aiguiser': pierre_a_aiguiser, 'dague_de_l_ombre': dague_de_l_ombre, 'guillotine': guillotine,
    'trefle_a_quatre_feuilles': trefle_a_quatre_feuilles, 'bourse_garnie': bourse_garnie,
    'fortune_du_roi': fortune_du_roi, 'transfusion': transfusion, 'baiser_vampire': baiser_vampire,
    'pacte_de_sang': pacte_de_sang, 'rempart': rempart, 'miroir_taille': miroir_taille,
    'diamant_brut': diamant_brut, 'gain_500': gain_500, 'gain_2000': gain_2000, 'lucky_charm': lucky_charm,
    'pot_de_lutin': pot_de_lutin, 'bet': bet, 'safe': safe, 'insurance': insurance,
}
