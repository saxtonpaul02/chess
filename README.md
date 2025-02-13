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

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3XZfQN++oQADW6ErB2LINO2XM5QATE4nN0y0MxfdVk9q6law20E2zJxTF5fP4AtB2OSYAAZCDRJIBNIZLLIcxslvlap1JqtAzqBJoPsDcuDytPF5vD6rZvFShs-Ml-u3e+fR-6K8SxTl+IJFnKKDlAgG48rC66bqi6KxNiCaGC6YZumSFIGrSd6jCaRLhhaHIwNyvIGoKwowKK4qutKSbgWqkEwGgEDMIqyqYMACDME+SwwAAvAJ1H2toMB8e8jrOgSGEsuUnrekGAZBiGdHmpGJHRjAsbBtoUmJsm5zAuU8E8tmuaYKBhZFNQ363gOowPlMo7jo2-yOh+DHwPuGBdj2N40UOjzOUGrmTv8MDThwUWzt4fiBF4KDoGuG6+Mw27pJkmDtnkjElMe0gAKKroV9SFc0LQXqoV7dC59boB5YFnECJZ1ROllGZ5eXgjA0H2GlcGpb6iEYihEEarJpJGCg3CZDpyk1vVaAEUybrEeU0gzRShg6fG41eaBJlDWA5k8R1LWtnl35vrZl3eTkYB+b2vQHOg0WeHFC6Qraq7QjAADig6shlu7ZT5zCyrdx7-aVFX2IOtWhUtjWfp1aZtQ1VmQ068q9dCcHQiNyH6WhMmEZhMDkmA80Y8tobk-RhSWqRPIxipYlUbT9OrfR2M9ax7FKiqqETQzU3ILEgOjKosIrWaEZMyRZG2txCAwDysTsCSMAQAAZgDg4k9Zh1roTagWVj2N2fD0vjBU-Q2ygACS0jjAAjJ2ADMAAsTw7pkBoVv+Uw6AgoB1oHf7DlMjsAHJR-sMCNDdxxeTlj0wN2z0O0Dds56MLvu17vtTP7+p4UFXyh+HkeOcHfRxwnfR7Enr0zh986BNgPhQNg3DwLqmQG6MKSZXuD2Hu+aYng0cMI8ESMTn2jejCnXXNam5S08vg7x6v52ppPTHyYPKCwnAp9E1iRvoWL7qUxSNOL+gctERp5TK9p7PyEKYRc2pEY+a4wFixIWN8yY8ymgpTIUsz4rxQK-Na78B5eiHrAm+hkLrlAvqglAp08xo2skefOztXalA9j7Ned105PRvI7Qu5Di5t3enOeKARLAzWgskGAAApCAPJh6GACNXEAdYwYTyttPKolIzwtEdojRaS9eh92ABwqAcAIDQSgLMeh0gqGoywTAbeyiw5qI0VonRg4XYAkIUA5iAArfhaBYR8LMigNEo1wEyEmvfKmT9FEv25vLdaLNeS7Q5n-Z+aAglvzsQqNioDOIi1vpA3xFJYGwl0Ygxm7IP6s0Eb-SmVjpAxKQXEliCSOLC32hA+W5Q-BaBgYOWEKizGaOgJYgu0hsnqUVuUSk2BGmGFgTrfWuiMGFBNq4tA+CD4Fi8sQ-Radwa0JejFDubCvCqO8l6WAwBsB90IPERIo9QbpyPvlSoRUSplQqsYFGmDN5zPXqheS3A8Cyy8QAt5uzPkAJCZtWahhVbqzQJrNA2s9ZfzjNoWYFFYVGN5Og5JjzjI7LwLMy2V00w9CWW2FZmd-JrLepgIAA
