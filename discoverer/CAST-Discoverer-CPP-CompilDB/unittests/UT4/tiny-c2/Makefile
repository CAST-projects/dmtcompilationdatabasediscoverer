CXX = g++
CFLAGS = -O2 -std=c++11
OBJS = a.o b.o main.o

all: tiny-c

a.o: a.cpp
	$(CXX) $(CFLAGS) -c -o $@ $<

b.o: b.cpp
	$(CXX) $(CFLAGS) -c -o $@ $<

main.o: main.cpp
	$(CXX) $(CFLAGS) -c -o $@ $<
	
tiny-c: $(OBJS)
	$(CXX) $(CFLAGS) -o $@ $(OBJS)

clean:
	rm -f *.o tiny-c
