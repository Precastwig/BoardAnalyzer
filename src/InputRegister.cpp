#include <InputRegister.hpp>

void InputRegister::register_input(const sf::Event::EventType& event, std::function<void(sf::Event&)> callback) {
    m_input_map[event].push_back(callback);
}