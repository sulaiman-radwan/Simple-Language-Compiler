package symbol;

import java.util.Hashtable;

public class Environment {
    private Environment previous;
    private Hashtable table;

    public Environment(Environment previous) {
        table = new Hashtable();
        this.previous = previous;
    }

    public void put(String s, Symbol symbol) {
        table.put(s, symbol);
    }

    public Symbol get(String s) {
        for (Environment e = this; e != null; e = e.previous){
            Symbol found = (Symbol) (e.table.get(s));
            if (found != null)return found;
        }
        return null;
    }
}
