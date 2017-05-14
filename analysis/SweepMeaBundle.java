package com.platformda.iv.analysis;

/**
 */
public class SweepMeaBundle extends MeaBundle implements Sweep {

    public static final int TYPE_LINEAR = 0;
    public static final int TYPE_LOG10 = 1;
    public static final String[] TYPE_STRINGS = {"linear", "log10"};
    //
    double start, stop, step;
    int point;
    int type = TYPE_LINEAR;

    public SweepMeaBundle() {
    }

    public SweepMeaBundle(double start, double stop, double step, int point) {
        this.start = start;
        this.stop = stop;
        this.step = step;
        this.point = point;
    }

    @Override
    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    @Override
    public double getStop() {
        return stop;
    }

    public void setStop(double stop) {
        this.stop = stop;
    }

    @Override
    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public MeaBundle reverse() {
        SweepMeaBundle sweep = new SweepMeaBundle();
        sweep.setPoint(point);
        sweep.setStart(stop * -1);
        sweep.setStop(start * -1);
        sweep.setStep(step);
        return sweep;
    }

    @Override
    public MeaBundle[] split(int pointNumber) {
        // don't split when log
        if (SweepMeaBundle.TYPE_LOG10 == type) {
            return new MeaBundle[]{this};
        }

        int tempPoint = getPoint();
        if (tempPoint <= pointNumber) {
            SweepMeaBundle tempBundle = new SweepMeaBundle(start, stop, step, tempPoint);
            tempBundle.setType(type);
            return new MeaBundle[]{tempBundle};
        }

        MeaBundle[] sweeps = new MeaBundle[(int) Math.ceil(tempPoint / (double) pointNumber)];
        if (pointNumber == 1) {
            for (int i = 0; i < sweeps.length; i++) {
                sweeps[i] = new ConstMeaBundle(start + step * i);
            }
        } else {
            int index = 0;
            // the number of points that have been handled
            int n = 0;
            while (n < tempPoint) {
                int len = n + pointNumber > tempPoint ? tempPoint - n : pointNumber;
                double tstart = start + step * n;
                double tstop = start + step * (n + len - 1);
                SweepMeaBundle tsweep = new SweepMeaBundle();
                tsweep.setPoint(len);
                tsweep.setStart(tstart);
                tsweep.setStop(tstop);
                tsweep.setStep(step);
                tsweep.setType(type);
                sweeps[index++] = tsweep;
                n += len;
            }
        }

        return sweeps;
    }

    @Override
    public double get(int index) {
        if (type == SweepMeaBundle.TYPE_LOG10) {
            if (point == 1) {
                return start;
            }

            double tempStart = start;
            double tempStop = stop;
            if (start < 0 && stop < 0) {
                tempStart = -tempStart;
                tempStop = -tempStop;
            }
            double step = (Math.log10(tempStop) - Math.log10(tempStart)) / (point - 1);
            double retValue = Math.pow(10, Math.log10(tempStart) + index * step);

            if (start < 0 && stop < 0) {
                retValue = -retValue;
            }

            return retValue;
        }

        return start + step * index;
    }

    @Override
    public int size() {
        return getPoint();
    }

    @Override
    public void shift(double shift) {
        start += shift;
        stop += shift;
    }

    @Override
    public void scale(double factor) {
        start *= factor;
        stop *= factor;
        step *= factor;
    }
}
