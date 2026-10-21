package com.agent1k.region.view;

import com.agent1k.region.data.RegionData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RegionOutline {
    private RegionOutline() { }

    public record Point(double x, double y, double z) { }

    public static List<Point> points(RegionData region, int worldMin, int worldMax, double spacing, int maxPoints) {
        double minX = (double) region.getX() - region.getRadius();
        double minY = Math.max(worldMin, (double) region.getY() - region.getRadius());
        double minZ = (double) region.getZ() - region.getRadius();
        double maxX = (double) region.getX() + region.getRadius() + 1;
        double maxY = Math.min(worldMax, (double) region.getY() + region.getRadius() + 1);
        double maxZ = (double) region.getZ() + region.getRadius() + 1;
        int perEdge = Math.max(1, (maxPoints + 4) / 12);
        int stepsX = steps(maxX - minX, spacing, perEdge);
        int stepsY = steps(maxY - minY, spacing, perEdge);
        int stepsZ = steps(maxZ - minZ, spacing, perEdge);
        Set<Point> points = new LinkedHashSet<>();
        for (double y : new double[]{minY, maxY}) for (double z : new double[]{minZ, maxZ}) {
            for (int i = 0; i <= stepsX; i++) points.add(new Point(at(minX, maxX, i, stepsX), y, z));
        }
        for (double x : new double[]{minX, maxX}) for (double z : new double[]{minZ, maxZ}) {
            for (int i = 0; i <= stepsY; i++) points.add(new Point(x, at(minY, maxY, i, stepsY), z));
        }
        for (double x : new double[]{minX, maxX}) for (double y : new double[]{minY, maxY}) {
            for (int i = 0; i <= stepsZ; i++) points.add(new Point(x, y, at(minZ, maxZ, i, stepsZ)));
        }
        return List.copyOf(points);
    }

    private static int steps(double length, double spacing, int max) {
        return Math.max(1, (int) Math.min(max, Math.ceil(length / spacing)));
    }

    private static double at(double min, double max, int index, int steps) {
        return index == steps ? max : min + (max - min) * index / steps;
    }
}
