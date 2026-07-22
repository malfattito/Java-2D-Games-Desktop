#!/usr/bin/env python3
"""
Name: GenManual
Description: escreve o manual ilustrado do JGames2D em PDF. As figuras sao
             desenhadas pelo proprio motor (Tools/FigureMaker.java), de modo
             que o que o manual mostra e o que o motor desenha, e nao um
             desenho do que ele faria.
Author: Silvano Malfatti
Date: 22/07/26

Uso:
    javac -encoding UTF-8 -cp "Libs/*" -d /tmp/fig JGames2D/*.java Tools/FigureMaker.java
    java -cp "/tmp/fig:.:Libs/*" FigureMaker /tmp/figuras
    python3 Tools/GenManual.py /tmp/figuras Docs/JGames2D-Manual.pdf
"""

import os
import random
import subprocess
import sys
import tempfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import pdfkit
from pdfkit import A4

# ------------------------------------------------------------------ paleta

PAPEL = (253, 251, 246)
TINTA = (34, 40, 50)
SUAVE = (110, 118, 132)
VERDE = (76, 175, 122)
AZUL = (76, 134, 214)
LARANJA = (240, 168, 72)
VERMELHO = (214, 100, 92)
ROXO = (150, 110, 200)
NOITE = (24, 28, 36)
NOTA_FUNDO = (255, 244, 205)
NOTA_TITULO = (120, 90, 20)
NOTA_TEXTO = (90, 76, 40)

MARGEM = 58
LARGURA = A4[0] - 2 * MARGEM

CORES = [VERDE, AZUL, LARANJA, VERMELHO, ROXO]


# ------------------------------------------------------------- ferramentas

def nova(doc, numero, titulo, subtitulo="", cor=VERDE):
    """Uma pagina com a faixa do topo, o numero e o titulo."""
    p = doc.page()
    p.rect(0, 0, A4[0], A4[1], fill=PAPEL)

    p.rect(0, 0, A4[0], 96, fill=cor)
    p.circle(MARGEM + 22, 48, 22, fill=PAPEL)
    p.text(MARGEM + 22, 56, str(numero), 24, "bold", cor, "center")

    p.text(MARGEM + 62, 44, titulo, 24, "bold", PAPEL)
    if subtitulo:
        p.text(MARGEM + 63, 68, subtitulo, 12, "regular", PAPEL)

    p.line(MARGEM, A4[1] - 46, A4[0] - MARGEM, A4[1] - 46, (226, 222, 212), 1)
    p.text(MARGEM, A4[1] - 30, "JGames2D  -  manual ilustrado", 9, "regular", SUAVE)
    p.text(A4[0] - MARGEM, A4[1] - 30, str(numero), 9, "bold", SUAVE, "right")
    return p


def texto(p, y, conteudo, tamanho=11.5, cor=TINTA, largura=LARGURA, x=MARGEM):
    return p.paragraph(x, y, conteudo, largura, tamanho, "regular", cor, tamanho * 1.5)


def titulo2(p, y, conteudo, cor=TINTA):
    p.text(MARGEM, y, conteudo, 14, "bold", cor)
    return y + 22


def codigo(p, y, linhas, largura=LARGURA, x=MARGEM):
    """Um bloco de codigo em painel escuro."""
    altura = 16 + len(linhas) * 14.5
    p.rect(x, y, largura, altura, fill=NOITE)
    p.rect(x, y, 4, altura, fill=VERDE)

    linha = y + 24
    for conteudo in linhas:
        cor = (150, 158, 176) if conteudo.strip().startswith("//") else (222, 232, 240)
        p.text(x + 16, linha, conteudo, 9.5, "mono", cor)
        linha += 14.5

    return y + altura + 18


def nota(p, y, titulo, corpo, cor=LARANJA, largura=LARGURA, x=MARGEM):
    """Um recado colado na pagina."""
    linhas = pdfkit.wrap(corpo, 10.5, largura - 34)
    altura = 44 + len(linhas) * 15

    p.rect(x, y, largura, altura, fill=NOTA_FUNDO)
    p.rect(x, y, 5, altura, fill=cor)
    p.text(x + 18, y + 24, titulo, 11.5, "bold", NOTA_TITULO)

    linha = y + 42
    for conteudo in linhas:
        p.text(x + 18, linha, conteudo, 10.5, "regular", NOTA_TEXTO)
        linha += 15

    return y + altura + 18


