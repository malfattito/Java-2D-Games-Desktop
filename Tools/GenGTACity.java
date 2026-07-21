import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

// Tileset visto de cima + mapa do chao + mapa de alturas, estilo GTA 1
public class GenGTACity
{
	static final int T = 64, COLS = 4, ROWS = 3;   // 12 tiles de 64x64
	static Random r = new Random(7);

	static void ruido(Graphics2D g,int ox,int oy,Color base,int qtd,int var)
	{
		for (int i=0;i<qtd;i++){int d=r.nextInt(var*2)-var;
			g.setColor(new Color(cl(base.getRed()+d),cl(base.getGreen()+d),cl(base.getBlue()+d)));
			g.fillRect(ox+r.nextInt(T),oy+r.nextInt(T),2,2);}
	}
	static int cl(int v){return Math.max(0,Math.min(255,v));}

	public static void main(String[] a) throws Exception
	{
		BufferedImage sheet = new BufferedImage(T*COLS, T*ROWS, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sheet.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color ASF=new Color(64,66,72), CAL=new Color(172,168,160), GRA=new Color(72,132,60);

		for (int i=0;i<COLS*ROWS;i++)
		{
			int ox=(i%COLS)*T, oy=(i/COLS)*T;
			switch(i)
			{
				case 0: // asfalto liso
					g.setColor(ASF); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ASF,150,12); break;
				case 1: // rua vertical com faixa central
					g.setColor(ASF); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ASF,150,12);
					g.setColor(new Color(235,225,120));
					for(int k=0;k<4;k++) g.fillRect(ox+T/2-2, oy+k*16+4, 4, 8); break;
				case 2: // rua horizontal com faixa central
					g.setColor(ASF); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ASF,150,12);
					g.setColor(new Color(235,225,120));
					for(int k=0;k<4;k++) g.fillRect(ox+k*16+4, oy+T/2-2, 8, 4); break;
				case 3: // cruzamento com faixa de pedestre
					g.setColor(ASF); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ASF,150,12);
					g.setColor(new Color(230,230,230));
					for(int k=0;k<5;k++){ g.fillRect(ox+6+k*12, oy+2, 7, 10); g.fillRect(ox+6+k*12, oy+T-12, 7, 10); }
					break;
				case 4: // calcada
					g.setColor(CAL); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,CAL,120,10);
					g.setColor(new Color(148,144,136));
					for(int k=0;k<=T;k+=16){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k);} break;
				case 5: // grama
					g.setColor(GRA); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,GRA,260,18); break;
				case 6: // telhado claro com caixa d'agua
				case 7: // telhado escuro
				{
					Color base = (i==6)? new Color(150,146,140) : new Color(96,92,88);
					g.setColor(base); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,base,140,12);
					g.setColor(base.darker()); g.drawRect(ox+1,oy+1,T-3,T-3);
					g.setColor(new Color(70,70,74)); g.fillRect(ox+10,oy+10,16,12);
					g.setColor(new Color(110,110,116)); g.fillRect(ox+T-28,oy+T-24,18,14);
					break;
				}
				case 8: // parede com janelas (usada nas faces)
				{
					Color par = new Color(126,120,112);
					g.setColor(par); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,par,100,10);
					for(int fy=6;fy<T-8;fy+=18) for(int fx=8;fx<T-10;fx+=18){
						g.setColor(new Color(64,88,116)); g.fillRect(ox+fx,oy+fy,10,12);
						g.setColor(new Color(150,180,205)); g.fillRect(ox+fx,oy+fy,10,4);}
					g.setColor(new Color(90,86,80)); g.drawRect(ox,oy,T-1,T-1);
					break;
				}
				case 9: // agua
				{
					Color ag=new Color(48,104,170); g.setColor(ag); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ag,150,10);
					g.setColor(new Color(110,165,215));
					for(int k=0;k<5;k++) g.drawLine(ox+6+(k%2)*8, oy+8+k*12, ox+30+(k%2)*8, oy+8+k*12);
					break;
				}
				case 10: // estacionamento
				{
					g.setColor(ASF); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,ASF,150,12);
					g.setColor(new Color(220,220,220));
					for(int k=0;k<=T;k+=16) g.drawLine(ox+k,oy+8,ox+k,oy+T-8);
					break;
				}
				default: // terra
				{
					Color te=new Color(146,118,82); g.setColor(te); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,te,220,16); break;
				}
			}
		}
		g.dispose();

		File dir = new File(a[0]);
		ImageIO.write(sheet,"png",new File(dir,"gta_city_tiles.png"));

		// ---- mapa do chao e mapa de alturas (32x32) ----
		int N = 32;
		int[] pal = {0x404040,0xFFD700,0xFF8C00,0xFF0000,0xC0C0C0,0x2E8B57,
		             0x9E9E9E,0x5A5A5A,0x7E786F,0x1E90FF,0xB0B0B0,0x8B4513};
		int[] palAlt = {0x000000,0x111111,0x222222,0x333333,0x444444,0x555555};

		BufferedImage chao = new BufferedImage(N,N,BufferedImage.TYPE_INT_RGB);
		BufferedImage alt  = new BufferedImage(N,N,BufferedImage.TYPE_INT_RGB);

		for (int y=0;y<N;y++) for (int x=0;x<N;x++)
		{
			boolean ruaV = (x % 8 == 0);          // avenida no eixo vertical
			boolean ruaH = (y % 8 == 0);          // avenida no eixo horizontal
			boolean calc = (x%8==1)||(x%8==7)||(y%8==1)||(y%8==7);
			int tile, altura = 0;

			if (ruaV && ruaH)      tile = 3;
			else if (ruaV)         tile = 1;
			else if (ruaH)         tile = 2;
			else if (calc)         tile = 4;
			else
			{
				//miolo do quarteirao: predios, praca ou lago
				int bx = x/8, by = y/8;
				boolean praca = (bx==1 && by==1);
				boolean lago  = (bx==2 && by==2);
				if (praca)     { tile = 5; }
				else if (lago) { tile = 9; }
				else
				{
					tile = ((bx+by)%2==0) ? 6 : 7;      // telhado claro ou escuro
					altura = 1 + ((x*7 + y*13 + bx*5 + by*3) % 5);
				}
			}
			chao.setRGB(x,y,pal[tile]);
			alt.setRGB(x,y,palAlt[Math.min(altura,5)]);
		}
		ImageIO.write(chao,"png",new File(dir,"gta_city_map.png"));
		ImageIO.write(alt,"png",new File(dir,"gta_city_height.png"));

		System.out.println("gta_city_tiles.png  " + sheet.getWidth()+"x"+sheet.getHeight()+"  ("+COLS*ROWS+" tiles de "+T+"x"+T+")");
		System.out.println("gta_city_map.png / gta_city_height.png  " + N + "x" + N + " blocos");
		System.exit(0);
	}
}
