package ua.restaurant.api;

import java.util.EnumSet;

import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import jakarta.servlet.DispatcherType;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import ua.restaurant.io.DataFormat;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.TableRepository;

public class ApiServer {

    private final Server server;
    private final ValidatorFactory validatorFactory;

    public ApiServer(int port, TableRepository tables, OrderRepository orders) {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        JsonSupport json = new JsonSupport(DataFormat.JSON.mapper(), validatorFactory.getValidator());

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addFilter(new FilterHolder(new LoggingFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(new FilterHolder(new ErrorHandlingFilter(json)), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(new ServletHolder(new TableServlet(tables, json)), "/api/tables/*");
        context.addServlet(new ServletHolder(new OrderServlet(orders, json)), "/api/orders/*");
        context.addServlet(new ServletHolder(new NotFoundServlet()), "/");

        server = new Server(port);
        server.setHandler(context);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        validatorFactory.close();
    }

    public int port() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}
