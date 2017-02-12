import net.jini.core.entry.Entry;

import java.util.List;

/**
 * Created by yurireis on 10/02/17.
 */

public class Ambiente implements Entry {
    public String name;
    public List<Dispositivo> devices;

    public Ambiente(){}

    public Ambiente(String name){
        this.name = name;
    }
}
