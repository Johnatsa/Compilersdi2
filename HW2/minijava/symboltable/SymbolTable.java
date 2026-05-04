package symboltable;
import java.util.LinkedHashMap;
import java.util.Map;   

public class SymbolTable {
    //Fields
    Map<String, ClassInfo> classes; 

    //Methods
    public SymbolTable(){
        this.classes = new LinkedHashMap<>();
    }

    public void add(String name, ClassInfo c){
        classes.put(name, c);
    }

    public ClassInfo find_class(String name){
        return classes.get(name);
    }
}