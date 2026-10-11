"""Cartes spéciales (lot 4) : Casino Englouti (bar, machines, coffres, VIP) et cartes de l'échoppe du combat,
au style des cartes classiques (voir base.py et CONSIGNES.md). Aucun texte, aucun chiffre."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD = hexc('#3fae4a'), hexc('#23793a')
P, PD, PL = hexc('#9a4ad8'), hexc('#5e2c8f'), hexc('#c890f0')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
S, SD = hexc('#f6dcbc'), hexc('#d8aa84')
B, BD = hexc('#3a6fd8'), hexc('#24459a')
C, CD, CL = hexc('#5cc8d8'), hexc('#2e8aa0'), hexc('#c8f4f6')
O, OD = hexc('#f08a2c'), hexc('#b0521a')
WD, WDD, WDL = hexc('#a0643a'), hexc('#6a3e22'), hexc('#c88a58')
ST, STD, STL = hexc('#9aa6b4'), hexc('#5e6878'), hexc('#d4dce4')
LIME, LIMED = hexc('#8ef05a'), hexc('#3fae4a')
BONE, BONED = hexc('#f0e8d4'), hexc('#c4b496')
DARK = hexc('#1a1028')


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def shift(L, dx, dy):
    N = layer()
    N.paste(L, (dx, dy))
    return N


def finish(im, L, corner, cpal):
    clip(im, L)
    corners(im, corner, cpal)
    return im


def small_bubble(d, x, y, r):
    d.ellipse([x - r, y - r, x + r, y + r], fill=CL)


def bubble_hl(d, x, y, r):
    d.point((x - r + 1, y - r + 1) if r > 1 else (x, y), fill=WHITE)


def coin(d, x, y):
    """Petite pièce 3x3 (à contourner)."""
    d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=Y)
    d.point((x - 1, y - 1), fill=YL)
    d.point((x + 1, y + 1), fill=YD)


BUBBLE_IDX = ['.ccc.', 'cw..c', 'c...c', 'c...c', '.ccc.']
SKULL_IDX = ['.www.', 'wwwww', 'wkwkw', 'wwwww', '.w.w.']
CHERRY_IDX = ['...g.', '..gg.', '.g..g', 'rr.rr', 'rr.rr']
STAR_IDX = ['..y..', '.yyy.', 'yyyyy', '.yyy.', 'y...y']
GEM_IDX = ['.ccc.', 'ccccc', '.ccc.', '..c..']
DIAMOND_IDX = ['..k..', '.kkk.', 'kkkkk', '.kkk.', '..k..']
FLAME_IDX = ['..r..', '.rr..', '.ror.', 'royor', '.ryr.']


# ======================================================== Bar
def bulle_d_air(pal):
    """Une grosse bulle d'air qui enferme une petite bouteille de plongée, et des bulles qui montent."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.ellipse([8, 15, 30, 37], fill=C)
    d.ellipse([9, 16, 29, 35], fill=CL)
    small_bubble(d, 27, 11, 2)
    small_bubble(d, 11, 40, 2)
    small_bubble(d, 24, 42, 1)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # reflet
    d.line([(12, 22), (12, 20), (13, 19), (14, 18), (16, 18)], fill=WHITE)
    d.point([(11, 24)], fill=WHITE)
    # bouteille d'air
    d.rectangle([16, 23, 21, 32], fill=INK)
    d.rectangle([17, 24, 20, 31], fill=R)
    d.line([(17, 25), (17, 30)], fill=RL)
    d.line([(20, 25), (20, 31)], fill=RD)
    d.rectangle([17, 27, 20, 28], fill=Y)
    d.rectangle([18, 20, 19, 22], fill=INK)
    d.rectangle([16, 20, 21, 20], fill=INK)
    d.point([(18, 21), (19, 21)], fill=ST)
    for (x, y) in [(27, 11), (11, 40)]:
        d.point((x - 1, y - 1), fill=WHITE)
    return finish(im, L, BUBBLE_IDX, {'c': CD, 'w': WHITE})


