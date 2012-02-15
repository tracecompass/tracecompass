package org.eclipse.linuxtools.lttng.ui.tests.distribution;

import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.ui.views.latency.model.Config;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.GraphScaledData;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.IGraphModelListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.LatencyGraphModel;

@SuppressWarnings("nls")
public class LatencyGraphModelTest extends TestCase {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    
    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------
    
    public void testLatencyGraphModel() {
        LatencyGraphModel model = new LatencyGraphModel();
        assertEquals("nbBuckets", Config.DEFAULT_NUMBER_OF_BUCKETS, model.getNbBuckets());
        assertEquals("currentTime", Config.INVALID_EVENT_TIME, model.getCurrentEventTime());
    }
    
    public void testLatencyGraphModelInt() {
        LatencyGraphModel model = new LatencyGraphModel(100);
        assertEquals("nbBuckets", 100, model.getNbBuckets());
        assertEquals("currentTime", Config.INVALID_EVENT_TIME, model.getCurrentEventTime());
    }

    
    public void testGraphModelListener() {
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final int[] count = new int[2];
        count [0] = 0;
        count [1] = 0;

        // Test add listener and call of listener
        IGraphModelListener listener = new IGraphModelListener() {
            
            @Override
            public void graphModelUpdated() {
                count[0]++;
                
            }
            
            @Override
            public void currentEventUpdated(long currentEventTime) {
                count[1]++;
            }
        };

        // Test that the listener interface is called every 10000 events.
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        model.addGraphModelListener(listener);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i+1, i, i);
        }
        
        assertEquals("listener", 2, count[0]);

        // Test that the listener interface is called when complete is called.
        model.complete();
        assertEquals("listener", 3, count[0]);

        // Test that clear triggers call of listener interface
        model.clear();
        assertEquals("listener", 4, count[0]);

        // Test that clear triggers call of listener interface
        model.setCurrentEventNotifyListeners(100);
        assertEquals("listener", 1, count[1]);
        
        // Test remove listener
        count[0] = 0;
        count[1] = 0;
        model.removeGraphModelListener(listener);
        
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i, i);
        }
        model.complete();
        assertEquals("listener", 0, count[1]);
        
        // Test that clear triggers call of listener interface
        model.setCurrentEventNotifyListeners(100);
        assertEquals("listener", 0, count[1]);
    }
    
    public void testConstructor() {
        final int nbBuckets = 2000;
        
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        GraphScaledData scaledData = model.scaleTo(100, 100, 1);
        
        // Verify model parameters
        assertEquals("Horizontal bucket duration", 1, model.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 1, model.getVerBucketDuration());

        assertEquals("Horizontal first bucket time", 0, model.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", 0, model.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket ", 0, model.getHorLastBucket());
        assertEquals("Vertical last bucket ", 0, model.getVerLastBucket());
        
        assertEquals("Horizontal first time", 0, model.getHorFirstEventTime());
        assertEquals("Vertical first time", 0, model.getVerFirstEventTime());

        assertEquals("Horizontal last time", 0, model.getHorLastEventTime());
        assertEquals("Vertical last time", 0, model.getVerLastEventTime());
        
        assertEquals("Horizontal time limit", 2000, model.getHorTimeLimit());
        assertEquals("Vertical time limit", 2000, model.getVerTimeLimit());
     
        // Verify scaled data parameters
        scaledData = model.scaleTo(101, 100, 1);
        
        assertEquals("barWidth", 1, scaledData.getBarWidth());
        assertEquals("height", 100, scaledData.getHeight());
        assertEquals("width", 101, scaledData.getWidth());
        
        assertEquals(Config.INVALID_EVENT_TIME, scaledData.getCurrentEventTime());
        
        assertEquals("Horizontal bucket duration", 1, scaledData.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 1, scaledData.getVerBucketDuration());
        
        assertEquals("Horizontal bucket end time", 1, scaledData.getHorBucketEndTime(0));
        assertEquals("Vertical bucket end time", 1, scaledData.getVerBucketEndTime(0));
        
        assertEquals("Horizontal bucket start time", 0, scaledData.getHorBucketStartTime(0));
        assertEquals("Vertical bucket start time", 0, scaledData.getVerBucketStartTime(0));

        assertEquals("Horizontal first time", 0, scaledData.getHorFirstEventTime());
        assertEquals("Vertical first time", 0, scaledData.getVerFirstEventTime());
        
        assertEquals("Horizontal first bucket time", 0, scaledData.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", 0, scaledData.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket time", 0, scaledData.getHorLastBucketTime());
        assertEquals("Vertical last bucket time", 0, scaledData.getVerLastBucketTime());
        
        assertEquals("Horizontal number of buckets", 101, scaledData.getHorNbBuckets());
        assertEquals("Vertical nubmer of buckets", 100, scaledData.getVerNbBuckets());
        
        assertEquals("Horizontal getIndex", 100, scaledData.getHorBucketIndex(100));
        assertEquals("Vertical getIndex", 124, scaledData.getVerBucketIndex(124));
        
        assertEquals("Horizontal last bucket", 0, scaledData.getHorLastBucket());
        assertEquals("Vertical last bucket", 0, scaledData.getVerLastBucket());
        
        assertEquals("Horizontal last event time", 0, scaledData.getHorLastEventTime());
        assertEquals("Vertical last event time", 0, scaledData.getVerLastEventTime());
    }
    
    public void testClear() {
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i+1, i, i);
        }
        // make sure that we actually counted something.
        GraphScaledData scaledData = model.scaleTo(100, 100, 1);

        assertTrue(scaledData.getHorLastBucket() > 0);
        
        model.clear();
        
        // Verify model parameters
        assertEquals("Horizontal bucket duration", 1, model.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 1, model.getVerBucketDuration());

        assertEquals("Horizontal first bucket time", 0, model.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", 0, model.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket ", 0, model.getHorLastBucket());
        assertEquals("Vertical last bucket ", 0, model.getVerLastBucket());
        
        assertEquals("Horizontal first time", 0, model.getHorFirstEventTime());
        assertEquals("Vertical first time", 0, model.getVerFirstEventTime());

        assertEquals("Horizontal last time", 0, model.getHorLastEventTime());
        assertEquals("Vertical last time", 0, model.getVerLastEventTime());
        
        assertEquals("Horizontal time limit", 2000, model.getHorTimeLimit());
        assertEquals("Vertical time limit", 2000, model.getVerTimeLimit());

        // Verify scaled data parameters
        scaledData = model.scaleTo(101, 100, 1);
        
        assertEquals("barWidth", 1, scaledData.getBarWidth());
        assertEquals("height", 100, scaledData.getHeight());
        assertEquals("width", 101, scaledData.getWidth());
        
        assertEquals(Config.INVALID_EVENT_TIME, scaledData.getCurrentEventTime());
        
        assertEquals("Horizontal bucket duration", 1, scaledData.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 1, scaledData.getVerBucketDuration());
        
        assertEquals("Horizontal bucket end time", 1, scaledData.getHorBucketEndTime(0));
        assertEquals("Vertical bucket end time", 1, scaledData.getVerBucketEndTime(0));
        
        assertEquals("Horizontal bucket start time", 0, scaledData.getHorBucketStartTime(0));
        assertEquals("Vertical bucket start time", 0, scaledData.getVerBucketStartTime(0));

        assertEquals("Horizontal first time", 0, scaledData.getHorFirstEventTime());
        assertEquals("Vertical first time", 0, scaledData.getVerFirstEventTime());
        
        assertEquals("Horizontal first bucket time", 0, scaledData.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", 0, scaledData.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket time", 0, scaledData.getHorLastBucketTime());
        assertEquals("Vertical last bucket time", 0, scaledData.getVerLastBucketTime());
        
        assertEquals("Horizontal getIndex", 100, scaledData.getHorBucketIndex(100));
        assertEquals("Vertical getIndex", 124, scaledData.getVerBucketIndex(124));

        assertEquals("Horizontal number of buckets", 101, scaledData.getHorNbBuckets());
        assertEquals("Vertical nubmer of buckets", 100, scaledData.getVerNbBuckets());
        
        assertEquals("Horizontal last bucket", 0, scaledData.getHorLastBucket());
        assertEquals("Vertical last bucket", 0, scaledData.getVerLastBucket());
        
        assertEquals("Horizontal last event time", 0, scaledData.getHorLastEventTime());
        assertEquals("Vertical last event time", 0, scaledData.getVerLastEventTime());
    }
    
    public void testCountEvent() {
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final long hOffset = 100;
        final long vOffset = 55;
        
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i + 1, hOffset + i, vOffset + i);
        }

        // Verify model parameters
        assertEquals("Horizontal bucket duration", 16, model.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 16, model.getVerBucketDuration());

        assertEquals("Horizontal first bucket time", hOffset, model.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", vOffset, model.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket ", (nbEvents - 1)/16, model.getHorLastBucket());
        assertEquals("Vertical last bucket ", (nbEvents - 1)/16, model.getVerLastBucket());
        
        assertEquals("Horizontal first time", hOffset, model.getHorFirstEventTime());
        assertEquals("Vertical first time", vOffset, model.getVerFirstEventTime());

        assertEquals("Horizontal last time", nbEvents + hOffset - 1, model.getHorLastEventTime());
        assertEquals("Vertical last time", nbEvents + vOffset - 1, model.getVerLastEventTime());
        
        assertEquals("Horizontal time limit", 16 * nbBuckets + hOffset, model.getHorTimeLimit());
        assertEquals("Vertical time limit", 16 * nbBuckets + vOffset, model.getVerTimeLimit());

        // Verify scaled data parameters
        GraphScaledData scaledData = model.scaleTo(50, 100, 1);
        
        assertEquals("barWidth", 1, scaledData.getBarWidth());
        assertEquals("height", 100, scaledData.getHeight());
        assertEquals("width", 50, scaledData.getWidth());
        
        assertEquals(Config.INVALID_EVENT_TIME, scaledData.getCurrentEventTime());

        // nbBars = width / barWidth
        // bucketsPerBar = lastBucket/nbBars + 1
        // scaled bucket duration = bucketsPerBar * model.bucketDuration
        // for nbBuckets=2000 and nbEvents=20256 (means 20256 ns + offset) -> model.bucketDuration = 16
        assertEquals("Horizontal bucket duration", 416, scaledData.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 208, scaledData.getVerBucketDuration());
        
        // startTime + scaledData.bucketDuration
        assertEquals("Horizontal bucket end time", hOffset + 416, scaledData.getHorBucketEndTime(0));
        assertEquals("Vertical bucket end time", 55 + 208, scaledData.getVerBucketEndTime(0));
        
        assertEquals("Horizontal bucket start time", 100, scaledData.getHorBucketStartTime(0));
        assertEquals("Vertical bucket start time", 55, scaledData.getVerBucketStartTime(0));

        assertEquals("Horizontal first time", 100, scaledData.getHorFirstEventTime());
        assertEquals("Vertical first time", 55, scaledData.getVerFirstEventTime());
        
        assertEquals("Horizontal first bucket time", hOffset, scaledData.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", 55, scaledData.getVerFirstBucketTime());
        
        assertEquals("Horizontal last bucket time", hOffset + 48 * 416, scaledData.getHorLastBucketTime());
        assertEquals("Vertical last bucket time", vOffset + 97 * 208, scaledData.getVerLastBucketTime());
        
        assertEquals("Horizontal getIndex", 47, scaledData.getHorBucketIndex(20000));
        assertEquals("Vertical getIndex", 47, scaledData.getVerBucketIndex(10000));
        
        // nb Buckets = nbBars
        assertEquals("Horizontal number of buckets", 50, scaledData.getHorNbBuckets());
        assertEquals("Vertical nubmer of buckets", 100, scaledData.getVerNbBuckets());
        
        assertEquals("Horizontal last bucket", 48, scaledData.getHorLastBucket());
        assertEquals("Vertical last bucket", 97, scaledData.getVerLastBucket());
        
        // start time of last bucket
        assertEquals("Horizontal last event time", hOffset + nbEvents - 1, scaledData.getHorLastEventTime());
        assertEquals("Vertical last event time", vOffset + nbEvents - 1 , scaledData.getVerLastEventTime());
    }

    public void testCountEvent2() {

        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final long offset = 100;
        final int height = 100;
        final int width = 100;
        final int barWidth = 1;
        
        int[][] expectedResults = new int[width/barWidth][height/barWidth];
        
        int total = 0;
        
        // Horizontally and vertically the same data is used
        
        // for nbBuckets=2000 and nbEvents=20256 (means 20256 ns + offset) -> model.bucketDuration = 16
        // nbBars = width / barWidth = 100
        // bucketsPerBar = lastBucket/nbBars + 1 = 13
        // scaled bucket duration = bucketsPerBar * model.bucketDuration =  13 * 16
        boolean isFinished = false;
        for (int i = 0; i < width/barWidth; i++) {
            if (isFinished) {
                break;
            }
            for (int j = 0; j < height/barWidth; j++) {
                if (i == j) {
                    int value = 13 * 16;
                    if (total + value > nbEvents) {
                        expectedResults[i][j] = nbEvents - total;
                        isFinished = true;
                        break;
                    }
                    else {
                        expectedResults[i][j] = value;
                        total += value;
                    }
                }
            }
        }
       
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i+1, offset + i, offset + i);
        }

        GraphScaledData scaledData = model.scaleTo(height, width, barWidth);

        for (int i = 0; i < scaledData.getHorLastBucket(); i++) {
            for (int j = 0; j < scaledData.getVerLastBucket(); j++) {
                assertEquals(expectedResults[i][j], scaledData.getEventCount(i, j));
            }
        }
    }
    
    public void testCountEvent3() {
        // Test with barWidth > 1
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final long offset = 100;
        final int height = 100;
        final int width = 100;
        final int barWidth = 4;
        
        int[][] expectedResults = new int[width/barWidth][height/barWidth];
        
        int total = 0;
        
        // Horizontally and vertically the same data is used
        
        // for nbBuckets=2000 and nbEvents=20256 (means 20256 ns + offset) -> model.bucketDuration = 16
        // nbBars = width / barWidth = 25
        // bucketsPerBar = lastBucket/nbBars + 1 = 51
        // scaled bucket duration = bucketsPerBar * model.bucketDuration =  51 * 16
        boolean isFinished = false;
        for (int i = 0; i < width/barWidth; i++) {
            if (isFinished) {
                break;
            }
            for (int j = 0; j < height/barWidth; j++) {
                if (i == j) {
                    int value = 51 * 16;
                    if (total + value > nbEvents) {
                        expectedResults[i][j] = nbEvents - total;
                        isFinished = true;
                        break;
                    }
                    else {
                        expectedResults[i][j] = value;
                        total += value;
                    }
                }
            }
        }
       
        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i+1, offset + i, offset + i);
        }

        GraphScaledData scaledData = model.scaleTo(height, width, barWidth);

        for (int i = 0; i < scaledData.getHorLastBucket(); i++) {
            for (int j = 0; j < scaledData.getVerLastBucket(); j++) {
                assertEquals(expectedResults[i][j], scaledData.getEventCount(i, j));
            }
        }
    }
    
    public void testCountEventReverse1() {
        // Depending on the number of buckets and events the start buckets can be different
        // between forward and reserve times. However, the content is correct.
        final int nbBuckets = 100;
        final int nbEvents = 256;
        final long hOffset = 100;
        final long vOffset = 55;
        final int height = 100;
        final int width = 50;
        final int barWidth = 1;

        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i + 1, hOffset + i, vOffset + i);
        }
        
        GraphScaledData scaledData = model.scaleTo(width, height, barWidth);
        
        model.clear();
        
        for (int i = nbEvents - 1; i >= 0; i--) {
            model.countEvent(nbEvents - i, hOffset + i, vOffset + i);
        }
        
        GraphScaledData scaledDataReverse = model.scaleTo(50, 100, 1);
        
        long count = 0;
        for (int i = 0; i <= scaledData.getHorLastBucket(); i++) {
            for (int j = 0; j <= scaledData.getVerLastBucket(); j++) {
                count += scaledData.getEventCount(i, j);
            }
        }
        
        long revCount = 0;
        for (int i = 0; i <= scaledDataReverse.getHorLastBucket(); i++) {
            for (int j = 0; j <= scaledDataReverse.getVerLastBucket(); j++) {
                revCount += scaledDataReverse.getEventCount(i, j);
            }
        }

        assertEquals(count, revCount);

        // Make sure that both scaledData have the same content
        assertTrue("barWidth", scaledData.getBarWidth() == scaledDataReverse.getBarWidth());
        assertTrue("height", scaledData.getHeight() == scaledDataReverse.getHeight());
        assertTrue("width", scaledData.getWidth() == scaledDataReverse.getWidth());
        
        assertTrue(scaledData.getCurrentEventTime() == scaledDataReverse.getCurrentEventTime());

        assertTrue("Horizontal bucket duration", scaledData.getHorBucketDuration() == scaledDataReverse.getHorBucketDuration());
        assertTrue("Vertical bucket duration", scaledData.getVerBucketDuration() == scaledDataReverse.getVerBucketDuration());
        
        // startTime + scaledData.bucketDuration
        assertTrue("Horizontal bucket end time", scaledData.getHorBucketEndTime(0) == scaledDataReverse.getHorBucketEndTime(0));
        assertTrue("Vertical bucket end time", scaledData.getVerBucketEndTime(0) == scaledDataReverse.getVerBucketEndTime(0));
        
        assertTrue("Horizontal bucket start time", scaledData.getHorBucketStartTime(0) == scaledDataReverse.getHorBucketStartTime(0));
        assertTrue("Vertical bucket start time", scaledData.getVerBucketStartTime(0) == scaledDataReverse.getVerBucketStartTime(0));

        assertTrue("Horizontal first time", scaledData.getHorFirstEventTime() == scaledDataReverse.getHorFirstEventTime());
        assertTrue("Vertical first time",  scaledData.getVerFirstEventTime() == scaledDataReverse.getVerFirstEventTime());
        
        assertTrue("Horizontal getIndex", scaledData.getHorBucketIndex(200) == scaledDataReverse.getHorBucketIndex(200));
        assertTrue("Vertical getIndex", scaledData.getVerBucketIndex(100) == scaledDataReverse.getVerBucketIndex(100));

        assertTrue("Horizontal last bucket", scaledData.getHorNbBuckets() == scaledDataReverse.getHorNbBuckets());
        assertTrue("Vertical last bucket", scaledData.getVerNbBuckets() == scaledDataReverse.getVerNbBuckets());

        assertTrue("Horizontal nubmer of buckets", scaledData.getHorLastBucket() == scaledDataReverse.getHorLastBucket());
        assertTrue("Vertical nubmer of buckets", scaledData.getVerLastBucket() == scaledDataReverse.getVerLastBucket());
        
        // start time of last bucket
        assertTrue("Horizontal last event time", scaledData.getHorLastEventTime() == scaledDataReverse.getHorLastEventTime());
        assertTrue("Vertical last event time", scaledData.getVerLastEventTime() == scaledDataReverse.getVerLastEventTime());
    }

    public void testCountEventReverse2() {
        // Depending on the number of buckets and events the start buckets can be different
        // between forward and reserve times. However, the content is correct.
        final int nbBuckets = 100;
        final int nbEvents = 256;
        final long hOffset = 100;
        final long vOffset = 55;
        final int height = 100;
        final int width = 50;
        final int barWidth = 1;

        LatencyGraphModel model = new LatencyGraphModel(nbBuckets);
        
        for (int i = nbEvents - 1; i >= 0; i--) {
            model.countEvent(nbEvents - i, hOffset + i, vOffset + i);
        }

        // Verify model parameters
        int expectedBucketDuration = 4;
        assertEquals("Horizontal bucket duration", expectedBucketDuration, model.getHorBucketDuration());
        assertEquals("Vertical bucket duration", expectedBucketDuration, model.getVerBucketDuration());

        assertEquals("Horizontal first bucket time", hOffset, model.getHorFirstBucketTime());
        assertEquals("Vertical first bucket time", vOffset, model.getVerFirstBucketTime());

        assertEquals("Horizontal last bucket", (nbEvents -1)/expectedBucketDuration, model.getHorLastBucket());
        assertEquals("Vertical last bucket", (nbEvents -1)/expectedBucketDuration, model.getVerLastBucket());

        assertEquals("Horizontal first time", hOffset, model.getHorFirstEventTime());
        assertEquals("Vertical first time", vOffset, model.getVerFirstEventTime());

        assertEquals("Horizontal last time", nbEvents + hOffset - 1, model.getHorLastEventTime());
        assertEquals("Vertical last time", nbEvents + vOffset - 1, model.getVerLastEventTime());

        assertEquals("Horizontal time limit", expectedBucketDuration * nbBuckets + hOffset, model.getHorTimeLimit());
        assertEquals("Vertical time limit", expectedBucketDuration * nbBuckets + vOffset, model.getVerTimeLimit());

        GraphScaledData scaledData = model.scaleTo(50, 100, 1);
        
        // Make sure that both scaledData have the same content
        assertEquals("barWidth", barWidth, scaledData.getBarWidth());
        assertEquals("height", height, scaledData.getHeight());
        assertEquals("width", width, scaledData.getWidth());
        
        assertEquals(-1, scaledData.getCurrentEventTime()); 
        
        assertEquals("Horizontal bucket duration", 8, scaledData.getHorBucketDuration());
        assertEquals("Vertical bucket duration", 4, scaledData.getVerBucketDuration());
        
        // startTime + scaledData.bucketDuration
        assertEquals("Horizontal bucket end time", hOffset + 8, scaledData.getHorBucketEndTime(0));
        assertEquals("Vertical bucket end time", vOffset + 4, scaledData.getVerBucketEndTime(0));
        
        assertEquals("Horizontal bucket start time", hOffset, scaledData.getHorBucketStartTime(0));
        assertEquals("Vertical bucket start time", vOffset, scaledData.getVerBucketStartTime(0));

        assertEquals("Horizontal first time", hOffset, scaledData.getHorFirstEventTime());
        assertEquals("Vertical first time",  vOffset, scaledData.getVerFirstEventTime());
        
        assertEquals("Horizontal getIndex", 12, scaledData.getHorBucketIndex(200));
        assertEquals("Vertical getIndex", 11, scaledData.getVerBucketIndex(100));

        // nb Buckets = nbBars
        assertEquals("Horizontal number of buckets", 50, scaledData.getHorNbBuckets());
        assertEquals("Vertical number of buckets", 100, scaledData.getVerNbBuckets());
        
        assertEquals("Horizontal last bucket", 31, scaledData.getHorLastBucket());
        assertEquals("Vertical last bucket", 63, scaledData.getVerLastBucket());
 
        // start time of last bucket
        assertEquals("Horizontal last event time", 355, scaledData.getHorLastEventTime());
        assertEquals("Vertical last event time", 310, scaledData.getVerLastEventTime());
    }
}
