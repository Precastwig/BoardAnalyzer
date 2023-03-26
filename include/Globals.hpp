#ifndef GLOBALS
#define GLOBALS

#include "InputRegister.hpp"
#include "Logger.hpp"
#include <SFML/Graphics.hpp>


Logger g_logger;
sf::Font g_font;
InputRegister g_input_reg;

class Globals {
    public:
    static void register_input(const sf::Event::EventType& e, std::function<void()> c) {
        g_input_reg.register_input(e, c);
    }
    static Logger& logger() {
        return g_logger;
    }
    static sf::Font& font() {
        return g_font;
    }
};

#endif