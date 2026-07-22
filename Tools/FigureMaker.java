import JGames2D.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;

// Renders the figures of the manual with the engine itself, so what the
// pages show is what the engine draws and not a drawing of it
public class FigureMaker
{
	static JGEngine engine;
	static int levelIndex = 0;
	static String out;

	static URL res(String p) { return FigureMaker.class.getResource(p); }

	static void frames(JGLevel level, int count) throws Exception
	{
		for (int i = 0; i < count; i++)
		{
			JGTimeManager.update();
			level.execute();
			level.update();
			engine.windowManager.clearBackBuffer();
			level.render();
			engine.inputManager.reset();
			Thread.sleep(12);
		}
	}

	static void save(String name) throws Exception
	{
		ImageIO.write(engine.windowManager.getBackBufferImage(), "png", new File(out, name));
		System.out.println("  " + name);
	}

	//A level that only holds what a figure needs
	static abstract class Fig extends JGLevel
	{
		public void execute() {}
	}

	public static void main(String[] a) throws Exception
	{
		out = a[0];
		engine = new JGEngine();
		engine.windowManager.setResolution(760, 420, 32);
		engine.windowManager.setBackgroundColor(new Color(16, 18, 24));
		engine.windowManager.showWindow();

		spriteFigure();
		animationFigure();
		orthoFigure();
		isoFigure();
		topDownFigure();
		collisionFigure();
		fontFigure();

		System.exit(0);
	}

