package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Enemy projectiles (travel left toward the player, in Scene1's separate list):
//  - BULLET: fast, 20% dmg. Plays the documented muzzle/start frame, jumps
//            forward from the muzzle, then travels in the enemy's facing
//            direction. On contact it stops and plays the hit/fade sequence.
//  - BOMB:   slow, 30% dmg. enemy_projectile.png frames (full size) played in
//            order over its trip across the board. The sheet frames contain
//            their own fade; animation completion does not end the projectile.
public class EnemyBullet extends Sprite {

    private static final int BOMB_ANIMATION_END_X = -40;
    private static final int BULLET_START_TICKS = 4;
    private static final int BULLET_START_OFFSET = 60;
    private static final int BULLET_IMPACT_FRAME_TICKS = 3;
    private static final int BULLET_TRAVEL_WIDTH = 24;
    private static final int BULLET_TRAVEL_HEIGHT = 5;
    private static final int[][] BULLET_RECTS = {
        {311, 508, 48, 83},
        {580, 536, 129, 27},
        {820, 492, 120, 115},
        {1084, 513, 63, 73},
        {1324, 520, 56, 59}
    };

    private enum BulletPhase {
        START,
        TRAVEL,
        IMPACT
    }

    private static Image[] bombFrames;
    private static BufferedImage[] bulletFrames;
    private static final Map<String, Image> ROT = new HashMap<>();

    private final double velocityX;
    private final double velocityY;
    private final boolean bomb;
    private final int rotDeg;
    private final double startX;
    private double preciseX;
    private double preciseY;
    private int frameIndex;
    private BulletPhase bulletPhase;
    private int bulletPhaseTick;
    private int bulletFrameIndex;
    private double impactCenterX;
    private double impactCenterY;

    public EnemyBullet(int x, int y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, false);
    }

    public EnemyBullet(int x, int y, double velocityX, double velocityY, boolean bomb) {
        this.preciseX = x;
        this.preciseY = y;
        this.startX = x;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.bomb = bomb;
        int deg = (int) Math.round(Math.toDegrees(Math.atan2(velocityY, velocityX)) - 180);
        this.rotDeg = ((deg % 360) + 360) % 360;
        if (bomb) {
            setImage(bombFrames()[0]);
        } else {
            bulletPhase = BulletPhase.START;
            setBulletFrame(0);
        }
    }

    private static Image[] bombFrames() {
        if (bombFrames == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_PROJECTILE);
            int[][] r = {
                {18, 10, 67, 64}, {124, 12, 65, 62}, {230, 12, 61, 62},
                {23, 119, 59, 55}, {129, 119, 54, 54}, {237, 121, 47, 49},
                {30, 228, 41, 44}, {136, 230, 38, 38}, {243, 233, 35, 34}
            };
            bombFrames = new Image[r.length];
            for (int i = 0; i < r.length; i++) {
                bombFrames[i] = clip(sheet, r[i][0], r[i][1], r[i][2], r[i][3], 1, false); // full size
            }
        }
        return bombFrames;
    }

    private static BufferedImage[] bulletFrames() {
        if (bulletFrames == null) {
            BufferedImage sheet = loadSheet(IMG_ENEMY_EFFECT);
            bulletFrames = new BufferedImage[BULLET_RECTS.length];
            double scaleX = (double) BULLET_TRAVEL_WIDTH / BULLET_RECTS[1][2];
            double scaleY = (double) BULLET_TRAVEL_HEIGHT / BULLET_RECTS[1][3];
            for (int i = 0; i < BULLET_RECTS.length; i++) {
                int[] r = BULLET_RECTS[i];
                int width = Math.max(1, (int) Math.round(r[2] * scaleX));
                int height = Math.max(1, (int) Math.round(r[3] * scaleY));
                bulletFrames[i] = scaleImage(
                        sheet.getSubimage(r[0], r[1], r[2], r[3]), width, height);
            }
        }
        return bulletFrames;
    }

    private Image rotatedBulletFrame(int index) {
        BufferedImage base = bulletFrames()[index];
        if (rotDeg == 0) {
            return base;
        }
        return ROT.computeIfAbsent(index + ":" + rotDeg, k -> rotate(base, rotDeg));
    }

    private void setBulletFrame(int index) {
        bulletFrameIndex = index;
        setImage(rotatedBulletFrame(index));
    }

    private void placeImpactFrame() {
        x = (int) Math.round(impactCenterX - getImage().getWidth(null) / 2.0);
        y = (int) Math.round(impactCenterY - getImage().getHeight(null) / 2.0);
    }

    public boolean isPlasma() {
        return bomb;
    }

    public int getDamage() {
        return bomb ? DMG_PLASMA : DMG_BULLET;
    }

    public float getAlpha() {
        return 1f;
    }

    public boolean canDamagePlayer() {
        return bomb || bulletPhase == BulletPhase.TRAVEL;
    }

    public boolean beginImpact() {
        if (bomb || bulletPhase != BulletPhase.TRAVEL) {
            return false;
        }
        impactCenterX = x + getImage().getWidth(null) / 2.0;
        impactCenterY = y + getImage().getHeight(null) / 2.0;
        bulletPhase = BulletPhase.IMPACT;
        bulletPhaseTick = 0;
        setBulletFrame(2);
        placeImpactFrame();
        return true;
    }

    public boolean isImpacting() {
        return !bomb && bulletPhase == BulletPhase.IMPACT;
    }

    @Override
    public void act() {
        if (bomb) {
            actBomb();
        } else {
            actBullet();
        }
    }

    private void actBomb() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
        double travelDistance = Math.max(1.0, startX - BOMB_ANIMATION_END_X);
        double progress = Math.max(0.0,
                Math.min(1.0, (startX - preciseX) / travelDistance));
        frameIndex = Math.min(bombFrames().length - 1,
                (int) (progress * bombFrames().length));
        setImage(bombFrames()[frameIndex]);
        if (x < -80 || y < -80 || y > BOARD_HEIGHT + 80) {
            die();
        }
    }

    private void actBullet() {
        if (bulletPhase == BulletPhase.START) {
            bulletPhaseTick++;
            if (bulletPhaseTick >= BULLET_START_TICKS) {
                double speed = Math.hypot(velocityX, velocityY);
                double directionX = speed == 0 ? -1 : velocityX / speed;
                double directionY = speed == 0 ? 0 : velocityY / speed;
                double centerX = preciseX + getImage().getWidth(null) / 2.0
                        + directionX * BULLET_START_OFFSET;
                double centerY = preciseY + getImage().getHeight(null) / 2.0
                        + directionY * BULLET_START_OFFSET;
                bulletPhase = BulletPhase.TRAVEL;
                bulletPhaseTick = 0;
                setBulletFrame(1);
                preciseX = centerX - getImage().getWidth(null) / 2.0;
                preciseY = centerY - getImage().getHeight(null) / 2.0;
                x = (int) Math.round(preciseX);
                y = (int) Math.round(preciseY);
            }
        } else if (bulletPhase == BulletPhase.TRAVEL) {
            preciseX += velocityX;
            preciseY += velocityY;
            x = (int) Math.round(preciseX);
            y = (int) Math.round(preciseY);
            setBulletFrame(1);
        } else {
            bulletPhaseTick++;
            int impactFrame = bulletPhaseTick / BULLET_IMPACT_FRAME_TICKS;
            if (impactFrame >= 3) {
                die();
                return;
            }
            setBulletFrame(2 + impactFrame);
            placeImpactFrame();
        }

        if (!isImpacting()
                && (x < -80 || x > BOARD_WIDTH + 80
                || y < -80 || y > BOARD_HEIGHT + 80)) {
            die();
        }
    }

    int getFrameIndex() {
        return frameIndex;
    }

    int getBulletFrameIndex() {
        return bulletFrameIndex;
    }

    double getBulletCenterX() {
        return x + getImage().getWidth(null) / 2.0;
    }

    double getBulletCenterY() {
        return y + getImage().getHeight(null) / 2.0;
    }
}
