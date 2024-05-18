import cv2
import numpy as np
from PIL import Image


def normalize_contrast(image, filename):

    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    hsv[:, :, 1] = 2.0 * hsv[:, :, 1]
    image = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)

    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    edges = cv2.Canny(gray_image, 50, 150, apertureSize=3)

    lines = cv2.HoughLinesP(edges, 1, np.pi / 180, 100, minLineLength=100, maxLineGap=10)

    min_x, min_y = np.inf, np.inf
    max_x, max_y = -np.inf, -np.inf

    for line in lines:
        x1, y1, x2, y2 = line[0]
        min_x, min_y = min(min_x, x1, x2), min(min_y, y1, y2)
        max_x, max_y = max(max_x, x1, x2), max(max_y, y1, y2)

    cv2.rectangle(image, (min_x, min_y), (max_x, max_y), (0, 255, 0), 2)

    cropped_image = image[min_y:max_y, min_x:max_x]

    gray_cropped_image = cv2.cvtColor(cropped_image, cv2.COLOR_BGR2GRAY)

    c = cv2.convertScaleAbs(gray_cropped_image, alpha=2, beta=-100)

    cv2.imwrite(filename + '.png', c)
    im = Image.open(filename + '.png')
    im = im.convert("RGB")
    im.save(filename + '.png')

    return im


if __name__ == '__main__':
    normalize_contrast(cv2.imread(input()), input())
