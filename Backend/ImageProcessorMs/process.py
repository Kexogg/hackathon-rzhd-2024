import cv2
import contrast
from shiftlab_ocr.doc2text.reader import Reader
from shiftlab_ocr.doc2text.crop import Crop
from PIL import Image

import json


def run(filename):
    reader = Reader()
    image = cv2.imread(filename)
    contrast.normalize_contrast(image, filename)
    crops = get_crops(filename, reader)
    avg, dic = get_rows(crops, filename)
    rs = process_rows(avg, crops, dic, reader)
    text_result = get_rows_text(rs)
    res = json.dumps(text_result)
    return res


def get_rows_text(rs):
    text_result = {}
    for i in rs.keys():
        to_print = ''
        for word in rs[i]:
            to_print += word[1] + ' '
        text_result[i] = to_print
    return text_result


def process_rows(avg, crops, dic, reader):
    for i in crops:
        middle = int((i.points[1][1] * 0.4 + i.points[0][1] * 0.6))
        text = reader.recognizer.run(i.img)
        dic[int(middle / avg)].append((i, text))
    rs = {}
    for index, i in enumerate(dic.keys()):
        if len(dic[i]) != 0:
            rs[index] = dic[i]
    return rs


def get_rows(crops, filename):
    summ = 0
    count = len(crops)
    for i in crops:
        summ += (i.points[1][1] - i.points[0][1])
    avg = summ / count
    print(avg)
    dic = {}
    img = cv2.imread(filename + ".png")
    for i in range(int(img.shape[1] / avg + 1)):
        dic[i] = []
    return avg, dic


def get_crops(filename, reader):
    image = Image.open(filename + ".png")
    boxes = reader.detector.run(filename + ".png")
    crops = []
    for box in boxes:
        cropped = image.crop((box[0], box[1],
                              box[2], box[3]))

        crops.append(Crop([[box[0], box[1]], [box[2], box[3]]], img=cropped))
    crops = sorted(crops)
    return crops
