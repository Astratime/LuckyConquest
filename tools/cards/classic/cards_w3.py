"""Cartes spéciales (lot 3) au style des cartes classiques : Mines d'Or et cartes de main.
Voir CONSIGNES.md : pas de texte, contours INK, sujet centré, tout passe par clip()."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401
from base import ImageDraw, Image  # noqa: E402

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD = hexc('#3fae4a'), hexc('#23793a')
P, PD = hexc('#9a4ad8'), hexc('#5e2c8f')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
O, OD = hexc('#f08a2a'), hexc('#b8541a')
B, BD, BL = hexc('#3a6fd8'), hexc('#24459a'), hexc('#8ec0ff')
WD, WM, WL = hexc('#6b3e1e'), hexc('#9a5e2c'), hexc('#c8884a')   # bois
ST, STD, STL = hexc('#8a8a96'), hexc('#5a5a66'), hexc('#b8b8c4')  # pierre / fer
IR, IRD, IRL = hexc('#9aa4b4'), hexc('#5e6878'), hexc('#dfe6f0')  # acier
C, CD = hexc('#fbf6ea'), hexc('#d8ccb4')                         # carton des cartes
RP, RPD, RPL = hexc('#d8a860'), hexc('#9a6c34'), hexc('#f0d098')  # corde
WH = WHITE


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def start(pal):
    im = card(*pal)
    L = layer()
    return im, L, ImageDraw.Draw(L)


def finish(im, L, rows, cpal):
    clip(im, L)
    corners(im, rows, cpal)
    return im


def nugget(d, x, y):
    d.rectangle([x, y, x + 2, y + 1], fill=Y)
    d.point((x + 1, y - 1), fill=Y)
    d.point((x, y), fill=YL)
    d.point((x + 2, y + 1), fill=YD)


def spark(d, x, y, col=YL, r=1):
    d.line([(x - r, y), (x + r, y)], fill=col)
    d.line([(x, y - r), (x, y + r)], fill=col)


def coin(d, x, y, r=2):
    d.ellipse([x - r, y - r, x + r, y + r], fill=Y)
    d.point((x - 1, y - 1), fill=YL)
    d.point((x + r - 1, y + r - 1), fill=YD)


# --- petits motifs d'angle (5x5) ---
NUG = ['.yy..', 'yllyy', 'yyyyd', '.yydd']
NUG_P = {'y': Y, 'l': YL, 'd': YD}
GEM = ['.yyy.', 'ylyyy', 'yyyyd', '.yyd.', '..d..']
SPADE_CARD = ['wwww', 'wkkw', 'wkkw', 'wwww']
CARD_P = {'w': C, 'k': B}
FLAME = ['..o..', '.oyo.', '.oyo.', 'oyyyo', '.ooo.']
FLAME_P = {'o': O, 'y': Y}
DROP = ['..b..', '.bbb.', 'bblbb', 'bbbbb', '.bbb.']
DROP_P = {'b': B, 'l': BL}
STONE = ['.sss.', 'slsss', 'sssss', '.ssd.']
STONE_P = {'s': ST, 'l': STL, 'd': STD}
HEART = ['.r.r.', 'rrrrr', 'rrrrr', '.rrr.', '..r..']
GHEART = ['.y.y.', 'ylyyy', 'yyyyd', '.yyd.', '..d..']
DIE = ['kkkkk', 'kw.wk', 'kw.wk', 'kkkkk']
CARDI = ['kkkk.', 'kwwk.', 'kwwkk', 'kkwwk', '.kkkk']
BOLT = ['..yy', '.yy.', 'yyyy', '.yy.', 'yy..']


def C_(rows, extra):
    return rows, extra


# ================================================================ Mines : filon
def pioche_du_mineur(pal):
    """Pioche du mineur : la pioche en diagonale et deux pépites d'or."""
    im, L, d = start(pal)
    # manche
    d.line([(12, 41), (24, 15)], fill=WM, width=3)
    d.line([(11, 40), (23, 14)], fill=WL)
    # fer de la pioche (croissant)
    head = [(9, 15), (14, 12), (20, 11), (25, 12), (28, 15), (29, 19), (26, 16), (22, 15), (17, 15), (12, 17)]
    poly(d, head, IR)
    d.rectangle([21, 12, 26, 17], fill=IRD)
    d.line([(10, 15), (15, 13), (20, 12), (25, 13)], fill=IRL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    nugget(d, 20, 38)
    nugget(d, 25, 35)
    nugget(d, 23, 41)
    L = outline(L)
    d = ImageDraw.Draw(L)
    spark(d, 29, 29)
    spark(d, 9, 26)
    return finish(im, L, NUG, NUG_P)


def tamis(pal):
    """Tamis : un tamis rond, grille et pépites d'or, gouttes d'eau dessous."""
    im, L, d = start(pal)
    d.ellipse([8, 16, 30, 34], fill=WM)
    d.ellipse([10, 18, 28, 31], fill=hexc('#4a2c16'))
    # grille
    for x in range(11, 28, 3):
        d.line([(x, 18), (x, 31)], fill=WL)
    for y in range(20, 31, 3):
        d.line([(10, y), (28, y)], fill=WL)
    import numpy as np
    arr = np.array(L)
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).ellipse([10, 18, 28, 31], fill=255)
    rim = Image.new('L', (W, H), 0)
    ImageDraw.Draw(rim).ellipse([8, 16, 30, 34], fill=255)
    inside = np.array(m) > 0
    ring = (np.array(rim) > 0) & ~inside
    arr[ring] = WM
    L = Image.fromarray(arr)
    d = ImageDraw.Draw(L)
    d.arc([8, 16, 30, 34], 200, 330, fill=WL)
    d.arc([8, 16, 30, 34], 20, 160, fill=WD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for (x, y) in [(13, 22), (17, 25), (21, 21), (23, 26), (14, 27)]:
        nugget(d, x, y)
    # gouttes
    for (x, y) in [(12, 37), (16, 39), (21, 38), (25, 37)]:
        d.line([(x, y), (x, y + 2)], fill=B)
        d.point((x, y), fill=BL)
    spark(d, 28, 13)
    return finish(im, L, NUG, NUG_P)


def veine_d_or(pal):
    """Veine d'or : un gros bloc de roche traversé d'un filon d'or qui brille."""
    im, L, d = start(pal)
    rock = [(9, 22), (12, 15), (19, 12), (27, 14), (30, 20), (30, 31), (27, 38), (18, 40), (10, 37), (8, 30)]
    poly(d, rock, ST)
    poly(d, [(27, 14), (30, 20), (30, 31), (27, 38), (23, 39), (28, 30), (28, 20)], STD)
    poly(d, [(12, 15), (19, 12), (22, 13), (14, 18), (9, 23), (8, 22)], STL)
    # filon
    vein = [(9, 31), (12, 27), (15, 29), (19, 23), (23, 25), (27, 19), (29, 20)]
    d.line(vein, fill=Y, width=3)
    d.line([(x, y - 1) for x, y in vein], fill=YL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for (x, y) in [(14, 33), (24, 31)]:
        d.point([(x, y), (x + 1, y)], fill=STD)
    spark(d, 19, 21, WH)
    spark(d, 28, 16, WH)
    spark(d, 11, 26, YL)
    spark(d, 30, 10)
    return finish(im, L, GEM, NUG_P | {'d': YD, 'l': YL})


# ================================================================ Mines : puits
def casque(pal):
    """Casque : casque de mineur doré, lampe frontale allumée."""
    im, L, d = start(pal)
    # faisceau
    beam = layer()
    bd = ImageDraw.Draw(beam)
    poly(bd, [(16, 23), (22, 23), (29, 8), (12, 8)], YL[:3] + (150,))
    im = clip(im, beam)
    d.chord([9, 18, 29, 42], 180, 360, fill=Y)
    d.rectangle([8, 30, 30, 33], fill=Y)
    d.rectangle([8, 32, 30, 33], fill=YD)
    d.arc([11, 20, 27, 40], 200, 250, fill=YL)
    d.line([(19, 28), (19, 29)], fill=YD)
    # lampe
    d.rectangle([15, 21, 23, 27], fill=IRD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.rectangle([16, 22, 22, 26], fill=WH)
    d.rectangle([17, 23, 21, 25], fill=YL)
    d.point((17, 23), fill=WH)
    # rochers qui tombent
    for (x, y) in [(10, 11), (26, 13), (19, 39)]:
        pass
    return finish(im, L, ['.yyy.', 'yyyyy', 'ywwyy', 'ddddd'], {'y': Y, 'w': WH, 'd': YD})


def etai(pal):
    """Étai : un cadre de poutres qui retient les rochers, un bouclier bleu dessous."""
    im, L, d = start(pal)
    # rochers sur la poutre
    for (x, y, r) in [(13, 16, 3), (19, 15, 4), (25, 16, 3)]:
        d.ellipse([x - r, y - r + 1, x + r, y + r], fill=ST)
    d.rectangle([8, 18, 30, 21], fill=WM)
    d.line([(8, 18), (30, 18)], fill=WL)
    d.rectangle([10, 22, 13, 41], fill=WM)
    d.rectangle([25, 22, 28, 41], fill=WM)
    d.line([(10, 22), (10, 41)], fill=WL)
    d.line([(25, 22), (25, 41)], fill=WL)
    # jambes de force
    d.line([(13, 25), (16, 22)], fill=WD, width=2)
    d.line([(25, 25), (22, 22)], fill=WD, width=2)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for (x, y) in [(12, 15), (18, 13), (24, 15)]:
        d.point([(x, y), (x + 1, y)], fill=STL)
    d.point([(11, 19), (27, 19)], fill=IRD)
    sh = layer()
    sd = ImageDraw.Draw(sh)
    poly(sd, [(15, 27), (23, 27), (23, 34), (19, 39), (15, 34)], B)
    poly(sd, [(19, 27), (23, 27), (23, 34), (19, 39)], BD)
    sd.line([(16, 28), (16, 33)], fill=BL)
    sh = outline(sh)
    L.alpha_composite(sh)
    return finish(im, L, ['bbbbb', 'blbdb', 'bbbdb', '.bbd.', '..d..'], {'b': B, 'l': BL, 'd': BD})


def corde_de_rappel(pal):
    """Corde de rappel : rouleau de corde surmonté d'un grappin."""
    im, L, d = start(pal)
    for y in range(40, 29, -3):
        d.ellipse([9, y - 4, 29, y + 4], fill=RP)
        d.arc([9, y - 4, 29, y + 4], 0, 180, fill=RPD)
        d.arc([9, y - 4, 29, y + 4], 190, 350, fill=RPL)
    d.ellipse([14, 28, 24, 32], fill=RPD)
    # corde qui monte vers le grappin
    d.line([(19, 30), (19, 19)], fill=RP, width=2)
    d.line([(19, 29), (19, 19)], fill=RPL)
    # grappin
    d.rectangle([18, 13, 21, 19], fill=IR)
    d.line([(18, 15), (14, 13), (13, 10)], fill=IR, width=2)
    d.line([(21, 15), (25, 13), (26, 10)], fill=IR, width=2)
    d.rectangle([18, 18, 21, 19], fill=IRD)
    d.point((19, 13), fill=IRL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for y in range(33, 43, 3):
        d.point([(12, y), (26, y)], fill=RPD)
    return finish(im, L, ['.rrr.', 'r...r', 'r.r.r', 'r...r', '.rrr.'], {'r': RPD})


# ================================================================ Mines : forge
def marteau_de_forge(pal):
    """Marteau de forge : le marteau frappe l'enclume, gerbe d'étincelles."""
    im, L, d = start(pal)
    # enclume (pointe à gauche)
    d.rectangle([12, 29, 30, 32], fill=STD)
    poly(d, [(12, 29), (8, 29), (12, 32)], STD)
    d.rectangle([16, 33, 26, 36], fill=STD)
    d.rectangle([13, 37, 29, 40], fill=STD)
    d.line([(8, 29), (30, 29)], fill=ST)
    d.line([(13, 37), (29, 37)], fill=ST)
    # marteau, tête en haut à droite
    d.line([(12, 26), (22, 15)], fill=WM, width=3)
    d.line([(12, 25), (22, 14)], fill=WL)
    poly(d, [(17, 13), (23, 8), (30, 14), (24, 19)], STD)
    poly(d, [(17, 13), (23, 8), (24, 9), (18, 14)], STL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # métal chaud et étincelles
    d.rectangle([17, 27, 25, 28], fill=O)
    d.line([(18, 27), (24, 27)], fill=Y)
    for (x, y, c) in [(15, 21, Y), (27, 22), (9, 24, O), (29, 25, O), (20, 22, YL)] if False else \
            [(15, 21, Y), (27, 22, YL), (9, 24, O), (30, 25, O), (21, 22, YL)]:
        spark(d, x, y, c)
    return finish(im, L, ['kkkkk', '.kkk.', '..k..', '.kkk.'], {'k': STD})


def trempe(pal):
    """Trempe : la lame rougeoyante plonge dans un baquet d'eau, vapeur au-dessus."""
    im, L, d = start(pal)
    # baquet
    d.rectangle([9, 30, 29, 41], fill=WM)
    d.ellipse([9, 27, 29, 33], fill=WM)
    d.ellipse([10, 28, 28, 32], fill=B)
    d.line([(9, 34), (29, 34)], fill=STD)
    d.line([(9, 39), (29, 39)], fill=STD)
    for x in (14, 19, 24):
        d.line([(x, 33), (x, 41)], fill=WD)
    d.line([(10, 33), (10, 41)], fill=WL)
    # lame chaude
    poly(d, [(17, 30), (21, 30), (21, 17), (19, 14), (17, 17)], O)
    d.line([(18, 17), (18, 29)], fill=Y)
    d.point((19, 15), fill=YL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.ellipse([12, 29, 26, 31], outline=BL)
    d.line([(17, 30), (21, 30)], fill=B)
    # vapeur
    st = layer()
    sd = ImageDraw.Draw(st)
    for (x, y, r) in [(13, 19, 3), (14, 14, 2), (25, 17, 3), (26, 12, 2), (23, 22, 2), (14, 24, 2)]:
        sd.ellipse([x - r, y - r, x + r, y + r], fill=WH)
    st = outline(st, IR)
    L.alpha_composite(st)
    return finish(im, L, DROP, DROP_P)


def lame_forgee(pal):
    """Lame forgée : une épée neuve, pointe en haut, qui brille encore."""
    im, L, d = start(pal)
    poly(d, [(17, 33), (21, 33), (21, 13), (19, 9), (17, 13)], IR)
    poly(d, [(19, 33), (21, 33), (21, 13), (19, 9)], IRD)
    d.line([(18, 13), (18, 32)], fill=IRL)
    d.rectangle([11, 33, 27, 35], fill=Y)
    d.rectangle([11, 35, 27, 35], fill=YD)
    d.rectangle([18, 36, 20, 41], fill=WM)
    d.ellipse([17, 41, 21, 45], fill=Y)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(19, 37), (19, 39)], fill=WD)
    d.point((18, 42), fill=YL)
    for (x, y, c, r) in [(13, 15, YL, 2), (26, 22, O, 1), (11, 26, O, 1), (25, 11, YL, 1)]:
        spark(d, x, y, c, r)
    spark(d, 19, 7, WH, 1)
    return finish(im, L, ['...i', '..i.', 'yi..', '.y..', 'w.y.'], {'i': IRD, 'y': YD, 'w': WM})


# ================================================================ Mines : gouffre
def dynamite(pal):
    """Dynamite : trois bâtons rouges ficelés, mèche allumée."""
    im, L, d = start(pal)
    for x in (11, 17, 23):
        d.rectangle([x, 21, x + 5, 40], fill=R)
        d.line([(x + 1, 21), (x + 1, 40)], fill=RL)
        d.line([(x + 5, 21), (x + 5, 40)], fill=RD)
        d.ellipse([x, 19, x + 5, 23], fill=RD)
    d.rectangle([10, 25, 29, 27], fill=WD)
    d.rectangle([10, 34, 29, 36], fill=WD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    for x in (11, 17, 23):
        d.line([(x + 1, 21), (x + 4, 21)], fill=RD)
    d.line([(19, 20), (19, 16), (21, 13), (23, 12), (24, 11)], fill=STD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.rectangle([24, 9, 26, 11], fill=O)
    d.point((25, 10), fill=YL)
    for (x, y) in [(28, 8), (22, 7), (29, 13)]:
        spark(d, x, y, Y)
    return finish(im, L, ['...y.', '..k..', 'rrr..', 'rrr..', 'rrr..'], {'y': O, 'k': STD, 'r': R})


def lampe_a_carbure(pal):
    """Lampe à carbure : lampe de laiton à réflecteur rond, flamme bleue au centre."""
    im, L, d = start(pal)
    d.rectangle([14, 26, 24, 39], fill=Y)
    d.rectangle([12, 39, 26, 42], fill=YD)
    d.rectangle([14, 30, 24, 31], fill=YD)
    d.line([(15, 26), (15, 38)], fill=YL)
    # crochet
    d.arc([21, 21, 30, 38], 270, 90, fill=YD)
    d.arc([20, 21, 29, 38], 270, 90, fill=YD)
    # réflecteur
    d.ellipse([10, 10, 28, 27], fill=Y)
    d.ellipse([12, 12, 26, 25], fill=IRL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.ellipse([15, 15, 23, 23], fill=WH)
    poly(d, [(19, 14), (22, 20), (19, 22), (16, 20)], BL)
    poly(d, [(19, 17), (20, 20), (18, 20)], B)
    d.point([(16, 35), (22, 35)], fill=YD)
    for (x, y) in [(8, 15), (30, 14), (8, 27)]:
        spark(d, x, y, YL if pal[0] != FACE else Y)
    return finish(im, L, ['..b..', '.bbb.', '.bwb.', 'yyyyy', '.yyy.'], {'b': B, 'w': BL, 'y': YD})


def coeur_d_or(pal):
    """Cœur d'or : un grand cœur d'or massif, taillé à facettes."""
    im, L, d = start(pal)
    d.ellipse([8, 14, 20, 26], fill=Y)
    d.ellipse([18, 14, 30, 26], fill=Y)
    poly(d, [(8, 21), (30, 21), (19, 39)], Y)
    poly(d, [(19, 18), (30, 20), (29, 24), (19, 39)], YD)
    poly(d, [(19, 18), (19, 39), (13, 31)], hexc('#e0a83c'))
    d.ellipse([10, 16, 14, 20], fill=YL)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.point([(10, 17), (11, 16)], fill=WH)
    spark(d, 12, 19, WH, 1)
    for (x, y, r) in [(29, 11, 2), (8, 34, 1), (30, 35, 1)]:
        spark(d, x, y, YD if pal[0] == FACE else YL, r)
    return finish(im, L, GHEART, {'y': Y, 'l': YL, 'd': YD})


# ================================================================ main
SUITS = {
    'h': (['.r.r.', 'rrrrr', 'rrrrr', '.rrr.', '..r..'], {'r': R}),
    's': (['..k..', '.kkk.', 'kkkkk', '..k..', '.kkk.'], {'k': INK}),
}


def rot_poly(cx, cy, w, h, rot):
    a = math.radians(rot)
    ca, sa = math.cos(a), math.sin(a)
    pts = []
    for (x, y) in [(-w / 2, -h / 2), (w / 2, -h / 2), (w / 2, h / 2), (-w / 2, h / 2)]:
        pts.append((cx + x * ca - y * sa, cy + x * sa + y * ca))
    return pts


def one_card(L, cx, cy, rot, suit, w=10, h=14):
    """Une petite carte 10x14 (face crème, un pique ou un cœur au centre), tournée de `rot` degrés."""
    sc = layer()
    sd = ImageDraw.Draw(sc)
    poly(sd, rot_poly(cx, cy, w, h, rot), C)
    rows, sp = SUITS[suit]
    sprite(sd, cx - 2, cy - 2, rows, sp)
    sc = outline(sc)
    L.alpha_composite(sc)


def deck(d, top):
    """La pioche : une pile de cartes au dos violet."""
    d.rectangle([12, top + 3, 26, top + 15], fill=PD)
    d.rectangle([12, top, 26, top + 12], fill=P)
    for x in range(14, 26, 3):
        for y in range(top + 2, top + 11, 3):
            d.point((x, y), fill=hexc('#c49af0'))
    d.line([(12, top + 14), (26, top + 14)], fill=P)


def fan(L, specs):
    for (rot, cx, cy, suit, *wh) in specs:
        one_card(L, cx, cy, rot, suit, *wh)


def draw_card_pic(n):
    def f(pal):
        im, L, d = start(pal)
        deck(d, 28)
        L = outline(L)
        if n == 2:
            fan(L, [(-14, 15, 18, 'h'), (14, 23, 18, 's')])
            cpal = ['rr.kk', 'rr.kk', 'rr.kk', 'rr.kk', 'rr.kk']
        else:
            fan(L, [(-16, 12, 21, 'h', 8, 12), (16, 26, 21, 'h', 8, 12), (0, 19, 17, 's', 8, 12)])
            cpal = ['r.k.r', 'r.k.r', 'r.k.r', 'r.k.r', 'r.k.r']
        return finish(im, L, cpal, {'k': INK, 'r': R})
    return f


draw_2 = draw_card_pic(2)
draw_3 = draw_card_pic(3)


def in_the_sleeve(pal):
    """Dans la manche : une manche de costume, une carte cachée glisse de sous la manchette."""
    im, L, d = start(pal)
    sleeve, sleeveL, sleeveD = hexc('#3a2a5e'), hexc('#5a4a8e'), hexc('#241a3e')
    # manche en diagonale (bas gauche -> haut droite)
    poly(d, [(7, 41), (7, 33), (19, 20), (26, 27), (13, 43)], sleeve)
    poly(d, [(7, 34), (19, 21), (20, 22), (7, 36)], sleeveL)
    L = outline(L)
    # carte qui sort de la manchette
    fan(L, [(-32, 23, 34, 'h')])
    top = layer()
    td = ImageDraw.Draw(top)
    # manchette blanche
    poly(td, [(19, 19), (23, 15), (30, 22), (26, 26)], WH)
    poly(td, [(26, 26), (30, 22), (29, 21), (25, 25)], IRL)
    td.point((26, 22), fill=Y)
    # main
    td.ellipse([24, 11, 31, 18], fill=hexc('#f6dcbc'))
    td.point([(29, 16), (30, 15)], fill=hexc('#d8aa84'))
    top = outline(top)
    L.alpha_composite(top)
    d = ImageDraw.Draw(L)
    for (x, y) in [(14, 13), (8, 22), (29, 41)]:
        spark(d, x, y, Y)
    return finish(im, L, ['.kk..', 'krrk.', 'krrrk', '.krrk', '..kk.'], {'k': INK, 'r': R})


def recycle(pal):
    """Recyclage : une bande de rouleau entourée de deux flèches qui tournent."""
    im, L, d = start(pal)
    d.rectangle([14, 15, 24, 38], fill=C)
    d.rectangle([14, 15, 24, 17], fill=CD)
    d.rectangle([14, 36, 24, 38], fill=CD)
    d.line([(23, 18), (23, 35)], fill=CD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # cerise
    d.line([(18, 20), (20, 18), (21, 19)], fill=GD)
    d.rectangle([16, 21, 18, 23], fill=R)
    d.rectangle([20, 21, 22, 23], fill=R)
    d.point([(16, 21), (20, 21)], fill=RL)
    # cloche
    poly(d, [(17, 33), (19, 28), (21, 33)], Y)
    d.rectangle([16, 33, 22, 33], fill=Y)
    d.point((19, 34), fill=YD)
    d.point((19, 27), fill=YD)
    d.line([(15, 26), (23, 26)], fill=CD)
    # flèches
    arr = layer()
    ad = ImageDraw.Draw(arr)
    ad.arc([8, 10, 30, 30], 200, 335, fill=Y, width=2)
    poly(ad, [(27, 13), (30, 19), (24, 19)], Y)
    ad.arc([8, 23, 30, 43], 20, 155, fill=Y, width=2)
    poly(ad, [(11, 40), (8, 34), (14, 34)], Y)
    arr = outline(arr)
    L.alpha_composite(arr)
    return finish(im, L, ['.yyy.', 'y...y', 'y.yyy', 'y..y.', '.....'], {'y': YD})


def die(d, x, y, n):
    d.rectangle([x, y, x + 8, y + 8], fill=C)
    d.line([(x + 8, y + 1), (x + 8, y + 8)], fill=CD)
    d.line([(x + 1, y + 8), (x + 8, y + 8)], fill=CD)
    pts = {1: [(4, 4)], 2: [(2, 2), (6, 6)], 3: [(2, 2), (4, 4), (6, 6)],
           4: [(2, 2), (6, 2), (2, 6), (6, 6)], 5: [(2, 2), (6, 2), (4, 4), (2, 6), (6, 6)],
           6: [(2, 2), (6, 2), (2, 4), (6, 4), (2, 6), (6, 6)]}[n]
    for (px, py) in pts:
        d.point((x + px, y + py), fill=R if n in (1, 4) else INK)


PIPS = {4: [(-2, -2), (2, -2), (-2, 2), (2, 2)], 5: [(-2, -2), (2, -2), (0, 0), (-2, 2), (2, 2)],
        3: [(-2, -2), (0, 0), (2, 2)]}


def tilted_die(L, cx, cy, rot, n):
    sc = layer()
    sd = ImageDraw.Draw(sc)
    poly(sd, rot_poly(cx, cy, 9, 9, rot), C)
    a = math.radians(rot)
    for (x, y) in PIPS[n]:
        px, py = cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a)
        sd.point((round(px), round(py)), fill=R if n == 4 else INK)
    sc = outline(sc)
    L.alpha_composite(sc)


def reroll(pal):
    """Relance : deux dés qui roulent, une flèche courbe de relance."""
    im, L, d = start(pal)
    arr = layer()
    ad = ImageDraw.Draw(arr)
    ad.arc([8, 11, 30, 41], 120, 325, fill=Y, width=2)
    poly(ad, [(24, 9), (30, 14), (24, 19)], Y)
    arr = outline(arr)
    L.alpha_composite(arr)
    tilted_die(L, 15, 21, 22, 4)
    die_l = layer()
    die(ImageDraw.Draw(die_l), 18, 29, 5)
    L.alpha_composite(outline(die_l))
    return finish(im, L, ['kkkkk', 'kr.rk', 'k...k', 'kr.rk', 'kkkkk'], {'k': INK, 'r': R})


def magnet(pal):
    """Aimant : un aimant en fer à cheval rouge qui attire des pièces d'or."""
    im, L, d = start(pal)
    d.ellipse([8, 8, 30, 30], fill=R)
    d.rectangle([8, 19, 30, 29], fill=R)
    d.ellipse([14, 14, 24, 24], fill=(0, 0, 0, 0))
    d.rectangle([14, 19, 24, 32], fill=(0, 0, 0, 0))
    d.rectangle([8, 29, 14, 32], fill=IRL)
    d.rectangle([24, 29, 30, 32], fill=IRL)
    d.arc([9, 9, 29, 29], 190, 260, fill=RL)
    d.line([(9, 19), (9, 28)], fill=RL)
    d.line([(29, 19), (29, 28)], fill=RD)
    L = outline(L)
    sub = layer()
    sd = ImageDraw.Draw(sub)
    for (x, y) in [(11, 37), (19, 36), (27, 37), (15, 41), (23, 41)]:
        coin(sd, x, y)
    sub = outline(sub)
    L.alpha_composite(sub)
    d = ImageDraw.Draw(L)
    return finish(im, L, ['rr.rr', 'rr.rr', 'rr.rr', '.rrr.'], {'r': R})


def rainbow(pal):
    """Arc-en-ciel : un arc-en-ciel entre deux nuages, la marmite d'or au pied."""
    import numpy as np
    im, L, d = start(pal)
    cols = [R, O, Y, G, B, P]
    cx, cy = 19, 32
    yy, xx = np.mgrid[0:H, 0:W]
    arr = np.zeros((H, W, 4), np.uint8)
    for k, c in enumerate(cols):
        rx, ry = 12.5 - 1.4 * k, 20.5 - 2.0 * k
        m = ((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2 <= 1
        arr[m] = c
    rx, ry = 12.5 - 1.4 * 6, 20.5 - 2.0 * 6
    arr[((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2 <= 1] = 0
    arr[yy > cy] = 0
    L = outline(Image.fromarray(arr))
    # nuages au pied de l'arc
    cl = layer()
    cd = ImageDraw.Draw(cl)
    for x in (12, 26):
        cd.ellipse([x - 5, 29, x + 1, 35], fill=WH)
        cd.ellipse([x - 1, 29, x + 4, 35], fill=WH)
        cd.ellipse([x - 3, 26, x + 2, 32], fill=WH)
    cl = outline(cl, IR)
    L.alpha_composite(cl)
    # marmite d'or
    pot = layer()
    pd = ImageDraw.Draw(pot)
    pd.ellipse([13, 34, 25, 44], fill=hexc('#2a2a36'))
    pd.rectangle([12, 34, 26, 36], fill=hexc('#44445a'))
    pd.ellipse([14, 30, 24, 35], fill=Y)
    pot = outline(pot)
    pd = ImageDraw.Draw(pot)
    pd.point([(16, 31), (19, 30), (22, 32)], fill=YL)
    pd.point([(17, 33), (21, 33)], fill=YD)
    pd.point([(15, 38), (16, 37)], fill=hexc('#5a5a70'))
    L.alpha_composite(pot)
    return finish(im, L, ['rrrrr', 'oyyyo', 'yg.gy', 'gb.bg'], {'r': R, 'o': O, 'y': Y, 'g': G, 'b': B})


DESIGNS = {
    'pioche_du_mineur': pioche_du_mineur, 'tamis': tamis, 'veine_d_or': veine_d_or,
    'casque': casque, 'etai': etai, 'corde_de_rappel': corde_de_rappel,
    'marteau_de_forge': marteau_de_forge, 'trempe': trempe, 'lame_forgee': lame_forgee,
    'dynamite': dynamite, 'lampe_a_carbure': lampe_a_carbure, 'coeur_d_or': coeur_d_or,
    'draw_2': draw_2, 'draw_3': draw_3, 'in_the_sleeve': in_the_sleeve, 'recycle': recycle,
    'reroll': reroll, 'magnet': magnet, 'rainbow': rainbow,
}
