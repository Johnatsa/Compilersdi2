import syntaxtree.*;
import visitor.*;
import symboltable.*;

class TypeChecker extends GJDepthFirst<String, MethodInfo>{
    private SymbolTable globalTable;
    private String currentClass;
    // 2. Pass it in via the constructor
    public TypeCheckVisitor(SymbolTable st) {
        this.globalTable = st;
    }

    //TODO Use class decl to implement currentClass 


    @Override
    public String visit(Identifier n, MethodInfo m) throws Exception{
        String name = n.f0.toString();

        //Local variables
        if (m != null && m.localvars.containsKey(name)) {
            return m.localvars.get(name).type;
        }

        //Parameters
        if (m != null && m.params.containsKey(name)) {
            return m.params.get(name).type;
        }

        ClassInfo searchClass = globalTable.find_class(this.currentClassName);
        while (searchClass != null) {
            if(searchClass.fields.containsKey(name))
                return searchClass.fields.get(name).type;

            if(searchClass.parent != null) 
                searchClass = globalTable.find_class(searchClass.parent);
            else 
                searchClass = null;
        }

        throw new Exception("Semantic Error: Undeclared variable '" + name + "' in class " + currentClassName);
    }
    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, MethodInfo m) throws Exception{
        String object = n.f1.f0.toString();

        if(globalTable.find_class(object) == NULL)
            throw new Exception("Semantic Error: There is no object named: " + object);
        else
            return object;
    }

    //! Leaf Nodes:
    @Override
    public String visit(IntegerLiteral n, MethodInfo m) throws Exception{
        return "int";
    }

    @Override 
    public String visit(TrueLiteral n, MethodInfo m) throws Exception{
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, MethodInfo m) throws Exception{
        return "boolean";
    }

    @Override
    public String visit(ThisExpression n, MethodInfo m) throws Exception{
        return this.currentClass;
    }

    @Override
    public String visit(BooleanArrayAllocationExpression n, MethodInfo m) throws Exception{
        return "boolean";
    }

    @Override 
    public String visit(IntegerArrayAllocationExpression n, MethodInfo m) throws Exception{
        return "integer";
    }
}