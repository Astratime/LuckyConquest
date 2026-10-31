"""
Grands dessins de la vue de face pour les ennemis des lieux (même style que le
Rat des cales et la Prairie : pixel art détaillé, contour épais de 2 pixels).

Chaque ennemi est en pièces détachées, dans assets/front/enemies/<ennemi>/ :
body, head, eyelid, swing (ce qui se balance : objet tenu, plume, queue...) et
paw (main posée sur le rebord), avec leurs silhouettes blanches *_flash. Le pied
de la pièce « swing » et son balancement sont recopiés dans FrontScene.BIG_ART.

Les dessins reprennent le portrait de chaque ennemi (vue classique), en grand.

Lancer depuis la racine du dépôt : python3 tools/front/generate_big_enemies.py
"""
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generate_front_art import (outline, thick, flash, canvas, shade, glove, ROOT, CANVAS,  # noqa: E402
                                OUTLINE)

W, H = CANVAS
CX = 65
EYE = (26, 16, 24)
WHITE = (250, 246, 240)
HAND = (114, 108)  # pied d'un objet tenu dans la main droite (FrontScene.BigArt.held)


def skin_tones(base):
    return base, shade(base, 0.8), tuple(min(255, int(v * 1.08) + 8) for v in base)


def head_shape(d, cy, skin, rx=18, ry=19, ears=True):
    s, sd, sl = skin_tones(skin)
    if ears:
        for ex in (CX - rx - 2, CX + rx + 2):
            d.ellipse([ex - 4, cy - 4, ex + 4, cy + 7], fill=sd)
    d.ellipse([CX - rx, cy - ry, CX + rx, cy + ry], fill=sd)
    d.ellipse([CX - rx, cy - ry, CX + rx - 5, cy + ry - 3], fill=s)
    d.ellipse([CX - rx + 4, cy - ry + 4, CX - 3, cy - 3], fill=sl)


def eyes(d, cy, iris=EYE, white=WHITE, wink=False, size=3):
    for k, ex in enumerate((CX - 9, CX + 9)):
        if wink and k == 1:
            d.line([(ex - 3, cy + 2), (ex + 2, cy + 2)], fill=EYE, width=2)
            continue
        d.rectangle([ex - size, cy, ex + size - 1, cy + 4], fill=white)
        d.rectangle([ex - 1, cy + 1, ex + 1, cy + 4], fill=iris)
        d.point([(ex - 1, cy + 1)], fill=(255, 255, 255))


def nose(d, cy, skin, color=None):
    if color:
        d.ellipse([CX - 4, cy + 5, CX + 4, cy + 12], fill=color)
        d.point([(CX - 2, cy + 7)], fill=(255, 230, 230))
    else:
        d.polygon([(CX - 1, cy + 5), (CX + 3, cy + 10), (CX - 3, cy + 11)], fill=shade(skin, 0.8))


def lids(cy, skin, wink=False):
    im, d = canvas()
    for k, ex in enumerate((CX - 9, CX + 9)):
        if wink and k == 1:
            continue
        d.rectangle([ex - 4, cy - 1, ex + 3, cy + 5], fill=skin)
        d.line([(ex - 3, cy + 3), (ex + 2, cy + 3)], fill=EYE)
    return im


def torso(d, color, top=66, neck=None):
    """Buste aux épaules arrondies, jusqu'au bas du cadre ; ombre à droite."""
    if neck:
        d.rectangle([CX - 8, top - 10, CX + 8, top + 2], fill=neck)
    d.rounded_rectangle([20, top, 110, H + 10], 14, fill=color)
    d.rounded_rectangle([CX + 8, top, 110, H + 10], 14, fill=shade(color, 0.78))
    d.rectangle([CX + 8, top, CX + 20, H], fill=shade(color, 0.78))


def arms(d, sleeve, cuff=None):
    """Les bras qui descendent vers les mains posées sur le rebord."""
    d.polygon([(22, 76), (42, 80), (36, H), (8, H)], fill=sleeve)
    d.polygon([(22, 76), (28, 77), (16, H), (8, H)], fill=shade(sleeve, 1.15))
    d.polygon([(108, 76), (88, 80), (94, H), (122, H)], fill=shade(sleeve, 0.78))
    if cuff:
        d.rectangle([9, H - 9, 36, H - 4], fill=cuff)
        d.rectangle([94, H - 9, 121, H - 4], fill=shade(cuff, 0.8))


