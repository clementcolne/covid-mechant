package servlets;

import sql.Sql;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CreerLieuServlet")
public class CreerLieuServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String adress = request.getParameter("adress");

        Sql sql = new Sql();
        if(!sql.containsPlace(name)) {
            // le lieu n'existait pas encore, on le crée
            sql.addPlace(name, adress);
            response.sendRedirect("creer-activite.jsp?success=Le lieu a ete ajoute a la liste des lieux avec succes.");
        }else {
            response.sendRedirect("creer-activite.jsp?error=Le lieu que vous souhaitez existe deja, veuillez le selectionner dans la liste des lieux.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
