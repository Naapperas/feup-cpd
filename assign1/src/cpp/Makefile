CC=g++
CFLAGS=-O2 -Wall
LIBS=-lpapi
SRC=matrixproduct.cpp
EXEC=main
OUT_FILE = results.txt

all: $(EXEC)

$(EXEC): $(SRC)
	$(CC) $(CFLAGS) $(SRC) -o $(EXEC) $(LIBS)

run: all
	./$(EXEC)

run_to_file: all
	./$(EXEC) > $(OUT_FILE)

clean:
	rm -f $(EXEC)