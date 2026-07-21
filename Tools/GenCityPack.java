import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

// Gera, para cada cidade, o conjunto de tiles e os mapas do chao, de alturas
// e a previa colorida que serve de minimapa.
public class GenCityPack
{
	static final int T = 64, COLS = 6, ROWS = 4;   // 24 tiles

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

	// ---------------- desenho dos tiles ----------------
	static Random r;

	static void ruido(Graphics2D g,int ox,int oy,Color base,int qtd,int var)
	{
		for (int i=0;i<qtd;i++){int d=r.nextInt(var*2)-var;
			g.setColor(new Color(cl(base.getRed()+d),cl(base.getGreen()+d),cl(base.getBlue()+d)));
			g.fillRect(ox+r.nextInt(T),oy+r.nextInt(T),2,2);}
	}
	static int cl(int v){return Math.max(0,Math.min(255,v));}
	static Color escurece(Color c,double f){return new Color(cl((int)(c.getRed()*f)),cl((int)(c.getGreen()*f)),cl((int)(c.getBlue()*f)));}

	static void janelas(Graphics2D g,int ox,int oy,Estilo e,Color parede)
	{
		g.setColor(parede); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,parede,90,8);
		g.setColor(e.vidro);
		if (e.janela==0)            // altas e estreitas
			for(int fy=6;fy<T-10;fy+=20) for(int fx=8;fx<T-10;fx+=16){
				g.setColor(e.vidro); g.fillRect(ox+fx,oy+fy,9,14);
				g.setColor(escurece(e.vidro,1.35)); g.fillRect(ox+fx,oy+fy,9,4); }
		else if (e.janela==1)       // quadradas com moldura
			for(int fy=8;fy<T-12;fy+=22) for(int fx=9;fx<T-12;fx+=22){
				g.setColor(escurece(parede,0.75)); g.fillRect(ox+fx-2,oy+fy-2,16,16);
				g.setColor(e.vidro); g.fillRect(ox+fx,oy+fy,12,12); }
		else                         // faixa continua
			for(int fy=10;fy<T-12;fy+=20){
				g.setColor(e.vidro); g.fillRect(ox+6,oy+fy,T-12,9);
				g.setColor(escurece(parede,0.7)); g.drawRect(ox+6,oy+fy,T-12,9); }
		g.setColor(escurece(parede,0.7)); g.drawRect(ox,oy,T-1,T-1);
	}

	static BufferedImage criaTileset(Estilo e)
	{
		r = new Random(e.semente);
		BufferedImage sheet = new BufferedImage(T*COLS, T*ROWS, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sheet.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (int i=0;i<COLS*ROWS;i++)
		{
			int ox=(i%COLS)*T, oy=(i/COLS)*T;
			switch(i)
			{
			case ASFALTO: g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,140,10); break;
			case RUA_V:
				g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,140,10);
				g.setColor(e.faixa); for(int k=0;k<4;k++) g.fillRect(ox+T/2-2,oy+k*16+4,4,8); break;
			case RUA_H:
				g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,140,10);
				g.setColor(e.faixa); for(int k=0;k<4;k++) g.fillRect(ox+k*16+4,oy+T/2-2,8,4); break;
			case CRUZAMENTO:
				g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,140,10);
				g.setColor(new Color(232,232,232));
				for(int k=0;k<5;k++){ g.fillRect(ox+6+k*12,oy+2,7,10); g.fillRect(ox+6+k*12,oy+T-12,7,10);} break;
			case CALCADA:
				g.setColor(e.calcada); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.calcada,110,8);
				g.setColor(escurece(e.calcada,0.86));
				for(int k=0;k<=T;k+=16){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k);} break;
			case GRAMA: g.setColor(e.grama); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.grama,240,16); break;
			case ARBUSTO:
				g.setColor(e.grama); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.grama,240,16);
				for(int k=0;k<3;k++){
					int bx=ox+10+r.nextInt(34), by=oy+10+r.nextInt(34), rad=10+r.nextInt(8);
					g.setColor(escurece(e.arbusto,0.75)); g.fillOval(bx+2,by+3,rad,rad);
					g.setColor(e.arbusto); g.fillOval(bx,by,rad,rad);
					g.setColor(escurece(e.arbusto,1.25)); g.fillOval(bx+2,by+2,rad/2,rad/2);} break;
			case AGUA:
				g.setColor(e.agua); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.agua,140,8);
				g.setColor(escurece(e.agua,1.4));
				for(int k=0;k<5;k++) g.drawLine(ox+6+(k%2)*8,oy+8+k*12,ox+30+(k%2)*8,oy+8+k*12); break;
			case AREIA: g.setColor(e.areia); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.areia,200,12); break;
			case ESTACIONAMENTO:
				g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,140,10);
				g.setColor(new Color(220,220,220)); for(int k=0;k<=T;k+=16) g.drawLine(ox+k,oy+8,ox+k,oy+T-8); break;
			case TELHADO_A: case TELHADO_B: case TELHADO_C:
			{
				Color base = e.telhado[i-TELHADO_A];
				g.setColor(base); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,base,120,10);
				g.setColor(escurece(base,0.8)); g.drawRect(ox+1,oy+1,T-3,T-3);
				g.setColor(escurece(base,0.7)); g.fillRect(ox+12,oy+12,14,10);
				g.setColor(escurece(base,1.15)); g.fillRect(ox+T-26,oy+T-22,16,12); break;
			}
			case TELHADO_CASA:
			{
				g.setColor(e.telhadoCasa); g.fillRect(ox,oy,T,T);
				g.setColor(escurece(e.telhadoCasa,0.82));
				for(int k=0;k<T;k+=8) g.drawLine(ox,oy+k,ox+T,oy+k);       // telhas
				g.setColor(escurece(e.telhadoCasa,0.6)); g.drawRect(ox,oy,T-1,T-1);
				g.setColor(new Color(120,116,112)); g.fillRect(ox+T-20,oy+8,10,14); break;   // chamine
			}
			case PAREDE_A: janelas(g,ox,oy,e,e.parede[0]); break;
			case PAREDE_B: janelas(g,ox,oy,e,e.parede[1]); break;
			case PAREDE_C: janelas(g,ox,oy,e,e.parede[2]); break;
			case PAREDE_CASA:
			{
				g.setColor(e.paredeCasa); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.paredeCasa,80,8);
				g.setColor(escurece(e.paredeCasa,0.6)); g.fillRect(ox+24,oy+26,16,38);   // porta
				g.setColor(e.vidro); g.fillRect(ox+8,oy+16,14,14); g.fillRect(ox+44,oy+16,14,14);
				g.setColor(escurece(e.paredeCasa,0.7)); g.drawRect(ox,oy,T-1,T-1); break;
			}
			case PONTE_V: case PONTE_H:
			{
				g.setColor(e.asfalto); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.asfalto,120,8);
				Color rail = new Color(196,192,186);
				if (i==PONTE_V){
					g.setColor(e.faixa); for(int k=0;k<4;k++) g.fillRect(ox+T/2-2,oy+k*16+4,4,8);
					g.setColor(escurece(rail,0.6)); g.fillRect(ox,oy,7,T); g.fillRect(ox+T-7,oy,7,T);
					g.setColor(rail); g.fillRect(ox+1,oy,4,T); g.fillRect(ox+T-5,oy,4,T);
					g.setColor(escurece(rail,0.8)); for(int k=4;k<T;k+=12){ g.fillRect(ox+1,oy+k,4,3); g.fillRect(ox+T-5,oy+k,4,3);} }
				else {
					g.setColor(e.faixa); for(int k=0;k<4;k++) g.fillRect(ox+k*16+4,oy+T/2-2,8,4);
					g.setColor(escurece(rail,0.6)); g.fillRect(ox,oy,T,7); g.fillRect(ox,oy+T-7,T,7);
					g.setColor(rail); g.fillRect(ox,oy+1,T,4); g.fillRect(ox,oy+T-5,T,4);
					g.setColor(escurece(rail,0.8)); for(int k=4;k<T;k+=12){ g.fillRect(ox+k,oy+1,3,4); g.fillRect(ox+k,oy+T-5,3,4);} }
				break;
			}
			case HOTDOG:
			{
				g.setColor(e.calcada); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,e.calcada,80,8);
				g.setColor(escurece(e.calcada,0.86));
				for(int k=0;k<=T;k+=16){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k);}
				//guarda-sol listrado e o carrinho
				g.setColor(new Color(40,40,40)); g.fillOval(ox+16,oy+40,32,12);
				g.setColor(new Color(210,60,50)); g.fillOval(ox+12,oy+12,40,30);
				g.setColor(new Color(240,240,240));
				for(int k=0;k<4;k++) g.fillArc(ox+12,oy+12,40,30,90+k*90-14,14);
				g.setColor(new Color(180,180,185)); g.fillRect(ox+22,oy+30,20,16);
				g.setColor(new Color(230,180,90)); g.fillRect(ox+25,oy+33,14,4);
				break;
			}
			case PRACA:
			{
				Color piso = escurece(e.calcada,0.94);
				g.setColor(piso); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,piso,90,8);
				g.setColor(escurece(piso,0.85));
				for(int k=0;k<=T;k+=21){ g.drawLine(ox+k,oy,ox+k,oy+T); g.drawLine(ox,oy+k,ox+T,oy+k);}
				g.setColor(e.arbusto); g.fillOval(ox+22,oy+22,20,20); break;
			}
			case TELHADO_TOPO:
			{
				Color base = e.telhado[1];
				g.setColor(base); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,base,120,10);
				g.setColor(escurece(base,0.75)); g.fillOval(ox+14,oy+14,36,36);
				g.setColor(new Color(235,235,235)); g.setStroke(new BasicStroke(3));
				g.drawOval(ox+16,oy+16,32,32); g.drawLine(ox+24,oy+24,ox+40,oy+40); g.drawLine(ox+40,oy+24,ox+24,oy+40);
				g.setStroke(new BasicStroke(1)); break;
			}
			default:
			{
				Color te=new Color(146,118,82); g.setColor(te); g.fillRect(ox,oy,T,T); ruido(g,ox,oy,te,200,14); break;
			}
			}
		}
		g.dispose();
		return sheet;
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
			if (r.nextInt(9)==0){ alturaBloco=4+r.nextInt(2); telhado=TELHADO_TOPO; }

			for(int y=by;y<by+passo-3&&y<=y1;y++) for(int x=bx;x<bx+passo-3&&x<=x1;x++)
			{
				if(!dentro(x,y)||dist[y][x]<margem) continue;
				if(tipo==0){ chao[y][x]=telhado; altura[y][x]=alturaBloco; }
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
