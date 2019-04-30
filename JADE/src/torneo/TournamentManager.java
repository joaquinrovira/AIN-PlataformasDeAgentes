/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torneo;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ximo
 */
public class TournamentManager extends Agent {

    static final int NUM_AGENTES = 4;
    static final int NUM_RONDAS = 10;
    private final List<AID> agentes = new LinkedList<>();
    private final List<Integer> resultados = new LinkedList<>();
    private List<AID> aux;
    private int cuenta = 0;

    private final String INICIO = "INICIO",
            CONTROL = "CONTROL",
            COMPETIR = "COMPETIR",
            RESULTADOS = "RESULTADOS";

    @Override
    protected void setup() {
        print("STARTED");

        // Definir la Maquina de Estados Finitos
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                print("FSM behaviour completed.");
                doDelete();
                return super.onEnd();
            }
        };

        // Registrar los estados
        fsm.registerFirstState(new Inicio(), INICIO);
        fsm.registerState(new Control(), CONTROL);
        fsm.registerState(new Competir(), COMPETIR);
        fsm.registerLastState(new Resultados(), RESULTADOS);

        // Transiciones entre estados
        fsm.registerDefaultTransition(INICIO, CONTROL);
        fsm.registerTransition(CONTROL, CONTROL, 0);
        fsm.registerTransition(CONTROL, COMPETIR, 1);
        fsm.registerTransition(CONTROL, RESULTADOS, 2);
        fsm.registerDefaultTransition(COMPETIR, CONTROL);

        addBehaviour(fsm);
    }

    private class Inicio extends Behaviour {

        public void action() {
            print("Esperando mensaje...");
            ACLMessage msg = blockingReceive();
            print("Recibido!");
            if (msg != null) {
                print("Registrado[" + agentes.size() + "] " + msg.getSender().getLocalName());
                agentes.add(msg.getSender());
                resultados.add(0);
            }
        }

        public boolean done() {
            if (agentes.size() == NUM_AGENTES) {
                print("Todos los agentes a la espera, comenzando torneo");
                aux = new LinkedList<>(agentes);
                return true;
            } else {
                return false;
            }
        }
    }

    private class Control extends OneShotBehaviour {

        int nextState = 0;

        public void action() {
            cuenta++;
            if (aux.size() == 0) {
                // Ir a estado RESULTADOS
                nextState = 2;
                return;
            }
            if (cuenta >= aux.size()) {
                cuenta = 0;
                aux.remove(0);
                // Ir al estado CONTROL
                nextState = 0;
                return;
            }
            print("\n\t|| " + aux.get(0).getLocalName() + " vs " + aux.get(cuenta).getLocalName() + " ||");
            // Ir a estado COMPETIR
            nextState = 1;
        }

        public int onEnd() {
            return nextState;
        }
    }

    private class Competir extends OneShotBehaviour {

        private ACLMessage msg;
        private AID a1, a2;
        int a1_last, a1_res, a2_last, a2_res;

        private final int[][][] tablaRef = {
            {{1, 1}, {3, 0}},
            {{0, 3}, {2, 2}}
        };

        @Override
        public void action() {
            a1 = aux.get(0);
            a1_last = 1;
            a1_res = 0;
            a2 = aux.get(cuenta);
            a2_last = 1;
            a2_res = 0;

            for (int i = 0; i < NUM_RONDAS; i++) {
                // Enviar mensajes
                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(a1);
                msg.setContent(i + " " + a2_last);
                send(msg);

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(a2);
                msg.setContent(i + " " + a1_last);
                send(msg);

                // Recibir respuestas
                msg = blockingReceive(5000);
                if (msg != null) {
                    if (msg.getSender().equals(a1)) {
                        a1_last = Integer.parseInt(msg.getContent());
                    } else {
                        a2_last = Integer.parseInt(msg.getContent());
                    }
                } else {
                    print("El agente no responde. Terminando torneo.");
                    doDelete();
                }

                msg = blockingReceive(5000);
                if (msg != null) {
                    if (msg.getSender().equals(a1)) {
                        a1_last = Integer.parseInt(msg.getContent());
                    } else {
                        a2_last = Integer.parseInt(msg.getContent());
                    }
                } else {
                    print("El agente no responde. Terminando torneo.");
                    doDelete();
                }

                //Añadir resultados
                print("\n\tRonda " + i + ":\n\t\t" + a1.getLocalName() + " -> " + a1_last + "\n\t\t" + a2.getLocalName() + " -> " + a2_last);
                a1_res += tablaRef[a1_last][a2_last][0];
                a2_res += tablaRef[a1_last][a2_last][1];
            }

            print("\n\tRes :\n\t\t" + a1.getLocalName() + " -> " + a1_res + "\n\t\t" + a2.getLocalName() + " -> " + a2_res);
            int i = agentes.indexOf(a1);
            resultados.set(i, resultados.get(i) + a1_res);
            i = agentes.indexOf(a2);
            resultados.set(i, resultados.get(i) + a2_res);
        }
    }

    private class Resultados extends OneShotBehaviour {

        public void action() {
            ACLMessage msg;
            print("RESULTADOS:");
            for (int i = 0; i < agentes.size(); i++) {
                print("\t" + agentes.get(i).getLocalName() + "\t" + resultados.get(i));
            }
            for (int i = 0; i < agentes.size(); i++) {
                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(agentes.get(i));
                msg.setContent("STOP");
                send(msg);
            }
        }
    }

    private void print(String s) {
        System.out.println(getLocalName() + " >> " + s);
    }
}
