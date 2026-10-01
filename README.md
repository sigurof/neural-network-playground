# Neural Networks 101

This hobby project is a server/client application that trains a neural network against a dataset, and displays a live view of the current loss function in the UI.

Implemented using React/Kotlin + websockets.

If I were to flex one thing from this project it would be that I deduced backpropagation from scratch by pen and paper, and then implemented it by myself in code - no pre-made python libraries used or referenced :P 
Backpropagation is actually super simple from a mathematical point of view: it's just differentiating a function (the loss function) with respect to its parameters (weights and biases). In code you produce the derivatives by walking the neural network backwards while making sure to reuse as much information as possible between subsequent calculations.
