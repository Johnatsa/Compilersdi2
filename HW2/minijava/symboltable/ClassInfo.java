package symboltable;
import java.util.LinkedHashMap;
import java.util.Map;


public class ClassInfo {   
    //Fields
    String name;
    String parent;
    Map<String, VariableInfo> fields;
    Map<String, MethodInfo> methods;

    public ClassInfo(String n, String p){
        this.name = n; 
        this.parent = p;
        fields = new LinkedHashMap<>();
        methods = new LinkedHashMap<>();
    }

    public void addField(VariableInfo var){
        fields.put(var.name, var);
    }

    public void addParent(String name){
        this.parent = name;
    }

    public void addMethod(MethodInfo met){
        methods.put(met.name, met);
    }
}


