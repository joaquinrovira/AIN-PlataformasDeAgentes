import ast
import spade
from spade.message import Message
from spade.behaviour import FSMBehaviour, State

INICIO = "INICIO"
CONTROL = "CONTROL"

class AgentBehaviour(FSMBehaviour):
    async def on_start(self):
        print(f"{self.agent.name} >> INIT")

    async def on_end(self):
        print(f"{self.agent.name} >> FIN")
        await self.agent.stop()


class inicio(State):
    async def run(self):
        msg = Message(to="tournamentManager@localhost")
        msg.body = ""  

        await self.send(msg)
        print(f"{self.agent.name} >> Notificacion enviada")
        self.set_next_state(CONTROL)


class control(State):
    async def run(self):
        msg = await self.receive(timeout=1)
        if msg:
            print(f">> {msg.body}")
            if(msg.body == "STOP"):
                await self.agent.stop()
                return
            res = ast.literal_eval(msg.body)
            msg = Message(to="tournamentManager@localhost")
            msg.body = str(self.agent.strategy(self, res[0], res[1]))
            await self.send(msg)

        self.set_next_state(CONTROL)


class Agent(spade.agent.Agent):
    # default constructor
    def __init__(self, strategy, *args):
        self.strategy = strategy
        super().__init__(*args)

    async def setup(self):
        # Definir la Maquina de Estados Finitos
        fsm = AgentBehaviour()

        # Estados
        fsm.add_state(name=INICIO, state=inicio(), initial=True)
        fsm.add_state(name=CONTROL, state=control())

        # Transiciones entre estados
        fsm.add_transition(source=INICIO, dest=CONTROL)
        fsm.add_transition(source=CONTROL, dest=CONTROL)

        self.add_behaviour(fsm)
