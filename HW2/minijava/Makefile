all: compile

compile:
	java -jar ../jtb133di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac *.java symboltable/*.java

clean:
	rm -rf *.class symboltable/*.class syntaxtree visitor minijava-jtb.jj *~