
import org.steam.LoopThread;
import org.steam.Produto;
import org.steam.api.UserAPI;

import java.util.Arrays;
import java.util.Comparator;


public class Main {
    public static void main(String[] args) {

        Produto[] listajogos = UserAPI.getJogos("76561198241059866");

        Arrays.sort(listajogos, Comparator.comparing(Produto::getDiasSemJogar).reversed());

        LoopThread t0 = new LoopThread();
        t0.setI(0);
        t0.setListaJogos(listajogos);

        LoopThread t1 = new LoopThread();
        t1.setI(1);
        t1.setListaJogos(listajogos);

        t0.start();
        t1.start();


    }
}
