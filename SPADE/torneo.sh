#!/bin/bash

python3 Torneo.py > ./logs/torneo.log &
sleep 1
python3 OjoPorOjo.py > ./logs/ojo.log &
python3 Cooperativo.py > ./logs/coop.log &
python3 NoCooperativo.py > ./logs/noco.log &
python3 Random.py > ./logs/rand.log