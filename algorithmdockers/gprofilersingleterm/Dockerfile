FROM continuumio/miniconda3

RUN conda install -y -c conda-forge python-igraph
RUN pip install pandas
RUN pip install gprofiler-official

COPY run.py /run.py

ENTRYPOINT ["/run.py"]
CMD ["--help"]
