import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.Sql2oCategoryDao;
import dao.Sql2oTaskDao;
import dao.TaskDao;
import models.Category;
import models.Task;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import static spark.Spark.*;

public class App {
    public static void main(String[] args) { //type “psvm + tab” to autocreate this
        staticFileLocation("/public");
        String connectionString = "jdbc:h2:~/todolist.db;INIT=RUNSCRIPT from 'classpath:db/create.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        Sql2oTaskDao taskDao = new Sql2oTaskDao(sql2o);
        Sql2oCategoryDao categoryDao = new Sql2oCategoryDao(sql2o);


        //get: delete all tasks

        get("/tasks/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            taskDao.clearAllTasks();
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());


        //get: show new task form
        get("/tasks/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
//            int categoryId = Integer.parseInt(req.params("id"));
//            model.put("categoryId", categoryId);
            return new ModelAndView(model, "task-form.hbs");
        }, new HandlebarsTemplateEngine());

        //task: process new task form
        post("/tasks/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            String description = request.queryParams("description");
            int categoryId = Integer.parseInt(request.queryParams("categoryId"));
            Task newTask = new Task(description, categoryId ); //ignore the hardcoded categoryId
            taskDao.add(newTask);
            model.put("task", newTask);
            response.redirect("/");
            return null;
        });

        //get: show all categories and tasks
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Task> tasks = taskDao.getAll();
            List<Category> categories = categoryDao.getAll();
            model.put("tasks", tasks);
            model.put("categories", categories);

            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        //get: show an individual task
        get("categories/:categoryid/tasks/:id", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfTaskToFind = Integer.parseInt(req.params("id")); //pull id - must match route segment
            Task foundTask = taskDao.findById(idOfTaskToFind); //use it to find task
            model.put("task", foundTask); //add it to model for template to display
            return new ModelAndView(model, "task-detail.hbs"); //individual task page.
        }, new HandlebarsTemplateEngine());

        //get: show a form to update a task
        get("/tasks/:id/update", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfTaskToEdit = Integer.parseInt(req.params("id"));
            Task editTask = taskDao.findById(idOfTaskToEdit);
            model.put("editTask", editTask);
            return new ModelAndView(model, "task-form.hbs");
        }, new HandlebarsTemplateEngine());

        //task: process a form to update a task
        post("/tasks/:id/update", (req, res) -> { //URL to make new task on POST route
            Map<String, Object> model = new HashMap<>();
            String newContent = req.queryParams("description");
            int idOfTaskToEdit = Integer.parseInt(req.params("id"));
            Task editTask = taskDao.findById(idOfTaskToEdit);
            taskDao.update(idOfTaskToEdit, newContent);
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //get: delete an individual task
        get("/tasks/:id/delete", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfTaskToDelete = Integer.parseInt(req.params("id")); //pull id - must match route segment
            taskDao.deleteById(idOfTaskToDelete); //use it to find task
            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

        //Categories

        //get: show new category form
        get("/categories/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "category-form.hbs");
        }, new HandlebarsTemplateEngine());

        //task: process new category form
        post("/categories/new", (request, response) -> { //URL to make new task on POST route
            Map<String, Object> model = new HashMap<>();
            String name = request.queryParams("name");
            Category newCategory = new Category(name);
            categoryDao.add(newCategory);
            model.put("task", newCategory);
            response.redirect("/");
            return null;
        });

        //get: show an individual category and tasks it contains
        get("/categories/:categoryId", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfCategoryToFind = Integer.parseInt(req.params("categoryId")); //new

            List<Category> categories = categoryDao.getAll(); //refresh list of links for navbar.
            model.put("categories", categories);

            Category foundCategory = categoryDao.findById(idOfCategoryToFind);
            model.put("category", foundCategory);
            List<Task> allTasksByCategory = categoryDao.getAllTasksByCategory(idOfCategoryToFind);
            model.put("tasks", allTasksByCategory);

            return new ModelAndView(model, "category-detail.hbs"); //new
        }, new HandlebarsTemplateEngine());

        //get: show a form to update a category
        get("/categories/update", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            model.put("editCategory", true);

            List<Category> allCategories = categoryDao.getAll();
            model.put("categories", allCategories);

            return new ModelAndView(model, "category-form.hbs");
        }, new HandlebarsTemplateEngine());


        //post: process a form to update a category and tasks it contains
        post("/categories/update", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            int idOfCategoryToEdit = Integer.parseInt(req.queryParams("editCategoryId"));
            String newName = req.queryParams("newCategoryName");
            categoryDao.update(categoryDao.findById(idOfCategoryToEdit).getId(), newName);

            List<Category> categories = categoryDao.getAll(); //refresh list of links for navbar.
            model.put("categories", categories);

            return new ModelAndView(model, "success.hbs");
        }, new HandlebarsTemplateEngine());

    }
}
