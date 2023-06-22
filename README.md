# (Climbing) Board analyzer
When resetting my climbing wall, I found that I often wanted something to tell me where to put a jug so I get a nice even spread. So I've made this.

This tool aims to help one analyze any particular climbing board set, provide suggestions on new hold positions/direction/type, produce heatmaps of various types.

## How to "install"
1. Make sure you have at least java version 17 installed (https://www.java.com/en/download/manual.jsp)
2. Download the latest .zip release and unpack
3. Run /bin/BoardAnalyzer.bat on windows

## How to use
Since the program is changing quite often, I won't go into details but the jist is:
1. Create a new board (by clicking the `new board` button)
2. Give it a name, and give it an image of the board. Ideally the image is as flat as possible
3. Select the corners of the board in the image
4. Input your boards physical dimensions (corresponding to the corners you clicked)
5. Click on the image to add holds
6. Use the various analysis tools

### To do
- Refactorings:
    - Clean up MainWindow (figure out how you're actually meant to use jpanels)
    - Make all panels respond to app state
- BoardPanel:
    - Force every hold to have a type
    - Change hold creation input to be "better"
        - Drag on hold -> change location
        - Drag on "tip" of direction -> change size/direction
        - ????
    - Add ability to select other holds while one is selected
- UI:
    - Fix the degrees symbol displaying a spanish A
    - Dissallow features until board is fully set up (ie. don't let people use any analysis tools until all corners added and board size has been input)
    - Show errors/warnings in red on info pane, like when saving a hold that's invalid
    - Show progress bar during long generations/Put them on a seperate thread
- Heatmap:
    - Improve efficiency of heatmap generation (do something like quadtree for raytracing (Is it called Kd-tree?))
    - Visualize heatmap overlay on board and add button to hide/show
- Major features:
    - Add ability to upload a new image for the same board
        - Upload new image
        - Pick new corners
        - Translate all points based upon new corner positions
    - Add ability to zoom in on board image (difficult? I think)
    - Allow dragging hold into ellipses, and fix underlying analysis to work with ellipses (tricksy, lots of math involved)
    - Auto hold discovery (I will probably never do this)

#### Why Java?
This would be more useful to others as a website, but I don't want the faff of hosting a website, and I hate HTML/CSS/Javascript. This project is mainly for my own use, if others find it useful too then that'd be nice.

C++ is fun, until you want to make rapid progress with pre-made GUI elements, then you're faffing around with various garbage, so that's a no-go.

Java has good, easy to produce, GUI design built into Swing, so java it is.