def figura(p, y, caminho, legenda, largura=LARGURA, x=MARGEM):
    """Uma imagem com moldura e legenda."""
    altura = largura * 420.0 / 760.0
    p.rect(x - 3, y - 3, largura + 6, altura + 6, fill=(232, 228, 218))
    p.image(caminho, x, y, largura, altura)
    p.text(x + largura / 2.0, y + altura + 20, legenda, 9.5, "oblique", SUAVE, "center")
    return y + altura + 38


def caixa(p, x, y, largura, altura, cor, titulo, corpo):
    p.rect(x, y, largura, altura, fill=(255, 255, 255), stroke=(226, 222, 212))
    p.rect(x, y, largura, 4, fill=cor)
    p.text(x + 14, y + 28, titulo, 11.5, "bold", cor)
    p.paragraph(x + 14, y + 48, corpo, largura - 28, 10, "regular", TINTA, 15)


def seta(p, x1, y1, x2, y2, cor=SUAVE):
    p.line(x1, y1, x2, y2, cor, 1.5)
    p.line(x2, y2, x2 - 7, y2 - 4, cor, 1.5)
    p.line(x2, y2, x2 - 7, y2 + 4, cor, 1.5)


# --------------------------------------------------------------- as paginas

def capa(doc, fig):
    p = doc.page()
    p.rect(0, 0, A4[0], A4[1], fill=NOITE)

    # confete: quadradinhos como os blocos de uma camada
    random.seed(7)
    for _ in range(90):
        cor = CORES[random.randrange(len(CORES))]
        lado = random.choice([6, 8, 10, 14])
        p.rect(random.uniform(0, A4[0]), random.uniform(0, A4[1]), lado, lado, fill=cor)

    p.rect(0, 250, A4[0], 300, fill=NOITE)
    p.text(A4[0] / 2, 330, "JGames2D", 62, "bold", PAPEL, "center")
    p.text(A4[0] / 2, 368, "um motor de jogos 2D em Java puro", 15, "regular", VERDE, "center")
    p.rect(A4[0] / 2 - 150, 392, 300, 3, fill=VERDE)
    p.text(A4[0] / 2, 430, "manual ilustrado", 20, "oblique", PAPEL, "center")
    p.text(A4[0] / 2, 460, "sprites  -  camadas  -  colisão  -  som  -  tempo",
           12, "regular", SUAVE, "center")

    p.image(fig("fig_ortho"), MARGEM + 40, 520, LARGURA - 80, (LARGURA - 80) * 420.0 / 760.0)

    p.text(A4[0] / 2, A4[1] - 70, "Silvano Malfatti", 12, "bold", PAPEL, "center")
    p.text(A4[0] / 2, A4[1] - 52, "todas as figuras foram desenhadas pelo próprio motor",
           9, "oblique", SUAVE, "center")


