"""Cartes des 5 archétypes de deck (Joker, Lutin, Rentier, Bluffeur, Relanceur), au style des cartes classiques
(voir base.py et README.md). Aucun texte. Seule exception à « aucun chiffre » : le rang de la carte (pour les
combinaisons, demandes d'Astra du 2026-10-25), en index dans le coin haut gauche comme sur les cartes classiques
(A, K, Q, J, 10, 9 : les mêmes lettres que les cartes classiques), suivi de l'emblème de l'archétype.
L'index ne doit cacher aucune partie du dessin : le dessin est placé sous l'index et, s'il ne tient pas, resserré
en retirant les lignes et colonnes qui changent le moins le dessin (voir fit_art)."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
import numpy as np  # noqa: E402
import cards_w1  # noqa: E402
from cards_w1 import poly, ob, stack, sparkle, coin_stack, big_coin  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
P, PD, PL = hexc('#9a4ad8'), hexc('#5e2c8f'), hexc('#c890f0')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ab4ff')
C, CD, CL = hexc('#5cc8d8'), hexc('#2e8aa0'), hexc('#c8f4f6')
O, OD = hexc('#f08a2c'), hexc('#b0521a')
S, SD = hexc('#f6dcbc'), hexc('#d8aa84')
WD, WDD, WDL = hexc('#a0643a'), hexc('#6a3e22'), hexc('#c88a58')
ST, STD, STL = hexc('#9aa6b4'), hexc('#5e6878'), hexc('#d4dce4')
PAPER, PAPERD = hexc('#f4ecd4'), hexc('#c8b890')
DARK = hexc('#1a1028')
RAINBOW = [R, O, Y, G, B, P]

JOKER_I = ['p.g.p', 'ppgpp', '.ppp.', '.yyy.']
LUTIN_I = ['.ggg.', '.ggg.', '.gyg.', 'ggggg']
RENTE_I = ['.yyy.', 'yylyy', 'ylyyy', 'yyyyy', '.ddd.']
BLUFF_I = ['kkkkk', 'k.k.k', 'kkkkk', '.kkk.']
RELANCE_I = ['kkkkk', 'k.r.k', 'kkkkk', '..k..', '.kkk.']
JP = {'p': P, 'g': G, 'y': Y}
LP = {'g': G, 'y': Y}
RP = {'y': Y, 'l': YL, 'd': YD}
BP = {'k': INK}
XP = {'k': INK, 'r': R}


def jester_hat(d, cx, top, w=9):
    """Bonnet de fou à trois pointes et grelots, centré sur cx, bord bas à top + 9."""
    base_y = top + 9
    poly(d, [(cx - w, base_y), (cx - w - 2, top), (cx - 2, base_y - 3)], P)
    poly(d, [(cx - 3, base_y - 2), (cx, top - 2), (cx + 3, base_y - 2)], G)
    poly(d, [(cx + w, base_y), (cx + w + 2, top), (cx + 2, base_y - 3)], P)
    d.rectangle([cx - w, base_y - 1, cx + w, base_y + 1], fill=Y)
    for x, y in ((cx - w - 2, top - 1), (cx, top - 3), (cx + w + 2, top - 1)):
        d.ellipse([x - 1, y - 1, x + 1, y + 1], fill=Y)


def reel(d, x0, y0, x1, y1):
    """Fenêtre de rouleau (cadre gris, fond clair)."""
    d.rectangle([x0, y0, x1, y1], fill=STD)
    d.rectangle([x0 + 1, y0 + 1, x1 - 1, y1 - 1], fill=WHITE)
    d.line([(x0 + 1, y0 + 1), (x1 - 1, y0 + 1)], fill=STL)


def cherry(d, x, y):
    d.line([(x + 1, y - 3), (x + 3, y - 5)], fill=GD)
    d.line([(x + 4, y - 2), (x + 3, y - 5)], fill=GD)
    d.ellipse([x - 1, y - 2, x + 2, y + 1], fill=R)
    d.ellipse([x + 3, y - 1, x + 6, y + 2], fill=R)
    d.point([(x, y - 1), (x + 4, y)], fill=RL)


def lever(d, x, top, bottom, knob=R):
    d.rectangle([x, top + 2, x + 1, bottom], fill=STL)
    d.line([(x + 1, top + 2), (x + 1, bottom)], fill=ST)
    d.ellipse([x - 2, top - 2, x + 3, top + 3], fill=knob)
    d.point((x - 1, top - 1), fill=WHITE)


# ======================================================== Joker
def joker_sauvage(pal):
    """Un bonnet de fou au-dessus de trois griffures : le Joker devient sauvage."""
    hat = ob(lambda d: jester_hat(d, 19, 9))

    def claws(d):
        for i, x in enumerate((11, 18, 25)):
            poly(d, [(x, 24 + i), (x + 3, 23 + i), (x + 2, 44 - i), (x - 1, 43 - i)], R)
            d.line([(x + 1, 25 + i), (x + 1, 40 - i)], fill=RL)
    return finish(pal, stack(ob(claws), hat), JOKER_I, JP)


def rire_du_joker(pal):
    """Une grande bouche de Joker qui rit et crache deux cartes."""
    def mouth(d):
        d.ellipse([7, 20, 31, 40], fill=RD)
        d.rectangle([7, 18, 31, 28], fill=(0, 0, 0, 0))
        d.chord([7, 14, 31, 40], 0, 180, fill=RD)
        d.chord([10, 18, 28, 37], 0, 180, fill=DARK)
        d.rectangle([11, 27, 27, 28], fill=WHITE)
        d.chord([14, 30, 24, 37], 180, 360, fill=R)

    def cards_(d):
        for x, y in ((9, 7), (21, 6)):
            d.rectangle([x, y, x + 7, y + 10], fill=WHITE)
            d.rectangle([x + 2, y + 2, x + 5, y + 8], fill=P)
            d.point((x + 3, y + 4), fill=Y)
    L = stack(ob(cards_), ob(mouth))
    ImageDraw.Draw(L).line([(12, 27), (26, 27)], fill=STL)
    return finish(pal, L, JOKER_I, JP)


def couronne_du_fou(pal):
    """Une couronne d'or dont les pointes finissent en grelots de fou."""
    def crown(d):
        poly(d, [(7, 36), (7, 20), (13, 28), (19, 16), (25, 28), (31, 20), (31, 36)], Y)
        d.rectangle([7, 34, 31, 39], fill=YD)
        for x, y, c in ((7, 18, P), (19, 14, G), (31, 18, P)):
            d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=c)
    L = ob(crown)
    d = ImageDraw.Draw(L)
    for x, c in ((12, R), (19, G), (26, B)):
        d.rectangle([x - 1, 35, x + 1, 37], fill=c)
    d.line([(9, 24), (9, 32)], fill=YL)
    d.point([(19, 13), (6, 17), (30, 17)], fill=WHITE)
    return finish(pal, L, JOKER_I, JP)


