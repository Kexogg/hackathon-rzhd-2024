import os

import pika
import base64
import process


def on_request(ch, method, props, body):
    log_start = 'CORRELATION ID: ' + props.correlation_id + " | "
    print(log_start + 'Got message')
    with open(props.correlation_id + ".png", "wb") as fh:
        fh.write(base64.decodebytes(body))
    print(log_start + 'Image decoded, starting processing')
    response = process.run(props.correlation_id + '.png')
    print(log_start + 'Result: ' + response)

    ch.basic_publish(exchange='',
                     routing_key='handler_queue',
                     properties=pika.BasicProperties(correlation_id=props.correlation_id),
                     body=str(response))
    ch.basic_ack(delivery_tag=method.delivery_tag)


connection = pika.BlockingConnection(
    pika.ConnectionParameters(host="localhost"))

channel = connection.channel()

channel.basic_qos(prefetch_count=1)
channel.basic_consume(queue='processing_queue', on_message_callback=on_request)

channel.start_consuming()
