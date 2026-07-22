"""
Name: pdfkit
Description: the smallest PDF writer that will do: pages, text in the fonts
             every reader already has, rectangles, lines, circles and JPEG
             pictures. No library to install, which is the same rule the
             engine follows.

             A PDF is a list of numbered objects and a table saying where
             each one starts. Everything here builds that list.
Author: Silvano Malfatti
Date: 22/07/26
"""

import struct
import zlib

A4 = (595.276, 841.890)

#The fonts every reader carries, so nothing has to be embedded
FONTS = {
    "regular": "Helvetica",
    "bold": "Helvetica-Bold",
    "oblique": "Helvetica-Oblique",
    "mono": "Courier",
    "mono-bold": "Courier-Bold",
}

#Width of every character, in thousandths of the size. Only what is needed to
#centre a line and to break a paragraph.
_HELV = None


def _widths():
    """Character widths of Helvetica, close enough for laying out a page."""
    global _HELV
    if _HELV is not None:
        return _HELV

    w = [556] * 256
    for c, v in zip(" !\"#$%&'()*+,-./0123456789:;<=>?@",
                    [278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584,
                     278, 333, 278, 278] + [556] * 10 + [278, 278, 584, 584, 584, 556, 1015]):
        w[ord(c)] = v
    for c, v in zip("ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    [667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833,
                     722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611]):
        w[ord(c)] = v
    for c, v in zip("abcdefghijklmnopqrstuvwxyz",
                    [556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833,
                     556, 556, 556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500]):
        w[ord(c)] = v
    for c, v in zip("[]{}|", [278, 278, 334, 334, 260]):
        w[ord(c)] = v
    _HELV = w
    return w


def text_width(text, size, font="regular"):
    """How wide a line will be once drawn."""
    if font.startswith("mono"):
        return len(text) * size * 0.6

    w = _widths()
    total = 0
    for ch in text:
        code = ord(ch)
        total += w[code] if code < 256 else 556
    return total * size / 1000.0


def wrap(text, size, width, font="regular"):
    """Breaks a paragraph into lines that fit."""
    lines = []
    line = ""

    for word in text.split():
        candidate = word if not line else line + " " + word
        if text_width(candidate, size, font) <= width:
            line = candidate
        else:
            if line:
                lines.append(line)
            line = word

    if line:
        lines.append(line)
    return lines


def _escape(text):
    out = text.replace("\\", r"\\").replace("(", r"\(").replace(")", r"\)")
    return out.encode("cp1252", "replace")


def jpeg_size(data):
    """Width and height of a JPEG, read from its frame header."""
    i = 2
    while i < len(data):
        if data[i] != 0xFF:
            i += 1
            continue
        marker = data[i + 1]
        if marker in (0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7,
                      0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF):
            height, width = struct.unpack(">HH", data[i + 5:i + 9])
            return width, height
        length = struct.unpack(">H", data[i + 2:i + 4])[0]
        i += 2 + length
    raise ValueError("not a JPEG")


class Page:
    """One page being drawn on. Coordinates are in points from the top left,
    which is how a page is read, and turned upside down on the way out."""

    def __init__(self, document, size=A4):
        self.document = document
        self.width, self.height = size
        self.parts = []
        self.images = {}

    # ------------------------------------------------------------- drawing

    def rect(self, x, y, width, height, fill=None, stroke=None, line=1.0):
        y = self.height - y - height
        if fill:
            self.parts.append("%s rg %.2f %.2f %.2f %.2f re f" % (_rgb(fill), x, y, width, height))
        if stroke:
            self.parts.append("%s RG %.2f w %.2f %.2f %.2f %.2f re S"
                              % (_rgb(stroke), line, x, y, width, height))

    def line(self, x1, y1, x2, y2, colour=(0, 0, 0), width=1.0):
        self.parts.append("%s RG %.2f w %.2f %.2f m %.2f %.2f l S"
                          % (_rgb(colour), width, x1, self.height - y1, x2, self.height - y2))

    def circle(self, x, y, radius, fill=None, stroke=None, width=1.0):
        y = self.height - y
        k = radius * 0.5523
        path = ("%.2f %.2f m "
                "%.2f %.2f %.2f %.2f %.2f %.2f c "
                "%.2f %.2f %.2f %.2f %.2f %.2f c "
                "%.2f %.2f %.2f %.2f %.2f %.2f c "
                "%.2f %.2f %.2f %.2f %.2f %.2f c") % (
            x + radius, y,
            x + radius, y + k, x + k, y + radius, x, y + radius,
            x - k, y + radius, x - radius, y + k, x - radius, y,
            x - radius, y - k, x - k, y - radius, x, y - radius,
            x + k, y - radius, x + radius, y - k, x + radius, y)
        if fill:
            self.parts.append("%s rg %s f" % (_rgb(fill), path))
        if stroke:
            self.parts.append("%s RG %.2f w %s S" % (_rgb(stroke), width, path))

    def text(self, x, y, text, size=11, font="regular", colour=(0, 0, 0), align="left"):
        if align == "center":
            x -= text_width(text, size, font) / 2.0
        elif align == "right":
            x -= text_width(text, size, font)

        self.parts.append("BT %s rg /%s %.2f Tf %.2f %.2f Td (%s) Tj ET"
                          % (_rgb(colour), _font_name(font), size, x,
                             self.height - y, _escape(text).decode("latin-1")))
        return x

    def paragraph(self, x, y, text, width, size=11, font="regular",
                  colour=(0, 0, 0), leading=None, align="left"):
        leading = leading or size * 1.45
        for line in wrap(text, size, width, font):
            place = x if align == "left" else (x + width / 2.0 if align == "center" else x + width)
            self.text(place, y, line, size, font, colour, align)
            y += leading
        return y

    def image(self, path, x, y, width, height):
        name = self.document.add_image(path)
        self.images[name] = True
        self.parts.append("q %.2f 0 0 %.2f %.2f %.2f cm /%s Do Q"
                          % (width, height, x, self.height - y - height, name))

    def content(self):
        return "\n".join(self.parts).encode("latin-1")


