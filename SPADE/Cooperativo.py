import sys
import os
from time import sleep
from Agente import Agent

if __name__ == "__main__":
    def estrategia(self, i, last):
        return 1
    
    tm = Agent(estrategia, "cooperativo@localhost", "123")
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
