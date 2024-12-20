package engine;

import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Ship;
import entity.Barrier;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;
import java.util.TimerTask;
import java.util.Timer;


/**
 * Manages item drop and use.
 *
 * @author Seochan Moon
 * @author noturavrigk
 * @author specture258
 * @author javadocq
 * @author bamcasa
 * @author D0hunLee
 *
 */
public class ItemManager {
    /** Width of game screen. */
    private int WIDTH;
    /** Height of game screen. */
    private int HEIGHT;
    /** Item drop probability, (1 ~ 100). */
    private static final int ITEM_DROP_PROBABILITY = 30;
    /** Cooldown of Ghost */
    private static final int GHOST_COOLDOWN = 3000;
    /** Cooldown of Time-stop */
    private static final int TIMESTOP_COOLDOWN = 4000;
    private static final int  LASER_COOLDOWN = 2000;
    /** Judging that you ate laser items */
    private boolean LasershootActive = false;

    private final List<ItemType> storedItems = new ArrayList<>();


    /** Random generator. */
    private final Random rand;
    /** Player's ship. */
    private final Ship ship;
    /** Formation of enemy ships. */
    private EnemyShipFormation enemyShipFormation;
    /** Set of Barriers in game screen. */
    private final Set<Barrier> barriers;
    /** Application logger. */
    private final Logger logger;
    /** Singleton instance of SoundManager */
    private final SoundManager soundManager = SoundManager.getInstance();
    /** Cooldown variable for Ghost */
    private Cooldown ghost_cooldown = Core.getCooldown(0);
    /** Cooldown variable for Time-stop */
    private Cooldown timeStop_cooldown = Core.getCooldown(0);
    /** Cooldown variable for use item */
    private final Cooldown itemUseCooldown = Core.getCooldown(500);
    /** Cooldown variable for swap item */
    private final Cooldown swapCooldown = Core.getCooldown(500);


    /** Check if the number of shot is max, (maximum 3). */
    private boolean isMaxShotNum;
    /** Number of bullets that player's ship shoot. */
    private int shotNum;
    /** Sound balance for each player*/
    private float balance;


    public boolean addItem(ItemType item) {
        if (storedItems.size() < 2) {
            storedItems.add(item);
            System.out.println("아이템 추가됨: " + storedItems);
            return true;
        }
        System.out.println("아이템 추가 실패: 저장 공간이 가득 찼습니다.");
        return false;
    }

    public ItemType useStoredItem() {
        if (!itemUseCooldown.checkFinished()) {
            return null;
        }

        if (!storedItems.isEmpty()) {
            itemUseCooldown.reset();
            ItemType usedItem = storedItems.remove(0);
            return usedItem;
        }

        System.out.println("저장된 아이템이 없습니다.");
        return null;
    }

    public boolean swapItems() {
        if (!swapCooldown.checkFinished()) {
            System.out.println("아이템 스왑 대기 중입니다.");
            return false;
        }

        if (storedItems.size() == 2) {
            Collections.swap(storedItems, 0, 1);
            swapCooldown.reset();
            System.out.println("아이템이 스왑되었습니다: " + storedItems);
            return true;
        } else {
            System.out.println("스왑할 아이템이 충분하지 않습니다.");
            return false;
        }
    }

    public List<ItemType> getStoredItems() {
        return storedItems;
    }

    /** Types of item */
    public enum ItemType {
        Bomb,
        LineBomb,
        Barrier,
        Ghost,
        TimeStop,
        MultiShot,
        Laser

    }

    /**
     * Constructor, sets the initial conditions.
     *
     * @param ship Player's ship.
     * @param enemyShipFormation Formation of enemy ships.
     * @param barriers Set of barriers in game screen.
     * @param balance 1p -1.0, 2p 1.0, both 0.0
     *
     */
    public ItemManager(Ship ship, EnemyShipFormation enemyShipFormation, Set<Barrier> barriers, int WIDTH, int HEIGHT, float balance) {
        this.shotNum = 1;
        this.rand = new Random();
        this.ship = ship;
        this.enemyShipFormation = enemyShipFormation;
        this.barriers = barriers;
        this.logger = Core.getLogger();
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.balance = balance;


    }

