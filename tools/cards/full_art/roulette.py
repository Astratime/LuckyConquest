import math
import numpy as np
from PIL import ImageDraw
from px import *


def build():
    img = layer()
    x, y = grid()
    # fond : rayons rouges qui partent du barillet + vignette noire
    cx, cy = 40, 46
    ang = np.arctan2(y - cy, x - cx)
    rays = (np.floor((ang + math.pi) / (2 * math.pi) * 16) % 2) == 0
    dist = np.sqrt((x - cx) ** 2 + ((y - cy) * 0.8) ** 2) / 62
    full = np.ones((H, W), bool)
    img = ramp(img, full & rays, dist, ['#b5232e', '#8a1824', '#5e111b', '#380b12', '#1c060b'], dither=0.3)
    img = ramp(img, full & ~rays, dist + 0.08, ['#7c1520', '#5a0f19', '#3c0a12', '#24070d', '#120307'], dither=0.3)
    d = ImageDraw.Draw(img)

    # volutes de fumée
    from PIL import Image
    smoke = layer()
    sd = ImageDraw.Draw(smoke)
    for (sx, sy, flip) in [(17, 30, 1), (63, 30, -1)]:
        pts = []
        for t in range(0, 26):
            pts.append((sx + flip * (math.sin(t / 4.0) * 4 - t * 0.25), sy - t))
        for i in range(len(pts) - 1):
            w = 3 if i < 12 else 2
            sd.line([pts[i], pts[i + 1]], fill=hexc('#c9bfc8', 190), width=w)
        for i in range(0, len(pts) - 1, 3):
            sd.point(pts[i], fill=hexc('#f2ecf0', 230))
    yy, xx = np.mgrid[0:H, 0:W]

    # barillet
    cyl = layer()
    R = 27
    cyl = sphere(cyl, cx, cy, R, R, ['#eef1f5', '#c3c9d2', '#929aa6', '#636b78', '#3d434e', '#262a32'])
    cd = ImageDraw.Draw(cyl)
    # cannelures entre les chambres, sur le bord
    for k in range(6):
        t = -math.pi / 2 + math.pi / 6 + k * math.pi / 3
        gx, gy = cx + math.cos(t) * (R - 1), cy + math.sin(t) * (R - 1)
        cd.ellipse([gx - 4, gy - 4, gx + 4, gy + 4], fill=(0, 0, 0, 0))
    cyl = outline(cyl, '#120a0e')
    # chambres
    loaded = 0
    for k in range(6):
        t = -math.pi / 2 + k * math.pi / 3
        hx, hy = cx + math.cos(t) * 15, cy + math.sin(t) * 15
        r = 6.2
        cd = ImageDraw.Draw(cyl)
        cd.ellipse([hx - r - 1, hy - r - 1, hx + r, hy + r], fill=hexc('#2a2e36'))
        if k == loaded:
            cyl = sphere(cyl, hx, hy, r - 0.6, r - 0.6, ['#fff3b8', '#f7d27a', '#e0a83a', '#b0752a', '#7a4a16'])
            cd = ImageDraw.Draw(cyl)
            cd.ellipse([hx - 2.2, hy - 2.2, hx + 1.8, hy + 1.8], fill=hexc('#9a5a1e'))
            cd.point((int(hx - 1), int(hy - 1)), fill=hexc('#ffe9a0'))
        else:
            cyl = sphere(cyl, hx + 0.6, hy + 0.6, r - 1.2, r - 1.2, ['#1a1c22', '#0d0e12', '#050507'], light=(0.5, 0.6))
            cd = ImageDraw.Draw(cyl)
            cd.arc([hx - r, hy - r, hx + r - 1, hy + r - 1], 200, 290, fill=hexc('#0b0c10'))
            cd.arc([hx - r, hy - r, hx + r - 1, hy + r - 1], 20, 110, fill=hexc('#aab1bc'))
    # axe central
    cyl = sphere(cyl, cx, cy, 4.5, 4.5, ['#8d95a1', '#545b67', '#2a2e36'])
    cd = ImageDraw.Draw(cyl)
    cd.ellipse([cx - 2, cy - 2, cx + 1, cy + 1], fill=hexc('#0d0e12'))

    # lueur autour de la balle chargée
    glow = layer()
    gx, gy = cx, cy - 15
    g = np.sqrt((x - gx) ** 2 + (y - gy) ** 2)
    ga = np.array(glow)
    ring = (g > 7) & (g < 10.5) & (((xx + yy) % 2) == 0)
    ga[ring] = hexc('#ffd86b', 200)
    img.alpha_composite(Image.fromarray(ga))
    img.alpha_composite(cyl)

    # aiguille de roulette dorée qui désigne la chambre chargée
    d = ImageDraw.Draw(img)
    pin = layer()
    pd = ImageDraw.Draw(pin)
    pd.polygon([(36, 9), (44, 9), (40, 16)], fill=hexc('#f2c14e'))
    pd.line([(37, 10), (40, 13)], fill=hexc('#fff1a8'))
    pd.rectangle([38, 6, 42, 9], fill=hexc('#c8892a'))
    pin = outline(pin, '#120a0e')
    img.alpha_composite(pin)

    # cinq balles couchées sous le barillet
    for i in range(5):
        bx = 14 + i * 11 + (i % 2)
        by = 90 + (2 if i in (0, 4) else (0 if i == 2 else 1))
        b = layer()
        bd = ImageDraw.Draw(b)
        bd.rectangle([bx, by, bx + 5, by + 2], fill=hexc('#e0a83a'))
        bd.line([bx, by, bx + 5, by], fill=hexc('#fff1a8'))
        bd.line([bx, by + 2, bx + 5, by + 2], fill=hexc('#9a5a1e'))
        bd.polygon([(bx + 6, by), (bx + 8, by + 1), (bx + 6, by + 2)], fill=hexc('#b6bcc6'))
        bd.point((bx + 6, by), fill=hexc('#eef1f5'))
        b = outline(b, '#120a0e')
        img.alpha_composite(b)

    # gouttes / impacts
    for (px_, py_) in [(9, 64), (71, 58), (66, 74), (12, 40)]:
        d.point((px_, py_), fill=hexc('#ff5a4a'))
        d.point((px_ + 1, py_), fill=hexc('#c32a2a'))
    sparkle(d, 50, 25, '#fff3b8')

    img = frame(img, '#0a0507', '#f0d9c4')
    return img


if __name__ == '__main__':
    up(build()).save('assets/cards/special_full/russian_roulette.png')
