# Socket-Scrabble
### Saad Khalil (Lunafreya2)

## Intro
Scrabble is a word game where you can score points creating words by placing letters on the board. 
This is a Java-based scrabble game designed with a client-server architecture using sockets and JavaFX based GUI, 
which allows 2+ people on a network to connect and play with each other.

## Requirements
1. You should be able to play scrabble with at least 2 people over a network with a client
& server application. 
2. The client should be able to connect to a server, play a game and announce the
winner in the end.
3. The server should be able to host at least one game with 2 players, following the rules
of scrabble, and determine the winner in the end.
4. It is required to have a UI in the client in order to play the game. This may be a TUI.
5. Structured as an MVC application.
6. Handle common exceptions and errors correctly during
running (connection loss, invalid input).
7. Pre-conditions, post-conditions and invariants for the most significant class.
8. Unit tests for the most significant/complex class.

The protocol is defined in Protocol.pdf.

## Specifics
- 15x15 (A-O, 1-15) gameboard, 100 letter tiles, a letter bag, and two racks
- WordChecker.jar has the word validation service based on the Collins Scrabble dictionary.
- Randomly assign 7 tiles to each player and inform them.
- Number of players (n) = 2 to 4
- Decide which player should start - pick Player 1 (i=0) first always. Then in order of IDs, i+1 % n would be the next player, where n == number of players.

### Premium squares
- 8x dark red “triple-word”    `[(0,0),(0,7),(0,14),(7,0),(7,14),(14,0),(14,7),(14,14)]`
- 16x pale red “double-word”    `[(1,1),(2,2),(3,3),(4,4),(10,10),(11,11),(12,12),(13,13),(1,13),(2,12),(3,11),(4,10),(10,4,(11,3),(12,2),(13,1)]`
- 1x start “center square”, which also counts double word    `[(7,7)]`
- 12x dark blue “triple-letter”    `[(1,5),(1,9),(5,1),(5,5),(5,9),(5,13),(9,1),(9,5),(9,9),(9,13),(13,5),(13,9)]`
- 24x pale blue “double-letter”   `[(0,3),(0,11),(2,6),(2,8),(3,0),(3,7),(3,14),(6,2),(6,6),(6,8),(6,12),(7,3),(7,11),(8,2),(8,6),(8,8),(8,12),(11,0),(11,7),(11,14),(12,6),(12,8),(14,3),(14,11)]`


### Tiles
#### Quantities
```
{'A':9, 'B':2, 'C':2, 'D':4, 'E':12, 'F':2, 'G':2, 'H':2, 'I':8, 'J':2, 'K':2, 'L':4, 'M':2, 
 'N':6, 'O':8, 'P':2, 'Q':1, 'R':6,  'S':4, 'T':6, 'U':4, 'V':2, 'W':2, 'X':1, 'Y':2, 'Z':1,  'Blank':2}
```
#### Scoring
```
{'A':1, 'B':3, 'C':3, 'D':2,  'E':1, 'F':4, 'G':2, 'H':4, 'I':1, 'J':8, 'K':5, 'L':1, 'M':3, 
 'N':1, 'O':1, 'P':3, 'Q':10, 'R':1, 'S':1, 'T':1, 'U':1. 'V':4, 'W':4, 'X':8, 'Y':4, 'Z':10, 'Blank':0}
```
