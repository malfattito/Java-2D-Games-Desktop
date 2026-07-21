import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

// Gera, para cada cidade, o conjunto de tiles e os mapas do chao, de alturas
// e a previa colorida que serve de minimapa.
public class GenCityPack
{
	static final int T = 128, COLS = 6, ROWS = 4;   // 24 tiles de 128x128

	// indices dos tiles
	static final int ASFALTO=0, RUA_V=1, RUA_H=2, CRUZAMENTO=3, CALCADA=4, GRAMA=5,
	                 ARBUSTO=6, AGUA=7, AREIA=8, ESTACIONAMENTO=9,
	                 TELHADO_A=10, TELHADO_B=11, TELHADO_C=12, TELHADO_CASA=13,
	                 PAREDE_A=14, PAREDE_B=15, PAREDE_C=16, PAREDE_CASA=17,
	                 PONTE_V=18, PONTE_H=19, HOTDOG=20, PRACA=21, TELHADO_TOPO=22, TERRA=23;

	// uma cor por indice no mapa do chao
	static final int[] PALETA = {
		0x404040,0xFFD700,0xFF8C00,0xFF0000,0xC0C0C0,0x2E8B57,
		0x228B22,0x1E90FF,0xF4A460,0xB0B0B0,
		0x9E9E9E,0x8B0000,0x4682B4,0xD2691E,
		0x7E786F,0x6B5B4F,0x556677,0xDEB887,
		0x9370DB,0xBA55D3,0xFF69B4,0xADFF2F,0x708090,0x8B4513 };
	static final int[] PALETA_ALTURA = {0x000000,0x111111,0x222222,0x333333,0x444444,0x555555};

	// ---------------- estilos das tres cidades ----------------
	static class Estilo
	{
		String nome;
		Color asfalto, calcada, grama, agua, areia, faixa;
		Color[] telhado = new Color[3];
		Color[] parede = new Color[3];
		Color telhadoCasa, paredeCasa, arbusto;
		int janela;          // 0 alta, 1 quadrada, 2 em faixa
		Color vidro;
		int passoA, passoB;  // tamanho das quadras
		long semente;
	}

	static Estilo metropole()
	{
		Estilo e = new Estilo();
		e.nome="1"; e.semente=1997;
		e.asfalto=new Color(64,66,72); e.calcada=new Color(172,168,160);
		e.grama=new Color(72,132,60);  e.agua=new Color(48,104,170);
		e.areia=new Color(226,206,150); e.faixa=new Color(235,225,120);
		e.telhado[0]=new Color(150,146,140); e.telhado[1]=new Color(96,92,88); e.telhado[2]=new Color(74,96,120);
		e.parede[0]=new Color(126,120,112); e.parede[1]=new Color(96,104,116); e.parede[2]=new Color(150,152,158);
		e.telhadoCasa=new Color(140,70,55); e.paredeCasa=new Color(196,186,168);
		e.arbusto=new Color(52,104,44); e.janela=0; e.vidro=new Color(96,140,190);
		e.passoA=6; e.passoB=8;
		return e;
	}

	static Estilo litoranea()
	{
		Estilo e = new Estilo();
		e.nome="2"; e.semente=2024;
		e.asfalto=new Color(92,90,86); e.calcada=new Color(214,206,188);
		e.grama=new Color(104,158,72); e.agua=new Color(40,150,180);
		e.areia=new Color(240,224,170); e.faixa=new Color(250,250,250);
		e.telhado[0]=new Color(198,96,70); e.telhado[1]=new Color(176,80,60); e.telhado[2]=new Color(222,210,190);
		e.parede[0]=new Color(238,232,214); e.parede[1]=new Color(224,212,186); e.parede[2]=new Color(206,196,176);
		e.telhadoCasa=new Color(190,88,64); e.paredeCasa=new Color(246,240,226);
		e.arbusto=new Color(86,140,60); e.janela=1; e.vidro=new Color(120,170,190);
		e.passoA=7; e.passoB=9;
		return e;
	}

	static Estilo industrial()
	{
		Estilo e = new Estilo();
		e.nome="3"; e.semente=3131;
		e.asfalto=new Color(48,48,52); e.calcada=new Color(120,118,114);
		e.grama=new Color(84,102,64);  e.agua=new Color(38,72,96);
		e.areia=new Color(150,138,110); e.faixa=new Color(210,190,90);
		e.telhado[0]=new Color(112,68,52); e.telhado[1]=new Color(76,74,72); e.telhado[2]=new Color(92,54,44);
		e.parede[0]=new Color(126,72,54); e.parede[1]=new Color(86,84,82); e.parede[2]=new Color(104,62,48);
		e.telhadoCasa=new Color(96,60,48); e.paredeCasa=new Color(150,124,102);
		e.arbusto=new Color(70,96,52); e.janela=2; e.vidro=new Color(180,170,120);
		e.passoA=6; e.passoB=10;
		return e;
	}

