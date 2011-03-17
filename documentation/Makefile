DOCUMENT_NAME = documentation
DOCUMENT_SOURCE = $(DOCUMENT_NAME).tex
DOCUMENT_OUTPUT = $(DOCUMENT_NAME).pdf

SOURCES = $(wildcard *.tex)

TEX = xelatex
BIBTEX = bibtex

FLAGS = -jobname $(DOCUMENT_NAME)

all: documentation

documentation: $(DOCUMENT_OUTPUT)

$(DOCUMENT_OUTPUT): $(SOURCES) bibliography.bib
	$(TEX) $(FLAGS) $(DOCUMENT_SOURCE)
	$(BIBTEX) $(DOCUMENT_NAME)
	$(TEX) $(FLAGS) $(DOCUMENT_SOURCE)
	$(TEX) $(FLAGS) $(DOCUMENT_SOURCE)

clean:
	@rm -rf *.aux
	@rm -rf *.bbl *.blg
	@rm -rf *.log *.out documentation.pdf

.PHONY: all documentation clean
