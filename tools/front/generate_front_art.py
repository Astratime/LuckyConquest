"""
Dessins de la vue de face du combat (Options > Visuel > « Vue de la table : de face »).

Produit, en pixel art à la taille 1 (le jeu les agrandit sans lissage) :
  assets/front/decor_cale.png         le décor de la Cale (Port des Contrebandiers)
  assets/front/rat_cales/*.png        le Rat des cales en pièces détachées, animées par le jeu
                                      (corps, tête, paupière, queue, patte) et leurs silhouettes
                                      blanches (*_flash.png) pour le flash quand il est touché.
  assets/front/props/*.png            les faces des volumes posés sur la table : tranches des
                                      piles de cartes, et l'étal de l'échoppe (comptoir, dessus
                                      garni, marchandises, auvent rayé et son lambrequin).

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


def outline(img):
    """Contour sombre d'un pixel autour de la forme."""
    a = np.array(img)
    alpha = a[:, :, 3] > 0
    grown = alpha.copy()
    grown[1:, :] |= alpha[:-1, :]
    grown[:-1, :] |= alpha[1:, :]
    grown[:, 1:] |= alpha[:, :-1]
    grown[:, :-1] |= alpha[:, 1:]
    o = np.zeros_like(a)
    o[grown] = OUTLINE
    out = Image.fromarray(o)
    out.alpha_composite(img)
    return out


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


# ---------------------------------------------------------------------------- volumes sur la table

WOOD, WOODD, WOODL = (150, 96, 52), (98, 60, 32), (182, 124, 70)
RED, REDD, CREAM, CREAMD = (204, 44, 52), (150, 28, 40), (242, 230, 206), (206, 190, 164)
GOODS = [(214, 52, 52), (246, 196, 60), (70, 170, 76), (236, 128, 44), (150, 70, 170)]


def card_edges():
    """Tranche d'une pile : une ligne claire par carte, une ombre entre deux."""
    im = Image.new('RGBA', (4, 64))
    for y in range(64):
        c = (238, 230, 212) if y % 2 == 0 else (168, 158, 140)
        for x in range(4):
            im.putpixel((x, y), c + (255,))
    return im


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
    return outline(im)


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
    w, h = 32, 8
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    for x in range(w):
        red = (x // 4) % 2 == 0
        c = RED if red else CREAM
        k = x % 4
        bottom = h - 1 if k in (1, 2) else h - 3
        for y in range(0, bottom + 1):
            im.putpixel((x, y), (REDD if (red and y == bottom) else (CREAMD if y == bottom else c)) + (255,))
    return outline(im)


def main():
    rat_dir = os.path.join(ROOT, 'rat_cales')
    os.makedirs(rat_dir, exist_ok=True)
    decor_cale().save(os.path.join(ROOT, 'decor_cale.png'))
    parts = {'body': rat_body(), 'head': rat_head(), 'tail': rat_tail(), 'paw': rat_paw()}
    for name, img in parts.items():
        img.save(os.path.join(rat_dir, name + '.png'))
        flash(img).save(os.path.join(rat_dir, name + '_flash.png'))
    rat_eyelid().save(os.path.join(rat_dir, 'eyelid.png'))
    props_dir = os.path.join(ROOT, 'props')
    os.makedirs(props_dir, exist_ok=True)
    for name, img in {'card_edges': card_edges(), 'stall_front': stall_front(), 'stall_top': stall_top(),
                      'stall_goods': stall_goods(), 'stall_awning': stall_awning(),
                      'stall_valance': stall_valance()}.items():
        img.save(os.path.join(props_dir, name + '.png'))


if __name__ == '__main__':
    main()
