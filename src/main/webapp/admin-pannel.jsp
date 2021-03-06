<%@ page import="beans.User" %>
<%@ page import="sql.Sql" %>
<%@ page import="java.sql.ResultSet" %>

<%
	User u = (User) request.getSession().getAttribute("user");
	if(u == null) {
		// viteur non connecté, page interdite
		response.sendRedirect("/");
	}else if(!u.isAdmin()) {
		// utilisateur non administrateur, page interdite
		response.sendRedirect("/");
	}else{
%>
<!DOCTYPE html>
<html lang="fr">
<head>
	<meta charset="utf-8">
	<meta name="viewport"    content="width=device-width, initial-scale=1.0">
	<meta name="description" content="">
	<meta name="author"      content="Sergey Pozhilov (GetTemplate.com)">

	<title>Covid Mechant - Panneau Administrateur</title>

	<link rel="shortcut icon" href="assets-template/images/gt_favicon.png">

	<link rel="stylesheet" media="screen" href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,700">
	<link rel="stylesheet" href="assets-template/css/bootstrap.min.css">
	<link rel="stylesheet" href="assets-template/css/font-awesome.min.css">

	<!-- Custom styles for our template -->
	<link rel="stylesheet" href="assets-template/css/bootstrap-theme.css" media="screen" >
	<link rel="stylesheet" href="assets-template/css/main.css">

	<link rel="stylesheet" href="assets/notre-css.css">

	<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!--[if lt IE 9]>
    <script src="assets-template/js/html5shiv.js"></script>
    <script src="assets-template/js/respond.min.js"></script>
    <![endif]-->
</head>

<body>
	<!-- Fixed navbar -->
	<div class="navbar navbar-inverse navbar-fixed-top headroom" >
		<div class="container">
			<div class="navbar-header">
				<!-- Button for smallest screens -->
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse"><span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span> </button>
				<a class="navbar-brand" href="index.jsp">Covid Mechant</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav pull-right">
					<li><a href="index.jsp">Accueil</a></li>
					<li><a href="activites.jsp">Activites</a></li>
					<%
						if(u != null && u.isAdmin()) {
							out.println("<li class='active'><a href=/AdminPannelServlet>Panneau Administrateur</a></li>");
						}
						Sql sql = new Sql();
						ResultSet notifications = sql.doRequest("SELECT * FROM notification WHERE id_user_dst = " + u.getId() + " ORDER BY id_notification DESC");
						ResultSet nbNotifications = sql.doRequest("SELECT COUNT(*) AS total FROM notification WHERE id_user_dst = " + u.getId() + " ORDER BY id_notification DESC");
						while (nbNotifications.next()) {
							out.println("<li class='dropdown'>" +
									"<a href='#' class='dropdown-toggle' data-toggle='dropdown'>Notifications (" + nbNotifications.getInt("total") + ")<b class='caret'></b></a>" +
									"<ul class='dropdown-menu'>");
							while (notifications.next()) {
								out.println("<li><a href='#'> " + notifications.getString("message") + "</a></li>");
							}
							out.println("</ul>" +
									"</li>");
						}
					%>
					<li><a href="/profil.jsp">Profil</a></li>
					<li><a class="btn" href="/DeconnexionServlet">DECONNEXION</a></li>
				</ul>
			</div><!--/.nav-collapse -->
		</div>
	</div>
	<!-- /.navbar -->

	<header id="head" class="secondary"></header>

	<!-- container -->
	<div class="container">

		<ol class="breadcrumb">
			<li><a href="index.html">Accueil</a></li>
			<li class="active">Panneau Administrateur</li>
		</ol>

		<div class="row">

			<!-- Article main content -->
			<article class="col-sm-offset-1 col-sm-10 maincontent">

				<%
					if(request.getParameter("success") != null) {
						out.println("</br><div class=\"col-sm-12\">");
						out.println("<div class='alert alert-success' role='alert'>");
						out.println(request.getParameter("success"));
						out.println("</div>");
						out.println("</div>");
					}
				%>

				<header class="page-header">
					<h1 class="page-title">Comptes utilisateurs</h1>
				</header>

				<p>
					Voici la liste des comptes utilisateur existants.
				</p>
				<br>
				<table class="table">
					<thead>
						<tr>
							<th scope="col">ID</th>
							<th scope="col">Nom</th>
							<th scope="col">Prenom</th>
							<th scope="col">Mail</th>
							<th scope="col">Date de naissance</th>
							<th scope="col">Infecte ?</th>
							<th scope="col">Modification</th>
						</tr>
					</thead>
					<tbody>

					<%
						ResultSet results = sql.doRequest("SELECT * FROM user ORDER BY id_user DESC");
						while(results.next()) {
							String isInfected;
							if(results.getInt("is_infected") == 1) {
								isInfected = "Oui";
							}else{
								isInfected = "Non";
							}
							out.println("<tr>" +
									"<td>" + results.getInt("id_user") + "</td>" +
									"<td>" + results.getString("last_name") + "</td>" +
									"<td>" + results.getString("first_name") + "</td>" +
									"<td>" + results.getString("login") + "</td>" +
									"<td>" + results.getString("birthday") + "</td>" +
									"<td>" + isInfected + "</td>" +
									"<td><form method='Post' action='/UpdateUserServlet'><input type='hidden' name='login' value='" + results.getString("login") + "'/><button type='submit' class='btn btn-warning'>Modifier</button></form></td>" +
									"</tr>");
						}

					%>
					</tbody>
				</table>
				</br>

			</article>
			<!-- /Article -->

			<!-- Article main content -->
			<article class="col-sm-offset-1 col-sm-10 maincontent">
				<header class="page-header">
					<h1 class="page-title">Activites</h1>
				</header>

				<p>
					Voici la liste des activites existantes.
				</p>
				<br>
				<table class="table">
					<thead>
					<tr>
						<th scope="col">ID</th>
						<th scope="col">Nom</th>
						<th scope="col">Date</th>
						<th scope="col">Debut</th>
						<th scope="col">Fin</th>
						<th scope="col">Lieu</th>
						<th scope="col">Cree par</th>
						<th scope="col">Modification</th>
					</tr>
					</thead>
					<tbody>

					<%
						// on récupère toutes les activités
						ResultSet resultActivity = sql.doRequest("SELECT * FROM activity ORDER BY id_activity DESC");
						ResultSet resultUser;
						ResultSet resultPlace;
						while(resultActivity.next()) {
							// pour chaque activité, on récupère l'utilisateur et le lieu associé
							resultUser = sql.doRequest("SELECT * FROM user WHERE id_user=" + resultActivity.getInt("id_user"));
							resultPlace = sql.doRequest("SELECT * FROM place WHERE id_place=" + resultActivity.getInt("id_place"));
							while(resultUser.next()) {
								while(resultPlace.next()) {
									out.println("<tr>" +
											"<td>" + resultActivity.getInt("id_activity") + "</td>" +
											"<td>" + resultActivity.getString("name") + "</td>" +
											"<td>" + resultActivity.getString("date") + "</td>" +
											"<td>" + resultActivity.getString("start_time") + "</td>" +
											"<td>" + resultActivity.getString("end_time") + "</td>" +
											"<td>" + resultPlace.getString("name") + "</td>" +
											"<td>" + resultUser.getString("login") + "</td>" +
											"<td><a type='button' class='btn btn-warning' href='modifier-activite.jsp?activityToUpdate=" + resultActivity.getInt("id_activity") + "'>Modifier</a></td>" +
											"</tr>"
									);
								}
							}
						}

					%>
					</tbody>
				</table>
				</br>

			</article>
			<!-- /Article -->

			<!-- Article main content -->
			<article class="col-sm-offset-1 col-sm-10 maincontent">
				<header class="page-header">
					<h1 class="page-title">Lieux</h1>
				</header>

				<p>
					Voici la liste des lieux existants.
				</p>
				<br>
				<table class="table">
					<thead>
					<tr>
						<th scope="col">ID</th>
						<th scope="col">Nom</th>
						<th scope="col">Adresse</th>
						<th scope="col">Modification</th>
					</tr>
					</thead>
					<tbody>

					<%
						results = sql.doRequest("SELECT * FROM place ORDER BY id_place DESC");
						while(results.next()) {
							out.println("<tr>" +
									"<td>" + results.getInt("id_place") + "</td>" +
									"<td>" + results.getString("name") + "</td>" +
									"<td>" + results.getString("adress") + "</td>" +
									"<td><a type='button' class='btn btn-warning' href='update-place.jsp?placeToUpdate=" + results.getInt("id_place") + "'>Modifier</a></td>" +
									"</tr>");
						}

					%>
					</tbody>
				</table>
				</br>

			</article>
			<!-- /Article -->

		</div>
	</div>	<!-- /container -->

	</br>

	<!-- JavaScript libs are placed at the end of the document so the pages load faster -->
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
	<script src="assets-template/js/headroom.min.js"></script>
	<script src="assets-template/js/jQuery.headroom.min.js"></script>
	<script src="assets-template/js/template.js"></script>

	<!-- Google Maps -->
	<script src="https://maps.googleapis.com/maps/api/js?key=&amp;sensor=false&amp;extension=.js"></script>
	<script src="assets-template/js/google-map.js"></script>


</body>
</html>
<%
	}
%>