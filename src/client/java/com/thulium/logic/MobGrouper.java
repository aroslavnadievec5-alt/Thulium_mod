package com.thulium.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class MobGrouper {

    public record GroupResult<T>(
            List<T> visibleEntities,
            List<T> clumpedEntities
    ) {}

    public static <T> GroupResult<T> groupEntities(
            List<T> entities,
            double radius,
            int maxRenderedPerGroup,
            Function<T, Object> kindExtractor,
            ToDoubleFunction<T> xExtractor,
            ToDoubleFunction<T> yExtractor,
            ToDoubleFunction<T> zExtractor
    ) {
        List<T> visible = new ArrayList<>();
        List<T> clumped = new ArrayList<>();

        if (entities == null || entities.isEmpty()) {
            return new GroupResult<>(visible, clumped);
        }

        double safeRadius = Math.max(0.1, radius);
        double radiusSq = safeRadius * safeRadius;
        int limit = Math.max(1, maxRenderedPerGroup);

        // Sort deterministically so grouping does not flicker randomly every frame
        List<T> sorted = new ArrayList<>(entities);
        sorted.sort(Comparator
                .comparingDouble(xExtractor)
                .thenComparingDouble(yExtractor)
                .thenComparingDouble(zExtractor));

        Set<Integer> visitedIndices = new HashSet<>();

        for (int i = 0; i < sorted.size(); i++) {
            if (visitedIndices.contains(i)) {
                continue;
            }

            T anchor = sorted.get(i);
            Object anchorKind = kindExtractor.apply(anchor);
            double ax = xExtractor.applyAsDouble(anchor);
            double ay = yExtractor.applyAsDouble(anchor);
            double az = zExtractor.applyAsDouble(anchor);

            List<T> cluster = new ArrayList<>();
            cluster.add(anchor);
            visitedIndices.add(i);

            for (int j = i + 1; j < sorted.size(); j++) {
                if (visitedIndices.contains(j)) {
                    continue;
                }

                T candidate = sorted.get(j);
                Object candidateKind = kindExtractor.apply(candidate);
                if (anchorKind == null || !anchorKind.equals(candidateKind)) {
                    continue;
                }

                double dx = xExtractor.applyAsDouble(candidate) - ax;
                double dy = yExtractor.applyAsDouble(candidate) - ay;
                double dz = zExtractor.applyAsDouble(candidate) - az;
                double distSq = dx * dx + dy * dy + dz * dz;

                if (distSq <= radiusSq) {
                    cluster.add(candidate);
                    visitedIndices.add(j);
                }
            }

            for (int k = 0; k < cluster.size(); k++) {
                if (k < limit) {
                    visible.add(cluster.get(k));
                } else {
                    clumped.add(cluster.get(k));
                }
            }
        }

        return new GroupResult<>(visible, clumped);
    }
}