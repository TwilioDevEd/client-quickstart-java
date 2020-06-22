.PHONY: install serve

install:
	mvn package

serve:
	java -jar target/client-quickstart-1.0-SNAPSHOT.jar
