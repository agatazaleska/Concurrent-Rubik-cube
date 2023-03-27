package concurrentcube;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

public class CubeTest {

    private static final int TOP = 0;
    private static final int LEFT = 1;
    private static final int FRONT = 2;
    private static final int RIGHT = 3;
    private static final int BACK = 4;
    private static final int BOTTOM = 5;

    public static Random random = new Random();

    public static class Stopwatch {

        private Instant start;
        public void start() {
            start = Instant.now();
        }
        public Duration stop() {
            Duration duration = Duration.between(start, Instant.now());
            start = null;
            return duration;
        }
    }

    public static class Rotation {
        private final int side;
        private final int layer;

        public Rotation(int side, int layer) {
            this.side = side;
            this.layer = layer;
        }
    }

    private static class Rotator implements Runnable {
        private final int side;
        private final int layer;
        private final Cube c;

        private Rotator(Cube c, int side, int layer) {
            this.side = side;
            this.layer = layer;
            this.c = c;
        }

        @Override
        public void run() {
            try {
                c.rotate(side, layer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ShowRunnable implements Runnable {
        private final Cube c;

        private ShowRunnable(Cube c) {
            this.c = c;
        }

        @Override
        public void run() {
            try {
                c.show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int factorial(int n) {
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private void swapRotations(Rotation[] arr, int i, int j) {
        Rotation temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private void getAllRecursive(
            int n, Rotation[] elements, ArrayList<Rotation[]> result) {

        if(n == 1) {
            result.add(elements.clone());
        } else {
            for(int i = 0; i < n-1; i++) {
                getAllRecursive(n - 1, elements, result);
                if(n % 2 == 0) {
                    swapRotations(elements, i, n-1);
                } else {
                    swapRotations(elements, 0, n-1);
                }
            }
            getAllRecursive(n - 1, elements, result);
        }
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test1_rotate_front() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "000" +
                "000" +
                "111" +

                "115" +
                "115" +
                "115" +

                "222" +
                "222" +
                "222" +

                "033" +
                "033" +
                "033" +

                "444" +
                "444" +
                "444" +

                "333" +
                "555" +
                "555";

        c.rotate(FRONT, 0);
        assert(s.equals(c.show()));
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test2_rotate_back() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "333" +
                "000" +
                "000" +

                "011" +
                "011" +
                "011" +

                "222" +
                "222" +
                "222" +

                "335" +
                "335" +
                "335" +

                "444" +
                "444" +
                "444" +

                "555" +
                "555" +
                "111";

        c.rotate(BACK, 0);
        assert(s.equals(c.show()));
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test3_rotate_left() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "400" +
                "400" +
                "400" +

                "111" +
                "111" +
                "111" +

                "022" +
                "022" +
                "022" +

                "333" +
                "333" +
                "333" +

                "445" +
                "445" +
                "445" +

                "255" +
                "255" +
                "255";

        c.rotate(LEFT, 0);
        assert(s.equals(c.show()));
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test4_rotate_right() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "002" +
                "002" +
                "002" +

                "111" +
                "111" +
                "111" +

                "225" +
                "225" +
                "225" +

                "333" +
                "333" +
                "333" +

                "044" +
                "044" +
                "044" +

                "554" +
                "554" +
                "554";

        c.rotate(RIGHT, 0);
        assert(s.equals(c.show()));
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test5_rotate_top() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "000" +
                "000" +
                "000" +

                "222" +
                "111" +
                "111" +

                "333" +
                "222" +
                "222" +

                "444" +
                "333" +
                "333" +

                "111" +
                "444" +
                "444" +

                "555" +
                "555" +
                "555";

        c.rotate(TOP, 0);
        assert(s.equals(c.show()));
    }

    /* prosty test na poprawność obrotów */
    @Test
    public void test6_rotate_bottom() throws InterruptedException {
        Cube c = new Cube(3,
                (x, y) -> {},
                (x, y) -> {},
                () -> {},
                () -> {}
        );

        String s = "000" +
                "000" +
                "000" +

                "111" +
                "111" +
                "444" +

                "222" +
                "222" +
                "111" +

                "333" +
                "333" +
                "222" +

                "444" +
                "444" +
                "333" +

                "555" +
                "555" +
                "555";

        c.rotate(BOTTOM, 0);
        assert(s.equals(c.show()));
    }

    /* Test sprawdza czy obroty wykonują się szybciej współbieżnie */
    @Test
    public void test7_fast_concurrency() throws InterruptedException {
        int cube_size = 1000;

        Cube c_fast = new Cube(cube_size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}, () -> {}
        );

        Stopwatch stopwatch1 = new Stopwatch();
        stopwatch1.start();

        Thread[] threads = new Thread[cube_size];
        for (int j = 0; j < cube_size; j++)
            threads[j] = new Thread(new Rotator(c_fast, 1 , j));

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        Duration syncDuration = stopwatch1.stop();
        System.out.println("Concurrent rotations finished");
        System.out.println("It took " + syncDuration);

        Cube c_slow = new Cube(cube_size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}, () -> {}
        );

        Stopwatch stopwatch2 = new Stopwatch();
        stopwatch2.start();

        for (int j = 0; j < cube_size; j++)
            c_slow.rotate(1, j);

        Duration syncDuration2 = stopwatch2.stop();
        System.out.println("Sequential rotations finished");
        System.out.println("It took " + syncDuration2);

        assert(syncDuration2.compareTo(syncDuration) > 0);
    }

    /* Test sprawdza czy obroty wykonują się szybciej współbieżnie
       Tym razem obsługujemy więcej niż jedną grup procesów */
    @Test
    public void test8_fast_concurrency2() throws InterruptedException {
        int cube_size = 3;
        int thread_count = 1000;

        Cube c_fast = new Cube(cube_size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}, () -> {}
        );

        Stopwatch stopwatch1 = new Stopwatch();
        stopwatch1.start();

        Thread[] threads = new Thread[thread_count];
        for (int j = 0; j < thread_count; j++)
            threads[j] = new Thread(new Rotator(c_fast, j % 6 , j % cube_size));

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        Duration syncDuration = stopwatch1.stop();
        System.out.println("Concurrent rotations finished");
        System.out.println("It took " + syncDuration);

        Cube c_slow = new Cube(cube_size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}, () -> {}
        );

        Stopwatch stopwatch2 = new Stopwatch();
        stopwatch2.start();

        for (int j = 0; j < thread_count; j++)
            c_slow.rotate(j % 6, j % cube_size);

        Duration syncDuration2 = stopwatch2.stop();
        System.out.println("Sequential rotations finished");
        System.out.println("It took " + syncDuration2);

        assert(syncDuration2.compareTo(syncDuration) > 0);
    }

    /* wykonuje współbieżnie thread_number obrotów na kostce i sprawdza poprawną ilość kolorów */
    @Test
    public void test9_correct_square_count_safety() throws InterruptedException {
        int cube_size = 3;
        int thread_number = 1000;
        //Shower.setDefaultMode(Shower.COLOR | Shower.GRID);

        Cube c = new Cube(cube_size,
            (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {}
        );

        c.reset();
        Thread[] threads = new Thread[thread_number];
        for (int j = 0; j < thread_number; j++)
            threads[j] = new Thread(new Rotator(c, j % 6, j % cube_size));

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        assert (c.correctSquareCount());
    }

    /* sprawdza czy wykonanie współbieżnych operacji daje jeden z prawidłowych wyników
     * generujemy wszystkie możliwe permutacje operacji do wykonania
     * a następnie porównujemy wynik współbieżny z każdą z nich */
    @Test
    public void test10_correct_concurrent_rotations() throws InterruptedException {
        int size = 4;
        int thread_number = 5;

        Cube c = new Cube(size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {},
                () -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}
        );

        Thread[] threads = new Thread[thread_number];
        Rotation[] rotations = new Rotation[thread_number];
        int side, layer;

        for (int i = 0; i < thread_number; i++) {
            side = random.nextInt(6);
            layer = random.nextInt(size);

            threads[i] = new Thread(new Rotator(c, side, layer));
            rotations[i] = (new Rotation(side, layer));
        }

        for (Thread t : threads)
            t.start();

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cubeState = c.show();

        int permutations_number = factorial(thread_number);
        ArrayList<Rotation[]> all_permutations = new ArrayList<>();
        getAllRecursive(thread_number, rotations, all_permutations);

        Cube sequentialCube = new Cube(size,
                (x, y) -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> {},
                () -> {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                () -> {}
        );

        boolean found = false;
        Rotation[] current_permutation;
        for (int i = 0; i < permutations_number; i++) {

            current_permutation = all_permutations.get(i);

            sequentialCube.reset();
            for (Rotation rotation : current_permutation) {
                sequentialCube.rotate(rotation.side, rotation.layer);
            }
            String currentSequentialCubeState = sequentialCube.show();

            if (cubeState.equals(currentSequentialCubeState)) {
                found = true;
                break;
            }
        }
        assert(found);
    }

    /* sprawdza czy obsługa współbieżnego wykonywania funkcji rotate i show jest poprawna */
    @Test
    public void test11_concurrent_show_and_rotate() throws InterruptedException {
        int cube_size = 3;
        int thread_number = 9999;

        Cube c = new Cube(cube_size,
                (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {}
        );

        Thread[] threads = new Thread[thread_number];

        int j = 0;
        for (int i = 0; i < thread_number/3; i++) {
            threads[j++] = new Thread(new Rotator(c, i % 6, i % cube_size));
            threads[j++] = new Thread(new Rotator(c, (i + 13) % 6, (i + 7) % cube_size));
            threads[j++] = new Thread(new ShowRunnable(c));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        assert(c.correctSquareCount());
    }

    /* sprawdza czy dzieje się coś złego jak przerwiemy losowy wątek */
    @Test
    public void test12_interrupted() throws InterruptedException {
        int cube_size = 3;
        int thread_number = 9999;

        Cube c = new Cube(cube_size,
                (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {}
        );

        Thread[] threads = new Thread[thread_number];

        int j = 0;
        for (int i = 0; i < thread_number/3; i++) {
            threads[j++] = new Thread(new Rotator(c, i % 6, i % cube_size));
            threads[j++] = new Thread(new Rotator(c, (i + 13) % 6, (i + 7) % cube_size));
            threads[j++] = new Thread(new ShowRunnable(c));
        }

        for (Thread t : threads) t.start();
        threads[random.nextInt(thread_number)].interrupt();

        for (Thread t : threads) t.join();

        assert(c.correctSquareCount());
    }
}

