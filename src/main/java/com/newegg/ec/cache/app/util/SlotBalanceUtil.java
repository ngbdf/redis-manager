package com.newegg.ec.cache.app.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jn50 on 2017/9/6.
 */
public class SlotBalanceUtil {
    public static final Log logger = LogFactory.getLog(SlotBalanceUtil.class);

    private static int totalSlot = 16384;

    public static List<Shade> balanceSlot(int totalShade) {
        List<Shade> shades = new LinkedList();
        int almostCount = totalSlot / totalShade;
        int leftCount = totalSlot % totalShade;
        for (int i = 0; i < totalShade; i++) {
            if (i < leftCount) {
                Shade shade = new Shade();
                shade.setStartSlot(i * (almostCount + 1));
                shade.setEndSlot((i + 1) * (almostCount + 1) - 1);
                shade.setSlotCount(almostCount + 1);
                shades.add(shade);
            } else {
                Shade shade = new Shade();
                shade.setStartSlot(i * almostCount + leftCount);
                shade.setEndSlot((i + 1) * almostCount + leftCount - 1);
                shade.setSlotCount(almostCount);
                shades.add(shade);
            }
        }
        return shades;
    }

    public static class Shade {

        private int startSlot;

        private int endSlot;

        private int slotCount;

        public int getStartSlot() {
            return startSlot;
        }

        public void setStartSlot(int startSlot) {
            this.startSlot = startSlot;
        }

        public int getEndSlot() {
            return endSlot;
        }

        public void setEndSlot(int endSlot) {
            this.endSlot = endSlot;
        }

        public int getSlotCount() {
            return slotCount;
        }

        public void setSlotCount(int slotCount) {
            this.slotCount = slotCount;
        }

        public String toString() {
            return startSlot + "-" + endSlot + ":" + slotCount;
        }
    }
}
