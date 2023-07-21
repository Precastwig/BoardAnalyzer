# (Climbing) Board analyzer
When resetting my climbing wall, I found that I often wanted something to tell me where to put a jug so I get a nice even spread. So I've made this.

This tool aims to help one analyze any particular climbing board set, provide suggestions on new hold positions/direction/type, and highlight areas of interest.

## How to "install"
1. Make sure you have at least java version 17 installed (https://www.oracle.com/uk/java/technologies/downloads/)
2. Download the latest .zip release (from here: https://github.com/Precastwig/BoardAnalyzer/releases) and unpack
3. Run /bin/BoardAnalyzer.bat on windows

## How to use
Since the program is changing quite often, I won't go into details but the jist is:
1. Create a new board (by clicking the `new board` button)
2. Give it a name, and select an image of your board.
3. Drag the corners of the black squar to the corners of the board in the image
4. Input your boards physical dimensions (corresponding to the corners you clicked)
5. Click on the image to add holds
6. Use the various analysis/generation tools

### Contributing
If you think this program could be improved, feel free to open a PR.

### To do
- BoardPanel:
	- Make the hotkeys actually work
    - Change hold creation input to be "better"
        - Drag on hold -> change location
        - Drag on "tip" of direction -> change size/direction
        - ????
    - Add ability to select other holds while one is selected
- BoardSettings:
    - Add "set to current" button to type/direction/size tabs
- Hold selection:
    - Add "discard changes" button - hotkey to esc
- UI:
    - Add a "Would you like to save" window when quitting,
    	- Make it so it only does this when something has changed since the last time you saved?! (awkward) 
    - Dissallow features until board is fully set up (ie. don't let people use any analysis tools until all corners added and board size has been input)
    - Show errors/warnings in red on info pane, like when saving a hold that's invalid
    - Show progress bar during saving and do on a seperate thread
	- Remove weird colour mixing and just display single colour (probably should be done after the hold type overhaul)
- Major features:
	- Hold type overhaul
		- Make hold types have one MAJOR/MAIN type with any number of sub-types
		- Add the "wood/plastic" sub-type
		- Force every hold to have a MAJOR/MAIN type
	- Add colour choosing to settings
    - Add Suggestions for holds to REMOVE
    - Allow dragging hold into ellipses, and fix underlying analysis to work with ellipses (tricksy, lots of math involved)
    - Add route creation/saving (Not really the point of this app)
        - Add hold suggestion during route creation
        - Add auto finish climb feature
        - Train AI on moonboard problems
    - Auto hold discovery (I will probably never do this)

#### Why Java?
This would be more useful to others as a website, but I don't want the faff of hosting a website, and I hate HTML/CSS/Javascript. This project is mainly for my own use, if others find it useful too then that'd be nice.

C++ is fun, until you want to make rapid progress with pre-made GUI elements, then you're faffing around with various garbage, so that's a no-go.

Java has good, easy to produce, GUI design built into Swing, so java it is.

Why not an smartphone app? Because I don't really know what I'm doing with android development, and I don't want to learn.
