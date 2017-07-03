# run using the script: ./cloneDigger.sh ./config.xml

JAVAC = javac
JAVAFLAGS = -cp "./lib/*:./lib2/stanford-corenlp-3.8.0.jar:."

# Builds the classes' jar file
all:
	$(JAVAC) $(JAVAFLAGS) *.java

clean:
	rm -rf *.class

