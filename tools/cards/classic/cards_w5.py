"""Cartes spéciales (lot 5) : nouvelles cartes de l'échoppe du combat (2026-11-06), au style des cartes
classiques (voir base.py et README.md). Aucun texte, aucun chiffre."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401

R, RD, RL = hexc('#d8333f'), hexc('#8e1420'), hexc('#ff8a8a')
G, GD, GL = hexc('#3fae4a'), hexc('#23793a'), hexc('#8ef05a')
P, PD, PL = hexc('#9a4ad8'), hexc('#5e2c8f'), hexc('#c890f0')
Y, YD, YL = hexc('#f2c14e'), hexc('#b0752a'), hexc('#fff1a8')
ST, STD, STL = hexc('#9aa6b4'), hexc('#5e6878'), hexc('#d4dce4')
WD, WDD, WDL = hexc('#a0643a'), hexc('#6a3e22'), hexc('#c88a58')
PAPER, PAPERD = hexc('#f4e2b8'), hexc('#c8a874')
GLOVE, GLOVED = hexc('#fafafa'), hexc('#c4c8d4')
B, BD = hexc('#3a6fd8'), hexc('#24459a')


def poly(d, pts, col):
    d.polygon([(round(x), round(y)) for x, y in pts], fill=col)


def finish(im, L, corner, cpal):
    clip(im, L)
    corners(im, corner, cpal)
    return im


def sparkle(d, x, y, col=YL):
    d.point([(x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)], fill=col)
    d.point((x, y), fill=WHITE)


def golden_reel(pal):
    """Une petite machine à trois fenêtres : celle du milieu est dorée et brille."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # caisse de la machine
    d.rectangle([7, 16, 31, 38], fill=R)
    d.rectangle([7, 16, 31, 18], fill=RL)
    d.rectangle([7, 36, 31, 38], fill=RD)
    # pied
    d.rectangle([11, 39, 27, 43], fill=RD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    # fenêtres : gauche et droite grises, milieu dorée
    for x0, gold in ((9, False), (16, True), (24, False)):
        d.rectangle([x0, 21, x0 + 5, 33], fill=INK)
        d.rectangle([x0 + 1, 22, x0 + 4, 32], fill=Y if gold else STL)
        if gold:
            d.rectangle([x0 + 1, 22, x0 + 4, 23], fill=YL)
            d.rectangle([x0 + 1, 31, x0 + 4, 32], fill=YD)
            # étoile dans la fenêtre dorée
            d.point([(x0 + 2, 26), (x0 + 3, 26), (x0 + 2, 27), (x0 + 3, 27)], fill=WHITE)
            d.point([(x0 + 2, 25), (x0 + 3, 28), (x0 + 1, 27), (x0 + 4, 26)], fill=YL)
        else:
            d.rectangle([x0 + 1, 26, x0 + 4, 28], fill=ST)
    # cadre doré autour de la fenêtre du milieu
    d.rectangle([15, 20, 22, 20], fill=YD)
    d.rectangle([15, 34, 22, 34], fill=YD)
    # levier
    d.line([(33, 22), (33, 30)], fill=STD)
    d.rectangle([32, 19, 33, 21], fill=R)
    # rayons de lumière au-dessus
    for (x, y) in [(19, 8), (19, 10), (19, 12), (13, 10), (25, 10), (15, 12), (23, 12)]:
        d.point((x, y), fill=Y)
    sparkle(d, 10, 10)
    sparkle(d, 28, 9)
    sparkle(d, 19, 6)
    return finish(im, L, ['.yyy.', 'y.y.y', 'yyyyy', 'y.y.y', '.yyy.'], {'y': YD})


def dealer_hand(pal):
    """Une main gantée de blanc qui tient un éventail de trois cartes, une étincelle au-dessus."""
    im = card(*pal)
    # éventail de cartes
    C = layer()
    cd = ImageDraw.Draw(C)
    poly(cd, [(7, 10), (15, 7), (19, 27), (12, 29)], WHITE)
    poly(cd, [(15, 5), (23, 5), (23, 27), (15, 27)], WHITE)
    poly(cd, [(23, 7), (31, 10), (26, 29), (19, 27)], WHITE)
    C = outline(C)
    cd = ImageDraw.Draw(C)
    cd.line([(15, 6), (15, 26)], fill=INK)
    cd.line([(23, 6), (23, 26)], fill=INK)
    # enseignes simples : cœur, pique, carreau
    cd.point([(10, 13), (12, 13), (10, 14), (11, 14), (12, 14), (11, 15)], fill=R)
    cd.point([(19, 9), (18, 10), (19, 10), (20, 10), (17, 11), (18, 11), (19, 11), (20, 11), (21, 11), (19, 12)],
             fill=INK)
    cd.point([(27, 13), (26, 14), (27, 14), (28, 14), (27, 15)], fill=R)
    # main gantée qui tient le bas de l'éventail
    Hd = layer()
    hd = ImageDraw.Draw(Hd)
    hd.ellipse([11, 25, 27, 38], fill=GLOVE)
    for x in (12, 16, 20, 24):  # doigts repliés devant les cartes
        hd.ellipse([x, 22, x + 4, 27], fill=GLOVE)
    hd.rectangle([14, 35, 24, 46], fill=GLOVE)  # poignet
    hd.rectangle([13, 43, 25, 46], fill=R)      # manchette rouge du croupier
    Hd = outline(Hd)
    hd = ImageDraw.Draw(Hd)
    for x in (16, 20, 24):
        hd.line([(x, 24), (x, 26)], fill=GLOVED)
    hd.line([(13, 36), (25, 36)], fill=GLOVED)
    hd.line([(13, 44), (25, 44)], fill=RL)
    hd.point([(26, 30), (26, 31), (26, 32)], fill=GLOVED)
    C.alpha_composite(Hd)
    d = ImageDraw.Draw(C)
    sparkle(d, 31, 40)
    sparkle(d, 8, 37)
    return finish(im, C, ['.kkk.', 'k...k', 'k...k', 'k...k', '.kkk.'], {'k': INK})


def bounty(pal):
    """Une affiche de prime clouée : une cible rouge dessus, une bourse de pièces au pied."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # affiche
    d.rectangle([11, 8, 30, 35], fill=PAPER)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(12, 34), (29, 34)], fill=PAPERD)
    d.line([(29, 8), (29, 34)], fill=PAPERD)
    # clous
    d.point([(12, 9), (29, 9)], fill=STD)
    # cible
    d.ellipse([13, 12, 29, 28], fill=R)
    d.ellipse([15, 14, 27, 26], fill=WHITE)
    d.ellipse([17, 16, 25, 24], fill=R)
    d.ellipse([19, 18, 23, 22], fill=WHITE)
    d.point([(21, 20)], fill=RD)
    # bandes sous la cible (sans texte : de simples traits)
    d.line([(14, 31), (28, 31)], fill=PAPERD)
    # bourse de pièces
    Bg = layer()
    bd = ImageDraw.Draw(Bg)
    bd.ellipse([12, 37, 26, 48], fill=WD)
    bd.rectangle([16, 35, 22, 38], fill=WD)
    bd.ellipse([24, 42, 30, 47], fill=Y)
    bd.ellipse([7, 43, 13, 48], fill=Y)
    Bg = outline(Bg)
    bd = ImageDraw.Draw(Bg)
    bd.line([(15, 38), (23, 38)], fill=Y)
    bd.point([(14, 41), (15, 40)], fill=WDL)
    bd.line([(13, 46), (25, 46)], fill=WDD)
    bd.point([(25, 43), (8, 44)], fill=YL)
    bd.point([(29, 46), (12, 47)], fill=YD)
    L.alpha_composite(Bg)
    return finish(im, L, ['.rrr.', 'r...r', 'r.r.r', 'r...r', '.rrr.'], {'r': R})


def lucky_strike(pal):
    """Un bonnet de Joker à trois grelots, entouré d'un trèfle et d'étincelles."""
    im = card(*pal)
    L = layer()
    d = ImageDraw.Draw(L)
    # bandeau
    d.rectangle([10, 30, 28, 34], fill=Y)
    # trois pointes du bonnet
    poly(d, [(10, 30), (16, 30), (7, 16)], P)
    poly(d, [(15, 30), (23, 30), (19, 11)], G)
    poly(d, [(22, 30), (28, 30), (31, 16)], P)
    # grelots
    for (x, y) in [(6, 14), (19, 9), (32, 14)]:
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=Y)
    # trèfle à quatre feuilles en dessous
    for (x, y) in [(17, 38), (21, 38), (17, 42), (21, 42)]:
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=G)
    d.line([(19, 44), (21, 48)], fill=GD)
    L = outline(L)
    d = ImageDraw.Draw(L)
    d.line([(11, 31), (27, 31)], fill=YL)
    d.line([(11, 34), (27, 34)], fill=YD)
    for x in (13, 19, 25):
        d.point((x, 32), fill=R)
    d.line([(9, 18), (13, 27)], fill=PL)
    d.line([(19, 14), (18, 27)], fill=GL)
    d.line([(29, 18), (26, 27)], fill=PD)
    for (x, y) in [(6, 14), (19, 9), (32, 14)]:
        d.point((x - 1, y - 1), fill=YL)
        d.point((x, y + 1), fill=YD)
    d.point([(19, 40)], fill=GD)
    d.point([(16, 37), (20, 37)], fill=GL)
    sparkle(d, 8, 40)
    sparkle(d, 30, 39)
    sparkle(d, 27, 7)
    return finish(im, L, ['.g.g.', 'ggggg', '.ggg.', 'ggggg', '.g.g.'], {'g': GD})


DESIGNS = {
    'golden_reel': golden_reel, 'dealer_hand': dealer_hand, 'bounty': bounty, 'lucky_strike': lucky_strike,
}
