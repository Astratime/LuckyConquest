"""
Dessins de la vue de face du combat (Options > Visuel > « Vue de la table : de face »).

Produit, en pixel art à la taille 1 (le jeu les agrandit sans lissage) :
  assets/front/decor_cale.png         le décor de la Cale (Port des Contrebandiers)
  assets/front/decor_prairie_*.png    le décor de la Prairie, un par donjon (château et bannières à sa couleur)
  assets/front/props/*.png            les faces de l'étal de l'échoppe en relief (comptoir, dessus
                                      garni, marchandises, auvent rayé et son lambrequin), et la
                                      pancarte des decks et défausses.
  assets/front/machine/*.png          la machine à sous qui sort de la table (joueur et ennemi) :
                                      caisse et fronton étirables, ampoules, bac, sommet arrondi,
                                      dessus et flancs (la machine est en volume).

Les ennemis gardent leur portrait de la vue classique, agrandi et animé par le
jeu (FrontScene).

Lancer depuis la racine du dépôt : python3 tools/front/generate_front_art.py
"""
import math
import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'assets', 'front')
OUTLINE = (34, 20, 26, 255)


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
    os.makedirs(ROOT, exist_ok=True)
    decor_cale().save(os.path.join(ROOT, 'decor_cale.png'))
    for suit in SUITS:
        decor_prairie(suit).save(os.path.join(ROOT, 'decor_prairie_' + suit + '.png'))
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