	/* --------------------------------------------------- a sprite and its grid */
	static void spriteFigure() throws Exception
	{
		Fig level = new Fig()
		{
			public void init()
			{
				JGSprite plane = createSprite(res("/Images/spr_airplane.png"), 1, 4);
				plane.addAnimation(6, true, 0, 3);
				plane.position.setXY(380, 300);
				plane.zoom.setXY(2.2, 2.2);
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(0);
		frames(level, 10);

		//the sheet itself, drawn over the frame so the grid can be seen
		Graphics2D g = engine.graphics;
		JGImage sheet = JGImageManager.loadImage(res("/Images/spr_airplane.png"));
		g.drawImage(sheet.getImage(), 250, 60, 512, 130, null);
		g.setColor(new Color(120, 200, 140));
		g.setStroke(new BasicStroke(2));
		for (int i = 0; i < 4; i++) g.drawRect(250 + i * 128, 60, 128, 130);
		g.setFont(new Font("Menlo", Font.BOLD, 14));
		for (int i = 0; i < 4; i++) g.drawString("" + i, 250 + i * 128 + 60, 210);
		g.setColor(new Color(200, 204, 212));
		g.setFont(new Font("verdana", Font.PLAIN, 13));
		g.drawString("spr_airplane.png  -  createSprite(url, 1, 4)", 250, 40);
		g.drawString("one sheet, four frames", 250, 232);
		g.setColor(new Color(120, 200, 140));
		g.drawString("position is the centre", 300, 380);
		g.drawLine(380, 300, 380, 366);
		g.fillOval(376, 296, 8, 8);
		save("fig_sprite.png");
	}

	/* ------------------------------------------------ an animation, frame by frame */
	static void animationFigure() throws Exception
	{
		engine.windowManager.clearBackBuffer();
		Graphics2D g = engine.graphics;
		JGImage sheet = JGImageManager.loadImage(res("/Images/spr_bigexplosion.png"));
		BufferedImage image = sheet.getImage();

		g.setColor(new Color(200, 204, 212));
		g.setFont(new Font("verdana", Font.PLAIN, 13));
		g.drawString("addAnimation(10, false, 0, 7)  -  eight frames, ten a second, once", 40, 40);

		for (int i = 0; i < 8; i++)
		{
			int sx = (i % 4) * 64, sy = (i / 4) * 64;
			int x = 40 + (i % 4) * 175, y = 70 + (i / 4) * 170;
			g.drawImage(image, x, y, x + 150, y + 150, sx, sy, sx + 64, sy + 64, null);
			g.setColor(new Color(60, 64, 74));
			g.drawRect(x, y, 150, 150);
			g.setColor(new Color(120, 200, 140));
			g.setFont(new Font("Menlo", Font.BOLD, 13));
			g.drawString("frame " + i, x + 6, y + 145);
			g.setColor(new Color(200, 204, 212));
		}
		save("fig_animation.png");
	}

	/* ------------------------------------------------------------- the three layers */
	static JGColorIndex[] colours()
	{
		JGColorIndex[] v = new JGColorIndex[10];
		v[0] = new JGColorIndex(0, new Color(0,0,0));
		v[1] = new JGColorIndex(1, new Color(255,0,0));
		v[2] = new JGColorIndex(2, new Color(0,255,0));
		v[3] = new JGColorIndex(4, new Color(0,0,255));
		v[4] = new JGColorIndex(5, new Color(255,255,0));
		v[5] = new JGColorIndex(6, new Color(0,255,255));
		v[6] = new JGColorIndex(7, new Color(255,0,255));
		v[7] = new JGColorIndex(8, new Color(255,255,255));
		v[8] = new JGColorIndex(9, new Color(175,0,0));
		v[9] = new JGColorIndex(10, new Color(0,175,0));
		return v;
	}

	static void orthoFigure() throws Exception
	{
		Fig level = new Fig()
		{
			public void init()
			{
				JGOrthoLayer layer = createOrthoLayer(res("/Images/spr_elements.png"),
					res("/Images/lay_level.bmp"), colours(), new JGVector2D(32, 32), true);
				layer.setSpeed(new JGVector2D(0, 30));

				JGSprite plane = createSprite(res("/Images/spr_airplane.png"), 1, 4);
				plane.addAnimation(12, true, 0, 3);
				plane.position.setXY(380, 300);
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(++levelIndex);
		frames(level, 60);
		label("JGOrthoLayer  -  a grid squared with the screen, repeating for ever");
		save("fig_ortho.png");
	}

	static void isoFigure() throws Exception
	{
		Fig level = new Fig()
		{
			public void init()
			{
				JGIsoLayer layer = createIsoLayer(res("/Images/spr_elements.png"),
					res("/Images/lay_level.bmp"), colours(), new JGVector2D(64, 32), true);
				layer.getOffset().setXY(-120, -40);
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(++levelIndex);
		frames(level, 8);
		label("JGIsoLayer  -  the same map, laid out as diamonds");
		save("fig_iso.png");
	}

	static void topDownFigure() throws Exception
	{
		Fig level = new Fig()
		{
			public void init()
			{
				JGTopDownLayer layer = createTopDownLayer(res("/Images/spr_elements.png"),
					res("/Images/lay_level.bmp"), colours(), new JGVector2D(32, 32), true);
				layer.createHeightMap(res("/Images/lay_level.bmp"), colours());
				layer.setWallFrameIndex(4);
				layer.getOffset().setXY(60, 60);
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(++levelIndex);
		frames(level, 8);
		label("JGTopDownLayer  -  seen from above, with the walls opening outwards");
		save("fig_topdown.png");
	}

	/* ----------------------------------------------------------------- collision */
	static void collisionFigure() throws Exception
	{
		final JGSprite[] four = new JGSprite[4];
		Fig level = new Fig()
		{
			public void init()
			{
				//apart on the left, overlapping on the right
				four[0] = plane(200, 180);
				four[1] = enemy(200, 300);
				four[2] = plane(560, 210);
				four[3] = enemy(560, 250);
			}

			JGSprite plane(double x, double y)
			{
				JGSprite s = createSprite(res("/Images/spr_airplane.png"), 1, 4);
				s.position.setXY(x, y);
				s.zoom.setXY(1.5, 1.5);
				return s;
			}

			JGSprite enemy(double x, double y)
			{
				JGSprite s = createSprite(res("/Images/spr_enemy.png"), 1, 3);
				s.position.setXY(x, y);
				s.zoom.setXY(1.5, 1.5);
				return s;
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(++levelIndex);
		frames(level, 6);

		Graphics2D g = engine.graphics;
		g.setStroke(new BasicStroke(2));
		g.setFont(new Font("Menlo", Font.BOLD, 15));

		for (int pair = 0; pair < 2; pair++)
		{
			JGSprite one = four[pair * 2], other = four[pair * 2 + 1];
			boolean hit = one.collide(other);
			Color colour = hit ? new Color(230, 120, 110) : new Color(120, 200, 140);

			g.setColor(colour);
			for (JGSprite s : new JGSprite[]{ one, other })
			{
				Rectangle r = s.getRectangle();
				g.drawRect(r.x, r.y, r.width, r.height);
			}
			g.drawString("collide(other) = " + hit, pair == 0 ? 120 : 480, 350);
		}
		label("collision is the overlap of two boxes, and costs nothing to ask");
		save("fig_collision.png");
	}

	/* --------------------------------------------------------------------- JGFont */
	static void fontFigure() throws Exception
	{
		Fig level = new Fig()
		{
			public void init()
			{
				JGFont a = createFont("verdana", Font.BOLD, 22);
				a.text = "LEFT";  a.alignment = JGFont.LEFT;
				a.color = new Color(238, 240, 246);
				a.setPosition(380, 150);

				JGFont b = createFont("verdana", Font.BOLD, 22);
				b.text = "CENTER"; b.alignment = JGFont.CENTER;
				b.color = new Color(120, 200, 140);
				b.setPosition(380, 210);

				JGFont c = createFont("verdana", Font.BOLD, 22);
				c.text = "RIGHT"; c.alignment = JGFont.RIGHT;
				c.color = new Color(110, 170, 230);
				c.setPosition(380, 270);
			}
		};
		engine.addLevel(level);
		engine.setCurrentLevel(++levelIndex);
		frames(level, 6);

		Graphics2D g = engine.graphics;
		g.setColor(new Color(200, 90, 90));
		g.setStroke(new BasicStroke(1));
		g.drawLine(380, 110, 380, 300);
		g.setFont(new Font("verdana", Font.PLAIN, 12));
		g.drawString("the position", 388, 320);
		label("JGFont  -  a line of text as an object, drawn over everything else");
		save("fig_font.png");
	}

	static void label(String text)
	{
		Graphics2D g = engine.graphics;
		g.setColor(new Color(10, 12, 16, 210));
		g.fillRect(0, 380, 760, 40);
		g.setColor(new Color(200, 204, 212));
		g.setFont(new Font("verdana", Font.PLAIN, 13));
		g.drawString(text, 20, 405);
	}
}
