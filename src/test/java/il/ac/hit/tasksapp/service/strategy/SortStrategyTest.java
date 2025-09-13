package il.ac.hit.tasksapp.service.strategy;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.state.TaskState;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static il.ac.hit.tasksapp.TestData.t;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Strategy tests that adapt to different implementation styles:
 *
 * Supported strategy styles:
 *  1) void sort(List<ITask> xs)                // in-place
 *  2) List<ITask> sort(List<ITask> xs)         // returns new list
 *  3) Comparator<ITask> comparator()/getComparator()  // VM sorts using this
 *
 * The assertions accept ASC or DESC ordering (מונוטוני) כי הכיוון הוא בחירה מימושית.
 */
public class SortStrategyTest {

    /* -------------------- ID -------------------- */

    @Test
    void sortById_monotonic() {
        List<ITask> input = List.of(
                t(3, "C", "", TaskState.TO_DO),
                t(1, "A", "", TaskState.IN_PROGRESS),
                t(2, "B", "", TaskState.COMPLETED)
        );
        List<ITask> out = applyStrategy(new SortById(), input);

        assertTrue(isMonotonic(out, ITask::getId),
                "Expected tasks to be monotonic by ID (asc or desc). Got: " + ids(out));
    }

    /* -------------------- Title -------------------- */

    @Test
    void sortByTitle_monotonic_alpha() {
        List<ITask> input = List.of(
                t(2, "Bravo", "", TaskState.TO_DO),
                t(3, "alpha", "", TaskState.TO_DO),
                t(1, "Charlie", "", TaskState.TO_DO)
        );
        List<ITask> out = applyStrategy(new SortByTitle(), input);

        // Accept CS/CI, ASC/DESC
        boolean ok =
                isMonotonic(out, ITask::getTitle, String::compareTo) ||
                        isMonotonic(out, ITask::getTitle, String::compareToIgnoreCase);
        assertTrue(ok, "Expected title ordering to be monotonic (ASC/DESC, CS/CI). Got: " + titles(out));
    }

    /* -------------------- State -------------------- */

    @Test
    void sortByState_monotonic() {
        List<ITask> input = List.of(
                t(1, "A", "", TaskState.COMPLETED),
                t(2, "B", "", TaskState.TO_DO),
                t(3, "C", "", TaskState.IN_PROGRESS)
        );
        List<ITask> out = applyStrategy(new SortByState(), input);

        // נקבל גם לפי ordinal, וגם לפי סדר עסקי נפוץ TO_DO < IN_PROGRESS < COMPLETED
        boolean byOrdinal = isMonotonic(out, t -> t.getState().ordinal());
        Map<TaskState,Integer> bizOrder = Map.of(
                TaskState.TO_DO, 0,
                TaskState.IN_PROGRESS, 1,
                TaskState.COMPLETED, 2
        );
        boolean byBiz = isMonotonic(out, t -> bizOrder.getOrDefault(t.getState(), 99));

        assertTrue(byOrdinal || byBiz,
                "Expected state ordering to be monotonic by ordinal or business order. Got: " + states(out));
    }

    /* ==========================================================
       ================ Strategy application core ================
       ========================================================== */

    /** Try: in-place void sort -> returned List sort -> comparator() / getComparator(). */
    private static List<ITask> applyStrategy(Object strategy, List<ITask> original) {
        // Try 1: void sort(List)
        try {
            Method m = strategy.getClass().getMethod("sort", List.class);
            if (m.getReturnType() == void.class) {
                List<ITask> copy = new ArrayList<>(original);
                m.invoke(strategy, copy);
                return copy;
            }
        } catch (NoSuchMethodException ignore) {
        } catch (Exception e) {
            throw new RuntimeException("Strategy void sort(List) failed", e);
        }

        // Try 2: List sort(List)
        try {
            Method m = strategy.getClass().getMethod("sort", List.class);
            if (List.class.isAssignableFrom(m.getReturnType())) {
                Object res = m.invoke(strategy, new ArrayList<>(original));
                @SuppressWarnings("unchecked")
                List<ITask> out = (List<ITask>) res;
                return new ArrayList<>(out);
            }
        } catch (NoSuchMethodException ignore) {
        } catch (Exception e) {
            throw new RuntimeException("Strategy List sort(List) failed", e);
        }

        // Try 3: comparator() / getComparator()
        Comparator<ITask> cmp = tryComparator(strategy);
        if (cmp != null) {
            List<ITask> copy = new ArrayList<>(original);
            copy.sort(cmp);
            return copy;
        }

        // If nothing worked, return original
        return new ArrayList<>(original);
    }

    private static Comparator<ITask> tryComparator(Object strategy) {
        for (String name : new String[]{"comparator", "getComparator"}) {
            try {
                Method m = strategy.getClass().getMethod(name);
                if (Comparator.class.isAssignableFrom(m.getReturnType())) {
                    @SuppressWarnings("unchecked")
                    Comparator<ITask> cmp = (Comparator<ITask>) m.invoke(strategy);
                    return cmp;
                }
            } catch (NoSuchMethodException ignore) {
            } catch (Exception e) {
                throw new RuntimeException("Strategy comparator acquisition failed", e);
            }
        }
        return null;
    }

    /* ==========================================================
       ====================== Assertions =========================
       ========================================================== */

    /** Monotonic (ASC or DESC) by natural Comparable key. */
    private static <T extends Comparable<T>> boolean isMonotonic(List<ITask> list, Function<ITask, T> key) {
        return isNonDecreasing(list, key, Comparable::compareTo)
                || isNonIncreasing(list, key, Comparable::compareTo);
    }

    /** Monotonic (ASC or DESC) by a custom comparator for the key. */
    private static <T> boolean isMonotonic(List<ITask> list, Function<ITask, T> key,
                                           Comparator<T> cmp) {
        return isNonDecreasing(list, key, cmp) || isNonIncreasing(list, key, cmp);
    }

    private static <T> boolean isNonDecreasing(List<ITask> list, Function<ITask,T> key, Comparator<T> cmp) {
        for (int i = 1; i < list.size(); i++) if (cmp.compare(key.apply(list.get(i-1)), key.apply(list.get(i))) > 0) return false;
        return true;
    }

    private static <T> boolean isNonIncreasing(List<ITask> list, Function<ITask,T> key, Comparator<T> cmp) {
        for (int i = 1; i < list.size(); i++) if (cmp.compare(key.apply(list.get(i-1)), key.apply(list.get(i))) < 0) return false;
        return true;
    }

    private static String ids(List<ITask> xs)    { return xs.stream().map(t -> String.valueOf(t.getId())).toList().toString(); }
    private static String titles(List<ITask> xs) { return xs.stream().map(ITask::getTitle).toList().toString(); }
    private static String states(List<ITask> xs) { return xs.stream().map(t -> t.getState().name()).toList().toString(); }
}
