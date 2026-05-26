# Compilersdi2

For this project I implemented two visitor passes:
-Visitor pass 1: Iterates through the syntax tree and creates the symbol table (SymbolTableBuilder.java)
-Visotor pass 2: Iterates through the syntax tree and according to the symbol table does the type checking (TypeChecker.java)

Execution:
cd HW2/minijava
make
java Main [File1] [File2] ...