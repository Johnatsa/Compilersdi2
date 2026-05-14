package symboltable;
import java.util.LinkedHashMap;
import java.util.Map;


public class ClassInfo {   
    //Fields
    
    //Class info
    public String name;
    public String parent;
    public Map<String, VariableInfo> fields;
    public Map<String, MethodInfo> methods;

    //Offsets
    public int currFieldOffset;
    public int currMethodOffset;
    public Map<String, Integer> fieldOffsets;
    public Map<String, Integer> methodOffsets;

    //Methods
    public ClassInfo(String n, String p){
        this.name = n; 
        this.parent = p;
        fields = new LinkedHashMap<>();
        methods = new LinkedHashMap<>();
        fieldOffsets = new LinkedHashMap<>();
        methodOffsets = new LinkedHashMap<>();
    }

    public void addField(VariableInfo var){
        fields.put(var.name, var);
    }

    public void addParent(String name){
        this.parent = name;
    }

    public void addMethod(MethodInfo met){
        methods.put(met.getSignature(), met);
    }
}


