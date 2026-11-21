"""Cartes des rois (2026-11-14, proposition C3) : la carte signature de chacun des 24 rois de l'Exploration,
en version joueur, vendue à la Boutique après 10 victoires sur ce roi. Style des cartes classiques
(voir base.py et README.md) : pixel art, contour encre, aucun texte ni chiffre. L'index d'angle est une
petite couronne, la même pour les 24 cartes (ce sont les cartes des rois). En couleurs, chaque carte prend
la couleur du donjon de son roi (palettes.py)."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

# Couleurs communes
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
ST, STD, STL = hexc('#9aa6b4'), hexc('#5e6878'), hexc('#d4dce4')
R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ab0f0')
WD, WDD, WDL = hexc('#a0643a'), hexc('#6a3e22'), hexc('#d09060')
P, PD, PL = hexc('#8a4fc8'), hexc('#552a88'), hexc('#c8a0f0')
O, OD, OL = hexc('#f08a24'), hexc('#a8501a'), hexc('#ffc870')
DK, DK2 = hexc('#2c2a3c'), hexc('#4a4860')
SK, SKD = hexc('#f0c8a0'), hexc('#c09070')
BEIGE, BEIGED = hexc('#e8dcb8'), hexc('#b0a07c')
CY, CYD = hexc('#58d0e0'), hexc('#2a8a9c')

CROWN = ['y.y.y', 'yyyyy', 'yyyyy']


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def part(L, shape, detail=None):
    """Dessine `shape(d)` sur un calque, le cerne d'encre, puis ajoute `detail(d)` par-dessus."""
    S = layer()
    shape(ImageDraw.Draw(S))
    S = outline(S)
    if detail:
        detail(ImageDraw.Draw(S))
    L.alpha_composite(S)