def pagina_visao(doc, fig):
    p = nova(doc, 1, "O que você tem nas mãos", "duas camadas, nenhuma dependência", VERDE)
    y = 140

    y = texto(p, y, "O JGames2D é um motor de jogos 2D escrito em Java puro, sobre AWT e "
                    "Swing. Não há nada para instalar: nenhum Maven, nenhum Gradle, nenhuma "
                    "biblioteca de terceiros no caminho do desenho. O que existe são classes "
                    "que você lê de cabo a rabo numa tarde.")
    y += 10

    caixa(p, MARGEM, y, LARGURA / 2 - 10, 150, AZUL, "JGames2D/",
          "O motor. Janela, laço, sprites, camadas, texto, som, tempo e entrada. Escrito em "
          "inglês, com um bloco de comentário acima de cada método.")
    caixa(p, MARGEM + LARGURA / 2 + 10, y, LARGURA / 2 - 10, 150, LARANJA, "o seu jogo",
          "As cenas e a classe principal. Você cria o motor, configura a janela, registra as "
          "cenas e manda rodar. O resto é por sua conta.")
    y += 172

    y = titulo2(p, y, "O menor jogo possível")
    y = codigo(p, y, [
        "JGEngine engine = new JGEngine();",
        "",
        "engine.windowManager.setResolution(800, 600, 32);",
        "engine.windowManager.setWindowTitle(\"meu jogo\");",
        "",
        "engine.addLevel(new MinhaCena());   // a cena de indice 0",
        "engine.start();                     // daqui nao volta",
    ])

    y = texto(p, y, "E uma cena é uma classe que estende JGLevel e responde a duas perguntas: "
                    "o que carregar quando ela começa, e o que fazer a cada quadro.")
    y += 4
    y = codigo(p, y, [
        "public class MinhaCena extends JGLevel",
        "{",
        "    public void init()     { /* carrega o que precisa */ }",
        "    public void execute()  { /* um quadro de logica   */ }",
        "}",
    ])

    nota(p, y, "Sem sistema de build, de propósito",
         "Abra a pasta como projeto Java, ponha os jars de Libs/ no classpath e rode. A raiz "
         "do projeto é a pasta de código, e é isso que faz Images/ e Sounds/ aparecerem como "
         "recursos.")


def pagina_laco(doc, fig):
    p = nova(doc, 2, "O laço do jogo", "o coração que bate trinta vezes por segundo", AZUL)
    y = 140

    y = texto(p, y, "Quando você chama start(), o motor abre uma thread própria e passa a "
                    "repetir sempre a mesma sequência. Cada volta é um quadro. O alvo é um "
                    "quadro a cada 33 milissegundos, mais ou menos trinta por segundo, e o "
                    "tempo que o seu código gastou é descontado da pausa.")
    y += 12

    passos = [("execute()", "a lógica da cena", VERDE),
              ("update()", "sprites e camadas", AZUL),
              ("clear", "limpa o quadro", LARANJA),
              ("render()", "desenha tudo", VERMELHO)]

    largura_caixa = (LARGURA - 3 * 18) / 4
    for i, (nome, descricao, cor) in enumerate(passos):
        x = MARGEM + i * (largura_caixa + 18)
        p.rect(x, y, largura_caixa, 76, fill=(255, 255, 255), stroke=(226, 222, 212))
        p.rect(x, y, largura_caixa, 4, fill=cor)
        p.text(x + largura_caixa / 2, y + 34, nome, 12, "mono-bold", cor, "center")
        p.text(x + largura_caixa / 2, y + 56, descricao, 8.5, "regular", SUAVE, "center")
        if i < 3:
            seta(p, x + largura_caixa + 3, y + 38, x + largura_caixa + 15, y + 38)

    y += 100
    p.text(A4[0] / 2, y, "e então troca os buffers e dorme o que sobrou dos 33 ms",
           10, "oblique", SUAVE, "center")
    y += 30

    y = nota(p, y, "A regra que mais custa caro quando esquecida",
             "Desenhar só vale dentro de render(). O que você desenhar em execute() será "
             "apagado pelo clear que vem logo depois, no mesmo quadro. O resultado é um HUD "
             "invisível e nenhuma mensagem de erro.", VERMELHO)

    y = titulo2(p, y, "Trocar de cena")
    y = texto(p, y, "As cenas são numeradas na ordem em que você as registrou com addLevel(). "
                    "Trocar é dizer o número, de dentro de execute(), e voltar em seguida: o "
                    "quadro atual é abandonado e a cena nova desenha no próximo.")
    y = codigo(p, y, [
        "if (gameManager.inputManager.keyTyped(KeyEvent.VK_ENTER))",
        "{",
        "    gameManager.setCurrentLevel(2);",
        "    return;                       // nada depois disso",
        "}",
    ])

    texto(p, y, "Uma exceção que escape da sua cena não mata a thread em silêncio: o motor "
                "registra o erro e desliga por inteiro, em vez de deixar uma janela órfã "
                "segurando a JVM.", 10.5, SUAVE)


