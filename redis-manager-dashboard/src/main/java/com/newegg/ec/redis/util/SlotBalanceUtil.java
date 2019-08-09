package com.newegg.ec.redis.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jn50
 * @date 2017/9/6
 */
public class SlotBalanceUtil {

    private static int TOTAL_SLOT = 16384;

    public static List<Shade> balanceSlot(int totalShade) {
        List<Shade> shadeList = new LinkedList();
        int almostCount = TOTAL_SLOT / totalShade;
        int leftCount = TOTAL_SLOT % totalShade;
        for (int i = 0; i < totalShade; i++) {
            if (i < leftCount) {
                Shade shade = new Shade();
                shade.setStartSlot(i * (almostCount + 1));
                shade.setEndSlot((i + 1) * (almostCount + 1) - 1);
                shade.setSlotCount(almostCount + 1);
                shadeList.add(shade);
            } else {
                Shade shade = new Shade();
                shade.setStartSlot(i * almostCount + leftCount);
                shade.setEndSlot((i + 1) * almostCount + leftCount - 1);
                shade.setSlotCount(almostCount);
                shadeList.add(shade);
            }
        }
        return shadeList;
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

        @Override
        public String toString() {
            return startSlot + "-" + endSlot + ":" + slotCount;
        }
    }
}
