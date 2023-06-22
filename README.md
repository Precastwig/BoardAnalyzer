# (climbing) Board analyzer

In the climbing world, it's common to create one's own climbing wall, often people try to fill the areas of the board with various hold types and directions. One problem is that humans are stupid, and we struggle to effectively fill the board.

I've found that some hold setups work really well, and some just don't. And this tool exists to help me analyze any particular setup, provide suggestions, and maybe MORE.

## Java?
This would be more useful as a website, but I don't want to mess with css, I don't like it.

Java is one of the easiest languages with the best inherent GUI design, so java it is.

### To do
- Force every hold to have a type
- Dissallow features until board is fully set up (ie. don't let people use any analysis tools until all corners added and board size has been input)
- Clean up MainWindow (figure out how you're actually meant to use jpanels)
- Change hold creation input to be "better"
    - Drag on hold -> change location
    - Drag on "tip" of direction -> change size/direction
    - ????
- Improve efficiency of heatmap generation (do something like quadtree for raytracing (Is it called Kd-tree?))
- Visualize heatmap overlay on board and add button to hide/show
- Show errors/warnings in red on info pane, like when saving a hold that's invalid
- Show progress bar during long generations/Put them on a seperate thread
- Major features
    - Add ability to zoom in on board image (difficult? I think)
    - Allow dragging hold into ellipses, and fix underlying analysis to work with ellipses (tricksy, lots of math involved)
    - Auto hold discovery (I will probably never do this)