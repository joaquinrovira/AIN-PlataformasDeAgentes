/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torneo;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ximo
 */
public class Agente extends Agent {

    Strategy s;
    private FileWriter fw;
    private final String INICIO = "INICIO",
            CONTROL = "CONTROL",
            FIN = "FIN";

    public class Strategy {

        public int calc(int roundNum, int last) {
            return 0;
        }
    }

    public void setup() {
        try {
            fw = new FileWriter(getLocalName() + ".txt");
        } catch (IOException e) {
            System.out.println(e);
        }
        print("STARTED");

        // Definir la Maquina de Estados Finitos
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                try {
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(TournamentManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                doDelete();
                return super.onEnd();
            }
        };

        // Registrar los estados
        fsm.registerFirstState(new Inicio(), INICIO);
        fsm.registerState(new Control(), CONTROL);
        fsm.registerLastState(new Fin(), FIN);

        // Transiciones entre estados
        fsm.registerDefaultTransition(INICIO, CONTROL);
        fsm.registerTransition(CONTROL, CONTROL, 0);
        fsm.registerTransition(CONTROL, FIN, 1);

        addBehaviour(fsm);
    }

    private class Inicio extends OneShotBehaviour {

        public void action() {
            AID tm = new AID("TournamentManager", AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tm);
            msg.setContent("");
            send(msg);
        }
    }

    private class Control extends OneShotBehaviour {
        int nextState = 0;
        ACLMessage msg;

        public void action() {
            msg = blockingReceive();
            print(">> " + msg.getContent());
            if (msg.getContent().equals("STOP")) {
                nextState = 1;
                return;
            }
            String[] args = msg.getContent().split(" ");
            AID tm = msg.getSender();
            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tm);
            msg.setContent("" + s.calc(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            send(msg);
        }
        public int onEnd() {
            return nextState;
        }
    }

    private class Fin extends OneShotBehaviour {

        public void action() {
            print("Terminado.");
        }
    }

    private void print(String s) {
        try {
            fw.write(s + "\n");
        } catch (IOException ex) {
            Logger.getLogger(TournamentManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