def pagina_janela(doc, fig):
    p = nova(doc, 3, "A janela e os dois buffers", "por que a tela não pisca", ROXO)
    y = 140

    y = texto(p, y, "Você nunca desenha direto na tela. Existe uma imagem do tamanho da sua "
                    "resolução, o buffer de trás, e é nela que tudo acontece. Quando o quadro "
                    "termina, ele é copiado para um segundo buffer, o da frente, sob trava. A "
                    "janela só pinta esse segundo.")
    y += 8

    largura_caixa = (LARGURA - 40) / 2
    caixa(p, MARGEM, y, largura_caixa, 132, VERDE, "buffer de trás",
          "Onde a sua cena desenha. É limpo e redesenhado a cada quadro. engine.graphics "
          "aponta para ele.")
    caixa(p, MARGEM + largura_caixa + 40, y, largura_caixa, 132, AZUL, "buffer da frente",
          "A cópia pronta. É o único que a janela pinta, então ela nunca mostra um quadro "
          "pela metade.")
    seta(p, MARGEM + largura_caixa + 8, y + 66, MARGEM + largura_caixa + 32, y + 66)
    y += 154

    y = nota(p, y, "Sem essa cópia, o preço aparece",
             "Pintar direto do buffer que está sendo limpo faz a tela rasgar e piscar preto no "
             "meio do desenho. Foi exatamente o que aconteceu aqui, antes da separação.")

    y = titulo2(p, y, "Medidas: pergunte ao motor, não à janela")
    y = codigo(p, y, [
        "int largura = gameManager.windowManager.getResolutionWidth();",
        "int altura  = gameManager.windowManager.getResolutionHeight();",
        "",
        "// getWidth() e getHeight(), herdados de JFrame, sao outra coisa:",
        "// medem a janela inteira, bordas e barra de titulo incluidas,",
        "// e devolvem zero antes de a janela aparecer",
    ])

    y = titulo2(p, y, "Tela cheia")
    texto(p, y, "Chame setfullScreen(true) antes de mostrar a janela. No macOS o motor usa a "
                "tela cheia nativa, a mesma do botão verde, porque o modo exclusivo do Java "
                "pinta preto nas versões recentes; nos demais sistemas usa uma janela sem "
                "bordas sobre a área útil. O buffer de trás continua na resolução que você "
                "pediu e é ampliado na hora de pintar, com barras pretas para não distorcer. "
                "O ponteiro do sistema some.")


def pagina_sprite(doc, fig):
    p = nova(doc, 4, "Sprites", "uma folha de desenhos e um retângulo que anda", LARANJA)
    y = 136

    y = texto(p, y, "Um sprite é uma imagem cortada numa grade de linhas por colunas. Você diz "
                    "a grade na hora de criar, e o motor cuida do resto.")
    y += 4
    y = codigo(p, y, [
        "JGSprite aviao = createSprite(getURL(\"/Images/spr_airplane.png\"), 1, 4);",
        "",
        "aviao.position.setXY(400, 300);   // o centro, nao o canto",
        "aviao.zoom.setXY(2.0, 2.0);       // o dobro do tamanho",
        "aviao.speed.setXY(0, -120);       // move-se sozinho, por segundo",
    ])

    y = figura(p, y, fig("fig_sprite"), "a folha, a grade de quadros e o sprite desenhado")

    nota(p, y, "position é o centro",
         "Ao desenhar, o motor recua metade do quadro em cada eixo. Isso poupa contas ao dar "
         "zoom e ao perguntar se dois sprites se encostaram.", VERDE)


def pagina_animacao(doc, fig):
    p = nova(doc, 5, "Animação", "quadros em fila, com hora marcada", VERMELHO)
    y = 136

    y = texto(p, y, "Uma animação é uma lista de quadros da folha, com uma taxa e um aviso de "
                    "repetir ou não. Um sprite pode ter várias, escolhidas pelo índice.")
    y += 4
    y = codigo(p, y, [
        "aviao.addAnimation(15, true, 0, 3);    // 0 a 3, quinze por segundo, em laco",
        "aviao.addAnimation(10, false, 4, 7);   // 4 a 7, uma vez so",
        "",
        "aviao.setCurrentAnimation(1);          // troca para a segunda",
        "",
        "if (aviao.getCurrentAnimation().isEnded()) { /* acabou */ }",
    ])

    y = figura(p, y, fig("fig_animation"),
               "os oito quadros de uma explosão, na ordem em que aparecem")

    texto(p, y, "Uma animação em laço nunca termina, e isEnded() devolve falso para sempre "
                "nela. O relógio de cada animação desconta o intervalo em vez de zerar, então "
                "ela não atrasa aos poucos quando um quadro demora mais que o outro.",
          10.5, SUAVE)


