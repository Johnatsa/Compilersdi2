import java.util.*;
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

    //* Main Class
    /**
     * Grammar production:
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
    public String visit(MainClass n, MethodInfo m) throws Exception {
        this.currentClass = n.f1.f0.toString();
        
        n.f15.accept(this, null); 

        return null;
    }


    //! Statements

    //*Assignment statement
    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, MethodInfo m) throws Exception{
        String left = n.f0.accept(this, m);
        String right = n.f2.accept(this, m);

        if(!isSubtype(right, left))
            throw new Exception("Type error: Can't assign " + right + " to " + left);
        return null;
    }

    //*Array assignment statement
    /**
     * Grammar production:
     * f0 -> Identifier()j
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public String visit(ArrayAssignmentStatement n, MethodInfo m) throws Exception{
        String arrayType = n.f0.accept(this, m);
        String indexType = n.f2.accept(this, m);
        String valueType = n.f5.accept(this, m);

        if(!arrayType.equals("int[]"))
            throw new Exception("Type error: All arrays must be of type 'int[]");
        if(!indexType.equals("int"))
            throw new Exception("Type error: Can't use non 'int' to index an array");
        if(!valueType.equals("int"))
            throw new Exception("Type error: Arrays must contain only 'int'");
        return null;
    }   

    //*If statement
    /**
     * Grammar production:
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public String visit(IfStatement n, MethodInfo m) throws Exception{
        String ExprType = n.f2.accept(this, m);

        if(!ExprType.equals("boolean"))
            throw new Exception("Type error: Condition must be of type 'boolean'");
        n.f4.accept(this, m);
        n.f6.accept(this, m);
        return null;
    }

    //*While statement
    /**
     * Grammar production:
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public String visit(WhileStatement n, MethodInfo m) throws Exception{
        String ExprType = n.f2.accept(this, m);
    
        if(!ExprType.equals("boolean"))
            throw new Exception("Type error: Condition must be of type 'boolean'");  
        n.f4.accept(this, m);
        return null;
    }

    //*Print statement
    /**
     * Grammar production:
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public String visit(PrintStatement n, MethodInfo m) throws Exception{
        String ExprType = n.f2.accept(this, m);

        if(!ExprType.equals("int"))
            throw new Exception("Type error: Can only print objects of type'int'");
        return null;
    }


    //! Expressions

    //*Bracket expression
    @Override 
    public String visit(BracketExpression n, MethodInfo m) throws Exception{
        return n.f1.accept(this, m);
    }

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
            throw new Exception("Type error: Can't perform '*' with non 'int' types");

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
        return "int";
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
        
        //Treat type as a classname
        ClassInfo searchClass = this.globalTable.find_class(type);
        String method_name = n.f2.f0.toString();

        if(searchClass == null)
            throw new Exception("Semantic error: Object doesn't exist");
        List<String> argTypes = new ArrayList<>();
        if(n.f4.present()){
            ExpressionList exprList= (ExpressionList) n.f4.node;
            argTypes.add(exprList.f0.accept(this, m)); //first arg

            if(exprList.f1.f0.present()){ //Remainder args
                for(int i = 0; i < exprList.f1.f0.nodes.size(); i++){
                    ExpressionTerm rest = (ExpressionTerm) exprList.f1.f0.nodes.get(i);
                    argTypes.add(rest.f1.accept(this, m));
                }
            }
        }
        MethodInfo matchedMethod = null;

        ClassInfo currClass = searchClass;
        while (currClass != null && matchedMethod == null) {
            for (MethodInfo method : currClass.methods.values()) {
                if (method.name.equals(method_name) && method.params.size() == argTypes.size()) {
                    boolean isValidMatch = true;
                    List<VariableInfo> expectedParams = new ArrayList<>(method.params.values());
                    
                    for (int i = 0; i < argTypes.size(); i++) {
                        if (!isSubtype(argTypes.get(i), expectedParams.get(i).type)) {
                            isValidMatch = false;
                            break;
                        }
                    }
                    if (isValidMatch) {
                        matchedMethod = method;
                        break;
                    }
                }
            }
            
            if (matchedMethod == null && currClass.parent != null) 
                currClass = globalTable.find_class(currClass.parent);
            else 
                currClass = null; 
            
        }

        if(matchedMethod == null)
            throw new Exception("Type Error: No matching method found for call '" + method_name + "' with the provided arguments in class " + type);

        return matchedMethod.retype;
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
        
        List<String> declaredParamTypes = new ArrayList<>();
        if(n.f4.present()) {
            FormalParameterList fpl = (FormalParameterList) n.f4.node;
            declaredParamTypes.add(getTypeString(fpl.f0.f0));
            if(fpl.f1.f0.present()) {
                FormalParameterTail tail = (FormalParameterTail) fpl.f1;
                for(Node node : tail.f0.nodes) {
                    FormalParameterTerm term = (FormalParameterTerm) node;
                    declaredParamTypes.add(getTypeString(term.f1.f0));
                }
            } 
        }
        
        StringBuilder sigBuilder = new StringBuilder(name);
        for(String t : declaredParamTypes) sigBuilder.append("_").append(t);
        String mySignature = sigBuilder.toString();
        
        MethodInfo currentMethodInfo = currentClassInfo.methods.get(mySignature);

        if (currentMethodInfo == null) {
            throw new Exception("Compiler Bug: Could not find method '" + mySignature + "' in Symbol Table!");
        }
        
        ClassInfo searchClass = currentClassInfo;
        while(searchClass != null) {
            for(MethodInfo otherMethod : searchClass.methods.values()) {
                // If it's a different method but shares the same name and same parameter count
                if (otherMethod != currentMethodInfo && otherMethod.name.equals(name)) {
                    if (otherMethod.params.size() == currentMethodInfo.params.size()) {
                        
                        boolean exactMatch = true;
                        boolean allSubOrSuper = true;
                        
                        List<VariableInfo> myParams = new ArrayList<>(currentMethodInfo.params.values());
                        List<VariableInfo> otherParams = new ArrayList<>(otherMethod.params.values());
                        
                        for(int i = 0; i < myParams.size(); i++) {
                            String t1 = myParams.get(i).type;
                            String t2 = otherParams.get(i).type;
                            
                            if (!t1.equals(t2)) exactMatch = false;
                            
                            if (!isSubtype(t1, t2) && !isSubtype(t2, t1)) {
                                allSubOrSuper = false; 
                            }
                        }
                        
                        if(allSubOrSuper) {
                            if (exactMatch && searchClass != currentClassInfo) {
                                if (!currentMethodInfo.retype.equals(otherMethod.retype)) {
                                    throw new Exception("Semantic Error: Overriding method '" + name + "' must have same return type as parent.");
                                }
                            } else {
                                throw new Exception("Semantic Error: Invalid overloading for method '" + name + "'. Arguments have a strict subtype/supertype relationship.");
                            }
                        }
                    }
                }
            }
            searchClass = searchClass.parent != null ? this.globalTable.find_class(searchClass.parent) : null;
        }

        // 3. Finally, visit the statements and check the return type
        n.f8.accept(this, currentMethodInfo);

        String returnExpressionType = n.f10.accept(this, currentMethodInfo);
        if (!isSubtype(returnExpressionType, currentMethodInfo.retype)) 
            throw new Exception("Type Error in method " + name + ": Cannot return '" + returnExpressionType + "' when expecting '" + currentMethodInfo.retype + "'");

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

    //Checks for subtypes: If a subclass is named with the name of the parent
    public boolean isSubtype(String child, String parent){
        if(child.equals(parent))
            return true;

        if (child.equals("int") || child.equals("boolean") || child.equals("int[]") || 
            parent.equals("int") || parent.equals("boolean") || parent.equals("int[]")) {
            return false;
        }

        ClassInfo searchClass = globalTable.find_class(child);
    
        while (searchClass != null && searchClass.parent != null) {
            if (searchClass.parent.equals(parent)) 
                return true;
            
            searchClass = globalTable.find_class(searchClass.parent);
        }

        return false;
    }
    
    //Evaluates primitive types. Helpful for method declaration instead of using more visitors.
    public String getTypeString(Type t) {
        Node choice = t.f0.choice;
        if (choice instanceof ArrayType) return "int[]";
        if (choice instanceof BooleanType) return "boolean";
        if (choice instanceof IntegerType) return "int";
        if (choice instanceof Identifier) return ((Identifier)choice).f0.toString();
        return "";
    }
}

