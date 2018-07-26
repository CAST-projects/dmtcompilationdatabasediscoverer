#include <iostream>
#include <vector>


int main(int argc, char** argv)
{
        std::vector<int> P;
	P.reserve(168);
	P.push_back(2); P.push_back(3);
	int w[] = {2, 4};
	for(auto p=5, i=0; p<1500; p+=w[i], i^=1) {
                auto r = 1;
		for(auto d=3; (d*d<=p) && (r=p%d); d+=2);
		if(r) {
			P.push_back(p);
		}
	}
	auto i = 0;
	std::cout << "\n";
        for(auto p : P) {
		std::cout << "  " << p;
		++i;
		if(!(i % 8)) {
			std::cout << "\n";
		}
	}
	std::cout << "\n";
}

