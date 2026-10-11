"""Briques de dessin des cartes « pleine illustration » (voir px.py pour la grille 80x112)."""
import math
import numpy as np
from PIL import Image, ImageDraw
from px import W, H, hexc, layer, outline, ramp, grid, sphere, frame, up, sparkle

INK = '#140c14'

# Rampes de couleurs (du plus clair au plus sombre)
GOLD = ['#fff6c4', '#f7d76a', '#e0a83a', '#b0752a', '#6e4214']
STEEL = ['#f4f6fa', '#c3c9d2', '#929aa6', '#636b78', '#3d434e']
IRON = ['#9aa2ae', '#6c7480', '#4a515c', '#30353e', '#1c2026']
WOOD = ['#d9a066', '#b5793e', '#8a5528', '#5e3618', '#3a200e']
RED = ['#ffb0a8', '#f05a50', '#c8283a', '#8e1420', '#520a14']
GREEN = ['#c9ffb0', '#6fd860', '#3fae4a', '#23793a', '#124a24']
BLUE = ['#c4e4ff', '#6fb0ff', '#3a6fd8', '#24459a', '#14265a']
PURPLE = ['#ecc8ff', '#c07cf0', '#9a4ad8', '#6a2aa0', '#3a145e']
PAPER = ['#fffaf0', '#f4ead8', '#dcc8a8', '#b39a78', '#7a6448']
SKIN = ['#fff1e0', '#f4d4b4', '#dca884', '#b07a5a', '#6e4632']
BONE = ['#ffffff', '#ece6dc', '#c8bfb2', '#968c80', '#5e564e']
GLASS = ['#ffffff', '#d8f4ff', '#9ad8f0', '#5aa8d0', '#2a6a94']
TEAL = ['#c8fff0', '#5ae0c0', '#20a890', '#127066', '#0a4040']
FIRE = ['#fffbe0', '#ffe07a', '#ffa83a', '#f05a28', '#a82818']
SHADOW = ['#8a7aa8', '#5e4e80', '#3e3058', '#261c3a', '#140e22']
ROCK = ['#b8a898', '#8e7e70', '#6a5c50', '#4a3e36', '#2c2420']


def rot(pts, cx, cy, deg):
    t = math.radians(deg)
    c, s = math.cos(t), math.sin(t)
    return [(cx + (x - cx) * c - (y - cy) * s, cy + (x - cx) * s + (y - cy) * c) for x, y in pts]


