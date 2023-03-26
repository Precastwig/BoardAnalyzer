#ifndef LOGGER
#define LOGGER

#include <string>
#include <iostream>
#include <SFML/Graphics.hpp>

class Logger {
public:
	enum Level {
		INFO,
		WARNING,
		ERROR};
	void enable() {
		m_enabled = true;
	}
	void log(Level l, std::string str) {
		if (l == ERROR || m_enabled) {
			std::cout << levelToString(l) << str << "\033[0m\n";
		}
	};
	void log(Level l, double d) {
		if (l == ERROR || m_enabled) {
			std::cout << levelToString(l) << d << "\033[0m\n";
		}
	};
	void log(Level l, sf::Vector2f v) {
		if (l == ERROR || m_enabled) {
			std::cout << levelToString(l) << "(" << v.x << ", " << v.y << ")" << "\033[0m\n";
		}
	};
private:
	std::string levelToString(Level l) {
		if (l == INFO) {
			return "INFO: ";
		}
		if (l == WARNING) {
			return "\033[;33mWARNING: ";
		}
		if (l == ERROR) {
			return "\033[;31mERROR: ";
		}
		return "";
	}
	bool m_enabled = false;
};

#endif