def atelier_du_joker(pal):
    """Une fenêtre de rouleau où l'on visse un bonnet de fou, et une clé à molette."""
    def win(d):
        reel(d, 8, 10, 30, 32)
    L = ob(win)
    L.alpha_composite(ob(lambda d: jester_hat(d, 19, 17, 6)))

    def wrench(d):
        d.line([(12, 44), (24, 36)], fill=ST, width=3)
        d.ellipse([22, 31, 30, 39], fill=ST)
        d.rectangle([25, 33, 27, 35], fill=(0, 0, 0, 0))
        d.ellipse([9, 41, 14, 46], fill=ST)
    W = ob(wrench)
    ImageDraw.Draw(W).line([(13, 42), (23, 35)], fill=STL)
    return finish(pal, stack(L, W), JOKER_I, JP)


def joker_cameleon(pal):
    """Un caméléon enroulé sur une carte aux quatre enseignes."""
    def card_(d):
        d.rectangle([8, 8, 22, 28], fill=WHITE)
    L = ob(card_)
    d = ImageDraw.Draw(L)
    for (x, y), c in zip(((10, 10), (17, 10), (10, 22), (17, 22)), (INK, R, INK, R)):
        d.rectangle([x, y, x + 2, y + 2], fill=c)

    def cham(d):
        d.ellipse([10, 28, 28, 42], fill=G)
        d.ellipse([22, 24, 32, 33], fill=G)
        d.arc([6, 34, 16, 46], 90, 360, fill=G, width=3)
        d.line([(14, 41), (12, 46)], fill=GD, width=2)
        d.line([(23, 41), (25, 46)], fill=GD, width=2)
    Ch = ob(cham)
    cd = ImageDraw.Draw(Ch)
    for i, c in enumerate(RAINBOW):
        cd.point([(12 + 2 * i, 33), (13 + 2 * i, 34)], fill=c)
    cd.ellipse([26, 26, 29, 29], fill=WHITE)
    cd.point((28, 27), fill=INK)
    cd.line([(12, 30), (20, 29)], fill=GL)
    return finish(pal, stack(L, Ch), JOKER_I, JP)


# ======================================================== Lutin
def chapeau_du_lutin(pal):
    """Le haut-de-forme vert du lutin à boucle d'or, des pièces qui en tombent."""
    def hat(d):
        d.rectangle([12, 10, 26, 30], fill=G)
        d.ellipse([6, 28, 32, 35], fill=G)
        d.rectangle([12, 25, 26, 29], fill=DARK)
        d.rectangle([17, 24, 21, 30], fill=Y)
        d.rectangle([18, 26, 20, 28], fill=DARK)
    L = ob(hat)
    d = ImageDraw.Draw(L)
    d.line([(13, 11), (13, 24)], fill=GL)
    d.line([(8, 31), (30, 31)], fill=GD)
    C_ = ob(lambda d: [big_coin(d, x, y, 5) for x, y in ((9, 38), (17, 41), (25, 37))])
    return finish(pal, stack(L, C_), LUTIN_I, LP)


