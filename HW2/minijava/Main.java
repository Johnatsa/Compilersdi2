import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;
import symboltable.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ... ");
            System.exit(1);
        }


        for(int i = 0; i < args.length; i++){
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");
                
                //1st visitor pass
                SymbolTableBuilder builder = new SymbolTableBuilder();
                SymbolTable st = new SymbolTable();
                root.accept(builder, st);
                st.calculateOffsets();

                //2nd visitor pass
                TypeChecker checker = new TypeChecker(st);
                root.accept(checker, null);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}