class Piece:
    """Un élément dessiné sur son propre calque, puis détouré et posé sur la carte."""

    def __init__(self):
        self.im = layer()

    @property
    def d(self):
        return ImageDraw.Draw(self.im)

    def poly(self, pts, col):
        self.d.polygon([(round(x), round(y)) for x, y in pts], fill=hexc(col))
        return self

    def rect(self, box, col):
        self.d.rectangle(box, fill=hexc(col))
        return self

    def ell(self, box, col):
        self.d.ellipse(box, fill=hexc(col))
        return self

    def line(self, pts, col, w=1):
        self.d.line([(round(x), round(y)) for x, y in pts], fill=hexc(col), width=w)
        return self

    def pt(self, x, y, col):
        self.d.point((round(x), round(y)), fill=hexc(col))
        return self

    def arc(self, box, a0, a1, col, w=1):
        self.d.arc(box, a0, a1, fill=hexc(col), width=w)
        return self

    def sph(self, cx, cy, rx, ry, cols, light=(-0.45, -0.55), dither=0.45):
        self.im = sphere(self.im, cx, cy, rx, ry, cols, light=light, dither=dither)
        return self

    def _mask(self, shape):
        m = Image.new('L', (W, H), 0)
        md = ImageDraw.Draw(m)
        kind, data = shape
        if kind == 'poly':
            md.polygon([(round(x), round(y)) for x, y in data], fill=255)
        elif kind == 'rect':
            md.rectangle(data, fill=255)
        elif kind == 'ell':
            md.ellipse(data, fill=255)
        return np.array(m) > 0

    def shade(self, shape, cols, direction=(0.6, 1.0), dither=0.45, mask=None):
        """Remplit une forme d'un dégradé linéaire (clair -> sombre dans `direction`)."""
        m = self._mask(shape) if mask is None else mask
        if not m.any():
            return self
        x, y = grid()
        dx, dy = direction
        n = math.hypot(dx, dy)
        p = (x * dx + y * dy) / n
        lo, hi = p[m].min(), p[m].max()
        v = (p - lo) / max(1e-6, hi - lo)
        self.im = ramp(self.im, m, v, cols, dither)
        return self

    def spoly(self, pts, cols, direction=(0.6, 1.0), dither=0.45):
        return self.shade(('poly', pts), cols, direction, dither)

    def cyl(self, box, cols, vertical=True, hi=0.3, dither=0.35):
        """Cylindre : reflet à `hi` de la largeur, assombri vers les bords."""
        m = self._mask(('rect', box))
        x, y = grid()
        x0, y0, x1, y1 = box
        if vertical:
            t = (x - x0) / max(1, x1 - x0 + 1)
        else:
            t = (y - y0) / max(1, y1 - y0 + 1)
        v = np.where(t < hi, (hi - t) / hi * 0.35, (t - hi) / (1 - hi))
        self.im = ramp(self.im, m, v, cols, dither)
        return self

    def sprite(self, x, y, rows, pal):
        """Petit dessin en caractères : '.' = vide, autre lettre = couleur de `pal`."""
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch != '.':
                    self.pt(x + i, y + j, pal[ch])
        return self

    def erase(self, shape):
        m = self._mask(shape)
        arr = np.array(self.im)
        arr[m] = 0
        self.im = Image.fromarray(arr)
        return self

    def out(self, col=INK, diag=False):
        self.im = outline(self.im, col, diag)
        return self

    def alpha(self, a):
        arr = np.array(self.im)
        arr[:, :, 3] = (arr[:, :, 3].astype(int) * a // 255).astype(np.uint8)
        self.im = Image.fromarray(arr)
        return self


class Card:
    def __init__(self):
        self.img = layer()

    def bg(self, cols, pattern='none', center=(40, 46), alt=0.14, scale=1.0, n=16, dither=0.3):
        """Fond plein cadre : dégradé radial + motif (rays, diamonds, checker, stripes, dots, waves, bricks, planks, bubbles, stars)."""
        x, y = grid()
        cx, cy = center
        dist = np.sqrt((x - cx) ** 2 + ((y - cy) * 0.8) ** 2) / 62 * scale
        full = np.ones((H, W), bool)
        if pattern == 'rays':
            ang = np.arctan2(y - cy, x - cx)
            pm = (np.floor((ang + math.pi) / (2 * math.pi) * n) % 2) == 0
        elif pattern == 'diamonds':
            pm = ((np.floor(x / 10 + y / 14) + np.floor(x / 10 - y / 14)) % 2) == 0
        elif pattern == 'checker':
            pm = ((np.floor(x / 8) + np.floor(y / 8)) % 2) == 0
        elif pattern == 'stripes':
            pm = (np.floor((x + y) / 7) % 2) == 0
        elif pattern == 'vstripes':
            pm = (np.floor(x / 8) % 2) == 0
        elif pattern == 'dots':
            pm = ((x % 10 - 5) ** 2 + (y % 10 - 5) ** 2) < 5
        elif pattern == 'waves':
            pm = (np.floor((y + 3 * np.sin(x / 5)) / 7) % 2) == 0
        elif pattern == 'bricks':
            row = np.floor(y / 7)
            pm = ((y % 7) < 1) | (((x + (row % 2) * 7) % 14) < 1)
        elif pattern == 'planks':
            pm = ((y % 12) < 1) | (((x + np.floor(y / 12) * 23) % 37) < 1)
        elif pattern == 'bubbles':
            pm = np.zeros((H, W), bool)
            rng = np.random.RandomState(3)
            for _ in range(26):
                bx, by, br = rng.uniform(0, W), rng.uniform(0, H), rng.uniform(1.5, 4)
                r = np.sqrt((x - bx) ** 2 + (y - by) ** 2)
                pm |= (r < br) & (r > br - 1.2)
        elif pattern == 'stars':
            pm = np.zeros((H, W), bool)
        else:
            pm = np.zeros((H, W), bool)
        self.img = ramp(self.img, full & ~pm, dist, cols, dither)
        self.img = ramp(self.img, full & pm, dist + alt, cols, dither)
        if pattern == 'stars':
            d = ImageDraw.Draw(self.img)
            rng = np.random.RandomState(5)
            for _ in range(30):
                d.point((int(rng.uniform(2, W - 2)), int(rng.uniform(2, H - 2))), fill=hexc('#ffffff', 200))
        return self

    def vignette(self, strength=0.5, col='#000000'):
        x, y = grid()
        d = np.sqrt(((x - 40) / 40) ** 2 + ((y - 56) / 56) ** 2)
        yy, xx = np.mgrid[0:H, 0:W]
        from px import BAYER
        m = (d - 0.85) * 3 * strength > BAYER[yy % 4, xx % 4]
        arr = np.array(self.img)
        c = np.array(hexc(col), float)
        arr[m] = (arr[m] * 0.45 + c * 0.55).astype(np.uint8)
        self.img = Image.fromarray(arr)
        return self

    def glow(self, cx, cy, r0, r1, col, a=200):
        """Halo tramé en anneau (lueur pixel art)."""
        x, y = grid()
        yy, xx = np.mgrid[0:H, 0:W]
        g = np.sqrt((x - cx) ** 2 + (y - cy) ** 2)
        m = (g > r0) & (g < r1) & (((xx + yy) % 2) == 0)
        arr = np.array(self.img)
        c = np.array(hexc(col), float)
        k = a / 255
        arr[m] = (arr[m] * (1 - k) + c * k).astype(np.uint8)
        self.img = Image.fromarray(arr)
        return self

    def add(self, piece):
        self.img.alpha_composite(piece.im)
        return self

    def spark(self, x, y, col='#fffbe6', big=False):
        sparkle(ImageDraw.Draw(self.img), x, y, col, big)
        return self

    def done(self, edge='#0a0508', light='#f4ead8'):
        return frame(self.img, edge, light)


# ---------- sujets récurrents ----------

def coin(p, cx, cy, w=3.4, h=2.2, cols=GOLD, edge=True):
    p.sph(cx, cy, w, h, cols[:4], dither=0.2)
    p.pt(cx - 1, cy - 1, '#fffbe6')
    if edge:
        p.line([(cx - w + 1, cy + h), (cx + w - 1, cy + h)], cols[3])
    return p


def coin_face(p, cx, cy, r, cols=GOLD, mark=True):
    """Pièce vue de face, avec rebord."""
    p.sph(cx, cy, r, r, cols[:4], dither=0.3)
    p.arc([cx - r + 2, cy - r + 2, cx + r - 2, cy + r - 2], 0, 360, cols[3])
    p.arc([cx - r + 2, cy - r + 2, cx + r - 2, cy + r - 2], 200, 290, cols[0])
    return p


def gem(p, cx, cy, r, cols):
    p.poly([(cx, cy - r), (cx + r, cy), (cx, cy + r), (cx - r, cy)], cols[1])
    p.poly([(cx, cy - r), (cx, cy), (cx - r, cy)], cols[0])
    p.poly([(cx, cy + r), (cx + r, cy), (cx, cy)], cols[2])
    return p


SUITS = {
    'H': ['.r.r.', 'rrrrr', 'rrrrr', '.rrr.', '..r..'],
    'D': ['..r..', '.rrr.', 'rrrrr', '.rrr.', '..r..'],
    'S': ['..k..', '.kkk.', 'kkkkk', 'kkkkk', '..k..'],
    'C': ['.kkk.', '.kkk.', 'kkkkk', 'kkkkk', '..k..'],
}


def suit(p, x, y, s, red='#d8333f', black='#20182a'):
    p.sprite(x, y, SUITS[s], {'r': red, 'k': black})
    return p


def playing_card(card, cx, cy, w, h, deg, s='S', face='#fffaf0', back=None):
    """Carte à jouer tournée de `deg` degrés, avec une enseigne au centre."""
    p = Piece()
    pts = rot([(cx - w / 2, cy - h / 2), (cx + w / 2, cy - h / 2), (cx + w / 2, cy + h / 2), (cx - w / 2, cy + h / 2)], cx, cy, deg)
    if back:
        p.poly(pts, back[0])
        inner = rot([(cx - w / 2 + 2, cy - h / 2 + 2), (cx + w / 2 - 2, cy - h / 2 + 2), (cx + w / 2 - 2, cy + h / 2 - 2),
                     (cx - w / 2 + 2, cy + h / 2 - 2)], cx, cy, deg)
        p.poly(inner, back[1])
    else:
        p.spoly(pts, [face, '#f4ead8', '#dcc8a8'], (0.4, 1))
        if s:
            suit(p, cx - 2, cy - 2, s)
            tl = rot([(cx - w / 2 + 2, cy - h / 2 + 2)], cx, cy, deg)[0]
            p.pt(tl[0], tl[1], '#d8333f' if s in 'HD' else '#20182a')
    p.out()
    card.add(p)
    return pts


def slot_seven(p, x, y, col='#e0303c', hi='#ff9a9a'):
    p.sprite(x, y, ['rrrrrr', 'hhhhrr', '...rr.', '..rr..', '..rr..', '.rr...', '.rr...'], {'r': col, 'h': hi})
    return p


def cherry(p, cx, cy):
    p.line([(cx - 3, cy), (cx, cy - 6), (cx + 3, cy - 1)], '#3fae4a')
    p.sph(cx - 3, cy + 1, 2.4, 2.4, ['#ff9a9a', '#e0303c', '#8e1420'])
    p.sph(cx + 3, cy, 2.4, 2.4, ['#ff9a9a', '#e0303c', '#8e1420'])
    return p


def chip(p, cx, cy, w, h, col, light, dark):
    """Jeton de casino vu de biais."""
    p.ell([cx - w, cy - h + 2, cx + w, cy + h + 2], dark)
    p.ell([cx - w, cy - h, cx + w, cy + h], col)
    for k in range(4):
        t = k * math.pi / 2 + 0.4
        p.pt(cx + math.cos(t) * (w - 1), cy + math.sin(t) * (h - 0.6), '#fffaf0')
    p.ell([cx - w + 3, cy - h + 1.5, cx + w - 3, cy + h - 1.5], light)
    return p


def bolt(p, pts, col='#fff6c4', core='#ffffff'):
    p.line(pts, col, 2)
    p.line(pts, core, 1)
    return p