def bouchons_d_oreille(pal):
    """Deux bouchons d'oreille en mousse sous une note de musique interdite (plus de bruit)."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # panneau d'interdiction
    d.ellipse([10, 6, 28, 24], fill=R)
    d.ellipse([12, 8, 26, 22], fill=pal[0])
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.ellipse([13, 9, 25, 21], fill=pal[0])
    # note de musique
    d.rectangle([20, 10, 21, 18], fill=INK)
    d.line([(22, 10), (24, 12), (24, 14)], fill=INK)
    d.line([(22, 11), (23, 12)], fill=INK)
    d.ellipse([15, 16, 21, 20], fill=INK)
    d.rectangle([16, 17, 19, 19], fill=P)
    d.point([(16, 17)], fill=PL)
    # barre
    d.line([(14, 10), (24, 20)], fill=R, width=2)
    d.point([(12, 11), (13, 10)], fill=RL)
    # bouchons
    PL_ = layer()
    pd = ImageDraw.Draw(PL_)
    for (x, y) in ((10, 27), (20, 28)):
        pd.rounded_rectangle([x, y, x + 6, y + 13], radius=3, fill=O)
        pd.rectangle([x + 5, y + 3, x + 6, y + 10], fill=OD)
        pd.rectangle([x + 1, y + 3, x + 1, y + 9], fill=Y)
    PL_ = outline(PL_)
    pd = ImageDraw.Draw(PL_)
    for (x, y) in ((10, 27), (20, 28)):
        pd.point([(x + 3, y + 4), (x + 4, y + 7), (x + 2, y + 9), (x + 3, y + 11)], fill=OD)
        pd.line([(x + 2, y), (x + 4, y)], fill=Y)
        pd.point((x + 1, y + 1), fill=Y)
    L.alpha_composite(PL_)
    return finish(im, L, ['.oo.', 'oyoo', 'oyoo', 'oooo', '.oo.'], {'o': O, 'y': Y})


def cocktail_des_abysses(pal):
    """Un verre à cocktail violet, cerise, ombrelle et un tentacule qui sort du verre."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # tentacule
    d.line([(24, 22), (26, 18), (27, 14), (26, 11), (24, 10), (22, 11)], fill=hexc('#e86aa8'), width=2)
    # ombrelle
    poly(d, [(8, 17), (13, 12), (18, 17)], Y)
    d.line([(13, 17), (16, 23)], fill=WD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # verre
    cup = [(7, 21), (31, 21), (20, 32), (18, 32)]
    poly(d, cup, CL)
    poly(d, [(9, 23), (29, 23), (20, 31), (18, 31)], P)
    d.rectangle([18, 32, 20, 40], fill=CL)
    d.ellipse([13, 39, 25, 42], fill=CL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(9, 23), (29, 23)], fill=PL)
    d.line([(19, 33), (19, 39)], fill=WHITE)
    d.line([(15, 40), (23, 40)], fill=WHITE)
    d.point([(14, 25), (22, 27), (25, 24), (17, 28)], fill=PL)
    d.point([(20, 26)], fill=WHITE)
    # cerise
    d.ellipse([20, 18, 23, 21], fill=INK)
    d.rectangle([21, 19, 22, 20], fill=R)
    d.point((21, 19), fill=RL)
    d.line([(22, 18), (24, 15)], fill=GD)
    # ombrelle : rayures
    d.point([(11, 16), (13, 14), (15, 16)], fill=O)
    # ventouses du tentacule
    d.point([(27, 15), (26, 18)], fill=hexc('#ffc4e0'))
    L = shift(L, 0, 2)
    return finish(im, L, CHERRY_IDX, {'g': GD, 'r': R})


# ======================================================== Machines
def piece_truquee(pal):
    """Une pièce d'or lestée d'un plomb, avec deux flèches qui la font tourner."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.ellipse([9, 17, 29, 37], fill=YD)
    d.ellipse([9, 17, 28, 36], fill=Y)
    d.ellipse([12, 20, 26, 34], fill=YD)
    d.ellipse([13, 21, 26, 34], fill=Y)
    # plomb
    d.ellipse([23, 30, 30, 37], fill=STD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    sprite(d, 17, 24, STAR_IDX, {'y': YD})
    d.point([(18, 25)], fill=YL)
    d.line([(11, 23), (12, 21), (14, 19)], fill=YL)
    d.point([(25, 31), (26, 31), (25, 32)], fill=STL)
    # flèches de rotation
    A = layer()
    ad = ImageDraw.Draw(A)
    ad.arc([7, 11, 31, 43], 200, 300, fill=WHITE, width=2)
    poly(ad, [(25, 10), (30, 14), (24, 16)], WHITE)
    ad.arc([7, 11, 31, 43], 20, 120, fill=WHITE, width=2)
    poly(ad, [(13, 44), (8, 40), (14, 38)], WHITE)
    A = outline(A)
    L.alpha_composite(A)
    return finish(im, L, ['.yyy.', 'yyyyy', 'yykyy', 'yyyyy', '.yyy.'], {'y': Y, 'k': STD})


def levier_rouille(pal):
    """Le levier d'une machine à sous, rouillé, avec sa boule rouge et son socle."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    RU, RUD = hexc('#b8643a'), hexc('#7a3a22')
    d.line([(16, 39), (25, 15)], fill=RU, width=3)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # boule
    d.ellipse([21, 9, 29, 17], fill=R)
    # socle
    poly(d, [(10, 44), (11, 38), (22, 38), (24, 44)], WDD)
    d.ellipse([12, 35, 20, 41], fill=ST)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(23, 11), (24, 11), (23, 12)], fill=RL)
    d.point([(27, 15), (28, 14), (26, 16)], fill=RD)
    # rouille
    for (x, y) in [(18, 33), (20, 28), (22, 24), (23, 20), (19, 31), (21, 26)]:
        d.point((x, y), fill=O if (x + y) % 2 else RUD)
    d.point([(14, 37), (13, 38)], fill=STL)
    d.point([(18, 40), (17, 40)], fill=STD)
    d.line([(12, 42), (22, 42)], fill=hexc('#4a2a16'))
    d.point([(14, 40), (19, 41)], fill=O)
    return finish(im, L, ['...r.', '..rrr', '...r.', '..k..', '.k...'], {'r': R, 'k': hexc('#b8643a')})