def _rgb(colour):
    return "%.3f %.3f %.3f" % (colour[0] / 255.0, colour[1] / 255.0, colour[2] / 255.0)


def _font_name(kind):
    return {"regular": "F1", "bold": "F2", "oblique": "F3",
            "mono": "F4", "mono-bold": "F5"}[kind]


class Document:
    """The whole file. Objects are numbered as they are written."""

    def __init__(self, title="", author=""):
        self.pages = []
        self.image_files = []
        self.image_names = {}
        self.title = title
        self.author = author

    def page(self, size=A4):
        page = Page(self, size)
        self.pages.append(page)
        return page

    def add_image(self, path):
        if path not in self.image_names:
            self.image_names[path] = "Im%d" % (len(self.image_files) + 1)
            self.image_files.append(path)
        return self.image_names[path]

    def save(self, path):
        objects = []                      # each entry is the body of one object

        def add(body):
            objects.append(body)
            return len(objects)           # objects are numbered from one

        font_ids = {}
        for kind, name in (("F1", "Helvetica"), ("F2", "Helvetica-Bold"),
                           ("F3", "Helvetica-Oblique"), ("F4", "Courier"),
                           ("F5", "Courier-Bold")):
            font_ids[kind] = add(("<< /Type /Font /Subtype /Type1 /BaseFont /%s "
                                  "/Encoding /WinAnsiEncoding >>" % name).encode("latin-1"))

        image_ids = {}
        for picture in self.image_files:
            data = open(picture, "rb").read()
            width, height = jpeg_size(data)
            body = (("<< /Type /XObject /Subtype /Image /Width %d /Height %d "
                     "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode "
                     "/Length %d >>\nstream\n") % (width, height, len(data))).encode("latin-1")
            body += data + b"\nendstream"
            image_ids[self.image_names[picture]] = add(body)

        #the pages object comes after two objects per page, and every page
        #has to point at it, so its number is worked out before they are made
        pages_id = len(objects) + 2 * len(self.pages) + 1
        page_ids = []

        for page in self.pages:
            stream = zlib.compress(page.content())
            content_id = add((("<< /Length %d /Filter /FlateDecode >>\nstream\n")
                              % len(stream)).encode("latin-1") + stream + b"\nendstream")

            resources = "/Font << %s >>" % " ".join(
                "/%s %d 0 R" % (kind, font_ids[kind]) for kind in font_ids)
            if page.images:
                resources += " /XObject << %s >>" % " ".join(
                    "/%s %d 0 R" % (name, image_ids[name]) for name in page.images)

            page_ids.append(add((
                "<< /Type /Page /Parent %d 0 R /MediaBox [0 0 %.2f %.2f] "
                "/Resources << %s >> /Contents %d 0 R >>"
                % (pages_id, page.width, page.height, resources, content_id)).encode("latin-1")))

        written = add(("<< /Type /Pages /Count %d /Kids [%s] >>"
                       % (len(page_ids), " ".join("%d 0 R" % i for i in page_ids))).encode("latin-1"))

        assert written == pages_id, "the pages object did not land where the pages expected it"

        info_id = add(("<< /Title (%s) /Author (%s) /Producer (pdfkit, no libraries) >>"
                       % (self.title, self.author)).encode("latin-1"))
        catalog_id = add(("<< /Type /Catalog /Pages %d 0 R >>" % pages_id).encode("latin-1"))

        out = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
        offsets = [0]

        for number, body in enumerate(objects, start=1):
            offsets.append(len(out))
            out += b"%d 0 obj\n" % number + body + b"\nendobj\n"

        start = len(out)
        out += b"xref\n0 %d\n" % (len(objects) + 1)
        out += b"0000000000 65535 f \n"
        for offset in offsets[1:]:
            out += b"%010d 00000 n \n" % offset

        out += (b"trailer\n<< /Size %d /Root %d 0 R /Info %d 0 R >>\nstartxref\n%d\n%%%%EOF\n"
                % (len(objects) + 1, catalog_id, info_id, start))

        open(path, "wb").write(bytes(out))
        return len(out)
