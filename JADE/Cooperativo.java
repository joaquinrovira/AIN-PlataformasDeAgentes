/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torneo;

/**
 *
 * @author Ximo
 */
public class Cooperativo extends Agente {

    public Cooperativo() {
        super();
        Strategy s = new Strategy() {
            public int calc(int roundNum, int last) {
                return 1;
            }

        };
        this.s = s;
    }
}