def slot_machine(d, x0, y0, body, bodyd, bodyl, top=True, w=20, h=24):
    """Machine à sous de base : corps x0..x0+w, y0..y0+h (à contourner ensuite)."""
    d.rectangle([x0, y0 + 4, x0 + w, y0 + h], fill=body)
    d.rectangle([x0 + w - 2, y0 + 4, x0 + w, y0 + h], fill=bodyd)
    d.rectangle([x0, y0 + 4, x0 + 1, y0 + h], fill=bodyl)
    if top:
        d.rectangle([x0 + 1, y0, x0 + w - 1, y0 + 4], fill=Y)
        d.rectangle([x0 + 1, y0 + 3, x0 + w - 1, y0 + 4], fill=YD)


def reels(d, x0, y0, syms):
    """Fenêtre à trois rouleaux en x0..x0+18, y0..y0+8, puis un symbole par rouleau."""
    d.rectangle([x0, y0, x0 + 18, y0 + 8], fill=INK)
    for k in range(3):
        rx = x0 + 1 + k * 6
        d.rectangle([rx, y0 + 1, rx + 4, y0 + 7], fill=WHITE)
        d.line([(rx, y0 + 7), (rx + 4, y0 + 7)], fill=STL)
        syms[k](d, rx, y0 + 2)


def sym_star(d, x, y):
    sprite(d, x, y, ['..o..', '.oyo.', 'ooyoo', '.ooo.', 'o...o'], {'o': O, 'y': Y})


def sym_cherry(d, x, y):
    sprite(d, x, y, ['...g.', '..g.g', '.g..g', 'rr.r.', 'rrrrr'], {'g': GD, 'r': R})


def sym_bell(d, x, y):
    sprite(d, x, y, ['..y..', '.yyy.', '.yyy.', 'yyyyy', '..d..'], {'y': Y, 'd': YD})


def sym_fish(d, x, y):
    sprite(d, x, y, ['.....', 'o.oo.', 'ooook', 'o.oo.', '.....'], {'o': O, 'k': INK})


def sym_crack(d, x, y):
    sprite(d, x, y, ['.k...', '..k..', '.k...', '..k..', '.k...'], {'k': INK})


