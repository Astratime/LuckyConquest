"""Cartes des 3 archétypes du Valet à l'As (Cour royale, Revenant, Horloger ; demande d'Astra du 2026-11-08), au
style des cartes classiques (voir base.py et README.md). Comme les autres cartes d'archétype (cards_archetypes.py),
seul le rang est écrit, en index dans le coin haut gauche (lettres copiées des cartes classiques), suivi de
l'emblème de l'archétype ; aucun autre texte ni chiffre."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
import cards_archetypes  # noqa: E402
from cards_archetypes import finish, ranked, R, RD, RL, Y, YD, YL, P, PD, PL, B, BD, BL, G, GD, ST, STD, STL, \
    WD, WDD, WDL, DARK  # noqa: E402
from cards_w1 import poly, ob, stack, sparkle  # noqa: E402

GH, GHD, GHL = hexc('#c4cce4'), hexc('#8890b4'), hexc('#eef2ff')   # fantôme
BR, BRD, BRL = hexc('#d89a3a'), hexc('#8e5a1c'), hexc('#f6cc7a')   # laiton
EARTH, EARTHD = hexc('#8a5a34'), hexc('#5a361c')

COUR_I = ['y.y.y', 'yyyyy', 'yrypy', 'yyyyy']
REVENANT_I = ['.ggg.', 'gkgkg', 'ggggg', 'g.g.g']
HORLOGER_I = ['.kkk.', 'k.k.k', 'k.kkk', 'k...k', '.kkk.']
CP = {'y': Y, 'r': R, 'p': B}
GP = {'g': hexc('#5c6488'), 'k': WHITE}
HP = {'k': INK}

cards_archetypes.RANKS.update({
    'heraut': 11, 'dame_de_cour': 12, 'sceptre_du_roi': 13, 'quinte_royale': 1,
    'fossoyeur': 11, 'dame_blanche': 12, 'roi_des_ombres': 13, 'requiem': 1,
    'cle_de_remontage': 11, 'coucou': 12, 'grande_aiguille': 13, 'douze_coups': 1,
})


def crown(d, cx, top, w=6, gem=R):
    """Petite couronne d'or à trois pointes, centrée sur cx."""
    poly(d, [(cx - w, top + 7), (cx - w, top + 1), (cx - w // 2, top + 4), (cx, top), (cx + w // 2, top + 4),
             (cx + w, top + 1), (cx + w, top + 7)], Y)
    d.rectangle([cx - w, top + 6, cx + w, top + 8], fill=YD)
    d.point((cx, top + 7), fill=gem)


def mini_card(d, x, y, pip, col):
    """Petite carte blanche (7x11) avec une enseigne au centre."""
    d.rectangle([x, y, x + 6, y + 10], fill=WHITE)
    sprite(d, x + 1, y + 2, pip, {'k': col})


HEART_S = ['k.k', 'kkk', '.k.']
SPADE_S = ['.k.', 'kkk', '.k.']
DIAM_S = ['.k.', 'kkk', '.k.']
CLUB_S = ['.k.', 'kkk', 'k.k']


# ======================================================== Cour royale
def heraut(pal):
    """Une trompette de héraut d'or, d'où pend une bannière rouge à la couronne."""
    def trumpet(d):
        d.rectangle([7, 15, 26, 17], fill=Y)
        poly(d, [(25, 16), (32, 10), (32, 23)], Y)
        d.rectangle([6, 14, 8, 18], fill=YD)
        d.rectangle([14, 17, 16, 20], fill=YD)
    T = ob(trumpet)
    td = ImageDraw.Draw(T)
    td.line([(8, 15), (25, 15)], fill=YL)
    td.line([(30, 12), (30, 20)], fill=YL)

    def banner(d):
        poly(d, [(10, 19), (24, 19), (24, 40), (17, 35), (10, 40)], R)
    Ba = ob(banner)
    bd = ImageDraw.Draw(Ba)
    bd.line([(11, 20), (23, 20)], fill=RL)
    crown(bd, 17, 24, 4)
    bd.line([(11, 37), (11, 21)], fill=RD)
    return finish(pal, stack(Ba, T), COUR_I, CP)


def dame_de_cour(pal):
    """Un éventail de dame grand ouvert, coiffé d'un diadème."""
    def fan(d):
        d.pieslice([5, 14, 33, 42], 180, 360, fill=R)
        for k, a in enumerate(range(180, 360, 30)):
            d.pieslice([5, 14, 33, 42], a, a + 15, fill=RL if k % 2 else hexc('#f4a8c8'))
        d.rectangle([17, 27, 21, 40], fill=WD)
    F = ob(fan)
    fd = ImageDraw.Draw(F)
    fd.point((19, 39), fill=Y)
    for x, y in ((8, 25), (30, 25), (19, 16)):
        fd.point((x, y), fill=WHITE)
    Cr = ob(lambda d: crown(d, 19, 5, 5, B))
    return finish(pal, stack(F, Cr), COUR_I, CP)


def sceptre_du_roi(pal):
    """Le sceptre d'or du roi, en diagonale, surmonté d'un globe rouge qui brille."""
    def sceptre(d):
        d.line([(10, 44), (25, 17)], fill=Y, width=3)
        d.ellipse([7, 41, 12, 46], fill=YD)
        d.rectangle([21, 19, 29, 21], fill=YD)
        d.ellipse([21, 8, 31, 18], fill=R)
    S = ob(sceptre)
    sd = ImageDraw.Draw(S)
    sd.line([(11, 41), (24, 18)], fill=YL)
    sd.rectangle([25, 6, 26, 9], fill=Y)
    sd.point([(23, 10), (24, 10), (23, 11)], fill=RL)
    sd.arc([22, 9, 30, 17], 20, 120, fill=RD)
    sparkle(sd, 31, 26)
    sparkle(sd, 14, 22)
    return finish(pal, S, COUR_I, CP)


def quinte_royale(pal):
    """Cinq cartes en éventail sous une couronne : la plus belle main du poker."""
    pips = [(SPADE_S, INK), (HEART_S, R), (CLUB_S, INK), (DIAM_S, R), (SPADE_S, INK)]
    cards_ = []
    for i, (pip, col) in enumerate(pips):
        x = 5 + i * 5
        y = 30 - (3 if i in (1, 3) else 5 if i == 2 else 0)
        cards_.append(ob(lambda d, x=x, y=y, pip=pip, col=col: mini_card(d, x, y, pip, col)))
    H_ = stack(*cards_)
    Cr = ob(lambda d: crown(d, 19, 9, 7))
    L = stack(H_, Cr)
    d = ImageDraw.Draw(L)
    sparkle(d, 8, 14)
    sparkle(d, 31, 16)
    return finish(pal, L, COUR_I, CP)


# ======================================================== Revenant
def fossoyeur(pal):
    """Une pelle plantée dans un tas de terre d'où sort une carte."""
    def mound(d):
        d.chord([5, 34, 33, 54], 180, 360, fill=EARTH)
    M = ob(mound)
    md = ImageDraw.Draw(M)
    md.point([(12, 40), (20, 38), (26, 41)], fill=EARTHD)

    def card_(d):
        poly(d, [(9, 24), (16, 22), (19, 37), (12, 39)], WHITE)
    Ca = ob(card_)
    sprite(ImageDraw.Draw(Ca), 12, 28, HEART_S, {'k': R})

    def shovel(d):
        d.line([(26, 8), (26, 30)], fill=WD, width=2)
        d.rectangle([22, 7, 30, 9], fill=WD)
        poly(d, [(22, 29), (30, 29), (30, 36), (26, 41), (22, 36)], ST)
    Sh = ob(shovel)
    ImageDraw.Draw(Sh).line([(23, 30), (23, 35)], fill=STL)
    return finish(pal, stack(Ca, Sh, M), REVENANT_I, GP)


def dame_blanche(pal):
    """Une dame fantôme voilée, pâle et flottante."""
    def ghost(d):
        d.ellipse([11, 8, 27, 24], fill=GH)
        d.rectangle([11, 16, 27, 38], fill=GH)
        for i, x in enumerate(range(11, 28, 4)):
            d.ellipse([x - 1, 35 + (i % 2) * 2, x + 3, 40 + (i % 2) * 2], fill=GH)
        poly(d, [(11, 22), (5, 32), (11, 30)], GH)
        poly(d, [(27, 22), (33, 32), (27, 30)], GH)
    Gh = ob(ghost)
    gd = ImageDraw.Draw(Gh)
    gd.rectangle([15, 15, 16, 17], fill=INK)
    gd.rectangle([22, 15, 23, 17], fill=INK)
    gd.rectangle([18, 20, 20, 22], fill=INK)
    gd.point((19, 21), fill=GHD)
    gd.line([(13, 10), (13, 30)], fill=GHL)
    gd.line([(25, 22), (25, 34)], fill=GHD)
    gd.arc([10, 7, 28, 22], 200, 340, fill=GHL)
    return finish(pal, Gh, REVENANT_I, GP)


def roi_des_ombres(pal):
    """Une silhouette d'ombre couronnée, aux yeux jaunes qui luisent."""
    def shade(d):
        d.ellipse([12, 14, 26, 28], fill=DARK)
        poly(d, [(6, 44), (10, 30), (19, 26), (28, 30), (32, 44)], DARK)
        for x in (8, 14, 20, 26):
            poly(d, [(x, 43), (x + 3, 47), (x + 6, 43)], DARK)
    Sh = ob(shade)
    sd = ImageDraw.Draw(Sh)
    sd.rectangle([15, 20, 16, 21], fill=YL)
    sd.rectangle([22, 20, 23, 21], fill=YL)
    sd.line([(11, 32), (15, 29)], fill=PD)
    sd.line([(27, 32), (23, 29)], fill=PD)

    def crown_(d):
        poly(d, [(12, 15), (12, 7), (15, 11), (19, 5), (23, 11), (26, 7), (26, 15)], PD)
    Cr = ob(crown_)
    cd = ImageDraw.Draw(Cr)
    cd.point([(19, 12), (14, 13), (24, 13)], fill=PL)
    return finish(pal, stack(Sh, Cr), REVENANT_I, GP)


def requiem(pal):
    """Une pierre tombale entre deux bougies, et des cartes qui tourbillonnent autour."""
    def stone(d):
        d.chord([11, 16, 27, 32], 180, 360, fill=ST)
        d.rectangle([11, 24, 27, 42], fill=ST)
        d.rectangle([8, 41, 30, 44], fill=STD)
    St = ob(stone)
    sd = ImageDraw.Draw(St)
    sd.line([(13, 22), (13, 40)], fill=STL)
    sd.line([(16, 30), (22, 30)], fill=STD)
    sd.line([(16, 33), (22, 33)], fill=STD)

    def candles(d):
        for x in (5, 31):
            d.rectangle([x, 34, x + 2, 42], fill=WHITE)
            d.ellipse([x, 29, x + 2, 33], fill=Y)
    Ca = ob(candles)
    ImageDraw.Draw(Ca).point([(6, 31), (32, 31)], fill=YL)

    def swirl(d):
        d.rectangle([7, 9, 11, 15], fill=WHITE)
        d.rectangle([27, 8, 31, 14], fill=WHITE)
    Sw = ob(swirl)
    wd = ImageDraw.Draw(Sw)
    wd.point([(9, 12), (29, 11)], fill=R)
    wd.arc([9, 4, 29, 20], 200, 340, fill=GHD)
    return finish(pal, stack(St, Ca, Sw), REVENANT_I, GP)


# ======================================================== Horloger
def cle_de_remontage(pal):
    """Une grande clé de remontage en laiton, et un ressort qui se tend."""
    def key(d):
        d.ellipse([7, 8, 17, 18], fill=BR)
        d.ellipse([20, 8, 30, 18], fill=BR)
        d.rectangle([16, 12, 21, 16], fill=BR)
        d.rectangle([17, 16, 20, 30], fill=BR)
        d.rectangle([15, 28, 22, 31], fill=BRD)
    K = ob(key)
    kd = ImageDraw.Draw(K)
    kd.ellipse([10, 11, 14, 15], fill=pal[0])
    kd.ellipse([23, 11, 27, 15], fill=pal[0])
    kd.line([(8, 12), (9, 10)], fill=BRL)
    kd.line([(18, 17), (18, 28)], fill=BRL)

    def spring(d):
        for i in range(5):
            y = 33 + i * 3
            d.line([(12, y), (26, y + 1)], fill=ST, width=2)
    Sp = ob(spring)
    sd = ImageDraw.Draw(Sp)
    for i in range(5):
        sd.point((14, 33 + i * 3), fill=STL)
    return finish(pal, stack(Sp, K), HORLOGER_I, HP)


def coucou(pal):
    """Une horloge à coucou en bois, l'oiseau qui sort de sa porte."""
    def house(d):
        poly(d, [(6, 18), (19, 7), (32, 18)], WDD)
        d.rectangle([9, 17, 29, 40], fill=WD)
        d.rectangle([13, 25, 25, 37], fill=WHITE)
        d.rectangle([17, 13, 21, 18], fill=DARK)
    Ho = ob(house)
    hd = ImageDraw.Draw(Ho)
    hd.line([(10, 18), (10, 39)], fill=WDL)
    hd.line([(19, 31), (19, 27)], fill=INK)
    hd.line([(19, 31), (22, 31)], fill=INK)
    hd.point([(19, 26), (24, 31), (19, 36), (14, 31)], fill=INK)

    def chains(d):
        d.line([(14, 40), (14, 46)], fill=ST)
        d.line([(24, 40), (24, 44)], fill=ST)
        d.rectangle([12, 46, 16, 48], fill=BR)
        d.rectangle([22, 44, 26, 46], fill=BR)
    Ch = ob(chains)

    def bird(d):
        d.ellipse([16, 9, 25, 16], fill=Y)
        poly(d, [(25, 12), (29, 13), (25, 14)], R)
    Bi = ob(bird)
    ImageDraw.Draw(Bi).point((22, 11), fill=INK)
    return finish(pal, stack(Ch, Ho, Bi), HORLOGER_I, HP)


def grande_aiguille(pal):
    """Un cadran d'horloge et sa grande aiguille rouge qui avance."""
    def dial(d):
        d.ellipse([6, 14, 32, 40], fill=BR)
        d.ellipse([8, 16, 30, 38], fill=WHITE)
    Di = ob(dial)
    dd = ImageDraw.Draw(Di)
    import math
    for k in range(12):
        a = math.radians(k * 30)
        x, y = 19 + 9 * math.sin(a), 27 - 9 * math.cos(a)
        dd.point((round(x), round(y)), fill=INK)
    dd.line([(19, 27), (23, 29)], fill=INK, width=1)
    dd.arc([7, 15, 31, 39], 200, 260, fill=BRL)

    def hand(d):
        poly(d, [(18, 28), (20, 26), (31, 9), (29, 9)], R)
    Ha = ob(hand)
    ImageDraw.Draw(Ha).point((19, 27), fill=Y)
    L = stack(Di, Ha)
    d = ImageDraw.Draw(L)
    sparkle(d, 9, 44)
    return finish(pal, L, HORLOGER_I, HP)


def douze_coups(pal):
    """Une cloche d'or frappée par son marteau, qui sonne à toute volée."""
    def bell(d):
        d.chord([10, 12, 28, 30], 180, 360, fill=Y)
        poly(d, [(10, 21), (28, 21), (31, 38), (7, 38)], Y)
        d.rectangle([6, 37, 32, 40], fill=YD)
        d.ellipse([17, 39, 21, 43], fill=YD)
        d.rectangle([17, 9, 21, 12], fill=YD)
    Be = ob(bell)
    bd = ImageDraw.Draw(Be)
    bd.line([(12, 22), (10, 36)], fill=YL)
    bd.line([(13, 31), (25, 31)], fill=YD)

    def hammer(d):
        d.line([(33, 24), (27, 33)], fill=WD, width=2)
        d.rectangle([30, 19, 34, 25], fill=ST)
    Hm = ob(hammer)
    L = stack(Be, Hm)
    d = ImageDraw.Draw(L)
    for (x0, y0), (x1, y1) in (((5, 16), (7, 19)), ((3, 24), (6, 25)), ((5, 31), (7, 30))):
        d.line([(x0, y0), (x1, y1)], fill=INK)
    sparkle(d, 31, 44)
    return finish(pal, L, HORLOGER_I, HP)


DESIGNS = {f.__name__: ranked(f.__name__, f) for f in (
    heraut, dame_de_cour, sceptre_du_roi, quinte_royale,
    fossoyeur, dame_blanche, roi_des_ombres, requiem,
    cle_de_remontage, coucou, grande_aiguille, douze_coups,
)}
