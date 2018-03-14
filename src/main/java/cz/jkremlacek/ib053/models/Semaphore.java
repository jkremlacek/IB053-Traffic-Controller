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

    private SemaphoreType type;
    private boolean interstep = false;
    private SemaphoreColor color = SemaphoreColor.RED;

    public Semaphore(SemaphoreType type) {
        this.type = type;
    }

    public void changeColor(SemaphoreColor requestColor) {
        if (requestColor != color) {
            switch (color) {
                case RED:
                    if (type == SemaphoreType.PEDESTRIAN) {
                        if (interstep) {
                            this.color = SemaphoreColor.GREEN;
                        }
                        interstep = !interstep;
                    } else {
                        this.color = SemaphoreColor.YELLOW;
                    }
                    break;
                case YELLOW:
                    this.color = SemaphoreColor.GREEN;
                    break;
                case RED_AND_YELLOW:
                    this.color = SemaphoreColor.RED;
                    break;
                case GREEN:
                    if (type == SemaphoreType.PEDESTRIAN) {
                        if (interstep) {
                            this.color = SemaphoreColor.RED;
                        }
                        interstep = !interstep;
                    } else {
                        this.color = SemaphoreColor.RED_AND_YELLOW;
                    }
                    break;
            }
        }
    }

    public SemaphoreColor getColor() {
        return color;
    }

    public String toString() {
        return "semaphoreColor: " + color.toString();
    }
}
