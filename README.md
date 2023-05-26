# (climbing) board analyzer

In the climbing world, it's common to create one's own climbing wall, often people try to fill the areas of the board with various hold types and directions. One problem is that humans are stupid, and we struggle to effectively fill the board.

I've found that some hold setups work really well, and some just don't. And this tool exists to help me analyze any particular setup, provide suggestions, and maybe MORE.

## Java?
This would be more useful as a website, but I don't want to mess with css, I don't like it.

Java is one of the easiest languages with the best inherent GUI design, so java it is.

### To do
- Fix board flattening algorithm. It currenly does some odd things
- Proper heatmap generation, currently it makes some lovely red squares
- Heatmap settings, based upon hold type/etc
- Produce Hold suggestions (and eventually based upon board preferences)
- Display hold suggestions on the main window! (hard? Got to undo the flattening algorithm, might be fine)
- Auto hold discovery (grumble?)