    /**
     * Drop the item.
     *
     * @return Checks if the item was dropped.
     */
    public boolean dropItem() {
        return (rand.nextInt(101)) <= ITEM_DROP_PROBABILITY;
    }

    /**
     * Select item randomly.
     *
     * @return Item type.
     */
    public ItemType selectItemType() {
        ItemType[] itemTypes = ItemType.values();

        if (isMaxShotNum)
            return itemTypes[rand.nextInt(5)];

        return itemTypes[rand.nextInt(7)];
    }




    /**
     * Uses a randomly selected item.
     *
     * @return If the item is offensive, returns the score to add and the number of ships destroyed.
     *         If the item is non-offensive, returns null.
     */
    public Entry<Integer, Integer> useItem(ItemType usedItem) {
        if (usedItem == null) {
            System.out.println("사용할 아이템이 없습니다.");
            return null;
        }

        return switch (usedItem) {
            case Bomb -> operateBomb();
            case LineBomb -> operateLineBomb();
            case Barrier -> operateBarrier();
            case Ghost -> operateGhost();
            case TimeStop -> operateTimeStop();
            case MultiShot -> operateMultiShot();
            case Laser -> operateLaser();
        };
    }




    /**
     * Operate Bomb item.
     *
     * @return The score to add and the number of ships destroyed.
     */
    private Entry<Integer, Integer> operateBomb() {
        if(this.enemyShipFormation != null) {
            this.soundManager.playSound(Sound.ITEM_BOMB, balance);

            int addScore = 0;
            int addShipsDestroyed = 0;

            List<List<EnemyShip>> enemyships = this.enemyShipFormation.getEnemyShips();
            int enemyShipsSize = enemyships.size();

            int maxCnt = -1;
            int maxRow = 0, maxCol = 0;

            for (int i = 0; i <= enemyShipsSize - 3; i++) {

                List<EnemyShip> rowShips = enemyships.get(i);
                int rowSize = rowShips.size();

                for (int j = 0; j <= rowSize - 3; j++) {

                    int currentCnt = 0;

                    for (int x = i; x < i + 3; x++) {

                        List<EnemyShip> subRowShips = enemyships.get(x);

                        for (int y = j; y < j + 3; y++) {
                            EnemyShip ship = subRowShips.get(y);

                            if (ship != null && !ship.isDestroyed())
                                currentCnt++;
                        }
                    }

                    if (currentCnt > maxCnt) {
                        maxCnt = currentCnt;
                        maxRow = i;
                        maxCol = j;
                    }
                }
            }

            List<EnemyShip> targetEnemyShips = new ArrayList<>();
            for (int i = maxRow; i < maxRow + 3; i++) {
                List<EnemyShip> subRowShips = enemyships.get(i);
                for (int j = maxCol; j < maxCol + 3; j++) {
                    EnemyShip ship = subRowShips.get(j);

                    if (ship != null && !ship.isDestroyed())
                        targetEnemyShips.add(ship);
                }
            }

            if (!targetEnemyShips.isEmpty()) {
                for (EnemyShip destroyedShip : targetEnemyShips) {
                    addScore += destroyedShip.getPointValue();
                    addShipsDestroyed++;
                    enemyShipFormation.destroy(destroyedShip, balance);
                }
            }

            return new SimpleEntry<>(addScore, addShipsDestroyed);
        }
        else return new SimpleEntry<>(0, 0);
    }