def pagina_camadas(doc, fig):
    p = nova(doc, 6, "Camadas", "o mapa desenhado num arquivo de imagem", VERDE)
    y = 140

    y = texto(p, y, "Uma camada é um mapa de blocos. O mais interessante é como ele nasce: "
                    "você pinta o mapa numa imagem, um pixel por bloco, e diz qual cor vira "
                    "qual quadro do conjunto de tiles.")
    y += 8

    y = codigo(p, y, [
        "JGColorIndex[] cores = new JGColorIndex[3];",
        "cores[0] = new JGColorIndex(0, new Color(0, 0, 0));      // preto   -> tile 0",
        "cores[1] = new JGColorIndex(2, new Color(0, 255, 0));    // verde   -> tile 2",
        "cores[2] = new JGColorIndex(5, new Color(255, 255, 0));  // amarelo -> tile 5",
        "",
        "JGOrthoLayer camada = createOrthoLayer(",
        "        getURL(\"/Images/spr_elements.png\"),   // a folha de tiles",
        "        getURL(\"/Images/lay_level.bmp\"),      // o mapa pintado",
        "        cores, new JGVector2D(32, 32), true);",
        "",
        "camada.setSpeed(new JGVector2D(0, 30));       // ela rola sozinha",
    ])

    y = figura(p, y, fig("fig_ortho"), "JGOrthoLayer: a grade alinhada com a tela")

    nota(p, y, "O mapa se repete para sempre",
         "Nas três projeções, chegar ao fim do mapa é voltar ao começo. Um cenário de trinta "
         "e dois blocos rola sem emenda a noite inteira.", AZUL)


def pagina_iso(doc, fig):
    p = nova(doc, 7, "Camadas de outro ângulo", "a mesma ideia, outra projeção", AZUL)
    y = 138

    y = texto(p, y, "Trocar a projeção é trocar a classe. O mapa, as cores e os tiles "
                    "continuam os mesmos, e uma cena pode misturar camadas de tipos "
                    "diferentes.")
    y += 4

    largura_figura = LARGURA * 0.82
    x = MARGEM + (LARGURA - largura_figura) / 2

    y = figura(p, y, fig("fig_iso"),
               "JGIsoLayer: cada bloco é um losango, do fundo para a frente", largura_figura, x)
    y = figura(p, y, fig("fig_topdown"),
               "JGTopDownLayer: visto de cima, com altura e perspectiva", largura_figura, x)

    texto(p, y, "Na isométrica o desenho segue a soma coluna mais linha, que cresce junto com "
                "o y da tela: é a ordem do pintor, e por isso um bloco alto cobre o de trás. "
                "Na vista de cima, um mapa de alturas diz quantos blocos há empilhados em cada "
                "célula, e as paredes se abrem para fora do ponto que a câmera olha.",
          10.5, SUAVE)


def pagina_colisao(doc, fig):
    p = nova(doc, 8, "Colisão", "dois retângulos e uma pergunta", VERMELHO)
    y = 136

    y = texto(p, y, "A colisão entre sprites é a interseção de dois retângulos. Não há máscara "
                    "por pixel nem forma poligonal: é barata e responde na hora.")
    y += 4
    y = codigo(p, y, [
        "if (aviao.collide(inimigo))",
        "{",
        "    inimigo.visible = false;",
        "    explosao.setCurrentAnimation(0);",
        "}",
    ])

    y = figura(p, y, fig("fig_collision"), "à esquerda não se tocam; à direita, sim")

    y = titulo2(p, y, "Contra o cenário")
    texto(p, y, "Para o mapa a pergunta é outra: isBlockAt(x, y) diz se há bloco sob um ponto "
                "da tela, e getFrameIndexAt(x, y) diz qual tile está ali, devolvendo -1 onde o "
                "mapa tem buraco. Com isso você testa o chão sob os pés do personagem sem "
                "varrer camada nenhuma.")


