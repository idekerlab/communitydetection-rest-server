FROM continuumio/miniconda3

RUN apt-get update
RUN apt-get install -y build-essential
RUN git clone --depth 1 https://github.com/fanzheng10/CliXO clixo
WORKDIR "/clixo"
RUN rm -f clixo clustersToDAG
RUN make clean
RUN make

COPY run.py /run.py

ENTRYPOINT ["/run.py"]
CMD ["--help"]