    /**
     * Operate Line-bomb item.
     *
     * @return The score to add and the number of ships destroyed.
     */
    private Entry<Integer, Integer> operateLineBomb() {
        if (this.enemyShipFormation != null) {
            this.soundManager.playSound(Sound.ITEM_BOMB, balance);

            int addScore = 0;
            int addShipsDestroyed = 0;

            List<List<EnemyShip>> enemyShips = this.enemyShipFormation.getEnemyShips();

            int destroyRow = -1;

            for (List<EnemyShip> column : enemyShips) {
                for (int i = 0; i < column.size(); i++) {
                    if (column.get(i) != null && !column.get(i).isDestroyed())
                        destroyRow = Math.max(destroyRow, i);
                }
            }

            if (destroyRow != -1) {
                for (List<EnemyShip> column : enemyShips) {
                    if (column.get(destroyRow) != null && !column.get(destroyRow).isDestroyed()) {
                        addScore += column.get(destroyRow).getPointValue();
                        addShipsDestroyed++;
                        enemyShipFormation.destroy(column.get(destroyRow), balance);
                    }
                }
            }

            return new SimpleEntry<>(addScore, addShipsDestroyed);
        } else return null;
    }

    /**
     * Operate Barrier item.
     *
     * @return null
     */
    private Entry<Integer, Integer> operateBarrier() {
        this.soundManager.playSound(Sound.ITEM_BARRIER_ON, balance);

        int BarrierY = HEIGHT - 70;
        int middle = WIDTH / 2 - 39;
        int range = 200;
        this.barriers.clear();

        this.barriers.add(new Barrier(middle, BarrierY));
        this.barriers.add(new Barrier(middle - range, BarrierY));
        this.barriers.add(new Barrier(middle + range, BarrierY));
        logger.info("Barrier created at positions: (" + middle + ", " + (BarrierY) + "), ("
                + (middle - range) + ", " + (BarrierY) + "), ("
                + (middle + range) + ", " + (BarrierY) + ")");
        return null;
    }

    /**
     * Operate Ghost item.
     *
     * @return null
     */
    private Entry<Integer, Integer> operateGhost() {
        this.soundManager.playSound(Sound.ITEM_GHOST, balance);

        this.ship.setColor(Color.DARK_GRAY);
        this.ghost_cooldown = Core.getCooldown(GHOST_COOLDOWN);
        this.ghost_cooldown.reset();

        return null;
    }

    /**
     * Operate Time-stop item.
     *
     * @return null
     */
    private Entry<Integer, Integer> operateTimeStop() {
        this.soundManager.playSound(Sound.ITEM_TIMESTOP_ON, balance);

        this.timeStop_cooldown = Core.getCooldown(TIMESTOP_COOLDOWN);
        this.timeStop_cooldown.reset();

        return null;
    }

    /**
     * Operate Multi-shot item.
     *
     * @return null
     */
    private Entry<Integer, Integer> operateMultiShot() {
        if (this.shotNum < 3) {
            this.shotNum++;
            if (this.shotNum == 3) {
                this.isMaxShotNum = true;
            }
        }

        return null;
    }

    /**
     * operate Laser_shoot item.
     */
    private Entry<Integer, Integer> operateLaser() {
        this.soundManager.playSound(Sound.ITEM_shooting_laser, 3.0f);
        if (!LasershootActive) {
            LasershootActive = true;
            this.ship.setLaserMode(true);
            this.ship.setShootingCooldown(Core.getCooldown(0));

            // set timer activating laser two second
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LasershootActive = false;
                    ship.setLaserMode(false);
                    ship.setShootingCooldown(Core.getCooldown(ship.getShootingInterval()));
                }
            }, LASER_COOLDOWN);
        }
        return new SimpleEntry<>(0, 0);
    }




    /**
     * Checks if Ghost is active.
     *
     * @return True when Ghost is active.
     */
    public boolean isGhostActive() {
        return !this.ghost_cooldown.checkFinished();
    }


    /**
     * Checks if Time-stop is active.
     *
     * @return True when Time-stop is active.
     */
    public boolean isTimeStopActive() {
        return !this.timeStop_cooldown.checkFinished();
    }

    /**
     * Returns the number of bullets that player's ship shoot.
     * @return Number of bullets that player's ship shoot.
     */
    public int getShotNum() {
        return this.shotNum;
    }

    public void setEnemyShipFormation(EnemyShipFormation e) {
        this.enemyShipFormation = e;
    }

}