def gigue_du_lutin(pal):
    """Une chaussure de lutin qui danse, entourée de notes de musique."""
    def shoe(d):
        poly(d, [(9, 34), (11, 26), (19, 26), (21, 31), (30, 32), (31, 37), (9, 38)], G)
        d.rectangle([9, 37, 31, 39], fill=WDD)
        d.rectangle([13, 27, 17, 30], fill=Y)
        d.rectangle([14, 28, 16, 29], fill=G)
        d.ellipse([29, 31, 32, 34], fill=G)
    L = ob(shoe)
    ImageDraw.Draw(L).line([(12, 33), (20, 33)], fill=GL)

    def notes(d):
        for x, y in ((9, 13), (22, 9)):
            d.ellipse([x, y + 6, x + 4, y + 9], fill=INK)
            d.rectangle([x + 3, y, x + 4, y + 7], fill=INK)
            d.line([(x + 4, y), (x + 7, y + 3)], fill=INK, width=2)
    N = layer()
    notes(ImageDraw.Draw(N))
    S_ = layer()
    sd = ImageDraw.Draw(S_)
    for x, y in ((28, 20), (8, 23), (18, 18)):
        sparkle(sd, x, y, Y)
    return finish(pal, stack(N, S_, L), LUTIN_I, LP)


def bout_de_l_arc_en_ciel(pal):
    """Un arc-en-ciel qui finit dans une marmite d'or."""
    A = layer()
    d = ImageDraw.Draw(A)
    for i, c in enumerate(RAINBOW):
        d.arc([2 + 2 * i, 8 + 2 * i, 38 - 2 * i, 52 - 2 * i], 180, 290, fill=c, width=2)
    A = outline(A)

    def pot(d):
        d.ellipse([16, 33, 32, 46], fill=DARK)
        d.rectangle([16, 31, 32, 34], fill=DARK)
        d.ellipse([17, 27, 31, 33], fill=Y)
    P_ = ob(pot)
    pd = ImageDraw.Draw(P_)
    pd.point([(20, 29), (24, 28), (27, 30)], fill=YL)
    pd.line([(18, 36), (18, 42)], fill=STD)
    return finish(pal, stack(A, P_), LUTIN_I, LP)


def marteau_du_cordonnier(pal):
    """Le marteau du cordonnier frappe une pièce qui jaillit en éclats d'or."""
    def hammer(d):
        poly(d, [(12, 42), (14, 44), (27, 24), (25, 22)], WD)
        d.rectangle([20, 12, 32, 18], fill=ST)
        poly(d, [(20, 12), (17, 14), (17, 16), (20, 18)], ST)
        d.rectangle([24, 18, 27, 23], fill=STD)
    H = ob(hammer)
    hd = ImageDraw.Draw(H)
    hd.line([(21, 13), (31, 13)], fill=STL)
    hd.line([(13, 42), (25, 24)], fill=WDL)
    C_ = ob(lambda d: big_coin(d, 6, 22, 9))
    S_ = layer()
    sd = ImageDraw.Draw(S_)
    for x, y in ((8, 17), (16, 20), (6, 34)):
        sparkle(sd, x, y, Y)
    return finish(pal, stack(C_, S_, H), LUTIN_I, LP)


def farce_du_lutin(pal):
    """Un bouclier fendu d'où s'échappe une pluie de pièces : la défense ennemie paie."""
    def shield(d):
        poly(d, [(8, 9), (30, 9), (30, 24), (19, 36), (8, 24)], ST)
        poly(d, [(10, 11), (28, 11), (28, 23), (19, 33), (10, 23)], STL)
    L = ob(shield)
    d = ImageDraw.Draw(L)
    d.line([(19, 11), (17, 17), (21, 22), (18, 28), (19, 33)], fill=INK, width=2)
    d.line([(11, 12), (11, 22)], fill=WHITE)
    C_ = ob(lambda d: [coin_stack(d, x, 46, n, 3) for x, n in ((11, 3), (19, 5), (27, 2))])
    return finish(pal, stack(L, C_), LUTIN_I, LP)


# ======================================================== Rentier
def reconnaissance_de_dette(pal):
    """Un parchemin signé et scellé de cire rouge."""
    def scroll(d):
        d.rectangle([9, 9, 29, 42], fill=PAPER)
        d.ellipse([7, 6, 31, 12], fill=PAPERD)
        d.ellipse([7, 39, 31, 45], fill=PAPERD)
    L = ob(scroll)
    d = ImageDraw.Draw(L)
    for y in (15, 19, 23, 27):
        d.line([(12, y), (26 - (y % 3) * 2, y)], fill=STD)
    d.line([(12, 33), (14, 31), (16, 34), (18, 31), (20, 33)], fill=B)
    W = ob(lambda d: d.ellipse([21, 31, 29, 39], fill=R))
    wd = ImageDraw.Draw(W)
    wd.ellipse([23, 33, 27, 37], fill=RD)
    wd.point((23, 32), fill=RL)
    return finish(pal, stack(L, W), RENTE_I, RP)


