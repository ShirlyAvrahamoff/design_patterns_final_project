package il.ac.hit.tasksapp.model.visitor;

import il.ac.hit.tasksapp.model.record.TaskRecord;

/** Builds a CSV string. */
public class CsvVisitor implements TaskVisitor {
    private final StringBuilder sb = new StringBuilder("id,title,description,state\n");

    @Override
    public void visit(TaskRecord t) {
        sb.append(t.id()).append(',')
                .append(escape(t.title())).append(',')
                .append(escape(t.description())).append(',')
                .append(t.state()).append('\n');
    }

    /** Final CSV text. */
    public String result() { return sb.toString(); }

    private static String escape(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        return '"' + out + '"';
    }
}
