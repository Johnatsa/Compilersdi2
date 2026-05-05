package symboltable;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodInfo {
    //Fields
    public String name;
    public String retype;
    public Map<String, VariableInfo> params;
    public Map<String, VariableInfo> localvars;

    //Methods
    public MethodInfo(String n, String r){
        this.name = n; 
        this.retype = r;
        params = new LinkedHashMap<>();
        localvars = new LinkedHashMap<>();
    }

    public MethodInfo(String n, String r, Map<String, VariableInfo> p){
        this.name = n; 
        this.retype = r;
        this.params = p
        localvars = new LinkedHashMap<>();
    }

    public MethodInfo(String n, String r, Map<String, VariableInfo> p, Map<String, VariableInfo> l){
        this.name = n; 
        this.retype = r;
        this.params = p;
        this.localvars = l;
    }


    public void addParam(VariableInfo var){
        params.put(var.name, var);
    }

    public void addLocal(VariableInfo var){
        localvars.put(var.name, var);
    }
}