def stripes(d, x0, y0, x1, y1, color, step=6, width=3):
    for y in range(y0, y1, step):
        d.rectangle([x0, y, x1, y + width - 1], fill=color)


def save(name, parts):
    folder = os.path.join(ROOT, 'enemies', name)
    os.makedirs(folder, exist_ok=True)
    for part, img in parts.items():
        if part != 'eyelid':
            img = thick(img if part == 'paw' else outline(img))
        img.save(os.path.join(folder, part + '.png'))
        if part != 'eyelid':
            flash(img).save(os.path.join(folder, part + '_flash.png'))


def held(draw):
    """Objet tenu debout dans la main droite (pièce « swing ») : {draw}(d) le dessine."""
    im, d = canvas()
    draw(d, im)
    return im


# ---------------------------------------------------------------------------- la Foire

def clown():
    cy = 44
    skin = (248, 244, 240)
    body, d = canvas()
    torso(d, (240, 170, 40), neck=skin)
    for x in range(30, 104, 12):
        for y in range(80, H, 12):
            d.ellipse([x, y, x + 4, y + 4], fill=(60, 110, 220))
    arms(d, (240, 170, 40), (250, 250, 250))
    for k in range(9):  # collerette
        x = 30 + k * 9
        d.polygon([(x, 64), (x + 10, 64), (x + 5, 76)], fill=(250, 250, 250) if k % 2 else (230, 60, 80))
    head, d = canvas()
    for ex in (CX - 22, CX + 22):  # cheveux rouges en boules
        d.ellipse([ex - 13, cy - 24, ex + 13, cy + 2], fill=(226, 50, 40))
        d.ellipse([ex - 9, cy - 20, ex + 3, cy - 10], fill=(250, 100, 80))
    head_shape(d, cy, skin, ears=False)
    for ex in (CX - 9, CX + 9):
        d.polygon([(ex - 4, cy - 6), (ex + 3, cy - 6), (ex, cy - 1)], fill=(60, 110, 220))
    eyes(d, cy)
    nose(d, cy, skin, (230, 30, 40))
    d.chord([CX - 12, cy + 6, CX + 12, cy + 20], 0, 180, fill=(220, 40, 60))
    d.line([(CX - 8, cy + 14), (CX + 8, cy + 14)], fill=(250, 250, 250))
    d.polygon([(CX - 8, cy - 18), (CX + 8, cy - 18), (CX, cy - 34)], fill=(60, 110, 220))  # petit chapeau
    d.ellipse([CX - 3, cy - 38, CX + 3, cy - 32], fill=(250, 210, 40))
    swing = held(lambda d, im: (
        d.line([HAND, (HAND[0] - 2, 40)], fill=(200, 200, 200)),
        d.ellipse([HAND[0] - 12, 12, HAND[0] + 8, 40], fill=(230, 40, 60)),
        d.ellipse([HAND[0] - 8, 16, HAND[0] - 2, 24], fill=(255, 140, 150)),
        d.polygon([(HAND[0] - 4, 40), (HAND[0], 40), (HAND[0] - 2, 44)], fill=(200, 30, 50))))
    return {'body': body, 'head': head, 'swing': swing, 'eyelid': lids(cy, skin),
            'paw': glove((250, 250, 250), (255, 255, 255), (210, 210, 220))}


