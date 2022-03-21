To run the CLI:
1) Build the maven project.
2) Set the JAVA_HOME on your PATH if it not already set.
3) Take githubCLI (located at root directory of the project) file and 'whitesource-github-cli-0.0.1-SNAPSHOT.jar' (located at target) file and put them on the same directory.
4) Run the CLI, for example: 
		sh githubCLI downloads -r "whitesource/log4j-detect-distribution" -o "test2.txt"