package com.prisonbreakmod.util;

import net.minecraft.util.math.Vec3d;

/** Vector math utilities for guard AI and projectile calculations. */
public class VectorMath {

    /** Build a direction vector from yaw angle (degrees). */
    public static Vec3d fromYaw(float yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        return new Vec3d(-Math.sin(rad), 0, Math.cos(rad)).normalize();
    }

    /** Build full direction vector from yaw + pitch. */
    public static Vec3d fromYawPitch(float yawDeg, float pitchDeg) {
        double yawRad = Math.toRadians(yawDeg);
        double pitchRad = Math.toRadians(pitchDeg);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3d(x, y, z);
    }

    /** Angle in degrees between two vectors. */
    public static double angleBetween(Vec3d a, Vec3d b) {
        double dot = a.normalize().dotProduct(b.normalize());
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.toDegrees(Math.acos(dot));
    }

    /** Returns true if point is within a cone from origin in direction dir, half-angle degrees. */
    public static boolean isInCone(Vec3d origin, Vec3d direction, double halfAngleDeg,
                                    double maxRange, Vec3d point) {
        Vec3d toPoint = point.subtract(origin);
        double dist = toPoint.lengthVector();
        if (dist > maxRange) return false;
        return angleBetween(direction, toPoint) <= halfAngleDeg;
    }
}
