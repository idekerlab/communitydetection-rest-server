FROM continuumio/miniconda3

RUN pip install ndex2 

COPY run.py /run.py

ENTRYPOINT ["/run.py"]
CMD ["--help"]