	// ---------------- ruido periodico ----------------
	// O mesmo tile se repete em celulas vizinhas, entao o ruido precisa fechar
	// nas bordas: os pontos da grade dao a volta com o resto da divisao.
	static class Ruido
	{
		double[][] grade; int lado;
		Ruido(int lado, Random r)
		{
			this.lado = lado;
			grade = new double[lado][lado];
			for (int y=0;y<lado;y++) for (int x=0;x<lado;x++) grade[y][x]=r.nextDouble();
		}
		double suave(double t){ return t*t*(3-2*t); }
		double amostra(double x, double y)   // x,y em [0,1)
		{
			double fx=x*lado, fy=y*lado;
			int x0=(int)Math.floor(fx), y0=(int)Math.floor(fy);
			double tx=suave(fx-x0), ty=suave(fy-y0);
			int x1=(x0+1)%lado, y1=(y0+1)%lado; x0=((x0%lado)+lado)%lado; y0=((y0%lado)+lado)%lado;
			double a=grade[y0][x0], b=grade[y0][x1], c=grade[y1][x0], d=grade[y1][x1];
			return (a*(1-tx)+b*tx)*(1-ty) + (c*(1-tx)+d*tx)*ty;
		}
	}

	static double fbm(Ruido[] oitavas, double x, double y)
	{
		double soma=0, peso=0, amp=1;
		for (Ruido o : oitavas){ soma += amp*o.amostra(x,y); peso += amp; amp*=0.5; }
		return soma/peso;
	}

	// Preenche o tile com uma textura de ruido em volta de uma cor base
	static void textura(BufferedImage img, int ox, int oy, Color base, double forca, long semente, int detalhe)
	{
		Random r = new Random(semente);
		Ruido[] oitavas = { new Ruido(detalhe, r), new Ruido(detalhe*2, r), new Ruido(detalhe*4, r) };
		for (int y=0;y<T;y++)
			for (int x=0;x<T;x++)
			{
				double n = fbm(oitavas, x/(double)T, y/(double)T) - 0.5;
				int rr=cl((int)(base.getRed()  *(1+n*forca)));
				int gg=cl((int)(base.getGreen()*(1+n*forca)));
				int bb=cl((int)(base.getBlue() *(1+n*forca)));
				img.setRGB(ox+x, oy+y, 0xFF000000 | (rr<<16) | (gg<<8) | bb);
			}
	}

	// Escurece as bordas de baixo e da direita: da a impressao de luz vinda
	// de cima e da esquerda, o que separa visualmente os blocos
	static void luz(Graphics2D g, int ox, int oy)
	{
		g.setColor(new Color(255,255,255,26)); g.fillRect(ox,oy,T,2); g.fillRect(ox,oy,2,T);
		g.setColor(new Color(0,0,0,40)); g.fillRect(ox,oy+T-2,T,2); g.fillRect(ox+T-2,oy,2,T);
	}

	// ---------------- desenho dos tiles ----------------
	static Random r;
	static BufferedImage folha;

	static int cl(int v){return Math.max(0,Math.min(255,v));}
	static Color escurece(Color c,double f){return new Color(cl((int)(c.getRed()*f)),cl((int)(c.getGreen()*f)),cl((int)(c.getBlue()*f)));}
	static Color mistura(Color a, Color b, double t)
	{ return new Color(cl((int)(a.getRed()*(1-t)+b.getRed()*t)), cl((int)(a.getGreen()*(1-t)+b.getGreen()*t)), cl((int)(a.getBlue()*(1-t)+b.getBlue()*t))); }

	// asfalto: agregado fino, manchas de remendo e o desgaste dos pneus
	static void asfalto(Graphics2D g,int ox,int oy,Estilo e,long semente)
	{
		textura(folha,ox,oy,e.asfalto,0.30,semente,8);
		Random rr=new Random(semente);
		//pedrisco
		for(int i=0;i<900;i++){
			int x=ox+rr.nextInt(T), y=oy+rr.nextInt(T);
			int d=rr.nextInt(46)-18;
			g.setColor(new Color(cl(e.asfalto.getRed()+d),cl(e.asfalto.getGreen()+d),cl(e.asfalto.getBlue()+d),190));
			g.fillRect(x,y,1+rr.nextInt(2),1+rr.nextInt(2)); }
		//remendos
		for(int i=0;i<2;i++){
			int x=ox+rr.nextInt(T-40), y=oy+rr.nextInt(T-40);
			g.setColor(new Color(0,0,0,22)); g.fillOval(x,y,26+rr.nextInt(30),20+rr.nextInt(24)); }
		//trilha dos pneus
		g.setColor(new Color(0,0,0,14));
		g.fillRect(ox+T/4-9,oy,18,T); g.fillRect(ox+3*T/4-9,oy,18,T);
	}

	// faixa pintada com desgaste, para nao parecer adesivo
	static void faixaPintada(Graphics2D g,int x,int y,int w,int h,Color cor,Random rr)
	{
		g.setColor(cor); g.fillRect(x,y,w,h);
		g.setColor(new Color(0,0,0,40));
		for(int i=0;i<w*h/8;i++) g.fillRect(x+rr.nextInt(w),y+rr.nextInt(h),1,1);
	}

