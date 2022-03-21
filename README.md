To run the CLI:
	(optional) I already submitted the jar file on the root directory. But you can build the maven project and take the jar from the target directory.
	1) Set the JAVA_HOME on your PATH if it not already set.
	2) Make sure githubCLI file and 'whitesource-github-cli-0.0.1-SNAPSHOT.jar' (both are located at root directory of the project) are on the same directory.
	3) Run the CLI, for example:
			sh githubCLI downloads -r "whitesource/log4j-detect-distribution" -o "test2.txt"