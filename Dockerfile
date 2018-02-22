FROM openjdk:8
MAINTAINER MoGol07

WORKDIR /app/

# copy installation build archive and create output folder
ADD build/distributions/shrpy_prsr.tar /app/
RUN ["mkdir", "/app/shrpy_prsr/output"]

# run grabber
CMD ["/app/shrpy_prsr/bin/shrpy_prsr"]

# RUN ["ls", "-ll", "/app/shrpy_prsr/output"]

VOLUME ["/app/shrpy_prsr/output"]
