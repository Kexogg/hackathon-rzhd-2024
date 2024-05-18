import cv2
import contrast
from shiftlab_ocr.doc2text.reader import Reader

def run(filename):
    reader = Reader()
    image = cv2.imread(filename)

    contrast_image = contrast.normalize_contrast(image, filename)
    result = reader.doc2text(filename + ".png")

    return result[0]