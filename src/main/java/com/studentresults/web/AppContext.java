package com.studentresults.web;

import com.studentresults.database.Database;
import com.studentresults.repository.ResultRepository;
import com.studentresults.repository.UserRepository;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class AppContext implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        Database database = Database.production();
        database.initialize();
        event.getServletContext().setAttribute("users", new UserRepository(database));
        event.getServletContext().setAttribute("results", new ResultRepository(database));
    }
}