def jackpot_englouti(pal):
    """Une machine à sous engloutie, posée dans le sable : poisson, bulles, algues et pièces."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    TL, TLD, TLL = hexc('#3a9a8a'), hexc('#226a5e'), hexc('#6ac8b4')
    slot_machine(d, 9, 13, TL, TLD, TLL, w=20, h=25)
    # algues
    d.line([(8, 40), (7, 36), (8, 32), (7, 28), (8, 25)], fill=G, width=1)
    L = outline(L)
    d = ImageDraw.Draw(L)
    reels(d, 10, 20, [sym_cherry, sym_cherry, sym_cherry])
    d.rectangle([12, 31, 26, 33], fill=TLD)
    # coquillage incrusté et rayures d'usure
    d.point([(12, 18), (25, 19), (14, 36), (24, 35)], fill=TLD)
    F = layer()
    fd = ImageDraw.Draw(F)
    # poisson qui passe
    sprite(fd, 21, 7, ['.oo..o', 'ooooo.', 'ooooo.', '.oo..o'], {'o': O})
    small_bubble(fd, 15, 9, 1)
    # monticule de sable
    poly(fd, [(7, 44), (9, 40), (14, 37), (24, 37), (28, 39), (29, 41), (25, 43)], hexc('#e8cc8a'))
    F = outline(F)
    fd = ImageDraw.Draw(F)
    fd.point((22, 8), fill=INK)
    fd.point([(14, 8)], fill=WHITE)
    for (x, y) in [(12, 41), (18, 41), (23, 40)]:
        coin(fd, x, y)
    fd.point([(10, 42), (16, 43), (22, 39)], fill=hexc('#c4a464'))
    L.alpha_composite(F)
    return finish(im, L, ['.....', 'o.oo.', 'ooook', 'o.oo.', '.....'], {'o': O, 'k': INK})


def bingo(pal):
    """La machine à sous rouge et or qui affiche trois étoiles, ampoules allumées et pièces qui tombent."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    slot_machine(d, 8, 16, R, RD, RL, w=20, h=23)
    d.rectangle([9, 13, 27, 16], fill=Y)
    # bac à pièces
    d.rectangle([11, 40, 25, 42], fill=Y)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for i, x in enumerate(range(10, 27, 2)):
        d.point((x, 14), fill=WHITE if i % 2 == 0 else R)
        d.point((x, 15), fill=YD)
    reels(d, 9, 22, [sym_star, sym_star, sym_star])
    d.rectangle([10, 33, 26, 34], fill=Y)
    d.rectangle([13, 36, 23, 37], fill=RD)
    for x in range(13, 24, 3):
        coin(d, x, 41)
    # étincelles
    sp = [(-1, 0), (1, 0), (0, -1), (0, 1), (0, 0)]
    for (cx, cy) in [(7, 24), (30, 15), (19, 9)]:
        d.point([(cx + a, cy + b) for a, b in sp], fill=Y)
        d.point((cx, cy), fill=YL)
    return finish(im, L, STAR_IDX, {'y': YD})


def overheat(pal):
    """Une machine à sous en surchauffe : flammes au sommet, rouleaux brouillés et fissurés."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # flammes
    for (x, h) in [(12, 6), (16, 9), (20, 11), (24, 8), (27, 5)]:
        poly(d, [(x - 3, 23), (x, 23 - h), (x + 3, 23)], O)
    for (x, h) in [(12, 3), (16, 6), (20, 8), (24, 5), (27, 3)]:
        poly(d, [(x - 2, 23), (x, 23 - h + 1), (x + 2, 23)], Y)
    L = outline(L)
    d = ImageDraw.Draw(L)
    DM, DMD, DML = hexc('#5a3a4a'), hexc('#3a2230'), hexc('#7a5468')
    slot_machine(d, 9, 19, DM, DMD, DML, top=False, w=20, h=22)
    d.rectangle([10, 19, 28, 22], fill=OD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(12, 20), (17, 21), (22, 20), (26, 21)], fill=O)
    reels(d, 10, 25, [sym_cherry, sym_bell, sym_star])
    # fissure qui traverse les rouleaux
    d.line([(21, 23), (20, 25), (22, 27), (20, 30), (21, 34)], fill=INK)
    d.rectangle([13, 36, 25, 38], fill=DMD)
    d.point([(15, 37), (19, 37), (23, 37)], fill=R)
    # fumée
    SM = layer()
    sd = ImageDraw.Draw(SM)
    for (x, y, r) in [(28, 11, 2), (25, 8, 1), (10, 14, 1)]:
        sd.ellipse([x - r, y - r, x + r, y + r], fill=STL)
    SM = outline(SM, STD)
    L.alpha_composite(SM)
    d = ImageDraw.Draw(L)
    # étincelles
    d.point([(6, 30), (7, 29), (32, 33), (31, 34)], fill=Y)
    return finish(im, L, FLAME_IDX, {'r': R, 'o': O, 'y': Y})


# ======================================================== Coffres
def perle_noire(pal):
    """Une huître ouverte, violette, qui montre une grosse perle noire."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    SH, SHD, SHL = hexc('#b48ad8'), hexc('#7a52a8'), hexc('#e0c8f4')
    # valve du haut (éventail)
    d.pieslice([8, 13, 30, 35], 180, 360, fill=SH)
    # valve du bas
    d.chord([8, 23, 30, 43], 0, 180, fill=SH)
    d.rectangle([8, 33, 30, 33], fill=SH)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for a in (200, 225, 250, 275, 300, 325, 350):
        t = math.radians(a)
        d.line([(19, 24), (round(19 + 11 * math.cos(t)), round(24 + 11 * math.sin(t)))], fill=SHD)
    d.rectangle([9, 23, 29, 24], fill=SHD)
    d.chord([10, 26, 28, 40], 0, 180, fill=SHL)
    d.rectangle([10, 33, 28, 33], fill=SHL)
    d.arc([8, 23, 30, 43], 20, 160, fill=SHD)
    # perle
    PR = layer()
    pd = ImageDraw.Draw(PR)
    pd.ellipse([14, 25, 24, 35], fill=hexc('#2a2440'))
    PR = outline(PR)
    pd = ImageDraw.Draw(PR)
    pd.point([(16, 27), (17, 27), (16, 28)], fill=WHITE)
    pd.point([(18, 28)], fill=hexc('#6a6290'))
    pd.arc([14, 25, 24, 35], 20, 80, fill=hexc('#4a4270'))
    L.alpha_composite(PR)
    return finish(im, L, ['.....', '.kkk.', 'kkwkk', 'kkkkk', '.kkk.'], {'k': hexc('#2a2440'), 'w': WHITE})