	static void desenhaTile(Graphics2D g,int i,int ox,int oy,Estilo e)
	{
		Random rr = new Random(e.semente*97 + i*131);
		switch(i)
		{
		case ASFALTO: asfalto(g,ox,oy,e,e.semente+i); break;

		case RUA_V:
			asfalto(g,ox,oy,e,e.semente+i);
			for(int k=0;k<4;k++) faixaPintada(g,ox+T/2-3,oy+k*32+8,6,16,e.faixa,rr);
			break;

		case RUA_H:
			asfalto(g,ox,oy,e,e.semente+i);
			for(int k=0;k<4;k++) faixaPintada(g,ox+k*32+8,oy+T/2-3,16,6,e.faixa,rr);
			break;

		case CRUZAMENTO:
			asfalto(g,ox,oy,e,e.semente+i);
			for(int k=0;k<5;k++){
				faixaPintada(g,ox+12+k*24,oy+4,14,20,new Color(236,236,232),rr);
				faixaPintada(g,ox+12+k*24,oy+T-24,14,20,new Color(236,236,232),rr); }
			break;

		case CALCADA: case HOTDOG:
		{
			textura(folha,ox,oy,e.calcada,0.16,e.semente+i,6);
			//lajotas com chanfro
			int lado=32;
			for(int y=0;y<T;y+=lado) for(int x=0;x<T;x+=lado){
				int d=rr.nextInt(16)-8;
				g.setColor(new Color(cl(e.calcada.getRed()+d),cl(e.calcada.getGreen()+d),cl(e.calcada.getBlue()+d),90));
				g.fillRect(ox+x+1,oy+y+1,lado-2,lado-2);
				g.setColor(new Color(255,255,255,50)); g.drawLine(ox+x+1,oy+y+1,ox+x+lado-2,oy+y+1);
				g.setColor(new Color(0,0,0,55)); g.drawLine(ox+x+1,oy+y+lado-2,ox+x+lado-2,oy+y+lado-2);
				g.setColor(escurece(e.calcada,0.72)); g.drawRect(ox+x,oy+y,lado,lado); }
			if (i==HOTDOG)
			{
				//sombra no chao, carrinho, guarda-sol listrado e vapor
				g.setColor(new Color(0,0,0,70)); g.fillOval(ox+30,oy+78,66,24);
				g.setColor(new Color(190,190,196)); g.fillRoundRect(ox+40,oy+58,48,34,8,8);
				g.setColor(new Color(150,150,158)); g.fillRect(ox+40,oy+74,48,4);
				g.setColor(new Color(120,120,126)); g.fillOval(ox+44,oy+86,14,14); g.fillOval(ox+72,oy+86,14,14);
				g.setColor(new Color(60,60,64)); g.drawOval(ox+44,oy+86,14,14); g.drawOval(ox+72,oy+86,14,14);
				g.setColor(new Color(210,180,120)); g.fillRect(ox+50,oy+64,28,8);
				g.setColor(new Color(90,90,96)); g.fillRect(ox+62,oy+30,4,32);
				g.setColor(new Color(200,55,50)); g.fillOval(ox+24,oy+14,80,42);
				g.setColor(new Color(240,240,240));
				for(int k=0;k<5;k++) g.fillArc(ox+24,oy+14,80,42,k*72+8,26);
				g.setColor(new Color(0,0,0,45)); g.fillArc(ox+24,oy+34,80,22,180,180);
				g.setColor(new Color(255,255,255,60));
				for(int k=0;k<3;k++) g.fillOval(ox+58+k*3,oy+50-k*7,7-k,7-k);
			}
			luz(g,ox,oy); break;
		}

		case GRAMA: case ARBUSTO:
		{
			textura(folha,ox,oy,e.grama,0.34,e.semente+i,7);
			//tufos de capim
			for(int k=0;k<450;k++){
				int x=ox+rr.nextInt(T), y=oy+rr.nextInt(T);
				g.setColor(rr.nextBoolean()? new Color(255,255,255,26) : new Color(0,0,0,26));
				g.drawLine(x,y,x+rr.nextInt(3)-1,y-2-rr.nextInt(3)); }
			if (i==ARBUSTO)
				for(int k=0;k<3;k++){
					int bx=ox+16+rr.nextInt(64), by=oy+16+rr.nextInt(64), rad=26+rr.nextInt(16);
					g.setColor(new Color(0,0,0,80)); g.fillOval(bx+5,by+8,rad,rad*3/4);
					for(int c=0;c<9;c++){
						int px=bx+rr.nextInt(rad/2), py=by+rr.nextInt(rad/2), pr=rad/2+rr.nextInt(rad/3);
						g.setColor(mistura(e.arbusto,new Color(0,0,0),0.25+rr.nextDouble()*0.2));
						g.fillOval(px,py,pr,pr); }
					for(int c=0;c<7;c++){
						int px=bx+rr.nextInt(rad/2), py=by+rr.nextInt(rad/3), pr=rad/3+rr.nextInt(rad/4);
						g.setColor(mistura(e.arbusto,new Color(255,255,255),0.15+rr.nextDouble()*0.25));
						g.fillOval(px,py,pr,pr); } }
			break;
		}

		case AGUA:
		{
			textura(folha,ox,oy,e.agua,0.22,e.semente+i,5);
			//ondulacao e brilhos
			for(int y=0;y<T;y++){
				double onda=Math.sin((y/(double)T)*Math.PI*4)*0.5+0.5;
				g.setColor(new Color(255,255,255,(int)(10+onda*14)));
				g.drawLine(ox,oy+y,ox+T,oy+y); }
			g.setColor(new Color(255,255,255,90));
			for(int k=0;k<14;k++){
				int x=ox+rr.nextInt(T-24), y=oy+rr.nextInt(T);
				g.fillRoundRect(x,y,10+rr.nextInt(14),2,2,2); }
			break;   // sem luz nas bordas: o mar e continuo
		}

		case AREIA:
		{
			textura(folha,ox,oy,e.areia,0.20,e.semente+i,7);
			for(int k=0;k<1400;k++){
				int x=ox+rr.nextInt(T), y=oy+rr.nextInt(T);
				g.setColor(rr.nextBoolean()? new Color(255,255,255,40) : new Color(120,100,70,40));
				g.fillRect(x,y,1,1); }
			//marcas do vento
			g.setColor(new Color(0,0,0,16));
			for(int k=0;k<6;k++) g.drawArc(ox-20+rr.nextInt(T),oy+rr.nextInt(T),80,26,0,180);
			break;
		}

		case ESTACIONAMENTO:
		{
			asfalto(g,ox,oy,e,e.semente+i);
			for(int k=0;k<=T;k+=32) faixaPintada(g,ox+k,oy+14,4,T-28,new Color(226,226,220),rr);
			break;
		}

		case TELHADO_A: case TELHADO_B: case TELHADO_C: case TELHADO_TOPO:
		{
			Color base = (i==TELHADO_TOPO)? e.telhado[1] : e.telhado[i-TELHADO_A];
			textura(folha,ox,oy,base,0.18,e.semente+i,6);
			//juntas das placas
			g.setColor(escurece(base,0.85));
			for(int k=0;k<=T;k+=42){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k); }
			//parapeito: faixa clara na volta e sombra por dentro
			g.setColor(escurece(base,1.18)); g.fillRect(ox,oy,T,6); g.fillRect(ox,oy,6,T);
			g.fillRect(ox,oy+T-6,T,6); g.fillRect(ox+T-6,oy,6,T);
			g.setColor(new Color(0,0,0,60)); g.fillRect(ox+6,oy+6,T-12,7); g.fillRect(ox+6,oy+6,7,T-12);

			if (i==TELHADO_TOPO)
			{
				g.setColor(escurece(base,0.72)); g.fillOval(ox+22,oy+22,84,84);
				g.setColor(new Color(240,240,240)); g.setStroke(new BasicStroke(5));
				g.drawOval(ox+28,oy+28,72,72);
				g.drawLine(ox+46,oy+46,ox+82,oy+82); g.drawLine(ox+82,oy+46,ox+46,oy+82);
				g.setStroke(new BasicStroke(1));
			}
			else
			{
				//equipamentos: condensadoras, caixa d'agua e clarabóia
				for(int k=0;k<2;k++){
					int x=ox+18+rr.nextInt(56), y=oy+18+rr.nextInt(56);
					g.setColor(new Color(0,0,0,70)); g.fillRect(x+3,y+4,22,16);
					g.setColor(new Color(150,152,156)); g.fillRect(x,y,22,16);
					g.setColor(new Color(110,112,116));
					for(int l=2;l<14;l+=3) g.drawLine(x+2,y+l,x+20,y+l);
					g.setColor(new Color(190,192,196)); g.drawRect(x,y,22,16); }
				int cx=ox+72+rr.nextInt(20), cy=oy+72+rr.nextInt(20);
				g.setColor(new Color(0,0,0,70)); g.fillOval(cx+4,cy+5,26,20);
				g.setColor(new Color(176,178,182)); g.fillOval(cx,cy,26,20);
				g.setColor(new Color(140,142,146)); g.drawOval(cx,cy,26,20);
				g.setColor(new Color(120,160,190,180)); g.fillRect(ox+18,oy+86,24,20);
				g.setColor(new Color(90,92,96)); g.drawRect(ox+18,oy+86,24,20);
			}
			break;
		}

		case TELHADO_CASA:
		{
			textura(folha,ox,oy,e.telhadoCasa,0.16,e.semente+i,6);
			//fiadas de telha, com sombra sob cada fiada
			for(int y=0;y<T;y+=14){
				g.setColor(new Color(0,0,0,70)); g.fillRect(ox,oy+y+11,T,3);
				for(int x=(y/14%2)*10;x<T;x+=20){
					g.setColor(mistura(e.telhadoCasa,new Color(255,255,255),0.06+rr.nextDouble()*0.12));
					g.fillRoundRect(ox+x,oy+y,18,11,5,5); } }
			//cumeeira e chamine
			g.setColor(escurece(e.telhadoCasa,0.7)); g.fillRect(ox,oy+T/2-3,T,6);
			g.setColor(new Color(0,0,0,80)); g.fillRect(ox+T-40,oy+18,26,30);
			g.setColor(new Color(126,110,100)); g.fillRect(ox+T-44,oy+14,26,30);
			g.setColor(new Color(96,84,76)); g.fillRect(ox+T-46,oy+10,30,7);
			luz(g,ox,oy); break;
		}

		case PAREDE_A: case PAREDE_B: case PAREDE_C:
		{
			Color parede = e.parede[i-PAREDE_A];
			textura(folha,ox,oy,parede,0.14,e.semente+i,6);
			int alturaAndar = T/2;
			for(int andar=0; andar<2; andar++)
			{
				int base = oy + andar*alturaAndar;
				//friso entre andares
				g.setColor(escurece(parede,1.12)); g.fillRect(ox,base,T,4);
				g.setColor(new Color(0,0,0,55)); g.fillRect(ox,base+4,T,3);

				if (e.janela==0)          // altas e estreitas
					for(int fx=12; fx<T-22; fx+=26) janela(g,ox+fx,base+16,16,36,e,parede,rr);
				else if (e.janela==1)     // quadradas
					for(int fx=16; fx<T-26; fx+=32) janela(g,ox+fx,base+18,22,22,e,parede,rr);
				else                       // faixa continua
					janela(g,ox+10,base+18,T-20,20,e,parede,rr);
			}
			g.setColor(new Color(0,0,0,70)); g.fillRect(ox,oy+T-5,T,5);
			g.setColor(escurece(parede,0.7)); g.drawRect(ox,oy,T-1,T-1);
			break;
		}

		case PAREDE_CASA:
		{
			textura(folha,ox,oy,e.paredeCasa,0.12,e.semente+i,6);
			//porta com soleira e macaneta
			g.setColor(new Color(0,0,0,60)); g.fillRect(ox+50,oy+52,36,76);
			g.setColor(escurece(e.paredeCasa,0.5)); g.fillRect(ox+48,oy+50,36,74);
			g.setColor(escurece(e.paredeCasa,0.38)); g.drawRect(ox+48,oy+50,36,74);
			g.setColor(new Color(220,200,120)); g.fillOval(ox+76,oy+86,5,5);
			janela(g,ox+14,oy+40,28,28,e,e.paredeCasa,rr);
			janela(g,ox+96,oy+40,22,28,e,e.paredeCasa,rr);
			g.setColor(escurece(e.paredeCasa,0.7)); g.drawRect(ox,oy,T-1,T-1);
			break;
		}

		case PONTE_V: case PONTE_H:
		{
			asfalto(g,ox,oy,e,e.semente+i);
			Color rail=new Color(202,198,192);
			if (i==PONTE_V){
				for(int k=0;k<4;k++) faixaPintada(g,ox+T/2-3,oy+k*32+8,6,16,e.faixa,rr);
				//junta de dilatacao
				g.setColor(new Color(0,0,0,70)); g.fillRect(ox,oy+T-4,T,4);
				for(int lado=0;lado<2;lado++){
					int bx = (lado==0)? ox : ox+T-16;
					g.setColor(new Color(0,0,0,90)); g.fillRect(bx+ (lado==0?12:-4),oy,6,T);
					g.setColor(escurece(rail,0.62)); g.fillRect(bx,oy,16,T);
					g.setColor(rail); g.fillRect(bx+2,oy,7,T);
					g.setColor(escurece(rail,0.8)); for(int k=0;k<T;k+=22) g.fillRect(bx+2,oy+k,12,7);
					g.setColor(new Color(255,255,255,70)); g.fillRect(bx+2,oy,2,T); } }
			else {
				for(int k=0;k<4;k++) faixaPintada(g,ox+k*32+8,oy+T/2-3,16,6,e.faixa,rr);
				g.setColor(new Color(0,0,0,70)); g.fillRect(ox+T-4,oy,4,T);
				for(int lado=0;lado<2;lado++){
					int by = (lado==0)? oy : oy+T-16;
					g.setColor(new Color(0,0,0,90)); g.fillRect(ox,by+(lado==0?12:-4),T,6);
					g.setColor(escurece(rail,0.62)); g.fillRect(ox,by,T,16);
					g.setColor(rail); g.fillRect(ox,by+2,T,7);
					g.setColor(escurece(rail,0.8)); for(int k=0;k<T;k+=22) g.fillRect(ox+k,by+2,7,12);
					g.setColor(new Color(255,255,255,70)); g.fillRect(ox,by+2,T,2); } }
			break;
		}

		case PRACA:
		{
			Color piso = escurece(e.calcada,0.95);
			textura(folha,ox,oy,piso,0.14,e.semente+i,6);
			//piso em espinha, com um canteiro no meio
			g.setColor(escurece(piso,0.82));
			for(int k=0;k<=T;k+=21){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k); }
			g.setColor(new Color(0,0,0,60)); g.fillOval(ox+42,oy+48,46,40);
			g.setColor(escurece(e.grama,0.9)); g.fillOval(ox+38,oy+42,48,42);
			for(int c=0;c<8;c++){
				g.setColor(mistura(e.arbusto,new Color(255,255,255),rr.nextDouble()*0.3));
				g.fillOval(ox+42+rr.nextInt(30),oy+46+rr.nextInt(26),18,16); }
			luz(g,ox,oy); break;
		}

