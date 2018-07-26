CXX = g++
CFLAGS = -O2 -std=c++11
OBJS = a.o b.o

all: tiny-c

a.o: a.cpp
	$(CXX) $(CFLAGS) -c -o $@ $<

b.o: b.cpp
	$(CXX) $(CFLAGS) -c -o $@ $<

tiny-c: main.cpp $(OBJS)
	$(CXX) $(CFLAGS) $(OBJS) -o $@ $< -lm

clean:
	rm -f *.o tiny-c