def cage_a_requin(pal):
    """Un requin gueule ouverte au-dessus d'une cage de plongée à barreaux."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # cage : ossature, on voit la face à travers
    d.rectangle([10, 26, 28, 43], fill=STD)
    d.rectangle([11, 28, 27, 41], fill=pal[0])
    for x in range(14, 27, 4):
        d.rectangle([x, 27, x + 1, 42], fill=STD)
    d.rectangle([10, 34, 28, 34], fill=STD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # l'intérieur n'est pas contouré : on ré-efface le contour intérieur
    for x in range(14, 27, 4):
        d.line([(x, 28), (x, 41)], fill=ST)
    d.line([(10, 26), (28, 26)], fill=ST)
    d.line([(10, 43), (28, 43)], fill=INK)
    for (x, y) in [(12, 38), (20, 30), (24, 37)]:
        d.point((x, y), fill=C)
    # requin
    SK = layer()
    sd = ImageDraw.Draw(SK)
    SG, SGD = hexc('#8a9cb0'), hexc('#5a6a80')
    poly(sd, [(7, 19), (12, 15), (21, 14), (27, 16), (31, 13), (31, 23), (27, 20), (20, 22), (10, 22)], SG)
    poly(sd, [(16, 15), (20, 9), (22, 14)], SG)
    poly(sd, [(17, 21), (21, 21), (19, 25)], SGD)
    poly(sd, [(8, 20), (10, 22), (20, 22), (24, 21), (13, 21)], WHITE)
    SK = outline(SK)
    sd = ImageDraw.Draw(SK)
    poly(sd, [(7, 19), (12, 19), (10, 22)], RD)
    sd.point([(8, 19), (10, 19), (11, 21)], fill=WHITE)
    sd.point([(12, 17)], fill=INK)
    sd.line([(14, 18), (14, 20)], fill=SGD)
    sd.line([(16, 18), (16, 20)], fill=SGD)
    sd.line([(13, 15), (21, 15)], fill=hexc('#b4c4d4'))
    L.alpha_composite(SK)
    return finish(im, L, ['..k..', '.kk..', 'kkk..', 'kkkkk', '.....'], {'k': hexc('#5a6a80')})


def coffre_de_l_epave(pal):
    """Le coffre de l'épave : bois vert-brun, ouvert, débordant d'or et de gemmes, une algue."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    WG, WGD, WGL = hexc('#7a7a3a'), hexc('#4e4e22'), hexc('#a4a45a')
    # couvercle ouvert (en arrière)
    poly(d, [(9, 22), (11, 13), (27, 13), (29, 22)], WG)
    # caisse
    d.rectangle([8, 28, 30, 42], fill=WG)
    # tas d'or
    d.chord([9, 18, 29, 34], 180, 360, fill=Y)
    d.rectangle([9, 26, 29, 28], fill=Y)
    # algue
    d.line([(31, 41), (31, 37), (30, 34), (31, 31)], fill=G)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(11, 15), (27, 15)], fill=WGL)
    d.rectangle([10, 17, 28, 18], fill=YD)
    d.rectangle([8, 28, 30, 29], fill=WGL)
    d.rectangle([8, 39, 30, 40], fill=YD)
    d.rectangle([12, 29, 13, 42], fill=YD)
    d.rectangle([25, 29, 26, 42], fill=YD)
    # serrure
    d.rectangle([17, 31, 21, 36], fill=Y)
    d.rectangle([18, 32, 20, 35], fill=INK)
    d.point((19, 33), fill=Y)
    d.line([(9, 41), (29, 41)], fill=WGD)
    # or et gemmes
    for (x, y) in [(13, 24), (16, 22), (22, 23), (25, 25), (19, 26), (11, 27)]:
        d.point((x, y), fill=YL)
        d.point((x + 1, y + 1), fill=YD)
    d.rectangle([18, 21, 20, 23], fill=R)
    d.point((18, 21), fill=RL)
    d.rectangle([23, 20, 24, 21], fill=G)
    d.rectangle([13, 25, 14, 26], fill=C)
    # pièces qui débordent
    C2 = layer()
    cd = ImageDraw.Draw(C2)
    for (x, y) in [(7, 44), (11, 45)]:
        coin(cd, x, y)
    C2 = outline(C2)
    L.alpha_composite(C2)
    return finish(im, L, GEM_IDX, {'c': hexc('#3fae4a')})