def squelette():
    cy = 40
    bone, bone_d = (236, 232, 214), (190, 184, 164)
    body, d = canvas()
    d.rectangle([CX - 3, 56, CX + 3, H], fill=bone)  # colonne
    for k, y in enumerate(range(68, 108, 8)):  # côtes
        w = 34 - k * 3
        d.rounded_rectangle([CX - w, y, CX + w, y + 4], 2, fill=bone)
        d.rectangle([CX + 4, y, CX + w, y + 4], fill=bone_d)
    d.rounded_rectangle([CX - 40, 60, CX + 40, 66], 3, fill=bone)  # clavicules
    for x0, x1 in ((24, 12), (106, 118)):  # bras en os
        d.line([(x0, 64), (x1, H)], fill=bone, width=6)
        d.line([(x0, 64), (x1, H)], fill=bone_d, width=2)
    head, d = canvas()
    d.ellipse([CX - 20, cy - 24, CX + 20, cy + 14], fill=bone)
    d.ellipse([CX - 20, cy - 24, CX + 14, cy + 10], fill=(246, 242, 228))
    d.rounded_rectangle([CX - 12, cy + 8, CX + 12, cy + 22], 3, fill=bone)
    for x in range(CX - 10, CX + 11, 4):
        d.line([(x, cy + 12), (x, cy + 21)], fill=EYE)
    for ex in (CX - 9, CX + 9):  # orbites, yeux verts
        d.ellipse([ex - 7, cy - 6, ex + 6, cy + 6], fill=EYE)
        d.rectangle([ex - 2, cy - 1, ex + 1, cy + 2], fill=(100, 250, 120))
    d.polygon([(CX - 3, cy + 6), (CX + 3, cy + 6), (CX, cy + 1)], fill=EYE)
    d.line([(CX + 6, cy - 22), (CX + 12, cy - 12)], fill=bone_d)  # fêlure
    plume, d = canvas()
    pts = [(CX + 8 + k, cy - 22 - k * 1.2) for k in range(18)]
    for i, (x, y) in enumerate(pts):
        r = 2 + int(3 * math.sin(i / 18 * math.pi))
        d.ellipse([x - r, y - r, x + r, y + r], fill=(240, 110, 150))
    d.line(pts, fill=(250, 180, 200))
    lid, d = canvas()  # les orbites se ferment : un trait
    for ex in (CX - 9, CX + 9):
        d.ellipse([ex - 7, cy - 6, ex + 6, cy + 6], fill=bone)
        d.line([(ex - 5, cy), (ex + 4, cy)], fill=EYE, width=2)
    return {'body': body, 'head': head, 'swing': plume, 'eyelid': lid,
            'paw': glove(bone, (250, 248, 236), bone_d)}


def forain():
    cy = 44
    skin = (176, 118, 74)
    body, d = canvas()
    torso(d, (240, 240, 236), neck=shade(skin, 0.85))
    stripes(d, 20, 72, 110, H, (40, 70, 150), 8, 4)
    arms(d, (240, 240, 236))
    stripes(d, 8, 82, 42, H, (40, 70, 150), 8, 3)
    stripes(d, 88, 82, 122, H, (30, 54, 120), 8, 3)
    d.polygon([(CX - 10, 64), (CX + 10, 64), (CX, 76)], fill=shade(skin, 0.85))
    head, d = canvas()
    head_shape(d, cy, skin)
    eyes(d, cy)
    nose(d, cy, skin)
    d.rectangle([CX - 12, cy + 11, CX + 12, cy + 14], fill=(50, 34, 26))  # moustache
    d.polygon([(CX - 12, cy + 11), (CX - 18, cy + 17), (CX - 12, cy + 14)], fill=(50, 34, 26))
    d.polygon([(CX + 12, cy + 11), (CX + 18, cy + 17), (CX + 12, cy + 14)], fill=(50, 34, 26))
    d.line([(CX - 5, cy + 17), (CX + 5, cy + 17)], fill=(130, 60, 40))
    d.chord([CX - 22, cy - 30, CX + 22, cy - 2], 180, 360, fill=(110, 96, 80))  # casquette plate
    d.rectangle([CX - 24, cy - 16, CX + 26, cy - 11], fill=(90, 78, 64))
    d.polygon([(CX + 10, cy - 11), (CX + 30, cy - 11), (CX + 24, cy - 6)], fill=(80, 68, 56))

    def mallet(d, im):  # maillet du « tape-fort »
        x, y = HAND
        d.rectangle([x - 2, 30, x + 2, y], fill=(170, 120, 70))
        d.rounded_rectangle([x - 14, 14, x + 14, 32], 4, fill=(200, 40, 50))
        d.rectangle([x - 14, 20, x + 14, 24], fill=(250, 220, 80))
    return {'body': body, 'head': head, 'swing': held(mallet), 'eyelid': lids(cy, skin),
            'paw': glove(shade(skin, 0.95), skin_tones(skin)[2], shade(skin, 0.75))}


