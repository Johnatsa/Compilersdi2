import syntaxtree.*;
import visitor.*;
import symboltable.*;

class TypeChecker extends GJDepthFirst<String, MethodInfo>{
    private SymbolTable globalTable;
    private String currentClass;
    // 2. Pass it in via the constructor
    public TypeChecker(SymbolTable st) {
        this.globalTable = st;
    }

    //TODO Use class decl to implement currentClass 

    //! Declarations:

    //* Class declaration
    /**
     * Grammar production:
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, MethodInfo m) throws Exception{
        this.currentClass = n.f1.f0.toString();
        n.f3.accept(this, m);
        n.f4.accept(this, m);
        return null;
    }

    //*Class extends declaration
    /**
     * Grammar production:
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
    public String visit(ClassExtendsDeclaration n, MethodInfo m) throws Exception{
        this.currentClass = n.f1.f0.toString();
        String parent = n.f3.f0.toString();
        this.globalTable.find_class(this.currentClass).addParent(parent);
        n.f5.accept(this, m);
        n.f6.accept(this, m);
        return null;
    }

    //*Method declaration
    /**
     * Grammar production:
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
    public String visit(MethodDeclaration n, MethodInfo m) throws Exception{
        String name = n.f2.f0.toString();
        
        ClassInfo currentClassInfo = this.globalTable.find_class(this.currentClass);
        MethodInfo currentMethodInfo = currentClassInfo.methods.get(name);

        n.f8.accept(this, currentMethodInfo);

        String returnExpressionType = n.f10.accept(this, currentMethodInfo);
        String declared = currentMethodInfo.retype;

        if (!isSubtype(returnExpressionType, declaredReturnType)) 
            throw new Exception("Type Error in method " + methodName + ": Cannot return " + returnExpressionType + " when expecting " + declaredReturnType);

        return null;
    }


    //! Leaf Nodes:
    //*Identifier
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

        ClassInfo searchClass = globalTable.find_class(this.currentClass);
        while (searchClass != null) {
            if(searchClass.fields.containsKey(name))
                return searchClass.fields.get(name).type;

            if(searchClass.parent != null) 
                searchClass = globalTable.find_class(searchClass.parent);
            else 
                searchClass = null;   
        }

        throw new Exception("Semantic Error: Undeclared variable '" + name + "' in class " + currentClass);
    }

    //*Allocation Expression
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

        if(globalTable.find_class(object) == null)
            throw new Exception("Semantic Error: There is no object named: " + object);
        else
            return object;
    }

    //*Integer
    @Override
    public String visit(IntegerLiteral n, MethodInfo m) throws Exception{
        return "int";
    }

    //*True
    @Override 
    public String visit(TrueLiteral n, MethodInfo m) throws Exception{
        return "boolean";
    }

    //*False
    @Override
    public String visit(FalseLiteral n, MethodInfo m) throws Exception{
        return "boolean";
    }

    //*This expression
    @Override
    public String visit(ThisExpression n, MethodInfo m) throws Exception{
        return this.currentClass;
    }

    //*Boolean array
    @Override
    public String visit(BooleanArrayAllocationExpression n, MethodInfo m) throws Exception{
        return "boolean[]";
    }

    //*Integer array
    @Override 
    public String visit(IntegerArrayAllocationExpression n, MethodInfo m) throws Exception{
        return "int[]";
    }
    
    //*Helper functions
    public boolean isSubtype(String child, String parent){
        if(child.equals(parent))
            return true;

        if (child.equals("int") || child.equals("boolean") || child.equals("int[]") || child.equals("boolean[]")
            parent.equals("int") || parent.equals("boolean") || parent.equals("int[]") || parent.equals("boolean[]")) {
            return false;
        }

        ClassInfo searchClass = globalTable.find_class(child);
    
        while (searchClass != null && searchClass.parent != null) {
            if (searchClass.parent.equals(parentType)) 
                return true;
            
            searchClass = globalTable.find_class(searchClass.parent);
        }

        return false;
    }
}