# ======================================================== VIP
def harpon(pal):
    """Un harpon en diagonale : pointe d'acier barbelée, hampe en bois et corde enroulée."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.line([(10, 44), (25, 15)], fill=WD, width=3)
    # pointe
    poly(d, [(23, 17), (29, 8), (28, 19)], ST)
    poly(d, [(23, 18), (19, 20), (25, 14)], ST)
    poly(d, [(27, 19), (30, 23), (26, 15)], ST)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(26, 16), (28, 10)], fill=STL)
    d.line([(10, 43), (24, 16)], fill=WDL)
    d.point([(14, 37), (18, 29)], fill=WDD)
    # corde
    RP = layer()
    rd = ImageDraw.Draw(RP)
    rd.rectangle([11, 38, 15, 40], fill=hexc('#e8d4a0'))
    RP = outline(RP)
    rd = ImageDraw.Draw(RP)
    rd.point([(12, 39), (14, 39)], fill=hexc('#b8a06a'))
    L.alpha_composite(RP)
    return finish(im, L, ['....k', '...kk', '..k..', '.k...', 'k....'], {'k': STD})


def ancre(pal):
    """Une ancre d'acier avec son anneau, son jas et ses deux pattes."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.ellipse([15, 8, 23, 16], outline=ST, width=2)
    d.rectangle([18, 15, 20, 39], fill=ST)
    d.rectangle([11, 18, 27, 20], fill=ST)
    d.arc([10, 24, 28, 42], 20, 160, fill=ST, width=3)
    poly(d, [(7, 37), (13, 37), (9, 30)], ST)
    poly(d, [(31, 37), (25, 37), (29, 30)], ST)
    d.ellipse([10, 17, 12, 21], fill=ST)
    d.ellipse([26, 17, 28, 21], fill=ST)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(18, 21), (18, 38)], fill=STL)
    d.line([(20, 21), (20, 38)], fill=STD)
    d.line([(12, 18), (26, 18)], fill=STL)
    d.point([(17, 9), (16, 10)], fill=STL)
    d.point([(12, 37), (26, 37), (16, 41), (22, 41)], fill=STD)
    d.point([(9, 32), (29, 32)], fill=STL)
    return finish(im, L, ['..k..', '.k.k.', '..k..', 'kkkkk', '..k..'], {'k': STD})


