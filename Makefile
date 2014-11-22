# run using the script: ./cloneDigger.sh ./config.xml

JAVAC = javac
JAVAFLAGS = -cp "./lib/*:."

# Builds the classes' jar file
all:
	$(JAVAC) $(JAVAFLAGS) *.java

clean:
	rm -rf *.class