def bonimenteur():
    cy = 44
    skin = (240, 196, 156)
    body, d = canvas()
    torso(d, (230, 110, 40), neck=skin)
    for x in range(26, 108, 10):
        d.rectangle([x, 70, x + 3, H], fill=(250, 170, 80))
    d.polygon([(CX - 12, 64), (CX + 12, 64), (CX + 6, 90), (CX - 6, 90)], fill=(250, 246, 236))  # plastron
    d.polygon([(CX - 12, 66), (CX, 72), (CX - 12, 78)], fill=(210, 30, 40))  # noeud papillon
    d.polygon([(CX + 12, 66), (CX, 72), (CX + 12, 78)], fill=(210, 30, 40))
    d.rectangle([CX - 2, 70, CX + 2, 74], fill=(160, 20, 30))
    arms(d, (230, 110, 40), (250, 246, 236))
    head, d = canvas()
    head_shape(d, cy, skin)
    eyes(d, cy, wink=True)
    nose(d, cy, skin)
    d.chord([CX - 10, cy + 10, CX + 12, cy + 20], 0, 180, fill=(130, 50, 40))
    d.line([(CX - 8, cy + 15), (CX + 10, cy + 15)], fill=(250, 250, 250))
    d.rectangle([CX - 26, cy - 18, CX + 26, cy - 14], fill=(230, 200, 110))  # canotier
    d.rectangle([CX - 18, cy - 30, CX + 18, cy - 18], fill=(240, 214, 130))
    d.rectangle([CX - 18, cy - 24, CX + 18, cy - 20], fill=(200, 40, 50))

    def cane(d, im):
        x, y = HAND
        d.rectangle([x - 2, 20, x + 1, y], fill=(60, 30, 20))
        d.arc([x - 2, 10, x + 14, 26], 180, 360, fill=(60, 30, 20), width=4)
        d.rectangle([x - 3, 26, x + 2, 30], fill=(240, 200, 60))
    return {'body': body, 'head': head, 'swing': held(cane), 'eyelid': lids(cy, skin, wink=True),
            'paw': glove((250, 246, 236), (255, 255, 255), (210, 204, 196))}


# ---------------------------------------------------------------------------- le Port

def buveur():
    cy = 44
    skin = (240, 196, 160)
    body, d = canvas()
    torso(d, (230, 220, 196), neck=skin)
    d.polygon([(CX - 10, 64), (CX + 10, 64), (CX, 84)], fill=skin)
    d.line([(CX - 10, 64), (CX, 84), (CX + 10, 64)], fill=(190, 176, 150), width=2)
    arms(d, (230, 220, 196))
    d.rectangle([20, 100, 110, 106], fill=(110, 70, 40))
    head, d = canvas()
    for k in range(9):  # cheveux en bataille
        x = CX - 22 + k * 5
        d.polygon([(x, cy - 12), (x + 8, cy - 12), (x + 3, cy - 26 - (k % 3) * 3)], fill=(110, 70, 40))
    head_shape(d, cy, skin)
    d.ellipse([CX - 18, cy - 21, CX + 18, cy - 8], fill=(110, 70, 40))
    eyes(d, cy + 1, size=3)
    d.line([(CX - 13, cy - 1), (CX - 5, cy + 1)], fill=(80, 50, 30), width=2)
    d.line([(CX + 13, cy - 1), (CX + 5, cy + 1)], fill=(80, 50, 30), width=2)
    for ex in (CX - 13, CX + 13):  # joues rouges
        d.ellipse([ex - 4, cy + 7, ex + 4, cy + 12], fill=(236, 120, 110))
    nose(d, cy, skin, (220, 70, 70))
    for x in range(CX - 12, CX + 13, 3):  # barbe de trois jours
        for y in range(cy + 13, cy + 19, 3):
            d.point([(x + (y % 2), y)], fill=(130, 100, 80))
    d.line([(CX - 5, cy + 15), (CX + 5, cy + 14)], fill=(130, 60, 40), width=2)

    def mug(d, im):
        x, y = HAND
        d.rectangle([x - 10, y - 36, x + 6, y - 4], fill=(170, 120, 60))
        d.rectangle([x - 8, y - 32, x + 4, y - 8], fill=(240, 180, 50))
        d.ellipse([x - 13, y - 44, x + 9, y - 32], fill=(250, 248, 236))
        d.arc([x + 2, y - 30, x + 14, y - 12], 270, 90, fill=(170, 120, 60), width=3)
        for k in range(3):
            d.rectangle([x - 10, y - 30 + k * 9, x + 6, y - 29 + k * 9], fill=(130, 86, 40))
    return {'body': body, 'head': head, 'swing': held(mug), 'eyelid': lids(cy + 1, skin),
            'paw': glove(skin, skin_tones(skin)[2], shade(skin, 0.78))}


