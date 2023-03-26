COMPILER=g++-9
CACHER=ccache

clean:
	rm -rf bin/*
	rm -rf include/*.gch
	rm -rf *.o

all:
	@$(CACHER) $(COMPILER) -std=c++2a -o boulderdescriber -I ./include *.cpp ./src/*.cpp -lsfml-graphics -lsfml-audio -lsfml-window -lsfml-system -pthread -lX11
	@echo "Linked all into executable"
