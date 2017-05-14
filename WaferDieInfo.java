package com.platformda.iv.admin;

import com.platformda.iv.admin.VarProviderEditingTableModel.IconableImpl;
import com.platformda.utility.Iconable;
import java.awt.Color;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * see MeaDie.java
 * 
 * holds the information(x/y coordinate, index) of a die in a wafer map file
 *
 */
public class WaferDieInfo implements Iconable, Comparable<WaferDieInfo> {

    public static final Image image = ImageUtilities.loadImage("com/platformda/mea/resources/die.png");
    public static final Image partBadge = ImageUtilities.loadImage("com/platformda/mea/resources/partMeaed.png");
    public static final Image fullBadge = ImageUtilities.loadImage("com/platformda/mea/resources/fullMeaed.png");
    public static final Image waitingBadge = ImageUtilities.loadImage("com/platformda/mea/resources/waiting.png");
    //
    public static final Image partImage = ImageUtilities.mergeImages(image, partBadge, 8, 8);
    public static final Image fullImage = ImageUtilities.mergeImages(image, fullBadge, 8, 8);
    public static final Image waitingImage = ImageUtilities.mergeImages(image, waitingBadge, 8, 8);
    //
    public static final Icon partIcon = new ImageIcon(partImage);
    public static final Icon fullIcon = new ImageIcon(fullImage);
    public static final Icon icon = new ImageIcon(image);
    public static final Icon waitingIcon = new ImageIcon(waitingImage);
    //
    public static final int STATUS_NUMBER = 3;
    public static final int STATUS_NOT_MEAED = 0;
    public static final int STATUS_PART_MEAED = 1;
    public static final int STATUS_MEAED = 2;
    public static Color[] COLORS = {null, new Color(243, 152, 117), new Color(127, 177, 227)};
    public static String[] STATUS_STRINGS = {"Not", "Part", "Full"};
    public static final Iconable[] ICONABLES = new Iconable[]{
        new IconableImpl(STATUS_STRINGS[0], icon),
        new IconableImpl(STATUS_STRINGS[1], partIcon),
        new IconableImpl(STATUS_STRINGS[2], fullIcon)
    };
    int status = STATUS_NOT_MEAED;
    int x; // column
    int y; // row
    int dieIndex; // to avoid confusing with index of list

//    public WaferDieInfo(int index) {
//        this.index = index;
////        status = index % 3;
//    }
    //
    public WaferDieInfo(int x, int y, int index) {
//        this(index);
        this.dieIndex = index;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

//    public void setX(int x) {
//        this.x = x;
//    }
    public int getY() {
        return y;
    }

//    public void setY(int y) {
//        this.y = y;
//    }
    public int getDieIndex() {
        return dieIndex;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.x;
        hash = 53 * hash + this.y;
        hash = 53 * hash + this.dieIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WaferDieInfo other = (WaferDieInfo) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.dieIndex != other.dieIndex) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "index=" + dieIndex + ",x=" + x + ",y=" + y;
    }

    @Override
    public Icon getIcon() {
        if (status == STATUS_NOT_MEAED) {
            return icon;
        } else if (status == STATUS_PART_MEAED) {
            return partIcon;
        } else {
            return fullIcon;
        }
    }

    @Override
    public String getName() {
//        return String.valueOf(index);
        return STATUS_STRINGS[status];
    }

    @Override
    public int compareTo(WaferDieInfo o) {
        return status - o.getStatus();
    }

    public static Icon getIcon(int status) {
        if (status == STATUS_NOT_MEAED) {
            return icon;
        } else if (status == STATUS_PART_MEAED) {
            return partIcon;
        } else {
            return fullIcon;
        }
    }

    public static Iconable getIconable(int status) {
        return ICONABLES[status];
    }
}
