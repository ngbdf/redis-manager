package com.newegg.ec.cache.app.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by gl49 on 2017/9/26.
 */
public class SlotRebanlance {

    private static final Log logger = LogFactory.getLog(SlotRebanlance.class);

    private static int totalSlot = 16384;

    private static int initCount = 5;

    private static int targetCount = 5;

    public static void main(String[] args) {
        if (initCount <= targetCount)
            expand();
        else
            reduced();
    }

    public static void expand() {
        List<Shade> initShades = getShades("init", initCount);
        List<Shade> targetShades = getShades("", targetCount);
        logger.info("target:" + targetShades);

        int missSlot = 0;
        boolean[] oldShades = new boolean[initCount];
        boolean[] newShades = new boolean[targetCount];
        int assignCount = 0;
        while (assignCount < initCount) {
            for (int j = 0; j < initCount; j++) {
                if (oldShades[j] == false) {
                    for (int i = 0; i < targetCount; i++) {
                        if (newShades[j] == false) {
                            if (targetShades.get(i).getStartSlot() <= initShades.get(j).getEndSlot() && targetShades.get(i).getStartSlot() >= initShades.get(j).getStartSlot()) {
                                if (initShades.get(j).getEndSlot() - targetShades.get(i).getEndSlot() + missSlot >= 0) {
                                    oldShades[j] = true;
                                    newShades[i] = true;
                                    assignCount++;
                                    targetShades.get(i).setName(initShades.get(j).getName());
                                    i = targetCount;
                                }
                            } else if (targetShades.get(i).getEndSlot() <= initShades.get(j).getEndSlot() && targetShades.get(i).getEndSlot() >= initShades.get(j).getStartSlot()) {
                                if (targetShades.get(i).getStartSlot() - initShades.get(j).getStartSlot() + missSlot >= 0) {
                                    oldShades[j] = true;
                                    newShades[i] = true;
                                    assignCount++;
                                    targetShades.get(i).setName(initShades.get(j).getName());
                                    i = targetCount;
                                }
                            }
                        }
                    }
                }
            }
            missSlot++;
        }
        logger.info("assign old shades:" + targetShades);
        for (int jj = 0; jj < targetShades.size(); jj++) {
            if (targetShades.get(jj).getName() == null) {
                targetShades.get(jj).setName("new");
            }
        }
        logger.info("result:" + targetShades);
    }

    public static void reduced() {
        List<Shade> initShades = getShades("init", initCount);
        logger.info("init:" + initShades);
        List<Shade> targetShades = getShades("", targetCount);
        logger.info("target:" + targetShades);
        for (int i = 0; i < initCount - targetCount; i++) {
            int x = new Random().nextInt(initShades.size());
            initShades.remove(x);
        }
        logger.info("left Shade" + initShades);
        int missSlot = 0;
        boolean[] oldShades = new boolean[targetCount];
        boolean[] newShades = new boolean[targetCount];
        int assignCount = 0;
        while (assignCount < targetCount) {
            for (int j = 0; j < targetCount; j++) {
                if (oldShades[j] == false) {
                    for (int i = 0; i < targetCount; i++) {
                        if (newShades[j] == false) {
                            if (targetShades.get(i).getStartSlot() <= initShades.get(j).getEndSlot() && targetShades.get(i).getStartSlot() >= initShades.get(j).getStartSlot()) {
                                if (initShades.get(j).getEndSlot() - targetShades.get(i).getEndSlot() + missSlot >= 0) {
                                    oldShades[j] = true;
                                    newShades[i] = true;
                                    assignCount++;
                                    targetShades.get(i).setName(initShades.get(j).getName());
                                    i = targetCount;
                                }
                            } else if (targetShades.get(i).getEndSlot() <= initShades.get(j).getEndSlot() && targetShades.get(i).getEndSlot() >= initShades.get(j).getStartSlot()) {
                                if (targetShades.get(i).getStartSlot() - initShades.get(j).getStartSlot() + missSlot >= 0) {
                                    oldShades[j] = true;
                                    newShades[i] = true;
                                    assignCount++;
                                    targetShades.get(i).setName(initShades.get(j).getName());
                                    i = targetCount;
                                }
                            }
                        }
                    }
                }
            }
            missSlot++;
        }
        for (int jj = 0; jj < targetShades.size(); jj++) {
            if (targetShades.get(jj).getName() == null) {
                for (int zz = 0; zz < oldShades.length; zz++) {
                    if (oldShades[zz] == false) {
                        oldShades[zz] = true;
                        targetShades.get(jj).setName(initShades.get(zz).getName());
                    }
                    break;
                }
            }
        }
        logger.info("result" + targetShades);
    }

    public static List<Shade> getShades(String tag, int shadeCount) {
        List<Shade> shades = new LinkedList();
        int almostCount = totalSlot / shadeCount;
        int leftCount = totalSlot % shadeCount;
        for (int i = 0; i < shadeCount; i++) {
            if (i < leftCount) {
                Shade shade = new Shade();
                if (tag != null && !"".equals(tag))
                    shade.setName(tag + i);
                shade.setStartSlot(1 + i * (almostCount + 1));
                shade.setEndSlot(1 + (i + 1) * (almostCount + 1) - 1);
                shade.setSlotCount(almostCount + 1);
                shades.add(shade);
            } else {
                Shade shade = new Shade();
                if (tag != null && !"".equals(tag))
                    shade.setName(tag + i);
                shade.setStartSlot(1 + i * almostCount + leftCount);
                shade.setEndSlot(1 + (i + 1) * almostCount + leftCount - 1);
                shade.setSlotCount(almostCount);
                shades.add(shade);
            }
        }
        return shades;
    }

    static class Shade {

        private String name;
        private int startSlot;
        private int endSlot;
        private int slotCount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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
            return name + ";" + startSlot + "-" + endSlot + ":" + slotCount;
        }
    }
}