def pagina_texto(doc, fig):
    p = nova(doc, 9, "Texto", "uma linha escrita é um objeto como outro qualquer", ROXO)
    y = 136

    y = texto(p, y, "JGFont guarda a fonte, o tamanho, a cor, o alinhamento e a posição de uma "
                    "linha. A cena cria pelo nível, e o nível desenha os textos por último, "
                    "depois das camadas e dos sprites: um texto nunca fica coberto.")
    y += 4
    y = codigo(p, y, [
        "JGFont pontos = createFont(\"verdana\", Font.BOLD, 24);",
        "",
        "pontos.text = \"1500\";",
        "pontos.color = Color.white;",
        "pontos.alignment = JGFont.RIGHT;",
        "pontos.setPosition(largura - 20, 40);   // y e a linha de base",
        "",
        "pontos.getWidth();   // quanto ocupa, para encostar algo ao lado",
    ])

    y = figura(p, y, fig("fig_font"), "os três alinhamentos, todos contra o mesmo ponto")

    nota(p, y, "Meça pelo objeto, não na mão",
         "getLeft() e getRight() dizem onde as letras começam e terminam de verdade. Foi o que "
         "permitiu ao menu do MakeMeDev encostar as tags nas palavras sem medir texto nenhum.",
         VERDE)


def pagina_entrada(doc, fig):
    p = nova(doc, 10, "Entrada", "duas perguntas diferentes sobre a mesma tecla", LARANJA)
    y = 140

    largura_caixa = (LARGURA - 30) / 2
    caixa(p, MARGEM, y, largura_caixa, 150, AZUL, "keyPressed(tecla)",
          "Está pressionada agora. Use para andar, mirar, acelerar: tudo que continua "
          "acontecendo enquanto o dedo estiver lá.")
    caixa(p, MARGEM + largura_caixa + 30, y, largura_caixa, 150, VERDE, "keyTyped(tecla)",
          "Foi solta durante este quadro. Use para escolher, confirmar, pular: tudo que "
          "acontece uma vez por toque.")
    y += 174

    y = codigo(p, y, [
        "JGInputManager entrada = gameManager.inputManager;",
        "",
        "if (entrada.keyPressed(KeyEvent.VK_LEFT))  nave.position.setX(x - 4);",
        "if (entrada.keyTyped(KeyEvent.VK_SPACE))   atira();",
        "",
        "if (entrada.mouseClicked()) { /* botao solto neste quadro */ }",
        "JGVector2D onde = entrada.getMousePosition();",
    ])

    y = nota(p, y, "O evento não se gasta ao ser lido",
             "O motor limpa os eventos de borda uma vez por quadro, no fim dele. Assim todos "
             "os objetos da cena veem o mesmo clique, e um toque que começa e termina entre "
             "dois quadros ainda é entregue.", VERDE)

    texto(p, y, "As coordenadas do mouse já chegam no sistema do seu jogo: o motor desconta a "
                "borda da janela e desfaz a ampliação da tela cheia antes de entregar.",
          10.5, SUAVE)


