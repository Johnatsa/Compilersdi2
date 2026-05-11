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

    //! Expressions

    //*Plus expression
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, MethodInfo m) throws Exception{
        String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!left.equals("int") || !right.equals("int"))
            throw new Exception("Type error: Can't perform '+' with non 'int' types");

        return "int";
    }

    //*Minus expression
    @Override
    public String visit(MinusExpression n, MethodInfo m) throws Exception{
         String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!left.equals("int") || !right.equals("int"))
            throw new Exception("Type error: Can't perform '-' with non 'int' types");

        return "int";       
    }

    //*Times expression
    @Override
    public String visit(TimesExpression n, MethodInfo m) throws Exception{
        String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!left.equals("int") || !right.equals("int"))
            throw new Exception("Type error: Can't perform '-' with non 'int' types");

        return "int";              
    }

    //*Compare expression
    @Override
    public String visit(CompareExpression n, MethodInfo m) throws Exception{
        String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!left.equals("int") || !right.equals("int"))
            throw new Exception("Type error: Can't perform '<' with non 'int' types");

        return "boolean"; 
    }

    //*And expression
    @Override
    public String visit(AndExpression n, MethodInfo m) throws Exception{
        String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!left.equals("boolean") || !right.equals("boolean"))
            throw new Exception("Type error: Can't perform '&&' with non 'boolean' types");

        return "boolean"; 
    }

    //*Not expression
    @Override
    public String visit(NotExpression n, MethodInfo m) throws Exception {
        String middle = n.f1.accept(this, m);

        if (!middle.equals("boolean")) {
            throw new Exception("Type Error: Can't perform '!' with non 'boolean' type");
        }

        return "boolean";
    }

    //*Array lookup
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, MethodInfo m) throws Exception{
        String type = n.f0.accept(this, m);
        String index = n.f2.accept(this, m);

        if(!index.equals("int"))
            throw new Exception("Type error: Array index must be of type 'int'");

        if(type.equals("int[]"))
            return "int";

        throw new Exception("Type Error: Cannot use [] on non-array type");
    }

    //*Array length
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, MethodInfo m) throws Exception{
        String type = n.f0.accept(this, m);
        if(!type.equals("int[]")) throw new Exception(".length requires type 'int[]'");
        return "int"
    }

    //*Message send
    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, MethodInfo m) throws Exception{
        String type = n.f0.accept(this, m);

        if(type.equals("int") || type.equals("boolean") || type.equals("int[]"))
            throw new Exception("Type error: Can't call a method on a primitive type");
        
        //Treat the type as a classname
        boolean found = false;
        ClassInfo searchClass = this.globalTable.find_class(type);
        String method_name = n.f2.f0.toString();

        if(searchClass == null)
            throw new Exception("Semantic error: Object doesn't exist");

        while (searchClass != null) {
            if(searchClass.methods.containsKey(method_name)){
                found = true;
                break;
            }

            if(searchClass.parent != null) 
                searchClass = globalTable.find_class(searchClass.parent);
            else 
                searchClass = null;   
        }

        if(found == false)
            throw new Exception("Semantic Error: class " + type + "doesn't contain method" + method_name);

        MethodInfo method = searchClass.methods.get(method_name);

        //Parameters we expect
        List<VariableInfo> expectedParams = new ArrayList<>(method.params.values());
        int expectedCount = expectedParams.size();
        int actualCount = 0;

        if (n.f4.present()) {
            ExpressionList exprList = (ExpressionList) n.f4.node;

            //Evaulate first parameter
            String firstArgType = exprList.f0.accept(this, m);
            actualCount++;

            //Check the counts
            if (actualCount > expectedCount) {
                throw new Exception("Type Error: Too many arguments for method " + method_name);
            }

            //Type check first argument
            if (!isSubtype(firstArgType, expectedParams.get(0).type)) {
                throw new Exception("Type Error: Method " + method_name + 
                                    " argument 1 expected " + expectedParams.get(0).type + 
                                    " but got " + firstArgType);
            }

            //Rest of the arguments
            if (exprList.f1.present()) {
                for (int i = 0; i < exprList.f1.nodes.size(); i++) {
                    ExpressionRest rest = (ExpressionRest) exprList.f1.nodes.get(i);

                    String nextArgType = rest.f1.accept(this, m);
                    actualCount++;

                    if (actualCount > expectedCount) {
                        throw new Exception("Type Error: Too many arguments for method " + methodName);
                    }

                    if (!isSubtype(nextArgType, expectedParams.get(actualCount - 1).type)) {
                        throw new Exception("Type Error: Method " + methodName + " argument " + actualCount + 
                                            " expected " + expectedParams.get(actualCount - 1).type + 
                                            " but got " + nextArgType);
                    }
                }
            }
        }

        //Count check
        if (actualCount < expectedCount) {
            throw new Exception("Type Error: Not enough arguments for method " + methodName + 
                                ". Expected " + expectedCount + " but got " + actualCount);
        }
        return calledMethod.retype;
    }


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
    
    //* Array Allocation Expression
    @Override
    public String visit(ArrayAllocationExpression n, MethodInfo m) throws Exception{
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

