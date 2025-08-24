package org.fish.uitoolkit.utils;

/**
 * 简单的可变二维向量类型，包装 x/y 双精度坐标。
 */
public class Vector2d {
    public double x;
    public double y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d() {
        this(0, 0);
    }

    public Vector2d copy() {
        return new Vector2d(this.x, this.y);
    }

    /**
     * 将另一个向量加到当前向量并返回 this（便于链式调用）。
     */
    public Vector2d add(Vector2d other) {
        if (other == null) return this;
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    /** 用另一个向量替换当前向量的值（不更换对象引用） */
    public void set(Vector2d other) {
        if (other == null) return;
        this.x = other.x;
        this.y = other.y;
    }

    @Override
    public String toString() {
        return "Vector2d(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2d)) return false;
        Vector2d v = (Vector2d) o;
        return Double.compare(v.x, x) == 0 && Double.compare(v.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits = 31 * bits + Double.doubleToLongBits(y);
        return (int) (bits ^ (bits >>> 32));
    }
}
