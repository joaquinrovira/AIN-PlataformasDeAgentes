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
public class OjoPorOjo extends Agente {

    public OjoPorOjo() {
        super();
        Strategy s = new Strategy() {
            public int calc(int roundNum, int last) {
                if(roundNum == 0) return 1;
                else return last;
            }
        };
        this.s = s;
    }
}
