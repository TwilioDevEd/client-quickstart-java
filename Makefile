install:
	mvn package

serve:
	. .env
	java -jar target/client-quickstart-1.0-SNAPSHOT.jar
