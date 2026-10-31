"""
Dessins de la vue de face du combat (Options > Visuel > « Vue de la table : de face »).

Produit, en pixel art à la taille 1 (le jeu les agrandit sans lissage) :
  assets/front/decor_cale.png         le décor de la Cale (Port des Contrebandiers)
  assets/front/rat_cales/*.png        le Rat des cales en pièces détachées, animées par le jeu
                                      (contour épais de 2 pixels, comme le reste du jeu)
                                      (corps, tête, paupière, queue « swing », patte) et leurs silhouettes
                                      blanches (*_flash.png) pour le flash quand il est touché.
  assets/front/decor_prairie_*.png    le décor de la Prairie, un par donjon (château et bannières à sa couleur)
  assets/front/prairie/<soldat|roi>_<couleur>/*.png
                                      les soldats et les rois de la Prairie en grand, en pièces
                                      détachées comme le rat (swing : plumet du soldat, arme du roi ;
                                      paw : main gantée posée sur le rebord), même contour épais.
  assets/front/props/*.png            les faces de l'étal de l'échoppe en relief (comptoir, dessus
                                      garni, marchandises, auvent rayé et son lambrequin), et la
                                      pancarte des decks et défausses.
  assets/front/machine/*.png          la machine à sous qui sort de la table (joueur et ennemi) :
                                      caisse et fronton étirables, ampoules, bac, sommet arrondi,
                                      dessus et flancs (la machine est en volume).

Toutes les pièces du rat partagent le même cadre (CANVAS) : le jeu les superpose
au même point, puis décale la tête (respiration), fait osciller la queue autour
de TAIL_PIVOT et ferme l'oeil (paupière) de temps en temps.

Lancer depuis la racine du dépôt : python3 tools/front/generate_front_art.py
"""
import math
import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'assets', 'front')
OUTLINE = (34, 20, 26, 255)
CANVAS = (130, 112)
TAIL_PIVOT = (96, 96)  # base de la queue, rappelée dans FrontScene.RAT_TAIL_PIVOT

GR, GRD, GRL, GRLL = (122, 122, 136), (92, 92, 106), (156, 156, 170), (190, 190, 200)
PINK, PINKD = (232, 150, 166), (190, 104, 124)


def outline(img, width=1):
    """Contour sombre de {width} pixels autour de la forme."""
    a = np.array(img)
    alpha = a[:, :, 3] > 0
    grown = alpha.copy()
    for _ in range(width):
        prev = grown.copy()
        grown[1:, :] |= prev[:-1, :]
        grown[:-1, :] |= prev[1:, :]
        grown[:, 1:] |= prev[:, :-1]
        grown[:, :-1] |= prev[:, 1:]
    o = np.zeros_like(a)
    o[grown] = OUTLINE
    out = Image.fromarray(o)
    out.alpha_composite(img)
    return out


def thick(img):
    """Le style des ennemis en grand (choix d'Astra, 2026-10-31) : contour épais de 2 pixels, comme le reste du jeu."""
    return outline(img)


def flash(img):
    """Silhouette blanche de la pièce."""
    a = np.array(img)
    a[a[:, :, 3] > 0, :3] = 255
    return Image.fromarray(a)


