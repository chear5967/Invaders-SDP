package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import entity.*;
import screen.Screen;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {
	/** Singleton instance of the class. */
	private static DrawManager instance;
	/** Current frame. */
	private static Frame frame;
	/** FileManager instance. */
	private static FileManager fileManager;
	/** Application logger. */
	private static Logger logger;
	/** Graphics context. */
	private static Graphics graphics;
	/** Buffer Graphics. */
	private static Graphics backBufferGraphics;
	/** Buffer Graphics for multi screens. */
	private static final Graphics[] threadBufferGraphics = new Graphics[2];
	/** Buffer image. */
	private static BufferedImage backBuffer;
	/** Buffer images for multi screens **/
	private static final BufferedImage[] threadBuffers = new BufferedImage[4];
	/** Small sized font. */
	private static Font fontSmall;
	/** Small sized font properties. */
	private static FontMetrics fontSmallMetrics;
	/** Normal sized font. */
	private static Font fontRegular;
	/** Normal sized font properties. */
	private static FontMetrics fontRegularMetrics;
	/** Big sized font. */
	private static Font fontBig;
	/** Big sized font properties. */
	private static FontMetrics fontBigMetrics;
	/** Vertical line width for two player mode **/
	private static final int LINE_WIDTH = 1;

	/** Sprite types mapped to their images. */
	private static Map<SpriteType, boolean[][]> spriteMap;

	/** For Shopscreen image */
	private static BufferedImage img_additionallife;
	private static BufferedImage img_bulletspeed;
	private static BufferedImage img_coin;
	private static BufferedImage img_coingain;
	private static BufferedImage img_shotinterval;
	private static BufferedImage img_saturn;
	private static BufferedImage img_saturn_destroyed;
	private static BufferedImage img_saturn2;
	private static BufferedImage img_saturn3;
	private static BufferedImage img_timelimit;

	private final Object bufferLock = new Object();
	private BufferedImage[] tempBuffers = new BufferedImage[2];

	private static BufferedImage img_bossHPbar;
	private static BufferedImage img_hacking;
	private static BufferedImage img_laserShooter;
	private static BufferedImage img_LblackHole;
	private static BufferedImage img_RblackHole;

	private static BufferedImage img_upArrow;
	private static BufferedImage img_downArrow;
	private static BufferedImage img_leftArrow;
	private static BufferedImage img_rightArrow;

	/** For item image */
	private static final Map<ItemManager.ItemType, BufferedImage> itemImages = new HashMap<>();


	/** For initialize item images */
	public static void initializeItemImages() {
		try {
			itemImages.put(ItemManager.ItemType.Bomb, loadImage("res/image/bomb.png"));
			itemImages.put(ItemManager.ItemType.LineBomb, loadImage("res/image/linebomb.png"));
			itemImages.put(ItemManager.ItemType.Barrier, loadImage("res/image/barrier.png"));
			itemImages.put(ItemManager.ItemType.Ghost, loadImage("res/image/ghost.png"));
			itemImages.put(ItemManager.ItemType.TimeStop, loadImage("res/image/timestop.png"));
			itemImages.put(ItemManager.ItemType.MultiShot, loadImage("res/image/multishot.png"));
			itemImages.put(ItemManager.ItemType.Laser, loadImage("res/image/laser.png"));
		} catch (Exception e) {
			System.err.println("아이템 이미지를 로드할 수 없습니다: " + e.getMessage());
		}
	}

	/** For loading item images */
	private static BufferedImage loadImage(String filePath) {
		try {
			File file = new File(filePath); // referencing file path
			if (!file.exists()) {
				throw new IllegalArgumentException("리소스 파일을 찾을 수 없습니다: " + filePath);
			}
			return ImageIO.read(file);
		} catch (IOException e) {
			System.err.println("이미지 로드 실패: " + filePath + " (" + e.getMessage() + ")");
			return null;
		}
	}

	/** Drawing item Hud */
	public void drawItemHud(Screen screen, int screenHeight, List<ItemManager.ItemType> storedItems) {
		int rectWidth = 50;
		int rectHeight = 50;
		int startX = 10;
		int startY = screenHeight - 70;
		int padding = 10;

		for (int i = 0; i < 2; i++) {
			int x = startX + i * (rectWidth + padding);
			int y = startY;

			if (i < storedItems.size()) {
				// bringing storedItem
				ItemManager.ItemType itemType = storedItems.get(i);
				BufferedImage itemImage = itemImages.get(itemType);
				if (itemImage != null) {
					// if item have image, print itemimage
					backBufferGraphics.drawImage(itemImage, x, y, rectWidth, rectHeight, null);
				} else {
					// if item don't have image, print red
					backBufferGraphics.setColor(Color.RED);
					backBufferGraphics.fillRect(x, y, rectWidth, rectHeight);
				}
			} else {
				// if no storeditem, print lightGRAY
				backBufferGraphics.setColor(Color.LIGHT_GRAY);
				backBufferGraphics.fillRect(x, y, rectWidth, rectHeight);
			}

			// drawing rectangle
			backBufferGraphics.setColor(Color.BLACK);
			backBufferGraphics.drawRect(x, y, rectWidth, rectHeight);
		}

	}



	/** Sprite types. */
	public static enum SpriteType {
		/** Player ship. */
		Ship,
		/** Destroyed player ship. */
		ShipDestroyed,
		/** Player bullet. */
		Bullet,
		/** Enemy bullet. */
		EnemyBullet,
		/** First enemy ship - first form. */
		EnemyShipA1,
		/** First enemy ship - second form. */
		EnemyShipA2,
		/** Second enemy ship - first form. */
		EnemyShipB1,
		/** Second enemy ship - second form. */
		EnemyShipB2,
		/** Third enemy ship - first form. */
		EnemyShipC1,
		/** Third enemy ship - second form. */
		EnemyShipC2,
		/** Bonus ship. */
		EnemyShipSpecial,
		/** Destroyed enemy ship. */
		Explosion,
		/** Barrier. */
		Barrier,
        /** Item Box. */
        ItemBox,
		/** Spider webs restricting player movement */
		Web,
		/** Obstacles preventing a player's bullet */
		Block,
		/** Obstruction 1 (satellite) */
		Blocker1,
		/** Obstruction 2 (Astronaut) */
		Blocker2,
        /** 2nd player ship. */
        Ship2,
        /** 3rd player ship. */
        Ship3,
        /** 4th player ship. */
        Ship4,
		/** Fourth enemy ship - first form. */
		EnemyShipD1,
		/** Fourth enemy ship - second form. */
		EnemyShipD2,
		/** Fifth enemy ship - first form. */
		EnemyShipE1,
		/** Fifth enemy ship - second form. */
		EnemyShipE2,
		/** Elite enemy ship - first form. */
		EnemyShipF1
	};

	/**
	 * Private constructor.
	 */
	private DrawManager() {
		fileManager = Core.getFileManager();
		logger = Core.getLogger();
		logger.info("Started loading resources.");

		try {
			spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();

			spriteMap.put(SpriteType.Ship, new boolean[13][8]);
			spriteMap.put(SpriteType.ShipDestroyed, new boolean[13][8]);
			spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
			spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
			spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
			spriteMap.put(SpriteType.Barrier, new boolean[39][11]);
			spriteMap.put(SpriteType.ItemBox, new boolean[7][7]);
			spriteMap.put(SpriteType.Web, new boolean[12][8]);
			spriteMap.put(SpriteType.Block, new boolean[20][7]);
			spriteMap.put(SpriteType.Blocker1, new boolean[182][93]); // artificial satellite
			spriteMap.put(SpriteType.Blocker2, new boolean[82][81]); // astronaut
			spriteMap.put(SpriteType.Ship2, new boolean[13][8]);
			spriteMap.put(SpriteType.Ship3, new boolean[13][8]);
			spriteMap.put(SpriteType.Ship4, new boolean[13][8]);
			spriteMap.put(SpriteType.EnemyShipD1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipD2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipE1, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipE2, new boolean[12][8]);
			spriteMap.put(SpriteType.EnemyShipF1, new boolean[16][7]);

			fileManager.loadSprite(spriteMap);
			logger.info("Finished loading the sprites.");

			// Font loading.
			fontSmall = fileManager.loadFont(10f);
			fontRegular = fileManager.loadFont(14f);
			fontBig = fileManager.loadFont(24f);
			logger.info("Finished loading the fonts.");

		} catch (IOException e) {
			logger.warning("Loading failed.");
		} catch (FontFormatException e) {
			logger.warning("Font formating failed.");
		}

		/** Shop image load*/
		try{
			img_additionallife = ImageIO.read(new File("res/image/additional life.jpg"));
			img_bulletspeed = ImageIO.read(new File("res/image/bullet speed.jpg"));
			img_coin = ImageIO.read(new File("res/image/coin.jpg"));
			img_coingain = ImageIO.read(new File("res/image/coin gain.jpg"));
			img_shotinterval = ImageIO.read(new File("res/image/shot interval.jpg"));
		} catch (IOException e) {
			logger.info("Shop image loading failed");
		}

		/* Bonus Stage image load*/
		try {
			img_saturn = ImageIO.read(new File("res/image/saturn.png"));
			img_saturn2 = ImageIO.read(new File("res/image/saturn2.png"));
			img_saturn3= ImageIO.read(new File("res/image/saturn3.png"));
			img_saturn_destroyed = ImageIO.read(new File("res/image/saturn_destroyed.png"));
			img_timelimit = ImageIO.read(new File("res/image/timelimit.png"));
		} catch (IOException e) {
			logger.info("Bonus Boss image loading failed.");
		}


		/* Boss Stage image load*/
		try {
			img_bossHPbar = ImageIO.read(new File("res/image/bossHPbar.png"));
			img_hacking = ImageIO.read(new File("res/image/404.png"));
			img_laserShooter = ImageIO.read(new File("res/image/laserShooter.png"));
			img_LblackHole = ImageIO.read(new File("res/image/blackhole01.png"));
			img_RblackHole = ImageIO.read(new File("res/image/blackhole02.png"));

		} catch (IOException e) {
			logger.info("Boss image loading failed.");
		}

		/** Puzzle image load */
		try {
			img_upArrow = ImageIO.read(new File("res/image/upArrow.png"));
			img_downArrow = ImageIO.read(new File("res/image/downArrow.png"));
			img_leftArrow = ImageIO.read(new File("res/image/leftArrow.png"));
			img_rightArrow = ImageIO.read(new File("res/image/rightArrow.png"));
		} catch (IOException e) {
			logger.info("Puzzle image loading failed.");
		}
	}

	/**
	 * Returns shared instance of DrawManager.
	 *
	 * @return Shared instance of DrawManager.
	 */
	protected static DrawManager getInstance() {
		if (instance == null)
			instance = new DrawManager();
		return instance;
	}

	/**
	 * Sets the frame to draw the image on.
	 *
	 * @param currentFrame
	 *            Frame to draw on.
	 */
	public void setFrame(final Frame currentFrame) {
		frame = currentFrame;
	}

	/**
	 * First part of the drawing process. Initializes buffers, draws the
	 * background and prepares the images.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	public void initDrawing(final Screen screen) {
		synchronized(bufferLock) {
			backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			graphics = frame.getGraphics();
			backBufferGraphics = backBuffer.getGraphics();

			// Initialize background black
			backBufferGraphics.setColor(Color.BLACK);
			backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());

			// Initialize font metrics
			fontSmallMetrics = backBufferGraphics.getFontMetrics(fontSmall);
			fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
			fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);
		}

		//drawBorders(screen);
		//drawGrid(screen);
	}

	/**
	 * Draws the completed drawing on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void completeDrawing(final Screen screen) {
		synchronized(bufferLock) {
			graphics.drawImage(backBuffer, frame.getInsets().left,
					frame.getInsets().top, frame);
		}
	}

	/**
	 * Draws an entity, using the appropriate image.
	 *
	 * @param entity
	 *            Entity to be drawn.
	 * @param positionX
	 *            Coordinates for the left side of the image.
	 * @param positionY
	 *            Coordinates for the upper side of the image.
	 */
	public void drawEntity(final Entity entity, final int positionX,
                           final int positionY) {
		boolean[][] image = spriteMap.get(entity.getSpriteType());

		backBufferGraphics.setColor(entity.getColor());
		for (int i = 0; i < image.length; i++)
			for (int j = 0; j < image[i].length; j++)
				if (image[i][j])
					backBufferGraphics.drawRect(positionX + i * 2, positionY
							+ j * 2, 1, 1);
	}

	//Drawing an Entity (Blocker) that requires angle setting
	public void drawRotatedEntity(Entity entity, int x, int y, double angle) {
		Graphics2D g2d = (Graphics2D) backBufferGraphics; // Convert to Graphics2D
		AffineTransform oldTransform = g2d.getTransform(); // Save previous conversion

		//Set center point to rotate
		int centerX = x + entity.getWidth() / 2;
		int centerY = y + entity.getHeight() / 2;

		//rotate by a given angle
		g2d.rotate(Math.toRadians(angle), centerX, centerY);

		//Drawing entities
		drawEntity(entity, x, y);

		g2d.setTransform(oldTransform); // Restore to original conversion state
	}

	/**
	 * For debugging purposes, draws the canvas borders.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawBorders(final Screen screen) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
		backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
		backBufferGraphics.drawLine(screen.getWidth() - 1, 0,
				screen.getWidth() - 1, screen.getHeight() - 1);
		backBufferGraphics.drawLine(0, screen.getHeight() - 1,
				screen.getWidth() - 1, screen.getHeight() - 1);
	}

	/**
	 * For debugging purposes, draws a grid over the canvas.
	 *
	 * @param screen
	 *            Screen to draw in.
	 */
	@SuppressWarnings("unused")
	private void drawGrid(final Screen screen) {
		backBufferGraphics.setColor(Color.DARK_GRAY);
		for (int i = 0; i < screen.getHeight() - 1; i += 2)
			backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
		for (int j = 0; j < screen.getWidth() - 1; j += 2)
			backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
	}

	/**
	 * Draws current score on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Current score.
	 */
	public void drawScore(final Screen screen, final int score) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("%04d", score);
		backBufferGraphics.drawString(scoreString, screen.getWidth() - 60, 25);
	}
	/**
	 * Draws level on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Current level.
	 */
	public void drawLevel(final Screen screen, final int level) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		String scoreString = String.format("lv.%d", level);
		backBufferGraphics.drawString(scoreString, screen.getWidth() / 2 - 60, 25);
	}

	/**
	 * Draws elapsed time on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param elapsedTime
	 *            Elapsed time.
	 */
	public void drawElapsedTime(final Screen screen, final int elapsedTime) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.LIGHT_GRAY);

		int cent = (elapsedTime % 1000)/10;
		int seconds = elapsedTime / 1000;
		int sec = seconds % 60;
		int min = seconds / 60;

        String elapsedTimeString;
        if (min < 1){
            elapsedTimeString = String.format("%d.%02d", sec, cent);
        } else {
            elapsedTimeString = String.format("%d:%02d.%02d", min, sec, cent);
        }
        backBufferGraphics.drawString(elapsedTimeString, screen.getWidth()/2, 25);
	}

	/**

	 * Draws alert message on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param alertMessage
	 *            Alert message.
	 */
	public void drawAlertMessage(final Screen screen, final String alertMessage) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.RED);
		backBufferGraphics.drawString(alertMessage,
				(screen.getWidth() - fontRegularMetrics.stringWidth(alertMessage))/2, 65);
	}

	/**

	 * Draws number of remaining lives on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param lives
	 *            Current lives.
	 */
	public void drawLives(final Screen screen, final int lives, final Ship.ShipType shipType) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(Integer.toString(lives), 20, 25);
		Ship dummyShip = ShipFactory.create(shipType, 0, 0);
		for (int i = 0; i < lives; i++)
			drawEntity(dummyShip, 40 + 35 * i, 10);
	}

	/**
	 * Draws launch trajectory on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param positionX
	 *            X coordinate of the line.
	 */
	public void drawLaunchTrajectory(final Screen screen, final int positionX, final int positionY, final double angle) {
		backBufferGraphics.setColor(Color.DARK_GRAY);
		double shootAngle = Math.toRadians(angle);

		// 시작점 설정
		int startX = positionX + 13; // 플레이어 중앙
		int startY = positionY ; // 플레이어 위치

		// 속도 및 이동 간격 조정
		double speed = 10; // 총알 이동 속도
		double step = 1; // 경로 갱신 간격 (값이 작을수록 더 부드러움)

		// 경로 그리기
		for (double i = 0; i < screen.getHeight() + 1000; i += step) {
			// 각도에 따른 x, y 좌표 계산
			int offsetX = (int) (Math.cos(shootAngle) * speed * i / 20);
			int offsetY = (int) (Math.sin(shootAngle) * speed * i / 20);

			// 직사각형 그리기
			backBufferGraphics.drawRect(startX - offsetX, startY - offsetY, 1, 1); // 작게 설정
		}
	}

	/**
	 * Draws a thick line from side to side of the screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param positionY
	 *            Y coordinate of the line.
	 */
	public void drawHorizontalLine(final Screen screen, final int positionY) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
		backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(),
				positionY + 1);
	}

	/**
	 * Draws a thick line from side to side of the screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawVerticalLine(final Screen screen) {
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawLine(screen.getWidth() /2  ,0,screen.getWidth() / 2,screen.getHeight());
	}

	/**
	 * Draws game title.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawTitle(final Screen screen) {
		String titleString = "Invaders";
		String instructionsString =
				"select with w+s / arrows, confirm with space";

		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 5 * 2);

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 5);
	}

	public void drawGameTitle(final Screen screen) {
		String titleString = "Invaders";
		backBufferGraphics.setColor(Color.DARK_GRAY);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 2);
	}

	/**
	 * Draws main menu.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param option
	 *            Option selected.
	 */
	public void drawMenu(final Screen screen, final int option, final int coin) {
		String playString = "Play";
		String shopString = "SHOP";
		String coinString = "YOUR COIN: " + coin;
		String achievementString = "ACHIEVEMENT";
		String settingString = "SETTING";
		String exitString = "EXIT";

		if (option == 6) /*option2 => Game Settings */
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, playString,
				screen.getHeight() / 7 * 4);

		if (option == 3) /*option3 => Shop */
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, shopString, screen.getHeight()
				/ 7 * 4 + fontRegularMetrics.getHeight() * 2);

		backBufferGraphics.setColor(Color.ORANGE);
		drawCenteredSmallString(screen, coinString, screen.getHeight()
				/ 7 * 4 + fontRegularMetrics.getHeight() * 3);

		if (option == 4) /*option4 => Achievement */
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, achievementString, screen.getHeight()
				/ 7 * 4 + fontRegularMetrics.getHeight() * 5);


		if (option == 5) /*option5 => Setting */
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, settingString, screen.getHeight()
				/ 7 * 4 + fontRegularMetrics.getHeight() * 7);

		if (option == 0) /*option0 => exit */
			backBufferGraphics.setColor(Color.GREEN);
		else
			backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, exitString, screen.getHeight()
				/ 7 * 4 + fontRegularMetrics.getHeight() * 9);
	}



	/**
	 * Draws game results.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param score
	 *            Score obtained.
	 * @param livesRemaining
	 *            Lives remaining when finished.
	 * @param shipsDestroyed
	 *            Total ships destroyed.
	 * @param accuracy
	 *            Total accuracy.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawResults(final Screen screen, final int score,
							final int livesRemaining, final int shipsDestroyed,
							final double accuracy, final boolean isNewRecord, final int coinsEarned) {
		String scoreString = String.format("score %04d", score);
		String livesRemainingString = "lives remaining " + livesRemaining;
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String
				.format("accuracy %.2f%%", accuracy);
		String coinsEarnedString = "EARNED COIN " + coinsEarned;

		int height = isNewRecord ? 4 : 2;

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, scoreString, screen.getHeight()
				/ height);
		drawCenteredRegularString(screen, livesRemainingString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 2);
		drawCenteredRegularString(screen, shipsDestroyedString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 4);
		drawCenteredRegularString(screen, accuracyString, screen.getHeight()
				/ height + fontRegularMetrics.getHeight() * 6);
		backBufferGraphics.setColor(Color.YELLOW);
		drawCenteredRegularString(screen, coinsEarnedString, screen.getHeight()
				/ height + fontRegularMetrics.getHeight() * 9);
	}

	/**
	 * Draws basic content of game over screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param acceptsInput
	 *            If the screen accepts input.
	 * @param isNewRecord
	 *            If the score is a new high score.
	 */
	public void drawGameOver(final Screen screen, final boolean acceptsInput,
							 final boolean isNewRecord, final int menuOptionSelected) {
		String gameOverString = "Game Over";
		String continueString = "RETRY";
		String exitString = "Exit";

		int height = isNewRecord ? 4 : 2;

		// "Game Over" 텍스트 그리기
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, gameOverString, screen.getHeight()
				/ height - fontBigMetrics.getHeight() * 2);

		// "Continue" 항목 그리기 (선택되었을 때는 초록색, 그렇지 않으면 흰색)
		if (menuOptionSelected == 0) {
			backBufferGraphics.setColor(Color.GREEN); // 선택된 항목은 초록색
		} else {
			backBufferGraphics.setColor(Color.WHITE); // 선택되지 않은 항목은 흰색
		}
		drawCenteredRegularString(screen, continueString,
				screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 11);
		// "Exit" 항목 그리기 (선택되었을 때는 초록색, 그렇지 않으면 흰색)
		if (menuOptionSelected == 1) {
			backBufferGraphics.setColor(Color.GREEN); // 선택된 항목은 초록색
		} else {
			backBufferGraphics.setColor(Color.WHITE); // 선택되지 않은 항목은 흰색
		}
		drawCenteredRegularString(screen, exitString,
				screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 13);
	}

	/**
	 * Draws achievement screen title and instructions.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawAchievementMenu(final Screen screen, final int totalScore, final int totalPlayTime, final int maxCombo,
									final int currentPerfectStage, final int nextPerfectStage, boolean checkFlawlessFailure) {
		//high score section
		String highScoreTitle = "High Scores";

		//cumulative section
		String totalScoreTitle = "Total Score";
		String totalPlayTimesTitle = "-Total  Playtime-";

		// centered strings
		String achievementTitle = "Achievement";
		String instructionsString = "Press ESC to return";
		String achievementsStatusTitle = "Achievements Status";
		String achievementsExplain = "Applies to single-player play only";

		// Achievements names
		String maxComboTitle = " Combo Mastery ";
		String perfectClearTitle = "perfect clear";
		String flawlessFailureTitle = "Flawless Failure";
		String eternityTimeTitle = "A time of eternity";

		// draw "perfect clear"
		if (currentPerfectStage <= 6) {
			String[] PERFECT_COIN_REWARD = { "200", "400", "800", "2000", "3000", "4000", "5000"}; // 퍼펙트 스테이지 리워드
			backBufferGraphics.setColor(Color.orange);
			drawRightSideAchievementCoinBigString(screen, PERFECT_COIN_REWARD[currentPerfectStage],
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*3);

			backBufferGraphics.setColor(Color.green);
			drawRightSideAchievementSmallString_1(screen,"current",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*2+7);
			backBufferGraphics.setColor(Color.red);
			drawRightSideAchievementSmallString_2(screen,"target",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*2+7);

			String sampleAchievementsString2 = "lv." + currentPerfectStage + "   =>  lv." +
					nextPerfectStage;
			backBufferGraphics.setColor(Color.WHITE);
			drawRightSideAchievementBigString(screen, sampleAchievementsString2,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*3);

		}
		else{
			backBufferGraphics.setColor(Color.gray);
			drawRightSideAchievementCoinBigString(screen, "5000",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*3);

			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementSmallEventString2(screen, "You clear all levels perfectly",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*2+fontBigMetrics.getHeight()*3-5);

			String sampleAchievementsString2 = " 100% Clear !! ";
			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementBigString(screen, sampleAchievementsString2,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()*3);

		}

		// draw "achievement"
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, achievementTitle, screen.getHeight() / 8);

		// draw instruction
		backBufferGraphics.setColor(Color.GRAY);
		drawCenteredRegularString(screen, instructionsString,
				screen.getHeight() / 8 + fontRegularMetrics.getHeight());

		backBufferGraphics.setColor(Color.cyan);
		drawCenteredRegularString(screen, achievementsExplain,
				screen.getHeight() / 7 + fontBigMetrics.getHeight() );

		// draw "high score"
		backBufferGraphics.setColor(Color.GREEN);
		drawLeftSideScoreRegularString(screen, highScoreTitle,
				screen.getHeight() / 5+ fontBigMetrics.getHeight());

		// draw total score
		backBufferGraphics.setColor(Color.yellow);
		drawRightSideCumulativeRegularString(screen, totalScoreTitle,
				screen.getHeight() / 5 + fontBigMetrics.getHeight());

		// draw "Total play-time"
		backBufferGraphics.setColor(Color.yellow);
		drawRightSideCumulativeRegularString(screen, totalPlayTimesTitle,
				screen.getHeight() / 5 + 2*fontRegularMetrics.getHeight()+2* fontBigMetrics.getHeight()+10 );

		// draw "Total Score"
		backBufferGraphics.setColor(Color.WHITE);
		String totalScoreString = String.format("%s", totalScore);
		drawRightSideCumulativeBigString(screen, totalScoreString, screen.getHeight() / 3
				- fontRegularMetrics.getHeight() + 10);

		// draw "achievement status"
		backBufferGraphics.setColor(Color.MAGENTA);
		drawCenteredBigString(screen, achievementsStatusTitle,
				screen.getHeight() / 2 + fontBigMetrics.getHeight() );



		// draw "high accuracy"
		backBufferGraphics.setColor(Color.WHITE);
		drawLeftSideAchievementRegularString(screen, maxComboTitle,
				screen.getHeight() /2 + fontRegularMetrics.getHeight()*3+fontBigMetrics.getHeight()+7);

		// draw "Perfect clear"
		backBufferGraphics.setColor(Color.WHITE);
		drawLeftSideAchievementRegularString(screen, perfectClearTitle,
				screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*2+7);

		// draw "Flawless Failure"
		backBufferGraphics.setColor(Color.WHITE);
		drawLeftSideAchievementRegularString(screen, flawlessFailureTitle,
				screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*3+5);

		// draw "best friends"
		backBufferGraphics.setColor(Color.WHITE);
		drawLeftSideAchievementRegularString(screen, eternityTimeTitle,
				screen.getHeight() /2 + fontRegularMetrics.getHeight()*6+fontBigMetrics.getHeight()*4+3);

		int totalHours = totalPlayTime / 3600;
		int remainHours = totalPlayTime % 3600;

		int totalMinutes = remainHours / 60;
		int remainMinutes = remainHours % 60;

		int totalSeconds = remainMinutes % 60;

		// draw total play time record
		String totalPlayTimeeString = String.format("%02dH %02dm %02ds",totalHours,totalMinutes,totalSeconds);
		backBufferGraphics.setColor(Color.WHITE);
		drawRightSideCumulativeBigString(screen, totalPlayTimeeString, screen.getHeight() / 2
				- fontRegularMetrics.getHeight() - 15);

		// draw accuracy reward
		final String[] ACCURACY_COIN_REWARD = {"500", "1500", "2000", "2500"};

		// draw accuracy achievement
		if (maxCombo >= 25) {
			backBufferGraphics.setColor(Color.gray);
			drawRightSideAchievementCoinBigString(screen, ACCURACY_COIN_REWARD[3],
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*2+fontBigMetrics.getHeight()*2);

			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementSmallEventString(screen, "You record high combo",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*2+fontBigMetrics.getHeight()+8);

			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementBigString(screen, "You are crazy!",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*2+fontBigMetrics.getHeight()*2);
		} else {
			backBufferGraphics.setColor(Color.orange);

			drawRightSideAchievementComboString_1(screen, "combo",
					screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 5+5);
			drawRightSideAchievementComboString_2(screen, "combo",
					screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 5+5);


			backBufferGraphics.setColor(Color.green);
			drawRightSideAchievementSmallString_1(screen, "current",
					screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 4 - 2);
			backBufferGraphics.setColor(Color.red);
			drawRightSideAchievementSmallString_2(screen, "target",
					screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 4 - 2);
			if (maxCombo < 10) {
				backBufferGraphics.setColor(Color.orange);
				drawRightSideAchievementCoinBigString(screen, ACCURACY_COIN_REWARD[0],
						screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 2 + fontBigMetrics.getHeight() * 2);

				backBufferGraphics.setColor(Color.WHITE);
				String accuracyAchievement = String.format("             %d", maxCombo) + " =>" + "         10";
				drawRightSideAchievementBigString(screen, accuracyAchievement,
						screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 5 + 5);
			} else {
				backBufferGraphics.setColor(Color.orange);
				drawRightSideAchievementCoinBigString(screen, ACCURACY_COIN_REWARD[maxCombo <= 9 ? 0 : maxCombo / 5 - 1],
						screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 2 + fontBigMetrics.getHeight() * 2);

				backBufferGraphics.setColor(Color.WHITE);
				String accuracyAchievement = String.format("             %d", maxCombo) + " =>" + String.format("         %d", maxCombo <= 9 ? 10 : (((( (maxCombo - 10) / 5) + 1) * 5 ) + 10));
				drawRightSideAchievementBigString(screen, accuracyAchievement,
						screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 5 + 5);
			}
		}

		// draw flawless failure achievement
		String flawlessFailureReward = "1000";
		if (checkFlawlessFailure) {
			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementBigString(screen, "Complete!",
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*4-5);
			backBufferGraphics.setColor(Color.gray);
			drawRightSideAchievementCoinBigString(screen, flawlessFailureReward,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*4-5);

		} else {
			String explainFlawlessFailure_1 = "    Achieved when the game ends";
			String explainFlawlessFailure_2 = "                with 0% accuracy.";
			backBufferGraphics.setColor(Color.GRAY);
			drawRightSideAchievementSmallString_3(screen, explainFlawlessFailure_1,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*3+fontSmallMetrics.getHeight());

			backBufferGraphics.setColor(Color.GRAY);
			drawRightSideAchievementSmallString_3(screen, explainFlawlessFailure_2,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*3+fontSmallMetrics.getHeight()*2);
			backBufferGraphics.setColor(Color.orange);
			drawRightSideAchievementCoinBigString(screen, flawlessFailureReward,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*4+fontBigMetrics.getHeight()*4-5);
		}

		// draw play time achievement
		String eternityTimeReward = "1000";
		String sampleAchievementsString = "complete!";
		String explainEternityTime_1 = "              Total play time ";
		String explainEternityTime_2 = "        must exceed 10 minutes...";
		if (totalPlayTime >= 600) {
			backBufferGraphics.setColor(Color.GREEN);
			drawRightSideAchievementBigString(screen, sampleAchievementsString,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*5-5);
			backBufferGraphics.setColor(Color.gray);
			drawRightSideAchievementCoinBigString(screen, eternityTimeReward,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*5-5);

		} else {
			backBufferGraphics.setColor(Color.GRAY);
			drawRightSideAchievementSmallString_3(screen, explainEternityTime_1,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*4+fontSmallMetrics.getHeight());
			backBufferGraphics.setColor(Color.GRAY);
			drawRightSideAchievementSmallString_3(screen, explainEternityTime_2,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*4+fontSmallMetrics.getHeight()*2);
			backBufferGraphics.setColor(Color.orange);
			drawRightSideAchievementCoinBigString(screen, eternityTimeReward,
					screen.getHeight() /2 + fontRegularMetrics.getHeight()*5+fontBigMetrics.getHeight()*5-5);

		}
	}

	/**
	 * Draws high scores.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param highScores
	 *            List of high scores.
	 */
	public void drawHighScores(final Screen screen,
							   final List<Score> highScores) {
		backBufferGraphics.setColor(Color.WHITE);
		int i = 0;
		String scoreString = "";

		final int limitDrawingScore = 3;
		int countDrawingScore = 0;
		for (Score score : highScores) {
			scoreString = String.format("%s        %04d", score.getName(),
					score.getScore());
			drawLeftSideScoreRegularString(screen, scoreString, screen.getHeight()
					/ 4 + fontRegularMetrics.getHeight() * (i + 1) * 2);
			i++;
			countDrawingScore++;
			if(countDrawingScore>=limitDrawingScore){
				break;
			}
		}
	}

	/**
	 * Draws a centered string on small font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	private void drawCenteredSmallString(final Screen screen, final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontSmallMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws credit screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawEndingCredit(final Screen screen, List<String> creditlist, int currentFrame)  {
		backBufferGraphics.setColor(Color.WHITE);
		final int startPoint = screen.getHeight() / 2;

		for (int i = 0;i < creditlist.size(); i++) {
			String target = creditlist.get(i);
			drawCenteredRegularString(screen, target,startPoint + (fontRegularMetrics.getHeight() * 2) * i - currentFrame);
		}


	}

	/**
	 * Draws a centered string on regular font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredRegularString(final Screen screen,
										  final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws a centered string on big font.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param string
	 *            String to draw.
	 * @param height
	 *            Height of the drawing.
	 */
	public void drawCenteredBigString(final Screen screen, final String string,
									  final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontBigMetrics.stringWidth(string) / 2, height);
	}

	// left side score
	public void drawLeftSideScoreRegularString(final Screen screen,
											   final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() / 4
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}
	public void drawLeftSideScoreSmallString(final Screen screen,
											 final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() / 4
				- fontRegularMetrics.stringWidth(string) / 3, height);
	}

	//right side Cumulative score
	public void drawRightSideCumulativeRegularString(final Screen screen,
													 final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() *71/ 100
				- fontRegularMetrics.stringWidth(string)/2 , height);
	}
	public void drawRightSideCumulativeBigString(final Screen screen,
												 final String string, final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() *71/ 100
				- fontBigMetrics.stringWidth(string)/2, height);
	}


	// left side achievement
	public void drawLeftSideAchievementRegularString(final Screen screen,
													 final String string, final int height) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(string, screen.getWidth() *22/ 100
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}
	public void drawLeftSideAchievementSmallString(final Screen screen,
												   final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *26/ 100
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	// right side achievement(sample)
	public void drawRightSideAchievementSmallEventString(final Screen screen,
														 final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *65/100-
				fontRegularMetrics.stringWidth(string)/2, height);
	}public void drawRightSideAchievementSmallEventString2(final Screen screen,
														   final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *68/100-
				fontRegularMetrics.stringWidth(string)/2, height);
	}

	public void drawRightSideAchievementBigString(final Screen screen,
												  final String string, final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth() *63/100-
				fontRegularMetrics.stringWidth(string), height);
	}
	public void drawRightSideAchievementComboString_1(final Screen screen,
													  final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *52/100-
				fontRegularMetrics.stringWidth(string), height);
	}public void drawRightSideAchievementComboString_2(final Screen screen,
													   final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *74/100-
				fontRegularMetrics.stringWidth(string), height);
	}
	public void drawRightSideAchievementSmallString_1(final Screen screen,
													  final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *59/100-
				fontRegularMetrics.stringWidth(string), height);
	}public void drawRightSideAchievementSmallString_2(final Screen screen,
													   final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() *77/100-
				fontRegularMetrics.stringWidth(string), height);
	}

	public void drawRightSideAchievementSmallString_3(final Screen screen,
													  final String string, final int height) {
		backBufferGraphics.setFont(fontSmall);
		backBufferGraphics.drawString(string, screen.getWidth() / 2-
				fontRegularMetrics.stringWidth(string) / 7, height);
	}

	public void drawRightSideAchievementCoinBigString(final Screen screen,
													  final String string, final int height) {
		backBufferGraphics.setFont(fontBig);
		backBufferGraphics.drawString(string, screen.getWidth()*81/100 , height);
	}



	public void drawStory(final Screen screen){
		backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		graphics = frame.getGraphics();
		backBufferGraphics = backBuffer.getGraphics();

		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics
				.fillRect(0, 0, screen.getWidth(), screen.getHeight());


		// 텍스트 박스 채우기
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(42, 400, 500, 160); // (x, y, width, height)

		// 직사각형 테두리를 흰색으로 그리기
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawRect(42, 400, 500, 160); // (x, y, width, height)

	}

	public static Graphics getBackBufferGraphics() {return backBufferGraphics; }

	/**
	 * Countdown to game start.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param level
	 *            Game difficulty level.
	 * @param number
	 *            Countdown number.
	 * @param bonusLife
	 *            Checks if a bonus life is received.
	 */

	public void drawCountDown(final Screen screen, final int level,
							  final int number, final boolean bonusLife) {
		int rectWidth = screen.getWidth();
		int rectHeight = screen.getHeight() / 6;
		backBufferGraphics.setColor(Color.BLACK);
		backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2,
				rectWidth, rectHeight);
		backBufferGraphics.setColor(Color.GREEN);
		if (number >= 4)
			if (!bonusLife) {
				if (level == 3 || level == 6) {
					drawCenteredBigString(screen, "Level " + level + " - Boss Round",
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				} else if (level == 4 || level == 7){
					drawCenteredBigString(screen, "Level " + level + " - Bonus Round",
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				} else {
					drawCenteredBigString(screen, "Level " + level,
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				}
			} else {
				if (level == 3 || level == 6) {
					drawCenteredBigString(screen, "Level " + level + " - Boss Round"
									+ " (Bonus life!)",
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				} else if (level == 4 || level == 7) {
					drawCenteredBigString(screen, "Level " + level + " - Bonus Round"
									+ " (Bonus life!)",
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				} else {
					drawCenteredBigString(screen, "Level " + level
									+ " (Bonus life!)",
							screen.getHeight() / 2
									+ fontBigMetrics.getHeight() / 3);
				}
			}
		else if (number != 0)
			drawCenteredBigString(screen, Integer.toString(number),
					screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
		else
			drawCenteredBigString(screen, "GO!", screen.getHeight() / 2
					+ fontBigMetrics.getHeight() / 3);
	}
	/**
	 * Draws recorded highscores on screen.
	 *
	 * @param highScores
	 *            Recorded highscores.
	 */

	public void drawRecord(List<Score> highScores, final Screen screen) {

		//add variable for highest score
		int highestScore = -1;
		String highestPlayer = "";

		// find the highest score from highScores list
		for (Score entry : highScores) {
			if (entry.getScore() > highestScore) {
				highestScore = entry.getScore();
				highestPlayer = entry.getName();
			}
		}


		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.LIGHT_GRAY);
		FontMetrics metrics = backBufferGraphics.getFontMetrics(fontRegular);
		String highScoreDisplay = highestPlayer + " " + highestScore;

		backBufferGraphics.drawString(highScoreDisplay,
				screen.getWidth() - metrics.stringWidth(highScoreDisplay) - 76, 25);
	}

	/**
	 * Draws ReloadTimer on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param ship
	 *            player's ship.
	 * @param remainingTime
	 *            remaining reload time.
	 */
	public void drawReloadTimer(final Screen screen,final Ship ship,final long remainingTime) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		if(remainingTime > 0){

			int shipX = (int) ship.getPositionX();
			int shipY = (int) ship.getPositionY();
			int shipWidth = ship.getWidth();
			int circleSize = 16;
			int startAngle = 90;
			int endAngle = 0;
			switch(Core.BASE_SHIP){
				case Ship.ShipType.VoidReaper:
					endAngle = 360 * (int)remainingTime / (int)(750 * 0.4);
					break;
				case Ship.ShipType.CosmicCruiser:
					endAngle = 360 * (int)remainingTime / (int)(750 * 1.6);
					break;
				case Ship.ShipType.StarDefender:
					endAngle = 360 * (int)remainingTime / (int)(750 * 1.0);
					break;
				case Ship.ShipType.GalacticGuardian:
					endAngle = 360 * (int)remainingTime / (int)(750 * 1.2);
					break;


			}

			backBufferGraphics.fillArc(shipX + shipWidth/2 - circleSize/2, shipY - 3*circleSize/2,
					circleSize, circleSize, startAngle, endAngle);
		}
	}

	/**
	 * Draws Combo on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param combo
	 *            Number of enemies killed in a row.
	 */
	public void drawCombo(final Screen screen, final int combo) {
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.WHITE);
		if (combo >= 2) {
			String comboString = String.format("Combo %03d", combo);
			backBufferGraphics.drawString(comboString, screen.getWidth() - 100, 85);
		}
	}

	/**
	 * Draws intermediate aggregation on screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param maxCombo
	 *            Value of maxCombo.
	 * @param elapsedTime
	 *            Value of elapsedTime.
	 * @param lapTime
	 *            Value of lapTime/prevTime.
	 * @param score
	 *            Value of score/prevScore.
	 * @param tempScore
	 *            Value of tempScore.
	 */
	public void interAggre(final Screen screen, final int level, final int maxCombo,
						   final int elapsedTime, final int lapTime,
						   final int score, final int tempScore) {

		int prevTime = elapsedTime - lapTime;
		int prevScore = score - tempScore;

		int pcent = (prevTime % 1000)/10;
		int pseconds = prevTime / 1000;
		int psec = pseconds % 60;
		int pmin = pseconds / 60;

		String timeString;
		if (pmin < 1){
			timeString = String.format("Elapsed Time: %d.%02d", psec, pcent);
		} else {
			timeString = String.format("Elapsed Time: %d:%02d.%02d", pmin, psec, pcent);
		}

		String levelString = String.format("Statistics at Level %d", level);
		String comboString = String.format("MAX COMBO: %03d", maxCombo);
		String scoreString = String.format("Scores earned: %04d", prevScore);

		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.setColor(Color.GREEN);
		backBufferGraphics.drawString(levelString,
				(screen.getWidth() - fontRegularMetrics.stringWidth(levelString))/2,
				5*screen.getHeight()/7);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawString(comboString,
				(screen.getWidth() - fontRegularMetrics.stringWidth(comboString))/2,
				5*screen.getHeight()/7 + 21);
		backBufferGraphics.drawString(timeString,
				(screen.getWidth() - fontRegularMetrics.stringWidth(timeString))/2,
				5*screen.getHeight()/7 + 42);
		backBufferGraphics.drawString(scoreString,
				(screen.getWidth() - fontRegularMetrics.stringWidth(scoreString))/2,
				5*screen.getHeight()/7 + 63);
	}

	/**
	 * Draws the game setting screen.
	 *
	 * @param screen
	 *            Screen to draw on.
	 */
	public void drawGameSetting(final Screen screen) {
		String titleString = "Game Setting";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, titleString, screen.getHeight() / 100 * 25);
	}

	public void drawSettingsScreen(final Screen screen) {
		String settingsTitle = "Settings"; // 타이틀

		// 타이틀을 초록색으로 중앙에 그리기
		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, settingsTitle, screen.getHeight() / 8);
	}

	/** 볼륨 바를 그리는 메서드 */
	public void drawVolumeBar(Screen screen, int x, int y, int totalWidth, int filledWidth, boolean isSelected) {
		// 선택된 경우 초록색, 그렇지 않으면 흰색으로 표시
		backBufferGraphics.setColor(isSelected ? Color.GREEN : Color.WHITE);
		backBufferGraphics.fillRect(x, y, filledWidth, 10); // 채워진 부분

		// 나머지 부분은 회색으로 표시
		backBufferGraphics.setColor(Color.GRAY);
		backBufferGraphics.fillRect(x + filledWidth, y, totalWidth - filledWidth, 10); // 바의 나머지 부분
	}

	/** 퍼센트 값을 그리는 메서드 */
	public void drawVolumePercentage(Screen screen, int x, int y, int volume, boolean isSelected) {
		String volumeText = volume + "%";
		// 선택된 경우 초록색, 그렇지 않으면 흰색으로 표시
		backBufferGraphics.setColor(isSelected ? Color.GREEN : Color.WHITE);
		drawCenteredRegularString(screen, volumeText, y); // 퍼센트 값을 중앙에 표시
	}

	/** Almost same function as drawVolumeBar to draw a bar to select the ship*/
	public void drawShipBoxes(Screen screen, int x, int y, boolean isSelected, int index, boolean current) {
		if(current){
			// Ship box
			backBufferGraphics.setColor(isSelected ? Color.GREEN : Color.WHITE);
			backBufferGraphics.fillRect(x + index*60, y+index*20, isSelected ? 0 : 10, 10);
			backBufferGraphics.setColor(Color.GREEN);
			backBufferGraphics.fillRect(x + index*60, y+index*20, (isSelected ? 10 : 0), 10);
			// Ship name
			backBufferGraphics.setFont(fontRegular);
			backBufferGraphics.drawString(Ship.ShipType.values()[index].name(), x + index*60 + 15, y+index*20);
		} else {
			// Ship box
			backBufferGraphics.setColor(isSelected ? Color.GREEN : Color.WHITE);
			backBufferGraphics.fillRect(x + index*60, y+index*20, isSelected ? 0 : 10, 10);
			backBufferGraphics.setColor(Color.GRAY);
			backBufferGraphics.fillRect(x + index*60, y+index*20, (isSelected ? 10 : 0), 10);
			// Ship name
			backBufferGraphics.setFont(fontRegular);
			backBufferGraphics.drawString(Ship.ShipType.values()[index].name(), x + index*60 + 15, y + index*20);
		}

	}

	public void drawCenteredRegularString(final Screen screen,
										  final String string, final int height, boolean isSelected) {
		backBufferGraphics.setFont(fontRegular);
		// 선택된 경우 초록색, 그렇지 않으면 흰색으로 표시
		backBufferGraphics.setColor(isSelected ? Color.GREEN : Color.WHITE);
		backBufferGraphics.drawString(string, screen.getWidth() / 2
				- fontRegularMetrics.stringWidth(string) / 2, height);
	}

	/**
	 * Draws the game setting row.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param selectedRow
	 *            Selected row.
	 *
	 * @author <a href="mailto:dayeon.dev@gmail.com">Dayeon Oh</a>
	 *
	 */
	public void drawGameSettingRow(final Screen screen, final int selectedRow) {
		int y = 0;
		int height = 0;
		int screenHeight = screen.getHeight();

		if (selectedRow == 1) {
			y = screenHeight / 100 * 50;
			height = screenHeight / 100 * 18;
		} else if (selectedRow == 2) {
			y = screenHeight / 100 * 92;
			height = screenHeight / 100 * 10;
		}

		backBufferGraphics.setColor(Color.DARK_GRAY);
		backBufferGraphics.fillRect(0, y, screen.getWidth(), height);
	}

	/**
	 * Draws the game setting elements.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param selectedRow
	 *            Selected row.
	 * @param difficultyLevel
	 *            Difficulty level.
	 *
	 * @author <a href="mailto:dayeon.dev@gmail.com">Dayeon Oh</a>
	 *
	 */
	public void drawGameSettingElements(final Screen screen, final int selectedRow, final int difficultyLevel) {

		String spaceString = " ";
		String levelEasyString = "Easy";
		String levelNormalString = "Normal";
		String levelHardString = "Hard";
		String startString = "Start";

		final int DIFFICULTY_HEIGHT = screen.getHeight() / 100 * 60;
		final int START_HEIGHT = screen.getHeight() / 100 * 98;

		if (difficultyLevel==0) backBufferGraphics.setColor(Color.GREEN);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, levelEasyString + spaceString.repeat(60), DIFFICULTY_HEIGHT);

		if (difficultyLevel==1) backBufferGraphics.setColor(Color.GREEN);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, levelNormalString, DIFFICULTY_HEIGHT);

		if (difficultyLevel==2) backBufferGraphics.setColor(Color.GREEN);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, spaceString.repeat(60) + levelHardString, DIFFICULTY_HEIGHT);

		if (selectedRow == 2) backBufferGraphics.setColor(Color.GREEN);
		else backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, startString, START_HEIGHT);
	}

	/**
	 *  draw shop
	 * @param screen
	 * 				Screen to draw on.
	 * @param option
	 * 				selected shop item
	 * @param wallet
	 * 				player's wallet
	 * @param money_alertcooldown
	 * 				cooldown for insufficient coin alert
	 * @param max_alertcooldown
	 * 				cooldown for max level alert
	 */
	public void drawShop(final Screen screen, final int option, final Wallet wallet, final Cooldown money_alertcooldown, final Cooldown max_alertcooldown) {

		String shopString = "Shop";
		int shopStringY = Math.round(screen.getHeight() * 0.15f);


		String coinString = ":  " + wallet.getCoin();
		String exitString = "PRESS \"ESC\" TO RETURN TO MAIN MENU";
		String[] costs = new String[] {"2000", "4000", "8000", "MAX LEVEL"};

		String[] itemString = new String[]{"BULLET SPEED", "SHOT INTERVAL", "ADDITIONAL LIFE","COIN GAIN"};
		int[] walletLevel = new int[]{wallet.getBullet_lv(), wallet.getShot_lv(), wallet.getLives_lv(), wallet.getCoin_lv()};

		BufferedImage[] itemImages = new BufferedImage[]{img_bulletspeed,img_shotinterval,img_additionallife,img_coingain};

		int imgstartx = screen.getWidth()/80*23;
		int imgstarty = screen.getHeight()/80*27;
		int imgdis = screen.getHeight()/80*12;
		int coinstartx = screen.getWidth()/80*55;
		int coinstarty = screen.getHeight()/160*66;
		int coindis = screen.getHeight()/80*12;
		int coinSize = 20;
		int cointextstartx = screen.getWidth()/80*60;
		int cointextstarty = screen.getHeight()/160*71;
		int cointextdis = screen.getHeight()/80*12;

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, shopString, shopStringY);
		backBufferGraphics.drawImage(img_coin, screen.getWidth()/80*39-(coinString.length()-3)*screen.getWidth()/80,screen.getHeight()/80*18,coinSize,coinSize,null);
		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.setFont(fontRegular);
		backBufferGraphics.drawString(coinString,screen.getWidth()/80*44-(coinString.length()-3)*screen.getWidth()/80,screen.getHeight()/80*20);

		for(int i = 0;i<4;i++)
		{
			backBufferGraphics.setColor(Color.WHITE);
			drawCenteredRegularString(screen,itemString[i],screen.getHeight()/80*(28 + 12*i));
			for (int j = 0; j < 3; j++)
			{
				if (j + 2 <= walletLevel[i])
				{
					backBufferGraphics.setColor(Color.GREEN);
					backBufferGraphics.fillRect(screen.getWidth() / 40 * (33 / 2) + j * (screen.getWidth() / 10), screen.getHeight() / 80 * (30 + 12*i), 20, 20);
				} else
				{
					backBufferGraphics.setColor(Color.WHITE);
					backBufferGraphics.fillRect(screen.getWidth() / 40 * (33 / 2) + j * (screen.getWidth() / 10), screen.getHeight() / 80 * (30 + 12*i), 20, 20);
				}
			}
		}

		backBufferGraphics.setColor(Color.WHITE);
		backBufferGraphics.drawImage(itemImages[option-1],imgstartx,imgstarty + (imgdis*(option-1)),50,40,null);
		backBufferGraphics.drawImage(img_coin,coinstartx,coinstarty + (coindis*(option-1)),coinSize,coinSize,null);
		backBufferGraphics.drawString("X "+costs[walletLevel[option-1]-1],cointextstartx,cointextstarty + (cointextdis*(option-1)));

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen,exitString,screen.getHeight()/80*80);

		if (!money_alertcooldown.checkFinished())
		{
			backBufferGraphics.setColor(Color.red);
			backBufferGraphics.fillRect((screen.getWidth()-300)/2, (screen.getHeight()-100)/2, 300, 80);
			backBufferGraphics.setColor(Color.black);
			drawCenteredBigString(screen, "Insufficient coin", screen.getHeight()/2);
		}
		if(!max_alertcooldown.checkFinished())
		{
			backBufferGraphics.setColor(Color.red);
			backBufferGraphics.fillRect((screen.getWidth()-300)/2, (screen.getHeight()-100)/2, 300, 80);
			backBufferGraphics.setColor(Color.black);
			drawCenteredBigString(screen, "Already max level", screen.getHeight()/2);

		}
	}

	public void drawBonusBoss(final Screen screen, int level, BonusBoss bonusBoss, final int positionX, final int positionY) {
		switch (level) {
			case 4:
				if (!bonusBoss.isDestroyed()) {
					if (bonusBoss.getHealth() >= 12) {
						backBufferGraphics.drawImage(img_saturn, (screen.getWidth() - img_saturn.getWidth()) / 2, 50, img_saturn.getWidth(), img_saturn.getHeight(), null);
					} else if (bonusBoss.getHealth() >= 8 && bonusBoss.getHealth() <= 11) {
						backBufferGraphics.drawImage(img_saturn2, (screen.getWidth() - img_saturn2.getWidth()) / 2, 50, img_saturn2.getWidth(), img_saturn2.getHeight(), null);
					} else {
						backBufferGraphics.drawImage(img_saturn3, (screen.getWidth() - img_saturn3.getWidth()) / 2, 50, img_saturn3.getWidth(), img_saturn3.getHeight(), null);
					}
				} else {
					backBufferGraphics.drawImage(img_saturn_destroyed, (screen.getWidth() - img_saturn_destroyed.getWidth()) / 2, 50, img_saturn_destroyed.getWidth(), img_saturn_destroyed.getHeight(), null);
				}
				break;
			case 7:
				if (!bonusBoss.isDestroyed()) {
					if (bonusBoss.getHealth() >= 12) {
						backBufferGraphics.drawImage(img_saturn, (screen.getWidth() - img_saturn.getWidth()) / 2, 50, img_saturn.getWidth(), img_saturn.getHeight(), null);
					} else if (bonusBoss.getHealth() >= 8 && bonusBoss.getHealth() <= 11) {
						backBufferGraphics.drawImage(img_saturn2, (screen.getWidth() - img_saturn2.getWidth()) / 2, 50, img_saturn2.getWidth(), img_saturn2.getHeight(), null);
					} else {
						backBufferGraphics.drawImage(img_saturn3, (screen.getWidth() - img_saturn3.getWidth()) / 2, 50, img_saturn3.getWidth(), img_saturn3.getHeight(), null);
					}
				} else {
					backBufferGraphics.drawImage(img_saturn_destroyed, (screen.getWidth() - img_saturn_destroyed.getWidth()) / 2, 50, img_saturn_destroyed.getWidth(), img_saturn_destroyed.getHeight(), null);
				}
				break;
			// 위에꺼 다른 보너스 보스로 바꾸기 나중에 그리고 나면
			default:
				backBufferGraphics.drawImage(img_saturn, (screen.getWidth() - img_saturn.getWidth()) / 2, 50, img_saturn.getWidth(), img_saturn.getHeight(), null);
				break;
		}
	}

	public void drawTimerBar(final Screen screen, int x, int y, int width, int height) {
		backBufferGraphics.setColor(Color.ORANGE);
		backBufferGraphics.fillRect(x, y, width, height);
		backBufferGraphics.drawImage(img_timelimit, (screen.getWidth() - img_timelimit.getWidth() * 2) / 2, 60, img_timelimit.getWidth() * 2, img_timelimit.getHeight() * 2, null);
	}


	/**
	 * Draw puzzle overlay
	 *
	 * @param screen Screen to draw on
	 * @param sequence puzzle sequence
	 * @param playerInput player's input
	 * @param playerNumber player's number
	 */
	public void drawPuzzle(final Screen screen, final Ship ship,
						   final List<Integer> sequence,
						   final List<Integer> playerInput, final int playerNumber) {
		int arrowSize = 50;
		int spacing = 10;
		int startX = screen.getWidth() / 2 - ((arrowSize + spacing) * sequence.size()) / 2;
		int startY = 50;

		for (int i = 0; i < sequence.size(); i++) {
			if (i >= playerInput.size()) {
				BufferedImage arrowImage = getArrowImage(sequence.get(i), playerNumber);
				if (arrowImage != null) {
					backBufferGraphics.drawImage(arrowImage,
							startX + i * (arrowSize + spacing),
							startY,
							arrowSize, arrowSize, null);
				}
			}
		}
	}

	private BufferedImage getArrowImage(int keyCode, int playerNumber) {
		switch (keyCode) {
			case KeyEvent.VK_UP: return img_upArrow;
			case KeyEvent.VK_DOWN: return img_downArrow;
			case KeyEvent.VK_LEFT: return img_leftArrow;
			case KeyEvent.VK_RIGHT: return img_rightArrow;
			default: return null;
		}
	}

	/**
	 * Return key symbol
	 */
	private String getDirectionSymbol(int keyCode, int playerNumber) {
		if (playerNumber == 0) {
			return switch (keyCode) {
				case java.awt.event.KeyEvent.VK_W -> "W";
				case java.awt.event.KeyEvent.VK_A -> "A";
				case java.awt.event.KeyEvent.VK_S -> "S";
				case java.awt.event.KeyEvent.VK_D -> "D";
				default -> "";
			};
		} else {
			return switch (keyCode) {
				case java.awt.event.KeyEvent.VK_UP -> "UP";
				case java.awt.event.KeyEvent.VK_DOWN -> "DOWN";
				case java.awt.event.KeyEvent.VK_LEFT -> "LEFT";
				case java.awt.event.KeyEvent.VK_RIGHT -> "RIGHT";
				default -> "";
			};
		}
	}

	public void drawPause(Screen screen, int set){
		backBufferGraphics.setColor(new Color(0, 0,0));
		backBufferGraphics.fillRect(0, 0, 650, 600);

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredBigString(screen, "PAUSE", screen.getHeight()/3);

		switch(set){
			case 0:
				backBufferGraphics.setColor(Color.GREEN);
				drawCenteredBigString(screen, "CONTINUE", screen.getHeight()/2);
				backBufferGraphics.setColor(Color.WHITE);
				drawCenteredBigString(screen, "STOP", screen.getHeight()/2+100);
				break;
			case 1:
				backBufferGraphics.setColor(Color.WHITE);
				drawCenteredBigString(screen, "CONTINUE", screen.getHeight()/2);
				backBufferGraphics.setColor(Color.GREEN);
				drawCenteredBigString(screen, "STOP", screen.getHeight()/2+100);
				break;
			default:
				break;
		}
	}

	public void drawBoss(final Screen screen, Boss boss) {
		backBufferGraphics.drawImage(boss.getBossShipImage(), (int) boss.getPositionX(), (int) boss.getPositionY(), boss.getBossShipImage().getWidth(), boss.getBossShipImage().getHeight(), null);
	}

	public void drawTopInterfaceBox (final Screen screen) {
		backBufferGraphics.setColor(Color.black);
		backBufferGraphics.fillRect(0, 0, screen.getWidth(), 40);
	}

	public void drawBossHPBar(final Screen screen, int x, int y, int width, int height) {
		backBufferGraphics.setColor(Color.RED);
		backBufferGraphics.fillRect(x, y, width, height);
		backBufferGraphics.drawImage(img_bossHPbar, (screen.getWidth() - img_bossHPbar.getWidth() * 2) / 2, 50, img_bossHPbar.getWidth() * 2, img_bossHPbar.getHeight() * 2, null);
	}

	public void drawBossBullet(BossBullet bossBullet, int positionX, int positionY) {
		if (bossBullet.getAttackType() >= 4 && bossBullet.getAttackType() <= 9) {
			if (bossBullet.getAttackType() == 4 || bossBullet.getAttackType() == 5 || bossBullet.getAttackType() == 8) {
				backBufferGraphics.drawImage(img_RblackHole, 500, bossBullet.getSAVED_Y() - 30, img_RblackHole.getWidth(), img_RblackHole.getHeight(), null);
			} else {
				backBufferGraphics.drawImage(img_LblackHole, 20, bossBullet.getSAVED_Y()- 30, img_LblackHole.getWidth(), img_LblackHole.getHeight(), null);
			}
		}
		backBufferGraphics.drawImage(bossBullet.getBulletImage(), positionX, positionY, bossBullet.getBulletImage().getWidth(), bossBullet.getBulletImage().getHeight(), null);
	}

	public void drawHackedState(final Screen screen) {
		backBufferGraphics.drawImage(img_hacking, (screen.getWidth() - img_bossHPbar.getWidth() * 2) / 2 + 40, 200, img_hacking.getWidth(), img_hacking.getHeight(), null);
	}

	public void drawBossLaser(BossBullet bossBullet, int positionX, int positionY, int height, boolean left) {
		if (height > bossBullet.getBulletImage().getHeight()) height = bossBullet.getBulletImage().getHeight();
		BufferedImage image = bossBullet.getBulletImage().getSubimage(0, 0, bossBullet.getWidth(), height);
		if (left) {
			backBufferGraphics.drawImage(img_laserShooter, positionX - 84, positionY , bossBullet.getBulletImage().getWidth(), bossBullet.getHeight(), null);
			backBufferGraphics.drawImage(image, positionX - 84, positionY,null);
		} else {
			backBufferGraphics.drawImage(img_laserShooter, positionX + bossBullet.getBulletImage().getWidth()+ 38, positionY , bossBullet.getBulletImage().getWidth(), bossBullet.getHeight(), null);
			backBufferGraphics.drawImage(image, positionX + bossBullet.getBulletImage().getWidth()+ 38, positionY,null);
		}
	}

	/**
	 * Draws interactive characters for name input.
	 *
	 * @param screen
	 *            Screen to draw on.
	 * @param name
	 *            Current name selected.
	 * @param nameCharSelected
	 *            Current character selected for modification.
	 */
	public void drawNameInput(final Screen screen, final char[] name,
							  final int nameCharSelected) {
		String newRecordString = "New Record!";
		String introduceNameString = "Introduce name:";

		backBufferGraphics.setColor(Color.GREEN);
		drawCenteredRegularString(screen, newRecordString, screen.getHeight()
				/ 4 + fontRegularMetrics.getHeight() * 11);
		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, introduceNameString,
				screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 13);

		// 3 letters name.
		int positionX = screen.getWidth()
				/ 2
				- (fontRegularMetrics.getWidths()[name[0]]
				+ fontRegularMetrics.getWidths()[name[1]]
				+ fontRegularMetrics.getWidths()[name[2]]
				+ fontRegularMetrics.getWidths()[' ']) / 2;

		for (int i = 0; i < 3; i++) {
			if (i == nameCharSelected)
				backBufferGraphics.setColor(Color.GREEN);
			else
				backBufferGraphics.setColor(Color.WHITE);

			positionX += fontRegularMetrics.getWidths()[name[i]] / 2;
			positionX = i == 0 ? positionX
					: positionX
					+ (fontRegularMetrics.getWidths()[name[i - 1]]
					+ fontRegularMetrics.getWidths()[' ']) / 2;

			backBufferGraphics.drawString(Character.toString(name[i]),
					positionX,
					screen.getHeight() / 4 + fontRegularMetrics.getHeight()
							* 14);
		}
	}
}