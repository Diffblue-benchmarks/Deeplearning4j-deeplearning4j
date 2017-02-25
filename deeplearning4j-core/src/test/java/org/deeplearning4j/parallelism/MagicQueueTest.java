package org.deeplearning4j.parallelism;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.*;

/**
 * @author raver119@gmail.com
 */
@Slf4j
public class MagicQueueTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void addDataSet1() throws Exception {
        MagicQueue queue = new MagicQueue.Builder().setNumberOfBuckets(1).build();

        int numDevices = 1; // Force single device

        DataSet dataSet_1 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_2 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_3 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_4 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_5 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_6 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_7 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_8 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));

        queue.add(dataSet_1);
        queue.add(dataSet_2);
        queue.add(dataSet_3);
        queue.add(dataSet_4);
        queue.add(dataSet_5);
        queue.add(dataSet_6);
        queue.add(dataSet_7);
        queue.add(dataSet_8);

        Thread.sleep(500);

        assertEquals(8 / numDevices, queue.size());


        int cnt = 0;
        while (!queue.isEmpty()) {
            DataSet ds = queue.poll();
            assertNotEquals("Failed on iteration: " + cnt,null, ds);
            cnt++;
        }

        assertEquals(8, cnt);
    }


    /**
     * This test will fail on single-gpu system
     *
     * @throws Exception
     */
    @Test
    public void addDataSet2() throws Exception {
        MagicQueue queue = new MagicQueue.Builder().build();

        int numDevices = Nd4j.getAffinityManager().getNumberOfDevices();

        for (int i = 0; i < numDevices * 4; i++ ) {
            DataSet dataSet = new DataSet(Nd4j.create(new float[]{1f, 2f, 3f}), Nd4j.create(new float[]{1f, 2f, 3f}));
            queue.add(dataSet);
        }

        Thread.sleep(500);

        assertEquals(8 / numDevices, queue.size());


        int cnt = 0;
        while (!queue.isEmpty()) {
            DataSet ds = queue.poll();
            if (cnt < 4) {
                assertNotEquals("Failed on iteration: " + cnt, null, ds);
                cnt++;

            } else {
                break;
            }
        }

        assertEquals(4, cnt);
    }


    /**
     * THIS TEST REQUIRES CUDA BACKEND AND MULTI-GPU ENVIRONMENT
     * TO USE THIS TEST - ENABLE ND4J-CUDA BACKEND FOR THIS MODULE
     *
     * In this test we check actual data relocation within MagicQueue
     *
     * @throws Exception
     */
    @Test
    public void test_cuda_multiGPU_testAffinityChange1() throws Exception {
        MagicQueue queue = new MagicQueue.Builder().build();

        int numDevices = Nd4j.getAffinityManager().getNumberOfDevices();

        DataSet dataSet_1 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_2 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_3 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_4 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_5 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_6 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_7 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));
        DataSet dataSet_8 = new DataSet(Nd4j.create(new float[]{1f,2f,3f}), Nd4j.create(new float[]{1f,2f,3f}));



        // All arrays are located on same initial device
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_1.getFeatures()).intValue());
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_2.getFeatures()).intValue());
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_3.getFeatures()).intValue());
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_4.getFeatures()).intValue());

        queue.add(dataSet_1);
        queue.add(dataSet_2);
        queue.add(dataSet_3);
        queue.add(dataSet_4);
        queue.add(dataSet_5);
        queue.add(dataSet_6);
        queue.add(dataSet_7);
        queue.add(dataSet_8);

        Thread.sleep(500);

        assertEquals(8 / numDevices, queue.size());

        log.info("Checking first device...");

        // All arrays are spread over all available devices
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_1.getFeatures()).intValue());
        assertEquals(0, Nd4j.getAffinityManager().getDeviceForArray(dataSet_1.getLabels()).intValue());

        int nextDev = 0;
        if (numDevices > 1) {
            log.info("Checking second device...");
            nextDev++;
        }

        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_2.getFeatures()).intValue());
        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_2.getLabels()).intValue());

        if (numDevices > 2) {
            log.info("Checking third device...");
            nextDev++;
        } else {
            log.info("Checking first device...");
            nextDev = 0;
        }

        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_3.getFeatures()).intValue());
        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_3.getLabels()).intValue());

        if (numDevices > 2) {
            log.info("Checking fourth device...");
            nextDev++;
        } else {
            log.info("Checking second device...");
            nextDev = 1;
        }


        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_4.getFeatures()).intValue());
        assertEquals(nextDev, Nd4j.getAffinityManager().getDeviceForArray(dataSet_4.getLabels()).intValue());
    }

}
