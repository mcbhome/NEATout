Breakout - NEAT
========

This repo currently provides a working NEAT implementation, including a serialization mechanism for persistant solutions and a diagnostic user interface.

NEAT is an algorithm to evolve neural networks from a minimal state using an evolutionary approach. It features speciation, different kinds of mutation, fitness sharing and many other cool things.
For more information on NEAT, read this paper: http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf

Should you decide to let the program learn the game, you should expect it to not deliver many valueable results for about 15 to 30 generations.

Please note that this source code was implemented with IntelliJ IDEA. If you want to compile this project from source, you should probably use it, especially since the user interface was generated with IDEA. When the project reaches a state where a functional version is existent, I can supply ready-to-run JARs and fully trained populations/networks.

What's next?
======
- Documentation (ugh)
- Providing a "play only" mode in which one network can be selected to run without training the population
- Changes so that population files are deserializable after changes on the source and compiler-independent (serialVersionUID)
- Afterwards: Uploading an executable .jar and example populations

-- Original Description --
This program is a reimplementation of the classic Breakout game, which was released in 1976 by Atari. The player bounces a ball on a paddle controlled by the keyboard, which bounces off the top and side walls of the screen. Rows of bricks exist at the top of the screen which are destroyed when the ball hits them. Points are awarded to the player for each brick destroyed and when all are destroyed, the next level, which is randomly generated, is presented. The player has 3 lives, which are lost when the ball touches the bottom of the screen. 
The player is presented with a main menu at the start of the game, which starts when <SPACE> is pressed. If the player loses, he has the option to restart. As in the original, there is no end to the game. In this implementation, levels are randomly generated, meaning that the game ends only when the player dies.

![Example](https://raw.github.com/zacoppotamus/Breakout/master/Screenshot%202.png) ![Example](https://raw.github.com/zacoppotamus/Breakout/master/Screenshot%201.png)