def pirate():
    cy = 44
    skin = (232, 186, 146)
    body, d = canvas()
    torso(d, (240, 236, 226), neck=skin)
    stripes(d, 20, 72, 110, H, (200, 40, 50), 9, 4)
    arms(d, (240, 236, 226))
    stripes(d, 8, 82, 42, H, (200, 40, 50), 9, 3)
    stripes(d, 88, 82, 122, H, (160, 30, 40), 9, 3)
    d.line([(26, 70), (104, 104)], fill=(80, 50, 30), width=5)  # baudrier
    d.rectangle([62, 82, 70, 90], fill=(240, 200, 60))
    head, d = canvas()
    head_shape(d, cy, skin)
    eyes(d, cy)
    d.line([(CX - 22, cy - 8), (CX + 20, cy - 2)], fill=EYE, width=2)  # cache-oeil
    d.ellipse([CX - 15, cy - 3, CX - 3, cy + 7], fill=EYE)
    nose(d, cy, skin)
    d.chord([CX - 20, cy + 2, CX + 20, cy + 32], 0, 180, fill=(40, 30, 28))  # barbe
    d.rectangle([CX - 20, cy + 10, CX + 20, cy + 17], fill=(40, 30, 28))
    d.line([(CX - 5, cy + 14), (CX + 5, cy + 14)], fill=(150, 60, 50), width=2)
    d.chord([CX - 22, cy - 28, CX + 22, cy + 2], 180, 360, fill=(200, 40, 50))  # bandana
    d.polygon([(CX + 18, cy - 14), (CX + 34, cy - 18), (CX + 28, cy - 6)], fill=(170, 30, 40))
    d.ellipse([CX + 18, cy + 6, CX + 24, cy + 12], outline=(240, 200, 60), width=2)

    def cutlass(d, im):
        x, y = HAND
        d.rectangle([x - 2, y - 16, x + 2, y], fill=(80, 50, 30))
        d.rectangle([x - 8, y - 18, x + 8, y - 15], fill=(240, 200, 60))
        d.polygon([(x - 3, y - 18), (x + 3, y - 18), (x + 8, 30), (x + 2, 16), (x - 4, 30)], fill=(200, 206, 216))
        d.line([(x - 1, y - 20), (x + 2, 24)], fill=(240, 244, 250))
    return {'body': body, 'head': head, 'swing': held(cutlass), 'eyelid': lids(cy, skin),
            'paw': glove(skin, skin_tones(skin)[2], shade(skin, 0.78))}


# ---------------------------------------------------------------------------- les Mines

def chercheur():
    cy = 46
    skin = (240, 200, 160)
    body, d = canvas()
    torso(d, (220, 200, 160), neck=skin)
    d.rectangle([34, 76, 96, H], fill=(60, 90, 160))  # salopette
    d.rectangle([34, 66, 42, 78], fill=(60, 90, 160))
    d.rectangle([88, 66, 96, 78], fill=(50, 76, 140))
    d.rectangle([48, 86, 82, 100], fill=(50, 76, 140))
    for x, y in ((52, 92), (60, 96), (70, 93), (78, 97)):  # pépites dans la poche
        d.rectangle([x, y, x + 3, y + 2], fill=(250, 210, 60))
    arms(d, (220, 200, 160))
    head, d = canvas()
    head_shape(d, cy, skin)
    d.rectangle([CX - 16, cy - 8, CX + 16, cy - 2], fill=(240, 210, 120))  # mèche blonde
    eyes(d, cy)
    nose(d, cy, skin)
    d.line([(CX - 5, cy + 14), (CX + 5, cy + 14)], fill=(150, 60, 50), width=2)
    for x, y in ((CX - 14, cy + 8), (CX + 12, cy + 4), (CX + 8, cy + 12)):  # salissures
        d.rectangle([x, y, x + 2, y + 2], fill=(120, 100, 80))
    d.chord([CX - 24, cy - 30, CX + 24, cy + 4], 180, 360, fill=(240, 196, 40))  # casque
    d.rectangle([CX - 26, cy - 13, CX + 26, cy - 9], fill=(210, 160, 30))
    d.rectangle([CX - 6, cy - 30, CX + 6, cy - 20], fill=(150, 150, 160))
    d.rectangle([CX - 4, cy - 28, CX + 4, cy - 22], fill=(255, 250, 200))

    def pickaxe(d, im):
        x, y = HAND
        d.rectangle([x - 2, 26, x + 2, y], fill=(150, 100, 56))
        d.polygon([(x - 22, 30), (x, 18), (x + 22, 30), (x + 22, 26), (x, 12), (x - 22, 26)], fill=(170, 176, 186))
        d.line([(x - 20, 27), (x, 15)], fill=(220, 226, 236))
    return {'body': body, 'head': head, 'swing': held(pickaxe), 'eyelid': lids(cy, skin),
            'paw': glove((150, 110, 70), (180, 140, 96), (110, 76, 46))}


