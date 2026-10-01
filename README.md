# Neural Networks 101

This was a fun little hobby project I worked on during Christmas 2023. I was inspired by the recent advances in AI and by the amazing tutorials/coding adventures of [Sebastian Lague](https://www.youtube.com/watch?v=hfMk-kjRv4c) to try my hand at implementing one of the most foundational exercises in AI myself – with a twist; as a systems developer and maths nerd I set some constraints for myself that may seem quite unnatural for any AI/ML engineer:

1. deduce the full equation for backpropagation through my own, independent mathematical reasoning and pen/paper - if you talk with me over a beer you might be unlucky enough to get the full recounting of it 🍻
2. Implement said equation as an algorithm in Kotlin – this requires being a bit clever about how you reuse derivative subterms, or otherwise it takes forever to evaluate
3. Test out websockets and async web UIs - this gave a new understanding of the beauty (and complexity) of event based communication
4. hone my Kotlin and Typescript skills

I ended up making a server/client application that trains a neural network against a dataset, and displays a live view of the current loss function in the UI.
