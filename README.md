# Tasks Management Application

A **Java Swing application** built as part of the **Design Patterns course final project**.  
The project demonstrates how multiple design patterns can be applied in a real-world **Task Management System**, following the **MVVM architecture** with an embedded **DerbyDB** database.

---

## Features

- Create, edit, and delete tasks
- Mark tasks with states (`TO_DO`, `IN_PROGRESS`, `COMPLETED`)
- Apply simple and advanced filters with **AND/OR** combinator logic
- Undo/Redo functionality (command history)
- Export and reporting using the Visitor pattern
- Task persistence with embedded **Apache DerbyDB**
- Interactive Swing GUI with MVVM separation

---

## Screenshots

### Main Interface
![main screen.png](main%20screen.png)  
The main window allows managing tasks, filtering, sorting, and exporting.

---

### Advanced Filter
![advanved filter.png](advanved%20filter.png)  
An advanced filtering system where users can combine multiple conditions with **AND/OR** logic.

---

### Statistics
![state popup.png](state%20popup.png)  
A popup window displaying the total number of tasks and distribution by state.

---

## Implemented Design Patterns

This project demonstrates both **mandatory** and **additional** design patterns:

### Mandatory Patterns
1. **MVVM (Model–View–ViewModel)**
    - Strict separation of concerns between UI, business logic, and data.
    - The `ViewModel` mediates communication between the `Model` (tasks, DAO, DB) and the `View` (Swing components).

2. **DAO (Data Access Object)**
    - Encapsulates all database interactions with embedded **DerbyDB**.
    - Provides a clean interface for CRUD operations on tasks.

3. **Combinator Pattern**
    - Implements flexible filtering logic (e.g., “title contains” AND “state is” OR “ID in range”).
    - Makes filters composable and reusable.

4. **Visitor Pattern (with Records & Pattern Matching)**
    - Generates reports and statistics without modifying task classes.
    - Uses Java records and pattern matching to simplify traversal and data extraction.

---

### Additional Patterns
5. **Command Pattern**
    - Implements **Undo/Redo** for add, update, and delete task operations.

6. **Observer Pattern**
    - Ensures the **UI automatically updates** when the model changes.

7. **State Pattern**
    - Represents the lifecycle of a task (`TO_DO`, `IN_PROGRESS`, `COMPLETED`) as distinct states.

8. **Strategy Pattern**
    - Provides multiple sorting strategies (by ID, by title, by state).

9. **Singleton Pattern**
    - Ensures a single shared instance of DAO and managers across the application.

10. **Proxy Pattern**
    - Used to manage access and caching for database queries.

---

## Architecture & Technologies

- **Language**: Java 24+
- **UI Framework**: Swing
- **Database**: Apache Derby (embedded mode)
- **Architecture**: MVVM (Model–View–ViewModel)