def forgeron():
    cy = 44
    skin = (200, 146, 104)
    body, d = canvas()
    torso(d, skin, neck=shade(skin, 0.85))
    d.line([(CX - 6, 78), (CX - 6, 96)], fill=shade(skin, 0.8))  # muscles
    d.line([(CX + 6, 78), (CX + 6, 96)], fill=shade(skin, 0.8))
    d.polygon([(34, 82), (96, 82), (100, H), (30, H)], fill=(110, 70, 40))  # tablier de cuir
    d.line([(34, 82), (20, 66)], fill=(90, 56, 30), width=3)
    d.line([(96, 82), (110, 66)], fill=(90, 56, 30), width=3)
    d.rectangle([50, 92, 80, 104], fill=(96, 60, 34))
    arms(d, skin)
    head, d = canvas()
    head_shape(d, cy, skin)
    d.chord([CX - 19, cy - 22, CX + 19, cy - 2], 180, 360, fill=(40, 30, 26))  # cheveux
    eyes(d, cy)
    d.line([(CX - 14, cy - 4), (CX - 4, cy - 1)], fill=(40, 30, 26), width=2)
    d.line([(CX + 14, cy - 4), (CX + 4, cy - 1)], fill=(40, 30, 26), width=2)
    nose(d, cy, skin)
    d.line([(CX - 6, cy + 15), (CX + 6, cy + 15)], fill=(120, 50, 40), width=2)
    d.rectangle([CX + 10, cy + 6, CX + 14, cy + 8], fill=(60, 50, 50))  # suie

    def hammer(d, im):
        x, y = HAND
        d.rectangle([x - 2, 30, x + 2, y], fill=(120, 80, 44))
        d.rectangle([x - 14, 16, x + 14, 32], fill=(130, 134, 146))
        d.rectangle([x - 14, 16, x + 14, 19], fill=(190, 196, 206))
    return {'body': body, 'head': head, 'swing': held(hammer), 'eyelid': lids(cy, skin),
            'paw': glove((96, 62, 36), (130, 90, 56), (70, 44, 24))}


# ---------------------------------------------------------------------------- le Casino Englouti

def barman():
    cy = 44
    skin = (120, 200, 160)
    body, d = canvas()
    torso(d, (236, 236, 232), neck=skin)
    d.polygon([(CX - 12, 66), (CX, 72), (CX - 12, 78)], fill=EYE)
    d.polygon([(CX + 12, 66), (CX, 72), (CX + 12, 78)], fill=EYE)
    for y in (84, 94, 104):
        d.ellipse([CX - 2, y, CX + 2, y + 4], fill=(60, 60, 70))
    arms(d, (236, 236, 232))
    for x, y in ((30, 90), (96, 82), (80, 104)):  # algues accrochées
        d.line([(x, y), (x + 3, y + 8), (x, y + 14)], fill=(40, 130, 70), width=2)
    head, d = canvas()
    head_shape(d, cy, skin)
    d.chord([CX - 21, cy - 24, CX + 21, cy + 4], 180, 360, fill=(30, 70, 50))  # cheveux mouillés
    for x in (CX - 20, CX + 18):
        d.rectangle([x - 2, cy - 10, x + 2, cy + 10], fill=(30, 70, 50))
    eyes(d, cy, iris=(40, 60, 50), white=(220, 240, 230))
    nose(d, cy, skin)
    d.line([(CX - 6, cy + 15), (CX + 6, cy + 15)], fill=(60, 110, 90), width=2)

    def bottle(d, im):
        x, y = HAND
        d.rectangle([x - 6, y - 40, x + 6, y - 6], fill=(50, 130, 70))
        d.rectangle([x - 3, y - 54, x + 3, y - 40], fill=(50, 130, 70))
        d.rectangle([x - 3, y - 58, x + 3, y - 54], fill=(150, 100, 50))
        d.rectangle([x - 5, y - 30, x + 5, y - 18], fill=(240, 230, 200))
        d.line([(x - 4, y - 38), (x - 4, y - 10)], fill=(120, 200, 140))
        for k in range(3):
            d.ellipse([x + 4 + k * 3, y - 70 - k * 8, x + 8 + k * 3, y - 66 - k * 8], outline=(200, 240, 255))
    return {'body': body, 'head': head, 'swing': held(bottle), 'eyelid': lids(cy, skin),
            'paw': glove(skin, skin_tones(skin)[2], shade(skin, 0.78))}


