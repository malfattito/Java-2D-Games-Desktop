#!/bin/bash
# Monta um jar executavel com o motor, o jogo, as imagens, os sons e as
# bibliotecas do JavaZOOM ja embutidas.
#
#   uso:  Tools/build-jar.sh [ClassePrincipal] [nome-do-jar]
#
# Sem argumentos gera JGames2D.jar apontando para GameTopDownPrincipal.
set -e

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

PRINCIPAL="${1:-GameTopDownPrincipal}"
SAIDA="${2:-JGames2D.jar}"
BUILD="build"

echo "==> limpando"
rm -rf "$BUILD" "$SAIDA"
mkdir -p "$BUILD/classes"

echo "==> compilando"
javac -encoding UTF-8 -cp "Libs/*" -d "$BUILD/classes" JGames2D/*.java *.java

echo "==> embutindo as bibliotecas"
mkdir -p "$BUILD/tmp"
for LIB in Libs/*.jar; do
	rm -rf "$BUILD/tmp"; mkdir -p "$BUILD/tmp"
	unzip -qo "$LIB" -d "$BUILD/tmp"

	# Os arquivos de servico do javax.sound.sampled se repetem entre as
	# bibliotecas. Precisam ser somados: sobrescrever um deles tira o
	# suporte a MP3 ou a OGG.
	if [ -d "$BUILD/tmp/META-INF/services" ]; then
		mkdir -p "$BUILD/classes/META-INF/services"
		for SVC in "$BUILD/tmp/META-INF/services/"*; do
			[ -f "$SVC" ] || continue
			cat "$SVC" >> "$BUILD/classes/META-INF/services/$(basename "$SVC")"
			echo "" >> "$BUILD/classes/META-INF/services/$(basename "$SVC")"
		done
		rm -rf "$BUILD/tmp/META-INF/services"
	fi

	# Assinaturas de terceiros invalidam o jar montado
	rm -f "$BUILD/tmp/META-INF/"*.SF "$BUILD/tmp/META-INF/"*.DSA "$BUILD/tmp/META-INF/"*.RSA
	rm -f "$BUILD/tmp/META-INF/MANIFEST.MF"

	cp -R "$BUILD/tmp/." "$BUILD/classes/"
done
rm -rf "$BUILD/tmp"

echo "==> copiando as imagens e os sons"
cp -R Images "$BUILD/classes/"
cp -R Sounds "$BUILD/classes/"
find "$BUILD/classes" -name "Thumbs.db" -delete

echo "==> gerando o manifesto"
printf 'Main-Class: %s\n' "$PRINCIPAL" > "$BUILD/manifest.txt"

jar cfm "$SAIDA" "$BUILD/manifest.txt" -C "$BUILD/classes" .

echo
echo "pronto: $SAIDA  ($(du -h "$SAIDA" | cut -f1))"
echo "  executar:            java -jar $SAIDA"
echo "  outras principais:   java -cp $SAIDA GamePrincipal"
echo "                       java -cp $SAIDA IsoPrincipal"
echo "                       java -cp $SAIDA GTAPrincipal"