def trident(pal):
    """Le trident doré du roi des mers, une gemme turquoise au centre."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.rectangle([18, 26, 20, 44], fill=Y)
    # traverse courbe
    d.arc([11, 14, 27, 30], 20, 160, fill=Y, width=3)
    # dents
    d.rectangle([18, 12, 20, 28], fill=Y)
    d.rectangle([11, 17, 13, 23], fill=Y)
    d.rectangle([25, 17, 27, 23], fill=Y)
    for x, top in ((12, 12), (19, 7), (26, 12)):
        poly(d, [(x - 2, top + 5), (x, top), (x + 2, top + 5)], Y)
    # pointes de harpon
    poly(d, [(11, 19), (9, 16), (11, 17)], Y)
    poly(d, [(27, 19), (29, 16), (27, 17)], Y)
    d.rectangle([17, 39, 21, 40], fill=Y)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(18, 12), (18, 43)], fill=YL)
    d.line([(20, 29), (20, 43)], fill=YD)
    d.line([(11, 17), (11, 22)], fill=YL)
    d.line([(25, 17), (25, 22)], fill=YL)
    d.line([(13, 17), (13, 22)], fill=YD)
    d.line([(27, 17), (27, 22)], fill=YD)
    d.rectangle([17, 27, 21, 31], fill=INK)
    d.rectangle([18, 28, 20, 30], fill=C)
    d.point((18, 28), fill=CL)
    d.line([(17, 39), (21, 39)], fill=YD)
    # éclats
    for (cx, cy) in [(9, 30), (29, 28)]:
        d.point([(cx - 1, cy), (cx + 1, cy), (cx, cy - 1), (cx, cy + 1)], fill=YD)
        d.point((cx, cy), fill=YL)
    return finish(im, L, ['k.k.k', 'k.k.k', 'kkkkk', '..k..', '..k..'], {'k': YD})


# ======================================================== Échoppe du combat
def russian_roulette(pal):
    """Le barillet d'un revolver vu de face : six chambres, une seule balle dorée."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    GN, GND, GNL = hexc('#5a6070'), hexc('#3a3e4a'), hexc('#8a92a4')
    # barillet à six lobes
    for k in range(6):
        a = math.radians(k * 60 + 30)
        cx, cy = 19 + 8 * math.cos(a), 27 + 8 * math.sin(a)
        d.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], fill=GN)
    d.ellipse([10, 18, 28, 36], fill=GN)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for k in range(6):
        a = math.radians(k * 60 - 90)
        cx, cy = round(19 + 7 * math.cos(a)), round(27 + 7 * math.sin(a))
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=INK)
        if k == 0:
            d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=YD)
            d.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=Y)
            d.point((cx - 1, cy - 1), fill=YL)
        else:
            d.point((cx - 1, cy - 2), fill=GND)
    d.ellipse([17, 25, 21, 29], fill=GNL)
    d.point((19, 27), fill=GND)
    d.arc([10, 18, 28, 36], 200, 250, fill=GNL)
    # flèche vers la chambre chargée
    A = layer()
    ad = ImageDraw.Draw(A)
    poly(ad, [(16, 7), (22, 7), (19, 11)], R)
    A = outline(A)
    L.alpha_composite(A)
    d = ImageDraw.Draw(L)
    # balles alignées en dessous
    BL = layer()
    bd = ImageDraw.Draw(BL)
    for x in (10, 16, 22):
        bd.rectangle([x, 41, x + 3, 43], fill=YD)
        bd.rectangle([x + 4, 41, x + 4, 43], fill=ST)
        bd.point((x + 5, 42), fill=ST)
    BL = outline(BL)
    bd = ImageDraw.Draw(BL)
    for x in (10, 16, 22):
        bd.line([(x, 41), (x + 3, 41)], fill=Y)
    L.alpha_composite(BL)
    return finish(im, L, ['.kkk.', 'k.k.k', 'kkykk', 'k.k.k', '.kkk.'], {'k': hexc('#5a6070'), 'y': Y})


