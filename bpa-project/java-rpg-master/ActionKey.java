
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionKey implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(MainPanel.class.getName());

    // constants for key mode
    // isPressed() method returns true while key is pressed
    public static final int NORMAL = 0;

    // isPressed() method returns true
    // only when a key is pressed for the first time
    public static final int DETECT_INITIAL_PRESS_ONLY = 1;

    // isPressed() method returns true while key is pressed, but loops it at a slower rate
    public static final int SLOWER_INPUT = 2;

    // When the hero has died, no input will be taken
    public static final int DEAD_INPUT = 3;

    // constants key state
    private static final int STATE_RELEASED = 0;
    private static final int STATE_PRESSED = 1;
    private static final int STATE_WAITING_FOR_RELEASE = 2;

    // current key's mode
    private int mode;
    // the number of the time that the key was pressed
    private int amount;
    // current key's state
    private int state;
    // first press
    private boolean isFirst;
    // ready to move
    private boolean isReady;

    private static Thread threadSlower;

    public ActionKey() {
        this(NORMAL);
    }

    public ActionKey(int mode) {
        this.mode = mode;
        isFirst = true;
        reset();
    }

    // reset key's state
    private void reset() {
        state = STATE_RELEASED;
        amount = 0;

    }

    // The button has been pressed
    public void press() {
        if (mode == DEAD_INPUT) {
            return;
        }

        if (state != STATE_WAITING_FOR_RELEASE) {
            amount++;
            state = STATE_PRESSED;
        }
        if (mode == SLOWER_INPUT && isFirst) {
            // run thread
            threadSlower = new Thread(new ActionKey.SlowerInputThread());
            threadSlower.start();
            isReady = true;
            state = STATE_WAITING_FOR_RELEASE;
            amount = 0;
        }

        isFirst = false;
    }

    // The button has been released
    public void release() {
        if (mode == DEAD_INPUT) {
            return;
        }

        state = STATE_RELEASED;
        if (mode == SLOWER_INPUT) {

            try {
                threadSlower.stop();
            } catch (Exception e) {
                System.err.println("Key removed.");
            }
        }

        isReady = false;
        isFirst = true;
    }

    // Check if the button is currently pressed
    public boolean isPressed() {

        if (amount != 0) {
            if (state == STATE_RELEASED) {
                amount = 0;
            } else if (mode == DETECT_INITIAL_PRESS_ONLY) {
                state = STATE_WAITING_FOR_RELEASE;
                amount = 0;
            }
            return true;
        } else if (isReady) {
            isReady = false;
            amount = 0;
            return true;
        }
        return false;
    }

    private class SlowerInputThread extends Thread {

        public void run() {
            while (true) {

                try {
                    SlowerInputThread.sleep(400);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);

                    try {
                        CrashReport cr = new CrashReport(e);
                        cr.show();
                    } catch (Exception n) {
                        // do nothing
                    }
                }

                isReady = true;
            }
        }
    }
}
