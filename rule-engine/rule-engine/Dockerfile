FROM openjdk:17

RUN mkdir /tmp/sclera

COPY target/rule-engine.jar /tmp/sclera

COPY runner.sh /tmp/sclera

WORKDIR /tmp/sclera

RUN chmod +x runner.sh

CMD ["./runner.sh"]