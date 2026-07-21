import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

// Gera a cidade em ilhas: mapa do chao, mapa de alturas e uma previa colorida
// que tambem serve de minimapa dentro do jogo.
public class GenCityMap
{
	// indices dos tiles da folha gta_city_tiles.png
	static final int ASFALTO=0, RUA_V=1, RUA_H=2, CRUZAMENTO=3, CALCADA=4, GRAMA=5,
	                 TELHADO_CLARO=6, TELHADO_ESCURO=7, PAREDE=8, AGUA=9, ESTACIONAMENTO=10, AREIA=11;

	static final int[] PALETA = {0x404040,0xFFD700,0xFF8C00,0xFF0000,0xC0C0C0,0x2E8B57,
	                             0x9E9E9E,0x5A5A5A,0x7E786F,0x1E90FF,0xB0B0B0,0x8B4513};
	static final int[] PALETA_ALTURA = {0x000000,0x111111,0x222222,0x333333,0x444444,0x555555};

	// cores da previa, escolhidas para o mapa ficar legivel de longe
	static final int[] PREVIA = {0x50535A,0x5A5D64,0x5A5D64,0x6A6D74,0x9A968E,0x4C8440,
	                             0xB4483C,0x8C4038,0x7E786F,0x2E7F94,0xA0A0A0,0xE8D9A8};

	static final int LARGURA = 160, ALTURA = 224;
	static int[][] chao = new int[ALTURA][LARGURA];
	static int[][] altura = new int[ALTURA][LARGURA];
	static Random r = new Random(1997);

	static boolean dentro(int x,int y){ return x>=0 && y>=0 && x<LARGURA && y<ALTURA; }
	static void pinta(int x,int y,int tile){ if(dentro(x,y)) chao[y][x]=tile; }

	// Ilha de contorno organico: o raio varia com o angulo, somando algumas
	// ondas. Sem isso a ilha fica um retangulo, que denuncia o gerador.
	static void criaIlha(double cx, double cy, double raioX, double raioY, long semente)
	{
		Random ruido = new Random(semente);
		double[] fase = new double[4];
		double[] peso = new double[4];
		for (int k=0;k<4;k++){ fase[k]=ruido.nextDouble()*Math.PI*2; peso[k]=0.06+ruido.nextDouble()*0.10; }

		for (int y=0;y<ALTURA;y++)
			for (int x=0;x<LARGURA;x++)
			{
				double dx=(x-cx)/raioX, dy=(y-cy)/raioY;
				double dist=Math.sqrt(dx*dx+dy*dy);
				if (dist < 0.0001) { chao[y][x]=GRAMA; continue; }
				double ang=Math.atan2(dy,dx);
				double limite=1.0;
				for (int k=0;k<4;k++) limite += peso[k]*Math.sin((k+2)*ang + fase[k]);
				if (dist < limite) chao[y][x]=GRAMA;
			}
	}

	// Praia: a grama vizinha da agua vira areia, algumas voltas
	static void criaPraias(int voltas)
	{
		for (int volta=0; volta<voltas; volta++)
		{
			int[][] copia = new int[ALTURA][LARGURA];
			for (int y=0;y<ALTURA;y++) copia[y]=chao[y].clone();
			for (int y=0;y<ALTURA;y++)
				for (int x=0;x<LARGURA;x++)
				{
					if (copia[y][x]!=GRAMA) continue;
					boolean beiraMar=false;
					for (int dy=-1;dy<=1;dy++) for (int dx=-1;dx<=1;dx++)
					{ int nx=x+dx, ny=y+dy; if(!dentro(nx,ny)||copia[ny][nx]==AGUA||copia[ny][nx]==AREIA) beiraMar=true; }
					if (beiraMar) chao[y][x]=AREIA;
				}
		}
	}

	// Distancia de cada celula ate a agua, para saber onde cabe cidade
	static int[][] distanciaDaAgua()
	{
		int[][] d = new int[ALTURA][LARGURA];
		for (int y=0;y<ALTURA;y++) for (int x=0;x<LARGURA;x++)
			d[y][x] = (chao[y][x]==AGUA) ? 0 : 9999;
		for (int passo=0; passo<40; passo++)
		{
			boolean mudou=false;
			for (int y=0;y<ALTURA;y++) for (int x=0;x<LARGURA;x++)
			{
				if (d[y][x]==0) continue;
				int menor=d[y][x];
				for (int k=0;k<4;k++)
				{
					int nx=x+(k==0?1:k==1?-1:0), ny=y+(k==2?1:k==3?-1:0);
					int viz = dentro(nx,ny) ? d[ny][nx] : 0;
					if (viz+1<menor) menor=viz+1;
				}
				if (menor!=d[y][x]) { d[y][x]=menor; mudou=true; }
			}
			if (!mudou) break;
		}
		return d;
	}