def pagina_som(doc, fig):
    p = nova(doc, 11, "Som", "tiros que se sobrepõem e trilhas que dão a volta", VERMELHO)
    y = 140

    y = texto(p, y, "Há três caminhos, e escolher o certo evita quase todo problema de áudio.")
    y += 8

    itens = [
        (VERDE, "JGSoundEffect",
         "WAV curto, seis cópias abertas. Disparos seguidos se sobrepõem em vez de se "
         "cortarem, e as cópias são entregues em rodízio, porque a que acabou de tocar ainda "
         "está se esvaziando na placa."),
        (AZUL, "loadTrack(url)",
         "O mesmo objeto com uma cópia só, para música. O loop de um Clip é exato em "
         "amostras: não há buraco na emenda."),
        (LARANJA, "JGMusic",
         "MP3 e OGG por streaming, pela pilha JavaZOOM. Ocupa pouca memória, mas reabre o "
         "fluxo para recomeçar, então deixa um furo a cada volta."),
    ]

    for cor, nome, corpo in itens:
        linhas = pdfkit.wrap(corpo, 10, LARGURA - 150)
        altura = 30 + len(linhas) * 14
        p.rect(MARGEM, y, LARGURA, altura, fill=(255, 255, 255), stroke=(230, 226, 216))
        p.rect(MARGEM, y, 5, altura, fill=cor)
        p.text(MARGEM + 18, y + 26, nome, 11, "mono-bold", cor)

        linha = y + 24
        for conteudo in linhas:
            p.text(MARGEM + 140, linha, conteudo, 10, "regular", TINTA)
            linha += 14
        y += altura + 12

    y += 8
    y = codigo(p, y, [
        "JGSoundManager.init();                    // uma vez, no comeco",
        "",
        "JGSoundEffect tiro =",
        "    JGSoundManager.loadSoundEffect(getURL(\"/Sounds/SHOT.wav\"));",
        "tiro.setVolume(80);                       // 0 a 100; 100 e o arquivo",
        "tiro.play();",
        "",
        "JGSoundEffect trilha =",
        "    JGSoundManager.loadTrack(getURL(\"/Sounds/tema.wav\"));",
        "trilha.loop();                            // ate alguem mandar parar",
    ])

    nota(p, y, "O volume só desce",
         "A escala vai de 0 a 100 e vira decibéis. Em 100 você ouve o arquivo como ele é; não "
         "há ganho acima disso. Som baixo demais se conserta no arquivo, não no código.",
         VERMELHO)


def pagina_tempo(doc, fig):
    p = nova(doc, 12, "Tempo", "um relógio para o quadro, outro para cada coisa", AZUL)
    y = 140

    y = texto(p, y, "JGTimeManager mede quanto durou o quadro. JGTimer conta para trás a "
                    "partir de um intervalo, e serve para cadência de tiro, tempo de "
                    "invencibilidade, espera entre inimigos.")
    y += 4
    y = codigo(p, y, [
        "private JGTimer recarga = new JGTimer(250);   // 250 ms entre tiros",
        "",
        "public void execute()",
        "{",
        "    recarga.update();                         // voce e quem alimenta",
        "",
        "    if (recarga.isTimeEnded() && atirando)",
        "    {",
        "        atira();",
        "        recarga.restart(250);",
        "    }",
        "}",
    ])

    y = nota(p, y, "Um timer parado é um timer que você esqueceu de atualizar",
             "Nada os alimenta automaticamente. Chame update() no execute() da cena, sempre.")

    y = titulo2(p, y, "O salto depois de uma pausa")
    texto(p, y, "Se a janela ficar minimizada, ou você parar num ponto de interrupção, o "
                "relógio acumularia vários segundos e todos os temporizadores disparariam de "
                "uma vez ao voltar. Por isso o intervalo de um quadro é limitado a 250 ms: o "
                "jogo perde o tempo parado em vez de correr atrás dele.")


def pagina_recursos(doc, fig):
    p = nova(doc, 13, "Recursos", "carregados uma vez, contados enquanto servem", VERDE)
    y = 140

    y = texto(p, y, "As imagens e os sons passam por gerentes estáticos que guardam o que já "
                    "foi lido. Pedir duas vezes o mesmo arquivo devolve o mesmo objeto.")
    y += 8

    y = titulo2(p, y, "Contagem de referências, nas imagens")
    y = texto(p, y, "Cada loadImage() soma um; cada free() subtrai. Os pixels só são "
                    "descartados quando o último dono larga. É o que permite dois sprites "
                    "compartilharem a mesma folha sem que a saída de um apague o desenho do "
                    "outro.")
    y += 6

    y = codigo(p, y, [
        "JGImage folha = JGImageManager.loadImage(getURL(\"/Images/tiles.png\"));",
        "// ... use ...",
        "JGImageManager.free(folha);",
    ])

    y = titulo2(p, y, "Na carga, duas coisas acontecem sozinhas")
    y = texto(p, y, "A imagem é convertida para o formato da tela, o que evita uma conversão a "
                    "cada desenho, e o motor olha os pixels para descobrir a transparência que "
                    "ela realmente usa. Um PNG com canal alfa mas sem nenhum pixel meio "
                    "transparente vira máscara de um bit, que desenha bem mais rápido.")
    y += 8

    y = nota(p, y, "Todo mundo tem free()",
             "A convenção do motor é que cada classe libera o que segurava e anula suas "
             "referências. Uma classe nova sua deve seguir a mesma regra, e quem cria deve "
             "chamar na hora de desmontar.", AZUL)

    texto(p, y, "Um arquivo que não existe reclama na hora de carregar, com uma exceção clara, "
                "em vez de virar um NullPointerException três telas depois.", 10.5, SUAVE)


