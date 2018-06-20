package cz.jkremlacek.ib053.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author Jakub Kremláček
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Semaphore {
    public enum SemaphoreColor {
        RED, YELLOW, RED_AND_YELLOW, GREEN
    }

    public enum SemaphoreType {
        TRAFFIC, PEDESTRIAN
    }

    private final SemaphoreType TYPE;

    private boolean interstep = false;
    private SemaphoreColor color = SemaphoreColor.RED;

    public Semaphore(SemaphoreType type) {
        this.TYPE = type;
    }

    public void changeColor(SemaphoreColor requestColor) {
        if (requestColor != color) {
            switch (color) {
                case RED:
                    if (TYPE == SemaphoreType.PEDESTRIAN) {
                        if (interstep) {
                            this.color = SemaphoreColor.GREEN;
                        }
                        interstep = !interstep;
                    } else {
                        this.color = SemaphoreColor.RED_AND_YELLOW;
                    }
                    break;
                case YELLOW:
                    this.color = SemaphoreColor.RED;
                    break;
                case RED_AND_YELLOW:
                    this.color = SemaphoreColor.GREEN;
                    break;
                case GREEN:
                    if (TYPE == SemaphoreType.PEDESTRIAN) {
                        if (interstep) {
                            this.color = SemaphoreColor.RED;
                        }
                        interstep = !interstep;
                    } else {
                        this.color = SemaphoreColor.YELLOW;
                    }
                    break;
            }
        }
    }

    public String toString() {
        return "semaphoreColor: " + color.toString();
    }
}
