package il.ac.hit.tasksapp.model.visitor;

import il.ac.hit.tasksapp.model.record.TaskRecord;

/** Builds a compact JSON array (no external libs). */
public class JsonVisitor implements TaskVisitor {
    private final StringBuilder sb = new StringBuilder("[");
    private boolean first = true;

    @Override
    public void visit(TaskRecord t) {
        if (!first) sb.append(',');
        first = false;
        sb.append("{\"id\":").append(t.id())
                .append(",\"title\":").append(q(t.title()))
                .append(",\"description\":").append(q(t.description()))
                .append(",\"state\":").append(q(String.valueOf(t.state())))
                .append('}');
    }

    /** Final JSON string. */
    public String result() {
        return sb.append(']').toString();
    }

    private static String q(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