		default:
		{
			Color te=new Color(146,118,82);
			textura(folha,ox,oy,te,0.26,e.semente+i,7);
			for(int k=0;k<600;k++){ int x=ox+rr.nextInt(T), y=oy+rr.nextInt(T);
				g.setColor(new Color(0,0,0,30)); g.fillRect(x,y,2,2); }
			break;
		}
		}
	}

	// janela com moldura, peitoril, vidro em degrade e reflexo
	static void janela(Graphics2D g,int x,int y,int w,int h,Estilo e,Color parede,Random rr)
	{
		g.setColor(new Color(0,0,0,70)); g.fillRect(x+2,y+3,w,h);
		g.setColor(escurece(parede,0.78)); g.fillRect(x-2,y-2,w+4,h+4);
		for(int k=0;k<h;k++){
			double t=k/(double)h;
			g.setColor(mistura(escurece(e.vidro,1.25), escurece(e.vidro,0.7), t));
			g.drawLine(x,y+k,x+w,y+k); }
		//reflexo na diagonal
		g.setColor(new Color(255,255,255,60));
		g.fillPolygon(new int[]{x,x+w/2,x+w/3,x}, new int[]{y+h,y,y,y+h/2}, 4);
		//caixilho
		g.setColor(escurece(parede,0.55));
		g.drawRect(x,y,w,h);
		if (w>18) g.drawLine(x+w/2,y,x+w/2,y+h);
		if (h>24) g.drawLine(x,y+h/2,x+w,y+h/2);
		//peitoril
		g.setColor(escurece(parede,1.2)); g.fillRect(x-3,y+h,w+6,3);
		g.setColor(new Color(0,0,0,60)); g.fillRect(x-3,y+h+3,w+6,2);
		//algumas luzes acesas
		if (rr.nextInt(6)==0){ g.setColor(new Color(255,236,170,120)); g.fillRect(x+1,y+1,w-1,h-1); }
	}

	static BufferedImage criaTileset(Estilo e)
	{
		r = new Random(e.semente);
		folha = new BufferedImage(T*COLS, T*ROWS, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = folha.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		for (int i=0;i<COLS*ROWS;i++)
		{
			desenhaTile(g, i, (i%COLS)*T, (i/COLS)*T, e);
		}
		g.dispose();
		return folha;
	}

	// ---------------- mapas ----------------
	static final int LARG=160, ALT=224;
	static int[][] chao, altura;

	static boolean dentro(int x,int y){ return x>=0&&y>=0&&x<LARG&&y<ALT; }
	static void pinta(int x,int y,int t){ if(dentro(x,y)) chao[y][x]=t; }

	static void criaIlha(double cx,double cy,double rx,double ry,long semente)
	{
		Random ru=new Random(semente); double[] fase=new double[4], peso=new double[4];
		for(int k=0;k<4;k++){fase[k]=ru.nextDouble()*Math.PI*2; peso[k]=0.06+ru.nextDouble()*0.10;}
		for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++)
		{
			double dx=(x-cx)/rx, dy=(y-cy)/ry, d=Math.sqrt(dx*dx+dy*dy);
			if(d<0.0001){chao[y][x]=GRAMA;continue;}
			double ang=Math.atan2(dy,dx), lim=1.0;
			for(int k=0;k<4;k++) lim+=peso[k]*Math.sin((k+2)*ang+fase[k]);
			if(d<lim) chao[y][x]=GRAMA;
		}
	}

	static void criaPraias(int voltas)
	{
		for(int v=0;v<voltas;v++){
			int[][] c=new int[ALT][LARG];
			for(int y=0;y<ALT;y++) c[y]=chao[y].clone();
			for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++){
				if(c[y][x]!=GRAMA) continue;
				boolean beira=false;
				for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++){
					int nx=x+dx,ny=y+dy; if(!dentro(nx,ny)||c[ny][nx]==AGUA||c[ny][nx]==AREIA) beira=true;}
				if(beira) chao[y][x]=AREIA; }
		}
	}

	static int[][] distanciaDaAgua()
	{
		int[][] d=new int[ALT][LARG];
		for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++) d[y][x]=(chao[y][x]==AGUA)?0:9999;
		for(int p=0;p<40;p++){ boolean mudou=false;
			for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++){
				if(d[y][x]==0) continue; int menor=d[y][x];
				for(int k=0;k<4;k++){ int nx=x+(k==0?1:k==1?-1:0), ny=y+(k==2?1:k==3?-1:0);
					int viz=dentro(nx,ny)?d[ny][nx]:0; if(viz+1<menor) menor=viz+1; }
				if(menor!=d[y][x]){d[y][x]=menor;mudou=true;} }
			if(!mudou) break; }
		return d;
	}

	static void urbaniza(int x0,int y0,int x1,int y1,int passo,int[][] dist,int margem,boolean bairroDeCasas)
	{
		for(int y=y0;y<=y1;y++) for(int x=x0;x<=x1;x++){
			if(!dentro(x,y)||dist[y][x]<margem) continue;
			boolean rv=((x-x0)%passo==0), rh=((y-y0)%passo==0);
			if(rv&&rh) chao[y][x]=CRUZAMENTO;
			else if(rv) chao[y][x]=RUA_V;
			else if(rh) chao[y][x]=RUA_H;
			else if(((x-x0)%passo==1)||((x-x0)%passo==passo-1)||((y-y0)%passo==1)||((y-y0)%passo==passo-1)){
				//alguns pontos da calcada recebem um carrinho de hotdog
				chao[y][x] = (r.nextInt(90)==0) ? HOTDOG : CALCADA; }
		}

		for(int by=y0+2;by<y1-1;by+=passo) for(int bx=x0+2;bx<x1-1;bx+=passo)
		{
			int sorteio=r.nextInt(14);
			int tipo;
			if (bairroDeCasas) tipo = (sorteio<9)?4 : (sorteio<11)?1 : (sorteio<13)?2 : 3;
			else               tipo = (sorteio<8)?0 : (sorteio<10)?1 : (sorteio<12)?2 : (sorteio<13)?3 : 4;

			int alturaBloco=1+r.nextInt(5);
			int telhado = new int[]{TELHADO_A,TELHADO_B,TELHADO_C}[r.nextInt(3)];
			//O heliponto e uma peca so, no meio da cobertura. Aplicado ao
			//quarteirao inteiro viraria um tabuleiro de helipontos.
			boolean comHeliponto = (r.nextInt(9)==0);
			if (comHeliponto) alturaBloco = 4+r.nextInt(2);
			int heliX = bx + (passo-3)/2, heliY = by + (passo-3)/2;

			for(int y=by;y<by+passo-3&&y<=y1;y++) for(int x=bx;x<bx+passo-3&&x<=x1;x++)
			{
				if(!dentro(x,y)||dist[y][x]<margem) continue;
				if(tipo==0){
					chao[y][x] = (comHeliponto && x==heliX && y==heliY) ? TELHADO_TOPO : telhado;
					altura[y][x]=alturaBloco; }
				else if(tipo==1){ chao[y][x]= (r.nextInt(3)==0)?ARBUSTO:GRAMA; }
				else if(tipo==2){ chao[y][x]=ESTACIONAMENTO; }
				else if(tipo==3){ chao[y][x]=PRACA; }
				else { chao[y][x]=TELHADO_CASA; altura[y][x]=1; }     // bairro de casas
			}
		}
	}

	static void ponte(int x0,int y0,int x1,int y1)
	{
		if(y0==y1) for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++){
			pinta(x,y0,PONTE_H); pinta(x,y0+1,PONTE_H);
			for(int k=0;k<=1;k++) if(dentro(x,y0+k)) altura[y0+k][x]=0; }
		else for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++){
			pinta(x0,y,PONTE_V); pinta(x0+1,y,PONTE_V);
			for(int k=0;k<=1;k++) if(dentro(x0+k,y)) altura[y][x0+k]=0; }
	}

	static void geraCidade(Estilo e, File dir) throws Exception
	{
		r = new Random(e.semente);
		chao=new int[ALT][LARG]; altura=new int[ALT][LARG];
		for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++) chao[y][x]=AGUA;

		//cada cidade tem um recorte de ilhas diferente
		if (e.nome.equals("1")) {
			criaIlha(54,34,44,27,11); criaIlha(40,132,34,54,22);
			criaIlha(118,134,36,60,33); criaIlha(131,208,20,13,44);
		} else if (e.nome.equals("2")) {
			criaIlha(78,44,58,32,55); criaIlha(46,140,38,62,66);
			criaIlha(124,150,32,54,77); criaIlha(40,208,26,14,88);
		} else {
			criaIlha(80,40,66,26,99); criaIlha(52,126,44,46,111);
			criaIlha(120,180,38,40,122); criaIlha(30,196,22,20,133);
		}
		criaPraias(3);
		int[][] dist = distanciaDaAgua();

		if (e.nome.equals("1")) {
			urbaniza(20,14,88,54,e.passoA,dist,5,false);
			urbaniza(12,86,68,132,e.passoA,dist,5,false);
			urbaniza(12,134,68,178,e.passoB,dist,5,true);
			urbaniza(88,84,148,140,e.passoB,dist,5,false);
			urbaniza(88,142,148,190,e.passoA,dist,5,false);
			urbaniza(116,198,146,218,6,dist,3,true);
			ponte(52,52,52,100); ponte(62,116,108,116); ponte(60,170,106,170); ponte(128,186,128,212);
		} else if (e.nome.equals("2")) {
			urbaniza(26,16,132,70,e.passoA,dist,5,false);
			urbaniza(14,86,80,140,e.passoB,dist,5,true);
			urbaniza(14,142,80,196,e.passoA,dist,5,false);
			urbaniza(96,102,152,196,e.passoB,dist,5,true);
			urbaniza(18,198,62,218,6,dist,3,true);
			ponte(70,66,70,104); ponte(72,140,112,140); ponte(44,186,44,212);
		} else {
			urbaniza(18,14,144,62,e.passoA,dist,5,false);
			urbaniza(12,86,94,166,e.passoB,dist,5,false);
			urbaniza(86,146,154,214,e.passoA,dist,5,false);
			urbaniza(12,182,50,212,6,dist,3,true);
			ponte(70,58,70,96); ponte(88,150,116,150); ponte(34,166,34,188);
		}

		BufferedImage mChao=new BufferedImage(LARG,ALT,BufferedImage.TYPE_INT_RGB);
		BufferedImage mAlt=new BufferedImage(LARG,ALT,BufferedImage.TYPE_INT_RGB);
		BufferedImage prev=new BufferedImage(LARG*2,ALT*2,BufferedImage.TYPE_INT_RGB);

		Color[] previaCor = new Color[24];
		previaCor[ASFALTO]=e.asfalto; previaCor[RUA_V]=e.asfalto; previaCor[RUA_H]=e.asfalto;
		previaCor[CRUZAMENTO]=escurece(e.asfalto,1.2); previaCor[CALCADA]=e.calcada;
		previaCor[GRAMA]=e.grama; previaCor[ARBUSTO]=escurece(e.grama,0.85);
		previaCor[AGUA]=e.agua; previaCor[AREIA]=e.areia; previaCor[ESTACIONAMENTO]=escurece(e.asfalto,1.3);
		previaCor[TELHADO_A]=e.telhado[0]; previaCor[TELHADO_B]=e.telhado[1]; previaCor[TELHADO_C]=e.telhado[2];
		previaCor[TELHADO_CASA]=e.telhadoCasa; previaCor[PAREDE_A]=e.parede[0];
		previaCor[PAREDE_B]=e.parede[1]; previaCor[PAREDE_C]=e.parede[2]; previaCor[PAREDE_CASA]=e.paredeCasa;
		previaCor[PONTE_V]=escurece(e.asfalto,1.1); previaCor[PONTE_H]=escurece(e.asfalto,1.1);
		previaCor[HOTDOG]=new Color(210,60,50); previaCor[PRACA]=escurece(e.calcada,0.94);
		previaCor[TELHADO_TOPO]=escurece(e.telhado[1],1.2); previaCor[TERRA]=new Color(146,118,82);

		int predios=0;
		for(int y=0;y<ALT;y++) for(int x=0;x<LARG;x++)
		{
			mChao.setRGB(x,y,PALETA[chao[y][x]]);
			mAlt.setRGB(x,y,PALETA_ALTURA[Math.min(altura[y][x],5)]);
			if(altura[y][x]>0) predios++;
			Color c = previaCor[chao[y][x]];
			if(altura[y][x]>0){ double f=1.0+altura[y][x]*0.10;
				c=new Color(cl((int)(c.getRed()*f)),cl((int)(c.getGreen()*f)),cl((int)(c.getBlue()*f))); }
			int rgb=c.getRGB();
			for(int k=0;k<4;k++) prev.setRGB(x*2+k%2,y*2+k/2,rgb);
		}

		ImageIO.write(criaTileset(e),"png",new File(dir,"city"+e.nome+"_tiles.png"));
		ImageIO.write(mChao,"png",new File(dir,"city"+e.nome+"_ground.png"));
		ImageIO.write(mAlt,"png",new File(dir,"city"+e.nome+"_height.png"));
		ImageIO.write(prev,"png",new File(dir,"city"+e.nome+"_preview.png"));
		System.out.println("cidade " + e.nome + ": " + predios + " celulas com construcao");
	}

	public static void main(String[] a) throws Exception
	{
		File dir=new File(a[0]);
		geraCidade(metropole(), dir);
		geraCidade(litoranea(), dir);
		geraCidade(industrial(), dir);
		System.out.println("conjuntos de tiles: " + (T*COLS) + "x" + (T*ROWS) + ", " + (COLS*ROWS) + " tiles de " + T + "x" + T);
		System.exit(0);
	}
}