def taux_variable(pal):
    """Une courbe en zigzag entre une flèche qui monte et une flèche qui descend."""
    def board(d):
        d.rectangle([7, 12, 31, 38], fill=WHITE)
    L = ob(board)
    d = ImageDraw.Draw(L)
    for y in (18, 25, 32):
        d.line([(8, y), (30, y)], fill=STL)
    d.line([(9, 30), (14, 22), (19, 28), (24, 16), (29, 24)], fill=G, width=2)

    def arrows(d):
        poly(d, [(19, 4), (24, 9), (21, 9), (21, 11), (17, 11), (17, 9), (14, 9)], G)
        poly(d, [(19, 47), (24, 42), (21, 42), (21, 40), (17, 40), (17, 42), (14, 42)], R)
    return finish(pal, stack(L, ob(arrows)), RENTE_I, RP)


def interets_composes(pal):
    """Des piles de pièces de plus en plus hautes, une petite pousse sur la dernière."""
    def piles(d):
        for x, n in ((9, 2), (16, 4), (23, 7), (30, 11)):
            coin_stack(d, x, 45, n, 3)
    L = ob(piles)

    def sprout(d):
        d.line([(30, 21), (30, 16)], fill=GD)
        d.ellipse([25, 12, 30, 16], fill=G)
        d.ellipse([30, 10, 35, 14], fill=G)
    S_ = ob(sprout)
    return finish(pal, stack(L, S_), RENTE_I, RP, dx=-1)


