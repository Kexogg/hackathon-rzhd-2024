import cv2
import contrast
from shiftlab_ocr.doc2text.reader import Reader
import json

def run(filename):
    reader = Reader()
    image = cv2.imread(filename)

    contrast_image = contrast.normalize_contrast(image, filename)
    result = reader.doc2text(filename + ".png")

    print(result[0])

    summ = 0
    count = len(result[1])
    for i in result[1]:
        summ += (i.points[1][1] - i.points[0][1])
    avg = summ / count
    print(avg)
    dic = {}
    img = cv2.imread('t.jpg.png')
    for i in range(int(img.shape[1] / avg + 1)):
        dic[i] = []

    reader = Reader()

    for i in result[1]:
        middle = int((i.points[1][1] + i.points[0][1]) / 2)
        text = reader.recognizer.run(i.img)
        dic[int(middle / avg)].append((i, text))

    rs = {}
    for index, i in enumerate(dic.keys()):
        if len(dic[i]) != 0:
            rs[index] = dic[i]

    text_result = {}

    for i in rs.keys():
        to_print = ''
        for word in rs[i]:
            to_print += word[1] + ' '
        text_result[i] = to_print

    res = json.dumps(text_result)

    return res