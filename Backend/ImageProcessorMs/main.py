import cv2
import contrast
from shiftlab_ocr.doc2text.reader import Reader
import pika
import base64

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost'))

channel = connection.channel()

channel.queue_declare(queue='my_queue')

def fib(filename):
    reader = Reader()
    image = cv2.imread(filename)

    contrast_image = contrast.normalize_contrast(image)
    result = reader.doc2text("cropped.png")

    return result[1]

def on_request(ch, method, props, body):
    with open("got.png", "wb") as fh:
        fh.write(base64.decodebytes(str(body)))

    response = fib('got.png')

    ch.basic_publish(exchange='',
                     routing_key=props.reply_to,
                     body=str(response))
    ch.basic_ack(delivery_tag=method.delivery_tag)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(queue='my_queue', on_message_callback=on_request)

channel.start_consuming()
