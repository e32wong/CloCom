# run using the script: ./cloneDigger.sh ./config.xml

JAVAC = javac
JAVAFLAGS = -cp "./lib/*:./lib2/stanford-corenlp-3.8.0.jar:."
JAVAFLAGS2 = -J-agentlib:hprof=heap=sites -cp "./lib/*:./lib2/stanford-corenlp-3.8.0.jar:."


# Builds the classes' jar file
all:
	$(JAVAC) $(JAVAFLAGS) *.java

debug:
	$(JAVAC) $(JAVAFLAGS2) *.java

clean:
	rm -rf *.class

