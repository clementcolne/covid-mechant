package sql;

import beans.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Clément Colné
 */
public class Sql {

    static final String JDBC_DRIVER = Constants.DRIVER;
    static final String DB_URL = Constants.PATH;
    static final String USER = "root";
    static final String PASS = Constants.PASSWORD;

    /**
     * Effectue une connexion à la base de données
     * @return une connexion à la base de données
     */
    public Connection connect() {
        Connection connection = null;

        try{
            Class.forName(JDBC_DRIVER);
        }catch (ClassNotFoundException e) {
            System.out.println("Erreur, impossible de se connecter à la base de donnée.");
            e.printStackTrace();
            System.exit(99);
        }

        try{
            System.out.println("Connexion à la base de données ...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Conexion réussie !");
        } catch (SQLException throwables) {
            System.out.println("Erreur, impossible de se connecter à la base de donnée.");
            throwables.printStackTrace();
            System.exit(99);
        }

        return connection;
    }

    /**
     * Ajoute un utilisateur dans la base de données si celui-ci n'existe pas
     * @param nom nom de l'utilisateur
     * @param prenom prénom de l'utilisateur
     * @param mail mail de l'utilisateur (unique)
     * @param password mot de passe de l'utilisateur en clair
     * @param naissance date de laissance JJ/MM/YYYY de l'utilisateur
     * @return User l'utilisateur
     */
    public User addUser(String nom, String prenom, String mail, String password, String naissance) {
        Connection con = connect();

        // on vérifie si un utilisateur n'a pas déjà ce mail
        User u = getUser(mail);
        if(u != null) {
            // l'utilisateur existe déjà
            return null;
        }

        String rqString = "INSERT INTO User(login, password, last_name, first_name, birthday, is_admin, is_infected) VALUES(?, ?, ?, ?, ?, ?, ?);";
        
        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setString(1, mail);
            preparedStmt.setString(2, generateHashedPassword(password));
            preparedStmt.setString(3, nom);
            preparedStmt.setString(4, prenom);
            preparedStmt.setString(5, naissance);
            // par défaut, un utilisateur n'est ni un admin, ni infecté
            preparedStmt.setInt(6, 0);
            preparedStmt.setInt(7, 0);
            preparedStmt.execute();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }

        u = new User();
        u.setBirthday(naissance);
        u.setMail(mail);
        u.setNom(nom);
        u.setPrenom(prenom);
        return u;
    }

    /**
     * Retourne l'utilisateur correspondant au mail donné en paramètre si il existe
     * @param mail login de l'utilisateur
     * @return User l'utilisateur correspondant au mail en paramètre si il existe, null sinon
     */
    public User getUser(String mail) {
        User user = null;

        String rqString = "SELECT * FROM user WHERE login ='" + mail + "'";
        ResultSet res = doRequest(rqString);

        try{
            while (res.next()){
                user = new User();
                user.setMail(res.getString("login"));
                user.setPassword(res.getString("password"));
                user.setNom(res.getString("last_name"));
                user.setPrenom(res.getString("first_name"));
                user.setBirthday(res.getString("birthday"));
                user.setAdmin(res.getInt("is_admin"));
                user.setInfected(res.getInt("is_infected"));
                user.setId(res.getInt("id_user"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Retourne l'utilisateur qui match avec mail/password. Si l'utilisateur est inconnu, l'objet user avec tous les champs à null sont retournée.
     * Si l'utilisateur existe mais que les mots de passe ne concordent pas, alors seulement le champ mail de l'utilisateur est complété.
     * @param mail login de l'utilisateur
     * @param password mot de passe en clair de l'utilisateur
     * @return User l'utilisateur
     */
    public User connectUser(String mail, String password) {

        String rqString = "Select * from user where login ='" + mail + "'";
        ResultSet res = doRequest(rqString);
        User user = new User();

        try{
            while(res.next()) {
                if((res.getString("login") != null)) {
                    // l'utilisateur existe, on enregistre son login
                    user.setMail(res.getString("login"));
                    if(res.getString("password").equals(generateHashedPassword(password))) {
                        // l'utilisateur existe et les mots de passe concordent, on enregistre le reste des info de l'utilisateur
                        user.setPassword(res.getString("password"));
                        user.setNom(res.getString("first_name"));
                        user.setPrenom(res.getString("last_name"));
                        user.setBirthday(res.getString("birthday"));
                        user.setAdmin(res.getInt("is_admin"));
                        user.setInfected(res.getInt("is_infected"));
                        user.setId(res.getInt("id_user"));
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return user;
    }

    /**
     * METHODE COPIE COLLE D'ANCIEN CODE NON TESTEE
     */
    public ResultSet doRequest(String sql_string){
        ResultSet results = null;
        Connection con = connect();
        try {
            Statement stmt = con.createStatement();
            results = stmt.executeQuery(sql_string);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return results;
    }

    /**
     * Génère le hash du mot de passe en SHA-512 en paramètre
     * @param password mot de passe à hasher
     * @return mot de passe hashé
     */
    public String generateHashedPassword(String password) {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hash's bytes
            byte[] bytes = md.digest();
            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0 ; i< bytes.length ; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public User updateAccount(String nom, String prenom, String mail, String password, String naissance, String oldMail) {
        Connection con = connect();
        User u;

        // l'utilisateur veut changer de mail
        if(!mail.equals(oldMail)) {
            // on vérifie si un utilisateur n'a pas déjà ce mail
            u = getUser(mail);
            if (u != null) {
                // il existe un utilisateur qui a le mail demandé
                return null;
            }
        }

        if(password == null) {
            // un admin modifie le compte
            String rqString = "UPDATE User SET login = ?, last_name = ?, first_name = ?, birthday = ? WHERE login = ?;";

            try {
                PreparedStatement preparedStmt = con.prepareStatement(rqString);
                preparedStmt.setString(1, mail);
                preparedStmt.setString(2, nom);
                preparedStmt.setString(3, prenom);
                preparedStmt.setString(4, naissance);
                preparedStmt.setString(5, oldMail);
                preparedStmt.executeUpdate();

                con.close();
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ignored) {
                    }
                }
                e.printStackTrace();
                u = getUser(mail);
                // on notifie qu'un admin a modifié le compte
                sendNotification(u.getId(), getDate() + " : Un administrateur a mis à jour votre profil.");
            }
        }else{
            // un utilisateur modifie son propre compte
            String rqString = "UPDATE User SET login = ?, password = ?, last_name = ?, first_name = ?, birthday = ? WHERE login = ?;";

            try {
                PreparedStatement preparedStmt = con.prepareStatement(rqString);
                preparedStmt.setString(1, mail);
                preparedStmt.setString(2, generateHashedPassword(password));
                preparedStmt.setString(3, nom);
                preparedStmt.setString(4, prenom);
                preparedStmt.setString(5, naissance);
                preparedStmt.setString(6, oldMail);
                preparedStmt.executeUpdate();

                con.close();
            } catch (SQLException e) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ignored) {
                    }
                }
                e.printStackTrace();
            }
        }

        u = getUser(mail);

        return u;
    }

    public String getDate() {
        // on notifie qu'un admin a modifié le compte
        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy à HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public void addActivity(int idUser, String name, Date date, String startTime, String endTime, int idPlace) {
        Connection con = connect();

        String rqString = "INSERT INTO activity(date, start_time, end_time, id_place, id_user, name) VALUES(?, ?, ?, ?, ?, ?);";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setDate(1, date);
            preparedStmt.setString(2, startTime);
            preparedStmt.setString(3, endTime);
            preparedStmt.setInt(4, idPlace);
            preparedStmt.setInt(5, idUser);
            preparedStmt.setString(6, name);
            preparedStmt.execute();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

    public void updateActivity(int idActivity, String name, Date date, String startTime, String endTime, int idPlace, int idUser) {
        Connection con = connect();

        String rqString = "UPDATE activity SET date = ?, start_time = ?, end_time = ?, id_place = ?, name = ? WHERE id_activity = ?;";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setDate(1, date);
            preparedStmt.setString(2, startTime);
            preparedStmt.setString(3, endTime);
            preparedStmt.setInt(4, idPlace);
            preparedStmt.setString(5, name);
            preparedStmt.setInt(6, idActivity);
            preparedStmt.executeUpdate();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
        // on notifie l'utilisateur qui a créé l'activité qu'elle a été modifiée par un administrateur
        sendNotification(idUser, getDate() + " : Un administrateur a mis à jour votre activité \"" + name + "\"");
    }

    public void deleteActivity(int idActivity, int idUser, String name) {
        Connection con = connect();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM activity WHERE id_activity = " + idActivity);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(idUser != -1 && name != null) {
            // on notifie l'utilisateur que son activité a été supprimée par un administrateur
            sendNotification(idUser, "" + getDate() + " : Un administrateur a supprimé votre activité \"" + name + "\"");
        }
    }

    public void deleteUser(int idUser) {
        Connection con = connect();
        try {
            Statement stmt = con.createStatement();
            // on commence par supprimer toutes les activités liées à cet utilisateur
            stmt.execute("DELETE FROM activity WHERE id_user = " + idUser);
            // puis on supprime l'utilisateur
            stmt = con.createStatement();
            stmt.execute("DELETE FROM user WHERE id_user = " + idUser);
            // maintenant, on supprime toutes ses notifications liées
            stmt = con.createStatement();
            stmt.execute("DELETE FROM notification WHERE id_user_dst = " + idUser);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addPlace(String name, String adress) {
        Connection con = connect();

        String rqString = "INSERT INTO place(name, adress) VALUES(?, ?);";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, adress);
            preparedStmt.execute();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

    public void updatePlace(int idPlace, String name, String adress) {
        Connection con = connect();

        String rqString = "UPDATE place SET name = ?, adress = ? WHERE id_place = ?;";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, adress);
            preparedStmt.setInt(3, idPlace);
            preparedStmt.executeUpdate();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

    public void deletePlace(int idPlace) {
        Connection con = connect();
        try {
            Statement stmt = con.createStatement();
            stmt.execute("DELETE FROM place WHERE id_place = " + idPlace);
            stmt = con.createStatement();
            stmt.execute("DELETE FROM activity WHERE id_place = " + idPlace);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Retourne true si le lieu en paramètre est déjà présent dans la table place (à la case près), false sinon
     * @param place lieu à vérifier si il est déjà présent dans la base de données
     * @return true si le lieu en paramètre est déjà présent dans la table place (à la case près), false sinon
     */
    public boolean containsPlace(String place) {
        String placeToCompare;
        // on commence par récupérer tous les lieux existants
        ResultSet res = this.doRequest("SELECT * FROM place");

        try{
            while(res.next()) {
                placeToCompare = res.getString("name");
                if(place.equalsIgnoreCase(placeToCompare)) {
                    // il existe 2 lieux identiques à la case près, on retourne true
                    return true;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public void declarerPositif(int idUserPositif, String login) {
        System.out.println("Declarer cas positif");
        // on déclare déjà l'utilisateur comme positif dans sa table
        setPositive(idUserPositif);

        // liste pour retenir les utilisateurs ayant déjà reçu une notification
        List<Integer> notifiedUsers = new ArrayList<>();

        // on récupère la liste des activites de l'utilisateur positif
        ResultSet activitesUtilisateurPositif = doRequest("SELECT * FROM activity WHERE Date(date) >= DATE_SUB(Date(NOW()), INTERVAL 10 DAY) AND id_user = " + idUserPositif);

        // on parcours toutes les activités de l'utilisateur positif
        try {
            while (activitesUtilisateurPositif.next()) {
                // on récupère l'id de l'activite de l'utilisateur positif
                int idLieuUtilisateurPositif = activitesUtilisateurPositif.getInt("id_place");
                // on récupère la date de l'activité de l'utilisateur positif
                Date dateActiviteUtilisateurPositif = activitesUtilisateurPositif.getDate("date");
                // on récupère les heures de début et fin de l'activité de l'utilisateur positif
                String startTimeUtilisateurPositif = activitesUtilisateurPositif.getString("start_time");
                String endTimeUtilisateurPositif = activitesUtilisateurPositif.getString("end_time");
                // on récupère la liste des activites qui ont le même lieu et que l'activité date d'il y a moins de 10 jours
                ResultSet listActivites = doRequest("SELECT * FROM activity WHERE id_place = " + idLieuUtilisateurPositif);
                try {
                    while (listActivites.next()) {
                        // on envoie les notification à tous les utilisateurs cas contact
                        int userToNotify = listActivites.getInt("id_user");
                        Date dateActivite = listActivites.getDate("date");
                        String startTimeActivite = listActivites.getString("start_time");
                        String endTimeActivite = listActivites.getString("end_time");
                        if (idUserPositif != userToNotify) {
                            // on n'envoie pas la notification à l'utilisateur positif
                            if (sameDate(dateActiviteUtilisateurPositif, dateActivite)) {
                                // on envoie la notification si c'est le même jour
                                if (samePlageHoraire(startTimeUtilisateurPositif, endTimeUtilisateurPositif, startTimeActivite, endTimeActivite)) {
                                    // on regarde si les plages horaires correspondent
                                    if (!notifiedUsers.contains(userToNotify)) {
                                        // enfin, on vérifie que l'utilisateur n'a pas déjà été notifié pour éviter les doublons
                                        sendNotification(userToNotify, getDate() + " : Vous êtes cas contact à la Covid-19. Faite vous tester et confinez vous.");
                                        notifiedUsers.add(userToNotify);
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean samePlageHoraire(String startTimeUtilisateurPositif, String endTimeUtilisateurPositif, String startTimeActivite, String endTimeActivite) {
        int startTimeUtilisateurPositifInt = Integer.parseInt(startTimeUtilisateurPositif.replace(":", ""));
        int endTimeUtilisateurPositifInt = Integer.parseInt(endTimeUtilisateurPositif.replace(":", ""));
        int startTimeActiviteInt = Integer.parseInt(startTimeActivite.replace(":", ""));
        int endTimeActiviteInt = Integer.parseInt(endTimeActivite.replace(":", ""));

        if(startTimeActiviteInt >= startTimeUtilisateurPositifInt && startTimeActiviteInt <= endTimeUtilisateurPositifInt) {
            return true;
        }

        if(endTimeActiviteInt >= startTimeUtilisateurPositifInt && endTimeActiviteInt <= endTimeUtilisateurPositifInt) {
            return true;
        }

        return false;
    }

    public boolean sameDate(Date d1, Date d2) {
        return d1.equals(d2);
    }

    public void sendNotification(int idUserDst, String message) {
        Connection con = connect();

        String rqString = "INSERT INTO notification(id_user_dst, message) VALUES(?, ?);";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setInt(1, idUserDst);
            preparedStmt.setString(2, message);
            preparedStmt.execute();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

    public void setPositive(int idUser) {
        Connection con = connect();

        String rqString = "UPDATE user SET is_infected = ? WHERE id_user = ?;";

        try {
            PreparedStatement preparedStmt = con.prepareStatement(rqString);
            preparedStmt.setInt(1, 1);
            preparedStmt.setInt(2, idUser);
            preparedStmt.executeUpdate();

            con.close();
        }catch (SQLException e) {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

}
