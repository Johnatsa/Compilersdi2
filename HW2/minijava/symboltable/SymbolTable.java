package symboltable;
import java.util.LinkedHashMap;
import java.util.Map;   

public class SymbolTable {
    //Fields
    public Map<String, ClassInfo> classes; 

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

    public void calculateOffsets(){
        for(ClassInfo c : this.classes.values()){
            //Check if class has a parent
            if(c.parent != null){
                ClassInfo parentClass = this.find_class(c.parent);
                c.currFieldOffset = parentClass.currFieldOffset;
                c.currMethodOffset = parentClass.currMethodOffset;
            }

            //Iterate through the fields of the class
            for(VariableInfo v : c.fields.values()){
                c.fieldOffsets.put(v.name, c.currFieldOffset);
                System.out.println(c.name + "." + v.name + " : " + c.currFieldOffset);

                if(v.type.equals("int"))
                    c.currFieldOffset += 4;
                else if(v.type.equals("boolean"))
                    c.currFieldOffset += 1;
                else
                    c.currFieldOffset += 8;
            }
        
            //Iterate through the methods of the class
            for(MethodInfo m : c.methods.values()){
                //Check if this method overides a parent method
                boolean isOverride = false;
                
                //If parent exists
                ClassInfo searchClass = c.parent != null ? this.find_class(c.parent) : null;
                while(searchClass != null){
                    if(searchClass.methods.containsKey(m.getSignature())){
                        isOverride = true;
                        
                        int inheritedOffset = searchClass.methodOffsets.get(m.getSignature());
                        c.methodOffsets.put(m.getSignature(), inheritedOffset);
                        break;

                    }
                    //Iterate through all the parents
                    searchClass = searchClass.parent != null ? this.find_class(searchClass.parent) : null;
                }

                if (!isOverride) {
                    c.methodOffsets.put(m.getSignature(), c.currMethodOffset);
                    System.out.println(c.name + "." + m.name + " : " + c.currMethodOffset);
                    c.currMethodOffset += 8;
                }
            } 
        }
    }
}