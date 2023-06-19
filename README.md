# (climbing) Board analyzer

In the climbing world, it's common to create one's own climbing wall, often people try to fill the areas of the board with various hold types and directions. One problem is that humans are stupid, and we struggle to effectively fill the board.

I've found that some hold setups work really well, and some just don't. And this tool exists to help me analyze any particular setup, provide suggestions, and maybe MORE.

## Java?
This would be more useful as a website, but I don't want to mess with css, I don't like it.

Java is one of the easiest languages with the best inherent GUI design, so java it is.

### To do
- Fix board flattening algorithm. It currenly does some odd things
- Force every hold to have a type
- Make selected hold more obvious
- Fix colours for different hold types?
- Change hold direction/type suggestions based upon location (less sidepulls near the edge, only feet at the bottom)
- Show progress bar during long generations/Put them on a seperate thread
- Refactor the Analyzer class (it's a mess)
- Auto hold discovery (grumble?)