def bandit_manchot():
    """La machine à sous vivante : la caisse est le corps, les rouleaux sont ses yeux, le levier son bras."""
    wood, wood_d = (150, 80, 50), (110, 56, 34)
    body, d = canvas()
    d.rounded_rectangle([26, 60, 104, H + 8], 6, fill=wood)
    d.rectangle([CX + 14, 60, 104, H], fill=wood_d)
    d.rectangle([40, 70, 90, 80], fill=(30, 20, 20))  # bouche : fente à pièces
    d.rectangle([44, 74, 86, 76], fill=(80, 60, 50))
    d.rectangle([36, 90, 94, 100], fill=(240, 200, 60))  # bac à pièces
    d.rectangle([40, 92, 90, 98], fill=(40, 26, 20))
    arms(d, (120, 120, 132))
    head, d = canvas()
    cy = 40
    d.rounded_rectangle([24, 10, 106, 66], 14, fill=wood)
    d.rounded_rectangle([CX + 14, 10, 106, 66], 14, fill=wood_d)
    d.rectangle([CX + 14, 10, CX + 26, 66], fill=wood_d)
    d.rectangle([32, cy - 14, 98, cy + 12], fill=(30, 20, 20))
    for k in range(3):  # rouleaux-yeux
        x = 36 + k * 21
        d.rectangle([x, cy - 10, x + 17, cy + 8], fill=WHITE)
        d.rectangle([x + 5, cy - 5, x + 12, cy + 3], fill=(210, 30, 40))
    d.rectangle([40, 14, 90, 22], fill=(240, 200, 60))
    lid, d = canvas()
    for k in range(3):
        x = 36 + k * 21
        d.rectangle([x, cy - 10, x + 17, cy + 8], fill=(190, 190, 196))
        d.line([(x + 2, cy - 1), (x + 15, cy - 1)], fill=EYE)

    def lever(d, im):  # le levier, au flanc droit
        d.rectangle([104, 40, 112, 48], fill=(130, 130, 140))
        d.rectangle([108, 10, 112, 44], fill=(180, 180, 190))
        d.ellipse([102, 0, 118, 14], fill=(220, 30, 40))
        d.ellipse([105, 2, 110, 7], fill=(255, 140, 140))
    return {'body': body, 'head': head, 'swing': held(lever), 'eyelid': lid,
            'paw': glove((150, 150, 162), (190, 190, 200), (100, 100, 112))}


# ---------------------------------------------------------------------------- le Désert

def momie():
    cy = 42
    band, band_d = (226, 214, 180), (180, 166, 130)
    body, d = canvas()
    torso(d, band)
    for k, y in enumerate(range(66, H, 7)):
        d.line([(20, y + (k % 2) * 3), (110, y + 4 - (k % 2) * 3)], fill=band_d, width=2)
    arms(d, band)
    for y in range(80, H, 8):
        d.line([(10, y), (40, y + 3)], fill=band_d)
        d.line([(90, y + 3), (120, y)], fill=shade(band_d, 0.8))
    head, d = canvas()
    d.ellipse([CX - 20, cy - 22, CX + 20, cy + 22], fill=band_d)
    d.ellipse([CX - 20, cy - 22, CX + 15, cy + 18], fill=band)
    for k, y in enumerate(range(cy - 20, cy + 22, 6)):
        d.line([(CX - 20, y + (k % 2) * 2), (CX + 20, y + 3 - (k % 2) * 2)], fill=band_d, width=2)
    d.rectangle([CX - 18, cy - 2, CX + 18, cy + 6], fill=(40, 30, 24))  # fente des yeux
    for ex in (CX - 9, CX + 9):
        d.rectangle([ex - 4, cy, ex + 3, cy + 3], fill=(250, 210, 60))
    lid, d = canvas()
    d.rectangle([CX - 18, cy - 2, CX + 18, cy + 6], fill=band)
    d.line([(CX - 16, cy + 2), (CX + 16, cy + 2)], fill=band_d, width=2)
    strip, d = canvas()  # bandelette qui pend et flotte
    pts = [(CX + 16 + k * 1.2, cy + 10 + k * 2.2 + 3 * math.sin(k / 3)) for k in range(20)]
    d.line(pts, fill=band, width=5)
    d.line(pts, fill=band_d, width=1)
    return {'body': body, 'head': head, 'swing': strip, 'eyelid': lid,
            'paw': glove(band, (240, 232, 204), band_d)}


