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
public class Random extends Agente {

    public Random() {
        super();
        Strategy s = new Strategy() {
            public int calc(int roundNum, int last) {
                return (int) Math.round(Math.random());
            }

        };
        this.s = s;
    }
}