def corruption(pal):
    """Un crâne sur lequel coule une bave verte, orbites qui luisent, flaque au pied."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    d.ellipse([9, 11, 29, 31], fill=BONE)
    d.rectangle([12, 26, 26, 37], fill=BONE)
    # bave : posée sur le haut du crâne, avec des coulures
    SL = layer()
    sd = ImageDraw.Draw(SL)
    sd.rectangle([0, 0, W, 16], fill=LIME)
    for (x, l) in [(10, 5), (15, 8), (21, 3), (25, 6)]:
        sd.rectangle([x, 16, x + 1, 16 + l], fill=LIME)
    import numpy as np
    sk = np.array(L)[:, :, 3] > 0
    arr = np.array(SL)
    arr[~sk] = 0
    SL = Image.fromarray(arr)
    sd = ImageDraw.Draw(SL)
    for (x, l) in [(10, 5), (15, 8), (21, 3), (25, 6)]:
        sd.rectangle([x - 1 if x > 9 else x, 15 + l, x + 2 if x < 28 else x + 1, 17 + l], fill=LIME)
    sa = np.array(SL)
    sm = sa[:, :, 3] > 0
    edge = sm & ~np.roll(sm, -1, axis=0) & np.roll(sk, -1, axis=0)
    sa[edge] = LIMED
    SL = Image.fromarray(sa)
    L.alpha_composite(SL)
    # flaque
    d = ImageDraw.Draw(L)
    d.ellipse([11, 40, 27, 44], fill=LIME)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(13, 13), (16, 12)], fill=hexc('#d4ffb0'))
    d.point([(12, 14)], fill=hexc('#d4ffb0'))
    # orbites lumineuses
    for ex in (15, 23):
        d.rectangle([ex - 2, 25, ex + 1, 28], fill=INK)
        d.point([(ex - 3, 26), (ex - 3, 27), (ex + 2, 26), (ex + 2, 27)], fill=INK)
        d.rectangle([ex - 1, 26, ex, 27], fill=LIME)
    d.point([(19, 29), (18, 30), (20, 30)], fill=INK)
    # dents
    d.line([(13, 33), (25, 33)], fill=INK)
    for x in (15, 18, 21, 24):
        d.line([(x, 33), (x, 36)], fill=INK)
    d.line([(13, 37), (25, 37)], fill=BONED)
    d.line([(26, 27), (26, 37)], fill=BONED)
    d.line([(12, 43), (26, 43)], fill=LIMED)
    d.point([(14, 41), (23, 41)], fill=hexc('#d4ffb0'))
    # gouttes qui tombent
    G2 = layer()
    gd = ImageDraw.Draw(G2)
    gd.rectangle([16, 37, 17, 38], fill=LIME)
    gd.rectangle([7, 33, 8, 34], fill=LIME)
    gd.rectangle([30, 22, 31, 23], fill=LIME)
    G2 = outline(G2)
    L.alpha_composite(G2)
    return finish(im, L, ['.ggg.', 'ggggg', 'g.g.g', '..g..', '..g..'], {'g': LIMED})


def cursed_joker(pal):
    """Le Joker maudit : visage gris fendu, yeux rouges, sourire cousu, bonnet noir et rouge sang."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    BK, BKL = hexc('#2a2430'), hexc('#4a4058')
    GR, GRD, GRL = hexc('#b8b4c4'), hexc('#8a8498'), hexc('#dcd8e4')
    # bonnet : trois pointes qui retombent, rouge sang et noir
    poly(d, [(11, 24), (15, 22), (9, 15), (10, 14)], BK)
    poly(d, [(15, 22), (23, 22), (19, 11)], RD)
    poly(d, [(23, 22), (27, 24), (29, 14), (28, 15)], BK)
    poly(d, [(12, 23), (14, 22), (10, 16)], BKL)
    poly(d, [(18, 22), (20, 22), (19, 13)], hexc('#5a0a14'))
    # visage
    d.ellipse([11, 23, 27, 39], fill=GR)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # bandeau à pointes (dents de scie)
    d.rectangle([11, 23, 27, 24], fill=BK)
    for x in range(12, 27, 3):
        d.point((x, 25), fill=BK)
    d.arc([11, 23, 27, 39], 10, 100, fill=GRD)
    d.point([(13, 28), (13, 29)], fill=GRL)
    # yeux : orbites noires, pupilles rouges, sourcils en V
    for ex in (15, 23):
        d.rectangle([ex - 2, 29, ex + 2, 31], fill=INK)
        d.rectangle([ex - 1, 30, ex, 30], fill=R) if ex < 19 else d.rectangle([ex, 30, ex + 1, 30], fill=R)
    d.line([(13, 27), (16, 28)], fill=INK)
    d.line([(25, 27), (22, 28)], fill=INK)
    # fissure sur le front et la joue
    d.line([(21, 25), (20, 27), (21, 28)], fill=INK)
    d.line([(24, 32), (25, 34)], fill=GRD)
    # larme de sang
    d.point([(15, 32), (15, 33)], fill=R)
    d.point((15, 34), fill=RD)
    # sourire cousu
    d.line([(13, 33), (14, 34), (16, 35), (22, 35), (24, 34), (25, 33)], fill=INK)
    d.line([(14, 35), (16, 36), (22, 36), (24, 35)], fill=RD)
    for x in (15, 18, 21, 24):
        d.line([(x, 34), (x, 37)], fill=INK)
    # collerette déchirée
    C2 = layer()
    cd = ImageDraw.Draw(C2)
    for i, x in enumerate(range(11, 26, 3)):
        poly(cd, [(x, 39), (x + 3, 39), (x + 1.5, 43 if i % 2 else 42)], RD if i % 2 == 0 else BK)
    C2 = outline(C2)
    L.alpha_composite(C2)
    d = ImageDraw.Draw(L)
    # grelots fêlés
    for (bx, by) in [(10, 14), (19, 10), (28, 14)]:
        d.rectangle([bx - 1, by - 1, bx + 1, by + 1], fill=ST)
        d.point((bx - 1, by - 1), fill=STL)
        d.point((bx + 1, by + 1), fill=INK)
    L = outline(L)
    # flammèches maudites
    d = ImageDraw.Draw(L)
    return finish(im, L, ['.k.k.', 'krkrk', '.rrr.', 'k.k.k'], {'k': hexc('#2a2430'), 'r': RD})


DESIGNS = {
    'bulle_d_air': bulle_d_air, 'bouchons_d_oreille': bouchons_d_oreille,
    'cocktail_des_abysses': cocktail_des_abysses,
    'piece_truquee': piece_truquee, 'levier_rouille': levier_rouille, 'jackpot_englouti': jackpot_englouti,
    'perle_noire': perle_noire, 'cage_a_requin': cage_a_requin, 'coffre_de_l_epave': coffre_de_l_epave,
    'harpon': harpon, 'ancre': ancre, 'trident': trident,
    'bingo': bingo, 'russian_roulette': russian_roulette, 'corruption': corruption,
    'cursed_joker': cursed_joker, 'overheat': overheat,
}
