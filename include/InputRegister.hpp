#ifndef INPUTREGISTER
#define INPUTREGISTER

#include <SFML/Graphics.hpp>
#include <unordered_map>
#include <functional>

class InputRegister {
    public:
        InputRegister() : m_input_map() {};
        void register_input(const sf::Event::EventType& event, std::function<void(sf::Event&)> callback);
        void input(const sf::Event& event);
    private:
        std::unordered_map<sf::Event, std::vector<std::function<void(sf::Event&)>>> m_input_map;
};

#endif