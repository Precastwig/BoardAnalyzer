// SFML Includes
#include <SFML/Window/WindowStyle.hpp>
#include <SFML/Graphics.hpp>
#include "include/Globals.hpp"

#if __linux__
#include <X11/Xlib.h>
#endif

int main(int argc, char *argv[]) { 
	#if __linux__
		XInitThreads();
	#endif

    Globals::logger().enable();

    if (!g_font.loadFromFile("resources/NotoSansCJK-Medium.ttc")) {
		Globals::logger().log(Logger::ERROR, "Font not loaded");
	}

    int window_width = 1920;
	int window_height = 1080;

    sf::ContextSettings settings;
	settings.antialiasingLevel = 8;
	sf::RenderWindow window(sf::VideoMode(window_width, window_height), "Boulder Describer", sf::Style::Titlebar | sf::Style::Close, settings);
	window.setFramerateLimit(120); // 120 seems like plenty

    // Start the loop
	while (window.isOpen()) {
        // Close window: exit
        // Process event's
		sf::Event event;
		while (window.pollEvent(event))
		{
			if (event.type == sf::Event::Closed)
				window.close();
            
        }
        // Clear screen
		window.clear(sf::Color(194, 240, 242));
		// Draw the relevent thing
		// menu.performMultithreadedActions();
		// window.draw(menu);
		// Update the window
		window.display();
    }
}   