def recouvrement(pal):
    """Un sac d'or ficelé et cadenassé d'une chaîne."""
    def bag(d):
        d.ellipse([7, 20, 31, 45], fill=WDL)
        poly(d, [(14, 21), (12, 13), (19, 17), (26, 13), (24, 21)], WDL)
        d.rectangle([14, 19, 24, 21], fill=WDD)
    L = ob(bag)
    d = ImageDraw.Draw(L)
    d.line([(7, 30), (31, 34)], fill=ST, width=2)
    for x in range(8, 31, 4):
        d.point((x, 30 + (x - 7) // 6), fill=STL)
    d.line([(10, 24), (10, 38)], fill=hexc('#e4b484'))

    def lock(d):
        d.arc([16, 29, 24, 39], 180, 360, fill=STD, width=2)
        d.rectangle([15, 34, 25, 42], fill=Y)
    K = ob(lock)
    kd = ImageDraw.Draw(K)
    kd.rectangle([19, 36, 21, 39], fill=DARK)
    kd.line([(16, 35), (24, 35)], fill=YL)
    return finish(pal, stack(L, K), RENTE_I, RP)


def rente_viagere(pal):
    """Un sablier qui laisse couler des pièces au lieu du sable."""
    def glass(d):
        d.rectangle([9, 7, 29, 10], fill=WD)
        d.rectangle([9, 42, 29, 45], fill=WD)
        poly(d, [(11, 11), (27, 11), (20, 26), (18, 26)], CL)
        poly(d, [(18, 26), (20, 26), (27, 41), (11, 41)], CL)
    L = ob(glass)
    d = ImageDraw.Draw(L)
    poly(d, [(14, 15), (24, 15), (20, 23), (18, 23)], Y)
    d.line([(19, 24), (19, 34)], fill=Y)
    poly(d, [(13, 41), (19, 34), (25, 41)], Y)
    d.point([(16, 16), (21, 17), (17, 39), (22, 40)], fill=YL)
    d.line([(10, 8), (28, 8)], fill=WDL)
    d.line([(10, 43), (28, 43)], fill=WDL)
    return finish(pal, L, RENTE_I, RP)


# ======================================================== Bluffeur
def intimidation(pal):
    """Deux yeux furieux sous des sourcils froncés, et un poing serré."""
    def eyes(d):
        for x in (8, 21):
            d.ellipse([x, 12, x + 9, 20], fill=WHITE)
    L = ob(eyes)
    d = ImageDraw.Draw(L)
    for x in (12, 25):
        d.rectangle([x - 1, 14, x + 1, 18], fill=R)
        d.point((x, 16), fill=INK)
    d.line([(7, 9), (17, 13)], fill=INK, width=2)
    d.line([(31, 9), (21, 13)], fill=INK, width=2)

    def fist(d):
        d.rounded_rectangle([11, 26, 27, 40], radius=3, fill=S)
        d.rectangle([14, 39, 24, 46], fill=S)
    F = ob(fist)
    fd = ImageDraw.Draw(F)
    for x in (15, 19, 23):
        fd.line([(x, 27), (x, 33)], fill=SD)
    fd.line([(12, 34), (21, 34)], fill=SD)
    fd.line([(12, 28), (12, 32)], fill=WHITE)
    return finish(pal, stack(L, F), BLUFF_I, BP)


def cartes_marquees(pal):
    """Une carte au dos marqué d'un coin plié, vue à la loupe."""
    def back(d):
        d.rectangle([8, 7, 24, 31], fill=B)
        d.rectangle([10, 9, 22, 29], fill=BD)
    L = ob(back)
    d = ImageDraw.Draw(L)
    for i in range(0, 20, 4):
        d.line([(10, 9 + i), (22, 21 + i)], fill=BL)
    poly(d, [(24, 7), (24, 12), (19, 7)], WHITE)

    def loupe(d):
        d.line([(26, 41), (31, 47)], fill=WDD, width=3)
        d.ellipse([13, 22, 30, 39], fill=STD)
        d.ellipse([15, 24, 28, 37], fill=CL)
    M = ob(loupe)
    md = ImageDraw.Draw(M)
    md.line([(17, 30), (21, 33), (26, 27)], fill=R, width=2)
    md.point([(17, 26), (18, 25)], fill=WHITE)
    return finish(pal, stack(L, M), BLUFF_I, BP)


def fausse_piste(pal):
    """Un poteau indicateur aux flèches qui pointent dans des sens opposés."""
    def post(d):
        d.rectangle([18, 10, 21, 46], fill=WD)
        poly(d, [(8, 12), (27, 12), (31, 16), (27, 20), (8, 20)], Y)
        poly(d, [(31, 24), (12, 24), (8, 28), (12, 32), (31, 32)], R)
        poly(d, [(9, 36), (26, 36), (29, 39), (26, 42), (9, 42)], G)
    L = ob(post)
    d = ImageDraw.Draw(L)
    d.line([(9, 13), (26, 13)], fill=YL)
    d.line([(13, 25), (30, 25)], fill=RL)
    d.line([(10, 37), (25, 37)], fill=GL)
    return finish(pal, L, BLUFF_I, BP)


def tour_de_passe_passe(pal):
    """Un chapeau de magicien, une baguette et des étincelles : le symbole disparaît."""
    def hat(d):
        d.rectangle([12, 22, 26, 40], fill=DARK)
        d.ellipse([7, 38, 31, 44], fill=DARK)
        d.rectangle([12, 35, 26, 38], fill=R)
    L = ob(hat)
    ImageDraw.Draw(L).line([(13, 23), (13, 34)], fill=PD)

    def wand(d):
        poly(d, [(20, 18), (22, 20), (31, 9), (29, 7)], INK)
        poly(d, [(29, 7), (31, 9), (32, 8), (30, 6)], WHITE)
    W = layer()
    wand(ImageDraw.Draw(W))
    S_ = layer()
    sd = ImageDraw.Draw(S_)
    for x, y, c in ((10, 10, Y), (17, 7, PL), (25, 16, Y), (9, 18, C)):
        sparkle(sd, x, y, c)
    return finish(pal, stack(S_, L, W), BLUFF_I, BP)


def coup_de_bluff(pal):
    """Un éventail de cartes face cachée derrière une pile de jetons."""
    L = layer()
    for dx, dy in ((-7, 3), (7, 3), (0, 0)):
        def one(d, x=19 + dx, y=9 + dy):
            d.rectangle([x - 5, y, x + 5, y + 18], fill=WHITE)
            d.rectangle([x - 4, y + 1, x + 4, y + 17], fill=R)
            d.rectangle([x - 2, y + 3, x + 2, y + 15], fill=RD)
            d.point([(x, y + 6), (x, y + 9), (x, y + 12)], fill=RL)
        L.alpha_composite(ob(one))

    def chips(d):
        coin_stack(d, 19, 45, 5, 6, c=(B, BD, BL), stripe=WHITE)
    return finish(pal, stack(L, ob(chips)), BLUFF_I, BP)


# ======================================================== Relanceur
def rouleau_retenu(pal):
    """Un rouleau bloqué par un cadenas, la cerise reste en place."""
    def win(d):
        reel(d, 9, 8, 29, 34)
    L = ob(win)
    d = ImageDraw.Draw(L)
    cherry(d, 15, 24)
    d.line([(10, 14), (28, 14)], fill=STL)
    d.line([(10, 30), (28, 30)], fill=STL)

    def lock(d):
        d.arc([14, 30, 24, 42], 180, 360, fill=STD, width=2)
        d.rectangle([12, 36, 26, 46], fill=Y)
    K = ob(lock)
    kd = ImageDraw.Draw(K)
    kd.rectangle([18, 39, 20, 43], fill=DARK)
    kd.line([(13, 37), (25, 37)], fill=YL)
    return finish(pal, stack(L, K), RELANCE_I, XP)


def memoire_de_la_machine(pal):
    """Une petite machine à sous qui rêve d'une cerise, dans une bulle de pensée."""
    def machine(d):
        d.rounded_rectangle([8, 28, 28, 46], radius=2, fill=R)
        reel(d, 11, 32, 25, 39)
    M = ob(machine)
    md = ImageDraw.Draw(M)
    md.line([(9, 29), (9, 45)], fill=RL)
    for x in (13, 17, 21):
        md.rectangle([x, 34, x + 2, 37], fill=STL)
    M.alpha_composite(ob(lambda d: lever(d, 30, 30, 40)))

    def bubble(d):
        d.ellipse([12, 6, 32, 22], fill=WHITE)
        d.ellipse([10, 22, 14, 26], fill=WHITE)
        d.ellipse([7, 25, 9, 27], fill=WHITE)
    B_ = ob(bubble)
    cherry(ImageDraw.Draw(B_), 18, 16)
    return finish(pal, stack(B_, M), RELANCE_I, XP)


def calibrage(pal):
    """Un cadran à aiguille et un tournevis qui le règle."""
    def gauge(d):
        d.ellipse([7, 9, 31, 33], fill=STD)
        d.ellipse([9, 11, 29, 31], fill=WHITE)
    L = ob(gauge)
    d = ImageDraw.Draw(L)
    d.arc([11, 13, 27, 29], 150, 210, fill=R, width=2)
    d.arc([11, 13, 27, 29], 210, 330, fill=Y, width=2)
    d.arc([11, 13, 27, 29], 330, 390, fill=G, width=2)
    d.line([(19, 22), (24, 16)], fill=INK, width=2)
    d.ellipse([17, 20, 21, 24], fill=INK)

    def driver(d):
        poly(d, [(8, 46), (11, 47), (17, 38), (14, 36)], B)
        d.line([(16, 37), (23, 29)], fill=ST, width=2)
    S_ = ob(driver)
    ImageDraw.Draw(S_).line([(10, 45), (15, 38)], fill=BL)
    return finish(pal, stack(L, S_), RELANCE_I, XP)


def second_levier(pal):
    """Deux leviers côte à côte, le second tiré vers le bas."""
    def body(d):
        d.rounded_rectangle([8, 30, 30, 44], radius=2, fill=STD)
        d.rectangle([10, 32, 28, 34], fill=ST)
    L = ob(body)

    def levers(d):
        lever(d, 13, 9, 31, R)
        d.rectangle([24, 22, 25, 31], fill=STL)
        d.ellipse([22, 17, 27, 22], fill=G)
    V = ob(levers)
    vd = ImageDraw.Draw(V)
    vd.point((23, 18), fill=WHITE)
    vd.line([(28, 12), (28, 17)], fill=G)
    poly(vd, [(26, 17), (30, 17), (28, 20)], G)
    return finish(pal, stack(V, L), RELANCE_I, XP)


def martingale(pal):
    """Des piles de jetons qui doublent à chaque fois, et une flèche qui relance."""
    def piles(d):
        for x, n, c in ((9, 2, (R, RD, RL)), (18, 4, (B, BD, BL)), (28, 8, (G, GD, GL))):
            coin_stack(d, x, 45, n, 3, c=c, stripe=WHITE)
    L = ob(piles)
    A = layer()
    ad = ImageDraw.Draw(A)
    ad.arc([6, 10, 28, 34], 200, 340, fill=Y, width=2)
    poly(ad, [(25, 10), (30, 15), (24, 17)], Y)
    A = outline(A)
    return finish(pal, stack(A, L), RELANCE_I, XP)


# ======================================================== Nouveaux lieux (2026-10-24)
def diable_en_boite(pal):
    """Une boîte à ressort d'où jaillit une tête de Joker en bonnet de fou."""
    def box(d):
        d.rectangle([9, 34, 29, 47], fill=R)
        d.rectangle([9, 34, 29, 35], fill=RL)
        d.rectangle([27, 36, 29, 47], fill=RD)
        poly(d, [(9, 34), (4, 29), (6, 27), (11, 33)], RD)
    L = ob(box)
    d = ImageDraw.Draw(L)
    for x in (13, 19, 25):
        d.rectangle([x - 1, 39, x + 1, 42], fill=Y)
    d.rectangle([30, 39, 32, 40], fill=ST)

    def spring(d):
        for k in range(4):
            y = 32 - k * 3
            d.line([(16, y), (22, y - 2)], fill=STL, width=1)
            d.line([(22, y - 2), (16, y - 3)], fill=ST, width=1)
    Sp = ob(spring)

    def head(d):
        d.ellipse([12, 14, 26, 25], fill=S)
        d.arc([15, 18, 23, 24], 20, 160, fill=RD, width=1)
        d.point([(16, 18), (22, 18)], fill=INK)
        d.ellipse([18, 19, 20, 21], fill=R)
    H = ob(head)
    hat = ob(lambda d: jester_hat(d, 19, 5, w=7))
    return finish(pal, stack(Sp, L, H, hat), JOKER_I, JP)


def chamboule_tout(pal):
    """Une pyramide de boîtes de conserve qui dégringole sous une balle."""
    cols = [(R, RD, RL), (B, BD, BL), (G, GD, GL)]

    def cans(d):
        spots = [(9, 39), (17, 39), (25, 39), (13, 30), (22, 27)]
        for i, (x, y) in enumerate(spots):
            c, cd, cl = cols[i % 3]
            d.rectangle([x, y, x + 6, y + 8], fill=c)
            d.rectangle([x, y, x + 6, y + 1], fill=STL)
            d.rectangle([x, y + 7, x + 6, y + 8], fill=ST)
            d.line([(x + 1, y + 2), (x + 1, y + 6)], fill=cl)
            d.line([(x + 5, y + 2), (x + 5, y + 6)], fill=cd)
    L = ob(cans)

    def tilted(d):
        poly(d, [(24, 16), (30, 14), (32, 21), (26, 23)], Y)
        d.line([(24, 16), (30, 14)], fill=STL)
    T = ob(tilted)

    def ball(d):
        d.ellipse([5, 11, 13, 19], fill=WDL)
        d.arc([6, 12, 12, 18], 200, 300, fill=WHITE)
    Bl = ob(ball)
    bd = ImageDraw.Draw(Bl)
    for k in range(3):
        bd.point((14 + 2 * k, 18 + k), fill=STD)
    return finish(pal, stack(L, T, Bl), RELANCE_I, XP)


def sceau_du_pharaon(pal):
    """Un parchemin scellé d'un grand sceau d'or à l'oeil du Pharaon."""
    def scroll(d):
        d.rectangle([8, 12, 30, 40], fill=PAPER)
        d.rectangle([6, 9, 32, 13], fill=PAPERD)
        d.rectangle([6, 39, 32, 43], fill=PAPERD)
        for y in (17, 21, 25):
            d.line([(11, y), (27, y)], fill=PAPERD)
    L = ob(scroll)

    def seal(d):
        d.ellipse([12, 25, 28, 41], fill=YD)
        d.ellipse([13, 26, 27, 40], fill=Y)
        d.ellipse([15, 28, 25, 38], fill=YD)
    Se = ob(seal)
    sd = ImageDraw.Draw(Se)
    sd.ellipse([16, 31, 24, 35], fill=WHITE)
    sd.ellipse([19, 31, 21, 35], fill=B)
    sd.point((20, 33), fill=INK)
    sd.line([(16, 33), (14, 33)], fill=INK)
    sd.line([(20, 36), (19, 38)], fill=INK)
    sd.point((14, 27), fill=YL)
    ribbon = ob(lambda d: (poly(d, [(15, 40), (18, 40), (16, 47), (13, 45)], R),
                           poly(d, [(22, 40), (25, 40), (27, 45), (24, 47)], R)))
    return finish(pal, stack(L, ribbon, Se), RENTE_I, RP)


def magot_du_dragon(pal):
    """Un tas d'or sous une griffe de dragon et une écaille rouge."""
    def hoard(d):
        poly(d, [(5, 47), (12, 34), (19, 30), (27, 34), (33, 47)], Y)
        poly(d, [(19, 30), (27, 34), (33, 47), (24, 47)], YD)
    L = ob(hoard)
    d = ImageDraw.Draw(L)
    for x, y in ((11, 40), (17, 36), (23, 41), (15, 44), (27, 44)):
        d.ellipse([x, y, x + 3, y + 2], fill=YL)
    sparkle(d, 20, 33)
    sparkle(d, 9, 43)

    def claw(d):
        poly(d, [(14, 8), (26, 8), (28, 18), (12, 18)], R)
        for x in (12, 18, 24):
            poly(d, [(x, 18), (x + 4, 18), (x + 2, 27)], WHITE)
        d.line([(15, 10), (25, 10)], fill=RL)
    C_ = ob(claw)
    cd = ImageDraw.Draw(C_)
    for x in (16, 22):
        cd.arc([x - 2, 12, x + 2, 16], 0, 180, fill=RD)
    return finish(pal, stack(L, C_), LUTIN_I, LP)


# Rang de chaque carte, comme son "rank" dans assets/cards/definitions/archetypes.json (le Joker caméléon,
# carte joker, n'en a pas et garde son emblème dans les deux coins).
# Rang de chaque carte, comme son "rank" dans assets/cards/definitions/archetypes.json : chaque archétype part de
# l'As (1), puis Roi, Dame, Valet, 10, 9. Le Joker caméléon, carte joker, n'en a pas et garde son emblème
# dans les deux coins.
RANKS = {
    'couronne_du_fou': 1, 'joker_sauvage': 13, 'rire_du_joker': 12, 'diable_en_boite': 11, 'atelier_du_joker': 10,
    'magot_du_dragon': 1, 'bout_de_l_arc_en_ciel': 13, 'chapeau_du_lutin': 12, 'marteau_du_cordonnier': 11,
    'farce_du_lutin': 10, 'gigue_du_lutin': 9,
    'sceau_du_pharaon': 1, 'rente_viagere': 13, 'interets_composes': 12, 'recouvrement': 11, 'taux_variable': 10,
    'reconnaissance_de_dette': 9,
    'coup_de_bluff': 1, 'tour_de_passe_passe': 13, 'intimidation': 12, 'fausse_piste': 11, 'cartes_marquees': 10,
    'chamboule_tout': 1, 'martingale': 13, 'second_levier': 12, 'calibrage': 11, 'memoire_de_la_machine': 10,
    'rouleau_retenu': 9,
}

# Index de rang, 3 cases de large sur 5 de haut (le « 1 » du 10 en fait 1), posé à l'échelle des index des
# cartes classiques (chaque case en fait 2x2).
GLYPHS = {
    '1': ['k', 'k', 'k', 'k', 'k'],
    '0': ['kkk', 'k.k', 'k.k', 'k.k', 'kkk'],
    '9': ['kkk', 'k.k', 'kkk', '..k', 'kkk'],
    'A': ['.k.', 'k.k', 'kkk', 'k.k', 'k.k'],
    'J': ['..k', '..k', '..k', 'k.k', 'kkk'],
    'Q': ['kkk', 'k.k', 'k.k', 'kk.', '.kk'],
    'K': ['k.k', 'k.k', 'kk.', 'k.k', 'k.k'],
}
RANK_TEXT = {1: 'A', 11: 'J', 12: 'Q', 13: 'K'}
INDEX_X, INDEX_Y = X0 + 2, Y0 + 2   # coin haut gauche de l'index
INDEX_H = 10                        # hauteur de l'index (5 cases x 2)
INDEX_GAP = 2                       # entre l'index et l'emblème, et autour de la zone réservée

_current = {}                       # rang de la carte en cours de rendu (voir ranked)


def rank_rows(rank):
    """@return les lignes du sprite de l'index du rang, à l'échelle 2."""
    text = RANK_TEXT.get(rank, str(rank))
    rows = ['', '', '', '', '']
    for n, ch in enumerate(text):
        for j in range(5):
            rows[j] += ('.' if n else '') + GLYPHS[ch][j]
    return [''.join(c * 2 for c in row) for row in rows for _ in range(2)]


def _line_cost(a, i, axis):
    """Nombre de pixels qui changent si on retire la ligne (axis 0) ou colonne (axis 1) i de a."""
    return int((np.take(a, i, axis) != np.take(a, i - 1, axis)).any(axis=-1).sum())


def _cheapest(a, axis):
    n = a.shape[axis]
    if n < 8:
        return None
    # jamais les 2 premières ni les 2 dernières lignes (contours)
    return min(range(2, n - 2), key=lambda i: (_line_cost(a, i, axis), abs(i - n / 2)))


def _place(a, zw, zh):
    """@return la position (x, y) du dessin a la plus centrée où il reste dans la face sans toucher la zone de
    l'index (zw x zh cases dans le coin haut gauche), ou None."""
    h, w = a.shape[:2]
    m = a[:, :, 3] > 0
    best = None
    for y in range(Y0, Y1 + 2 - h):
        for x in range(X0, X1 + 2 - w):
            top = Y0 + zh - y      # lignes du dessin dans la hauteur de la zone
            left = X0 + zw - x     # colonnes du dessin dans la largeur de la zone
            if top > 0 and left > 0 and m[:top, :left].any():
                continue
            score = abs(x + w / 2 - (X0 + X1 + 1) / 2) + abs(y + h / 2 - (Y0 + Y1 + 1) / 2)
            if best is None or score < best[0]:
                best = (score, x, y)
    return best[1:] if best else None


def fit_art(L, zw, zh):
    """Place le calque L sous la zone de l'index : s'il ne tient pas, retire une à une la ligne (de préférence)
    ou la colonne qui change le moins le dessin, jusqu'à ce qu'il tienne. @return le calque placé."""
    L = L.copy()
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).rectangle([X0, Y0, X1, Y1], fill=255)
    from PIL import ImageChops
    L.putalpha(ImageChops.multiply(L.split()[3], m))
    a = np.array(L.crop(L.getbbox()))
    while True:
        pos = _place(a, zw, zh)
        if pos:
            break
        r, c = _cheapest(a, 0), _cheapest(a, 1)
        if r is not None and (c is None or _line_cost(a, r, 0) <= 3 * _line_cost(a, c, 1)):
            a = np.delete(a, r, 0)
        else:
            a = np.delete(a, c, 1)
    out = layer()
    out.alpha_composite(Image.fromarray(a), pos)
    return out


def finish(pal, L, idx, ipal, dx=0, dy=0):
    """Comme cards_w1.finish, mais pour une carte à rang : index du rang puis emblème dans le coin haut gauche,
    et le dessin à côté, sans que l'index en cache une partie."""
    rank = _current.get('rank')
    if rank is None:
        return cards_w1.finish(pal, L, idx, ipal, dx, dy)
    if dx or dy:
        L2 = layer()
        L2.alpha_composite(L, (max(dx, 0), max(dy, 0)), (max(-dx, 0), max(-dy, 0)))
        L = L2
    rows = rank_rows(rank)
    emblem_x = INDEX_X + len(rows[0]) + INDEX_GAP
    zw = emblem_x + len(idx[0]) + INDEX_GAP - X0
    zh = INDEX_Y + INDEX_H + INDEX_GAP - Y0
    im = card(*pal)
    clip(im, fit_art(L, zw, zh))
    d = ImageDraw.Draw(im)
    sprite(d, INDEX_X, INDEX_Y, rows, {'k': INK})
    sprite(d, emblem_x, INDEX_Y + (INDEX_H - len(idx)) // 2, idx, ipal)
    return im


def ranked(cid, fn):
    rank = RANKS.get(cid)
    if rank is None:
        return fn

    def draw(pal):
        _current['rank'] = rank
        try:
            return fn(pal)
        finally:
            _current.pop('rank', None)
    return draw


DESIGNS = {f.__name__: ranked(f.__name__, f) for f in (
    joker_sauvage, rire_du_joker, couronne_du_fou, atelier_du_joker, joker_cameleon,
    chapeau_du_lutin, gigue_du_lutin, bout_de_l_arc_en_ciel, marteau_du_cordonnier, farce_du_lutin,
    reconnaissance_de_dette, taux_variable, interets_composes, recouvrement, rente_viagere,
    intimidation, cartes_marquees, fausse_piste, tour_de_passe_passe, coup_de_bluff,
    rouleau_retenu, memoire_de_la_machine, calibrage, second_levier, martingale,
    diable_en_boite, chamboule_tout, sceau_du_pharaon, magot_du_dragon,
)}
