#!/usr/bin/make -f

FIGS-PDF=$(patsubst %.svg,%.pdf,$(wildcard Imagenes/*.svg))
FIGS-EPS=$(patsubst %.svg,%.eps,$(wildcard Imagenes/*.svg))

FIGS-DIA-PDF=$(patsubst %.dia,%.pdf,$(wildcard Imagenes/*.dia))
FIGS-DIA-EPS=$(patsubst %.dia,%.eps,$(wildcard Imagenes/*.dia))

FIGS-PNG-PDF=$(patsubst %.png,%.pdf,$(wildcard Imagenes/*.png))
FIGS-PNG-EPS=$(patsubst %.png,%.eps,$(wildcard Imagenes/*.png))

GRAFICAS-PDF=$(patsubst %.glp,fig/%.pdf,$(wildcard Imagenes/*.glp))
GRAFICAS-EPS=$(patsubst %.glp,fig/%.eps,$(wildcard Imagenes/*.glp))


all:	pdf
pdf:	main.pdf
dvi:	main.dvi

Imagenes/%.pdf:	Imagenes/%.eps
	epstopdf	$<

Imagenes/%.eps:	Imagenes/%.gpl
	gnuplot	$<

Imagenes/%.eps:	Imagenes/%.svg
	inkscape -E $@ $<
	
Imagenes/%.eps: Imagenes/%.dia
	dia $< -t eps -e $@

Imagenes/%.eps: Imagenes/%.png
	convert $< $@ 

main.pdf:	main.bbl
	pdflatex $(patsubst %.pdf,%,$@)
	pdflatex $(patsubst %.pdf,%,$@)

main.dvi:	main.bbl
	latex $(patsubst %.dvi,%,$@)

main.bbl: main.aux
#	bibtex $(patsubst %.bbl,%,$@)
	pdflatex $(patsubst %.bbl,%,$@)

main.aux:	  $(FIGS-PDF) $(FIGS-DIA-PDF) $(FIGS-PNG-PDF) main.tex
	pdflatex $(patsubst %.aux,%,$@)

figs-eps:	$(FIGS-EPS) 
figs-pdf:	$(FIGS-PDF)

figs-dia-eps:	$(FIGS-DIA-EPS)
figs-dia-pdf:	$(FIGS-DIA-PDF)

figs-png-eps:	$(FIGS-PNG-EPS)
figs-png-pdf:	$(FIGS-PNG-PDF)

graficas-gnuplot-eps: $(GRAFICAS-EPS)
graficas-gnuplot-pdf: $(GRAFICAS-PDF)



CLEANFILES=$(wildcard main.pdf main.dvi main.aux main.out \
		main.bbl main.blg main.log \
		*.bak *~ *.backup Imagenes/*~ bib/*~ *.log *.toc *.snm *.aux *.out *.nav)
clean:
ifeq ("","$(CLEANFILES)")
	@echo Already clean
else
	rm -vf  $(CLEANFILES)
endif

cleanall:	clean
	rm -vf $(FIGS-EPS) $(FIGS-PDF) $(FIGS-DIA-PDF) $(FIGS-DIA-EPS) $(GRAFICAS-PDF) $(GRAFICAS-EPS) $(FIGS-PNG-EPS) $(FIGS-PNG-PDF)

check:
	pdffonts main.pdf
	pdfinfo main.pdf
