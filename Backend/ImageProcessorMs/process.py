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
    image = cv2.imread(filename)
    width = image.shape[1]
    crops = get_crops(filename, reader)
    avg, dic = get_rows(crops, filename)
    rs = process_rows(avg, crops, dic, reader, width)
    text_result = get_rows_text(rs)
    res = json.dumps(text_result)
    return res


def get_rows_text(rs):
    text_result = {}
    index = 0
    for i in rs.keys():
        if len(rs[i][0]) == 0 and len(rs[i][1]) == 0 and len(rs[i][2]) == 0:
            continue
        to_print_left = ''
        to_print_middle = ''
        to_print_right = ''
        for word in rs[i][0]:
            to_print_left += word[1] + ' '
        for word in rs[i][1]:
            to_print_middle += word[1] + ' '
        for word in rs[i][2]:
            to_print_right += word[1] + ' '
        text_result[index] = [to_print_left, to_print_middle, to_print_right]
        index += 1
    return text_result


def process_rows(avg, crops, dic, reader, width):
    for i in crops:
        middle_height = int((i.points[1][1] * 0.4 + i.points[0][1] * 0.6))
        middle_width = int((i.points[1][0] * 0.5 + i.points[0][0] * 0.5))
        text = reader.recognizer.run(i.img)
        # cv2 курит в сторонке
        if middle_width > width * 0.805:
            dic[int(middle_height / avg)][2].append((i, text))
        elif middle_width < width * 0.195:
            dic[int(middle_height / avg)][0].append((i, text))
        else:
            dic[int(middle_height / avg)][1].append((i, text))
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
    avg = summ / count * 1.2
    print(avg)
    dic = {}
    img = cv2.imread(filename)
    for i in range(int(img.shape[1] / avg)):
        dic[i] = [[],[],[]]
    return avg, dic


def get_crops(filename, reader):
    image = Image.open(filename)
    boxes = reader.detector.run(filename)
    crops = []
    for box in boxes:
        cropped = image.crop((box[0], box[1],
                              box[2], box[3]))

        crops.append(Crop([[box[0], box[1]], [box[2], box[3]]], img=cropped))
    crops = sorted(crops)
    return crops
