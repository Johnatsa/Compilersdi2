import syntaxtree.*;
import visitor.*;
import symboltable.*;


class SymbolTableBuilder extends GJDepthFirst<Object, SymbolTable>{
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, SymbolTable st) throws Exception {
        String classname = (String) n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        super.visit(n, st);

        System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, SymbolTable st) throws Exception {
        n.f0.accept(this, null);
        
        String classname = (String) n.f1.accept(this, st);
        System.out.println("Class: " + classname);
        
        ClassInfo c = new ClassInfo(classname, null);

        n.f2.accept(this, null);
        
        System.out.println("Fields: ");
        if (n.f3.present()) { 
            for (int i = 0; i < n.f3.nodes.size(); i++) {
                VariableInfo v = (VariableInfo) n.f3.nodes.get(i).accept(this, st); 
                c.addField(v);
            }
        }
        
        System.out.println("Methods: ");
        if (n.f4.present()) { 
            for (int i = 0; i < n.f4.nodes.size(); i++) {
                MethodInfo m = (MethodInfo) n.f4.nodes.get(i).accept(this, st); 
                c.addMethod(m);
            }
        }

        n.f5.accept(this, null);

        st.add(classname, c);
        System.out.println();
    
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, SymbolTable st) throws Exception {
        n.f0.accept(this, null);

        String classname = (String) n.f1.accept(this, st);
        System.out.println("Class: " + classname);

        n.f2.accept(this, null);
        String parentname = (String) n.f3.accept(this, st);

        ClassInfo c = new ClassInfo(classname, parentname);

        n.f4.accept(this, null);
        
        System.out.println("Fields: ");
        if (n.f5.present()) { 
            for (int i = 0; i < n.f5.nodes.size(); i++) {
                VariableInfo v = (VariableInfo) n.f5.nodes.get(i).accept(this, st); 
                c.addField(v);
            }
        }
        
        System.out.println("Methods: ");
        if (n.f6.present()) { 
            for (int i = 0; i < n.f6.nodes.size(); i++) {
                MethodInfo m = (MethodInfo) n.f6.nodes.get(i).accept(this, st); 
                c.addMethod(m);
            }
        }

        n.f7.accept(this, null);

        st.add(classname, c);
        System.out.println();

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public VariableInfo visit(VarDeclaration n, SymbolTable st) throws Exception {
        String type = (String) n.f0.accept(this, st);
        String var = (String) n.f1.accept(this, st);
        System.out.println(var + " " + type);
        
        VariableInfo ret = new VariableInfo(type, var);
        n.f2.accept(this, null);
        return ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public MethodInfo visit(MethodDeclaration n, SymbolTable st) throws Exception {
        String myType = (String) n.f1.accept(this, st);
        String myName = (String) n.f2.accept(this, st);

        System.out.println("Method: " + myType + " " + myName);
        System.out.println("Local vars:");

        n.f3.accept(this, null);
        
        Map<String, VariableInfo> params = new LinkedHashMap<>();
        params = (Map<String, VariableInfo>) n.f4.accept(this, st);

        n.f5.accept(this, null);
        n.f6.accept(this, null);
        
        Map<String, VariableInfo> locals = new LinkedHashMap<>();
        
        if (n.f7.present()) { 
            for (int i = 0; i < n.f7.nodes.size(); i++) {
                VariableInfo v = (VariableInfo) n.f7.nodes.get(i).accept(this, st); 
                locals.put(v.name, v);
            }
        }

        n.f8.accept(this, null);
        n.f9.accept(this, null);
        n.f10.accept(this, null);
        n.f11.accept(this, null);
        n.f12.accept(this, null);

        MethodInfo m = new MethodInfo(myName, myType, params, locals);
        return m;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public Map<String, VariableInfo> visit(FormalParameterList n, SymbolTable st) throws Exception {
        Map<String, VariableInfo> ret = new LinkedHashMap<>();
        VariableInfo v = (VariableInfo) n.f0.accept(this, null);
        ret.put(v.name, v);

        if (n.f1 != null) {
            Map<String, VariableInfo> tailMap = (Map<String, VariableInfo>) n.f1.accept(this, st);
            ret.putAll(tailMap);
        }

        return ret;
    }

    /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
    public VariableInfo visit(FormalParameterTerm n, SymbolTable st) throws Exception {
        return (VariableInfo) n.f1.accept(this, st);
    }

    /**
    * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public Map<String, VariableInfo> visit(FormalParameterTail n, SymbolTable st) throws Exception {
        Map<String, VariableInfo> ret = new LinkedHashMap<>();
        for ( Node node: n.f0.nodes) {
            VariableInfo v = (VariableInfo) node.accept(this, null);
            ret.put(v.name, v);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public VariableInfo visit(FormalParameter n, SymbolTable st) throws Exception{
        String type = (String) n.f0.accept(this, null);
        String name = (String) n.f1.accept(this, null);
        VariableInfo v = new VariableInfo(type, name);
        return v;
    }

    @Override
    public String visit(ArrayType n, SymbolTable st) {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, SymbolTable st) {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, SymbolTable st) {
        return "int";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, SymbolTable st) {
        return n.f0.toString();
    }
}