def pagina_maos(doc, fig):
    p = nova(doc, 14, "Mãos à obra", "compilar, rodar, distribuir", LARANJA)
    y = 140

    y = titulo2(p, y, "Compilar e rodar")
    y = codigo(p, y, [
        "javac -encoding UTF-8 -cp \"Libs/*\" -d out JGames2D/*.java *.java",
        "java  -cp \"out:.:Libs/*\" GamePrincipal",
    ])

    y = texto(p, y, "A raiz do projeto precisa estar no classpath: é o que faz Images/ e "
                    "Sounds/ serem encontrados como recursos. Todos os arquivos são UTF-8, e "
                    "o -encoding evita que a compilação dependa da configuração da máquina.")
    y += 10

    y = titulo2(p, y, "Um jar que roda em qualquer lugar")
    y = codigo(p, y, ["Tools/build-jar.sh GamePrincipal"])
    y = texto(p, y, "O script junta motor, jogo, arte, som e os jars da JavaZOOM num arquivo "
                    "só. Ele também gera um lançador .command para macOS: um jar recebido por "
                    "download chega em quarentena e o sistema o recusa com um aviso enganoso "
                    "de arquivo danificado, sem saída pela interface.")
    y += 14

    y = titulo2(p, y, "Por onde começar a ler")
    for nome, descricao in [("JGEngine", "o laço e a lista de cenas"),
                            ("JGLevel", "o que uma cena é capaz de criar"),
                            ("JGSprite", "quadros, animação, movimento, colisão"),
                            ("JGLayer", "o mapa e o que as três projeções compartilham")]:
        p.text(MARGEM + 6, y, nome, 11, "mono-bold", AZUL)
        p.text(MARGEM + 130, y, descricao, 10.5, "regular", TINTA)
        y += 20

    y += 14
    p.rect(MARGEM, y, LARGURA, 92, fill=NOITE)
    p.text(A4[0] / 2, y + 38, "boa sorte, e divirta-se", 17, "bold", PAPEL, "center")
    p.text(A4[0] / 2, y + 64, "o motor cabe na cabeça: leia antes de contornar",
           10.5, "oblique", VERDE, "center")


# ------------------------------------------------------------------ montagem

PAGINAS = [capa, pagina_visao, pagina_laco, pagina_janela, pagina_sprite, pagina_animacao,
           pagina_camadas, pagina_iso, pagina_colisao, pagina_texto, pagina_entrada,
           pagina_som, pagina_tempo, pagina_recursos, pagina_maos]


def main():
    figuras = sys.argv[1] if len(sys.argv) > 1 else "/tmp/figuras"
    destino = sys.argv[2] if len(sys.argv) > 2 else "Docs/JGames2D-Manual.pdf"

    temporaria = tempfile.mkdtemp()
    convertidas = {}

    def fig(nome):
        """Uma figura PNG vira JPEG, que e o que um PDF embute direto."""
        if nome not in convertidas:
            origem = os.path.join(figuras, nome + ".png")
            copia = os.path.join(temporaria, nome + ".jpg")
            subprocess.run(["ffmpeg", "-v", "error", "-y", "-i", origem, "-q:v", "3", copia],
                           check=True)
            convertidas[nome] = copia
        return convertidas[nome]

    doc = pdfkit.Document("JGames2D - manual ilustrado", "Silvano Malfatti")

    for pagina in PAGINAS:
        pagina(doc, fig)

    pasta = os.path.dirname(destino)
    if pasta:
        os.makedirs(pasta, exist_ok=True)

    tamanho = doc.save(destino)
    print("%s  %d paginas  %.0f KB" % (destino, len(doc.pages), tamanho / 1024.0))


if __name__ == "__main__":
    main()
