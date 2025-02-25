# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Phase 2 Sequence Diagram URL

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3XZfQN++oQADW6ErB2LINO2XM5QATE4nN0y0MxfdVk9q6law20E2zJxTF5fP4AtB2OSYAAZCDRJIBNIZLLIcxslvlap1JqtAzqBJoPsDcuDytPF5vD6rZvFShs-Ml-u3e+fR-6K8SxTl+IJFnKKDlAgG48rC66bqi6KxNiCaGC6YZumSFIGrSd6jCaRLhhaHIwNyvIGoKwowKK4qutKSbgWqkHUfa2iOs6jFOvKMAgKkKAgHWVSAc+sJPkssxGtoKEQRqGEsuUnrekGAZBiGdHmpGJHRjAsbBmxqGFmcQIlvBPLZrmmCgYZR6lreA6jA+UyjuOjb-I6H4MfA+4YF2PY3jRQ6PE5QYuZO-wwNOHCzt4fiBF4KDoGuG6+Mw27pJkmDtnknElhU0gAKKrvl9T5c0LQXqoV7dM59boO5YFGam5Q1ROlnnAWspMVByW+nBPVgIhGLSUxsmEZhMDkmAukqTWtVoARTJusR5RkTGqnaEKYQtXV6kRp1XHMaO8YyehY3yUYKDcJk03bfNoZnRphSWjIl0UoYukOrt1kQQqSoqgZnmgeU8EpeZCB5u1Hn7d+b7UFDhRZWAvm9r0BzoFFngxQukK2qu0IwAA4oOrJpbumXecw0NphUBPFWV9iDtVIVzfVn6Q2mt1tcZDWoVB0JwdCg3IexiYEnJpITRSN3MxOC1mhGT0katOnrfIm0wJzX2ebzGuq8AItoWLD3ujAyCxEToyqLCctEZpK08rawDKjAPKxOwJIwBAABmhODvdi30ft4ITcTeOxAbhlA2ugtqBZVna++ab9AzlvjBUyeDgAktI4wAIydgAzAALE8O6ZAaFb-lMOgIKAdYV3+w5TCnKAAHKN-sMCNLDxyeYjyM3i3qhpxnozZ3nhcl1MZf6nhgVfDXdcNw5Vd9C37cr6+XdozOmPzoE2A+FA2DcPAuqZL7owpOle45JTOXU7UDT04zwQy+gfbr4OPceYUUe3Z-QcG8UAAnZoeH6PFz4oFhHAKBQssQR1OgHCWk1pazVlv7eWy1SIOxVnGDaVFNbiz2pxYOR19InSNsgk2ilMgW2gV-fCmDbaK3KLAr0F96HMKWgnLqMBFTKkQcmdmbC4Gx3BlzVM304ZlFHigcepR87Fx-q2BGFMB69DXlnHOijJ47wxnOWKARLCXWgskGAAApCAPJL6GACIvAS5M77gJkceKolIzwtBbkzdBH9egn2ACYqAcAIDQSgLMFu2cVFs25s1d+15-G1yCSEsJETtGgO5i4g65QABW1i0CwisWZFAaIhqIKofLLCU1lK3Rtjw1hODeQfQIVteJ3DA6kO4uQ+Q5SZDEKqfQ2EkTpB1Pog05W9D1bDPaRpIO3EW7HRGkgyp1FsBaDoYOXC9kUCzACck0J0A0mjDUsQ7BlI1l6lsVMwcn1Tm8Oyfwv6Qi-4iMsfksGENMn3MuD0aJfd1EwG7CjX5kVor7wCF4QJXkvSwGANgE+hB4iJGvmTRGWTcoFSKiVMqxhWbCNiaWDJUi5nMRANwPA1tem7QUuSuE3TgCjMeuyco0hXoXydggF2aA3ZoA9t7PBekelaxJb9QRAMKnhhpTCrZUATnG2wayq6hgOVcp5Xyn2FE2LCs6cxAR-1KF9ONlKilCypIzIVsyl6SqNbO1dvIXlahPY+y4dqoofC9XPMasCNhtKPmSI6lTK4fy2wAqBX2fRmAgA
