FROM continuumio/miniconda3

RUN conda install -y -c conda-forge python-igraph
RUN conda install -y -c conda-forge igraph
RUN conda install -y -c conda-forge louvain
RUN pip install ndex2 


COPY run.py /run.py

ENTRYPOINT ["/run.py"]
CMD ["--help"]
