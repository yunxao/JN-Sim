#!/usr/bin/make -f

FIGS-EPS-PDF=$(patsubst %.eps,%.pdf,$(wildcard *.eps))

FIGS-SVG-PDF=$(patsubst %.svg,%.pdf,$(wildcard *.svg))
FIGS-SVG-EPS=$(patsubst %.svg,%.eps,$(wildcard *.svg))

FIGS-DIA-PDF=$(patsubst %.dia,%.pdf,$(wildcard *.dia))
FIGS-DIA-EPS=$(patsubst %.dia,%.eps,$(wildcard *.dia))

FIGS-PNG-PDF=$(patsubst %.png,%.pdf,$(wildcard *.png))
FIGS-PNG-EPS=$(patsubst %.png,%.eps,$(wildcard *.png))

FIGS-GLP-PDF=$(patsubst %.glp,%.pdf,$(wildcard *.glp))
FIGS-GLP-EPS=$(patsubst %.glp,%.eps,$(wildcard *.glp))


all:	figuras

figuras: $(FIGS-EPS-PDF) $(FIGS-SVG-PDF) $(FIGS-DIA-PDF) $(FIGS-PNG-PDF) $(FIGS-GLP-PDF) $(FIGS-SVG-EPS) $(FIGS-DIA-EPS) $(FIGS-PNG-EPS) $(FIGS-GLP-EPS)


%.pdf:	%.eps
	epstopdf	$<

%.eps:	%.gpl
	gnuplot	$<

%.eps:	%.svg
	inkscape -E $@ $<
	
%.eps:  %.dia
	dia $< -t eps -e $@

%.eps:  %.png
	convert $< $@ 

CLEANFILES=$(wildcard (FIGS-SVG-PDF) $(FIGS-DIA-PDF) $(FIGS-PNG-PDF) $(FIGS-GLP-PDF) $(FIGS-SVG-EPS) $(FIGS-DIA-EPS) $(FIGS-PNG-EPS) $(FIGS-GLP-EPS) $(FIGS-EPS-PDF))

clean: 
ifeq ("","$(CLEANFILES)")
	@echo Archivos temporales borrados
else
	@echo No limpiados
	rm -vf  $(CLEANFILES)
endif