	// Bairro: malha viaria e quarteiroes onde ha terreno suficiente.
	// O passo diferente por bairro evita a cidade toda com o mesmo desenho.
	static void urbaniza(int x0,int y0,int x1,int y1,int passo,int[][] distAgua,int margem)
	{
		for (int y=y0;y<=y1;y++)
			for (int x=x0;x<=x1;x++)
			{
				if (!dentro(x,y) || distAgua[y][x] < margem) continue;
				boolean ruaV=((x-x0)%passo==0), ruaH=((y-y0)%passo==0);
				if (ruaV&&ruaH)  chao[y][x]=CRUZAMENTO;
				else if (ruaV)   chao[y][x]=RUA_V;
				else if (ruaH)   chao[y][x]=RUA_H;
				else if (((x-x0)%passo==1)||((x-x0)%passo==passo-1)||
				         ((y-y0)%passo==1)||((y-y0)%passo==passo-1)) chao[y][x]=CALCADA;
			}

		for (int by=y0+2; by<y1-1; by+=passo)
			for (int bx=x0+2; bx<x1-1; bx+=passo)
			{
				int sorteio=r.nextInt(12);
				int tipo = (sorteio<7)?0 : (sorteio<9)?1 : (sorteio<11)?2 : 3;
				int alturaBloco = 1+r.nextInt(5);
				int telhado = r.nextBoolean()?TELHADO_CLARO:TELHADO_ESCURO;
				//alguns quarteiroes viram um predio so, mais alto
				boolean torre = (r.nextInt(9)==0);
				if (torre) alturaBloco = 4+r.nextInt(2);

				for (int y=by; y<by+passo-3 && y<=y1; y++)
					for (int x=bx; x<bx+passo-3 && x<=x1; x++)
					{
						if (!dentro(x,y) || distAgua[y][x] < margem) continue;
						if (tipo==0)      { chao[y][x]=telhado; altura[y][x]=alturaBloco; }
						else if (tipo==1) { chao[y][x]=GRAMA; }
						else if (tipo==2) { chao[y][x]=ESTACIONAMENTO; }
						else              { chao[y][x]=AGUA; }   // lago do parque
					}
			}
	}

	// ponte de mao dupla ligando dois pontos, com calcada nas bordas
	static void ponte(int x0,int y0,int x1,int y1)
	{
		if (y0==y1)
			for (int x=Math.min(x0,x1); x<=Math.max(x0,x1); x++)
			{ pinta(x,y0-1,CALCADA); pinta(x,y0,RUA_H); pinta(x,y0+1,RUA_H); pinta(x,y0+2,CALCADA);
			  for(int k=-1;k<=2;k++) if(dentro(x,y0+k)) altura[y0+k][x]=0; }
		else
			for (int y=Math.min(y0,y1); y<=Math.max(y0,y1); y++)
			{ pinta(x0-1,y,CALCADA); pinta(x0,y,RUA_V); pinta(x0+1,y,RUA_V); pinta(x0+2,y,CALCADA);
			  for(int k=-1;k<=2;k++) if(dentro(x0+k,y)) altura[y][x0+k]=0; }
	}

	public static void main(String[] a) throws Exception
	{
		for (int y=0;y<ALTURA;y++) for (int x=0;x<LARGURA;x++) chao[y][x]=AGUA;

		//quatro ilhas de contornos diferentes, como no mapa de referencia
		criaIlha( 54,  34, 44, 27, 11);    // ilha norte
		criaIlha( 40, 132, 34, 54, 22);    // ilha oeste, a maior
		criaIlha(118, 134, 36, 60, 33);    // ilha leste
		criaIlha(131, 208, 20, 13, 44);    // ilhota do porto

		criaPraias(3);

		int[][] distAgua = distanciaDaAgua();

		//bairros com passos diferentes: centro denso, bairros mais largos
		urbaniza( 20,  14, 88, 54,  7, distAgua, 5);
		urbaniza( 12,  86, 68, 132, 6, distAgua, 5);   // centro, quadra curta
		urbaniza( 12, 134, 68, 178, 9, distAgua, 5);   // bairro largo
		urbaniza( 88,  84,148, 140, 8, distAgua, 5);
		urbaniza( 88, 142,148, 190, 6, distAgua, 5);
		urbaniza(116, 198,146, 218, 6, distAgua, 3);

		//pontes entre as ilhas
		//As pontes precisam entrar alguns blocos em cada margem, senao ficam
		//penduradas na agua
		ponte(52, 52, 52, 100);
		ponte(62, 116,108, 116);
		ponte(60, 170,106, 170);
		ponte(128,186,128, 212);

		File dir = new File(a[0]);

		BufferedImage mapaChao = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
		BufferedImage mapaAltura = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_RGB);
		BufferedImage previa = new BufferedImage(LARGURA*2, ALTURA*2, BufferedImage.TYPE_INT_RGB);

		int comPredio=0;
		for (int y=0;y<ALTURA;y++)
			for (int x=0;x<LARGURA;x++)
			{
				mapaChao.setRGB(x,y,PALETA[chao[y][x]]);
				mapaAltura.setRGB(x,y,PALETA_ALTURA[Math.min(altura[y][x],5)]);
				if (altura[y][x]>0) comPredio++;

				int cor = PREVIA[chao[y][x]];
				if (altura[y][x]>0)
				{
					//predios mais altos aparecem mais claros na previa
					Color base = new Color(cor);
					double f = 1.0 + altura[y][x]*0.10;
					cor = new Color(Math.min(255,(int)(base.getRed()*f)),
					                Math.min(255,(int)(base.getGreen()*f)),
					                Math.min(255,(int)(base.getBlue()*f))).getRGB();
				}
				for (int k=0;k<4;k++) previa.setRGB(x*2+k%2, y*2+k/2, cor);
			}

		ImageIO.write(mapaChao,"png",new File(dir,"city_ground.png"));
		ImageIO.write(mapaAltura,"png",new File(dir,"city_height.png"));
		ImageIO.write(previa,"png",new File(dir,"city_preview.png"));

		System.out.println("city_ground.png / city_height.png  " + LARGURA + "x" + ALTURA + " celulas");
		System.out.println("city_preview.png                   " + LARGURA*2 + "x" + ALTURA*2 + " (minimapa)");
		System.out.println("celulas com predio: " + comPredio);
		System.out.println("mundo em pixels: " + LARGURA*64 + "x" + ALTURA*64);
		System.exit(0);
	}
}
