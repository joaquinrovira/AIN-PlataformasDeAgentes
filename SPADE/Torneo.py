import sys
import os
from time import sleep

from spade.agent import Agent
from spade.message import Message
from spade.behaviour import FSMBehaviour, State

NUM_AGENTES = 4
NUM_RONDAS = 10


# TOURNAMENT MANAGER

INICIO = "INICIO"
CONTROL = "CONTROL"
COMPETIR = "COMPETIR"
RESULTADOS = "RESULTADOS"


class TournamentManagerBehaviour(FSMBehaviour):

    async def on_start(self):
        print("Esperando a los otros agentes...")

    async def on_end(self):
        print("FIN")
        await self.agent.stop()


class inicio(State):
    async def run(self):
        if self.agent.agentes.__len__() == NUM_AGENTES:
            print("Todos los agentes a la espera, comenzando el torneo")
            # Copiar la lista de participantes a una variable auxiliar para la ejecucion del torneo
            self.agent.aux = self.agent.agentes.copy()
            self.agent.cuenta = 0  # Variable auxiliar para la ejecucion del torneo
            self.set_next_state(CONTROL)
            return

        msg = await self.receive(timeout=1)
        if msg:
            print(f"Recibida notificacion de: {msg.sender.localpart}")
            self.agent.agentes.append(msg.sender.localpart)
            self.agent.resultados.append(0)

        self.set_next_state(INICIO)


class control(State):
    async def run(self):
        self.agent.cuenta += 1  # Siguiente ronda
        if self.agent.aux.__len__() == 0:
            self.set_next_state(RESULTADOS)
            return
        if self.agent.cuenta >= self.agent.aux.__len__():
            self.agent.cuenta = 0
            self.agent.aux.pop(0)
            self.set_next_state(CONTROL)
            return

        print(f"\n\t|| {self.agent.aux[0]} vs {self.agent.aux[self.agent.cuenta]} ||")
        self.set_next_state(COMPETIR)


class competir(State):
    async def run(self):
        tabla_ref = [
            [(2, 2), (3, 0)],
            [(0, 3), (1, 1)]
        ]
        a1 = self.agent.aux[0]
        a1_last = 1
        a1_res = 0
        a2 = self.agent.aux[self.agent.cuenta]
        a2_last = 1
        a2_res = 0

        for i in range(NUM_RONDAS):
            # Enviar mensajes
            msg = Message(to=f"{a1}@localhost")
            msg.body = str([i, a2_last])
            await self.send(msg)

            msg = Message(to=f"{a2}@localhost")
            msg.body = str([i, a1_last])
            await self.send(msg)

            #Esperar las respuestas
            msg = await self.receive(timeout=5)
            if msg:
                if msg.sender.localpart == a1:
                    a1_last = int(msg.body)
                else:
                    a2_last = int(msg.body)
            else: 
                print("Agente no responde. Terminando torneo.")
                sys.stdout.flush()
                os._exit(0)

            msg = await self.receive(timeout=5)
            if msg:
                if msg.sender.localpart == a1:
                    a1_last = int(msg.body)
                else:
                    a2_last = int(msg.body)
            else: 
                print("Agente no responde. Terminando torneo.")
                sys.stdout.flush()
                os._exit(0)

            # Añadir resultados
            print(f"\tRonda {i}:\n\t\t{a1} -> {a1_last}\n\t\t{a2} -> {a2_last}")
            a1_res += tabla_ref[a1_last][a2_last][0]
            a2_res += tabla_ref[a1_last][a2_last][1]

        print(f"\t>> Res :\n\t\t{a1} -> {a1_res}\n\t\t{a2} -> {a2_res}")
        self.agent.resultados[self.agent.agentes.index(a1)] += a1_res
        self.agent.resultados[self.agent.agentes.index(a2)] += a2_res
        self.set_next_state(CONTROL)


class resultados(State):
    async def run(self):
        print("\n>> RESULTADOS:")
        for i in range(self.agent.agentes.__len__()):
            msg = Message(to=f"{self.agent.agentes[i]}@localhost")
            msg.body = "STOP"
            await self.send(msg)
            print(f"\t{self.agent.agentes[i]}\t{self.agent.resultados[i]}")


class TournamentManager(Agent):
    async def setup(self):
        # Atributos de control del torneo
        self.agentes = []
        self.resultados = []

        # Definir la Maquina de Estados Finitos
        fsm = TournamentManagerBehaviour()

        # Estados
        fsm.add_state(name=INICIO, state=inicio(), initial=True)
        fsm.add_state(name=CONTROL, state=control())
        fsm.add_state(name=COMPETIR, state=competir())
        fsm.add_state(name=RESULTADOS, state=resultados())

        # Transiciones entre estados
        fsm.add_transition(source=INICIO, dest=INICIO)
        fsm.add_transition(source=INICIO, dest=CONTROL)
        fsm.add_transition(source=CONTROL, dest=CONTROL)
        fsm.add_transition(source=CONTROL, dest=COMPETIR)
        fsm.add_transition(source=COMPETIR, dest=CONTROL)
        fsm.add_transition(source=CONTROL, dest=RESULTADOS)

        self.add_behaviour(fsm)




# tournamentManager@localhost
# ojoPorOjo@localhost
# cooperativo@localhost
# noCooperativo@localhost
# random@localhost

if __name__ == "__main__":
    tm = TournamentManager("tournamentManager@localhost", "123")
    tm.start()
    sleep(1)

    while tm.is_alive():
        try:
            sleep(1)
        except KeyboardInterrupt:
            tm.stop()
            break
    print("Agent finished")
    sys.stdout.flush()
    os._exit(0)
