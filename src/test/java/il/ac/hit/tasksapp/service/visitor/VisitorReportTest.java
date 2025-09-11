package il.ac.hit.tasksapp.service.visitor;

import il.ac.hit.tasksapp.model.ITask;
import il.ac.hit.tasksapp.model.record.TaskRecord;
import il.ac.hit.tasksapp.model.state.TaskState;
import il.ac.hit.tasksapp.model.visitor.CsvVisitor;
import il.ac.hit.tasksapp.model.visitor.JsonVisitor;
import il.ac.hit.tasksapp.model.visitor.StatsVisitor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import static il.ac.hit.tasksapp.TestData.t;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Your Visitors accept single TaskRecord per visit.
 * We feed them one-by-one, then extract the final output via
 * result()/getResult()/getOutput() (or toString()).
 * Assertions are regex/contains-based to allow formatting variations.
 */
public class VisitorReportTest {

    @Test
    void csv_json_stats_outputs_are_consistent() {
        List<ITask> tasks = List.of(
                t(1, "A", "d1", TaskState.TO_DO),
                t(2, "B", "d2", TaskState.IN_PROGRESS),
                t(3, "C", "d3", TaskState.COMPLETED)
        );
        List<TaskRecord> records = tasks.stream()
                .map(tt -> new TaskRecord(tt.getId(), tt.getTitle(), tt.getDescription(), tt.getState()))
                .toList();

        // --- CSV ---
        CsvVisitor csv = new CsvVisitor();
        records.forEach(csv::visit);
        String csvOut = extractOutput(csv);
        String csvLower = csvOut.toLowerCase();
        assertTrue(csvLower.contains("id") && csvLower.contains("title") && csvLower.contains("state"),
                "CSV must contain headers");
        assertTrue(csvOut.contains("TO_DO") || csvOut.contains("IN_PROGRESS") || csvOut.contains("COMPLETED"),
                "CSV must contain at least one state token");
        assertTrue(csvOut.matches("(?s).*1.*A.*TO_DO.*"),
                "CSV must contain row for id=1/title=A/state=TO_DO (allowing separators)");
        assertTrue(csvOut.matches("(?s).*3.*C.*COMPLETED.*"),
                "CSV must contain row for id=3/title=C/state=COMPLETED");

        // --- JSON ---
        JsonVisitor json = new JsonVisitor();
        records.forEach(json::visit);
        String jsonOut = extractOutput(json);
        assertTrue(jsonOut.contains("id"), "JSON must contain 'id'");
        assertTrue(jsonOut.contains("title"), "JSON must contain 'title'");
        assertTrue(Pattern.compile("IN_PROGRESS").matcher(jsonOut).find(),
                "JSON must contain 'IN_PROGRESS'");

        // --- Stats ---
        StatsVisitor stats = new StatsVisitor();
        records.forEach(stats::visit);
        String statsOut = extractOutput(stats);
        // accept formats like "TO_DO: 1" or "TO_DO = 1" etc.
        assertTrue(containsCount(statsOut, "TO_DO", 1), "Stats must show TO_DO: 1");
        assertTrue(containsCount(statsOut, "IN_PROGRESS", 1), "Stats must show IN_PROGRESS: 1");
        assertTrue(containsCount(statsOut, "COMPLETED", 1), "Stats must show COMPLETED: 1");
    }

    /* ---------- helpers ---------- */

    private static String extractOutput(Object visitor) {
        for (String name : new String[]{"result", "getResult", "getOutput"}) {
            try {
                Method m = visitor.getClass().getMethod(name);
                if (m.getReturnType() == String.class) {
                    return (String) m.invoke(visitor);
                }
            } catch (Exception ignore) { /* try next */ }
        }
        return visitor.toString();
    }

    private static boolean containsCount(String text, String token, int count) {
        String regex = "(?i)" + Pattern.quote(token) + "\\s*[:=]\\s*" + count;
        return Pattern.compile(regex).matcher(text).find();
    }
}
