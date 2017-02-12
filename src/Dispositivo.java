import net.jini.core.entry.Entry;

/**
 * Created by yurireis on 10/02/17.
 */

public class Dispositivo implements Entry{
    public String name;

    public Dispositivo(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dispositivo that = (Dispositivo) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
