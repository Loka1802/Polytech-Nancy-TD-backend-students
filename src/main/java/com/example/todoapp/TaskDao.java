package com.example.todoapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);

    public TaskDao() {
        try {
            createTableIfNotExists();
            initializeTable();
        } catch (SQLException e) {
            log.error("Error while initializing table", e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = """
                        CREATE TABLE IF NOT EXISTS Tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        done BOOLEAN NOT NULL 
                    );
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void initializeTable() throws SQLException {
        if (!findAll().isEmpty()) {
            return;
        }
        save(new Task(null, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(null, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(null, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    private static Task buildTaskModel(ResultSet rs) throws SQLException {
        return new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getBoolean("done"));
    }

    /**
     * Persist {@link Task} model.
     *
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) throws SQLException {
        String sql = "INSERT INTO Tasks (title, description, done) VALUES (?, ?, ?) RETURNING id, title, description, done;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setBoolean(3, task.done());
            ResultSet rs = ps.executeQuery();

            return buildTaskModel(rs);
        } catch (SQLException e) {
            throw new RuntimeException("failed", e);
        }

    }

    /**
     * Retrieve {@link Task} model by id.
     *
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) throws SQLException {
        String sql = "SELECT id, title, description, done FROM Tasks WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildTaskModel(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Task retrieval failed", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all {@link Task} models.
     * @return list of tasks.
     */
    public List<Task> findAll() {
        String sql = "SELECT id, title, description, done FROM Tasks;";

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(buildTaskModel(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return tasks;
    }

    /**
     * Retrieve all {@link Task} models.
     * @return list of tasks to do only.
     */
    public List<Task> findTodoOnly() {
        String sql = """
        SELECT id, title, description, done
        FROM Tasks
        WHERE done = 0;
        """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(buildTaskModel(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }

    /**
     * Delete {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     */
    public void delete(int id) {
        String sql = "DELETE FROM Tasks WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update {@link Task} model.
     * @param task updated task.
     */
    public void update(Task task) {
        String sql = """
        UPDATE Tasks
        SET title = ?, description = ?, done = ?
        WHERE id = ?;
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.title());
            ps.setString(2, task.description());
            ps.setBoolean(3, task.done());
            ps.setInt(4, task.id());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}