IBM® MQ
=======

mq-docker
---------

https://github.com/ibm-messaging/mq-docker

```
sudo docker run \
  --env LICENSE=accept \
  --env MQ_QMGR_NAME=QM1 \
  --volume /tmp/mqm:/mnt/mqm \
  --publish 1414:1414 \
  --publish 9443:9443 \
  --detach \
  ibmcom/mq:8
```

mqlight-docker
--------------

https://github.com/ibm-messaging/mqlight-docker

```
sudo docker run \
  --env LICENSE=accept \
  --volume /tmp/mqlight:/var/mqlight \
  --publish 5672:5672 \
  --publish 9180:9180 \
  --detach \
  ibmcom/mqlight:1.0
```