def salamandre():
    cy = 34
    red, red_d, belly = (230, 90, 40), (180, 56, 26), (250, 170, 70)
    body, d = canvas()
    d.rounded_rectangle([28, 56, 102, H + 10], 20, fill=red)
    d.rounded_rectangle([CX + 10, 56, 102, H + 10], 20, fill=red_d)
    d.ellipse([44, 70, 86, H + 20], fill=belly)
    for k in range(5):
        d.line([(48, 76 + k * 8), (82, 76 + k * 8)], fill=(230, 140, 50))
    for x, y in ((34, 70), (96, 80), (40, 96), (92, 100)):  # taches dorées
        d.rectangle([x - 2, y - 2, x + 2, y + 2], fill=(255, 210, 60))
    arms(d, red)
    head, d = canvas()
    d.ellipse([CX - 30, cy - 4, CX + 30, cy + 30], fill=red_d)
    d.ellipse([CX - 30, cy - 4, CX + 24, cy + 26], fill=red)
    for ex in (CX - 16, CX + 16):  # gros yeux sur le dessus
        d.ellipse([ex - 10, cy - 18, ex + 10, cy + 2], fill=red)
        d.ellipse([ex - 7, cy - 15, ex + 7, cy - 1], fill=(250, 220, 50))
        d.rectangle([ex - 1, cy - 13, ex + 1, cy - 3], fill=EYE)
    d.line([(CX - 18, cy + 18), (CX + 18, cy + 18)], fill=red_d, width=2)
    d.rectangle([CX - 2, cy + 19, CX + 2, cy + 30], fill=(240, 100, 140))  # langue
    d.polygon([(CX - 4, cy + 30), (CX, cy + 26), (CX + 4, cy + 30), (CX, cy + 34)], fill=(240, 100, 140))
    for x, y in ((CX - 10, cy + 6), (CX + 6, cy + 10), (CX - 20, cy + 14)):
        d.rectangle([x - 2, y - 2, x + 2, y + 2], fill=(255, 210, 60))
    lid, d = canvas()
    for ex in (CX - 16, CX + 16):
        d.ellipse([ex - 7, cy - 15, ex + 7, cy - 1], fill=red)
        d.line([(ex - 5, cy - 8), (ex + 5, cy - 8)], fill=EYE, width=2)
    tail, d = canvas()  # queue à la pointe en flamme, derrière le corps
    pts = [(96 + k * 1.2, 100 - k * 2.6 - 6 * math.sin(k / 6)) for k in range(28)]
    for i, (x, y) in enumerate(pts):
        r = 6 - i * 0.18
        d.ellipse([x - r, y - r, x + r, y + r], fill=red)
    tx, ty = pts[-1]
    d.polygon([(tx - 6, ty + 2), (tx + 6, ty + 2), (tx + 3, ty - 10), (tx, ty - 16), (tx - 3, ty - 8)], fill=(250, 140, 40))
    d.polygon([(tx - 3, ty + 2), (tx + 3, ty + 2), (tx, ty - 8)], fill=(255, 230, 100))
    return {'body': body, 'head': head, 'swing': tail, 'eyelid': lid,
            'paw': glove(red, (250, 130, 70), red_d)}


ENEMIES = {
    'clown': clown, 'squelette': squelette, 'forain': forain, 'bonimenteur': bonimenteur,
    'buveur': buveur, 'pirate': pirate, 'chercheur_or': chercheur, 'forgeron': forgeron,
    'barman_noye': barman, 'bandit_manchot': bandit_manchot, 'momie': momie, 'salamandre': salamandre,
}


def main():
    for name, draw in ENEMIES.items():
        save(name, draw())


if __name__ == '__main__':
    main()
