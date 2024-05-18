ARG BASE_CONTAINER=python:3.8

FROM --platform=linux/amd64 $BASE_CONTAINER

USER root
RUN apt-get update && apt-get install -y && apt-get clean

WORKDIR /app

COPY requirements.txt .
COPY contrast.py .
COPY process.py .
COPY main.py .

RUN pip install --upgrade pip && pip install -r requirements.txt

CMD ["python", "main.py"]