def sparkle(d, x, y, col=YL):
    d.point([(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)
    d.point((x, y), fill=WHITE)


def finish(im, L):
    clip(im, L)
    corners(im, CROWN, {'y': YD})
    return im


def coin(L, x, y, r=3):
    part(L, lambda d: d.ellipse([x - r, y - r, x + r, y + r], fill=Y),
         lambda d: (d.point((x - 1, y - 1), fill=YL), d.line([(x - 1, y + r - 1), (x + 1, y + r - 1)], fill=YD)))


# --- Prairie -----------------------------------------------------------------

def roi_provocation(pal):
    """Une épée dressée, pommeau en pique, et les marques de colère de la provocation."""
    im = card(*pal)
    L = layer()
    def sword(d):
        d.rectangle([18, 12, 20, 34], fill=STL)
        poly(d, [(18, 12), (19, 8), (20, 12)], STL)
        d.rectangle([12, 34, 26, 36], fill=Y)
        d.rectangle([18, 37, 20, 42], fill=WD)
        poly(d, [(19, 43), (16, 46), (19, 45), (22, 46)], DK)
    part(L, sword, lambda d: d.line([(20, 13), (20, 33)], fill=ST))
    for cx, cy, r in ((10, 17, 4), (28, 23, 3)):  # éclats de colère : de petites explosions rouges
        def burst(d, cx=cx, cy=cy, r=r):
            pts = []
            for k in range(12):
                rr = r if k % 2 == 0 else r / 2
                a = math.radians(k * 30)
                pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
            poly(d, pts, R)
        part(L, burst, lambda d, cx=cx, cy=cy: d.point((cx, cy), fill=Y))
    return finish(im, L)


def roi_taux_usuraire(pal):
    """Trois piles de pièces qui montent, et une flèche qui grimpe au-dessus."""
    im = card(*pal)
    L = layer()
    for x0, h in ((7, 2), (15, 4), (23, 6)):
        for k in range(h):
            coin(L, x0 + 3, 43 - k * 3, 3)
    def arrow(d):
        d.line([(8, 26), (16, 20), (21, 23), (28, 13)], fill=G, width=2)
        poly(d, [(25, 11), (30, 10), (29, 15)], G)
    part(L, arrow)
    return finish(im, L)


def roi_saignee(pal):
    """Une grosse goutte de sang, et un croc qui la perce."""
    im = card(*pal)
    L = layer()
    def drop(d):
        poly(d, [(19, 9), (27, 24), (11, 24)], R)
        d.ellipse([10, 18, 28, 38], fill=R)
    part(L, drop, lambda d: (d.ellipse([13, 22, 17, 28], fill=RL), d.arc([12, 20, 26, 36], 20, 120, fill=RD)))
    def fang(d):
        poly(d, [(20, 34), (30, 34), (25, 46)], WHITE)
    part(L, fang, lambda d: d.line([(28, 35), (25, 44)], fill=STL))
    return finish(im, L)


def roi_ronces(pal):
    """Un bouclier en losange pris dans une ronce pleine d'épines."""
    im = card(*pal)
    L = layer()
    part(L, lambda d: poly(d, [(19, 12), (29, 27), (19, 42), (9, 27)], O),
         lambda d: (poly(d, [(19, 15), (19, 39), (12, 27)], OL), d.line([(19, 16), (19, 38)], fill=OD)))
    d = ImageDraw.Draw(L)
    pts = [(6 + k, 27 + round(7 * math.sin(k / 3.2))) for k in range(27)]
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        d.line([(x0, y0), (x1, y1)], fill=GD)
    for k in range(2, 26, 4):
        x, y = pts[k]
        d.point([(x, y - 1), (x, y + 1)], fill=GD)
        d.point([(x - 1, y - 2), (x + 1, y + 2)], fill=INK)
    return finish(im, L)


# --- Fête foraine ------------------------------------------------------------

def roi_roulement_de_tambour(pal):
    """Un tambour de cirque rouge et or, deux baguettes croisées au-dessus."""
    im = card(*pal)
    L = layer()
    def drum(d):
        d.rectangle([8, 26, 30, 42], fill=R)
        d.ellipse([8, 21, 30, 31], fill=BEIGE)
        d.ellipse([8, 37, 30, 46], fill=R)
    def drum_d(d):
        d.ellipse([10, 23, 28, 29], fill=WHITE)
        for x in range(10, 30, 5):
            d.line([(x, 31), (x + 4, 41)], fill=Y)
        d.line([(9, 30), (29, 30)], fill=YD)
        d.line([(9, 41), (29, 41)], fill=YD)
    part(L, drum, drum_d)
    def sticks(d):
        d.line([(9, 9), (21, 23)], fill=WDL, width=2)
        d.line([(29, 9), (17, 23)], fill=WDL, width=2)
        d.ellipse([7, 7, 10, 10], fill=WHITE)
        d.ellipse([28, 7, 31, 10], fill=WHITE)
    part(L, sticks)
    return finish(im, L)


def roi_tunnel_sans_fin(pal):
    """L'entrée d'un tunnel de train fantôme, des rails qui plongent dans le noir."""
    im = card(*pal)
    L = layer()
    def arch(d):
        d.rectangle([6, 22, 32, 46], fill=WD)
        d.ellipse([6, 10, 32, 34], fill=WD)
    def arch_d(d):
        d.rectangle([11, 24, 27, 46], fill=DK)
        d.ellipse([11, 15, 27, 31], fill=DK)
        d.ellipse([14, 19, 24, 29], fill=BLACK)
        d.rectangle([15, 24, 23, 46], fill=BLACK)
        for y in (20, 28, 36):
            d.line([(7, y), (10, y)], fill=WDD)
            d.line([(28, y + 4), (31, y + 4)], fill=WDD)
    part(L, arch, arch_d)
    d = ImageDraw.Draw(L)
    d.line([(12, 46), (17, 30)], fill=STL)
    d.line([(26, 46), (21, 30)], fill=STL)
    for k, y in enumerate((44, 40, 36, 33)):
        w = 7 - k
        d.line([(19 - w, y), (19 + w, y)], fill=WDL)
    d.point([(17, 24), (21, 24)], fill=YL)  # deux yeux dans le noir
    return finish(im, L)


def roi_roue_lestee(pal):
    """Une grande roue de foire à cases, un gros poids de fonte accroché en bas."""
    im = card(*pal)
    L = layer()
    def wheel(d):
        d.ellipse([7, 7, 31, 31], fill=Y)
    def wheel_d(d):
        cols = [R, B, G, P, O, CY]
        for k in range(6):
            d.pieslice([9, 9, 29, 29], k * 60, k * 60 + 60, fill=cols[k])
        d.ellipse([16, 16, 22, 22], fill=Y)
        d.point((19, 19), fill=YD)
        poly(d, [(17, 5), (21, 5), (19, 9)], RD)
    part(L, wheel, wheel_d)
    d = ImageDraw.Draw(L)
    d.line([(19, 31), (19, 36)], fill=STD)
    part(L, lambda d: (d.rectangle([12, 37, 26, 46], fill=DK2), d.ellipse([15, 34, 23, 40], fill=DK2)),
         lambda d: (d.ellipse([17, 36, 21, 38], fill=pal[0]), d.line([(13, 38), (13, 45)], fill=STD)))
    return finish(im, L)


def roi_carte_biseautee(pal):
    """Une carte à jouer au bord limé de biais, la lime posée à côté."""
    im = card(*pal)
    L = layer()
    def c(d):
        poly(d, [(9, 10), (25, 10), (29, 14), (27, 42), (9, 42)], WHITE)
    def c_d(d):
        poly(d, [(14, 20), (19, 15), (24, 20), (19, 25)], R)
        poly(d, [(14, 32), (19, 27), (24, 32), (19, 37)], R)
        d.line([(25, 11), (28, 15)], fill=STD)
        d.line([(28, 16), (26, 41)], fill=SHADOW)
    part(L, c, c_d)
    def file_(d):
        d.line([(22, 46), (32, 30)], fill=ST, width=3)
        d.line([(31, 31), (33, 27)], fill=WD, width=3)
    part(L, file_, lambda d: [d.point((24 + k, 42 - k * 2 + 1), fill=STD) for k in range(5)])
    return finish(im, L)


# --- Port des Contrebandiers -------------------------------------------------

def roi_dents_de_rat(pal):
    """Une tête de rat de cale, deux grandes dents qui rongent le coin d'une carte."""
    im = card(*pal)
    L = layer()
    part(L, lambda d: poly(d, [(8, 40), (28, 40), (30, 46), (8, 46)], WHITE),
         lambda d: [d.point((28 - k, 40 + k), fill=pal[0]) for k in range(3)])
    def head(d):
        d.ellipse([6, 9, 14, 17], fill=ST)
        d.ellipse([24, 9, 32, 17], fill=ST)
        poly(d, [(8, 16), (30, 16), (22, 34), (16, 34)], ST)
    def head_d(d):
        d.ellipse([8, 11, 12, 15], fill=RL)
        d.ellipse([26, 11, 30, 15], fill=RL)
        d.point([(14, 21), (24, 21)], fill=INK)
        d.point([(13, 20), (23, 20)], fill=WHITE)
        d.ellipse([17, 31, 21, 34], fill=RL)
        d.rectangle([17, 35, 21, 39], fill=WHITE)
        d.line([(19, 35), (19, 39)], fill=STD)
        for k in (-1, 1):
            d.line([(19 + 3 * k, 30), (19 + 10 * k, 28)], fill=STD)
    part(L, head, head_d)
    return finish(im, L)


def roi_tournee_du_patron(pal):
    """Deux chopes de bière mousseuse qui trinquent."""
    im = card(*pal)
    L = layer()
    for x0, tilt in ((6, -1), (20, 1)):
        def mug(d, x0=x0, tilt=tilt):
            d.rectangle([x0, 20 + tilt, x0 + 11, 42 + tilt], fill=Y)
            d.ellipse([x0 - 1, 14 + tilt, x0 + 12, 22 + tilt], fill=WHITE)
            hx = x0 - 4 if tilt < 0 else x0 + 12
            d.rectangle([hx, 24 + tilt, hx + 3, 36 + tilt], fill=Y)
        def mug_d(d, x0=x0, tilt=tilt):
            hx = x0 - 3 if tilt < 0 else x0 + 13
            d.rectangle([hx, 26 + tilt, hx + 1, 34 + tilt], fill=pal[0])
            d.line([(x0 + 2, 23 + tilt), (x0 + 2, 40 + tilt)], fill=YL)
            d.line([(x0 + 9, 23 + tilt), (x0 + 9, 40 + tilt)], fill=YD)
            d.point([(x0 + 5, 30 + tilt), (x0 + 7, 34 + tilt)], fill=YL)
        part(L, mug, mug_d)
    d = ImageDraw.Draw(L)
    sparkle(d, 19, 9)
    d.point([(15, 11), (23, 11)], fill=WHITE)
    return finish(im, L)


def roi_feu_du_phare(pal):
    """Un phare rayé rouge et blanc, son faisceau qui balaie le ciel."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    poly(d, [(19, 15), (4, 7), (4, 21)], YL)
    poly(d, [(19, 15), (34, 9), (34, 19)], YL)
    def tower(d):
        poly(d, [(15, 20), (23, 20), (26, 46), (12, 46)], WHITE)
        d.rectangle([14, 12, 24, 19], fill=DK2)
        poly(d, [(13, 12), (19, 6), (25, 12)], R)
    def tower_d(d):
        for y in (24, 34):
            poly(d, [(15, y), (24, y), (25, y + 4), (14, y + 4)], R)
        d.rectangle([16, 13, 22, 18], fill=Y)
        d.point((19, 15), fill=WHITE)
        d.rectangle([17, 40, 21, 46], fill=WD)
    part(L, tower, tower_d)
    return finish(im, L)


def roi_grappin(pal):
    """Un grappin d'abordage à trois crocs, sa corde qui serpente."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    pts = [(19 + round(4 * math.sin(k / 2.5)), 6 + k) for k in range(20)]
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        d.line([(x0, y0), (x1, y1)], fill=WDL, width=2)
    def hook(d):
        d.rectangle([18, 24, 20, 40], fill=ST)
        d.ellipse([17, 21, 21, 25], fill=ST)
        d.arc([7, 30, 19, 44], 90, 200, fill=ST, width=3)
        d.arc([19, 30, 31, 44], -20, 90, fill=ST, width=3)
        poly(d, [(7, 34), (9, 30), (10, 36)], ST)
        poly(d, [(31, 34), (29, 30), (28, 36)], ST)
        poly(d, [(17, 40), (21, 40), (19, 45)], ST)
    part(L, hook, lambda d: d.line([(19, 25), (19, 39)], fill=STL))
    return finish(im, L)


# --- Mines d'Or ---------------------------------------------------------------

def roi_tamis_noir(pal):
    """Un tamis sombre plein de gravier, d'où brillent des pépites d'or."""
    im = card(*pal)
    L = layer()
    def sieve(d):
        d.ellipse([5, 20, 33, 34], fill=WD)
        d.ellipse([6, 28, 32, 40], fill=WDD)
    def sieve_d(d):
        d.ellipse([7, 21, 31, 31], fill=DK)
        for x in range(9, 30, 3):
            d.line([(x, 33), (x, 37)], fill=DK2)
        for x, y in ((12, 25), (17, 23), (23, 27), (27, 24), (15, 28), (21, 25)):
            d.point((x, y), fill=STD)
    part(L, sieve, sieve_d)
    for x, y in ((14, 25), (22, 24), (19, 28)):
        part(L, lambda d, x=x, y=y: poly(d, [(x - 2, y), (x, y - 2), (x + 3, y - 1), (x + 2, y + 2), (x - 1, y + 2)], Y),
             lambda d, x=x, y=y: d.point((x, y - 1), fill=YL))
    d = ImageDraw.Draw(L)
    for x in (11, 19, 27):
        d.point([(x, 42), (x + 1, 45)], fill=STD)
    sparkle(d, 9, 14)
    sparkle(d, 28, 13)
    return finish(im, L)


def roi_meche_de_diamant(pal):
    """Une mèche de foreuse en spirale, terminée par un diamant qui perce la roche."""
    im = card(*pal)
    L = layer()
    part(L, lambda d: poly(d, [(5, 40), (14, 37), (24, 39), (33, 36), (33, 47), (5, 47)], STD),
         lambda d: [d.point(p, fill=DK2) for p in ((9, 42), (20, 44), (28, 41))])
    def bit(d):
        d.rectangle([16, 6, 22, 12], fill=ST)
        poly(d, [(14, 13), (24, 13), (21, 31), (17, 31)], ST)
    def bit_d(d):
        for y in range(14, 31, 4):
            d.line([(15 + (y - 13) // 6, y), (23 - (y - 13) // 6, y + 2)], fill=STD)
        d.line([(17, 7), (17, 11)], fill=STL)
    part(L, bit, bit_d)
    part(L, lambda d: poly(d, [(15, 31), (23, 31), (19, 40)], CY),
         lambda d: (poly(d, [(16, 32), (19, 32), (19, 38)], WHITE), d.line([(15, 32), (23, 32)], fill=CYD)))
    d = ImageDraw.Draw(L)
    sparkle(d, 27, 33)
    sparkle(d, 11, 34)
    return finish(im, L)


def roi_coup_d_enclume(pal):
    """Une enclume de forge, un marteau qui s'abat et deux gerbes d'étincelles."""
    im = card(*pal)
    L = layer()
    def anvil(d):
        poly(d, [(5, 30), (31, 30), (31, 34), (24, 36), (24, 40), (28, 45), (10, 45), (14, 40), (14, 36), (8, 34)], DK2)
    part(L, anvil, lambda d: d.line([(7, 31), (30, 31)], fill=STD))
    def hammer(d):
        d.rectangle([18, 9, 30, 15], fill=ST)
        poly(d, [(22, 15), (25, 15), (14, 27), (11, 25)], WD)
    part(L, hammer, lambda d: d.line([(19, 10), (29, 10)], fill=STL))
    d = ImageDraw.Draw(L)
    for x, y in ((8, 24), (6, 20), (11, 21), (28, 24), (31, 21), (26, 20)):
        d.point((x, y), fill=O)
    sparkle(d, 9, 26, OL)
    sparkle(d, 30, 26, OL)
    return finish(im, L)


def roi_filon(pal):
    """Une roche fendue où court un filon d'or, un coeur doré qui bat au milieu."""
    im = card(*pal)
    L = layer()
    def rock(d):
        poly(d, [(6, 16), (16, 9), (29, 12), (33, 28), (27, 44), (11, 45), (5, 32)], STD)
    def rock_d(d):
        d.line([(8, 18), (16, 24), (14, 32), (22, 36), (28, 42)], fill=Y, width=2)
        d.line([(30, 14), (24, 22)], fill=Y)
        for p in ((10, 36), (26, 18), (20, 13)):
            d.point(p, fill=ST)
    part(L, rock, rock_d)
    def heart(d):
        d.ellipse([12, 20, 19, 27], fill=Y)
        d.ellipse([18, 20, 25, 27], fill=Y)
        poly(d, [(12, 24), (25, 24), (18, 33)], Y)
    part(L, heart, lambda d: (d.point([(14, 22), (15, 22), (14, 23)], fill=YL), d.line([(19, 31), (23, 26)], fill=YD)))
    d = ImageDraw.Draw(L)
    sparkle(d, 29, 34)
    return finish(im, L)


# --- Casino Englouti -----------------------------------------------------------

def roi_refrain(pal):
    """Deux notes de musique liées qui se répètent, sur une vague."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    for k in range(29):
        d.point((5 + k, 42 + round(2 * math.sin(k / 2))), fill=CYD)
    for x0, y0 in ((8, 12), (20, 24)):
        def note(d, x0=x0, y0=y0):
            d.ellipse([x0, y0 + 10, x0 + 5, y0 + 14], fill=DK)
            d.ellipse([x0 + 7, y0 + 8, x0 + 12, y0 + 12], fill=DK)
            d.rectangle([x0 + 4, y0, x0 + 5, y0 + 12], fill=DK)
            d.rectangle([x0 + 11, y0 - 2, x0 + 12, y0 + 10], fill=DK)
            poly(d, [(x0 + 4, y0), (x0 + 12, y0 - 2), (x0 + 12, y0 + 1), (x0 + 4, y0 + 3)], DK)
        part(L, note, lambda d, x0=x0, y0=y0: d.point([(x0 + 1, y0 + 11), (x0 + 8, y0 + 9)], fill=DK2))
    d = ImageDraw.Draw(L)
    sparkle(d, 29, 12, CY)
    sparkle(d, 9, 34, CY)
    return finish(im, L)


def roi_rage_du_jackpot(pal):
    """Deux cerises jumelles (une paire) dans des flammes de rage."""
    im = card(*pal)
    L = layer()
    def fire(d):
        poly(d, [(6, 44), (8, 28), (12, 34), (14, 18), (19, 30), (24, 14), (27, 32), (31, 24), (32, 44)], O)
    part(L, fire, lambda d: poly(d, [(11, 44), (13, 34), (17, 38), (19, 28), (22, 38), (26, 32), (28, 44)], Y))
    def cherries(d):
        d.line([(14, 32), (20, 20)], fill=GD, width=1)
        d.line([(25, 33), (20, 20)], fill=GD, width=1)
        d.ellipse([9, 31, 18, 40], fill=R)
        d.ellipse([21, 32, 30, 41], fill=R)
        poly(d, [(20, 20), (26, 18), (23, 22)], G)
    part(L, cherries, lambda d: (d.point([(12, 33), (13, 33), (24, 34), (25, 34)], fill=RL),
                                 d.point([(16, 38), (28, 39)], fill=RD)))
    return finish(im, L)


def roi_frenesie(pal):
    """Un aileron de requin qui fend une eau rougie, les crocs d'une mâchoire en dessous."""
    im = card(*pal)
    L = layer()
    def water(d):
        d.rectangle([4, 26, 33, 46], fill=B)
    def water_d(d):
        for k in range(30):
            d.point((4 + k, 26 + round(math.sin(k / 1.5))), fill=BL)
        d.ellipse([10, 28, 22, 33], fill=R)
        d.ellipse([20, 30, 30, 34], fill=RD)
        for x in range(8, 31, 4):
            poly(d, [(x, 38), (x + 3, 38), (x + 1, 43)], WHITE)
    part(L, water, water_d)
    part(L, lambda d: poly(d, [(14, 27), (20, 8), (26, 27)], DK2),
         lambda d: d.line([(20, 10), (24, 26)], fill=STD))
    return finish(im, L)


def roi_encre(pal):
    """Une grosse tache d'encre noire, un tentacule de kraken qui s'en échappe."""
    im = card(*pal)
    L = layer()
    def blot(d):
        d.ellipse([8, 18, 30, 40], fill=DK)
        for x, y, r in ((7, 22, 3), (31, 26, 2), (27, 41, 3), (10, 41, 2), (20, 16, 3)):
            d.ellipse([x - r, y - r, x + r, y + r], fill=DK)
    part(L, blot, lambda d: (d.ellipse([13, 22, 18, 26], fill=DK2), d.point([(6, 46), (32, 14), (33, 44)], fill=DK)))
    d = ImageDraw.Draw(L)
    pts = [(14 + k, 30 - round(13 * math.sin(k / 9)) - k // 2) for k in range(18)]
    for i, ((x0, y0), (x1, y1)) in enumerate(zip(pts, pts[1:])):
        d.line([(x0, y0), (x1, y1)], fill=P, width=3 if i < 10 else 2)
    for x, y in pts[2:14:3]:
        d.point((x, y + 2), fill=PL)
    return finish(im, L)


# --- Désert de Braise -----------------------------------------------------------

def roi_usure(pal):
    """Une lettre de change roulée, scellée de cire, sur une pile de pièces."""
    im = card(*pal)
    L = layer()
    for k in range(3):
        coin(L, 11, 42 - k * 3, 4)
        coin(L, 24, 42 - k * 3 + (2 if k == 2 else 0), 4)
    def scroll(d):
        d.rectangle([9, 12, 29, 32], fill=BEIGE)
        d.ellipse([6, 10, 11, 16], fill=BEIGED)
        d.ellipse([27, 28, 32, 34], fill=BEIGED)
    def scroll_d(d):
        for y in (16, 20, 24):
            d.line([(12, y), (26, y)], fill=BEIGED)
        d.ellipse([20, 25, 26, 31], fill=R)
        d.point((22, 27), fill=RL)
    part(L, scroll, scroll_d)
    return finish(im, L)


def roi_momification(pal):
    """Une main de momie enroulée de bandelettes, qui serre une carte ligotée."""
    im = card(*pal)
    L = layer()
    part(L, lambda d: d.rectangle([11, 9, 27, 31], fill=WHITE),
         lambda d: poly(d, [(16, 16), (19, 12), (22, 16), (19, 20)], R))
    def hand(d):
        d.rectangle([12, 26, 26, 40], fill=BEIGE)
        for x in (12, 16, 20, 24):
            d.rectangle([x, 22, x + 2, 28], fill=BEIGE)
        d.rectangle([15, 40, 23, 47], fill=BEIGE)
    def hand_d(d):
        for y in range(28, 47, 3):
            d.line([(12, y), (26, y - 2)], fill=BEIGED)
        d.line([(9, 18), (29, 24)], fill=BEIGED, width=2)
        d.line([(9, 24), (29, 18)], fill=BEIGED, width=2)
    part(L, hand, hand_d)
    return finish(im, L)


def roi_fata_morgana(pal):
    """Un palmier sur une dune, et son reflet de mirage qui tremble à l'envers."""
    im = card(*pal)
    L = layer()
    def palm(d, flip=False):
        f = (lambda y: 54 - y) if flip else (lambda y: y)
        col_t, col_l = (WDL, GL) if flip else (WD, G)
        d.line([(19, f(26)), (20, f(14))], fill=col_t, width=2)
        for dx in (-8, -4, 4, 8):
            d.line([(20, f(13)), (20 + dx, f(13 + abs(dx) // 2 + 2))], fill=col_l, width=2)
    part(L, lambda d: d.ellipse([3, 24, 35, 32], fill=Y), lambda d: d.line([(6, 27), (32, 27)], fill=YD))
    part(L, palm)
    M = layer()
    palm(ImageDraw.Draw(M), flip=True)
    import numpy as np
    a = np.array(M)
    for y in range(a.shape[0]):  # le mirage tremble : une ligne sur deux décalée
        if y % 2:
            a[y] = np.roll(a[y], 1, axis=0)
    a[:, :, 3] = (a[:, :, 3] * 0.6).astype(a.dtype)
    L.alpha_composite(Image.fromarray(a))
    d = ImageDraw.Draw(L)
    d.ellipse([26, 7, 31, 12], fill=YL)
    return finish(im, L)


def roi_braise(pal):
    """Un tas de braises rougeoyantes, une flamme de dragon qui s'en élève."""
    im = card(*pal)
    L = layer()
    def flame(d):
        poly(d, [(10, 38), (12, 24), (16, 30), (19, 10), (23, 28), (27, 22), (28, 38)], R)
    part(L, flame, lambda d: poly(d, [(14, 38), (16, 30), (19, 34), (20, 20), (23, 34), (25, 30), (25, 38)], O))
    def coals(d):
        for x, y in ((9, 41), (16, 43), (23, 41), (29, 43), (13, 46), (25, 46)):
            d.ellipse([x - 4, y - 3, x + 4, y + 3], fill=DK2)
    def coals_d(d):
        for x, y in ((9, 41), (16, 43), (23, 41), (29, 43)):
            d.line([(x - 2, y), (x + 1, y - 1)], fill=O)
            d.point((x, y + 1), fill=Y)
    part(L, coals, coals_d)
    d = ImageDraw.Draw(L)
    for x, y in ((9, 18), (29, 14), (6, 28), (32, 30)):
        d.point((x, y), fill=OL)
    return finish(im, L)


# --- Rapide de Minuit (10 lieux, 2026-11-20) ------------------------------------

def roi_plein_regime(pal):
    """La Locomotive à plein régime : l'avant d'une machine noire, sa lanterne, un panache de vapeur qui monte."""
    im = card(*pal)
    L = layer()
    def engine(d):
        d.ellipse([7, 22, 31, 46], fill=DK)
        d.rectangle([14, 14, 22, 24], fill=DK)
        d.rectangle([12, 12, 24, 14], fill=DK2)
    def engine_d(d):
        d.ellipse([10, 25, 28, 43], fill=DK2)
        d.ellipse([16, 27, 22, 32], fill=YL)
        d.rectangle([12, 37, 26, 41], fill=DK)
        for x in range(13, 26, 2):
            d.line([(x, 37), (x, 41)], fill=ST)
        d.ellipse([9, 30, 13, 34], fill=R)
        d.ellipse([25, 30, 29, 34], fill=R)
    part(L, engine, engine_d)
    def steam(d):
        for x, y, r in [(18, 9, 3), (13, 7, 3), (24, 6, 3), (9, 10, 2), (29, 9, 2)]:
            d.ellipse([x - r, y - r, x + r, y + r], fill=WHITE)
    part(L, steam)
    return finish(im, L)


def roi_banquet(pal):
    """Le Banquet du chef : une toque blanche au-dessus d'une soupière fumante, un coeur et des pièces."""
    im = card(*pal)
    L = layer()
    def tureen(d):
        d.ellipse([7, 28, 31, 46], fill=WHITE)
        d.rectangle([5, 31, 8, 34], fill=WHITE)
        d.rectangle([30, 31, 33, 34], fill=WHITE)
    def tureen_d(d):
        d.ellipse([9, 28, 29, 34], fill=O)
        d.line([(9, 40), (29, 40)], fill=Y)
    part(L, tureen, tureen_d)
    def toque(d):
        d.rectangle([12, 14, 26, 20], fill=WHITE)
        d.ellipse([10, 6, 18, 15], fill=WHITE)
        d.ellipse([16, 4, 24, 13], fill=WHITE)
        d.ellipse([21, 6, 29, 15], fill=WHITE)
    part(L, toque, lambda d: d.line([(12, 18), (26, 18)], fill=BEIGED))
    def heart(d):
        d.polygon([(19, 27), (15, 23), (15, 21), (17, 20), (19, 22), (21, 20), (23, 21), (23, 23)], fill=R)
    part(L, heart)
    coin(L, 7, 22)
    coin(L, 31, 24)
    return finish(im, L)


def roi_marchand_de_sable(pal):
    """Le Marchand de sable : un sablier de nuit violet, une lune et des étoiles, un bouclier qui veille."""
    im = card(*pal)
    L = layer()
    def glass(d):
        d.rectangle([10, 10, 28, 12], fill=WD)
        d.rectangle([10, 42, 28, 44], fill=WD)
        d.polygon([(12, 13), (26, 13), (20, 27), (26, 41), (12, 41), (18, 27)], fill=PL)
    def glass_d(d):
        d.polygon([(14, 14), (24, 14), (19, 22)], fill=Y)
        d.polygon([(14, 41), (24, 41), (19, 35)], fill=Y)
        d.line([(19, 23), (19, 35)], fill=YL)
    part(L, glass, glass_d)
    def moon(d):
        d.ellipse([27, 15, 35, 23], fill=YL)
    part(L, moon, lambda d: d.ellipse([30, 14, 37, 21], fill=pal[0]))
    sparkle(ImageDraw.Draw(L), 6, 18)
    sparkle(ImageDraw.Draw(L), 31, 36)
    return finish(im, L)


def roi_haut_les_mains(pal):
    """Haut les mains : un revolver et un sac d'or éventré, les pièces qui roulent."""
    im = card(*pal)
    L = layer()
    def bag(d):
        d.ellipse([16, 26, 34, 46], fill=BEIGE)
        d.polygon([(21, 27), (29, 27), (27, 22), (23, 22)], fill=BEIGE)
    def bag_d(d):
        d.line([(21, 27), (29, 27)], fill=WDD)
        d.ellipse([22, 34, 28, 40], fill=Y)
        d.point((24, 36), fill=YL)
    part(L, bag, bag_d)
    def gun(d):
        d.rectangle([5, 14, 24, 18], fill=ST)
        d.rectangle([10, 13, 16, 21], fill=STD)
        d.polygon([(9, 19), (15, 19), (12, 30), (6, 30)], fill=WD)
    part(L, gun, lambda d: (d.line([(6, 15), (23, 15)], fill=STL), d.point((13, 17), fill=DK)))
    coin(L, 10, 41)
    coin(L, 15, 45)
    return finish(im, L)


DESIGNS = {name: fn for name, fn in list(globals().items()) if name.startswith('roi_') and callable(fn)}