def canvas():
    im = Image.new('RGBA', CANVAS, (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


# ---------------------------------------------------------------------------- le rat

def rat_tail():
    im, d = canvas()
    pts = [(int(96 + k * 1.1 + 8 * math.sin(k / 6)), int(96 - k * 1.2 - 6 * math.cos(k / 8))) for k in range(0, 34)]
    d.line(pts, fill=PINKD, width=4)
    d.line(pts, fill=PINK, width=2)
    return outline(im)


def rat_body():
    im, d = canvas()
    w, h = CANVAS
    by = 66
    d.polygon([(30, h), (38, by + 8), (92, by + 8), (100, h)], fill=GRD)
    d.polygon([(40, h), (46, by + 10), (84, by + 10), (90, h)], fill=(232, 228, 214))
    for y in range(by + 12, h, 5):
        d.rectangle([44, y, 86, y + 1], fill=(44, 64, 120))
    d.polygon([(34, h), (40, by + 9), (52, by + 9), (56, h)], fill=(116, 64, 36))
    d.polygon([(96, h), (90, by + 9), (78, by + 9), (74, h)], fill=(116, 64, 36))
    d.point([(53, by + 22), (53, by + 30), (77, by + 22), (77, by + 30)], fill=(230, 190, 80))
    d.polygon([(38, by + 10), (26, by + 12), (14, h), (30, h)], fill=GR)
    d.polygon([(92, by + 10), (104, by + 12), (116, h), (100, h)], fill=GR)
    d.line([(27, by + 14), (17, h)], fill=GRL)
    return outline(im)


def rat_head():
    im, d = canvas()
    hy = 6
    for ex in (26, 104):
        d.ellipse([ex - 15, hy - 2, ex + 15, hy + 28], fill=GRD)
        d.ellipse([ex - 10, hy + 3, ex + 10, hy + 23], fill=PINK)
        d.ellipse([ex - 6, hy + 8, ex + 4, hy + 20], fill=PINKD)
    d.ellipse([90, hy + 25, 100, hy + 35], outline=(240, 200, 70), width=2)  # boucle d'oreille
    d.ellipse([30, hy + 6, 100, hy + 62], fill=GR)
    d.ellipse([36, hy + 10, 80, hy + 40], fill=GRL)
    d.ellipse([44, hy + 38, 86, hy + 70], fill=GRL)
    d.ellipse([50, hy + 42, 80, hy + 66], fill=GRLL)
    # bandana rouge à pois
    d.chord([30, hy + 6, 100, hy + 62], 190, 350, fill=(196, 44, 40))
    d.chord([30, hy + 6, 100, hy + 62], 190, 350, outline=(140, 26, 30))
    for px, py in ((46, hy + 12), (60, hy + 9), (74, hy + 10), (88, hy + 15), (54, hy + 18), (68, hy + 17), (82, hy + 20)):
        d.rectangle([px, py, px + 1, py + 1], fill=(250, 236, 220))
    d.polygon([(98, hy + 20), (114, hy + 16), (110, hy + 26)], fill=(176, 36, 34))
    d.polygon([(98, hy + 22), (116, hy + 28), (106, hy + 32)], fill=(196, 44, 40))
    # bandeau sur un oeil, l'autre rouge
    d.line([(32, hy + 26), (98, hy + 22)], fill=(30, 22, 22), width=2)
    d.ellipse([44, hy + 28, 58, hy + 40], fill=(30, 22, 22))
    d.ellipse([71, hy + 28, 85, hy + 40], fill=(250, 240, 230))
    d.ellipse([74, hy + 30, 83, hy + 39], fill=(214, 24, 30))
    d.rectangle([77, hy + 33, 78, hy + 36], fill=(40, 10, 10))
    d.point([(75, hy + 31)], fill=(255, 255, 255))
    d.line([(68, hy + 26), (86, hy + 24)], fill=(60, 54, 64), width=2)  # sourcil méchant
    # museau, dents, moustaches, cicatrice
    d.ellipse([60, hy + 52, 72, hy + 60], fill=PINKD)
    d.ellipse([62, hy + 53, 67, hy + 56], fill=PINK)
    d.line([(66, hy + 60), (66, hy + 64)], fill=(70, 60, 70))
    d.rectangle([62, hy + 64, 65, hy + 70], fill=(250, 244, 220))
    d.rectangle([67, hy + 64, 70, hy + 70], fill=(250, 244, 220))
    d.line([(62, hy + 64), (70, hy + 64)], fill=(70, 60, 70))
    for dy in (-3, 0, 3):
        d.line([(56, hy + 58 + dy), (34, hy + 54 + dy * 2)], fill=(214, 214, 220))
        d.line([(76, hy + 58 + dy), (98, hy + 54 + dy * 2)], fill=(214, 214, 220))
    d.line([(56, hy + 74), (64, hy + 72)], fill=(180, 180, 190))
    return outline(im)


def rat_eyelid():
    """Paupière fermée, posée sur l'oeil rouge (même cadre que la tête)."""
    im, d = canvas()
    hy = 6
    d.ellipse([70, hy + 27, 86, hy + 41], fill=GR)
    d.line([(72, hy + 34), (84, hy + 34)], fill=(40, 30, 34), width=2)
    return im


def rat_paw():
    im = Image.new('RGBA', (30, 18), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([2, 0, 27, 12], 5, fill=GR)
    d.rounded_rectangle([4, 1, 25, 6], 3, fill=GRL)
    for k in range(4):
        x = 4 + k * 6
        d.rounded_rectangle([x, 8, x + 4, 16], 2, fill=PINK)
        d.line([(x + 2, 14), (x + 2, 16)], fill=(246, 236, 222))
    return outline(im)


# ---------------------------------------------------------------------------- la cale

def decor_cale():
    """Cale d'un navire : planches, poutres, hublot, tonneaux, caisses et deux lanternes (allumées par le jeu)."""
    w, h = 360, 220
    im = Image.new('RGBA', (w, h), (40, 26, 18, 255))
    d = ImageDraw.Draw(im)
    planks = [(92, 60, 36), (82, 53, 31), (99, 66, 40), (86, 56, 33)]
    for i, y in enumerate(range(0, 168, 8)):
        d.rectangle([0, y, w, y + 7], fill=planks[i % 4])
        d.line([0, y + 7, w, y + 7], fill=(52, 32, 20))
        for x in range((i * 37) % 60, w, 60):
            d.line([x, y, x, y + 6], fill=(60, 38, 23))
            d.point([(x + 3, y + 3)], fill=(150, 140, 120))
    for x in (0, 88, 180, 272, 352):
        d.rectangle([x, 0, x + 9, 168], fill=(58, 36, 22))
        d.line([x + 1, 0, x + 1, 168], fill=(84, 54, 33))
    d.rectangle([0, 0, w, 10], fill=(52, 32, 20))
    d.line([0, 10, w, 10], fill=(30, 18, 12))
    # hublot
    cx, cy = 48, 62
    d.ellipse([cx - 19, cy - 19, cx + 19, cy + 19], fill=(176, 132, 52))
    d.ellipse([cx - 15, cy - 15, cx + 15, cy + 15], fill=(42, 92, 140))
    for k in range(-15, 16):
        yy = cy + 3 + int(2 * math.sin(k / 3))
        d.line([cx + k, yy, cx + k, cy + 15], fill=(28, 66, 108))
    d.ellipse([cx - 15, cy - 15, cx + 15, cy + 15], outline=(120, 84, 30))
    d.line([cx - 8, cy - 8, cx - 3, cy - 12], fill=(170, 210, 240))
    for a in range(0, 360, 45):
        d.point([(cx + int(17 * math.cos(math.radians(a))), cy + int(17 * math.sin(math.radians(a))))], fill=(230, 196, 110))
    # tonneaux et caisses
    for bx, by in ((8, 128), (32, 136)):
        d.rectangle([bx, by, bx + 22, by + 36], fill=(110, 70, 38))
        d.rectangle([bx, by + 6, bx + 22, by + 8], fill=(70, 70, 76))
        d.rectangle([bx, by + 28, bx + 22, by + 30], fill=(70, 70, 76))
        d.line([bx + 7, by, bx + 7, by + 36], fill=(88, 54, 30))
    for cx2, cy2 in ((318, 130), (332, 108)):
        d.rectangle([cx2, cy2, cx2 + 22, cy2 + 22], fill=(126, 88, 48))
        d.rectangle([cx2, cy2, cx2 + 22, cy2 + 22], outline=(70, 44, 24))
        d.line([cx2, cy2, cx2 + 22, cy2 + 22], fill=(84, 56, 30))
        d.line([cx2, cy2 + 22, cx2 + 22, cy2], fill=(84, 56, 30))
    for k in range(0, 60):  # cordage
        d.point([(286 + k, 20 + int(10 * math.sin(k / 60 * math.pi)))], fill=(170, 140, 90))
    # sol
    d.rectangle([0, 168, w, h], fill=(48, 31, 20))
    for y in range(170, h, 10):
        d.line([0, y, w, y], fill=(36, 23, 15))
    # lanternes (FrontScene.CALE_LANTERNS : centre de leur flamme)
    for lx in (128, 236):
        lan = Image.new('RGBA', (14, 22), (0, 0, 0, 0))
        ld = ImageDraw.Draw(lan)
        ld.line([7, 0, 7, 4], fill=(60, 60, 64))
        ld.rectangle([2, 5, 11, 19], fill=(46, 40, 36))
        ld.rectangle([4, 7, 9, 17], fill=(255, 214, 120))
        ld.rectangle([5, 9, 8, 15], fill=(255, 246, 200))
        ld.rectangle([1, 19, 12, 21], fill=(46, 40, 36))
        im.alpha_composite(outline(lan), (lx - 7, 18))
    # coins assombris
    a = np.array(im).astype(float)
    yy, xx = np.mgrid[0:h, 0:w]
    dist = np.sqrt(((xx - w / 2) / (w / 2)) ** 2 + ((yy - h * 0.45) / (h * 0.9)) ** 2)
    a[:, :, :3] *= np.clip(1.12 - 0.5 * dist, 0.4, 1)[:, :, None]
    return Image.fromarray(a.astype(np.uint8)).convert('RGB')


# ---------------------------------------------------------------------------- la Prairie

SUITS = ['pique', 'trefle', 'coeur', 'carreau']
ACCENT = {'pique': (74, 74, 138), 'trefle': (46, 138, 74), 'coeur': (192, 40, 58), 'carreau': (224, 134, 30)}
ACCENT_D = {'pique': (46, 46, 90), 'trefle': (30, 90, 48), 'coeur': (128, 26, 38), 'carreau': (168, 90, 18)}
ACCENT_L = {s: tuple(min(255, int(v * 1.3) + 20) for v in c) for s, c in ACCENT.items()}
INK = {'pique': (30, 26, 46), 'trefle': (30, 26, 46), 'coeur': (216, 32, 44), 'carreau': (216, 32, 44)}
SKIN, SKIN_D, SKIN_L = (240, 200, 160), (200, 154, 114), (250, 220, 188)
PAPER, PAPER_D = (246, 240, 226), (216, 208, 192)
GOLD_P, GOLD_PD, GOLD_PL = (255, 213, 74), (196, 137, 30), (255, 238, 150)
STEEL, STEEL_D, STEEL_L = (184, 192, 204), (122, 132, 148), (226, 232, 240)
SHAFT, SHAFT_D = (138, 90, 42), (94, 58, 24)
EYE_C = (26, 16, 24)
LEATHER, LEATHER_D, LEATHER_L = (120, 74, 40), (82, 48, 24), (156, 104, 60)

SYMBOLS = {
    'coeur': [".XX.XX.", "XXXXXXX", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."],
    'carreau': ["...X...", "..XXX..", ".XXXXX.", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."],
    'pique': ["...X...", "..XXX..", ".XXXXX.", "XXXXXXX", "XXXXXXX", "...X...", "..XXX.."],
    'trefle': ["..XXX..", "..XXX..", "XX.X.XX", "XXXXXXX", "XX.X.XX", "...X...", "..XXX.."],
}


def stamp(d, suit, x, y, scale, color):
    """Le symbole de la couleur (7 x 7), coin haut gauche en (x, y), agrandi {scale} fois."""
    for row, line in enumerate(SYMBOLS[suit]):
        for col, ch in enumerate(line):
            if ch == 'X':
                d.rectangle([x + col * scale, y + row * scale, x + col * scale + scale - 1, y + row * scale + scale - 1],
                            fill=color)


def symbol_image(suit, scale, color, rim=None):
    """Le symbole seul, détouré de {rim} (contour d'un pixel) s'il est donné."""
    im = Image.new('RGBA', (7 * scale + 2, 7 * scale + 2), (0, 0, 0, 0))
    stamp(ImageDraw.Draw(im), suit, 1, 1, scale, color)
    if rim:
        a = np.array(im)
        alpha = a[:, :, 3] > 0
        grown = alpha.copy()
        grown[1:, :] |= alpha[:-1, :]
        grown[:-1, :] |= alpha[1:, :]
        grown[:, 1:] |= alpha[:, :-1]
        grown[:, :-1] |= alpha[:, 1:]
        o = np.zeros_like(a)
        o[grown] = rim + (255,)
        out = Image.fromarray(o)
        out.alpha_composite(im)
        return out
    return im


# Gens de la Prairie : tête au centre en haut, corps dessous, bras qui descendent vers les mains
# posées sur le rebord de la table (pièce « paw », dessinée par le jeu). L'arme se tient debout à
# droite (pièce « swing » chez les rois : elle se balance un peu autour de la main).
HEAD_CX = 65


def arms(d, sleeve, sleeve_d, cuff, cuff_d):
    """Les deux bras, des épaules jusqu'au rebord de la table (les mains sont posées dessus)."""
    w, h = CANVAS
    d.polygon([(26, 70), (46, 72), (36, h), (8, h)], fill=sleeve)
    d.polygon([(26, 70), (32, 71), (16, h), (8, h)], fill=shade(sleeve, 1.2))
    d.polygon([(104, 70), (84, 72), (94, h), (122, h)], fill=sleeve_d)
    d.polygon([(104, 70), (98, 71), (114, h), (122, h)], fill=shade(sleeve_d, 0.8))
    d.polygon([(10, h - 12), (36, h - 12), (36, h - 6), (9, h - 6)], fill=cuff)
    d.line([(10, h - 7), (36, h - 7)], fill=cuff_d)
    d.polygon([(94, h - 12), (120, h - 12), (121, h - 6), (94, h - 6)], fill=cuff_d)


def card_torso(d, suit, x0, y0, x1, y1, border, border_d, big=3):
    """Corps en carte à jouer : papier, bordure, grand symbole et petits symboles aux coins."""
    d.rounded_rectangle([x0, y0, x1, y1 + 8], 5, fill=border_d)
    d.rounded_rectangle([x0 + 2, y0 + 2, x1 - 2, y1 + 8], 4, fill=PAPER)
    d.rectangle([x1 - 10, y0 + 4, x1 - 3, y1 + 8], fill=PAPER_D)
    d.line([(x0 + 2, y0 + 4), (x0 + 2, y1)], fill=border)
    size = 7 * big
    stamp(d, suit, (x0 + x1 - size) // 2 + 1, (y0 + y1 - size) // 2 + 2, big, INK[suit])
    stamp(d, suit, x0 + 5, y0 + 5, 1, INK[suit])


def face(d, cy, brows=None):
    """Visage rond et oreilles, centré sur HEAD_CX ; yeux à (HEAD_CX -/+ 9, cy + 2)."""
    cx = HEAD_CX
    for ex in (cx - 20, cx + 20):
        d.ellipse([ex - 4, cy - 4, ex + 4, cy + 7], fill=SKIN_D)
        d.ellipse([ex - 2, cy - 2, ex + 2, cy + 4], fill=SKIN)
    d.ellipse([cx - 18, cy - 18, cx + 18, cy + 18], fill=SKIN_D)
    d.ellipse([cx - 18, cy - 18, cx + 13, cy + 15], fill=SKIN)
    d.ellipse([cx - 14, cy - 14, cx - 2, cy - 2], fill=SKIN_L)
    for ex in (cx - 9, cx + 9):
        d.rectangle([ex - 3, cy, ex + 2, cy + 4], fill=(250, 246, 240))
        d.rectangle([ex - 1, cy + 1, ex + 1, cy + 4], fill=EYE_C)
        d.point([(ex - 1, cy + 1)], fill=(255, 255, 255))
    if brows:
        d.line([(cx - 14, cy - 4), (cx - 5, cy - 1)], fill=brows, width=2)
        d.line([(cx + 14, cy - 4), (cx + 5, cy - 1)], fill=brows, width=2)
    d.polygon([(cx - 1, cy + 4), (cx + 3, cy + 9), (cx - 3, cy + 10)], fill=SKIN_D)  # nez
    d.ellipse([cx - 15, cy + 7, cx - 9, cy + 11], fill=(236, 160, 140))
    d.ellipse([cx + 9, cy + 7, cx + 15, cy + 11], fill=(236, 160, 140))


def eyelids(cy, color=SKIN):
    """Paupières fermées sur les deux yeux de {face}."""
    im, d = canvas()
    for ex in (HEAD_CX - 9, HEAD_CX + 9):
        d.rectangle([ex - 4, cy - 1, ex + 3, cy + 5], fill=color)
        d.line([(ex - 3, cy + 3), (ex + 2, cy + 3)], fill=EYE_C)
    return im


def spear_head(d, suit, x, top):
    """Fer de lance à la forme du symbole de la couleur, au sommet d'une hampe en {x}."""
    img = symbol_image(suit, 3, STEEL, rim=STEEL_D)
    return img


def spear(im, suit, x, top, bottom):
    """Lance debout : hampe de bois, bague à la couleur, fer en forme de symbole."""
    d = ImageDraw.Draw(im)
    d.rectangle([x - 2, top + 18, x + 1, bottom], fill=SHAFT)
    d.rectangle([x + 1, top + 18, x + 2, bottom], fill=SHAFT_D)
    d.rectangle([x - 4, top + 18, x + 4, top + 22], fill=ACCENT[suit])
    d.rectangle([x - 4, top + 21, x + 4, top + 22], fill=ACCENT_D[suit])
    head = outline(symbol_image(suit, 3, STEEL, rim=STEEL_D))
    hd = ImageDraw.Draw(head)
    hd.line([(5, 4), (5, 16)], fill=STEEL_L)
    im.alpha_composite(head, (x - head.width // 2, top))


def soldier_parts(suit):
    accent, dark, light = ACCENT[suit], ACCENT_D[suit], ACCENT_L[suit]
    w, h = CANVAS
    cy = 44  # yeux
    # Corps
    body, d = canvas()
    if suit == 'carreau':
        pass
    spear(body, suit, 114, 0, h)
    d.rectangle([57, 58, 73, 68], fill=SKIN_D)
    arms(d, accent, dark, GOLD_P, GOLD_PD)
    card_torso(d, suit, 38, 64, 92, h, accent, dark)
    for ex, c in ((32, accent), (98, dark)):  # épaulettes à franges dorées
        d.ellipse([ex - 13, 62, ex + 13, 78], fill=c)
        d.ellipse([ex - 9, 64, ex + 3, 72], fill=shade(c, 1.25))
        for k in range(-11, 12, 3):
            d.line([(ex + k, 76), (ex + k, 80)], fill=GOLD_P)
            d.point([(ex + k, 80)], fill=GOLD_PD)
    d.rectangle([40, h - 14, 90, h - 9], fill=LEATHER)
    d.line([(40, h - 9), (90, h - 9)], fill=LEATHER_D)
    d.rectangle([61, h - 15, 69, h - 8], fill=GOLD_P)
    d.rectangle([63, h - 13, 67, h - 10], fill=LEATHER_D)
    if suit == 'trefle':  # Intérêts : bourse pleine à la ceinture et piles de pièces
        d.ellipse([74, 84, 92, 102], fill=LEATHER)
        d.ellipse([76, 86, 86, 96], fill=LEATHER_L)
        d.rectangle([79, 80, 87, 86], fill=LEATHER_D)
        d.line([(77, 85), (89, 85)], fill=GOLD_P)
        stamp(d, suit, 80, 90, 1, GOLD_P)
        for k in range(4):
            for px in (44, 51):
                py = 96 - k * 3 - (2 if px == 51 else 0)
                d.rectangle([px - 3, py - 1, px + 3, py + 1], fill=GOLD_P)
                d.line([(px - 3, py + 1), (px + 3, py + 1)], fill=GOLD_PD)
                d.point([(px - 2, py - 1)], fill=GOLD_PL)
    if suit == 'coeur':  # potions glissées dans la ceinture, de part et d'autre du symbole
        for px in (45, 54, 77, 86):
            py = 90
            d.rectangle([px - 1, py - 8, px + 1, py - 5], fill=(200, 210, 220))
            d.rectangle([px - 2, py - 10, px + 2, py - 8], fill=LEATHER_D)
            d.ellipse([px - 4, py - 5, px + 4, py + 5], fill=(150, 220, 240))
            d.ellipse([px - 3, py - 2, px + 3, py + 5], fill=(224, 30, 60))
            d.point([(px - 2, py - 3), (px - 2, py - 1)], fill=(255, 255, 255))
    if suit == 'carreau':  # grand bouclier en losange au bras gauche (défense épaisse)
        sh = Image.new('RGBA', (52, 60), (0, 0, 0, 0))
        sd = ImageDraw.Draw(sh)
        sd.polygon([(26, 0), (51, 30), (26, 59), (0, 30)], fill=GOLD_PD)
        sd.polygon([(26, 3), (48, 30), (26, 56), (3, 30)], fill=GOLD_P)
        sd.polygon([(26, 7), (44, 30), (26, 52), (7, 30)], fill=accent)
        sd.polygon([(26, 7), (44, 30), (26, 30)], fill=light)
        sd.polygon([(26, 30), (44, 30), (26, 52)], fill=dark)
        stamp(sd, suit, 19, 23, 2, (250, 244, 230))
        for px, py in ((26, 5), (47, 30), (26, 54), (5, 30)):
            sd.point([(px, py)], fill=(255, 255, 255))
        body.alpha_composite(outline(sh), (4, 60))
    body = outline(body)
    # Tête : visage, casque d'acier à nasal
    head, d = canvas()
    face(d, cy)
    d.line([(HEAD_CX - 6, cy + 14), (HEAD_CX + 5, cy + 13)], fill=(138, 58, 42), width=2)
    d.chord([HEAD_CX - 23, 12, HEAD_CX + 23, 62], 180, 360, fill=STEEL)
    d.chord([HEAD_CX - 23, 12, HEAD_CX + 23, 62], 270, 360, fill=STEEL_D)
    d.chord([HEAD_CX - 17, 16, HEAD_CX - 3, 40], 180, 300, fill=STEEL_L)
    d.rectangle([HEAD_CX - 25, 36, HEAD_CX + 25, 40], fill=STEEL_D)
    d.line([(HEAD_CX - 25, 36), (HEAD_CX + 25, 36)], fill=STEEL_L)
    for k in range(-21, 24, 7):
        d.point([(HEAD_CX + k, 38)], fill=STEEL_L)
    d.rectangle([HEAD_CX - 2, 40, HEAD_CX + 1, 52], fill=STEEL_D)
    d.line([(HEAD_CX - 2, 40), (HEAD_CX - 2, 51)], fill=STEEL)
    stamp(d, suit, HEAD_CX - 3, 24, 1, accent)
    d.rectangle([HEAD_CX - 3, 9, HEAD_CX + 3, 14], fill=GOLD_P)  # porte-plumet
    d.line([(HEAD_CX - 3, 14), (HEAD_CX + 3, 14)], fill=GOLD_PD)
    head = outline(head)
    # Plumet : crinière qui part du sommet du casque et retombe en arrière (il se balance)
    plume, d = canvas()
    pts = [(HEAD_CX + int(k * 1.15), int(11 - 9 * math.sin(k / 34 * math.pi) + k * 0.25)) for k in range(0, 40)]
    for i, (px, py) in enumerate(pts):
        r = 3 + int(3 * math.sin(i / 40 * math.pi))
        d.ellipse([px - r, py - r, px + r, py + r], fill=dark)
    for i, (px, py) in enumerate(pts):
        r = 2 + int(2 * math.sin(i / 40 * math.pi))
        d.ellipse([px - r, py - r - 1, px + r, py + r - 1], fill=accent)
    for i, (px, py) in enumerate(pts[4:-4:3]):
        d.point([(px, py - 2)], fill=light)
    plume = outline(plume)
    return {'body': body, 'head': head, 'swing': plume, 'eyelid': eyelids(cy),
            'paw': glove(LEATHER, LEATHER_L, LEATHER_D)}


def glove(color, light, dark):
    """Main gantée agrippée au rebord de la table (même taille que la patte du rat)."""
    im = Image.new('RGBA', (30, 18), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([2, 0, 27, 11], 4, fill=color)
    d.rounded_rectangle([4, 1, 25, 5], 3, fill=light)
    for k in range(4):
        x = 4 + k * 6
        d.rounded_rectangle([x, 7, x + 4, 16], 2, fill=color)
        d.line([(x + 1, 9), (x + 1, 13)], fill=light)
        d.line([(x + 4, 9), (x + 4, 15)], fill=dark)
    return outline(im)


def king_parts(suit):
    accent, dark, light = ACCENT[suit], ACCENT_D[suit], ACCENT_L[suit]
    w, h = CANVAS
    cy = 40
    gem = {'trefle': (58, 216, 106), 'pique': (106, 122, 255)}.get(suit, (255, 58, 74))
    beard, beard_d = (232, 228, 220), (184, 178, 168)
    # Corps : manteau royal, col d'hermine, plastron en carte
    body, d = canvas()
    d.polygon([(22, 64), (108, 64), (126, h), (4, h)], fill=accent)
    d.polygon([(70, 64), (108, 64), (126, h), (80, h)], fill=dark)
    for x in (16, 30, 100, 114):
        d.line([(x + 6, 72), (x, h)], fill=shade(accent if x < 65 else dark, 0.8))
    d.rectangle([57, 54, 73, 66], fill=SKIN_D)
    arms(d, accent, dark, PAPER, PAPER_D)
    for x0 in (10, 94):  # hermine des manchettes
        for k in range(3):
            d.rectangle([x0 + 5 + k * 9, h - 10, x0 + 6 + k * 9, h - 8], fill=EYE_C)
    card_torso(d, suit, 46, 72, 84, h, GOLD_P, GOLD_PD, big=2)
    d.rounded_rectangle([24, 60, 106, 72], 6, fill=PAPER)
    d.rounded_rectangle([24, 67, 106, 72], 3, fill=PAPER_D)
    for x in range(30, 102, 8):
        d.rectangle([x, 64 + (x // 8) % 2 * 2, x + 1, 66 + (x // 8) % 2 * 2], fill=EYE_C)
    if suit == 'carreau':  # Épines : épaulières hérissées de pointes
        for ex in (26, 104):
            d.chord([ex - 18, 56, ex + 18, 88], 180, 360, fill=STEEL_D)
            d.chord([ex - 15, 59, ex + 15, 85], 180, 360, fill=STEEL)
            for k in (-12, -5, 2, 9):
                tx = ex + k + (2 if ex > 65 else 0)
                d.polygon([(tx - 3, 66), (tx + 3, 66), (tx, 52)], fill=STEEL_L)
                d.line([(tx, 54), (tx + 2, 65)], fill=STEEL_D)
    if suit == 'trefle':  # Intérêts et Dés pipés : chaîne d'or et deux dés
        for k in range(15):
            px = 46 + k * 2.7
            py = 74 + 12 * math.sin(k / 14 * math.pi)
            d.ellipse([px - 1.5, py - 1.5, px + 1.5, py + 1.5], fill=GOLD_P)
        for dx, dy, pips in ((52, 86, ((2, 2), (6, 6))), (66, 88, ((2, 2), (4, 4), (6, 6)))):
            d.rectangle([dx, dy, dx + 9, dy + 9], fill=(250, 248, 240))
            d.rectangle([dx + 7, dy + 1, dx + 9, dy + 9], fill=PAPER_D)
            d.rectangle([dx, dy, dx + 9, dy + 9], outline=OUTLINE)
            for px, py in pips:
                d.rectangle([dx + px, dy + py, dx + px + 1, dy + py + 1], fill=(200, 20, 40))
    body = outline(body)
    # Tête : visage, barbe, couronne
    head, d = canvas()
    face(d, cy, brows=(120, 110, 100) if suit != 'pique' else (60, 40, 40))
    if suit == 'pique':  # Rage : sourcils froncés, veine
        d.line([(HEAD_CX + 13, cy - 12), (HEAD_CX + 16, cy - 9)], fill=(200, 60, 60), width=2)
    if suit == 'coeur':  # yeux rouges
        for ex in (HEAD_CX - 9, HEAD_CX + 9):
            d.rectangle([ex - 1, cy + 1, ex + 1, cy + 4], fill=(214, 24, 30))
    d.ellipse([HEAD_CX - 21, cy + 4, HEAD_CX + 21, cy + 34], fill=beard_d)
    d.ellipse([HEAD_CX - 21, cy + 4, HEAD_CX + 15, cy + 30], fill=beard)
    d.polygon([(HEAD_CX - 16, cy + 22), (HEAD_CX + 16, cy + 22), (HEAD_CX, cy + 46)], fill=beard)
    d.polygon([(HEAD_CX, cy + 22), (HEAD_CX + 16, cy + 22), (HEAD_CX, cy + 46)], fill=beard_d)
    for k in range(-12, 13, 5):
        d.line([(HEAD_CX + k, cy + 20), (HEAD_CX + k * 0.6, cy + 36)], fill=beard_d)
    # moustache et bouche (crocs pour le Roi de Coeur)
    d.ellipse([HEAD_CX - 16, cy + 10, HEAD_CX, cy + 18], fill=beard)
    d.ellipse([HEAD_CX, cy + 10, HEAD_CX + 16, cy + 18], fill=beard_d)
    if suit == 'coeur':
        d.rounded_rectangle([HEAD_CX - 8, cy + 17, HEAD_CX + 8, cy + 25], 3, fill=(90, 16, 26))
        d.polygon([(HEAD_CX - 7, cy + 17), (HEAD_CX - 3, cy + 17), (HEAD_CX - 5, cy + 24)], fill=(255, 255, 250))
        d.polygon([(HEAD_CX + 3, cy + 17), (HEAD_CX + 7, cy + 17), (HEAD_CX + 5, cy + 24)], fill=(255, 255, 250))
    else:
        d.line([(HEAD_CX - 6, cy + 19), (HEAD_CX + 6, cy + 19)], fill=(138, 58, 42), width=2)
    # couronne
    d.rectangle([HEAD_CX - 22, 14, HEAD_CX + 22, 24], fill=GOLD_P)
    d.rectangle([HEAD_CX - 22, 22, HEAD_CX + 22, 24], fill=GOLD_PD)
    d.line([(HEAD_CX - 22, 15), (HEAD_CX + 22, 15)], fill=GOLD_PL)
    for k, top in ((-22, 4), (-11, 8), (11, 8), (22, 4)):
        d.polygon([(HEAD_CX + k - 4, 15), (HEAD_CX + k + 4, 15), (HEAD_CX + k, top)], fill=GOLD_P)
        d.ellipse([HEAD_CX + k - 2, top - 3, HEAD_CX + k + 2, top + 1], fill=GOLD_PL)
    crest = outline(symbol_image(suit, 2, GOLD_P, rim=GOLD_PD))
    head.alpha_composite(crest, (HEAD_CX - crest.width // 2, 0))
    for k, c in ((-14, gem), (0, (255, 255, 255)), (14, gem)):
        d.rectangle([HEAD_CX + k - 2, 17, HEAD_CX + k + 2, 21], fill=c)
        d.point([(HEAD_CX + k - 1, 18)], fill=(255, 255, 255))
    head = outline(head)
    # Arme debout à droite, tenue par la main sur le rebord : elle se balance un peu
    staff, d = canvas()
    x = 114
    if suit == 'pique':  # grande épée
        d.polygon([(x - 4, 22), (x + 4, 22), (x + 4, 92), (x - 4, 92)], fill=STEEL)
        d.polygon([(x - 4, 22), (x, 10), (x + 4, 22)], fill=STEEL)
        d.polygon([(x, 12), (x + 4, 22), (x + 4, 92), (x, 92)], fill=STEEL_D)
        d.line([(x - 2, 22), (x - 2, 90)], fill=STEEL_L)
        d.rectangle([x - 11, 92, x + 11, 96], fill=GOLD_P)
        d.rectangle([x - 11, 95, x + 11, 96], fill=GOLD_PD)
        d.rectangle([x - 2, 96, x + 2, h], fill=LEATHER)
        d.ellipse([x - 5, 88, x + 5, 98], fill=gem)
    else:  # sceptre surmonté du symbole
        d.rectangle([x - 2, 26, x + 2, h], fill=GOLD_P)
        d.rectangle([x + 1, 26, x + 2, h], fill=GOLD_PD)
        for y in (34, 60, 86):
            d.rectangle([x - 4, y, x + 4, y + 3], fill=GOLD_PD)
        top = outline(symbol_image(suit, 3, GOLD_P, rim=GOLD_PD))
        ImageDraw.Draw(top).rectangle([10, 8, 13, 11], fill=gem)
        staff.alpha_composite(top, (x - top.width // 2, 4))
    staff = outline(staff)
    glove_c = (250, 246, 236)
    return {'body': body, 'head': head, 'swing': staff, 'eyelid': eyelids(cy),
            'paw': glove(glove_c, (255, 255, 255), PAPER_D)}


def decor_prairie(suit):
    """La Prairie : ciel, nuages, collines, le château de la couleur et ses bannières, champ fleuri."""
    w, h = 360, 220
    im = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    d = ImageDraw.Draw(im)
    top, bottom = (96, 168, 232), (196, 230, 246)
    for y in range(0, 150):
        t = min(1, y / 140)
        d.line([(0, y), (w, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bottom)))
    # soleil
    d.ellipse([292, 14, 322, 44], fill=(255, 240, 170))
    d.ellipse([296, 18, 318, 40], fill=(255, 252, 220))
    # nuages
    for cx, cy, k in ((40, 30, 1.0), (150, 18, 0.8), (240, 46, 0.9), (340, 70, 0.7), (96, 64, 0.6)):
        for dx, dy, r in ((0, 0, 10), (12, -5, 12), (26, 0, 10), (13, 4, 11)):
            r = int(r * k)
            d.ellipse([cx + dx * k - r, cy + dy * k - r, cx + dx * k + r, cy + dy * k + r], fill=(250, 252, 255))
        d.line([(cx - 8 * k, cy + 9 * k), (cx + 34 * k, cy + 9 * k)], fill=(214, 228, 240), width=2)
    # collines lointaines puis proches
    for base, amp, freq, c in ((112, 14, 50, (138, 190, 150)), (128, 12, 38, (104, 170, 96)), (146, 8, 28, (82, 150, 70))):
        for x in range(w):
            y = base - int(amp * (0.5 + 0.5 * math.sin(x / freq + base)))
            d.line([(x, y), (x, h)], fill=c)
    # devant les collines, détourés comme le reste du jeu : château, arbre, clôture, étendards
    fg = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(fg)
    # château de la couleur, à gauche
    wall, wall_d, roof = (214, 206, 192), (170, 160, 148), ACCENT_D[suit]
    cx0 = 30
    d.rectangle([cx0, 70, cx0 + 80, 128], fill=wall)
    d.rectangle([cx0 + 60, 70, cx0 + 80, 128], fill=wall_d)
    for tx in (cx0 - 6, cx0 + 30, cx0 + 72):
        d.rectangle([tx, 48, tx + 16, 128], fill=wall)
        d.rectangle([tx + 11, 48, tx + 16, 128], fill=wall_d)
        d.polygon([(tx - 3, 48), (tx + 19, 48), (tx + 8, 24)], fill=roof)
        d.polygon([(tx + 8, 24), (tx + 19, 48), (tx + 8, 48)], fill=shade(roof, 0.7))
        d.line([(tx + 8, 24), (tx + 8, 12)], fill=(60, 50, 50))
        d.polygon([(tx + 9, 12), (tx + 21, 15), (tx + 9, 19)], fill=ACCENT[suit])
        d.rectangle([tx + 6, 60, tx + 9, 66], fill=(60, 50, 60))
    for bx in range(cx0, cx0 + 80, 8):
        d.rectangle([bx, 64, bx + 4, 70], fill=wall)
    d.rounded_rectangle([cx0 + 34, 100, cx0 + 48, 128], 6, fill=(70, 46, 30))
    d.line([(cx0 + 41, 102), (cx0 + 41, 128)], fill=(50, 32, 20))
    # bannières de la couleur, sur les murs
    for bx in (cx0 + 10, cx0 + 56):
        d.rectangle([bx, 76, bx + 12, 96], fill=ACCENT[suit])
        d.polygon([(bx, 96), (bx + 6, 92), (bx + 12, 96), (bx + 12, 100), (bx + 6, 96), (bx, 100)], fill=ACCENT[suit])
        d.rectangle([bx + 9, 76, bx + 12, 98], fill=ACCENT_D[suit])
        stamp(d, suit, bx + 3, 82, 1, (250, 244, 230))
        d.line([(bx - 1, 76), (bx + 13, 76)], fill=GOLD_PD)
    # arbre et clôture, à droite
    d.rectangle([300, 92, 308, 140], fill=(100, 64, 34))
    d.rectangle([305, 92, 308, 140], fill=(76, 46, 24))
    for cx, cy, r, c in ((304, 80, 26, (52, 120, 52)), (290, 90, 16, (60, 136, 58)), (318, 88, 18, (44, 104, 46)),
                         (302, 70, 16, (74, 152, 66))):
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=c)
    for x in range(230, 360, 18):
        d.rectangle([x, 128, x + 3, 150], fill=(176, 130, 80))
    for y in (134, 142):
        d.rectangle([226, y, 360, y + 2], fill=(196, 150, 96))
    # étendards plantés dans le champ
    for fx in (196, 260):
        d.line([(fx, 96), (fx, 150)], fill=(90, 60, 36), width=2)
        d.polygon([(fx + 1, 96), (fx + 22, 101), (fx + 1, 108)], fill=ACCENT[suit])
        stamp(d, suit, fx + 4, 99, 1, (250, 244, 230))
    im.alpha_composite(outline(fg))
    d = ImageDraw.Draw(im)
    # champ fleuri
    d.rectangle([0, 150, w, h], fill=(76, 146, 62))
    rng = np.random.default_rng(7)
    flower = {'pique': (120, 120, 220), 'trefle': (250, 250, 240), 'coeur': (236, 70, 96), 'carreau': (250, 170, 50)}[suit]
    for _ in range(320):
        x, y = int(rng.integers(0, w)), int(rng.integers(130, h))
        c = [(96, 170, 76), (60, 120, 50)][int(rng.integers(0, 2))]
        d.line([(x, y), (x, y - 3)], fill=c)
    for _ in range(90):
        x, y = int(rng.integers(0, w)), int(rng.integers(140, h))
        d.point([(x, y)], fill=flower)
        d.point([(x + 1, y)], fill=(255, 236, 120))
    # coins assombris
    a = np.array(im).astype(float)
    yy, xx = np.mgrid[0:h, 0:w]
    dist = np.sqrt(((xx - w / 2) / (w / 2)) ** 2 + ((yy - h * 0.45) / (h * 0.9)) ** 2)
    a[:, :, :3] *= np.clip(1.15 - 0.35 * dist, 0.55, 1)[:, :, None]
    return Image.fromarray(a.astype(np.uint8)).convert('RGB')


# ---------------------------------------------------------------------------- volumes sur la table

WOOD, WOODD, WOODL = (150, 96, 52), (98, 60, 32), (182, 124, 70)
RED, REDD, CREAM, CREAMD = (204, 44, 52), (150, 28, 40), (242, 230, 206), (206, 190, 164)
GOODS = [(214, 52, 52), (246, 196, 60), (70, 170, 76), (236, 128, 44), (150, 70, 170)]


def stall_front():
    """Devant du comptoir : caisse en planches, montants aux coins."""
    w, h = 64, 22
    im = Image.new('RGBA', (w, h), WOOD + (255,))
    d = ImageDraw.Draw(im)
    for y in (7, 14):
        d.line([(0, y), (w, y)], fill=WOODD)
    for y in (1, 8, 15):
        d.line([(4, y), (w - 5, y)], fill=WOODL)
    for x0 in (0, w - 5):
        d.rectangle([x0, 0, x0 + 4, h - 1], fill=WOODD)
        d.line([(x0 + 1, 1), (x0 + 1, h - 2)], fill=WOOD)
    for x in (10, 32, 53):
        for y in (4, 11, 18):
            d.point((x, y), fill=(60, 36, 20))
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    return im


def stall_top():
    """Dessus du comptoir garni : planches et paniers de fruits vus d'en haut."""
    w, h = 64, 20
    im = Image.new('RGBA', (w, h), WOODL + (255,))
    d = ImageDraw.Draw(im)
    for y in (6, 13):
        d.line([(0, y), (w, y)], fill=WOOD)
    for i, c in enumerate(GOODS):
        x0 = 4 + i * 12
        d.rectangle([x0, 4, x0 + 9, 14], fill=WOODD)
        for fx, fy in ((x0 + 2, 6), (x0 + 6, 6), (x0 + 4, 10), (x0 + 2, 11), (x0 + 6, 11)):
            d.rectangle([fx, fy, fx + 2, fy + 2], fill=c)
            d.point((fx, fy), fill=tuple(min(255, v + 60) for v in c))
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    return im


def stall_goods():
    """Marchandises debout sur le comptoir : bocaux, bouteilles, pyramides de fruits."""
    w, h = 64, 14
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    x = 3
    for i, c in enumerate(GOODS * 2):
        kind = i % 3
        light = tuple(min(255, v + 70) for v in c)
        if kind == 0:  # bocal
            d.rectangle([x, 5, x + 5, 12], fill=c)
            d.rectangle([x + 1, 3, x + 4, 4], fill=WOODD)
            d.line([(x + 1, 6), (x + 1, 10)], fill=light)
            x += 8
        elif kind == 1:  # bouteille
            d.rectangle([x, 6, x + 3, 12], fill=c)
            d.rectangle([x + 1, 2, x + 2, 5], fill=c)
            d.point((x + 1, 7), fill=light)
            x += 6
        else:  # pyramide de fruits
            for fx, fy in ((x, 9), (x + 3, 9), (x + 1, 6)):
                d.rectangle([fx, fy, fx + 2, fy + 2], fill=c)
                d.point((fx, fy), fill=light)
            x += 7
        if x > w - 8:
            break
    return outline(im, 2)


def stall_awning():
    """Toile de l'auvent, rayée rouge et crème (le bas de l'image est le bord avant)."""
    w, h = 32, 24
    im = Image.new('RGBA', (w, h))
    for y in range(h):
        for x in range(w):
            red = (x // 4) % 2 == 0
            c = RED if red else CREAM
            if y > h - 4 or x % 4 == 3:
                c = REDD if red else CREAMD
            im.putpixel((x, y), c + (255,))
    return im


def stall_valance():
    """Lambrequin, le bord festonné qui pend devant l'auvent."""
    w, h = 32, 10
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    for x in range(w):
        red = (x // 4) % 2 == 0
        c = RED if red else CREAM
        k = x % 4
        bottom = h - 3 if k in (1, 2) else h - 5  # de la place en bas pour le contour
        for y in range(0, bottom + 1):
            im.putpixel((x, y), (REDD if (red and y == bottom) else (CREAMD if y == bottom else c)) + (255,))
    return outline(im, 2)


def sign_board():
    """Pancarte des piles : planche de bois sombre clouée, étirée par le jeu (bords de 3 pixels fixes)."""
    w, h = 12, 10
    im = Image.new('RGBA', (w, h), (92, 56, 30, 255))
    d = ImageDraw.Draw(im)
    d.line([(1, 1), (w - 2, 1)], fill=(132, 86, 48))
    d.line([(1, h // 2), (w - 2, h // 2)], fill=(68, 40, 22))
    for x, y in ((2, 2), (w - 3, 2), (2, h - 3), (w - 3, h - 3)):
        d.point((x, y), fill=(214, 190, 128))
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    return im


# ---------------------------------------------------------------------------- machines à sous

GOLD, GOLDD, GOLDL = (230, 180, 34), (168, 116, 26), (255, 226, 120)
MACHINE_COLORS = {'player': (156, 28, 44), 'enemy': (74, 42, 106)}


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c)


def machine_body(color):
    """Caisse de la machine (étirée par le jeu, bords de 5 pixels fixes) : reflet à gauche, ombre à droite, rivets."""
    w = h = 16
    im = Image.new('RGBA', (w, h), color + (255,))
    d = ImageDraw.Draw(im)
    d.rectangle([1, 1, 2, h - 2], fill=shade(color, 1.35))
    d.rectangle([w - 3, 1, w - 2, h - 2], fill=shade(color, 0.7))
    d.line([(1, h - 2), (w - 2, h - 2)], fill=shade(color, 0.6))
    d.line([(3, 1), (w - 4, 1)], fill=shade(color, 1.2))
    for x, y in ((3, 3), (w - 5, 3), (3, h - 5), (w - 5, h - 5)):
        d.point((x, y), fill=GOLDL)
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    return im


def machine_top(color):
    """Dessus de la caisse (le haut de l'image est le fond) : rebord doré, grilles d'aération, rivets."""
    w, h = 40, 16
    im = Image.new('RGBA', (w, h), shade(color, 1.15) + (255,))
    d = ImageDraw.Draw(im)
    d.rectangle([1, 1, w - 2, h - 2], outline=GOLD)
    d.line([(2, h - 3), (w - 3, h - 3)], fill=GOLDD)
    for x in range(8, w - 8, 3):
        d.line([(x, 5), (x, h - 6)], fill=shade(color, 0.6))
    for x, y in ((3, 3), (w - 4, 3), (3, h - 4), (w - 4, h - 4)):
        d.point((x, y), fill=GOLDL)
    return im


def machine_side(color):
    """Flanc de la caisse (le haut de l'image est le haut) : bande dorée en haut, rivets."""
    w, h = 16, 40
    im = Image.new('RGBA', (w, h), shade(color, 0.85) + (255,))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, w - 1, 3], fill=GOLD)
    d.line([(0, 3), (w - 1, 3)], fill=GOLDD)
    d.line([(0, h - 2), (w - 1, h - 2)], fill=shade(color, 0.55))
    for y in (8, h - 6):
        d.point((3, y), fill=GOLDL)
        d.point((w - 4, y), fill=GOLDL)
    return im


def machine_marquee():
    """Fronton : panneau sombre cerclé d'or où clignotent les ampoules (bords de 4 pixels fixes)."""
    w = h = 12
    im = Image.new('RGBA', (w, h), (34, 20, 26, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, w - 1, h - 1], fill=GOLD)
    d.line([(1, h - 2), (w - 2, h - 2)], fill=GOLDD)
    d.line([(w - 2, 1), (w - 2, h - 2)], fill=GOLDD)
    d.line([(1, 1), (w - 2, 1)], fill=GOLDL)
    d.rectangle([3, 3, w - 4, h - 4], fill=(48, 22, 30))
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    d.rectangle([2, 2, w - 3, h - 3], outline=OUTLINE)
    return im


def bulb(on):
    im = Image.new('RGBA', (4, 4), (0, 0, 0, 0))
    c, l = ((255, 242, 168), (255, 255, 236)) if on else ((201, 134, 42), (226, 170, 80))
    for x, y in ((1, 0), (2, 0), (0, 1), (3, 1), (0, 2), (3, 2), (1, 3), (2, 3)):
        im.putpixel((x, y), OUTLINE)
    for x, y in ((1, 1), (2, 1), (1, 2), (2, 2)):
        im.putpixel((x, y), c + (255,))
    im.putpixel((1, 1), l + (255,))
    return im


def machine_crest(color):
    """Fronton arrondi au sommet de la caisse, une étoile dorée au milieu."""
    w, h = 40, 14
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([0, 0, w - 1, 2 * h], fill=GOLD)
    d.ellipse([2, 2, w - 3, 2 * h - 2], fill=color)
    d.ellipse([4, 4, w - 5, 2 * h - 4], fill=shade(color, 0.75))
    cx, cy = w // 2, 8
    star = [(cx, cy - 5), (cx + 1, cy - 2), (cx + 4, cy - 2), (cx + 2, cy), (cx + 3, cy + 3), (cx, cy + 1),
            (cx - 3, cy + 3), (cx - 2, cy), (cx - 4, cy - 2), (cx - 1, cy - 2)]
    d.polygon(star, fill=GOLDL, outline=GOLD)
    return outline(im)


def machine_tray():
    """Bac à pièces, en bas de la caisse."""
    w, h = 30, 7
    im = Image.new('RGBA', (w, h), GOLD + (255,))
    d = ImageDraw.Draw(im)
    d.line([(0, 0), (w - 1, 0)], fill=GOLDL)
    d.line([(0, h - 1), (w - 1, h - 1)], fill=GOLDD)
    d.rectangle([3, 2, w - 4, h - 3], fill=(20, 10, 14))
    d.line([(4, 3), (w - 5, 3)], fill=(60, 36, 30))
    d.rectangle([0, 0, w - 1, h - 1], outline=OUTLINE)
    return im


def main():
    rat_dir = os.path.join(ROOT, 'rat_cales')
    os.makedirs(rat_dir, exist_ok=True)
    decor_cale().save(os.path.join(ROOT, 'decor_cale.png'))
    parts = {'body': rat_body(), 'head': rat_head(), 'swing': rat_tail(), 'paw': rat_paw()}
    for name, img in parts.items():
        img = thick(img)
        img.save(os.path.join(rat_dir, name + '.png'))
        flash(img).save(os.path.join(rat_dir, name + '_flash.png'))
    rat_eyelid().save(os.path.join(rat_dir, 'eyelid.png'))
    for suit in SUITS:
        decor_prairie(suit).save(os.path.join(ROOT, 'decor_prairie_' + suit + '.png'))
        for who, parts in (('soldat', soldier_parts(suit)), ('roi', king_parts(suit))):
            folder = os.path.join(ROOT, 'prairie', who + '_' + suit)
            os.makedirs(folder, exist_ok=True)
            for name, img in parts.items():
                if name != 'eyelid':
                    img = thick(img)
                img.save(os.path.join(folder, name + '.png'))
                if name != 'eyelid':
                    flash(img).save(os.path.join(folder, name + '_flash.png'))
    props_dir = os.path.join(ROOT, 'props')
    os.makedirs(props_dir, exist_ok=True)
    for name, img in {'stall_front': stall_front(), 'stall_top': stall_top(),
                      'stall_goods': stall_goods(), 'stall_awning': stall_awning(),
                      'stall_valance': stall_valance(), 'sign_board': sign_board()}.items():
        img.save(os.path.join(props_dir, name + '.png'))
    machine_dir = os.path.join(ROOT, 'machine')
    os.makedirs(machine_dir, exist_ok=True)
    parts = {'marquee': machine_marquee(), 'bulb_on': bulb(True), 'bulb_off': bulb(False), 'tray': machine_tray()}
    for who, color in MACHINE_COLORS.items():
        parts['body_' + who] = machine_body(color)
        parts['crest_' + who] = machine_crest(color)
        parts['top_' + who] = machine_top(color)
        parts['side_' + who] = machine_side(color)
    for name, img in parts.items():
        img.save(os.path.join(machine_dir, name + '.png'))


if __name__ == '__main__':
    main()
