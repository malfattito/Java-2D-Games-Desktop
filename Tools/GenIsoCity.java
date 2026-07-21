import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

// Gera o tileset isometrico da cidade e o mapa de indices por cor
public class GenIsoCity
{
	static final int TW = 64, TH = 32;          // celula do losango
	static final int COLS = 4, ROWS = 2;        // 8 tiles

	// indices: 0 grama, 1 asfalto, 2 rua-coluna, 3 rua-linha,
	//          4 cruzamento, 5 calcada, 6 agua, 7 terra
	static Polygon losango(int ox, int oy)
	{
		return new Polygon(new int[]{ox+TW/2, ox+TW, ox+TW/2, ox},
		                   new int[]{oy,      oy+TH/2, oy+TH,  oy+TH/2}, 4);
	}

	static void ruido(Graphics2D g, Shape corte, Color base, int qtd, int variacao, Random r)
	{
		Shape antigo = g.getClip();
		g.setClip(corte);
		Rectangle b = corte.getBounds();
		for (int i = 0; i < qtd; i++)
		{
			int d = r.nextInt(variacao*2) - variacao;
			g.setColor(new Color(clamp(base.getRed()+d), clamp(base.getGreen()+d), clamp(base.getBlue()+d)));
			g.fillRect(b.x + r.nextInt(b.width), b.y + r.nextInt(b.height), 2, 2);
		}
		g.setClip(antigo);
	}
	static int clamp(int v){ return Math.max(0, Math.min(255, v)); }

	// linha tracejada entre dois pontos, recortada pelo losango
	static void tracejado(Graphics2D g, Shape corte, double x1,double y1,double x2,double y2, Color cor, float largura, float traco)
	{
		Shape antigo = g.getClip();
		g.setClip(corte);
		g.setColor(cor);
		g.setStroke(new BasicStroke(largura, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
		            10f, new float[]{traco, traco}, 0f));
		g.draw(new Line2D.Double(x1,y1,x2,y2));
		g.setStroke(new BasicStroke(1));
		g.setClip(antigo);
	}

	public static void main(String[] a) throws Exception
	{
		Random r = new Random(20260721);
		BufferedImage sheet = new BufferedImage(TW*COLS, TH*ROWS, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sheet.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color GRAMA = new Color(76,140,64), ASFALTO = new Color(70,72,78),
		      CALCADA = new Color(176,172,164), AGUA = new Color(58,116,186), TERRA = new Color(150,120,84);

		for (int i = 0; i < COLS*ROWS; i++)
		{
			int ox = (i%COLS)*TW, oy = (i/COLS)*TH;
			Polygon p = losango(ox, oy);

			Color base = (i==0)?GRAMA : (i==5)?CALCADA : (i==6)?AGUA : (i==7)?TERRA : ASFALTO;
			g.setColor(base);
			g.fillPolygon(p);
			ruido(g, p, base, 120, (i==6)?10:14, r);

			//centro da celula e direcoes dos eixos do mapa na tela
			double cx = ox + TW/2.0, cy = oy + TH/2.0;
			//meio das arestas: coluna vai para baixo-direita, linha para baixo-esquerda
			double colX1 = ox+TW/4.0,     colY1 = oy+TH/4.0;      // (16,8)
			double colX2 = ox+TW*3/4.0,   colY2 = oy+TH*3/4.0;    // (48,24)
			double linX1 = ox+TW*3/4.0,   linY1 = oy+TH/4.0;      // (48,8)
			double linX2 = ox+TW/4.0,     linY2 = oy+TH*3/4.0;    // (16,24)

			if (i==2) tracejado(g,p,colX1,colY1,colX2,colY2,new Color(230,220,120),2f,5f);
			if (i==3) tracejado(g,p,linX1,linY1,linX2,linY2,new Color(230,220,120),2f,5f);
			if (i==4)
			{
				//cruzamento: faixa de pedestre nos dois sentidos
				Shape antigo = g.getClip(); g.setClip(p);
				g.setColor(new Color(225,225,225));
				g.setStroke(new BasicStroke(2f));
				for (int k=-3;k<=3;k++)
					g.draw(new Line2D.Double(cx-16+k*4.5, cy-8+k*2.25, cx+16+k*4.5, cy+8+k*2.25));
				g.setStroke(new BasicStroke(1)); g.setClip(antigo);
			}
			if (i==5)
			{
				//calcada: juntas seguindo os dois eixos
				Shape antigo = g.getClip(); g.setClip(p);
				g.setColor(new Color(150,146,138));
				for (int k=-2;k<=2;k++) {
					g.draw(new Line2D.Double(colX1+k*8, colY1+k*4, colX2+k*8, colY2+k*4));
					g.draw(new Line2D.Double(linX1-k*8, linY1+k*4, linX2-k*8, linY2+k*4));
				}
				g.setClip(antigo);
			}
			if (i==6)
			{
				Shape antigo = g.getClip(); g.setClip(p);
				g.setColor(new Color(120,175,225));
				for (int k=0;k<4;k++) {
					double yy = oy + 6 + k*6;
					g.draw(new Line2D.Double(ox+14+ (k%2)*6, yy, ox+30+(k%2)*6, yy+4));
				}
				g.setClip(antigo);
			}

			//aresta superior mais clara e inferior mais escura: dá volume
			g.setColor(new Color(255,255,255,45));
			g.drawLine(ox+TW/2, oy, ox+TW, oy+TH/2);
			g.drawLine(ox+TW/2, oy, ox, oy+TH/2);
			g.setColor(new Color(0,0,0,55));
			g.drawLine(ox, oy+TH/2, ox+TW/2, oy+TH);
			g.drawLine(ox+TW, oy+TH/2, ox+TW/2, oy+TH);
		}
		g.dispose();

		File dirImg = new File(a[0]);
		ImageIO.write(sheet, "png", new File(dirImg, "iso_city_tiles.png"));

		// ---- mapa de indices ----
		int N = 24;                            // 24 e multiplo de 6: a repeticao fecha
		int[] paleta = {0x2E8B57, 0x404040, 0xFFD700, 0xFF8C00, 0xFF0000, 0xC0C0C0, 0x1E90FF, 0x8B4513};
		BufferedImage mapa = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);
		for (int linha = 0; linha < N; linha++)
			for (int coluna = 0; coluna < N; coluna++)
			{
				boolean ruaColuna = (linha % 6 == 0);   // via que corre no eixo das colunas
				boolean ruaLinha  = (coluna % 6 == 0);  // via que corre no eixo das linhas
				int idx;

				if (ruaColuna && ruaLinha)      idx = 4;
				else if (ruaColuna)             idx = 2;
				else if (ruaLinha)              idx = 3;
				else if (linha%6==1 || linha%6==5 || coluna%6==1 || coluna%6==5) idx = 5;
				else
				{
					//um lago dentro de um dos quarteiroes
					boolean lago = (coluna>=13 && coluna<=16 && linha>=13 && linha<=16);
					idx = lago ? 6 : ((coluna+linha)%7==0 ? 7 : 0);
				}
				mapa.setRGB(coluna, linha, paleta[idx]);
			}
		ImageIO.write(mapa, "png", new File(dirImg, "iso_city_map.png"));

		System.out.println("iso_city_tiles.png  " + sheet.getWidth() + "x" + sheet.getHeight()
			+ "  (" + COLS*ROWS + " tiles de " + TW + "x" + TH + ")");
		System.out.println("iso_city_map.png    " + N + "x" + N + " blocos");
		System.exit(0);
	}
}
