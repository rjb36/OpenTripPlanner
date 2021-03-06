package org.opentripplanner.profile;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.api.model.WalkStep;
import org.opentripplanner.index.model.RouteShort;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Option {

    private static final Logger LOG = LoggerFactory.getLogger(Option.class);

    public List<Segment> transit;
    public List<StreetSegment> access;
    public List<StreetSegment> egress;
    public Stats stats = new Stats();
    public String summary;
    public List<DCFareCalculator.Fare> fares;
    // The fares are outside the transit segments because a fare can apply to multiple segments so there is no one-to-one
    // correspondance. For example, when you transfer from one subway to another and pay one fare for the two segments.

    public Option (Ride tail, Collection<StopAtDistance> accessPaths, Collection<StopAtDistance> egressPaths) {
        access = StreetSegment.list(accessPaths);
        egress = StreetSegment.list(egressPaths);
        // Include access and egress times across all modes in the overall travel time statistics for this option.
        // FIXME In the event that there is only access, N will still be 1 which is strange.
        stats.add(access);
        stats.add(egress);
        List<Ride> rides = Lists.newArrayList();
        for (Ride ride = tail; ride != null; ride = ride.previous) rides.add(ride);
        if ( ! rides.isEmpty()) {
            Collections.reverse(rides);
            rides.get(0).accessTime = 0; // Avoid double-inclusion of the access time for the first leg.
            rides.get(0).accessDist = 0; // Just to make dist coherent with the accessTime in the result object.
            transit = Lists.newArrayList();
            for (Ride ride : rides) {
                Segment segment = new Segment(ride);
                transit.add(segment);
                stats.add(segment.walkTime);
                if(segment.waitStats != null) stats.add(segment.waitStats);
                stats.add(segment.rideStats);
            }
        }
        // Really should be one per segment, with transfers to the same operator having a price of 0.
        fares = DCFareCalculator.calculateFares(rides);
        summary = generateSummary();
    }

    /** Make a human readable text summary of this option. */
    public String generateSummary() {
        if (transit == null || transit.isEmpty()) {
            return "Non-transit options";
        }
        List<String> vias = Lists.newArrayList();
        List<String> routes = Lists.newArrayList();
        for (Segment segment : transit) {
            List<String> routeShortNames = Lists.newArrayList();
            for (RouteShort rs : segment.routes) {
                String routeName = rs.shortName == null ? rs.longName : rs.shortName;
                routeShortNames.add(routeName);
            }
            routes.add(Joiner.on("/").join(routeShortNames));
            vias.add(segment.toName);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("routes ");
        sb.append(Joiner.on(", ").join(routes));
        if (!vias.isEmpty()) vias.remove(vias.size() - 1);
        if (!vias.isEmpty()) {
            sb.append(" via ");
            sb.append(Joiner.on(", ").join(vias));
        }
        return sb.toString();
    }

    public static enum SortOrder {
        MIN, AVG, MAX;
    }

    public static class MinComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.min - two.stats.min;
        }
    }

    public static class AvgComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.avg - two.stats.avg;
        }
    }

    public static class MaxComparator implements Comparator<Option> {
        @Override
        public int compare(Option one, Option two) {
            return one.stats.max - two.stats.max;
        }
    }

    /**
     * Rides or transfers may contain no patterns after applying time window.
     */
    public boolean hasEmptyRides() {
        for (Segment seg : transit) {
            if (seg.rideStats.num == 0 || seg.waitStats.num == 0) {
                return true;
            }
        }
        return false;
    }

}
