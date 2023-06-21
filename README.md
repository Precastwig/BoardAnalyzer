# (climbing) Board analyzer

In the climbing world, it's common to create one's own climbing wall, often people try to fill the areas of the board with various hold types and directions. One problem is that humans are stupid, and we struggle to effectively fill the board.

I've found that some hold setups work really well, and some just don't. And this tool exists to help me analyze any particular setup, provide suggestions, and maybe MORE.

## Java?
This would be more useful as a website, but I don't want to mess with css, I don't like it.

Java is one of the easiest languages with the best inherent GUI design, so java it is.

### To do
- Force every hold to have a type
- Change hold direction/type suggestions based upon location (less sidepulls near the edge, only feet at the bottom)
- Show errors/warnings in red on info pane, like when saving a hold that's invalid
- Show progress bar during long generations/Put them on a seperate thread
- Add ability to zoom in on board image (difficult? I think)
- Allow dragging hold into ellipses, and fix underlying analysis to work with ellipses (tricksy, lots of math involved)
- Auto hold discovery (I will probably never do this)