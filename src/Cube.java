package concurrentcube;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class Cube {
    private static final int NUMBER_OF_SIDES = 6;
    private static final int NUMBER_OF_GROUPS = 4;
    private static final int TOP = 0;
    private static final int LEFT = 1;
    private static final int FRONT = 2;
    private static final int RIGHT = 3;
    private static final int BACK = 4;
    private static final int BOTTOM = 5;
    private static final int SHOWER_GROUP = 3;
    private static final int GROUP_LIMIT = 50;

    private final int size;
    private final int[][][] cube_state;
    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;

    /* Semafor, na którym czekają reprezentanci - pierwsze procesy z danej grupy */
    private final Semaphore representatives = new Semaphore(0, true);

    /* Mutex - semafor wzajemnie wykluczający do dostępu do zmiennych globalnych */
    private final Semaphore mutex = new Semaphore(1, true);

    /* Semafor, na którym czekają pozostali członkowie grup */
    private final Semaphore[] other_group_members = new Semaphore[NUMBER_OF_GROUPS];

    /* Semafor, na którym czeka proces z pracującej grupy. Musi zaczekać aż inny skończy obracać jego warstwę */
    private final Semaphore[] layer_rotating;

    private int current_group = -1; //obecna grupa
    private int processes_running = 0; //pracujące procesy
    private int representatives_number = 0; //ilość czekających reprezentantów
    private final int[] how_many_waiting_for_rotation = new int[NUMBER_OF_GROUPS]; //ilość czekających procesów z grupy

    //licznik dostępu procesów z jednej grupy do sekcji krytycznej - zapobiega zagłodzeniu
    private int current_group_counter = 0;

    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {

        this.size = size;
        cube_state = new int[NUMBER_OF_SIDES][size][size];
        for (int side = 0; side < NUMBER_OF_SIDES; side++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    cube_state[side][x][y] = side;
                }
            }
        }
        this.afterRotation = afterRotation;
        this.beforeRotation = beforeRotation;
        this.afterShowing = afterShowing;
        this.beforeShowing = beforeShowing;

        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            how_many_waiting_for_rotation[i] = 0;
            other_group_members[i] = new Semaphore(0);
        }

        layer_rotating = new Semaphore[size];
        for (int i = 0; i < size; i++) {
            layer_rotating[i] = new Semaphore(1);
        }
    }

    public void reset() {
        for (int i = 0; i < NUMBER_OF_SIDES; i++) {
            for (int row = 0; row < size; row++) {
                for (int column = 0; column < size; column++) {
                    cube_state[i][row][column] = i;
                }
            }
        }
    }

    public boolean correctSquareCount() {
        int[] colors = new int[NUMBER_OF_SIDES];
        for (int i = 0; i < NUMBER_OF_SIDES; i++)
            colors[i] = 0;

        for (int side = 0; side < NUMBER_OF_SIDES; side++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    colors[cube_state[side][x][y]]+= 1;
                }
            }
        }

        for (int i = 0; i < NUMBER_OF_SIDES; i++)
            if (colors[i] != size * size) return false;

        return true;
    }

    private int oppositeSide(int side) {
        switch (side) {
            case TOP:
                return BOTTOM;
            case LEFT:
                return RIGHT;
            case FRONT:
                return BACK;
            case RIGHT:
                return LEFT;
            case BACK:
                return FRONT;
            case BOTTOM:
                return TOP;
        }
        return -1;
    }

    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private void writeRowToArray(int[] array, int side, int layer, boolean reversed) {
        if (size >= 0) System.arraycopy(cube_state[side][layer], 0, array, 0, size);
        if (reversed) reverseArray(array);
    }

    private void writeColumnToArray(int[] array, int side, int layer, boolean reversed) {
        for (int i = 0; i < size; i++)
            array[i] = cube_state[side][i][layer];

        if (reversed) reverseArray(array);
    }

    private void writeRowToCube(int[] array, int side, int layer, boolean reversed) {
        if (size >= 0) System.arraycopy(array, 0, cube_state[side][layer], 0, size);
        if (reversed) reverseArray(array);
    }

    private void writeColumnToCube(int[] array, int side, int layer, boolean reversed) {
        for (int i = 0; i < size; i++)
            cube_state[side][i][layer] = array[i];

        if (reversed) reverseArray(array);
    }

    private void rotateFront(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeRowToArray(temp_array1, TOP, size - 1 - layer, false);

        writeColumnToArray(temp_array2, RIGHT, layer, false);
        writeColumnToCube(temp_array1, RIGHT, layer, false);

        writeRowToArray(temp_array1, BOTTOM, layer, true);
        writeRowToCube(temp_array2, BOTTOM, layer, true);

        writeColumnToArray(temp_array2, LEFT, size - 1 - layer, true);
        writeColumnToCube(temp_array1, LEFT, size - 1 - layer, true);

        writeRowToCube(temp_array2, TOP, size - 1 - layer, false);
    }

    private void rotateBack(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeRowToArray(temp_array1, TOP, layer, false);

        writeColumnToArray(temp_array2, LEFT, layer, true);
        writeColumnToCube(temp_array1, LEFT, layer, true);

        writeRowToArray(temp_array1, BOTTOM, size - 1 - layer, false);
        writeRowToCube(temp_array2, BOTTOM, size - 1 - layer, false);

        writeColumnToArray(temp_array2, RIGHT, size - 1 - layer, true);
        writeColumnToCube(temp_array1, RIGHT, size - 1 - layer, true);

        writeRowToCube(temp_array2, TOP, layer, false);
    }

    private void rotateLeft(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeColumnToArray(temp_array1, TOP, layer, false);

        writeColumnToArray(temp_array2, FRONT, layer, false);
        writeColumnToCube(temp_array1, FRONT, layer, false);

        writeColumnToArray(temp_array1, BOTTOM, layer, false);
        writeColumnToCube(temp_array2, BOTTOM, layer, false);

        writeColumnToArray(temp_array2, BACK, size - 1 - layer, true);
        writeColumnToCube(temp_array1, BACK, size - 1 - layer, true);

        writeColumnToCube(temp_array2, TOP, layer, false);
    }

    private void rotateRight(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeColumnToArray(temp_array1, TOP, size - 1 - layer, true);

        writeColumnToArray(temp_array2, BACK, layer, false);
        writeColumnToCube(temp_array1, BACK, layer, false);

        writeColumnToArray(temp_array1, BOTTOM, size - 1 - layer, true);
        writeColumnToCube(temp_array2, BOTTOM, size - 1 - layer, true);

        writeColumnToArray(temp_array2, FRONT, size - 1 - layer, true);
        writeColumnToCube(temp_array1, FRONT, size - 1 - layer, true);

        writeColumnToCube(temp_array2, TOP, size - 1 - layer, true);
    }

    private void rotateTop(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeRowToArray(temp_array1, FRONT, layer, true);

        writeRowToArray(temp_array2, LEFT, layer, true);
        writeRowToCube(temp_array1, LEFT, layer, true);

        writeRowToArray(temp_array1, BACK, layer, true);
        writeRowToCube(temp_array2, BACK, layer, true);

        writeRowToArray(temp_array2, RIGHT, layer, true);
        writeRowToCube(temp_array1, RIGHT, layer, true);

        writeRowToCube(temp_array2, FRONT, layer, true);
    }

    private void rotateBottom(int layer) {
        int[] temp_array1 = new int[size];
        int[] temp_array2 = new int[size];

        writeRowToArray(temp_array1, FRONT, size - layer - 1, false);

        writeRowToArray(temp_array2, RIGHT, size - layer - 1, false);
        writeRowToCube(temp_array1, RIGHT, size - layer - 1, false);

        writeRowToArray(temp_array1, BACK, size - layer - 1, false);
        writeRowToCube(temp_array2, BACK, size - layer - 1, false);

        writeRowToArray(temp_array2, LEFT, size - layer - 1, false);
        writeRowToCube(temp_array1, LEFT, size - layer - 1, false);

        writeRowToCube(temp_array2, FRONT, size - layer - 1, false);
    }

    private void rotateEdgeLayer(boolean layer_zero, int side) {
        int[][] new_layer = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (layer_zero) new_layer[y][size - 1 - x] = cube_state[side][x][y];
                else new_layer[size - 1 - y][x] = cube_state[oppositeSide(side)][x][y];
            }
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (layer_zero) cube_state[side][x][y] = new_layer[x][y];
                else cube_state[oppositeSide(side)][x][y] = new_layer[x][y];
            }
        }
    }

    /* obsługa czekania aż inna grupa skończy / będziemy mogli wejść */
    private void waitForOtherGroup(int group) throws InterruptedException {
        how_many_waiting_for_rotation[group]++; //zaznaczamy, że czekamy

        if (how_many_waiting_for_rotation[group] == 1) { //jesteśmy reprezentantem
            representatives_number++;
            mutex.release(); //oddajemy mutex przed zawieszeniem się na semaforze

            try {
                representatives.acquire(); //tutaj dziedziczymy mutexa
            } //przywracamy poprzedni stan przed przerwaniem wątków
            catch (InterruptedException e) {
                mutex.acquireUninterruptibly();
                representatives_number--;
                how_many_waiting_for_rotation[group]--;
                mutex.release();
                throw e;
            }

            ++current_group_counter; //zwiększamy licznik dostępów po obudzeniu
            current_group = group; //nasza grupa będzie teraz pracować
            representatives_number--;
        }
        else { //jesteśmy kolejnym z grupy procesem
            mutex.release();

            try {
                other_group_members[group].acquire(); //tutaj dziedziczymy mutexa
            } //przywracamy poprzedni stan przed przerwaniem wątków
            catch (InterruptedException e) {
                mutex.acquireUninterruptibly();
                how_many_waiting_for_rotation[group]--;
                mutex.release();
                throw e;
            }
        }
        how_many_waiting_for_rotation[group]--; //doczekaliśmy się
        processes_running++; //zaraz będziemy wchodzić do sekcji krytycznej - zwiększamy licznik

        //sprawdzamy, czy chcemy obudzić innych kolegów z grupy
        //jeśli przekroczyliśmy limit nie budzimy - dajemy szansę innym grupom (o ile czekają)
        if (how_many_waiting_for_rotation[group] > 0
            && (current_group_counter < GROUP_LIMIT || representatives_number == 0)) {

            other_group_members[group].release(); //tutaj przekazujemy mutexa
        }
        else mutex.release();
    }

    /* protokół wstępny przed wejściem do sekcji krytycznej */
    private void waitForActionIfNecessary(int group) throws InterruptedException {
        mutex.acquireUninterruptibly();

        if (current_group != group && current_group != -1) {
            waitForOtherGroup(group); //wchodzimy do funckji posiadając mutexa
        } else {
            if (current_group == group) { //nie przekroczyliśmy limitu lub nie ma innych grup
                if (current_group_counter < GROUP_LIMIT || representatives_number == 0) {
                    current_group_counter++;
                    processes_running++; //wchodzimy do sekcji krytycznej
                    mutex.release();
                }
                else { //przekroczony limit - dajemy szansę innym grupom
                    waitForOtherGroup(group);
                }
            }
            else { //nikogo nie ma w sekcji krytycznej - wchodzimy
                ++current_group_counter;
                current_group = group;
                processes_running++;
                mutex.release();
            }
        }
    }

    /* protokół końcowy po wyjściu z sekcji krytycznej */
    private void actionFinished() throws InterruptedException {
        mutex.acquireUninterruptibly();
        processes_running--;
        if (processes_running == 0) { //jeśli grupa skończyła patrzymy czy kogoś obudzić
            if (representatives_number > 0) {
                current_group_counter = 0; //zerujemy licznik przed wejściem kolejnej grupy
                representatives.release(); //tutaj przekazujemy mutexa
            }
            else { //nie ma kogo obbudzić - oddajemy mutexa i zerujemy licznik
                current_group_counter = 0;
                current_group = -1;
                mutex.release();
            }
        }
        else mutex.release();
    }

    /* zwraca uniwersalną warstwę w stosunku do osi */
    private int axisLayer(int side, int layer) {
        if (side == FRONT || side == LEFT || side == TOP)
            return layer;
        else return size - 1 - layer;
    }

    public void rotate(int side, int layer) throws InterruptedException {
        int group = ((side + 2) % 5) % 3;

        waitForActionIfNecessary(group);

        try {
            layer_rotating[axisLayer(side, layer)].acquire();
        }
        catch (InterruptedException e) { //przerwany wątek - musimy wykonać protokół końcowy
            Thread.currentThread().interrupt();
            actionFinished();
            throw e;
        }

        beforeRotation.accept(side, layer);
        if (layer == 0)
            rotateEdgeLayer(true, side);

        else if (layer == size - 1)
            rotateEdgeLayer(false, side);

        if (side == FRONT) rotateFront(layer);
        else if (side == BACK) rotateBack(layer);
        else if (side == LEFT) rotateLeft(layer);
        else if (side == RIGHT) rotateRight(layer);
        else if (side == TOP) rotateTop(layer);
        else if (side == BOTTOM) rotateBottom(layer);

        afterRotation.accept(side, layer);

        layer_rotating[axisLayer(side, layer)].release();
        actionFinished();
    }

    public String show() throws InterruptedException {
        waitForActionIfNecessary(SHOWER_GROUP);

        beforeShowing.run();
        StringBuilder state = new StringBuilder();

        for (int side = 0; side < NUMBER_OF_SIDES; side++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    state.append(cube_state[side][x][y]);
                }
            }
        }
        afterShowing.run();

        actionFinished();
        return state.toString();
    }
}
